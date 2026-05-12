package com.interview.microservices;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demonstrates REST API concepts WITHOUT any framework (pure Java).
 * Shows the same patterns as ProductController but framework-agnostic.
 *
 * Run: java RestApiPatternsDemo
 */
public class RestApiPatternsDemo {

    // --- In-memory store (simulates database) ---
    private static final Map<Long, Map<String, Object>> store = new ConcurrentHashMap<>();
    private static final AtomicLong idGen = new AtomicLong(1);

    public static void main(String[] args) {
        System.out.println("=== REST API Patterns Demo (No Framework) ===\n");

        // 1. POST — Create (201 Created + Location header)
        System.out.println("--- POST /products (Create) ---");
        var created = handlePost(Map.of("name", "Laptop", "price", 999.99), Map.of());
        System.out.println("  Status: 201 Created");
        System.out.println("  Location: " + created.get("location"));
        System.out.println("  Body: " + created.get("body"));

        handlePost(Map.of("name", "Mouse", "price", 29.99), Map.of());
        handlePost(Map.of("name", "Keyboard", "price", 79.99), Map.of());

        // 2. GET — Read (200 OK)
        System.out.println("\n--- GET /products/1 (Read) ---");
        var getResult = handleGet(1L, Map.of("Accept", "application/json"));
        System.out.println("  Status: " + getResult.get("status"));
        System.out.println("  Body: " + getResult.get("body"));
        System.out.println("  _links: " + getResult.get("_links"));
        System.out.println("  Response Headers: " + getResult.get("responseHeaders"));

        // 3. GET — Not Found (404)
        System.out.println("\n--- GET /products/99 (Not Found) ---");
        var notFound = handleGet(99L, Map.of());
        System.out.println("  Status: " + notFound.get("status"));
        System.out.println("  Body: " + notFound.get("body"));

        // 4. PUT — Update (200 OK, idempotent)
        System.out.println("\n--- PUT /products/1 (Update — idempotent) ---");
        var updated = handlePut(1L, Map.of("name", "Gaming Laptop", "price", 1499.99));
        System.out.println("  Status: " + updated.get("status"));
        System.out.println("  Body: " + updated.get("body"));

        // 5. DELETE — Remove (204 No Content)
        System.out.println("\n--- DELETE /products/1 (Delete) ---");
        var deleted = handleDelete(1L);
        System.out.println("  Status: " + deleted.get("status"));

        System.out.println("\n--- DELETE /products/1 again (idempotent — same end state) ---");
        var deleted2 = handleDelete(1L);
        System.out.println("  Status: " + deleted2.get("status"));

        // 6. Pagination
        System.out.println("\n--- GET /products?page=0&size=2 (Pagination) ---");
        var page = handleGetAll(0, 2);
        System.out.println("  " + page);

        // 7. Error handling (RFC 7807)
        System.out.println("\n--- POST /products (Validation Error) ---");
        var error = handlePost(Map.of("name", "", "price", -5.0), Map.of());
        System.out.println("  Status: 400 Bad Request");
        System.out.println("  Problem Details: " + error.get("body"));

        // 8. Idempotency-Key header (prevents duplicate POST)
        System.out.println("\n--- POST with Idempotency-Key (retry-safe) ---");
        Map<String, String> headers = Map.of("Idempotency-Key", "key-abc-123");
        var first = handlePost(Map.of("name", "Monitor", "price", 399.99), headers);
        System.out.println("  1st call: Status " + first.get("status") + " → " + first.get("body"));
        var retry = handlePost(Map.of("name", "Monitor", "price", 399.99), headers);
        System.out.println("  2nd call (same key): Status " + retry.get("status") + " → " + retry.get("body"));
        System.out.println("  ↑ Same response, no duplicate created!");

        // 9. X-Request-ID (correlation/tracing)
        System.out.println("\n--- GET with X-Request-ID (correlation) ---");
        var traced = handleGet(2L, Map.of("X-Request-ID", "trace-789-xyz"));
        System.out.println("  Request Header:  X-Request-ID: trace-789-xyz");
        System.out.println("  Response Header: X-Request-ID: " +
                ((Map<?,?>) traced.get("responseHeaders")).get("X-Request-ID"));
        System.out.println("  ↑ Server propagates correlation ID back");
    }

    // --- Simulated handlers (what a framework does behind the scenes) ---

    private static final Map<String, Map<String, Object>> idempotencyCache = new ConcurrentHashMap<>();

    static Map<String, Object> handlePost(Map<String, Object> body, Map<String, String> requestHeaders) {
        // Validation
        String name = (String) body.get("name");
        Double price = (Double) body.get("price");
        if (name == null || name.isBlank() || price == null || price <= 0) {
            return Map.of("status", 400, "body", Map.of(
                    "type", "https://api.example.com/errors/validation",
                    "title", "Validation Failed",
                    "status", 400,
                    "detail", "name must not be blank, price must be positive"
            ));
        }

        // Idempotency-Key: if same key seen before, return cached response
        String idempotencyKey = requestHeaders.get("Idempotency-Key");
        if (idempotencyKey != null && idempotencyCache.containsKey(idempotencyKey)) {
            Map<String, Object> cached = idempotencyCache.get(idempotencyKey);
            return Map.of("status", 200, "body", cached.get("body"),
                    "location", cached.get("location"));
        }

        long id = idGen.getAndIncrement();
        Map<String, Object> product = Map.of("id", id, "name", name, "price", price);
        store.put(id, product);

        Map<String, Object> result = Map.of(
                "status", 201,
                "location", "/products/" + id,
                "body", product
        );

        // Cache for idempotency
        if (idempotencyKey != null) {
            idempotencyCache.put(idempotencyKey, result);
        }
        return result;
    }

    static Map<String, Object> handleGet(Long id, Map<String, String> requestHeaders) {
        Map<String, Object> product = store.get(id);
        if (product == null) {
            return Map.of("status", 404, "body", Map.of(
                    "type", "https://api.example.com/errors/not-found",
                    "title", "Not Found",
                    "status", 404,
                    "detail", "Product " + id + " not found"
            ));
        }

        // Build response headers
        Map<String, String> responseHeaders = new LinkedHashMap<>();
        responseHeaders.put("Content-Type", "application/json");
        responseHeaders.put("X-RateLimit-Limit", "100");
        responseHeaders.put("X-RateLimit-Remaining", "97");
        // Propagate correlation ID (or generate one)
        String requestId = requestHeaders.getOrDefault("X-Request-ID", UUID.randomUUID().toString());
        responseHeaders.put("X-Request-ID", requestId);

        return Map.of(
                "status", 200,
                "body", product,
                "_links", Map.of("self", "/products/" + id, "all", "/products"),
                "responseHeaders", responseHeaders
        );
    }

    static Map<String, Object> handlePut(Long id, Map<String, Object> body) {
        if (!store.containsKey(id)) {
            return Map.of("status", 404, "body", "Not Found");
        }
        Map<String, Object> updated = new HashMap<>(body);
        updated.put("id", id);
        store.put(id, updated);
        return Map.of("status", 200, "body", updated);
    }

    static Map<String, Object> handleDelete(Long id) {
        if (store.remove(id) == null) {
            return Map.of("status", 404, "body", "Not Found");
        }
        return Map.of("status", 204, "body", "No Content");
    }

    static Map<String, Object> handleGetAll(int page, int size) {
        List<Map<String, Object>> all = new ArrayList<>(store.values());
        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<Map<String, Object>> content = (start < all.size()) ? all.subList(start, end) : List.of();
        return Map.of(
                "content", content,
                "page", page,
                "size", size,
                "totalElements", all.size(),
                "totalPages", (int) Math.ceil((double) all.size() / size)
        );
    }
}
