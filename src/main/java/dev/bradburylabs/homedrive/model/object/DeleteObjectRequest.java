package dev.bradburylabs.homedrive.model.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DeleteObjectRequest {
    private final String userId;
    private final String key;
}
