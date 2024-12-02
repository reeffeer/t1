package ru.t1.java.demo.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaAccountConsumer {

    @Autowired
    private final AccountService accountService;

    @KafkaListener(id = "${t1.kafka.consumer.group-id}",
            topics = "${t1.kafka.topic.client_registration}",
            containerFactory = "kafkaListenerContainerFactory")
    public void listener(@Payload List<Account> accountList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Account consumer: Обработка новых сообщений");
        try {
            List<Account> accounts = accountList.stream()
                    .map(account -> {
                        account.setClientId(Long.parseLong(key));
                        return account;
                    })
                    .toList();
            accountService.saveAll(accounts);
        } finally {
            ack.acknowledge();
        }
        log.debug("Account consumer: записи обработаны");
    }
}
