package com.bellagnech.dig_bank.security.web;

import com.bellagnech.dig_bank.entities.AppUser;
import com.bellagnech.dig_bank.security.dtos.PasswordChangeRequest;
import com.bellagnech.dig_bank.security.dtos.UserProfileDTO;
import com.bellagnech.dig_bank.security.exceptions.InvalidCredentialsException;
import com.bellagnech.dig_bank.security.services.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

/**
 * Controller for user profile management operations
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile Management", description = "Endpoints for managing user profile")
public class UserController {

    private final SecurityService securityService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Operation(summary = "Get user profile", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Retrieving profile for user: {}", username);
        
        AppUser user = securityService.loadUserByUsername(username);
        
        UserProfileDTO profileDTO = mapToUserProfileDTO(user);
        
        return ResponseEntity.ok(profileDTO);
    }
    
    @Operation(summary = "Update user profile", description = "Updates the email of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid profile data")
    })
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> updateUserProfile(@Valid @RequestBody UserProfileDTO profileDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Updating profile for user: {}", username);
        
        // Only allow users to update their own profile
        if (!username.equals(profileDTO.getUsername())) {
            log.warn("User {} attempted to update profile for {}", username, profileDTO.getUsername());
            throw new InvalidCredentialsException("You can only update your own profile");
        }
        
        AppUser user = securityService.loadUserByUsername(username);
        
        // Only allow email update for security reasons
        user.setEmail(profileDTO.getEmail());
        
        // Save updated user
        securityService.updateUser(user);
        
        UserProfileDTO updatedProfile = mapToUserProfileDTO(user);
        
        return ResponseEntity.ok(updatedProfile);
    }
    
    @Operation(summary = "Change password", description = "Changes the password of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid password data")
    })
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Password change requested for user: {}", username);
        
        // Validate password confirmation
        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            log.warn("Password change failed for user {}: passwords do not match", username);
            throw new InvalidCredentialsException("New password and confirmation password do not match");
        }
        
        // Change password
        boolean changed = securityService.changePassword(
            username, 
            passwordChangeRequest.getCurrentPassword(), 
            passwordChangeRequest.getNewPassword()
        );
        
        if (!changed) {
            log.warn("Password change failed for user {}", username);
            throw new InvalidCredentialsException("Failed to change password");
        }
        
        log.info("Password changed successfully for user: {}", username);
        
        return ResponseEntity.ok().build();
    }
    
    /**
     * Maps an AppUser entity to a UserProfileDTO
     */
    private UserProfileDTO mapToUserProfileDTO(AppUser user) {
        return UserProfileDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .enabled(user.isEnabled())
            .roles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList()))
            .createdBy(user.getCreatedBy())
            .createdDate(user.getCreatedDate() != null ? dateFormat.format(user.getCreatedDate()) : null)
            .lastModifiedBy(user.getLastModifiedBy())
            .lastModifiedDate(user.getLastModifiedDate() != null ? dateFormat.format(user.getLastModifiedDate()) : null)
            .build();
    }
}
