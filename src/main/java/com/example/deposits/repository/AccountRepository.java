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

    @SqlQuery("""
            SELECT id, external_id, currency_code, balance, status, creation_date, last_modified_date
            FROM account
            WHERE id = :id
            """)
    Optional<Account> findById(@Bind("id") long id);

    @SqlUpdate("""
            UPDATE account
            SET balance = :balance, last_modified_date = :lastModifiedDate
            WHERE id = :id
            """)
    void updateBalance(@Bind("id") long id,
                       @Bind("balance") BigDecimal balance,
                       @Bind("lastModifiedDate") LocalDateTime lastModifiedDate);
}
