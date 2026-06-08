package com.example.deposits.api;

import com.example.deposits.api.dto.*;
import com.example.deposits.domain.Account;
import com.example.deposits.domain.TransactionType;
import com.example.deposits.service.AccountService;
import com.example.deposits.service.TransactionImportService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountController {

    private final AccountService accountService;
    private final TransactionImportService transactionImportService;

    public AccountController(AccountService accountService,
                             TransactionImportService transactionImportService) {
        this.accountService = accountService;
        this.transactionImportService = transactionImportService;
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@RequestBody CreateAccountRequest request) {
        Account account = accountService.createAccount(request.currencyCode(), request.externalId());
        return AccountResponse.from(account);
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(@PathVariable long id) {
        Account account = accountService.getAccount(id);
        return AccountResponse.from(account);
    }

    @PostMapping("/accounts/{id}/deposits")
    public AccountResponse deposit(@PathVariable long id, @RequestBody DepositRequest request) {
        Account account = accountService.deposit(id, request.amount());
        return AccountResponse.from(account);
    }

    @PostMapping("/accounts/{id}/withdrawals")
    public AccountResponse withdraw(@PathVariable long id, @RequestBody WithdrawRequest request) {
        Account account = accountService.withdraw(id, request.amount());
        return AccountResponse.from(account);
    }

    @PostMapping("/transactions:batch-import")
    @ResponseStatus(HttpStatus.OK)
    public void batchImport(@RequestBody BatchImportRequest request) {
        List<TransactionImportService.TransactionLine> lines = request.transactions().stream()
                .map(e -> new TransactionImportService.TransactionLine(
                        TransactionType.valueOf(e.type()),
                        e.amount()
                ))
                .toList();
        transactionImportService.importBatch(request.accountId(), lines);
    }
}
