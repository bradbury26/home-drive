package dev.bradburylabs.homedrive.model.object;

import java.time.Instant;
import dev.bradburylabs.homedrive.entity.ObjectType;

public record ObjectModel(String id, String name, ObjectType objectType, String mediaType, long contentLength, Instant lastModified) {
}
