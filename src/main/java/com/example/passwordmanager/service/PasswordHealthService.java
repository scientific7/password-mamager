package com.example.passwordmanager.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import com.example.passwordmanager.dto.PasswordHealthDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PasswordHealthService {

    private static final int OLD_PASSWORD_DAYS = 180;
    private static final List<String> COMMON_WORDS = List.of(
            "password", "admin", "qwerty", "letmein", "welcome", "123456", "iloveyou");

    private final String fingerprintPepper;

    public PasswordHealthService(@Value("${app.encryption.secret:default-secret-key}") String fingerprintPepper) {
        this.fingerprintPepper = fingerprintPepper;
    }

    public PasswordHealthDto analyze(String password, boolean reused, LocalDateTime updatedAt) {
        List<String> hints = new ArrayList<>();
        int score = 0;

        if (password == null || password.isBlank()) {
            return new PasswordHealthDto(0, "Weak", reused, isOld(updatedAt), List.of("Add a password"));
        }

        int length = password.length();
        if (length >= 16) {
            score += 40;
        } else if (length >= 12) {
            score += 30;
        } else if (length >= 8) {
            score += 18;
            hints.add("Use at least 12 characters");
        } else {
            score += 5;
            hints.add("Use at least 12 characters");
        }

        if (password.matches(".*[a-z].*")) {
            score += 12;
        } else {
            hints.add("Add lowercase letters");
        }

        if (password.matches(".*[A-Z].*")) {
            score += 12;
        } else {
            hints.add("Add uppercase letters");
        }

        if (password.matches(".*\\d.*")) {
            score += 12;
        } else {
            hints.add("Add a number");
        }

        if (password.matches(".*[^A-Za-z0-9].*")) {
            score += 16;
        } else {
            hints.add("Add a symbol");
        }

        if (containsCommonWord(password)) {
            score -= 25;
            hints.add("Avoid common words");
        }

        if (hasLongRepeat(password)) {
            score -= 15;
            hints.add("Avoid repeated characters");
        }

        if (reused) {
            score -= 25;
            hints.add("Use a unique password for this login");
        }

        boolean old = isOld(updatedAt);
        if (old) {
            score -= 10;
            hints.add("Rotate passwords older than 180 days");
        }

        score = Math.max(0, Math.min(100, score));
        String label = score >= 75 ? "Strong" : score >= 50 ? "Medium" : "Weak";

        if (hints.isEmpty()) {
            hints.add("This password looks healthy");
        }

        return new PasswordHealthDto(score, label, reused, old, hints);
    }

    public String fingerprint(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((fingerprintPepper + ":" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    public boolean isOld(LocalDateTime updatedAt) {
        return updatedAt != null && updatedAt.isBefore(LocalDateTime.now().minusDays(OLD_PASSWORD_DAYS));
    }

    private boolean containsCommonWord(String password) {
        String lowered = password.toLowerCase(Locale.ROOT);
        return COMMON_WORDS.stream().anyMatch(lowered::contains);
    }

    private boolean hasLongRepeat(String password) {
        for (int i = 0; i <= password.length() - 3; i++) {
            char current = password.charAt(i);
            if (password.charAt(i + 1) == current && password.charAt(i + 2) == current) {
                return true;
            }
        }
        return false;
    }
}
