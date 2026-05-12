package com.interview.corejava.microservices;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Demonstrates event-driven communication between microservices
 * using an in-memory event bus (simulates Kafka/RabbitMQ pattern).
 *
 * Pattern: Services publish domain events; other services subscribe and react.
 * This decouples services — publisher doesn't know about subscribers.
 *
 * Run: java EventDrivenDemo
 */
public class EventDrivenDemo {

    // Simple in-memory event bus (in production: Kafka, RabbitMQ)
    static class EventBus {
        private final Map<String, List<Consumer<Map<String, Object>>>> subscribers = new ConcurrentHashMap<>();
        private final ExecutorService executor = Executors.newFixedThreadPool(4);

        void subscribe(String eventType, Consumer<Map<String, Object>> handler) {
            subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(handler);
        }

        void publish(String eventType, Map<String, Object> event) {
            System.out.printf("  📤 Published: %s %s%n", eventType, event);
            List<Consumer<Map<String, Object>>> handlers = subscribers.getOrDefault(eventType, List.of());
            for (Consumer<Map<String, Object>> handler : handlers) {
                executor.submit(() -> handler.accept(event));
            }
        }

        void shutdown() throws InterruptedException {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Event-Driven Microservices Demo ===\n");

        EventBus bus = new EventBus();

        // --- Subscribe: Notification Service listens for OrderCreated ---
        bus.subscribe("OrderCreated", event -> {
            System.out.printf("  📧 [NotificationService] Sending confirmation email for order %s%n",
                    event.get("orderId"));
        });

        // --- Subscribe: Inventory Service listens for OrderCreated ---
        bus.subscribe("OrderCreated", event -> {
            System.out.printf("  📦 [InventoryService] Reserving stock for order %s%n",
                    event.get("orderId"));
            // After reserving, publish its own event
            bus.publish("InventoryReserved", Map.of(
                    "orderId", event.get("orderId"),
                    "warehouse", "WH-EAST"
            ));
        });

        // --- Subscribe: Shipping Service listens for InventoryReserved ---
        bus.subscribe("InventoryReserved", event -> {
            System.out.printf("  🚚 [ShippingService] Scheduling shipment from %s for order %s%n",
                    event.get("warehouse"), event.get("orderId"));
        });

        // --- Publish: Order Service creates an order ---
        System.out.println("--- Order Service creates order ---");
        bus.publish("OrderCreated", Map.of(
                "orderId", "ORD-100",
                "customerId", "CUST-42",
                "amount", 149.99
        ));

        // Wait for async processing
        Thread.sleep(500);

        System.out.println("\n--- Second order ---");
        bus.publish("OrderCreated", Map.of(
                "orderId", "ORD-101",
                "customerId", "CUST-77",
                "amount", 29.99
        ));

        Thread.sleep(500);
        bus.shutdown();

        System.out.println("\n✅ All events processed asynchronously (decoupled services)");
    }
}
