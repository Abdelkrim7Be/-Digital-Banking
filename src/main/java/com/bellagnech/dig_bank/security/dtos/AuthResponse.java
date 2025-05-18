package com.bellagnech.dig_bank.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for authentication responses
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String username;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private List<String> roles;
}
