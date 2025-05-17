package com.bellagnech.dig_bank.services;

import com.bellagnech.dig_bank.entities.BankAccount;
import com.bellagnech.dig_bank.entities.CurrentAccount;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.entities.SavingAccount;
import com.bellagnech.dig_bank.exceptions.BalanceNotSufficientException;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;

import java.util.List;

public interface BankAccountService {
    // Save a new customer to the system
    Customer saveCustomer(Customer customer);
    // Create a new current account with overdraft facility
    CurrentAccount saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
    // Create a new savings account with interest rate
    SavingAccount saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
    // Get all customers from the database
    List<Customer> listCustomers();
    // Find a bank account by its ID
    BankAccount getBankAccount(String accountId) throws BankAccountNotFoundException;
    // Withdraw money from an account
    void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    // Deposit money into an account
    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;
    // Transfer money between two accounts
    void transfer(String accountIdSource, String accountIdDestination, Double amount)
            throws BankAccountNotFoundException, BalanceNotSufficientException;
    // List of all bank accounts 
    List<BankAccount> bankAccountList();
}
