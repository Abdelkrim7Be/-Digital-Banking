package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.User;
import com.bellagnech.dig_bank.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find users by role
    List<User> findByRole(Role role);

    // Find users by role with pagination
    Page<User> findByRole(Role role, Pageable pageable);

    // Find enabled users
    List<User> findByEnabledTrue();

    // Find user by ID and role
    Optional<User> findByIdAndRole(Long id, Role role);

    // Count users by role
    long countByRole(Role role);

    // Count users by role and enabled status
    long countByRoleAndEnabled(Role role, boolean enabled);

    // Search users by role and username or email containing text
    Page<User> findByRoleAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        Role role, String username, String email, Pageable pageable);

    // Search users by role and username or email containing text (without pagination)
    List<User> findByRoleAndUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        Role role, String username, String email);
}
