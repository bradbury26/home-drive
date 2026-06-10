package dev.bradburylabs.homedrive.model.user;

public record CreateUserModel(String username, String name, String email, String password, boolean adminUser) {
}
