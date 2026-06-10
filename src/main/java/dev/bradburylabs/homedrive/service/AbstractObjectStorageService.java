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
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.FileSystemUtils;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidChunkException;
import dev.bradburylabs.homedrive.entity.ObjectUpload;
import dev.bradburylabs.homedrive.entity.ObjectUploadPart;
import dev.bradburylabs.homedrive.entity.UploadStatus;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.exception.BadDigestException;
import dev.bradburylabs.homedrive.exception.MultiPartUserObjectNotFoundException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.ObjectUploadPartRepository;
import dev.bradburylabs.homedrive.repository.ObjectUploadRepository;
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
    private final ObjectUploadRepository objectUploadRepository;
    private final ObjectUploadPartRepository objectUploadPartRepository;
    private final UserObjectOutboxService userObjectOutboxService;
    private final HomeDriveProperties homeDriveProperties;
    private final TransactionTemplate transactionTemplate;

    @Override
    public StoreObjectResponse storeSinglePartObject(T objectStorageRequest, InputStream inputStream) {
        return storeSinglePartObject(objectStorageRequest, outputStream -> {
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

    @Override
    @Transactional
    public String createObjectUpload(T request) {
        Instant now = Instant.now();

        ObjectUpload objectUpload = objectUploadRepository.findByUserIdAndObjectKeyAndUploadStatus(request.getUserId(), request.getKey(), UploadStatus.STARTED)
                .orElseGet(() -> new ObjectUpload(IdUtils.generateId(), request.getUserId(), request.getKey(), now));

        objectUploadPartRepository.deleteAllByObjectUploadId(objectUpload.getId());

        objectUpload.update(request.getContentEncoding(), request.getContentType(), request.getChecksum().checksumType(), request.getChecksum().checksum(),
                now);

        objectUploadRepository.save(objectUpload);

        return objectUpload.getId();
    }

    @Override
    public StoreObjectResponse storeObjectUploadPart(String uploadId, int partNumber, T request, InputStream inputStream) {
        return storeUserObjectPart(uploadId, partNumber, request, outputStream -> {
            try {
                inputStream.transferTo(outputStream);
            } catch (IOException e) {
                throw new InvalidChunkException("Error reading object stream", e);
            }
        });
    }

    @Override
    public StoreObjectResponse completeObjectUpload(String uploadId) {
        ObjectUpload objectUpload = objectUploadRepository.findById(uploadId)
                .orElseThrow(() -> new MultiPartUserObjectNotFoundException("Multipart upload not found for uploadId: " + uploadId));

        List<ObjectUploadPart> parts = objectUploadPartRepository.findAllByObjectUploadIdOrderByPartNumber(uploadId);

        long contentLength = 0;
        byte[] etagBytes = new byte[0];
        byte[] checksumBytes = new byte[0];

        for (ObjectUploadPart part : parts) {
            contentLength += part.getContentLength();
            etagBytes = ArrayUtils.addAll(etagBytes, Base64.getDecoder().decode(part.getEtag()));

            if (part.getChecksum() != null) {
                checksumBytes = ArrayUtils.addAll(checksumBytes, Base64.getDecoder().decode(part.getChecksum()));
            }
        }

        String userId = objectUpload.getUserId();
        String objectKey = objectUpload.getObjectKey();
        String etag = Base64.getEncoder().encodeToString(etagBytes);
        String checksum = objectUpload.getChecksumType() != null ? Base64.getEncoder().encodeToString(checksumBytes) : null;
        long finalContentLength = contentLength;
        Instant now = Instant.now();

        UserObject updatedUserObject = transactionTemplate.execute(status -> {
            UserObject userObject = userObjectRepository.findByUserIdAndObjectKey(userId, objectKey)
                    .orElseGet(() -> new UserObject(IdUtils.generateId(), userId, objectKey, now));

            userObjectOutboxService.createOutboxEntry(userObject.getUserId(), userObject.getObjectKey(), userObject.getObjectVersion(), uploadId);

            userObject.updateObject(etag, objectUpload.getContentEncoding(), objectUpload.getContentType(), finalContentLength, objectUpload.getChecksumType(),
                    checksum, objectUpload.getId(), now);

            objectUpload.markAsCompleted();

            objectUploadRepository.save(objectUpload);

            return userObjectRepository.save(userObject);
        });

        //        postValidateUserObject(null, updatedUserObject, checksum);

        return new StoreObjectResponse(updatedUserObject.getId(), updatedUserObject.getEtag(),
                new Checksum(updatedUserObject.getChecksumType(), updatedUserObject.getChecksum()));
    }

    protected void preValidateUserObject(T objectStorageRequest, UserObject userObject) {
        // Do Nothing
    }

    protected void postValidateUserObject(T objectStorageRequest, UserObject userObject, String calculatedChecksum) {
        validateChecksum(objectStorageRequest, calculatedChecksum);
    }

    protected void postValidateUserObjectPart(T objectStorageRequest, ObjectUploadPart userObjectPart, String calculatedChecksum) {
        validateChecksum(objectStorageRequest, calculatedChecksum);
    }

    protected void validateDelete(D objectDeleteRequest, UserObject userObject) {
        // Do Nothing
    }

    private void createDirectory(Path directory, boolean deleteMarker) {
        try {
            Files.createDirectories(directory);

            if (deleteMarker) {
                Files.createFile(directory.resolve(".delete"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected StoreObjectResponse storeSinglePartObject(T objectStorageRequest, Consumer<OutputStream> outputStreamConsumer) {
        String key = objectStorageRequest.getKey();

        String objectVersion = IdUtils.generateId();

        Path dataDirectory = Path.of(homeDriveProperties.getDataLocation(), objectStorageRequest.getUserId());
        Path storageDirectory = PathUtils.calculateStorageDirectory(key);
        Path versionDirectory = dataDirectory.resolve(storageDirectory).resolve(objectVersion);

        createDirectory(versionDirectory, true);

        UserObject userObject = handleSinglePartObject(objectStorageRequest, objectVersion, versionDirectory, outputStreamConsumer);

        return new StoreObjectResponse(userObject.getId(), userObject.getEtag(), new Checksum(userObject.getChecksumType(), userObject.getChecksum()));
    }

    protected StoreObjectResponse storeUserObjectPart(String uploadId, int partNumber, T request, Consumer<OutputStream> outputStreamConsumer) {
        ObjectUpload objectUpload = objectUploadRepository.findById(uploadId)
                .orElseThrow(() -> new MultiPartUserObjectNotFoundException("Multipart upload not found for uploadId: " + uploadId));

        String id = objectUpload.getId();
        String key = objectUpload.getObjectKey();

        Path dataDirectory = Path.of(homeDriveProperties.getDataLocation(), request.getUserId());
        Path storageDirectory = PathUtils.calculateStorageDirectory(key);
        Path uploadDirectory = dataDirectory.resolve(storageDirectory).resolve(id);

        createDirectory(uploadDirectory, false);

        ObjectUploadPart objectUploadPart = handleObjectPart(objectUpload, partNumber, uploadDirectory, request, outputStreamConsumer);

        return new StoreObjectResponse(objectUpload.getId(), objectUploadPart.getEtag(),
                new Checksum(objectUpload.getChecksumType(), objectUploadPart.getChecksum()));
    }

    private UserObject handleSinglePartObject(T objectStorageRequest, String objectVersion, Path versionDirectory,
            Consumer<OutputStream> outputStreamConsumer) {
        try {
            Instant now = Instant.now();

            UserObject userObject = userObjectRepository.findByUserIdAndObjectKey(objectStorageRequest.getUserId(), objectStorageRequest.getKey())
                    .orElseGet(() -> new UserObject(IdUtils.generateId(), objectStorageRequest.getUserId(), objectStorageRequest.getKey(), now));

            preValidateUserObject(objectStorageRequest, userObject);

            Path objectPath = versionDirectory.resolve("%s.dat".formatted(userObject.getId()));
            Checksum checksum = objectStorageRequest.getChecksum();
            ChecksumType checksumType = checksum.checksumType();

            StreamProcessResult streamProcessResult = processStream(objectPath, checksumType, outputStreamConsumer);

            postValidateUserObject(objectStorageRequest, userObject, streamProcessResult.checksum());

            return transactionTemplate.execute(status -> {
                userObjectOutboxService.createOutboxEntry(userObject.getUserId(), userObject.getObjectKey(), userObject.getObjectVersion(), objectVersion);

                userObject.updateObject(streamProcessResult.md5Checksum(), objectStorageRequest.getContentEncoding(), objectStorageRequest.getContentType(),
                        streamProcessResult.contentLength(), checksumType, streamProcessResult.checksum(), objectVersion, now);

                return userObjectRepository.save(userObject);
            });
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

    protected ObjectUploadPart handleObjectPart(ObjectUpload objectUpload, int partNumber, Path directory, T request,
            Consumer<OutputStream> outputStreamConsumer) {
        Path objectPath = directory.resolve("%s.part%d".formatted(objectUpload.getId(), partNumber));
        Checksum checksum = request.getChecksum();
        ChecksumType checksumType = checksum.checksumType();

        StreamProcessResult streamProcessResult = processStream(objectPath, checksumType, outputStreamConsumer);

        return transactionTemplate.execute(status -> {
            Instant now = Instant.now();

            ObjectUploadPart userObjectPart = objectUploadPartRepository.findByObjectUploadIdAndPartNumber(objectUpload.getId(), partNumber)
                    .orElseGet(() -> new ObjectUploadPart(IdUtils.generateId(), objectUpload.getId(), partNumber, now));

            userObjectPart.update(streamProcessResult.md5Checksum(), streamProcessResult.contentLength(), streamProcessResult.checksum(), now);

            postValidateUserObjectPart(request, userObjectPart, streamProcessResult.checksum());

            return objectUploadPartRepository.save(userObjectPart);
        });
    }

    private StreamProcessResult processStream(Path objectPath, ChecksumType checksumType, Consumer<OutputStream> outputStreamConsumer) {
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

            return new StreamProcessResult(base64Md5, base64Checksum, contentLength);

        } catch (IOException e) {
            throw new InvalidChunkException("Error storing object in version directory", e);
        }
    }

    private void validateChecksum(T request, String calculatedChecksum) {
        String checksum = request.getChecksum().checksum();

        if (checksum != null && !checksum.equals(calculatedChecksum)) {
            throw new BadDigestException();
        }
    }

    private record StreamProcessResult(String md5Checksum, String checksum, long contentLength) {

    }
}
