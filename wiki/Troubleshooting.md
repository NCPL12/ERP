# Troubleshooting

Start with the selected Spring profile, the first relevant exception in the application log, and the exact page route or operation that failed. The routes below include `/ncpl-sales` only for the `h2` and `dev` profiles in this checkout; check the configured context path elsewhere.

| Symptom | Check |
| --- | --- |
| The app does not start | Confirm JDK 8, the selected profile, the database connection, and whether the configured port is free. `dev` and `h2` use port `8880`; `qa` and `prod` use `8080` in their profile files. |
| Login succeeds but a page shows access denied | Check the user's enabled state, assigned role, module permission, and any report-specific authority. Sign out and back in after changing a role. See [Reports and permissions](Reports-and-Permissions.md). |
| The H2 app opens but lists are empty | The `h2` profile uses an empty, in-memory database with `create-drop`. It does not provide demo transactions or a preconfigured login account. |
| A GRN link opens the list instead | `/grn/{grnId}` redirects to `/grnLists` if the ID does not exist. Opening an existing GRN also requires `GRN_EDIT`. |
| A DC link opens the list instead | `/dc/{dcId}` redirects to `/dcList` if the DC is missing or its sales order/party cannot be loaded. |
| A report card is missing or its URL is denied | Check the role's Reports View permission, selected report, any legacy role-name check in the JSP, and the route's `REPORT_*` authority. |
| A PO-to-Tally action fails | Confirm `cashflow.tally.server.url`, the exact destination `cashflow.tally.company-name`, Tally availability, and `PURCHASE_EDIT` for export/import actions. See [Configuration and operations](Configuration-and-Operations.md). |

For a reproducible failure, record the selected profile, route, action, time, and relevant log excerpt. Remove credentials and customer data before sharing it. For code-level tracing, see [Architecture](Architecture.md) and [Development](Development.md).
