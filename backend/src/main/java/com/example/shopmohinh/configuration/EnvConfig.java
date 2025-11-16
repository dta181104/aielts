package com.example.shopmohinh.configuration;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    private static final Dotenv dotenv;

    static {
        Dotenv temp = null;
        try {
            // Chỉ load file .env nếu tồn tại
            temp = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            System.out.println("EnvConfig: .env file loaded");
        } catch (Exception e) {
            System.out.println("EnvConfig: Skipping .env loading, using environment variables.");
        }
        dotenv = temp;
    }

    public static String get(String key) {
        // Ưu tiên lấy từ System Environment (Docker)
        String value = System.getenv(key);
        if (value != null) return value;

        // Nếu không có thì lấy từ file .env (local dev)
        return dotenv != null ? dotenv.get(key) : null;
    }
}
