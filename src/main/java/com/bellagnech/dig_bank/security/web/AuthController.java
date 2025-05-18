package com.bellagnech.dig_bank.security.web;

import com.bellagnech.dig_bank.entities.AppUser;
import com.bellagnech.dig_bank.security.dtos.AuthResponse;
import com.bellagnech.dig_bank.security.dtos.LoginRequest;
import com.bellagnech.dig_bank.security.dtos.RegisterRequest;
import com.bellagnech.dig_bank.security.exceptions.InvalidCredentialsException;
import com.bellagnech.dig_bank.security.jwt.JwtUtil;
import com.bellagnech.dig_bank.security.services.SecurityService;
import com.bellagnech.dig_bank.security.services.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling authentication operations
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityService securityService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "Authenticate user", description = "Authenticate with username and password to get JWT tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("Attempting to authenticate user: {}", loginRequest.getUsername());
            
            // Authenticate with Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            // Set authentication in context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generate tokens
            String accessToken = jwtUtil.generateToken(authentication);
            String refreshToken = jwtUtil.generateRefreshToken(authentication);
            
            // Extract roles for response
            List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
            
            log.info("User authenticated successfully: {}", loginRequest.getUsername());
            
            // Return authentication response
            return ResponseEntity.ok(AuthResponse.builder()
                .username(loginRequest.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(roles)
                .build());
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", loginRequest.getUsername(), e.getMessage());
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @Operation(summary = "Register new user", description = "Register a new user with username, email and password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getUsername());
        
        // Register user through service
        AppUser user = securityService.registerUser(registerRequest);
        
        // By default assign USER role
        securityService.addRoleToUser(user.getUsername(), "USER");
        log.info("Added USER role to newly registered user: {}", user.getUsername());
        
        // Authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                registerRequest.getUsername(),
                registerRequest.getPassword()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(authentication);
        String refreshToken = jwtUtil.generateRefreshToken(authentication);
        
        // Extract roles for response
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        
        log.info("User registered and authenticated successfully: {}", registerRequest.getUsername());
        
        // Return authentication response with 201 Created status
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder()
            .username(registerRequest.getUsername())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .roles(roles)
            .build());
    }

    @Operation(summary = "Refresh token", description = "Get a new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String refreshTokenHeader) {
        log.info("Token refresh requested");
        
        if (refreshTokenHeader != null && refreshTokenHeader.startsWith("Bearer ")) {
            String refreshToken = refreshTokenHeader.substring(7);
            
            if (jwtUtil.validateToken(refreshToken)) {
                String username = jwtUtil.extractUsername(refreshToken);
                log.info("Refreshing token for user: {}", username);
                
                // Reload the user and create a new authentication
                AppUser user = securityService.loadUserByUsername(username);
                
                // Create authentication with user roles
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username, 
                    null, 
                    user.getRoles().stream()
                        .map(role -> (GrantedAuthority) () -> "ROLE_" + role.getName())
                        .collect(Collectors.toList())
                );
                
                // Generate new access token
                String newAccessToken = jwtUtil.generateToken(authentication);
                
                // Get roles for response
                List<String> roles = user.getRoles().stream()
                    .map(role -> "ROLE_" + role.getName())
                    .collect(Collectors.toList());
                
                log.info("Token refreshed successfully for user: {}", username);
                
                // Return refreshed tokens
                return ResponseEntity.ok(AuthResponse.builder()
                    .username(username)
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Return the same refresh token
                    .roles(roles)
                    .build());
            }
        }
        
        log.warn("Invalid refresh token provided");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String tokenHeader) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().build();
    }
}
