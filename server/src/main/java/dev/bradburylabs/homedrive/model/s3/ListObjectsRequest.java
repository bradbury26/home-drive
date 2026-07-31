package dev.bradburylabs.homedrive.model.s3;

public record ListObjectsRequest(String bucketName, String delimiter, String prefix, String marker, Integer maxKeys, EncodingType encodingType) {
}
