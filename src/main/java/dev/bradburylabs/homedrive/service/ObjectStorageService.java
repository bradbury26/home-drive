package dev.bradburylabs.homedrive.service;

import java.io.InputStream;
import dev.bradburylabs.homedrive.model.object.DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;

public interface ObjectStorageService<T extends StoreObjectRequest, D extends DeleteObjectRequest> {
    StoreObjectResponse storeObject(T objectStorageRequest, InputStream inputStream);

    StoreObjectResponse storeObjectStream(T objectStorageRequest, InputStream inputStream, boolean trailers);

    void deleteObject(D objectDeleteRequest);
}
