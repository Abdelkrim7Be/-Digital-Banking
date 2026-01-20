package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.services.BankAccountService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

// REST Controller for Customer Management - Provides endpoints for CRUD operations on customers
@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing bank customers")
public class CustomerController {

    private final BankAccountService bankAccountService;

    @Operation(summary = "Get all customers", description = "Retrieves a list of all customers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        log.info("Retrieving all customers");
        List<CustomerDTO> customers = bankAccountService.listCustomersDTO();
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a specific customer by their ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customer"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomer(@PathVariable Long id) throws CustomerNotFoundException {
        log.info("Retrieving customer with ID: {}", id);
        CustomerDTO customer = bankAccountService.getCustomer(id);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Create new customer", description = "Creates a new customer in the system")
    @ApiResponse(responseCode = "201", description = "Customer successfully created")
    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        log.info("Creating new customer: {}", customerDTO.getName());
        CustomerDTO savedCustomer = bankAccountService.saveCustomer(customerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }

    @Operation(summary = "Update customer", description = "Updates an existing customer's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer successfully updated"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Updating customer with ID: {}", id);
        customerDTO.setId(id);
        CustomerDTO updatedCustomer = bankAccountService.updateCustomer(customerDTO);
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Delete customer", description = "Removes a customer from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Customer not found"),
        @ApiResponse(responseCode = "400", description = "Cannot delete customer with existing accounts")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) throws CustomerNotFoundException {
        log.info("Deleting customer with ID: {}", id);
        bankAccountService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get paginated customers", description = "Retrieves a paginated list of customers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customer page")
    @GetMapping("/page")
    public ResponseEntity<Page<CustomerDTO>> getCustomersPage(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Retrieving customers page {} with size {}", page, size);
        Page<CustomerDTO> customersPage = bankAccountService.getCustomersPageable(page, size);
        return ResponseEntity.ok(customersPage);
    }

    @Operation(summary = "Search customers", description = "Search customers by name or email")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved matching customers")
    @GetMapping("/search")
    public ResponseEntity<Page<CustomerDTO>> searchCustomers(
            @RequestParam(name = "keyword") String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Searching for customers with keyword: {} (page: {}, size: {})", keyword, page, size);
        Page<CustomerDTO> customersPage = bankAccountService.searchCustomers(keyword, page, size);
        return ResponseEntity.ok(customersPage);
    }
}
