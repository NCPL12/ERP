# Monthly stock fixes (#144–147)

Branch: `fix/viren-monthly-stock-144-147`, based on the existing monthly-stock
PR branch (`origin/features/monthly-stock`, commit `941db77`). Apply these
changes with PR #57's monthly reporting code, not as a replacement for unrelated
ERP or Cashflow changes.

The PR has also been updated with `development` at `e3a5783`. Merge conflicts
were resolved while retaining development's DC report implementation and README.
After that merge, 56 related monthly-stock, DC and Tally tests passed.

- **#144:** Missing PO prices are ignored in the average numerator and count,
  matching SQL AVG semantics. An all-null group has no average, and a real zero
  price remains a valid observation. Missing prices cannot crash GRN valuation.
- **#145:** Report and frozen snapshot quantities retain six decimal places.
  A closing quantity such as `0.004321` carries into the next month's opening
  without rounding to zero. Monetary and Excel display rules remain separate.
- **#146:** Each generation loads PO price history once and shares the same
  average-rate map between GRN, DC and main-report valuation. There is no shared
  cache that could retain prices across requests.
- **#147:** Every movement query uses the same business-zone interval as the
  Envers audit snapshots. Calendar parameters retain the configured zone through
  Hibernate's JDBC binding. The range is start-of-first-day inclusive to
  start-of-day-after-last-day exclusive, including the complete final second.

## Deployment and data

Build/deploy the ERP WAR and restart ERP. The existing monthly feature schema
in `docs/monthly-stock-schema.sql` already defines `closing_qty DECIMAL(19,6)`;
these fixes require no additional migration when that schema is installed.
Confirm the deployed column retains scale 6 before freezing a new report.
Use the same `monthly.stock.auto-freeze.zone` (default `Asia/Kolkata`) for the
report and scheduler.

No production data or frozen snapshots were changed during this work.
Previously rounded frozen quantities cannot be reconstructed automatically;
any historical replacement still requires an approved source and Finance review.
These code fixes do not erase genuine negative stock or quantity discrepancies.

## Validation

```text
mvn -B -o -Dtest=MonthlyStockMovementRegressionTest,MonthlyStockSnapshotSchedulerTest,StockReportMoneyTest test
```

14 tests pass, including mixed/all-null/zero PO prices, one shared price-history
query, six-decimal freeze and next-month carry-forward, month-end scheduling,
UTC/Los Angeles/Kolkata server zones, and the actual Hibernate JDBC Calendar
binder. Tests use mocks and do not connect to a production database.
