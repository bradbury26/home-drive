package dev.bradburylabs.homedrive.exception;

public class OutboxHandlingException extends RuntimeException {
    public OutboxHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
