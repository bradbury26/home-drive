package dev.bradburylabs.homedrive.model.object;

import dev.bradburylabs.homedrive.entity.ObjectType;
import dev.bradburylabs.homedrive.util.ChecksumType;

public record SaveObjectModel(String objectVersion, String userId, ObjectType objectType, String objectName, String etag, String contentEncoding,
        String contentType, long contentLength, ChecksumType checksumType, String checksum) {
}
