package dev.bradburylabs.homedrive.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import dev.bradburylabs.homedrive.entity.UserObject;
import dev.bradburylabs.homedrive.exception.ObjectReadException;
import dev.bradburylabs.homedrive.exception.UserObjectNotFoundException;
import dev.bradburylabs.homedrive.mapper.ObjectRetrievalResponseMapper;
import dev.bradburylabs.homedrive.model.object.HttpRange;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectRequest;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectResponse;
import dev.bradburylabs.homedrive.properties.HomeDriveProperties;
import dev.bradburylabs.homedrive.repository.UserObjectRepository;
import dev.bradburylabs.homedrive.util.LimitedInputStream;
import dev.bradburylabs.homedrive.util.PathUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractObjectRetrievalService<T extends RetrieveObjectRequest> implements ObjectRetrievalService<T> {
    private final UserObjectRepository userObjectRepository;
    private final HomeDriveProperties homeDriveProperties;
    private final ObjectRetrievalResponseMapper objectRetrievalResponseMapper;

    @Override
    public RetrieveObjectResponse retrieveObject(T objectRetrievalRequest, boolean retrieveInputStream) {
        String userId = objectRetrievalRequest.getUserId();
        String key = objectRetrievalRequest.getKey();

        UserObject userObject = userObjectRepository.findByUserIdAndObjectKey(userId, key)
                .orElseThrow(() -> new UserObjectNotFoundException("Object not found for key: " + key));

        preValidateUserObject(objectRetrievalRequest, userObject);

        Path dataDirectory = Path.of(homeDriveProperties.getDataLocation(), userId);
        Path storageDirectory = PathUtils.calculateStorageDirectory(key);
        Path versionDirectory = dataDirectory.resolve(storageDirectory).resolve(userObject.getObjectVersion());
        Path deleteMarkerPath = versionDirectory.resolve(".delete");

        File[] objectFiles = versionDirectory.toFile().listFiles((dir, name) -> !name.equals(".delete"));

        if (deleteMarkerPath.toFile().exists() || objectFiles == null || objectFiles.length == 0) {
            throw new UserObjectNotFoundException("Object not found for key: " + key);
        }

        InputStream inputStream = null;

        if (retrieveInputStream) {
            try {
                List<FileInputStream> fileInputStreams = Stream.of(objectFiles).map(objectFile -> {
                    try {
                        return new FileInputStream(objectFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList();

                inputStream = new SequenceInputStream(Collections.enumeration(fileInputStreams));
                HttpRange range = objectRetrievalRequest.getRange();

                if (range != null) {
                    inputStream.skipNBytes(range.start());
                    inputStream = new LimitedInputStream(inputStream, range.totalBytes(userObject.getContentLength()));
                }

            } catch (IOException e) {
                throw new ObjectReadException("Unable to read object", e);
            }
        }

        return objectRetrievalResponseMapper.map(userObject, inputStream);
    }

    protected void preValidateUserObject(T objectStorageRequest, UserObject userObject) {
        // Do Nothing
    }
}
