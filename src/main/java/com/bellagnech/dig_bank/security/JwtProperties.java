package com.bellagnech.dig_bank.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "jwt.auth")
@Data
public class JwtProperties {
    private String app;
    private String secret;
    private long expires;
}
