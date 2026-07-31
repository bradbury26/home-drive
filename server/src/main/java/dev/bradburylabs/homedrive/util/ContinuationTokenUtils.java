package dev.bradburylabs.homedrive.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContinuationTokenUtils {
    public static String createContinuationToken(Window<?> window) {
        if (!window.hasNext()) {
            return null;
        }

        KeysetScrollPosition nextScrollPosition = (KeysetScrollPosition) window.positionAt(window.size() - 1);
        Map<String, Object> keyMap = nextScrollPosition.getKeys();

        String key = keyMap.entrySet().stream().map(entry -> "%s=%s".formatted(entry.getKey(), entry.getValue())).collect(Collectors.joining(","));

        return Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));
    }

    public static ScrollPosition createScrollPosition(String continuationToken) {
        if (continuationToken == null) {
            return ScrollPosition.keyset();
        }

        String key = new String(Base64.getDecoder().decode(continuationToken), StandardCharsets.UTF_8);

        Map<String, Object> keyMap = Stream.of(key.split(",")).map(item -> item.split("=")).collect(Collectors.toMap(item -> item[0], item -> item[1]));

        return ScrollPosition.forward(keyMap);
    }
}
