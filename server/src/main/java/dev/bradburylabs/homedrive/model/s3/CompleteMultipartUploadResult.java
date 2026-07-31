package dev.bradburylabs.homedrive.model.s3;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompleteMultipartUploadResult(@JsonProperty("Bucket") String bucket, @JsonProperty("Key") String key, @JsonProperty("ETag") String etag) {
}
