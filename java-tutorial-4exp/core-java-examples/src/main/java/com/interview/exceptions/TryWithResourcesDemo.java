package com.interview.exceptions;

/**
 * Demo: try-with-resources, AutoCloseable, suppressed exceptions.
 */
public class TryWithResourcesDemo {

    // Custom AutoCloseable resource
    static class ManagedConnection implements AutoCloseable {
        private final String name;
        private boolean open = true;

        ManagedConnection(String name) {
            this.name = name;
            System.out.println("  [OPEN] " + name);
        }

        String query(String sql) {
            if (!open) throw new IllegalStateException(name + " is closed");
            return name + " → result of: " + sql;
        }

        @Override
        public void close() {
            open = false;
            System.out.println("  [CLOSE] " + name);
        }
    }

    // Resource that throws on close — demonstrates suppressed exceptions
    static class FlakyResource implements AutoCloseable {
        void doWork() { throw new RuntimeException("Work failed"); }

        @Override
        public void close() { throw new RuntimeException("Close also failed"); }
    }

    public static void main(String[] args) {
        // 1. Basic try-with-resources — auto-close guaranteed
        System.out.println("=== 1. Auto-close on success ===");
        try (var conn = new ManagedConnection("db-pool-1")) {
            System.out.println("  " + conn.query("SELECT 1"));
        }

        // 2. Auto-close on exception
        System.out.println("\n=== 2. Auto-close on exception ===");
        try (var conn = new ManagedConnection("db-pool-2")) {
            throw new RuntimeException("Query timeout");
        } catch (RuntimeException e) {
            System.out.println("  Caught: " + e.getMessage());
        }
        // Connection still closed!

        // 3. Multiple resources — closed in reverse order
        System.out.println("\n=== 3. Multiple resources (reverse close order) ===");
        try (var conn1 = new ManagedConnection("primary");
             var conn2 = new ManagedConnection("replica")) {
            System.out.println("  " + conn1.query("WRITE"));
            System.out.println("  " + conn2.query("READ"));
        }

        // 4. Suppressed exceptions
        System.out.println("\n=== 4. Suppressed exceptions ===");
        try (var flaky = new FlakyResource()) {
            flaky.doWork();
        } catch (RuntimeException e) {
            System.out.println("  Primary: " + e.getMessage());
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("  Suppressed: " + suppressed.getMessage());
            }
        }
    }
}
