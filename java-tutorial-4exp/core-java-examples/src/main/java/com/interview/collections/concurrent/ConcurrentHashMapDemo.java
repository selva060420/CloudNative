package com.interview.collections.concurrent;

import java.util.*;
import java.util.concurrent.*;

/**
 * Demonstrates ConcurrentHashMap vs HashMap vs synchronizedMap.
 * Interview focus: Thread safety, CAS, segment locking, fail-safe iteration.
 */
public class ConcurrentHashMapDemo {

    public static void main(String[] args) throws InterruptedException {
        // 1. HashMap — NOT thread-safe (can lose data or throw CME)
        System.out.println("=== HashMap (not thread-safe) ===");
        Map<Integer, Integer> hashMap = new HashMap<>();
        runConcurrentPuts(hashMap, "HashMap");

        // 2. Collections.synchronizedMap — thread-safe but slow (full lock)
        System.out.println("\n=== synchronizedMap ===");
        Map<Integer, Integer> syncMap = Collections.synchronizedMap(new HashMap<>());
        runConcurrentPuts(syncMap, "synchronizedMap");

        // 3. ConcurrentHashMap — thread-safe and fast (bucket-level locking)
        System.out.println("\n=== ConcurrentHashMap ===");
        Map<Integer, Integer> concMap = new ConcurrentHashMap<>();
        runConcurrentPuts(concMap, "ConcurrentHashMap");

        // 4. Fail-safe iteration — ConcurrentHashMap does NOT throw CME
        System.out.println("\n=== Fail-safe iteration (ConcurrentHashMap) ===");
        ConcurrentHashMap<String, String> safeMap = new ConcurrentHashMap<>();
        safeMap.put("a", "1");
        safeMap.put("b", "2");
        safeMap.put("c", "3");
        for (String key : safeMap.keySet()) {
            safeMap.put("new-" + key, "added"); // no ConcurrentModificationException
        }
        System.out.println("After concurrent modification: " + safeMap);

        // 5. Atomic operations — compute, merge, putIfAbsent
        System.out.println("\n=== Atomic operations ===");
        ConcurrentHashMap<String, Integer> counter = new ConcurrentHashMap<>();
        String[] words = {"java", "spring", "java", "docker", "java", "spring"};
        for (String word : words) {
            counter.merge(word, 1, Integer::sum); // atomic increment
        }
        System.out.println("Word count: " + counter);

        counter.compute("java", (k, v) -> v == null ? 1 : v * 10);
        System.out.println("After compute: " + counter);

        counter.putIfAbsent("kubernetes", 1);
        System.out.println("After putIfAbsent: " + counter);
    }

    static void runConcurrentPuts(Map<Integer, Integer> map, String name) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int total = 1000;
        for (int i = 0; i < total; i++) {
            final int val = i;
            executor.submit(() -> map.put(val, val));
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println(name + " size (expected " + total + "): " + map.size());
    }
}
