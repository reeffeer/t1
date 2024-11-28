package ru.t1.java.demo.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;
import ru.t1.java.demo.service.AccountService;
import ru.t1.java.demo.service.Service1;
import ru.t1.java.demo.service.TransactionService;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class Service1Test {

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private Service1 service1;

    @Captor
    private ArgumentCaptor<String> kafkaMessageCaptor;

    private WebClient webClientMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        webClientMock = mock(WebClient.class, RETURNS_DEEP_STUBS);
        service1 = new Service1(accountService, transactionService, kafkaTemplate);
    }

    @Test
    public void testListenerWithUnknownAccountStatus() {
        Transaction transaction = new Transaction();
        transaction.setAccountId(654123L);
        transaction.setClientId(1L);
        transaction.setAmount(100.0);
        transaction.setTransactionId(2350006L);

        Account account = new Account();
        account.setAccountId(654123L);
        account.setClientId(1L);
        account.setStatus(AccountStatus.UNKNOWN);
        account.setBalance(500.0);

        when(accountService.getAccountById(anyLong())).thenReturn(Optional.of(account));
        when(webClientMock.get().uri(anyString()).retrieve().bodyToMono(AccountStatus.class).block())
                .thenReturn(AccountStatus.OPEN);

        service1.listener(Collections.singletonList(transaction), acknowledgment, "test-topic", "test-key");

        verify(accountService).updateAccount(eq(4677L), any(Account.class));
        verify(kafkaTemplate).send(eq("t1_demo_transaction_accept"), kafkaMessageCaptor.capture());
        String sentMessage = kafkaMessageCaptor.getValue();
        assertTrue(sentMessage.contains("\"transactionId\": \"transaction123\""));
    }

    @Test
    public void testListenerWithOpenAccountStatus() {
        Transaction transaction = new Transaction();
        transaction.setAccountId(7004677L);
        transaction.setClientId(1L);
        transaction.setAmount(100.0);
        transaction.setTransactionId(461277L);

        Account account = new Account();
        account.setAccountId(461L);
        account.setClientId(1L);
        account.setStatus(AccountStatus.OPEN);
        account.setBalance(500.0);

        when(accountService.getAccountById(anyLong())).thenReturn(Optional.of(account));

        service1.listener(Collections.singletonList(transaction), acknowledgment, "test-topic", "test-key");

        verify(transactionService).createTransaction(any(Transaction.class));
        verify(accountService).updateAccount(eq(3L), any(Account.class));
        verify(kafkaTemplate).send(eq("t1_demo_transaction_accept"), kafkaMessageCaptor.capture());
        String sentMessage = kafkaMessageCaptor.getValue();
        assertTrue(sentMessage.contains("\"transactionId\": \"transaction123\""));
    }

    @Test
    public void testListenerWithRejectedTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAccountId(33L);
        transaction.setClientId(1L);
        transaction.setAmount(600.0);
        transaction.setTransactionId(123L);

        Account account = new Account();
        account.setAccountId(5L);
        account.setClientId(1L);
        account.setStatus(AccountStatus.OPEN);
        account.setBalance(500.0);

        when(accountService.getAccountById(anyLong())).thenReturn(Optional.of(account));

        service1.listener(Collections.singletonList(transaction), acknowledgment, "test-topic", "test-key");

        verify(transactionService).updateTransaction(any(Transaction.class));
        verify(accountService, never()).updateAccount(anyLong(), any(Account.class));
        verify(kafkaTemplate).send(eq("t1_demo_transaction_result"), kafkaMessageCaptor.capture());
        String sentMessage = kafkaMessageCaptor.getValue();
        assertTrue(sentMessage.contains("\"status\": \"REJECTED\""));
    }

    @Test
    public void testFetchAccountStatusFromService2() {
        when(webClientMock.get().uri(anyString()).retrieve().bodyToMono(AccountStatus.class).block())
                .thenReturn(AccountStatus.OPEN);

        AccountStatus status = service1.fetchAccountStatusFromService2(1L, 4L);

        assertEquals(AccountStatus.OPEN, status);
    }

    @Test
    public void testFetchAccountStatusFromService2WithError() {
        when(webClientMock.get().uri(anyString()).retrieve().bodyToMono(AccountStatus.class).block())
                .thenThrow(WebClientResponseException.NotFound.class);

        assertThrows(WebClientResponseException.NotFound.class, () -> service1.fetchAccountStatusFromService2(1L, 2L));
    }
}
