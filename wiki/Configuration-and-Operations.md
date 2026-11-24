# Configuration and operations

## Profiles and data

`application.properties` selects the active Spring profile through Maven filtering. Profile files live in `src/main/resources/`:

| Profile | Intended use | Database behavior |
| --- | --- | --- |
| `dev` | Local development | MySQL settings in `application-dev.properties` |
| `qa` | QA environment | MySQL settings in `application-qa.properties` |
| `prod` | Production | MySQL settings in `application-prod.properties` |
| `h2` | Isolated development/tests | In-memory H2 with `create-drop` |

Set `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` for the target environment using deployment-managed configuration. Review `spring.jpa.hibernate.ddl-auto` before pointing the app at an existing database. Back up the database before schema or data changes. SQL dumps in the parent folder of this checkout may contain real data and should not be copied into the wiki.

## Tally purchase-order integration

The purchase-order integration reads `cashflow.tally.server.url`, `cashflow.tally.company-name`, and `cashflow.tally.default-stock-group`. Set the company name to the exact destination company and confirm the Tally service is reachable before export. The UI uses `/po_list/tally/options`, `/po_list/tally`, `/po_list/tally/sync-status`, and `/po_list/tally/progress`; importing through `/po_list/tally/import` also requires `PURCHASE_EDIT`.

Do not publish a live Tally endpoint or credentials in the wiki. The repository contains tests for export, automatic sync, and validation in `src/test/java/com/ncpl/sales/service/`.

## Error alerts and scheduled work

In `dev` and `prod`, `error.alert.to` enables HTTP 5xx email alerts. `error.alert.cooldown-ms` controls suppression of repeated alerts for the same route and failure type; the default in code is five minutes. Check mail configuration and delivery in the target environment before relying on alerts. The app also has scheduled email/report jobs; review their service classes and recipients before enabling a new deployment.

## Deployment checklist

1. Build with JDK 8 and run `mvn clean verify`.
2. Select the intended profile and review its database, port, context path, mail recipients, and Tally settings.
3. Confirm user roles and permissions in the target database.
4. Confirm database backup and recovery procedures for that environment.
5. Smoke test login and the workflows used at that site, then check logs and alert delivery.

This checklist supplements your site's deployment process; the repository does not contain a complete environment-specific deployment runbook.
