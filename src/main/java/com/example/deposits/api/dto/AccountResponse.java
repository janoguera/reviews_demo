package com.example.deposits.api.dto;

import com.example.deposits.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        long id,
        String externalId,
        String currencyCode,
        BigDecimal balance,
        String status,
        LocalDateTime creationDate,
        LocalDateTime lastModifiedDate
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getExternalId(),
                account.getCurrencyCode(),
                account.getBalance(),
                account.getStatus().name(),
                account.getCreationDate(),
                account.getLastModifiedDate()
        );
    }
}
