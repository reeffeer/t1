package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.aop.LogDataSourceError;
import ru.t1.java.demo.aop.Metric;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.service.AccountService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @Metric(500)
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account createdAccount = accountService.registerAccount(account);
        return ResponseEntity.ok(createdAccount);
    }

    @PutMapping("/{id}")
    @Metric(1000)
    public Account updateAccount(@PathVariable Long id, @RequestBody Account accountDetails) {
        Account account = accountService.getAccountById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        account.setClientId(accountDetails.getClientId());
        account.setAccountType(accountDetails.getAccountType());
        account.setBalance(accountDetails.getBalance());
        return accountService.registerAccount(account);
    }

    @GetMapping("/{id}")
    @LogDataSourceError
    public ResponseEntity<Account> getAccount(@PathVariable Long id) {
        return accountService.getAccountById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register-accounts")
    public ResponseEntity<List<Account>> registerAccounts(@RequestBody List<Account> accounts) {
        List<Account> registeredAccounts = accountService.registerAccounts(accounts);
        return ResponseEntity.ok(registeredAccounts);
    }

    @DeleteMapping("/{id}")
    @Metric(500)
    public void deleteAccount(@PathVariable Long id) {
        Account account = accountService.getAccountById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        accountService.delete(id);
    }
}