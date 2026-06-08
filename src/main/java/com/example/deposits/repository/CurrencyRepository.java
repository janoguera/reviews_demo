package com.example.deposits.repository;

import com.example.deposits.cache.Currency;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

@RegisterRowMapper(CurrencyRowMapper.class)
public interface CurrencyRepository {

    @SqlQuery("SELECT code, name, minor_units FROM currency")
    List<Currency> findAll();
}
