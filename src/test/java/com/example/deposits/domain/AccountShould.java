package com.example.deposits.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class AccountShould {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(
                1L, "EXT-1", "EUR",
                new BigDecimal("100.00"),
                AccountStatus.ACTIVE,
                LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void increaseBalanceWhenDepositIsPositive() {
        account.deposit(new BigDecimal("50.00"));
        assertThat(account.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void rejectNonPositiveDeposit() {
        assertThatThrownBy(() -> account.deposit(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> account.deposit(new BigDecimal("-10")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectNullDeposit() {
        assertThatThrownBy(() -> account.deposit(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void decreaseBalanceWhenWithdrawalIsValid() {
        account.withdraw(new BigDecimal("40.00"));
        assertThat(account.getBalance()).isEqualByComparingTo("60.00");
    }

    @Test
    void rejectWithdrawalWhenFundsInsufficient() {
        assertThatThrownBy(() -> account.withdraw(new BigDecimal("200.00")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void rejectNonPositiveWithdrawal() {
        assertThatThrownBy(() -> account.withdraw(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
