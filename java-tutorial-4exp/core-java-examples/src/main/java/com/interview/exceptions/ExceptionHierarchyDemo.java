package com.interview.exceptions;

/**
 * Demo: Exception hierarchy, checked vs unchecked, multi-catch, chaining.
 */
public class ExceptionHierarchyDemo {

    // Checked — caller must handle
    static String readConfig(String path) throws ConfigNotFoundException {
        if (path == null) throw new ConfigNotFoundException("Path is null");
        return "config-value";
    }

    // Unchecked — programming error
    static int divide(int a, int b) {
        if (b == 0) throw new IllegalArgumentException("Divisor cannot be zero");
        return a / b;
    }

    // Exception chaining — preserve root cause
    static void connectToDatabase() {
        try {
            throw new java.io.IOException("Connection refused");
        } catch (java.io.IOException e) {
            throw new ServiceInitException("DB init failed", e); // chain original cause
        }
    }

    public static void main(String[] args) {
        // 1. Checked exception
        try {
            readConfig(null);
        } catch (ConfigNotFoundException e) {
            System.out.println("Checked: " + e.getMessage());
        }

        // 2. Unchecked exception
        try {
            divide(10, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("Unchecked: " + e.getMessage());
        }

        // 3. Multi-catch
        try {
            Object obj = "hello";
            Integer num = (Integer) obj; // ClassCastException
        } catch (ClassCastException | NullPointerException e) {
            System.out.println("Multi-catch: " + e.getClass().getSimpleName());
        }

        // 4. Exception chaining
        try {
            connectToDatabase();
        } catch (ServiceInitException e) {
            System.out.println("Chained: " + e.getMessage() + " → cause: " + e.getCause().getMessage());
        }

        // 5. finally always runs
        try {
            System.out.println("Try block");
            throw new RuntimeException("boom");
        } catch (RuntimeException e) {
            System.out.println("Caught: " + e.getMessage());
        } finally {
            System.out.println("Finally: always executes");
        }
    }

    // Custom checked exception
    static class ConfigNotFoundException extends Exception {
        ConfigNotFoundException(String msg) { super(msg); }
    }

    // Custom unchecked with chaining
    static class ServiceInitException extends RuntimeException {
        ServiceInitException(String msg, Throwable cause) { super(msg, cause); }
    }
}
