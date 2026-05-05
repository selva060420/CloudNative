package com.interview.patterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer pattern — custom implementation + how Spring does it.
 */
public class ObserverDemo {

    // Observer interface
    interface EventListener {
        void onEvent(String event);
    }

    // Subject (Publisher)
    static class EventBus {
        private final List<EventListener> listeners = new ArrayList<>();

        void subscribe(EventListener listener) { listeners.add(listener); }
        void unsubscribe(EventListener listener) { listeners.remove(listener); }

        void publish(String event) {
            System.out.println("  Publishing: " + event);
            listeners.forEach(l -> l.onEvent(event));
        }
    }

    // Concrete observers
    static class AuditLogger implements EventListener {
        public void onEvent(String event) {
            System.out.println("  📝 AuditLogger: logged '" + event + "'");
        }
    }

    static class MetricsCollector implements EventListener {
        public void onEvent(String event) {
            System.out.println("  📊 Metrics: incremented counter for '" + event + "'");
        }
    }

    static class AlertService implements EventListener {
        public void onEvent(String event) {
            if (event.contains("ERROR")) {
                System.out.println("  🚨 Alert: triggered PagerDuty for '" + event + "'");
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Observer Pattern ===\n");

        var bus = new EventBus();
        var audit = new AuditLogger();
        var metrics = new MetricsCollector();
        var alert = new AlertService();

        bus.subscribe(audit);
        bus.subscribe(metrics);
        bus.subscribe(alert);

        System.out.println("Event 1 (normal):");
        bus.publish("user.login");

        System.out.println("\nEvent 2 (error):");
        bus.publish("ERROR: database connection timeout");

        // Unsubscribe and verify
        bus.unsubscribe(metrics);
        System.out.println("\nEvent 3 (after metrics unsubscribed):");
        bus.publish("user.logout");

        System.out.println("\n--- Spring equivalent ---");
        System.out.println("// @Component");
        System.out.println("// public class AuditListener {");
        System.out.println("//     @EventListener");
        System.out.println("//     public void handle(UserLoginEvent event) { ... }");
        System.out.println("// }");
        System.out.println("// Publisher: applicationEventPublisher.publishEvent(new UserLoginEvent(...))");
    }
}
