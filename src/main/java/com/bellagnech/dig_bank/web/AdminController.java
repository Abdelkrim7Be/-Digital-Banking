package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.enums.Role;
import com.bellagnech.dig_bank.repositories.UserRepository;
import com.bellagnech.dig_bank.services.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Management", description = "APIs for admin operations - ADMIN role required")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final BankAccountService bankAccountService;
    private final UserRepository userRepository;

    @Operation(summary = "Get all users", description = "Retrieve all users in the system (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Admin requesting all users");
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get all customers", description = "Retrieve all customers (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        log.info("Admin requesting all customers");
        List<CustomerDTO> customers = bankAccountService.listCustomersDTO();
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get users by role", description = "Retrieve users by specific role (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users")
    @GetMapping("/users/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUsersByRole(@PathVariable Role role) {
        log.info("Admin requesting users with role: {}", role);
        List<User> users = userRepository.findByRole(role);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Enable/Disable user", description = "Enable or disable a user account (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "User status updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateUserStatus(@PathVariable Long userId, @RequestParam boolean enabled) {
        log.info("Admin updating user {} status to: {}", userId, enabled);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEnabled(enabled);
        userRepository.save(user);
        
        String message = enabled ? "User enabled successfully" : "User disabled successfully";
        return ResponseEntity.ok(message);
    }
}
