package com.interview.corejava.jvm;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates JVM memory areas: Stack vs Heap, GC behavior.
 * Shows what goes where and how GC reclaims memory.
 */
public class JvmMemoryDemo {

    // Static field → Metaspace (class metadata) + Heap (object)
    private static final String APP_NAME = "NEF-Service";

    public static void main(String[] args) {
        System.out.println("=== Stack vs Heap Demo ===");
        stackVsHeap();

        System.out.println("\n=== GC Eligibility Demo ===");
        gcDemo();

        System.out.println("\n=== Memory Info ===");
        printMemoryInfo();
    }

    static void stackVsHeap() {
        // Primitive → Stack
        int requestCount = 42;

        // Reference → Stack, Object → Heap
        String userId = "user-456";

        // Array reference → Stack, Array object + elements → Heap
        int[] metrics = {100, 200, 300};

        System.out.println("requestCount (stack): " + requestCount);
        System.out.println("userId (heap, ref on stack): " + userId);
        System.out.println("metrics[0] (heap): " + metrics[0]);

        // When this method returns, stack frame is popped
        // But heap objects remain until GC collects them
    }

    static void gcDemo() {
        List<byte[]> data = new ArrayList<>();

        // Allocate objects
        for (int i = 0; i < 5; i++) {
            data.add(new byte[1024 * 1024]); // 1MB each
        }
        System.out.println("Allocated 5MB, objects reachable → NOT eligible for GC");

        // Remove references → eligible for GC
        data.clear();
        System.out.println("References cleared → eligible for GC");

        // Suggest GC (not guaranteed)
        System.gc();
        System.out.println("System.gc() called (hint only, JVM decides)");
    }

    static void printMemoryInfo() {
        Runtime rt = Runtime.getRuntime();
        long maxMB = rt.maxMemory() / (1024 * 1024);
        long totalMB = rt.totalMemory() / (1024 * 1024);
        long freeMB = rt.freeMemory() / (1024 * 1024);

        System.out.println("Max Heap: " + maxMB + "MB");
        System.out.println("Total (allocated): " + totalMB + "MB");
        System.out.println("Free: " + freeMB + "MB");
        System.out.println("Used: " + (totalMB - freeMB) + "MB");
    }
}
