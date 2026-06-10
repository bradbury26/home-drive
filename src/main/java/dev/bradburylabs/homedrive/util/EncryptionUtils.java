package dev.bradburylabs.homedrive.util;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

public final class EncryptionUtils {
    private static final int PASSWORD_COMPONENT_SIZE = 2;
    private static final int MIN_SECRET_KEY_LENGTH = 8;

    private EncryptionUtils() {

    }

    public static String encrypt(String secretKey, String password) {
        if (password == null) {
            return null;
        }

        validateSecretKey(secretKey);

        String salt = KeyGenerators.string().generateKey();
        TextEncryptor textEncryptor = Encryptors.delux(secretKey, salt);
        String encrypted = textEncryptor.encrypt(password);

        return "%s:%s".formatted(salt, encrypted);
    }

    public static String decrypt(String secretKey, String encrypted) {
        if (encrypted == null) {
            return null;
        }

        validateSecretKey(secretKey);

        String[] components = encrypted.split(":");

        if (components.length != PASSWORD_COMPONENT_SIZE) {
            throw new IllegalArgumentException("Invalid encrypted password, no salt");
        }

        TextEncryptor textEncryptor = Encryptors.delux(secretKey, components[0]);

        return textEncryptor.decrypt(components[1]);
    }

    private static void validateSecretKey(String secretKey) {
        if (secretKey == null || secretKey.length() != MIN_SECRET_KEY_LENGTH) {
            throw new IllegalArgumentException("Secret key must be at least 8 characters");
        }
    }
}
