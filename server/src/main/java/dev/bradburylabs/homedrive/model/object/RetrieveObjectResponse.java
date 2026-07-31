package dev.bradburylabs.homedrive.model.object;

import java.io.InputStream;
import java.time.Instant;

public record RetrieveObjectResponse(String id, String userId, String objectName, String contentEncoding, String contentType, long contentLength,
        Checksum checksum, String etag, String version, Instant lastModified, InputStream inputStream) {
}
