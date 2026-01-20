package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bellagnech.dig_bank.entities.AccountOperation;
import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports")
@AllArgsConstructor
@Slf4j
@Tag(name = "Admin Reports", description = "APIs for generating admin reports and analytics")
public class AdminReportsController {

    private final AccountOperationRepository accountOperationRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get report statistics", description = "Get comprehensive statistics for reports dashboard (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved report statistics")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getReportStats() {
        log.info("Admin requesting report statistics");

        try {
            Map<String, Object> stats = new HashMap<>();

            // Basic counts
            long totalCustomers = customerRepository.count();
            long totalAccounts = bankAccountRepository.count();
            long totalTransactions = accountOperationRepository.count();

            // Total balance across all accounts
            Double totalBalance = bankAccountRepository.sumAllBalances();

            stats.put("totalCustomers", totalCustomers);
            stats.put("totalAccounts", totalAccounts);
            stats.put("totalTransactions", totalTransactions);
            stats.put("totalBalance", totalBalance != null ? totalBalance : 0.0);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error retrieving report statistics", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to retrieve report statistics");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Generate customer summary report", description = "Generate comprehensive customer summary report (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully generated customer report")
    @GetMapping("/customer-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCustomerSummaryReport() {
        log.info("Admin requesting customer summary report");

        try {
            List<Customer> customers = customerRepository.findAll();
            List<Map<String, Object>> customerSummaries = new ArrayList<>();

            for (Customer customer : customers) {
                Map<String, Object> summary = new HashMap<>();
                summary.put("id", customer.getId());
                summary.put("name", customer.getName());
                summary.put("email", customer.getEmail());
                summary.put("phone", customer.getPhone());
                summary.put("createdDate", customer.getCreatedDate());

                // Get customer accounts
                List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customer.getId());
                summary.put("totalAccounts", accounts.size());

                // Calculate total balance
                double totalBalance = accounts.stream()
                    .mapToDouble(BankAccount::getBalance)
                    .sum();
                summary.put("totalBalance", totalBalance);

                // Get transaction count
                List<String> accountIds = accounts.stream()
                    .map(BankAccount::getId)
                    .collect(Collectors.toList());

                long transactionCount = 0;
                if (!accountIds.isEmpty()) {
                    transactionCount = accountOperationRepository.countByBankAccountIdIn(accountIds);
                }
                summary.put("transactionCount", transactionCount);

                customerSummaries.add(summary);
            }

            Map<String, Object> report = new HashMap<>();
            report.put("reportType", "Customer Summary");
            report.put("generatedDate", new Date());
            report.put("totalCustomers", customers.size());
            report.put("customers", customerSummaries);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            log.error("Error generating customer summary report", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate customer summary report");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Generate account balance report", description = "Generate account balance analysis report (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully generated account balance report")
    @GetMapping("/account-balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAccountBalanceReport() {
        log.info("Admin requesting account balance report");

        try {
            List<BankAccount> accounts = bankAccountRepository.findAll();

            Map<String, Object> report = new HashMap<>();
            report.put("reportType", "Account Balance Analysis");
            report.put("generatedDate", new Date());

            // Account type distribution
            Map<String, Integer> accountTypeDistribution = new HashMap<>();
            Map<String, Double> balanceByType = new HashMap<>();

            for (BankAccount account : accounts) {
                String type = account.getClass().getSimpleName();
                accountTypeDistribution.merge(type, 1, Integer::sum);
                balanceByType.merge(type, account.getBalance(), Double::sum);
            }

            report.put("accountTypeDistribution", accountTypeDistribution);
            report.put("balanceByType", balanceByType);

            // Balance ranges
            Map<String, Integer> balanceRanges = new HashMap<>();
            balanceRanges.put("0-1000", 0);
            balanceRanges.put("1000-5000", 0);
            balanceRanges.put("5000-10000", 0);
            balanceRanges.put("10000-50000", 0);
            balanceRanges.put("50000+", 0);

            for (BankAccount account : accounts) {
                double balance = account.getBalance();
                if (balance <= 1000) {
                    balanceRanges.merge("0-1000", 1, Integer::sum);
                } else if (balance <= 5000) {
                    balanceRanges.merge("1000-5000", 1, Integer::sum);
                } else if (balance <= 10000) {
                    balanceRanges.merge("5000-10000", 1, Integer::sum);
                } else if (balance <= 50000) {
                    balanceRanges.merge("10000-50000", 1, Integer::sum);
                } else {
                    balanceRanges.merge("50000+", 1, Integer::sum);
                }
            }

            report.put("balanceRanges", balanceRanges);

            // Summary statistics
            double totalBalance = accounts.stream().mapToDouble(BankAccount::getBalance).sum();
            double averageBalance = accounts.isEmpty() ? 0 : totalBalance / accounts.size();
            double maxBalance = accounts.stream().mapToDouble(BankAccount::getBalance).max().orElse(0);
            double minBalance = accounts.stream().mapToDouble(BankAccount::getBalance).min().orElse(0);

            Map<String, Double> summary = new HashMap<>();
            summary.put("totalBalance", totalBalance);
            summary.put("averageBalance", averageBalance);
            summary.put("maxBalance", maxBalance);
            summary.put("minBalance", minBalance);

            report.put("summary", summary);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            log.error("Error generating account balance report", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate account balance report");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Generate transaction analysis report", description = "Generate transaction volume and analysis report (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully generated transaction report")
    @GetMapping("/transaction-analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTransactionAnalysisReport(
            @RequestParam(defaultValue = "30") int days) {

        log.info("Admin requesting transaction analysis report for {} days", days);

        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);

            Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

            List<AccountOperation> operations = accountOperationRepository.findByOperationDateBetween(start, end);

            Map<String, Object> report = new HashMap<>();
            report.put("reportType", "Transaction Analysis");
            report.put("generatedDate", new Date());
            report.put("periodDays", days);
            report.put("startDate", start);
            report.put("endDate", end);

            // Transaction count by type
            Map<String, Long> transactionsByType = operations.stream()
                .collect(Collectors.groupingBy(
                    op -> op.getType().name(),
                    Collectors.counting()
                ));
            report.put("transactionsByType", transactionsByType);

            // Transaction volume by type
            Map<String, Double> volumeByType = operations.stream()
                .collect(Collectors.groupingBy(
                    op -> op.getType().name(),
                    Collectors.summingDouble(AccountOperation::getAmount)
                ));
            report.put("volumeByType", volumeByType);

            // Daily transaction counts
            Map<String, Long> dailyTransactions = operations.stream()
                .collect(Collectors.groupingBy(
                    op -> op.getOperationDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE),
                    Collectors.counting()
                ));
            report.put("dailyTransactions", dailyTransactions);

            // Summary statistics
            double totalVolume = operations.stream().mapToDouble(AccountOperation::getAmount).sum();
            double averageAmount = operations.isEmpty() ? 0 : totalVolume / operations.size();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalTransactions", operations.size());
            summary.put("totalVolume", totalVolume);
            summary.put("averageAmount", averageAmount);

            report.put("summary", summary);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            log.error("Error generating transaction analysis report", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to generate transaction analysis report");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(summary = "Export customer data to CSV", description = "Export customer data in CSV format (ADMIN only)")
    @ApiResponse(responseCode = "200", description = "Successfully exported customer data")
    @GetMapping("/export/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportCustomersCSV() {
        log.info("Admin requesting customer data export");

        try {
            List<Customer> customers = customerRepository.findAll();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);

            // CSV Header
            writer.println("ID,Name,Email,Phone,Address,Created Date,Total Accounts,Total Balance");

            // CSV Data
            for (Customer customer : customers) {
                List<BankAccount> accounts = bankAccountRepository.findByCustomerId(customer.getId());
                double totalBalance = accounts.stream().mapToDouble(BankAccount::getBalance).sum();

                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s,%d,%.2f%n",
                    customer.getId(),
                    customer.getName() != null ? customer.getName().replace("\"", "\"\"") : "",
                    customer.getEmail() != null ? customer.getEmail() : "",
                    customer.getPhone() != null ? customer.getPhone() : "",
                    customer.getAddress() != null ? customer.getAddress().replace("\"", "\"\"") : "",
                    customer.getCreatedDate() != null ? customer.getCreatedDate().toString() : "",
                    accounts.size(),
                    totalBalance
                );
            }

            writer.flush();
            writer.close();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "customers_export.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error exporting customer data", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
