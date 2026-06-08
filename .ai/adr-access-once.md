# Single Access Per Table Per Operation

## Context
In performance-critical operations (high-throughput APIs and batch processes), hitting the
same table more than once per operation type compounds latency and database load. Even two
sequential SELECTs on a high-volume table per request doubles the read pressure.

A *transactional table* is any table that stores accounts, transactions, or similar
high-volume business records.

## Decision
A transactional table must not be accessed more than once per operation type
(SELECT, INSERT, UPDATE, DELETE) within a single business operation.

## Compliance Levels
- **Non-compliant**: Multiple statements of the same type hit the same table in one operation.
- **Partial**: Performance-critical APIs comply; batch/background jobs do not.
- **Compliant**: The rule is applied everywhere — APIs and batch jobs alike.

## Examples

### Preferred
```java
// Single SELECT that retrieves everything needed in one query
AccountWithDetails account = accountRepository.findByIdWithDetails(id)
    .orElseThrow(() -> new NoSuchElementException("Account not found: " + id));
```

### Not Allowed
```java
// Two SELECTs on the same table in one operation
Account account = accountRepository.findById(id).orElseThrow(...);
// ... later in the same method:
Account refreshed = accountRepository.findById(id).orElseThrow(...); // second hit — not allowed
```
