package dev.bradburylabs.homedrive.model.object;

import java.io.InputStream;

public record GetObjectResponse(ObjectModel objectModel, InputStream inputStream) {
}
