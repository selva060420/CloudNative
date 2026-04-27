package com.interview.collections.hashmap;

import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates HashMap internals — hashing, collision, treeification, resizing.
 * Interview focus: How put/get works, why capacity is power of 2, load factor.
 */
public class HashMapInternalsDemo {

    public static void main(String[] args) {
        // 1. Basic put/get — hashing flow
        Map<String, String> map = new HashMap<>(16, 0.75f);
        map.put("name", "Selva");
        map.put("role", "Engineer");

        System.out.println("=== Basic Operations ===");
        System.out.println("get('name'): " + map.get("name"));
        System.out.println("size: " + map.size());

        // 2. Hash calculation (same as internal)
        String key = "name";
        int hash = key.hashCode() ^ (key.hashCode() >>> 16);
        int bucket = hash & (16 - 1);
        System.out.println("\n=== Hash Calculation ===");
        System.out.println("key: " + key);
        System.out.println("hashCode(): " + key.hashCode());
        System.out.println("spread hash: " + hash);
        System.out.println("bucket index (capacity=16): " + bucket);

        // 3. Collision demo — keys landing in same bucket
        System.out.println("\n=== Collision Demo ===");
        Map<Integer, String> collisionMap = new HashMap<>(16);
        // Multiples of 16 all land in bucket 0: hash & (16-1) == 0
        for (int i = 0; i < 10; i++) {
            collisionMap.put(i * 16, "value-" + i);
        }
        System.out.println("Keys 0,16,32...144 all land in bucket 0 (hash & 15 == 0)");
        System.out.println("Bucket converts to Red-Black tree at 8+ entries");
        System.out.println("size: " + collisionMap.size());
        System.out.println("get(80): " + collisionMap.get(80));

        // 4. Resizing demo
        System.out.println("\n=== Resizing Demo ===");
        Map<Integer, Integer> resizeMap = new HashMap<>(4, 0.75f);
        System.out.println("Initial capacity: 4, threshold: 3 (4 * 0.75)");
        for (int i = 1; i <= 5; i++) {
            resizeMap.put(i, i);
            // Internal capacity starts at 4, threshold = 4 * 0.75 = 3
            // When size exceeds 3, HashMap doubles capacity: 4 → 8
            System.out.println("After put(" + i + ") → size=" + resizeMap.size());
        }
        System.out.println("(Internally: capacity doubled from 4 → 8 after 4th insert exceeded threshold 3)");

        // 5. Null key handling
        System.out.println("\n=== Null Key ===");
        Map<String, String> nullMap = new HashMap<>();
        nullMap.put(null, "null-value");
        nullMap.put(null, "overwritten");
        nullMap.put("key", null);
        System.out.println("null key → value: " + nullMap.get(null));
        System.out.println("'key' → null value: " + nullMap.get("key"));
    }

}
