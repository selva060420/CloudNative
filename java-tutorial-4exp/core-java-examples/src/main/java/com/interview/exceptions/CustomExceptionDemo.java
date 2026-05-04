package com.interview.exceptions;

import java.util.Map;

/**
 * Demo: Custom exception hierarchy for REST API — mirrors Spring Boot @ControllerAdvice pattern.
 */
public class CustomExceptionDemo {

    // --- Exception hierarchy ---
    static abstract class ApiException extends RuntimeException {
        private final int status;
        private final String errorCode;

        protected ApiException(String message, int status, String errorCode) {
            super(message);
            this.status = status;
            this.errorCode = errorCode;
        }
        int getStatus() { return status; }
        String getErrorCode() { return errorCode; }
    }

    static class ResourceNotFoundException extends ApiException {
        ResourceNotFoundException(String resource, Object id) {
            super(resource + " not found: " + id, 404, "NOT_FOUND");
        }
    }

    static class ValidationException extends ApiException {
        private final Map<String, String> fieldErrors;

        ValidationException(Map<String, String> fieldErrors) {
            super("Validation failed", 400, "VALIDATION_ERROR");
            this.fieldErrors = fieldErrors;
        }
        Map<String, String> getFieldErrors() { return fieldErrors; }
    }

    static class RateLimitException extends ApiException {
        // High-performance: no stack trace
        private static final RateLimitException INSTANCE = new RateLimitException();
        static RateLimitException instance() { return INSTANCE; }

        private RateLimitException() { super("Rate limit exceeded", 429, "RATE_LIMITED"); }

        @Override
        public synchronized Throwable fillInStackTrace() { return this; } // skip expensive stack walk
    }

    // --- Simulated controller advice ---
    static String handleException(ApiException e) {
        String response = String.format("HTTP %d | %s | %s", e.getStatus(), e.getErrorCode(), e.getMessage());
        if (e instanceof ValidationException ve) {
            response += " | fields: " + ve.getFieldErrors();
        }
        return response;
    }

    // --- Simulated service ---
    static String getUser(long id) {
        if (id <= 0) throw new ValidationException(Map.of("id", "must be positive"));
        if (id == 999) throw new ResourceNotFoundException("User", id);
        return "User-" + id;
    }

    public static void main(String[] args) {
        System.out.println("=== Custom Exception Hierarchy Demo ===\n");

        // 1. Validation error (400)
        try {
            getUser(-1);
        } catch (ApiException e) {
            System.out.println(handleException(e));
        }

        // 2. Not found (404)
        try {
            getUser(999);
        } catch (ApiException e) {
            System.out.println(handleException(e));
        }

        // 3. Rate limit — singleton, no stack trace cost
        try {
            throw RateLimitException.instance();
        } catch (ApiException e) {
            System.out.println(handleException(e));
            System.out.println("Stack trace length: " + e.getStackTrace().length + " (0 = optimized)");
        }

        // 4. Normal success
        System.out.println("\nSuccess: " + getUser(42));
    }
}
