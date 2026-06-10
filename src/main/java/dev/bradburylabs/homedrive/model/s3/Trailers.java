package dev.bradburylabs.homedrive.model.s3;

public record Trailers(String previousSignature, String checksumHeaderName, String expectedSignature) {
}
