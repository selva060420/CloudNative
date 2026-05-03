package com.interview.multithreading;

import java.util.concurrent.*;

/**
 * Demonstrates CompletableFuture patterns for async microservice calls.
 * Pattern: fan-out parallel calls, combine results, handle errors.
 */
public class CompletableFutureDemo {

    private static final ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws Exception {
        // 1. Basic async chain
        System.out.println("=== Async Chain ===");
        CompletableFuture<String> result = CompletableFuture
            .supplyAsync(() -> fetchUser(1), pool)
            .thenApply(user -> user.toUpperCase())
            .thenApply(user -> "Processed: " + user);
        System.out.println("  " + result.get());

        // 2. Combine two independent calls
        System.out.println("\n=== Combine Two Calls ===");
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> fetchUser(1), pool);
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(() -> fetchOrder(1), pool);

        String combined = userFuture.thenCombine(orderFuture,
            (user, order) -> user + " | " + order).get();
        System.out.println("  " + combined);

        // 3. Fan-out: parallel calls to multiple services
        System.out.println("\n=== Fan-out (allOf) ===");
        CompletableFuture<?>[] futures = new CompletableFuture[3];
        for (int i = 0; i < 3; i++) {
            int svcId = i;
            futures[i] = CompletableFuture.supplyAsync(() -> callService(svcId), pool)
                .thenAccept(r -> System.out.println("  Service-" + svcId + ": " + r));
        }
        CompletableFuture.allOf(futures).get();

        // 4. Error handling
        System.out.println("\n=== Error Handling ===");
        String fallback = CompletableFuture
            .<String>supplyAsync(() -> { throw new RuntimeException("Service down"); }, pool)
            .exceptionally(ex -> "Fallback: " + ex.getMessage())
            .get();
        System.out.println("  " + fallback);

        // 5. Timeout (Java 9+)
        System.out.println("\n=== Timeout ===");
        String timedResult = CompletableFuture
            .supplyAsync(() -> { sleep(3000); return "slow"; }, pool)
            .orTimeout(1, TimeUnit.SECONDS)
            .exceptionally(ex -> "Timed out!")
            .get();
        System.out.println("  " + timedResult);

        pool.shutdown();
    }

    private static String fetchUser(int id) { sleep(200); return "User-" + id; }
    private static String fetchOrder(int id) { sleep(300); return "Order-" + id; }
    private static String callService(int id) { sleep(100 * (id + 1)); return "OK"; }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
