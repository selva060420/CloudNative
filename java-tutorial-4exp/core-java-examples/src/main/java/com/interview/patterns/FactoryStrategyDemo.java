package com.interview.patterns;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Factory + Strategy combined — notification dispatch system.
 */
public class FactoryStrategyDemo {

    enum ChannelType { EMAIL, SMS, WEBHOOK }

    // Strategy interface
    interface NotificationStrategy {
        void send(String recipient, String message);
        ChannelType channel();
    }

    // Concrete strategies
    static class EmailStrategy implements NotificationStrategy {
        public void send(String recipient, String msg) {
            System.out.printf("  📧 Email to %s: %s%n", recipient, msg);
        }
        public ChannelType channel() { return ChannelType.EMAIL; }
    }

    static class SmsStrategy implements NotificationStrategy {
        public void send(String recipient, String msg) {
            System.out.printf("  📱 SMS to %s: %s%n", recipient, msg);
        }
        public ChannelType channel() { return ChannelType.SMS; }
    }

    static class WebhookStrategy implements NotificationStrategy {
        public void send(String recipient, String msg) {
            System.out.printf("  🔗 Webhook to %s: %s%n", recipient, msg);
        }
        public ChannelType channel() { return ChannelType.WEBHOOK; }
    }

    // Factory — registry-based (no switch/if-else)
    static class NotificationFactory {
        private final Map<ChannelType, NotificationStrategy> registry;

        NotificationFactory(List<NotificationStrategy> strategies) {
            this.registry = strategies.stream()
                .collect(Collectors.toMap(NotificationStrategy::channel, s -> s));
        }

        NotificationStrategy get(ChannelType type) {
            return Optional.ofNullable(registry.get(type))
                .orElseThrow(() -> new IllegalArgumentException("No strategy for: " + type));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Factory + Strategy Pattern ===\n");

        var factory = new NotificationFactory(List.of(
            new EmailStrategy(), new SmsStrategy(), new WebhookStrategy()
        ));

        // Client doesn't know concrete class — just asks factory
        for (ChannelType ch : ChannelType.values()) {
            System.out.println("Channel: " + ch);
            factory.get(ch).send("selva@ericsson.com", "NEF event triggered");
        }
    }
}
