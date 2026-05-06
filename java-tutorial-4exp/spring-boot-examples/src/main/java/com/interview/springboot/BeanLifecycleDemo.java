package com.interview.springboot;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Demonstrates the full Spring bean lifecycle:
 * Constructor → @Autowired → @PostConstruct → InitializingBean → READY → @PreDestroy → DisposableBean
 */
@Component
public class BeanLifecycleDemo implements InitializingBean, DisposableBean {

    public BeanLifecycleDemo() {
        System.out.println("[Lifecycle] 1. Constructor called");
    }

    @PostConstruct
    public void postConstruct() {
        System.out.println("[Lifecycle] 2. @PostConstruct — bean is fully injected, do init logic here");
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("[Lifecycle] 3. InitializingBean.afterPropertiesSet() — alternative to @PostConstruct");
    }

    @PreDestroy
    public void preDestroy() {
        System.out.println("[Lifecycle] 4. @PreDestroy — cleanup before destruction");
    }

    @Override
    public void destroy() {
        System.out.println("[Lifecycle] 5. DisposableBean.destroy() — alternative to @PreDestroy");
    }
}
