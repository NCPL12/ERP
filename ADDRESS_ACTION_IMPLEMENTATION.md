# Address-Specific Audit Action Implementation

## What You Requested
You wanted the audit action to show **"ADDRESS_UPDATED"** specifically when address changes occur, instead of the generic "UPDATE".

## Solution Implemented

### 1. Added New Action Constant
```java
public static final String ACTION_ADDRESS_UPDATED = "ADDRESS_UPDATED";
```

### 2. Enhanced Update Detection Logic
```java
public void logSalesOrderUpdate(SalesOrder oldSalesOrder, SalesOrder newSalesOrder, String performedBy, HttpServletRequest request) {
    
    // Check if this is specifically an address update
    if (hasAddressChanges(oldSalesOrder, newSalesOrder)) {
        logAudit(newSalesOrder.getId(), ACTION_ADDRESS_UPDATED, performedBy, oldSalesOrder, newSalesOrder,
                "Address updated: " + newSalesOrder.getClientPoNumber(), request);
    } else {
        logAudit(newSalesOrder.getId(), ACTION_UPDATE, performedBy, oldSalesOrder, newSalesOrder,
                "Sales Order updated: " + newSalesOrder.getClientPoNumber(), request);
    }
}
```

### 3. Address Change Detection Method
```java
private boolean hasAddressChanges(SalesOrder oldOrder, SalesOrder newOrder) {
    if (oldOrder == null || newOrder == null) {
        return false;
    }
    
    // Check shipping address changes
    String oldShipping = oldOrder.getShippingAddress();
    String newShipping = newOrder.getShippingAddress();
    boolean shippingChanged = !java.util.Objects.equals(oldShipping, newShipping);
    
    // Check billing address changes
    String oldBilling = oldOrder.getBillingAddress();
    String newBilling = newOrder.getBillingAddress();
    boolean billingChanged = !java.util.Objects.equals(oldBilling, newBilling);
    
    return shippingChanged || billingChanged;
}
```

## How It Works Now

### Address Updates
When you update shipping/billing address:
```sql
-- Audit Log Entry:
207	ADDRESS_UPDATED	2026-04-04 13:15:30.000000	Address updated: PO-123	{"shippingAddress":"Old Address"}	{"shippingAddress":"New Address"}	admin
```

### Other Updates
When you update other fields (not addresses):
```sql
-- Audit Log Entry:
208	UPDATE	2026-04-04 13:16:45.000000	Sales Order updated: PO-123	{"gst":18.0}	{"gst":18.5}	admin
```

### New Sales Orders
When creating new sales orders:
```sql
-- Audit Log Entry:
209	CREATE_SALES_ITEM	2026-04-04 13:17:20.000000	Created 2 Sales Items...	{"itemsCount":2}	null	admin
```

## Expected Audit Actions

| Change Type | Action | Description |
|-------------|--------|-------------|
| **Address Update** | `ADDRESS_UPDATED` | Address updated: PO-123 |
| **Other Field Update** | `UPDATE` | Sales Order updated: PO-123 |
| **New Sales Order** | `CREATE_SALES_ITEM` | Created X Sales Items... |
| **Archive** | `ARCHIVE` | Sales Order archived: SO-123 |
| **Unarchive** | `UNARCHIVE` | Sales Order unarchived: SO-123 |

## Test the Fix

### 1. Deploy the Code
```bash
mvn clean package
# Deploy the updated WAR file
```

### 2. Test Address Update
1. Open a sales order
2. Change shipping address or billing address
3. Save the form

### 3. Verify Results
Run `verify-address-action-fix.sql`:

```sql
-- Should show:
-- ✅ Action: ADDRESS_UPDATED
-- ✅ Description: Address updated: PO-123
-- ✅ old_values: JSON with old address
-- ✅ new_values: JSON with new address
```

## Files Modified
- `src/main/java/com/ncpl/sales/service/SalesOrderAuditService.java`
  - Added `ACTION_ADDRESS_UPDATED` constant
  - Enhanced `logSalesOrderUpdate()` method
  - Added `hasAddressChanges()` helper method

## Compilation Status
✅ **SUCCESS**: All changes compile without errors

## Benefits
1. **Specific Action Tracking**: Address updates clearly identified
2. **Better Filtering**: Can easily filter address-specific changes
3. **Clear Audit Trail**: Distinction between address and other updates
4. **Maintains Compatibility**: Other audit actions remain unchanged

Now when you update addresses, the audit log will show **"ADDRESS_UPDATED"** as requested! 🎯
