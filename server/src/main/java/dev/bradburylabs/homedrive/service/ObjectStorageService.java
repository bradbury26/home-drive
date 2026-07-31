package dev.bradburylabs.homedrive.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;
import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.HttpRange;
import dev.bradburylabs.homedrive.model.object.StoreObjectResponse;

public interface ObjectStorageService {
    InputStream retrieveObject(String objectName, String objectVersion, String userId);

    InputStream retrieveObject(String objectName, String objectVersion, String userId, HttpRange range, long contentLength);

    StoreObjectResponse storeObject(String objectName, String userId, Checksum checksum, Consumer<OutputStream> outputStreamConsumer);

    StoreObjectResponse storeObjectUploadPart(String uploadId, int partNumber, String objectName, String userId, Checksum checksum,
            Consumer<OutputStream> outputStreamConsumer);
}
