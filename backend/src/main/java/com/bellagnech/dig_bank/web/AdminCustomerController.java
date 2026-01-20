package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.RegisterRequest;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.AccountOperation;
import com.bellagnech.dig_bank.enums.Role;
import com.bellagnech.dig_bank.repositories.UserRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Customer Management", description = "APIs for admin customer operations - ADMIN role required")
@SecurityRequirement(name = "bearerAuth")
public class AdminCustomerController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountOperationRepository accountOperationRepository;

    @Operation(summary = "Get all customers with pagination", description = "Retrieve all customers with optional pagination and filtering (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search,
            Authentication authentication) {

        log.info("Admin requesting customers - page: {}, size: {}, sortBy: {}, sortOrder: {}, search: {}",
                page, size, sortBy, sortOrder, search);
        log.info("Authentication: {}, Principal: {}, Authorities: {}",
                authentication != null ? authentication.getName() : "null",
                authentication != null ? authentication.getPrincipal() : "null",
                authentication != null ? authentication.getAuthorities() : "null");

        try {
            log.info("Creating sort and pageable objects...");

            // Handle sorting with null values
            Sort sort;
            if (sortOrder.equalsIgnoreCase("desc")) {
                sort = Sort.by(Sort.Order.desc(sortBy).nullsLast());
            } else {
                sort = Sort.by(Sort.Order.asc(sortBy).nullsLast());
            }

            Pageable pageable = PageRequest.of(page, size, sort);
            log.info("Sort: {}, Pageable: {}", sort, pageable);

            Page<User> usersPage;
            if (search != null && !search.trim().isEmpty()) {
                log.info("Searching with term: {}", search);
                usersPage = userRepository.findByRoleAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    Role.CUSTOMER, search, search, pageable);
            } else {
                log.info("Finding all customers with role: {}", Role.CUSTOMER);
                // Try simple query first
                List<User> allCustomers = userRepository.findByRole(Role.CUSTOMER);
                log.info("Found {} customers without pagination", allCustomers.size());

                // Create a manual page
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), allCustomers.size());
                List<User> pageContent = allCustomers.subList(start, end);
                usersPage = new PageImpl<>(pageContent, pageable, allCustomers.size());
            }
            log.info("Query completed, found {} users", usersPage.getTotalElements());

            // Convert to DTOs to avoid circular reference issues
            List<Map<String, Object>> customerDTOs = usersPage.getContent().stream()
                .map(user -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", user.getId());
                    dto.put("username", user.getUsername());
                    dto.put("email", user.getEmail());
                    dto.put("firstName", user.getFirstName());
                    dto.put("lastName", user.getLastName());
                    dto.put("enabled", user.isEnabled());
                    dto.put("createdDate", user.getCreatedDate());
                    dto.put("role", user.getRole().name());
                    return dto;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", customerDTOs);
            response.put("totalElements", usersPage.getTotalElements());
            response.put("totalPages", usersPage.getTotalPages());
            response.put("currentPage", usersPage.getNumber());
            response.put("size", usersPage.getSize());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving customers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve customers"));
        }
    }

    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by ID (ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getCustomerById(@PathVariable Long id) {
        log.info("Admin requesting customer with ID: {}", id);

        try {
            User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error retrieving customer {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Create new customer", description = "Create a new customer with user account (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Customer created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or customer already exists")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createCustomer(@Valid @RequestBody RegisterRequest request) {
        log.info("Admin creating new customer: {}", request.getUsername());

        try {
            // Set role to CUSTOMER
            request.setRole(Role.CUSTOMER);

            // Register the user through AuthService
            authService.register(request);

            // Find the created user
            User createdUser = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Failed to create customer"));

            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            log.error("Customer creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating customer: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update customer", description = "Update customer information (ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateCustomer(@PathVariable Long id, @Valid @RequestBody RegisterRequest request) {
        log.info("Admin updating customer with ID: {}", id);

        try {
            User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Update user fields (excluding password and username)
            user.setEmail(request.getEmail());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            User updatedUser = userRepository.save(user);

            // Update associated customer if exists
            Customer customer = customerRepository.findByUser(user).orElse(null);
            if (customer != null && request.getName() != null) {
                customer.setName(request.getName());
                customer.setEmail(request.getEmail());
                customerRepository.save(customer);
            }

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error updating customer {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete customer", description = "Delete a customer and associated data (ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete customer with existing accounts"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("Admin deleting customer with ID: {}", id);

        try {
            User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Check if customer has associated accounts
            Customer customer = customerRepository.findByUser(user).orElse(null);
            if (customer != null && !customer.getBankAccounts().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Delete customer first, then user
            if (customer != null) {
                customerRepository.delete(customer);
            }
            userRepository.delete(user);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting customer {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update customer status", description = "Enable or disable customer account (ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateCustomerStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> statusUpdate) {
        log.info("Admin updating customer {} status", id);

        try {
            User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            Boolean enabled = statusUpdate.get("enabled");
            if (enabled != null) {
                user.setEnabled(enabled);
                User updatedUser = userRepository.save(user);
                return ResponseEntity.ok(updatedUser);
            }

            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating customer {} status: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get customer accounts", description = "Get all accounts for a specific customer (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer accounts")
    @GetMapping("/{id}/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCustomerAccounts(@PathVariable Long id) {
        log.info("Admin requesting accounts for customer: {}", id);

        try {
            User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            Customer customer = customerRepository.findByUser(user).orElse(null);
            if (customer != null) {
                // Convert bank accounts to DTOs
                List<Map<String, Object>> accounts = customer.getBankAccounts().stream()
                        .map(account -> {
                            Map<String, Object> accountInfo = new HashMap<>();
                            accountInfo.put("id", account.getId());
                            accountInfo.put("balance", account.getBalance());
                            accountInfo.put("status", account.getStatus());
                            accountInfo.put("createdDate", account.getCreateDate());
                            accountInfo.put("type", account.getClass().getSimpleName().replace("Account", "").toUpperCase());
                            return accountInfo;
                        })
                        .toList();
                return ResponseEntity.ok(accounts);
            }

            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            log.error("Error retrieving accounts for customer {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get customer transactions", description = "Get all transactions for a specific customer (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer transactions")
    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCustomerTransactions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type) {

        log.info("Admin requesting transactions for customer: {}", id);

        try {
            User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            Customer customer = customerRepository.findByUser(user).orElse(null);
            if (customer == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("content", Collections.emptyList());
                response.put("totalElements", 0);
                response.put("totalPages", 0);
                response.put("customerId", id);
                return ResponseEntity.ok(response);
            }

            // Get all accounts for the customer
            List<String> accountIds = customer.getBankAccounts().stream()
                .map(BankAccount::getId)
                .collect(Collectors.toList());

            if (accountIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("content", Collections.emptyList());
                response.put("totalElements", 0);
                response.put("totalPages", 0);
                response.put("customerId", id);
                return ResponseEntity.ok(response);
            }

            Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "operationDate"));

            Page<AccountOperation> transactionPage =
                accountOperationRepository.findByBankAccountIdIn(accountIds, pageable);

            List<Map<String, Object>> transactions = transactionPage.getContent().stream()
                .map(operation -> {
                    Map<String, Object> transactionData = new HashMap<>();
                    transactionData.put("id", operation.getId());
                    transactionData.put("type", operation.getType().name());
                    transactionData.put("amount", operation.getAmount());
                    transactionData.put("description", operation.getDescription());
                    transactionData.put("operationDate", operation.getOperationDate());
                    transactionData.put("performedBy", operation.getPerformedBy());
                    transactionData.put("accountId", operation.getBankAccount().getId());
                    transactionData.put("accountBalance", operation.getBankAccount().getBalance());
                    return transactionData;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", transactions);
            response.put("totalElements", transactionPage.getTotalElements());
            response.put("totalPages", transactionPage.getTotalPages());
            response.put("size", transactionPage.getSize());
            response.put("number", transactionPage.getNumber());
            response.put("customerId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving transactions for customer: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve customer transactions");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Get customer statistics", description = "Get statistics about customers (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer statistics")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCustomerStats() {
        log.info("Admin requesting customer statistics");

        try {
            long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
            long activeCustomers = userRepository.countByRoleAndEnabled(Role.CUSTOMER, true);
            long inactiveCustomers = totalCustomers - activeCustomers;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCustomers", totalCustomers);
            stats.put("activeCustomers", activeCustomers);
            stats.put("inactiveCustomers", inactiveCustomers);
            stats.put("suspendedCustomers", 0); // Implement if you have suspension logic
            stats.put("newCustomersThisMonth", 0); // Implement based on creation date
            stats.put("totalBalance", 0.0); // Implement by summing all account balances

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving customer statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve statistics"));
        }
    }

    @Operation(summary = "Bulk update customer status", description = "Update status for multiple customers (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Customers status updated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @PatchMapping("/bulk/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> bulkUpdateCustomerStatus(@RequestBody Map<String, Object> request) {
        log.info("Admin bulk updating customer status");

        try {
            @SuppressWarnings("unchecked")
            List<Long> customerIds = (List<Long>) request.get("customerIds");
            Boolean enabled = (Boolean) request.get("enabled");

            if (customerIds == null || enabled == null) {
                return ResponseEntity.badRequest().build();
            }

            List<User> updatedCustomers = customerIds.stream()
                .map(id -> {
                    try {
                        User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                        user.setEnabled(enabled);
                        return userRepository.save(user);
                    } catch (Exception e) {
                        log.error("Error updating customer {}: {}", id, e.getMessage());
                        return null;
                    }
                })
                .filter(user -> user != null)
                .toList();

            return ResponseEntity.ok(updatedCustomers);
        } catch (Exception e) {
            log.error("Error in bulk status update: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Bulk delete customers", description = "Delete multiple customers (ADMIN only)")
    @ApiResponse(responseCode = "204", description = "Customers deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> bulkDeleteCustomers(@RequestBody Map<String, List<Long>> request) {
        log.info("Admin bulk deleting customers");

        try {
            List<Long> customerIds = request.get("customerIds");
            if (customerIds == null || customerIds.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            for (Long id : customerIds) {
                try {
                    User user = userRepository.findByIdAndRole(id, Role.CUSTOMER)
                            .orElseThrow(() -> new RuntimeException("Customer not found"));

                    // Check if customer has associated accounts
                    Customer customer = customerRepository.findByUser(user).orElse(null);
                    if (customer != null && !customer.getBankAccounts().isEmpty()) {
                        log.warn("Cannot delete customer {} - has existing accounts", id);
                        continue;
                    }

                    // Delete customer first, then user
                    if (customer != null) {
                        customerRepository.delete(customer);
                    }
                    userRepository.delete(user);
                } catch (Exception e) {
                    log.error("Error deleting customer {}: {}", id, e.getMessage());
                }
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error in bulk delete: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Export customers to CSV", description = "Export customers data to CSV format (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "CSV file generated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        log.info("Admin exporting customers to CSV");

        try {
            List<User> customers;
            if (search != null && !search.trim().isEmpty()) {
                customers = userRepository.findByRoleAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    Role.CUSTOMER, search, search);
            } else {
                customers = userRepository.findByRole(Role.CUSTOMER);
            }

            // Filter by status if provided
            if (status != null && !status.trim().isEmpty()) {
                boolean enabled = "ACTIVE".equalsIgnoreCase(status);
                customers = customers.stream()
                    .filter(customer -> customer.isEnabled() == enabled)
                    .toList();
            }

            // Generate CSV content
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Username,Email,First Name,Last Name,Status,Created Date,Account Count,Total Balance\n");

            for (User customer : customers) {
                // Get customer account info
                Customer customerEntity = customerRepository.findByUser(customer).orElse(null);
                int accountCount = 0;
                double totalBalance = 0.0;

                if (customerEntity != null && customerEntity.getBankAccounts() != null) {
                    accountCount = customerEntity.getBankAccounts().size();
                    totalBalance = customerEntity.getBankAccounts().stream()
                        .mapToDouble(account -> account.getBalance())
                        .sum();
                }

                csv.append(customer.getId()).append(",")
                   .append(escapeCSV(customer.getUsername())).append(",")
                   .append(escapeCSV(customer.getEmail())).append(",")
                   .append(escapeCSV(customer.getFirstName() != null ? customer.getFirstName() : "")).append(",")
                   .append(escapeCSV(customer.getLastName() != null ? customer.getLastName() : "")).append(",")
                   .append(customer.isEnabled() ? "ACTIVE" : "INACTIVE").append(",")
                   .append(customer.getCreatedDate()).append(",")
                   .append(accountCount).append(",")
                   .append(String.format("%.2f", totalBalance)).append("\n");
            }

            byte[] csvBytes = csv.toString().getBytes("UTF-8");

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv; charset=UTF-8")
                    .header("Content-Disposition", "attachment; filename=customers.csv")
                    .body(csvBytes);
        } catch (Exception e) {
            log.error("Error exporting customers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}