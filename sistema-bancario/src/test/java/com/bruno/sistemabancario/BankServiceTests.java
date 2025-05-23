package com.bruno.sistemabancario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.bruno.sistemabancario.adapter.dtos.request.AccountOpeningDTO;
import com.bruno.sistemabancario.adapter.dtos.request.TransactionDTO;
import com.bruno.sistemabancario.adapter.dtos.response.AccountDTO;
import com.bruno.sistemabancario.adapter.dtos.response.BalanceDTO;
import com.bruno.sistemabancario.adapter.dtos.response.ReportDTO;
import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import com.bruno.sistemabancario.domain.exceptions.BadRequest;
import com.bruno.sistemabancario.domain.exceptions.ResourceNotFoundException;
import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.domain.model.Transaction;
import com.bruno.sistemabancario.domain.service.BankService;
import com.bruno.sistemabancario.infrastructure.repository.BankAccountRepository;
import com.bruno.sistemabancario.infrastructure.repository.TransactionRepository;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class BankServiceTests {

	@Mock
	private BankAccountRepository accountRepository;

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private BankService accountService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testCreateAccountShouldSetAutomaticFieldsCorrectly() {
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
	void testCheckBalanceByIDWhenIdExistsShouldReturnBalance() {
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
	void testCheckBalanceByIDWhenIdDoesNotExistShouldThrowException() {
		String id = "4134125";
		when(accountRepository.findById(id)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
			accountService.checkBalanceByID(id);
		});

		assertEquals("No account found for this ID!", exception.getMessage());
		verify(accountRepository, times(1)).findById(id);
	}

	@Test
	void testMoneyTransactionSuccess() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123456");
		request.setDestinationAccount("654321");
		request.setValue(new BigDecimal("100.00"));

		BankAccount sourceAccount = new BankAccount();
		sourceAccount.setAccountNumber("123456");
		sourceAccount.setBalance(new BigDecimal("500.00"));

		BankAccount destinationAccount = new BankAccount();
		destinationAccount.setAccountNumber("654321");
		destinationAccount.setBalance(new BigDecimal("200.00"));

		when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber("654321")).thenReturn(Optional.of(destinationAccount));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(accountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

		String result = accountService.moneyTransaction(request);

		assertEquals("Transaction approved successfully!", result);

		assertEquals(new BigDecimal("400.00"), sourceAccount.getBalance());
		assertEquals(new BigDecimal("300.00"), destinationAccount.getBalance());

		verify(transactionRepository, times(1)).save(any(Transaction.class));
		verify(accountRepository, times(2)).save(any(BankAccount.class));
	}

	@Test
	void testMoneyTransactionThrowsBadRequestWhenInsufficientBalance() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123456");
		request.setDestinationAccount("654321");
		request.setValue(new BigDecimal("1000.00"));

		BankAccount source = new BankAccount();
		source.setAccountNumber("123456");
		source.setBalance(new BigDecimal("100.00"));

		BankAccount destination = new BankAccount();
		destination.setAccountNumber("654321");
		destination.setBalance(new BigDecimal("500.00"));

		when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(source));
		when(accountRepository.findByAccountNumber("654321")).thenReturn(Optional.of(destination));

		BadRequest ex = assertThrows(BadRequest.class, () -> accountService.moneyTransaction(request));

		assertEquals("Insufficient source account balance!", ex.getMessage());
	}

	@Test
	void testMoneyTransactionThrowsExceptionWhenSourceAccountNotFound() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123456");
		request.setDestinationAccount("654321");
		request.setValue(new BigDecimal("100.00"));

		when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			accountService.moneyTransaction(request);
		});

		assertEquals("Incorrect account number!", ex.getMessage());

		verify(accountRepository, times(1)).findByAccountNumber("123456");
		verify(accountRepository, never()).findByAccountNumber("654321");
	}

	@Test
	void testMoneyTransactionThrowsExceptionWhenDestinationAccountNotFound() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123456");
		request.setDestinationAccount("654321");
		request.setValue(new BigDecimal("100.00"));

		BankAccount sourceAccount = new BankAccount();
		sourceAccount.setAccountNumber("123456");
		sourceAccount.setBalance(new BigDecimal("500.00"));

		when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber("654321")).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			accountService.moneyTransaction(request);
		});

		assertEquals("Incorrect account number!", ex.getMessage());

		verify(accountRepository, times(1)).findByAccountNumber("123456");
		verify(accountRepository, times(1)).findByAccountNumber("654321");
	}

	@Test
	void testListOfTransactionsSpecificAccountReturnsMappedPage() {
		String accountNumber = "123456";
		Pageable pageable = PageRequest.of(0, 10);

		BankAccount account = new BankAccount();
		account.setAccountNumber(accountNumber);
		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

		Transaction transaction1 = new Transaction();
		transaction1.setId("134124");
		transaction1.setSourceAccount(accountNumber);
		transaction1.setValue(new BigDecimal("100.00"));

		Transaction transaction2 = new Transaction();
		transaction2.setId("1231");
		transaction2.setSourceAccount(accountNumber);
		transaction2.setValue(new BigDecimal("200.00"));

		List<Transaction> transactions = List.of(transaction1, transaction2);
		Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, transactions.size());

		when(transactionRepository.findAllByAccountNumber(accountNumber, pageable)).thenReturn(transactionPage);

		Page<TransactionsUserDTO> result = accountService.listOfTransactionsSpecificAccount(accountNumber, pageable);

		assertFalse(result.isEmpty());
		assertEquals(transactions.size(), result.getNumberOfElements());

		assertEquals(transaction1.getId(), result.getContent().get(0).getId());
		assertEquals(transaction2.getId(), result.getContent().get(1).getId());

		verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
		verify(transactionRepository, times(1)).findAllByAccountNumber(accountNumber, pageable);
	}

	@Test
	void testListOfTransactionsSpecificAccountThrowsWhenAccountNotFound() {
		String accountNumber = "123456";
		Pageable pageable = PageRequest.of(0, 10);

		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			accountService.listOfTransactionsSpecificAccount(accountNumber, pageable);
		});

		assertEquals("Account not found!", ex.getMessage());

		verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
		verify(transactionRepository, never()).findAllByAccountNumber(anyString(), any());
	}

	@Test
	void testTransactionReversalSuccess() {
		String transactionId = "tx123";

		Transaction transaction = new Transaction();
		transaction.setId(transactionId);
		transaction.setStatus("APPROVED");
		transaction.setSourceAccount("111111");
		transaction.setDestinationAccount("222222");
		transaction.setValue(new BigDecimal("100"));

		BankAccount sourceAccount = new BankAccount();
		sourceAccount.setAccountNumber("111111");
		sourceAccount.setBalance(new BigDecimal("500"));

		BankAccount destinationAccount = new BankAccount();
		destinationAccount.setAccountNumber("222222");
		destinationAccount.setBalance(new BigDecimal("200"));

		when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
		when(accountRepository.findByAccountNumber("111111")).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber("222222")).thenReturn(Optional.of(destinationAccount));
		when(accountRepository.save(any(BankAccount.class))).thenAnswer(i -> i.getArgument(0));
		when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

		String result = accountService.transactionReversal(transactionId);

		assertEquals("Transfer successfully reversed!", result);

		assertEquals(new BigDecimal("600"), sourceAccount.getBalance());
		assertEquals(new BigDecimal("100"), destinationAccount.getBalance());

		assertEquals("REVERSED", transaction.getStatus());

		verify(transactionRepository).findById(transactionId);
		verify(accountRepository, times(2)).findByAccountNumber(anyString());
		verify(accountRepository, times(2)).save(any(BankAccount.class));
		verify(transactionRepository).save(transaction);
	}

	@Test
	void testTransactionReversalTransactionNotFound() {
		String transactionId = "tx123";
		when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			accountService.transactionReversal(transactionId);
		});

		assertEquals("Transaction not found!", ex.getMessage());
	}

	@Test
	void testTransactionReversalSourceAccountNotFound() {
		String transactionId = "tx123";

		Transaction transaction = new Transaction();
		transaction.setId(transactionId);
		transaction.setStatus("APPROVED");
		transaction.setSourceAccount("111111");
		transaction.setDestinationAccount("222222");
		transaction.setValue(new BigDecimal("100"));

		when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
		when(accountRepository.findByAccountNumber("111111")).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			accountService.transactionReversal(transactionId);
		});

		assertEquals("Incorrect account number!", ex.getMessage());

		verify(accountRepository).findByAccountNumber("111111");
		verify(accountRepository, never()).findByAccountNumber("222222");
	}

	@Test
	void testTransactionReversalDestinationAccountNotFound() {
		String transactionId = "tx123";

		Transaction transaction = new Transaction();
		transaction.setId(transactionId);
		transaction.setStatus("APPROVED");
		transaction.setSourceAccount("111111");
		transaction.setDestinationAccount("222222");
		transaction.setValue(new BigDecimal("100"));

		when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
		when(accountRepository.findByAccountNumber("111111")).thenReturn(Optional.of(new BankAccount()));
		when(accountRepository.findByAccountNumber("222222")).thenReturn(Optional.empty());

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			accountService.transactionReversal(transactionId);
		});

		assertEquals("Incorrect account number!", ex.getMessage());

		verify(accountRepository).findByAccountNumber("111111");
		verify(accountRepository).findByAccountNumber("222222");
	}

	@Test
	void testTransactionReversalInsufficientDestinationBalance() {
		String transactionId = "tx123";

		Transaction transaction = new Transaction();
		transaction.setId(transactionId);
		transaction.setStatus("APPROVED");
		transaction.setSourceAccount("111111");
		transaction.setDestinationAccount("222222");
		transaction.setValue(new BigDecimal("100"));

		BankAccount sourceAccount = new BankAccount();
		sourceAccount.setAccountNumber("111111");
		sourceAccount.setBalance(new BigDecimal("500"));

		BankAccount destinationAccount = new BankAccount();
		destinationAccount.setAccountNumber("222222");
		destinationAccount.setBalance(new BigDecimal("50"));

		when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
		when(accountRepository.findByAccountNumber("111111")).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber("222222")).thenReturn(Optional.of(destinationAccount));

		BadRequest ex = assertThrows(BadRequest.class, () -> {
			accountService.transactionReversal(transactionId);
		});

		assertEquals("Insufficient destination account balance!", ex.getMessage());
	}

	@Test
	void testTransactionReversalNotApprovedOrAlreadyReversed() {
		String transactionId = "tx123";

		Transaction transaction = new Transaction();
		transaction.setId(transactionId);
		transaction.setStatus("REVERSED");

		when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

		BadRequest ex = assertThrows(BadRequest.class, () -> {
			accountService.transactionReversal(transactionId);
		});

		assertEquals("Transaction is not approved or already reversed.", ex.getMessage());
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
