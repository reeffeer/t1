package ru.t1.java.demo.util;

import ru.t1.java.demo.model.Account;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.model.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DataGenerator {

    public static void main(String[] args) {
        List<Account> accounts = generateAccounts(10);
        List<Transaction> transactions = generateTransactions(20, accounts);

        System.out.println("Generated Accounts:");
        accounts.forEach(account -> System.out.println(
                "Client ID: " + account.getClientId() + ", Account Type: " + account.getAccountType() + ", Balance: " + account.getBalance()));

        System.out.println("\nGenerated Transactions:");
        transactions.forEach(transaction -> System.out.println(
                "Account ID: " + transaction.getAccountId() + ", Amount: " + transaction.getAmount() + ", Transaction Time: " + transaction.getTransactionTime()));
    }

    private static final Random RANDOM = new Random();

    public static List<Account> generateAccounts(int count) {
        List<Account> accounts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Long clientId = (long) RANDOM.nextInt(100000);
            AccountType accountType = RANDOM.nextBoolean() ? AccountType.DEBIT : AccountType.CREDIT;
            double balance = RANDOM.nextDouble() * 10000;
            accounts.add(Account.builder().clientId(clientId).accountType(accountType).balance(balance).build());
        }
        return accounts;
    }

    public static List<Transaction> generateTransactions(int count, List<Account> accounts) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Long accountId = accounts.get(RANDOM.nextInt(accounts.size())).getClientId();
            BigDecimal amount = BigDecimal.valueOf((RANDOM.nextDouble() * 2000) - 1000);
            transactions.add(Transaction.builder().accountId(accountId).amount(amount).transactionTime(LocalDateTime.now()).build());
        }
        return transactions;
    }
}
