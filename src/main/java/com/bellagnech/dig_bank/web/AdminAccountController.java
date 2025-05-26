package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CreateAccountRequest;
import com.bellagnech.dig_bank.dtos.CurrentBankAccountDTO;
import com.bellagnech.dig_bank.dtos.SavingBankAccountDTO;
import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.entities.AccountOperation;
import com.bellagnech.dig_bank.enums.AccountStatus;
import com.bellagnech.dig_bank.enums.Role;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.UserRepository;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.services.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Account Management", description = "APIs for admin account operations - ADMIN role required")
@SecurityRequirement(name = "bearerAuth")
public class AdminAccountController {

    private final BankAccountService bankAccountService;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final AccountOperationRepository accountOperationRepository;

    @Operation(summary = "Get all accounts with pagination", description = "Retrieve all bank accounts with optional pagination and filtering (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        log.info("Admin requesting accounts - page: {}, size: {}, sortBy: {}, sortOrder: {}, search: {}, status: {}",
                page, size, sortBy, sortOrder, search, status);

        try {
            List<BankAccountDTO> allAccounts = bankAccountService.bankAccountList();

            // Filter by search term if provided
            if (search != null && !search.trim().isEmpty()) {
                allAccounts = allAccounts.stream()
                    .filter(account ->
                        account.getId().toLowerCase().contains(search.toLowerCase()) ||
                        (account.getCustomerDTO() != null &&
                         account.getCustomerDTO().getName().toLowerCase().contains(search.toLowerCase())))
                    .toList();
            }

            // Filter by status if provided
            if (status != null && !status.trim().isEmpty()) {
                allAccounts = allAccounts.stream()
                    .filter(account -> account.getStatus().toString().equalsIgnoreCase(status))
                    .toList();
            }

            // Manual pagination
            int totalElements = allAccounts.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, totalElements);

            List<BankAccountDTO> paginatedAccounts = allAccounts.subList(startIndex, endIndex);

            Map<String, Object> response = new HashMap<>();
            response.put("content", paginatedAccounts);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("size", size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving accounts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve accounts"));
        }
    }

    @Operation(summary = "Get account by ID", description = "Retrieve a specific account by ID (ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankAccountDTO> getAccountById(@PathVariable String id) {
        log.info("Admin requesting account with ID: {}", id);

        try {
            BankAccountDTO account = bankAccountService.getBankAccount(id);
            return ResponseEntity.ok(account);
        } catch (BankAccountNotFoundException e) {
            log.error("Account not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving account {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Create current account", description = "Create a new current account for a customer (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Current account created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @PostMapping("/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CurrentBankAccountDTO> createCurrentAccount(
            @RequestParam double initialBalance,
            @RequestParam double overDraft,
            @RequestParam Long customerId) {

        log.info("Admin creating current account for customer ID: {} with balance: {} and overdraft: {}",
                customerId, initialBalance, overDraft);

        try {
            CurrentBankAccountDTO account = bankAccountService.saveCurrentBankAccount(initialBalance, overDraft, customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", customerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error creating current account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Create saving account", description = "Create a new saving account for a customer (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Saving account created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @PostMapping("/saving")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SavingBankAccountDTO> createSavingAccount(
            @RequestParam double initialBalance,
            @RequestParam double interestRate,
            @RequestParam Long customerId) {

        log.info("Admin creating saving account for customer ID: {} with balance: {} and interest rate: {}",
                customerId, initialBalance, interestRate);

        try {
            SavingBankAccountDTO account = bankAccountService.saveSavingBankAccount(initialBalance, interestRate, customerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", customerId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error creating saving account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Create account", description = "Create a new bank account for a customer (ADMIN only)")
    @ApiResponse(responseCode = "201", description = "Account created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Customer not found")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankAccountDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Admin creating {} account for customer ID: {}", request.getAccountType(), request.getCustomerId());

        try {
            BankAccountDTO account;
            if ("CURRENT".equalsIgnoreCase(request.getAccountType())) {
                CurrentBankAccountDTO currentAccount = bankAccountService.saveCurrentBankAccount(
                    request.getInitialBalance(), request.getOverdraft(), request.getCustomerId());
                account = currentAccount;
            } else if ("SAVING".equalsIgnoreCase(request.getAccountType())) {
                SavingBankAccountDTO savingAccount = bankAccountService.saveSavingBankAccount(
                    request.getInitialBalance(), request.getInterestRate(), request.getCustomerId());
                account = savingAccount;
            } else {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (CustomerNotFoundException e) {
            log.error("Customer not found: {}", request.getCustomerId());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error creating account: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Update account status", description = "Update the status of a bank account (ADMIN only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BankAccountDTO> updateAccountStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) {

        log.info("Admin updating account {} status", id);

        try {
            String statusStr = statusUpdate.get("status");
            if (statusStr != null) {
                AccountStatus status = AccountStatus.valueOf(statusStr.toUpperCase());
                bankAccountService.updateAccountStatus(id, status);
                BankAccountDTO updatedAccount = bankAccountService.getBankAccount(id);
                return ResponseEntity.ok(updatedAccount);
            }

            return ResponseEntity.badRequest().build();
        } catch (BankAccountNotFoundException e) {
            log.error("Account not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", statusUpdate.get("status"));
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating account {} status: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get account transactions", description = "Get all transactions for a specific account (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account transactions")
    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAccountTransactions(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type) {

        log.info("Admin requesting transactions for account: {}", id);

        try {
            // Verify account exists
            BankAccount account = bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found"));

            Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "operationDate"));

            Page<AccountOperation> transactionPage =
                accountOperationRepository.findByBankAccountId(id, pageable);

            List<Map<String, Object>> transactions = transactionPage.getContent().stream()
                .map(operation -> {
                    Map<String, Object> transactionData = new HashMap<>();
                    transactionData.put("id", operation.getId());
                    transactionData.put("type", operation.getType().name());
                    transactionData.put("amount", operation.getAmount());
                    transactionData.put("description", operation.getDescription());
                    transactionData.put("operationDate", operation.getOperationDate());
                    transactionData.put("performedBy", operation.getPerformedBy());
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
            response.put("accountId", id);
            response.put("accountBalance", account.getBalance());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving transactions for account: {}", id, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve account transactions");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Get account statistics", description = "Get statistics about accounts (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account statistics")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAccountStats() {
        log.info("Admin requesting account statistics");

        try {
            List<BankAccountDTO> accounts = bankAccountService.bankAccountList();

            long totalAccounts = accounts.size();
            long currentAccounts = accounts.stream()
                .filter(account -> account.getType().equals("CurrentAccount"))
                .count();
            long savingAccounts = accounts.stream()
                .filter(account -> account.getType().equals("SavingAccount"))
                .count();

            double totalBalance = accounts.stream()
                .mapToDouble(BankAccountDTO::getBalance)
                .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalAccounts", totalAccounts);
            stats.put("currentAccounts", currentAccounts);
            stats.put("savingAccounts", savingAccounts);
            stats.put("totalBalance", totalBalance);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving account statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve statistics"));
        }
    }

    @Operation(summary = "Export accounts to CSV", description = "Export accounts data to CSV format (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "CSV file generated successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportAccounts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {

        log.info("Admin exporting accounts to CSV");

        try {
            List<BankAccountDTO> accounts = bankAccountService.bankAccountList();

            // Filter by search term if provided
            if (search != null && !search.trim().isEmpty()) {
                accounts = accounts.stream()
                    .filter(account ->
                        account.getId().toLowerCase().contains(search.toLowerCase()) ||
                        (account.getCustomerDTO() != null &&
                         account.getCustomerDTO().getName().toLowerCase().contains(search.toLowerCase())))
                    .toList();
            }

            // Filter by status if provided
            if (status != null && !status.trim().isEmpty()) {
                accounts = accounts.stream()
                    .filter(account -> account.getStatus().toString().equalsIgnoreCase(status))
                    .toList();
            }

            // Generate CSV content
            StringBuilder csv = new StringBuilder();
            csv.append("Account ID,Customer Name,Customer Email,Type,Balance,Status,Created Date\n");

            for (BankAccountDTO account : accounts) {
                csv.append(account.getId()).append(",")
                   .append(account.getCustomerDTO() != null ? account.getCustomerDTO().getName() : "").append(",")
                   .append(account.getCustomerDTO() != null ? account.getCustomerDTO().getEmail() : "").append(",")
                   .append(account.getType()).append(",")
                   .append(account.getBalance()).append(",")
                   .append(account.getStatus()).append(",")
                   .append(account.getCreateDate()).append("\n");
            }

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=accounts.csv")
                    .body(csv.toString());
        } catch (Exception e) {
            log.error("Error exporting accounts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
