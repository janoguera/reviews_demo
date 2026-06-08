package com.example.deposits.service;

import com.example.deposits.domain.Transaction;
import com.example.deposits.domain.TransactionType;
import com.example.deposits.repository.TransactionRepository;
import com.example.deposits.support.IdSupport;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionImportService {

    private final TransactionRepository transactionRepository;

    public TransactionImportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void importBatch(long accountId, List<TransactionLine> lines) {
        LocalDateTime now = LocalDateTime.now();
        List<Transaction> transactions = new ArrayList<>(lines.size());
        for (TransactionLine line : lines) {
            transactions.add(new Transaction(
                    IdSupport.nextId(),
                    accountId,
                    line.type(),
                    line.amount(),
                    now,
                    now
            ));
        }
        transactionRepository.insertAll(transactions);
    }

    public record TransactionLine(TransactionType type, BigDecimal amount) {}
}
