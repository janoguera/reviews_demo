# Set-Based Processing for Multiple Records

## Context
Processing records one-by-one in a loop requires a separate database round-trip per item.
Under load this serialises all I/O, saturates the connection pool, and scales linearly with
record count. Processing in sets (batches) sends data in bulk, greatly reducing round-trips
and enabling parallel execution.

## Decision
When processing multiple records of the same table, use set-based (batched) operations.
Iterating over records and issuing one DML statement per record is not allowed when the
operation can be expressed as a single batched statement.

Set size should be configurable or have a sensible default — sets that are too large increase
transaction size and memory pressure.

## Compliance Levels
- **Non-compliant**: Records are processed one-by-one with individual INSERT/UPDATE statements.
- **Partial**: Performance-critical APIs use set-based processing; batch jobs do not.
- **Compliant**: Set-based processing is used everywhere multiple records are handled.

## Examples

### Preferred
```java
// Collect all records first, then write in a single batched statement
List<Transaction> transactions = lines.stream()
        .map(line -> new Transaction(IdSupport.nextId(), accountId, line.type(), line.amount(), now, now))
        .toList();
transactionRepository.insertAll(transactions); // one batched INSERT
```

```sql
-- Repository method that accepts a list and uses JDBI batch insert
@SqlBatch("INSERT INTO transaction (id, account_id, type, amount, created_at) VALUES (:id, :accountId, :type, :amount, :createdAt)")
void insertAll(@BindBean List<Transaction> transactions);
```

### Not Allowed
```java
// One INSERT per record — N round-trips for N records
for (TransactionLine line : lines) {
    Transaction tx = new Transaction(IdSupport.nextId(), accountId, line.type(), line.amount(), now, now);
    transactionRepository.insert(tx); // separate SQL call per iteration
}
```
