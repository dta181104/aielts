package com.example.shopmohinh.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public final class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    public static String listToJson(List<String> list) {
        if (list == null) return null;
        try {
            return MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to convert list to JSON", e);
        }
    }

    public static List<String> jsonToList(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse JSON to list", e);
        }
    }
}

