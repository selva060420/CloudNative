package com.interview.multithreading;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Practice Task: Thread-safe bounded buffer (producer-consumer) — 3 approaches.
 * Compares behavior under 10 producers, 10 consumers, buffer size 5.
 */
public class BoundedBufferDemo {

    static final int BUFFER_SIZE = 5;
    static final int PRODUCERS = 10;
    static final int CONSUMERS = 10;
    static final int ITEMS_PER_PRODUCER = 20;

    // ─── Approach 1: synchronized + wait/notifyAll ───────────────────────────────

    static class SynchronizedBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;

        SynchronizedBuffer(int capacity) { this.capacity = capacity; }

        synchronized void put(int item) throws InterruptedException {
            while (queue.size() == capacity) wait();
            queue.add(item);
            notifyAll();
        }

        synchronized int take() throws InterruptedException {
            while (queue.isEmpty()) wait();
            int item = queue.poll();
            notifyAll();
            return item;
        }
    }

    // ─── Approach 2: ReentrantLock + Condition ───────────────────────────────────

    static class LockConditionBuffer {
        private final Queue<Integer> queue = new LinkedList<>();
        private final int capacity;
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notFull = lock.newCondition();
        private final Condition notEmpty = lock.newCondition();

        LockConditionBuffer(int capacity) { this.capacity = capacity; }

        void put(int item) throws InterruptedException {
            lock.lock();
            try {
                while (queue.size() == capacity) notFull.await();
                queue.add(item);
                notEmpty.signal(); // only wake consumers, not all threads
            } finally {
                lock.unlock();
            }
        }

        int take() throws InterruptedException {
            lock.lock();
            try {
                while (queue.isEmpty()) notEmpty.await();
                int item = queue.poll();
                notFull.signal(); // only wake producers
                return item;
            } finally {
                lock.unlock();
            }
        }
    }

    // ─── Approach 3: BlockingQueue ───────────────────────────────────────────────

    // ArrayBlockingQueue already implements bounded buffer internally.
    // No custom class needed — just use it directly.

    // ─── Benchmark harness ───────────────────────────────────────────────────────

    interface Buffer {
        void put(int item) throws InterruptedException;
        int take() throws InterruptedException;
    }

    static long benchmark(String name, Buffer buffer) throws InterruptedException {
        AtomicInteger produced = new AtomicInteger();
        AtomicInteger consumed = new AtomicInteger();
        CountDownLatch done = new CountDownLatch(PRODUCERS + CONSUMERS);

        long start = System.nanoTime();

        for (int i = 0; i < PRODUCERS; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < ITEMS_PER_PRODUCER; j++) {
                        buffer.put(produced.incrementAndGet());
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            }).start();
        }

        int itemsPerConsumer = (PRODUCERS * ITEMS_PER_PRODUCER) / CONSUMERS;
        for (int i = 0; i < CONSUMERS; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < itemsPerConsumer; j++) {
                        buffer.take();
                        consumed.incrementAndGet();
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            }).start();
        }

        done.await();
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%-25s | Time: %4d ms | Produced: %d | Consumed: %d%n",
                name, elapsed, produced.get(), consumed.get());
        return elapsed;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Bounded Buffer: 10 producers, 10 consumers, buffer=5 ===\n");

        SynchronizedBuffer sb = new SynchronizedBuffer(BUFFER_SIZE);
        LockConditionBuffer lb = new LockConditionBuffer(BUFFER_SIZE);
        ArrayBlockingQueue<Integer> bq = new ArrayBlockingQueue<>(BUFFER_SIZE);

        benchmark("synchronized+wait/notify", new Buffer() {
            public void put(int item) throws InterruptedException { sb.put(item); }
            public int take() throws InterruptedException { return sb.take(); }
        });

        benchmark("ReentrantLock+Condition", new Buffer() {
            public void put(int item) throws InterruptedException { lb.put(item); }
            public int take() throws InterruptedException { return lb.take(); }
        });

        benchmark("ArrayBlockingQueue", new Buffer() {
            public void put(int item) throws InterruptedException { bq.put(item); }
            public int take() throws InterruptedException { return bq.take(); }
        });

        System.out.println("\n=== Comparison ===");
        System.out.println("""
        ┌───────────────────────────┬────────────────────────────────────────────────────┐
        │ Approach                  │ Characteristics                                    │
        ├───────────────────────────┼────────────────────────────────────────────────────┤
        │ synchronized + wait/notify│ • Wakes ALL threads (thundering herd)              │
        │                           │ • Simple but coarse-grained                        │
        │                           │ • No fairness guarantee                            │
        ├───────────────────────────┼────────────────────────────────────────────────────┤
        │ ReentrantLock + Condition │ • Separate conditions → signal only relevant thread│
        │                           │ • Supports fairness, tryLock, interruptibility      │
        │                           │ • Better throughput under high contention           │
        ├───────────────────────────┼────────────────────────────────────────────────────┤
        │ BlockingQueue             │ • Zero boilerplate, battle-tested JDK impl         │
        │                           │ • Internally uses ReentrantLock + 2 Conditions     │
        │                           │ • Best choice for production code                  │
        └───────────────────────────┴────────────────────────────────────────────────────┘
        """);
    }
}
