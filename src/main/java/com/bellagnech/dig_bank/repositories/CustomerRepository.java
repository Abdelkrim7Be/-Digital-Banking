package com.bellagnech.dig_bank.repositories;

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

    //Find customers by name containing the given string (case insensitive)
    List<Customer> findByNameContainingIgnoreCase(String name);

    //Search customers by keyword in name or email
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.email LIKE %:keyword%")
    Page<Customer> searchCustomers(@Param("keyword") String keyword, Pageable pageable);

    //Check if a customer with the given email already exists
    boolean existsByEmail(String email);

    //Find customers by email containing the given string (case insensitive)
    List<Customer> findByEmailContainingIgnoreCase(String email);

    //Find customers by phone containing the given string
    List<Customer> findByPhoneContaining(String phone);
}