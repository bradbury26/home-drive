package dev.bradburylabs.homedrive.model.s3;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ObjectIdentifier(@JsonProperty("Key") String key, @JsonProperty("ETag") String etag) {
}
