package com.bellagnech.dig_bank.mappers;

import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.entities.Customer;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


//MapStruct : framwork qui fait mapper
@Service
public class BankAccountMapperImpl {
    public CustomerDTO fromCustomer(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        return customerDTO;
    }

    public Customer fromCustomerDTO(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        return customer;
    }
}
