package com.example.deposits.examples;

import com.example.deposits.cache.Currency;
import com.example.deposits.cache.CurrencyCache;

import java.math.BigDecimal;
import java.util.List;

/**
 * DEMO EXAMPLES — Data Access principles.
 *
 * These classes are NOT Spring beans and NOT wired into the application.
 * They exist purely as annotated teaching aids that compile and can be
 * read side-by-side during a code review exercise.
 *
 * Principles illustrated:
 *   1. Single-pass table access — one SELECT per table per business operation.
 *   2. Minimal column projection — explicit column lists, never SELECT *.
 *   3. Set-based batch operations — one INSERT batch, never a per-row loop.
 *   4. Config from cache, not hot-path SQL — no JOIN to a config table on the hot path.
 */
public final class DataAccessExamples {

    private DataAccessExamples() {}

    // -------------------------------------------------------------------------
    // 1. SINGLE-PASS TABLE ACCESS
    //    The account table must be read at most once and written at most once
    //    during a single deposit operation. Reading it twice to "refresh" the
    //    balance is an anti-pattern: it doubles I/O and introduces a TOCTOU gap.
    // -------------------------------------------------------------------------

    /** COMPLIANT: one read, one write. */
    static class SinglePassCompliant {

        private final FakeAccountRepository accountRepo;
        private final FakeTransactionRepository txRepo;

        SinglePassCompliant(FakeAccountRepository accountRepo, FakeTransactionRepository txRepo) {
            this.accountRepo = accountRepo;
            this.txRepo = txRepo;
        }

        void deposit(long accountId, BigDecimal amount) {
            FakeAccount account = accountRepo.findById(accountId);          // 1 SELECT
            account.applyDeposit(amount);
            accountRepo.updateBalance(account);                              // 1 UPDATE
            txRepo.insert(new FakeTransaction(accountId, amount));           // 1 INSERT
            // Total: 1 SELECT + 1 UPDATE on account table.
        }
    }

    /** NON-COMPLIANT: reads account twice — once before, once after. */
    static class SinglePassViolation {

        private final FakeAccountRepository accountRepo;
        private final FakeTransactionRepository txRepo;

        SinglePassViolation(FakeAccountRepository accountRepo, FakeTransactionRepository txRepo) {
            this.accountRepo = accountRepo;
            this.txRepo = txRepo;
        }

        void deposit(long accountId, BigDecimal amount) {
            FakeAccount account = accountRepo.findById(accountId);          // SELECT #1
            account.applyDeposit(amount);
            accountRepo.updateBalance(account);                              // UPDATE
            txRepo.insert(new FakeTransaction(accountId, amount));
            FakeAccount refreshed = accountRepo.findById(accountId);        // SELECT #2 — redundant!
            System.out.println("New balance: " + refreshed.getBalance());
        }
    }

    // -------------------------------------------------------------------------
    // 2. MINIMAL COLUMN PROJECTION
    //    Selecting only the columns a use-case needs reduces the amount of data
    //    transferred from the DB, keeps the buffer pool efficient, and makes
    //    schema evolution safer (new columns don't appear in existing queries).
    // -------------------------------------------------------------------------

    /** COMPLIANT: explicit column list in query string. */
    static class ProjectionCompliant {

        String findByIdQuery(long accountId) {
            // Only the columns the GET /accounts/{id} response actually needs.
            return "SELECT id, external_id, currency_code, balance, status, "
                 + "creation_date, last_modified_date "
                 + "FROM account WHERE id = " + accountId;
        }
    }

    /** NON-COMPLIANT: SELECT * pulls every column, including internal ones. */
    static class ProjectionViolation {

        String findByIdQuery(long accountId) {
            // Fetches ALL columns — including ones the response never exposes.
            return "SELECT * FROM account WHERE id = " + accountId;
        }
    }

    // -------------------------------------------------------------------------
    // 3. SET-BASED BATCH OPERATIONS
    //    When inserting many rows, a single batched statement costs one network
    //    round-trip regardless of the row count. A per-row loop costs N trips.
    //    At 10 000 rows the difference is ~10 000x latency.
    // -------------------------------------------------------------------------

    /** COMPLIANT: insert the whole list in one batch. */
    static class BatchCompliant {

        private final FakeTransactionRepository txRepo;

        BatchCompliant(FakeTransactionRepository txRepo) {
            this.txRepo = txRepo;
        }

        void importTransactions(List<FakeTransaction> transactions) {
            txRepo.insertAll(transactions);  // 1 batch statement, N rows
        }
    }

    /** NON-COMPLIANT: one INSERT per row — N round-trips instead of 1. */
    static class BatchViolation {

        private final FakeTransactionRepository txRepo;

        BatchViolation(FakeTransactionRepository txRepo) {
            this.txRepo = txRepo;
        }

        void importTransactions(List<FakeTransaction> transactions) {
            for (FakeTransaction tx : transactions) {
                txRepo.insert(tx);  // N individual INSERT statements
            }
        }
    }

    // -------------------------------------------------------------------------
    // 4. CONFIG FROM CACHE, NOT HOT-PATH SQL
    //    Currency precision is static config data. It is loaded into an
    //    in-process Caffeine cache at startup (see CurrencyCache).
    //    The deposit hot path must read from that cache — never issue a new
    //    SQL query or JOIN to the currency table during a deposit.
    // -------------------------------------------------------------------------

    /** COMPLIANT: currency precision from the in-memory cache — zero extra SQL. */
    static class CacheAccessCompliant {

        private final CurrencyCache currencyCache;

        CacheAccessCompliant(CurrencyCache currencyCache) {
            this.currencyCache = currencyCache;
        }

        int getCurrencyPrecision(String currencyCode) {
            Currency currency = currencyCache.get(currencyCode);  // O(1) heap lookup
            return currency.minorUnits();
        }
    }

    /**
     * NON-COMPLIANT: currency precision fetched via a SQL JOIN on every deposit.
     * This query now hits the database on the performance-critical hot path.
     */
    static class CacheAccessViolation {

        String depositQuery(long accountId) {
            // The JOIN to the currency table fetches config on every deposit.
            return "SELECT a.id, a.balance, a.currency_code, c.minor_units "
                 + "FROM account a "
                 + "JOIN currency c ON a.currency_code = c.code "
                 + "WHERE a.id = " + accountId;
        }
    }

    // ---- Minimal fakes so this file compiles standalone ----

    interface FakeAccountRepository {
        FakeAccount findById(long id);
        void updateBalance(FakeAccount account);
    }

    interface FakeTransactionRepository {
        void insert(FakeTransaction tx);
        void insertAll(List<FakeTransaction> txs);
    }

    static class FakeAccount {
        private BigDecimal balance = BigDecimal.ZERO;
        long id;
        String currencyCode;

        void applyDeposit(BigDecimal amount) { this.balance = this.balance.add(amount); }
        BigDecimal getBalance() { return balance; }
    }

    static class FakeTransaction {
        long accountId;
        BigDecimal amount;

        FakeTransaction(long accountId, BigDecimal amount) {
            this.accountId = accountId;
            this.amount = amount;
        }
    }
}
