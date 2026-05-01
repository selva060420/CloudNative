package com.interview.corejava.annotations;

import java.lang.annotation.*;
import java.lang.reflect.Method;

/**
 * Demonstrates custom annotations and runtime processing via reflection.
 * Context: How Spring Boot @RequestMapping, @Autowired work under the hood.
 */
public class AnnotationsDemo {

    // Custom annotation — retained at runtime for reflection
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface ApiEndpoint {
        String path();
        String method() default "GET";
    }

    // Usage — similar to Spring's @GetMapping
    static class UserController {

        @ApiEndpoint(path = "/users", method = "GET")
        public String getUsers() { return "List of users"; }

        @ApiEndpoint(path = "/users", method = "POST")
        public String createUser() { return "User created"; }

        public String helperMethod() { return "Not an endpoint"; }
    }

    public static void main(String[] args) {
        System.out.println("=== Custom Annotation Processing (like Spring does) ===\n");

        // Scan class for annotated methods — this is what Spring does at startup
        for (Method method : UserController.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ApiEndpoint.class)) {
                ApiEndpoint endpoint = method.getAnnotation(ApiEndpoint.class);
                System.out.println("Found endpoint: " + endpoint.method() + " " + endpoint.path()
                        + " → " + method.getName() + "()");
            }
        }

        System.out.println("\n=== Built-in Annotations ===");
        System.out.println("@Override — compile-time check for method override");
        System.out.println("@Deprecated — marks API for removal");
        System.out.println("@SuppressWarnings — suppress compiler warnings");
        System.out.println("@FunctionalInterface — ensures single abstract method");
    }
}
