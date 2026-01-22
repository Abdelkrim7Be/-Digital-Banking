package com.bellagnech.transaction.services;

import com.bellagnech.transaction.clients.AccountServiceClient;
import com.bellagnech.transaction.dtos.AccountOperationDTO;
import com.bellagnech.transaction.entities.AccountOperation;
import com.bellagnech.transaction.enums.OperationType;
import com.bellagnech.transaction.exceptions.AccountNotFoundException;
import com.bellagnech.transaction.exceptions.BalanceNotSufficientException;
import com.bellagnech.transaction.repositories.AccountOperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final AccountOperationRepository operationRepository;
    private final AccountServiceClient accountServiceClient;

    @Transactional
    public void credit(String accountId, double amount, String description) throws AccountNotFoundException {
        log.info("Crediting account {} with amount {}", accountId, amount);
        
        AccountServiceClient.AccountDTO account = accountServiceClient.getAccount(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        double newBalance = account.balance + amount;
        updateAccountBalance(accountId, newBalance);

        AccountOperation operation = new AccountOperation();
        operation.setBankAccountId(accountId);
        operation.setAmount(amount);
        operation.setDescription(description);
        operation.setType(OperationType.CREDIT);
        operationRepository.save(operation);

        log.info("Credit operation completed for account {}", accountId);
    }

    @Transactional
    public void debit(String accountId, double amount, String description) 
            throws AccountNotFoundException, BalanceNotSufficientException {
        log.info("Debiting account {} with amount {}", accountId, amount);
        
        AccountServiceClient.AccountDTO account = accountServiceClient.getAccount(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        double currentBalance = account.balance;
        double overdraft = 0.0; // Could be fetched from account details if needed
        
        if (currentBalance + overdraft < amount) {
            throw new BalanceNotSufficientException(
                String.format("Insufficient balance. Current: %.2f, Required: %.2f", currentBalance, amount));
        }

        double newBalance = currentBalance - amount;
        updateAccountBalance(accountId, newBalance);

        AccountOperation operation = new AccountOperation();
        operation.setBankAccountId(accountId);
        operation.setAmount(amount);
        operation.setDescription(description);
        operation.setType(OperationType.DEBIT);
        operationRepository.save(operation);

        log.info("Debit operation completed for account {}", accountId);
    }

    @Transactional
    public void transfer(String sourceAccountId, String destinationAccountId, Double amount)
            throws AccountNotFoundException, BalanceNotSufficientException {
        log.info("Transferring {} from account {} to account {}", amount, sourceAccountId, destinationAccountId);
        
        debit(sourceAccountId, amount, "Transfer to " + destinationAccountId);
        credit(destinationAccountId, amount, "Transfer from " + sourceAccountId);
        
        log.info("Transfer completed successfully");
    }

    public List<AccountOperationDTO> getAccountHistory(String accountId) {
        log.info("Retrieving transaction history for account {}", accountId);
        return operationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<AccountOperationDTO> getAccountHistoryPaginated(String accountId, int page, int size) {
        log.info("Retrieving paginated transaction history for account {} (page: {}, size: {})", accountId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        return operationRepository.findByBankAccountIdOrderByOperationDateDesc(accountId, pageable)
                .map(this::toDTO);
    }

    private void updateAccountBalance(String accountId, double newBalance) {
        try {
            Map<String, Double> balanceUpdate = Map.of("balance", newBalance);
            accountServiceClient.updateBalance(accountId, balanceUpdate);
        } catch (Exception e) {
            log.error("Failed to update account balance for account {}: {}", accountId, e.getMessage());
            throw new RuntimeException("Failed to update account balance", e);
        }
    }

    private AccountOperationDTO toDTO(AccountOperation operation) {
        AccountOperationDTO dto = new AccountOperationDTO();
        dto.setId(operation.getId());
        dto.setOperationDate(operation.getOperationDate());
        dto.setAmount(operation.getAmount());
        dto.setDescription(operation.getDescription());
        dto.setType(operation.getType());
        dto.setBankAccountId(operation.getBankAccountId());
        dto.setPerformedBy(operation.getPerformedBy());
        return dto;
    }
}

