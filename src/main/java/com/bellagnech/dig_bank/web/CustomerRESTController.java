package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.services.BankAccountService;
import com.bellagnech.dig_bank.security.services.SecurityService;
import com.bellagnech.dig_bank.entities.AppUser;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing bank customers")
public class CustomerRESTController {
    private BankAccountService bankAccountService;
    private SecurityService securityService;
    
    @Operation(summary = "Get all customers", description = "Retrieves a list of all customers in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer list")
    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public List<CustomerDTO> customers() {
        String currentUsername = getCurrentUsername();
        log.info("User {} is requesting all customers", currentUsername);
        return bankAccountService.listCustomersByUser(currentUsername);
    }
    
    @Operation(summary = "Get customer by ID", description = "Retrieves a specific customer by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customer"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/customers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public CustomerDTO getCustomer(@PathVariable (name = "id") Long customerId) throws CustomerNotFoundException {
        String currentUsername = getCurrentUsername();
        log.info("User {} is requesting customer with ID: {}", currentUsername, customerId);
        return bankAccountService.getCustomerForUser(customerId, currentUsername);
    }

    @Operation(summary = "Create new customer", description = "Creates a new customer in the system")
    @ApiResponse(responseCode = "200", description = "Customer successfully created")
    @PostMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public CustomerDTO saveCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        String username = getCurrentUsername();
        log.info("User {} is creating a new customer", username);
        
        // Associate customer with current user
        return bankAccountService.saveCustomerForUser(customerDTO, username);
    }

    @Operation(summary = "Update customer", description = "Updates an existing customer's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer successfully updated"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public CustomerDTO updateCustomer(@PathVariable Long customerId, @Valid @RequestBody CustomerDTO customerDTO) {
        String username = getCurrentUsername();
        log.info("User {} is updating customer with ID: {}", username, customerId);
        
        customerDTO.setId(customerId);
        return bankAccountService.updateCustomerForUser(customerDTO, username);
    }

    @Operation(summary = "Delete customer", description = "Removes a customer from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer successfully deleted"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/customers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCustomer(@PathVariable Long id) {
        String username = getCurrentUsername();
        log.info("User {} is deleting customer with ID: {}", username, id);
        
        // Check if customer belongs to user (for non-admin users)
        if (!bankAccountService.customerBelongsToUser(id, username)) {
            throw new com.bellagnech.dig_bank.exceptions.AccessDeniedException("You don't have permission to delete this customer");
        }
        
        bankAccountService.deleteCustomer(id);
    }

    @Operation(summary = "Get paginated customers", description = "Retrieves a paginated list of customers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer page")
    @GetMapping("/customers/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public Page<CustomerDTO> getCustomersPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        String currentUsername = getCurrentUsername();
        log.info("User {} is requesting customers page {} with size {}", currentUsername, page, size);
        return bankAccountService.getCustomersPageableByUser(currentUsername, page, size);
    }

    @Operation(summary = "Search customers", description = "Search customers by name or email")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching customers")
    @GetMapping("/customers/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public Page<CustomerDTO> searchCustomers(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        String currentUsername = getCurrentUsername();
        log.info("User {} is searching for customers with keyword: {}", currentUsername, keyword);
        return bankAccountService.searchCustomersByUser(currentUsername, keyword, page, size);
    }
    
    /**
     * Get the username of the currently authenticated user
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }
}