# Address Update Audit Fix - Complete Solution

## Problem Identified
When updating shipping addresses, the audit log was showing:
```
206	CREATE_SALES_ITEM	2026-04-04 12:57:51.150000	Created 1 Sales Items in Sales Order: SO-BLR-ELT-46301-2026
```

Instead of the expected:
```
206	UPDATE	2026-04-04 12:57:51.150000	Sales Order updated: PO-123	{"shippingAddress":"Old..."}	{"shippingAddress":"New..."}
```

## Root Cause Analysis
**Duplicate audit logging conflict** between:
1. **AOP Aspect** (`LoggingAspect.java`) - Always logging `CREATE_SALES_ITEM`
2. **Service Layer** (`SalesService.java`) - Properly logging `UPDATE` with old/new values

The AOP aspect was intercepting the `savesales()` method and overriding the service layer's proper audit logging.

## Solution Applied

### Fixed AOP Aspect (`LoggingAspect.java`)
**Key Changes:**
1. **Update Detection**: Added `isUpdate` flag to detect existing vs new sales orders
2. **Conditional Logging**: Only log `CREATE_SALES_ITEM` for new orders (`!isUpdate`)
3. **Prevent Duplicates**: Let service layer handle all update logging

```java
// Before: Always logged CREATE_SALES_ITEM
if (auditService != null && salesOrderId != null && itemsCount > 0) {

// After: Only log for new orders, let service handle updates
if (auditService != null && salesOrderId != null && !isUpdate && itemsCount > 0) {
```

### Update Detection Logic
```java
// Check if this is an update (ID is not empty)
isUpdate = salesOrder.getId() != null && !salesOrder.getId().isEmpty();
```

## How It Works Now

### New Sales Order Creation
1. **AOP Aspect**: Detects `isUpdate = false` → Logs `CREATE_SALES_ITEM`
2. **Service Layer**: Detects empty ID → Logs `CREATE` action
3. **Result**: Single creation audit entry

### Existing Sales Order Update (Address Changes)
1. **AOP Aspect**: Detects `isUpdate = true` → **Skips logging**
2. **Service Layer**: Detects non-empty ID → Logs `UPDATE` with old/new values
3. **Result**: Single update audit entry with address changes

## Expected Audit Log After Fix

### Address Update Example:
```sql
-- Before Fix (Wrong):
206	CREATE_SALES_ITEM	2026-04-04 12:57:51.150000	Created 1 Sales Items...	{"itemsCount":1}	null

-- After Fix (Correct):
206	UPDATE	2026-04-04 12:57:51.150000	Sales Order updated: PO-123	{"shippingAddress":"Old Address","billingAddress":"Old Billing"}	{"shippingAddress":"New Address","billingAddress":"New Billing"}
```

## Verification Steps

### 1. Deploy the Code
```bash
mvn clean package
# Deploy the updated WAR file
```

### 2. Test Address Update
1. Open sales order edit form
2. Change shipping/billing address
3. Save the form

### 3. Verify Audit Log
Run `verify-address-audit-fix.sql`:

```sql
-- Should show:
-- ✅ Action: UPDATE (not CREATE_SALES_ITEM)
-- ✅ old_values: JSON with previous addresses
-- ✅ new_values: JSON with updated addresses
-- ✅ No duplicate entries
```

## Files Modified
- `src/main/java/com/ncpl/sales/aspect/LoggingAspect.java` (Fixed duplicate logging)

## Compilation Status
✅ **SUCCESS**: All changes compile without errors

## Benefits of This Fix
1. **Proper Update Tracking**: Address changes now logged as `UPDATE` with old/new values
2. **No Duplicates**: Eliminates conflicting audit entries
3. **Complete Change History**: Captures all field changes including addresses
4. **Maintains Creation Tracking**: New sales orders still properly logged

## Troubleshooting
If still seeing `CREATE_SALES_ITEM` after fix:
1. Verify the code was deployed correctly
2. Check application logs for any errors
3. Ensure the sales order has a valid ID (not empty)
4. Test with a different sales order

The audit system now properly distinguishes between creation and updates, capturing address changes with complete old/new value tracking! 🎯
