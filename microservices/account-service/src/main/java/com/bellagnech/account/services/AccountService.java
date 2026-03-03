package com.bellagnech.account.services;

import com.bellagnech.account.clients.CustomerServiceClient;
import com.bellagnech.account.dtos.*;
import com.bellagnech.account.entities.BankAccount;
import com.bellagnech.account.entities.CurrentAccount;
import com.bellagnech.account.entities.SavingAccount;
import com.bellagnech.account.enums.AccountStatus;
import com.bellagnech.account.events.AccountBalanceUpdatedEvent;
import com.bellagnech.account.events.AccountCreatedEvent;
import com.bellagnech.account.exceptions.BankAccountNotFoundException;
import com.bellagnech.account.exceptions.CustomerNotFoundException;
import com.bellagnech.account.messaging.AccountEventProducer;
import com.bellagnech.account.repositories.BankAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final BankAccountRepository bankAccountRepository;
    private final CustomerServiceClient customerServiceClient;
    private final AccountEventProducer eventProducer;

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
        publishAccountCreatedEvent(saved.getId(), customerId, "CurrentAccount", initialBalance, "CREATED");
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
        BankAccountDTO dto = toDTO(account);
        enrichWithCustomer(dto);
        return dto;
    }

    public List<BankAccountDTO> bankAccountList() {
        log.info("Retrieving all accounts");
        List<BankAccountDTO> list = bankAccountRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        enrichWithCustomerNames(list);
        return list;
    }

    public List<BankAccountDTO> getCustomerAccounts(Long customerId) {
        log.info("Retrieving accounts for customer ID: {}", customerId);
        List<BankAccountDTO> list = bankAccountRepository.findByCustomerId(customerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        enrichWithCustomerNames(list);
        return list;
    }

    private void enrichWithCustomer(BankAccountDTO dto) {
        if (dto == null || dto.getCustomerId() == null) return;
        try {
            CustomerServiceClient.CustomerDTO c = customerServiceClient.getCustomer(dto.getCustomerId());
            if (c != null) {
                dto.setCustomerName(c.name);
                dto.setCustomerEmail(c.email);
            }
        } catch (Exception e) {
            log.debug("Could not resolve customer {}: {}", dto.getCustomerId(), e.getMessage());
        }
    }

    private void enrichWithCustomerNames(List<BankAccountDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return;
        Map<Long, CustomerServiceClient.CustomerDTO> cache = new HashMap<>();
        for (BankAccountDTO dto : dtos) {
            if (dto.getCustomerId() == null) continue;
            CustomerServiceClient.CustomerDTO c = cache.get(dto.getCustomerId());
            if (c == null) {
                try {
                    c = customerServiceClient.getCustomer(dto.getCustomerId());
                    if (c != null) cache.put(dto.getCustomerId(), c);
                } catch (Exception e) {
                    log.debug("Could not resolve customer {}: {}", dto.getCustomerId(), e.getMessage());
                }
            }
            if (c != null) {
                dto.setCustomerName(c.name);
                dto.setCustomerEmail(c.email);
            }
        }
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

        double previousBalance = account.getBalance();
        account.setBalance(newBalance);
        bankAccountRepository.save(account);

        // Publish balance updated event
        AccountBalanceUpdatedEvent event = AccountBalanceUpdatedEvent.builder()
                .eventType("BALANCE_UPDATED")
                .aggregateId(accountId)
                .accountId(accountId)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .reason("Balance update")
                .initiatedBy("system")
                .build();
        eventProducer.publishBalanceUpdated(event);
    }

    private void publishAccountCreatedEvent(String accountId, Long customerId, String accountType, double initialBalance, String status) {
        String customerEmail = null;
        String customerName = null;
        try {
            var c = customerServiceClient.getCustomer(customerId);
            if (c != null) {
                customerEmail = c.email;
                customerName = c.name;
            }
        } catch (Exception e) {
            log.debug("Could not resolve customer for event: {}", e.getMessage());
        }
        AccountCreatedEvent event = AccountCreatedEvent.builder()
                .eventType("ACCOUNT_CREATED")
                .aggregateId(accountId)
                .accountId(accountId)
                .customerId(customerId)
                .accountType(accountType)
                .initialBalance(initialBalance)
                .status(status)
                .customerEmail(customerEmail)
                .customerName(customerName)
                .build();
        eventProducer.publishAccountCreated(event);
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

