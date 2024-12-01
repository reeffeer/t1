package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<Account> saveAll(List<Account> accounts);
    Account save(Account account);
    Optional<Account> getAccountById(Long id);
    Optional<Account> delete(Long id);

    Account updateAccount(Long accountId, Account account);
}
