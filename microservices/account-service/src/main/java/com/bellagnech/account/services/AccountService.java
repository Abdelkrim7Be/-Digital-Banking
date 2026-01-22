package com.bellagnech.account.services;

import com.bellagnech.account.clients.CustomerServiceClient;
import com.bellagnech.account.dtos.*;
import com.bellagnech.account.entities.BankAccount;
import com.bellagnech.account.entities.CurrentAccount;
import com.bellagnech.account.entities.SavingAccount;
import com.bellagnech.account.enums.AccountStatus;
import com.bellagnech.account.exceptions.BankAccountNotFoundException;
import com.bellagnech.account.exceptions.CustomerNotFoundException;
import com.bellagnech.account.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerServiceClient customerServiceClient;

    @Transactional
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId)
            throws CustomerNotFoundException {
        log.info("Creating current account for customer ID: {} with balance: {} and overdraft: {}", 
                customerId, initialBalance, overDraft);
        
        // Validate customer exists
        try {
            customerServiceClient.getCustomer(customerId);
        } catch (Exception e) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }

        CurrentAccount account = new CurrentAccount();
        account.setId(UUID.randomUUID().toString());
        account.setBalance(initialBalance);
        account.setOverDraft(overDraft);
        account.setCustomerId(customerId);
        account.setStatus(AccountStatus.CREATED);

        CurrentAccount saved = bankAccountRepository.save(account);
        return toCurrentDTO(saved);
    }

    @Transactional
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId)
            throws CustomerNotFoundException {
        log.info("Creating saving account for customer ID: {} with balance: {} and interest rate: {}", 
                customerId, initialBalance, interestRate);
        
        // Validate customer exists
        try {
            customerServiceClient.getCustomer(customerId);
        } catch (Exception e) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }

        SavingAccount account = new SavingAccount();
        account.setId(UUID.randomUUID().toString());
        account.setBalance(initialBalance);
        account.setInterestRate(interestRate);
        account.setCustomerId(customerId);
        account.setStatus(AccountStatus.CREATED);

        SavingAccount saved = bankAccountRepository.save(account);
        return toSavingDTO(saved);
    }

    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        log.info("Retrieving account with ID: {}", accountId);
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found with ID: " + accountId));
        return toDTO(account);
    }

    public List<BankAccountDTO> bankAccountList() {
        log.info("Retrieving all accounts");
        return bankAccountRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BankAccountDTO> getCustomerAccounts(Long customerId) {
        log.info("Retrieving accounts for customer ID: {}", customerId);
        return bankAccountRepository.findByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAccountStatus(String accountId, AccountStatus status) throws BankAccountNotFoundException {
        log.info("Updating account {} status to {}", accountId, status);
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found with ID: " + accountId));
        account.setStatus(status);
        bankAccountRepository.save(account);
    }

    @Transactional
    public void updateBalance(String accountId, double newBalance) throws BankAccountNotFoundException {
        log.info("Updating account {} balance to {}", accountId, newBalance);
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found with ID: " + accountId));
        account.setBalance(newBalance);
        bankAccountRepository.save(account);
    }

    private BankAccountDTO toDTO(BankAccount account) {
        BankAccountDTO dto = new BankAccountDTO();
        dto.setId(account.getId());
        dto.setBalance(account.getBalance());
        dto.setCreateDate(account.getCreateDate());
        dto.setStatus(account.getStatus());
        dto.setCustomerId(account.getCustomerId());
        dto.setType(account.getClass().getSimpleName());
        return dto;
    }

    private CurrentBankAccountDTO toCurrentDTO(CurrentAccount account) {
        CurrentBankAccountDTO dto = new CurrentBankAccountDTO();
        dto.setId(account.getId());
        dto.setBalance(account.getBalance());
        dto.setCreateDate(account.getCreateDate());
        dto.setStatus(account.getStatus());
        dto.setCustomerId(account.getCustomerId());
        dto.setType("CurrentAccount");
        dto.setOverDraft(account.getOverDraft());
        return dto;
    }

    private SavingBankAccountDTO toSavingDTO(SavingAccount account) {
        SavingBankAccountDTO dto = new SavingBankAccountDTO();
        dto.setId(account.getId());
        dto.setBalance(account.getBalance());
        dto.setCreateDate(account.getCreateDate());
        dto.setStatus(account.getStatus());
        dto.setCustomerId(account.getCustomerId());
        dto.setType("SavingAccount");
        dto.setInterestRate(account.getInterestRate());
        return dto;
    }
}

