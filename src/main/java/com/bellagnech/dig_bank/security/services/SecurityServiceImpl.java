package com.bellagnech.dig_bank.security.services;

import com.bellagnech.dig_bank.entities.AppRole;
import com.bellagnech.dig_bank.entities.AppUser;
import com.bellagnech.dig_bank.repositories.AppRoleRepository;
import com.bellagnech.dig_bank.repositories.AppUserRepository;
import com.bellagnech.dig_bank.security.dtos.RegisterRequest;
import com.bellagnech.dig_bank.security.exceptions.InvalidCredentialsException;
import com.bellagnech.dig_bank.security.exceptions.RoleNotFoundException;
import com.bellagnech.dig_bank.security.exceptions.UserAlreadyExistsException;
import com.bellagnech.dig_bank.security.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the SecurityService interface
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SecurityServiceImpl implements SecurityService {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public AppUser registerUser(RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getUsername());
        
        // Check if username or email already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        }
        
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + registerRequest.getEmail());
        }
        
        // Create new user
        AppUser newUser = new AppUser();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setEmail(registerRequest.getEmail());
        newUser.setEnabled(true);
        newUser.setRoles(new ArrayList<>());
        
        // Set audit information
        String currentUsername = getCurrentUsername();
        Date now = new Date();
        newUser.setCreatedBy(currentUsername);
        newUser.setCreatedDate(now);
        newUser.setLastModifiedBy(currentUsername);
        newUser.setLastModifiedDate(now);
        
        // Save user
        return userRepository.save(newUser);
    }
    
    @Override
    public void addRoleToUser(String username, String roleName) {
        log.info("Adding role {} to user {}", roleName, username);
        
        // Find user and role
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
                
        AppRole role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleName));
        
        // Check if user already has the role
        if (user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName))) {
            log.info("User {} already has role {}", username, roleName);
            return;
        }
        
        // Add role to user
        user.getRoles().add(role);
        
        // Update audit information
        user.setLastModifiedBy(getCurrentUsername());
        user.setLastModifiedDate(new Date());
    }
    
    @Override
    public void removeRoleFromUser(String username, String roleName) {
        log.info("Removing role {} from user {}", roleName, username);
        
        // Find user and role
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        // Check if user has the role
        if (user.getRoles().stream().noneMatch(r -> r.getName().equals(roleName))) {
            log.info("User {} does not have role {}", username, roleName);
            return;
        }
        
        // Remove role from user
        user.getRoles().removeIf(r -> r.getName().equals(roleName));
        
        // Update audit information
        user.setLastModifiedBy(getCurrentUsername());
        user.setLastModifiedDate(new Date());
    }
    
    @Override
    public AppUser loadUserByUsername(String username) {
        log.info("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }
    
    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", username);
        
        // Find user
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }
        
        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        
        // Update audit information
        user.setLastModifiedBy(getCurrentUsername());
        user.setLastModifiedDate(new Date());
        
        userRepository.save(user);
        return true;
    }
    
    @Override
    public List<AppUser> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll();
    }
    
    @Override
    public AppUser getUserById(Long userId) {
        log.info("Getting user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }
    
    @Override
    public void setUserEnabled(String username, boolean enabled) {
        log.info("Setting user {} enabled status to {}", username, enabled);
        
        // Find user
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        
        // Set enabled status
        user.setEnabled(enabled);
        
        // Update audit information
        user.setLastModifiedBy(getCurrentUsername());
        user.setLastModifiedDate(new Date());
        
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public AppUser updateUser(AppUser user) {
        log.info("Updating user: {}", user.getUsername());
        
        // Find existing user
        AppUser existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + user.getId()));
        
        // Only update allowed fields
        existingUser.setEmail(user.getEmail());
        
        // Update audit information
        existingUser.setLastModifiedBy(getCurrentUsername());
        existingUser.setLastModifiedDate(new Date());
        
        return userRepository.save(existingUser);
    }
    
    /**
     * Get the username of the currently authenticated user
     * @return the username, or "system" if no user is authenticated
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    public AppRole createRoleIfNotFound(String name) {
        Optional<AppRole> optionalRole = roleRepository.findByName(name);
        if (optionalRole.isPresent()) {
            return optionalRole.get();
        }
        
        AppRole role = new AppRole();
        role.setName(name);
        return roleRepository.save(role);
    }
    
    @Override
    public AppUser registerUser(String username, String email, String password) {
        // Check if username is already taken
        if (userRepository.existsByUsername(username)) {
            log.warn("Username {} already exists", username);
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        
        // Check if email is already taken
        if (userRepository.existsByEmail(email)) {
            log.warn("Email {} already exists", email);
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }
        
        // Create new user
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedDate(new Date());
        user.setEnabled(true);
        
        log.info("Registering new user: {}", username);
        return userRepository.save(user);
    }
}
