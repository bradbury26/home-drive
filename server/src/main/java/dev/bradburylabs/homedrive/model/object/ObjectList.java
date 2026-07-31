package dev.bradburylabs.homedrive.model.object;

import java.util.List;
import dev.bradburylabs.homedrive.entity.AbstractUserObject;

public record ObjectList<T extends AbstractUserObject>(List<T> objects, String continuationToken) {
}
