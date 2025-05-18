package com.bellagnech.dig_bank.repositories;

import com.bellagnech.dig_bank.entities.AppUser;
import com.bellagnech.dig_bank.entities.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.email LIKE %:keyword%")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);
    
    boolean existsByEmail(String email);
    
    // New methods for user-customer relationship
    List<Customer> findByOwner(AppUser user);
    Page<Customer> findByOwner(AppUser user, Pageable pageable);
    
    @Query("SELECT c FROM Customer c WHERE (c.name LIKE %:keyword% OR c.email LIKE %:keyword%) AND c.owner = :user")
    Page<Customer> searchCustomersByOwner(@Param("keyword") String keyword, @Param("user") AppUser user, Pageable pageable);
    
    boolean existsByIdAndOwner(Long id, AppUser user);
}