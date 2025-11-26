package com.example.shopmohinh.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.LinkedHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        // find first colon that separates key and value
        int colon = s.indexOf(":");
        String rhs = s;
        if (colon >= 0) {
            rhs = s.substring(colon + 1).trim();
        }
        // remove surrounding quotes (leading or trailing) repeatedly and also remove stray backslashes
        while (!rhs.isEmpty() && (rhs.charAt(0) == '\\' || rhs.charAt(0) == '"' || rhs.charAt(0) == '\'')) {
            if (rhs.charAt(0) == '\\') {
                // drop a single backslash and continue
                rhs = rhs.substring(1);
            } else {
                rhs = rhs.substring(1);
            }
            rhs = rhs.trim();
        }
        while (!rhs.isEmpty() && (rhs.charAt(rhs.length() - 1) == '\\' || rhs.charAt(rhs.length() - 1) == '"' || rhs.charAt(rhs.length() - 1) == '\'')) {
            if (rhs.charAt(rhs.length() - 1) == '\\') {
                rhs = rhs.substring(0, rhs.length() - 1);
            } else {
                rhs = rhs.substring(0, rhs.length() - 1);
            }
            rhs = rhs.trim();
        }
        return rhs;
    }

    public static List<String> normalizeOptions(List<String> rawOptions) {
        if (rawOptions == null) return null;
        return rawOptions.stream()
                .map(QuestionOptionUtils::parseOptionText)
                .collect(Collectors.toList());
    }

    // Convert provided raw options into a JSON string. If the raw options contain labeled entries
    // (e.g. 'A: 4 PM' or '"A": "4 PM"'), produce an object mapping {"A":"4 PM", ...}.
    // Otherwise produce a JSON array ["opt1","opt2",...].
    public static String optionsToJson(List<String> rawOptions) {
        if (rawOptions == null) return null;
        ObjectMapper mapper = new ObjectMapper();
        // try to detect labeled entries
        boolean anyLabeled = false;
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < rawOptions.size(); i++) {
            String raw = rawOptions.get(i);
            if (raw == null) raw = "";
            String s = raw.trim();
            int colon = s.indexOf(":");
            if (colon >= 0) {
                String keyPart = s.substring(0, colon).trim();
                // strip surrounding quotes on key
                if ((keyPart.startsWith("\"") && keyPart.endsWith("\"")) || (keyPart.startsWith("'") && keyPart.endsWith("'"))) {
                    keyPart = keyPart.substring(1, keyPart.length() - 1).trim();
                }
                // also remove stray starting/ending quotes
                if (keyPart.startsWith("\"" ) || keyPart.startsWith("'")) keyPart = keyPart.substring(1).trim();
                if (keyPart.endsWith("\"" ) || keyPart.endsWith("'")) keyPart = keyPart.substring(0, keyPart.length()-1).trim();

                if (keyPart.length() == 1 && Character.isLetter(keyPart.charAt(0))) {
                    String key = String.valueOf(Character.toUpperCase(keyPart.charAt(0)));
                    String value = parseOptionText(raw);
                    map.put(key, value == null ? "" : value);
                    anyLabeled = true;
                    continue;
                }
            }
            // not labeled, defer
            map.put(null, parseOptionText(raw));
        }

        try {
            if (anyLabeled) {
                // fill unlabeled entries with next letters
                char next = 'A';
                LinkedHashMap<String, String> finalMap = new LinkedHashMap<>();
                for (Map.Entry<String, String> e : map.entrySet()) {
                    if (e.getKey() != null) {
                        finalMap.put(e.getKey(), e.getValue());
                    } else {
                        // find next unused letter
                        while (finalMap.containsKey(String.valueOf(next))) next++;
                        finalMap.put(String.valueOf(next), e.getValue());
                        next++;
                    }
                }
                return mapper.writeValueAsString(finalMap);
            } else {
                // produce array of cleaned option texts
                List<String> arr = rawOptions.stream().map(QuestionOptionUtils::parseOptionText).collect(Collectors.toList());
                return mapper.writeValueAsString(arr);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to convert options to JSON", ex);
        }
    }

    // Parse stored options JSON (either array or object mapping) into a list of option texts.
    // If stored JSON is an object mapping (e.g. {"A":"opt1","B":"opt2"}), it returns the values
    // in key order A,B,C,...; if it's an array it returns the array elements.
    public static List<String> storedJsonToList(String json) {
        if (json == null || json.isBlank()) return List.of();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String trimmed = json.trim();
            if (trimmed.startsWith("{")) {
                // parse as linked map to preserve insertion order
                Map<String, String> map = mapper.readValue(trimmed, mapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, String.class));
                // sort keys alphabetically to ensure A,B,C order
                return map.entrySet().stream()
                        .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toList());
            } else {
                return mapper.readValue(trimmed, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
        } catch (Exception ex) {
            // fallback: naive parse for array-like strings
            String t = json.trim();
            if (t.length() >= 2 && t.startsWith("[") && t.endsWith("]")) {
                String inner = t.substring(1, t.length() - 1);
                if (inner.isBlank()) return List.of();
                return List.of(inner.split(","))
                        .stream().map(s -> s.trim()).map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                        .collect(Collectors.toList());
            }
            return List.of();
        }
    }
}
