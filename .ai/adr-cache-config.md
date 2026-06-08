# Configuration Data Must Be Served From Cache

## Context
Configuration data (currencies, product settings, reference tables) has low volume and changes
rarely. Querying it during every business operation — especially by joining it to high-volume
transactional tables — adds unnecessary database round-trips and can cause performance issues
at scale.

*Configuration data*: any low-volume reference table (e.g. currencies, rate sheets, product
config). *Transactional data*: high-volume tables such as accounts and transactions.

## Decision
Configuration data must always be served from an in-process cache during business operations.
SQL joins between transactional tables and configuration tables are not allowed in hot paths.
The cache is populated from the database at startup or on first access; subsequent reads come
from memory.

## Compliance Levels
- **Non-compliant**: Configuration is fetched via SQL during every operation.
- **Partial**: Performance-critical APIs use the cache; batch jobs still query the database.
- **Compliant**: Configuration is always served from cache everywhere.

## Examples

### Preferred
```java
// Look up configuration from the in-memory cache — no SQL in the hot path
Currency currency = currencyCache.get(account.getCurrencyCode());
BigDecimal scaled = amount.setScale(currency.minorUnits(), RoundingMode.HALF_UP);
```

```sql
-- Only query the transactional table; resolve currency details from cache in Java
SELECT id, currency_code, balance
FROM account
WHERE id = :id
```

### Not Allowed
```sql
-- Joining a transactional table with a configuration table in the hot path
SELECT a.id, a.balance, c.minor_units
FROM account a
JOIN currency c ON a.currency_code = c.code
WHERE a.id = :id
```
