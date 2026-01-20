package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.services.BankAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankAccountService bankAccountService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerDTO customerDTO;

    @BeforeEach
    void setUp() {
        customerDTO = new CustomerDTO();
        customerDTO.setId(1L);
        customerDTO.setName("John Doe");
        customerDTO.setEmail("john.doe@example.com");
        customerDTO.setPhone("1234567890");
        customerDTO.setAddress("123 Main St");
    }

    @Test
    void getAllCustomers_ShouldReturnListOfCustomers() throws Exception {
        List<CustomerDTO> customers = Arrays.asList(customerDTO);
        when(bankAccountService.listCustomersDTO()).thenReturn(customers);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(bankAccountService).listCustomersDTO();
    }

    @Test
    void getCustomer_ShouldReturnCustomer_WhenCustomerExists() throws Exception {
        when(bankAccountService.getCustomer(1L)).thenReturn(customerDTO);

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(bankAccountService).getCustomer(1L);
    }

    @Test
    void getCustomer_ShouldReturnNotFound_WhenCustomerDoesNotExist() throws Exception {
        when(bankAccountService.getCustomer(999L)).thenThrow(new CustomerNotFoundException("Customer not found"));

        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());

        verify(bankAccountService).getCustomer(999L);
    }

    @Test
    void createCustomer_ShouldReturnCreatedCustomer() throws Exception {
        CustomerDTO newCustomer = new CustomerDTO();
        newCustomer.setName("Jane Doe");
        newCustomer.setEmail("jane.doe@example.com");
        newCustomer.setPhone("0987654321");
        newCustomer.setAddress("456 Oak St");

        CustomerDTO savedCustomer = new CustomerDTO();
        savedCustomer.setId(2L);
        savedCustomer.setName("Jane Doe");
        savedCustomer.setEmail("jane.doe@example.com");
        savedCustomer.setPhone("0987654321");
        savedCustomer.setAddress("456 Oak St");

        when(bankAccountService.saveCustomer(any(CustomerDTO.class))).thenReturn(savedCustomer);

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.email").value("jane.doe@example.com"));

        verify(bankAccountService).saveCustomer(any(CustomerDTO.class));
    }

    @Test
    void createCustomer_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        CustomerDTO invalidCustomer = new CustomerDTO();
        invalidCustomer.setName(""); // Invalid: empty name
        invalidCustomer.setEmail("invalid-email"); // Invalid: bad email format

        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest());

        verify(bankAccountService, never()).saveCustomer(any(CustomerDTO.class));
    }

    @Test
    void updateCustomer_ShouldReturnUpdatedCustomer() throws Exception {
        CustomerDTO updatedCustomer = new CustomerDTO();
        updatedCustomer.setId(1L);
        updatedCustomer.setName("John Updated");
        updatedCustomer.setEmail("john.updated@example.com");
        updatedCustomer.setPhone("1111111111");
        updatedCustomer.setAddress("789 Pine St");

        when(bankAccountService.updateCustomer(any(CustomerDTO.class))).thenReturn(updatedCustomer);

        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(bankAccountService).updateCustomer(any(CustomerDTO.class));
    }

    @Test
    void deleteCustomer_ShouldReturnNoContent() throws Exception {
        doNothing().when(bankAccountService).deleteCustomer(1L);

        mockMvc.perform(delete("/api/customers/1"))
                .andExpect(status().isNoContent());

        verify(bankAccountService).deleteCustomer(1L);
    }

    @Test
    void getCustomersPage_ShouldReturnPagedCustomers() throws Exception {
        List<CustomerDTO> customers = Arrays.asList(customerDTO);
        Page<CustomerDTO> customerPage = new PageImpl<>(customers);

        when(bankAccountService.getCustomersPageable(0, 10)).thenReturn(customerPage);

        mockMvc.perform(get("/api/customers/page")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));

        verify(bankAccountService).getCustomersPageable(0, 10);
    }

    @Test
    void searchCustomers_ShouldReturnMatchingCustomers() throws Exception {
        List<CustomerDTO> customers = Arrays.asList(customerDTO);
        Page<CustomerDTO> customerPage = new PageImpl<>(customers);

        when(bankAccountService.searchCustomers("John", 0, 10)).thenReturn(customerPage);

        mockMvc.perform(get("/api/customers/search")
                .param("keyword", "John")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));

        verify(bankAccountService).searchCustomers("John", 0, 10);
    }
}
