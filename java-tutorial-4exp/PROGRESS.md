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

| # | Topic | README | Code | Status |
|---|-------|--------|------|--------|
| 01 | Core Java | `01-core-java/README.md` | `corejava/` | ✅ DONE |
| 02 | Collections | `02-collections/README.md` | `collections/` | ✅ DONE |
| 03 | Java 8+ Features | `03-java8-plus/README.md` | `java8plus/` | ✅ DONE |
| 04 | Multithreading | `04-multithreading/README.md` | `core-java-examples/.../multithreading/` | ❌ TODO |
| 05 | Exception Handling | `05-exception-handling/README.md` | `core-java-examples/.../exceptions/` | ❌ TODO |
| 06 | Design Patterns | `06-design-patterns/README.md` | `core-java-examples/.../patterns/` | ❌ TODO |
| 07 | Spring Boot | `07-spring-boot/README.md` | `spring-boot-examples/.../springboot/` | ❌ TODO |
| 08 | Microservices | `08-microservices/README.md` | `spring-boot-examples/.../microservices/` | ❌ TODO |
| 09 | REST API | `09-rest-api/README.md` | `spring-boot-examples/.../restapi/` | ❌ TODO |
| 10 | SQL & NoSQL | `10-sql-nosql/README.md` | `core-java-examples/.../database/` | ❌ TODO |
| 11 | Testing | `11-testing/README.md` | `core-java-examples/.../testing/` | ❌ TODO |
| 12 | System Design | `12-system-design/README.md` | *(no code)* | ❌ TODO |
| 13 | DSA | `13-dsa/README.md` | `core-java-examples/.../dsa/` | ❌ TODO |

**Completed: 3/13**

## How to Resume

In a new session, say:
> "Continue my interview study guide — check PROGRESS.md in /local/github/CloudNative/java-tutorial-4exp/"

The agent will read this file, load the TODO list, and pick up the next pending topic.
