package dev.bradburylabs.homedrive.util;

import java.nio.file.Path;
import org.apache.commons.codec.digest.DigestUtils;

public final class PathUtils {
    private PathUtils() {

    }

    public static Path calculateStorageDirectory(String key) {
        String keyHash = DigestUtils.md5Hex(key);
        String firstDirectory = keyHash.substring(0, 2);
        String secondDirectory = keyHash.substring(2, 4);

        return Path.of(firstDirectory, secondDirectory);
    }
}
