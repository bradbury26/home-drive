package dev.bradburylabs.homedrive.exception;

public class UserObjectNotFoundException extends RuntimeException {
    public UserObjectNotFoundException(String reason) {
        super(reason);
    }
}
