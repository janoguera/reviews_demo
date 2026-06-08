# Audit Columns on Every Root Table

## Context
Knowing when a record was created and last modified is essential for debugging, auditing,
data lineage, and compliance. Without these timestamps, reconstructing the sequence of events
from production data becomes difficult or impossible.

A *root table* is any table that represents an independent aggregate (e.g. accounts,
transactions). *Child tables* (records that cannot exist without a parent, such as line items)
can omit audit columns because their timeline can be inferred from the parent.

## Decision
Every root table must have:
- `creation_date TIMESTAMP NOT NULL` — set once when the record is first inserted.
- `last_modified_date TIMESTAMP NOT NULL` — updated on every write.

Both columns must be non-nullable and populated by the application layer.

## Compliance Levels
- **Non-compliant**: Root table has neither audit column.
- **Partial**: `creation_date` is present but `last_modified_date` is missing, or columns are nullable.
- **Compliant**: Both columns are present, non-nullable, and always populated.

## Examples

### Preferred
```sql
CREATE TABLE account (
    id                  BIGINT          PRIMARY KEY,
    -- business columns ...
    creation_date       TIMESTAMP       NOT NULL,
    last_modified_date  TIMESTAMP       NOT NULL
);
```

```java
public Account(...) {
    this.creationDate     = LocalDateTime.now();
    this.lastModifiedDate = LocalDateTime.now();
}

public void touch() {
    this.lastModifiedDate = LocalDateTime.now();
}
```

### Not Allowed
```sql
-- Root table with no audit columns
CREATE TABLE interest_posting (
    id          VARCHAR(36)   PRIMARY KEY,
    account_id  BIGINT        NOT NULL,
    amount      NUMERIC(19,4) NOT NULL,
    posted_date DATE          NOT NULL
    -- no creation_date, no last_modified_date
);
```
