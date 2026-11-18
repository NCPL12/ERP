# Neptune ERP — cashflow deployment guide

Branch: `features/cashflow`. Base: `origin/development` (`652adad`). Merge through the team review process into `development`; do not deploy to `main`. This branch contains only the feature described here, its tests/docs, and shared build/ignore requirements. Newer development role/dashboard/returnable functionality is retained.

Generated output tracked by the existing development base is removed from the index in this branch; files are retained locally and ignored for future builds. No Finance/customer workbook or generated release is added.

## Prerequisites and files to share

- Maven and a project-compatible JDK; `pom.xml` targets Java 8 and Spring Boot 2.2.2. Local development has also used JDK 21. Confirm the team's chosen runtime before rollout.
- An existing ERP MySQL database, configured datasource credentials, and an authorized ERP login.
- Tally with HTTP access enabled, the intended company loaded, and the required masters for Tally features. Tally need not run on the ERP host; the configured address must be reachable from the ERP process.
- Node.js is optional for the Cashflow client-side test.

Share source, tests, build configuration and documentation through Git. Do not commit generated ZIP/EXE/WAR/JAR releases, `target/`, `deployment/output/`, backups, logs, caches, runtime checkpoints or handoff copies. These generated directories are ignored in the current branch. Never push the local `backup/viren-before-generated-cleanup-20261008` recovery branch or use `git push --all`.

Supply environment credentials and Finance-approved workbooks separately. Git does not transfer database rows or the Finance baseline imported on a developer laptop.

## Deployment procedure common to all features

1. Back up the target ERP database and the currently deployed application/configuration. For PO synchronization, also preserve the runtime checkpoint file described below.
2. Review and merge the approved feature branch into `development` through the team's process. Resolve shared-file conflicts without removing the other modules' routes or configuration.
3. Configure the correct datasource, Tally URL/company, port and context path externally. Do not reuse laptop credentials or assume the checked-in dev settings are appropriate for another machine.
4. Prepare/verify the database schema in Database preparation before starting with schema changes disabled.
5. Build from the repository root:

   ```powershell
   mvn clean package
   ```

   For the Maven QA profile, use `mvn -Pqa clean package`; use the team's selected deployment profile. The default Maven profile is dev. The WAR is generated under `target/`; deploy the newly built WAR using the team's existing ERP/Tomcat process. Generated WAR files are deployment artifacts, not Git source.
6. Stop the previous ERP process before replacing/restarting it. Start only one ERP instance for the target database and scheduled jobs. Retain the existing ERP context path when replacing the WAR.
7. Verify login, feature pages, logs and acceptance checks below. Merging source alone does not update the running application.

### Local development startup with scheduled writes disabled

After the database schema is ready, these PowerShell overrides prevent initial PO export, Cashflow sync and month-end snapshot writes:

```powershell
$env:TALLY_PO_SYNC_ENABLED = 'false'
$env:CASHFLOW_TALLY_SYNC_ENABLED = 'false'
$env:MONTHLY_STOCK_AUTO_FREEZE_ENABLED = 'false'
$env:SPRING_JPA_HIBERNATE_DDL_AUTO = 'none'
mvn spring-boot:run "-Dspring-boot.run.profiles=dev,cashflow-test"
```

Open `http://localhost:8880/ncpl-sales/login`. These environment values apply to the process started from this terminal. Enable the relevant scheduler explicitly only after configuration and test scope have been accepted. This feature branch keeps its scheduled job disabled by default. Configure and enable it explicitly after acceptance checks.

### External configuration on the deployed machine

Keep machine credentials outside Git. Set the existing ERP datasource URL/user/password through the team's external configuration. Once schema setup is complete, use `spring.jpa.hibernate.ddl-auto=none` (or the team's validation policy). Do not overwrite existing datasource SSL/options blindly.

An external properties file can be loaded by setting `SPRING_CONFIG_ADDITIONAL_LOCATION=file:C:/ERP/config/erp-local.properties` for the actual ERP process. For Maven testing in PowerShell:

```powershell
$env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'file:C:/ERP/config/erp-local.properties'
```

For Windows service/Tomcat deployment, configure that location in the service environment/JVM configuration and restart the service; a variable in your terminal does not change a running service. Replace host, port and context path examples with the real deployment values. Only one ERP scheduler instance should operate on the target environment.

## Database requirements

| Feature | Database/schema requirement | Data preparation |
| --- | --- | --- |
| Cashflow | Five Cashflow tables and their current columns/indexes, listed below | Sync from the target Tally company; do not copy another environment's cache or manual plans blindly |

Cashflow tables:

- `cashflow_invoices` — imported invoices and cashflow annotations, including expected cash date and remarks.
- `cashflow_recurring_transactions` — recurring transaction records.
- `cashflow_tally_sync_status` — synchronization state/errors.
- `cashflow_finance_plan_entries` — manual Finance cash-plan entries.
- `cashflow_finance_plan_settings` — opening balance, balance date and reserve settings.

Use the current entities under `src/main/java/com/ncpl/sales/cashflow/entity/` as the schema reference. An older deployment may already have three tables but still lack cash-plan tables or newer invoice columns. Existing table names alone are not enough to establish compatibility.

### Cashflow MySQL migration: exact order

These SQL files are manually applied deployment scripts; this project does not automatically execute them through Flyway.

1. Stop ERP/sync jobs and back up the database. Select the correct existing ERP schema in MySQL Workbench. Run [schema checks](database/cashflow_schema_checks.sql) before migration and save the results.
2. Execute [V001 tables](database/V001__cashflow_tables.sql) as a full script. It creates any missing Cashflow tables with the current mapped columns/indexes. It does not replace existing tables or seed financial data.
3. Execute [V002 existing columns](database/V002__cashflow_existing_columns.sql) as a full script in the same selected schema/session. It adds missing expected_cash_date, cashflow_remarks and last_sync_end_time columns; repeating it skips columns already present.
4. Re-run schema checks and compare every existing column/index with V001 and the entities. Stop if old types/lengths/required fields differ. No script can safely infer a backfill for an arbitrary incompatible legacy schema. MySQL DDL auto-commits; restore from backup through the team's rollback process if necessary.
5. Start the updated ERP against the migrated test database with `spring.jpa.hibernate.ddl-auto=validate`. Successful Hibernate validation checks mapped schema compatibility. Then use the team's normal `none`/validation deployment policy, test Finance plan save/reload and recurring entries, and enable Tally sync for acceptance.

These scripts were checked against the source entities, not executed on the testing team's MySQL server. Existing indexes on old servers may need review for supported key sizes/character sets. No existing invoice rows, manual plans or ERP stock transactions are deleted or backfilled by these scripts.

## ERP-integrated Cashflow Analyzer

### Configure and start

Use the same ERP WAR and login; do not activate `cashflow-standalone` for this integrated deployment. Prepare the five tables in Database preparation. Configure:

```properties
cashflow.tally.server.url=http://<TALLY-HOST>:9000
cashflow.tally.company-name=<exact company name>
cashflow.tally.sync.enabled=true
cashflow.tally.sync.initial-delay-ms=30000
cashflow.tally.sync.interval-ms=300000
```

This branch defaults Cashflow sync to disabled and provides `127.0.0.1:9000` as an example; that address works only if Tally is on the same machine as ERP. PO export uses the same connection URL/company, but its enable flag is independent. Start with both sync flags false while verifying the environment.

Log in as ERP **ADMIN / SUPER ADMIN**, then open `/ncpl-sales/cashflow-analyzer/overview`. Confirm Tally is connected and use **Sync Now** where available after enabling/configuring Cashflow sync.

### Acceptance checks

Check overview, receivables/payables, advances, customer/vendor analysis, cost centres, aging, forecast, reconciliation, recurring transactions and Finance cash plan. Match selected dates/company and compare totals with Tally. Test authorized access, exports and manual-plan persistence after restart.

Invoice Pipeline combines ERP GRN/vendor invoice and sales/client-invoice data with Tally matches. Missing source records or ambiguous matches remain review items; do not manufacture links to clear counters. A zero Linked History count alone does not prove vendor-side matching is broken.

Cashflow annotations, plans and recurring entries are database data and need backup. Deploying code does not transfer a developer's saved entries. Further implementation detail: [Cashflow integration guide](CASHFLOW-INTEGRATION.md); its old table count and Tally server examples are superseded by this guide.

### Connecting ERP to the main Tally server

The Tally data/HTTP server is the destination; the network licence server may be a different machine. Confirm the data server's IP/hostname, configured HTTP port and exact company name with the Tally administrator.

1. On the Tally data server, enable its HTTP/server interface using the installed Tally version's connectivity settings. Confirm the actual port (commonly 9000) and keep the intended company loaded.
2. Permit the ERP host to reach that port through the office network and Tally server firewall. Limit access to the required internal hosts; do not expose Tally's HTTP interface publicly.
3. Configure the deployed ERP application externally:

   ```properties
   cashflow.tally.server.url=http://<TALLY-SERVER-IP>:9000
   cashflow.tally.company-name=<exact loaded company name>
   ```

   Substitute the actual port if different. Both PO export and Cashflow currently use these properties. `127.0.0.1` or `localhost` works only when Tally and ERP run on the same machine. The test profile's local address must be overridden for the office server.
4. Run this from the ERP Windows host, replacing the placeholder with the real address:

   ```powershell
   Test-NetConnection -ComputerName '<TALLY-SERVER-IP>' -Port 9000
   ```

   `TcpTestSucceeded: True` confirms network/port connectivity only; it does not establish that the correct company or masters are available.
5. Restart ERP with the new configuration and scheduled PO export disabled initially. Check application Tally availability, confirm the company, then verify one scoped PO export and a Cashflow sync against that company. Enable automatic PO export only after these checks and the approved date scope are confirmed.


## Build and acceptance

```powershell
mvn test
node src/test/js/cashflow-paths.test.js
```

Build a clean WAR with `mvn clean package`. Automated tests are not live Finance acceptance. Validate the target database/company/date scope before enabling scheduled jobs. Back up application/configuration and data before deployment. Restore the prior application through the team process for rollback; restoring ERP does not undo Tally imports.

## Projected cash balance graph

The graph uses a dated Finance opening cash/bank balance, expected invoice collections, and planned invoice payments. Opening and closing balances carry forward across empty days and into daily drill-down. A missing dated balance displays Not set; zero is accepted only as an explicit balance. Future invoice due dates are fallback assumptions. Past-due bills or expired expected dates remain excluded until Finance schedules a future expected cash date. Backlog remains separately accessible and is not treated as money already received/paid.

Use Save opening balance on the graph page to persist the amount/date in the existing Finance Plan settings table. Expected dates can be saved from the invoice detail table and are preserved for retained invoices during Tally sync. These settings are shared with Finance Plan. No new database migration is needed beyond the existing Cashflow settings/invoice schema.

This graph models outstanding invoice movements; manual expense/plan entries and historical bank transactions are not included. Enter a confirmed opening balance appropriate to the date. Invoice detail export exports the selected invoice IDs, including expected dates, rather than applying the old due-date-only filter. Test the dated opening, collection/payment assumptions and backlog with Finance before relying on projected balances.

## Retention in outstanding tables

Retention only filters bills whose Tally ledger/party name explicitly contains the word Retention or Retainage, including labels such as Retention-JSS and Retention - Manipal Udupi found in the local snapshot. The separate Retention column is a component of Outstanding, not an additional amount. Both header totals follow all filters/search; Excel applies the same retention filter and includes that column. The classification works for either receivables or payables and does not estimate a percentage of ordinary invoices. Unlabelled or partially retained ordinary invoices cannot be identified by this rule.

Retention is excluded from the projected balance until a future expected release date is explicitly recorded. Standard future due-date fallback applies to ordinary invoices, not retention. No new database columns are needed.
