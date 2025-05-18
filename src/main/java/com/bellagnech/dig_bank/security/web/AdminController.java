package com.bellagnech.dig_bank.security.web;

import com.bellagnech.dig_bank.entities.AppRole;
import com.bellagnech.dig_bank.entities.AppUser;
import com.bellagnech.dig_bank.repositories.AppRoleRepository;
import com.bellagnech.dig_bank.security.dtos.UserProfileDTO;
import com.bellagnech.dig_bank.security.exceptions.RoleNotFoundException;
import com.bellagnech.dig_bank.security.exceptions.UserNotFoundException;
import com.bellagnech.dig_bank.security.services.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for administrative user management operations
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin User Management", description = "Administrative endpoints for managing users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SecurityService securityService;
    private final AppRoleRepository roleRepository;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Operation(summary = "Get all users", description = "Retrieves all users in the system (Admin only)")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping("/users")
    public ResponseEntity<List<UserProfileDTO>> getAllUsers() {
        log.info("Retrieving all users");
        
        List<AppUser> users = securityService.getAllUsers();
        
        List<UserProfileDTO> userDTOs = users.stream()
            .map(this::mapToUserProfileDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDTOs);
    }
    
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user by ID (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserProfileDTO> getUserById(@PathVariable Long userId) {
        log.info("Retrieving user with ID: {}", userId);
        
        AppUser user = securityService.getUserById(userId);
        
        return ResponseEntity.ok(mapToUserProfileDTO(user));
    }
    
    @Operation(summary = "Add role to user", description = "Assigns a role to a specific user (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
        @ApiResponse(responseCode = "404", description = "User or role not found")
    })
    @PostMapping("/users/{username}/roles/{roleName}")
    public ResponseEntity<Void> addRoleToUser(
            @PathVariable String username, 
            @PathVariable String roleName) {
        log.info("Adding role {} to user {}", roleName, username);
        
        // Check if role exists
        if (!roleRepository.existsByName(roleName)) {
            throw new RoleNotFoundException("Role not found: " + roleName);
        }
        
        securityService.addRoleToUser(username, roleName);
        
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Remove role from user", description = "Removes a role from a specific user (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role removed successfully"),
        @ApiResponse(responseCode = "404", description = "User or role not found")
    })
    @DeleteMapping("/users/{username}/roles/{roleName}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable String username, 
            @PathVariable String roleName) {
        log.info("Removing role {} from user {}", roleName, username);
        
        // Check if role exists
        if (!roleRepository.existsByName(roleName)) {
            throw new RoleNotFoundException("Role not found: " + roleName);
        }
        
        securityService.removeRoleFromUser(username, roleName);
        
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Enable or disable user", description = "Changes the enabled status of a user (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/users/{username}/enabled")
    public ResponseEntity<Void> setUserEnabled(
            @PathVariable String username, 
            @RequestParam boolean enabled) {
        log.info("Setting user {} enabled status to {}", username, enabled);
        
        securityService.setUserEnabled(username, enabled);
        
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Get available roles", description = "Retrieves all available roles in the system (Admin only)")
    @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        log.info("Retrieving all available roles");
        
        List<String> roles = roleRepository.findAll().stream()
            .map(AppRole::getName)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(roles);
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
