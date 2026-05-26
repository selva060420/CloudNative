# 01 — Core Java

> Interview-ready guide for a 3+ year Java backend engineer targeting mid-senior roles.

---

## 1. Definition

**Core Java** covers the foundational building blocks of the language that every backend system relies on:

| Concept | One-liner |
|---------|-----------|
| **OOP** | Encapsulation, Inheritance, Polymorphism, Abstraction — how you model real systems |
| **JVM Internals** | How bytecode executes: ClassLoader → Bytecode Verifier → JIT → Execution Engine |
| **Memory Model (JMM)** | Rules for how threads see shared variables (happens-before, visibility, ordering) |
| **String Pool** | Interned strings in heap's special region to save memory |
| **Immutability** | Objects whose state cannot change after creation — thread-safe by design |
| **static/final** | Class-level vs instance-level; compile-time constants vs runtime constants |
| **Serialization** | Converting objects to byte streams for network/disk transfer |
| **Generics** | Type-safe collections and methods without casting — erased at runtime |
| **Annotations** | Metadata on code — processed at compile-time or runtime |

---

## 2. Why This Is Needed

| Problem | Core Java Solution |
|---------|-------------------|
| Microservice with 50+ classes needs clean boundaries | OOP (encapsulation, abstraction) |
| Kubernetes pod OOMKilled | JVM memory tuning (heap, metaspace, GC) |
| Race condition in concurrent request handling | JMM (volatile, happens-before) |
| 10M user sessions cached — memory pressure | String pool + immutability reduces duplication |
| Kafka message deserialization fails after schema change | Serialization versioning (serialVersionUID) |
| REST API returns Object instead of typed response | Generics enforce compile-time safety |
| Spring Boot auto-wires beans magically | Annotations (@Component, @Autowired) processed via reflection |

---

## 3. How It Works Internally

### JVM Architecture

```
Source.java → javac → Bytecode (.class)
                          ↓
                    ClassLoader (Bootstrap → Extension → Application)
                          ↓
                    Bytecode Verifier
                          ↓
                    JIT Compiler (HotSpot: C1 → C2)
                          ↓
                    Execution Engine → Native OS
```

### JVM Memory Layout

```
┌─────────────────────────────────────────────┐
│                   HEAP                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  Young   │  │   Old    │  │  String  │  │
│  │   Gen    │  │   Gen    │  │   Pool   │  │
│  │(Eden+S0  │  │(Tenured) │  │          │  │
│  │   +S1)   │  │          │  │          │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└─────────────────────────────────────────────┘
┌──────────┐  ┌──────────┐  ┌──────────┐
│ Metaspace│  │  Stack   │  │  Native  │
│(classes, │  │(per      │  │  Method  │
│ methods) │  │ thread)  │  │  Stack   │
└──────────┘  └──────────┘  └──────────┘
```

### String Pool Internals

```java
String s1 = "hello";        // Goes to String Pool
String s2 = "hello";        // Reuses same reference from pool
String s3 = new String("hello"); // New object on heap (NOT pool)

s1 == s2;    // true  (same pool reference)
s1 == s3;    // false (different objects)
s1.equals(s3); // true (same content)

s3.intern(); // Moves/returns pool reference
```

### Generics — Type Erasure

```java
// What you write:
List<String> names = new ArrayList<>();

// What JVM sees after erasure:
List names = new ArrayList();  // Generic type removed at runtime

// This is why you can't do:
// if (obj instanceof List<String>) — erased, JVM doesn't know <String>
```

### Serialization Flow

```
Object → ObjectOutputStream.writeObject()
       → Checks Serializable marker
       → Writes class metadata + field values as bytes
       → Network/Disk

Bytes → ObjectInputStream.readObject()
      → Checks serialVersionUID match
      → Reconstructs object (NO constructor called)
```

---

## 4. Real-World Example

### OOP in Microservice Design (Ericsson NEF/CAPIF)

```java
// Abstraction — define contract for notification delivery
public interface NotificationSender {
    void send(String userId, Event event);
}

// Polymorphism — different implementations per channel
public class KafkaNotificationSender implements NotificationSender {
    public void send(String userId, Event event) {
        kafkaTemplate.send("notifications", userId, event);
    }
}

public class WebhookNotificationSender implements NotificationSender {
    public void send(String userId, Event event) {
        restTemplate.postForEntity(callbackUrl, event, Void.class);
    }
}

// Encapsulation — Spring injects the right implementation
@Service
public class EventProcessor {
    private final NotificationSender sender; // abstracted away

    public EventProcessor(@Qualifier("kafka") NotificationSender sender) {
        this.sender = sender;
    }
}
```

### JVM Tuning for Kubernetes Pod

```yaml
# Deployment.yaml — JVM flags for containerized microservice
env:
  - name: JAVA_OPTS
    value: >-
      -XX:+UseG1GC
      -XX:MaxRAMPercentage=75.0
      -XX:+UseContainerSupport
      -XX:MaxMetaspaceSize=256m
      -XX:+HeapDumpOnOutOfMemoryError
      -XX:HeapDumpPath=/tmp/heapdump.hprof
resources:
  requests:
    memory: "512Mi"
  limits:
    memory: "1Gi"
```

### Immutability in Distributed Systems

```java
// Immutable DTO for Kafka messages — thread-safe, no defensive copies needed
public record SessionEvent(
    String sessionId,
    String userId,
    Instant timestamp,
    Map<String, String> metadata
) {
    public SessionEvent {
        // Defensive copy in compact constructor
        metadata = Map.copyOf(metadata);
    }
}
```

---

## 5. Common Interview Questions

### Q1: What are the 4 pillars of OOP? Give a real example from your project.

**Answer:** Encapsulation, Inheritance, Polymorphism, Abstraction.

In our NEF platform, we use **abstraction** via interfaces for notification delivery (Kafka vs Webhook). The `NotificationSender` interface hides implementation details. **Polymorphism** lets us swap implementations without changing the caller. **Encapsulation** keeps Kafka connection details private within the implementation class.

### Q2: Explain JVM memory areas. Where does each thing go?

**Answer:**
- **Heap** — Objects, arrays, String pool (shared across threads)
- **Stack** — Method frames, local variables, references (per thread)
- **Metaspace** — Class metadata, method bytecode (replaced PermGen in Java 8)
- **Native Method Stack** — JNI calls

Key point: Stack stores references, Heap stores actual objects. That's why passing an object to a method passes the reference (not the object itself).

### Q3: What is `volatile` and when would you use it?

**Answer:** `volatile` guarantees visibility — when one thread writes to a volatile variable, all other threads immediately see the new value. It prevents CPU cache staleness.

Use case: A shutdown flag in a microservice:
```java
private volatile boolean running = true;

public void processMessages() {
    while (running) { // Without volatile, thread may never see the update
        Message msg = queue.poll();
        process(msg);
    }
}

public void shutdown() { running = false; }
```

It does NOT provide atomicity — for read-modify-write, use `AtomicBoolean`.

### Q4: Why is String immutable in Java?

**Answer:**
1. **String Pool** — Immutability allows safe sharing; if one reference could mutate the string, all references would be affected
2. **Thread safety** — Strings are used as HashMap keys, class names, connection URLs — mutation would break everything
3. **Security** — Class loading uses strings; mutable strings could allow class injection
4. **Hashcode caching** — String caches its hashcode; immutability guarantees it never changes

### Q5: What is `serialVersionUID` and what happens if you don't define it?

**Answer:** It's a version identifier for serialized classes. If you don't define it, JVM auto-generates one based on class structure. Problem: if you add a field and redeploy one microservice but not another, deserialization fails with `InvalidClassException` because the auto-generated UID changed.

In our Cassandra-backed system, we always define it explicitly:
```java
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;
    // Adding new fields won't break existing serialized data
}
```

### Q6: Explain type erasure. What problems does it cause?

**Answer:** Java generics exist only at compile-time. At runtime, `List<String>` and `List<Integer>` are both just `List`. This means:
- Can't do `instanceof List<String>`
- Can't create `new T()` (type unknown at runtime)
- Can't create generic arrays `new T[10]`

Workaround for frameworks (like Jackson/Spring): pass `Class<T>` token or use `TypeReference<>`.

### Q7: Difference between `static` and `final`?

**Answer:**
| | `static` | `final` |
|---|----------|---------|
| Variable | One copy shared across all instances | Value can't change after assignment |
| Method | Belongs to class, not instance | Can't be overridden by subclass |
| Class | N/A (inner class: no outer reference) | Can't be extended |
| Combined | `static final` = compile-time constant | — |

```java
public class Config {
    static final int MAX_RETRIES = 3;        // Compile-time constant, inlined
    static final String ENV = System.getenv("ENV"); // Runtime constant (not inlined)
}
```

---

## 6. Tricky Edge Cases or Pitfalls

### Pitfall 1: String concatenation in loops

```java
// BAD — creates N intermediate String objects (immutability cost)
String result = "";
for (int i = 0; i < 100_000; i++) {
    result += i; // New String object every iteration
}

// GOOD — StringBuilder is mutable
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 100_000; i++) {
    sb.append(i);
}
```

### Pitfall 2: `==` vs `.equals()` with Integer cache

```java
Integer a = 127;
Integer b = 127;
a == b; // true — Integer cache [-128, 127]

Integer c = 128;
Integer d = 128;
c == d; // false — outside cache, different objects
```

### Pitfall 3: Serialization breaks Singleton

```java
public class Singleton implements Serializable {
    private static final Singleton INSTANCE = new Singleton();

    // Without this, deserialization creates a NEW instance
    private Object readResolve() {
        return INSTANCE;
    }
}
```

### Pitfall 4: `static` fields in Kubernetes — multiple pods

A `static` counter doesn't work as a global counter in microservices — each pod has its own JVM. Use Redis/DB for shared state.

### Pitfall 5: Generics and arrays don't mix

```java
// Compile error — generic array creation not allowed
List<String>[] array = new ArrayList<String>[10]; // ILLEGAL

// Workaround
List<List<String>> listOfLists = new ArrayList<>();
```

---

## 7. Comparison with Related Concepts

### Immutability Approaches

| Approach | Thread-safe? | Boilerplate | Java Version |
|----------|-------------|-------------|--------------|
| Manual (private final + no setters) | Yes | High | Any |
| `Collections.unmodifiableList()` | Yes (view only) | Medium | 1.2+ |
| `List.of()` / `Map.of()` | Yes | Low | 9+ |
| `record` | Yes | Minimal | 16+ |

### Serialization Options

| Method | Speed | Size | Schema Evolution | Use Case |
|--------|-------|------|-----------------|----------|
| Java Serializable | Slow | Large | Poor (UID breaks) | Legacy |
| JSON (Jackson) | Medium | Medium | Good | REST APIs |
| Protobuf | Fast | Small | Excellent | gRPC, Kafka |
| Avro | Fast | Small | Excellent (schema registry) | Kafka, Big Data |

### GC Algorithms

| GC | Best For | Pause Time | Throughput |
|----|----------|-----------|------------|
| G1GC | General purpose (default Java 11+) | Medium | High |
| ZGC | Low-latency (<10ms pauses) | Very Low | Medium |
| Shenandoah | Low-latency (RedHat) | Very Low | Medium |
| Parallel GC | Batch processing, throughput | High | Very High |

---

## 8. Performance Impact

### String Pool

| Operation | Without Pool | With Pool |
|-----------|-------------|-----------|
| 1M identical strings | 1M objects (~48MB) | 1 object (~48 bytes) |
| Comparison | `.equals()` O(n) | `==` O(1) if interned |

### JVM Tuning Impact (Real numbers from Ericsson microservice)

| Setting | Before | After | Impact |
|---------|--------|-------|--------|
| Default GC → G1GC | 200ms p99 pauses | 50ms p99 | 4x improvement |
| Heap 256m → 75% container | OOMKilled weekly | Stable | Zero OOM |
| `-XX:+UseContainerSupport` | JVM sees host memory | JVM sees container limit | Correct sizing |

### Generics vs Raw Types

| Aspect | Raw Type | Generic |
|--------|----------|---------|
| Type safety | Runtime ClassCastException | Compile-time error |
| Performance | Same (erasure) | Same |
| Readability | Poor | Self-documenting |

---

## 9. Trade-offs

### Immutability

| ✅ Use When | ❌ Avoid When |
|------------|--------------|
| DTOs passed between threads | Large objects modified frequently (GC pressure) |
| Map keys, Set elements | Builder pattern intermediate state |
| Kafka/REST message payloads | Performance-critical inner loops |

### Serialization Strategy

| ✅ Java Serializable | ❌ Avoid Java Serializable |
|---------------------|--------------------------|
| Quick prototyping | Cross-language systems |
| Internal JVM caching | Public APIs |
| Legacy system integration | High-throughput Kafka pipelines |

### static/final

| ✅ Use `static final` | ❌ Avoid `static` |
|----------------------|-------------------|
| Constants (config values) | Mutable shared state in microservices |
| Utility methods | State that differs per request/thread |
| Logger instances | Anything that needs per-pod isolation |

---

## 10. 30–60 Second Interview Answers

### "Explain OOP in 30 seconds"

> "OOP models systems using objects with state and behavior. The four pillars are: **Encapsulation** — hiding internal state behind methods; **Abstraction** — exposing only what's needed via interfaces; **Inheritance** — reusing behavior through class hierarchies; **Polymorphism** — same interface, different implementations. In my microservices, I use interfaces for abstraction and polymorphism to swap implementations like Kafka vs Webhook notification senders without changing the caller."

### "Explain JVM memory model in 30 seconds"

> "JVM divides memory into Heap (shared, stores objects — split into Young Gen and Old Gen), Stack (per-thread, stores method frames and local variables), and Metaspace (class metadata). The Garbage Collector reclaims unreachable heap objects. In Kubernetes, I use `-XX:MaxRAMPercentage=75` and G1GC to prevent OOMKilled while maximizing throughput. The Java Memory Model (JMM) defines happens-before rules that guarantee visibility of changes across threads."

### "Why is String immutable?"

> "Three reasons: security (class loading, connection URLs can't be tampered), thread-safety (safe to share across threads without synchronization), and performance (hashcode is cached, String Pool allows deduplication). The trade-off is GC pressure from concatenation in loops — use StringBuilder there."

---

## 11. Real Production Scenario

### Scenario: OOMKilled Pod in Kubernetes (Ericsson)

**Symptoms:**
- Kubernetes pod restarting every 2-3 hours
- `OOMKilled` exit code (137)
- Datadog showing heap growing linearly

**Root Cause:**
- JVM was using default `-Xmx` which didn't respect container memory limits (pre-Java 10 behavior)
- String concatenation in a logging loop creating millions of temporary objects
- Old Gen filling up because G1GC wasn't tuned for the workload

**Fix:**
```bash
# Before
java -jar service.jar

# After
java -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=75.0 \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -jar service.jar
```

Plus: replaced string concatenation with `StringBuilder` in the hot path, added `-XX:+HeapDumpOnOutOfMemoryError` for future debugging.

**Result:** Zero OOMKilled events in 3 months, p99 latency dropped from 200ms to 50ms.

---

## 12. If This Fails, How to Debug

### OOMKilled / Memory Issues

| Symptom | Tool | Action |
|---------|------|--------|
| Pod OOMKilled | `kubectl describe pod` | Check exit code 137, memory limits |
| Heap growing | Datadog/Prometheus JMX metrics | Monitor `jvm.memory.heap.used` |
| Need heap dump | `-XX:+HeapDumpOnOutOfMemoryError` | Analyze with Eclipse MAT or VisualVM |
| Metaspace OOM | `-XX:MaxMetaspaceSize` | Check for classloader leaks (hot-deploy) |

### Serialization Failures

| Symptom | Cause | Fix |
|---------|-------|-----|
| `InvalidClassException` | serialVersionUID mismatch | Define explicit `serialVersionUID` |
| `NotSerializableException` | Non-serializable field | Mark as `transient` or make it serializable |
| Data corruption after deploy | Field added/removed | Use explicit UID + handle missing fields |

### ClassLoader Issues

| Symptom | Cause | Fix |
|---------|-------|-----|
| `ClassNotFoundException` | Missing dependency | Check Maven/Gradle dependencies |
| `NoClassDefFoundError` | Class found at compile, missing at runtime | Check runtime classpath |
| `ClassCastException` with same class | Different classloaders loaded same class | Common in OSGi/hot-deploy — restart |

---

## Follow-Up Interview Questions

**Q1:** Your Kubernetes microservice processes 10K requests/sec. You notice GC pauses causing p99 latency spikes. How would you diagnose and fix this?

**Answer:**

**Diagnose:**
1. Enable GC logging: `-Xlog:gc*:file=gc.log:time,uptime,level,tags`
2. Check which collector is running (`-XX:+UseG1GC` vs default) and pause times in logs
3. Monitor heap usage: `jstat -gcutil <pid>` or Prometheus + Grafana (JMX exporter)
4. Look for frequent Full GCs or long Young GC pauses — indicates heap too small or too many long-lived objects promoted to Old Gen

**Fix (progressive):**
- **Tune heap:** Set `-Xms` = `-Xmx` (avoid resize pauses), typically 60-70% of container memory limit
- **Switch to low-latency GC:** ZGC (`-XX:+UseZGC`) for sub-ms pauses, or G1 with `-XX:MaxGCPauseMillis=50`
- **Reduce allocation rate:** Reuse objects (object pooling for hot paths), avoid autoboxing in loops, use `StringBuilder`
- **Fix memory leaks:** Unbounded caches, static collections growing — profile with `jmap -histo` or async-profiler
- **Right-size container:** K8s memory limit should be heap + metaspace + native memory + ~25% headroom

---

**Q2:** You're designing an immutable configuration object that loads from multiple sources (env vars, ConfigMap, DB). How would you implement the builder pattern while keeping the final object immutable?

**Answer:**

```java
public final class AppConfig {
    private final String dbUrl;
    private final int maxConnections;
    private final List<String> allowedOrigins;

    private AppConfig(Builder builder) {
        this.dbUrl = builder.dbUrl;
        this.maxConnections = builder.maxConnections;
        this.allowedOrigins = List.copyOf(builder.allowedOrigins); // defensive copy
    }

    public String getDbUrl() { return dbUrl; }
    public int getMaxConnections() { return maxConnections; }
    public List<String> getAllowedOrigins() { return allowedOrigins; } // already unmodifiable

    public static class Builder {
        private String dbUrl;
        private int maxConnections = 10;
        private List<String> allowedOrigins = new ArrayList<>();

        public Builder fromEnv() {
            this.dbUrl = System.getenv("DB_URL");
            return this;
        }
        public Builder fromConfigMap(Map<String, String> cm) {
            this.maxConnections = Integer.parseInt(cm.getOrDefault("MAX_CONN", "10"));
            return this;
        }
        public Builder allowedOrigins(List<String> origins) {
            this.allowedOrigins = origins;
            return this;
        }
        public AppConfig build() {
            Objects.requireNonNull(dbUrl, "DB_URL is required");
            return new AppConfig(this);
        }
    }
}
// Usage: new AppConfig.Builder().fromEnv().fromConfigMap(cm).build();
```

**Key points:** Class is `final`, all fields `private final`, constructor is private, mutable collections defensively copied with `List.copyOf()`, builder is the only way to construct.

---

## Practice Task

Build a demo that shows:
1. JVM memory areas in action (stack vs heap) → [JvmMemoryDemo.java](../core-java-examples/src/main/java/com/interview/corejava/jvm/JvmMemoryDemo.java)
2. String pool behavior with `==` vs `.equals()` vs `intern()` → [StringPoolDemo.java](../core-java-examples/src/main/java/com/interview/corejava/strings/StringPoolDemo.java)
3. Immutable class with defensive copying → See Q2 answer above
4. Serialization with `serialVersionUID` — prove that changing it breaks deserialization → [SerializationDemo.java](../core-java-examples/src/main/java/com/interview/corejava/serialization/SerializationDemo.java)
5. Generic method with bounded type parameter → [GenericsDemo.java](../core-java-examples/src/main/java/com/interview/corejava/generics/GenericsDemo.java)

---

## Additional Core Concepts (Frequently Asked)

### Enum — More Than Constants

```java
public enum HttpStatus {
    OK(200, "Success"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_ERROR(500, "Server Error");

    private final int code;
    private final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
```

**Interview points:**
- Enums are implicitly `final` and extend `java.lang.Enum`
- Thread-safe singleton: `enum Singleton { INSTANCE; }` — safest way (no reflection/serialization attacks)
- Can implement interfaces, have abstract methods
- `EnumSet` and `EnumMap` are highly optimized (bit-vector internally)

### Pass by Value (Classic Trick Question)

```java
public static void main(String[] args) {
    String name = "Selva";
    changeName(name);
    System.out.println(name); // Still "Selva" — String is immutable + pass by value

    int[] arr = {1, 2, 3};
    changeArray(arr);
    System.out.println(arr[0]); // 99 — reference copied, but points to same object
}

static void changeName(String s) { s = "Changed"; }  // New reference, original untouched
static void changeArray(int[] a) { a[0] = 99; }      // Same object modified via copied reference
```

**30-sec answer:** "Java is always pass-by-value. For primitives, the value is copied. For objects, the *reference* is copied — so you can modify the object's state, but reassigning the reference inside the method doesn't affect the caller."

### Shallow vs Deep Copy

```java
// Shallow copy — nested objects still shared
class Team implements Cloneable {
    String name;
    List<String> members; // Same list reference after clone!

    @Override
    protected Team clone() throws CloneNotSupportedException {
        return (Team) super.clone(); // Shallow
    }
}

// Deep copy — fully independent
protected Team deepClone() throws CloneNotSupportedException {
    Team copy = (Team) super.clone();
    copy.members = new ArrayList<>(this.members); // New list
    return copy;
}
```

**When it matters:** Kafka message objects, cached DTOs — shallow copy leads to shared mutable state bugs across threads.

### Java Records (Java 16+) and Sealed Classes (Java 17)

```java
// Record — immutable data carrier (replaces boilerplate POJOs)
public record ApiResponse<T>(T data, int statusCode, String message) {
    // Compact constructor for validation
    public ApiResponse {
        if (statusCode < 100 || statusCode > 599) throw new IllegalArgumentException("Invalid status");
    }
}

// Sealed class — restricts which classes can extend
public sealed interface Shape permits Circle, Rectangle, Triangle {}
public record Circle(double radius) implements Shape {}
public record Rectangle(double w, double h) implements Shape {}
public record Triangle(double a, double b, double c) implements Shape {}

// Pattern matching (Java 21)
double area(Shape s) {
    return switch (s) {
        case Circle c -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.w() * r.h();
        case Triangle t -> /* Heron's formula */ 0;
    };
}
```

**Interview points:**
- Records auto-generate: constructor, getters, `equals()`, `hashCode()`, `toString()`
- Records are implicitly `final` — can't extend them
- Sealed classes enable exhaustive `switch` — compiler knows all subtypes

### Inner Classes

| Type | Has access to outer? | Use case |
|------|---------------------|----------|
| **Static nested** | No (only static members) | Helper class, Builder pattern |
| **Member inner** | Yes (including private) | Iterator implementation |
| **Local** (inside method) | Yes + effectively final locals | One-off logic |
| **Anonymous** | Yes + effectively final locals | Pre-lambda callbacks |

```java
// Static nested — most common in production (no hidden outer reference)
public class KafkaConfig {
    public static class ProducerSettings { // No reference to KafkaConfig instance
        String bootstrapServers;
    }
}

// Anonymous → replaced by lambdas in modern Java
Runnable old = new Runnable() { public void run() { System.out.println("old"); } };
Runnable modern = () -> System.out.println("modern");
```

**Pitfall:** Non-static inner classes hold a hidden reference to the outer instance → memory leak if the inner object outlives the outer (common in Android, rare in backend).

### Autoboxing / Unboxing Pitfalls

```java
// NullPointerException from unboxing null
Integer count = null;
int value = count; // NPE! Unboxing null → crash

// Performance trap in loops
Long sum = 0L;
for (int i = 0; i < 1_000_000; i++) {
    sum += i; // Creates ~1M Long objects (autoboxing)
}
// Fix: use primitive long sum = 0L;

// Equality trap
Integer a = 200, b = 200;
System.out.println(a == b);      // false (outside [-128,127] cache)
System.out.println(a.equals(b)); // true (always use .equals() for wrappers)
```

---

## Code Examples

All code is in `core-java-examples/src/main/java/com/interview/corejava/`:

| File | Package | Topic |
|------|---------|-------|
| [OopDemo.java](../core-java-examples/src/main/java/com/interview/corejava/oop/OopDemo.java) | `oop` | Polymorphism, abstraction, encapsulation |
| [JvmMemoryDemo.java](../core-java-examples/src/main/java/com/interview/corejava/jvm/JvmMemoryDemo.java) | `jvm` | Stack vs Heap, GC behavior |
| [StringPoolDemo.java](../core-java-examples/src/main/java/com/interview/corejava/strings/StringPoolDemo.java) | `strings` | String pool, intern(), immutability |
| [SerializationDemo.java](../core-java-examples/src/main/java/com/interview/corejava/serialization/SerializationDemo.java) | `serialization` | Serializable, serialVersionUID, transient |
| [GenericsDemo.java](../core-java-examples/src/main/java/com/interview/corejava/generics/GenericsDemo.java) | `generics` | Type erasure, bounded types, wildcards |
| [AnnotationsDemo.java](../core-java-examples/src/main/java/com/interview/corejava/annotations/AnnotationsDemo.java) | `annotations` | Custom annotations, reflection processing |

---

## SOLID Principles

| Principle | Meaning | Violation Example | Fix |
|-----------|---------|-------------------|-----|
| **S** — Single Responsibility | A class should have only one reason to change | `UserService` handles auth + email + DB | Split into `AuthService`, `EmailService`, `UserRepository` |
| **O** — Open/Closed | Open for extension, closed for modification | Adding new payment type requires editing `PaymentProcessor` | Use strategy pattern — new class per payment type |
| **L** — Liskov Substitution | Subtypes must be substitutable for their base types | `Square extends Rectangle` breaks `setWidth()`/`setHeight()` | Use composition or separate interfaces |
| **I** — Interface Segregation | Clients shouldn't depend on methods they don't use | `Animal` interface with `fly()` forces `Dog` to implement it | Split into `Flyable`, `Swimmable` |
| **D** — Dependency Inversion | Depend on abstractions, not concretions | `OrderService` directly instantiates `MySQLRepository` | Inject `OrderRepository` interface |

```java
// D — Dependency Inversion in Spring Boot
@Service
public class OrderService {
    private final OrderRepository repo; // depends on abstraction

    public OrderService(OrderRepository repo) { // injected by Spring
        this.repo = repo;
    }
}

// Can swap MySQL → Cassandra without touching OrderService
@Repository
public class CassandraOrderRepository implements OrderRepository { ... }
```

**30-sec answer:** "SOLID keeps code maintainable at scale. Single Responsibility means one class, one reason to change. Open/Closed means I add features by adding new classes, not editing existing ones — strategy pattern is a classic example. Dependency Inversion means my service depends on an interface, so I can swap implementations without touching business logic. In Spring Boot, constructor injection naturally enforces DIP."

---

## Association, Aggregation & Composition

| Relationship | Strength | Lifecycle | Example |
|-------------|----------|-----------|---------|
| **Association** | Weak | Independent | `Teacher` ↔ `Student` (many-to-many) |
| **Aggregation** | Medium | Independent (HAS-A, can exist alone) | `Department` → `Employee` (employee survives dept deletion) |
| **Composition** | Strong | Dependent (PART-OF, dies with parent) | `House` → `Room` (room doesn't exist without house) |

```java
// Composition — Room cannot exist without House
public class House {
    private final List<Room> rooms; // created and destroyed with House

    public House(int numRooms) {
        rooms = IntStream.range(0, numRooms)
            .mapToObj(i -> new Room()) // House creates rooms
            .toList();
    }
    // When House is GC'd, Rooms are GC'd too
}

// Aggregation — Engine can exist independently
public class Car {
    private Engine engine; // injected, not created by Car

    public Car(Engine engine) { this.engine = engine; }
    public void setEngine(Engine e) { this.engine = e; } // can swap
}
```

**Interview tip:** Composition = "creates and owns." Aggregation = "uses but doesn't own." In microservices, services have association (call each other) but not composition (one service dying shouldn't kill another).

---

## Cohesion & Coupling

| Concept | Good | Bad | Metric |
|---------|------|-----|--------|
| **Cohesion** | High (class does one thing well) | Low (class does unrelated things) | Methods use most of the class's fields |
| **Coupling** | Low (classes are independent) | High (classes depend on each other's internals) | Changes in one class force changes in another |

```java
// LOW cohesion (bad) — does too many unrelated things
public class UserManager {
    public void createUser() { ... }
    public void sendEmail() { ... }      // not user management
    public void generateReport() { ... } // not user management
    public void backupDatabase() { ... } // definitely not user management
}

// HIGH cohesion (good) — focused responsibility
public class UserRepository {
    public User findById(String id) { ... }
    public User save(User user) { ... }
    public void delete(String id) { ... }
}
```

**Coupling in microservices:**
- **Tight coupling:** Service A calls Service B synchronously and breaks if B changes its API → use API versioning, contracts
- **Loose coupling:** Service A publishes event to Kafka, Service B consumes when ready → event-driven architecture

---

## Shutdown Hook

A shutdown hook is a thread registered via `Runtime.addShutdownHook()` that runs when the JVM is shutting down (SIGTERM, System.exit(), Ctrl+C).

```java
public class GracefulShutdown {
    public static void main(String[] args) {
        ExecutorService pool = Executors.newFixedThreadPool(10);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered — cleaning up...");
            pool.shutdown();
            try {
                if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                    pool.shutdownNow(); // force kill after 30s
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
            }
            System.out.println("Cleanup complete.");
        }));

        // Application runs...
    }
}
```

**When it runs:** `kill <pid>` (SIGTERM), `System.exit()`, last non-daemon thread exits.
**When it does NOT run:** `kill -9` (SIGKILL), JVM crash, power failure.

**Kubernetes relevance:** K8s sends SIGTERM → `preStop` hook + shutdown hook runs → 30s grace period → SIGKILL. Use shutdown hooks to drain connections, flush buffers, deregister from service discovery.

---

## finalize() — Deprecated (Java 9+)

```java
// DON'T USE — unpredictable, slow, deprecated
@Override
protected void finalize() throws Throwable {
    // Called by GC before reclaiming object — maybe. Eventually. Or never.
    closeConnection();
}
```

**Why it's bad:**
1. **No guarantee it runs** — GC may never collect the object
2. **Performance penalty** — Objects with finalizers go to a separate queue, survive an extra GC cycle
3. **Resurrection risk** — Object can make itself reachable again inside finalize()
4. **Thread-unsafe** — Finalizer runs on a random GC thread

**Modern alternatives:**
| Instead of finalize() | Use |
|----------------------|-----|
| Close resources | `try-with-resources` + `AutoCloseable` |
| Custom cleanup | `Cleaner` (Java 9+) — safer, registered action |
| Release native memory | `Cleaner` or explicit `close()` method |

```java
// Modern approach — try-with-resources
try (var conn = dataSource.getConnection();
     var stmt = conn.prepareStatement(sql)) {
    // auto-closed even on exception
}

// Java 9+ Cleaner for native resources
public class NativeResource implements AutoCloseable {
    private static final Cleaner cleaner = Cleaner.create();
    private final Cleaner.Cleanable cleanable;

    public NativeResource() {
        long ptr = allocateNative();
        cleanable = cleaner.register(this, () -> freeNative(ptr));
    }

    @Override
    public void close() { cleanable.clean(); }
}
```

**Interview answer:** "finalize() is deprecated since Java 9 because it's unreliable — no guarantee when or if it runs, it adds GC overhead, and it's not thread-safe. I use try-with-resources for all closeable resources, and Cleaner API for native memory cleanup as a safety net."
