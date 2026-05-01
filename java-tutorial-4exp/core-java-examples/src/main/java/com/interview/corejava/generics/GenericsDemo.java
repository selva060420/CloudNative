package com.interview.corejava.generics;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstrates Generics: type safety, bounded types, wildcards, type erasure.
 */
public class GenericsDemo {

    // Generic class — type-safe container
    static class ApiResponse<T> {
        private final T data;
        private final int statusCode;

        ApiResponse(T data, int statusCode) {
            this.data = data;
            this.statusCode = statusCode;
        }

        T getData() { return data; }
        int getStatusCode() { return statusCode; }
    }

    // Bounded type parameter — T must be Comparable
    static <T extends Comparable<T>> T findMax(List<T> items) {
        if (items.isEmpty()) throw new IllegalArgumentException("Empty list");
        T max = items.get(0);
        for (T item : items) {
            if (item.compareTo(max) > 0) max = item;
        }
        return max;
    }

    // Wildcard — upper bounded (read-only)
    static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number n : numbers) {
            total += n.doubleValue();
        }
        return total;
    }

    // Wildcard — lower bounded (write)
    static void addIntegers(List<? super Integer> list) {
        list.add(1);
        list.add(2);
        list.add(3);
    }

    public static void main(String[] args) {
        System.out.println("=== Generic Class (ApiResponse) ===");
        ApiResponse<String> strResp = new ApiResponse<>("Success", 200);
        ApiResponse<List<Integer>> listResp = new ApiResponse<>(List.of(1, 2, 3), 200);
        System.out.println("String response: " + strResp.getData());
        System.out.println("List response: " + listResp.getData());

        System.out.println("\n=== Bounded Type Parameter ===");
        System.out.println("Max of [3,1,4,1,5]: " + findMax(Arrays.asList(3, 1, 4, 1, 5)));
        System.out.println("Max of [a,z,m]: " + findMax(Arrays.asList("a", "z", "m")));

        System.out.println("\n=== Wildcards ===");
        List<Integer> ints = Arrays.asList(1, 2, 3);
        List<Double> doubles = Arrays.asList(1.5, 2.5);
        System.out.println("Sum of ints: " + sum(ints));
        System.out.println("Sum of doubles: " + sum(doubles));

        System.out.println("\n=== Type Erasure Proof ===");
        List<String> strings = Arrays.asList("a", "b");
        List<Integer> integers = Arrays.asList(1, 2);
        // At runtime, both are just "List" — generic type is erased
        System.out.println("Same class? " + strings.getClass().equals(integers.getClass())); // true
        System.out.println("Class name: " + strings.getClass().getName()); // java.util.Arrays$ArrayList
    }
}
