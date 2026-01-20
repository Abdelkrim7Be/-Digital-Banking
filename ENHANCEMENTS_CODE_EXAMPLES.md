# 💡 Enhancement Code Examples
## Ready-to-Use Code Snippets for Your Banking Application

---

## 🔒 1. Rate Limiting Implementation

### Add to pom.xml
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
```

### Rate Limiter Configuration
```java
package com.bellagnech.dig_bank.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimiterConfig {
    
    @Bean
    public Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofSeconds(1))))
            .build();
    }
}
```

### Rate Limiter Filter
```java
package com.bellagnech.dig_bank.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Bucket bucket;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Rate limit exceeded");
        }
    }
}
```

---

## 🚀 2. Redis Caching Implementation

### Add to pom.xml
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

### Redis Configuration
```java
package com.bellagnech.dig_bank.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

### Use Caching in Service
```java
package com.bellagnech.dig_bank.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {
    
    @Cacheable(value = "customers", key = "#id")
    public CustomerDTO getCustomer(Long id) {
        // Fetch from database
        return customerRepository.findById(id)
            .map(this::toDTO)
            .orElseThrow(() -> new CustomerNotFoundException(id));
    }
    
    @CacheEvict(value = "customers", key = "#customerDTO.id")
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        // Update logic
        return updatedCustomer;
    }
}
```

### application.properties
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
```

---

## 🔄 3. Circuit Breaker with Resilience4j

### Add to pom.xml
```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Circuit Breaker Configuration
```java
package com.bellagnech.dig_bank.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {
    
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofMillis(1000))
            .slidingWindowSize(10)
            .build();
    }
}
```

### Use Circuit Breaker
```java
package com.bellagnech.dig_bank.services;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    
    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountFallback")
    public AccountDTO getAccount(String accountId) {
        return accountRepository.findById(accountId)
            .map(this::toDTO)
            .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
    
    public AccountDTO getAccountFallback(String accountId, Exception e) {
        log.error("Circuit breaker opened for account: {}", accountId, e);
        return AccountDTO.builder()
            .id(accountId)
            .status("UNAVAILABLE")
            .message("Service temporarily unavailable")
            .build();
    }
}
```

### application.properties
```properties
resilience4j.circuitbreaker.instances.accountService.failureRateThreshold=50
resilience4j.circuitbreaker.instances.accountService.waitDurationInOpenState=1000
resilience4j.circuitbreaker.instances.accountService.slidingWindowSize=10
```

---

## 📊 4. Distributed Tracing with Sleuth

### Add to pom.xml
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

### Tracing Configuration
```java
package com.bellagnech.dig_bank.config;

import brave.sampler.Sampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TracingConfig {
    
    @Bean
    public Sampler defaultSampler() {
        return Sampler.ALWAYS_SAMPLE;
    }
}
```

### application.properties
```properties
spring.sleuth.sampler.probability=1.0
spring.zipkin.base-url=http://localhost:9411
```

---

## 📧 5. Email Notification Service

### Add to pom.xml
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

### Email Service
```java
package com.bellagnech.dig_bank.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public void sendTransactionNotification(String to, String accountId, 
                                            String transactionType, 
                                            double amount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Transaction Notification");
        message.setText(String.format(
            "Your account %s has a new %s transaction of $%.2f",
            accountId, transactionType, amount
        ));
        mailSender.send(message);
    }
    
    public void sendWelcomeEmail(String to, String customerName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to Digital Banking");
        message.setText(String.format(
            "Dear %s,\n\nWelcome to Digital Banking! Your account has been created successfully.",
            customerName
        ));
        mailSender.send(message);
    }
}
```

### application.properties
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🔍 6. Request/Response Logging

### Logging Filter
```java
package com.bellagnech.dig_bank.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Component
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        ContentCachingRequestWrapper wrappedRequest = 
            new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = 
            new ContentCachingResponseWrapper(response);
        
        long startTime = System.currentTimeMillis();
        
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest, wrappedResponse, duration);
            wrappedResponse.copyBodyToResponse();
        }
    }
    
    private void logRequest(ContentCachingRequestWrapper request, 
                          ContentCachingResponseWrapper response, 
                          long duration) {
        log.info("Request: {} {} - Status: {} - Duration: {}ms",
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            duration
        );
    }
}
```

---

## 🛡️ 7. API Key Authentication (for Service-to-Service)

### API Key Filter
```java
package com.bellagnech.dig_bank.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${app.api-key}")
    private String validApiKey;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String apiKey = request.getHeader("X-API-Key");
        
        if (apiKey == null || !apiKey.equals(validApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid API Key");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 📈 8. Custom Metrics with Micrometer

### Add to pom.xml
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### Metrics Service
```java
package com.bellagnech.dig_bank.services;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    public void incrementTransactionCounter(String type) {
        Counter.builder("banking.transactions")
            .tag("type", type)
            .register(meterRegistry)
            .increment();
    }
    
    public Timer.Sample startTransactionTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordTransactionTime(Timer.Sample sample, String type) {
        sample.stop(Timer.builder("banking.transaction.duration")
            .tag("type", type)
            .register(meterRegistry));
    }
}
```

### Use in Service
```java
@Service
@RequiredArgsConstructor
public class TransactionService {
    
    private final MetricsService metricsService;
    
    public void credit(String accountId, double amount) {
        Timer.Sample sample = metricsService.startTransactionTimer();
        try {
            // Transaction logic
            metricsService.incrementTransactionCounter("credit");
        } finally {
            metricsService.recordTransactionTime(sample, "credit");
        }
    }
}
```

---

## 🔐 9. Enhanced Security Headers

### Security Headers Configuration
```java
package com.bellagnech.dig_bank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityHeadersConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self'"))
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubdomains(true))
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            );
        
        return http.build();
    }
}
```

---

## 📝 10. Audit Logging

### Audit Entity
```java
package com.bellagnech.dig_bank.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    
    private String ipAddress;
    private String userAgent;
}
```

### Audit Service
```java
package com.bellagnech.dig_bank.services;

import com.bellagnech.dig_bank.entities.AuditLog;
import com.bellagnech.dig_bank.repositories.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    public void logAction(String username, String action, 
                         String entityType, String entityId, 
                         String details, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(username);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDetails(details);
        auditLog.setTimestamp(new Date());
        auditLog.setIpAddress(getClientIpAddress(request));
        auditLog.setUserAgent(request.getHeader("User-Agent"));
        
        auditLogRepository.save(auditLog);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### Audit Aspect
```java
package com.bellagnech.dig_bank.aspects;

import com.bellagnech.dig_bank.services.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    
    private final AuditService auditService;
    
    @Before("@annotation(com.bellagnech.dig_bank.annotations.Auditable)")
    public void audit(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) 
            RequestContextHolder.currentRequestAttributes()).getRequest();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";
        
        String action = joinPoint.getSignature().getName();
        String entityType = joinPoint.getTarget().getClass().getSimpleName();
        
        auditService.logAction(username, action, entityType, null, 
                             "Method executed", request);
    }
}
```

### Auditable Annotation
```java
package com.bellagnech.dig_bank.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String value() default "";
}
```

---

## 🎯 11. Pagination Helper

### Pagination Utility
```java
package com.bellagnech.dig_bank.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {
    
    public static Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        return PageRequest.of(page, size, sort);
    }
    
    public static <T> PaginationResponse<T> toPaginationResponse(Page<T> page) {
        return PaginationResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .last(page.isLast())
            .first(page.isFirst())
            .build();
    }
}
```

---

## 🔄 12. Retry Mechanism

### Add to pom.xml
```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### Retry Configuration
```java
package com.bellagnech.dig_bank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
}
```

### Use Retry
```java
@Service
public class ExternalServiceClient {
    
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String callExternalService() {
        // External service call
        return result;
    }
    
    @Recover
    public String recover(Exception e) {
        return "Fallback response";
    }
}
```

---

## 📋 Summary

These code examples provide:
- ✅ Rate limiting
- ✅ Redis caching
- ✅ Circuit breakers
- ✅ Distributed tracing
- ✅ Email notifications
- ✅ Request/response logging
- ✅ API key authentication
- ✅ Custom metrics
- ✅ Security headers
- ✅ Audit logging
- ✅ Pagination utilities
- ✅ Retry mechanisms

**Next Steps**: 
1. Copy relevant code snippets
2. Add dependencies to pom.xml
3. Configure application.properties
4. Test each enhancement
5. Deploy incrementally

---

**Happy Coding! 🚀**


