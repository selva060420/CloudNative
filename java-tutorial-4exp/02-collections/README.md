# Java Collections — Interview Guide

---

## 1. Definition

Java Collections Framework is a unified architecture of interfaces (`List`, `Set`, `Queue`, `Map`) and classes (`ArrayList`, `HashMap`, `TreeSet`, etc.) that provides ready-made data structures and algorithms for storing, retrieving, and manipulating groups of objects.

---

## 2. Why This Is Needed

**Problem without collections:** Arrays are fixed-size, no built-in search/sort, no type safety (pre-generics), manual resizing, no thread-safe variants.

**What collections solve:**
- **Dynamic sizing** — ArrayList grows automatically, no manual array copying
- **Type safety** — Generics prevent ClassCastException at compile time
- **Rich API** — Built-in sort, search, filter, group operations
- **Thread safety** — ConcurrentHashMap, CopyOnWriteArrayList for multi-threaded apps
- **Interchangeability** — Code to interfaces (`List`, `Map`), swap implementations without changing logic

---

## Table of Contents
- [Collections Hierarchy](#collections-hierarchy)
- [Time Complexity Comparison](#time-complexity-comparison)
- [HashMap Internals](#hashmap-internals)
- [ConcurrentHashMap vs HashMap vs Hashtable](#concurrenthashmap-vs-hashmap-vs-hashtable)
- [ArrayList vs LinkedList](#arraylist-vs-linkedlist)
- [HashSet vs TreeSet vs LinkedHashSet](#hashset-vs-treeset-vs-linkedhashset)
- [Comparable vs Comparator](#comparable-vs-comparator)
- [equals() and hashCode() Contract](#equals-and-hashcode-contract)
- [Fail-Fast vs Fail-Safe Iterators](#fail-fast-vs-fail-safe-iterators)
- [Stream API with Collections](#stream-api-with-collections)
- [Immutable Collections (Java 9+)](#immutable-collections-java-9)
- [Interview Questions](#interview-questions)
- [Code Examples](#code-examples)

---

## Collections Hierarchy

```
Iterable
 └── Collection
      ├── List (ordered, allows duplicates)
      │    ├── ArrayList        — dynamic array, O(1) random access
      │    ├── LinkedList       — doubly linked list, O(1) insert/delete at ends
      │    ├── Vector           — synchronized ArrayList (legacy)
      │    └── Stack            — LIFO, extends Vector (legacy)
      │
      ├── Set (no duplicates)
      │    ├── HashSet          — backed by HashMap, O(1) add/remove/contains
      │    ├── LinkedHashSet    — insertion order preserved
      │    ├── TreeSet          — sorted (Red-Black tree), O(log n)
      │    └── EnumSet          — specialized for enums, bit-vector based
      │
      └── Queue
           ├── PriorityQueue   — min-heap, O(log n) insert/remove
           ├── ArrayDeque       — resizable array deque, faster than Stack/LinkedList
           └── BlockingQueue    — thread-safe (ArrayBlockingQueue, LinkedBlockingQueue)

Map (key-value pairs, NOT part of Collection interface)
 ├── HashMap            — O(1) avg, allows null key/values
 ├── LinkedHashMap      — insertion/access order preserved
 ├── TreeMap            — sorted by keys (Red-Black tree), O(log n)
 ├── Hashtable          — synchronized (legacy), no null key/values
 ├── ConcurrentHashMap  — segment-level locking, thread-safe
 ├── WeakHashMap        — keys are weak references (GC can reclaim)
 └── EnumMap            — specialized for enum keys
```

---

## Time Complexity Comparison

### List

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| get(index) | O(1) | O(n) |
| add(end) | O(1) amortized | O(1) |
| add(index) | O(n) | O(n)* |
| remove(index) | O(n) | O(n)* |
| contains | O(n) | O(n) |

*LinkedList: O(1) if you already have the node reference, O(n) to find it.

### Map

| Operation | HashMap | TreeMap | LinkedHashMap | ConcurrentHashMap |
|-----------|---------|---------|---------------|-------------------|
| put | O(1) avg | O(log n) | O(1) avg | O(1) avg |
| get | O(1) avg | O(log n) | O(1) avg | O(1) avg |
| remove | O(1) avg | O(log n) | O(1) avg | O(1) avg |
| containsKey | O(1) avg | O(log n) | O(1) avg | O(1) avg |

### Set

| Operation | HashSet | TreeSet | LinkedHashSet |
|-----------|---------|---------|---------------|
| add | O(1) | O(log n) | O(1) |
| remove | O(1) | O(log n) | O(1) |
| contains | O(1) | O(log n) | O(1) |

---

## HashMap Internals

### How HashMap Works (Java 8+)

1. **Structure**: Array of `Node<K,V>[]` (called buckets), default size = 16
2. **Hashing**: `key.hashCode()` → spread bits → `index = hash & (n-1)`
3. **Collision handling**:
   - **Linked List** when bucket has < 8 entries
   - **Red-Black Tree** when bucket has >= 8 entries (treeification) — improves worst case from O(n) to O(log n)
   - Converts back to linked list when entries drop below 6 (untreeify)
4. **Resizing**: When `size > capacity * loadFactor (0.75)`, capacity doubles and all entries are rehashed

### Key Constants

```
DEFAULT_INITIAL_CAPACITY = 16
DEFAULT_LOAD_FACTOR = 0.75
TREEIFY_THRESHOLD = 8       // linked list → tree
UNTREEIFY_THRESHOLD = 6     // tree → linked list
MIN_TREEIFY_CAPACITY = 64   // min table size for treeification
```

### put() Flow

```
1. Calculate hash: hash = key.hashCode() ^ (key.hashCode() >>> 16)
2. Find bucket: index = hash & (capacity - 1)
3. If bucket empty → insert new Node
4. If bucket occupied:
   a. If key matches (hash equal + equals()) → replace value
   b. If TreeNode → insert into Red-Black tree
   c. Else → append to linked list
      - If list length >= TREEIFY_THRESHOLD → convert to tree
5. If size > threshold → resize (double capacity)
```

### Why capacity is always power of 2?

`index = hash & (capacity - 1)` works as a fast modulo operation only when capacity is a power of 2. This is faster than `hash % capacity`.

### Null handling

- HashMap allows **one null key** (stored at index 0) and **multiple null values**
- TreeMap does NOT allow null keys (needs comparison)
- ConcurrentHashMap does NOT allow null keys or values

→ See [HashMapInternalsDemo.java](../core-java-examples/src/main/java/com/interview/collections/hashmap/HashMapInternalsDemo.java)

---

## ConcurrentHashMap vs HashMap vs Hashtable

| Feature | HashMap | Hashtable | ConcurrentHashMap |
|---------|---------|-----------|-------------------|
| Thread-safe | No | Yes (synchronized) | Yes (segment locking) |
| Null key/value | 1 null key, N null values | No nulls | No nulls |
| Performance | Best (single-thread) | Poor (full lock) | Good (concurrent) |
| Iterator | Fail-fast | Fail-fast | Fail-safe (weakly consistent) |
| Java version | 1.2 | 1.0 | 1.5 |

### ConcurrentHashMap Internals (Java 8+)

- Uses **CAS (Compare-And-Swap)** + **synchronized on individual bucket** (not segments like Java 7)
- Reads are **lock-free** (volatile reads)
- Writes lock only the **specific bucket** being modified
- `size()` is approximate during concurrent modifications — use `mappingCount()` for long return type

→ See [ConcurrentHashMapDemo.java](../core-java-examples/src/main/java/com/interview/collections/concurrent/ConcurrentHashMapDemo.java)

---

## ArrayList vs LinkedList

| Criteria | ArrayList | LinkedList |
|----------|-----------|------------|
| Backing structure | Dynamic array | Doubly linked list |
| Random access | O(1) — fast | O(n) — slow |
| Insert at end | O(1) amortized | O(1) |
| Insert at middle | O(n) — shifting | O(n) — traversal |
| Memory | Less (contiguous) | More (node + 2 pointers per element) |
| Cache performance | Better (locality) | Worse (scattered memory) |
| Use when | Frequent reads, rare inserts | Frequent inserts/deletes at ends |

**Interview tip**: ArrayList is almost always preferred. LinkedList wins only for queue/deque operations.

---

## HashSet vs TreeSet vs LinkedHashSet

| Feature | HashSet | TreeSet | LinkedHashSet |
|---------|---------|---------|---------------|
| Order | No order | Sorted (natural/comparator) | Insertion order |
| Null | Allows one null | No null (comparison fails) | Allows one null |
| Backing | HashMap | TreeMap (Red-Black tree) | LinkedHashMap |
| Performance | O(1) | O(log n) | O(1) |
| Use when | No order needed | Need sorted iteration | Need insertion order |

---

## Comparable vs Comparator

| Feature | Comparable | Comparator |
|---------|------------|------------|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T o)` | `compare(T o1, T o2)` |
| Modifies class | Yes (implements in class) | No (external) |
| Sorting | Single natural ordering | Multiple custom orderings |
| Null handling | Must handle manually | `Comparator.nullsFirst()` / `nullsLast()` |

```java
// Comparable — natural ordering inside the class
class Employee implements Comparable<Employee> {
    public int compareTo(Employee other) {
        return this.name.compareTo(other.name);
    }
}

// Comparator — external, multiple strategies
Comparator<Employee> bySalary = Comparator.comparingDouble(Employee::getSalary);
Comparator<Employee> byNameThenSalary = Comparator.comparing(Employee::getName)
                                                   .thenComparingDouble(Employee::getSalary);
```

→ See [ComparableVsComparatorDemo.java](../core-java-examples/src/main/java/com/interview/collections/comparable/ComparableVsComparatorDemo.java)

---

## equals() and hashCode() Contract

### Rules

1. If `a.equals(b)` is true → `a.hashCode() == b.hashCode()` **must** be true
2. If `a.hashCode() == b.hashCode()` → `a.equals(b)` **may or may not** be true (collision)
3. If `a.equals(b)` is false → hashCodes **can** be same or different

### Why both must be overridden together?

If you override `equals()` but not `hashCode()`:
- Two logically equal objects may land in **different buckets** in HashMap/HashSet
- `map.get(key)` may return null even though an equal key exists

```java
// BAD — only equals overridden
class Employee {
    String id;
    public boolean equals(Object o) { return this.id.equals(((Employee)o).id); }
    // hashCode NOT overridden — uses default Object.hashCode() (memory address)
}

Employee e1 = new Employee("101");
Employee e2 = new Employee("101");
e1.equals(e2);  // true
Set<Employee> set = new HashSet<>();
set.add(e1);
set.contains(e2);  // FALSE! Different hashCode → different bucket
```

→ See [EqualsHashCodeDemo.java](../core-java-examples/src/main/java/com/interview/collections/equalshashcode/EqualsHashCodeDemo.java)

---

## Fail-Fast vs Fail-Safe Iterators

| Feature | Fail-Fast | Fail-Safe |
|---------|-----------|-----------|
| Exception | `ConcurrentModificationException` | No exception |
| Works on | Original collection | Copy or snapshot |
| Collections | ArrayList, HashMap, HashSet | CopyOnWriteArrayList, ConcurrentHashMap |
| Memory | No extra | Extra (copy) |

```java
// Fail-fast — throws ConcurrentModificationException
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    list.remove(s);  // THROWS ConcurrentModificationException
}

// Safe removal — use Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) it.remove();  // SAFE
}

// Fail-safe — no exception
CopyOnWriteArrayList<String> cowList = new CopyOnWriteArrayList<>(List.of("a", "b"));
for (String s : cowList) {
    cowList.remove(s);  // NO exception, iterates over snapshot
}
```

---

## Stream API with Collections

### Common Operations

```java
List<Employee> employees = getEmployees();

// Filter + Map + Collect
List<String> names = employees.stream()
    .filter(e -> e.getSalary() > 50000)
    .map(Employee::getName)
    .collect(Collectors.toList());

// Grouping
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Reduce
double totalSalary = employees.stream()
    .mapToDouble(Employee::getSalary)
    .sum();

// Sorting
List<Employee> sorted = employees.stream()
    .sorted(Comparator.comparing(Employee::getName).reversed())
    .collect(Collectors.toList());

// Distinct + Count
long uniqueDepts = employees.stream()
    .map(Employee::getDepartment)
    .distinct()
    .count();

// toMap with merge function (handle duplicate keys)
Map<String, Double> maxSalaryByDept = employees.stream()
    .collect(Collectors.toMap(Employee::getDepartment, Employee::getSalary, Math::max));
```

### Stream vs Collection

| Feature | Collection | Stream |
|---------|------------|--------|
| Storage | Stores elements | Computes on demand (lazy) |
| Consumption | Reusable | Single use |
| Iteration | External (for-each) | Internal (pipeline) |
| Modification | Can modify source | Does not modify source |

→ See [StreamApiDemo.java](../core-java-examples/src/main/java/com/interview/collections/streams/StreamApiDemo.java)

---

## Immutable Collections (Java 9+)

```java
// Java 9 — List.of(), Set.of(), Map.of()
List<String> list = List.of("a", "b", "c");          // immutable
Set<String> set = Set.of("a", "b", "c");              // immutable
Map<String, Integer> map = Map.of("a", 1, "b", 2);   // immutable (max 10 entries)
Map<String, Integer> map2 = Map.ofEntries(             // immutable (any size)
    Map.entry("a", 1), Map.entry("b", 2));

// All throw UnsupportedOperationException on modification
list.add("d");  // THROWS UnsupportedOperationException

// Collections.unmodifiableList — wraps existing list (view, not copy)
List<String> mutable = new ArrayList<>(List.of("a", "b"));
List<String> unmodifiable = Collections.unmodifiableList(mutable);
mutable.add("c");           // modifies original
unmodifiable.contains("c"); // true! It's just a view

// Java 10 — List.copyOf() — true copy
List<String> copy = List.copyOf(mutable);  // independent copy, immutable
```

| Method | True copy? | Null elements? |
|--------|-----------|----------------|
| `List.of()` | N/A | No |
| `Collections.unmodifiableList()` | No (view) | Yes |
| `List.copyOf()` (Java 10) | Yes | No |

→ See [ImmutableCollectionsDemo.java](../core-java-examples/src/main/java/com/interview/collections/immutable/ImmutableCollectionsDemo.java)

---

## 4. Real-World Example (Backend / Microservices / Kubernetes)

**API Gateway Request Routing (Ericsson 5G NEF):**
```
ConcurrentHashMap<String, ServiceEndpoint> routeCache = new ConcurrentHashMap<>();

// Multiple threads handling incoming 5G API requests simultaneously
// Each thread looks up the target microservice endpoint from the cache
// ConcurrentHashMap ensures thread-safe reads without blocking

routeCache.computeIfAbsent("/nef/v1/subscriptions", key -> discoverService(key));
```

**Cassandra Query Result Caching:**
```
// LinkedHashMap as LRU cache for frequently queried subscriber profiles
LinkedHashMap<String, SubscriberProfile> cache = new LinkedHashMap<>(100, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > 1000; // evict oldest when cache exceeds 1000
    }
};
```

**Kubernetes Pod Status Tracking:**
```
// TreeMap to maintain pods sorted by creation time for rolling updates
TreeMap<Instant, PodStatus> podTimeline = new TreeMap<>();
// subMap to get pods created in last 5 minutes
podTimeline.subMap(fiveMinutesAgo, now);
```

---

## Interview Questions

### Q1: How does HashMap handle collisions?
Uses **chaining** — linked list at each bucket. In Java 8+, when a bucket has ≥8 entries, it converts to a **Red-Black tree** for O(log n) lookup instead of O(n).

### Q2: Why is the default load factor 0.75?
Trade-off between **space and time**. Lower value = more empty buckets (wastes memory). Higher value = more collisions (slower lookups). 0.75 provides ~25% empty buckets which statistically minimizes collisions.

### Q3: Can we use a mutable object as HashMap key?
Technically yes, but **never do it**. If the object's hashCode changes after insertion, the entry becomes unreachable. Always use immutable objects (String, Integer) as keys.

### Q4: Difference between HashMap and TreeMap?
HashMap is O(1) unordered; TreeMap is O(log n) sorted by keys using Red-Black tree. Use TreeMap when you need sorted iteration or range queries (`subMap`, `headMap`, `tailMap`).

### Q5: When would you use LinkedHashMap?
When you need **insertion order** or **access order** (LRU cache). Set `accessOrder=true` in constructor for LRU behavior, override `removeEldestEntry()` to cap size.

### Q6: Why does ConcurrentHashMap not allow null keys/values?
Ambiguity — `map.get(key)` returning null could mean "key not found" or "value is null". In concurrent context, you can't use `containsKey()` reliably to distinguish (race condition between containsKey and get).

### Q7: How to make a collection thread-safe?
1. `Collections.synchronizedList/Map/Set()` — wraps with synchronized blocks
2. `ConcurrentHashMap`, `CopyOnWriteArrayList` — purpose-built concurrent collections
3. Option 2 is preferred — better performance due to fine-grained locking

### Q8: What is the difference between Iterator and ListIterator?
Iterator: forward-only, works on all Collections. ListIterator: bidirectional, works only on Lists, supports `add()`, `set()`, `previousIndex()`, `nextIndex()`.

---

## 6. Tricky Edge Cases & Pitfalls

1. **Mutable HashMap key** — If you mutate a key after inserting, `get()` returns null. The entry is orphaned in the wrong bucket.
2. **ConcurrentModificationException** — Modifying a collection while iterating with for-each. Use `Iterator.remove()` or concurrent collections.
3. **HashMap with bad hashCode** — If all keys return same hashCode, HashMap degrades to O(n) linked list (O(log n) with treeification in Java 8+).
4. **Collections.unmodifiableList is a view** — Modifying the backing list changes the "unmodifiable" list too.
5. **TreeMap/TreeSet with inconsistent compareTo and equals** — If `compareTo` returns 0 but `equals` returns false, TreeSet treats them as duplicates but HashSet doesn't.
6. **Autoboxing null in Map** — `map.get("missing")` returns `null`. Unboxing to `int` throws `NullPointerException`.
7. **Arrays.asList() returns fixed-size list** — `add()`/`remove()` throws `UnsupportedOperationException`. Use `new ArrayList<>(Arrays.asList(...))`.

---

## 8. Performance Impact — Under Load

| Scenario | What Happens | Fix |
|----------|-------------|-----|
| HashMap with millions of entries | Frequent resizing (rehash all entries), GC pressure from old arrays | Set initial capacity: `new HashMap<>(expectedSize / 0.75 + 1)` |
| HashMap with poor hashCode | All entries in one bucket → O(n) lookups | Implement proper hashCode using `Objects.hash()` |
| CopyOnWriteArrayList with frequent writes | Every write copies entire array → O(n) per write, high GC | Use only when reads >> writes. Switch to `ConcurrentLinkedQueue` for write-heavy |
| Synchronized collections under high concurrency | Full lock contention, threads block each other | Use `ConcurrentHashMap` (bucket-level locking) |
| TreeMap with millions of entries | O(log n) per operation, but higher constant factor than HashMap | Use HashMap if sorting not needed |
| Large ArrayList remove from middle | O(n) shifting for every removal | Use LinkedList or iterate in reverse, or use `removeIf()` |

---

## 9. Trade-offs — When to Use / Not Use

| Collection | Use When | Don't Use When |
|------------|----------|----------------|
| **ArrayList** | Random access, mostly reads | Frequent inserts/deletes in middle |
| **LinkedList** | Queue/Deque operations | Random access needed |
| **HashMap** | Fast key-value lookup, single-threaded | Need ordering or thread safety |
| **TreeMap** | Sorted keys, range queries | Don't need ordering (HashMap is faster) |
| **LinkedHashMap** | Need insertion/access order, LRU cache | Don't need ordering |
| **ConcurrentHashMap** | Multi-threaded key-value access | Single-threaded (unnecessary overhead) |
| **HashSet** | Unique elements, fast lookup | Need ordering |
| **CopyOnWriteArrayList** | Read-heavy, rare writes | Write-heavy workloads |
| **PriorityQueue** | Always need min/max element | Need random access |

---

## 10. 30–60 Second Interview Answers

**"Explain HashMap internals":**
> HashMap uses an array of buckets. When you put a key, it computes hashCode, spreads the bits, and maps to a bucket index. If the bucket is empty, it inserts a Node. On collision, it chains entries as a linked list. In Java 8+, when a bucket exceeds 8 entries, it converts to a Red-Black tree for O(log n) instead of O(n). When the map exceeds 75% capacity, it doubles in size and rehashes everything. That's why initial capacity matters for performance.

**"HashMap vs ConcurrentHashMap":**
> HashMap is not thread-safe — concurrent writes can corrupt data or cause infinite loops during resize. ConcurrentHashMap in Java 8+ uses CAS operations and synchronized blocks on individual buckets, so reads are lock-free and writes only lock the specific bucket. It doesn't allow null keys or values because null is ambiguous in a concurrent context — you can't distinguish "key not found" from "value is null" atomically.

**"equals and hashCode contract":**
> If two objects are equal via equals(), they must have the same hashCode. If you break this contract — say override equals but not hashCode — HashMap puts equal objects in different buckets, so get() returns null for a key that logically exists. Always override both together, and use Objects.hash() for consistent implementation.

---

## 11. Real Production Scenario

**Incident at Ericsson — ConcurrentModificationException in 5G NEF service:**

A microservice maintained a `HashMap<String, Subscription>` for active 5G subscriptions. During high traffic, one thread iterated the map to send notifications while another thread added new subscriptions.

**Symptom:** Intermittent `ConcurrentModificationException` in production logs, some notifications silently dropped.

**Root cause:** HashMap is not thread-safe. The notification thread's iterator detected structural modification by the subscription thread.

**Fix:** Replaced `HashMap` with `ConcurrentHashMap`. The notification thread used `forEach()` (weakly consistent — no CME), and new subscriptions were added via `putIfAbsent()` atomically.

**Lesson:** Never share a non-thread-safe collection across threads in a microservice. Use concurrent collections or make the collection local to the thread.

---

## 12. If This Fails — How to Debug

| Symptom | Likely Cause | How to Debug |
|---------|-------------|--------------|
| `NullPointerException` on `map.get()` | Autoboxing null to primitive | Check if key exists with `containsKey()` before unboxing |
| `ConcurrentModificationException` | Modifying collection during iteration | Use `Iterator.remove()`, `removeIf()`, or concurrent collection |
| `map.get(key)` returns null for existing key | Broken `hashCode`/`equals` contract | Log `hashCode()` of both keys, verify `equals()` returns true |
| HashMap performance degrades | Poor hashCode (all same bucket) | Profile with `jvisualvm`, check bucket distribution |
| `ClassCastException` in TreeMap | Key doesn't implement Comparable, no Comparator provided | Ensure keys are Comparable or pass Comparator to constructor |
| `OutOfMemoryError` with large collections | No initial capacity set, repeated resizing | Set initial capacity, use `WeakHashMap` for caches, profile heap |
| Silent data loss in HashMap | Concurrent writes without synchronization | Thread dump → check shared HashMap access → switch to ConcurrentHashMap |
| `StackOverflowError` in hashCode | Circular reference in hashCode implementation | Exclude circular fields from hashCode calculation |

---

## Follow-Up Interview Questions

**Q1:** You have a microservice that caches 10 million user sessions in memory. Which Map implementation would you choose and why? What initial capacity would you set?

**Q2:** In a Kubernetes pod running multiple threads, you need to maintain a sorted leaderboard that updates in real-time. Which collection would you use and how would you handle concurrent access?

---

## Practice Task

Build a simple **LRU Cache** using `LinkedHashMap`:
- Max capacity of 5 entries
- When a 6th entry is added, the least recently accessed entry is evicted
- Demonstrate that accessing an entry moves it to the "most recent" position
- Make it thread-safe using `Collections.synchronizedMap()`
- Write a main method that proves the eviction works

Hint: Use `LinkedHashMap(capacity, loadFactor, accessOrder=true)` and override `removeEldestEntry()`.

---

## Code Examples

All code is in `core-java-examples/src/main/java/com/interview/collections/`:

| File | Package | Topic |
|------|---------|-------|
| [HashMapInternalsDemo.java](../core-java-examples/src/main/java/com/interview/collections/hashmap/HashMapInternalsDemo.java) | `hashmap` | HashMap put/get flow, collision, resizing |
| [ConcurrentHashMapDemo.java](../core-java-examples/src/main/java/com/interview/collections/concurrent/ConcurrentHashMapDemo.java) | `concurrent` | Thread-safe map operations |
| [ComparableVsComparatorDemo.java](../core-java-examples/src/main/java/com/interview/collections/comparable/ComparableVsComparatorDemo.java) | `comparable` | Natural vs custom ordering |
| [EqualsHashCodeDemo.java](../core-java-examples/src/main/java/com/interview/collections/equalshashcode/EqualsHashCodeDemo.java) | `equalshashcode` | Contract violation demo |
| [StreamApiDemo.java](../core-java-examples/src/main/java/com/interview/collections/streams/StreamApiDemo.java) | `streams` | Stream operations on collections |
| [ImmutableCollectionsDemo.java](../core-java-examples/src/main/java/com/interview/collections/immutable/ImmutableCollectionsDemo.java) | `immutable` | Java 9+ immutable collections |
