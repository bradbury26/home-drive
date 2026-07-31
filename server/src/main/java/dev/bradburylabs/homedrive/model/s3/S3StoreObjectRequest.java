package dev.bradburylabs.homedrive.model.s3;

import dev.bradburylabs.homedrive.model.object.Checksum;
import dev.bradburylabs.homedrive.model.object.StoreObjectRequest;
import lombok.Getter;

@Getter
public class S3StoreObjectRequest extends StoreObjectRequest {
    private final String key;
    private final String ifMatch;
    private final boolean ifNoneMatch;
    private final String trailerHeader;

    public S3StoreObjectRequest(String userId, String contentEncoding, String contentType, Checksum checksum, String key, String ifMatch, boolean ifNoneMatch,
            String trailerHeader) {
        super(userId, contentEncoding, contentType, checksum);

        this.key = key;
        this.ifMatch = ifMatch;
        this.ifNoneMatch = ifNoneMatch;
        this.trailerHeader = trailerHeader;
    }
}
