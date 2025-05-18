package com.bellagnech.dig_bank.security.services;

import com.bellagnech.dig_bank.entities.AppRole;
import com.bellagnech.dig_bank.entities.AppUser;
import com.bellagnech.dig_bank.repositories.AppUserRepository;
import com.bellagnech.dig_bank.security.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Custom implementation of UserDetailsService to load user-specific data from our database.
 * This service bridges our custom user entity with Spring Security's authentication system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    /**
     * Load a user by username for Spring Security authentication
     * @param username the username to look up
     * @return UserDetails object containing user information required by Spring Security
     * @throws UserNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        log.info("Loading user by username: {}", username);
        
        // Find user in database
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UserNotFoundException("User not found: " + username);
                });
        
        if (!user.isEnabled()) {
            log.warn("Attempted login with disabled account: {}", username);
            throw new UserNotFoundException("User account is disabled");
        }
        
        // Create collection of authorities/roles
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Add all user roles as authorities
        for (AppRole role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));
        }
        
        log.debug("User found: {}, with authorities: {}", username, authorities);
        
        // Create and return Spring UserDetails object
        return new User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),      // enabled
                true,                  // account not expired
                true,                  // credentials not expired
                true,                  // account not locked
                authorities
        );
    }
}
