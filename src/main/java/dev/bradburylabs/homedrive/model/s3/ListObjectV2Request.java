package dev.bradburylabs.homedrive.model.s3;

public record ListObjectV2Request(String bucketName, String delimiter, String prefix, String continuationToken, Integer maxKeys, String startAfter,
        EncodingType encodingType) {
}
