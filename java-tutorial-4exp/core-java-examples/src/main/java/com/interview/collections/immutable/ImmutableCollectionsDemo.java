package com.interview.collections.immutable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Demonstrates immutable collections in Java 9+ and common pitfalls.
 * Interview focus: List.of vs unmodifiableList vs copyOf, null handling.
 */
public class ImmutableCollectionsDemo {

    public static void main(String[] args) {
        // 1. Java 9 — List.of(), Set.of(), Map.of()
        System.out.println("=== Java 9: List.of / Set.of / Map.of ===");
        List<String> immutableList = List.of("a", "b", "c");
        Set<String> immutableSet = Set.of("x", "y", "z");
        Map<String, Integer> immutableMap = Map.of("one", 1, "two", 2);
        System.out.println("List: " + immutableList);
        System.out.println("Set: " + immutableSet);
        System.out.println("Map: " + immutableMap);

        try {
            immutableList.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("List.of → add() throws UnsupportedOperationException");
        }

        // 2. Collections.unmodifiableList — view, NOT a copy
        System.out.println("\n=== unmodifiableList (view, not copy) ===");
        List<String> mutable = new ArrayList<>(List.of("a", "b"));
        List<String> unmodifiable = Collections.unmodifiableList(mutable);
        mutable.add("c"); // modifies the backing list!
        System.out.println("unmodifiable sees change: " + unmodifiable); // [a, b, c]

        // 3. Java 10 — List.copyOf (true independent copy)
        System.out.println("\n=== List.copyOf (true copy) ===");
        List<String> source = new ArrayList<>(List.of("x", "y"));
        List<String> copy = List.copyOf(source);
        source.add("z");
        System.out.println("source: " + source);  // [x, y, z]
        System.out.println("copy: " + copy);       // [x, y] — independent

        // 4. Null handling differences
        System.out.println("\n=== Null handling ===");
        try {
            List.of("a", null);
        } catch (NullPointerException e) {
            System.out.println("List.of → null throws NullPointerException");
        }

        List<String> withNull = new ArrayList<>(Arrays.asList("a", null));
        List<String> unmodWithNull = Collections.unmodifiableList(withNull);
        System.out.println("unmodifiableList allows null: " + unmodWithNull);

        try {
            List.copyOf(withNull);
        } catch (NullPointerException e) {
            System.out.println("List.copyOf → null throws NullPointerException");
        }

        // 5. Map.ofEntries for more than 10 entries
        System.out.println("\n=== Map.ofEntries ===");
        Map<String, Integer> largeMap = Map.ofEntries(
                Map.entry("a", 1), Map.entry("b", 2),
                Map.entry("c", 3), Map.entry("d", 4)
        );
        System.out.println("Map.ofEntries: " + largeMap);

        // 6. Collectors.toUnmodifiableList (Java 10)
        System.out.println("\n=== Stream to immutable collection ===");
        List<String> fromStream = List.of("c", "a", "b").stream()
                .sorted()
                .collect(Collectors.toUnmodifiableList());
        System.out.println("Sorted immutable from stream: " + fromStream);
        try {
            fromStream.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("toUnmodifiableList → add() throws UnsupportedOperationException");
        }
    }
}
