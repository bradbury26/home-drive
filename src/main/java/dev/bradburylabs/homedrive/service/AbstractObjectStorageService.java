package dev.bradburylabs.homedrive.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.FileSystemUtils;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidChunkException;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.exception.BadDigestException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserObjectRepository;
import dev.bradburylabs.homedrive.util.ChecksumType;
import dev.bradburylabs.homedrive.util.IdUtils;
import dev.bradburylabs.homedrive.util.MultiCheckedOutputStream;
import dev.bradburylabs.homedrive.util.PathUtils;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.checksums.SdkChecksum;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public abstract class AbstractObjectStorageService<T extends StoreObjectRequest, D extends DeleteObjectRequest> implements ObjectStorageService<T, D> {
    private final UserObjectRepository userObjectRepository;
    private final UserObjectOutboxService userObjectOutboxService;
    private final HomeDriveProperties homeDriveProperties;
    private final TransactionTemplate transactionTemplate;

    @Override
    public StoreObjectResponse storeObject(T objectStorageRequest, InputStream inputStream) {
        return storeObject(objectStorageRequest, outputStream -> {
            try {
                inputStream.transferTo(outputStream);
            } catch (IOException e) {
                throw new InvalidChunkException("Error reading object stream", e);
            }
        });
    }

    @Override
    @Transactional
    public void deleteObject(D objectDeleteRequest) {
        String userId = objectDeleteRequest.getUserId();
        String key = objectDeleteRequest.getKey();

        UserObject userObject = userObjectRepository.findByUserIdAndObjectKey(userId, key)
                .orElseThrow(() -> new UserObjectNotFoundException("Object not found for key: " + key));

        validateDelete(objectDeleteRequest, userObject);

        userObjectOutboxService.createOutboxEntry(userId, key, userObject.getObjectVersion(), null);

        userObjectRepository.delete(userObject);
    }

    protected StoreObjectResponse storeObject(T objectStorageRequest, Consumer<OutputStream> outputStreamConsumer) {
        String key = objectStorageRequest.getKey();

        String objectVersion = IdUtils.generateId();

        Path dataDirectory = Path.of(homeDriveProperties.getDataLocation(), objectStorageRequest.getUserId());
        Path storageDirectory = PathUtils.calculateStorageDirectory(key);
        Path versionDirectory = dataDirectory.resolve(storageDirectory).resolve(objectVersion);

        createVersionDirectory(versionDirectory);

        UserObject userObject = handleObject(objectStorageRequest, objectVersion, versionDirectory, outputStreamConsumer);

        return new StoreObjectResponse(userObject.getId(), userObject.getEtag(), new Checksum(userObject.getChecksumType(), userObject.getChecksum()));
    }

    protected void preValidateUserObject(T objectStorageRequest, UserObject userObject) {
        // Do Nothing
    }

    protected void postValidateUserObject(T objectStorageRequest, UserObject userObject, String calculatedChecksum) {
        String checksum = objectStorageRequest.getChecksum().checksum();

        if (checksum != null && !checksum.equals(calculatedChecksum)) {
            throw new BadDigestException();
        }
    }

    protected void validateDelete(D objectDeleteRequest, UserObject userObject) {
        // Do Nothing
    }

    private void createVersionDirectory(Path versionDirectory) {
        try {
            Files.createDirectories(versionDirectory);
            Files.createFile(versionDirectory.resolve(".delete"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private UserObject handleObject(T objectStorageRequest, String objectVersion, Path versionDirectory, Consumer<OutputStream> outputStreamConsumer) {
        try {
            Instant now = Instant.now();

            UserObject userObject = userObjectRepository.findByUserIdAndObjectKey(objectStorageRequest.getUserId(), objectStorageRequest.getKey())
                    .orElseGet(() -> new UserObject(IdUtils.generateId(), objectStorageRequest.getUserId(), objectStorageRequest.getKey(), now));

            preValidateUserObject(objectStorageRequest, userObject);

            Path objectPath = versionDirectory.resolve("%s.dat".formatted(userObject.getId()));
            Checksum checksum = objectStorageRequest.getChecksum();
            ChecksumType checksumType = checksum.checksumType();
            Set<ChecksumType> checksumTypes = new HashSet<>();

            if (checksumType != null) {
                checksumTypes.add(checksumType);
            }

            checksumTypes.add(ChecksumType.MD5);

            try (MultiCheckedOutputStream outputStream = new MultiCheckedOutputStream(new FileOutputStream(objectPath.toFile()), checksumTypes)) {
                outputStreamConsumer.accept(outputStream);

                SdkChecksum md5Checksum = outputStream.getChecksum(ChecksumType.MD5);
                String base64Md5 = Base64.getEncoder().encodeToString(md5Checksum.getChecksumBytes());

                String base64Checksum;

                if (checksumType == null) {
                    base64Checksum = null;
                } else if (!checksumType.equals(ChecksumType.MD5)) {
                    SdkChecksum calculatedChecksum = outputStream.getChecksum(checksumType);
                    base64Checksum = Base64.getEncoder().encodeToString(calculatedChecksum.getChecksumBytes());
                } else {
                    base64Checksum = base64Md5;
                }

                long contentLength = outputStream.getContentLength();

                postValidateUserObject(objectStorageRequest, userObject, base64Checksum);

                return transactionTemplate.execute(status -> {
                    userObjectOutboxService.createOutboxEntry(userObject.getUserId(), userObject.getObjectKey(), userObject.getObjectVersion(), objectVersion);

                    userObject.updateObject(base64Md5, objectStorageRequest.getContentEncoding(), objectStorageRequest.getContentType(), contentLength,
                            checksumType, base64Checksum, objectVersion, now);

                    return userObjectRepository.save(userObject);
                });

            } catch (IOException e) {
                throw new InvalidChunkException("Error storing object in version directory", e);
            }
        } catch (Exception e) {
            try {
                FileSystemUtils.deleteRecursively(versionDirectory);
            } catch (IOException ioe) {
                // Just log for now, scheduled task will attempt to delete later
                log.error("Unable to delete version directory", ioe);
            }

            throw e;
        }
    }
}
