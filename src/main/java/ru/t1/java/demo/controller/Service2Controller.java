package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.enums.AccountStatus;
import ru.t1.java.demo.service.AccountService;

import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class Service2Controller {private final AccountService accountService;

    @GetMapping("/{clientId}/{accountId}/status")
    public AccountStatus getAccountStatus(@PathVariable Long clientId, @PathVariable Long accountId) {
        Optional<Account> optionalAccount = accountService.getAccountByClientAndAccountId(clientId, accountId);
        if (optionalAccount.isPresent()) {
            return optionalAccount.get().getStatus();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
        }
    }
}