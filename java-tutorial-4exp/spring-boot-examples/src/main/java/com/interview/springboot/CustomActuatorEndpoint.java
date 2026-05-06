package com.interview.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Custom Actuator endpoint: GET /actuator/ratelimit
 * 
 * Shows current rate-limit configuration.
 * Demonstrates how to extend Actuator for operational visibility.
 * 
 * In production, custom endpoints expose app-specific health/config
 * that K8s operators or monitoring tools can query.
 */
@Component
@Endpoint(id = "ratelimit")
public class CustomActuatorEndpoint {

    private final AutoConfigConditionalDemo.RateLimitProperties properties;
    private final Optional<AutoConfigConditionalDemo.RateLimiter> rateLimiter;

    @Autowired
    public CustomActuatorEndpoint(
            AutoConfigConditionalDemo.RateLimitProperties properties,
            Optional<AutoConfigConditionalDemo.RateLimiter> rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @ReadOperation
    public Map<String, Object> rateLimitInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("active", rateLimiter.isPresent());
        info.put("enabled", properties.isEnabled());
        info.put("maxRequests", properties.getMaxRequests());
        info.put("window", properties.getWindow().toString());
        return info;
    }
}
