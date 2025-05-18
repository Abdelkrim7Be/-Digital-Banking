package com.bellagnech.dig_bank.services;

import com.bellagnech.dig_bank.dtos.AccountHistoryDTO;
import com.bellagnech.dig_bank.dtos.AccountOperationDTO;
import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CurrentBankAccountDTO;
import com.bellagnech.dig_bank.dtos.SavingBankAccountDTO;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.exceptions.BalanceNotSufficientException;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.enums.AccountStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface BankAccountService {
    // Save a new customer to the system
    CustomerDTO saveCustomer(CustomerDTO customer);
    // Create a new current account with overdraft facility
    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException;
    // Create a new current account with overdraft facility and track creator
    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId, String username) throws CustomerNotFoundException;
    // Create a new savings account with interest rate
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException;
    // Create a new savings account with interest rate and track creator
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId, String username) throws CustomerNotFoundException;
    // Find a bank account by its ID
    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;
    // Withdraw money from an account
    void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException;
    // Withdraw money from an account and track user
    void debit(String accountId, double amount, String description, String username) throws BankAccountNotFoundException, BalanceNotSufficientException;
    // Deposit money into an account
    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;
    // Deposit money into an account and track user
    void credit(String accountId, double amount, String description, String username) throws BankAccountNotFoundException;
    // Transfer money between two accounts
    void transfer(String accountIdSource, String accountIdDestination, Double amount)
    throws BankAccountNotFoundException, BalanceNotSufficientException;
    // Transfer money between two accounts and track user
    void transfer(String accountIdSource, String accountIdDestination, Double amount, String username)
    throws BankAccountNotFoundException, BalanceNotSufficientException;
    // List of all bank accounts 
    List<BankAccountDTO> bankAccountList();
    // Get all customers from the database (returns entities)
    List<Customer> listCustomers();
    // Get all customers as DTOs
    List<CustomerDTO> listCustomersDTO();
    // Get a specific customer
    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;
    // Update a customer
    CustomerDTO updateCustomer(CustomerDTO customerDTO);
    // Delete a customer
    void deleteCustomer(Long customerID);
    // Get the account history for a specific account
    List<AccountOperationDTO> accountHistory(String accountId);
    // Get the account history for a specific account with pagination
    AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;
    // Add interest rate calculation method
    void applyInterest(String accountId) throws BankAccountNotFoundException;
    // Add interest rate calculation method with user tracking
    void applyInterest(String accountId, String username) throws BankAccountNotFoundException;
    // Add account status management method
    void updateAccountStatus(String accountId, AccountStatus status) throws BankAccountNotFoundException;
    // Add account status management method with user tracking
    void updateAccountStatus(String accountId, AccountStatus status, String username) throws BankAccountNotFoundException;
    // Get paginated list of customers
    Page<CustomerDTO> getCustomersPageable(int page, int size);
    // Add this method to the interface
    Page<CustomerDTO> searchCustomers(String keyword, int page, int size);
}
