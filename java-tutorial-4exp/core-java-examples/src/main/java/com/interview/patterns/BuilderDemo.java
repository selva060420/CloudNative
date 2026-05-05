package com.interview.patterns;

import java.util.Map;
import java.util.Objects;

/**
 * Builder pattern — complex object with optional fields + validation.
 */
public class BuilderDemo {

    static class ApiRequest {
        private final String url;           // required
        private final String method;        // required
        private final Map<String, String> headers;  // optional
        private final String body;          // optional
        private final int timeoutMs;        // optional (default 5000)
        private final int retries;          // optional (default 3)

        private ApiRequest(Builder b) {
            this.url = Objects.requireNonNull(b.url, "URL is required");
            this.method = Objects.requireNonNull(b.method, "Method is required");
            this.headers = b.headers;
            this.body = b.body;
            this.timeoutMs = b.timeoutMs;
            this.retries = b.retries;
        }

        public static Builder builder() { return new Builder(); }

        @Override
        public String toString() {
            return String.format("%s %s [timeout=%dms, retries=%d, headers=%s, body=%s]",
                method, url, timeoutMs, retries, headers, body != null ? body.substring(0, Math.min(20, body.length())) + "..." : "null");
        }

        static class Builder {
            private String url, method, body;
            private Map<String, String> headers = Map.of();
            private int timeoutMs = 5000;
            private int retries = 3;

            public Builder url(String u) { this.url = u; return this; }
            public Builder method(String m) { this.method = m; return this; }
            public Builder headers(Map<String, String> h) { this.headers = h; return this; }
            public Builder body(String b) { this.body = b; return this; }
            public Builder timeoutMs(int t) { this.timeoutMs = t; return this; }
            public Builder retries(int r) { this.retries = r; return this; }

            public ApiRequest build() { return new ApiRequest(this); }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Builder Pattern ===\n");

        // Minimal — only required fields
        ApiRequest simple = ApiRequest.builder()
            .url("https://nef.ericsson.com/api/subscriptions")
            .method("GET")
            .build();
        System.out.println("Simple:  " + simple);

        // Full — all optional fields
        ApiRequest full = ApiRequest.builder()
            .url("https://nef.ericsson.com/api/notifications")
            .method("POST")
            .headers(Map.of("Authorization", "Bearer token123", "Content-Type", "application/json"))
            .body("{\"event\":\"location_change\",\"msisdn\":\"+1234567890\"}")
            .timeoutMs(10000)
            .retries(5)
            .build();
        System.out.println("Full:    " + full);

        // Validation — missing required field
        try {
            ApiRequest.builder().method("GET").build(); // no URL
        } catch (NullPointerException e) {
            System.out.println("\nValidation caught: " + e.getMessage());
        }
    }
}
