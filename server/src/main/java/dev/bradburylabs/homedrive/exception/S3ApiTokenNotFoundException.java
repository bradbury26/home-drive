package dev.bradburylabs.homedrive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class S3ApiTokenNotFoundException extends ResponseStatusException {
    public S3ApiTokenNotFoundException(String reason) {
        super(HttpStatus.NOT_FOUND, reason);
    }
}
