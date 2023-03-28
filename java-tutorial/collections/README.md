# Collections concepts in Java

This folder contains information related to Collections concepts in Java.

## Table of Contents
- [Concepts](#concepts)
    - [Collections](#collections)
    - [Iterator vs forEach](#iterator-vs-foreach)
    - [Hashing](#hashing)
    - [Hash Table](#hash-table)
    - [Concurrent Collections](#concurrent-collections)
    - [Why heap is used in Priority Queue](#why-heap-is-used-in-priority-queue)
- [References](#references)

## Concepts:
### Collections

Collection frameworks consist of Collection interface (List, Set, Queue), sub interfaces, Map, Iterator and various classes that provides day-to-day data structures.

### Iterator vs forEach

**Iterator:** provides a **direct reference** to the current element being iterated, **allowing modification** of the collection during the iteration. 

**forEach:** loop provides only a **read-only view** of the collection and **does not allow modification** during the iteration.

#### forEach

Used to iterate the elements. It is defined in Iterable and Stream interface. We can pass lambda expression as an argument

```
List<String> list = new ArrayList<String>();  
list.add("sample");   
list.forEach(i -> System.out.println(i));  
```

#### Iterators

**Types of Iterators:**

  * `Fail-Safe` (handles failure, used in concurrent collections)
  * `Fail-Fast` (doesn't handles failure and throws exception, used in synchronized collections)
  
**Fail fast iterator**: aborts the operation as soon it exposes failures and stops the entire operation. Example: HashMap (throws ConcurrentModificationException)

**Fail Safe iterator**: doesn't abort the operation in case of a failure. Instead, it tries to avoid failures as much as possible. Example: ConcurrentHashMap

### Hashing:

Collision strategy in Hashing:

- `Chaining` using **linked list**
- `Open addressing` using **(linear and quadratic probing, double hashing)**

### Hash Table

Hash Table (implements Map and is **synchronized**) is a data structure which organizes data using hash functions in order to support **quick insertion and search**.

**Principle of Hash Table:**

  * Uses a **hash function** to map keys to buckets
  
  * When we **insert** a new key, the hash function will decide which bucket the key should be assigned and  the key will be stored in the corresponding bucket providing **O(1)** for average case.
  
  * When we want to **search** for a key, the hash table will use the same hash function to find the corresponding bucket and search only in the specific bucket providing **O(1)** for average case.

### Concurrent Collections

Collections using new approaches to synchronization are available in **Java 5**. 

**Concurrent collections** which performs better than using **synchronized** keyword.

**Types of concurrent approaches:**
  * `Copy on Write`
  * `Compare and Swap` (Also used in Atomic Operations)
  * `Locks` (Reentrantreadwritelock is implementation of lock interface (lock and unlock))

**Copy on Write:** It is used whenever the **read operations will be huge** and the **write operation will be ocassionly** happened. Write operations will be executed on the **copy from main memory**, and read operations will be executed on the **original data in memory**. Read operations won't be blocked at any time, whereas write operation will be blocked for a while.

**Compare and Swap:** Also used in Atomic Operations, allows a thread to check if a **value in memory matches an expected value** and **update it to a new one atomically**, preventing synchronization problems and race conditions. CAS is commonly used in **lock-free** data structures to enable multiple threads to access and modify data simultaneously without causing conflicts or synchronization overhead.

**Locks:** Java 5 introduced Lock interface and its subinterface ReadWriteLock. Its implementation Reentrantlock and Reentrantreadwritelock provides lock and unlock methods.

### Why heap is used in Priority Queue

**Heap** is used in a priority queue because it efficiently maintains the priority queue's requirements, such as easily finding and removing the highest priority element in **O(log n)** time complexity. 

A **regular queue** does not have any inherent priority notion, making it less efficient for priority-based operations in **O(n)** time complexity.

## References:

  * https://www.programiz.com/java-programming/collections
  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_4_Java_Collections
  
