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
| 10 | SQL | 10 | SQL | 10 | SQL | 10 | SQL | 10 | SQL | 10 | SQL | 10 | SQL | 10 | SQL | 10 | SQL & NoSQL | `10-sql-nosql/README.md` | `core-java-examples/.../database/` | ❌ TODO | 🟡 Medium | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE | NoSQL | `10-sql-nosql/README.md` | `database/` | ✅ DONE |
| 11 | Testing | `11-testing/README.md` | `testing/` | ✅ DONE |
| 12 | System Design | `12-system-design/README.md` | `NONE/` | ✅ DONE |
| 13 | DSA | `13-dsa/README.md` | `dsa/` | ✅ DONE |
| 14 | Kafka & Messaging | `14-kafka-messaging/README.md` | `spring-boot-examples/.../kafka/` | ❌ TODO | 🟡 Medium |
| 15 | Spring Security | `15-spring-security/README.md` | `spring-boot-examples/.../security/` | ❌ TODO | 🟡 Medium |
| 16 | Docker & Kubernetes | `16-docker-k8s/README.md` | *(no code)* | ✅ DONE |
| 17 | CI/CD | `17-cicd/README.md` | *(no code)* | ✅ DONE | 🟡 Medium |

**Completed: 15/17**

## Backlog — Missed Topics ✅ DONE (2026-05-26)

All missed topics have been added to their respective READMEs:

### 01-core-java — Added:
- ✅ SOLID Principles (with code examples, 30-sec answer)
- ✅ Association, Aggregation, Composition (table + code)
- ✅ Cohesion & Coupling (good/bad examples, microservices context)
- ✅ Shutdown Hook (K8s graceful shutdown pattern)
- ✅ finalize() (why deprecated, modern alternatives: Cleaner, try-with-resources)

### 02-collections — Already covered:
- ✅ Fail-Fast vs Fail-Safe Iterators (was already in README)

### 04-multithreading — Added:
- ✅ User vs Daemon Thread (table + pitfalls)
- ✅ Semaphore (rate limiting pattern, comparison with synchronized)
- ✅ CountDownLatch (microservice startup pattern)
- ✅ CyclicBarrier (parallel phase processing, comparison table with CountDownLatch)
- ✅ Exchanger (double-buffering pattern)
- ✅ BlockingQueue deep dive (all implementations, Kafka consumer pattern)
- ✅ Object Monitor (wait/notify rules, spurious wakeups)
- ✅ Context Switching (cost, measurement commands, impact)
- ✅ Busy Spinning (Thread.onSpinWait, when to use, comparison table)
- ✅ Thread Dump quick reference (commands, how to read)

### 07-spring-boot — Added:
- ✅ Logging & Log Levels (SLF4J/Logback, MDC, best practices, 30-sec answer)

---

## How to Resume

In a new session, say:
> "Continue my interview study guide — check PROGRESS.md in /local/github/CloudNative/java-tutorial-4exp/"

The agent will read this file, load the TODO list, and pick up the next pending topic.
