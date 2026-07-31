package dev.bradburylabs.homedrive.model.object;

import dev.bradburylabs.homedrive.util.ChecksumType;

public record Checksum(ChecksumType checksumType, String checksum) {
    public static Checksum empty() {
        return new Checksum(null, null);
    }
}
