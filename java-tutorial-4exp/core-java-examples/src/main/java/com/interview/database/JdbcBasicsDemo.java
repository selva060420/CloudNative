package com.interview.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates JDBC fundamentals and SQL injection prevention.
 *
 * Key concepts:
 * - PreparedStatement vs Statement (parameterized queries prevent SQL injection)
 * - CRUD operations with proper resource management
 * - try-with-resources for auto-closing connections
 *
 * NOTE: Uses H2 in-memory DB — no external setup needed.
 * Run: java -cp h2.jar:. com.interview.database.JdbcBasicsDemo
 */
public class JdbcBasicsDemo {

    private static final String URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) throws Exception {
        System.out.println("=== JDBC Basics & SQL Injection Prevention ===\n");

        setupSchema();

        // CRUD operations
        insertProduct("Laptop", 999.99);
        insertProduct("Phone", 499.99);
        insertProduct("Tablet", 299.99);

        System.out.println("All products:");
        findAll().forEach(System.out::println);

        System.out.println("\nFind by name 'Laptop':");
        System.out.println(findByName("Laptop"));

        updatePrice("Laptop", 1099.99);
        System.out.println("\nAfter price update:");
        System.out.println(findByName("Laptop"));

        delete("Tablet");
        System.out.println("\nAfter delete:");
        findAll().forEach(System.out::println);

        // SQL Injection demo
        System.out.println("\n=== SQL Injection Prevention ===");
        demonstrateSqlInjection();
    }

    // --- Schema setup ---
    private static void setupSchema() throws SQLException {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE products (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    price DECIMAL(10,2) NOT NULL
                )
            """);
        }
    }

    // --- SAFE: PreparedStatement (parameterized) ---
    private static void insertProduct(String name, double price) throws SQLException {
        String sql = "INSERT INTO products (name, price) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);    // Parameter bound as DATA, never as SQL
            ps.setDouble(2, price);
            ps.executeUpdate();
        }
    }

    private static List<String> findAll() throws SQLException {
        List<String> results = new ArrayList<>();
        String sql = "SELECT id, name, price FROM products ORDER BY id";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add("  id=%d, name=%s, price=%.2f".formatted(
                        rs.getLong("id"), rs.getString("name"), rs.getDouble("price")));
            }
        }
        return results;
    }

    private static String findByName(String name) throws SQLException {
        String sql = "SELECT id, name, price FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "  id=%d, name=%s, price=%.2f".formatted(
                            rs.getLong("id"), rs.getString("name"), rs.getDouble("price"));
                }
            }
        }
        return "  Not found";
    }

    private static void updatePrice(String name, double newPrice) throws SQLException {
        String sql = "UPDATE products SET price = ? WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setString(2, name);
            int rows = ps.executeUpdate();
            System.out.println("  Updated " + rows + " row(s)");
        }
    }

    private static void delete(String name) throws SQLException {
        String sql = "DELETE FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            int rows = ps.executeUpdate();
            System.out.println("  Deleted " + rows + " row(s)");
        }
    }

    // --- SQL Injection demonstration ---
    private static void demonstrateSqlInjection() throws SQLException {
        String maliciousInput = "' OR '1'='1";

        // VULNERABLE (never do this in production!)
        System.out.println("\n[VULNERABLE] String concatenation with input: " + maliciousInput);
        String unsafeSql = "SELECT * FROM products WHERE name = '" + maliciousInput + "'";
        System.out.println("  SQL becomes: " + unsafeSql);
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(unsafeSql)) {
            int count = 0;
            while (rs.next()) count++;
            System.out.println("  Result: " + count + " rows returned (ALL rows leaked!)");
        }

        // SAFE (always use this)
        System.out.println("\n[SAFE] PreparedStatement with same input: " + maliciousInput);
        String safeSql = "SELECT * FROM products WHERE name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(safeSql)) {
            ps.setString(1, maliciousInput);  // Treated as literal string data
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) count++;
                System.out.println("  Result: " + count + " rows returned (correctly 0 — injection blocked!)");
            }
        }
    }
}
