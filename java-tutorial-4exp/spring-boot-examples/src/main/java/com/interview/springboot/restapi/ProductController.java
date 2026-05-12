package com.interview.springboot.restapi;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * REST controller demonstrating:
 * - Proper HTTP methods and status codes
 * - URI versioning (/api/v1/)
 * - Pagination (offset-based)
 * - Input validation
 * - HATEOAS-style links
 *
 * Run: Start Spring Boot app, then:
 *   GET    http://localhost:8080/api/v1/products
 *   GET    http://localhost:8080/api/v1/products?page=0&size=5
 *   POST   http://localhost:8080/api/v1/products
 *   PUT    http://localhost:8080/api/v1/products/1
 *   DELETE http://localhost:8080/api/v1/products/1
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final Map<Long, Product> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // --- GET all with pagination ---
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Product> all = new ArrayList<>(store.values());
        int start = page * size;
        int end = Math.min(start + size, all.size());
        List<Product> pageContent = (start < all.size()) ? all.subList(start, end) : List.of();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("content", pageContent);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", all.size());
        response.put("totalPages", (int) Math.ceil((double) all.size() / size));

        return ResponseEntity.ok(response);
    }

    // --- GET by ID ---
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Product product = store.get(id);
        if (product == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        // HATEOAS-style links
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", product);
        response.put("_links", Map.of(
                "self", "/api/v1/products/" + id,
                "all", "/api/v1/products"
        ));
        return ResponseEntity.ok(response); // 200
    }

    // --- POST (create) ---
    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody ProductRequest request) {
        long id = idGenerator.getAndIncrement();
        Product product = new Product(id, request.name(), request.price());
        store.put(id, product);
        URI location = URI.create("/api/v1/products/" + id);
        return ResponseEntity.created(location).body(product); // 201 + Location header
    }

    // --- PUT (full replace) ---
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id,
                                          @Valid @RequestBody ProductRequest request) {
        if (!store.containsKey(id)) {
            return ResponseEntity.notFound().build(); // 404
        }
        Product updated = new Product(id, request.name(), request.price());
        store.put(id, updated);
        return ResponseEntity.ok(updated); // 200
    }

    // --- DELETE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (store.remove(id) == null) {
            return ResponseEntity.notFound().build(); // 404
        }
        return ResponseEntity.noContent().build(); // 204
    }

    // --- DTOs ---
    record Product(Long id, String name, Double price) {}

    record ProductRequest(
            @NotBlank(message = "name must not be blank") String name,
            @Positive(message = "price must be positive") Double price) {}
}
