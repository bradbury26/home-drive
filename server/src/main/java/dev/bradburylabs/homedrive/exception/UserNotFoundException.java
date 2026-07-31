package dev.bradburylabs.homedrive.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String reason) {
        super(reason);
    }
}
