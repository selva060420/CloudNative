# 08 — Microservices Architecture

## 1. Definition

Microservices is an architectural style where an application is composed of **small, independently deployable services**, each running in its own process, communicating via lightweight protocols (HTTP/REST, gRPC, messaging), and organized around business capabilities.

Each service owns its data, can be deployed independently, and can be written in different languages/frameworks.

---

## 2. Why This Is Needed

| Problem with Monolith | Microservices Solution |
|---|---|
| Single deployment unit — one bug blocks entire release | Independent deployments per service |
| Scaling requires scaling everything | Scale only the bottleneck service |
| Technology lock-in | Polyglot — each service picks best tool |
| Large codebase — slow builds, hard onboarding | Small focused codebases |
| Single DB becomes bottleneck | Database per service |
| Team coupling — everyone touches same code | Team autonomy around bounded contexts |

---

## 3. How It Works Internally

```
Client → API Gateway → Service Discovery → Target Service
                ↓                              ↓
         Rate Limiting              Circuit Breaker → Fallback
         Auth/JWT                   Retry / Timeout
         Routing                    Bulkhead isolation
                                         ↓
                              Database (owned by service)
                                         ↓
                              Event Bus (Kafka/RabbitMQ) → Other Services
```

**Request flow:**
1. Client hits **API Gateway** (Spring Cloud Gateway / Kong / Envoy)
2. Gateway authenticates, rate-limits, routes to correct service
3. **Service Discovery** (Eureka / Consul / K8s DNS) resolves service location
4. Target service processes request with **circuit breaker** protection
5. If service needs data from another service → sync (REST/gRPC) or async (Kafka event)
6. **Distributed tracing** (OpenTelemetry / Zipkin) correlates the entire call chain
7. Response flows back through gateway to client

---

## 4. Real-World Example

**Ericsson 5G NEF/CAPIF Platform (Selva's context):**

```
┌─────────────────────────────────────────────────────┐
│                   API Gateway (Kong)                  │
├─────────────────────────────────────────────────────┤
│  NEF Service    │  CAPIF Service  │  Auth Service    │
│  (3GPP APIs)    │  (API Registry) │  (OAuth2/JWT)    │
├─────────────────────────────────────────────────────┤
│  Cassandra      │  PostgreSQL     │  Redis           │
├─────────────────────────────────────────────────────┤
│              Kafka (Event Bus)                        │
├─────────────────────────────────────────────────────┤
│         Kubernetes + Istio (Service Mesh)             │
└─────────────────────────────────────────────────────┘
```

- NEF service exposes 3GPP northbound APIs
- CAPIF service handles API discovery/registration
- Each service has its own DB (database-per-service pattern)
- Kafka for async event propagation (e.g., subscription notifications)
- Istio handles mTLS, traffic management, observability

---

## 5. Common Interview Questions

### Q1: Monolith vs Microservices — when to choose which?

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| Team size | < 10 devs | Multiple teams (2-pizza rule) |
| Deployment | Single artifact | Independent per service |
| Complexity | Simple at start | Distributed system complexity |
| Data consistency | ACID transactions | Eventual consistency (Saga) |
| Latency | In-process calls | Network calls (higher latency) |
| Debugging | Stack trace | Distributed tracing needed |

**Choose monolith when:** early-stage startup, small team, unclear domain boundaries.
**Choose microservices when:** scaling teams, independent deployments needed, clear bounded contexts.

### Q2: How does service discovery work?

**Client-side discovery (Eureka):**
- Services register themselves with registry on startup
- Client queries registry, gets list of instances, load-balances locally
- Heartbeat mechanism detects dead instances

**Server-side discovery (K8s DNS / AWS ALB):**
- Services register with platform (K8s Service object)
- Platform's DNS/load-balancer routes requests
- Client doesn't need discovery logic

```
// Spring Cloud — client-side with Eureka
@EnableDiscoveryClient
@FeignClient(name = "order-service")
public interface OrderClient {
    @GetMapping("/orders/{id}")
    Order getOrder(@PathVariable String id);
}
```

### Q3: Explain the Circuit Breaker pattern

**States:** CLOSED → OPEN → HALF_OPEN → CLOSED

```
CLOSED: requests flow normally, failures counted
  → failure rate exceeds threshold (e.g., 50% in 10 calls)
OPEN: all requests fail-fast with fallback, no calls to downstream
  → after wait duration (e.g., 30s)
HALF_OPEN: limited requests allowed through to test recovery
  → if successful → CLOSED; if fails → OPEN again
```

**Why needed:** Prevents cascade failures. If Order Service is down, Payment Service shouldn't keep hammering it — fail fast and return fallback.

### Q4: Saga pattern — how to handle distributed transactions?

Since microservices can't use 2PC (two-phase commit) across services, use **Saga**:

**Choreography (event-driven):**
```
Order Created → [event] → Payment Charged → [event] → Inventory Reserved → [event] → Order Confirmed
                          Payment Failed → [compensating event] → Order Cancelled
```
- Each service listens to events and reacts
- No central coordinator
- Hard to track overall flow

**Orchestration (coordinator):**
```
Saga Orchestrator:
  1. Call Payment Service → charge
  2. Call Inventory Service → reserve
  3. Call Shipping Service → schedule
  If step 2 fails → call Payment Service → refund (compensation)
```
- Central orchestrator manages the flow
- Easier to understand and debug
- Single point of failure risk

### Q5: What is the 12-Factor App methodology?

| # | Factor | Description |
|---|--------|-------------|
| 1 | Codebase | One repo per service, many deploys |
| 2 | Dependencies | Explicitly declare (pom.xml, package.json) |
| 3 | Config | Store in environment, not code |
| 4 | Backing services | Treat DB, cache, queue as attached resources |
| 5 | Build, release, run | Strict separation of stages |
| 6 | Processes | Stateless — store state in DB/cache |
| 7 | Port binding | Self-contained, export via port |
| 8 | Concurrency | Scale out via process model |
| 9 | Disposability | Fast startup, graceful shutdown |
| 10 | Dev/prod parity | Keep environments similar |
| 11 | Logs | Treat as event streams (stdout) |
| 12 | Admin processes | Run admin tasks as one-off processes |

---

## 6. Tricky Edge Cases / Pitfalls

| Pitfall | What Happens | Fix |
|---------|-------------|-----|
| Distributed monolith | Services tightly coupled, must deploy together | Proper bounded contexts, async communication |
| Shared database | One service's schema change breaks others | Database per service, API contracts |
| Synchronous chain | A→B→C→D — if D is slow, everything is slow | Async where possible, circuit breakers, timeouts |
| No idempotency | Retry causes duplicate orders/payments | Idempotency keys on all write operations |
| Missing correlation ID | Can't trace request across services | Propagate traceId in headers (OpenTelemetry) |
| Eventual consistency confusion | UI shows stale data after write | CQRS, read-your-own-writes pattern |
| Service discovery stale cache | Routing to dead instances | Short TTL, health checks, circuit breaker |
| Config drift | Services have different config versions | Centralized config (Spring Cloud Config, K8s ConfigMap) |

---

## 7. Comparison with Related Concepts

### Microservices vs SOA vs Monolith

| Aspect | Monolith | SOA | Microservices |
|--------|----------|-----|---------------|
| Size | Single unit | Large services | Small, focused |
| Communication | In-process | ESB (Enterprise Service Bus) | Lightweight (REST, gRPC, events) |
| Data | Shared DB | Shared or separate | Database per service |
| Governance | Centralized | Centralized (ESB) | Decentralized |
| Deployment | All-or-nothing | Service-level | Independent |
| Team | One team | Multiple teams | Small autonomous teams |

### Sync vs Async Communication

| Aspect | Synchronous (REST/gRPC) | Asynchronous (Kafka/RabbitMQ) |
|--------|------------------------|-------------------------------|
| Coupling | Temporal coupling | Decoupled |
| Latency | Immediate response | Eventually processed |
| Failure handling | Circuit breaker needed | Dead letter queue |
| Use case | Query, real-time | Events, notifications, long tasks |
| Consistency | Strong (if available) | Eventual |

### API Gateway vs Service Mesh

| Aspect | API Gateway | Service Mesh (Istio) |
|--------|-------------|---------------------|
| Position | Edge (north-south traffic) | Between services (east-west) |
| Concerns | Auth, rate limiting, routing | mTLS, retries, observability |
| Implementation | Application-level (Kong, Spring Cloud Gateway) | Infrastructure-level (sidecar proxy) |
| Who manages | Dev team | Platform/SRE team |

---

## 8. Performance Impact

| Pattern | Overhead | Mitigation |
|---------|----------|-----------|
| Service-to-service calls | Network latency (1-10ms per hop) | gRPC (binary), connection pooling, caching |
| Service discovery | Registry lookup per call | Client-side caching with TTL |
| Circuit breaker | Minimal (in-memory state machine) | Negligible — saves resources when open |
| Distributed tracing | Header propagation + span reporting | Sampling (report 1% of traces in prod) |
| API Gateway | Extra network hop | Deploy close to services, connection reuse |
| Serialization | JSON parsing overhead | gRPC/Protobuf for internal, JSON for external |
| Saga orchestration | Multiple network calls for one transaction | Async where possible, parallel steps |

**Rule of thumb:** Each service hop adds ~2-5ms latency. A 5-service chain = 10-25ms overhead. Keep call depth shallow.

---

## 9. Trade-offs

| When to Use Microservices | When NOT to Use |
|--------------------------|-----------------|
| Multiple teams need independent velocity | Small team (< 5 devs) |
| Different scaling requirements per component | Simple CRUD app |
| Need polyglot (different tech per service) | Unclear domain boundaries |
| High availability required for specific features | Tight budget (infra cost is higher) |
| Frequent independent deployments | Strong consistency required everywhere |

**Hidden costs of microservices:**
- Operational complexity (K8s, service mesh, monitoring)
- Distributed debugging is 10x harder
- Data consistency requires Saga/eventual consistency
- Integration testing across services is complex
- Network is unreliable — need retries, timeouts, circuit breakers

---

## 10. 30–60 Second Interview Answers

### "What are microservices?"
> "Microservices is an architectural style where we decompose an application into small, independently deployable services organized around business capabilities. Each service owns its data, communicates via lightweight protocols like REST or messaging, and can be scaled independently. The key benefits are independent deployments, team autonomy, and targeted scaling — but the trade-off is distributed system complexity: you need service discovery, circuit breakers, distributed tracing, and eventual consistency patterns like Saga."

### "How do you handle failures in microservices?"
> "We use a defense-in-depth approach: circuit breakers to fail fast and prevent cascade failures, retries with exponential backoff for transient errors, timeouts to avoid hanging, bulkheads to isolate failures to one service, and fallbacks to provide degraded but functional responses. For observability, we use distributed tracing with correlation IDs to track requests across services. In our Ericsson platform, we use Resilience4j for circuit breaking and Istio's retry policies at the mesh level."

### "Explain the Saga pattern"
> "Saga is a pattern for managing distributed transactions across microservices without 2PC. Instead of one atomic transaction, we break it into a sequence of local transactions, each with a compensating action for rollback. There are two approaches: choreography where services react to events autonomously, and orchestration where a central coordinator drives the flow. Orchestration is easier to debug but creates a single point of coordination. We use choreography for simple flows and orchestration for complex multi-step business processes."

---

## 11. Real Production Scenario

**Scenario: Cascade failure in 5G NEF notification system**

**Setup:** NEF Service → Notification Service → External App (webhook)

**What happened:**
1. External app's webhook endpoint became slow (30s response time)
2. Notification Service threads blocked waiting for responses
3. Thread pool exhausted — Notification Service stopped accepting requests
4. NEF Service calls to Notification Service started timing out
5. NEF Service thread pool started filling up
6. **Entire platform degraded** — even unrelated API calls affected

**Root cause:** No circuit breaker, no timeout, no bulkhead isolation.

**Fix applied:**
```java
// 1. Circuit breaker on external webhook calls
@CircuitBreaker(name = "webhook", fallbackMethod = "webhookFallback")
@TimeLimiter(name = "webhook")
@Bulkhead(name = "webhook")
public CompletableFuture<Void> sendWebhook(String url, String payload) { ... }

// 2. Bulkhead: webhook calls get their own thread pool (10 threads)
//    so they can't exhaust the main service thread pool

// 3. Fallback: queue failed notifications for retry
public CompletableFuture<Void> webhookFallback(String url, String payload, Exception e) {
    deadLetterQueue.send(new FailedNotification(url, payload));
    return CompletableFuture.completedFuture(null);
}
```

**Result:** When external app is slow, circuit opens after 5 failures, notifications queue for retry, and the rest of the platform remains healthy.

---

## 12. If This Fails, How to Debug

### Symptom: Service A can't reach Service B

| Check | Command/Tool | What to Look For |
|-------|-------------|-----------------|
| DNS resolution | `nslookup service-b` / K8s DNS | Service registered? |
| Network connectivity | `curl http://service-b:8080/actuator/health` | Firewall/NetworkPolicy blocking? |
| Service discovery | Eureka dashboard / `kubectl get endpoints` | Instances registered? |
| Circuit breaker state | Actuator `/circuitbreakers` | Is circuit OPEN? |
| Thread pool | Thread dump / actuator `/metrics` | Threads exhausted? |

### Symptom: High latency across services

```bash
# 1. Check distributed trace (Jaeger/Zipkin)
# Find the slow span — which service/DB call is the bottleneck?

# 2. Check connection pools
curl http://service:8080/actuator/metrics/hikaricp.connections.active

# 3. Check circuit breaker metrics
curl http://service:8080/actuator/metrics/resilience4j.circuitbreaker.calls

# 4. Check if retries are amplifying load
# Look for retry storms in logs
grep "Retry attempt" /var/log/app.log | wc -l
```

### Symptom: Data inconsistency across services

1. Check if Saga completed — look for compensating events
2. Check dead letter queue for failed events
3. Verify idempotency — was the same event processed twice?
4. Check event ordering — Kafka partition key correct?
5. Look for race conditions in eventual consistency window

---

## Follow-up Interview Questions

### FQ1: How would you decompose a monolith into microservices?

**Answer:**
1. **Identify bounded contexts** using Domain-Driven Design (DDD)
2. **Start with the strangler fig pattern** — don't rewrite, gradually extract
3. **Extract the most independent module first** (e.g., notification, auth)
4. **Define API contracts** before splitting (OpenAPI spec)
5. **Handle shared data** — introduce events for data that was previously joined via SQL
6. **Set up infrastructure first** — CI/CD per service, service discovery, monitoring
7. **Extract one service at a time**, validate in production, then continue

**Anti-patterns to avoid:**
- Extracting services that still share a database
- Creating too many services too fast (nano-services)
- Not investing in observability before splitting

### FQ2: How do you ensure data consistency without distributed transactions?

**Answer:**
- **Saga pattern** for multi-service workflows (orchestration or choreography)
- **Outbox pattern** — write event to local DB table, separate process publishes to Kafka (avoids dual-write problem)
- **Idempotent consumers** — handle duplicate events gracefully
- **Event sourcing** — store events as source of truth, derive state
- **CQRS** — separate read/write models, read model eventually consistent

```
// Outbox pattern
@Transactional
public Order createOrder(OrderRequest req) {
    Order order = orderRepo.save(new Order(req));
    // Write event to outbox table in SAME transaction
    outboxRepo.save(new OutboxEvent("OrderCreated", order.getId(), toJson(order)));
    return order;
}
// Separate CDC process (Debezium) reads outbox table → publishes to Kafka
```

---

## Practice Task

**Build a simplified Order Saga Orchestrator:**

1. Create an `OrderSagaOrchestrator` that coordinates:
   - Payment Service (charge) → Inventory Service (reserve) → Shipping Service (schedule)
2. If any step fails, execute compensating actions in reverse order
3. Add a circuit breaker on each service call
4. Add a fallback that queues the order for manual review
5. Log the saga state transitions

**Bonus:** Add idempotency — if the same orderId is submitted twice, don't re-execute completed steps.

---

## Code Examples

| File | Description |
|------|-------------|
| `CircuitBreakerDemo.java` | Resilience4j circuit breaker with state transitions |
| `SagaOrchestratorDemo.java` | Saga pattern with compensation logic |
| `EventDrivenDemo.java` | In-memory event bus for async communication |
