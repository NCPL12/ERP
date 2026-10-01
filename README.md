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

## @D0045 — Role Based Access Control

**Issue:** Access was only ever checked by a handful of hardcoded role names
(`ADMIN`, `SALES`, `PURCHASE`, ...) spread across the security config. There
was no way to give one role view-only on a module and edit on another
without a code change and a redeploy, and disabling a user didn't kick them
out of a session they already had open.

**What changed:** Added a `Role` → `RolePermission` model (per role, per
module: view/edit/delete) and a Role Management page to edit that grid from
the UI. Login now builds each user's authorities from their role's
permissions instead of one fixed role name, and every request re-derives
those authorities from the DB (`UserStatusFilter`) so a permission change or
a disabled account takes effect immediately, not just on next login. Also
added a User Management page for creating/editing/deleting users and
resetting passwords without touching the database directly, and made which
dashboard tiles a role sees configurable (`Role.dashboardTiles`) instead of
everyone seeing all 7.

**Files changed:**
- `Role.java`, `RolePermission.java`, `RolePermissionDto.java`, `Module.java`, `DashboardTile.java` (new)
- `RoleRepo.java`, `RolePermissionRepo.java` (new)
- `RolePermissionSeeder.java` (new — seeds default grid for existing roles)
- `RoleController.java`, `UserManagementController.java`, `AccessDeniedController.java` (new)
- `UserStatusFilter.java` (new)
- `SecurityConfig.java`, `LoginSuccessHandler.java`, `UserService.java`, `UserRepo.java`, `UserController.java`
- `roleManagement.js`, `userManagement.js` (new)
- `roleManagement.jsp`, `userManagement.jsp`, `access-denied.jsp` (new)

**Where to view it:** `/role-management`, `/user-management`

---

## @D0043 — GRN routing fix

**Issue:** `/new_grn` was used for both creating a new GRN and opening an
existing one for editing (via a flash attribute), so the URL never actually
pointed at the GRN you were looking at.

**What changed:** Editing a GRN now goes to `/grn/{grnId}`, a proper
per-record URL, while `/new_grn` stays create-only.

**Files changed:**
- `PurchaseController.java`
- `SecurityConfig.java`

---

## @D0044 — Edit GRN loads faster

**Issue:** Opening a GRN for editing loaded every purchase item in the
database to repopulate the form, regardless of which PO the GRN was for.

**What changed:** Only the purchase items belonging to that GRN's PO are
fetched now.

**Files changed:**
- `PurchaseController.java`
- `PurchaseItemService.java`

---

## @D0032 — Asset tracking now tracks who has each asset

**Issue:** Company assets (laptops etc.) had no record of which employee
they were currently issued to, or history of past assignments/returns.

**What changed:** Added asset assignment tracking (issue to an employee,
record location/issued-by, mark returned) with per-asset history, plus an
asset details page and configurable asset types.

**Files changed:**
- `LaptopAssignment.java`, `AssetType.java` (new)
- `LaptopAssignmentRepo.java`, `AssetTypeRepo.java` (new)
- `AssetTypeService.java` (new)
- `CompanyAssets.java`, `CompanyAssetsRepo.java`, `CompanyAssetService.java`
- `PurchaseController.java`
- `companyAsset.js`, `companyAssets.jsp`, `companyAssetDetails.jsp` (new)

**Where to view it:** `/companyAssets`, `/companyAssets/view/{id}`

---

