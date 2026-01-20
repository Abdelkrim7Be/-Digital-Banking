package com.bellagnech.dig_bank.web;

import com.bellagnech.dig_bank.dtos.*;
import com.bellagnech.dig_bank.enums.AccountStatus;
import com.bellagnech.dig_bank.exceptions.BankAccountNotFoundException;
import com.bellagnech.dig_bank.exceptions.CustomerNotFoundException;
import com.bellagnech.dig_bank.services.BankAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankAccountController.class)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankAccountService bankAccountService;

    @Autowired
    private ObjectMapper objectMapper;

    private CurrentBankAccountDTO currentAccountDTO;
    private SavingBankAccountDTO savingAccountDTO;
    private AccountHistoryDTO accountHistoryDTO;

    @BeforeEach
    void setUp() {
        currentAccountDTO = new CurrentBankAccountDTO();
        currentAccountDTO.setId("ACC001");
        currentAccountDTO.setBalance(1000.0);
        currentAccountDTO.setOverDraft(500.0);
        currentAccountDTO.setStatus(AccountStatus.CREATED);
        currentAccountDTO.setType("CURRENT");

        savingAccountDTO = new SavingBankAccountDTO();
        savingAccountDTO.setId("ACC002");
        savingAccountDTO.setBalance(5000.0);
        savingAccountDTO.setInterestRate(3.5);
        savingAccountDTO.setStatus(AccountStatus.CREATED);
        savingAccountDTO.setType("SAVING");

        accountHistoryDTO = new AccountHistoryDTO();
        accountHistoryDTO.setAccountId("ACC001");
        accountHistoryDTO.setBalance(1000.0);
        accountHistoryDTO.setCurrentPage(0);
        accountHistoryDTO.setPageSize(5);
        accountHistoryDTO.setTotalPages(1);
    }

    @Test
    void getAllAccounts_ShouldReturnListOfAccounts() throws Exception {
        List<BankAccountDTO> accounts = Arrays.asList(currentAccountDTO, savingAccountDTO);
        when(bankAccountService.bankAccountList()).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("ACC001"))
                .andExpect(jsonPath("$[0].balance").value(1000.0))
                .andExpect(jsonPath("$[1].id").value("ACC002"))
                .andExpect(jsonPath("$[1].balance").value(5000.0));

        verify(bankAccountService).bankAccountList();
    }

    @Test
    void getAccount_ShouldReturnAccount_WhenAccountExists() throws Exception {
        when(bankAccountService.getBankAccount("ACC001")).thenReturn(currentAccountDTO);

        mockMvc.perform(get("/api/accounts/ACC001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("ACC001"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.type").value("CURRENT"));

        verify(bankAccountService).getBankAccount("ACC001");
    }

    @Test
    void getAccount_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        when(bankAccountService.getBankAccount("INVALID")).thenThrow(new BankAccountNotFoundException("Account not found"));

        mockMvc.perform(get("/api/accounts/INVALID"))
                .andExpect(status().isNotFound());

        verify(bankAccountService).getBankAccount("INVALID");
    }

    @Test
    void getCustomerAccounts_ShouldReturnCustomerAccounts() throws Exception {
        List<BankAccountDTO> accounts = Arrays.asList(currentAccountDTO);
        when(bankAccountService.getCustomerAccounts(1L)).thenReturn(accounts);

        mockMvc.perform(get("/api/accounts/customer/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("ACC001"))
                .andExpect(jsonPath("$[0].balance").value(1000.0));

        verify(bankAccountService).getCustomerAccounts(1L);
    }

    @Test
    void createCurrentAccount_ShouldReturnCreatedAccount() throws Exception {
        when(bankAccountService.saveCurrentBankAccount(1000.0, 500.0, 1L)).thenReturn(currentAccountDTO);

        mockMvc.perform(post("/api/accounts/current")
                .param("initialBalance", "1000.0")
                .param("overDraft", "500.0")
                .param("customerId", "1"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("ACC001"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.overDraft").value(500.0));

        verify(bankAccountService).saveCurrentBankAccount(1000.0, 500.0, 1L);
    }

    @Test
    void createSavingAccount_ShouldReturnCreatedAccount() throws Exception {
        when(bankAccountService.saveSavingBankAccount(5000.0, 3.5, 1L)).thenReturn(savingAccountDTO);

        mockMvc.perform(post("/api/accounts/saving")
                .param("initialBalance", "5000.0")
                .param("interestRate", "3.5")
                .param("customerId", "1"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("ACC002"))
                .andExpect(jsonPath("$.balance").value(5000.0))
                .andExpect(jsonPath("$.interestRate").value(3.5));

        verify(bankAccountService).saveSavingBankAccount(5000.0, 3.5, 1L);
    }

    @Test
    void debit_ShouldReturnOk_WhenDebitSuccessful() throws Exception {
        doNothing().when(bankAccountService).debit("ACC001", 100.0, "Test withdrawal");

        mockMvc.perform(post("/api/accounts/ACC001/debit")
                .param("amount", "100.0")
                .param("description", "Test withdrawal"))
                .andExpect(status().isOk());

        verify(bankAccountService).debit("ACC001", 100.0, "Test withdrawal");
    }

    @Test
    void credit_ShouldReturnOk_WhenCreditSuccessful() throws Exception {
        doNothing().when(bankAccountService).credit("ACC001", 200.0, "Test deposit");

        mockMvc.perform(post("/api/accounts/ACC001/credit")
                .param("amount", "200.0")
                .param("description", "Test deposit"))
                .andExpect(status().isOk());

        verify(bankAccountService).credit("ACC001", 200.0, "Test deposit");
    }

    @Test
    void transfer_ShouldReturnOk_WhenTransferSuccessful() throws Exception {
        doNothing().when(bankAccountService).transfer("ACC001", "ACC002", 300.0);

        mockMvc.perform(post("/api/accounts/transfer")
                .param("sourceAccountId", "ACC001")
                .param("destinationAccountId", "ACC002")
                .param("amount", "300.0"))
                .andExpect(status().isOk());

        verify(bankAccountService).transfer("ACC001", "ACC002", 300.0);
    }

    @Test
    void getAccountHistory_ShouldReturnOperations() throws Exception {
        List<AccountOperationDTO> operations = Arrays.asList(
            createAccountOperationDTO(1L, 100.0, "Deposit"),
            createAccountOperationDTO(2L, 50.0, "Withdrawal")
        );
        when(bankAccountService.accountHistory("ACC001")).thenReturn(operations);

        mockMvc.perform(get("/api/accounts/ACC001/operations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(50.0));

        verify(bankAccountService).accountHistory("ACC001");
    }

    @Test
    void getAccountHistoryPaginated_ShouldReturnPagedHistory() throws Exception {
        when(bankAccountService.getAccountHistory("ACC001", 0, 5)).thenReturn(accountHistoryDTO);

        mockMvc.perform(get("/api/accounts/ACC001/history")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId").value("ACC001"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(5));

        verify(bankAccountService).getAccountHistory("ACC001", 0, 5);
    }

    @Test
    void applyInterest_ShouldReturnOk_WhenInterestApplied() throws Exception {
        doNothing().when(bankAccountService).applyInterest("ACC002");

        mockMvc.perform(post("/api/accounts/ACC002/apply-interest"))
                .andExpect(status().isOk());

        verify(bankAccountService).applyInterest("ACC002");
    }

    @Test
    void updateAccountStatus_ShouldReturnOk_WhenStatusUpdated() throws Exception {
        doNothing().when(bankAccountService).updateAccountStatus("ACC001", AccountStatus.ACTIVATED);

        mockMvc.perform(put("/api/accounts/ACC001/status")
                .param("status", "ACTIVATED"))
                .andExpect(status().isOk());

        verify(bankAccountService).updateAccountStatus("ACC001", AccountStatus.ACTIVATED);
    }

    private AccountOperationDTO createAccountOperationDTO(Long id, double amount, String description) {
        AccountOperationDTO dto = new AccountOperationDTO();
        dto.setId(id);
        dto.setAmount(amount);
        dto.setDescription(description);
        dto.setOperationDate(new Date());
        dto.setBankAccountId("ACC001");
        return dto;
    }
}
