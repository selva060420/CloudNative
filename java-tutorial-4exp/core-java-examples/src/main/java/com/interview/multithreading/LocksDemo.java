package com.interview.multithreading;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.TimeUnit;

/**
 * Demonstrates ReentrantLock, tryLock with timeout, and ReadWriteLock.
 */
public class LocksDemo {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static String sharedConfig = "initial";

    public static void main(String[] args) throws InterruptedException {
        // 1. ReentrantLock with tryLock (avoids deadlock)
        System.out.println("=== tryLock Demo ===");
        Thread holder = new Thread(() -> {
            lock.lock();
            try {
                System.out.println("  Holder acquired lock, sleeping...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        });

        Thread tryLocker = new Thread(() -> {
            try {
                boolean acquired = lock.tryLock(500, TimeUnit.MILLISECONDS);
                if (acquired) {
                    try { System.out.println("  TryLocker got lock!"); }
                    finally { lock.unlock(); }
                } else {
                    System.out.println("  TryLocker timed out — no deadlock!");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        holder.start();
        Thread.sleep(100);
        tryLocker.start();
        holder.join();
        tryLocker.join();

        // 2. ReadWriteLock — multiple readers, exclusive writer
        System.out.println("\n=== ReadWriteLock Demo ===");
        Runnable reader = () -> {
            rwLock.readLock().lock();
            try {
                System.out.println("  " + Thread.currentThread().getName() + " reads: " + sharedConfig);
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                rwLock.readLock().unlock();
            }
        };

        Runnable writer = () -> {
            rwLock.writeLock().lock();
            try {
                sharedConfig = "updated-" + System.currentTimeMillis();
                System.out.println("  " + Thread.currentThread().getName() + " wrote: " + sharedConfig);
            } finally {
                rwLock.writeLock().unlock();
            }
        };

        // Multiple readers can run concurrently
        Thread r1 = new Thread(reader, "Reader-1");
        Thread r2 = new Thread(reader, "Reader-2");
        Thread w1 = new Thread(writer, "Writer-1");

        r1.start(); r2.start();
        Thread.sleep(50);
        w1.start(); // writer waits for readers to finish
        r1.join(); r2.join(); w1.join();

        System.out.println("  Final config: " + sharedConfig);
    }
}
