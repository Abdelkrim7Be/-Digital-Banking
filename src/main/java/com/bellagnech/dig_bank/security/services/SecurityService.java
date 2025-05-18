package com.bellagnech.dig_bank.security.services;

import com.bellagnech.dig_bank.entities.AppUser;
import com.bellagnech.dig_bank.security.dtos.RegisterRequest;

import java.util.List;

/**
 * Service interface for security-related operations
 */
public interface SecurityService {
    
    /**
     * Register a new user
     * @param registerRequest the registration request data
     * @return the created user
     */
    AppUser registerUser(RegisterRequest registerRequest);
    
    /**
     * Add a role to a user
     * @param username the username of the user
     * @param roleName the name of the role to add
     */
    void addRoleToUser(String username, String roleName);
    
    /**
     * Remove a role from a user
     * @param username the username of the user
     * @param roleName the name of the role to remove
     */
    void removeRoleFromUser(String username, String roleName);
    
    /**
     * Load a user by username
     * @param username the username to search for
     * @return the user if found
     */
    AppUser loadUserByUsername(String username);
    
    /**
     * Change a user's password
     * @param username the username of the user
     * @param oldPassword the current password
     * @param newPassword the new password
     * @return true if the password was changed successfully
     */
    boolean changePassword(String username, String oldPassword, String newPassword);
    
    /**
     * Get a list of all users
     * @return list of all users
     */
    List<AppUser> getAllUsers();
    
    /**
     * Get a user by their ID
     * @param userId the ID of the user
     * @return the user if found
     */
    AppUser getUserById(Long userId);
    
    /**
     * Enable or disable a user
     * @param username the username of the user
     * @param enabled the enabled status to set
     */
    void setUserEnabled(String username, boolean enabled);
}
