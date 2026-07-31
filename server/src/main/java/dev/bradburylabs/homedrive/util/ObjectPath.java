package dev.bradburylabs.homedrive.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ObjectPath {
    private final List<String> path;
    private final String objectName;

    public static ObjectPath fromObjectKey(String objectKey) {
        if (!objectKey.contains("/")) {
            return new ObjectPath(Collections.emptyList(), objectKey);
        }

        String[] components = objectKey.split("/");

        List<String> path = Arrays.stream(components).limit(components.length - 1).toList();
        String objectName = components[components.length - 1];

        return new ObjectPath(path, objectName);
    }
}
