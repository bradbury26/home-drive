package dev.bradburylabs.homedrive.model.object;

public record CreateObjectUploadModel(String userId, String objectKey, String contentEncoding, String contentType, Checksum checksum) {
}
