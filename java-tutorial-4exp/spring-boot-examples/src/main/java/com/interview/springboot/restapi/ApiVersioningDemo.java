package com.interview.springboot.restapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Demonstrates API versioning strategies:
 * 1. URI path versioning: /api/v1/info, /api/v2/info
 * 2. Header-based versioning: Accept: application/vnd.api.v1+json
 *
 * URI path is most common in practice — simple, visible, cacheable.
 */
@RestController
public class ApiVersioningDemo {

    // --- Strategy 1: URI Path Versioning ---

    @GetMapping("/api/v1/info")
    public ResponseEntity<Map<String, Object>> infoV1() {
        return ResponseEntity.ok(Map.of(
                "version", "1.0",
                "name", "Product API",
                "deprecated", false
        ));
    }

    @GetMapping("/api/v2/info")
    public ResponseEntity<Map<String, Object>> infoV2() {
        return ResponseEntity.ok(Map.of(
                "version", "2.0",
                "name", "Product API",
                "features", Map.of("pagination", "cursor-based", "format", "HAL+JSON"),
                "deprecated", false
        ));
    }

    // --- Strategy 2: Header-based Versioning ---

    @GetMapping(value = "/api/info", headers = "X-API-Version=1")
    public ResponseEntity<Map<String, Object>> infoHeaderV1() {
        return ResponseEntity.ok(Map.of("version", "1.0", "source", "header-based"));
    }

    @GetMapping(value = "/api/info", headers = "X-API-Version=2")
    public ResponseEntity<Map<String, Object>> infoHeaderV2() {
        return ResponseEntity.ok(Map.of("version", "2.0", "source", "header-based",
                "features", "extended"));
    }
}
