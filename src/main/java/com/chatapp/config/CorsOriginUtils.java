package com.chatapp.config;

import java.util.Arrays;

/**
 * Utility for parsing and validating explicit CORS origin allowlists.
 */
public final class CorsOriginUtils {

    public static final String DEFAULT_ALLOWED_ORIGINS =
            "http://localhost:5000,http://127.0.0.1:5000,http://localhost:8080,http://127.0.0.1:8080";

    private CorsOriginUtils() {
    }

    /**
     * Parse a comma-separated allowlist, trimming whitespace and rejecting blank or wildcard '*' entries.
     */
    public static String[] parseAllowedOrigins(String rawOrigins) {
        String source = (rawOrigins == null || rawOrigins.isBlank()) ? DEFAULT_ALLOWED_ORIGINS : rawOrigins;
        return Arrays.stream(source.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty() && !"*".equals(origin))
                .distinct()
                .toArray(String[]::new);
    }
}
