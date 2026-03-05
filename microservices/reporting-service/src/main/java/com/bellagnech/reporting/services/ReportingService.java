package com.bellagnech.reporting.services;

import com.bellagnech.reporting.clients.AccountServiceClient;
import com.bellagnech.reporting.clients.CustomerServiceClient;
import com.bellagnech.reporting.clients.TransactionServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/** Aggregates stats and reports from customer/account/transaction services. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportingService {

    private final CustomerServiceClient customerServiceClient;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;

    public Map<String, Object> getDashboardStats() {
        log.info("Generating dashboard statistics");

        List<CustomerServiceClient.CustomerDTO> customers = Collections.emptyList();
        List<AccountServiceClient.AccountDTO> accounts = Collections.emptyList();

        try {
            customers = customerServiceClient.getAllCustomers();
        } catch (Exception e) {
            log.warn("Failed to load customers for dashboard stats: {}", e.getMessage());
        }

        try {
            accounts = accountServiceClient.getAllAccounts();
        } catch (Exception e) {
            log.warn("Failed to load accounts for dashboard stats: {}", e.getMessage());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCustomers", customers.size());
        stats.put("totalAccounts", accounts.size());

        double totalBalance = accounts.stream()
            .mapToDouble(acc -> acc.balance != null ? acc.balance : 0.0)
            .sum();
        stats.put("totalBalance", totalBalance);
        stats.put("averageBalance", accounts.isEmpty() ? 0.0 : totalBalance / accounts.size());

        long currentAccounts = accounts.stream()
            .filter(acc -> "CurrentAccount".equals(acc.type))
            .count();
        long savingAccounts = accounts.stream()
            .filter(acc -> "SavingAccount".equals(acc.type))
            .count();

        stats.put("currentAccounts", currentAccounts);
        stats.put("savingAccounts", savingAccounts);
        stats.putIfAbsent("totalTransactions", 0);
        stats.putIfAbsent("activeCustomers", customers.size());
        stats.putIfAbsent("pendingTransactions", 0);
        stats.putIfAbsent("monthlyGrowth", 0);
        stats.putIfAbsent("revenueGrowth", 0);

        return stats;
    }

    public Map<String, Object> getCustomerSummaryReport() {
        log.info("Generating customer summary report");
        
        List<CustomerServiceClient.CustomerDTO> customers = customerServiceClient.getAllCustomers();
        List<AccountServiceClient.AccountDTO> allAccounts = accountServiceClient.getAllAccounts();
        
        List<Map<String, Object>> customerSummaries = new ArrayList<>();
        
        for (CustomerServiceClient.CustomerDTO customer : customers) {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", customer.id);
            summary.put("name", customer.name);
            summary.put("email", customer.email);
            summary.put("phone", customer.phone);
            summary.put("createdDate", customer.createdDate);
            
            List<AccountServiceClient.AccountDTO> customerAccounts = allAccounts.stream()
                .filter(acc -> customer.id.equals(acc.customerId))
                .collect(Collectors.toList());
            
            summary.put("totalAccounts", customerAccounts.size());
            
            double totalBalance = customerAccounts.stream()
                .mapToDouble(acc -> acc.balance != null ? acc.balance : 0.0)
                .sum();
            summary.put("totalBalance", totalBalance);
            
            long transactionCount = customerAccounts.stream()
                .mapToLong(acc -> {
                    try {
                        return transactionServiceClient.getAccountTransactions(acc.id).size();
                    } catch (Exception e) {
                        log.warn("Failed to get transactions for account {}: {}", acc.id, e.getMessage());
                        return 0;
                    }
                })
                .sum();
            summary.put("transactionCount", transactionCount);
            
            customerSummaries.add(summary);
        }
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Customer Summary");
        report.put("generatedDate", new Date());
        report.put("totalCustomers", customers.size());
        report.put("customers", customerSummaries);
        
        return report;
    }

    public Map<String, Object> getAccountBalanceReport() {
        log.info("Generating account balance report");
        
        List<AccountServiceClient.AccountDTO> accounts = accountServiceClient.getAllAccounts();
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Account Balance Analysis");
        report.put("generatedDate", new Date());
        
        Map<String, Integer> accountTypeDistribution = new HashMap<>();
        Map<String, Double> balanceByType = new HashMap<>();
        
        for (AccountServiceClient.AccountDTO account : accounts) {
            String type = account.type != null ? account.type : "Unknown";
            accountTypeDistribution.merge(type, 1, Integer::sum);
            balanceByType.merge(type, account.balance != null ? account.balance : 0.0, Double::sum);
        }
        
        report.put("accountTypeDistribution", accountTypeDistribution);
        report.put("balanceByType", balanceByType);
        
        Map<String, Integer> balanceRanges = new HashMap<>();
        balanceRanges.put("0-1000", 0);
        balanceRanges.put("1000-5000", 0);
        balanceRanges.put("5000-10000", 0);
        balanceRanges.put("10000-50000", 0);
        balanceRanges.put("50000+", 0);
        
        for (AccountServiceClient.AccountDTO account : accounts) {
            double balance = account.balance != null ? account.balance : 0.0;
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
        
        double totalBalance = accounts.stream()
            .mapToDouble(acc -> acc.balance != null ? acc.balance : 0.0)
            .sum();
        double averageBalance = accounts.isEmpty() ? 0.0 : totalBalance / accounts.size();
        double maxBalance = accounts.stream()
            .mapToDouble(acc -> acc.balance != null ? acc.balance : 0.0)
            .max()
            .orElse(0.0);
        double minBalance = accounts.stream()
            .mapToDouble(acc -> acc.balance != null ? acc.balance : 0.0)
            .min()
            .orElse(0.0);
        
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalBalance", totalBalance);
        summary.put("averageBalance", averageBalance);
        summary.put("maxBalance", maxBalance);
        summary.put("minBalance", minBalance);
        
        report.put("summary", summary);
        
        return report;
    }

    public Map<String, Object> getTransactionAnalysisReport(int days) {
        log.info("Generating transaction analysis report for {} days", days);
        
        List<AccountServiceClient.AccountDTO> accounts = accountServiceClient.getAllAccounts();
        List<TransactionServiceClient.TransactionDTO> allTransactions = new ArrayList<>();
        
        for (AccountServiceClient.AccountDTO account : accounts) {
            try {
                List<TransactionServiceClient.TransactionDTO> transactions = 
                    transactionServiceClient.getAccountTransactions(account.id);
                allTransactions.addAll(transactions);
            } catch (Exception e) {
                log.warn("Failed to get transactions for account {}: {}", account.id, e.getMessage());
            }
        }
        
        Date cutoffDate = new Date(System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000));
        List<TransactionServiceClient.TransactionDTO> recentTransactions = allTransactions.stream()
            .filter(tx -> tx.operationDate != null && tx.operationDate.after(cutoffDate))
            .collect(Collectors.toList());
        
        Map<String, Object> report = new HashMap<>();
        report.put("reportType", "Transaction Analysis");
        report.put("generatedDate", new Date());
        report.put("periodDays", days);
        
        Map<String, Long> transactionsByType = recentTransactions.stream()
            .collect(Collectors.groupingBy(
                tx -> tx.type != null ? tx.type : "UNKNOWN",
                Collectors.counting()
            ));
        report.put("transactionsByType", transactionsByType);
        
        Map<String, Double> volumeByType = recentTransactions.stream()
            .collect(Collectors.groupingBy(
                tx -> tx.type != null ? tx.type : "UNKNOWN",
                Collectors.summingDouble(tx -> tx.amount != null ? tx.amount : 0.0)
            ));
        report.put("volumeByType", volumeByType);
        
        double totalVolume = recentTransactions.stream()
            .mapToDouble(tx -> tx.amount != null ? tx.amount : 0.0)
            .sum();
        double averageAmount = recentTransactions.isEmpty() ? 0.0 : totalVolume / recentTransactions.size();
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTransactions", recentTransactions.size());
        summary.put("totalVolume", totalVolume);
        summary.put("averageAmount", averageAmount);
        
        report.put("summary", summary);
        
        return report;
    }

    public Map<String, Object> getTransactionsSummary() {
        log.info("Generating simple transactions summary for admin dashboard");

        Map<String, Object> summary = new HashMap<>();

        try {
            List<AccountServiceClient.AccountDTO> accounts = accountServiceClient.getAllAccounts();
            List<TransactionServiceClient.TransactionDTO> allTransactions = new ArrayList<>();

            for (AccountServiceClient.AccountDTO account : accounts) {
                try {
                    List<TransactionServiceClient.TransactionDTO> txs =
                            transactionServiceClient.getAccountTransactions(account.id);
                    allTransactions.addAll(txs);
                } catch (Exception e) {
                    log.warn("Failed to get transactions for account {}: {}", account.id, e.getMessage());
                }
            }

            long depositCount = allTransactions.stream()
                .filter(tx -> tx.type != null && "CREDIT".equalsIgnoreCase(tx.type))
                .count();
            long withdrawalCount = allTransactions.stream()
                .filter(tx -> tx.type != null && "DEBIT".equalsIgnoreCase(tx.type))
                .count();
            long transferCount = allTransactions.stream()
                .filter(tx -> tx.type != null && "DEBIT".equalsIgnoreCase(tx.type)
                    && tx.description != null && tx.description.toLowerCase().startsWith("transfer to"))
                .count();

            Map<String, Long> transactionsByType = new HashMap<>();
            transactionsByType.put("DEPOSIT", depositCount);
            transactionsByType.put("WITHDRAWAL", withdrawalCount);
            transactionsByType.put("TRANSFER", transferCount);

            double totalVolume = allTransactions.stream()
                    .mapToDouble(tx -> tx.amount != null ? tx.amount : 0.0)
                    .sum();

            summary.put("totalTransactions", allTransactions.size());
            summary.put("transactionsByType", transactionsByType);
            summary.put("totalVolume", totalVolume);
            summary.put("pendingTransactions", 0);
        } catch (Exception e) {
            log.warn("Failed to generate transactions summary: {}", e.getMessage());
            summary.putIfAbsent("totalTransactions", 0);
            summary.putIfAbsent("transactionsByType", Collections.emptyMap());
            summary.putIfAbsent("totalVolume", 0.0);
            summary.putIfAbsent("pendingTransactions", 0);
        }

        return summary;
    }
}

