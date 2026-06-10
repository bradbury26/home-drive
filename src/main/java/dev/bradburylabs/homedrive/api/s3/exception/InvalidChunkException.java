package dev.bradburylabs.homedrive.api.s3.exception;

public class InvalidChunkException extends RuntimeException {
    public InvalidChunkException(String message, Throwable cause) {
        super(message, cause);
    }
}
