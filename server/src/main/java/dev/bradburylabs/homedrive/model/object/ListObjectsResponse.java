package dev.bradburylabs.homedrive.model.object;

import java.util.List;

public record ListObjectsResponse(List<ObjectModel> objects, String continuationToken) {
}
