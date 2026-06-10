package dev.bradburylabs.homedrive.model.s3;

import java.time.Instant;
import dev.bradburylabs.homedrive.model.object.HttpRange;
import dev.bradburylabs.homedrive.model.object.RetrieveObjectRequest;
import lombok.Getter;

@Getter
public class S3RetrieveObjectRequest extends RetrieveObjectRequest {
    private final String ifMatch;
    private final String ifNoneMatch;
    private final Instant ifModifiedSince;
    private final Instant ifUnmodifiedSince;

    public S3RetrieveObjectRequest(String userId, String key, HttpRange range, String ifMatch, String ifNoneMatch, Instant ifModifiedSince,
            Instant ifUnmodifiedSince) {
        super(userId, key, range);

        this.ifMatch = ifMatch;
        this.ifNoneMatch = ifNoneMatch;
        this.ifModifiedSince = ifModifiedSince;
        this.ifUnmodifiedSince = ifUnmodifiedSince;
    }
}
