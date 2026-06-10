package dev.bradburylabs.homedrive.api.s3.signature;

import org.apache.commons.codec.digest.DigestUtils;
import dev.bradburylabs.homedrive.api.s3.security.S3AuthenticationDetails;
import dev.bradburylabs.homedrive.model.s3.ChunkedMetadata;

public class ChunkSignatureValidator extends AbstractSignatureValidator<ChunkedMetadata> {
    private static final String EMPTY_HASH = DigestUtils.sha256Hex("");

    public ChunkSignatureValidator(S3AuthenticationDetails s3AuthenticationDetails, String secretAccessKey) {
        super(s3AuthenticationDetails, secretAccessKey);
    }

    @Override
    protected String stringToSign(ChunkedMetadata input, String hashedPayload) {
        String timestamp = getTimestamp();
        String scope = getScope();
        String previousSignature = input.getPreviousChunkSignature();

        return "AWS4-HMAC-SHA256-PAYLOAD\n%s\n%s\n%s\n%s\n%s".formatted(timestamp, scope, previousSignature, EMPTY_HASH, hashedPayload);
    }

    @Override
    protected String expectedSignature(ChunkedMetadata input) {
        return input.getCurrentChunkSignature();
    }
}
