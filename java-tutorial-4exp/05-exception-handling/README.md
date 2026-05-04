# 05 — Exception Handling

---

## 1. Definition

Exception handling is Java's mechanism for dealing with runtime errors and abnormal conditions. It separates error-handling code from normal logic using `try-catch-finally` blocks and a class hierarchy rooted at `Throwable`.

---

## 2. Why This Is Needed

- **Prevents silent failures** — forces callers to handle or propagate errors
- **Separates concerns** — business logic stays clean, error handling is explicit
- **Enables recovery** — retry, fallback, or graceful degradation instead of crashing
- **Provides context** — stack traces, custom messages, chained causes for debugging

---

## 3. How It Works Internally

```
Throwable
├── Error (JVM-level, don't catch)
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── NoClassDefFoundError
└── Exception
    ├── Checked (compile-time enforced)
    │   ├── IOException
    │   ├── SQLException
    │   └── InterruptedException
    └── RuntimeException (unchecked)
        ├── NullPointerException
        ├── IllegalArgumentException
        ├── IllegalStateException
        └── ConcurrentModificationException
```

**JVM flow when exception is thrown:**
1. JVM creates exception object with stack trace (expensive — walks entire call stack)
2. Searches current method's exception table for matching handler
3. If no match, unwinds stack frame by frame until handler found
4. If no handler found anywhere, thread terminates (UncaughtExceptionHandler called)
5. `finally` blocks execute during unwinding regardless of catch

---

## 4. Real-World Example

**Microservice calling downstream API with retry:**

```java
@Service
public class PaymentService {

    public PaymentResponse processPayment(PaymentRequest req) {
        try {
            return paymentGateway.charge(req);
        } catch (TimeoutException e) {
            // Retry with exponential backoff
            return retryWithBackoff(() -> paymentGateway.charge(req), 3);
        } catch (InsufficientFundsException e) {
            // Business error — don't retry, return meaningful response
            throw new PaymentFailedException("Insufficient funds", e);
        } catch (Exception e) {
            // Unknown error — log, alert, return generic failure
            log.error("Payment failed unexpectedly for orderId={}", req.getOrderId(), e);
            throw new ServiceUnavailableException("Payment service error", e);
        }
    }
}
```

---

## 5. Common Interview Questions

**Q: Checked vs Unchecked — when to use which?**

| Checked | Unchecked |
|---------|-----------|
| Caller can reasonably recover | Programming error / bug |
| External system failure (IO, network) | Null dereference, bad argument |
| Compiler forces handling | No compile-time enforcement |
| `extends Exception` | `extends RuntimeException` |

**Rule of thumb:** If the caller can do something about it → checked. If it's a bug → unchecked.

**Q: What's the difference between `throw` and `throws`?**

- `throw` — actually throws an exception instance: `throw new IllegalArgumentException("bad")`
- `throws` — declares that a method might throw: `void read() throws IOException`

**Q: Can you catch `Error`?**

Technically yes, but you shouldn't. `Error` indicates JVM-level problems (OOM, StackOverflow) where recovery is usually impossible. Exception: catching `OutOfMemoryError` in a specific thread to log before shutdown.

**Q: What happens if exception is thrown in `finally`?**

It suppresses the original exception. The `finally` exception is what propagates. Use try-with-resources to avoid this — suppressed exceptions are tracked via `getSuppressed()`.

**Q: Order of catch blocks?**

Most specific first. Compiler rejects catch of parent before child (unreachable code). Multi-catch: `catch (IOException | SQLException e)` — `e` is effectively final.

---

## 6. Tricky Edge Cases or Pitfalls

**1. Swallowing exceptions:**
```java
// NEVER do this
try { riskyOperation(); }
catch (Exception e) { } // Silent failure — impossible to debug in production
```

**2. Catching too broadly:**
```java
// Catches NPE, ClassCast, etc. — hides bugs
try { process(); }
catch (Exception e) { log.warn("failed"); }
```

**3. Exception in finally overrides original:**
```java
try {
    throw new IOException("original");
} finally {
    throw new RuntimeException("oops"); // IOException is LOST
}
```

**4. Checked exception from lambda:**
```java
// Won't compile — Function doesn't declare checked exceptions
list.stream().map(item -> {
    return Files.readString(Path.of(item)); // IOException — compile error
}).toList();

// Fix: wrap in unchecked
list.stream().map(item -> {
    try { return Files.readString(Path.of(item)); }
    catch (IOException e) { throw new UncheckedIOException(e); }
}).toList();
```

**5. `finally` return swallows exception:**
```java
int getValue() {
    try { throw new RuntimeException("error"); }
    finally { return 42; } // Exception silently swallowed, returns 42
}
```

---

## 7. Comparison with Related Concepts

| Feature | try-catch-finally | try-with-resources | Functional (Optional/Either) |
|---------|-------------------|-------------------|------------------------------|
| Auto-close resources | No (manual in finally) | Yes (AutoCloseable) | N/A |
| Suppressed exceptions | Lost | Tracked via getSuppressed() | N/A |
| Checked exception support | Yes | Yes | No (must wrap) |
| Boilerplate | High | Low | Lowest |
| Use case | General error handling | IO/DB connections | Absence of value, chaining |

| Pattern | Spring Boot Approach |
|---------|---------------------|
| Per-controller try-catch | `@ExceptionHandler` on controller |
| Global error handling | `@ControllerAdvice` + `@ExceptionHandler` |
| Validation errors | `@Valid` + `MethodArgumentNotValidException` |
| Custom error response | `ProblemDetail` (RFC 7807, Spring 6+) |

---

## 8. Performance Impact

| Operation | Cost |
|-----------|------|
| `try` block (no exception) | Near zero — JVM uses exception tables, not runtime checks |
| Throwing exception | **Expensive** — `fillInStackTrace()` walks entire call stack |
| Deep stack trace (50+ frames) | 5-10μs per throw |
| Exception for control flow | 100-1000x slower than if/else |

**Optimization tips:**
- Never use exceptions for control flow (e.g., `Integer.parseInt` in a loop — use `tryParse` pattern)
- Override `fillInStackTrace()` returning `this` for high-frequency business exceptions where stack trace isn't needed
- Pre-create singleton exceptions for known cases (like `INSTANCE` pattern)

```java
// High-performance exception — no stack trace overhead
public class RateLimitExceededException extends RuntimeException {
    public static final RateLimitExceededException INSTANCE = new RateLimitExceededException();

    private RateLimitExceededException() { super("Rate limit exceeded", null, true, false); }
    // writableStackTrace=false → no fillInStackTrace() cost
}
```

---

## 9. Trade-offs

| Use Checked Exceptions When | Use Unchecked Exceptions When |
|-----------------------------|-------------------------------|
| Caller MUST handle (file not found, network timeout) | Indicates a bug (NPE, illegal arg) |
| Recovery is possible | Caller can't reasonably recover |
| API boundary — force awareness | Internal implementation detail |
| Few callers in the chain | Exception would pollute every method signature |

**Modern trend:** Spring, Hibernate, and most frameworks use unchecked exceptions exclusively. Checked exceptions add coupling — every intermediate method must declare `throws`.

---

## 10. 30–60 Second Interview Answers

**"Explain Java exception handling":**
> "Java has a Throwable hierarchy: Errors for JVM problems you don't catch, checked exceptions for recoverable conditions the compiler forces you to handle, and unchecked RuntimeExceptions for programming bugs. I use try-with-resources for anything AutoCloseable, custom unchecked exceptions with meaningful messages for business errors, and @ControllerAdvice in Spring Boot for centralized error handling with RFC 7807 ProblemDetail responses."

**"Checked vs unchecked — your preference?":**
> "I follow the modern approach: unchecked for most cases. Checked exceptions create tight coupling — every method in the chain must declare them. I use custom RuntimeExceptions with clear names like OrderNotFoundException, and handle them centrally with @ControllerAdvice. The only time I'd use checked is at a library boundary where the caller absolutely must be aware of a failure mode."

---

## 11. Real Production Scenario

**Problem:** Ericsson NEF service returning 500 errors intermittently. Logs showed `NullPointerException` deep in the stack but no context about which request caused it.

**Root cause:** Generic catch block was logging `e.getMessage()` (which is null for NPE) instead of the full stack trace. The NPE was caused by a missing optional field in the 3GPP notification payload.

**Fix:**
1. Added `@Valid` + `@NotNull` annotations on the DTO to fail fast at controller level
2. Created `NefValidationException` with the field name and request correlation ID
3. Added `@ControllerAdvice` returning `ProblemDetail` with trace ID for debugging
4. Changed logging to always include full exception: `log.error("msg", e)` not `log.error(e.getMessage())`

**Lesson:** Never log just `getMessage()` — always pass the exception object as the last argument to the logger.

---

## 12. If This Fails, How to Debug

| Symptom | Root Cause | Fix |
|---------|-----------|-----|
| Silent failure, no logs | Swallowed exception (empty catch) | Grep for empty catch blocks, add logging |
| "null" in error message | Logging `e.getMessage()` for NPE | Log full exception: `log.error("ctx", e)` |
| Resource leak (connections exhausted) | Missing close in error path | Use try-with-resources |
| Original exception lost | Exception thrown in finally/catch | Chain with `addSuppressed()` or use try-with-resources |
| Stack trace missing | `fillInStackTrace()` overridden or `-XX:-OmitStackTraceInFastThrow` | JVM optimization for repeated exceptions — restart or disable flag |
| `ClassNotFoundException` at runtime | Checked exception not in classpath | Verify dependency versions, check fat JAR packaging |

**JVM flag:** `-XX:-OmitStackTraceInFastThrow` — JVM optimizes away stack traces for frequently thrown exceptions (NPE, ArrayIndexOutOfBounds). Disable this in production to always get full traces.

---

## Follow-up Interview Questions

**Q1:** "Your Spring Boot microservice needs to handle errors from 5 downstream services, each with different failure modes. How do you design the exception hierarchy?"

**Answer:**

```java
// Base exception for all service errors
public abstract class ServiceException extends RuntimeException {
    private final String serviceId;
    private final String traceId;

    protected ServiceException(String message, String serviceId, String traceId, Throwable cause) {
        super(message, cause);
        this.serviceId = serviceId;
        this.traceId = traceId;
    }

    public String getServiceId() { return serviceId; }
    public String getTraceId() { return traceId; }
}

// Retryable vs non-retryable — circuit breaker decides based on this
public class TransientServiceException extends ServiceException {
    public TransientServiceException(String msg, String svc, String trace, Throwable cause) {
        super(msg, svc, trace, cause);
    }
}

public class PermanentServiceException extends ServiceException {
    private final int httpStatus;
    public PermanentServiceException(String msg, String svc, String trace, int status) {
        super(msg, svc, trace, null);
        this.httpStatus = status;
    }
}

// Global handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TransientServiceException.class)
    public ProblemDetail handleTransient(TransientServiceException e) {
        ProblemDetail pd = ProblemDetail.forStatus(503);
        pd.setTitle("Service temporarily unavailable");
        pd.setProperty("serviceId", e.getServiceId());
        pd.setProperty("traceId", e.getTraceId());
        return pd;
    }

    @ExceptionHandler(PermanentServiceException.class)
    public ProblemDetail handlePermanent(PermanentServiceException e) {
        ProblemDetail pd = ProblemDetail.forStatus(e.getHttpStatus());
        pd.setTitle("Downstream service rejected request");
        pd.setProperty("serviceId", e.getServiceId());
        return pd;
    }
}
```

**Key points:**
- Split by recoverability: transient (retry) vs permanent (fail fast)
- Include correlation context (traceId, serviceId) for distributed debugging
- `@ControllerAdvice` maps to RFC 7807 ProblemDetail — standard error format
- Circuit breaker (Resilience4j) counts `TransientServiceException` toward open threshold

---

**Q2:** "How do you handle checked exceptions in Stream pipelines without making the code ugly?"

**Answer:**

```java
// Approach 1: Utility wrapper (most common)
@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;

    static <T, R> Function<T, R> unchecked(ThrowingFunction<T, R> f) {
        return t -> {
            try { return f.apply(t); }
            catch (Exception e) { throw new RuntimeException(e); }
        };
    }
}

// Usage:
list.stream()
    .map(ThrowingFunction.unchecked(item -> Files.readString(Path.of(item))))
    .toList();

// Approach 2: Collect successes and failures (Either pattern)
record Result<T>(T value, Exception error) {
    boolean isSuccess() { return error == null; }
    static <T> Result<T> success(T v) { return new Result<>(v, null); }
    static <T> Result<T> failure(Exception e) { return new Result<>(null, e); }
}

List<Result<String>> results = paths.stream()
    .map(p -> {
        try { return Result.success(Files.readString(p)); }
        catch (IOException e) { return Result.<String>failure(e); }
    })
    .toList();

List<String> successes = results.stream().filter(Result::isSuccess).map(Result::value).toList();
List<Exception> failures = results.stream().filter(r -> !r.isSuccess()).map(Result::error).toList();
```

**Key points:**
- Approach 1: simple, but loses the checked exception type info
- Approach 2: no data loss, process partial results, log failures separately
- In production: use Vavr's `Try` or `Either` for a battle-tested implementation

---

## Practice Task

Create a custom exception hierarchy for a REST API with:
1. Base `ApiException` with HTTP status, error code, and trace ID
2. `ResourceNotFoundException` (404)
3. `ValidationException` with field-level errors (400)
4. A `@ControllerAdvice` that maps them to RFC 7807 `ProblemDetail`
5. Demonstrate try-with-resources with a custom `AutoCloseable`

### Solution

```java
// --- Base exception ---
public abstract class ApiException extends RuntimeException {
    private final int status;
    private final String errorCode;
    private final String traceId;

    protected ApiException(String message, int status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.traceId = MDC.get("traceId"); // from Sleuth/Micrometer
    }

    public int getStatus() { return status; }
    public String getErrorCode() { return errorCode; }
    public String getTraceId() { return traceId; }
}

// --- 404 ---
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id, 404, "RESOURCE_NOT_FOUND");
    }
}

// --- 400 with field errors ---
public class ValidationException extends ApiException {
    private final Map<String, String> fieldErrors;

    public ValidationException(Map<String, String> fieldErrors) {
        super("Validation failed", 400, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
}

// --- Global handler ---
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handle(ApiException e) {
        ProblemDetail pd = ProblemDetail.forStatus(e.getStatus());
        pd.setTitle(e.getErrorCode());
        pd.setDetail(e.getMessage());
        pd.setProperty("traceId", e.getTraceId());
        if (e instanceof ValidationException ve) {
            pd.setProperty("fieldErrors", ve.getFieldErrors());
        }
        return pd;
    }
}

// --- try-with-resources with custom AutoCloseable ---
public class DatabaseConnection implements AutoCloseable {
    private boolean open = true;

    public DatabaseConnection() { System.out.println("Connection opened"); }

    public String query(String sql) {
        if (!open) throw new IllegalStateException("Connection closed");
        return "result";
    }

    @Override
    public void close() {
        open = false;
        System.out.println("Connection closed");  // always called, even on exception
    }
}

// Usage:
try (var conn = new DatabaseConnection()) {
    String result = conn.query("SELECT 1");
} // conn.close() called automatically — no leak even if query() throws
```

---

## Code Examples

See runnable demos in:
```
core-java-examples/src/main/java/com/interview/exceptions/
├── ExceptionHierarchyDemo.java   — Checked vs unchecked, hierarchy, multi-catch
├── TryWithResourcesDemo.java     — AutoCloseable, suppressed exceptions
├── CustomExceptionDemo.java      — API exception hierarchy + Spring-style handler
```
