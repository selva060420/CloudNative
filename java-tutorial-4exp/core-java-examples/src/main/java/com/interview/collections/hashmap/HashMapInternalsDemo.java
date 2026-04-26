package com.interview.collections.hashmap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates HashMap internals — hashing, collision, treeification, resizing.
 * Interview focus: How put/get works, why capacity is power of 2, load factor.
 */
public class HashMapInternalsDemo {

    public static void main(String[] args) throws Exception {
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
        Map<CollisionKey, String> collisionMap = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            collisionMap.put(new CollisionKey(i), "value-" + i);
        }
        System.out.println("All 10 keys have same hashCode but different equals");
        System.out.println("Bucket converts to Red-Black tree at 8+ entries");
        System.out.println("size: " + collisionMap.size());
        System.out.println("get(key-5): " + collisionMap.get(new CollisionKey(5)));

        // 4. Resizing demo
        System.out.println("\n=== Resizing Demo ===");
        Map<Integer, Integer> resizeMap = new HashMap<>(4, 0.75f);
        System.out.println("Initial capacity: 4, threshold: 3 (4 * 0.75)");
        for (int i = 1; i <= 5; i++) {
            resizeMap.put(i, i);
            System.out.println("After put(" + i + ") → size=" + resizeMap.size()
                    + ", capacity=" + getCapacity(resizeMap));
        }

        // 5. Null key handling
        System.out.println("\n=== Null Key ===");
        Map<String, String> nullMap = new HashMap<>();
        nullMap.put(null, "null-value");
        nullMap.put(null, "overwritten");
        nullMap.put("key", null);
        System.out.println("null key → value: " + nullMap.get(null));
        System.out.println("'key' → null value: " + nullMap.get("key"));
    }

    // Forces all keys into the same bucket to demonstrate collision/treeification
    static class CollisionKey {
        int id;

        CollisionKey(int id) { this.id = id; }

        @Override
        public int hashCode() { return 1; } // same hash for all

        @Override
        public boolean equals(Object o) {
            return o instanceof CollisionKey && ((CollisionKey) o).id == this.id;
        }
    }

    // Reflection to read internal capacity
    @SuppressWarnings("unchecked")
    static int getCapacity(Map<?, ?> map) throws Exception {
        Field table = HashMap.class.getDeclaredField("table");
        table.setAccessible(true);
        Object[] arr = (Object[]) table.get(map);
        return arr == null ? 0 : arr.length;
    }
}
