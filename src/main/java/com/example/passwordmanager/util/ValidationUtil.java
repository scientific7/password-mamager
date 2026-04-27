package com.example.passwordmanager.util;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }

        boolean hasMinLength = password.length() >= AppConstants.MIN_PASSWORD_LENGTH;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

        return hasMinLength && hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}