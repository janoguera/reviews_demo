package com.example.deposits.repository;

import com.example.deposits.domain.Account;
import com.example.deposits.domain.AccountStatus;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountRowMapper implements RowMapper<Account> {

    @Override
    public Account map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Account(
                rs.getLong("id"),
                rs.getString("external_id"),
                rs.getString("currency_code"),
                rs.getBigDecimal("balance"),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getTimestamp("last_modified_date").toLocalDateTime()
        );
    }
}
