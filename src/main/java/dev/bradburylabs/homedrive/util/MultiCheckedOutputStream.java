package dev.bradburylabs.homedrive.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import software.amazon.awssdk.checksums.SdkChecksum;
import software.amazon.awssdk.checksums.internal.Crc32Checksum;
import software.amazon.awssdk.checksums.internal.DigestAlgorithm;
import software.amazon.awssdk.checksums.internal.DigestAlgorithmChecksum;

public class MultiCheckedOutputStream extends FilterOutputStream {
    @Getter
    private long contentLength = 0;
    private final Map<ChecksumType, SdkChecksum> checksums;

    public MultiCheckedOutputStream(OutputStream out, Set<ChecksumType> checksumTypes) {
        super(out);

        this.checksums = createChecksumMap(checksumTypes);
    }

    public void write(int b) throws IOException {
        out.write(b);
        contentLength++;
        checksums.values().forEach(checksum -> checksum.update(b));
    }

    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        contentLength += len;
        checksums.values().forEach(checksum -> checksum.update(b, off, len));
    }

    public SdkChecksum getChecksum(ChecksumType checksumType) {
        return checksums.get(checksumType);
    }

    private Map<ChecksumType, SdkChecksum> createChecksumMap(Set<ChecksumType> checksumTypes) {
        return checksumTypes.stream().collect(Collectors.toMap(checksumType -> checksumType, checksumType -> switch (checksumType) {
            case CRC32 -> new Crc32Checksum();
            case MD5 -> new DigestAlgorithmChecksum(DigestAlgorithm.MD5);
            case SHA1 -> new DigestAlgorithmChecksum(DigestAlgorithm.SHA1);
            case SHA256 -> new DigestAlgorithmChecksum(DigestAlgorithm.SHA256);
            case SHA512 -> new DigestAlgorithmChecksum(DigestAlgorithm.SHA512);
        }));
    }
}
