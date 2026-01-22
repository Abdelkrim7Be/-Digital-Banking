package com.bellagnech.gateway.config;

import com.bellagnech.gateway.filter.JwtAuthenticationFilter;
import com.bellagnech.gateway.filter.RateLimitingFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder, 
            JwtAuthenticationFilter jwtFilter,
            RateLimitingFilter rateLimitingFilter) {
        return builder.routes()
            // Auth routes - no JWT required, but rate limited
            .route("auth-service", r -> r
                .path("/api/auth/**")
                .filters(f -> f.filter(rateLimitingFilter.apply(new RateLimitingFilter.Config())))
                .uri("lb://customer-service")
            )
            // Customer routes - JWT required + rate limited
            .route("customer-service", r -> r
                .path("/api/customers/**")
                .filters(f -> f
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://customer-service")
            )
            // Account routes - JWT required + rate limited
            .route("account-service", r -> r
                .path("/api/accounts/**")
                .filters(f -> f
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://account-service")
            )
            // Transaction routes - JWT required + rate limited
            .route("transaction-service", r -> r
                .path("/api/transactions/**")
                .filters(f -> f
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://transaction-service")
            )
            // Reporting routes - JWT required + rate limited
            .route("reporting-service", r -> r
                .path("/api/reports/**")
                .filters(f -> f
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://reporting-service")
            )
            // Notification routes - JWT required + rate limited
            .route("notification-service", r -> r
                .path("/api/notifications/**")
                .filters(f -> f
                    .filter(rateLimitingFilter.apply(new RateLimitingFilter.Config()))
                    .filter(jwtFilter.apply(new JwtAuthenticationFilter.Config()))
                )
                .uri("lb://notification-service")
            )
            .build();
    }
}

