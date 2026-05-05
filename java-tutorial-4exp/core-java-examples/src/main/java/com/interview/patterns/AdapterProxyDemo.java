package com.interview.patterns;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Adapter pattern — wrap legacy API to new interface.
 * Proxy pattern — JDK dynamic proxy for logging/timing.
 */
public class AdapterProxyDemo {

    // === ADAPTER PATTERN ===

    // Legacy SOAP-style service (can't modify)
    static class LegacySoapService {
        String fetchXmlData(String soapEnvelope) {
            return "<response><status>OK</status><data>subscriber-123</data></response>";
        }
    }

    // Modern interface our microservice expects
    interface SubscriberService {
        String getSubscriber(String id);
    }

    // Adapter — makes legacy work with new interface
    static class SoapToRestAdapter implements SubscriberService {
        private final LegacySoapService legacy;

        SoapToRestAdapter(LegacySoapService legacy) { this.legacy = legacy; }

        public String getSubscriber(String id) {
            String envelope = "<soap:Envelope><id>" + id + "</id></soap:Envelope>";
            String xml = legacy.fetchXmlData(envelope);
            // Parse XML → return JSON-friendly string
            return "{\"id\":\"" + id + "\",\"status\":\"OK\"}";
        }
    }

    // === PROXY PATTERN (JDK Dynamic Proxy) ===

    interface PaymentService {
        String processPayment(String orderId, double amount);
    }

    static class RealPaymentService implements PaymentService {
        public String processPayment(String orderId, double amount) {
            return "Payment processed: " + orderId + " ($" + amount + ")";
        }
    }

    // Logging proxy via JDK dynamic proxy
    static PaymentService createLoggingProxy(PaymentService real) {
        InvocationHandler handler = (proxy, method, args) -> {
            long start = System.nanoTime();
            System.out.printf("  ⏱️  [PROXY] Calling %s(%s)%n", method.getName(), java.util.Arrays.toString(args));
            Object result = method.invoke(real, args);
            long elapsed = (System.nanoTime() - start) / 1_000;
            System.out.printf("  ⏱️  [PROXY] %s returned in %dμs%n", method.getName(), elapsed);
            return result;
        };
        return (PaymentService) Proxy.newProxyInstance(
            PaymentService.class.getClassLoader(),
            new Class[]{PaymentService.class},
            handler
        );
    }

    public static void main(String[] args) {
        System.out.println("=== Adapter Pattern ===\n");

        // Client uses modern interface — doesn't know about SOAP
        SubscriberService service = new SoapToRestAdapter(new LegacySoapService());
        String result = service.getSubscriber("sub-456");
        System.out.println("  Adapted result: " + result);

        System.out.println("\n=== Proxy Pattern (Dynamic) ===\n");

        // Real service wrapped with logging proxy
        PaymentService real = new RealPaymentService();
        PaymentService proxied = createLoggingProxy(real);

        String payment = proxied.processPayment("ORD-789", 99.99);
        System.out.println("  Result: " + payment);

        System.out.println("\n--- Spring equivalent ---");
        System.out.println("// @Transactional  → Spring creates CGLIB proxy around your bean");
        System.out.println("// @Cacheable      → Proxy intercepts, checks cache before calling real method");
        System.out.println("// @Async          → Proxy submits call to thread pool");
    }
}
