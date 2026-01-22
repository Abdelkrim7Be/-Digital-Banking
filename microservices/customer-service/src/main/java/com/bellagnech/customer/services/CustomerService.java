package com.bellagnech.customer.services;

import com.bellagnech.customer.dtos.CustomerDTO;
import com.bellagnech.customer.entities.Customer;
import com.bellagnech.customer.exceptions.CustomerNotFoundException;
import com.bellagnech.customer.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving customer: {}", customerDTO.getName());
        Customer customer = toEntity(customerDTO);
        Customer saved = customerRepository.save(customer);
        return toDTO(saved);
    }

    public List<CustomerDTO> listCustomersDTO() {
        log.info("Retrieving all customers");
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Retrieving customer with ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        return toDTO(customer);
    }

    @Transactional
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) throws CustomerNotFoundException {
        log.info("Updating customer with ID: {}", customerDTO.getId());
        Customer existing = customerRepository.findById(customerDTO.getId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerDTO.getId()));
        
        existing.setName(customerDTO.getName());
        existing.setEmail(customerDTO.getEmail());
        existing.setPhone(customerDTO.getPhone());
        existing.setAddress(customerDTO.getAddress());
        
        Customer updated = customerRepository.save(existing);
        return toDTO(updated);
    }

    @Transactional
    public void deleteCustomer(Long customerId) throws CustomerNotFoundException {
        log.info("Deleting customer with ID: {}", customerId);
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }
        customerRepository.deleteById(customerId);
    }

    public Page<CustomerDTO> getCustomersPageable(int page, int size) {
        log.info("Retrieving customers page {} with size {}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<CustomerDTO> searchCustomers(String keyword, int page, int size) {
        log.info("Searching customers with keyword: {}", keyword);
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable)
                .map(this::toDTO);
    }

    private CustomerDTO toDTO(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        return dto;
    }

    private Customer toEntity(CustomerDTO dto) {
        Customer customer = new Customer();
        if (dto.getId() != null) {
            customer.setId(dto.getId());
        }
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setAddress(dto.getAddress());
        return customer;
    }
}

