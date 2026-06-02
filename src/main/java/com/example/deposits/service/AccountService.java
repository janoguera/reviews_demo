package com.example.deposits.service;

import com.example.deposits.cache.Currency;
import com.example.deposits.cache.CurrencyCache;
import com.example.deposits.domain.Account;
import com.example.deposits.domain.AccountStatus;
import com.example.deposits.domain.Transaction;
import com.example.deposits.domain.TransactionType;
import com.example.deposits.repository.AccountRepository;
import com.example.deposits.repository.AccountWithCurrencyPrecision;
import com.example.deposits.repository.TransactionRepository;
import com.example.deposits.support.IdSupport;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrencyCache currencyCache;

    public AccountService(AccountRepository accountRepository,
                          TransactionRepository transactionRepository,
                          CurrencyCache currencyCache) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currencyCache = currencyCache;
    }

    public Account createAccount(String currencyCode, String externalId) {
        Currency currency = currencyCache.get(currencyCode);
        if (currency == null) {
            throw new IllegalArgumentException("Unknown currency: " + currencyCode);
        }
        LocalDateTime now = LocalDateTime.now();
        Account account = new Account(
                IdSupport.nextId(),
                externalId,
                currencyCode,
                BigDecimal.ZERO.setScale(currency.minorUnits(), RoundingMode.UNNECESSARY),
                AccountStatus.ACTIVE,
                now,
                now
        );
        accountRepository.insert(account);
        return account;
    }

    public Account getAccount(long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
    }

    public Account deposit(long id, BigDecimal amount) {
        // Use authoritative currency precision from the DB to ensure we always
        // apply the correct minor-unit scale even if the cache hasn't refreshed yet.
        AccountWithCurrencyPrecision projection = accountRepository.findByIdWithCurrencyPrecision(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));

        Account account = projection.getAccount();
        int minorUnits = projection.getCurrencyMinorUnits();
        BigDecimal scaled = amount.setScale(minorUnits, RoundingMode.HALF_UP);

        account.deposit(scaled);

        LocalDateTime now = LocalDateTime.now();
        accountRepository.updateBalance(id, account.getBalance(), now);
        account.setLastModifiedDate(now);

        Transaction tx = new Transaction(
                IdSupport.nextId(),
                id,
                TransactionType.DEPOSIT,
                scaled,
                now,
                now
        );
        transactionRepository.insert(tx);

        return account;
    }

    public Account withdraw(long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));

        Currency currency = currencyCache.get(account.getCurrencyCode());
        BigDecimal scaled = amount.setScale(currency.minorUnits(), RoundingMode.HALF_UP);

        account.withdraw(scaled);

        LocalDateTime now = LocalDateTime.now();
        accountRepository.updateBalance(id, account.getBalance(), now);
        account.setLastModifiedDate(now);

        Transaction tx = new Transaction(
                IdSupport.nextId(),
                id,
                TransactionType.WITHDRAWAL,
                scaled,
                now,
                now
        );
        transactionRepository.insert(tx);

        return account;
    }
}
