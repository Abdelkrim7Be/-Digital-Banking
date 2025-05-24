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
}