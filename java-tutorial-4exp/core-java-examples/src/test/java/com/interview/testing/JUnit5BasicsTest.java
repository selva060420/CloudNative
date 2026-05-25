package com.interview.testing;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates JUnit 5 features: lifecycle, assertions, parameterized tests, nested tests.
 */
@DisplayName("JUnit 5 Basics")
class JUnit5BasicsTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    // --- Basic Assertions ---

    @Test
    @DisplayName("should add two numbers")
    void shouldAdd() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    @DisplayName("should throw on division by zero")
    void shouldThrowOnDivisionByZero() {
        ArithmeticException ex = assertThrows(ArithmeticException.class,
                () -> calculator.divide(10, 0));
        assertEquals("/ by zero", ex.getMessage());
    }

    @Test
    @DisplayName("should complete within timeout")
    void shouldCompleteWithinTimeout() {
        assertTimeout(Duration.ofMillis(100), () -> calculator.add(1, 1));
    }

    @Test
    @DisplayName("grouped assertions — all checked even if one fails")
    void groupedAssertions() {
        assertAll("calculator operations",
                () -> assertEquals(4, calculator.add(2, 2)),
                () -> assertEquals(0, calculator.subtract(2, 2)),
                () -> assertEquals(6, calculator.multiply(2, 3))
        );
    }

    // --- Parameterized Tests ---

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({"1, 1, 2", "2, 3, 5", "-1, 1, 0", "0, 0, 0"})
    void shouldAddParameterized(int a, int b, int expected) {
        assertEquals(expected, calculator.add(a, b));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    void shouldRejectBlankInput(String input) {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.parseAndAdd(input));
    }

    // --- Nested Tests ---

    @Nested
    @DisplayName("when result is negative")
    class NegativeResults {

        @Test
        @DisplayName("subtract larger from smaller")
        void shouldReturnNegative() {
            assertTrue(calculator.subtract(2, 5) < 0);
        }
    }

    @Nested
    @DisplayName("when working with lists")
    class ListOperations {

        @Test
        void shouldSumList() {
            assertEquals(10, calculator.sum(List.of(1, 2, 3, 4)));
        }

        @Test
        void shouldReturnZeroForEmptyList() {
            assertEquals(0, calculator.sum(List.of()));
        }
    }

    // --- Simple class under test ---

    static class Calculator {
        int add(int a, int b) { return a + b; }
        int subtract(int a, int b) { return a - b; }
        int multiply(int a, int b) { return a * b; }
        int divide(int a, int b) { return a / b; }
        int sum(List<Integer> numbers) { return numbers.stream().mapToInt(i -> i).sum(); }

        int parseAndAdd(String input) {
            if (input == null || input.isBlank()) {
                throw new IllegalArgumentException("Input must not be blank");
            }
            String[] parts = input.split("\\+");
            return Integer.parseInt(parts[0].trim()) + Integer.parseInt(parts[1].trim());
        }
    }
}
