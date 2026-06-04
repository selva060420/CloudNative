# 07 — Spring Boot

## 1. Definition

Spring Boot is an opinionated framework built on top of the Spring Framework that simplifies the creation of production-ready, stand-alone Spring applications. It eliminates boilerplate configuration through **auto-configuration**, **starter dependencies**, and an **embedded server** — letting you focus on business logic instead of infrastructure wiring.

---

## 2. Why This Is Needed

| Problem (Plain Spring) | Spring Boot Solution |
|---|---|
| Hundreds of lines of XML/Java config | Auto-configuration detects classpath and configures beans automatically |
| Manual dependency version management | Starters provide curated, compatible dependency sets |
| External app server deployment (WAR) | Embedded Tomcat/Jetty/Undertow — run as JAR |
| No standard for health/metrics | Actuator provides production-ready endpoints out of the box |
| Environment-specific config is manual | Profiles + externalized config hierarchy |

---

## 3. How It Works Internally

### Auto-Configuration Flow

```
@SpringBootApplication
  ├── @SpringBootConfiguration  (= @Configuration)
  ├── @EnableAutoConfiguration  ← triggers auto-config
  │     └── reads META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
  │     └── each class has @Conditional* annotations
  │     └── only activates if conditions are met
  └── @ComponentScan            ← scans current package + sub-packages
```

**Step-by-step:**
1. `SpringApplication.run()` creates `ApplicationContext`
2. `@EnableAutoConfiguration` imports `AutoConfigurationImportSelector`
3. Selector reads all auto-configuration class names from `META-INF/spring/...imports` (Spring Boot 3.x) or `spring.factories` (2.x)
4. Each auto-config class is guarded by `@Conditional*` annotations
5. Only matching configs create beans → context is ready

### Bean Lifecycle

```
Constructor → @Autowired (DI) → @PostConstruct → InitializingBean.afterPropertiesSet()
    → custom init-method → READY → @PreDestroy → DisposableBean.destroy() → custom destroy-method
```

### Externalized Config Precedence (highest → lowest)

1. Command-line arguments (`--server.port=9090`)
2. `SPRING_APPLICATION_JSON`
3. OS environment variables
4. `application-{profile}.properties/yml`
5. `application.properties/yml`
6. `@PropertySource`
7. Default properties

---

## 4. Real-World Example

**Ericsson 5G NEF Platform:**
- Spring Boot auto-configures embedded Tomcat for the CAPIF API gateway
- `@Profile("prod")` activates Cassandra config; `@Profile("dev")` uses H2
- Actuator `/health` is exposed to Kubernetes liveness/readiness probes
- `@ConfigurationProperties` binds 3GPP-specific config (NEF endpoint URLs, token TTLs)
- Custom `@ConditionalOnProperty("nef.feature.throttling.enabled")` toggles rate-limiting bean

---

## 5. Common Interview Questions

### Q1: What is Spring Boot auto-configuration and how does it work?

Auto-configuration automatically configures Spring beans based on classpath dependencies and existing bean definitions. It uses `@Conditional` annotations to decide what to configure.

Example: If `spring-boot-starter-data-jpa` is on classpath AND no `DataSource` bean exists → auto-configures HikariCP DataSource.

**Key annotations:**
- `@ConditionalOnClass` — class exists on classpath
- `@ConditionalOnMissingBean` — no user-defined bean of that type
- `@ConditionalOnProperty` — property has specific value

### Q2: Difference between @Component, @Service, @Repository, @Controller?

| Annotation | Layer | Extra Behavior |
|---|---|---|
| `@Component` | Generic | Base stereotype, no extra behavior |
| `@Service` | Business | Semantic only — no extra behavior |
| `@Repository` | Persistence | Translates persistence exceptions to Spring `DataAccessException` |
| `@Controller` | Web | Enables `@RequestMapping`, returns view names |
| `@RestController` | Web | = `@Controller` + `@ResponseBody` |

### Q3: What are bean scopes in Spring?

| Scope | Description |
|---|---|
| `singleton` (default) | One instance per ApplicationContext |
| `prototype` | New instance every time requested |
| `request` | One per HTTP request (web only) |
| `session` | One per HTTP session (web only) |
| `application` | One per ServletContext |

**Pitfall:** Injecting a prototype bean into a singleton → always gets the same prototype instance. Fix: use `ObjectProvider<T>` or `@Lookup`.

### Q4: How does @Transactional work internally?

1. Spring creates a **proxy** (CGLIB or JDK dynamic proxy) around the bean
2. Proxy intercepts method call → opens transaction
3. Method executes
4. If no exception → commit; if unchecked exception → rollback

**Key attributes:**
- `propagation` — REQUIRED (default), REQUIRES_NEW, NESTED, etc.
- `isolation` — READ_COMMITTED (default for most DBs)
- `rollbackFor` — by default only rolls back on `RuntimeException` and `Error`

**Pitfall:** Self-invocation (`this.method()`) bypasses proxy → no transaction!

### Q5: What is Spring Boot Actuator?

Production-ready features exposed via HTTP/JMX endpoints:
- `/actuator/health` — app health (used by K8s probes)
- `/actuator/metrics` — Micrometer metrics
- `/actuator/info` — build info
- `/actuator/env` — environment properties
- `/actuator/beans` — all registered beans

Secured by default in production. Custom endpoints via `@Endpoint`.

### Q6: Explain Spring Boot Profiles

Profiles allow environment-specific configuration:
```yaml
# application-dev.yml
spring.datasource.url: jdbc:h2:mem:test

# application-prod.yml  
spring.datasource.url: jdbc:cassandra://cluster:9042/nef
```

Activation: `spring.profiles.active=prod` (env var, CLI arg, or in application.yml)

Beans can be profile-specific: `@Profile("prod")`.

### Q7: Constructor Injection vs Field Injection — Why Prefer Constructor?

**Constructor Injection (Recommended):**
```java
@Service
public class OrderService {
    private final PaymentGateway paymentGateway;

    public OrderService(PaymentGateway paymentGateway) { // @Autowired optional since 4.3
        this.paymentGateway = paymentGateway;
    }
}
```

**Field Injection (Avoid):**
```java
@Service
public class OrderService {
    @Autowired
    private PaymentGateway paymentGateway; // no final, needs reflection to test
}
```

| Aspect | Constructor | Field | Setter |
|---|---|---|---|
| Immutability | ✅ `final` fields | ❌ | ❌ |
| Testability | ✅ Pass mocks directly | ❌ Needs Spring context | ✅ |
| Required deps | Compile-time enforced | NPE at runtime | Depends |
| Circular deps | Fails fast at startup | Hides them | Hides them |
| Optional deps | ❌ Awkward | ❌ | ✅ `@Autowired(required=false)` |

**When to use what:**
- Required deps → Constructor injection (+ Lombok `@RequiredArgsConstructor`)
- Optional deps → Setter injection
- Test classes only → Field injection is acceptable

**Circular dependency fix:**
```java
public ServiceA(@Lazy ServiceB serviceB) { ... } // breaks the cycle
```

**30-sec answer:**
> "Constructor injection is preferred because it allows `final` fields (immutability), enforces required deps at compile time, and makes unit testing easy — just pass mocks via constructor, no Spring context needed. Field injection uses reflection, hides dependencies, and makes testing harder. Since Spring 4.3, `@Autowired` is optional on a single constructor."

### Q8: What is @ConfigurationProperties?

Type-safe binding of external properties to a POJO:
```java
@ConfigurationProperties(prefix = "nef.throttling")
public class ThrottlingProperties {
    private int maxRequests = 100;
    private Duration window = Duration.ofMinutes(1);
    // getters/setters
}
```
Requires `@EnableConfigurationProperties` or `@ConfigurationPropertiesScan`.

Advantages over `@Value`: type safety, validation with `@Validated`, IDE auto-complete via `spring-configuration-metadata.json`.

---

## 6. Tricky Edge Cases / Pitfalls

| Pitfall | Symptom | Fix |
|---|---|---|
| `@Transactional` on private method | Transaction not applied | Must be public (proxy can't intercept private) |
| Self-invocation bypasses proxy | Inner `@Transactional` call ignored | Extract to separate bean or use `AopContext.currentProxy()` |
| Prototype in singleton | Always same instance | Use `ObjectProvider<T>` or `@Scope(proxyMode=TARGET_CLASS)` |
| `@PostConstruct` + `@Transactional` | Transaction not active during init | Use `ApplicationReadyEvent` listener instead |
| Circular dependency | `BeanCurrentlyInCreationException` | Redesign (break cycle), or use `@Lazy` on one injection point |
| Auto-config order matters | Your bean overridden by auto-config | Use `@AutoConfigureBefore/After` or `@ConditionalOnMissingBean` |
| `@Value` with missing property | `IllegalArgumentException` at startup | Provide default: `@Value("${key:default}")` |
| DevTools classloader | `ClassCastException` in dev | Exclude problematic classes from restart |

---

## 7. Comparison with Related Concepts

### Spring vs Spring Boot vs Spring MVC

| Aspect | Spring Framework | Spring MVC | Spring Boot |
|---|---|---|---|
| What | IoC/DI container + ecosystem | Web MVC module of Spring | Opinionated Spring wrapper |
| Config | Manual (XML/Java) | Manual DispatcherServlet setup | Auto-configured |
| Server | External (Tomcat WAR) | External | Embedded |
| Use case | Full control needed | Web layer only | Rapid development |

### Spring Boot vs Quarkus vs Micronaut

| Aspect | Spring Boot | Quarkus | Micronaut |
|---|---|---|---|
| Startup time | ~2-5s | ~0.5-1s | ~0.5-1s |
| Memory | Higher (~200MB+) | Lower (~50-100MB) | Lower (~50-100MB) |
| Ecosystem | Largest | Growing | Growing |
| Native image | Supported (3.x + GraalVM) | First-class | First-class |
| Learning curve | Low (huge community) | Medium | Medium |
| Best for | Enterprise, existing teams | Cloud-native, serverless | Cloud-native, serverless |

---

## 8. Performance Impact

| Factor | Impact | Mitigation |
|---|---|---|
| Auto-config scanning | Slower startup (scans 100+ configs) | Exclude unused: `spring.autoconfigure.exclude` |
| Component scanning | Startup time grows with package size | Narrow `@ComponentScan` base packages |
| Bean creation (singleton) | One-time cost at startup | Use `@Lazy` for heavy beans not needed immediately |
| Actuator endpoints | Minimal runtime overhead | Expose only needed endpoints in prod |
| Embedded Tomcat threads | Default 200 threads, 8KB stack each | Tune `server.tomcat.threads.max` based on load |
| @Transactional proxy | Slight overhead per call | Negligible for most apps; avoid on read-only hot paths |
| Spring Boot 3.x AOT | Faster startup via ahead-of-time compilation | Use for serverless/container cold starts |

**Startup optimization checklist:**
1. Exclude unused auto-configurations
2. Use `@Lazy` on non-critical beans
3. Reduce component scan scope
4. Consider Spring Boot AOT or GraalVM native image for cold-start-sensitive deployments

---

## 9. Trade-offs

| When to Use Spring Boot | When NOT to Use |
|---|---|
| Enterprise backend services | Ultra-low-latency systems (consider Vert.x) |
| Team already knows Spring | Serverless with strict cold-start requirements (consider Quarkus) |
| Need rich ecosystem (Security, Data, Cloud) | Simple CLI tools or scripts |
| Microservices with Spring Cloud | Memory-constrained environments (<64MB) |
| Rapid prototyping | When you need full control over every dependency |

---

## 10. 30–60 Second Interview Answers

### "What is Spring Boot?"

> "Spring Boot is an opinionated framework on top of Spring that eliminates boilerplate configuration. It provides auto-configuration — which detects your classpath and configures beans automatically — starter dependencies for curated dependency sets, and an embedded server so you can run as a standalone JAR. In our 5G platform at Ericsson, it let us focus on business logic while Spring Boot handled Tomcat setup, Cassandra configuration, and health endpoints for Kubernetes probes."

### "How does auto-configuration work?"

> "When the app starts, `@EnableAutoConfiguration` triggers the import selector to read all auto-configuration classes from META-INF. Each class is guarded by `@Conditional` annotations — like `@ConditionalOnClass` or `@ConditionalOnMissingBean`. So if you have JPA on your classpath but haven't defined a DataSource, Spring Boot auto-configures HikariCP. If you define your own, the auto-config backs off. This is the 'opinionated defaults with easy overrides' philosophy."

### "Explain @Transactional"

> "Spring creates a proxy around your bean. When a `@Transactional` method is called, the proxy opens a transaction, executes the method, and commits on success or rolls back on unchecked exceptions. Key gotchas: it only works on public methods, self-invocation bypasses the proxy, and by default it only rolls back on RuntimeExceptions — you need `rollbackFor` for checked exceptions."

---

## 11. Real Production Scenario

**Problem:** At Ericsson, our NEF service had 8-second startup times in Kubernetes, causing slow rolling deployments and failed readiness probes during peak scaling events.

**Root cause analysis:**
1. Auto-configuration was scanning 130+ configs (many irrelevant — Kafka, MongoDB, etc.)
2. Component scan covered the entire `com.ericsson` package (200+ beans from shared libraries)
3. Eager initialization of Cassandra session pool (3s alone)

**Fix:**
```properties
# Exclude unused auto-configs
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,\
  org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration

# Narrow component scan
@ComponentScan(basePackages = "com.ericsson.nef")
```
```java
// Lazy-init Cassandra (only connects when first query hits)
@Bean @Lazy
public CqlSession cassandraSession() { ... }
```

**Result:** Startup dropped from 8s → 3.2s. Readiness probe passed within K8s default timeout. Rolling deployments became smooth.

---

## 12. If This Fails, How to Debug

| Symptom | Likely Cause | Debug Steps |
|---|---|---|
| `NoSuchBeanDefinitionException` | Bean not scanned or condition not met | 1. Check `@ComponentScan` base package 2. Run with `--debug` → prints auto-config report 3. Check `CONDITIONS EVALUATION REPORT` in logs |
| `BeanCurrentlyInCreationException` | Circular dependency | 1. Check constructor injection chain 2. Use `@Lazy` on one side 3. Redesign to break cycle |
| App starts but endpoint returns 404 | Controller not scanned or wrong mapping | 1. Verify controller is in scanned package 2. Check `@RequestMapping` path 3. Check `/actuator/mappings` |
| `@Transactional` not working | Proxy not applied | 1. Verify method is public 2. Check it's not self-invocation 3. Verify `@EnableTransactionManagement` (auto-configured with JPA starter) |
| Properties not loaded | Wrong file name or profile | 1. Check file is `application.yml` (not `Application.yml`) 2. Verify active profile 3. Check `/actuator/env` |
| Startup too slow | Too many auto-configs or eager beans | 1. Run with `--debug` to see what's configured 2. Add `spring.main.lazy-initialization=true` temporarily 3. Profile with Spring Startup Analyzer |
| `Port already in use` | Another instance running | 1. `lsof -i :8080` 2. Kill process or change `server.port` |

**Essential debug flags:**
```bash
# Full auto-configuration report
java -jar app.jar --debug

# Specific logging
logging.level.org.springframework.boot.autoconfigure=DEBUG
```

---

## Follow-Up Interview Questions

### FQ1: How would you create a custom Spring Boot starter for your team?

**Answer:**

A custom starter has two modules:

1. **`xxx-spring-boot-autoconfigure`** — contains the auto-configuration logic:
```java
@AutoConfiguration
@ConditionalOnClass(ThrottlingService.class)
@EnableConfigurationProperties(ThrottlingProperties.class)
public class ThrottlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingService throttlingService(ThrottlingProperties props) {
        return new ThrottlingService(props.getMaxRequests(), props.getWindow());
    }
}
```

Register in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`:
```
com.ericsson.nef.throttling.ThrottlingAutoConfiguration
```

2. **`xxx-spring-boot-starter`** — empty module that just pulls in the autoconfigure module + required dependencies in its `pom.xml`.

**Why two modules?** Separation of concerns — users depend on the starter, and the autoconfigure module can be excluded if they want manual config.

---

### FQ2: Explain Spring AOP — how proxies work and when to use AOP vs interceptors.

**Answer:**

Spring AOP uses **proxies** to intercept method calls:
- **JDK Dynamic Proxy** — if bean implements an interface (default for interface-based beans)
- **CGLIB Proxy** — subclasses the target class (default in Spring Boot since 2.x via `spring.aop.proxy-target-class=true`)

```java
@Aspect
@Component
public class LoggingAspect {
    @Around("@annotation(Timed)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        log.info("{} took {}ms", joinPoint.getSignature(), System.currentTimeMillis() - start);
        return result;
    }
}
```

**AOP vs Interceptors:**
| Aspect | Spring AOP | HandlerInterceptor |
|---|---|---|
| Scope | Any Spring bean method | HTTP requests only |
| Mechanism | Proxy-based | DispatcherServlet callback |
| Use case | Cross-cutting (logging, security, caching) | Request pre/post processing |
| Access to | Method args, return value | HttpServletRequest/Response |

**When to use AOP:** Logging, metrics, security checks, retry logic, transaction management — anything that cuts across multiple classes.

---

## Practice Task

**Build a Spring Boot application that demonstrates:**

1. A custom `@ConfigurationProperties` class for rate-limiting config
2. A `@ConditionalOnProperty` bean that only activates when `app.ratelimit.enabled=true`
3. An AOP aspect that logs BEFORE/AFTER any method call in a service
4. A custom Actuator endpoint `/actuator/ratelimit` that shows current config
5. Profile-specific behavior: `dev` profile disables rate limiting, `prod` enables it

**Acceptance criteria:**
- Run with `--spring.profiles.active=prod` → rate limiter active, `/actuator/ratelimit` shows config
- Run with `--spring.profiles.active=dev` → rate limiter bean not created
- AOP aspect logs BEFORE/AFTER method calls at startup via `CommandLineRunner`

---

## Code Examples

See `spring-boot-examples/src/main/java/com/interview/springboot/`:
- `SpringBootDemoApp.java` — main application class
- `BeanLifecycleDemo.java` — demonstrates full bean lifecycle callbacks
- `AutoConfigConditionalDemo.java` — shows @Conditional* in action
- `AopTimingDemo.java` — AOP aspect with BEFORE/AFTER logging, auto-runs at startup
- `CustomActuatorEndpoint.java` — custom /actuator endpoint


---

## Logging & Log Levels

### Logging Stack in Spring Boot

```
Your Code → SLF4J (API/Facade) → Logback (Default Implementation) → Console/File/ELK
```

Spring Boot uses **SLF4J** as the facade and **Logback** as the default implementation. No configuration needed — it works out of the box.

### Log Levels (Severity Order)

| Level | When to Use | Example |
|-------|-------------|---------|
| `TRACE` | Very detailed debugging (method entry/exit) | `log.trace("Entering method with param={}", p)` |
| `DEBUG` | Development debugging, not for production | `log.debug("Cache hit for key={}", key)` |
| `INFO` | Normal operations, milestones | `log.info("Service started on port {}", port)` |
| `WARN` | Potential problem, system can recover | `log.warn("Retry attempt {} for service {}", count, svc)` |
| `ERROR` | Failure requiring attention | `log.error("Payment failed for order={}", id, exception)` |
| `FATAL` | System unusable (rarely used in SLF4J) | Application crash |

**Rule:** Production runs at `INFO`. Setting `DEBUG` in prod generates massive logs and impacts performance.

### Spring Boot Configuration

```properties
# application.properties
logging.level.root=INFO
logging.level.com.ericsson.nef=DEBUG
logging.level.org.springframework.web=WARN
logging.level.org.hibernate.SQL=DEBUG

# Log to file
logging.file.name=app.log
logging.file.max-size=10MB
logging.file.max-history=30

# Pattern
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### Best Practices

```java
// Use SLF4J with Lombok
@Slf4j
@Service
public class OrderService {

    public Order createOrder(OrderRequest req) {
        log.info("Creating order for user={}", req.getUserId()); // parameterized (no string concat)

        try {
            Order order = processOrder(req);
            log.info("Order created: id={}, total={}", order.getId(), order.getTotal());
            return order;
        } catch (PaymentException e) {
            log.error("Payment failed for user={}, amount={}", req.getUserId(), req.getAmount(), e);
            // Pass exception as LAST arg — Logback prints stack trace
            throw e;
        }
    }
}
```

**Key rules:**
1. **Use parameterized messages** — `log.info("x={}", x)` not `log.info("x=" + x)` (avoids string concat when level is disabled)
2. **Pass exception as last argument** — stack trace is printed automatically
3. **Never log sensitive data** — no passwords, tokens, PII
4. **Use MDC for correlation** — `MDC.put("correlationId", id)` appears in every log line for that request
5. **Don't log and throw** — either log OR throw, not both (causes duplicate log entries)

### MDC (Mapped Diagnostic Context) for Microservices

```java
// Filter that sets correlation ID for every request
@Component
public class CorrelationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        String correlationId = Optional.ofNullable(req.getHeader("X-Correlation-ID"))
            .orElse(UUID.randomUUID().toString());
        MDC.put("correlationId", correlationId);
        try {
            chain.doFilter(req, res);
        } finally {
            MDC.clear(); // MUST clear — thread pool reuses threads
        }
    }
}

// logback-spring.xml pattern includes MDC
// %d [%X{correlationId}] %-5level %logger - %msg%n
// Output: 2024-01-15 10:30:45 [abc-123-def] INFO  OrderService - Order created: id=456
```

### Log Levels — Interview Answer (30 sec)

> "SLF4J defines TRACE, DEBUG, INFO, WARN, ERROR in increasing severity. In production we run at INFO — it captures normal operations and problems without the noise of DEBUG. I use parameterized logging to avoid string concatenation overhead, MDC for request correlation across microservices, and structured logging (JSON) for ELK/Splunk ingestion. The key rule: log at the right level — ERROR means someone needs to act, WARN means something might become a problem, INFO is business events."
