# Deposit Account Microservice — Code Review Demo

This is a generic Spring Boot deposit-account microservice built as a teaching tool for code reviewers learning to validate pull requests against Architecture Decision Records (ADRs). The principles documented here are widely-known, industry-standard engineering practices (e.g. minimal column projection, set-based processing, cached reference data) restated generically for this exercise. They are not tied to any specific organisation's systems, schema, or internal standards.

## Tech Stack

- **Language & Framework**: Java 17, Spring Boot 3.2
- **Build**: Gradle
- **Persistence**: JDBI 3 (SQL-first ORM)
- **Database**: PostgreSQL (in Docker)
- **Testing**: JUnit 5, Testcontainers (PostgreSQL fixture)
- **Migrations**: Flyway
- **Caching**: Caffeine (in-process heap cache)

## Domain Model

The service manages deposit accounts and their transactions:

- **`account`** — Core entity with numeric primary key, account metadata, and audit columns (`creation_date`, `last_modified_date`).
- **`account_transaction`** — Ledger entries (deposits, withdrawals) with numeric primary key and audit columns.
- **`currency`** — Reference data (precision, conversion rates) loaded once into a Caffeine cache at startup.

All tables use `BIGINT` numeric primary keys assigned by the application and include standard audit timestamps.

## API Endpoints

| Method | Endpoint | Description | Example |
|--------|----------|-------------|---------|
| `POST` | `/accounts` | Create a new account | `curl -X POST http://localhost:8080/accounts -H "Content-Type: application/json" -d '{"accountNumber":"ACC001","currencyCode":"USD"}'` |
| `GET` | `/accounts/{id}` | Retrieve account details | `curl http://localhost:8080/accounts/1` |
| `POST` | `/accounts/{id}/deposits` | Record a deposit | `curl -X POST http://localhost:8080/accounts/1/deposits -H "Content-Type: application/json" -d '{"amount":100.50}'` |
| `POST` | `/accounts/{id}/withdrawals` | Record a withdrawal | `curl -X POST http://localhost:8080/accounts/1/withdrawals -H "Content-Type: application/json" -d '{"amount":50.00}'` |
| `POST` | `/transactions:batch-import` | Bulk import transactions | `curl -X POST http://localhost:8080/transactions:batch-import -H "Content-Type: application/json" -d '[{"accountId":1,"amount":25.00},...]'` |

## Running the Service

### Docker Compose (PostgreSQL + Service)
```bash
docker compose up --build
```
Starts PostgreSQL on the standard port and the Spring Boot service on **http://localhost:8080**.

### Local Gradle Build
```bash
./gradlew build              # Runs unit tests
./gradlew integrationTest    # Runs Testcontainers-based integration tests
```

## Architecture Principles Followed

This service is built to comply with the following engineering principles. Pull request reviewers should validate that new code maintains these constraints:

| Category | Principle | Description |
|----------|-----------|-------------|
| **Data Access** | Single-access rule | A transactional table is accessed at most once per operation type (SELECT/INSERT/UPDATE) per business operation. |
| **Data Access** | SQL-first persistence | Persistence uses a lightweight SQL mapper; heavy ORMs (JPA/Hibernate) are not allowed. |
| **Data Access** | Config cache, not hot-path queries | No configuration data is queried during a performance-critical operation; config (e.g. currency) must come from an in-memory cache, never via a SQL join in the hot path. |
| **Data Access** | Minimal column selection | Queries select the minimum set of columns and rows needed; no `SELECT *`. |
| **Data Access** | Set-based batch operations | When processing multiple records, use set-based (batched) operations, not one-row-at-a-time loops. |
| **Data Modelling** | Numeric primary keys | Every table has an efficient numeric primary key (BIGINT assigned by the application), not a UUID/string PK. |
| **Data Modelling** | Audit columns | Every root table has `creation_date` and `last_modified_date` audit columns (TIMESTAMP NOT NULL). |
| **Caching Strategy** | In-process heap cache | Caching uses an in-process heap cache; config is always served from cache in hot paths — never fetched from the database mid-request. |
