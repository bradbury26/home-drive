package dev.bradburylabs.homedrive.util;

import java.math.BigInteger;
import java.util.Date;

public final class IdUtils {
    private IdUtils() {

    }

    public static String generateId() {
        long x = 2147483648l;

        return BigInteger.valueOf((long) Math.floor(Math.random() * x)).toString(36) + BigInteger.valueOf(
                Math.abs((long) Math.floor(Math.random() * x) ^ new Date().getTime())).toString(36);
    }
}
