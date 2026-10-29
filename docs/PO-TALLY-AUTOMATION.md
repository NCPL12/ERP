# ERP purchase orders to Tally: daily automatic sync

## What this change does

The opt-in `po-tally` profile can run a PO sync once a day. It uses a fixed delay after the previous run completes, with a 30-second initial delay. Each run checks POs from the configured start date and rechecks older POs previously verified by this exporter, so edits to older POs are picked up too.

Archived or missing ERP POs are recorded as `CANCELLATION_REVIEW`. The scheduler never deletes or cancels Tally vouchers. Finance must investigate and decide separately, including any downstream references.

## One-line on/off switch

In `application-po-tally.properties`:

```properties
tally.po.sync.enabled=false
```

Keep `false` to disable scheduled writes. Set to `true` to enable them after testing and approval. A command-line option for the same property overrides the profile file. Restart the ERP service after a configuration change. This is a deployment setting, not an in-page toggle.

The checked-in profile uses `localhost` and a blank company name as safe placeholders. Set the Tally server address and exact company name for the target environment; do not commit production credentials or enable the scheduler in a test deployment.

## Deploy and configure

1. Merge this branch and build the ERP normally. No SQL migration or database schema change is needed.
2. Back up the existing ERP build and Tally data. Deploy with the `po-tally` Spring profile while `tally.po.sync.enabled=false`.
3. Configure `cashflow.tally.server.url`, the exact `cashflow.tally.company-name`, `tally.po.sync.from-date`, and absolute path for `tally.po.sync.state-file`. Confirm Tally HTTP import access from the ERP host.
4. Run first against a separate test Tally company and state directory. Confirm a new PO, an unchanged PO, an edited older PO, and an archived/deleted ERP PO produce the expected results. Confirm cancellation review does not remove anything from Tally.
5. After Finance approves production, enable only one designated ERP server and restart it. Check **Sales Reports → PO List by Date** for the last run, errors, and saved review statuses.

Do not run a separate terminal importer at the same time. Keep the ERP, Tally HTTP service, intended company, and saved state file available. Back up `tally-po-sync.properties` and its sibling `.journal.json`; these contain the verified PO state and latest auto-sync status. Do not remove them to force retries.

## Monitoring and safety

The PO List by Date page shows whether the scheduler is on, its interval, last run totals, and saved failure/cancellation-review records. The page refreshes status every 15 seconds. A daily run may take longer on its first reconciliation; later runs skip unchanged verified POs.

No voucher is deleted automatically. ERP edits are handled through the existing guarded Tally update path; Finance edits or ambiguous results remain subject to the existing PO-Tally checks and review statuses.
