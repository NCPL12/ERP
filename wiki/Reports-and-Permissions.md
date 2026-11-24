# Reports and permissions

The report hub is `/sales_report` (after the application's context path). A role needs `REPORTS_VIEW` to open it. An administrator can choose which report cards appear for that role on `/role-management`. The application also checks a separate `REPORT_<key>` authority when a user opens each report URL.

## Configure a role

1. Open **Role Management** with an account allowed to edit roles.
2. Give the role **Reports: View** access.
3. Select the reports the role should see and save the role.
4. Have affected users sign out and back in so their Spring Security authorities are reloaded.
5. Check the report hub and open a selected report to confirm access.

The report selection for an existing role defaults to all reports until it is configured. Once the role has Reports access, at least one report must remain selected. Dashboard tiles have a similar per-role selection and minimum-one rule when Dashboard access is enabled.

## Report route reference

These paths are relative to the configured context path. Query parameters and report filters are supplied by the page.

| Report | Route | Required authority |
| --- | --- | --- |
| Stock history by date | `/stock_history/Download` | `REPORT_STOCK_HISTORY_BY_DATE` |
| Monthly report and stock summary | `/monthly_stock_report/download`, `/stock/summary_details/` | `REPORT_MONTHLY_REPORT` |
| Stock by region | `/stock_report_by_region/Download` | `REPORT_STOCK_REPORT_BY_REGION` |
| Daily report | `/stock_report_by_date/Download` | `REPORT_DAILY_REPORT` |
| Outstanding stock | `/stock/outstandingReport/` | `REPORT_OUTSTANDING_STOCK_REPORT` |
| Pending purchase orders | `/stock/pendingporeport/` | `REPORT_PENDING_PO_REPORT` |
| Pending delivery challans | `/stock/dcReport/` | `REPORT_PENDING_DC_REPORT` |
| Purchase item history | `/poItem/History/` | `REPORT_PURCHASE_ITEM_HISTORY` |
| Sales list by item | `/sales_list/by_item_id` | `REPORT_SALES_LIST_BY_ITEM` |
| Pending PO by PO number | `/stock/pendingporeport/byPoNumber` | `REPORT_PENDING_PO_REPORT_BY_PONUMBER` |
| DC list by item | `/dc_list/by_item` | `REPORT_DC_LIST_BY_ITEM` |
| PO list by date | `/po_list/by_date` | `REPORT_PO_LIST_BY_DATE` |
| Active sales by customer | `/active_sales/by_customer` | `REPORT_ACTIVE_SALES_ORDER_BY_CUSTOMER` |
| GRN by date | `/grn_itemwise/by_date` | `REPORT_GRN_REPORT_BY_DATE` |
| DC by date | `/dc_list/by_date` | `REPORT_DC_REPORT_BY_DATE` |
| GRN and PO by model | `/api/grn_po_by_model` | `REPORT_GRN_AND_PONUMBER_BY_MODELS` |
| Monthly stock movement | `/monthly_report/by_date` | `REPORT_MONTHLY_STOCK_MOVEMENT_REPORT` |

The report JSP also contains legacy role-name checks on some cards. If a selected card remains hidden, inspect its `security:authorize` expression in `src/main/webapp/WEB-INF/views/salesReport.jsp` as well as the role's Reports permission. The route rules are in `src/main/java/com/ncpl/sales/security/SecurityConfig.java`; report keys are in `src/main/java/com/ncpl/sales/model/ReportItem.java`.

## Other access rules

Role Management controls module-level view, edit, and delete flags. A permission can allow a list while denying its edit page: for example, `/grnLists` requires `GRN_VIEW`, while `/new_grn` and `/grn/{grnId}` require `GRN_EDIT`. `/purchase` requires `PURCHASE_VIEW`, and `/purchaseOrder` requires `PURCHASE_EDIT`. Disabled accounts are rejected even if their session was already active.
