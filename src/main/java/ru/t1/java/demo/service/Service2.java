package ru.t1.java.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.TransactionStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@Service
public class Service2 {
    private final AccountService accountService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final Map<String, List<LocalDateTime>> transactionLog = new ConcurrentHashMap<>();

    @Value("${transaction.threshold")
    private final int transactionThreshold;
    @Value("${transaction.time.threshold}")
    private final Duration timeThreshold;

    @KafkaListener(id = "${t1.kafka.consumer.group-id}",
            topics = "t1_demo_transaction_accept",
            containerFactory = "kafkaListenerContainerFactory")
    public void listener(@Payload List<Transaction> transactionList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Service2: Обработка новых транзакций");
        try {
            for (Transaction transaction : transactionList) {
                Optional<Account> optionalAccount = accountService.getAccountById(transaction.getAccountId());
                if (optionalAccount.isPresent()) {
                    Account account = optionalAccount.get();
                    String accountKey = account.getClientId() + "-" + account.getAccountId();
                    LocalDateTime now = LocalDateTime.now();

                    transactionLog.computeIfAbsent(accountKey, k -> new java.util.ArrayList<>()).add(now);
                    List<LocalDateTime> times = transactionLog.get(accountKey);
                    times.removeIf(time -> Duration.between(time, now).compareTo(timeThreshold) > 0);

                    // Check if transaction count exceeds threshold
                    if (times.size() > transactionThreshold) {
                        // Block the transactions
                        transaction.setStatus(TransactionStatus.BLOCKED);
                        kafkaTemplate.send("t1_demo_transaction_result", String.format("{" +
                                        "\"transactionId\": \"%s\", " +
                                        "\"accountId\": \"%s\", " +
                                        "\"status\": \"%s\"" +
                                        "}",
                                transaction.getTransactionId(),
                                account.getAccountId(),
                                transaction.getStatus().toString()));
                        continue;
                    }
                    if (transaction.getAmount() > account.getBalance()) {
                        transaction.setStatus(TransactionStatus.REJECTED);
                        kafkaTemplate.send("t1_demo_transaction_result", String.format("{" +
                                        "\"transactionId\": \"%s\", " +
                                        "\"accountId\": \"%s\", " +
                                        "\"status\": \"%s\"" +
                                        "}",
                                transaction.getTransactionId(),
                                account.getAccountId(),
                                transaction.getStatus().toString()));
                        continue;
                    }
                    transaction.setStatus(TransactionStatus.ACCEPTED);
                    kafkaTemplate.send("t1_demo_transaction_result", String.format("{" +
                                    "\"transactionId\": \"%s\", " +
                                    "\"accountId\": \"%s\", " +
                                    "\"status\": \"%s\"" +
                                    "}",
                            transaction.getTransactionId(),
                            account.getAccountId(),
                            transaction.getStatus().toString()));
                }
            }
        } finally {
            ack.acknowledge();
        }
        log.debug("Service2: Транзакции обработаны");
    }
}
