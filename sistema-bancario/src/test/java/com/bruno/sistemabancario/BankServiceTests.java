package com.bruno.sistemabancario;

import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.adapter.dtos.response.ReportDTO;
import com.bruno.sistemabancario.application.ports.output.BankAccountRepositoryPort;
import com.bruno.sistemabancario.application.ports.output.TransactionRepositoryPort;
import com.bruno.sistemabancario.application.service.BankService;
import com.bruno.sistemabancario.domain.exceptions.ResourceNotFoundException;
import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.domain.utils.Code;
import com.bruno.sistemabancario.domain.utils.CustomMessageResolver;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class BankServiceTests {

    @Mock
    private BankAccountRepositoryPort accountRepository;

    @Mock
    private CustomMessageResolver customMessageResolver;

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private BankService accountService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccount() {
        AccountOpeningDTO request = new AccountOpeningDTO();
        request.setName("João Silva");
        request.setCpf("12345678901");

        when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountDTO result = accountService.createAccount(request);

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(accountRepository).save(captor.capture());
        BankAccount savedEntity = captor.getValue();

        assertEquals("João Silva", savedEntity.getName());
        assertEquals("12345678901", savedEntity.getCpf());
        assertEquals(BigDecimal.valueOf(1000), savedEntity.getBalance());
        assertEquals(LocalDate.now(), savedEntity.getOpeningDate());

        assertNotNull(savedEntity.getAccountNumber());
        assertEquals(6, savedEntity.getAccountNumber().length());
        assertTrue(savedEntity.getAccountNumber().matches("\\d{6}"));
    }

    @Test
    void testCheckBalanceByID() {
        String id = "abc123";
        BankAccount account = new BankAccount();
        account.setId(id);
        account.setBalance(BigDecimal.valueOf(2500.50));

        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        BalanceDTO result = accountService.checkBalanceByID(id);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2500.50), result.getBalance());

        verify(accountRepository, times(1)).findById(id);
    }

    @Test
    void testCheckBalanceByIDWhenIdDoesNotExist() {
        String id = "4134125";
        when(accountRepository.findById(id)).thenReturn(Optional.empty());
        when(customMessageResolver.getMessage(Code.NO_ACCOUNT_FOR_ID))
                .thenReturn("No account found for this ID!");

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            accountService.checkBalanceByID(id);
        });

        assertEquals("No account found for this ID!", exception.getMessage());
        verify(accountRepository, times(1)).findById(id);
    }

    @Test
    void testBankReport() {
        long totalAccounts = 5L;
        long totalTransactions = 20L;
        long totalTransactionsApproved = 5L;
        long totalTransactionsReversed = 2L;
        BigDecimal totalAmountMoved = new BigDecimal("12345.67");

        when(accountRepository.count()).thenReturn(totalAccounts);
        when(transactionRepository.count()).thenReturn(totalTransactions);
        when(transactionRepository.countByStatus("APPROVED")).thenReturn(totalTransactionsApproved);
        when(transactionRepository.countByStatus("REVERSED")).thenReturn(totalTransactionsReversed);

        Document aggregationResult = new Document();
        aggregationResult.put("totalValue", totalAmountMoved);
        AggregationResults<Document> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getUniqueMappedResult()).thenReturn(aggregationResult);

        when(mongoTemplate.aggregate(any(Aggregation.class), eq("transactions"), eq(Document.class)))
                .thenReturn(aggregationResults);

        ReportDTO report = accountService.bankReport();

        assertEquals(totalAccounts, report.getTotalAccounts());
        assertEquals(totalTransactions, report.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(totalTransactionsApproved), report.getTotalTransactionsApproved());
        assertEquals(BigDecimal.valueOf(totalTransactionsReversed), report.getTotalTransactionsReversed());
        assertEquals(totalAmountMoved, report.getTotalAmountMoved());

        verify(accountRepository).count();
        verify(transactionRepository).count();
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("transactions"), eq(Document.class));
    }

}
