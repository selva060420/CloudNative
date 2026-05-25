package com.interview.testing;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCart {
    private final List<CartItem> items = new ArrayList<>();
    private double discountPercent = 0;

    public void addItem(String name, double price, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        items.add(new CartItem(name, price, quantity));
    }

    public void removeItem(String name) {
        items.removeIf(item -> item.name().equals(name));
    }

    public void applyDiscount(double percent) {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Invalid discount");
        this.discountPercent = percent;
    }

    public int itemCount() { return items.size(); }

    public double total() {
        double sum = items.stream().mapToDouble(i -> i.price() * i.quantity()).sum();
        return sum * (1 - discountPercent / 100.0);
    }

    public record CartItem(String name, double price, int quantity) {}
}
