package com.trackerapp.tracker_app.util;

import java.util.Set;

/**
 * Centralized password-strength rules, applied consistently across
 * registration, profile password changes, and reset-via-email. Throws
 * IllegalArgumentException with a user-facing message on the first rule
 * violated; callers catch this and surface it as a form error.
 */
public final class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 64;

    // A small, illustrative denylist — not exhaustive. A production system
    // would check against a much larger breached-password list (e.g. the
    // "Have I Been Pwned" API); this is an honest scope limitation.
    private static final Set<String> COMMON_PASSWORDS = Set.of(
            "password", "password1", "password123", "12345678", "123456789",
            "qwerty123", "letmein123", "admin123", "welcome123", "iloveyou1"
    );

    private PasswordValidator() {
    }

    public static void validate(String password, String email, String name) {
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long.");
        }
        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password must not exceed " + MAX_LENGTH + " characters.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number.");
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>_\\-+=/\\\\\\[\\]~`;'].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character.");
        }
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            throw new IllegalArgumentException("This password is too common. Please choose a stronger one.");
        }
        if (email != null) {
            String localPart = email.split("@")[0].toLowerCase();
            if (localPart.length() >= 4 && password.toLowerCase().contains(localPart)) {
                throw new IllegalArgumentException("Password must not contain your email address.");
            }
        }
        if (name != null) {
            for (String part : name.toLowerCase().split("\\s+")) {
                if (part.length() >= 4 && password.toLowerCase().contains(part)) {
                    throw new IllegalArgumentException("Password must not contain your name.");
                }
            }
        }
    }
}