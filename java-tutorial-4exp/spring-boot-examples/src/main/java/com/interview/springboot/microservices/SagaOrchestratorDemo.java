package com.interview.springboot.microservices;

import java.util.*;

/**
 * Demonstrates the Saga Orchestrator pattern for distributed transactions.
 * 
 * Scenario: Order placement requires Payment → Inventory → Shipping.
 * If any step fails, compensating actions execute in reverse.
 *
 * Run: java SagaOrchestratorDemo
 */
public class SagaOrchestratorDemo {

    // Simulated service states
    private static final Set<String> chargedPayments = new HashSet<>();
    private static final Set<String> reservedInventory = new HashSet<>();
    private static boolean inventoryAvailable = true;

    public static void main(String[] args) {
        System.out.println("=== Saga Orchestrator Demo ===\n");

        // Scenario 1: All steps succeed
        System.out.println("--- Scenario 1: Happy path ---");
        executeOrderSaga("ORD-001", 99.99);

        // Scenario 2: Inventory fails → compensate payment
        System.out.println("\n--- Scenario 2: Inventory fails → rollback ---");
        inventoryAvailable = false;
        executeOrderSaga("ORD-002", 49.99);

        System.out.println("\n--- Final State ---");
        System.out.println("Charged payments: " + chargedPayments);
        System.out.println("Reserved inventory: " + reservedInventory);
    }

    static void executeOrderSaga(String orderId, double amount) {
        List<String> completedSteps = new ArrayList<>();

        try {
            // Step 1: Charge payment
            System.out.println("  [1] Charging payment $" + amount);
            chargePayment(orderId, amount);
            completedSteps.add("PAYMENT");
            System.out.println("      ✅ Payment charged");

            // Step 2: Reserve inventory
            System.out.println("  [2] Reserving inventory");
            reserveInventory(orderId);
            completedSteps.add("INVENTORY");
            System.out.println("      ✅ Inventory reserved");

            // Step 3: Schedule shipping
            System.out.println("  [3] Scheduling shipping");
            scheduleShipping(orderId);
            completedSteps.add("SHIPPING");
            System.out.println("      ✅ Shipping scheduled");

            System.out.println("  🎉 Saga COMPLETED for " + orderId);

        } catch (Exception e) {
            System.out.println("      ❌ FAILED: " + e.getMessage());
            System.out.println("  ⚠️  Executing compensations...");
            compensate(orderId, completedSteps);
            System.out.println("  🔄 Saga ROLLED BACK for " + orderId);
        }
    }

    // --- Compensating actions (reverse order) ---
    private static void compensate(String orderId, List<String> completedSteps) {
        Collections.reverse(completedSteps);
        for (String step : completedSteps) {
            switch (step) {
                case "SHIPPING" -> {
                    System.out.println("      ↩️ Cancelling shipping");
                }
                case "INVENTORY" -> {
                    reservedInventory.remove(orderId);
                    System.out.println("      ↩️ Releasing inventory");
                }
                case "PAYMENT" -> {
                    chargedPayments.remove(orderId);
                    System.out.println("      ↩️ Refunding payment");
                }
            }
        }
    }

    // --- Simulated service calls ---
    private static void chargePayment(String orderId, double amount) {
        chargedPayments.add(orderId);
    }

    private static void reserveInventory(String orderId) {
        if (!inventoryAvailable) {
            throw new RuntimeException("Insufficient inventory");
        }
        reservedInventory.add(orderId);
    }

    private static void scheduleShipping(String orderId) {
        // Always succeeds in this demo
    }
}
