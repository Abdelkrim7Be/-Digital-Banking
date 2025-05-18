package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    
    /**
     * Find a user by their username
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<AppUser> findByUsername(String username);
    
    /**
     * Find a user by their email
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    Optional<AppUser> findByEmail(String email);
    
    /**
     * Check if a username already exists
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email already exists
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by enabled status
     * @param enabled the enabled status to search for
     * @return list of users with the specified enabled status
     */
    List<AppUser> findByEnabled(boolean enabled);
    
    /**
     * Find users by role name
     * @param roleName the role name to search for
     * @return list of users with the specified role
     */
    @Query("SELECT u FROM AppUser u JOIN u.roles r WHERE r.name = :roleName")
    List<AppUser> findByRoleName(@Param("roleName") String roleName);
}
