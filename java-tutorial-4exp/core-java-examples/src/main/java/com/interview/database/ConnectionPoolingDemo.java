package com.interview.database;

import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates connection pooling concepts without external dependencies.
 *
 * Key concepts:
 * - Why pooling: creating connections is expensive (~5-20ms each)
 * - Pool sizing formula: connections = (core_count * 2) + effective_spindle_count
 * - Pool exhaustion and timeout handling
 * - HikariCP configuration (shown in comments, simulated here)
 *
 * In production, use HikariCP (Spring Boot default):
 *   spring.datasource.hikari.maximum-pool-size=10
 *   spring.datasource.hikari.connection-timeout=5000
 *   spring.datasource.hikari.idle-timeout=300000
 */
public class ConnectionPoolingDemo {

    // Simple pool implementation to demonstrate the concept
    private final BlockingQueue<Connection> pool;
    private final String url;
    private final int maxSize;
    private final AtomicInteger created = new AtomicInteger(0);

    public ConnectionPoolingDemo(String url, int maxSize) throws SQLException {
        this.url = url;
        this.maxSize = maxSize;
        this.pool = new LinkedBlockingQueue<>(maxSize);
        // Pre-create connections
        for (int i = 0; i < maxSize; i++) {
            pool.offer(DriverManager.getConnection(url, "sa", ""));
            created.incrementAndGet();
        }
    }

    /** Borrow a connection from the pool (blocks if none available) */
    public Connection borrowConnection(long timeoutMs) throws InterruptedException, TimeoutException {
        Connection conn = pool.poll(timeoutMs, TimeUnit.MILLISECONDS);
        if (conn == null) {
            throw new TimeoutException("Connection pool exhausted! All " + maxSize + " connections in use.");
        }
        return conn;
    }

    /** Return a connection to the pool */
    public void returnConnection(Connection conn) {
        if (conn != null) {
            pool.offer(conn);
        }
    }

    public int getAvailableConnections() {
        return pool.size();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Connection Pooling Demo ===\n");

        String url = "jdbc:h2:mem:pooldb;DB_CLOSE_DELAY=-1";

        // Create pool with 3 connections
        ConnectionPoolingDemo pool = new ConnectionPoolingDemo(url, 3);
        setupTable(url);

        System.out.println("Pool size: 3 connections");
        System.out.println("Available: " + pool.getAvailableConnections());

        // Simulate concurrent requests
        System.out.println("\n--- Simulating 5 concurrent requests with pool size 3 ---");
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 1; i <= 5; i++) {
            final int requestId = i;
            executor.submit(() -> {
                try {
                    long start = System.currentTimeMillis();
                    Connection conn = pool.borrowConnection(2000); // 2s timeout
                    long waitTime = System.currentTimeMillis() - start;

                    System.out.printf("  Request %d: got connection (waited %dms, available: %d)%n",
                            requestId, waitTime, pool.getAvailableConnections());

                    // Simulate work
                    Thread.sleep(500);

                    pool.returnConnection(conn);
                    System.out.printf("  Request %d: returned connection (available: %d)%n",
                            requestId, pool.getAvailableConnections());
                } catch (TimeoutException e) {
                    System.out.printf("  Request %d: TIMEOUT — %s%n", requestId, e.getMessage());
                } catch (Exception e) {
                    System.out.printf("  Request %d: ERROR — %s%n", requestId, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // Pool exhaustion demo
        System.out.println("\n--- Pool Exhaustion Demo ---");
        Connection c1 = pool.borrowConnection(1000);
        Connection c2 = pool.borrowConnection(1000);
        Connection c3 = pool.borrowConnection(1000);
        System.out.println("Borrowed all 3 connections. Available: " + pool.getAvailableConnections());

        try {
            pool.borrowConnection(500); // Will timeout
        } catch (TimeoutException e) {
            System.out.println("Expected timeout: " + e.getMessage());
        }

        pool.returnConnection(c1);
        pool.returnConnection(c2);
        pool.returnConnection(c3);
        System.out.println("Returned all. Available: " + pool.getAvailableConnections());

        System.out.println("""
            
            === HikariCP Configuration (Spring Boot) ===
            spring.datasource.hikari.maximum-pool-size=10
            spring.datasource.hikari.minimum-idle=5
            spring.datasource.hikari.connection-timeout=5000
            spring.datasource.hikari.idle-timeout=300000
            spring.datasource.hikari.max-lifetime=1800000
            spring.datasource.hikari.leak-detection-threshold=60000
            """);
    }

    private static void setupTable(String url) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS test (id INT PRIMARY KEY, value VARCHAR(50))");
        }
    }
}
