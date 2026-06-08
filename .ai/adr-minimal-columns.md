# Query Only the Columns and Rows That Are Needed

## Context
Fetching more data than required wastes network bandwidth, increases memory pressure, and
can expose columns unintentionally. `SELECT *` is especially problematic: it transfers every
column even when only a few are used, and silently breaks when the schema changes.

This applies to queries on high-volume transactional tables during performance-critical
operations.

## Decision
Queries must select the minimum set of columns and rows actually needed. `SELECT *` is
not allowed on transactional tables.

## Compliance Levels
- **Non-compliant**: `SELECT *` is used on transactional tables.
- **Partial**: Performance-critical APIs use explicit column lists; others do not.
- **Compliant**: Explicit column selection is applied everywhere.

## Examples

### Preferred
```sql
-- Explicit column list — only what the caller actually uses
SELECT id, external_id, currency_code, balance, status,
       creation_date, last_modified_date
FROM account
WHERE id = :id
```

### Not Allowed
```sql
-- Selects every column, including ones the caller never reads
SELECT *
FROM account
WHERE id = :id
```
