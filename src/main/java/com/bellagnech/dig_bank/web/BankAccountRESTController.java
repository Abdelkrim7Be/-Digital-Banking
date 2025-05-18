package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import com.bellagnech.dig_bank.dtos.*;
import com.bellagnech.dig_bank.exceptions.BalanceNotSufficientException;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.services.BankAccountService;
import com.bellagnech.dig_bank.dtos.AccountHistoryDTO;
import com.bellagnech.dig_bank.enums.AccountStatus;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
@Tag(name = "Bank Account Management", description = "APIs for managing bank accounts and transactions")
public class BankAccountRESTController {
    private BankAccountService bankAccountService;

    @Operation(summary = "Get account by ID", description = "Retrieves bank account details by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved account"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountId);
    }

    @Operation(summary = "List all accounts", description = "Retrieves all bank accounts in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts list")
    @GetMapping("")
    public List<BankAccountDTO> listAccounts() {
        return bankAccountService.bankAccountList();
    }
    
    @Operation(summary = "Create current account", description = "Creates a new current account with overdraft facility")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account successfully created"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/current")
    public CurrentBankAccountDTO createCurrentAccount(
            @RequestParam double initialBalance,
            @RequestParam double overDraft,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        return bankAccountService.saveCurrentBankAccount(initialBalance, overDraft, customerId);
    }
    
    @Operation(summary = "Create saving account", description = "Creates a new saving account with interest rate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account successfully created"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/saving")
    public SavingBankAccountDTO createSavingAccount(
            @RequestParam double initialBalance,
            @RequestParam double interestRate,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        return bankAccountService.saveSavingBankAccount(initialBalance, interestRate, customerId);
    }
    
    @Operation(summary = "Withdraw from account", description = "Performs a debit operation on an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance")
    })
    @PostMapping("/{accountId}/debit")
    public void debit(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        bankAccountService.debit(accountId, amount, description);
    }
    
    @Operation(summary = "Deposit to account", description = "Performs a credit operation on an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successful"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/{accountId}/credit")
    public void credit(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam String description) throws BankAccountNotFoundException {
        bankAccountService.credit(accountId, amount, description);
    }
    
    @Operation(summary = "Get account operations", description = "Retrieves all operations for a specific account")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved operations")
    @GetMapping("/{accountId}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId) {
        return bankAccountService.accountHistory(accountId);
    }
    
    @Operation(summary = "Get paginated account history", description = "Retrieves paginated operations for a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved operations"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/pageOperations")
    public AccountHistoryDTO getAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        return bankAccountService.getAccountHistory(accountId, page, size);
    }

    @Operation(summary = "Apply interest to saving account", description = "Calculates and applies interest to a saving account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interest applied successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Not a saving account")
    })
    @PostMapping("/{accountId}/apply-interest")
    public void applyInterest(@PathVariable String accountId) throws BankAccountNotFoundException {
        bankAccountService.applyInterest(accountId);
    }

    @Operation(summary = "Update account status", description = "Changes the status of an account (e.g., activate, suspend)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{accountId}/status")
    public void updateAccountStatus(
            @PathVariable String accountId,
            @RequestParam AccountStatus status) throws BankAccountNotFoundException {
        bankAccountService.updateAccountStatus(accountId, status);
    }

    @Operation(summary = "Transfer between accounts", description = "Transfers money from one account to another")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer successful"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance")
    })
    @PostMapping("/transfer")
    public void transfer(
            @RequestParam String sourceAccountId,
            @RequestParam String destinationAccountId,
            @RequestParam double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        bankAccountService.transfer(sourceAccountId, destinationAccountId, amount);
    }
}