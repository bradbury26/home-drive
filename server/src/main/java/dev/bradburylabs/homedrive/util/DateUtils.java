package dev.bradburylabs.homedrive.util;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateUtils {
    public static final String API_DATE_TIME_FORMAT_VALUE = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX").withZone(ZoneId.from(ZoneOffset.UTC));
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC));
    public static final DateTimeFormatter HEADER_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z").withZone(ZoneId.of("GMT"));

    private DateUtils() {

    }
}
