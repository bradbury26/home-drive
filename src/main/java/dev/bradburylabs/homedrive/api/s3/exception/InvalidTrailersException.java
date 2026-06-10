package dev.bradburylabs.homedrive.api.s3.exception;

public class InvalidTrailersException extends RuntimeException {
    public InvalidTrailersException(String message) {
        super(message);
    }

    public InvalidTrailersException(String message, Throwable cause) {
        super(message, cause);
    }
}
