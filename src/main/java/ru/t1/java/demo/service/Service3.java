package ru.t1.java.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class Service3 {
    private final Random random = new Random();

    public boolean unblockClient(Long clientId) {
        boolean decision = random.nextBoolean();
        log.info("Client {} unblock decision: {}", clientId, decision);
        return decision;
    }

    public boolean unblockAccount(Long accountId) {
        boolean decision = random.nextBoolean();
        log.info("Account {} unblock decision: {}", accountId, decision);
        return decision;
    }
}
