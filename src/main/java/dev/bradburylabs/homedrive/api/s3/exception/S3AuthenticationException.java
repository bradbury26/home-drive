package dev.bradburylabs.homedrive.api.s3.exception;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class S3AuthenticationException extends AuthenticationException {
    private final HttpStatus status;
    private final String code;

    public S3AuthenticationException(@Nullable String message, HttpStatus status, String code) {
        super(message);

        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
