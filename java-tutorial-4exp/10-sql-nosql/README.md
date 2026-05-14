# 10 — SQL & NoSQL

## 1. Definition

**SQL (Structured Query Language):** Relational databases that store data in tables with rows and columns, enforcing a fixed schema. Examples: PostgreSQL, MySQL, Oracle.

**NoSQL (Not Only SQL):** Non-relational databases designed for flexible schemas, horizontal scaling, and specific data access patterns. Categories:
- **Document:** MongoDB, Couchbase (JSON documents)
- **Wide-Column:** Cassandra, HBase (column families)
- **Key-Value:** Redis, DynamoDB (simple key→value)
- **Graph:** Neo4j (nodes + relationships)

---

## 2. Why This Is Needed

| Problem | SQL Solves | NoSQL Solves |
|---------|-----------|--------------|
| Complex relationships & joins | ✅ ACID transactions, foreign keys | ❌ Denormalization required |
| Massive write throughput | ❌ Single-master bottleneck | ✅ Distributed writes (Cassandra) |
| Flexible/evolving schema | ❌ ALTER TABLE is expensive | ✅ Schema-less documents |
| Strong consistency | ✅ ACID guarantees | ⚠️ Eventual consistency (tunable) |
| Horizontal scaling | ❌ Hard to shard | ✅ Built-in partitioning |

**In microservices:** Each service picks the database that fits its access pattern (polyglot persistence).

---

## 3. How It Works Internally

### SQL — Query Execution Flow
```
SQL Query → Parser → Query Optimizer → Execution Plan → Storage Engine → Result
                          ↓
              Uses indexes, statistics, cost-based optimization
```

### Indexes (B-Tree)
```
                    [50]
                   /    \
             [20,30]    [70,80]
            /  |  \    /  |  \
         [10][25][35][60][75][90]  ← Leaf nodes point to actual rows
```
- **Clustered index:** Data physically sorted by index key (1 per table — usually PK)
- **Non-clustered index:** Separate structure pointing to data rows (multiple allowed)
- **Composite index:** Multi-column — follows leftmost prefix rule

### Cassandra — Write Path
```
Client Write → Coordinator Node → Commit Log (disk) → Memtable (RAM)
                                                          ↓ (flush)
                                                      SSTable (disk, immutable)
                                                          ↓ (compaction)
                                                      Merged SSTables
```

### Cassandra — Read Path
```
Client Read → Coordinator → Bloom Filter → Partition Index → SSTable → Merge with Memtable → Result
```

---

## 4. Real-World Example

**Ericsson 5G NEF Platform:**
- **PostgreSQL:** Stores API subscriptions, user profiles — needs ACID for billing correctness
- **Cassandra:** Stores network event logs (millions/sec) — needs high write throughput, time-series queries
- **Redis:** Caches session tokens and rate-limit counters — needs sub-ms reads

**Query example — find all subscriptions expiring this week:**
```sql
SELECT s.id, u.name, s.expiry_date
FROM subscriptions s
JOIN users u ON s.user_id = u.id
WHERE s.expiry_date BETWEEN NOW() AND NOW() + INTERVAL '7 days'
  AND s.status = 'ACTIVE'
ORDER BY s.expiry_date;
```

---

## 5. Common Interview Questions

### Q1: What are SQL JOIN types?
| Join | Returns |
|------|---------|
| INNER JOIN | Only matching rows from both tables |
| LEFT JOIN | All left rows + matching right (NULL if no match) |
| RIGHT JOIN | All right rows + matching left |
| FULL OUTER JOIN | All rows from both (NULL where no match) |
| CROSS JOIN | Cartesian product (every combination) |
| SELF JOIN | Table joined with itself |

### Q2: What is database normalization?
Organizing data to reduce redundancy:
- **1NF:** Atomic values, no repeating groups
- **2NF:** 1NF + no partial dependencies (all non-key columns depend on full PK)
- **3NF:** 2NF + no transitive dependencies (non-key columns don't depend on other non-key columns)
- **BCNF:** Every determinant is a candidate key

**When to denormalize:** Read-heavy workloads where joins are expensive (reporting, analytics, NoSQL).

### Q3: What is ACID?
- **Atomicity:** All or nothing — transaction fully completes or fully rolls back
- **Consistency:** DB moves from one valid state to another (constraints enforced)
- **Isolation:** Concurrent transactions don't interfere with each other
- **Durability:** Committed data survives crashes (written to disk/WAL)

### Q4: What are transaction isolation levels?
| Level | Dirty Read | Non-Repeatable Read | Phantom Read | Performance |
|-------|-----------|-------------------|--------------|-------------|
| READ UNCOMMITTED | ✅ | ✅ | ✅ | Fastest |
| READ COMMITTED | ❌ | ✅ | ✅ | Good |
| REPEATABLE READ | ❌ | ❌ | ✅ | Moderate |
| SERIALIZABLE | ❌ | ❌ | ❌ | Slowest |

**Default:** PostgreSQL = READ COMMITTED, MySQL InnoDB = REPEATABLE READ.

### Q5: What is the N+1 query problem?
```java
// BAD: 1 query for orders + N queries for each order's items
List<Order> orders = orderRepo.findAll();           // 1 query
for (Order o : orders) {
    o.getItems().size();                            // N queries (lazy load)
}

// FIX: Use JOIN FETCH or @EntityGraph
@Query("SELECT o FROM Order o JOIN FETCH o.items")
List<Order> findAllWithItems();                     // 1 query
```

### Q6: What is connection pooling?
Reusing database connections instead of creating/destroying per request:
```
App Thread 1 ──┐
App Thread 2 ──┼──→ Connection Pool (10-50 connections) ──→ Database
App Thread 3 ──┘         ↑ borrow / return ↑
```
- **HikariCP** (Spring Boot default): fastest Java pool
- Key settings: `maximumPoolSize`, `connectionTimeout`, `idleTimeout`

### Q7: What is the CAP theorem?
A distributed system can guarantee only **2 of 3**:
- **C**onsistency — every read gets the latest write
- **A**vailability — every request gets a response
- **P**artition tolerance — system works despite network splits

| Database | CAP Choice | Trade-off |
|----------|-----------|-----------|
| PostgreSQL | CA (single node) | No partition tolerance |
| Cassandra | AP | Eventual consistency |
| MongoDB | CP | Unavailable during partition |
| DynamoDB | AP (tunable) | Tunable consistency |

**Reality:** P is mandatory in distributed systems, so the real choice is CP vs AP.

### Q8: How does Cassandra data modeling work?
**Design principle:** Model around queries, not relationships (opposite of SQL).

```
-- Query: Get all orders for a user, sorted by date
CREATE TABLE orders_by_user (
    user_id UUID,
    order_date TIMESTAMP,
    order_id UUID,
    total DECIMAL,
    PRIMARY KEY ((user_id), order_date)
) WITH CLUSTERING ORDER BY (order_date DESC);
```
- **Partition key** `(user_id)`: determines which node stores the data
- **Clustering key** `order_date`: sorts data within the partition
- **Rule:** One table per query pattern. Denormalization is expected.

### Q9: SQL vs NoSQL — when to use which?
| Criteria | Choose SQL | Choose NoSQL |
|----------|-----------|--------------|
| Data relationships | Complex, many joins | Simple, denormalized |
| Schema | Stable, well-defined | Evolving, flexible |
| Consistency | Strong ACID required | Eventual is acceptable |
| Scale | Vertical (bigger machine) | Horizontal (more nodes) |
| Write volume | Moderate | Very high (100K+ ops/sec) |
| Use case | Banking, ERP, billing | IoT, logging, real-time feeds |

### Q10: What is sharding?
Splitting data across multiple database instances by a shard key:
```
Shard Key: user_id % 4

Shard 0: users 0,4,8,12...   → DB Server A
Shard 1: users 1,5,9,13...   → DB Server B
Shard 2: users 2,6,10,14...  → DB Server C
Shard 3: users 3,7,11,15...  → DB Server D
```
**Challenges:** Cross-shard joins, rebalancing, hotspots.

### Q11: What is replication?
Copying data across multiple nodes for availability and read scaling:
- **Master-Slave:** Writes to master, reads from replicas (read scaling)
- **Master-Master:** Writes to any node (conflict resolution needed)
- **Synchronous:** Write confirmed after all replicas acknowledge (strong consistency, slower)
- **Asynchronous:** Write confirmed immediately, replicas catch up (faster, risk of data loss)

### Q12: How to prevent SQL injection?
```java
// VULNERABLE — string concatenation
String sql = "SELECT * FROM users WHERE name = '" + userInput + "'";
// Input: ' OR '1'='1  → returns all users!

// SAFE — prepared statement (parameterized query)
PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
ps.setString(1, userInput);  // Input is treated as data, never as SQL
```

### Q13: What are database indexes and when NOT to use them?
**Use indexes when:**
- Columns in WHERE, JOIN, ORDER BY clauses
- High cardinality columns (many distinct values)
- Read-heavy tables

**Don't use indexes when:**
- Small tables (full scan is faster)
- Write-heavy tables (index maintenance overhead)
- Low cardinality columns (boolean, status with 2-3 values)
- Columns rarely used in queries

### Q14: Query optimization techniques?
1. **EXPLAIN ANALYZE** — read the execution plan
2. **Add indexes** on WHERE/JOIN/ORDER BY columns
3. **Avoid SELECT *** — fetch only needed columns
4. **Use LIMIT** for pagination
5. **Avoid functions on indexed columns** — `WHERE YEAR(date) = 2024` won't use index
6. **Batch operations** — bulk INSERT instead of row-by-row
7. **Denormalize** for read-heavy queries
8. **Partition large tables** by date/range

### Q15: What is a deadlock?
Two transactions waiting for each other's locks:
```
T1: LOCK row A → wants LOCK row B (waiting for T2)
T2: LOCK row B → wants LOCK row A (waiting for T1)
→ DEADLOCK — DB kills one transaction
```
**Prevention:** Always acquire locks in the same order. Keep transactions short.

---

## 6. Tricky Edge Cases or Pitfalls

| Pitfall | What Happens | Fix |
|---------|-------------|-----|
| N+1 queries in ORM | 100 orders = 101 queries | JOIN FETCH or batch fetching |
| Connection pool exhaustion | App hangs, requests timeout | Set proper pool size, add timeout |
| Cassandra tombstones | Deletes create tombstones, slow reads | Use TTL, run compaction |
| Index on low-cardinality column | Full index scan slower than table scan | Remove index, use partial index |
| Long-running transaction | Holds locks, blocks others | Keep transactions short |
| SELECT * with large BLOBs | OOM, network saturation | Select only needed columns |
| Cassandra large partitions | Hot spots, OOM on node | Limit partition size (<100MB) |
| Missing WHERE on UPDATE/DELETE | Updates/deletes entire table | Always test with SELECT first |

---

## 7. Comparison with Related Concepts

### SQL Databases Compared
| Feature | PostgreSQL | MySQL | Oracle |
|---------|-----------|-------|--------|
| MVCC | ✅ | ✅ (InnoDB) | ✅ |
| JSON support | ✅ (native) | ✅ (5.7+) | ✅ |
| Partitioning | ✅ | ✅ | ✅ |
| Default isolation | READ COMMITTED | REPEATABLE READ | READ COMMITTED |
| License | Open source | Open source (GPL) | Commercial |

### NoSQL Databases Compared
| Feature | Cassandra | MongoDB | Redis | DynamoDB |
|---------|-----------|---------|-------|----------|
| Model | Wide-column | Document | Key-Value | Key-Value/Document |
| Consistency | Tunable (AP) | Strong (CP) | Strong (single) | Tunable |
| Scale | Linear horizontal | Horizontal (sharding) | In-memory cluster | Managed, auto-scale |
| Query language | CQL | MQL (JSON-like) | Commands | PartiQL |
| Best for | Time-series, high writes | Flexible schema, aggregation | Caching, sessions | Serverless, AWS-native |

### ORM vs Raw SQL
| Aspect | ORM (JPA/Hibernate) | Raw SQL (JDBC) |
|--------|-------------------|----------------|
| Productivity | High (less boilerplate) | Low (manual mapping) |
| Performance | Can be slower (N+1, lazy load) | Full control |
| Portability | DB-agnostic | DB-specific |
| Complex queries | Limited (JPQL) | Full SQL power |
| Best for | CRUD-heavy apps | Complex reporting, batch |

---

## 8. Performance Impact

### Connection Pool Sizing
```
Optimal pool size = (core_count * 2) + effective_spindle_count

Example: 4-core server with SSD
Pool size = (4 * 2) + 1 = 9-10 connections
```
**Too small:** Threads wait for connections → increased latency.
**Too large:** DB overwhelmed with connections → context switching overhead.

### Index Impact
| Operation | Without Index | With Index |
|-----------|--------------|------------|
| SELECT by PK | O(n) full scan | O(log n) B-tree |
| INSERT | Fast | Slower (index maintenance) |
| UPDATE indexed col | Fast | Slower (reindex) |
| DELETE | O(n) to find | O(log n) to find |

### Cassandra Performance
- **Writes:** O(1) — append to commit log + memtable
- **Reads:** Depends on partition size and number of SSTables
- **Consistency level impact:**
  - `ONE`: fastest, least consistent
  - `QUORUM`: balanced (majority of replicas)
  - `ALL`: slowest, strongest consistency

---

## 9. Trade-offs

### When to Use SQL
✅ Complex relationships and joins needed
✅ ACID transactions required (banking, billing)
✅ Data integrity is critical
✅ Ad-hoc queries and reporting
✅ Team knows SQL well

### When to Use NoSQL
✅ Massive scale (millions of ops/sec)
✅ Schema changes frequently
✅ Simple access patterns (key lookup, time-series)
✅ High availability > strong consistency
✅ Geographic distribution needed

### When NOT to Use
❌ **SQL for:** High-velocity event streams, simple key-value lookups
❌ **NoSQL for:** Complex joins, multi-entity transactions, ad-hoc reporting

---

## 10. 30–60 Second Interview Answers

### "Explain ACID"
> "ACID ensures reliable transactions. Atomicity means all-or-nothing — if any part fails, the whole transaction rolls back. Consistency ensures the database moves between valid states respecting all constraints. Isolation means concurrent transactions don't see each other's uncommitted changes — controlled by isolation levels from READ UNCOMMITTED to SERIALIZABLE. Durability means once committed, data survives crashes because it's written to the write-ahead log on disk."

### "SQL vs NoSQL — how do you choose?"
> "I choose based on the access pattern. If I need complex joins, strong consistency, and ACID transactions — like billing or user management — I use PostgreSQL. If I need massive write throughput with simple query patterns — like event logging or time-series data — I use Cassandra. In our 5G platform at Ericsson, we use both: PostgreSQL for subscription management and Cassandra for network event logs that handle millions of writes per second."

### "Explain the N+1 problem"
> "N+1 happens when an ORM lazily loads related entities. You fetch N parent records in one query, then for each parent, it fires a separate query to load children — so N+1 total queries. The fix is eager fetching with JOIN FETCH in JPQL, or using @EntityGraph, or batch fetching. I always check Hibernate's SQL output in dev to catch this early."

### "What is CAP theorem?"
> "CAP says a distributed database can only guarantee two of three: Consistency, Availability, and Partition tolerance. Since network partitions are inevitable in distributed systems, the real choice is CP or AP. Cassandra is AP — it stays available during partitions but may serve stale reads. MongoDB is CP — it becomes unavailable during partitions to maintain consistency. You pick based on whether your use case tolerates stale data."

---

## 11. Real Production Scenario

**Scenario: Connection Pool Exhaustion at Ericsson**

**Symptoms:** API response times spiked from 50ms to 30s during peak traffic. Some requests timing out with "Unable to acquire connection from pool."

**Root Cause:** Default HikariCP pool size was 10. A slow downstream service caused transactions to hold connections for 5+ seconds. 10 connections × 5s hold time = only 2 requests/sec throughput.

**Investigation:**
```sql
-- Check active connections in PostgreSQL
SELECT count(*) FROM pg_stat_activity WHERE state = 'active';

-- Find long-running queries
SELECT pid, now() - pg_stat_activity.query_start AS duration, query
FROM pg_stat_activity
WHERE state != 'idle'
ORDER BY duration DESC;
```

**Fix:**
1. Increased pool size to 20 (based on formula: cores × 2 + spindles)
2. Added connection timeout: `spring.datasource.hikari.connection-timeout=5000`
3. Added statement timeout: `spring.jpa.properties.javax.persistence.query.timeout=3000`
4. Isolated slow downstream calls to async processing (don't hold DB connection while waiting for HTTP)

**Lesson:** Always set explicit timeouts. A connection pool without timeouts is a ticking time bomb.

---

## 12. If This Fails, How to Debug

| Symptom | Likely Cause | Debug Steps |
|---------|-------------|-------------|
| "Unable to acquire connection" | Pool exhausted | Check `HikariPoolMXBean`, increase pool or fix leaks |
| Slow queries | Missing index or full table scan | Run `EXPLAIN ANALYZE`, add index |
| Deadlock detected | Circular lock dependency | Check `pg_locks`, reorder lock acquisition |
| Cassandra read timeout | Large partition or too many tombstones | Check partition size, run `nodetool tablestats` |
| Stale reads | Replication lag | Check replica lag, use `QUORUM` consistency |
| OOM on query | SELECT * on large table | Add LIMIT, paginate, select specific columns |

### Debug Commands

**PostgreSQL:**
```sql
-- Slow queries
SELECT * FROM pg_stat_statements ORDER BY total_time DESC LIMIT 10;

-- Lock contention
SELECT * FROM pg_locks WHERE NOT granted;

-- Table bloat
SELECT relname, n_dead_tup FROM pg_stat_user_tables ORDER BY n_dead_tup DESC;
```

**Cassandra:**
```bash
# Node status
nodetool status

# Table statistics (partition sizes, tombstones)
nodetool tablestats keyspace.table

# Slow queries (tracing)
TRACING ON;
SELECT * FROM orders_by_user WHERE user_id = ?;
```

**HikariCP monitoring (Spring Boot Actuator):**
```
GET /actuator/metrics/hikaricp.connections.active
GET /actuator/metrics/hikaricp.connections.idle
GET /actuator/metrics/hikaricp.connections.pending
```

---

## Follow-up Interview Questions

**Q1:** "You have a table with 100M rows and queries are slow. Walk me through your optimization approach."

**Expected answer:** Check EXPLAIN ANALYZE → identify full scans → add indexes on WHERE/JOIN columns → consider partitioning by date → check if query can be rewritten → consider read replicas for reporting → add caching layer for hot data.

**Q2:** "How would you migrate from a monolithic SQL database to microservices with separate databases?"

**Expected answer:** Strangler fig pattern — start with shared DB, extract one service at a time. Use CDC (Change Data Capture) or events to sync data between services during migration. Accept eventual consistency between services. Use saga pattern for distributed transactions.

---

## Practice Task

Design a Cassandra data model for a messaging app that supports:
1. Get all messages in a conversation (sorted by time)
2. Get all conversations for a user (sorted by last message time)
3. Get unread message count per conversation

Write the CQL CREATE TABLE statements and explain your partition key choices.

---

## Code Examples

See `core-java-examples/src/main/java/com/interview/database/`:
- `JdbcBasicsDemo.java` — CRUD with PreparedStatement, SQL injection prevention
- `ConnectionPoolingDemo.java` — HikariCP setup and monitoring
- `TransactionDemo.java` — ACID transactions, isolation levels, deadlock handling
