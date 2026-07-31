package dev.bradburylabs.homedrive.model.s3;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Error(@JsonProperty("Code") String code, @JsonProperty("Key") String key, @JsonProperty("Message") String message) {
}
