# Cashflow inside ERP

The cashflow module runs inside the existing ERP WAR, on ERP's port and context path.
It does not use the standalone application, port 8081, a proxy, or a separate login.
ERP remains on Java 8 / Spring Boot 2.2.2 with Apache POI 3.14 and Tiles/JSP.
Thymeleaf is scoped to cashflow views; the module uses Java-8-compatible Tally HTTP requests.

## Local test

From this ERP-development folder, build with Maven:

```powershell
mvn "-Dmaven.compiler.release=8" package
```

Stop your existing local ERP instance before starting another on port 8880.
Use your existing local ERP database and login configuration, and add the cashflow test profile:

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=dev,cashflow-test"
```

Confirm the dev datasource is your local ERP database before starting. Hibernate's existing
schema-update setting creates three additional tables in that database:
`cashflow_invoices`, `cashflow_recurring_transactions`, and `cashflow_tally_sync_status`.
Existing ERP tables and standalone cashflow databases are not migrated or renamed.

Log in as an existing ERP `ADMIN` or `SUPER ADMIN`. Open the Cashflow Analyzer tile, or:

http://localhost:8880/ncpl-sales/cashflow-analyzer/overview

The test profile uses Tally at `http://192.168.0.176:9000` and company
`Neptune Controls Pvt Ltd. -2024-2025 1-Apr-24)`. Keep that company loaded.
Click **Sync Now** to populate the module's own tables. Scheduled synchronization in this
test profile starts after 24 hours and repeats every 24 hours; stop the test when finished.
Without the cashflow test profile, synchronization defaults to disabled.

The overview, receivables, payables, advances, cost-centres, aging, reconciliation, and
cashflow-forecast routes are all beneath `/cashflow-analyzer`.
`/aging-analysis` redirects to the integrated aging view.

## Security and configuration

The module reuses ERP's authenticated session and permits `ADMIN` and `SUPER ADMIN` only.
Its filter chain requires CSRF tokens for mutations. ERP's existing security rules remain
unchanged. Finance API URLs are scoped below `/cashflow-analyzer/api/cashflow`, outside ERP's
public `/api/**` matcher. JSON date configuration applies only to finance requests.

Configuration keys are namespaced to avoid changing ERP's existing Tally integration:

```properties
cashflow.tally.server.url=http://TALLY-HOST:9000
cashflow.tally.company-name=Exact company name
cashflow.tally.sync.enabled=true
cashflow.tally.sync.interval-ms=60000
cashflow.tally.sync.initial-delay-ms=60000
```

Use external environment-specific settings for deployment. No production configuration
has been changed and no production rollout has been performed.

## Verification

The Maven suite includes the original financial classification and Excel export tests,
Java 8 HTTP request tests using a local fake Tally server, page rendering under the ERP
context path, ERP-role/CSRF checks, H2 persistence and report tests, and a complete ERP
context test with scheduled jobs disabled. It does not use a production database.

Run the client-side URL and CSRF test with Node:

```powershell
node src/test/js/cashflow-paths.test.js
```

Still required before rollout: browser verification against the actual local ERP login,
live Tally sync into the integrated module, comparison with the standalone report totals,
and regression checks of the ERP workflows used by the team. Passing automated checks
does not establish those live results.

The standalone analyzer remains available as the comparison baseline. Its unused legacy
modal pages were carried across; their existing incomplete UI behaviour (notably recurring
transaction actions) is not a new accounting implementation.


## Local verification — 18 September 2026

Fixed dashboard tile/close navigation and the top-performers export URL to retain
`/ncpl-sales/cashflow-analyzer`. Manual sync now reports a disabled configuration
instead of silently succeeding; the UI retains the immediate failure message.
Sync timestamps explicitly use Asia/Kolkata because existing ERP code sets the
JVM default timezone to EST. ERP's global timezone was not changed.

Validation: clean Java 8-compatible build, 19 Java tests, JavaScript syntax and URL/CSRF
checks passed. All 14 authenticated page routes returned HTTP 200. Live sync imported
640 bills; browser reports showed 289 receivables, 351 payables, 55 advances and
749 Cost Centres. Browser Sync Now updated the timestamp to 14:43:55 IST.
Aging, reconciliation and weekly forecast data endpoints returned JSON successfully.

The executable WAR launch encountered an existing servlet-api 2.3 / Tomcat 9
classpath conflict. Local verification used SalesApp with target/classes and Maven's
compile dependency classpath excluding servlet-api, with dev,cashflow-test profiles.
Do not treat the packaged WAR as deployment-validated. Existing Eclipse startup remains
supported; stop the background verification process before starting another ERP on 8880.
