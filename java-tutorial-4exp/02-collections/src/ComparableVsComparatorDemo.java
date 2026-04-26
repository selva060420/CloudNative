package collections;

import java.util.*;

/**
 * Demonstrates Comparable (natural ordering) vs Comparator (custom ordering).
 * Interview focus: When to use which, chaining comparators, Java 8 methods.
 */
public class ComparableVsComparatorDemo {

    // Comparable — defines natural ordering inside the class
    static class Employee implements Comparable<Employee> {
        String name;
        double salary;
        String department;

        Employee(String name, double salary, String department) {
            this.name = name;
            this.salary = salary;
            this.department = department;
        }

        @Override
        public int compareTo(Employee other) {
            return this.name.compareTo(other.name); // natural order: by name
        }

        @Override
        public String toString() {
            return name + "($" + salary + ", " + department + ")";
        }
    }

    public static void main(String[] args) {
        List<Employee> employees = new ArrayList<>(List.of(
                new Employee("Charlie", 70000, "Engineering"),
                new Employee("Alice", 90000, "Engineering"),
                new Employee("Bob", 60000, "Marketing"),
                new Employee("Alice", 50000, "Marketing")
        ));

        // 1. Comparable — natural ordering (by name)
        System.out.println("=== Comparable (natural order by name) ===");
        Collections.sort(employees);
        employees.forEach(System.out::println);

        // 2. Comparator — custom ordering (by salary)
        System.out.println("\n=== Comparator (by salary descending) ===");
        employees.sort(Comparator.comparingDouble(Employee::getSalary).reversed());
        employees.forEach(System.out::println);

        // 3. Chained comparators — by name, then by salary
        System.out.println("\n=== Chained (by name, then salary) ===");
        Comparator<Employee> chained = Comparator.comparing(Employee::getName)
                .thenComparingDouble(Employee::getSalary);
        employees.sort(chained);
        employees.forEach(System.out::println);

        // 4. Null-safe comparator
        System.out.println("\n=== Null-safe Comparator ===");
        List<String> withNulls = new ArrayList<>(Arrays.asList("banana", null, "apple", null, "cherry"));
        withNulls.sort(Comparator.nullsLast(Comparator.naturalOrder()));
        System.out.println(withNulls);

        // 5. Using TreeSet with Comparator
        System.out.println("\n=== TreeSet with custom Comparator (by salary) ===");
        TreeSet<Employee> sortedBySalary = new TreeSet<>(Comparator.comparingDouble(Employee::getSalary));
        sortedBySalary.addAll(employees);
        sortedBySalary.forEach(System.out::println);
    }

    // Helper for method reference
    static double getSalary(Employee e) { return e.salary; }
    static String getName(Employee e) { return e.name; }
}
