package com.bruno.sistemabancario;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.bruno.sistemabancario.adapter.dtos.request.TransactionDTO;
import com.bruno.sistemabancario.adapter.dtos.response.TransactionsUserDTO;
import com.bruno.sistemabancario.application.ports.output.BankAccountRepositoryPort;
import com.bruno.sistemabancario.application.ports.output.TransactionRepositoryPort;
import com.bruno.sistemabancario.application.service.TransferService;
import com.bruno.sistemabancario.domain.exceptions.BadRequest;
import com.bruno.sistemabancario.domain.exceptions.ResourceNotFoundException;
import com.bruno.sistemabancario.domain.model.BankAccount;
import com.bruno.sistemabancario.domain.model.Transaction;
import com.bruno.sistemabancario.application.service.BankService;
import com.bruno.sistemabancario.domain.utils.Code;
import com.bruno.sistemabancario.domain.utils.CustomMessageResolver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@SpringBootTest
class TransferServiceTests {

	@Mock
	private BankAccountRepositoryPort accountRepository;

	@Mock
	private CustomMessageResolver customMessageResolver;

	@InjectMocks
	private TransferService transferService;

	@Mock
	private TransactionRepositoryPort transactionRepository;

	@InjectMocks
	private BankService accountService;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);
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

		when(customMessageResolver.getMessage(Code.TRANSACTION_APPROVED_SUCCESS))
				.thenReturn("Transaction approved successfully!");

		String result = transferService.moneyTransaction(request);

		assertEquals("Transaction approved successfully!", result);

		assertEquals(new BigDecimal("400.00"), sourceAccount.getBalance());
		assertEquals(new BigDecimal("300.00"), destinationAccount.getBalance());

		verify(transactionRepository, times(1)).save(any(Transaction.class));
		verify(accountRepository, times(2)).save(any(BankAccount.class));
	}

	@Test
	void testMoneyTransactionBadRequestWhenInsufficientBalance() {
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
		when(customMessageResolver.getMessage(Code.INSUFFICIENT_BALANCE))
				.thenReturn("Insufficient balance!");

		BadRequest ex = assertThrows(BadRequest.class, () -> transferService.moneyTransaction(request));

		assertEquals("Insufficient balance!", ex.getMessage());
	}

	@Test
	void testMoneyTransactionExceptionWhenSourceAccountNotFound() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123456");
		request.setDestinationAccount("654321");
		request.setValue(new BigDecimal("100.00"));

		when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.empty());
		when(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND))
				.thenReturn("Incorrect account number!");

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Incorrect account number!", ex.getMessage());

		verify(accountRepository, times(1)).findByAccountNumber("123456");
		verify(accountRepository, never()).findByAccountNumber("654321");
	}

	@Test
	void testMoneyTransactionExceptionWhenDestinationAccountNotFound() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123456");
		request.setDestinationAccount("654321");
		request.setValue(new BigDecimal("100.00"));

		BankAccount sourceAccount = new BankAccount();
		sourceAccount.setAccountNumber("123456");
		sourceAccount.setBalance(new BigDecimal("500.00"));

		when(accountRepository.findByAccountNumber("123456")).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber("654321")).thenReturn(Optional.empty());
		when(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND))
				.thenReturn("Incorrect account number!");

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Incorrect account number!", ex.getMessage());

		verify(accountRepository, times(1)).findByAccountNumber("123456");
		verify(accountRepository, times(1)).findByAccountNumber("654321");
	}

	@Test
	void testListOfTransactionsSpecificAccount() {
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

		Page<TransactionsUserDTO> result = transferService.listOfTransactionsSpecificAccount(accountNumber, pageable);

		assertFalse(result.isEmpty());
		assertEquals(transactions.size(), result.getNumberOfElements());

		assertEquals(transaction1.getId(), result.getContent().get(0).getId());
		assertEquals(transaction2.getId(), result.getContent().get(1).getId());

		verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
		verify(transactionRepository, times(1)).findAllByAccountNumber(accountNumber, pageable);
	}

	@Test
	void testListOfTransactionsSpecificAccountWhenAccountNotFound() {
		String accountNumber = "123456";
		Pageable pageable = PageRequest.of(0, 10);

		when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());
		when(customMessageResolver.getMessage(Code.NUMBER_ACCOUNT_NOT_FOUND))
				.thenReturn("Account not found!");

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			transferService.listOfTransactionsSpecificAccount(accountNumber, pageable);
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
		when(customMessageResolver.getMessage(Code.TRANSACTION_REVERSED_SUCCESS))
				.thenReturn("Transfer successfully reversed!");

		String result = transferService.transactionReversal(transactionId);

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
		when(customMessageResolver.getMessage(Code.TRANSACTION_NOT_FOUND))
				.thenReturn("Transaction not found!");

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			transferService.transactionReversal(transactionId);
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
		when(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND))
				.thenReturn("Incorrect account number!");

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			transferService.transactionReversal(transactionId);
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
		when(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND))
				.thenReturn("Incorrect account number!");

		ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> {
			transferService.transactionReversal(transactionId);
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
		when(customMessageResolver.getMessage(Code.DESTINATION_ACCOUNT_INSUFFICIENT_BALANCE))
				.thenReturn("Insufficient destination account balance!");

		BadRequest ex = assertThrows(BadRequest.class, () -> {
			transferService.transactionReversal(transactionId);
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
		when(customMessageResolver.getMessage(Code.TRANSACTION_NOT_APPROVED))
				.thenReturn("Transaction is not approved or already reversed.");

		BadRequest ex = assertThrows(BadRequest.class, () -> {
			transferService.transactionReversal(transactionId);
		});

		assertEquals("Transaction is not approved or already reversed.", ex.getMessage());
	}

	@Test
	void testMoneyTransactionDestinationAccountNotFound() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123");
		request.setDestinationAccount("456");
		request.setValue(new BigDecimal("100.00"));

		BankAccount sourceAccount = new BankAccount();
		sourceAccount.setAccountNumber("123");
		sourceAccount.setBalance(new BigDecimal("500.00"));

		when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber("456")).thenReturn(Optional.empty());

		when(customMessageResolver.getMessage(Code.ACCOUNT_NOT_FOUND)).thenReturn("Account not found");

		ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Account not found", exception.getMessage());
	}

	@Test
	void testTransactionReversalStatusNotApproved() {
		Transaction transaction = new Transaction();
		transaction.setId("tx123");
		transaction.setStatus("PENDING");

		when(transactionRepository.findById("tx123")).thenReturn(Optional.of(transaction));
		when(customMessageResolver.getMessage(Code.TRANSACTION_NOT_APPROVED)).thenReturn("Transaction not approved");

		BadRequest exception = assertThrows(BadRequest.class, () -> {
			transferService.transactionReversal("tx123");
		});

		assertEquals("Transaction not approved", exception.getMessage());
	}

	@Test
	void testTransactionReversalInsufficientBalanceOnDestination() {
		Transaction transaction = new Transaction();
		transaction.setId("tx123");
		transaction.setStatus("APPROVED");
		transaction.setSourceAccount("123");
		transaction.setDestinationAccount("456");
		transaction.setValue(new BigDecimal("100.00"));

		BankAccount sourceAccount = new BankAccount();
		sourceAccount.setAccountNumber("123");
		sourceAccount.setBalance(new BigDecimal("300.00"));

		BankAccount destinationAccount = new BankAccount();
		destinationAccount.setAccountNumber("456");
		destinationAccount.setBalance(new BigDecimal("50.00")); // saldo insuficiente

		when(transactionRepository.findById("tx123")).thenReturn(Optional.of(transaction));
		when(accountRepository.findByAccountNumber("123")).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber("456")).thenReturn(Optional.of(destinationAccount));
		when(customMessageResolver.getMessage(Code.DESTINATION_ACCOUNT_INSUFFICIENT_BALANCE)).thenReturn("Insufficient balance on destination");

		BadRequest exception = assertThrows(BadRequest.class, () -> {
			transferService.transactionReversal("tx123");
		});

		assertEquals("Insufficient balance on destination", exception.getMessage());
	}

	@Test
	void testMoneyTransactionInvalidValue() {
		TransactionDTO request = new TransactionDTO();
		request.setSourceAccount("123");
		request.setDestinationAccount("456");

		request.setValue(BigDecimal.ZERO);

		when(customMessageResolver.getMessage(Code.INVALID_TRANSACTION_VALUE)).thenReturn("Invalid transaction value");

		BadRequest exception = assertThrows(BadRequest.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Invalid transaction value", exception.getMessage());

		request.setValue(new BigDecimal("-10"));

		exception = assertThrows(BadRequest.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Invalid transaction value", exception.getMessage());
	}

	@Test
	void testMoneyTransactionInvalidAccounts() {
		TransactionDTO request = new TransactionDTO();
		request.setValue(new BigDecimal("10.00"));

		request.setSourceAccount(null);
		request.setDestinationAccount("456");

		when(customMessageResolver.getMessage(Code.INVALID_ACCOUNT_NUMBER)).thenReturn("Invalid account number");

		BadRequest exception = assertThrows(BadRequest.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Invalid account number", exception.getMessage());

		request.setSourceAccount("123");
		request.setDestinationAccount(null);

		exception = assertThrows(BadRequest.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Invalid account number", exception.getMessage());

		request.setSourceAccount("123");
		request.setDestinationAccount("123");

		when(customMessageResolver.getMessage(Code.SOURCE_AND_DESTINATION_SAME)).thenReturn("Source and destination accounts cannot be the same");

		exception = assertThrows(BadRequest.class, () -> {
			transferService.moneyTransaction(request);
		});

		assertEquals("Source and destination accounts cannot be the same", exception.getMessage());
	}
}
