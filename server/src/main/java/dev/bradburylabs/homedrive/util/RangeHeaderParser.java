package dev.bradburylabs.homedrive.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dev.bradburylabs.homedrive.api.s3.exception.InvalidRequestException;
import dev.bradburylabs.homedrive.model.object.HttpRange;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RangeHeaderParser {
    private static final Pattern rangeHeaderPattern = Pattern.compile("^bytes=(\\d+)-(\\d+)?$");

    public static HttpRange parse(String rangeHeader) {
        if (rangeHeader == null) {
            return null;
        }

        Matcher matcher = rangeHeaderPattern.matcher(rangeHeader);

        if (!matcher.matches()) {
            throw new InvalidRequestException("Invalid range header");
        }

        String rangeStartString = matcher.group(1);
        String rangeEndString = matcher.group(2);

        return new HttpRange(Long.parseLong(rangeStartString), rangeEndString != null ? Long.parseLong(rangeEndString) : null);
    }
}
