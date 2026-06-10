package dev.bradburylabs.homedrive.api.s3.security;

import java.time.Instant;

public record S3AuthenticationDetails(String region, String signedHeaders, String signature, Instant signingInstant) {
}
