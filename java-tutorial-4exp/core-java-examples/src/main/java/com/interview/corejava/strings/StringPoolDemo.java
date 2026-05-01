package com.interview.corejava.strings;

/**
 * Demonstrates String Pool, intern(), immutability, and performance implications.
 */
public class StringPoolDemo {

    public static void main(String[] args) {
        System.out.println("=== String Pool Behavior ===");
        stringPoolDemo();

        System.out.println("\n=== Immutability Proof ===");
        immutabilityDemo();

        System.out.println("\n=== Performance: Concatenation vs StringBuilder ===");
        performanceDemo();
    }

    static void stringPoolDemo() {
        String s1 = "hello";                    // Pool
        String s2 = "hello";                    // Same pool reference
        String s3 = new String("hello");        // New heap object
        String s4 = s3.intern();                // Returns pool reference

        System.out.println("s1 == s2 (both pool): " + (s1 == s2));           // true
        System.out.println("s1 == s3 (pool vs heap): " + (s1 == s3));        // false
        System.out.println("s1 == s4 (pool vs intern): " + (s1 == s4));      // true
        System.out.println("s1.equals(s3) (content): " + s1.equals(s3));     // true

        // Integer cache comparison (related pitfall)
        Integer a = 127, b = 127;
        Integer c = 128, d = 128;
        System.out.println("\nInteger 127 == 127: " + (a == b));  // true (cached)
        System.out.println("Integer 128 == 128: " + (c == d));    // false (not cached)
    }

    static void immutabilityDemo() {
        String original = "Kubernetes";
        String modified = original.replace("Kubernetes", "Docker");

        System.out.println("Original unchanged: " + original);  // "Kubernetes"
        System.out.println("New string created: " + modified);   // "Docker"
        System.out.println("Same reference? " + (original == modified)); // false

        // Hashcode is cached — computed once
        int hash1 = original.hashCode();
        int hash2 = original.hashCode();
        System.out.println("Hashcode cached (same value): " + (hash1 == hash2)); // true
    }

    static void performanceDemo() {
        int iterations = 100_000;

        // BAD: String concatenation
        long start = System.nanoTime();
        String result = "";
        for (int i = 0; i < iterations; i++) {
            result += "x"; // Creates new String each time
        }
        long concatTime = (System.nanoTime() - start) / 1_000_000;

        // GOOD: StringBuilder
        start = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append("x");
        }
        long builderTime = (System.nanoTime() - start) / 1_000_000;

        System.out.println("Concatenation: " + concatTime + "ms");
        System.out.println("StringBuilder: " + builderTime + "ms");
        System.out.println("StringBuilder is ~" + (concatTime / Math.max(builderTime, 1)) + "x faster");
    }
}
