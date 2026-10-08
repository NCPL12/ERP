# ERP — Change Log

This file lists what was changed recently, in plain words.

---

## @D0014 — Returnable Items page loads faster

**Issue:** This page was loading every single item from every order all
at once, so it got slower as more data piled up.

**What changed:** Implemented lazy loading to load only the items required for the current page and fetch additional results as the user scrolls or searches.

**Files changed:**
- `ReturnableItemsRepo.java`
- `ReturnableService.java`
- `SalesController.java`
- `returnableItemList.js`
- `returnableItemsList.jsp`

---

## @D0015 — Party List page loads faster

**Issue:** It loaded every party in the
system plus ran an extra database check for each one, every time you
opened the page.

**What changed:** Implemented lazy loading to load only the items required for the current page and fetch additional results as the user scrolls or searches.

**Files changed:**
- `PartyRepo.java`
- `PartyService.java`
- `SalesController.java`
- `partyList.js`
- `partyList.jsp`

---

## @D0010 — Item Master – Role Access

**Issue:** Some normal users are unable to add items because role
validation is hardcoded. If your role said you should have
access but your name wasn't on that list, you were still blocked.

**What changed:** Removed that hardcoded list. Access is now based only
on your role (ITEMMASTER or STORE) as set in the database — change the
role in the DB and access changes automatically, no code change needed.

**Files changed:**
- `itemMaster.js`

---

## @D0017 — Purchase Order audit log

**Issue:** Added an audit log for Purchase Orders to track who created, updated,
a PO and when the changes happened. It also tracks when PO line items are added,
updated, or removed.

**What changed:** Added a matching audit log for Purchase Orders
(`tbl_purchase_order_audit`), and whenever a line item is added, changed, or removed.


**Files changed:**
- `PurchaseOrderAudit.java` (new)
- `PurchaseOrderAuditRepo.java` (new)
- `PurchaseOrderAuditService.java` (new)
- `PurchaseOrderService.java`
- `PurchaseController.java`
- `purchase-order-audit.jsp` (new)

**Where to view it:** `/audit/purchase-order`

---

