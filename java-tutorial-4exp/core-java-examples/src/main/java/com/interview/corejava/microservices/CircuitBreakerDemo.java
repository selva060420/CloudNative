package com.interview.corejava.microservices;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Demonstrates Circuit Breaker pattern using Resilience4j.
 * States: CLOSED → OPEN → HALF_OPEN → CLOSED
 *
 * Run: java CircuitBreakerDemo
 */
public class CircuitBreakerDemo {

    private static int callCount = 0;
    private static boolean serviceDown = true;

    public static void main(String[] args) throws InterruptedException {
        // Configure: open after 3 failures in 5 calls, wait 2s before half-open
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(60)           // 60% failure rate to open
                .minimumNumberOfCalls(5)            // need at least 5 calls to evaluate
                .slidingWindowSize(5)
                .waitDurationInOpenState(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();

        CircuitBreaker cb = CircuitBreakerRegistry.of(config).circuitBreaker("paymentService");

        // Listen to state transitions
        cb.getEventPublisher().onStateTransition(event ->
                System.out.println("  ⚡ STATE CHANGE: " + event.getStateTransition()));

        System.out.println("=== Circuit Breaker Demo ===\n");

        // Phase 1: Service is DOWN — circuit will open
        System.out.println("--- Phase 1: Service DOWN (failures accumulate) ---");
        for (int i = 1; i <= 6; i++) {
            callService(cb, i);
        }

        System.out.println("\nCircuit state: " + cb.getState()); // OPEN

        // Phase 2: Wait for open→half-open transition
        System.out.println("\n--- Phase 2: Waiting 2s for HALF_OPEN... ---");
        Thread.sleep(2500);

        // Phase 3: Service recovers — circuit will close
        serviceDown = false;
        System.out.println("\n--- Phase 3: Service RECOVERED (half-open → closed) ---");
        for (int i = 7; i <= 10; i++) {
            callService(cb, i);
        }

        System.out.println("\nFinal circuit state: " + cb.getState()); // CLOSED
    }

    private static void callService(CircuitBreaker cb, int attempt) {
        Supplier<String> decorated = CircuitBreaker.decorateSupplier(cb, () -> {
            callCount++;
            if (serviceDown) {
                throw new RuntimeException("Service unavailable");
            }
            return "OK";
        });

        try {
            String result = decorated.get();
            System.out.printf("  Call #%d: ✅ %s%n", attempt, result);
        } catch (Exception e) {
            System.out.printf("  Call #%d: ❌ %s%n", attempt, e.getMessage());
        }
    }
}
