# Interview Study Guide — Progress Tracker

**Repo:** `/local/github/CloudNative/java-tutorial-4exp/`
**GitHub:** `https://github.com/selva060420/CloudNative.git` (branch: `main`)
**Symlink:** `/home/elxesxx/github/CloudNative` → `/local/github/CloudNative`

## Repo Structure

```
java-tutorial-4exp/
├── pom.xml                              ← parent POM (Java 17, multi-module)
├── core-java-examples/                  ← plain Java module (topics 01-06, 10-11, 13)
│   ├── pom.xml
│   └── src/main/java/com/interview/
│       ├── collections/                 ← ✅ done
│       ├── corejava/                    ← pending
│       ├── java8plus/                   ← pending
│       ├── multithreading/              ← pending
│       ├── exceptions/                  ← pending
│       ├── patterns/                    ← pending
│       ├── database/                    ← pending
│       ├── testing/                     ← pending
│       └── dsa/                         ← pending
├── spring-boot-examples/                ← Spring Boot module (topics 07-09)
│   ├── pom.xml
│   └── src/main/java/com/interview/
│       ├── springboot/                  ← pending
│       ├── microservices/               ← pending
│       └── restapi/                     ← pending
└── XX-topic-name/README.md              ← study guide READMEs (12-point template)
```

## Template

Every README must follow the 12-point template:
1. Definition | 2. Why needed | 3. How it works internally | 4. Real-world example
5. Interview questions | 6. Edge cases/pitfalls | 7. Comparisons | 8. Performance impact
9. Trade-offs | 10. 30-60 sec answer | 11. Production scenario | 12. Debug guide
Plus: 2 follow-up questions, 1 practice task, runnable code examples

## Progress

| # | Topic | README | Code | Status | Interview Priority |
|---|-------|--------|------|--------|-------------------|
| 01 | Core Java | `01-core-java/README.md` | `corejava/` | ✅ DONE |
| 02 | Collections | `02-collections/README.md` | `collections/` | ✅ DONE |
| 03 | Java 8+ Features | `03-java8-plus/README.md` | `java8plus/` | ✅ DONE |
| 04 | Multithreading | `04-multithreading/README.md` | `multithreading/` | ✅ DONE |
| 05 | Exception Handling | `05-exception-handling/README.md` | `exceptions/` | ✅ DONE |
| 06 | Design Patterns | `06-design-patterns/README.md` | `patterns/` | ✅ DONE |
| 07 | Spring Boot | `07-spring-boot/README.md` | `springboot/` | ✅ DONE |
| 08 | Microservices | `08-microservices/README.md` | `microservices/` | ✅ DONE |
| 09 | REST API | `09-rest-api/README.md` | `restapi/` | ✅ DONE |
| 10 | SQL | 10 | SQL | 10 | SQL | 10 | SQL & NoSQL | `10-sql-nosql/README.md` | `core-java-examples/.../database/` | ❌ TODO | 🟡 Medium | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE |
| 11 | Testing | `11-testing/README.md` | `core-java-examples/.../testing/` | ❌ TODO | 🟢 Low |
| 12 | System Design | `12-system-design/README.md` | `NONE/` | ✅ DONE |
| 13 | DSA | `13-dsa/README.md` | `core-java-examples/.../dsa/` | ❌ TODO | 🔴 High |
| 14 | Kafka & Messaging | `14-kafka-messaging/README.md` | `spring-boot-examples/.../kafka/` | ❌ TODO | 🟡 Medium |
| 15 | Spring Security | `15-spring-security/README.md` | `spring-boot-examples/.../security/` | ❌ TODO | 🟡 Medium |
| 16 | Docker & Kubernetes | `16-docker-k8s/README.md` | *(no code)* | ❌ TODO | 🟡 Medium |

**Completed: 6/16**

## Backlog — Missed Topics (to add after all 16 topics done)

### 01-core-java

| Topic | Priority | Notes |
|-------|----------|-------|
| Association, Aggregation, Composition | 🟡 Medium | HAS-A relationships, weak vs strong |
| Cohesion & Coupling | 🟡 Medium | High/low cohesion, tight/loose coupling |
| Shutdown Hook | 🟢 Low | `Runtime.getRuntime().addShutdownHook()` |
| finalize() | 🟢 Low | Deprecated since Java 9; mention Cleaner replacement |

### 02-collections

| Topic | Priority | Notes |
|-------|----------|-------|
| Queue interface (ArrayDeque, LinkedList as Queue) | 🟡 Medium | Deque operations, when to use over Stack |

### 03-java8-plus

| Topic | Priority | Notes |
|-------|----------|-------|
| Date/Time API (LocalDate, ZonedDateTime, Duration) | 🔴 High | Frequently asked |
| Collectors (groupingBy, partitioningBy, toMap, joining) | 🔴 High | Frequently asked |
| `var` keyword (Java 10) | 🟡 Medium | Type inference, limitations |

### 04-multithreading

| Topic | Priority | Notes |
|-------|----------|-------|
| User Thread vs Daemon Thread | 🟡 Medium | JVM exits when only daemons remain |
| Semaphore | 🔴 High | Fixed permits, acquire/release |
| CountdownLatch | 🔴 High | await + countDown |
| CyclicBarrier | 🔴 High | Reusable barrier point |
| Exchanger | 🟢 Low | Two-thread object swap |
| Blocking Queue | 🔴 High | put/take, producer-consumer |
| Object Monitor | 🟡 Medium | monitorenter/monitorexit bytecode |
| Context Switching | 🟡 Medium | Save/restore thread state |
| Busy Spinning | 🟢 Low | CPU-wasting wait loop |
| Thread Scheduler / Time Slicing | 🟢 Low | Round-robin CPU allocation |
| Thread Group | 🟢 Low | Rarely used, not recommended — but asked as trivia |

### 05-exception-handling

No gaps — complete.

### New topic needed

| Topic | Priority | Notes |
|-------|----------|-------|
| Logging / Log Levels | 🟡 Medium | Debug, Info, Warn, Error, Trace — no existing topic covers this |

---

## How to Resume

In a new session, say:
> "Continue my interview study guide — check PROGRESS.md in /local/github/CloudNative/java-tutorial-4exp/"

The agent will read this file, load the TODO list, and pick up the next pending topic.
