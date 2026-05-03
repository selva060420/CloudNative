package com.interview.multithreading;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates race conditions and fixes: synchronized, volatile, AtomicInteger.
 */
public class SynchronizationDemo {

    private static int unsafeCounter = 0;
    private static int syncCounter = 0;
    private static volatile boolean stopFlag = false;
    private static final AtomicInteger atomicCounter = new AtomicInteger(0);
    private static final Object lock = new Object();

    public static void main(String[] args) throws InterruptedException {
        // 1. Race condition — unsafeCounter will likely be < 200000
        Thread t1 = new Thread(() -> { for (int i = 0; i < 100_000; i++) unsafeCounter++; });
        Thread t2 = new Thread(() -> { for (int i = 0; i < 100_000; i++) unsafeCounter++; });
        t1.start(); t2.start(); t1.join(); t2.join();
        System.out.println("Unsafe counter (expect < 200000): " + unsafeCounter);

        // 2. Fix with synchronized
        Thread t3 = new Thread(() -> { for (int i = 0; i < 100_000; i++) synchronized (lock) { syncCounter++; } });
        Thread t4 = new Thread(() -> { for (int i = 0; i < 100_000; i++) synchronized (lock) { syncCounter++; } });
        t3.start(); t4.start(); t3.join(); t4.join();
        System.out.println("Sync counter (expect 200000):     " + syncCounter);

        // 3. Fix with AtomicInteger (lock-free)
        Thread t5 = new Thread(() -> { for (int i = 0; i < 100_000; i++) atomicCounter.incrementAndGet(); });
        Thread t6 = new Thread(() -> { for (int i = 0; i < 100_000; i++) atomicCounter.incrementAndGet(); });
        t5.start(); t6.start(); t5.join(); t6.join();
        System.out.println("Atomic counter (expect 200000):   " + atomicCounter.get());

        // 4. volatile for visibility (flag pattern)
        Thread worker = new Thread(() -> {
            long count = 0;
            while (!stopFlag) { count++; }
            System.out.println("Worker stopped after " + count + " iterations");
        });
        worker.start();
        Thread.sleep(100);
        stopFlag = true; // visible to worker because volatile
        worker.join();
        System.out.println("Volatile flag demo complete");
    }
}
