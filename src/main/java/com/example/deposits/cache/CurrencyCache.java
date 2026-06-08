package com.example.deposits.cache;

import com.example.deposits.repository.CurrencyRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@DependsOn("flywayInitializer")
public class CurrencyCache {

    private final CurrencyRepository currencyRepository;
    private final Cache<String, Currency> cache;

    public CurrencyCache(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
        this.cache = Caffeine.newBuilder().build();
    }

    @PostConstruct
    public void load() {
        List<Currency> currencies = currencyRepository.findAll();
        for (Currency c : currencies) {
            cache.put(c.code(), c);
        }
    }

    public Currency get(String code) {
        return cache.getIfPresent(code);
    }
}
