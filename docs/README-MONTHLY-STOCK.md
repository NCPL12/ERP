# Neptune ERP — monthly-stock deployment guide

Branch: `features/monthly-stock`. Base: `origin/development` (`652adad`). Merge through the team review process into `development`; do not deploy to `main`. This branch contains only the feature described here, its tests/docs, and shared build/ignore requirements. Newer development role/dashboard/returnable functionality is retained.

Generated output tracked by the existing development base is removed from the index in this branch; files are retained locally and ignored for future builds. No Finance/customer workbook or generated release is added.

## Prerequisites and files to share

- Maven and a project-compatible JDK; `pom.xml` targets Java 8 and Spring Boot 2.2.2. Local development has also used JDK 21. Confirm the team's chosen runtime before rollout.
- An existing ERP MySQL database, configured datasource credentials, and an authorized ERP login.
- Monthly Stock reads ERP data; a Tally connection is not required for this feature.
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

Back up/select the existing ERP MySQL schema, then run [Monthly Stock schema](monthly-stock-schema.sql). This creates a missing `tbl_monthly_stock_movement` table; it does not alter an incompatible existing table. Compare its columns and `(item_master_id, report_date)` unique constraint with the entity. No ERP transaction quantity updates are needed. Import the approved Finance baseline separately after deploying.

## Monthly Stock report

### Changes and report usage

Deploy the report changes together with `StockReportMoney` and the corresponding GRN/DC rounding changes. The monthly report uses consistent decimal rounding, approved closing snapshots where present, and a reconciliation sheet separating actual delivery challans from stock adjustments.

Open **Sales Reports > Monthly Report** and use **01-09-2026 to 30-09-2026** for September. The route is `/stock/summary_details/`. The exported workbook includes **Stock summary** and **Movement Reconciliation**.

- Compare GRN quantities/values with GRN Report By Date for the same dates.
- Compare actual DC quantities/values on Movement Reconciliation with DC Report By Date.
- The main sheet's net outward includes stock adjustments; it need not equal actual DC totals.
- Review opening/closing negatives and missing-price flags instead of hiding them.

### Import an approved baseline on the target database

The Finance baseline imported locally is not included in Git. To reproduce that historical correction elsewhere:

1. Back up the target database and confirm the intended environment/schema.
2. Obtain the Finance-approved workbook separately. For the September case, the supplied file is `Clsoing Stock Statement for Sep-26.xlsx`.
3. Keep the first-sheet layout unchanged: row 1 is the title, row 2 is the header, and data starts at row 3. The importer requires **Id** in column A, **Stock** in column H and **Supply Price** in column K. It reads model number from B and description from C. Do not rename Supply Price to SupplyPrice. Duplicate item IDs will conflict with the item/date uniqueness constraint. Blank quantities/prices are read as zero; active items with zero price are flagged for review.
4. Log in as ERP ADMIN / SUPER ADMIN. There is currently no dedicated baseline upload button. Using Postman or an equivalent HTTP client with that authenticated ERP session, submit:

   ```text
   POST http://<ERP-HOST>:<PORT>/ncpl-sales/api/monthly_stock_report/baseline/import
   Body: multipart/form-data
     file         [File]  <approved workbook>
     closingDate  [Text]  30-09-2026
   ```

   Let the client generate the multipart boundary. Retain the authenticated session and any CSRF token required by the deployed security configuration; do not disable security or place credentials in the README. Use the actual deployment context path.
5. Check status and imported count. A login-page response is not success. An existing snapshot for the date returns a conflict; stop and investigate rather than deleting/reimporting. The current success message says 'opening-balance items', but the supplied closingDate identifies the closing snapshot.
6. Regenerate 1–30 September and compare with the approved workbook. For the exact workbook checked locally, 8,191 records were imported; closing quantity is 146,123 and closing value is INR 22,017,791.60. Verify these values on the target machine instead of assuming they transferred.

The import freezes report snapshot rows; it does not change stock transactions. The local September report has zero negative closing quantities, but **six negative opening balances remain**, requiring an approved **31 August** closing statement. **Three active items have missing prices**, so the reported closing value is not a fully priced valuation. This correction is not a blanket guarantee of no negatives in every month.

### Authenticated import using Postman

For testers who do not already have an authenticated HTTP client:

1. Set a private Postman variable `erpBaseUrl` to the actual ERP URL including its context, for example `http://<ERP-HOST>:8880/ncpl-sales`.
2. Send GET `{{erpBaseUrl}}/login` to establish cookies. In the same Postman cookie jar, send POST `{{erpBaseUrl}}/login` with **x-www-form-urlencoded** fields `username` and `password`, using the tester's existing ADMIN/SUPER ADMIN account. Enter credentials privately; do not save them in shared collections. If the deployed login requires a CSRF token, retain and submit the token from the login form.
3. Confirm login succeeded by requesting an authorized ERP page and checking that the response is not the login form. Keep the session cookie in that client's cookie jar; no password is sent with the baseline upload itself.
4. Send the multipart baseline request in this guide with that cookie jar. Set `file` to the File type and `closingDate` to Text. Do not add a manual Content-Type boundary.
5. Check the response and then verify the generated monthly workbook. Log out and clear private credentials/session data when finished.

In the current source, baseline routes use the legacy ERP security chain, which disables CSRF, and ADMIN/SUPER ADMIN method authorization protects the import/freeze endpoints. Cashflow routes use a separate chain with CSRF enabled. The generic `/api/**` URL permit rule does not remove the baseline method authorization. Retain any additional security required by the deployed environment; do not change security settings just to make the import work.

### Future month-end snapshots

`monthly.stock.auto-freeze.enabled` defaults to false and is false in dev. After Finance approves the baseline and month-end process, the scheduler can be enabled:

```properties
monthly.stock.auto-freeze.enabled=true
monthly.stock.auto-freeze.zone=Asia/Kolkata
monthly.stock.auto-freeze.cron=0 59 23 * * ?
```

It checks at 23:59 and acts only on the last calendar day of the month. ERP must be running, and postings must be finalized before capture; it does not automatically repair missed historical months or transactions posted after the snapshot. Existing snapshots are not overwritten. A manual administrator freeze endpoint also exists at `/api/monthly_stock_report/freeze`, taking reportFromDate/reportToDate in `dd-MM-yyyy`; use only after Finance confirms that period is ready to freeze.


## Build and acceptance

```powershell
mvn "-Dtest=StockReportMoneyTest" test
```

Build a clean WAR with `mvn clean package`. Automated tests are not live Finance acceptance. Validate the target database/company/date scope before enabling scheduled jobs. Back up application/configuration and data before deployment. Restore the prior application through the team process for rollback; restoring ERP does not undo Tally imports.
