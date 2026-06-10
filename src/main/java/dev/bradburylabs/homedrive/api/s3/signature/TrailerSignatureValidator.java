package dev.bradburylabs.homedrive.api.s3.signature;

import org.apache.commons.codec.digest.DigestUtils;
import dev.bradburylabs.homedrive.api.s3.security.S3AuthenticationDetails;
import dev.bradburylabs.homedrive.model.s3.Trailers;

public class TrailerSignatureValidator extends AbstractSignatureValidator<Trailers> {
    public TrailerSignatureValidator(S3AuthenticationDetails s3AuthenticationDetails, String secretAccessKey) {
        super(s3AuthenticationDetails, secretAccessKey);
    }

    @Override
    protected String stringToSign(Trailers input, String hashedPayload) {
        String timestamp = getTimestamp();
        String scope = getScope();
        String previousSignature = input.previousSignature();
        String hashedChecksum = DigestUtils.sha256Hex("%s:%s\n".formatted(input.checksumHeaderName(), hashedPayload));

        return "AWS4-HMAC-SHA256-TRAILER\n%s\n%s\n%s\n%s".formatted(timestamp, scope, previousSignature, hashedChecksum);
    }

    @Override
    protected String expectedSignature(Trailers input) {
        return input.expectedSignature();
    }
}
