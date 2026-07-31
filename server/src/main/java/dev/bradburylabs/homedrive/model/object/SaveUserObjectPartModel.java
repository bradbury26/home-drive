package dev.bradburylabs.homedrive.model.object;

public record SaveUserObjectPartModel(String uploadId, int partNumber, String etag, long contentLength, String checksum) {
}
