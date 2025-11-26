package com.example.shopmohinh.util;

public final class QuestionOptionUtils {
    private QuestionOptionUtils() {}

    public static Integer parseCorrectOption(String stored) {
        if (stored == null) {
            return null;
        }
        String trimmed = stored.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(trimmed);
        } catch (NumberFormatException ignored) {
            // fall through to letter decoding
        }
        if (trimmed.length() == 1) {
            char ch = Character.toUpperCase(trimmed.charAt(0));
            if (ch >= 'A' && ch <= 'Z') {
                return ch - 'A';
            }
        }
        return null;
    }

    public static String normalizeCorrectOption(String stored) {
        Integer idx = parseCorrectOption(stored);
        if (idx == null) {
            return null;
        }
        return String.valueOf((char) ('A' + idx));
    }
}
