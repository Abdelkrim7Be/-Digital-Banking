package com.bellagnech.account.controllers;

import com.bellagnech.account.dtos.*;
import com.bellagnech.account.enums.AccountStatus;
import com.bellagnech.account.exceptions.BankAccountNotFoundException;
import com.bellagnech.account.exceptions.CustomerNotFoundException;
import com.bellagnech.account.services.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAccountStats() {
        log.info("Retrieving global account statistics");
        return ResponseEntity.ok(accountService.getAccountStats());
    }

    @GetMapping("/selection/list")
    public ResponseEntity<List<Map<String, Object>>> getAccountsForSelection() {
        log.info("Retrieving accounts for selection dropdown");
        return ResponseEntity.ok(accountService.getAccountsForSelection(false));
    }

    @GetMapping("/selection/list/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveAccountsForSelection() {
        log.info("Retrieving active accounts for selection dropdown");
        return ResponseEntity.ok(accountService.getAccountsForSelection(true));
    }

    @GetMapping
    public ResponseEntity<List<BankAccountDTO>> getAllAccounts() {
        log.info("Retrieving all accounts");
        return ResponseEntity.ok(accountService.bankAccountList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountDTO> getAccount(@PathVariable String id) throws BankAccountNotFoundException {
        log.info("Retrieving account with ID: {}", id);
        return ResponseEntity.ok(accountService.getBankAccount(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BankAccountDTO>> getCustomerAccounts(@PathVariable Long customerId) {
        log.info("Retrieving accounts for customer ID: {}", customerId);
        return ResponseEntity.ok(accountService.getCustomerAccounts(customerId));
    }

    @PostMapping("/current")
    public ResponseEntity<CurrentBankAccountDTO> createCurrentAccount(
            @RequestParam double initialBalance,
            @RequestParam double overDraft,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        log.info("Creating current account for customer ID: {}", customerId);
        CurrentBankAccountDTO account = accountService.saveCurrentBankAccount(initialBalance, overDraft, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/saving")
    public ResponseEntity<SavingBankAccountDTO> createSavingAccount(
            @RequestParam double initialBalance,
            @RequestParam double interestRate,
            @RequestParam Long customerId) throws CustomerNotFoundException {
        log.info("Creating saving account for customer ID: {}", customerId);
        SavingBankAccountDTO account = accountService.saveSavingBankAccount(initialBalance, interestRate, customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping
    public ResponseEntity<BankAccountDTO> createAccount(@Valid @RequestBody CreateAccountRequest request) 
            throws CustomerNotFoundException {
        log.info("Creating {} account for customer ID: {}", request.getAccountType(), request.getCustomerId());
        
        BankAccountDTO account;
        if ("CURRENT".equalsIgnoreCase(request.getAccountType())) {
            CurrentBankAccountDTO currentAccount = accountService.saveCurrentBankAccount(
                request.getInitialBalance(), 
                request.getOverdraft() != null ? request.getOverdraft() : 0.0, 
                request.getCustomerId());
            account = currentAccount;
        } else if ("SAVING".equalsIgnoreCase(request.getAccountType())) {
            SavingBankAccountDTO savingAccount = accountService.saveSavingBankAccount(
                request.getInitialBalance(), 
                request.getInterestRate() != null ? request.getInterestRate() : 0.0, 
                request.getCustomerId());
            account = savingAccount;
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BankAccountDTO> updateAccountStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> statusUpdate) throws BankAccountNotFoundException {
        log.info("Updating account {} status", id);
        String statusStr = statusUpdate.get("status");
        if (statusStr != null) {
            AccountStatus status = AccountStatus.valueOf(statusStr.toUpperCase());
            accountService.updateAccountStatus(id, status);
            BankAccountDTO updatedAccount = accountService.getBankAccount(id);
            return ResponseEntity.ok(updatedAccount);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, Object>> getAccountBalance(@PathVariable String id) 
            throws BankAccountNotFoundException {
        log.info("Retrieving balance for account ID: {}", id);
        BankAccountDTO account = accountService.getBankAccount(id);
        return ResponseEntity.ok(Map.of(
            "accountId", account.getId(),
            "balance", account.getBalance(),
            "status", account.getStatus()
        ));
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<Void> updateBalance(
            @PathVariable String id,
            @RequestBody Map<String, Double> balanceUpdate) throws BankAccountNotFoundException {
        log.info("Updating balance for account ID: {}", id);
        Double newBalance = balanceUpdate.get("balance");
        if (newBalance != null) {
            accountService.updateBalance(id, newBalance);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}

