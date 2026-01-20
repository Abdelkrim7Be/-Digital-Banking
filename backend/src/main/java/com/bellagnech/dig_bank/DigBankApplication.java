package com.bellagnech.dig_bank;

import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.enums.AccountStatus;
import com.bellagnech.dig_bank.enums.OperationType;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.repositories.AccountOperationRepository;
import com.bellagnech.dig_bank.repositories.BankAccountRepository;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.UserRepository;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.enums.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
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
	public CommandLineRunner start(CustomerRepository customerRepository,
                           BankAccountRepository bankAccountRepository,
                           AccountOperationRepository accountOperationRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@digbank.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                admin.setAccountNonExpired(true);
                admin.setAccountNonLocked(true);
                admin.setCredentialsNonExpired(true);
                userRepository.save(admin);
            }

            Stream.of("Abdelkrim","Soufiane","Mohamed").forEach(name -> {
                String username = name.toLowerCase();
                if (!userRepository.existsByUsername(username)) {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(name + "@gmail.com");
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setRole(Role.CUSTOMER);
                    user.setEnabled(true);
                    user.setAccountNonExpired(true);
                    user.setAccountNonLocked(true);
                    user.setCredentialsNonExpired(true);
                    User savedUser = userRepository.save(user);

                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setEmail(name + "@gmail.com");
                    customer.setUser(savedUser);
                    customerRepository.save(customer);
                }
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
                    OperationType type = Math.random()>0.5? OperationType.DEBIT: OperationType.CREDIT;
                    accountOperation.setType(type);
                    accountOperation.setDescription(type == OperationType.CREDIT ? "Initial Credit" : "Initial Debit");
                    accountOperation.setBankAccount(acc);
                    accountOperationRepository.save(accountOperation);
                }
            });
        };
    }


}
