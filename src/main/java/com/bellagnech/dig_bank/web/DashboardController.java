package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.services.BankAccountService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// REST Controller for Dashboard and Statistics - Provides endpoints for banking statistics and dashboard data
@RestController
@RequestMapping("/api/dashboard")
@AllArgsConstructor
@Slf4j
@Tag(name = "Dashboard & Statistics", description = "APIs for banking statistics and dashboard information")
public class DashboardController {

    private final BankAccountService bankAccountService;

    @Operation(summary = "Get banking statistics", description = "Retrieves overall banking statistics including customer count, account count, and total balances")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getBankingStatistics() {
        log.info("Retrieving banking statistics");

        List<CustomerDTO> customers = bankAccountService.listCustomersDTO();
        List<BankAccountDTO> accounts = bankAccountService.bankAccountList();

        // Calculate statistics
        int totalCustomers = customers.size();
        int totalAccounts = accounts.size();

        double totalBalance = accounts.stream()
                .mapToDouble(BankAccountDTO::getBalance)
                .sum();

        long currentAccounts = accounts.stream()
                .filter(account -> "CURRENT".equals(account.getType()))
                .count();

        long savingAccounts = accounts.stream()
                .filter(account -> "SAVING".equals(account.getType()))
                .count();

        double averageBalance = totalAccounts > 0 ? totalBalance / totalAccounts : 0.0;

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCustomers", totalCustomers);
        statistics.put("totalAccounts", totalAccounts);
        statistics.put("totalBalance", totalBalance);
        statistics.put("currentAccounts", currentAccounts);
        statistics.put("savingAccounts", savingAccounts);
        statistics.put("averageBalance", averageBalance);

        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Get account summary", description = "Retrieves a summary of all accounts with basic information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account summary")
    @GetMapping("/accounts-summary")
    public ResponseEntity<Map<String, Object>> getAccountsSummary() {
        log.info("Retrieving accounts summary");

        List<BankAccountDTO> accounts = bankAccountService.bankAccountList();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAccounts", accounts.size());
        summary.put("accounts", accounts);

        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get customer summary", description = "Retrieves a summary of all customers with basic information")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer summary")
    @GetMapping("/customers-summary")
    public ResponseEntity<Map<String, Object>> getCustomersSummary() {
        log.info("Retrieving customers summary");

        List<CustomerDTO> customers = bankAccountService.listCustomersDTO();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalCustomers", customers.size());
        summary.put("customers", customers);

        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Health check", description = "Simple health check endpoint to verify API is running")
    @ApiResponse(responseCode = "200", description = "API is healthy")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        log.info("Health check requested");

        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Digital Banking API");
        health.put("version", "1.0.0");
        health.put("timestamp", java.time.Instant.now().toString());

        return ResponseEntity.ok(health);
    }
}
