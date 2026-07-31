package dev.bradburylabs.homedrive.model.object;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RetrieveObjectRequest {
    private final String userId;
    private final String key;
    private final HttpRange range;
}
