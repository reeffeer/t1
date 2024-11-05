package ru.t1.java.demo.service;

import ru.t1.java.demo.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    List<Account> registerAccounts(List<Account> accounts);
    Account registerAccount(Account account);
    Optional<Account> getAccountById(Long id);
}
