package com.bellagnech.customer.controllers;

import com.bellagnech.customer.dtos.CustomerDTO;
import com.bellagnech.customer.exceptions.CustomerNotFoundException;
import com.bellagnech.customer.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        log.info("Retrieving all customers");
        List<CustomerDTO> customers = customerService.listCustomersDTO();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) throws CustomerNotFoundException {
        log.info("Retrieving customer with ID: {}", id);
        CustomerDTO customer = customerService.getCustomer(id);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        log.info("Creating new customer: {}", customerDTO.getName());
        CustomerDTO savedCustomer = customerService.saveCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Updating customer with ID: {}", id);
        customerDTO.setId(id);
        CustomerDTO updatedCustomer = customerService.updateCustomer(customerDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) throws CustomerNotFoundException {
        log.info("Deleting customer with ID: {}", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/page")
    public ResponseEntity<Page<CustomerDTO>> getCustomersPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Retrieving customers page {} with size {}", page, size);
        Page<CustomerDTO> customersPage = customerService.getCustomersPageable(page, size);
        return ResponseEntity.ok(customersPage);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Searching for customers with keyword: {} (page: {}, size: {})", keyword, page, size);
        Page<CustomerDTO> customersPage = customerService.searchCustomers(keyword, page, size);
        return ResponseEntity.ok(customersPage);
    }
}

