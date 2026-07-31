package dev.bradburylabs.homedrive.model.s3;

public record ListBucketsRequest(Integer maxBuckets, String continuationToken, String prefix) {
}
