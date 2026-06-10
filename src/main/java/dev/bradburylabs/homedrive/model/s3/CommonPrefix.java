package dev.bradburylabs.homedrive.model.s3;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommonPrefix(@JsonProperty("Prefix") String prefix) {
}
