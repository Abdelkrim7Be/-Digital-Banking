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
import com.bellagnech.dig_bank.repositories.AppUserRepository;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.mappers.BankAccountMapperImpl;
import com.bellagnech.dig_bank.dtos.AccountHistoryDTO;
import com.bellagnech.dig_bank.dtos.AccountOperationDTO;
import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CurrentBankAccountDTO;
import com.bellagnech.dig_bank.dtos.SavingBankAccountDTO;
import com.bellagnech.dig_bank.security.exceptions.UserNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.bellagnech.dig_bank.enums.AccountStatus;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountServiceImpl implements BankAccountService {
    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private AppUserRepository appUserRepository;
    private BankAccountMapperImpl dtoMapper;

    // Save a new customer to the system
    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving new Customer");
        
        // Check if email already exists
        if (customerDTO.getId() == null && customerRepository.existsByEmail(customerDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + customerDTO.getEmail());
        }
        
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        
        // Set audit fields if provided
        if (customerDTO.getCreatedBy() != null) {
            customer.setCreatedBy(customerDTO.getCreatedBy());
            customer.setCreatedDate(new Date());
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    // Create a new current account with overdraft facility
    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        return saveCurrentBankAccount(initialBalance, overDraft, customerId, "system");
    }

    // Create a new current account with overdraft facility and track creator
    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId, String username) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if(customer == null)
            throw new CustomerNotFoundException("Customer not found");
            
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(initialBalance);
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        currentAccount.setCreateDate(new Date());
        currentAccount.setStatus(AccountStatus.CREATED);
        
        // Set creator information
        if (!"system".equals(username)) {
            AppUser creator = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            currentAccount.setCreatedBy(creator);
            currentAccount.setLastModifiedBy(creator);
            currentAccount.setLastModifiedDate(new Date());
        }
        
        CurrentAccount savedBankAccount = bankAccountRepository.save(currentAccount);
        return dtoMapper.fromCurrentBankAccount(savedBankAccount);
    }

    // Create a new savings account with interest rate
    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        return saveSavingBankAccount(initialBalance, interestRate, customerId, "system");
    }

    // Create a new savings account with interest rate and track creator
    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId, String username) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if(customer == null)
            throw new CustomerNotFoundException("Customer not found");
            
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(initialBalance);
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        savingAccount.setCreateDate(new Date());
        savingAccount.setStatus(AccountStatus.CREATED);
        
        // Set creator information
        if (!"system".equals(username)) {
            AppUser creator = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            savingAccount.setCreatedBy(creator);
            savingAccount.setLastModifiedBy(creator);
            savingAccount.setLastModifiedDate(new Date());
        }
        
        SavingAccount savedBankAccount = bankAccountRepository.save(savingAccount);
        return dtoMapper.fromSavingBankAccount(savedBankAccount);
    }

    // Get all customers from the database
    @Override
    public List<Customer> listCustomers() {
        return customerRepository.findAll();
    }

    // Get all customers as DTOs
    @Override
    public List<CustomerDTO> listCustomersDTO() {
        List<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOS = customers.stream()
                .map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());
        return customerDTOS;
    }

    // Find a bank account by its ID
    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(()->new BankAccountNotFoundException("BankAccount not found"));
        if (bankAccount instanceof SavingAccount) {
            SavingAccount savingAccount = (SavingAccount) bankAccount;
            return dtoMapper.fromSavingBankAccount(savingAccount);
        } else {
            CurrentAccount currentAccount = (CurrentAccount) bankAccount;
            return dtoMapper.fromCurrentBankAccount(currentAccount);
        }
    }

    // Withdraw money from an account
    @Override
    @Transactional
    public void debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountId, amount, description, "system");
    }

    // Withdraw money from an account and track user
    @Override
    @Transactional
    public void debit(String accountId, double amount, String description, String username) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
                
        if(bankAccount.getBalance() < amount)
            throw new BalanceNotSufficientException("Balance not sufficient");
            
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        
        accountOperationRepository.save(accountOperation);
        
        // Update account balance
        bankAccount.setBalance(bankAccount.getBalance() - amount);
        
        // Update last modified info
        if (!"system".equals(username)) {
            AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            bankAccount.setLastModifiedBy(user);
            bankAccount.setLastModifiedDate(new Date());
        }
        
        bankAccountRepository.save(bankAccount);
    }

    // Deposit money into an account
    @Override
    @Transactional
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        credit(accountId, amount, description, "system");
    }

    // Deposit money into an account and track user
    @Override
    @Transactional
    public void credit(String accountId, double amount, String description, String username) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
                
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setDescription(description);
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(bankAccount);
        
        accountOperationRepository.save(accountOperation);
        
        // Update account balance
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        
        // Update last modified info
        if (!"system".equals(username)) {
            AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            bankAccount.setLastModifiedBy(user);
            bankAccount.setLastModifiedDate(new Date());
        }
        
        bankAccountRepository.save(bankAccount);
    }

    // Transfer money between two accounts
    @Override
    @Transactional
    public void transfer(String accountIdSource, String accountIdDestination, Double amount)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        transfer(accountIdSource, accountIdDestination, amount, "system");
    }
    
    // Transfer money between two accounts and track user
    @Override
    @Transactional
    public void transfer(String accountIdSource, String accountIdDestination, Double amount, String username)
            throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource, amount, "Transfer to " + accountIdDestination, username);
        credit(accountIdDestination, amount, "Transfer from " + accountIdSource, username);
    }
    
    // List of all bank accounts 
    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS = bankAccounts.stream().map(bankAccount -> {
            if (bankAccount instanceof SavingAccount) {
                SavingAccount savingAccount = (SavingAccount) bankAccount;
                return dtoMapper.fromSavingBankAccount(savingAccount);
            } else {
                CurrentAccount currentAccount = (CurrentAccount) bankAccount;
                return dtoMapper.fromCurrentBankAccount(currentAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer Not found"));
        return dtoMapper.fromCustomer(customer);
    }

    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Updating Customer");
        
        // Check if customer exists
        if (!customerRepository.existsById(customerDTO.getId())) {
            throw new RuntimeException("Customer not found with ID: " + customerDTO.getId());
        }
        
        Customer customer = dtoMapper.fromCustomerDTO(customerDTO);
        
        // Set audit fields if provided
        if (customerDTO.getLastModifiedBy() != null) {
            customer.setLastModifiedBy(customerDTO.getLastModifiedBy());
            customer.setLastModifiedDate(new Date());
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.fromCustomer(savedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerID) {
        customerRepository.deleteById(customerID);
    }
    
    @Override
    public List<AccountOperationDTO> accountHistory(String accountId) {
        List<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations.stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
    }
    
    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId).orElse(null);
        if (bankAccount == null) throw new BankAccountNotFoundException("Account not Found");
        Page<AccountOperation> accountOperations = accountOperationRepository.findByBankAccountId(accountId, PageRequest.of(page, size));
        AccountHistoryDTO accountHistoryDTO = new AccountHistoryDTO();
        List<AccountOperationDTO> accountOperationsDTOS = accountOperations.getContent().stream().map(op -> dtoMapper.fromAccountOperation(op)).collect(Collectors.toList());
        accountHistoryDTO.setAccountOperationDTOS(accountOperationsDTOS);
        accountHistoryDTO.setAccountId(bankAccount.getId());
        accountHistoryDTO.setBalance(bankAccount.getBalance());
        accountHistoryDTO.setCurrentPage(page);
        accountHistoryDTO.setPageSize(size);
        accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
        return accountHistoryDTO;
    }
    
    @Override
    @Transactional
    public void applyInterest(String accountId) throws BankAccountNotFoundException {
        applyInterest(accountId, "system");
    }
    
    @Override
    @Transactional
    public void applyInterest(String accountId, String username) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
        
        if (!(bankAccount instanceof SavingAccount)) {
            throw new IllegalArgumentException("Interest can only be applied to Saving Accounts");
        }
        
        SavingAccount savingAccount = (SavingAccount) bankAccount;
        double interestAmount = savingAccount.getBalance() * savingAccount.getInterestRate() / 100;
        
        // Record the interest operation
        AccountOperation accountOperation = new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(interestAmount);
        accountOperation.setDescription("Interest Applied");
        accountOperation.setOperationDate(new Date());
        accountOperation.setBankAccount(savingAccount);
        
        accountOperationRepository.save(accountOperation);
        
        // Update account balance
        savingAccount.setBalance(savingAccount.getBalance() + interestAmount);
        
        // Update last modified info
        if (!"system".equals(username)) {
            AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            savingAccount.setLastModifiedBy(user);
            savingAccount.setLastModifiedDate(new Date());
        }
        
        bankAccountRepository.save(savingAccount);
        
        log.info("Interest applied to account: " + accountId + ", amount: " + interestAmount + " by " + username);
    }
    
    @Override
    @Transactional
    public void updateAccountStatus(String accountId, AccountStatus status) throws BankAccountNotFoundException {
        updateAccountStatus(accountId, status, "system");
    }
    
    @Override
    @Transactional
    public void updateAccountStatus(String accountId, AccountStatus status, String username) throws BankAccountNotFoundException {
        BankAccount bankAccount = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountNotFoundException("BankAccount not found"));
                
        bankAccount.setStatus(status);
        
        // Update last modified info
        if (!"system".equals(username)) {
            AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
            bankAccount.setLastModifiedBy(user);
            bankAccount.setLastModifiedDate(new Date());
        }
        
        bankAccountRepository.save(bankAccount);
        log.info("Account status updated for account: " + accountId + ", new status: " + status + " by " + username);
    }

    @Override
    public Page<CustomerDTO> getCustomersPageable(int page, int size) {
        Page<Customer> customersPage = customerRepository.findAll(PageRequest.of(page, size));
        List<CustomerDTO> customerDTOS = customersPage.getContent().stream()
                .map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());
        return new PageImpl<>(customerDTOS, PageRequest.of(page, size), customersPage.getTotalElements());
    }

    @Override
    public Page<CustomerDTO> searchCustomers(String keyword, int page, int size) {
        Page<Customer> customersPage = customerRepository.searchCustomers(keyword, PageRequest.of(page, size));
        List<CustomerDTO> customerDTOS = customersPage.getContent().stream()
                .map(customer -> dtoMapper.fromCustomer(customer))
                .collect(Collectors.toList());
        return new PageImpl<>(customerDTOS, PageRequest.of(page, size), customersPage.getTotalElements());
    }
}