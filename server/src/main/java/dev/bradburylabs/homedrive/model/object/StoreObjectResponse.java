package dev.bradburylabs.homedrive.model.object;

public record StoreObjectResponse(String objectVersion, String etag, Checksum checksum, long contentLength) {
}
