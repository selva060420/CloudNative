# Java Collections — Interview Guide

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

→ See [HashMapInternalsDemo.java](src/HashMapInternalsDemo.java)

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

→ See [ConcurrentHashMapDemo.java](src/ConcurrentHashMapDemo.java)

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

→ See [ComparableVsComparatorDemo.java](src/ComparableVsComparatorDemo.java)

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

→ See [EqualsHashCodeDemo.java](src/EqualsHashCodeDemo.java)

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

→ See [StreamApiDemo.java](src/StreamApiDemo.java)

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

→ See [ImmutableCollectionsDemo.java](src/ImmutableCollectionsDemo.java)

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

## Code Examples

| File | Topic |
|------|-------|
| [HashMapInternalsDemo.java](src/HashMapInternalsDemo.java) | HashMap put/get flow, collision, resizing |
| [ComparableVsComparatorDemo.java](src/ComparableVsComparatorDemo.java) | Natural vs custom ordering |
| [EqualsHashCodeDemo.java](src/EqualsHashCodeDemo.java) | Contract violation demo |
| [StreamApiDemo.java](src/StreamApiDemo.java) | Stream operations on collections |
| [ConcurrentHashMapDemo.java](src/ConcurrentHashMapDemo.java) | Thread-safe map operations |
| [ImmutableCollectionsDemo.java](src/ImmutableCollectionsDemo.java) | Java 9+ immutable collections |
