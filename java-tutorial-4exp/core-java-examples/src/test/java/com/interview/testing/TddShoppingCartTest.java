package com.interview.testing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Demonstrates TDD (Red-Green-Refactor) with a shopping cart example.
 * 
 * TDD Process:
 * 1. RED   — Write a failing test
 * 2. GREEN — Write minimum code to pass
 * 3. REFACTOR — Clean up while tests stay green
 */
@DisplayName("TDD — Shopping Cart")
class TddShoppingCartTest {

    // --- Tests written FIRST (TDD style) ---

    @Test
    @DisplayName("new cart should be empty")
    void newCartShouldBeEmpty() {
        ShoppingCart cart = new ShoppingCart();
        assertEquals(0, cart.itemCount());
        assertEquals(0.0, cart.total());
    }

    @Test
    @DisplayName("should add item to cart")
    void shouldAddItem() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Laptop", 999.99, 1);
        assertEquals(1, cart.itemCount());
        assertEquals(999.99, cart.total(), 0.01);
    }

    @Test
    @DisplayName("should calculate total for multiple items")
    void shouldCalculateTotal() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Laptop", 999.99, 1);
        cart.addItem("Mouse", 29.99, 2);
        assertEquals(1059.97, cart.total(), 0.01);
    }

    @Test
    @DisplayName("should apply percentage discount")
    void shouldApplyDiscount() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Laptop", 100.0, 1);
        cart.applyDiscount(10); // 10% off
        assertEquals(90.0, cart.total(), 0.01);
    }

    @Test
    @DisplayName("should reject negative quantity")
    void shouldRejectNegativeQuantity() {
        ShoppingCart cart = new ShoppingCart();
        assertThrows(IllegalArgumentException.class,
                () -> cart.addItem("Laptop", 999.99, -1));
    }

    @Test
    @DisplayName("should reject discount over 100%")
    void shouldRejectInvalidDiscount() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Item", 50.0, 1);
        assertThrows(IllegalArgumentException.class, () -> cart.applyDiscount(101));
    }

    @Test
    @DisplayName("should remove item from cart")
    void shouldRemoveItem() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("Laptop", 999.99, 1);
        cart.addItem("Mouse", 29.99, 1);
        cart.removeItem("Laptop");
        assertEquals(1, cart.itemCount());
        assertEquals(29.99, cart.total(), 0.01);
    }

    // --- Implementation (written AFTER tests — minimum code to pass) ---

    static class ShoppingCart {
        private final List<CartItem> items = new ArrayList<>();
        private double discountPercent = 0;

        void addItem(String name, double price, int quantity) {
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
            items.add(new CartItem(name, price, quantity));
        }

        void removeItem(String name) {
            items.removeIf(item -> item.name.equals(name));
        }

        void applyDiscount(double percent) {
            if (percent < 0 || percent > 100) throw new IllegalArgumentException("Invalid discount");
            this.discountPercent = percent;
        }

        int itemCount() { return items.size(); }

        double total() {
            double sum = items.stream().mapToDouble(i -> i.price * i.quantity).sum();
            return sum * (1 - discountPercent / 100.0);
        }

        record CartItem(String name, double price, int quantity) {}
    }
}
