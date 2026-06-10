package dev.bradburylabs.homedrive.service;

public interface UserObjectOutboxService {
    void createOutboxEntry(String userId, String key, String previousVersion, String currentVersion);
}
