package com.example.deposits.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {

    private long id;
    private String externalId;
    private String currencyCode;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime creationDate;
    private LocalDateTime lastModifiedDate;

    public Account() {}

    public Account(long id, String externalId, String currencyCode, BigDecimal balance,
                   AccountStatus status, LocalDateTime creationDate, LocalDateTime lastModifiedDate) {
        this.id = id;
        this.externalId = externalId;
        this.currencyCode = currencyCode;
        this.balance = balance;
        this.status = status;
        this.creationDate = creationDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (amount.compareTo(this.balance) > 0) {
            throw new IllegalArgumentException("Insufficient funds: balance is " + this.balance);
        }
        this.balance = this.balance.subtract(amount);
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
}
