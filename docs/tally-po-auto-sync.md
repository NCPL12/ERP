# ERP purchase orders to Tally

The ERP exporter updates the original imported purchase order, keeps the vendor first, and verifies the vendor identity, total and GST after Tally accepts the request. Vendor aliases are accepted only when Tally returns the same ledger GUID.

Automatic synchronization is implemented in `TallyPurchaseOrderAutoSync`. It runs every five minutes, prevents concurrent exports, excludes test PO prefixes and retries if Tally is unavailable. Verified ERP payload fingerprints and Tally alteration IDs are persisted in `data/tally-po-sync.properties`. Large exports checkpoint every 250 POs. The application and Tally must be running.

Automatic synchronization remains disabled until Finance selects the historical date scope. Set `tally.po.sync.enabled=true` and `tally.po.sync.from-date=YYYY-MM-DD` in the active application configuration, then restart ERP. To include earlier verified exports alongside new POs, also set `tally.po.sync.include-previously-exported=true`.

The Sales Reports page displays automatic synchronization status. Manual date filters and Send to Tally remain available. Failed records remain visible and are retried; successful HTTP/import responses alone are not reported as verified success.

To pause automatic pushes, set `tally.po.sync.enabled=false` and restart ERP. Preserve the state file and the `backups` directory for audit and recovery. No ERP database records are changed by this synchronization.
