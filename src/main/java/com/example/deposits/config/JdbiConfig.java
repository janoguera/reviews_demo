package com.example.deposits.config;

import com.example.deposits.repository.AccountRepository;
import com.example.deposits.repository.CurrencyRepository;
import com.example.deposits.repository.TransactionRepository;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JdbiConfig {

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    @Bean
    public AccountRepository accountRepository(Jdbi jdbi) {
        return jdbi.onDemand(AccountRepository.class);
    }

    @Bean
    public TransactionRepository transactionRepository(Jdbi jdbi) {
        return jdbi.onDemand(TransactionRepository.class);
    }

    @Bean
    public CurrencyRepository currencyRepository(Jdbi jdbi) {
        return jdbi.onDemand(CurrencyRepository.class);
    }
}
