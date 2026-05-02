package com.interview.java8plus;

import java.util.concurrent.*;

/**
 * Demonstrates: CompletableFuture — async pipelines, allOf, timeout, error handling.
 */
public class CompletableFutureDemo {

    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    public static void main(String[] args) throws Exception {
        // Basic async + chaining
        CompletableFuture<String> future = CompletableFuture
            .supplyAsync(() -> fetchData("service-A"), executor)
            .thenApply(data -> data.toUpperCase())
            .thenApply(data -> "Processed: " + data);
        System.out.println(future.join());

        // Parallel calls with allOf
        System.out.println("\n--- Parallel calls ---");
        CompletableFuture<String> call1 = CompletableFuture.supplyAsync(() -> fetchData("user-service"), executor);
        CompletableFuture<String> call2 = CompletableFuture.supplyAsync(() -> fetchData("order-service"), executor);
        CompletableFuture<String> call3 = CompletableFuture.supplyAsync(() -> fetchData("pref-service"), executor);

        CompletableFuture.allOf(call1, call2, call3).join();
        System.out.println("All done: " + call1.join() + ", " + call2.join() + ", " + call3.join());

        // Error handling with exceptionally
        System.out.println("\n--- Error handling ---");
        CompletableFuture<String> failing = CompletableFuture
            .<String>supplyAsync(() -> { throw new RuntimeException("Service down!"); }, executor)
            .exceptionally(ex -> "Fallback: " + ex.getMessage());
        System.out.println(failing.join());

        // Timeout (Java 9+)
        System.out.println("\n--- Timeout ---");
        CompletableFuture<String> slow = CompletableFuture
            .supplyAsync(() -> { sleep(3000); return "slow result"; }, executor)
            .orTimeout(1, TimeUnit.SECONDS)
            .exceptionally(ex -> "Timed out: " + ex.getMessage());
        System.out.println(slow.join());

        // thenCombine — combine two independent results
        System.out.println("\n--- thenCombine ---");
        CompletableFuture<String> combined = CompletableFuture
            .supplyAsync(() -> fetchData("profile"), executor)
            .thenCombine(
                CompletableFuture.supplyAsync(() -> fetchData("settings"), executor),
                (profile, settings) -> profile + " + " + settings
            );
        System.out.println("Combined: " + combined.join());

        executor.shutdown();
    }

    private static String fetchData(String service) {
        sleep(500); // simulate network call
        return "data-from-" + service;
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
