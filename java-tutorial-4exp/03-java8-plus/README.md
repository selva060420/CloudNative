# Java 8+ Features — Interview Guide

---

## 1. Definition

Java 8 (2014) introduced functional programming constructs — lambdas, streams, and functional interfaces — enabling declarative, concise code. Subsequent releases added `Optional` improvements, `var`, records, sealed classes, pattern matching, and virtual threads (Java 21).

---

## 2. Why This Is Needed

| Problem (Pre-Java 8) | Solution (Java 8+) |
|----------------------|---------------------|
| Verbose anonymous classes for callbacks | Lambda expressions |
| No standard way to represent "no value" | `Optional<T>` |
| Imperative loops for filter/map/reduce | Stream API |
| Date/time API was broken (`java.util.Date` mutable, not thread-safe) | `java.time` package |
| No way to add methods to interfaces without breaking implementations | Default methods |
| Blocking I/O with manual thread management | `CompletableFuture` |

---

## 3. How It Works Internally

### Lambda Expressions

- Compiled to **invokedynamic** bytecode (not anonymous inner classes)
- JVM generates implementation at runtime via `LambdaMetafactory`
- No extra `.class` file per lambda — more efficient than anonymous classes
- Captures only **effectively final** variables (no hidden outer reference unless needed)

### Stream Pipeline

```
Source → Intermediate ops (lazy) → Terminal op (triggers execution)
         filter, map, flatMap        collect, forEach, reduce, count
```

- **Lazy evaluation:** Intermediate operations build a pipeline, nothing executes until terminal op
- **Short-circuiting:** `findFirst()`, `limit()`, `anyMatch()` stop early
- **Spliterator:** Underlying mechanism for parallel streams — splits data for ForkJoinPool

### Optional

- Wrapper class: either contains a non-null value or is empty
- Forces explicit handling of absence — no more `null` checks scattered everywhere
- **Not serializable** — don't use as field types, only as return types

---

## 4. Real-World Example

### Microservice Request Processing Pipeline

```java
// Processing incoming 5G network events — filter, transform, batch
List<NetworkEvent> criticalAlerts = events.stream()
    .filter(e -> e.getSeverity() >= Severity.HIGH)
    .filter(e -> e.getTimestamp().isAfter(Instant.now().minus(Duration.ofMinutes(5))))
    .map(e -> enrichWithLocation(e))
    .sorted(Comparator.comparing(NetworkEvent::getSeverity).reversed())
    .collect(Collectors.toList());

// Async notification dispatch
CompletableFuture.allOf(
    criticalAlerts.stream()
        .map(alert -> CompletableFuture.runAsync(
            () -> notificationService.send(alert), executorService))
        .toArray(CompletableFuture[]::new)
).join();
```

---

## 5. Common Interview Questions

### Q1: What is a functional interface? Name the 4 core ones.

An interface with exactly **one abstract method** (can have default/static methods). Annotated with `@FunctionalInterface`.

| Interface | Method | Use Case |
|-----------|--------|----------|
| `Function<T,R>` | `R apply(T t)` | Transform: map operations |
| `Predicate<T>` | `boolean test(T t)` | Filter: conditions |
| `Consumer<T>` | `void accept(T t)` | Side effects: forEach, logging |
| `Supplier<T>` | `T get()` | Factory: lazy initialization |

### Q2: What's the difference between `map()` and `flatMap()`?

- `map()` — 1-to-1 transformation: `Stream<T>` → `Stream<R>`
- `flatMap()` — 1-to-many, flattens nested streams: `Stream<Stream<T>>` → `Stream<T>`

```java
// map: each order → one customer name
orders.stream().map(Order::getCustomerName)  // Stream<String>

// flatMap: each order → multiple items
orders.stream().flatMap(o -> o.getItems().stream())  // Stream<Item>
```

### Q3: Can streams be reused?

**No.** A stream can only be consumed once. Calling a terminal operation closes it. Attempting reuse throws `IllegalStateException`. Create a new stream from the source each time.

### Q4: What's the difference between `findFirst()` and `findAny()`?

- `findFirst()` — returns first element in encounter order (deterministic)
- `findAny()` — returns any element (non-deterministic, faster in parallel streams)

### Q5: Explain `Optional` best practices.

```java
// GOOD — return type
public Optional<User> findById(String id) { ... }

// GOOD — chaining
String city = user.getAddress()
    .map(Address::getCity)
    .orElse("Unknown");

// BAD — don't use as method parameter
public void process(Optional<String> name) { }  // Use overloading instead

// BAD — don't use as field
private Optional<String> nickname;  // Use nullable field + getter returns Optional

// BAD — don't use Optional.get() without check
optional.get();  // Throws NoSuchElementException — use orElse/orElseGet/orElseThrow
```

### Q6: `reduce()` vs `collect()` — when to use which?

- `reduce()` — immutable accumulation (sum, max, concatenation). Creates new object each step.
- `collect()` — mutable accumulation (building a list, map, string). Mutates a container. More efficient for collections.

```java
// reduce — each step creates new Integer
int sum = numbers.stream().reduce(0, Integer::sum);

// collect — mutates ArrayList in place
List<String> names = people.stream().map(Person::getName).collect(Collectors.toList());
```

### Q7: What are method references? Types?

Shorthand for lambdas that call a single method:

| Type | Syntax | Lambda Equivalent |
|------|--------|-------------------|
| Static | `Integer::parseInt` | `s -> Integer.parseInt(s)` |
| Instance (bound) | `str::toUpperCase` | `() -> str.toUpperCase()` |
| Instance (unbound) | `String::length` | `s -> s.length()` |
| Constructor | `ArrayList::new` | `() -> new ArrayList<>()` |

### Q8: Default methods — why and what's the diamond problem?

**Why:** Add new methods to interfaces without breaking existing implementations (backward compatibility). Example: `List.forEach()`, `Map.getOrDefault()`.

**Diamond problem:** If a class implements two interfaces with the same default method, it **must override** to resolve ambiguity:

```java
interface A { default void hello() { System.out.println("A"); } }
interface B { default void hello() { System.out.println("B"); } }
class C implements A, B {
    @Override
    public void hello() { A.super.hello(); }  // Explicit resolution
}
```

---

## 6. Tricky Edge Cases & Pitfalls

1. **Stream reuse** — Streams are single-use. Store the source (list), not the stream.
2. **Parallel stream + shared mutable state** — Race conditions. Never modify external state in parallel stream operations.
3. **`Optional.of(null)`** — Throws `NullPointerException`. Use `Optional.ofNullable()`.
4. **Lazy streams with side effects** — `peek()` may not execute if terminal op short-circuits.
5. **`Collectors.toMap()` with duplicate keys** — Throws `IllegalStateException`. Provide merge function: `toMap(k, v, (v1, v2) -> v1)`.
6. **`flatMap` + `Optional`** — `Optional.flatMap()` expects function returning `Optional`, not raw value.
7. **Infinite streams without limit** — `Stream.generate()` or `iterate()` without `limit()` runs forever.
8. **`forEach` ordering in parallel** — `forEachOrdered()` preserves order but kills parallelism benefit.

---

## 7. Comparison with Related Concepts

### Stream vs Collection

| Feature | Collection | Stream |
|---------|-----------|--------|
| Storage | Stores elements | Computes elements on demand |
| Consumption | Iterable multiple times | Single use |
| Laziness | Eager | Lazy (intermediate ops) |
| Modification | Can add/remove | Cannot modify source |
| Infinite | No (memory bound) | Yes (`Stream.generate()`) |

### `Optional` vs Null

| Aspect | Null | Optional |
|--------|------|----------|
| Intent | Ambiguous (error? empty? uninitialized?) | Explicit "may be absent" |
| Safety | NPE at runtime | Compile-time awareness |
| Chaining | Nested null checks | `map()`, `flatMap()`, `orElse()` |
| Performance | Zero cost | Object allocation (minor) |

### CompletableFuture vs Future

| Feature | Future | CompletableFuture |
|---------|--------|-------------------|
| Completion | Only by task | Can complete manually |
| Chaining | No | `thenApply`, `thenCompose`, `thenCombine` |
| Exception handling | `ExecutionException` wrapper | `exceptionally()`, `handle()` |
| Combining | Manual | `allOf()`, `anyOf()` |
| Blocking | `get()` only way | Non-blocking callbacks |

---

## 8. Performance Impact

| Scenario | Impact |
|----------|--------|
| Sequential stream vs for-loop | Stream ~5-10% slower (pipeline overhead). Negligible for I/O-bound work. |
| Parallel stream on small collections (<10K) | **Slower** — ForkJoinPool overhead exceeds benefit |
| Parallel stream on CPU-heavy + large data | 2-4x speedup on multi-core |
| `Optional` allocation | Minor GC pressure. JIT often eliminates via escape analysis. |
| `Collectors.toUnmodifiableList()` vs `toList()` | `toList()` (Java 16+) is slightly more optimized |
| Lambda vs anonymous class | Lambda is faster — no extra class loading, invokedynamic is JIT-friendly |

**Rule of thumb:** Use parallel streams only when: data > 10K elements, operation is CPU-intensive, source is easily splittable (ArrayList yes, LinkedList no), and no shared mutable state.

---

## 9. Trade-offs

| Use When | Avoid When |
|----------|-----------|
| Declarative data transformation pipelines | Simple 1-2 line loops (stream adds noise) |
| Chaining multiple operations (filter→map→collect) | Operations with side effects (use for-loop) |
| Parallel processing of large datasets | Small collections or I/O-bound tasks |
| `Optional` as return type for "may not exist" | `Optional` as method parameter or field |
| `CompletableFuture` for async orchestration | Simple synchronous calls |
| Method references for readability | Complex multi-line lambdas (extract to method) |

---

## 10. 30–60 Second Interview Answers

### "Explain Java 8 key features in 30 seconds"

> "Java 8 brought functional programming to Java. Lambdas let you pass behavior as data — no more verbose anonymous classes. The Stream API enables declarative data processing: filter, map, reduce on collections without manual loops. Functional interfaces like `Function`, `Predicate`, `Consumer` standardize single-method contracts. `Optional` eliminates null ambiguity. `CompletableFuture` enables non-blocking async pipelines. Default methods allow interface evolution without breaking implementations."

### "When would you use parallel streams?"

> "Only when three conditions are met: large dataset (10K+ elements), CPU-intensive operation per element, and the source supports efficient splitting like ArrayList. Never with shared mutable state, I/O operations, or LinkedList. In practice, I rarely use them in microservices because most work is I/O-bound — async with CompletableFuture is usually the right choice instead."

### "Explain CompletableFuture in 30 seconds"

> "CompletableFuture is Java's non-blocking async primitive. Unlike Future which only supports blocking `get()`, CompletableFuture lets you chain transformations with `thenApply`, compose dependent async calls with `thenCompose`, combine independent calls with `allOf`, and handle errors with `exceptionally`. It runs on ForkJoinPool by default or a custom executor. I use it to fan out parallel API calls — like fetching user profile, preferences, and notifications simultaneously — then combine results."

---

## 11. Real Production Scenario

**Scenario:** Notification service processes 50K events/minute. Original code used nested for-loops with if-conditions to filter, transform, and batch notifications.

**Problem:** Code was 80 lines, hard to read, and couldn't be parallelized without major refactoring. Adding a new filter condition meant touching multiple nested blocks.

**Solution with Streams:**

```java
Map<NotificationType, List<Notification>> batched = events.stream()
    .filter(Event::isActive)
    .filter(e -> e.getPriority() > threshold)
    .map(this::toNotification)
    .collect(Collectors.groupingBy(Notification::getType));

// Async dispatch per batch
batched.forEach((type, notifications) ->
    CompletableFuture.runAsync(() -> sender.sendBatch(type, notifications), executor));
```

**Result:** 80 lines → 10 lines. New filters are one-line additions. Async dispatch improved throughput 3x.

---

## 12. If This Fails — How to Debug

| Symptom | Root Cause | Fix |
|---------|-----------|-----|
| `NullPointerException` in stream | Null element in source collection | Filter nulls first: `.filter(Objects::nonNull)` |
| `IllegalStateException: stream already operated on` | Reusing a stream variable | Create new stream from source |
| `ConcurrentModificationException` in parallel stream | Modifying shared state | Use `collect()` or thread-safe accumulator |
| Silent data loss in `Collectors.toMap()` | Duplicate keys | Add merge function: `toMap(k, v, (a,b) -> a)` |
| Parallel stream slower than sequential | Small data or I/O-bound ops | Switch to sequential or use CompletableFuture |
| `CompletableFuture` hangs | All ForkJoinPool threads blocked | Use custom executor: `Executors.newFixedThreadPool(n)` |
| `Optional.get()` throws `NoSuchElementException` | Calling get() without checking | Use `orElse()`, `orElseGet()`, or `orElseThrow()` |
| Lambda captures stale variable | Variable reassigned after capture | Ensure effectively final, or use AtomicReference |

---

## Follow-Up Interview Questions

**Q1:** You have a microservice that calls 3 downstream APIs. How would you use `CompletableFuture` to call them in parallel, with a 2-second timeout, and return partial results if one fails?

**Answer:**

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

CompletableFuture<UserProfile> profileFuture = CompletableFuture
    .supplyAsync(() -> userService.getProfile(userId), executor)
    .orTimeout(2, TimeUnit.SECONDS)
    .exceptionally(ex -> UserProfile.defaultProfile());

CompletableFuture<List<Order>> ordersFuture = CompletableFuture
    .supplyAsync(() -> orderService.getOrders(userId), executor)
    .orTimeout(2, TimeUnit.SECONDS)
    .exceptionally(ex -> Collections.emptyList());

CompletableFuture<Preferences> prefsFuture = CompletableFuture
    .supplyAsync(() -> prefService.getPrefs(userId), executor)
    .orTimeout(2, TimeUnit.SECONDS)
    .exceptionally(ex -> Preferences.defaults());

CompletableFuture.allOf(profileFuture, ordersFuture, prefsFuture).join();

return new UserDashboard(profileFuture.join(), ordersFuture.join(), prefsFuture.join());
```

**Key points:** Each call has independent timeout + fallback. `allOf` waits for all. Partial results via `exceptionally()`. Custom executor avoids starving ForkJoinPool.

---

**Q2:** You're processing a CSV file with 10M rows. How would you use streams efficiently without loading everything into memory?

**Answer:**

```java
try (Stream<String> lines = Files.lines(Path.of("data.csv"))) {
    Map<String, Long> categoryCounts = lines
        .skip(1)  // header
        .map(line -> line.split(","))
        .filter(cols -> cols.length >= 3)
        .collect(Collectors.groupingBy(cols -> cols[2], Collectors.counting()));
}
```

**Key points:** `Files.lines()` is lazy — reads line by line, not entire file. `try-with-resources` closes the stream (and underlying file handle). Don't use `parallel()` here — file I/O is the bottleneck, not CPU. For truly large files, consider `BufferedReader` with custom batching.

---

## Practice Task

Build a demo that shows:
1. Lambda expressions with all 4 core functional interfaces
2. Stream pipeline: filter → map → collect with grouping
3. `Optional` chaining — nested object navigation without null checks
4. `CompletableFuture` — parallel async calls with timeout and error handling
5. Method references — all 4 types
6. `java.time` API — parsing, formatting, duration calculation

---

## Code Examples

All code is in `core-java-examples/src/main/java/com/interview/java8plus/`:

| File | Topic |
|------|-------|
| [LambdaDemo.java](../core-java-examples/src/main/java/com/interview/java8plus/LambdaDemo.java) | Lambdas, functional interfaces, method references |
| [StreamDemo.java](../core-java-examples/src/main/java/com/interview/java8plus/StreamDemo.java) | Stream API: filter, map, flatMap, collect, reduce |
| [OptionalDemo.java](../core-java-examples/src/main/java/com/interview/java8plus/OptionalDemo.java) | Optional chaining, orElse, flatMap |
| [CompletableFutureDemo.java](../core-java-examples/src/main/java/com/interview/java8plus/CompletableFutureDemo.java) | Async pipelines, allOf, timeout, error handling |
| [DateTimeDemo.java](../core-java-examples/src/main/java/com/interview/java8plus/DateTimeDemo.java) | java.time API: LocalDate, Instant, Duration, formatting |
