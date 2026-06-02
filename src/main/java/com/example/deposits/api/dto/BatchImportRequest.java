package com.example.deposits.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record BatchImportRequest(long accountId, List<TransactionEntry> transactions) {

    public record TransactionEntry(String type, BigDecimal amount, String reference) {}
}
