package com.interview.springboot;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Demonstrates Spring AOP with a custom @Timed annotation.
 * Any method annotated with @Timed will have its execution time logged.
 *
 * How it works:
 * 1. Spring creates a CGLIB proxy around beans with @Timed methods
 * 2. The proxy intercepts the call and delegates to the aspect
 * 3. Aspect measures time around the actual method execution
 */
public class AopTimingDemo {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Timed {
        String value() default "";
    }

    @Aspect
    @Component
    public static class TimingAspect {

        @Around("@annotation(timed)")
        public Object measureExecutionTime(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
            long start = System.nanoTime();
            try {
                return joinPoint.proceed();
            } finally {
                long durationMs = (System.nanoTime() - start) / 1_000_000;
                String label = timed.value().isEmpty() ? joinPoint.getSignature().toShortString() : timed.value();
                System.out.printf("[AOP] %s executed in %dms%n", label, durationMs);
            }
        }
    }

    /**
     * Example service using @Timed annotation.
     */
    @Component
    public static class OrderService {

        @Timed("processOrder")
        public String processOrder(String orderId) {
            // Simulate work
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return "Order " + orderId + " processed";
        }
    }
}
