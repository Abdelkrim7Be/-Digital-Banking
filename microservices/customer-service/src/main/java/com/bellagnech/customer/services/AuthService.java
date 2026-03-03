package com.bellagnech.customer.services;

import com.bellagnech.customer.dtos.AuthResponse;
import com.bellagnech.customer.dtos.LoginRequest;
import com.bellagnech.customer.dtos.RegisterRequest;
import com.bellagnech.customer.entities.Customer;
import com.bellagnech.customer.entities.User;
import com.bellagnech.customer.enums.Role;
import com.bellagnech.customer.events.CustomerCreatedEvent;
import com.bellagnech.customer.messaging.CustomerEventProducer;
import com.bellagnech.customer.repositories.CustomerRepository;
import com.bellagnech.customer.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerEventProducer eventProducer;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.CUSTOMER && request.getName() != null) {
            Customer customer = new Customer();
            customer.setName(request.getName());
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
            customer.setAddress(request.getAddress());
            customer.setUser(savedUser);
            Customer saved = customerRepository.save(customer);
            eventProducer.publishCustomerCreated(CustomerCreatedEvent.builder()
                    .customerId(saved.getId()).name(saved.getName()).email(saved.getEmail()).username(savedUser.getUsername()).build());
            log.info("Customer profile created for user: {}", savedUser.getUsername());
        }

        String jwtToken = jwtService.generateToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getUsername());
        return new AuthResponse(jwtToken, savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole());
    }

    public AuthResponse authenticate(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        String jwtToken = jwtService.generateToken(user);

        log.info("User authenticated successfully: {}", user.getUsername());
        return new AuthResponse(jwtToken, user.getUsername(), user.getEmail(), user.getRole());
    }
}

