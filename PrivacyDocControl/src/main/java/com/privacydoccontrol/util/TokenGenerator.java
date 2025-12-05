package com.privacydoccontrol.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;

public class TokenGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 6;
    private static final int TOKEN_EXPIRY_MINUTES = 15;

    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public static LocalDateTime calculateExpiryTime() {
        return LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES);
    }

    public static boolean isTokenExpired(LocalDateTime expiryTime) {
        return expiryTime != null && LocalDateTime.now().isAfter(expiryTime);
    }
}
