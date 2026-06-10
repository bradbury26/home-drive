package dev.bradburylabs.homedrive.util;

import java.util.List;

public final class S3Constants {
    public static final String X_AMZ_DATE_HEADER = "x-amz-date";
    public static final String X_AMZ_CONTENT_SHA256_HEADER = "x-amz-content-sha256";
    public static final String X_AMZ_REQUEST_ID_HEADER = "x-amz-request-id";
    public static final String X_AMZ_TRAILER_HEADER = "x-amz-trailer";
    public static final String X_AMZ_CHECKSUM_ALGORITHM_HEADER = "x-amz-checksum-algorithm";
    public static final String CONTENT_MD5_HEADER = "Content-MD5";

    public static final String CONTENT_TYPE_AWS_CHUNKED = "aws-chunked";

    public static final String X_AMZ_CHECKSUM_CRC32_HEADER = "x-amz-checksum-crc32";
    public static final String X_AMZ_CHECKSUM_MD5_HEADER = "x-amz-checksum-md5";
    public static final String X_AMZ_CHECKSUM_SHA1_HEADER = "x-amz-checksum-sha1";
    public static final String X_AMZ_CHECKSUM_SHA256_HEADER = "x-amz-checksum-sha256";
    public static final String X_AMZ_CHECKSUM_SHA512_HEADER = "x-amz-checksum-sha512";

    public static final List<String> CHECKSUM_HEADERS =
            List.of(X_AMZ_CHECKSUM_CRC32_HEADER, X_AMZ_CHECKSUM_MD5_HEADER, X_AMZ_CHECKSUM_SHA1_HEADER, X_AMZ_CHECKSUM_SHA256_HEADER,
                    X_AMZ_CHECKSUM_SHA512_HEADER);

    public static final String RESPONSE_CONTENT_ENCODING_PARAMETER = "response-content-encoding";
    public static final String RESPONSE_CONTENT_TYPE_PARAMETER = "response-content-type";

    public static final String REQUEST_ID = "requestId";

    private S3Constants() {}
}
