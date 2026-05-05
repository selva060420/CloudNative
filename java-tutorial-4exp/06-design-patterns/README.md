# 06 — Design Patterns

> Interview-ready guide for a 3+ year Java backend engineer targeting mid-senior roles.

---

## 1. Definition

Design patterns are **reusable solutions to common software design problems**. They are not code — they are templates for how to solve a problem in different situations.

| Pattern | Type | One-liner |
|---------|------|-----------|
| **Singleton** | Creational | One instance, global access |
| **Factory Method** | Creational | Subclass decides which object to create |
| **Builder** | Creational | Step-by-step construction of complex objects |
| **Strategy** | Behavioral | Swap algorithms at runtime |
| **Observer** | Behavioral | Notify dependents when state changes |
| **Template Method** | Behavioral | Fixed algorithm skeleton, subclass fills steps |
| **Chain of Responsibility** | Behavioral | Pass request along a chain of handlers |
| **Adapter** | Structural | Make incompatible interfaces work together |
| **Proxy** | Structural | Control access to an object |
| **Decorator** | Structural | Add behavior dynamically, same interface |
| **Dependency Injection** | Creational/Structural | Invert control — supply dependencies externally |

### Design Principles (asked alongside patterns)

| Principle | One-liner |
|-----------|-----------|
| **S** — Single Responsibility | A class should have only one reason to change |
| **O** — Open/Closed | Open for extension, closed for modification |
| **L** — Liskov Substitution | Subtypes must be substitutable for their base types without breaking behavior |
| **I** — Interface Segregation | Don't force clients to depend on methods they don't use |
| **D** — Dependency Inversion | Depend on abstractions, not concretions |
| **DRY** | Don't Repeat Yourself — extract common logic |
| **KISS** | Keep It Simple, Stupid — avoid unnecessary complexity |
| **YAGNI** | You Aren't Gonna Need It — don't build what's not required yet |

---

## 2. Why This Is Needed

| Problem | Pattern Solution |
|---------|-----------------|
| Database connection pool — only one instance needed | Singleton |
| Payment gateway — PayPal, Stripe, Razorpay — pick at runtime | Factory |
| Complex object with 10+ optional fields (API request) | Builder |
| Notification service — email, SMS, push — swap without changing caller | Strategy |
| Event-driven microservice — react to state changes | Observer |
| Legacy SOAP service consumed by REST microservice | Adapter |
| Lazy-load heavy resources, add caching/logging transparently | Proxy |

---

## 3. How It Works Internally

### Singleton (Double-Checked Locking)

```
getInstance() called
    ↓
Check: instance == null? (no lock)
    ↓ yes
Acquire lock (synchronized)
    ↓
Check again: instance == null? (double-check)
    ↓ yes
Create instance (volatile ensures visibility)
    ↓
Return instance
```

### Factory Method

```
Client → calls factory.create("type")
              ↓
Factory → switch/map → returns ConcreteProduct
              ↓
Client uses Product interface (doesn't know concrete class)
```

### Strategy

```
Context holds Strategy reference
    ↓
Client sets strategy: context.setStrategy(new ConcreteStrategyA())
    ↓
context.execute() → delegates to strategy.algorithm()
    ↓
Swap strategy at runtime without changing Context code
```

### Observer (Pub-Sub)

```
Subject maintains List<Observer>
    ↓
State changes → subject.notifyAll()
    ↓
Each observer.update(event) called
    ↓
Observers react independently (loose coupling)
```

---

## 4. Real-World Example

**Ericsson 5G NEF — Notification Dispatch System:**

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────┐
│ NEF Event   │────▶│ NotificationFactory│────▶│ SMS Notifier│
│ (subscriber │     │ (Factory Pattern)  │     │ (Strategy)  │
│  location   │     └──────────────────┘     ├─────────────┤
│  change)    │              │                │Email Notifier│
└─────────────┘              │                ├─────────────┤
                             │                │Push Notifier │
                             ▼                └─────────────┘
                    ┌──────────────┐
                    │ EventBus     │ (Observer Pattern)
                    │ subscribers  │
                    │ get notified │
                    └──────────────┘
```

- **Factory** creates the right notifier based on subscription config
- **Strategy** lets each notifier implement its own delivery logic
- **Observer** decouples event producers from consumers
- **Singleton** for the EventBus instance (one per JVM)

---

## 5. Common Interview Questions

**Q: How do you make Singleton thread-safe?**

| Approach | Thread-safe? | Lazy? | Notes |
|----------|-------------|-------|-------|
| Eager (static field) | ✅ | ❌ | JVM guarantees class loading is thread-safe |
| Synchronized method | ✅ | ✅ | Slow — lock on every call |
| Double-checked locking | ✅ | ✅ | Needs `volatile` — best general approach |
| Enum singleton | ✅ | ❌ | Serialization-safe, reflection-safe — Joshua Bloch recommended |
| Bill Pugh (static inner class) | ✅ | ✅ | Cleanest — no synchronization needed |

**Q: Factory vs Abstract Factory?**

| Factory Method | Abstract Factory |
|---------------|-----------------|
| Creates one product | Creates family of related products |
| Single method | Multiple factory methods |
| `NotifierFactory.create("sms")` | `UIFactory.createButton()`, `UIFactory.createCheckbox()` |

**Q: When would you use Builder over constructor?**

- More than 4 parameters
- Many optional fields
- Immutable objects with complex construction
- Fluent API readability (`Request.builder().url(...).timeout(...).build()`)

**Q: Strategy vs Template Method?**

| Strategy | Template Method |
|----------|----------------|
| Composition (HAS-A) | Inheritance (IS-A) |
| Swap at runtime | Fixed at compile time |
| Interface-based | Abstract class with hook methods |
| Spring: `@Qualifier` injection | Spring: `AbstractRoutingDataSource` |

---

## 6. Tricky Edge Cases or Pitfalls

| Pattern | Pitfall | Fix |
|---------|---------|-----|
| Singleton | Broken by reflection (`setAccessible(true)`) | Use enum singleton |
| Singleton | Broken by serialization (new instance on deserialize) | Add `readResolve()` |
| Singleton | Broken by multiple classloaders | Use Spring `@Scope("singleton")` — container-managed |
| Factory | Switch/if-else grows unbounded | Use `Map<String, Supplier<Product>>` registry |
| Observer | Memory leak — observer never unregistered | Use `WeakReference` or explicit `removeObserver()` |
| Observer | Notification order dependency | Document that order is undefined; use priority if needed |
| Builder | Forgetting `.build()` — returns builder not object | Make builder methods return `this`, `build()` returns target |
| Proxy | Proxy within same class — Spring AOP won't intercept | Self-injection or `AopContext.currentProxy()` |

---

## 7. Comparison with Related Concepts

| Pattern | vs | Difference |
|---------|-----|-----------|
| Strategy | State | Strategy: client chooses algorithm. State: object changes behavior based on internal state |
| Adapter | Decorator | Adapter: changes interface. Decorator: adds behavior, same interface |
| Proxy | Decorator | Proxy: controls access. Decorator: adds responsibility |
| Factory | Builder | Factory: creates in one step. Builder: step-by-step construction |
| Observer | Mediator | Observer: one-to-many broadcast. Mediator: many-to-many through central hub |

---

## 8. Performance Impact

| Pattern | Impact | When it matters |
|---------|--------|-----------------|
| Singleton | Lock contention on creation (DCL) | High-concurrency startup; negligible after init |
| Factory | Reflection-based factory is slow | Avoid reflection in hot paths; use `Supplier` map |
| Builder | Extra object allocation (builder + product) | Negligible for request-scoped objects; avoid in tight loops |
| Strategy | Virtual dispatch (interface call) | JIT inlines monomorphic calls; no real cost |
| Observer | O(n) notification to all observers | Use async dispatch (event bus) for large subscriber lists |
| Proxy | Extra indirection per call | Spring AOP proxy: ~1-2μs overhead per method call |

---

## 9. Trade-offs

| Pattern | Use when | Don't use when |
|---------|----------|----------------|
| Singleton | Shared resource (pool, config, registry) | Testability matters — hard to mock; use DI instead |
| Factory | Object creation logic is complex or varies | Only one implementation exists — over-engineering |
| Builder | 4+ params, optional fields, immutable objects | Simple 2-3 field objects — just use constructor |
| Strategy | Multiple algorithms, swap at runtime | Only one algorithm ever needed |
| Observer | Decoupled event notification | Tight ordering/transactional requirements |
| Adapter | Integrating legacy/third-party code | You control both interfaces — just refactor |
| Proxy | Cross-cutting concerns (logging, caching, auth) | Simple direct calls with no cross-cutting needs |

---

## 10. 30–60 Sec Interview Answer

**"Explain design patterns you've used in production"**

> "In our 5G NEF microservice at Ericsson, we use several patterns daily. **Singleton** for the connection pool and config registry — Spring manages this via `@Scope`. **Factory** for creating notification handlers — based on subscription type, we instantiate SMS, email, or webhook notifiers without the caller knowing the concrete class. **Strategy** for the actual delivery logic — each notifier implements a `NotificationStrategy` interface, so we can add new channels without touching existing code. **Observer** via Spring's `ApplicationEventPublisher` — when a subscriber's location changes, we publish an event and multiple listeners react independently. **Builder** for constructing complex API responses with optional fields. These patterns keep our code open for extension, closed for modification — which matters when 3GPP specs change every release."

---

## 11. Real Production Scenario

**Problem:** NEF notification service needed to support 4 delivery channels (SMS, email, push, webhook). Adding a new channel required modifying 6 files.

**Solution applied:**
1. **Strategy** — `NotificationStrategy` interface with `send(Notification n)` method
2. **Factory** — `NotificationStrategyFactory` with `Map<ChannelType, Supplier<NotificationStrategy>>`
3. **Observer** — `@EventListener` on `SubscriptionEvent` triggers the right strategy

**Result:** Adding a 5th channel (Kafka callback) = 1 new class + 1 line in factory map. Zero changes to existing code. Deployed in 1 sprint vs estimated 3 sprints with the old approach.

---

## 12. If This Fails, How to Debug

| Symptom | Root Cause | Fix |
|---------|-----------|-----|
| Two singleton instances exist | Multiple classloaders (EAR, OSGi) or reflection attack | Use enum singleton or Spring-managed bean |
| Factory returns null | Unregistered type in map/switch | Add default case with meaningful exception |
| Strategy NPE | Strategy not set before `execute()` called | Use Null Object pattern as default strategy |
| Observer not receiving events | Forgot to register, or registered on wrong instance | Log on register/unregister; verify subject reference |
| Proxy method not intercepted | Calling method from within same class (self-invocation) | Extract to separate bean or use `AopContext` |
| Builder produces invalid object | No validation in `build()` | Add validation in `build()` — throw if required fields missing |

---

## Additional Patterns (Frequently Asked)

### Dependency Injection (DI)

**What:** Instead of a class creating its own dependencies, they are provided (injected) from outside.

```java
// ❌ Tight coupling — hard to test
public class OrderService {
    private final PaymentGateway gateway = new StripeGateway(); // locked to Stripe
}

// ✅ DI — loose coupling
public class OrderService {
    private final PaymentGateway gateway;

    public OrderService(PaymentGateway gateway) { // injected
        this.gateway = gateway;
    }
}
```

**3 types of injection in Spring:**

| Type | Annotation | Recommended? |
|------|-----------|-------------|
| Constructor | `@Autowired` (optional on single constructor) | ✅ Yes — immutable, testable |
| Setter | `@Autowired` on setter | 🟡 For optional deps |
| Field | `@Autowired` on field | ❌ No — untestable, hides deps |

**Why constructor injection wins:**
- Fields can be `final` → immutable
- Easy to unit test (just pass mocks in constructor)
- Fails fast if dependency missing (compile-time vs runtime)
- No reflection needed

---

### Template Method

**What:** Define algorithm skeleton in base class; subclasses override specific steps.

```java
// Spring's JdbcTemplate uses this internally
public abstract class DataProcessor {
    // Template method — fixed algorithm
    public final void process() {
        fetchData();
        validate();
        transform();
        save();
    }

    abstract void fetchData();
    abstract void transform();

    void validate() { /* default validation */ }
    void save() { /* default save */ }
}

public class CsvProcessor extends DataProcessor {
    void fetchData() { System.out.println("Reading CSV file"); }
    void transform() { System.out.println("Parsing CSV rows"); }
}
```

**Spring examples:** `JdbcTemplate`, `RestTemplate`, `AbstractRoutingDataSource`, `OncePerRequestFilter`

---

### Decorator

**What:** Wrap an object to add behavior without changing its interface.

```java
// Classic Java I/O — each layer adds behavior
InputStream raw = new FileInputStream("data.txt");
InputStream buffered = new BufferedInputStream(raw);      // adds buffering
InputStream gzipped = new GZIPInputStream(buffered);      // adds decompression

// In microservices — decorating a service
public interface NotificationSender {
    void send(Notification n);
}

public class LoggingDecorator implements NotificationSender {
    private final NotificationSender delegate;

    public LoggingDecorator(NotificationSender delegate) { this.delegate = delegate; }

    public void send(Notification n) {
        log.info("Sending notification to {}", n.recipient());
        delegate.send(n);  // delegate to real implementation
        log.info("Notification sent successfully");
    }
}
```

**Decorator vs Proxy:** Decorator adds behavior (logging, caching). Proxy controls access (auth, lazy-load).

---

### Chain of Responsibility

**What:** Pass a request along a chain of handlers; each decides to process or pass it on.

```java
// Spring Security filter chain is exactly this pattern
public abstract class Handler {
    private Handler next;

    public Handler setNext(Handler next) { this.next = next; return next; }

    public void handle(Request request) {
        if (canHandle(request)) {
            process(request);
        } else if (next != null) {
            next.handle(request);
        }
    }

    abstract boolean canHandle(Request request);
    abstract void process(Request request);
}

// Usage: AuthHandler → RateLimitHandler → ValidationHandler → BusinessHandler
```

**Real-world examples:**
- Servlet filters (`FilterChain.doFilter()`)
- Spring Security filter chain (15+ filters in sequence)
- Spring Interceptors (`HandlerInterceptor`)
- Exception handler resolution (`@ControllerAdvice` chain)

---

## Follow-up Interview Questions

**Q1:** "Your microservice has 8 payment providers. Each has a different SDK, different error codes, and different retry policies. How do you design this cleanly?"

**Answer:**

```java
// Adapter — normalize each SDK to a common interface
public interface PaymentGateway {
    PaymentResult charge(PaymentRequest request);
    PaymentResult refund(String transactionId);
}

// Each provider gets an adapter
public class StripeAdapter implements PaymentGateway {
    private final StripeClient client; // their SDK
    
    public PaymentResult charge(PaymentRequest req) {
        try {
            StripeCharge charge = client.charges().create(mapToStripe(req));
            return PaymentResult.success(charge.getId());
        } catch (StripeException e) {
            return PaymentResult.failure(mapError(e)); // normalize error codes
        }
    }
}

// Factory — pick the right adapter
@Component
public class PaymentGatewayFactory {
    private final Map<Provider, PaymentGateway> gateways;
    
    public PaymentGatewayFactory(List<PaymentGateway> allGateways) {
        this.gateways = allGateways.stream()
            .collect(Collectors.toMap(PaymentGateway::provider, Function.identity()));
    }
    
    public PaymentGateway get(Provider provider) {
        return Optional.ofNullable(gateways.get(provider))
            .orElseThrow(() -> new UnsupportedProviderException(provider));
    }
}

// Strategy — retry policy per provider
public interface RetryPolicy {
    <T> T execute(Supplier<T> action);
}

public class ExponentialRetryPolicy implements RetryPolicy { /* ... */ }
public class NoRetryPolicy implements RetryPolicy { /* ... */ }
```

**Key patterns used:** Adapter (normalize SDKs), Factory (pick provider), Strategy (retry policy per provider).

---

**Q2:** "How does Spring Boot use design patterns internally?"

**Answer:**

| Spring Concept | Pattern | How |
|---------------|---------|-----|
| `ApplicationContext` | Singleton + Factory | Beans are singletons by default; context is the factory |
| `BeanFactory` | Factory Method | Creates beans based on config/annotations |
| `@EventListener` | Observer | Pub-sub for application events |
| `RestTemplate` / `WebClient` | Builder | Fluent construction with options |
| `HandlerAdapter` | Adapter | Adapts different controller types to common handler interface |
| `AOP Proxies` | Proxy | `@Transactional`, `@Cacheable` — CGLIB/JDK dynamic proxy |
| `@Profile` / `@Conditional` | Strategy | Different beans loaded based on condition |
| `JdbcTemplate` | Template Method | Fixed algorithm, customizable steps via callbacks |

---

## Practice Task

Design a **notification dispatch system** with:
1. `NotificationStrategy` interface (Strategy pattern)
2. `EmailStrategy`, `SmsStrategy`, `WebhookStrategy` implementations
3. `NotificationFactory` that returns the right strategy based on channel type (Factory pattern)
4. `NotificationService` as a singleton Spring bean that uses the factory
5. Demonstrate Builder pattern for constructing `Notification` objects with optional fields

### Solution

```java
// --- Strategy interface ---
public interface NotificationStrategy {
    void send(Notification notification);
    ChannelType channel();
}

// --- Implementations ---
public class EmailStrategy implements NotificationStrategy {
    public void send(Notification n) {
        System.out.printf("📧 Email to %s: %s%n", n.recipient(), n.message());
    }
    public ChannelType channel() { return ChannelType.EMAIL; }
}

public class SmsStrategy implements NotificationStrategy {
    public void send(Notification n) {
        System.out.printf("📱 SMS to %s: %s%n", n.recipient(), n.message());
    }
    public ChannelType channel() { return ChannelType.SMS; }
}

public class WebhookStrategy implements NotificationStrategy {
    public void send(Notification n) {
        System.out.printf("🔗 Webhook to %s: %s%n", n.recipient(), n.message());
    }
    public ChannelType channel() { return ChannelType.WEBHOOK; }
}

// --- Factory ---
public class NotificationFactory {
    private final Map<ChannelType, NotificationStrategy> strategies;

    public NotificationFactory(List<NotificationStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(NotificationStrategy::channel, Function.identity()));
    }

    public NotificationStrategy get(ChannelType type) {
        return Optional.ofNullable(strategies.get(type))
            .orElseThrow(() -> new IllegalArgumentException("No strategy for: " + type));
    }
}

// --- Builder for Notification ---
public class Notification {
    private final String recipient;
    private final String message;
    private final ChannelType channel;
    private final String subject;      // optional
    private final Map<String, String> metadata; // optional

    private Notification(Builder b) {
        this.recipient = Objects.requireNonNull(b.recipient);
        this.message = Objects.requireNonNull(b.message);
        this.channel = Objects.requireNonNull(b.channel);
        this.subject = b.subject;
        this.metadata = b.metadata;
    }

    public static Builder builder() { return new Builder(); }

    // getters...
    public String recipient() { return recipient; }
    public String message() { return message; }
    public ChannelType channel() { return channel; }
    public String subject() { return subject; }

    public static class Builder {
        private String recipient, message, subject;
        private ChannelType channel;
        private Map<String, String> metadata = Map.of();

        public Builder recipient(String r) { this.recipient = r; return this; }
        public Builder message(String m) { this.message = m; return this; }
        public Builder channel(ChannelType c) { this.channel = c; return this; }
        public Builder subject(String s) { this.subject = s; return this; }
        public Builder metadata(Map<String, String> md) { this.metadata = md; return this; }

        public Notification build() { return new Notification(this); }
    }
}

// --- Service (Singleton in Spring context) ---
public class NotificationService {
    private final NotificationFactory factory;

    public NotificationService(NotificationFactory factory) {
        this.factory = factory;
    }

    public void dispatch(Notification notification) {
        NotificationStrategy strategy = factory.get(notification.channel());
        strategy.send(notification);
    }
}

// --- Usage ---
public class Main {
    public static void main(String[] args) {
        var factory = new NotificationFactory(List.of(
            new EmailStrategy(), new SmsStrategy(), new WebhookStrategy()
        ));
        var service = new NotificationService(factory);

        Notification n = Notification.builder()
            .recipient("selva@ericsson.com")
            .message("5G subscriber location changed")
            .channel(ChannelType.EMAIL)
            .subject("NEF Alert")
            .build();

        service.dispatch(n); // 📧 Email to selva@ericsson.com: 5G subscriber location changed
    }
}

enum ChannelType { EMAIL, SMS, WEBHOOK }
```

---

## Code Examples

See runnable demos in:
```
core-java-examples/src/main/java/com/interview/patterns/
├── SingletonDemo.java       — All 5 approaches (eager, synchronized, DCL, enum, Bill Pugh)
├── FactoryStrategyDemo.java — Factory + Strategy combined (notification system)
├── BuilderDemo.java         — Builder pattern with validation
├── ObserverDemo.java        — Custom observer + Spring ApplicationEvent comparison
├── AdapterProxyDemo.java    — Adapter for legacy API + JDK dynamic proxy
```
