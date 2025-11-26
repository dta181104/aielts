package com.example.shopmohinh.util;

import java.util.List;
import java.util.stream.Collectors;

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

    // Convert an incoming request value (either numeric string, number-like string or letter) to
    // the stored canonical letter representation ("A", "B", ...). Returns null for invalid input.
    public static String toLetter(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;
        Integer idx = parseCorrectOption(trimmed);
        if (idx != null) {
            if (idx < 0) return null;
            return String.valueOf((char) ('A' + idx));
        }
        if (trimmed.length() == 1) {
            char ch = Character.toUpperCase(trimmed.charAt(0));
            if (ch >= 'A' && ch <= 'Z') return String.valueOf(ch);
        }
        return null;
    }

    // Clean an option entry that might be sent as 'A": "4 PM' or 'A: 4 PM' or 'A" : "4 PM"' etc.
    // If there is a colon, take the text after the first colon; else return trimmed value.
    public static String parseOptionText(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return s;
        // remove surrounding quotes
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        // find first colon that separates key and value
        int colon = s.indexOf(":");
        if (colon >= 0) {
            String rhs = s.substring(colon + 1).trim();
            // remove possible leading quotes on rhs
            if ((rhs.startsWith("\"") && rhs.endsWith("\"")) || (rhs.startsWith("'") && rhs.endsWith("'"))) {
                rhs = rhs.substring(1, rhs.length() - 1).trim();
            }
            return rhs;
        }
        return s;
    }

    public static List<String> normalizeOptions(List<String> rawOptions) {
        if (rawOptions == null) return null;
        return rawOptions.stream()
                .map(QuestionOptionUtils::parseOptionText)
                .collect(Collectors.toList());
    }
}
