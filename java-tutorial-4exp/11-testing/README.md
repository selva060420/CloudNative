# 11 — Testing in Java

## 1. Definition

**Software Testing** is the practice of verifying that code behaves as expected. In Java, the ecosystem centers around:

- **JUnit 5** — The standard unit testing framework (Jupiter API)
- **Mockito** — Mocking framework for isolating units under test
- **Spring Boot Test** — Integration testing with `@SpringBootTest`, `MockMvc`
- **Testcontainers** — Real Docker containers for integration tests (DB, Kafka, Redis)

**Test Pyramid:**
```
        /  E2E  \          ← Few, slow, expensive
       / Integration \     ← Moderate, test component interactions
      /    Unit Tests   \  ← Many, fast, isolated
```

---

## 2. Why Testing is Needed

| Problem | How Testing Solves It |
|---------|----------------------|
| Regression bugs | Automated tests catch breakage instantly |
| Fear of refactoring | Tests give confidence to change code |
| Unclear requirements | Tests document expected behavior |
| Production incidents | Catch bugs before deployment |
| Onboarding | Tests explain how code works |

**Without tests:** Every deployment is a gamble. With tests: you deploy with confidence.

---

## 3. How It Works Internally

### JUnit 5 Architecture

```
JUnit Platform (launcher, engines)
    └── JUnit Jupiter (JUnit 5 API)
    └── JUnit Vintage (JUnit 3/4 backward compat)
```

**Test Lifecycle:**
```
@BeforeAll (once per class)
  └── @BeforeEach (before every test)
        └── @Test (the actual test)
  └── @AfterEach (after every test)
@AfterAll (once per class)
```

### Mockito Internals

1. Creates a **proxy** (via ByteBuddy) that intercepts method calls
2. Records **stubbing** (when X is called, return Y)
3. Records **invocations** for verification
4. Uses `InvocationHandler` pattern internally

### Spring Boot Test Flow

```
@SpringBootTest
  → Starts full ApplicationContext
  → Injects beans
  → Runs test methods
  → Tears down context (cached between tests by default)

@WebMvcTest
  → Loads only web layer (controllers, filters)
  → MockMvc simulates HTTP without starting server
```

---

## 4. Real-World Example

**Ericsson NEF Platform — Testing a 5G API Gateway:**

```java
// Unit test: Validate rate limiter logic
@Test
void shouldRejectWhenRateLimitExceeded() {
    RateLimiter limiter = new TokenBucketRateLimiter(10, Duration.ofSeconds(1));
    IntStream.range(0, 10).forEach(i -> limiter.tryAcquire());
    
    assertFalse(limiter.tryAcquire()); // 11th request rejected
}

// Integration test: Full API call with Testcontainers
@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class NefApiIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateSubscription() {
        var response = restTemplate.postForEntity("/nef/subscriptions", 
            new SubscriptionRequest("5G-UE-001"), Subscription.class);
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }
}
```

---

## 5. Common Interview Questions

### Q1: What's the difference between @Mock and @Spy?

| Aspect | @Mock | @Spy |
|--------|-------|------|
| Behavior | All methods return defaults (null/0/false) | Real methods execute unless stubbed |
| Use case | Full isolation | Partial mocking |
| Stubbing | Required for any behavior | Optional (real code runs) |

```java
@Mock UserRepository mockRepo;    // findById() returns null
@Spy  UserRepository spyRepo;     // findById() hits real DB unless stubbed
```

### Q2: What is @SpringBootTest vs @WebMvcTest vs @DataJpaTest?

| Annotation | Loads | Speed | Use Case |
|-----------|-------|-------|----------|
| `@SpringBootTest` | Full context | Slow | End-to-end integration |
| `@WebMvcTest` | Controllers only | Fast | REST endpoint testing |
| `@DataJpaTest` | JPA + embedded DB | Medium | Repository testing |
| `@MockBean` | Replaces bean with mock | — | Isolate dependencies |

### Q3: Explain the Test Pyramid

- **Unit tests (70%):** Test single class/method in isolation. Fast, no I/O.
- **Integration tests (20%):** Test component interactions (DB, APIs, messaging).
- **E2E tests (10%):** Full system tests (Selenium, Cypress). Slow, brittle.

**Anti-pattern: Ice cream cone** — Too many E2E, too few unit tests.

### Q4: What is TDD?

**Red → Green → Refactor:**
1. **Red:** Write a failing test first
2. **Green:** Write minimum code to pass
3. **Refactor:** Clean up while tests stay green

### Q5: How does Mockito's verify() work?

```java
// Verify method was called exactly once
verify(emailService, times(1)).sendEmail(any());

// Verify never called
verify(emailService, never()).sendEmail(eq("spam@test.com"));

// Verify call order
InOrder inOrder = inOrder(service1, service2);
inOrder.verify(service1).validate();
inOrder.verify(service2).process();
```

### Q6: What are ArgumentCaptors?

Capture arguments passed to mocked methods for assertion:

```java
ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
verify(emailService).send(captor.capture());

Email sentEmail = captor.getValue();
assertEquals("Welcome!", sentEmail.getSubject());
assertEquals("user@test.com", sentEmail.getTo());
```

### Q7: What is Testcontainers?

Provides **real Docker containers** for integration tests — no more H2 pretending to be PostgreSQL:

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
    .withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");

@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
}
```

---

## 6. Tricky Edge Cases & Pitfalls

### Pitfall 1: Mocking final classes/methods

```java
// Mockito 2+ requires mockito-extensions to mock final classes
// Add: src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
// Content: mock-maker-inline
```

### Pitfall 2: @MockBean pollutes Spring context cache

```java
// Each unique @MockBean combination creates a NEW ApplicationContext
// BAD: Different @MockBean in every test class → slow test suite
// GOOD: Use shared test configuration or @TestConfiguration
```

### Pitfall 3: Testing time-dependent code

```java
// BAD: Tests fail at midnight or on weekends
@Test void badTest() {
    assertTrue(service.isBusinessHours()); // Depends on system clock!
}

// GOOD: Inject Clock
@Test void goodTest() {
    Clock fixedClock = Clock.fixed(Instant.parse("2024-01-15T10:00:00Z"), ZoneId.of("UTC"));
    assertTrue(service.isBusinessHours(fixedClock));
}
```

### Pitfall 4: Test interdependence

```java
// BAD: Test B depends on state from Test A
// JUnit does NOT guarantee execution order (by default)
// GOOD: Each test sets up its own state in @BeforeEach
```

### Pitfall 5: Over-mocking

```java
// BAD: Mocking everything including the class under test
when(service.calculate(any())).thenReturn(42); // You're testing Mockito, not your code!

// GOOD: Only mock external dependencies (DB, HTTP, messaging)
```

### Pitfall 6: Flaky tests

Common causes:
- Shared mutable state between tests
- Network calls in unit tests
- Race conditions in async tests
- Hardcoded ports/paths

Fix: Isolate state, use `@DirtiesContext` sparingly, use `Awaitility` for async.

---

## 7. Comparison with Related Concepts

### JUnit 4 vs JUnit 5

| Feature | JUnit 4 | JUnit 5 |
|---------|---------|---------|
| Package | `org.junit` | `org.junit.jupiter` |
| Annotations | `@Before`, `@After` | `@BeforeEach`, `@AfterEach` |
| Assertions | `Assert.assertEquals()` | `Assertions.assertEquals()` |
| Extensions | `@RunWith` + Rules | `@ExtendWith` |
| Parameterized | Clunky `@Parameters` | `@ParameterizedTest` + sources |
| Nested tests | Not supported | `@Nested` |
| Display names | Not supported | `@DisplayName` |
| Conditional | Not built-in | `@EnabledOnOs`, `@EnabledIf` |

### Mockito vs Other Mocking Frameworks

| Framework | Strengths | Weaknesses |
|-----------|-----------|------------|
| **Mockito** | Simple API, most popular | Can't mock static (needs mockito-inline) |
| **PowerMock** | Mocks static, constructors | Slow, invasive, deprecated patterns |
| **WireMock** | HTTP service mocking | Only for HTTP, not Java objects |
| **EasyMock** | Record-replay style | Verbose, less intuitive |

### Unit vs Integration vs E2E

| Aspect | Unit | Integration | E2E |
|--------|------|-------------|-----|
| Scope | Single class | Multiple components | Full system |
| Speed | ms | seconds | minutes |
| Dependencies | All mocked | Some real (DB, queue) | All real |
| Flakiness | Low | Medium | High |
| Maintenance | Low | Medium | High |
| Confidence | Low (isolated) | Medium | High |

---

## 8. Performance Impact

### Test Suite Speed Optimization

| Technique | Impact |
|-----------|--------|
| Parallel test execution | 2-4x faster |
| Spring context caching | Avoid 5-10s restart per class |
| Testcontainers reuse | `testcontainers.reuse.enable=true` |
| `@WebMvcTest` over `@SpringBootTest` | 3-5x faster for controller tests |
| In-memory DB for unit tests | Avoid network I/O |
| Lazy bean initialization in tests | Faster context startup |

### JUnit 5 Parallel Execution

```properties
# junit-platform.properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.config.fixed.parallelism=4
```

### Spring Context Caching Rules

Context is **reused** when tests share the same:
- `@ContextConfiguration` locations/classes
- `@ActiveProfiles`
- `@MockBean` / `@SpyBean` declarations
- Property sources

**One different `@MockBean` = new context = 5-10s penalty.**

---

## 9. Trade-offs

### When to Write Unit Tests

✅ Business logic, algorithms, transformations, validators
✅ Code with many branches/edge cases
✅ Utility classes

### When to Write Integration Tests

✅ Database queries (especially complex JPA/SQL)
✅ REST API endpoints
✅ Message consumers (Kafka, RabbitMQ)
✅ External service interactions

### When NOT to Test

❌ Trivial getters/setters (unless they have logic)
❌ Framework code (Spring, Hibernate internals)
❌ Generated code (Lombok, MapStruct)

### TDD Trade-offs

| Pro | Con |
|-----|-----|
| Better design (testable code) | Slower initial development |
| Living documentation | Learning curve |
| Fewer bugs | Over-engineering risk |
| Confidence to refactor | May test implementation, not behavior |

---

## 10. 30–60 Second Interview Answers

### "How do you approach testing in a microservices project?"

> "I follow the test pyramid. For each service, I write **unit tests** with JUnit 5 and Mockito for business logic — these are fast and run on every commit. For database and API interactions, I use **integration tests** with Testcontainers so we test against real PostgreSQL/Kafka, not mocks. For controller endpoints, I use `@WebMvcTest` with MockMvc to validate request/response contracts without loading the full context. At the top, we have a few **contract tests** (Spring Cloud Contract) to verify inter-service communication. In CI, unit tests run first as a fast feedback gate, integration tests run in parallel with Docker, and E2E tests run nightly."

### "What's your experience with TDD?"

> "I use TDD for complex business logic — especially algorithms, validators, and state machines. The Red-Green-Refactor cycle forces me to think about the API from the caller's perspective before implementation. At Ericsson, I used TDD for our rate limiter: I wrote tests for edge cases (burst traffic, token refill, concurrent access) first, then implemented the token bucket algorithm. The result was cleaner code with 95% branch coverage. I don't dogmatically apply TDD to everything — for CRUD endpoints or UI code, I write tests after."

### "How do you handle flaky tests?"

> "First, I identify the root cause: shared state, timing issues, or external dependencies. For shared state, I ensure each test has its own setup via `@BeforeEach`. For timing, I use `Awaitility` instead of `Thread.sleep()`. For external deps, I use Testcontainers with fixed ports or WireMock for HTTP services. If a test is inherently non-deterministic (like testing eventual consistency), I add retries with `@RepeatedTest` and document why. In CI, I quarantine flaky tests and fix them within the sprint — never ignore them."

---

## 11. Real Production Scenario

### Ericsson NEF — Catching a Race Condition with Testcontainers

**Situation:** Our 5G subscription API had intermittent duplicate entries in production. Unit tests all passed because they mocked the database.

**Problem:** Two concurrent requests for the same UE (User Equipment) could both pass the "exists?" check before either wrote to the DB.

**Solution:**

```java
@Testcontainers
@SpringBootTest
class ConcurrentSubscriptionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    SubscriptionService service;

    @Test
    void shouldPreventDuplicateSubscriptions() throws Exception {
        String ueId = "imsi-001010123456789";
        
        // Simulate 10 concurrent subscription requests
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1);
        
        List<Future<Subscription>> futures = IntStream.range(0, 10)
            .mapToObj(i -> executor.submit(() -> {
                latch.await(); // All threads start simultaneously
                return service.createSubscription(ueId);
            }))
            .toList();
        
        latch.countDown(); // Release all threads
        
        long successes = futures.stream()
            .map(this::getResult)
            .filter(Objects::nonNull)
            .count();
        
        assertEquals(1, successes); // Only one should succeed
        assertEquals(1, subscriptionRepo.countByUeId(ueId));
    }
}
```

**Fix:** Added `@Version` for optimistic locking + unique constraint on `ue_id`. The integration test caught what unit tests couldn't.

**Lesson:** Unit tests verify logic; integration tests verify behavior under real conditions.

---

## 12. If Tests Fail, How to Debug

### Symptom → Root Cause → Fix

| Symptom | Root Cause | Fix |
|---------|-----------|-----|
| `NullPointerException` in test | Forgot `@Mock` or `@InjectMocks` | Add `@ExtendWith(MockitoExtension.class)` |
| Test passes alone, fails in suite | Shared state / context pollution | Use `@DirtiesContext` or isolate state |
| `NoSuchBeanDefinitionException` | Wrong slice annotation | Use `@SpringBootTest` or add `@Import` |
| Mockito "wanted but not invoked" | Wrong method signature in verify | Check argument matchers match exactly |
| Testcontainers timeout | Docker not running / slow pull | Pre-pull images, increase timeout |
| `@Transactional` test doesn't rollback | Runs in different thread | Use `TransactionTemplate` in test |
| Flaky async test | Race condition | Use `Awaitility.await().atMost(5, SECONDS)` |
| Spring context takes 30s+ | Too many `@MockBean` variations | Consolidate test configurations |

### Debug Checklist

1. **Read the error message** — JUnit 5 gives excellent assertion messages
2. **Check test isolation** — Run the failing test alone (`-Dtest=ClassName#methodName`)
3. **Check mock setup** — Print `Mockito.mockingDetails(mock).getInvocations()`
4. **Check Spring context** — Enable `logging.level.org.springframework=DEBUG`
5. **Check test order** — Add `@TestMethodOrder(MethodOrderer.Random.class)` to find order-dependent tests

---

## Follow-up Interview Questions

### Q1: "How would you test a service that calls 3 external APIs and writes to 2 databases?"

**Answer:**
- **Unit tests:** Mock all 5 external dependencies. Test business logic (transformation, validation, error handling) in isolation.
- **Integration tests:** Use WireMock for HTTP APIs + Testcontainers for databases. Test the full flow with real I/O.
- **Contract tests:** Use Spring Cloud Contract or Pact to verify API contracts with upstream services don't break.
- **Resilience tests:** Simulate failures (WireMock returning 500, Testcontainers stopping mid-test) to verify circuit breakers and retries.

### Q2: "Your test suite takes 45 minutes in CI. How do you speed it up?"

**Answer:**
1. **Profile:** Find the slowest tests (`maven-surefire-report-plugin`)
2. **Parallelize:** JUnit 5 parallel execution + Maven parallel forks
3. **Reduce context restarts:** Consolidate `@MockBean` usage, use `@TestConfiguration`
4. **Testcontainers reuse:** Enable `testcontainers.reuse.enable=true` for local dev
5. **Test categorization:** Tag tests (`@Tag("slow")`) and run fast tests on every commit, slow tests on merge
6. **Replace `@SpringBootTest` with slices:** `@WebMvcTest`, `@DataJpaTest` where possible
7. **Shared containers:** Use `@Container` at class level with `static` for reuse

---

## Practice Task

**Build a fully tested Order Service:**

1. Create `OrderService` with methods: `createOrder()`, `cancelOrder()`, `getOrderStatus()`
2. Write **unit tests** with Mockito (mock `OrderRepository`, `PaymentGateway`, `NotificationService`)
3. Write **integration test** with Testcontainers (PostgreSQL) testing the full create→cancel flow
4. Write a **parameterized test** for order validation (invalid amounts, null items, exceeded limits)
5. Achieve 90%+ branch coverage
6. Use TDD: write tests first, then implement

**Bonus:** Add `@WebMvcTest` for the REST controller with MockMvc, testing happy path + error responses.

---

## Key Annotations Quick Reference

```java
// JUnit 5
@Test                          // Marks a test method
@DisplayName("human name")     // Readable test name
@BeforeEach / @AfterEach       // Per-test setup/teardown
@BeforeAll / @AfterAll         // Per-class (must be static)
@Nested                        // Group related tests
@ParameterizedTest             // Data-driven tests
@ValueSource, @CsvSource       // Parameter providers
@Tag("integration")            // Categorize tests
@Disabled("reason")            // Skip test
@Timeout(5)                    // Fail if exceeds 5 seconds
@RepeatedTest(3)               // Run 3 times
@TestMethodOrder               // Control execution order

// Mockito
@Mock                          // Create mock
@Spy                           // Partial mock (real methods)
@InjectMocks                   // Inject mocks into subject
@Captor                        // ArgumentCaptor shorthand
@ExtendWith(MockitoExtension.class)  // Enable Mockito

// Spring Boot Test
@SpringBootTest                // Full context
@WebMvcTest(Controller.class)  // Controller slice
@DataJpaTest                   // JPA slice
@MockBean                      // Replace bean with mock
@SpyBean                       // Wrap bean with spy
@DirtiesContext                // Reset context after test
@ActiveProfiles("test")        // Use test profile
@DynamicPropertySource         // Dynamic config (Testcontainers)

// Testcontainers
@Testcontainers                // Enable container lifecycle
@Container                     // Manage container lifecycle
```

---

## FIRST Principles for Good Tests

| Principle | Meaning |
|-----------|---------|
| **F**ast | Unit tests run in milliseconds |
| **I**solated | No test depends on another |
| **R**epeatable | Same result every time, any environment |
| **S**elf-validating | Pass or fail, no manual inspection |
| **T**imely | Written close to the code (ideally before — TDD) |
