package com.bellagnech.dig_bank.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bellagnech.dig_bank.entities.*;
import com.bellagnech.dig_bank.enums.OperationType;
import com.bellagnech.dig_bank.exceptions.BalanceNotSufficientException;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.mappers.BankAccountMapperImpl;
import com.bellagnech.dig_bank.dtos.AccountHistoryDTO;
import com.bellagnech.dig_bank.dtos.AccountOperationDTO;
import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CurrentBankAccountDTO;
import com.bellagnech.dig_bank.dtos.SavingBankAccountDTO;
import com.bellagnech.dig_bank.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl dtoMapper;

    // Customer Management
    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new customer: {}", customerDTO.getName());

        // Check if email already exists
        if (customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Customer with email " + customerDTO.getEmail() + " already exists");
        }

        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public List<CustomerDTO> listCustomersDTO() {
        log.info("Retrieving all customers");
        return customerRepository.findAll().stream()
                .map(dtoMapper::fromCustomer)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Retrieving customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Updating customer with ID: {}", customerDTO.getId());

        Customer existingCustomer = customerRepository.findById(customerDTO.getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Update fields
        existingCustomer.setName(customerDTO.getName());
        existingCustomer.setEmail(customerDTO.getEmail());
        existingCustomer.setPhone(customerDTO.getPhone());
        existingCustomer.setAddress(customerDTO.getAddress());

        Customer savedCustomer = customerRepository.save(existingCustomer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerID) throws CustomerNotFoundException {
        log.info("Deleting customer with ID: {}", customerID);

        Customer customer = customerRepository.findById(customerID)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Check if customer has accounts
        List<BankAccount> accounts = bankAccountRepository.findByCustomer(customer);
        if (!accounts.isEmpty()) {
            throw new IllegalStateException("Cannot delete customer with existing accounts");
        }

        customerRepository.delete(customer);
    }

    @Override
    public Page<CustomerDTO> getCustomersPageable(int page, int size) {
        log.info("Retrieving customers page {} with size {}", page, size);
        Page<Customer> customersPage = customerRepository.findAll(PageRequest.of(page, size));
        return customersPage.map(dtoMapper::fromCustomer);
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, int page, int size) {
        log.info("Searching customers with keyword: {} (page: {}, size: {})", keyword, page, size);
        Page<Customer> customersPage = customerRepository.searchCustomers(keyword, PageRequest.of(page, size));
        return customersPage.map(dtoMapper::fromCustomer);
    }

    // Account Management
    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId)
            throws CustomerNotFoundException {
        log.info("Creating current account for customer ID: {} with balance: {}", customerId, initialBalance);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        currentAccount.setCreateDate(new Date());
        currentAccount.setStatus(AccountStatus.CREATED);

        CurrentAccount savedAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId)
            throws CustomerNotFoundException {
        log.info("Creating saving account for customer ID: {} with balance: {}", customerId, initialBalance);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        savingAccount.setCreateDate(new Date());
        savingAccount.setStatus(AccountStatus.CREATED);

        SavingAccount savedAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedAccount);
    }

    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        log.info("Retrieving account with ID: {}", accountId);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        if (bankAccount instanceof SavingAccount) {
            return dtoMapper.fromSavingBankAccount((SavingAccount) bankAccount);
        } else {
            return dtoMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
        }
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        log.info("Retrieving all bank accounts");
        return bankAccountRepository.findAll().stream()
                .map(account -> {
                    if (account instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) account);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BankAccountDTO> getCustomerAccounts(Long customerId) throws CustomerNotFoundException {
        log.info("Retrieving accounts for customer ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        return bankAccountRepository.findByCustomer(customer).stream()
                .map(account -> {
                    if (account instanceof SavingAccount) {
                        return dtoMapper.fromSavingBankAccount((SavingAccount) account);
                    } else {
                        return dtoMapper.fromCurrentBankAccount((CurrentAccount) account);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void updateAccountStatus(String accountId, AccountStatus status) throws BankAccountNotFoundException {
        log.info("Updating status of account ID: {} to {}", accountId, status);
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        bankAccount.setStatus(status);
        bankAccountRepository.save(bankAccount);
    }

    // Banking Operations
    @Override
    @Transactional
    public void debit(String accountId, double amount, String description)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Debiting {} from account ID: {}", amount, accountId);

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        if (bankAccount.getBalance() < amount) {
            throw new BalanceNotSufficientException("Insufficient balance");
        }

        // Create operation record
        AccountOperation operation = new AccountOperation();
        operation.setType(OperationType.DEBIT);
        operation.setAmount(amount);
        operation.setDescription(description);
        operation.setOperationDate(new Date());
        operation.setBankAccount(bankAccount);
        accountOperationRepository.save(operation);

        // Update balance
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        log.info("Crediting {} to account ID: {}", amount, accountId);

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        // Create operation record
        AccountOperation operation = new AccountOperation();
        operation.setType(OperationType.CREDIT);
        operation.setAmount(amount);
        operation.setDescription(description);
        operation.setOperationDate(new Date());
        operation.setBankAccount(bankAccount);
        accountOperationRepository.save(operation);

        // Update balance
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    @Transactional
    public void transfer(String accountIdSource, String accountIdDestination, Double amount)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        log.info("Transferring {} from {} to {}", amount, accountIdSource, accountIdDestination);

        debit(accountIdSource, amount, "Transfer to " + accountIdDestination);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource);
    }

    @Override
    @Transactional
    public void applyInterest(String accountId) throws BankAccountNotFoundException {
        log.info("Applying interest to account ID: {}", accountId);

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        if (!(bankAccount instanceof SavingAccount)) {
            throw new IllegalArgumentException("Interest can only be applied to Saving Accounts");
        }

        SavingAccount savingAccount = (SavingAccount) bankAccount;
        double interestAmount = savingAccount.getBalance() * savingAccount.getInterestRate() / 100;

        // Record interest operation
        AccountOperation operation = new AccountOperation();
        operation.setType(OperationType.CREDIT);
        operation.setAmount(interestAmount);
        operation.setDescription("Interest Applied");
        operation.setOperationDate(new Date());
        operation.setBankAccount(savingAccount);
        accountOperationRepository.save(operation);

        // Update balance
        savingAccount.setBalance(savingAccount.getBalance() + interestAmount);
        bankAccountRepository.save(savingAccount);
    }

    // Account History and Operations
    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        log.info("Retrieving account history for account ID: {}", accountId);
        return accountOperationRepository.findByBankAccountId(accountId).stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size)
            throws BankAccountNotFoundException {
        log.info("Retrieving paginated account history for account ID: {} (page: {}, size: {})",
                accountId, page, size);

        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("Account not found"));

        Page<AccountOperation> operationsPage = accountOperationRepository
                .findByBankAccountId(accountId, PageRequest.of(page, size));

        List<AccountOperationDTO> operationDTOs = operationsPage.getContent().stream()
                .map(dtoMapper::fromAccountOperation)
                .collect(Collectors.toList());

        AccountHistoryDTO historyDTO = new AccountHistoryDTO();
        historyDTO.setAccountId(accountId);
        historyDTO.setBalance(bankAccount.getBalance());
        historyDTO.setAccountOperationDTOS(operationDTOs);
        historyDTO.setCurrentPage(page);
        historyDTO.setPageSize(size);
        historyDTO.setTotalPages(operationsPage.getTotalPages());

        return historyDTO;
    }
}
