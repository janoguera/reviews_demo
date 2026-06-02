package com.example.deposits.repository;

import com.example.deposits.domain.Account;
import com.example.deposits.domain.AccountStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Projection that bundles an account together with the authoritative currency
 * precision (minor_units) read from the currency config table in the same query.
 */
public class AccountWithCurrencyPrecision {

    private final Account account;
    private final int currencyMinorUnits;

    public AccountWithCurrencyPrecision(long id, String externalId, String currencyCode,
                                        BigDecimal balance, AccountStatus status,
                                        LocalDateTime creationDate, LocalDateTime lastModifiedDate,
                                        int currencyMinorUnits) {
        this.account = new Account(id, externalId, currencyCode, balance, status, creationDate, lastModifiedDate);
        this.currencyMinorUnits = currencyMinorUnits;
    }

    public Account getAccount() {
        return account;
    }

    public int getCurrencyMinorUnits() {
        return currencyMinorUnits;
    }
}
