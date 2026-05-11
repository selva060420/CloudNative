package com.interview.springboot;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Simple AOP demo — logs BEFORE and AFTER every method in GreetingService.
 * Runs automatically at startup so you can see it in the console.
 *
 * Flow: caller → PROXY → Aspect (before) → actual method → Aspect (after) → return
 */
@Configuration
public class AopTimingDemo {

    @Service
    public static class GreetingService {
        public String greet(String name) {
            System.out.println("  [Service] Hello, " + name + "!");
            return "Hello, " + name;
        }
    }

    @Aspect
    @Component
    public static class LoggingAspect {

        @Around("execution(* com.interview.springboot.AopTimingDemo.GreetingService.*(..))")
        public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
            String method = joinPoint.getSignature().toShortString();
            System.out.println("  [AOP] BEFORE → " + method);
            Object result = joinPoint.proceed();
            System.out.println("  [AOP] AFTER  → " + method + " returned: " + result);
            return result;
        }
    }

    @Bean
    CommandLineRunner runAopDemo(GreetingService greetingService) {
        return args -> {
            System.out.println("\n--- AOP Demo ---");
            greetingService.greet("Selva");
            System.out.println("--- AOP Demo End ---\n");
        };
    }
}
