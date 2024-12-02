package ru.t1.java.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.java.demo.service.Service3;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/unlock")
public class Service3Controller {
    private final Service3 service3;

    @PostMapping("/client/{clientId}")
    public ResponseEntity<String> unblockClient(@PathVariable Long clientId) {
        boolean isUnblocked = service3.unblockClient(clientId);
        if (isUnblocked) {
            return ResponseEntity.ok("Client " + clientId + " has been unblocked.");
        } else {
            return ResponseEntity.status(403).body("Client " + clientId + " cannot be unblocked.");
        }
    }

    @PostMapping("/account/{accountId}")
    public ResponseEntity<String> unblockAccount(@PathVariable Long accountId) {
        boolean isUnblocked = service3.unblockAccount(accountId);
        if (isUnblocked) {
            return ResponseEntity.ok("Account " + accountId + " has been unblocked.");
        } else {
            return ResponseEntity.status(403).body("Account " + accountId + " cannot be unblocked.");
        }
    }
}
