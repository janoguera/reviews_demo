package com.example.deposits.repository;

import com.example.deposits.cache.Currency;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyRowMapper implements RowMapper<Currency> {

    @Override
    public Currency map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Currency(
                rs.getString("code"),
                rs.getString("name"),
                rs.getInt("minor_units")
        );
    }
}
