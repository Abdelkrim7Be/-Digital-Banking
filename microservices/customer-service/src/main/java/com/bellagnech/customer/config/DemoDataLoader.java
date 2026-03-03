package com.bellagnech.customer.config;

import com.bellagnech.customer.entities.Customer;
import com.bellagnech.customer.entities.User;
import com.bellagnech.customer.enums.Role;
import com.bellagnech.customer.events.CustomerCreatedEvent;
import com.bellagnech.customer.messaging.CustomerEventProducer;
import com.bellagnech.customer.repositories.CustomerRepository;
import com.bellagnech.customer.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures demo users exist with password "password" using the same PasswordEncoder
 * used at login, so login works even if data.sql hash does not match.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataLoader implements ApplicationRunner {

    private static final String DEMO_PASSWORD = "password";

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerEventProducer eventProducer;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String encodedPassword = passwordEncoder.encode(DEMO_PASSWORD);

        ensureUser("admin", "admin@banque.fr", "Admin", "Système", Role.ADMIN, encodedPassword, null);
        ensureUser("marie.dupont", "marie.dupont@email.fr", "Marie", "Dupont", Role.CUSTOMER, encodedPassword, "Marie Dupont");
        ensureUser("jean.martin", "jean.martin@email.fr", "Jean", "Martin", Role.CUSTOMER, encodedPassword, "Jean Martin");
        ensureUser("sophie.bernard", "sophie.bernard@email.fr", "Sophie", "Bernard", Role.CUSTOMER, encodedPassword, "Sophie Bernard");
        // Ensure the demo customer used by the login button always has password "password"
        ensureUser("nadia.chakir", "nadia.chakir@email.fr", "Nadia", "Chakir", Role.CUSTOMER, encodedPassword, "Nadia Chakir");

        log.info("Demo users ready (username/password: admin/password, marie.dupont/password, etc.)");
    }

    private void ensureUser(String username, String email, String firstName, String lastName,
                           Role role, String encodedPassword, String customerName) {
        userRepository.findByUsername(username).ifPresentOrElse(
                user -> {
                    user.setPassword(encodedPassword);
                    userRepository.save(user);
                    log.debug("Updated password for demo user: {}", username);
                },
                () -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRole(role);
                    user.setPassword(encodedPassword);
                    user.setEnabled(true);
                    user.setAccountNonExpired(true);
                    user.setAccountNonLocked(true);
                    user.setCredentialsNonExpired(true);
                    User saved = userRepository.save(user);
                    if (role == Role.CUSTOMER && customerName != null) {
                        Customer customer = new Customer();
                        customer.setName(customerName);
                        customer.setEmail(email);
                        customer.setUser(saved);
                        Customer savedCustomer = customerRepository.save(customer);
                        eventProducer.publishCustomerCreated(CustomerCreatedEvent.builder()
                                .customerId(savedCustomer.getId())
                                .name(savedCustomer.getName())
                                .email(savedCustomer.getEmail())
                                .username(saved.getUsername())
                                .build());
                    }
                    log.debug("Created demo user: {}", username);
                }
        );
    }
}
