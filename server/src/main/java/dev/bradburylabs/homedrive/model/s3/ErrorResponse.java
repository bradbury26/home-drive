package dev.bradburylabs.homedrive.model.s3;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("Error")
public record ErrorResponse(@JsonProperty("Code") String code, @JsonProperty("Message") String message, @JsonProperty("Resource") String resource,
        @JsonProperty("RequestId") String requestId) {
}
