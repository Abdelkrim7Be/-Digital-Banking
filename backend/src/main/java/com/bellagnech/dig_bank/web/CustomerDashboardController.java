package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.AccountOperationDTO;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.AccountOperation;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.UserRepository;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.services.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Dashboard", description = "APIs for customer dashboard and account management")
public class CustomerDashboardController {

    private final BankAccountService bankAccountService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;

    @Operation(summary = "Get customer dashboard data", description = "Get dashboard overview for the authenticated customer")
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - CUSTOMER role required")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getCustomerDashboard() {
        log.info("Customer dashboard requested");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("Dashboard - Authentication: {}", authentication);
            log.info("Dashboard - Principal: {}", authentication.getPrincipal());
            log.info("Dashboard - Authorities: {}", authentication.getAuthorities());
            String username = authentication.getName();
            log.info("Dashboard - Username: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Find customer by user relationship
            Customer customer = customerRepository.findByUser(user).orElse(null);

            Map<String, Object> dashboard = new HashMap<>();

            if (customer != null) {
                // Get customer accounts
                List<BankAccountDTO> accounts = bankAccountService.getCustomerAccounts(customer.getId());

                // Calculate totals
                double totalBalance = accounts.stream()
                        .mapToDouble(BankAccountDTO::getBalance)
                        .sum();

                long totalAccounts = accounts.size();
                long currentAccounts = accounts.stream()
                        .filter(acc -> acc.getType().equals("CURRENT"))
                        .count();
                long savingAccounts = accounts.stream()
                        .filter(acc -> acc.getType().equals("SAVING"))
                        .count();

                dashboard.put("totalBalance", totalBalance);
                dashboard.put("totalAccounts", totalAccounts);
                dashboard.put("currentAccounts", currentAccounts);
                dashboard.put("savingAccounts", savingAccounts);
                dashboard.put("accounts", accounts);
                dashboard.put("customerName", customer.getName());
                dashboard.put("customerEmail", customer.getEmail());
            } else {
                dashboard.put("totalBalance", 0.0);
                dashboard.put("totalAccounts", 0);
                dashboard.put("currentAccounts", 0);
                dashboard.put("savingAccounts", 0);
                dashboard.put("accounts", List.of());
                dashboard.put("customerName", user.getUsername());
                dashboard.put("customerEmail", user.getEmail());
            }

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error retrieving customer dashboard: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to retrieve dashboard data"));
        }
    }

    @Operation(summary = "Get customer accounts", description = "Get all accounts for the authenticated customer")
    @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BankAccountDTO>> getCustomerAccounts() {
        log.info("Customer accounts requested");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Find customer by user relationship
            Customer customer = customerRepository.findByUser(user).orElse(null);

            if (customer != null) {
                List<BankAccountDTO> accounts = bankAccountService.getCustomerAccounts(customer.getId());
                return ResponseEntity.ok(accounts);
            } else {
                return ResponseEntity.ok(List.of());
            }
        } catch (Exception e) {
            log.error("Error retrieving customer accounts: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Get customer transactions", description = "Get transaction history for the authenticated customer")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Map<String, Object>> getCustomerTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Customer transactions requested - page: {}, size: {}", page, size);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Find customer by user relationship
            Customer customer = customerRepository.findByUser(user).orElse(null);

            Map<String, Object> result = new HashMap<>();

            if (customer != null) {
                // Get all accounts for the customer
                List<BankAccount> customerAccounts = bankAccountRepository.findByCustomerId(customer.getId());
                List<String> accountIds = customerAccounts.stream()
                        .map(BankAccount::getId)
                        .collect(Collectors.toList());

                if (!accountIds.isEmpty()) {
                    // Get transactions for all customer accounts
                    Pageable pageable = PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "operationDate"));

                    Page<AccountOperation> transactionPage =
                        accountOperationRepository.findByBankAccountIdIn(accountIds, pageable);

                    // Convert to response format
                    List<Map<String, Object>> transactions = transactionPage.getContent().stream()
                        .map(this::mapTransactionToResponse)
                        .collect(Collectors.toList());

                    result.put("transactions", transactions);
                    result.put("totalElements", transactionPage.getTotalElements());
                    result.put("totalPages", transactionPage.getTotalPages());
                } else {
                    result.put("transactions", List.of());
                    result.put("totalElements", 0);
                    result.put("totalPages", 0);
                }

                result.put("customerId", customer.getId());
                result.put("customerName", customer.getName());
            } else {
                result.put("transactions", List.of());
                result.put("totalElements", 0);
                result.put("totalPages", 0);
            }

            result.put("currentPage", page);
            result.put("size", size);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error retrieving customer transactions: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to retrieve transactions"));
        }
    }

    private Map<String, Object> mapTransactionToResponse(AccountOperation operation) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", operation.getId());
        response.put("operationDate", operation.getOperationDate());
        response.put("accountId", operation.getBankAccount().getId());
        response.put("amount", operation.getAmount());
        response.put("type", operation.getType().name());
        response.put("description", operation.getDescription());
        response.put("performedBy", operation.getPerformedBy());
        response.put("accountBalance", operation.getBankAccount().getBalance());

        // Add customer information
        if (operation.getBankAccount().getCustomer() != null) {
            Map<String, Object> customer = new HashMap<>();
            customer.put("id", operation.getBankAccount().getCustomer().getId());
            customer.put("name", operation.getBankAccount().getCustomer().getName());
            customer.put("email", operation.getBankAccount().getCustomer().getEmail());
            response.put("customer", customer);
        }

        return response;
    }
}
