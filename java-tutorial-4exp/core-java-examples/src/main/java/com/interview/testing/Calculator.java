package com.interview.testing;

import java.util.List;

public class Calculator {
    public int add(int a, int b) { return a + b; }
    public int subtract(int a, int b) { return a - b; }
    public int multiply(int a, int b) { return a * b; }
    public int divide(int a, int b) { return a / b; }
    public int sum(List<Integer> numbers) { return numbers.stream().mapToInt(i -> i).sum(); }

    public int parseAndAdd(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input must not be blank");
        }
        String[] parts = input.split("\\+");
        return Integer.parseInt(parts[0].trim()) + Integer.parseInt(parts[1].trim());
    }
}
