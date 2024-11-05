package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Transaction;

import java.util.Optional;

public interface TransactionService {
    Transaction createTransaction(Transaction transaction);
    Optional<Transaction> getTransactionById(Long id);
}
