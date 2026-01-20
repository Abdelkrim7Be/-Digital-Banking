package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.AuthResponse;
import com.bellagnech.dig_bank.dtos.LoginRequest;
import com.bellagnech.dig_bank.dtos.RegisterRequest;
import com.bellagnech.dig_bank.dtos.PasswordChangeRequest;
import com.bellagnech.dig_bank.dtos.ProfileUpdateRequest;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.services.AuthService;
import com.bellagnech.dig_bank.services.JwtService;
import com.bellagnech.dig_bank.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Register a new user", description = "Register a new user with ADMIN or CUSTOMER role")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for username: {}", request.getUsername());
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, null, e.getMessage()));
        }
    }

    @Operation(summary = "Authenticate user", description = "Authenticate user and return JWT token")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for username: {}", request.getUsername());
        try {
            AuthResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(new AuthResponse(null, null, null, null, "Invalid username or password"));
        }
    }

    @Operation(summary = "Refresh JWT token", description = "Refresh the current JWT token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken() {
        log.info("Token refresh requested");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, null, null, null, "Authentication required"));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String newToken = jwtService.generateToken(user);
            AuthResponse response = new AuthResponse(newToken, user.getUsername(), user.getEmail(), user.getRole());

            log.info("Token refreshed successfully for user: {}", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(new AuthResponse(null, null, null, null, "Token refresh failed"));
        }
    }

    @Operation(summary = "Change password", description = "Change user password")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid current password or validation error")
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change requested");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Authentication required"));
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Validate password confirmation
            if (!request.isPasswordConfirmed()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "New password and confirmation do not match"));
            }

            // Verify current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Current password is incorrect"));
            }

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            log.info("Password changed successfully for user: {}", username);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            log.error("Password change failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password change failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Get user profile", description = "Get current user profile information")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        log.info("Profile request");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Profile request without authentication");
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Authentication required"));
            }

            String username = authentication.getName();
            log.info("Getting profile for user: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("role", user.getRole());
            profile.put("enabled", user.isEnabled());
            profile.put("createdAt", user.getCreatedDate());
            profile.put("updatedAt", user.getLastModifiedDate());

            log.info("Profile retrieved successfully for user: {}", username);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Profile retrieval failed: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Profile retrieval failed: " + e.getMessage()));
        }
    }

    @Operation(summary = "Update user profile", description = "Update current user profile information")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        log.info("Profile update requested");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Profile update request without authentication");
                return ResponseEntity.status(401)
                        .body(Map.of("message", "Authentication required"));
            }

            String username = authentication.getName();
            log.info("Updating profile for user: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            boolean updated = false;

            // Update email if provided and different
            if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Email already exists"));
                }
                user.setEmail(request.getEmail());
                updated = true;
            }

            // Update firstName if provided
            if (request.getFirstName() != null && !request.getFirstName().equals(user.getFirstName())) {
                user.setFirstName(request.getFirstName());
                updated = true;
            }

            // Update lastName if provided
            if (request.getLastName() != null && !request.getLastName().equals(user.getLastName())) {
                user.setLastName(request.getLastName());
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
                log.info("Profile updated successfully for user: {}", username);
            } else {
                log.info("No changes detected for user profile: {}", username);
            }

            // Return updated profile data
            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("role", user.getRole());
            profile.put("enabled", user.isEnabled());
            profile.put("message", "Profile updated successfully");

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Profile update failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Profile update failed: " + e.getMessage()));
        }
    }
}
