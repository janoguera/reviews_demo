package com.example.deposits.repository;

import com.example.deposits.domain.Transaction;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterRowMapper(TransactionRowMapper.class)
public interface TransactionRepository {

    @SqlUpdate("""
            INSERT INTO account_transaction (id, account_id, type, amount, creation_date, last_modified_date)
            VALUES (:id, :accountId, :type, :amount, :creationDate, :lastModifiedDate)
            """)
    void insert(@BindBean Transaction transaction);

    @SqlBatch("""
            INSERT INTO account_transaction (id, account_id, type, amount, creation_date, last_modified_date)
            VALUES (:id, :accountId, :type, :amount, :creationDate, :lastModifiedDate)
            """)
    void insertAll(@BindBean List<Transaction> transactions);
}
