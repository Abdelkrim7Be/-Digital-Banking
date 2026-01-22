package com.bellagnech.transaction.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    // Resilience4j is configured via application.properties
    // Circuit breakers and retries are automatically applied to Feign clients
}

