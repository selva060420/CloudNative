# Basics concepts in Java

This folder contains information related to Basics concepts in Java.

## Table of Contents
- [Concepts](#concepts)
    - [Anonymous Class](#anonymous-class)
    - [Singleton](#singleton)
    - [Enum](#enum)
    - [Pass-by-Value](#pass-by-value)
    - [Pass by Reference](#pass-by-reference)
    - [Static](#static)
    - [ACID properties](#acid-properties)
    - [Why String is Immutable or Final in Java](#why-string-is-immutable-or-final-in-java)
    - [Java 7 Features](#java-7-features)
    - [Java 8 Features](#java-8-features)
    - [Streams API](#streams-api)
    - [JVM Model](#jvm-model)
- [References](#references)

## Concepts:
### Anonymous Class

It is a inner nested class that doesn’t have a name

### Singleton

It is a class that has only one instance and provides a global point of access to it

**Types of initialization:**

  * `Early initialization`
  * `Lazy initialization`

#### Early initialization

Creation of instance at load time. Created inside a class, which in turn returned when getInstance method called.

#### Lazy initialization

Creation of instance when required. Created when we call get instance method (creation of object in getInstance() method). 

### Enum

It indicates the list of items. Item’s should always be declared in capital letter

### Pass by Value

While passing value through the methods, **copy** of the object is passed rather than the object's reference. In java all object references are passed by value.

**Note**: In Java, all values passed are **passed by value** and not reference

### Pass by Reference

While setting a value for a variable with setter, Object’s **reference** will also be altered.

### Static

**Types of using Static keyword:**

  * `variable`
  * `method`
  * `block`
  * `class`

#### Static variable:

```
Global variable, common variable even if we create multiple objects. Called using Class reference 
```

#### Static method:

```
Called using Class reference
```

#### Static block:

```
This is a block of code that is executed when a class is first loaded into a JVM. 
```

#### Static class:

```
Static class is an inner static class which is called using Class reference.
```

### ACID properties

In order to maintain consistency in a database, before and after the transaction, certain properties are followed. These are called **ACID properties**

**Four properties:**

  * `Atomicity` (either transaction is successfull or failed)
  * `Consistency` (data integrity)
  * `Isolation` (multiple transactions with concurrency and consistency)
  * `Durability` (data persistence incase of failure)

### Why String is Immutable or Final in Java?

The immutable string cannot be modified once it is created. But we can **only change the reference** to the object. 

**Reason behind Immutability:**
  * `Security`
  * `synchronization and concurrency`
  * `caching`
  
**Note**: The reason of making String class **final** is to not allow others to extend it and **break** the functionality.

### Java 7 Features

The important features of JavaSE 7 are listed below

```
String in switch statement (Java 7)
The try-with-resources (Java 7)
Caching Multiple Exceptions by single catch (Java 7)
```

### Java 8 Features

The important features of JavaSE 8 are listed below:

```
Java 8 Date/Time API (Java 8)
Lambda Expressions (Java 8)
Functional Interfaces (Java 8)
Stream (Java 8)
Default Methods (Java 8)
forEach method(Java 8)
Collectors(Java 8)
Java Concurrency Improvement (Java 8)
```

### Streams API

Introduced in Java 8, the Stream API is used to **process collections of objects**. 

A stream is a sequence of objects supporting **sequential and parallel aggregate** operations and provides various methods which can be **pipelined** to produce the desired result.

### JVM Model

Java Virtual Machine, a **platform-independent model** refers to the abstract machine that provides the runtime environment for **executing Java bytecode**.

**JVM is responsible for:**

  * `interpreting or compiling Java bytecode into machine code`
  * `managing memory allocation`
  * `garbage collection`
  * `enforcing security policies`

**Main components of JVM:**

  * `Class Loader Subsystem` - includes class loader, linker which is responsible for loading Java class files into the JVM's memory
  * `Runtime Data Area` - includes the Method Area, Heap, Stack, and PC Registers, which are used to store data and execute Java bytecode
  * `Execution Engine` - includes JIT compiler, interpreter, GC which executes Java bytecode by interpreting or compiling it into machine code.

## References:

  * https://www.javatpoint.com/why-string-is-immutable-or-final-in-java#:~:text=The%20String%20is%20immutable%20in,it%20makes%20the%20String%20immutable.
  * https://www.javatpoint.com/java-atomic
  * https://www.baeldung.com/java-atomic-variables#:~:text=The%20most%20commonly%20used%20atomic,which%20can%20be%20atomically%20updated.
  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_1_Java_Basics
  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_10_Java_8
  * https://www.guru99.com/java-virtual-machine-jvm.html
  
