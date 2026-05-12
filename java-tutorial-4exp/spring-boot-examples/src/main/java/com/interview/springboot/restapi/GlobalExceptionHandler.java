package com.interview.springboot.restapi;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler returning RFC 7807 (Problem Details) format.
 *
 * Demonstrates:
 * - Centralized error handling (no try-catch in controllers)
 * - Consistent error response structure
 * - Validation error details with field-level messages
 * - Never exposing stack traces to clients
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Validation errors (from @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
                .toList();

        Map<String, Object> body = problemDetails(
                "https://api.example.com/errors/validation",
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "One or more fields have invalid values"
        );
        body.put("errors", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    // Generic unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> body = problemDetails(
                "https://api.example.com/errors/internal",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private Map<String, Object> problemDetails(String type, String title, int status, String detail) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", type);
        body.put("title", title);
        body.put("status", status);
        body.put("detail", detail);
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
