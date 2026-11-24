# PO export timeout and progress fix (#166)

Branch: `fix/viren-po-tally-166-development`, based on `origin/development`
at `e3a5783`. Only the PO timeout fix is included; unrelated main-branch
changes are not brought into development.

The Sales Reports page previously submitted a synchronous form and waited for
all master reads, imports and verification before receiving a response. A web
server/proxy timeout could hide the outcome even while the export continued.

The Send to Tally button now starts a background job and polls its status.
Preparation identifies groups, ledgers, stock items, units and existing POs;
import/verification shows processed, new, updated, skipped and failed counts.
The final table contains the verified per-PO results. Duplicate clicks are
blocked in both the browser and the server. Reopening the page reconnects to
the latest job, and a lost response never automatically retries an import.

## Deploy

Build/deploy the complete ERP WAR, including the new Java service, static JS
and JSP. No database migration, Tally IP change or scheduler change is required.
Restart ERP and refresh the browser so it loads the new JS asset.
Only users with `PURCHASE_EDIT` can start exports.

New API, relative to the ERP context:

- `POST /po_list/tally/jobs`: form fields `poListByFromDate`,
  `poListByToDate` (`dd-MM-yyyy`); returns HTTP 202 with `jobId` and state.
- `GET /po_list/tally/jobs/{jobId}`: polls state, progress, result and error.
- `GET /po_list/tally/jobs/current`: reconnects to the latest manual job.

Polling is read-only. The older synchronous endpoints remain compatible for
existing clients; clients needing to avoid long HTTP waits should use this job API.
The normal ownership checks, duplicate detection, shared Tally sync lock,
per-request deadlines and post-import verification remain in effect.

Jobs are process-local and retain the latest 20 results. They are not resumed
automatically after an ERP restart. Allow an active export to finish before
restarting. If interrupted, inspect Tally and the saved verified sync state
before another manual run. A genuine Tally connection timeout is still reported
as an error; this change does not claim to repair a remote Tally service.

## Validation

No production Tally requests or imports were used. Tests use Mockito and a
localhost fake Tally HTTP server.

```text
mvn -B -o -Dtest=TallyPurchaseOrderServiceTest,TallyPurchaseOrderAutoSyncTest,TallyIssueRegressionTest,TallyVerificationFlowTest,AssignedTallyIssuesTest,TallyPurchaseOrderJobsTest,TallyExportMethodSecurityTest test
node src/test/js/po-tally-jobs.test.cjs
node src/test/js/po-tally-refresh.test.cjs
```

The tests cover a blocked Tally worker with immediate HTTP acceptance, live
status reads, duplicate starts, final result/error delivery, invalid dates,
revoked export permissions, reconnects without resubmission, and existing
voucher ownership/verification regressions.
