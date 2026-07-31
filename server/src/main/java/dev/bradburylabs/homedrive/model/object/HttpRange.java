package dev.bradburylabs.homedrive.model.object;

public record HttpRange(long start, Long end) {
    public long totalBytes(long contentLength) {
        if (end == null) {
            return contentLength - start;
        }

        return end - start;
    }
}
