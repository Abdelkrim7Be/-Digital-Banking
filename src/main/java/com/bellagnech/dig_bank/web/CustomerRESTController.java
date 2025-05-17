package com.bellagnech.dig_bank.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.bellagnech.dig_bank.dtos.CustomerDTO;
import com.bellagnech.dig_bank.services.BankAccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
public class CustomerRESTController {
    private BankAccountService bankAccountService;
    @GetMapping("/customers")
    public List<CustomerDTO> customers() {
        return bankAccountService.listCustomersDTO();
    }
}