package collections;

import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates Stream API operations commonly asked in interviews.
 * Interview focus: filter, map, reduce, groupingBy, toMap, flatMap, parallel streams.
 */
public class StreamApiDemo {

    record Employee(String name, double salary, String department) {}

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Alice", 90000, "Engineering"),
                new Employee("Bob", 60000, "Marketing"),
                new Employee("Charlie", 70000, "Engineering"),
                new Employee("Diana", 80000, "Marketing"),
                new Employee("Eve", 95000, "Engineering")
        );

        // 1. Filter + Map + Collect
        System.out.println("=== Engineers earning > 80k ===");
        List<String> highEarners = employees.stream()
                .filter(e -> e.salary() > 80000)
                .map(Employee::name)
                .collect(Collectors.toList());
        System.out.println(highEarners);

        // 2. GroupingBy
        System.out.println("\n=== Group by department ===");
        Map<String, List<Employee>> byDept = employees.stream()
                .collect(Collectors.groupingBy(Employee::department));
        byDept.forEach((dept, emps) -> System.out.println(dept + ": " + emps));

        // 3. GroupingBy with downstream collector
        System.out.println("\n=== Average salary by department ===");
        Map<String, Double> avgSalary = employees.stream()
                .collect(Collectors.groupingBy(Employee::department,
                        Collectors.averagingDouble(Employee::salary)));
        System.out.println(avgSalary);

        // 4. Reduce
        System.out.println("\n=== Total salary (reduce) ===");
        double total = employees.stream()
                .mapToDouble(Employee::salary)
                .sum();
        System.out.println("Total: " + total);

        // 5. toMap with merge function
        System.out.println("\n=== Max salary by department (toMap) ===");
        Map<String, Double> maxByDept = employees.stream()
                .collect(Collectors.toMap(Employee::department, Employee::salary, Math::max));
        System.out.println(maxByDept);

        // 6. FlatMap
        System.out.println("\n=== FlatMap — flatten nested lists ===");
        List<List<String>> nested = List.of(List.of("a", "b"), List.of("c", "d"));
        List<String> flat = nested.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        System.out.println(flat);

        // 7. Distinct + Sorted
        System.out.println("\n=== Distinct departments sorted ===");
        List<String> depts = employees.stream()
                .map(Employee::department)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        System.out.println(depts);

        // 8. findFirst / findAny / anyMatch
        System.out.println("\n=== Search operations ===");
        Optional<Employee> first = employees.stream()
                .filter(e -> e.salary() > 80000)
                .findFirst();
        System.out.println("First > 80k: " + first.map(Employee::name).orElse("none"));
        System.out.println("Any in Marketing? " + employees.stream()
                .anyMatch(e -> e.department().equals("Marketing")));

        // 9. Collectors.joining
        System.out.println("\n=== Joining names ===");
        String names = employees.stream()
                .map(Employee::name)
                .collect(Collectors.joining(", "));
        System.out.println(names);

        // 10. Partitioning
        System.out.println("\n=== Partition by salary > 75k ===");
        Map<Boolean, List<Employee>> partitioned = employees.stream()
                .collect(Collectors.partitioningBy(e -> e.salary() > 75000));
        System.out.println("Above 75k: " + partitioned.get(true));
        System.out.println("Below 75k: " + partitioned.get(false));
    }
}
