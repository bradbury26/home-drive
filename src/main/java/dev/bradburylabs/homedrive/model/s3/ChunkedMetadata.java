package dev.bradburylabs.homedrive.model.s3;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkedMetadata {
    private static final Pattern METADATA_PATTERN = Pattern.compile("^(.*);chunk-signature=(.*)$");

    private final String seedSignature;
    private final List<Chunk> chunks;

    public ChunkedMetadata(String seedSignature) {
        this.seedSignature = seedSignature;
        this.chunks = new ArrayList<>();
    }

    public void addMetadata(String metadata) {
        Matcher matcher = METADATA_PATTERN.matcher(metadata);

        if (!matcher.find()) {
            throw new RuntimeException();
        }

        int chunkSize = Integer.parseInt(matcher.group(1), 16);
        String chunkSignature = matcher.group(2);

        chunks.add(new Chunk(chunkSize, chunkSignature));
    }

    public int getCurrentChunkSize() {
        return chunks.getLast().chunkSize;
    }

    public String getCurrentChunkSignature() {
        return chunks.getLast().chunkSignature;
    }

    public String getPreviousChunkSignature() {
        if (chunks.size() < 2) {
            return seedSignature;
        }

        return chunks.get(chunks.size() - 2).chunkSignature;
    }

    private record Chunk(int chunkSize, String chunkSignature) {
    }
}
