package com.interview.multithreading;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates deadlock creation and prevention with tryLock.
 * Run with: jstack <pid> to see "Found one Java-level deadlock"
 */
public class DeadlockDemo {

    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Deadlock Prevention with tryLock ===");
        safeTransfer();

        // Uncomment below to see actual deadlock (will hang!)
        // System.out.println("\n=== Creating Deadlock (will hang) ===");
        // createDeadlock();
    }

    /**
     * SAFE: Using tryLock with timeout to prevent deadlock.
     */
    private static void safeTransfer() throws InterruptedException {
        ReentrantLock lock1 = new ReentrantLock();
        ReentrantLock lock2 = new ReentrantLock();

        Thread t1 = new Thread(() -> {
            try {
                if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        Thread.sleep(100);
                        if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                            try { System.out.println("  T1: acquired both locks — transfer done"); }
                            finally { lock2.unlock(); }
                        } else {
                            System.out.println("  T1: couldn't get lock2, backing off");
                        }
                    } finally { lock1.unlock(); }
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Transfer-1");

        Thread t2 = new Thread(() -> {
            try {
                if (lock2.tryLock(1, TimeUnit.SECONDS)) {
                    try {
                        Thread.sleep(100);
                        if (lock1.tryLock(1, TimeUnit.SECONDS)) {
                            try { System.out.println("  T2: acquired both locks — transfer done"); }
                            finally { lock1.unlock(); }
                        } else {
                            System.out.println("  T2: couldn't get lock1, backing off");
                        }
                    } finally { lock2.unlock(); }
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Transfer-2");

        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("  No deadlock — both threads completed!");
    }

    /**
     * UNSAFE: Classic deadlock — T1 holds lockA wants lockB, T2 holds lockB wants lockA.
     * Uncomment in main() to demonstrate. Use jstack to diagnose.
     */
    @SuppressWarnings("unused")
    private static void createDeadlock() {
        Thread t1 = new Thread(() -> {
            synchronized (lockA) {
                System.out.println("  T1 holds lockA, waiting for lockB...");
                try { Thread.sleep(100); } catch (InterruptedException e) { }
                synchronized (lockB) { System.out.println("  T1 got both"); }
            }
        });

        Thread t2 = new Thread(() -> {
            synchronized (lockB) {
                System.out.println("  T2 holds lockB, waiting for lockA...");
                try { Thread.sleep(100); } catch (InterruptedException e) { }
                synchronized (lockA) { System.out.println("  T2 got both"); }
            }
        });

        t1.start(); t2.start();
    }
}
