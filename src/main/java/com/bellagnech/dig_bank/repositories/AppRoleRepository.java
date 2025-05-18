package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    
    /**
     * Find a role by its name
     * @param name the role name to search for
     * @return an Optional containing the role if found
     */
    Optional<AppRole> findByName(String name);
    
    /**
     * Check if a role name already exists
     * @param name the role name to check
     * @return true if the role exists, false otherwise
     */
    boolean existsByName(String name);
    
    /**
     * Find roles assigned to a specific user
     * @param userId the user ID to search for
     * @return list of roles assigned to the specified user
     */
    @Query("SELECT r FROM AppRole r JOIN r.users u WHERE u.id = :userId")
    List<AppRole> findRolesByUserId(@Param("userId") Long userId);
}
