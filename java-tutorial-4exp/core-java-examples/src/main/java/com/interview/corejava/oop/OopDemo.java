package com.interview.corejava.oop;

/**
 * Demonstrates OOP pillars: Abstraction, Polymorphism, Encapsulation, Inheritance.
 * Context: Notification system in a microservice (like Ericsson NEF/CAPIF).
 */
public class OopDemo {

    // ABSTRACTION — contract without implementation details
    interface NotificationSender {
        void send(String userId, String message);
        String getChannel();
    }

    // INHERITANCE + POLYMORPHISM — different implementations, same interface
    static class KafkaSender implements NotificationSender {
        public void send(String userId, String message) {
            System.out.println("[Kafka] → " + userId + ": " + message);
        }
        public String getChannel() { return "KAFKA"; }
    }

    static class WebhookSender implements NotificationSender {
        public void send(String userId, String message) {
            System.out.println("[Webhook] → " + userId + ": " + message);
        }
        public String getChannel() { return "WEBHOOK"; }
    }

    // ENCAPSULATION — internal state hidden, only exposed via methods
    static class NotificationService {
        private final NotificationSender sender;
        private int sentCount = 0;

        NotificationService(NotificationSender sender) {
            this.sender = sender;
        }

        void notify(String userId, String message) {
            sender.send(userId, message);
            sentCount++;
        }

        int getSentCount() { return sentCount; }
    }

    public static void main(String[] args) {
        // Polymorphism in action — same code, different behavior
        NotificationSender[] senders = { new KafkaSender(), new WebhookSender() };

        for (NotificationSender sender : senders) {
            NotificationService service = new NotificationService(sender);
            service.notify("user-123", "Session expired");
            System.out.println("Channel: " + sender.getChannel() + ", Count: " + service.getSentCount());
        }
    }
}
