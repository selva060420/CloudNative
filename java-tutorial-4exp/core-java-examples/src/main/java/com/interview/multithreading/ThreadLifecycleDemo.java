package com.interview.multithreading;

/**
 * Demonstrates thread states and transitions.
 * Run and observe: NEW → RUNNABLE → TIMED_WAITING → TERMINATED
 */
public class ThreadLifecycleDemo {

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            System.out.println("  Thread running: " + Thread.currentThread().getState()); // RUNNABLE
            try {
                Thread.sleep(1000); // → TIMED_WAITING
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "demo-thread");

        System.out.println("After new:   " + t.getState());   // NEW
        t.start();
        System.out.println("After start: " + t.getState());   // RUNNABLE

        Thread.sleep(200);
        System.out.println("During sleep:" + t.getState());    // TIMED_WAITING

        t.join();
        System.out.println("After join:  " + t.getState());   // TERMINATED

        // Demonstrate BLOCKED state
        Object lock = new Object();
        Thread holder = new Thread(() -> {
            synchronized (lock) {
                try { Thread.sleep(2000); } catch (InterruptedException e) { }
            }
        }, "lock-holder");

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                System.out.println("  Waiter acquired lock");
            }
        }, "lock-waiter");

        holder.start();
        Thread.sleep(100);
        waiter.start();
        Thread.sleep(100);
        System.out.println("Waiter state: " + waiter.getState()); // BLOCKED

        holder.join();
        waiter.join();
    }
}
