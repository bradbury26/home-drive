package dev.bradburylabs.homedrive.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import software.amazon.awssdk.checksums.SdkChecksum;

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
        return checksumTypes.stream().collect(Collectors.toMap(checksumType -> checksumType, ChecksumType::getChecksum));
    }
}
