package com.example.deposits.service;

import com.example.deposits.cache.Currency;
import com.example.deposits.cache.CurrencyCache;
import com.example.deposits.domain.Account;
import com.example.deposits.domain.AccountStatus;
import com.example.deposits.repository.AccountRepository;
import com.example.deposits.repository.AccountWithCurrencyPrecision;
import com.example.deposits.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceShould {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CurrencyCache currencyCache;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, transactionRepository, currencyCache);
    }

    @Test
    void readCurrencyPrecisionFromDbAndCallUpdateBalanceOnceOnDeposit() {
        AccountWithCurrencyPrecision projection = new AccountWithCurrencyPrecision(
                42L, "EXT-1", "EUR",
                new BigDecimal("0.00"),
                AccountStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(),
                2
        );
        when(accountRepository.findByIdWithCurrencyPrecision(42L)).thenReturn(Optional.of(projection));

        accountService.deposit(42L, new BigDecimal("100.00"));

        // Deposit path now reads currency precision directly from DB (not from cache)
        verify(accountRepository, times(1)).findByIdWithCurrencyPrecision(42L);
        verify(currencyCache, never()).get(anyString());

        // updateBalance called exactly once
        verify(accountRepository, times(1)).updateBalance(
                eq(42L),
                argThat(b -> b.compareTo(new BigDecimal("100.00")) == 0),
                any(java.time.LocalDateTime.class)
        );

        // insert transaction called once
        verify(transactionRepository, times(1)).insert(any());
    }

    @Test
    void returnUpdatedAccountAfterDeposit() {
        AccountWithCurrencyPrecision projection = new AccountWithCurrencyPrecision(
                99L, "EXT-2", "USD",
                new BigDecimal("50.00"),
                AccountStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now(),
                2
        );
        when(accountRepository.findByIdWithCurrencyPrecision(99L)).thenReturn(Optional.of(projection));

        Account result = accountService.deposit(99L, new BigDecimal("25.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("75.00");
    }

    @Test
    void returnUpdatedAccountAfterWithdrawal() {
        Account account = new Account(
                77L, "EXT-3", "GBP",
                new BigDecimal("200.00"),
                AccountStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now()
        );
        when(accountRepository.findById(77L)).thenReturn(Optional.of(account));
        when(currencyCache.get("GBP")).thenReturn(new Currency("GBP", "Pound Sterling", 2));

        Account result = accountService.withdraw(77L, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("150.00");
    }
}
