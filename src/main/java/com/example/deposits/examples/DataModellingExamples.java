package com.example.deposits.examples;

import com.example.deposits.support.IdSupport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DEMO EXAMPLES — Data Modelling principles.
 *
 * These classes are NOT Spring beans and NOT wired into the application.
 * They exist purely as annotated teaching aids that compile and can be
 * read side-by-side during a code review exercise.
 *
 * Principles illustrated:
 *   1. Numeric primary keys — BIGINT assigned by the application before insert.
 *   2. Audit columns — every root table has creation_date and last_modified_date.
 */
public final class DataModellingExamples {

    private DataModellingExamples() {}

    // -------------------------------------------------------------------------
    // 1. NUMERIC PRIMARY KEYS
    //    Primary keys must be BIGINT values assigned by the application layer
    //    using a time-sortable ID generator (see IdSupport.nextId()).
    //    UUID/String PKs are 36 bytes vs 8 bytes for BIGINT, bloating every
    //    index and every foreign-key column that references this table.
    // -------------------------------------------------------------------------

    /** COMPLIANT DDL — numeric BIGINT PK, assigned by the application. */
    static final String COMPLIANT_DDL = """
            CREATE TABLE payment (
                id                 BIGINT          PRIMARY KEY,   -- assigned by app via IdSupport
                account_id         BIGINT          NOT NULL,
                amount             NUMERIC(19, 4)  NOT NULL,
                reference          VARCHAR(64),
                creation_date      TIMESTAMP(6)    NOT NULL,
                last_modified_date TIMESTAMP(6)    NOT NULL
            );
            """;

    /** NON-COMPLIANT DDL — UUID string PK (36 bytes, unordered, index-unfriendly). */
    static final String NON_COMPLIANT_DDL = """
            CREATE TABLE payment (
                id                 VARCHAR(36)     PRIMARY KEY,   -- UUID string: 36 bytes, bad for indexes
                account_id         BIGINT          NOT NULL,
                amount             NUMERIC(19, 4)  NOT NULL,
                reference          VARCHAR(64)
                -- Also missing: creation_date, last_modified_date  (see principle 2)
            );
            """;

    /** COMPLIANT domain entity — numeric id, set before persist. */
    static class PaymentCompliant {

        private final long id;             // numeric, time-sortable
        private final long accountId;
        private final BigDecimal amount;
        private final LocalDateTime creationDate;
        private LocalDateTime lastModifiedDate;

        /** Factory: id is assigned HERE, not by the database. */
        static PaymentCompliant create(long accountId, BigDecimal amount) {
            return new PaymentCompliant(
                    IdSupport.nextId(),        // numeric, monotonically increasing
                    accountId,
                    amount,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        }

        private PaymentCompliant(long id, long accountId, BigDecimal amount,
                                 LocalDateTime creationDate, LocalDateTime lastModifiedDate) {
            this.id = id;
            this.accountId = accountId;
            this.amount = amount;
            this.creationDate = creationDate;
            this.lastModifiedDate = lastModifiedDate;
        }

        long getId() { return id; }
        BigDecimal getAmount() { return amount; }
        LocalDateTime getCreationDate() { return creationDate; }
    }

    /** NON-COMPLIANT domain entity — UUID String id. */
    static class PaymentViolation {

        private final String id;           // UUID string — 36-byte PK
        private final long accountId;
        private final BigDecimal amount;
        // No creationDate / lastModifiedDate — see principle 2

        static PaymentViolation create(long accountId, BigDecimal amount) {
            return new PaymentViolation(
                    java.util.UUID.randomUUID().toString(),   // UUID — non-compliant
                    accountId,
                    amount
            );
        }

        private PaymentViolation(String id, long accountId, BigDecimal amount) {
            this.id = id;
            this.accountId = accountId;
            this.amount = amount;
        }

        String getId() { return id; }
    }

    // -------------------------------------------------------------------------
    // 2. AUDIT COLUMNS
    //    Every ROOT table (one that owns its own data, not a child record)
    //    must have:
    //      creation_date      TIMESTAMP(6) NOT NULL
    //      last_modified_date TIMESTAMP(6) NOT NULL
    //    These columns are mandatory for traceability, debugging, and compliance.
    //    Child tables (e.g. line items of a root entity) inherit audit context
    //    from their parent and do not need duplicate audit columns.
    // -------------------------------------------------------------------------

    /** COMPLIANT root table entity — both audit timestamps present. */
    static class LedgerEntryCompliant {

        private final long id;
        private final long accountId;
        private final BigDecimal amount;
        private final LocalDateTime creationDate;      // REQUIRED on root tables
        private LocalDateTime lastModifiedDate;         // REQUIRED on root tables

        LedgerEntryCompliant(long accountId, BigDecimal amount) {
            this.id = IdSupport.nextId();
            this.accountId = accountId;
            this.amount = amount;
            this.creationDate = LocalDateTime.now();
            this.lastModifiedDate = LocalDateTime.now();
        }

        long getId() { return id; }
        LocalDateTime getCreationDate() { return creationDate; }
        LocalDateTime getLastModifiedDate() { return lastModifiedDate; }
    }

    /** NON-COMPLIANT root table entity — no audit timestamps at all. */
    static class LedgerEntryViolation {

        private final String id;           // also violates PK rule
        private final long accountId;
        private final BigDecimal amount;
        // ← Missing: creationDate, lastModifiedDate
        //   Without these, you cannot answer:
        //   "when was this posting created?", "how stale is this row?"

        LedgerEntryViolation(long accountId, BigDecimal amount) {
            this.id = java.util.UUID.randomUUID().toString();
            this.accountId = accountId;
            this.amount = amount;
        }

        String getId() { return id; }
    }
}
