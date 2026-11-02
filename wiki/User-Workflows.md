# User workflows

The routes below are paths after the application's configured context path. They are backed by controller mappings in this checkout. The menu may hide pages when the user's role lacks permission.

| Area | Typical task | Page route |
| --- | --- | --- |
| Parties | Create or edit a customer or supplier; browse parties | `/party`, `/partyList` |
| Item master | Maintain items | `/itemMaster` |
| Sales orders | Create and browse sales orders | `/new_salesOrder`, `/salesList` |
| Purchase orders | Create and browse purchase orders | `/purchaseOrder`, `/purchase` |
| Goods receipts | Record a GRN, browse GRNs, open an existing GRN | `/new_grn`, `/grnLists`, `/grn/{grnId}` |
| Delivery challans | Create, browse, or open a challan | `/deliveryChallan`, `/dcList`, `/dc/{dcId}` |
| Invoices | Create and browse invoices | `/invoice`, `/invoiceList` |
| Returnables | Create a returnable, browse returnable items or DCs | `/returnable/create`, `/returnableList`, `/returnableDcList` |
| Work orders | Create and browse work orders | `/work_order`, `/workOrderList` |
| TDS and non-billable | Review TDS lots and non-billable items | `/tds`, `/nonBillableList` |
| Reports | Open the report hub | `/sales_report` |
| Company assets | Browse assets and assignments | `/companyAssets` |
| Administration | Manage users and roles | `/user-management`, `/role-management` |

## Sales to delivery

1. Maintain the party and item records needed for the order.
2. Create a sales order. The sales list provides access to existing orders and their status.
3. Create a delivery challan for items leaving stock. The DC list shows existing challans; `/dc/{dcId}` opens a specific record.
4. Create an invoice from the invoice page and use the invoice list to find it later.
5. If items are to be returned, use the returnable pages. `/returnable/{dcId}` loads items from a selected delivery challan.

## Purchase to stock

1. Create a purchase order and inspect it from the purchase list.
2. Record a GRN against the relevant purchase order. The GRN page loads purchase items for the selected PO.
3. Open an existing receipt using `/grn/{grnId}`. The legacy `/api/grn/view` route redirects to that page.
4. Review stock and the available reports from the application menu. Stock quantities and reports depend on recorded transactions.

## Roles and permissions

Administrators manage users and roles on the management pages. Permissions are module specific, with authorities such as `PURCHASE_VIEW`, `PURCHASE_EDIT`, `GRN_VIEW`, and `GRN_EDIT`. A user may be able to view a list but not edit its records. Existing GRNs at `/grn/{grnId}` require `GRN_EDIT`. Dashboard tiles and report cards can also be selected for each role. Disabled users lose access during an active session. See [Reports and permissions](Reports-and-Permissions.md) for the report rules.

For exact access behavior, refer to `src/main/java/com/ncpl/sales/security/SecurityConfig.java` and the relevant controller method annotations. Report an unexpected access-denied response to an administrator with the page route and action attempted; do not share passwords.
