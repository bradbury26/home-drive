package dev.bradburylabs.homedrive.service;

import org.springframework.data.domain.Pageable;
import dev.bradburylabs.homedrive.model.object.GetObjectResponse;
import dev.bradburylabs.homedrive.model.object.ListObjectsResponse;

public interface ObjectService {
    GetObjectResponse getObject(String id, String userId, boolean retrieveInputStream);

    ListObjectsResponse listObjects(String userId, String parentId, String continuationToken, Pageable pageable);
}
