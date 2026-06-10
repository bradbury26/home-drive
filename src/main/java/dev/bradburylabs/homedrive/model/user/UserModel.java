package dev.bradburylabs.homedrive.model.user;

import java.time.Instant;

public record UserModel(String id, String username, String name, String email, Instant createdDate, Instant modifiedDate, boolean adminUser) {
}
