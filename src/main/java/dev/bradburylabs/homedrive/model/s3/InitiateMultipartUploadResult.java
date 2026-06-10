package dev.bradburylabs.homedrive.model.s3;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InitiateMultipartUploadResult(@JsonProperty("Bucket") String bucket, @JsonProperty("Key") String key, @JsonProperty("UploadId") String uploadId) {
}
