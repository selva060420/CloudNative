package com.interview.springboot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Demonstrates @Conditional annotations and @ConfigurationProperties.
 * 
 * The RateLimiter bean only activates when app.ratelimit.enabled=true.
 * This pattern is used in production to feature-toggle components.
 */
@Configuration
@EnableConfigurationProperties(AutoConfigConditionalDemo.RateLimitProperties.class)
public class AutoConfigConditionalDemo {

    @ConfigurationProperties(prefix = "app.ratelimit")
    public static class RateLimitProperties {
        private boolean enabled = false;
        private int maxRequests = 100;
        private Duration window = Duration.ofMinutes(1);

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public int getMaxRequests() { return maxRequests; }
        public void setMaxRequests(int maxRequests) { this.maxRequests = maxRequests; }
        public Duration getWindow() { return window; }
        public void setWindow(Duration window) { this.window = window; }

        @Override
        public String toString() {
            return "RateLimitProperties{enabled=" + enabled + ", maxRequests=" + maxRequests + ", window=" + window + "}";
        }
    }

    public static class RateLimiter {
        private final int maxRequests;
        private final Duration window;

        public RateLimiter(int maxRequests, Duration window) {
            this.maxRequests = maxRequests;
            this.window = window;
            System.out.println("[ConditionalDemo] RateLimiter CREATED: max=" + maxRequests + ", window=" + window);
        }

        public boolean allowRequest(String clientId) {
            // Simplified — real impl would use sliding window counter
            System.out.println("[RateLimiter] Checking request for client: " + clientId);
            return true;
        }

        public int getMaxRequests() { return maxRequests; }
        public Duration getWindow() { return window; }
    }

    /**
     * This bean is ONLY created when app.ratelimit.enabled=true.
     * In dev profile, this property is false → bean not created.
     * In prod profile, this property is true → bean is active.
     */
    @Bean
    @ConditionalOnProperty(name = "app.ratelimit.enabled", havingValue = "true")
    @ConditionalOnMissingBean(RateLimiter.class)
    public RateLimiter rateLimiter(RateLimitProperties props) {
        return new RateLimiter(props.getMaxRequests(), props.getWindow());
    }
}
