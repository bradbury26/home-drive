package dev.bradburylabs.homedrive.exception;

public class ObjectReadException extends RuntimeException {
    public ObjectReadException(String reason, Throwable cause) {
        super(reason, cause);
    }
}
