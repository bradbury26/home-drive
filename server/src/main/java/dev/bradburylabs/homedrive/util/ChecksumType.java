package dev.bradburylabs.homedrive.util;

import software.amazon.awssdk.checksums.SdkChecksum;
import software.amazon.awssdk.checksums.internal.Crc32Checksum;
import software.amazon.awssdk.checksums.internal.DigestAlgorithm;
import software.amazon.awssdk.checksums.internal.DigestAlgorithmChecksum;


public enum ChecksumType {
    CRC32, MD5, SHA1, SHA256, SHA512;

    public final SdkChecksum getChecksum() {
        return switch (this) {
            case CRC32 -> new Crc32Checksum();
            case MD5 -> new DigestAlgorithmChecksum(DigestAlgorithm.MD5);
            case SHA1 -> new DigestAlgorithmChecksum(DigestAlgorithm.SHA1);
            case SHA256 -> new DigestAlgorithmChecksum(DigestAlgorithm.SHA256);
            case SHA512 -> new DigestAlgorithmChecksum(DigestAlgorithm.SHA512);
        };
    }

}
