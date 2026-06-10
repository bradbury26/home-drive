package dev.bradburylabs.homedrive.model.user;

public record CreateS3ApiTokenModel(boolean readPermission, boolean writePermission) {
}
