package com.bellagnech.dig_bank.security.jwt;

import com.bellagnech.dig_bank.security.dtos.AuthResponse;
import com.bellagnech.dig_bank.security.dtos.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Filter for handling JWT authentication
 * This filter intercepts the /login endpoint, authenticates users, and generates JWT tokens
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * Attempt to authenticate the user with provided credentials
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            // Parse the login request from request body
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            log.info("Authentication attempt for user: {}", loginRequest.getUsername());
            
            // Create authentication token with credentials
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                );
            
            // Authenticate using the authentication manager
            return authenticationManager.authenticate(authenticationToken);
        } catch (IOException e) {
            log.error("Failed to parse authentication request", e);
            throw new RuntimeException("Failed to parse authentication request", e);
        }
    }

    /**
     * Handle successful authentication by generating JWT tokens
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult) 
                                            throws IOException, ServletException {
        User user = (User) authResult.getPrincipal();
        log.info("User authenticated successfully: {}", user.getUsername());
        
        // Generate access and refresh tokens
        String accessToken = jwtUtil.generateToken(authResult);
        String refreshToken = jwtUtil.generateRefreshToken(authResult);
        
        // Extract user roles
        var roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // Build authentication response
        AuthResponse authResponse = AuthResponse.builder()
                .username(user.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .roles(roles)
                .build();
        
        // Set response content type and write response
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(authResponse));
    }

    /**
     * Handle authentication failure
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, 
                                             AuthenticationException failed) 
                                             throws IOException, ServletException {
        log.error("Authentication failed: {}", failed.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        // Create error response
        var errorDetails = new ErrorResponse(
            "Authentication failed", 
            failed.getMessage()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
    
    /**
     * Simple error response class for authentication failures
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorResponse {
        private String error;
        private String message;
    }
}
