package com.bellagnech.dig_bank.services;

import com.bellagnech.dig_bank.dtos.AccountHistoryDTO;
import com.bellagnech.dig_bank.dtos.AccountOperationDTO;
import com.bellagnech.dig_bank.dtos.AccountSelectionDTO;
import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CurrentBankAccountDTO;
import com.bellagnech.dig_bank.dtos.SavingBankAccountDTO;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.exceptions.BalanceNotSufficientException;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.enums.AccountStatus;
import org.springframework.data.domain.Page;

import java.util.List;

// Service interface for managing bank accounts, customers, and banking operations
public interface BankAccountService {

    // Customer Management
    // Save a new customer to the system
    CustomerDTO saveCustomer(CustomerDTO customer);

    // Get all customers as DTOs
    List<CustomerDTO> listCustomersDTO();

    // Get a specific customer by ID
    CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException;

    // Update an existing customer
    CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException;

    // Delete a customer by ID
    void deleteCustomer(Long customerID) throws CustomerNotFoundException;

    // Get paginated list of customers
    Page<CustomerDTO> getCustomersPageable(int page, int size);

    // Search customers by keyword with pagination
    Page<CustomerDTO> searchCustomers(String keyword, int page, int size);

    // Account Management
    // Create a new current account with overdraft facility
    CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId)
        throws CustomerNotFoundException;

    // Create a new savings account with interest rate
    SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId)
        throws CustomerNotFoundException;

    // Find a bank account by its ID
    BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException;

    // Get list of all bank accounts
    List<BankAccountDTO> bankAccountList();

    // Get all bank accounts for a specific customer
    List<BankAccountDTO> getCustomerAccounts(Long customerId) throws CustomerNotFoundException;

    // Update account status
    void updateAccountStatus(String accountId, AccountStatus status) throws BankAccountNotFoundException;

    // Banking Operations
    // Withdraw money from an account
    void debit(String accountId, double amount, String description)
        throws BankAccountNotFoundException, BalanceNotSufficientException;

    // Deposit money into an account
    void credit(String accountId, double amount, String description) throws BankAccountNotFoundException;

    // Transfer money between two accounts
    void transfer(String accountIdSource, String accountIdDestination, Double amount)
        throws BankAccountNotFoundException, BalanceNotSufficientException;

    // Apply interest to a savings account
    void applyInterest(String accountId) throws BankAccountNotFoundException;

    // Account History and Operations
    // Get the account history for a specific account
    List<AccountOperationDTO> accountHistory(String accountId);

    // Get the account history for a specific account with pagination
    AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException;

    // Account Selection for Dropdowns
    // Get all accounts with customer usernames for dropdown selection
    List<AccountSelectionDTO> getAccountsForSelection();

    // Get active accounts with customer usernames for dropdown selection
    List<AccountSelectionDTO> getActiveAccountsForSelection();
}
