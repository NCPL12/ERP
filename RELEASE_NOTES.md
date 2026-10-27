# Neptune ERP release notes — Unreleased draft

**Status:** Draft; no version tag or GitHub release has been created.
**Target:** `main`, after the selected `development` changes are merged.
**Comparison:** `main` `5858317` to committed `development` `f94d723` (27 October 2026).

## Changes in committed `development`

### Added

- Returnable creation and detail pages, plus a separate Returnable DC list.
- Purchase-order export and automatic synchronization with Tally, with connection and company validation. See the [PO-to-Tally guide](docs/README-PO-TALLY.md).
- Maven CI and Java code-quality workflows.

### Improved

- Dashboard counts and lists use cached snapshots and background refresh to reduce page loading time.
- GRN editing loads purchase items from the selected purchase order instead of every purchase order. The committed `/grn/{grnId}` route still redirects to `/new_grn`.
- Delivery challan loading and dashboard queries have been updated.
- Monthly stock movement and other report queries use batched lookups.
- Permissions and navigation cover the additional Returnable and administration pages.
- Purchase descriptions, timestamps, billing addresses, TDS processing, and single-click navigation received fixes.

## Changes prepared locally, pending commit and merge

These items are in the current working tree and are **not yet part of `development` or `main`**:

- Delivery challans open directly by ID; the DC dashboard links to the selected challan's returnables.
- Existing GRNs render directly at `/grn/{grnId}`, while older `/api/grn/view` links continue to work.
- A JavaScript syntax workflow checks application scripts.
- The shared page header removes the retired service worker registration from browsers that still have it.
- HTTP 5xx failures trigger asynchronous email alerts with a five-minute repeat limit. The `dev` and `prod` recipient settings are documented in [error alerts](docs/error-alerts.md). Live email delivery has not been tested.

Edit this draft to include only changes present in the final tagged commit before copying it into a GitHub release.
