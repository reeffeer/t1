package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.repository.TransactionRepository;
import ru.t1.java.demo.service.TransactionService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public Transaction createTransaction(Transaction transaction) {
        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created: {}", saved.getAccountId());
        return saved;
    }

    @Override
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Optional<List<Transaction>> saveAll(List<Transaction> transactions) {
        return Optional.of(transactions);
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        if (transactionRepository.existsById(transaction.getTransactionId())) {
            Transaction updated = transactionRepository.save(transaction);
            log.info("Transaction updated: {}", updated.getTransactionId());
            return updated;
        } else {
            log.warn("Transaction not found for update: {}", transaction.getTransactionId());
            throw new IllegalArgumentException("Transaction not found for update");
        }
    }
}
