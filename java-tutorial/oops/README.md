# Object-Oriented Programming concepts in Java

This folder contains information related to OOPS concepts in Java.


OOPS guide
=================

## Table of Contents
- [Definition](#definition)
- [Concepts](#concepts)
    - [Object](#object)
    - [Class](#class)
    - [Inheritance](#inheritance)
    - [Encapsulation](#encapsulation)
    - [Polymorphism](#polymorphism)
    - [Abstraction](#abstraction)
    - [Association](#association)
    - [Cohesion](#cohesion)
    - [Coupling](#coupling)
    - [SOLID Principles](#solid-principles)
- [References](#references)

## Definition:

OOPS is a methodology to design a program using class and objects. Object is a real-life entity and an instance of a class. It helps in software development and maintenance.

## Concepts:

### Object

It is real life entity of a class. It is a **instance** of a class. It encapsulates data/behaviour and its behaviour is defined by methods/function in the classes. Example: person, animal, book. 

### Class

It is a **blueprint** or template for creating objects. It defines a set of attributes that defines the state and behaviour of the object.

### Inheritance

It is the mechanism that allows one class (child or subclass) to inherit the properties of another class(parent or superclass). Hence it helps in reducing the redundant code and improving code reusability. It exhibits **IS-A** relationship

**Types of inheritance:**
  * `Single`
  * `multiple` (only achieved by **interface**)
  * `multilevel`
  * `hybrid`

### Encapsulation

It is a process of wrapping or binding code and data together into a single unit. It helps to achieve data hiding. Example: Declare class variable as **private** and providing getter setter as **public**. 

### Polymorphism

If one task is performed in different ways, it is called Polymorphism. 

```
  * Method Overloading (Compile-time Polymorphism) 
  * Method Overriding (run time-Polymorphism) 
```

### Abstraction
Hiding internal details and only showing functionalities.

```
  * 0% to 100% abstraction (achieved by **abstract class**)
  * 100% abstraction (achieved by **interface**)
```

**FUNCTIONAL INTERFACE:**

It is interface that will have **only one abstract method**. To represent single unit of behaviour. 

### Association

It is relationship between two or more classes. It exhibits **HAS-A** relationship

```
- one to one
- one to many
- many to one
- many to many
```

**Types of Association:**
  * `Aggregation`
  * `Composition`

#### Aggregation

It represent **weak** relationship between two or more classes. (i.e.) One can exist independently of the other class. 

#### Composition

It represent **strong** relationship between two or more classes. (i.e.) One can't exist independently of the other class. 

### Cohesion

It refers to the **measure** of how strong the members (variables, methods, inner class) of the class related to each other.

```
  * High cohesion
  * Low cohesion
```

### Coupling

It refers to the **degree** to which one class or module depends on other.

```
  * Tight Coupling
  * Loose Coupling
```

### SOLID Principles:

**Types of principles:**

  * `Single Responsibility`
  * `Open/Closed`
  * `Liskov substitution`
  * `Interface Segregation`
  * `Dependency Inversion`

#### Single Responsibility

There should be only **one reason to change** a class. Each Class has a single responsibility. 

#### Open/Closed

Software entities (methods, module/ package, class) should be **opened for extension** and **closed for modification**. 

#### Liskov substitution

It is variation of open Closed principle. objects can be **replaced by objects of subclasses** without compromising functionalities (without breaking the normal flow)

#### Interface Segregation

Clients should not be forced to **implement** that they will not use which leads to unnecessary complexity and maintenance. 

#### Dependency Inversion

High level module should **not depend** on the low-level module. High level module should depend on **abstraction**. Abstraction should not depend on details. Details should depend on abstraction.

## References:

  * https://java2blog.com/core-java-tutorial-for-beginners-experienced/#Chapter_3_Object-Oriented_Programming_concepts
  * https://codegym.cc/groups/posts/232-solid-five-basic-principles-of-class-design-in-java
  * https://www.baeldung.com/solid-principles
