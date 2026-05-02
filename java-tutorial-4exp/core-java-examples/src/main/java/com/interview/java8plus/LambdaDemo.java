package com.interview.java8plus;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;

/**
 * Demonstrates: Lambda expressions, functional interfaces, method references.
 */
public class LambdaDemo {

    public static void main(String[] args) {
        // --- 4 Core Functional Interfaces ---

        // Predicate: T -> boolean
        Predicate<String> isLong = s -> s.length() > 5;
        System.out.println("isLong('Hello'): " + isLong.test("Hello"));
        System.out.println("isLong('HelloWorld'): " + isLong.test("HelloWorld"));

        // Function: T -> R
        Function<String, Integer> toLength = String::length; // method reference (unbound)
        System.out.println("length('Java'): " + toLength.apply("Java"));

        // Consumer: T -> void
        Consumer<String> printer = System.out::println; // method reference (bound)
        printer.accept("Consumer prints this");

        // Supplier: () -> T
        Supplier<List<String>> listFactory = List::of; // constructor-like
        System.out.println("Supplier: " + listFactory.get());

        // --- Method Reference Types ---
        List<String> names = Arrays.asList("alice", "bob", "charlie");

        // Static method reference
        names.stream().map(LambdaDemo::capitalize).forEach(System.out::println);

        // Unbound instance method reference
        names.stream().map(String::toUpperCase).forEach(System.out::println);

        // --- Composing functions ---
        Function<Integer, Integer> doubleIt = x -> x * 2;
        Function<Integer, Integer> addTen = x -> x + 10;

        // andThen: doubleIt first, then addTen
        System.out.println("compose 5: " + doubleIt.andThen(addTen).apply(5)); // (5*2)+10 = 20

        // Predicate composition
        Predicate<String> startsWithA = s -> s.startsWith("a");
        Predicate<String> longAndStartsWithA = isLong.and(startsWithA);
        System.out.println("'abcdef' long+startsA: " + longAndStartsWithA.test("abcdef"));
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
