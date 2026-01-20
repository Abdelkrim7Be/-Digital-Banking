package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.enums.Role;
import com.bellagnech.dig_bank.repositories.UserRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.services.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Dashboard", description = "APIs for admin dashboard and system overview")
public class AdminDashboardController {

    private final BankAccountService bankAccountService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final BankAccountRepository bankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;

    @Operation(summary = "Get admin dashboard data", description = "Get comprehensive dashboard overview for administrators")
    @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - ADMIN role required")
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminDashboard() {
        log.info("Admin dashboard requested");
        try {
            List<CustomerDTO> customers = bankAccountService.listCustomersDTO();
            List<BankAccountDTO> accounts = bankAccountService.bankAccountList();
            List<User> users = userRepository.findAll();

            // Calculate comprehensive statistics
            int totalCustomers = customers.size();
            int totalAccounts = accounts.size();
            int totalUsers = users.size();
            
            long adminUsers = users.stream()
                    .filter(user -> user.getRole() == Role.ADMIN)
                    .count();
            
            long customerUsers = users.stream()
                    .filter(user -> user.getRole() == Role.CUSTOMER)
                    .count();

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
            
            // Get total operations count
            long totalOperations = accountOperationRepository.count();

            Map<String, Object> dashboard = new HashMap<>();
            
            // User statistics
            dashboard.put("totalUsers", totalUsers);
            dashboard.put("adminUsers", adminUsers);
            dashboard.put("customerUsers", customerUsers);
            
            // Customer statistics
            dashboard.put("totalCustomers", totalCustomers);
            
            // Account statistics
            dashboard.put("totalAccounts", totalAccounts);
            dashboard.put("currentAccounts", currentAccounts);
            dashboard.put("savingAccounts", savingAccounts);
            
            // Financial statistics
            dashboard.put("totalBalance", totalBalance);
            dashboard.put("averageBalance", averageBalance);
            
            // Operation statistics
            dashboard.put("totalOperations", totalOperations);
            
            // Recent data (limited for dashboard)
            dashboard.put("recentCustomers", customers.stream().limit(5).toList());
            dashboard.put("recentAccounts", accounts.stream().limit(5).toList());

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            log.error("Error retrieving admin dashboard: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to retrieve dashboard data"));
        }
    }

    @Operation(summary = "Get admin statistics", description = "Get detailed statistics for admin dashboard")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        log.info("Admin statistics requested");
        try {
            List<CustomerDTO> customers = bankAccountService.listCustomersDTO();
            List<BankAccountDTO> accounts = bankAccountService.bankAccountList();
            List<User> users = userRepository.findAll();

            Map<String, Object> stats = new HashMap<>();
            
            // Basic counts
            stats.put("totalUsers", users.size());
            stats.put("totalCustomers", customers.size());
            stats.put("totalAccounts", accounts.size());
            stats.put("totalOperations", accountOperationRepository.count());
            
            // Financial data
            double totalBalance = accounts.stream()
                    .mapToDouble(BankAccountDTO::getBalance)
                    .sum();
            stats.put("totalBalance", totalBalance);
            stats.put("averageBalance", accounts.isEmpty() ? 0.0 : totalBalance / accounts.size());
            
            // Account type breakdown
            stats.put("currentAccounts", accounts.stream()
                    .filter(acc -> "CURRENT".equals(acc.getType()))
                    .count());
            stats.put("savingAccounts", accounts.stream()
                    .filter(acc -> "SAVING".equals(acc.getType()))
                    .count());
            
            // User role breakdown
            stats.put("adminUsers", users.stream()
                    .filter(user -> user.getRole() == Role.ADMIN)
                    .count());
            stats.put("customerUsers", users.stream()
                    .filter(user -> user.getRole() == Role.CUSTOMER)
                    .count());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error retrieving admin statistics: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to retrieve statistics"));
        }
    }

    @Operation(summary = "Get accounts summary for admin", description = "Get detailed accounts summary for admin dashboard")
    @ApiResponse(responseCode = "200", description = "Accounts summary retrieved successfully")
    @GetMapping("/dashboard/accounts-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAccountsSummary() {
        log.info("Admin accounts summary requested");
        try {
            List<BankAccountDTO> accounts = bankAccountService.bankAccountList();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalAccounts", accounts.size());
            summary.put("accounts", accounts);
            
            // Additional summary data
            double totalBalance = accounts.stream()
                    .mapToDouble(BankAccountDTO::getBalance)
                    .sum();
            summary.put("totalBalance", totalBalance);
            
            long currentAccounts = accounts.stream()
                    .filter(acc -> "CURRENT".equals(acc.getType()))
                    .count();
            long savingAccounts = accounts.stream()
                    .filter(acc -> "SAVING".equals(acc.getType()))
                    .count();
            
            summary.put("currentAccountsCount", currentAccounts);
            summary.put("savingAccountsCount", savingAccounts);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving accounts summary: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to retrieve accounts summary"));
        }
    }

    @Operation(summary = "Get transactions summary for admin", description = "Get transactions summary for admin dashboard")
    @ApiResponse(responseCode = "200", description = "Transactions summary retrieved successfully")
    @GetMapping("/dashboard/transactions-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionsSummary() {
        log.info("Admin transactions summary requested");
        try {
            long totalOperations = accountOperationRepository.count();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalTransactions", totalOperations);
            
            // For now, return basic data
            // In a real implementation, you would calculate more detailed transaction statistics
            summary.put("todayTransactions", 0);
            summary.put("weekTransactions", 0);
            summary.put("monthTransactions", 0);

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving transactions summary: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to retrieve transactions summary"));
        }
    }

    @Operation(summary = "Get customer growth data", description = "Get customer growth data for admin charts")
    @ApiResponse(responseCode = "200", description = "Customer growth data retrieved successfully")
    @GetMapping("/dashboard/customer-growth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCustomerGrowthData(
            @RequestParam(defaultValue = "12") int months) {
        log.info("Customer growth data requested for {} months", months);
        try {
            // For now, return mock data
            // In a real implementation, you would query the database for actual growth data
            List<Map<String, Object>> growthData = List.of(
                Map.of("month", "Jan", "count", 10),
                Map.of("month", "Feb", "count", 15),
                Map.of("month", "Mar", "count", 20),
                Map.of("month", "Apr", "count", 25),
                Map.of("month", "May", "count", 30),
                Map.of("month", "Jun", "count", 35)
            );

            return ResponseEntity.ok(growthData);
        } catch (Exception e) {
            log.error("Error retrieving customer growth data: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
