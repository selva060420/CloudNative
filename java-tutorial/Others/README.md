# Important interview questions in Java

This folder contains information related to important concepts in Java.

## Table of Contents
- [Questions](#questions)
- [References](#references) 

## Questions:

### 1.) What methods and class and approaches are there to efficiently handle memory in java 

**Approaches to efficiently handle memory:**

  * `Avoid creating unnecessary objects`
  * `Use primitives instead of objects where possible`
  * `Use object pooling to reuse objects`
  * `Use the finalize()/shutdownHook to clean up resources`
  * `Use the try-with-resources statement to automatically close resources`

### 2.) Memory management in java: heap vs stack

Heap: **dynamic memory allocation** of objects and is shared among all threads and stores objects.

Stack: consists of **stack frames and local variables** which is used for method calls and is specific to each thread. Used in stacktrace and while recursion.

**Note**: Refer JVM Model for more information.

### 3.) What is serialization in java

It provides a **standardized way** of converting an `object` into a `stream of bytes` so that it can be stored in a file, sent over a network, or saved in a database. It handles the details of formatting and encoding the byte array.

The reverse process of creating an object from a stream of bytes is called `deserialization`.

### 4.) What is Class loading in java

It is the process of **loading a class into memory** when it is first used in a program. It involves:

  * `Finding the class file`
  * `Verifying its correctness`
  * `Allocating memory for the class`

**Note**: Compile time: High level .java to .class files, Run time: Loads .class files, links packages and executes

### 5.) What is object monitor in java 

It is a **low-level construct** located within the JVM's memory management system which ensures that only one thread can execute synchronized blocks of code or methods at any given time during runtime.

### 6.) What is lambda function in java

A lambda function in Java is a **short, anonymous function** that can be used as a parameter to other methods or functions. It is also known as a lambda expression or a closure.

### 7.) What is a shutdown hook in java? 

A shutdown hook is a piece of code that is executed when the **JVM is about to shut down**. 

It is typically used to perform **cleanup tasks** such as closing files or releasing resources.

```
Runtime.getRuntime().addShutdownHook(new Thread()
{
    @Override
    public void run()
    {
        System.out.println("Cleaning up resources before JVM shuts down");
    }
});
```

### 8.) What is JIT compiler?

The Just-In-Time (JIT) compiler is a component of the runtime environment that improves the performance of Java applications by **compiling bytecodes to native machine code** at run time.

### 9.) what is quick sort vs merge sort in java

Quick sort is faster than merge sort for **small datasets**. Java provides Quick sort as its implemention

Merge sort is more efficient for **larger datasets**.

### 10.) Use cases of stack and queue? Which is used in which scenarios?

**Stacks:** (Most recently added)

  * `Tracking program state:` Used to keep track of the state of a program, particularly in recursive algorithms
  * `Expression evaluation:` Used to evaluate arithmetic expressions, by converting them to postfix notation and then evaluating them. 
  * `Undo/redo functionality:` Used to implement undo/redo functionality in applications, by storing the state of the application after each action.

**Queues:** (Least recently added)

  * `Task management:` Used to manage tasks or events that need to be processed in the order they were received, such as handling user requests in a web application.
  * `Resource pooling:` Used to manage a pool of resources, such as database connections, network socket or thread pools.
  * `Print queue:` Used to manage a print queue, by adding print jobs to the queue as they are received and printing them in the order they were received.

### 11.) What is Heap and its properties?

Heap is a **complete binary tree**. It has several methods such as **heapify**, extract and peek.

**Heap Property:**

  * `max-heap:` value of each parent node is **greater** than or equal to its child nodes
  * `min-heap:` value of each parent node is **lesser** than or equal to its child nodes

### 12.) What is CAP Theorem in database?

The CAP theorem states that a distributed system **cannot simultaneously** provide all three guarantees of CAP and **must trade off** one of the guarantees to achieve the other two.

 * `Consistency:` Every read operation in the system will return the most recent write for a given client.
 * `Availability:` Every request to the system will receive a response, without guarantee that it contains the most recent version of the information.
 * `Partition tolerance:`The system continues to function even when network partitions occur, causing communication delays or failures between nodes.

### 13.) Types of concurrent approaches in java?

**Some concurrent approaches in Java are:**

  * `Synchronization using locks and monitors`
  * `Atomicity by using Atomic variables`
  * `Concurrent collections`
  * `Volatile variables` 

### 14.) Difference between Comparator and Comparable

Comparator/Comparable are interfaces used for sorting objects.

**Differences:**

  * `Comparable:` defines a **natural** ordering between object
  * `Comparator:` defines a **custom** comparison between object

### 15.) What is the purpose of the finalize() method in java? 

The purpose of the finalize() method in Java is to **clean up any resources** associated with an object before it is garbage collected.

It is not recommended to rely on this method to release resources, as it is not guaranteed to be called.

### 16.) Context switching in java. 

Context switching refers to the process of switching between different threads or processes to allow them to execute concurrently. 

It involves **saving the current state** of a thread or process and restoring the state of another thread or process.

**Note:** Example: From Running state of Thread to Timed_Waiting

### 17.) What is mean by CyclicBarrier in java 

CyclicBarrier is a synchronization aid that allows a set of threads to wait for each other to reach a common barrier point before proceeding. 

It is useful for parallel computations that require all threads to complete a certain step before proceeding to the next step.

### 18.) What is Thread Scheduler and Time Slicing in java? 

The thread scheduler is responsible for **scheduling** threads to run on the CPU. 

Time slicing is a technique used by the scheduler to **allocate CPU time** to each thread in a round-robin fashion.

### 19.) Scenaries where object level lock is used in java??

Object level locks are used in Java when multiple threads need to access the same object's synchronized methods (**non-static or instance methods**) or blocks.

### 20.) Scenaries where class level lock is used in java??

Class level locks are used in Java when multiple threads need to access **static** synchronized methods or blocks in a class.

### 21.) Scenaries where shared resource level lock is used in java??

Shared resource level locks are used in Java when multiple threads need to access a shared resource, such as a file or a database, and modifications to the resource need to be synchronized.

### 22.) What is busy spinning in java? 

Busy spinning refers to a situation where a thread repeatedly checks for a condition using a loop, rather than waiting for the condition to become true. 

This can be inefficient and can waste CPU cycles.

### 23.) What do you mean by the ThreadLocal variable in Java?

A ThreadLocal variable is a type of variable that is local to a specific thread. 

Each thread that accesses the variable gets its own copy of the variable, so changes made to the variable by one thread do not affect the value of the variable in other threads.

### 24.) Explain Thread Group. Why should we not use it in java?

A thread group in Java is a way of organizing threads into a hierarchical structure. 

It can be used to control the behavior of a group of threads, such as setting their priority or interrupting them. However, thread groups are not recommended for most use cases because they can be difficult to manage and can cause performance issues.

### 25.) Is it possible that each thread can have its stack in multithreaded programming in java?

Yes, it is possible for each thread to have its own stack in multithreaded programming in Java. 

The stack is **specific to each thread** and is used for storing method calls and local variables. 

This allows each thread to have its own execution context and to operate independently of other threads.

## References:

  * https://java2blog.com/serialization-in-java/
  * http://www.javainterview.in/p/collections-interview-questions.html?m=1#question203
  * https://www.interviewbit.com/multithreading-interview-questions/
  * https://javarevisited.blogspot.com/2018/07/java-multi-threading-interview-questions-answers-from-investment-banks.html#axzz7wmutVuH3
  * https://www.geeksforgeeks.org/top-20-java-multithreading-interview-questions-answers/
  * https://data-flair.training/blogs/tricky-java-interview-questions/
  * https://www.knowledgehut.com/interview-questions/java-collection
  * https://www.edureka.co/blog/interview-questions/java-collections-interview-questions/
