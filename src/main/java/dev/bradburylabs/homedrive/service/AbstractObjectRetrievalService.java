package dev.bradburylabs.homedrive.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
        Path objectPath = versionDirectory.resolve("%s.dat".formatted(userObject.getId()));
        Path deleteMarkerPath = versionDirectory.resolve(".delete");

        if (deleteMarkerPath.toFile().exists() || !objectPath.toFile().exists()) {
            throw new UserObjectNotFoundException("Object not found for key: " + key);
        }

        InputStream inputStream = null;

        if (retrieveInputStream) {
            try {
                inputStream = Files.newInputStream(objectPath);
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
