package dev.bradburylabs.homedrive.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidChunkException;
import dev.bradburylabs.homedrive.exception.BadDigestException;
import dev.bradburylabs.homedrive.exception.ObjectReadException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.HttpRange;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.util.ChecksumType;
import dev.bradburylabs.homedrive.util.IdUtils;
import dev.bradburylabs.homedrive.util.LimitedInputStream;
import dev.bradburylabs.homedrive.util.MultiCheckedOutputStream;
import dev.bradburylabs.homedrive.util.ObjectFileDataEnumeration;
import dev.bradburylabs.homedrive.util.PathUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.checksums.SdkChecksum;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectStorageServiceImpl implements ObjectStorageService {
    private final HomeDriveProperties homeDriveProperties;

    @Override
    public InputStream retrieveObject(String objectName, String objectVersion, String userId) {
        return retrieveObject(objectName, objectVersion, userId, null, -1);
    }

    @Override
    public InputStream retrieveObject(String objectName, String objectVersion, String userId, HttpRange range, long contentLength) {
        Path dataDirectory = Path.of(homeDriveProperties.getDataLocation(), userId);
        Path storageDirectory = PathUtils.calculateStorageDirectory(objectName);
        Path versionDirectory = dataDirectory.resolve(storageDirectory).resolve(objectVersion);
        Path deleteMarkerPath = versionDirectory.resolve(".delete");

        File[] objectFiles = versionDirectory.toFile().listFiles();

        if (deleteMarkerPath.toFile().exists() || objectFiles == null || objectFiles.length == 0) {
            throw new UserObjectNotFoundException("Object not found: " + objectName);
        }

        try {
            Enumeration<InputStream> enumeration = new ObjectFileDataEnumeration(Arrays.asList(objectFiles).iterator());

            InputStream inputStream = new SequenceInputStream(enumeration);

            if (range != null) {
                inputStream.skipNBytes(range.start());
                inputStream = new LimitedInputStream(inputStream, range.totalBytes(contentLength));
            }

            return inputStream;

        } catch (IOException e) {
            throw new ObjectReadException("Unable to read object", e);
        }
    }

    @Override
    public StoreObjectResponse storeObject(String objectName, String userId, Checksum checksum, Consumer<OutputStream> outputStreamConsumer) {
        String objectVersion = IdUtils.generateId();

        Path dataDirectory = Path.of(homeDriveProperties.getDataLocation(), userId);
        Path storageDirectory = PathUtils.calculateStorageDirectory(objectName);
        Path versionDirectory = dataDirectory.resolve(storageDirectory).resolve(objectVersion);

        createDirectory(versionDirectory, true);

        StreamProcessResult streamProcessResult = handleSinglePartObject(checksum.checksumType(), versionDirectory, outputStreamConsumer);

        validateChecksum(checksum.checksum(), streamProcessResult.checksum());

        return new StoreObjectResponse(objectVersion, streamProcessResult.md5Checksum(), new Checksum(checksum.checksumType(), streamProcessResult.checksum()),
                streamProcessResult.contentLength());

        //        return storeSinglePartObject(id, userId, checksumType, outputStream -> {
        //            try {
        //                inputStream.transferTo(outputStream);
        //            } catch (IOException e) {
        //                throw new InvalidChunkException("Error reading object stream", e);
        //            }
        //        });
    }

    @Override
    public StoreObjectResponse storeObjectUploadPart(String uploadId, int partNumber, String objectName, String userId, Checksum checksum,
            Consumer<OutputStream> outputStreamConsumer) {
        Path dataDirectory = Path.of(homeDriveProperties.getDataLocation(), userId);
        Path storageDirectory = PathUtils.calculateStorageDirectory(objectName);
        Path uploadDirectory = dataDirectory.resolve(storageDirectory).resolve(uploadId);

        createDirectory(uploadDirectory, false);

        StreamProcessResult streamProcessResult = handleObjectPart(checksum.checksumType(), partNumber, uploadDirectory, outputStreamConsumer);

        validateChecksum(checksum.checksum(), streamProcessResult.checksum());

        return new StoreObjectResponse(uploadId, streamProcessResult.md5Checksum(), new Checksum(checksum.checksumType(), streamProcessResult.checksum()),
                streamProcessResult.contentLength());
    }

    private StreamProcessResult handleSinglePartObject(ChecksumType checksumType, Path versionDirectory, Consumer<OutputStream> outputStreamConsumer) {
        try {
            Path objectPath = versionDirectory.resolve("data.dat");

            return processStream(objectPath, checksumType, outputStreamConsumer);
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

    private StreamProcessResult handleObjectPart(ChecksumType checksumType, int partNumber, Path directory, Consumer<OutputStream> outputStreamConsumer) {
        Path objectPath = directory.resolve("data.part%d".formatted(partNumber));

        return processStream(objectPath, checksumType, outputStreamConsumer);
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



    private void validateChecksum(String checksum, String calculatedChecksum) {
        if (checksum != null && !checksum.equals(calculatedChecksum)) {
            throw new BadDigestException();
        }
    }

    private record StreamProcessResult(String md5Checksum, String checksum, long contentLength) {

    }
}
