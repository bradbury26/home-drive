package dev.bradburylabs.homedrive.api.s3.security;

import static dev.bradburylabs.homedrive.util.S3Constants.X_AMZ_CONTENT_SHA256_HEADER;
import org.springframework.security.web.util.matcher.RequestMatcher;
import jakarta.servlet.http.HttpServletRequest;

public class S3RequestMatcher implements RequestMatcher {
    @Override
    public boolean matches(HttpServletRequest request) {
        return request.getHeader(X_AMZ_CONTENT_SHA256_HEADER) != null;
    }
}
