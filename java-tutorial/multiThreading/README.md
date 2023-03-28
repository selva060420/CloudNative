# Multithreading concepts in Java

This folder contains information related to Multithreading concepts in Java.


Multithreading guide
=================

## Table of Contents
- [Definition](#definition)
- [Concepts](#concepts)
    - [Process](#process)
    - [Thread](#thread)
    - [Thread Implementation](#thread-implementation)
    - [Synchronization](#synchronization)
    - [Inter-thread Communication](#inter-thread-communication)
    - [Using Synchronized keyword](#using-synchronized-keyword)
    - [Atomic Operations](#atomic-operations)
    - [Executor Framework](#executor-framework)
    - [Semaphore](#semaphore)
    - [Exchanger](#exchanger)
    - [Countdown Latch](#countdown-latch)
    - [Blocking Queue](#blocking-queue)
    - [Thread Dump](#thread-dump)
- [References](#references) 

## Definition:

Ability of program to execute multiple threads concurrently, allowing for more efficient use of system resources and faster execution of certain tasks. 

Threads can be created by Extending **Thread class** or else by implementing **Runnable interface**

## Concepts:

### Process

In computing, a process is the **instance of a computer program that is being executed** by one or many threads.

### Thread

Represents the thread in Execution. It is considered as a single sequential flow of control within a program. Threads can be run concurrently to achieve efficiency. 
 
**Types of Thread:**
  * `User Thread`
  * `Daemon Thread`

#### User Thread

Created by application code, this will run even after main application thread terminates. Used for running background tasks (critical tasks) that doesn’t require main application to run. 

#### Daemon Thread

Run in background which provide service for the other threads in application. It will automatically **terminate** when all non-Daemon threads terminated. Used for running background tasks(non-critical) and can be terminated if necessary. Eg: Garbage Collector.

### Thread Implementation: 

**Types of implementing Thread:**
  * `Extend Thread class`
  * `Implement Runnable interface`
  
#### Thread class

Used to create and manage threads. By extending thread, we can create multiple subclass which can be run concurrently for time consuming process. provides sleep, join, interrupt, start, join, wait, notify, notifyAll etc.

#### Runnable interface

Task executed asynchronously. Only contain void run method. Used to run background tasks which doesn’t require return value or interact with user interface. 

#### Callable interface

Callable Task executed asynchronously. It has a single method call which can return result and throws an expression. Once thread started call method will return a future object represents the result of callable.get() ---> callable result.

**Note:** Both runnable and callable are functional interfaces (contains only one method)

### Synchronization

**Key concepts:**
  * `Race Condition`
  * `Critical Section`
  * `Deadlock`
  * `mutual exclusion`

**Race Condition:** It occurs in java When two or more threads access a shared resource concurrently. Here behaviour of program is unpredictable, and result depends on relative time of their execution.

**Critical Section:** Shared resource that is concurrently accessed by multiple threads.

**Deadlock**: It occurs in java, when two or more threads are blocked waiting for each other to release a resource that they need to continue executing. This leads to situation in which all threads are unable to make progress, results in deadlock. This mostly occurs due to timeouts in lock and nested locks 

**Mutual exclusion (Mutex)**: It is a property of **process synchronization** which states that `no two processes can exist in the critical section at any given point of time` to prevent race conditions and ensure consistency.

### Inter-thread Communication

Inter-thread communication or Co-operation is all about **allowing synchronized threads** to communicate with each other.

Cooperation (Inter-thread communication) is a mechanism in which a **thread is paused running** in its critical section and **another thread is allowed to enter** (or lock) in the same critical section to be executed.

**Implemented by following:**
  * `wait()`
  * `notify()`
  * `notifyAll()`
  * `Semaphore`
  * `Blocking Queue`
  * `Countdown Latch`

### Using Synchronized keyword

It is a keyword used to control access to shared resource, such as variables, methods, block of operations by multiple threads in a multi-threaded application.
 
**Types of Synchronization:**
  * `Synchronized Methods`
  * `Synchronized block`

#### Synchronized Methods:

It means only one thread can access this method at a time until lock released (when method returns or throws exception) 

#### Synchronized block: 

Here section of code is surrounded by Synchronized keyword and this block of code acquire lock on shared object. Other thread try to access this shared resource will blocked until the lock is released 

**Types of Synchronized block :**
  * `Object Level locking`
  * `Class Level Locking`

#### Object Level locking: 

It prevent multiple threads from accessing a block of code on the same object simultaneously. 

#### Class Level Locking: 

It prevent multiple threads from executing synchronized static methods of same class simultaneously 

####  How JVM knows if a method is synchronized?

When a method is declared as synchronized, the Java Virtual Machine (JVM) adds special instructions to the **compiled bytecode** of that method. 

Specifically, the JVM adds a **monitorenter** instruction at the beginning of the method and a **monitorexit** instruction at the end of the method. Thereby **object monitor** will monitor the changes.

### Atomic Operations

Operations that **always execute** together is known as the atomic operations or atomic action. 

All the atomic operations either **execute effectively happens all at once** or **it does not happen at all**. 

**Note**: Atomic variable are provided by Java such as AtomicInteger, AtomicBoolean etc

### Executor Framework

It provides a way a execution of tasks asynchronously in a thread Pool. It helps us to focus on program logic instead of creating tasks, submitting tasks to them, and managing the execution of those tasks

**Main components :**
  * `Executor interface` has execute (Runnable task) used only for execution 
  * `ExecutorService interface extends Executor` has additional methods such as submit, shutdown etc
  * `Executors class` It has static methods for creating Thread Pool and executing those tasks in those thread pool
  * `ScheduledExecutorService interface extends the ExecutorService` It is powerful tool for Scheduling and execution tasks at specified interval in java Applications

### Semaphore

It is Synchronization construct allows a fixed number of threads to access a shared resource at the time. 

**Methods in semaphore :**
  * `acquire` (if there is a place to access shared resource then semaphore gave permit or also acquire method will wait for a permit)
  * `release` (once thread accessed a shared resource and completes a task the this method helps to release place for some other threads)

### Exchanger

It is a synchronization construct that allows two threads to exchange objects in a blocking manner. Threads will be in lock until objects exchanged. Exchange occurs by **exchange()** method.

### Countdown Latch

It is a synchronization tool that allows one or more thread to wait until a set of operation being performed in other threads to complete.

**Methods in Countdown Latch :**
  * `latch.await()` (this will wait till specified number of countdown occurred latch)
  * `latch.countDown()` (value decreased from a specified value to zero. One decrement for when task completes)

### Blocking Queue

It allows multiple threads to communicate each other by adding or removing elements in a queue. We want to **set size** for the Blocking queue.

**Methods in Blocking Queue :**
  * `queue.put()` (this will allows out till max capacity reached. After additional put elements will be waited for take method to remove value in queue)
  * `queue.take()` (this will remove elements in a queue. If put() method processing then take() will wait and vice Versa)

### Thread Dump

It is a **snapshot of the state of all the threads** of a Java process. The state of each thread is presented with a **stack trace**, showing the content of a thread's stack.

**Steps to take thread dump of a process :**

  * `jps` - lists java processes running including process id(pid)
  * `jps <pid>` - list down the state/logs of threads with stackTrace of the process

## References:

  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_5_Java_Multithreading
  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_6_Java_Executor_Framework
  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_7_Java_Concurrency_util


