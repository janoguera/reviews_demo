package com.example.deposits.repository;

import com.example.deposits.domain.AccountStatus;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountWithCurrencyRowMapper implements RowMapper<AccountWithCurrencyPrecision> {

    @Override
    public AccountWithCurrencyPrecision map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new AccountWithCurrencyPrecision(
                rs.getLong("id"),
                rs.getString("external_id"),
                rs.getString("currency_code"),
                rs.getBigDecimal("balance"),
                AccountStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getTimestamp("last_modified_date").toLocalDateTime(),
                rs.getInt("currency_minor_units")
        );
    }
}
