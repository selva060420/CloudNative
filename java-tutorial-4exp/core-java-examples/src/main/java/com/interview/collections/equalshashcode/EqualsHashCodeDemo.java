package com.interview.collections.equalshashcode;

import java.util.*;

/**
 * Demonstrates equals/hashCode contract and what breaks when violated.
 * Interview focus: Why both must be overridden, HashMap behavior with broken contract.
 */
public class EqualsHashCodeDemo {

    // BAD — only equals overridden, hashCode uses default (memory address)
    static class BrokenKey {
        String id;

        BrokenKey(String id) { this.id = id; }

        @Override
        public boolean equals(Object o) {
            return o instanceof BrokenKey && ((BrokenKey) o).id.equals(this.id);
        }
        // hashCode NOT overridden!
    }

    // GOOD — both equals and hashCode overridden consistently
    static class CorrectKey {
        String id;

        CorrectKey(String id) { this.id = id; }

        @Override
        public boolean equals(Object o) {
            return o instanceof CorrectKey && ((CorrectKey) o).id.equals(this.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    public static void main(String[] args) {
        // 1. Broken contract — HashMap fails
        System.out.println("=== BROKEN: only equals() overridden ===");
        Map<BrokenKey, String> brokenMap = new HashMap<>();
        BrokenKey k1 = new BrokenKey("101");
        BrokenKey k2 = new BrokenKey("101");
        System.out.println("k1.equals(k2): " + k1.equals(k2));           // true
        System.out.println("k1.hashCode == k2.hashCode: " + (k1.hashCode() == k2.hashCode())); // false!
        brokenMap.put(k1, "Alice");
        System.out.println("get with equal key: " + brokenMap.get(k2));   // null — WRONG!

        // 2. Correct contract — HashMap works
        System.out.println("\n=== CORRECT: both equals() and hashCode() ===");
        Map<CorrectKey, String> correctMap = new HashMap<>();
        CorrectKey c1 = new CorrectKey("101");
        CorrectKey c2 = new CorrectKey("101");
        System.out.println("c1.equals(c2): " + c1.equals(c2));           // true
        System.out.println("c1.hashCode == c2.hashCode: " + (c1.hashCode() == c2.hashCode())); // true
        correctMap.put(c1, "Alice");
        System.out.println("get with equal key: " + correctMap.get(c2));  // Alice — CORRECT!

        // 3. HashSet behavior
        System.out.println("\n=== HashSet with broken contract ===");
        Set<BrokenKey> brokenSet = new HashSet<>();
        brokenSet.add(new BrokenKey("A"));
        brokenSet.add(new BrokenKey("A"));
        System.out.println("Set size (expected 1): " + brokenSet.size()); // 2 — WRONG!

        Set<CorrectKey> correctSet = new HashSet<>();
        correctSet.add(new CorrectKey("A"));
        correctSet.add(new CorrectKey("A"));
        System.out.println("Set size (expected 1): " + correctSet.size()); // 1 — CORRECT!

        // 4. Mutable key danger
        System.out.println("\n=== Mutable key danger ===");
        CorrectKey mutableKey = new CorrectKey("original");
        Map<CorrectKey, String> mutableMap = new HashMap<>();
        mutableMap.put(mutableKey, "value");
        System.out.println("Before mutation: " + mutableMap.get(mutableKey)); // value
        mutableKey.id = "changed"; // mutate the key!
        System.out.println("After mutation: " + mutableMap.get(mutableKey));  // null — LOST!
    }
}
