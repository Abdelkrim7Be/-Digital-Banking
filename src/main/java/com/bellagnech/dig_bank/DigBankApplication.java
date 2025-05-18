package com.bellagnech.dig_bank;

import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.enums.AccountStatus;
import com.bellagnech.dig_bank.enums.OperationType;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.security.services.SecurityService;
import com.bellagnech.dig_bank.services.BankAccountService;
import com.bellagnech.dig_bank.dtos.BankAccountDTO;
import com.bellagnech.dig_bank.dtos.CurrentBankAccountDTO;
import com.bellagnech.dig_bank.dtos.SavingBankAccountDTO;
import com.bellagnech.dig_bank.entities.AccountOperation;
import com.bellagnech.dig_bank.entities.CurrentAccount;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.entities.SavingAccount;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.UUID;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
public class DigBankApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigBankApplication.class, args);
	}

	@Bean
	@Profile("dev") // Only activate in dev profile
	public CommandLineRunner start(CustomerRepository customerRepository,
                           BankAccountRepository bankAccountRepository,
                           AccountOperationRepository accountOperationRepository) {
        return args -> {
            Stream.of("Abdelkrim","Soufiane","Mohamed").forEach(name -> {
                Customer customer = new Customer();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                customerRepository.save(customer);
            });
            customerRepository.findAll().forEach(customer -> {
                CurrentAccount currentAccount = new CurrentAccount();
                currentAccount.setId(UUID.randomUUID().toString());
                currentAccount.setCustomer(customer);
                currentAccount.setBalance(Math.random() * 90000);
                currentAccount.setCreateDate(new Date());
                currentAccount.setStatus(AccountStatus.CREATED);
                currentAccount.setOverDraft(9000);
                bankAccountRepository.save(currentAccount);


                SavingAccount savingAccount = new SavingAccount();
                savingAccount.setId(UUID.randomUUID().toString());
                savingAccount.setCustomer(customer);
                savingAccount.setBalance(Math.random() * 90000);
                savingAccount.setCreateDate(new Date());
                savingAccount.setStatus(AccountStatus.CREATED);
                savingAccount.setInterestRate(5.5);
                bankAccountRepository.save(savingAccount);
            });

            bankAccountRepository.findAll().forEach(acc -> {
                for (int i = 0; i<10 ; i++){
                    AccountOperation accountOperation = new AccountOperation();
                    accountOperation.setOperationDate(new Date());
                    accountOperation.setAmount(Math.random() * 12000);
                    accountOperation.setType(Math.random()>0.5? OperationType.DEBIT: OperationType.CREDIT);
                    accountOperation.setBankAccount(acc);
                    accountOperationRepository.save(accountOperation);
                }
            });
        };
    }
    
    //@Bean
    public CommandLineRunner commandLineRunner(BankAccountService bankAccountService) {
        return args -> {
            Stream.of("Manal","Hajjar","Aya").forEach(name -> {
                CustomerDTO customer = new CustomerDTO();
                customer.setName(name);
                customer.setEmail(name + "@gmail.com");
                bankAccountService.saveCustomer(customer);
            });
            bankAccountService.listCustomers().forEach(customer -> {
                try {
                    bankAccountService.saveCurrentBankAccount(Math.random()*90000, 9000, customer.getId());
                    bankAccountService.saveSavingBankAccount(Math.random()*12000, 5.5, customer.getId());
                } catch (CustomerNotFoundException e) {
                    e.printStackTrace();
                }
            });
            List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
            for (BankAccountDTO bankAccount : bankAccounts) {
                for (int i = 0; i < 10; i++) {
                    String accountId;
                    if (bankAccount instanceof SavingBankAccountDTO) {
                        accountId = ((SavingBankAccountDTO) bankAccount).getId();
                    } else {
                        accountId = ((CurrentBankAccountDTO) bankAccount).getId();
                    }
                    bankAccountService.credit(accountId, 10000 + Math.random() * 120000, "Credit");
                    bankAccountService.debit(accountId, 1000 + Math.random() * 9000, "Debit");

                }
            }
        };
    }
    
    /**
     * Initialize security data including roles and users
     */
    @Bean
    @Profile("dev") // Only activate in dev profile
    public CommandLineRunner initSecurityData(SecurityService securityService, PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("****** Initializing Security Data ******");
            
            // Create default roles
            securityService.createRoleIfNotFound("ADMIN");
            securityService.createRoleIfNotFound("USER");
            securityService.createRoleIfNotFound("MANAGER");
            securityService.createRoleIfNotFound("TELLER");
            securityService.createRoleIfNotFound("ACCOUNT_MANAGER");
            
            System.out.println("Default roles created successfully");
            
            // Create admin user if not exists
            String adminUsername = "admin";
            if (!securityService.userExists(adminUsername)) {
                securityService.registerUser(
                        adminUsername,
                        "admin@banking.com",
                        "Admin@123"
                );
                securityService.addRoleToUser(adminUsername, "ADMIN");
                System.out.println("Admin user created successfully");
            }
            
            // Create test users with different roles
            String[] testUsers = {"manager", "teller", "accountmgr", "user"};
            for (String username : testUsers) {
                if (!securityService.userExists(username)) {
                    securityService.registerUser(
                            username,
                            username + "@banking.com",
                            "Test@123"
                    );
                    
                    // Assign appropriate roles
                    securityService.addRoleToUser(username, "USER");
                    
                    switch (username) {
                        case "manager":
                            securityService.addRoleToUser(username, "MANAGER");
                            break;
                        case "teller":
                            securityService.addRoleToUser(username, "TELLER");
                            break;
                        case "accountmgr":
                            securityService.addRoleToUser(username, "ACCOUNT_MANAGER");
                            break;
                    }
                    
                    System.out.println("User " + username + " created successfully");
                }
            }
            
            System.out.println("****** Security Data Initialization Completed ******");
        };
    }
}
