package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import com.bellagnech.dig_bank.dtos.*;
import com.bellagnech.dig_bank.exceptions.BalanceNotSufficientException;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.services.BankAccountService;
import com.bellagnech.dig_bank.dtos.AccountHistoryDTO;
import com.bellagnech.dig_bank.enums.AccountStatus;
import com.bellagnech.dig_bank.security.services.SecurityService;
import com.bellagnech.dig_bank.entities.AppUser;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import org.springframework.data.domain.Page;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
@Tag(name = "Bank Account Management", description = "APIs for managing bank accounts and transactions")
@Slf4j
public class BankAccountRESTController {
    private BankAccountService bankAccountService;
    private SecurityService securityService;

    @Operation(summary = "Get account by ID", description = "Retrieves bank account details by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved account"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'TELLER')")
    public BankAccountDTO getBankAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        log.info("User {} is requesting account with ID: {}", getCurrentUsername(), accountId);
        return bankAccountService.getBankAccount(accountId);
    }

    @Operation(summary = "List all accounts", description = "Retrieves all bank accounts in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts list")
    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'TELLER')")
    public List<BankAccountDTO> listAccounts() {
        log.info("User {} is requesting all accounts", getCurrentUsername());
        return bankAccountService.bankAccountList();
    }
    
    @Operation(summary = "Create current account", description = "Creates a new current account with overdraft facility")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account successfully created"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/current")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_MANAGER')")
    public CurrentBankAccountDTO createCurrentAccount(
            @RequestParam double initialBalance,
            @RequestParam double overDraft,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        String username = getCurrentUsername();
        log.info("User {} is creating a current account for customer ID: {}", username, customerId);
        
        // Track user creating the account by passing username to service
        return bankAccountService.saveCurrentBankAccount(initialBalance, overDraft, customerId, username);
    }
    
    @Operation(summary = "Create saving account", description = "Creates a new saving account with interest rate")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account successfully created"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PostMapping("/saving")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_MANAGER')")
    public SavingBankAccountDTO createSavingAccount(
            @RequestParam double initialBalance,
            @RequestParam double interestRate,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        String username = getCurrentUsername();
        log.info("User {} is creating a saving account for customer ID: {}", username, customerId);
        
        // Track user creating the account by passing username to service
        return bankAccountService.saveSavingBankAccount(initialBalance, interestRate, customerId, username);
    }
    
    @Operation(summary = "Withdraw from account", description = "Performs a debit operation on an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance")
    })
    @PostMapping("/{accountId}/debit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER', 'ACCOUNT_MANAGER')")
    public void debit(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        String username = getCurrentUsername();
        log.info("User {} is performing a debit operation on account ID: {} for amount: {}", username, accountId, amount);
        
        // Track user performing the debit by passing username to service
        bankAccountService.debit(accountId, amount, description, username);
    }
    
    @Operation(summary = "Deposit to account", description = "Performs a credit operation on an account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deposit successful"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/{accountId}/credit")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER', 'ACCOUNT_MANAGER')")
    public void credit(
            @PathVariable String accountId,
            @RequestParam double amount,
            @RequestParam String description) throws BankAccountNotFoundException {
        String username = getCurrentUsername();
        log.info("User {} is performing a credit operation on account ID: {} for amount: {}", username, accountId, amount);
        
        // Track user performing the credit by passing username to service
        bankAccountService.credit(accountId, amount, description, username);
    }
    
    @Operation(summary = "Get account operations", description = "Retrieves all operations for a specific account")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved operations")
    @GetMapping("/{accountId}/operations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'TELLER')")
    public List<AccountOperationDTO> getHistory(@PathVariable String accountId) {
        log.info("User {} is requesting operations for account ID: {}", getCurrentUsername(), accountId);
        return bankAccountService.accountHistory(accountId);
    }
    
    @Operation(summary = "Get paginated account history", description = "Retrieves paginated operations for a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved operations"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/pageOperations")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER', 'TELLER')")
    public AccountHistoryDTO getAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        log.info("User {} is requesting paginated operations for account ID: {}", getCurrentUsername(), accountId);
        return bankAccountService.getAccountHistory(accountId, page, size);
    }

    @Operation(summary = "Apply interest to saving account", description = "Calculates and applies interest to a saving account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interest applied successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Not a saving account")
    })
    @PostMapping("/{accountId}/apply-interest")
    @PreAuthorize("hasRole('ADMIN')")
    public void applyInterest(@PathVariable String accountId) throws BankAccountNotFoundException {
        String username = getCurrentUsername();
        log.info("User {} is applying interest to account ID: {}", username, accountId);
        
        // Track user applying interest by passing username to service
        bankAccountService.applyInterest(accountId, username);
    }

    @Operation(summary = "Update account status", description = "Changes the status of an account (e.g., activate, suspend)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/{accountId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNT_MANAGER')")
    public void updateAccountStatus(
            @PathVariable String accountId,
            @RequestParam AccountStatus status) throws BankAccountNotFoundException {
        String username = getCurrentUsername();
        log.info("User {} is updating status of account ID: {} to {}", username, accountId, status);
        
        // Track user updating status by passing username to service
        bankAccountService.updateAccountStatus(accountId, status, username);
    }

    @Operation(summary = "Transfer between accounts", description = "Transfers money from one account to another")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer successful"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance")
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN', 'TELLER', 'ACCOUNT_MANAGER')")
    public void transfer(
            @RequestParam String sourceAccountId,
            @RequestParam String destinationAccountId,
            @RequestParam double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        String username = getCurrentUsername();
        log.info("User {} is transferring {} from account ID: {} to account ID: {}", 
                username, amount, sourceAccountId, destinationAccountId);
        
        // Track user performing transfer by passing username to service
        bankAccountService.transfer(sourceAccountId, destinationAccountId, amount, username);
    }
    
    @Operation(summary = "Get my operations", description = "Retrieves all operations performed by the current user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved operations")
    @GetMapping("/my-operations")
    @PreAuthorize("isAuthenticated()")
    public List<AccountOperationDTO> getMyOperations() {
        String username = getCurrentUsername();
        log.info("User {} is requesting their operations", username);
        return bankAccountService.accountOperationsByUser(username);
    }
    
    @Operation(summary = "Get my operations with pagination", description = "Retrieves paginated operations performed by the current user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved operations")
    @GetMapping("/my-operations/page")
    @PreAuthorize("isAuthenticated()")
    public Page<AccountOperationDTO> getMyOperationsPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        String username = getCurrentUsername();
        log.info("User {} is requesting their operations page {} with size {}", username, page, size);
        return bankAccountService.accountOperationsByUserPageable(username, page, size);
    }
    
    @Operation(summary = "Get my operations for account", description = "Retrieves operations performed by the current user on a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved operations"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/my-operations")
    @PreAuthorize("isAuthenticated()")
    public List<AccountOperationDTO> getMyOperationsForAccount(@PathVariable String accountId) throws BankAccountNotFoundException {
        String username = getCurrentUsername();
        log.info("User {} is requesting their operations for account ID: {}", username, accountId);
        return bankAccountService.accountOperationsByAccountAndUser(accountId, username);
    }
    
    @Operation(summary = "Get my paginated account history", description = "Retrieves paginated operations performed by the current user on a specific account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved operations"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountId}/my-history")
    @PreAuthorize("isAuthenticated()")
    public AccountHistoryDTO getMyAccountHistory(
            @PathVariable String accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) throws BankAccountNotFoundException {
        String username = getCurrentUsername();
        log.info("User {} is requesting their history for account ID: {}", username, accountId);
        return bankAccountService.getAccountHistoryByUser(accountId, username, page, size);
    }
    
    /**
     * Get the username of the currently authenticated user
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }
}