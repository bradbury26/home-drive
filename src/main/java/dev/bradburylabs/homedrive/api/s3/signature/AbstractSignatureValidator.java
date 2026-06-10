package dev.bradburylabs.homedrive.api.s3.signature;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import dev.bradburylabs.homedrive.api.s3.security.S3AuthenticationDetails;
import dev.bradburylabs.homedrive.util.DateUtils;

public abstract class AbstractSignatureValidator<T> {
    private final S3AuthenticationDetails s3AuthenticationDetails;
    private final String secretAccessKey;

    protected AbstractSignatureValidator(S3AuthenticationDetails s3AuthenticationDetails, String secretAccessKey) {
        this.s3AuthenticationDetails = s3AuthenticationDetails;
        this.secretAccessKey = secretAccessKey;
    }

    public boolean validateSignature(T input, String hashedPayload) {
        String stringToSign = stringToSign(input, hashedPayload);
        byte[] signingKey = createSigningKey();

        String signature = Hex.encodeHexString(new HmacUtils(HmacAlgorithms.HMAC_SHA_256, signingKey).hmac(stringToSign));
        String expectedSignature = expectedSignature(input);

        return signature.equals(expectedSignature);
    }

    protected abstract String stringToSign(T input, String hashedPayload);

    protected abstract String expectedSignature(T input);

    protected S3AuthenticationDetails getS3AuthenticationDetails() {
        return s3AuthenticationDetails;
    }

    protected String getTimestamp() {
        return DateUtils.DATE_TIME_FORMAT.format(s3AuthenticationDetails.signingInstant());
    }

    protected String getDate() {
        return DateUtils.DATE_FORMAT.format(s3AuthenticationDetails.signingInstant());
    }

    protected String getScope() {
        String date = getDate();

        return "%s/%s/s3/aws4_request".formatted(date, s3AuthenticationDetails.region());
    }

    private byte[] createSigningKey() {
        byte[] dateKey = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, "AWS4" + secretAccessKey).hmac(getDate());
        byte[] dateRegionKey = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, dateKey).hmac(s3AuthenticationDetails.region());
        byte[] dateRegionServiceKey = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, dateRegionKey).hmac("s3");

        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, dateRegionServiceKey).hmac("aws4_request");
    }
}
