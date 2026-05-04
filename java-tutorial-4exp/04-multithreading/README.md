# 04 — Multithreading & Concurrency

> Interview-ready guide for a 3+ year Java backend engineer targeting mid-senior roles.

---

## 1. Definition

**Multithreading** allows a Java program to execute multiple threads concurrently within a single process, sharing the same heap memory but maintaining separate stacks.

| Concept | One-liner |
|---------|-----------|
| **Thread** | Lightweight unit of execution within a process |
| **synchronized** | Intrinsic lock — only one thread enters the critical section at a time |
| **volatile** | Guarantees visibility of variable changes across threads (no caching in CPU registers) |
| **ReentrantLock** | Explicit lock with tryLock, fairness, and interruptible acquire |
| **ExecutorService** | Thread pool that manages thread lifecycle and task submission |
| **CompletableFuture** | Async computation with chaining, combining, and exception handling |
| **ThreadLocal** | Per-thread isolated variable — no sharing, no synchronization needed |
| **Deadlock** | Two+ threads waiting for each other's locks — system freezes |
| **Race Condition** | Non-deterministic result because threads access shared state without synchronization |

---

## 2. Why This Is Needed

| Problem | Concurrency Solution |
|---------|---------------------|
| REST API handling 1000 concurrent requests | Thread pool (ExecutorService) manages worker threads |
| Kafka consumer processing messages in parallel | CompletableFuture for async downstream calls |
| Shared counter incremented by multiple threads | synchronized or AtomicInteger |
| Request-scoped user context in microservice | ThreadLocal stores per-request data |
| Database connection pool exhausted | Proper thread pool sizing + lock timeout |
| K8s pod CPU spike during traffic burst | Bounded thread pools prevent thread explosion |
| Feature flag read by all threads, updated rarely | volatile for visibility without locking |

---

## 3. How It Works Internally

### Thread Lifecycle (States)

```
NEW → RUNNABLE → RUNNING → (BLOCKED | WAITING | TIMED_WAITING) → TERMINATED
```

```
Thread t = new Thread(task);  // NEW
t.start();                     // RUNNABLE (ready for scheduler)
// Scheduler picks it          // RUNNING
synchronized(lock) {}          // BLOCKED (waiting for monitor)
lock.wait();                   // WAITING (indefinite)
Thread.sleep(1000);            // TIMED_WAITING
// run() completes             // TERMINATED
```

### Java Memory Model (JMM) — Happens-Before

```
Thread 1 (CPU Core 1)          Thread 2 (CPU Core 2)
┌─────────────────┐            ┌─────────────────┐
│ Local Cache (L1) │            │ Local Cache (L1) │
│   x = 42        │            │   x = 0 (stale!) │
└────────┬────────┘            └────────┬────────┘
         │                              │
         └──────── Main Memory ─────────┘
                    x = 42
```

**volatile** forces read/write directly to main memory — no caching.

### synchronized Internals

```
Object header (Mark Word):
┌──────────────────────────────────────┐
│ [lock bits] [thread ID] [epoch] [age]│
└──────────────────────────────────────┘

Lock escalation: Biased → Lightweight (CAS) → Heavyweight (OS mutex)
```

### ExecutorService Architecture

```
Task Queue (BlockingQueue)
    ↓
┌─────────────────────────────┐
│  Thread Pool                 │
│  [Worker-1] [Worker-2] ...  │
│  corePoolSize → maxPoolSize │
└─────────────────────────────┘
    ↓
RejectedExecutionHandler (if queue full + max threads reached)
```

---

## 4. Real-World Example

### NEF Notification Dispatcher (Ericsson 5G)

```java
// NEF receives 5G event → fans out notifications to multiple subscribers
@Service
public class NotificationDispatcher {
    private final ExecutorService pool = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() * 2
    );

    public CompletableFuture<Void> dispatchAll(Event event, List<Subscriber> subs) {
        CompletableFuture<?>[] futures = subs.stream()
            .map(sub -> CompletableFuture.runAsync(
                () -> notifySubscriber(sub, event), pool))
            .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }
}
```

### ThreadLocal for Request Correlation ID

```java
public class CorrelationContext {
    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    public static void set(String id) { CORRELATION_ID.set(id); }
    public static String get() { return CORRELATION_ID.get(); }
    public static void clear() { CORRELATION_ID.remove(); } // MUST clear in finally!
}
```

---

## 5. Common Interview Questions

### Q1: What's the difference between `synchronized` and `ReentrantLock`?

| Feature | synchronized | ReentrantLock |
|---------|-------------|---------------|
| Lock acquisition | Implicit (enter block) | Explicit (`lock()`) |
| Try without blocking | ❌ | ✅ `tryLock()` |
| Fairness | No guarantee | Configurable |
| Interruptible | ❌ | ✅ `lockInterruptibly()` |
| Multiple conditions | One wait-set | Multiple `Condition` objects |
| Auto-release | ✅ (block exit) | ❌ (must `unlock()` in finally) |

### Q2: How does `volatile` differ from `synchronized`?

- **volatile**: Guarantees visibility only. No atomicity for compound operations (i++ is NOT atomic with volatile).
- **synchronized**: Guarantees both visibility AND atomicity (mutual exclusion).

### Q3: What causes a deadlock? How to prevent it?

**Conditions (all 4 must hold):**
1. Mutual exclusion
2. Hold and wait
3. No preemption
4. Circular wait

**Prevention:** Lock ordering (always acquire locks in same global order), timeout with `tryLock(timeout)`, avoid nested locks.

### Q4: `wait()` vs `sleep()` vs `yield()`?

| Method | Releases lock? | Resumes when? | Called on |
|--------|---------------|---------------|-----------|
| `wait()` | ✅ Yes | `notify()`/`notifyAll()` | Object (inside synchronized) |
| `sleep(ms)` | ❌ No | After timeout | Thread |
| `yield()` | ❌ No | Scheduler decides | Thread |

### Q5: Why use `CompletableFuture` over raw threads?

- Composable (thenApply, thenCompose, allOf)
- Exception handling (exceptionally, handle)
- No manual thread management
- Non-blocking chaining
- Integrates with ExecutorService

---

## 6. Tricky Edge Cases & Pitfalls

| Pitfall | What Happens | Fix |
|---------|-------------|-----|
| `ThreadLocal` not cleared in thread pool | Memory leak + stale data for next request | Always `remove()` in finally block |
| `synchronized` on non-final field | Lock object changes → threads enter simultaneously | Lock on `private final Object lock = new Object()` |
| Double-checked locking without volatile | Partially constructed object visible to other threads | Declare instance as `volatile` |
| `ExecutorService` not shut down | JVM won't exit, threads keep running | `shutdown()` + `awaitTermination()` in @PreDestroy |
| Catching `InterruptedException` and swallowing | Thread never knows it was interrupted | Re-interrupt: `Thread.currentThread().interrupt()` |
| Using `Executors.newCachedThreadPool()` in production | Unbounded thread creation under load → OOM | Use `ThreadPoolExecutor` with bounded queue |
| `ConcurrentHashMap` compound operations not atomic | Check-then-act race condition | Use `computeIfAbsent()`, `merge()` |

---

## 7. Comparison with Related Concepts

### Thread Pool Implementations

| Pool Type | Core Threads | Max Threads | Queue | Use Case |
|-----------|-------------|-------------|-------|----------|
| `FixedThreadPool` | n | n | Unbounded LinkedBlockingQueue | Predictable load |
| `CachedThreadPool` | 0 | Integer.MAX | SynchronousQueue | Short-lived bursts (dangerous!) |
| `ScheduledThreadPool` | n | Integer.MAX | DelayedWorkQueue | Periodic tasks |
| `WorkStealingPool` | CPU cores | CPU cores | Per-thread deque | CPU-bound parallel work |
| Custom `ThreadPoolExecutor` | configurable | configurable | Bounded | **Production recommendation** |

### Lock Types

| Lock | Reentrant | Read/Write | Stamped | Fairness |
|------|-----------|-----------|---------|----------|
| `synchronized` | ✅ | ❌ | ❌ | ❌ |
| `ReentrantLock` | ✅ | ❌ | ❌ | Optional |
| `ReentrantReadWriteLock` | ✅ | ✅ | ❌ | Optional |
| `StampedLock` | ❌ | ✅ | ✅ (optimistic) | ❌ |

---

## 8. Performance Impact

| Scenario | Impact | Numbers |
|----------|--------|---------|
| Uncontended synchronized | ~20ns (biased lock) | Negligible |
| Contended synchronized (10 threads) | Context switches + OS mutex | 10-100x slower |
| volatile read | ~same as normal read on x86 | Memory barrier on write |
| Thread creation (new Thread()) | ~1ms + 512KB-1MB stack | Use pools instead |
| ThreadPoolExecutor (bounded) | Amortized thread reuse | 1000x cheaper than new Thread() |
| CAS (AtomicInteger) | Lock-free, CPU spin | Best for low contention |
| Lock contention in K8s pod | CPU throttled → latency spike | Size pool to `availableProcessors * 2` for I/O |

### Thread Pool Sizing Formula

```
CPU-bound tasks:  threads = number of cores
I/O-bound tasks:  threads = cores * (1 + wait_time / compute_time)
                  Example: 4 cores, 80% I/O wait → 4 * (1 + 4) = 20 threads
```

---

## 9. Trade-offs

| Approach | Pros | Cons | When to Use |
|----------|------|------|-------------|
| synchronized | Simple, auto-release | No timeout, no fairness | Simple critical sections |
| ReentrantLock | Flexible, tryLock, fair | Must manually unlock | Complex locking scenarios |
| volatile | Lightweight, no blocking | Only visibility, not atomicity | Flags, status variables |
| AtomicInteger/Long | Lock-free, fast | Only single-variable ops | Counters, sequences |
| CompletableFuture | Composable, non-blocking | Complex error propagation | Async I/O orchestration |
| Virtual Threads (Java 21) | Millions of threads, cheap | New — ecosystem catching up | High-concurrency I/O |

---

## 10. 30–60 Second Interview Answers

### "Explain Java's memory model in 30 seconds"

> "The Java Memory Model defines how threads interact through memory. Each thread can cache variables locally. Without synchronization, one thread's write may not be visible to another. The JMM provides happens-before guarantees: synchronized blocks, volatile writes, and thread starts establish visibility. In practice, this means if I write to a volatile variable, any thread reading it afterward sees the updated value."

### "How do you handle concurrency in microservices?"

> "At the application level, I use ExecutorService with bounded thread pools sized to the workload — typically cores × 2 for I/O-bound tasks like HTTP calls. For async orchestration, CompletableFuture chains let me fan out parallel calls and combine results. I use ThreadLocal for request-scoped context like correlation IDs, always clearing in a finally block. For shared state, I prefer AtomicReference or ConcurrentHashMap over explicit locking. In Kubernetes, I set thread pool sizes relative to CPU limits to avoid throttling."

### "What's a deadlock and how do you prevent it?"

> "A deadlock occurs when two or more threads each hold a lock the other needs, creating a circular wait. All four conditions must hold: mutual exclusion, hold-and-wait, no preemption, and circular wait. I prevent it by enforcing a global lock ordering — always acquire lock A before lock B. In production, I use tryLock with a timeout so threads back off instead of waiting forever. For detection, I use thread dumps or JMX to find BLOCKED threads."

---

## 11. Real Production Scenario

### The Ericsson NEF Thread Pool Exhaustion Incident

**Context:** NEF notification service dispatching 5G event callbacks to subscribers.

**Symptom:** Response times spiked from 50ms to 30s during peak traffic. K8s pod CPU at 100%. Health check failing.

**Root Cause:** Used `Executors.newCachedThreadPool()` — under burst traffic (5000 events/sec), it created 3000+ threads. Each thread consumed 1MB stack. Pod hit memory limit → GC thrashing → CPU spike.

**Fix:**
```java
// Before (dangerous)
ExecutorService pool = Executors.newCachedThreadPool();

// After (production-safe)
ExecutorService pool = new ThreadPoolExecutor(
    8,                          // corePoolSize (= CPU cores)
    32,                         // maxPoolSize (bounded)
    60L, TimeUnit.SECONDS,      // idle thread keepalive
    new LinkedBlockingQueue<>(1000),  // bounded queue
    new ThreadFactoryBuilder().setNameFormat("nef-notify-%d").build(),
    new ThreadPoolExecutor.CallerRunsPolicy()  // backpressure
);
```

**Result:** Stable at 5000 events/sec, max 32 threads, queue provides backpressure, CallerRunsPolicy slows producers when overloaded.

---

## 12. If This Fails, How to Debug

| Symptom | Likely Cause | Debug Approach |
|---------|-------------|----------------|
| Application hangs, no CPU usage | Deadlock | `jstack <pid>` → look for "BLOCKED" cycles |
| Intermittent wrong results | Race condition | Add `-XX:+UseThreadSanitizer` or stress test with many threads |
| Memory grows over time in thread pool | ThreadLocal leak | Heap dump → find ThreadLocalMap entries |
| `RejectedExecutionException` | Queue full + max threads reached | Increase queue size or add backpressure |
| Thread count keeps growing | Unbounded pool or threads not terminating | `jcmd <pid> Thread.print` + check pool config |
| CPU 100% but low throughput | Lock contention | `async-profiler` lock profiling → find hot lock |

### Thread Dump Analysis

```bash
# Get thread dump
jstack <pid> > thread_dump.txt

# Find deadlocks
grep -A 5 "Found one Java-level deadlock" thread_dump.txt

# Count thread states
grep "java.lang.Thread.State" thread_dump.txt | sort | uniq -c

# In Kubernetes
kubectl exec <pod> -- jstack 1 > dump.txt
```

---

## Follow-up Interview Questions

**Q1:** "You have a microservice making 10 parallel HTTP calls to downstream services. How would you implement this with proper timeout and error handling?"

**A:** Use `CompletableFuture.supplyAsync()` with a bounded ExecutorService. Apply `orTimeout(2, SECONDS)` per call. Use `allOf()` to wait for all, with `exceptionally()` on each to provide fallback. If any call is critical, use `anyOf()` pattern for fast-fail.

**Q2:** "How would you implement a rate limiter that's thread-safe?"

**A:** Use `AtomicInteger` for a simple counter with `compareAndSet()` loop, or `Semaphore` with `tryAcquire(timeout)`. For sliding window, use `ConcurrentLinkedDeque` with timestamps. In production, prefer Resilience4j's `RateLimiter` which handles the concurrency internally.

---

## Practice Task

Implement a thread-safe bounded buffer (producer-consumer) using:
1. `synchronized` + `wait()`/`notifyAll()`
2. `ReentrantLock` + `Condition`
3. `BlockingQueue`

Compare their behavior under 10 producers and 10 consumers with a buffer size of 5.

### Solution

**See:** `BoundedBufferDemo.java` — full runnable implementation with benchmark.

**Key design decisions:**

| Approach | Wake Strategy | Fairness | Boilerplate |
|----------|--------------|----------|-------------|
| `synchronized + wait/notifyAll` | Wakes ALL waiting threads (thundering herd) | No | Low |
| `ReentrantLock + Condition` | Separate `notFull`/`notEmpty` → wakes only relevant threads | Configurable | Medium |
| `BlockingQueue` | JDK-optimized (uses Lock + 2 Conditions internally) | Optional | Zero |

**Behavior under 10P/10C/buffer=5:**
- `synchronized`: Higher contention — `notifyAll()` wakes all 20 threads even when only 1 slot opens
- `ReentrantLock`: Better throughput — `signal()` wakes exactly 1 producer OR 1 consumer
- `BlockingQueue`: Best throughput — same as Lock approach but with JDK-level optimizations

**Production recommendation:** Always use `BlockingQueue` unless you need custom behavior (e.g., priority, batching, metrics hooks).

---

## Code Examples

See runnable demos in:
```
core-java-examples/src/main/java/com/interview/multithreading/
├── ThreadLifecycleDemo.java      — Thread states and transitions
├── SynchronizationDemo.java      — synchronized, volatile, AtomicInteger
├── LocksDemo.java                — ReentrantLock, ReadWriteLock, tryLock
├── ExecutorServiceDemo.java      — Thread pools, custom ThreadPoolExecutor
├── CompletableFutureDemo.java    — Async chaining, combining, error handling
├── DeadlockDemo.java             — Deadlock creation and detection
├── BoundedBufferDemo.java        — Producer-consumer: 3 approaches + benchmark
```
