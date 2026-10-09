# Neptune ERP feature deployment

These feature branches are based on `development` (652adad). Review and merge into `development`; keep `main` unchanged.

Each branch includes its own deployment guide. After merging all three, all guides below are available:

- `features/po-tally`: [PO List By Date and automatic Tally export](docs/README-PO-TALLY.md)
- `features/cashflow`: [ERP-integrated Cashflow Analyzer](docs/README-CASHFLOW.md)
- `features/monthly-stock`: [Monthly Stock report and Finance baseline import](docs/README-MONTHLY-STOCK.md)

Read the matching guide before deploying. It covers build/start commands, database requirements, configuration, verification and rollback. Tally server IP/company settings and database credentials are environment-specific. Finance baseline rows must be imported separately; merging code does not transfer them.

Generated builds, ZIP/EXE/WAR releases, backups and local caches are excluded. Build your deployment artifact from source. Automatic jobs are disabled by default; enable them only after checking the target database and Tally company.
