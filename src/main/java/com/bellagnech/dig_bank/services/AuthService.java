package com.bellagnech.dig_bank.services;

import com.bellagnech.dig_bank.dtos.AuthResponse;
import com.bellagnech.dig_bank.dtos.LoginRequest;
import com.bellagnech.dig_bank.dtos.RegisterRequest;
import com.bellagnech.dig_bank.entities.Customer;
import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.enums.Role;
import com.bellagnech.dig_bank.repositories.CustomerRepository;
import com.bellagnech.dig_bank.repositories.UserRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Vérifier si l'utilisateur existe déjà
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Créer l'utilisateur
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        User savedUser = userRepository.save(user);

        // Si c'est un CUSTOMER et qu'on a des informations supplémentaires, créer un Customer
        if (request.getRole() == Role.CUSTOMER && request.getName() != null) {
            Customer customer = new Customer();
            customer.setName(request.getName());
            customer.setEmail(request.getEmail());
            customer.setPhone(request.getPhone());
            customer.setAddress(request.getAddress());
            customer.setUser(savedUser);
            customerRepository.save(customer);
        }

        // Générer le token JWT
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
