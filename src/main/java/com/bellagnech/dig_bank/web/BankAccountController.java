package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bellagnech.dig_bank.dtos.*;
import com.bellagnech.dig_bank.exceptions.BalanceNotSufficientException;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.services.BankAccountService;
import com.bellagnech.dig_bank.enums.AccountStatus;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

// REST Controller for Bank Account Management - Provides endpoints for account operations, transactions, and account management
@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
@Slf4j
@Tag(name = "Bank Account Management", description = "APIs for managing bank accounts and transactions")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @Operation(summary = "Get all accounts", description = "Retrieves all bank accounts in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts list")
    @GetMapping
    public ResponseEntity<List<BankAccountDTO>> getAllAccounts() {
        log.info("Retrieving all bank accounts");
        List<BankAccountDTO> accounts = bankAccountService.bankAccountList();
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Get account by ID", description = "Retrieves bank account details by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved account"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccountDTO> getAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        log.info("Retrieving account with ID: {}", accountId);
        BankAccountDTO account = bankAccountService.getBankAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Get customer accounts", description = "Retrieves all accounts for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customer accounts"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BankAccountDTO>> getCustomerAccounts(@PathVariable Long customerId) throws CustomerNotFoundException {
        log.info("Retrieving accounts for customer ID: {}", customerId);
        List<BankAccountDTO> accounts = bankAccountService.getCustomerAccounts(customerId);
        return ResponseEntity.ok(accounts);
    }

    @Operation(summary = "Create current account", description = "Creates a new current account with overdraft facility")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account successfully created"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/current")
    public ResponseEntity<CurrentBankAccountDTO> createCurrentAccount(
            @RequestParam double initialBalance,
            @RequestParam double overDraft,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        log.info("Creating current account for customer ID: {} with balance: {}", customerId, initialBalance);
        CurrentBankAccountDTO account = bankAccountService.saveCurrentBankAccount(initialBalance, overDraft, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @Operation(summary = "Create saving account", description = "Creates a new saving account with interest rate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account successfully created"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/saving")
    public ResponseEntity<SavingBankAccountDTO> createSavingAccount(
            @RequestParam double initialBalance,
            @RequestParam double interestRate,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        log.info("Creating saving account for customer ID: {} with balance: {}", customerId, initialBalance);
        SavingBankAccountDTO account = bankAccountService.saveSavingBankAccount(initialBalance, interestRate, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @Operation(summary = "Withdraw from account", description = "Performs a debit operation on an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance")
    })
    @PostMapping("/{accountId}/debit")
    public ResponseEntity<Void> debit(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Performing debit operation on account ID: {} for amount: {}", accountId, amount);
        bankAccountService.debit(accountId, amount, description);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deposit to account", description = "Performs a credit operation on an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successful"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/{accountId}/credit")
    public ResponseEntity<Void> credit(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam String description) throws BankAccountNotFoundException {
        log.info("Performing credit operation on account ID: {} for amount: {}", accountId, amount);
        bankAccountService.credit(accountId, amount, description);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Transfer between accounts", description = "Transfers money from one account to another")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer successful"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance")
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(
            @RequestParam String sourceAccountId,
            @RequestParam String destinationAccountId,
            @RequestParam double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Transferring {} from account ID: {} to account ID: {}", amount, sourceAccountId, destinationAccountId);
        bankAccountService.transfer(sourceAccountId, destinationAccountId, amount);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get account operations", description = "Retrieves all operations for a specific account")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved operations")
    @GetMapping("/{accountId}/operations")
    public ResponseEntity<List<AccountOperationDTO>> getAccountHistory(@PathVariable String accountId) {
        log.info("Retrieving operations for account ID: {}", accountId);
        List<AccountOperationDTO> operations = bankAccountService.accountHistory(accountId);
        return ResponseEntity.ok(operations);
    }

    @Operation(summary = "Get paginated account history", description = "Retrieves paginated operations for a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved operations"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/history")
    public ResponseEntity<AccountHistoryDTO> getAccountHistoryPaginated(
            @PathVariable String accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        log.info("Retrieving paginated operations for account ID: {} (page: {}, size: {})", accountId, page, size);
        AccountHistoryDTO history = bankAccountService.getAccountHistory(accountId, page, size);
        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Apply interest to saving account", description = "Calculates and applies interest to a saving account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interest applied successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Not a saving account")
    })
    @PostMapping("/{accountId}/apply-interest")
    public ResponseEntity<Void> applyInterest(@PathVariable String accountId) throws BankAccountNotFoundException {
        log.info("Applying interest to account ID: {}", accountId);
        bankAccountService.applyInterest(accountId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update account status", description = "Changes the status of an account (e.g., activate, suspend)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{accountId}/status")
    public ResponseEntity<Void> updateAccountStatus(
            @PathVariable String accountId,
            @RequestParam AccountStatus status) throws BankAccountNotFoundException {
        log.info("Updating status of account ID: {} to {}", accountId, status);
        bankAccountService.updateAccountStatus(accountId, status);
        return ResponseEntity.ok().build();
    }
}
