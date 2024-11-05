package ru.t1.java.demo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.repository.AccountRepository;
import ru.t1.java.demo.service.AccountService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;

    @Override
    public List<Account> registerAccounts(List<Account> accounts) {
        List<Account> savedAccounts = new ArrayList<>();
        for (Account account : accounts) {
            Account saved = accountRepository.save(account);
            savedAccounts.add(saved);
        }
        return savedAccounts;
    }

    @Override
    public Account registerAccount(Account account) {
        Account saved = accountRepository.save(account);
        log.info("Account registered: {}", saved.getClientId());
        return saved;
    }

    @Override
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }
}
