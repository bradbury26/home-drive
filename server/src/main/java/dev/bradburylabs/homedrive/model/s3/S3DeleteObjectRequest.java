package dev.bradburylabs.homedrive.model.s3;

import dev.bradburylabs.homedrive.model.object.DeleteObjectRequest;
import lombok.Getter;

@Getter
public class S3DeleteObjectRequest extends DeleteObjectRequest {
    private final String ifMatch;

    public S3DeleteObjectRequest(String userId, String key, String ifMatch) {
        super(userId, key);

        this.ifMatch = ifMatch;
    }
}
