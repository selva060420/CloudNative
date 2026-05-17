# 12 — System Design

## 1. Definition

System design is the process of defining the architecture, components, modules, interfaces, and data flow of a system to satisfy specified requirements. It bridges the gap between requirements and implementation — answering **"how do we build this at scale?"**

For backend engineers, system design interviews test your ability to:
- Break down ambiguous problems into concrete components
- Make trade-off decisions (consistency vs availability, latency vs throughput)
- Design for scale (millions of users, petabytes of data)
- Identify failure modes and build resilience

---

## 2. Why This Is Needed

| Problem | System Design Solves It By |
|---------|---------------------------|
| Single server can't handle traffic | Horizontal scaling, load balancing |
| Database becomes bottleneck | Sharding, read replicas, caching |
| Single point of failure | Redundancy, failover, replication |
| Slow response times | Caching, CDN, async processing |
| Data loss | Replication, backups, WAL |
| Unpredictable traffic spikes | Auto-scaling, rate limiting, queues |
| Cross-region latency | CDN, geo-distributed databases |

---

## 3. How It Works Internally — Core Building Blocks

### 3.1 Load Balancing

```
Client → Load Balancer → [Server 1, Server 2, Server 3]
```

**Algorithms:**
| Algorithm | How It Works | Best For |
|-----------|-------------|----------|
| Round Robin | Rotate sequentially | Equal-capacity servers |
| Weighted Round Robin | Proportional to server capacity | Mixed hardware |
| Least Connections | Route to server with fewest active connections | Long-lived connections |
| IP Hash | Hash client IP to consistent server | Session affinity |
| Consistent Hashing | Minimize redistribution on server add/remove | Cache clusters |

**Layers:**
- **L4 (Transport):** TCP/UDP level, fast, no content inspection (AWS NLB)
- **L7 (Application):** HTTP level, can route by URL/header/cookie (AWS ALB, Nginx)

**Health checks:** Active (periodic pings) vs Passive (monitor response codes)

---

### 3.2 Caching

**Cache levels:**
```
Client Cache → CDN → API Gateway Cache → Application Cache → Database Cache
```

**Strategies:**
| Strategy | Read | Write | Use Case |
|----------|------|-------|----------|
| Cache-Aside (Lazy) | App checks cache → miss → read DB → populate cache | App writes DB, invalidates cache | General purpose, most common |
| Read-Through | Cache handles DB read on miss | — | Simplifies app code |
| Write-Through | — | Write cache + DB synchronously | Strong consistency needed |
| Write-Behind (Write-Back) | — | Write cache, async flush to DB | High write throughput |
| Write-Around | — | Write directly to DB, skip cache | Write-heavy, rarely re-read |

**Eviction policies:** LRU, LFU, TTL, FIFO

**Cache invalidation problems:**
- Stale data (TTL too long)
- Cache stampede (many requests hit DB simultaneously on expiry)
- Thundering herd (popular key expires, all threads query DB)

**Solutions:** Mutex/lock on cache miss, probabilistic early expiration, background refresh

**Tools:** Redis, Memcached, Caffeine (local), Varnish (HTTP)

---

### 3.3 Database Scaling

#### Replication
```
Primary (writes) → Replica 1 (reads)
                 → Replica 2 (reads)
```
- **Sync replication:** Strong consistency, higher latency
- **Async replication:** Eventual consistency, lower latency, risk of data loss

#### Sharding (Horizontal Partitioning)
Split data across multiple databases by a shard key.

| Strategy | How | Pros | Cons |
|----------|-----|------|------|
| Range-based | user_id 1-1M → shard1, 1M-2M → shard2 | Simple, range queries easy | Hotspots if uneven distribution |
| Hash-based | hash(user_id) % N → shard | Even distribution | Range queries across shards expensive |
| Directory-based | Lookup table maps key → shard | Flexible | Lookup table is SPOF |
| Geo-based | Region → shard | Low latency for local users | Cross-region queries complex |

**Challenges:** Cross-shard joins, rebalancing, distributed transactions, unique IDs (Snowflake)

#### Indexing
- **B-Tree:** Range queries, ordered data (default in most RDBMS)
- **Hash Index:** Exact lookups, O(1)
- **LSM Tree:** Write-optimized (Cassandra, RocksDB)
- **Inverted Index:** Full-text search (Elasticsearch)

---

### 3.4 Message Queues & Event Streaming

```
Producer → Queue/Topic → Consumer(s)
```

| Feature | Message Queue (RabbitMQ, SQS) | Event Stream (Kafka) |
|---------|-------------------------------|---------------------|
| Delivery | Point-to-point or pub/sub | Pub/sub with replay |
| Ordering | Per-queue (FIFO) | Per-partition |
| Retention | Until consumed | Time/size-based retention |
| Replay | No | Yes (offset-based) |
| Throughput | Moderate | Very high |
| Use case | Task distribution, RPC | Event sourcing, CDC, analytics |

**Patterns:**
- **Fan-out:** One message → multiple consumers (notifications)
- **Fan-in:** Multiple producers → one consumer (log aggregation)
- **Dead Letter Queue:** Failed messages go to DLQ for investigation
- **Competing Consumers:** Multiple consumers share a queue for parallelism

---

### 3.5 Rate Limiting

**Algorithms:**
| Algorithm | How It Works | Pros | Cons |
|-----------|-------------|------|------|
| Token Bucket | Tokens added at fixed rate, request consumes token | Allows bursts, smooth | Memory per user |
| Leaky Bucket | Requests queue, processed at fixed rate | Smooth output | No burst tolerance |
| Fixed Window | Count requests per time window | Simple | Boundary burst (2x at window edge) |
| Sliding Window Log | Track timestamp of each request | Accurate | Memory-heavy |
| Sliding Window Counter | Weighted count across current + previous window | Accurate + memory efficient | Slight approximation |

**Implementation layers:** API Gateway (Kong, AWS API GW), Application (Resilience4j, Guava RateLimiter), Distributed (Redis + Lua script)

---

### 3.6 Consistent Hashing

Traditional hashing: `server = hash(key) % N` — adding/removing server redistributes ALL keys.

Consistent hashing: Keys and servers mapped to a ring. Key goes to next server clockwise.
- Adding a server: only keys between new server and its predecessor move
- Virtual nodes: each physical server gets multiple positions → even distribution

**Used in:** Cassandra, DynamoDB, Redis Cluster, CDN routing

---

### 3.7 CAP Theorem & PACELC

**CAP:** In a network partition, choose Consistency OR Availability.
- **CP:** Refuse requests if can't guarantee consistency (HBase, MongoDB with majority write)
- **AP:** Serve stale data rather than fail (Cassandra, DynamoDB)

**PACELC:** Extends CAP — even when no partition, trade-off between Latency and Consistency.
- Cassandra: PA/EL (available during partition, low latency normally)
- HBase: PC/EC (consistent always, higher latency)

---

### 3.8 CDN (Content Delivery Network)

```
User → Nearest Edge Server (cache hit?) → Origin Server
```

- **Push CDN:** Origin pushes content to edges proactively (static assets)
- **Pull CDN:** Edge fetches from origin on first request, caches (dynamic content)

**Use cases:** Static assets, video streaming, API response caching, DDoS protection

---

### 3.9 API Gateway

Single entry point for all microservices:
- Request routing
- Authentication/authorization
- Rate limiting
- Request/response transformation
- Circuit breaking
- Logging & monitoring

**Tools:** Kong, AWS API Gateway, Spring Cloud Gateway, Envoy

---

### 3.10 Distributed Consensus

**Problem:** Multiple nodes must agree on a value (leader election, config).

| Algorithm | Used By | Notes |
|-----------|---------|-------|
| Raft | etcd, Consul | Understandable, leader-based |
| Paxos | Chubby, Spanner | Proven, complex |
| ZAB | ZooKeeper | Similar to Raft |

---

## 4. Real-World Example — Ericsson NEF Context

**Problem:** Design a 5G Network Exposure Function (NEF) that handles:
- 100K+ API calls/sec from application functions
- Event subscriptions (monitoring events, location updates)
- Multi-tenant isolation
- 99.99% availability

**Architecture:**
```
AF (App Function) → API Gateway (Kong) → NEF Service Cluster
                                              ↓
                    ┌─────────────────────────────────────┐
                    │  Load Balancer (L7, path-based)      │
                    │  ┌─────┐  ┌─────┐  ┌─────┐         │
                    │  │NEF-1│  │NEF-2│  │NEF-3│  (K8s)  │
                    │  └──┬──┘  └──┬──┘  └──┬──┘         │
                    │     └────────┼────────┘             │
                    │              ↓                       │
                    │     Redis (session/rate limit)       │
                    │     Kafka (event notifications)      │
                    │     Cassandra (subscription store)   │
                    └─────────────────────────────────────┘
```

**Key decisions:**
- Cassandra for subscriptions (AP, write-heavy, multi-DC)
- Kafka for async event delivery (replay, ordering per subscriber)
- Redis for rate limiting (token bucket per tenant, Lua script for atomicity)
- K8s HPA for auto-scaling based on request rate

---

## 5. Common Interview Questions

### Q1: Design a URL Shortener (TinyURL)

**Requirements:** Generate short URL, redirect to original, analytics, expiry

**High-level:**
```
Client → API Gateway → URL Service → DB (mapping store)
                                    → Cache (hot URLs)
                                    → Analytics (async via Kafka)
```

**Key decisions:**
| Aspect | Decision | Reasoning |
|--------|----------|-----------|
| ID generation | Base62 encoding of auto-increment or Snowflake ID | Short, unique, no collisions |
| Storage | SQL (PostgreSQL) with read replicas | Strong consistency for writes, scale reads |
| Cache | Redis with LRU | 80/20 rule — 20% URLs get 80% traffic |
| Short URL length | 7 chars (62^7 = 3.5 trillion) | Enough for years |
| Analytics | Kafka → ClickHouse | Async, don't block redirects |

**Scale math:**
- 100M URLs/day = ~1200 writes/sec
- 10:1 read:write = 12K reads/sec
- 7 bytes key + 2KB value × 100M/day × 365 × 5 years = ~365 TB

---

### Q2: Design a Notification System

**Requirements:** Push, SMS, email; millions of users; pluggable channels; retry

```
Event Source → Notification Service → Priority Queue → Channel Workers
                    ↓                                      ↓
              Template Engine                    [Push | SMS | Email]
              User Preferences                         ↓
              Rate Limiter                      Delivery Status → DB
```

**Key decisions:**
- Priority queues (critical alerts > marketing)
- Idempotency key to prevent duplicate sends
- DLQ for failed deliveries with exponential backoff
- User preference service (opt-out, channel preference, quiet hours)
- Template engine for personalization

---

### Q3: Design a Rate Limiter (Distributed)

**Requirements:** Per-user, per-API, distributed across multiple servers

```
Request → API Gateway → Rate Limiter (Redis) → Backend Service
```

**Implementation (Sliding Window + Redis):**
```
MULTI
  ZADD key timestamp timestamp
  ZREMRANGEBYSCORE key 0 (now - window)
  ZCARD key
EXEC
→ if count > limit → 429 Too Many Requests
```

**Considerations:**
- Race conditions → Redis Lua script (atomic)
- Clock sync across servers → use Redis server time
- Failure mode → fail-open (allow) vs fail-closed (deny)

---

### Q4: Design a Chat System (WhatsApp-like)

**Requirements:** 1:1 and group chat, online status, read receipts, media

```
Client ←WebSocket→ Connection Service → Message Service → Cassandra
                         ↓                                    ↑
                   Presence Service                    Message Queue
                   (Redis pub/sub)                    (fan-out for groups)
```

**Key decisions:**
- WebSocket for real-time bidirectional communication
- Cassandra for messages (partition by chat_id, cluster by timestamp)
- Redis for online/presence status (TTL-based heartbeat)
- Fan-out on write for small groups, fan-out on read for large groups
- Media: upload to S3, store URL in message

---

### Q5: Design a Distributed Cache (Redis-like)

**Requirements:** Low latency, high throughput, fault tolerance, eviction

**Architecture:**
- Consistent hashing for key distribution
- Primary-replica per shard for fault tolerance
- Gossip protocol for cluster membership
- LRU eviction per node
- Client-side routing (smart client) or proxy-based

---

## 6. Tricky Edge Cases & Pitfalls

| Pitfall | Why It Happens | Fix |
|---------|---------------|-----|
| Hotspot/celebrity problem | One shard gets disproportionate traffic | Shard splitting, dedicated shard, caching |
| Cache stampede | Popular key expires, all threads hit DB | Mutex lock, early probabilistic expiry |
| Split brain | Network partition, two leaders elected | Fencing tokens, quorum-based writes |
| Cascading failure | One service down → backpressure → all down | Circuit breaker, bulkhead, timeouts |
| Data inconsistency after failover | Async replica promoted, missing recent writes | Sync replication for critical data, conflict resolution |
| Thundering herd on restart | All clients reconnect simultaneously | Jittered backoff, connection draining |
| ID collision in distributed systems | Multiple nodes generate same ID | Snowflake IDs, UUID v7, centralized ID service |

---

## 7. Comparison of Related Concepts

### SQL vs NoSQL for System Design

| Criteria | SQL (PostgreSQL) | NoSQL (Cassandra/DynamoDB) |
|----------|-----------------|---------------------------|
| Schema | Fixed, normalized | Flexible, denormalized |
| Scaling | Vertical + read replicas | Horizontal (native sharding) |
| Consistency | Strong (ACID) | Tunable (eventual to strong) |
| Joins | Native | Application-level |
| Best for | Complex queries, transactions | High write throughput, known access patterns |

### Sync vs Async Communication

| Aspect | Synchronous (REST/gRPC) | Asynchronous (Kafka/SQS) |
|--------|------------------------|--------------------------|
| Latency | Immediate response | Eventual processing |
| Coupling | Tight (caller waits) | Loose (fire and forget) |
| Failure handling | Caller must retry | Queue retries automatically |
| Ordering | Request-response order | Partition/queue ordering |
| Use case | User-facing APIs | Background jobs, events |

### Monolith vs Microservices

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| Deployment | All-or-nothing | Independent per service |
| Scaling | Scale entire app | Scale per service |
| Complexity | Simple initially | Distributed systems complexity |
| Data | Shared DB | Database per service |
| Team | Single team | Team per service |
| When | Early stage, small team | Scale, multiple teams, different SLAs |

---

## 8. Performance Impact

| Component | Latency | Throughput Impact |
|-----------|---------|-------------------|
| L4 Load Balancer | ~0.1ms | Millions req/sec |
| L7 Load Balancer | ~1-5ms | 100K+ req/sec |
| Redis cache hit | ~0.5ms | 100K+ ops/sec per node |
| Kafka produce | ~2-5ms | Millions msg/sec |
| DB query (indexed) | ~1-10ms | Depends on connection pool |
| DB query (full scan) | 100ms-seconds | Kills throughput |
| Cross-region call | 50-200ms | Limited by RTT |
| DNS resolution | ~20-50ms (uncached) | Cache aggressively |

**Capacity estimation formulas:**
- QPS = DAU × avg_requests_per_user / 86400
- Peak QPS = QPS × 3 (rule of thumb)
- Storage = records_per_day × record_size × retention_days
- Bandwidth = QPS × avg_response_size
- Servers needed = Peak QPS / single_server_capacity

---

## 9. Trade-offs

| Decision | Option A | Option B | When to Choose A |
|----------|----------|----------|-----------------|
| Consistency vs Availability | CP (strong consistency) | AP (high availability) | Financial transactions, inventory |
| Push vs Pull | Server pushes to client | Client polls server | Real-time needs, fewer clients |
| Normalize vs Denormalize | 3NF, no redundancy | Duplicate data for read speed | Write-heavy, complex queries |
| SQL vs NoSQL | Relational, ACID | Flexible, scalable | Need joins, transactions |
| Sync vs Async | Immediate response | Queue-based | User needs immediate feedback |
| Cache vs No Cache | Fast reads, stale risk | Always fresh, slower | Read-heavy, tolerance for staleness |
| Vertical vs Horizontal scale | Bigger machine | More machines | Quick fix, single-threaded workloads |

---

## 10. 30–60 Second Interview Answers

### "How would you scale a system to handle 10x traffic?"

> "I'd approach it in layers. First, add a CDN for static content and a Redis cache for hot data — that eliminates 80% of DB reads. Then horizontal scaling behind a load balancer with auto-scaling based on CPU/request rate. For the database, read replicas for read-heavy workloads, and if writes are the bottleneck, shard by a natural partition key like tenant_id. For async workloads, introduce a message queue to decouple producers from consumers. Finally, rate limiting to protect the system from abuse and circuit breakers to prevent cascading failures."

### "How do you ensure high availability?"

> "Redundancy at every layer: multiple app instances behind a load balancer with health checks, database replication with automatic failover, multi-AZ deployment. Eliminate single points of failure. Use circuit breakers so one failing dependency doesn't take down the whole system. Design for graceful degradation — serve cached data if the DB is down, queue writes if a downstream service is slow. Monitor with alerts on error rates and latency percentiles, not just averages."

### "How do you handle data consistency in distributed systems?"

> "It depends on the requirement. For strong consistency — synchronous replication, distributed transactions (2PC or Saga with compensation). For eventual consistency — async replication with conflict resolution (last-write-wins, vector clocks, or CRDTs). In practice, I use the Saga pattern for cross-service transactions with compensating actions for rollback, and an outbox pattern to ensure events are published reliably alongside DB writes."

---

## 11. Real Production Scenario

**Scenario: Ericsson NEF subscription notification storm**

**Problem:** During a network event (cell tower failover), 50K monitoring subscriptions triggered simultaneously. The notification service couldn't keep up — queue depth grew, latency spiked to 30s, and downstream AFs started timing out and retrying, making it worse.

**Root cause:** Fan-out on write with no backpressure. Each subscription generated a notification synchronously pushed to a single Kafka partition (keyed by event type, not subscriber).

**Fix:**
1. **Re-partition Kafka topic** by subscriber_id → parallel consumption across consumer group
2. **Add backpressure** — consumer pulls at its own rate, not pushed
3. **Priority queue** — critical notifications (service degradation) get separate high-priority topic
4. **Batch notifications** — aggregate multiple events into one notification within a 5s window
5. **Circuit breaker** on AF delivery — if AF is unresponsive, stop sending and alert

**Result:** P99 latency dropped from 30s to 200ms. System handled 200K notifications/sec during next failover event.

---

## 12. If This Fails, How to Debug

| Symptom | Likely Cause | Debug Approach |
|---------|-------------|----------------|
| High latency, low CPU | Waiting on I/O (DB, external service) | Check connection pool exhaustion, slow queries, downstream latency |
| High CPU, normal latency | Inefficient processing, GC pressure | Profile with async-profiler, check GC logs |
| Intermittent timeouts | Network issues, DNS, connection limits | Check TCP retransmits, DNS TTL, ulimit |
| Cascading failures | No circuit breaker, no timeouts | Add Resilience4j, set aggressive timeouts |
| Data inconsistency | Replication lag, race conditions | Check replica lag metrics, add idempotency keys |
| Queue depth growing | Consumer slower than producer | Scale consumers, check for poison messages |
| Memory growing (OOM) | Cache without eviction, connection leaks | Heap dump, check cache size, connection pool metrics |
| Uneven load distribution | Hot partition, bad shard key | Check per-shard metrics, consider re-sharding |

**Monitoring essentials:**
- **RED method:** Rate, Errors, Duration (for services)
- **USE method:** Utilization, Saturation, Errors (for resources)
- **Four golden signals:** Latency, Traffic, Errors, Saturation

---

## System Design Interview Framework (STEP)

Use this structure in every system design interview:

### S — Scope & Requirements (5 min)
- Functional requirements (what does it do?)
- Non-functional requirements (scale, latency, availability)
- Back-of-envelope estimation (QPS, storage, bandwidth)

### T — Top-level Design (10 min)
- High-level architecture diagram
- Core components and their responsibilities
- Data flow for main use cases

### E — Elaborate Components (15 min)
- Deep dive into 2-3 critical components
- Database schema, API design
- Algorithms (hashing, ranking, etc.)

### P — Production Concerns (10 min)
- Scaling bottlenecks and solutions
- Failure modes and mitigation
- Monitoring and alerting

---

## Follow-Up Interview Questions

### Q1: "You're designing a system and the interviewer says 'now handle 100x the traffic.' Walk me through your approach."

**Answer:**

1. **Identify the bottleneck** — Is it compute, memory, network, or storage? Use metrics.
2. **Cache aggressively** — Add Redis/Memcached for read-heavy paths. Cache at multiple levels (CDN, app, DB query cache).
3. **Scale horizontally** — Stateless services behind LB with auto-scaling. Move state to external stores (Redis, DB).
4. **Shard the database** — Choose a shard key that distributes evenly and matches access patterns. Consider read replicas first if reads dominate.
5. **Go async** — Move non-critical work to queues (Kafka/SQS). Batch processing where possible.
6. **Optimize hot paths** — Profile the top 3 endpoints. Denormalize data, pre-compute results, use materialized views.
7. **Rate limit and shed load** — Protect the system from abuse. Graceful degradation > total failure.

### Q2: "How would you migrate a monolith to microservices without downtime?"

**Answer:**

1. **Strangler Fig pattern** — Route new features to new services, gradually move old features
2. **Start with the bounded context that changes most frequently** — highest ROI
3. **Dual-write during migration** — write to both old and new, read from old, verify consistency
4. **Feature flags** — toggle between old and new path per request
5. **Shared DB → DB per service** — use Change Data Capture (Debezium) to sync during transition
6. **API Gateway as facade** — clients don't know about internal restructuring
7. **Canary deployment** — route 1% → 10% → 50% → 100% to new service

---

## Practice Task

**Design a distributed rate limiter for a multi-tenant API platform:**

Requirements:
- 1000 tenants, each with different rate limits (100-10K req/sec)
- Distributed across 5 data centers
- Sub-millisecond decision latency
- Accurate within 5% tolerance
- Handle clock skew between servers

Deliverables:
1. High-level architecture diagram
2. Choice of algorithm with justification
3. Data store selection and schema
4. How to handle a data center going offline
5. How to update rate limits without restart
