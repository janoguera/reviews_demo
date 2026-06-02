package com.example.deposits.repository;

import com.example.deposits.domain.Account;
import com.example.deposits.domain.AccountStatus;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@RegisterRowMapper(AccountRowMapper.class)
public interface AccountRepository {

    @SqlUpdate("""
            INSERT INTO account (id, external_id, currency_code, balance, status, creation_date, last_modified_date)
            VALUES (:id, :externalId, :currencyCode, :balance, :status, :creationDate, :lastModifiedDate)
            """)
    void insert(@BindBean Account account);

    // Select all columns so that any new columns added in future migrations are
    // picked up automatically without having to update this query.
    @SqlQuery("SELECT * FROM account WHERE id = :id")
    Optional<Account> findById(@Bind("id") long id);

    @SqlQuery("""
            SELECT a.id, a.external_id, a.currency_code, a.balance, a.status,
                   a.creation_date, a.last_modified_date,
                   c.minor_units AS currency_minor_units
            FROM account a
            JOIN currency c ON a.currency_code = c.code
            WHERE a.id = :id
            """)
    @RegisterRowMapper(AccountWithCurrencyRowMapper.class)
    Optional<AccountWithCurrencyPrecision> findByIdWithCurrencyPrecision(@Bind("id") long id);

    @SqlUpdate("""
            UPDATE account
            SET balance = :balance, last_modified_date = :lastModifiedDate
            WHERE id = :id
            """)
    void updateBalance(@Bind("id") long id,
                       @Bind("balance") BigDecimal balance,
                       @Bind("lastModifiedDate") LocalDateTime lastModifiedDate);
}
