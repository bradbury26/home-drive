package dev.bradburylabs.homedrive.api.s3.signature;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.util.Pair;
import dev.bradburylabs.homedrive.api.s3.security.S3AuthenticationDetails;
import jakarta.servlet.http.HttpServletRequest;
import software.amazon.awssdk.utils.http.SdkHttpUtils;

public class AuthorizationHeaderSignatureValidator extends AbstractSignatureValidator<HttpServletRequest> {
    public AuthorizationHeaderSignatureValidator(S3AuthenticationDetails s3AuthenticationDetails, String secretAccessKey) {
        super(s3AuthenticationDetails, secretAccessKey);
    }

    @Override
    protected String stringToSign(HttpServletRequest request, String hashedPayload) {
        String timestamp = getTimestamp();
        String scope = getScope();
        String canonicalRequestHash = DigestUtils.sha256Hex(createCanonicalRequest(request, hashedPayload));

        return "AWS4-HMAC-SHA256\n%s\n%s\n%s".formatted(timestamp, scope, canonicalRequestHash);
    }

    @Override
    protected String expectedSignature(HttpServletRequest request) {
        return getS3AuthenticationDetails().signature();
    }

    private String createCanonicalRequest(HttpServletRequest request, String hashedPayload) {
        URI uri = URI.create(request.getRequestURL().toString());

        String method = request.getMethod();
        String canonicalUri = SdkHttpUtils.urlEncodeIgnoreSlashes(uri.getPath());
        String canonicalQueryString = createCanonicalQueryString(request.getParameterMap());

        String signedHeaders = getS3AuthenticationDetails().signedHeaders();
        List<String> headers = List.of(signedHeaders.split(";"));

        String canonicalHeaders = headers.stream().map(header -> {
            String headerValue = request.getHeader(header);

            return "%s:%s\n".formatted(header.toLowerCase(), headerValue.trim());
        }).collect(Collectors.joining());

        return "%s\n%s\n%s\n%s\n%s\n%s".formatted(method, canonicalUri, canonicalQueryString, canonicalHeaders, signedHeaders, hashedPayload);
    }

    private String createCanonicalQueryString(Map<String, String[]> parameterMap) {
        if (parameterMap == null) {
            return "";
        }

        return parameterMap.entrySet().stream().flatMap(entry -> {
            String key = entry.getKey();
            String[] values = entry.getValue();

            return Stream.of(values).map(value -> Pair.of(SdkHttpUtils.urlEncode(key), SdkHttpUtils.urlEncode(value)));
        }).sorted(Comparator.comparing(Pair::getFirst)).map(pair -> "%s=%s".formatted(pair.getFirst(), pair.getSecond())).collect(Collectors.joining("&"));
    }
}
