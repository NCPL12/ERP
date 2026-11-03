# ERP purchase orders to Tally: nightly daily sync

## What this change does

The opt-in `po-tally` profile runs once each night at 12:15 AM Asia/Kolkata by default. Each run sends only active POs created during the previous completed calendar day. It does not scan history, recheck older POs, or backfill a date when the ERP was offline at its scheduled run. Changes made to an older PO after its day has passed are not sent automatically.

Archived or deleted POs are outside the daily export window. The scheduler never deletes or cancels Tally vouchers.

Before an existing ERP-origin PO is altered, the exporter compares Tally's current `ALTERID` with the last verified value. A changed ID, changed ERP-import narration, or absent legacy baseline stops the update and records `NEEDS_REVIEW`. It also rereads the Tally voucher immediately before writing to catch edits made during a run. Existing v3 state entries remain readable; new verified state uses separate ERP-content and Tally-change values in the v4 format.

Users with `PURCHASE_EDIT` can resolve a review from the status table:

- **Accept Tally version** stores the current Tally version as the baseline without writing a voucher.
- **Force ERP version** requires typing `FORCE ERP VERSION`, then performs one ERP-to-Tally overwrite after rechecking the current Tally change ID. The action is recorded in the sync journal.

An empty date range reports zero totals and a neutral no-PO message. Progress counts are maintained incrementally; updates are published no more often than once per 25 POs or once per second. Verified PO state checkpoints are written every 1,000 POs and once when an export ends.

## One-line on/off switch

In `application-po-tally.properties`:

```properties
tally.po.sync.enabled=false
tally.po.sync.cron=0 15 0 * * *
tally.po.sync.time-zone=Asia/Kolkata
```

Keep `false` to disable scheduled writes. Set to `true` to enable them after testing and approval. The default cron runs at 12:15 AM daily and processes only the previous calendar day. A command-line option for the same property overrides the profile file. Restart the ERP service after a configuration change. This is a deployment setting, not an in-page toggle.

This branch's checked-in profiles point to the configured Neptune Tally server and exact company name. Override them for another environment; do not enable automatic sync in a test deployment unless it targets a separate test Tally company.

## Deploy and configure

1. Merge this branch and build the ERP normally. No SQL migration or database schema change is needed.
2. Back up the existing ERP build and Tally data. Deploy with the `po-tally` Spring profile while `tally.po.sync.enabled=false`.
3. Configure `cashflow.tally.server.url`, the exact `cashflow.tally.company-name`, `tally.po.sync.cron`, `tally.po.sync.time-zone`, and absolute path for `tally.po.sync.state-file`. Confirm Tally HTTP import access from the ERP host.
4. Run first against a separate test Tally company and state directory. Confirm a new PO created on the previous day is sent, while POs from earlier dates are not selected. Confirm the state file prevents duplicate creation on a retry.
5. After Finance approves production, enable only one designated ERP server and restart it. Check **Sales Reports → PO List by Date** for the last run, errors, and saved review statuses.

Do not run a separate terminal importer at the same time. Keep the ERP, Tally HTTP service, intended company, and saved state file available. Back up `tally-po-sync.properties` and its sibling `.journal.json`; these contain the verified PO state and latest auto-sync status. Do not remove them to force retries.

## Monitoring and safety

The PO List by Date page shows whether the scheduler is on, its nightly schedule, last run totals, and saved failure/review records. The page refreshes status every 15 seconds. Each run is limited to one calendar day; it does not perform a historical reconciliation.

No voucher is deleted automatically. Only POs in the previous-day window are considered; updates to older POs require a manual PO export after review.
