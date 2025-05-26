package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    //Find all bank accounts for a specific customer
    List<BankAccount> findByCustomer(Customer customer);

    //Find all bank accounts for a specific customer ID
    List<BankAccount> findByCustomerId(Long customerId);

    //Find bank accounts by status
    List<BankAccount> findByStatus(AccountStatus status);

    //Find bank accounts by customer and status
    List<BankAccount> findByCustomerAndStatus(Customer customer, AccountStatus status);

    //Find bank accounts with balance greater than specified amount
    @Query("SELECT ba FROM BankAccount ba WHERE ba.balance > :minBalance")
    List<BankAccount> findAccountsWithBalanceGreaterThan(@Param("minBalance") double minBalance);

    // Count total number of accounts for a customer
    long countByCustomer(Customer customer);

    // Admin-specific queries for account management

    //Sum all account balances
    @Query("SELECT SUM(ba.balance) FROM BankAccount ba")
    Double sumAllBalances();

    //Count accounts by status
    long countByStatus(AccountStatus status);

    //Find accounts with balance in range
    @Query("SELECT ba FROM BankAccount ba WHERE ba.balance BETWEEN :minBalance AND :maxBalance")
    List<BankAccount> findAccountsWithBalanceInRange(@Param("minBalance") double minBalance,
                                                    @Param("maxBalance") double maxBalance);

    //Find accounts with no recent activity (dormant accounts)
    @Query("SELECT ba FROM BankAccount ba WHERE ba.id NOT IN " +
           "(SELECT DISTINCT ao.bankAccount.id FROM AccountOperation ao " +
           "WHERE ao.operationDate >= :cutoffDate)")
    List<BankAccount> findDormantAccounts(@Param("cutoffDate") java.util.Date cutoffDate);
}