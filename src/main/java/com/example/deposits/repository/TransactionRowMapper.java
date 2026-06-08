package com.example.deposits.repository;

import com.example.deposits.domain.Transaction;
import com.example.deposits.domain.TransactionType;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionRowMapper implements RowMapper<Transaction> {

    @Override
    public Transaction map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Transaction(
                rs.getLong("id"),
                rs.getLong("account_id"),
                TransactionType.valueOf(rs.getString("type")),
                rs.getBigDecimal("amount"),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getTimestamp("last_modified_date").toLocalDateTime()
        );
    }
}
