package dev.bradburylabs.homedrive.service;

import java.io.InputStream;
import dev.bradburylabs.homedrive.model.object.DeleteObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectRequest;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;

public interface ObjectStorageService<T extends StoreObjectRequest, D extends DeleteObjectRequest> {
    StoreObjectResponse storeSinglePartObject(T objectStorageRequest, InputStream inputStream);

    StoreObjectResponse storeChunkedSinglePartObject(T objectStorageRequest, InputStream inputStream, boolean trailers);

    void deleteObject(D objectDeleteRequest);

    String createObjectUpload(T request);

    StoreObjectResponse storeObjectUploadPart(String uploadId, int partNumber, T request, InputStream inputStream);

    StoreObjectResponse completeObjectUpload(String uploadId);
}
