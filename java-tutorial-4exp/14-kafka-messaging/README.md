# 14 — Kafka & Messaging

> Interview-ready guide for a 3+ year Java backend engineer targeting mid-senior roles.

---

## 1. Definition

**Apache Kafka** is a distributed event streaming platform for high-throughput, fault-tolerant, real-time data pipelines and event-driven architectures.

| Concept | One-liner |
|---------|-----------|
| **Topic** | Named category/feed of messages (like a table in DB) |
| **Partition** | Ordered, immutable sequence of records within a topic — unit of parallelism |
| **Producer** | Publishes messages to topics |
| **Consumer** | Reads messages from topics |
| **Consumer Group** | Set of consumers sharing the work of reading a topic — each partition assigned to one consumer |
| **Broker** | Kafka server that stores data and serves clients |
| **Offset** | Sequential ID of a message within a partition — consumer tracks its position |
| **Replication** | Each partition replicated across brokers for fault tolerance |
| **ZooKeeper/KRaft** | Cluster coordination (ZooKeeper legacy, KRaft is the new built-in consensus) |

---

## 2. Why This Is Needed

| Problem | Kafka Solution |
|---------|---------------|
| Microservices need async communication | Event-driven decoupling — producer doesn't wait for consumer |
| Handling traffic spikes (10x burst) | Kafka buffers messages — consumers process at their own pace |
| Data loss during service outages | Messages persisted to disk, replicated across brokers |
| Multiple services need same event | One event published, multiple consumer groups read independently |
| Audit trail / event sourcing | Immutable log — replay events from any offset |
| Real-time analytics pipeline | Stream processing with Kafka Streams or Flink |
| Order processing across services | Partition key ensures ordering per entity (e.g., per user) |

---

## 3. How It Works Internally

### Kafka Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Kafka Cluster                          │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                │
│  │ Broker 1 │  │ Broker 2 │  │ Broker 3 │                │
│  │ P0(L)    │  │ P1(L)    │  │ P2(L)    │  ← Leaders    │
│  │ P1(F)    │  │ P2(F)    │  │ P0(F)    │  ← Followers  │
│  └─────────┘  └─────────┘  └─────────┘                │
└─────────────────────────────────────────────────────────┘
        ↑                                    ↓
   Producers                            Consumers
   (round-robin                      (Consumer Group)
    or key-based)                    [C1→P0] [C2→P1] [C3→P2]
```

### Message Flow

```
Producer → serialize → partition (by key hash or round-robin)
        → send to partition leader broker
        → leader writes to commit log
        → followers replicate (ISR)
        → ack to producer (acks=0/1/all)

Consumer → poll() → fetch from partition leader
        → process message
        → commit offset (auto or manual)
```

### Partition & Ordering

```
Topic: "orders" (3 partitions)

Partition 0: [msg1, msg4, msg7, msg10]  ← key="user-A" (hash % 3 = 0)
Partition 1: [msg2, msg5, msg8, msg11]  ← key="user-B"
Partition 2: [msg3, msg6, msg9, msg12]  ← key="user-C"

Ordering guarantee: WITHIN a partition only (not across partitions)
→ All events for user-A are ordered because they go to same partition
```

### Consumer Group Rebalancing

```
Consumer Group "order-service" with 3 consumers:

Before (3 consumers, 3 partitions):
  C1 → P0, C2 → P1, C3 → P2

C3 dies → Rebalance:
  C1 → P0, P2    C2 → P1

C4 joins → Rebalance:
  C1 → P0, C2 → P1, C4 → P2
```

---

## 4. Real-World Example

### 5G Event Notification System (Ericsson NEF)

```java
// Producer: NEF publishes 5G network events
@Service
public class NetworkEventProducer {
    private final KafkaTemplate<String, NetworkEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, NetworkEvent>> publishEvent(NetworkEvent event) {
        // Key = subscriberId → ensures ordering per subscriber
        return kafkaTemplate.send("nef-events", event.getSubscriberId(), event);
    }
}

// Consumer: Notification service delivers to subscribers
@Service
public class NotificationConsumer {
    @KafkaListener(topics = "nef-events", groupId = "notification-service")
    public void handleEvent(NetworkEvent event, Acknowledgment ack) {
        try {
            notifySubscriber(event);
            ack.acknowledge(); // manual commit after successful processing
        } catch (Exception e) {
            // Don't ack → message will be redelivered
            log.error("Failed to notify subscriber={}", event.getSubscriberId(), e);
        }
    }
}
```

---

## 5. Common Interview Questions

### Q1: How does Kafka guarantee message ordering?

**Answer:** Ordering is guaranteed **within a partition only**. Messages with the same key always go to the same partition (via hash). So if you use `userId` as the key, all events for that user are ordered. Across partitions, there's no ordering guarantee. If you need global ordering, use a single partition (but lose parallelism).

### Q2: What happens when a consumer in a group dies?

**Answer:** Kafka triggers a **rebalance**. The partitions assigned to the dead consumer are redistributed among remaining consumers in the group. During rebalance, consumption pauses briefly. With `session.timeout.ms=10s`, detection takes up to 10 seconds. Cooperative sticky assignor (Kafka 2.4+) minimizes disruption by only moving affected partitions.

### Q3: Explain `acks=0` vs `acks=1` vs `acks=all`

| Setting | Behavior | Durability | Throughput |
|---------|----------|-----------|------------|
| `acks=0` | Fire and forget — don't wait for broker | Lowest (data loss possible) | Highest |
| `acks=1` | Wait for leader to write | Medium (loss if leader dies before replication) | Medium |
| `acks=all` | Wait for all ISR replicas to write | Highest (no data loss) | Lowest |

**Production recommendation:** `acks=all` + `min.insync.replicas=2` for critical data (payments, orders).

### Q4: How to handle duplicate messages (exactly-once)?

**Answer:** Three approaches:
1. **Idempotent producer** (`enable.idempotence=true`) — Kafka deduplicates at broker level using sequence numbers
2. **Transactional producer** — atomic writes across multiple partitions
3. **Consumer-side idempotency** — store processed message IDs in DB, skip duplicates

```java
// Idempotent producer config
props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
props.put(ProducerConfig.ACKS_CONFIG, "all");
props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
```

### Q5: What is a Dead Letter Queue (DLQ) in Kafka?

**Answer:** A separate topic where messages that fail processing after N retries are sent. Prevents poison pills from blocking the consumer.

```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(template); // sends to topic.DLT
    return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3)); // 3 retries, 1s apart
}
```

### Q6: Kafka vs RabbitMQ — when to use which?

| Feature | Kafka | RabbitMQ |
|---------|-------|----------|
| Model | Distributed log (pull-based) | Message broker (push-based) |
| Throughput | Millions msg/sec | Thousands msg/sec |
| Ordering | Per-partition guaranteed | Per-queue guaranteed |
| Replay | Yes (retain messages, seek to offset) | No (message deleted after ack) |
| Routing | Topic + partition key | Exchange + routing key (flexible) |
| Use case | Event streaming, analytics, audit log | Task queues, RPC, complex routing |
| Delivery | At-least-once / exactly-once | At-least-once / at-most-once |

**Rule of thumb:** Kafka for event streaming and high-throughput. RabbitMQ for task distribution and complex routing patterns.

---

## 6. Tricky Edge Cases & Pitfalls

| Pitfall | What Happens | Fix |
|---------|-------------|-----|
| Auto-commit with slow processing | Offset committed before processing completes → data loss on crash | Use manual commit (`enable.auto.commit=false`) |
| Consumer slower than producer | Lag grows → eventually consumer falls behind retention | Monitor lag, scale consumers, increase partitions |
| Too many partitions | More memory, longer rebalances, more file handles | Start with `partitions = throughput / consumer_throughput` |
| Rebalance storm | Frequent consumer joins/leaves trigger constant rebalancing | Increase `session.timeout.ms`, use static membership |
| Message too large | `RecordTooLargeException` | Increase `max.message.bytes` or store payload in S3, send reference |
| Poison pill message | One bad message blocks entire partition | DLQ + error handler with max retries |
| Consumer group with more consumers than partitions | Extra consumers sit idle | Consumers ≤ partitions per group |
| Key=null | Round-robin distribution → no ordering guarantee | Always set key for ordered events |

---

## 7. Comparison with Related Concepts

### Messaging Patterns

| Pattern | Description | Kafka Implementation |
|---------|-------------|---------------------|
| **Pub/Sub** | One-to-many broadcast | Multiple consumer groups on same topic |
| **Queue** | One-to-one (competing consumers) | Single consumer group |
| **Event Sourcing** | Store all state changes as events | Compacted topic as event store |
| **CQRS** | Separate read/write models | Write to Kafka → materialize read views |
| **Saga** | Distributed transaction coordination | Events on topic per step, compensating events on failure |

### Kafka vs Other Streaming

| Feature | Kafka | AWS SQS | AWS Kinesis | Redis Streams |
|---------|-------|---------|-------------|---------------|
| Throughput | Very high | Medium | High | High |
| Ordering | Per-partition | FIFO queues only | Per-shard | Per-stream |
| Retention | Configurable (days/forever) | 14 days max | 7 days (365 extended) | Memory-limited |
| Replay | Yes | No | Yes | Yes |
| Managed | Self-hosted or MSK | Fully managed | Fully managed | Self-hosted |
| Cost | Infrastructure | Per-message | Per-shard-hour | Memory |

---

## 8. Performance Impact

### Key Performance Tuning

| Setting | Default | Tuned | Impact |
|---------|---------|-------|--------|
| `batch.size` | 16KB | 64KB-128KB | Fewer network calls, higher throughput |
| `linger.ms` | 0 | 5-20ms | Batches more messages, slight latency increase |
| `compression.type` | none | `lz4` or `snappy` | 50-80% less network/disk, small CPU cost |
| `fetch.min.bytes` | 1 | 1024-65536 | Consumer fetches larger batches, fewer requests |
| `max.poll.records` | 500 | 100-1000 | Controls batch size per poll() |
| `num.partitions` | 1 | 6-12 per topic | More parallelism (consumers = partitions) |

### Throughput Numbers (Typical)

| Scenario | Throughput |
|----------|-----------|
| Single producer, no replication | ~800K msg/sec |
| Producer with `acks=all`, replication=3 | ~200K msg/sec |
| Consumer (single thread) | ~300K msg/sec |
| End-to-end with serialization (JSON) | ~100K msg/sec |

### Consumer Lag Monitoring

```
Consumer lag = Latest offset - Consumer committed offset

Healthy: lag < 1000 (catches up quickly)
Warning: lag > 10,000 (falling behind)
Critical: lag > 100,000 (consumer can't keep up)
```

---

## 9. Trade-offs

| Decision | Option A | Option B | When to Choose |
|----------|----------|----------|----------------|
| Commit strategy | Auto-commit | Manual commit | Manual for at-least-once guarantee |
| Serialization | JSON | Avro + Schema Registry | Avro for schema evolution + smaller payloads |
| Partitions | Few (3) | Many (30) | More = more parallelism but more overhead |
| Retention | 7 days | Infinite (compacted) | Infinite for event sourcing, 7d for transient events |
| Replication | RF=2 | RF=3 | RF=3 for production (survives 1 broker loss) |
| Consumer model | Single-threaded poll | Multi-threaded processing | Multi-thread for CPU-heavy processing |
| Delivery | At-least-once | Exactly-once | Exactly-once adds latency, use when critical |

---

## 10. 30–60 Second Interview Answers

### "Explain Kafka architecture in 30 seconds"

> "Kafka is a distributed commit log. Messages are published to topics, which are split into partitions for parallelism. Each partition is an ordered, immutable sequence replicated across brokers for fault tolerance. Producers write to partition leaders, consumers read in groups where each partition is assigned to exactly one consumer. Ordering is guaranteed within a partition. Consumer groups enable both pub/sub (multiple groups) and queue (single group) patterns."

### "How do you ensure no message loss?"

> "Three layers: Producer side — set `acks=all` so the message is written to all in-sync replicas before acknowledging. Broker side — set `replication.factor=3` and `min.insync.replicas=2` so data survives broker failures. Consumer side — disable auto-commit, process the message fully, then manually commit the offset. If the consumer crashes before committing, the message is redelivered."

### "How do you handle exactly-once processing?"

> "Kafka provides exactly-once semantics through idempotent producers (dedup via sequence numbers) and transactional writes (atomic multi-partition writes). On the consumer side, I use the transactional consumer pattern or implement idempotency in my service — store processed message IDs and skip duplicates. For most cases, at-least-once with idempotent consumers is simpler and sufficient."

---

## 11. Real Production Scenario

### Scenario: Consumer Lag Spike During Peak Traffic (Ericsson)

**Context:** 5G event notification pipeline — Kafka topic with 12 partitions, 4 consumer instances.

**Symptom:** Consumer lag jumped from ~100 to 500,000 during morning traffic peak. Notifications delayed by 15+ minutes.

**Root Cause:**
1. Each consumer was processing messages synchronously — making HTTP calls to subscriber endpoints
2. Some subscribers had slow endpoints (2-5s timeout) blocking the consumer thread
3. With `max.poll.interval.ms=300s` default, consumers weren't kicked out but were barely making progress

**Fix:**
```java
// Before: synchronous processing in consumer thread
@KafkaListener(topics = "nef-events")
public void handle(NetworkEvent event) {
    httpClient.post(event.getCallbackUrl(), event); // blocks 2-5s per message!
}

// After: async processing with bounded thread pool
@KafkaListener(topics = "nef-events", concurrency = "12") // match partitions
public void handle(NetworkEvent event, Acknowledgment ack) {
    CompletableFuture.runAsync(() -> httpClient.post(event.getCallbackUrl(), event), asyncPool)
        .orTimeout(3, TimeUnit.SECONDS)
        .whenComplete((result, ex) -> {
            if (ex != null) deadLetterProducer.send(event);
            ack.acknowledge();
        });
}
```

Plus: increased partitions from 12 to 24, scaled consumers to 8 instances, added DLQ for failed deliveries.

**Result:** Lag dropped to <100, notifications delivered within 2 seconds.

---

## 12. If This Fails, How to Debug

| Symptom | Likely Cause | Debug |
|---------|-------------|-------|
| Consumer lag growing | Consumer too slow or too few consumers | `kafka-consumer-groups.sh --describe --group X` |
| Messages not arriving | Wrong topic name, producer errors, serialization failure | Check producer logs, `kafka-console-consumer.sh` |
| Rebalance loop | Consumer processing too slow, exceeds `max.poll.interval.ms` | Reduce `max.poll.records`, increase interval, async processing |
| Duplicate messages | Consumer crash before offset commit | Implement idempotent consumer (dedup by message ID) |
| `RecordTooLargeException` | Message exceeds `max.message.bytes` | Increase limit or externalize payload |
| Consumer not reading | Consumer group stuck in rebalance | Check `session.timeout.ms`, consumer health |
| Data loss | `acks=1` + leader crash before replication | Set `acks=all`, `min.insync.replicas=2` |

### Useful Commands

```bash
# List topics
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Describe topic (partitions, replicas, ISR)
kafka-topics.sh --describe --topic nef-events

# Consumer group lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --describe --group notification-service

# Read from beginning
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic nef-events --from-beginning --max-messages 10

# Producer perf test
kafka-producer-perf-test.sh --topic test --num-records 1000000 \
  --record-size 1000 --throughput -1 --producer-props bootstrap.servers=localhost:9092
```

---

## Follow-Up Interview Questions

**Q1:** "You have an order service that publishes events to Kafka. The payment service and inventory service both need to consume these events independently. How would you design this?"

**Answer:**

```
Order Service → publishes to topic "order-events" (key = orderId)

Consumer Group "payment-service":
  - Reads all order events independently
  - Processes payment for new orders
  - Publishes "payment-completed" event

Consumer Group "inventory-service":
  - Reads same order events independently
  - Reserves inventory for new orders
  - Publishes "inventory-reserved" event
```

**Key design decisions:**
- **Separate consumer groups** — each service gets all messages independently (pub/sub pattern)
- **orderId as partition key** — all events for same order go to same partition → ordering guaranteed
- **Idempotent consumers** — both services store processed orderIds to handle redelivery
- **Saga coordination** — if payment fails, publish compensating event that inventory service listens to

---

**Q2:** "Your Kafka consumer is processing 10K messages/sec but suddenly lag spikes to millions. How do you diagnose and fix?"

**Answer:**

**Diagnose:**
1. `kafka-consumer-groups.sh --describe` — check which partitions have lag
2. Check consumer logs for errors (serialization failures, downstream timeouts)
3. Monitor consumer `poll()` rate — if it dropped, processing is blocking
4. Check if rebalances are happening (consumer joining/leaving)

**Immediate fix:**
- Scale consumers to match partition count
- If processing is slow: reduce `max.poll.records` to avoid `max.poll.interval.ms` timeout
- If downstream is slow: add async processing with bounded thread pool

**Long-term fix:**
- Increase partitions (requires topic recreation or `kafka-topics.sh --alter`)
- Add circuit breaker for slow downstream calls
- Implement backpressure: pause partitions when processing queue is full
- Add DLQ for poison pills blocking progress

---

## Practice Task

Build a Spring Boot Kafka application with:
1. Producer that sends order events with orderId as key
2. Consumer with manual offset commit
3. Error handler with DLQ (3 retries, then dead letter topic)
4. Configuration for both dev (embedded) and prod (cluster)

→ See code in `spring-boot-examples/src/main/java/com/interview/springboot/kafka/`

---

## Code Examples

| File | Topics |
|------|--------|
| [KafkaProducerDemo.java](../spring-boot-examples/src/main/java/com/interview/springboot/kafka/KafkaProducerDemo.java) | Producer with key-based partitioning, async send, callbacks |
| [KafkaConsumerDemo.java](../spring-boot-examples/src/main/java/com/interview/springboot/kafka/KafkaConsumerDemo.java) | Consumer with manual commit, error handling, DLQ |
| [KafkaConfig.java](../spring-boot-examples/src/main/java/com/interview/springboot/kafka/KafkaConfig.java) | Production-ready configuration with serialization, error handling |
