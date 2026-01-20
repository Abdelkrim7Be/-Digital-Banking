package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bellagnech.dig_bank.entities.AccountOperation;
import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.enums.OperationType;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/transactions")
@AllArgsConstructor
@Slf4j
@Tag(name = "Admin Transaction Management", description = "APIs for admin transaction management and monitoring")
public class AdminTransactionController {

    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get all transactions with pagination", description = "Retrieve all transactions with optional pagination and filtering (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "operationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount) {

        log.info("Admin requesting all transactions - page: {}, size: {}, sortBy: {}, sortOrder: {}",
                page, size, sortBy, sortOrder);

        try {
            Sort sort = Sort.by(sortOrder.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<AccountOperation> transactionPage;

            // Apply filters if provided
            if (search != null || type != null || accountId != null ||
                startDate != null || endDate != null || minAmount != null || maxAmount != null) {
                transactionPage = accountOperationRepository.findWithFilters(
                    search, type, accountId, startDate, endDate, minAmount, maxAmount, pageable);
            } else {
                transactionPage = accountOperationRepository.findAll(pageable);
            }

            List<Map<String, Object>> transactions = transactionPage.getContent().stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", transactions);
            response.put("totalElements", transactionPage.getTotalElements());
            response.put("totalPages", transactionPage.getTotalPages());
            response.put("size", transactionPage.getSize());
            response.put("number", transactionPage.getNumber());
            response.put("first", transactionPage.isFirst());
            response.put("last", transactionPage.isLast());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving transactions", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve transactions");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Get transaction by ID", description = "Get detailed information about a specific transaction (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionById(@PathVariable Long id) {
        log.info("Admin requesting transaction with ID: {}", id);

        Optional<AccountOperation> operationOpt = accountOperationRepository.findById(id);
        if (operationOpt.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Transaction not found");
            return ResponseEntity.notFound().build();
        }

        AccountOperation operation = operationOpt.get();
        Map<String, Object> response = mapTransactionToDetailedResponse(operation);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get transactions by account", description = "Get all transactions for a specific account (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account transactions")
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionsByAccount(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin requesting transactions for account: {}", accountId);

        try {
            Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "operationDate"));

            Page<AccountOperation> transactionPage =
                accountOperationRepository.findByBankAccountId(accountId, pageable);

            List<Map<String, Object>> transactions = transactionPage.getContent().stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", transactions);
            response.put("totalElements", transactionPage.getTotalElements());
            response.put("totalPages", transactionPage.getTotalPages());
            response.put("accountId", accountId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving transactions for account: {}", accountId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve account transactions");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Get transactions by customer", description = "Get all transactions for a specific customer (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer transactions")
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Admin requesting transactions for customer: {}", customerId);

        try {
            // Get all accounts for the customer
            List<BankAccount> customerAccounts = bankAccountRepository.findByCustomerId(customerId);
            List<String> accountIds = customerAccounts.stream()
                .map(BankAccount::getId)
                .collect(Collectors.toList());

            if (accountIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("content", Collections.emptyList());
                response.put("totalElements", 0);
                response.put("totalPages", 0);
                response.put("customerId", customerId);
                return ResponseEntity.ok(response);
            }

            Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "operationDate"));

            Page<AccountOperation> transactionPage =
                accountOperationRepository.findByBankAccountIdIn(accountIds, pageable);

            List<Map<String, Object>> transactions = transactionPage.getContent().stream()
                .map(this::mapTransactionToResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("content", transactions);
            response.put("totalElements", transactionPage.getTotalElements());
            response.put("totalPages", transactionPage.getTotalPages());
            response.put("customerId", customerId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving transactions for customer: {}", customerId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve customer transactions");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Get transaction statistics", description = "Get comprehensive transaction statistics (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction statistics")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionStats() {
        log.info("Admin requesting transaction statistics");

        try {
            Map<String, Object> stats = new HashMap<>();

            // Total transactions
            long totalTransactions = accountOperationRepository.count();
            stats.put("totalTransactions", totalTransactions);

            // Transactions by type
            Map<String, Long> transactionsByType = new HashMap<>();
            for (OperationType type : OperationType.values()) {
                long count = accountOperationRepository.countByType(type);
                transactionsByType.put(type.name(), count);
            }
            stats.put("transactionsByType", transactionsByType);

            // Today's transactions
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay = today.atTime(23, 59, 59);

            Date startDate = Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());

            long todayTransactions = accountOperationRepository.countByOperationDateBetween(startDate, endDate);
            stats.put("todayTransactions", todayTransactions);

            // Total transaction volume
            Double totalVolume = accountOperationRepository.sumAllAmounts();
            stats.put("totalVolume", totalVolume != null ? totalVolume : 0.0);

            // Average transaction amount
            Double avgAmount = totalTransactions > 0 && totalVolume != null ? totalVolume / totalTransactions : 0.0;
            stats.put("averageTransactionAmount", avgAmount);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error retrieving transaction statistics", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve transaction statistics");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private Map<String, Object> mapTransactionToResponse(AccountOperation operation) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", operation.getId());
        response.put("type", operation.getType().name());
        response.put("amount", operation.getAmount());
        response.put("description", operation.getDescription());
        response.put("operationDate", operation.getOperationDate());
        response.put("performedBy", operation.getPerformedBy());

        if (operation.getBankAccount() != null) {
            response.put("accountId", operation.getBankAccount().getId());
            response.put("accountBalance", operation.getBankAccount().getBalance());

            if (operation.getBankAccount().getCustomer() != null) {
                Customer customer = operation.getBankAccount().getCustomer();
                Map<String, Object> customerInfo = new HashMap<>();
                customerInfo.put("id", customer.getId());
                customerInfo.put("name", customer.getName());
                customerInfo.put("email", customer.getEmail());

                // Add username from associated User entity
                if (customer.getUser() != null) {
                    customerInfo.put("username", customer.getUser().getUsername());
                } else {
                    customerInfo.put("username", "N/A");
                }

                response.put("customer", customerInfo);
            }
        }

        return response;
    }

    private Map<String, Object> mapTransactionToDetailedResponse(AccountOperation operation) {
        Map<String, Object> response = mapTransactionToResponse(operation);

        // Add additional details for detailed view
        if (operation.getBankAccount() != null) {
            BankAccount account = operation.getBankAccount();
            Map<String, Object> accountDetails = new HashMap<>();
            accountDetails.put("id", account.getId());
            accountDetails.put("balance", account.getBalance());
            accountDetails.put("status", account.getStatus());
            accountDetails.put("createDate", account.getCreateDate());
            accountDetails.put("type", account.getClass().getSimpleName());

            response.put("accountDetails", accountDetails);
        }

        return response;
    }
}
