package dev.bradburylabs.homedrive.model.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class StoreObjectRequest {
    private final String userId;
    private final String contentEncoding;
    private final String contentType;
    private final Checksum checksum;
}
