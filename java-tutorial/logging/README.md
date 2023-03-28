# Exception handling and logging concepts in Java

This folder contains information related to Exception handling and logging concepts in Java.

## Table of Contents
- [Concepts](#concepts)
    - [Exception](#exception)
    - [Error](#error)
    - [Try with Resource](#try-with-resource)
    - [Log Levels](#log-levels)
- [References](#references) 

## Concepts:
### Exception

An exception that extend throwable is an event that occurs during execution of the program that disrupts normal flow of instructions

**Types of Exception:**

  * `Checked Exception` (Compile time - IO Exception, Class Not Found Exception, SQL Exception)
  * `Unchecked Exception` (Run time - arithmetic, null pointer, array out of bound)

### Error

Error refers to illegal operation performed by user that results in abnormal working of program. 

Example: Assertion error(Expected output not equal to Actual output),IO error, Stack Overflow, out of memory.

### Log Levels

**Types of levels:**

  * `Debug`
  * `Error`
  * `Info`
  * `Warn`
  * `Trace`    

### Try with Resource

This block ensures that each resource specified in the try block is closed at end of the statement execution thereby avoiding resource leak. This is also called as **automatic resource management**

## References:

  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_9_Java_Exception_Handling
