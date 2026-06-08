package com.example.deposits.examples;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * DEMO EXAMPLES — Caching Strategy principles.
 *
 * These classes are NOT Spring beans and NOT wired into the application.
 * They exist purely as annotated teaching aids that compile and can be
 * read side-by-side during a code review exercise.
 *
 * Principles illustrated:
 *   1. In-process heap cache — use Caffeine for low-latency config lookups.
 *   2. Key-based lookup — O(1) access by key, not load-all-then-iterate.
 *   3. Cache population pattern — load at startup or lazily on first miss,
 *      never inside the request hot path.
 */
public final class CachingStrategyExamples {

    private CachingStrategyExamples() {}

    // -------------------------------------------------------------------------
    // 1. KEY-BASED LOOKUP vs. LOAD-ALL-THEN-ITERATE
    //    The cache must be keyed by the access dimension you need (e.g. currency
    //    code). A single O(1) lookup is all that is allowed on the hot path.
    //    Loading the entire dataset and scanning it on every request wastes
    //    memory and CPU and negates the benefit of caching.
    // -------------------------------------------------------------------------

    /** COMPLIANT: Caffeine cache keyed by currency code — O(1) lookup on the hot path. */
    static class KeyBasedCacheCompliant {

        private final Cache<String, FakeCurrency> currencyByCode;
        private final FakeCurrencyRepository repo;

        KeyBasedCacheCompliant(FakeCurrencyRepository repo) {
            this.repo = repo;
            this.currencyByCode = Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();
        }

        /** Called once at startup (or by a @PostConstruct bean) to warm the cache. */
        void warmUp() {
            repo.findAll().forEach(c -> currencyByCode.put(c.code(), c));
        }

        /**
         * Hot-path lookup: O(1), no SQL, no iteration.
         * This is the ONLY thing that should happen on the deposit request path.
         */
        FakeCurrency getByCode(String code) {
            return Optional.ofNullable(currencyByCode.getIfPresent(code))
                    .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + code));
        }
    }

    /**
     * NON-COMPLIANT: loads ALL currencies from the cache as a list,
     * then scans for the one we need — O(n) and wasteful.
     * Even if the cache avoids a DB hit, the iteration cost grows with the
     * number of currencies and runs on every deposit request.
     */
    static class LoadAllThenIterateViolation {

        private final Cache<String, List<FakeCurrency>> allCurrenciesCache;
        private final FakeCurrencyRepository repo;

        LoadAllThenIterateViolation(FakeCurrencyRepository repo) {
            this.repo = repo;
            this.allCurrenciesCache = Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();
        }

        void warmUp() {
            allCurrenciesCache.put("ALL", repo.findAll());  // Stored as one blob
        }

        /** Hot-path: loads the full list, iterates — O(n) on every call. */
        FakeCurrency getByCode(String code) {
            List<FakeCurrency> all = allCurrenciesCache.getIfPresent("ALL");
            if (all == null) return null;
            for (FakeCurrency c : all) {                    // scan — O(n) per request
                if (c.code().equals(code)) {
                    return c;
                }
            }
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // 2. CACHE POPULATION OUTSIDE THE HOT PATH
    //    The cache must be populated BEFORE the first request arrives — either
    //    at application startup or lazily on first miss (with a loading cache).
    //    Populating it inside a request handler delays the response and can
    //    cause thundering-herd problems under concurrent load.
    // -------------------------------------------------------------------------

    /** COMPLIANT: loading Caffeine cache — populates on first miss automatically, thread-safe. */
    static class LoadingCacheCompliant {

        private final com.github.benmanes.caffeine.cache.LoadingCache<String, FakeCurrency> cache;

        LoadingCacheCompliant(FakeCurrencyRepository repo) {
            this.cache = Caffeine.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build(repo::findByCode);               // loader called once on first miss
        }

        /** Always O(1) amortised — the loader runs only on a cache miss, never on hits. */
        FakeCurrency getByCode(String code) {
            return cache.get(code);
        }
    }

    /**
     * NON-COMPLIANT: queries the database inside the hot-path method.
     * If the cache is cold (just started, or after eviction), the request
     * path issues a synchronous DB call — exactly what caching is meant to prevent.
     */
    static class HotPathPopulationViolation {

        private final Map<String, FakeCurrency> localMap = new java.util.HashMap<>();
        private final FakeCurrencyRepository repo;

        HotPathPopulationViolation(FakeCurrencyRepository repo) {
            this.repo = repo;
        }

        /** NON-COMPLIANT: DB query happens inside the deposit request. */
        FakeCurrency getByCode(String code) {
            if (!localMap.containsKey(code)) {
                FakeCurrency currency = repo.findByCode(code);   // SQL on the hot path!
                localMap.put(code, currency);
            }
            return localMap.get(code);
        }
    }

    // ---- Minimal fakes so this file compiles standalone ----

    record FakeCurrency(String code, String name, int minorUnits) {}

    interface FakeCurrencyRepository {
        List<FakeCurrency> findAll();
        FakeCurrency findByCode(String code);
    }
}
