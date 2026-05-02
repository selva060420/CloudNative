package com.interview.java8plus;

import java.util.Optional;

/**
 * Demonstrates: Optional chaining, orElse, flatMap, proper usage patterns.
 */
public class OptionalDemo {

    record Address(String city, String zip) {}
    record Company(String name, Address address) {}
    record User(String name, Company company) {}

    public static void main(String[] args) {
        User userWithCompany = new User("Selva", new Company("Ericsson", new Address("Stockholm", "11122")));
        User userNoCompany = new User("Bob", null);

        // Safe nested navigation with flatMap/map
        String city1 = getCity(userWithCompany);
        String city2 = getCity(userNoCompany);
        System.out.println("Selva's city: " + city1);
        System.out.println("Bob's city: " + city2);

        // orElse vs orElseGet
        Optional<String> empty = Optional.empty();
        System.out.println("orElse: " + empty.orElse("default"));
        System.out.println("orElseGet: " + empty.orElseGet(() -> "computed-" + System.currentTimeMillis()));

        // orElseThrow
        Optional<String> present = Optional.of("value");
        String val = present.orElseThrow(() -> new IllegalStateException("missing!"));
        System.out.println("orElseThrow: " + val);

        // filter
        Optional<String> filtered = Optional.of("hello")
            .filter(s -> s.length() > 3);
        System.out.println("filter (length>3): " + filtered);

        // ifPresentOrElse (Java 9+)
        Optional.of("found").ifPresentOrElse(
            v -> System.out.println("Present: " + v),
            () -> System.out.println("Empty!")
        );

        // or() — Java 9+ — provide alternative Optional
        Optional<String> result = Optional.<String>empty()
            .or(() -> Optional.of("fallback"));
        System.out.println("or() fallback: " + result);
    }

    private static String getCity(User user) {
        return Optional.ofNullable(user)
            .map(User::company)
            .map(Company::address)
            .map(Address::city)
            .orElse("Unknown");
    }
}
