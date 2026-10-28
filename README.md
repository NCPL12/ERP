# Neptune ERP

[ERP wiki](wiki/Home.md) · [Getting started](wiki/Getting-Started.md) · [User workflows](wiki/User-Workflows.md) · [Architecture](wiki/Architecture.md)

[Draft release notes](RELEASE_NOTES.md)

This README lists the features present on `main`, the committed differences on `development`, and work currently saved only in this local checkout. It compares `main` at `5858317` with `development` at `f94d723` (27 October 2026). Update the comparison when either branch moves.

## Features on `main`

- **Core ERP workflows:** sales orders, purchase orders, delivery challans, goods receipt notes (GRNs), invoices, returnables, stock, reports, TDS, and company assets.
- **Faster lists:** paginated Returnable Items and Party List pages load records as needed rather than loading the whole list at once.
- **Access control:** roles have module-level view, edit, and delete permissions. Role and User Management pages support administration, disabled users lose access during an active session, and dashboard tile visibility follows the role.
- **Purchase order audit tracking:** purchase order and line-item changes are recorded by the audit service. An audit JSP exists, but the advertised `/audit/purchase-order` page route is not mapped in this snapshot.
- **Asset assignments:** company assets can record the employee holding an asset, returns, assignment history, and configurable asset types.
- **Returnables by delivery challan:** `/returnable/{dcId}` loads the selected challan's items.

The previous `main` README also described direct GRN editing at `/grn/{grnId}` and purchase-order-only item loading. Those changes are **not in the `main` source at this snapshot**: `/api/grn/view` redirects to `/new_grn`, and the edit page calls `getAllPurchaseItems()`. Its purchase-order audit page URL is likewise not wired to a controller. The table below describes where the GRN implementations stand.

## Committed `development` changes compared with `main`

| Area | Difference in `development` |
| --- | --- |
| Returnables | Adds creation and detail pages, a separate Returnable DC list, and expanded returnable permissions. |
| Dashboard | Adds cached counts and lists, background refresh, and faster dashboard data loading. |
| GRN and reports | Limits GRN edit-page purchase items to the selected PO, adds a `/grn/{grnId}` route that still redirects to `/new_grn`, and batches monthly stock movement and other report lookups. |
| Delivery challans | Updates DC item loading and dashboard behavior, including query and display improvements. |
| Purchase orders and Tally | Adds PO-to-Tally export, automatic sync support, validation, configurable connection settings, and related tests. See [PO-to-Tally deployment](docs/README-PO-TALLY.md). |
| Access and navigation | Extends permission checks and menu behavior for the new pages and workflows. |
| Other fixes | Includes purchase descriptions, timestamps, billing addresses, TDS processing, and single-click navigation fixes. |
| Build checks | Adds Maven CI and Java code-quality workflows. Generated `target/` output is removed from version control, which accounts for most of the raw file diff. |

`main` and `development` have diverged. At these snapshots, the commits unique to `main` do not introduce additional source-file changes; the feature differences above are on `development`. For implementation details, see [development feature consolidation](docs/feature-consolidation.md).

## Local changes not yet committed to either branch

The current working tree also contains these changes. They are **not** part of the committed `development` snapshot above:

- DC pages open directly by DC ID, and the DC dashboard links to the selected challan's returnables.
- Existing GRNs render directly at `/grn/{grnId}`; old `/api/grn/view` links still redirect there. The page continues to load only the selected PO's items.
- A JavaScript syntax workflow checks application scripts.
- The shared page header unregisters the retired `/sw.js` service worker from browsers that still have it.
- HTTP 5xx responses trigger rate-limited, asynchronous email alerts in the `dev` and `prod` profiles. See [error alerts](docs/error-alerts.md).
- `application-dev.properties` contains local environment edits; review them separately before deployment.

## Recheck the comparison

Run `git diff --name-status main development -- src/main .github docs` to inspect committed source and documentation changes. Run `git status --short` to see local work that the branch comparison does not include.
