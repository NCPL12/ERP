# Address Update Audit Issue - Complete Solution

## Problem Analysis
From the debug logs, I identified the root cause:

### Issues Found:
1. **Address IDs instead of text**: The form was storing Party IDs (`PA-Man--00630/2019`) instead of formatted addresses
2. **Duplicate entries**: The save method was called twice, creating 2 audit entries
3. **No address change detection**: Since old/new addresses were identical (both IDs), no change was detected

### Debug Output Showed:
```
AUDIT DEBUG: Address comparison:
  Old Shipping: 'PA-Man--00630/2019'
  New Shipping: 'PA-Man--00630/2019'
  Shipping Changed: false
  Has Address Changes: false
AUDIT DEBUG: Logging as UPDATE
```

## Root Cause
The JavaScript dropdown handlers were storing **address IDs** in the hidden fields instead of **formatted address text**.

## Solution Implemented

### 1. Fixed JavaScript Address Handling
**File**: `src/main/resources/static/js/salesOrder.js`

#### Shipping Address Dropdown Fix:
```javascript
// Before: Only stored address ID
$("#shippingAddress").val(addressId);

// After: Stores formatted address text
var formattedAddress = shippingAddressObj.mainParty.partyName + " , " +
                      shippingAddressObj.mainParty.addr1 + " , " +
                      addr2 + " , " +
                      shippingAddressObj.mainParty.party_city.name + " , " +
                      shippingAddressObj.mainParty.party_city.state.name + " , " +
                      shippingAddressObj.mainParty.party_city.state.country.name + " - " +
                      pincode;
$("#shippingAddress").val(formattedAddress);
```

#### Billing Address Dropdown Fix:
Same fix applied to billing address dropdown handler.

### 2. Enhanced Audit Service
**File**: `src/main/java/com/ncpl/sales/service/SalesOrderAuditService.java`

- Added `ACTION_ADDRESS_UPDATED` constant
- Enhanced `logSalesOrderUpdate()` to detect address changes
- Added `hasAddressChanges()` method for address comparison

### 3. Fixed Duplicate Logging
**File**: `src/main/java/com/ncpl/sales/aspect/LoggingAspect.java`

- Modified AOP aspect to only log creations, not updates
- Prevents duplicate audit entries

## Expected Behavior After Fix

### When Address is Changed:
```sql
-- Audit Log Entry:
211	ADDRESS_UPDATED	2026-04-04 14:xx:xx.xxx	Address updated: PO-123	{"shippingAddress":"ABB Global Industries and Services Pvt Ltd , Old Address , Bangalore , Karnataka , India - 560001"}	{"shippingAddress":"ABB Global Industries and Services Pvt Ltd , New Address , Bangalore , Karnataka , India - 560002"}	admin
```

### When Other Fields are Changed:
```sql
-- Audit Log Entry:
212	UPDATE	2026-04-04 14:xx:xx.xxx	Sales Order updated: PO-123	{"gst":18.0}	{"gst":18.5}	admin
```

### No More Duplicates:
- Single audit entry per operation
- Proper action classification (ADDRESS_UPDATED vs UPDATE)

## Test the Fix

### 1. Deploy Updated Code
```bash
mvn clean package
# Deploy updated WAR file and JavaScript files
```

### 2. Test Address Update
1. Open a sales order
2. Change shipping address to a different address
3. Save the form
4. Check audit log

### 3. Verify Results
```sql
-- Should show:
SELECT action, COUNT(*) as count 
FROM tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 10 MINUTE)
GROUP BY action;

-- Expected:
-- ADDRESS_UPDATED: 1 (for address changes)
-- UPDATE: 0 (no duplicate entries)
```

## Files Modified

### Backend:
- `src/main/java/com/ncpl/sales/service/SalesOrderAuditService.java`
- `src/main/java/com/ncpl/sales/aspect/LoggingAspect.java`

### Frontend:
- `src/main/resources/static/js/salesOrder.js`

## Compilation Status
✅ **SUCCESS**: All changes compile without errors

## Benefits

1. **Proper Address Tracking**: Address changes now logged with full address text
2. **Specific Action Type**: `ADDRESS_UPDATED` clearly identifies address changes
3. **No Duplicates**: Single audit entry per operation
4. **Complete Audit Trail**: Old/new addresses captured in full detail
5. **Better Filtering**: Can easily filter address-specific changes

The audit system now properly tracks address updates with the specific `ADDRESS_UPDATED` action as requested! 🎯
