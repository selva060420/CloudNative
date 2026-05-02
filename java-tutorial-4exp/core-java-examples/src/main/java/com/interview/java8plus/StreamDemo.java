package com.interview.java8plus;

import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates: Stream API — filter, map, flatMap, collect, reduce, groupingBy.
 */
public class StreamDemo {

    record Order(String customer, List<String> items, double total) {}

    public static void main(String[] args) {
        List<Order> orders = List.of(
            new Order("Alice", List.of("Laptop", "Mouse"), 1200.0),
            new Order("Bob", List.of("Keyboard"), 80.0),
            new Order("Alice", List.of("Monitor", "Cable"), 450.0),
            new Order("Charlie", List.of("Phone", "Case", "Charger"), 900.0)
        );

        // filter + map + collect
        List<String> highValueCustomers = orders.stream()
            .filter(o -> o.total() > 100)
            .map(Order::customer)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("High-value customers: " + highValueCustomers);

        // flatMap — flatten nested lists
        List<String> allItems = orders.stream()
            .flatMap(o -> o.items().stream())
            .collect(Collectors.toList());
        System.out.println("All items: " + allItems);

        // reduce — total revenue
        double totalRevenue = orders.stream()
            .map(Order::total)
            .reduce(0.0, Double::sum);
        System.out.println("Total revenue: " + totalRevenue);

        // groupingBy — orders per customer
        Map<String, List<Order>> byCustomer = orders.stream()
            .collect(Collectors.groupingBy(Order::customer));
        byCustomer.forEach((k, v) -> System.out.println(k + ": " + v.size() + " orders"));

        // groupingBy + summarizing
        Map<String, Double> revenueByCustomer = orders.stream()
            .collect(Collectors.groupingBy(Order::customer, Collectors.summingDouble(Order::total)));
        System.out.println("Revenue by customer: " + revenueByCustomer);

        // partitioningBy — split into two groups
        Map<Boolean, List<Order>> partitioned = orders.stream()
            .collect(Collectors.partitioningBy(o -> o.total() > 500));
        System.out.println("High value orders: " + partitioned.get(true).size());
        System.out.println("Low value orders: " + partitioned.get(false).size());

        // toMap with merge function (handle duplicate keys)
        Map<String, Double> maxOrderByCustomer = orders.stream()
            .collect(Collectors.toMap(Order::customer, Order::total, Math::max));
        System.out.println("Max order per customer: " + maxOrderByCustomer);
    }
}
