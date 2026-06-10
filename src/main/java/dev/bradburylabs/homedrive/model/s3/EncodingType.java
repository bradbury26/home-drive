package dev.bradburylabs.homedrive.model.s3;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum EncodingType {
    URL("url");

    private final String name;

    public static EncodingType fromName(String name) {
        for (EncodingType encodingType : EncodingType.values()) {
            if (encodingType.getName().equals(name)) {
                return encodingType;
            }
        }

        return null;
    }
}
