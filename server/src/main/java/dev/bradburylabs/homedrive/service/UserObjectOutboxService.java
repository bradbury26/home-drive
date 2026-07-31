package dev.bradburylabs.homedrive.service;

public interface UserObjectOutboxService {
    void createOutboxEntry(String objectName, String userId, String previousVersion, String currentVersion);
}
