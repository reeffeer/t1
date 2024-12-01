package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class Service1 {
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(id = "${t1.kafka.consumer.group-id}",
            topics = {"${t1.kafka.topic.transactions}", "t1_demo_transaction_result"},
            containerFactory = "kafkaListenerContainerFactory")
    public void listener(@Payload List<Transaction> transactionList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Service1: Обработка сообщений из топика: {}", topic);
        try {
            for (Transaction transaction : transactionList) {
                Optional<Account> optionalAccount = accountService.getAccountById(transaction.getAccountId());
                if (optionalAccount.isPresent()) {
                    Account account = optionalAccount.get();

                    if (topic.equals("${t1.kafka.topic.transactions}")) {
                        if (account.getStatus() == AccountStatus.OPEN) {
                            transaction.setStatus(TransactionStatus.REQUESTED);
                            transactionService.createTransaction(transaction);

                            account.setBalance(account.getBalance() + transaction.getAmount());
                            accountService.updateAccount(account.getAccountId(), account);

                            String message = String.format("{" +
                                            "\"clientId\": %d, " +
                                            "\"accountId\": \"%s\", " +
                                            "\"transactionId\": \"%s\", " +
                                            "\"timestamp\": \"%s\", " +
                                            "\"amount\": %f, " +
                                            "\"balance\": %f" +
                                            "}",
                                    account.getClientId(),
                                    account.getAccountId(),
                                    transaction.getTransactionId(),
                                    transaction.getTransactionTime().toString(),
                                    transaction.getAmount(),
                                    account.getBalance());
                            kafkaTemplate.send("t1_demo_transaction_accept", message);
                        }
                    } else if (topic.equals("t1_demo_transaction_result")) {
                        switch (transaction.getStatus()) {
                            case ACCEPTED:
                                transaction.setStatus(TransactionStatus.ACCEPTED);
                                transactionService.updateTransaction(transaction);
                                break;
                            case BLOCKED:
                                transaction.setStatus(TransactionStatus.BLOCKED);
                                transactionService.updateTransaction(transaction);

                                account.setStatus(AccountStatus.BLOCKED);
                                BigDecimal transactionAmount = BigDecimal.valueOf(transaction.getAmount());
                                account.setFrozenAmount(account.getFrozenAmount().add(transactionAmount));
                                accountService.updateAccount(account.getAccountId(), account);
                                break;
                            case REJECTED:
                                transaction.setStatus(TransactionStatus.REJECTED);
                                transactionService.updateTransaction(transaction);

                                account.setBalance(account.getBalance() - transaction.getAmount());
                                accountService.updateAccount(account.getAccountId(), account);
                                break;
                            default:
                                log.warn("Неизвестный статус транзакции: {}", transaction.getStatus());
                        }
                    }
                }
            }
        } finally {
            ack.acknowledge();
        }
        log.debug("Service1: Обработка сообщений завершена");
    }
}
