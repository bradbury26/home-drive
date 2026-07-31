package dev.bradburylabs.homedrive.model.object;

public record CompleteObjectUploadModel(String uploadId, String etag, long contentLength, String checksum) {
}
