package com.bellagnech.dig_bank.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@ConfigurationProperties(prefix = "spring.jwt.auth")
@Data
public class JwtProperties {
    private String app;
    private String secret;
    private long expires;
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
