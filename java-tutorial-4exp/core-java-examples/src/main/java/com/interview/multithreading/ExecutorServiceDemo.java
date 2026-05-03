package com.interview.multithreading;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates thread pools: fixed, custom ThreadPoolExecutor, and proper shutdown.
 * Mirrors production patterns used in Ericsson NEF notification dispatch.
 */
public class ExecutorServiceDemo {

    public static void main(String[] args) throws Exception {
        // 1. Fixed thread pool — simple and safe
        System.out.println("=== FixedThreadPool ===");
        ExecutorService fixed = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            int taskId = i;
            fixed.submit(() -> {
                System.out.println("  Task-" + taskId + " on " + Thread.currentThread().getName());
                try { Thread.sleep(200); } catch (InterruptedException e) { }
            });
        }
        fixed.shutdown();
        fixed.awaitTermination(5, TimeUnit.SECONDS);

        // 2. Custom ThreadPoolExecutor — production-grade
        System.out.println("\n=== Custom ThreadPoolExecutor (production) ===");
        AtomicInteger threadNum = new AtomicInteger(0);
        ThreadPoolExecutor custom = new ThreadPoolExecutor(
            4,                                    // corePoolSize
            8,                                    // maxPoolSize
            60L, TimeUnit.SECONDS,                // keepAliveTime
            new LinkedBlockingQueue<>(100),        // bounded queue
            r -> new Thread(r, "nef-worker-" + threadNum.getAndIncrement()), // named threads
            new ThreadPoolExecutor.CallerRunsPolicy()  // backpressure: caller executes if full
        );

        for (int i = 0; i < 10; i++) {
            int taskId = i;
            custom.execute(() -> {
                System.out.println("  Task-" + taskId + " on " + Thread.currentThread().getName());
                try { Thread.sleep(100); } catch (InterruptedException e) { }
            });
        }

        custom.shutdown();
        custom.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("  Pool stats — completed: " + custom.getCompletedTaskCount()
            + ", largest pool: " + custom.getLargestPoolSize());

        // 3. ScheduledExecutorService — periodic tasks
        System.out.println("\n=== ScheduledExecutor ===");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            () -> System.out.println("  Heartbeat at " + System.currentTimeMillis()),
            0, 300, TimeUnit.MILLISECONDS
        );
        Thread.sleep(1000);
        scheduler.shutdown();
    }
}
