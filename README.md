## @D0050: Item Master loading issue

**Issue:** The Item Master screen was slow to open. The page load fetched all
items, stock records and suppliers and embedded them in the page as JSON, and
`findItemDetails()` ran about 14,000 queries.

**Root cause:**
- `GET /itemMaster` loaded and serialized the full item, stock and supplier
  lists on every page load.
- `findItemDetails()` ran 2 queries per item (stock list and supplier list).

**What changed:**
- `GET /itemMaster` now passes `itemList`, `allStocksList` and
  `allSupplierslist` as empty arrays.
- `itemMaster.jsp` no longer embeds those lists. `itemList`, `allSupplierslist`
  and `allStocksList` start as `[]`, and a new `window.itemMasterLazyConfig`
  (`enabled`, `toolTrackerOnly`, `api.list`) points the table at
  `GET /api/itemMaster/page`.
- Added `ItemMasterService.getItemMasterPage(...)`. It returns one page of items
  and the total count. Stock, supplier, customer and cost summaries are loaded
  only for the item ids on that page.
- Optimized `findItemDetails()` from about 14,000 queries to 3, joined in memory.
- New repo queries: `StockRepo.findStocksByItemIds`,
  `StockRepo.getStockTotalsGroupedByItemId`, `SupplierRepo.findByItemMasterIds`,
  `SupplierRepo.findPreferredCostByItem`, `ItemMasterRepo.findItemIdAndName`.

**Files changed:**
- `ItemMasterService.java`
- `itemMasterController.java`
- `ItemMasterRepo.java`
- `StockRepo.java`
- `SupplierRepo.java`
- `itemMaster.jsp`
- `PurchaseController.java`

**Where to verify:** `/itemMaster`

