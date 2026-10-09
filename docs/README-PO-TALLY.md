# Neptune ERP — po-tally deployment guide

Branch: `features/po-tally`. Base: `origin/development` (`652adad`). Merge through the team review process into `development`; do not deploy to `main`. This branch contains only the feature described here, its tests/docs, and shared build/ignore requirements. Newer development role/dashboard/returnable functionality is retained.

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
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Open `http://localhost:8880/ncpl-sales/login`. These environment values apply to the process started from this terminal. Enable the relevant scheduler explicitly only after configuration and test scope have been accepted. This feature branch keeps its scheduled job disabled by default. Configure and enable it explicitly after acceptance checks.

### External configuration on the deployed machine

Keep machine credentials outside Git. Set the existing ERP datasource URL/user/password through the team's external configuration. Once schema setup is complete, use `spring.jpa.hibernate.ddl-auto=none` (or the team's validation policy). Do not overwrite existing datasource SSL/options blindly.

An external properties file can be loaded by setting `SPRING_CONFIG_ADDITIONAL_LOCATION=file:C:/ERP/config/erp-local.properties` for the actual ERP process. For Maven testing in PowerShell:

```powershell
$env:SPRING_CONFIG_ADDITIONAL_LOCATION = 'file:C:/ERP/config/erp-local.properties'
```

For Windows service/Tomcat deployment, configure that location in the service environment/JVM configuration and restart the service; a variable in your terminal does not change a running service. Replace host, port and context path examples with the real deployment values. Only one ERP scheduler instance should operate on the target environment.

## Database preparation

No new PO synchronization table is needed. Configure the existing ERP database. Keep the local verified PO state file persistent and writable. The exporter uses cashflow.tally.* connection property names even when the Cashflow module is absent.

## PO-to-Tally integration

### Connection and automation settings

The PO exporter currently shares these connection properties with Cashflow:

```properties
cashflow.tally.server.url=http://<TALLY-HOST>:9000
cashflow.tally.company-name=<exact company name loaded in Tally>
cashflow.tally.default-stock-group=Billable Materials

tally.po.sync.enabled=false
tally.po.sync.from-date=2024-04-01
tally.po.sync.include-previously-exported=false
tally.po.sync.interval-ms=300000
tally.po.sync.initial-delay-ms=30000
tally.po.sync.state-file=data/tally-po-sync.properties
```

`from-date` is ISO `yyyy-MM-dd`; select the actual Finance-approved scope. The example date is not a requirement to import all history. Only enable `tally.po.sync.enabled=true` after confirming scope and company. This branch defaults PO sync to disabled; 2024-04-01 is only an example starting date. The scheduler uses a five-minute delay after each run completes, and an initial 30-second delay; a large run can take longer than five minutes.

If `include-previously-exported=true`, previously tracked exports may be included outside the starting-date scope. Keep it false unless that behavior is intended. Automatic sync excludes the CODEX-, ERP-PO-TEST and ERP-PO-VERIFY test prefixes.

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
5. Restart ERP with the new configuration and scheduled PO export disabled initially. Check application Tally availability, confirm the company, then verify one scoped PO export against that company. Enable automatic PO export only after these checks and the approved date scope are confirmed.

### Run and verify

1. Back up the intended Tally company before a bulk test. Confirm its HTTP interface is reachable from the ERP host and the exact company is loaded.
2. Open **Sales Reports > PO List By Date**, select From/To dates, and check Tally availability. Date filters remain; purchase and GST ledger selection is automatic.
3. Run a small manual export first. Check per-PO results and open the resulting purchase order in Tally. Verify vendor, PO number/date, item quantities/rates, applicable CGST/SGST or IGST, and total.
4. Enable automatic sync and restart/reconfigure ERP through the deployment process. Monitor the Sales Reports status and application logs. ERP and Tally must remain running; closing either prevents unattended sync.

PO export can create missing vendor ledgers and stock-item masters in Tally from ERP data. Existing Tally units, stock groups, purchase ledgers and tax mappings still need to be valid. Review the created masters as part of the test; the export is not limited to voucher creation. Existing unmarked Tally vouchers are protected. Unchanged verified exports may be skipped. Empty/nonpositive POs, missing masters, incorrect company configuration or Tally rejection can still fail. Do not interpret every skip as failure or claim that automation guarantees zero errors.

The state file contains verified fingerprints/checkpoints. Keep it writable and persistent across restarts, and separate it by environment/company. Do not delete it casually or commit it. Avoid concurrent manual/automatic exports and duplicate ERP instances.

The separate handoff PowerShell script is not currently tracked in this repository; an absolute path from a developer laptop is not a deployment command. Use the application UI/automation in this source checkout. Additional detail: [PO sync guide](tally-po-auto-sync.md), which contains earlier operational notes; current source/configuration and this guide take precedence over stale examples.


## Build and acceptance

```powershell
mvn "-Dtest=TallyPurchaseOrderServiceTest,TallyPurchaseOrderAutoSyncTest" test
```

Build a clean WAR with `mvn clean package`. Automated tests are not live Finance acceptance. Validate the target database/company/date scope before enabling scheduled jobs. Back up application/configuration and data before deployment. Restore the prior application through the team process for rollback; restoring ERP does not undo Tally imports.
