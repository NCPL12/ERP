# Debug Analysis for Address Update Issue

## Current Problem
You're seeing:
1. **2 duplicate UPDATE entries** instead of 1 ADDRESS_UPDATED entry
2. **Action showing as UPDATE** instead of ADDRESS_UPDATED
3. **Identical old/new values** suggesting no actual address change detected

## Likely Causes

### 1. Code Not Deployed
The updated code with address detection might not be deployed yet.

### 2. Address Values Actually Same
The addresses in old/new JSON appear identical:
```
"shippingAddress":"PA-Man--00630/2019"
```
This suggests either:
- No actual address change occurred
- The address change happened but wasn't captured properly

### 3. Multiple Save Operations
The 2 duplicate entries suggest multiple save operations are happening.

## Debug Steps Added

I've added debug logging to help diagnose:

### In `logSalesOrderUpdate()`:
```java
System.out.println("AUDIT DEBUG: logSalesOrderUpdate called");
System.out.println("  Has Address Change: " + hasAddressChange);
System.out.println("  Sales Order ID: " + newSalesOrder.getId());
```

### In `hasAddressChanges()`:
```java
System.out.println("AUDIT DEBUG: Address comparison:");
System.out.println("  Old Shipping: '" + oldShipping + "'");
System.out.println("  New Shipping: '" + newShipping + "'");
System.out.println("  Shipping Changed: " + shippingChanged);
```

## Next Steps to Debug

### 1. Deploy the Updated Code
```bash
mvn clean package
# Deploy the new WAR file with debug logging
```

### 2. Test with Actual Address Change
1. **Change the address to something completely different**
2. **Save the form**
3. **Check application logs** for the debug output
4. **Check audit log** for results

### 3. Check Application Logs
Look for these debug messages:
```
AUDIT DEBUG: logSalesOrderUpdate called
AUDIT DEBUG: Has Address Change: true/false
AUDIT DEBUG: Address comparison:
AUDIT DEBUG:   Old Shipping: '...'
AUDIT DEBUG:   New Shipping: '...'
```

### 4. Verify Expected Behavior
After proper deployment and address change, you should see:
```
AUDIT DEBUG: Has Address Change: true
AUDIT DEBUG: Logging as ADDRESS_UPDATED
```

And in audit log:
```sql
211	ADDRESS_UPDATED	2026-04-04 13:xx:xx.xxx	Address updated: PO-123	{"shippingAddress":"Old..."}	{"shippingAddress":"New..."}	admin
```

## Possible Issues to Check

### 1. Frontend Not Sending Updated Address
- Check if the form is actually sending the new address value
- Browser network tab can show what's being sent

### 2. Backend Not Receiving Updated Address
- The old/new sales orders might have the same address value
- Check controller binding

### 3. Multiple Service Calls
- Something might be calling the save method twice
- Check for duplicate form submissions or AJAX calls

## Test Script to Run

After deploying debug code:
```sql
-- Check latest audit entries
SELECT action, COUNT(*) as count 
FROM tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 10 MINUTE)
GROUP BY action;

-- Check for address changes
SELECT 
    action,
    JSON_EXTRACT(old_values, '$.shippingAddress') as old_shipping,
    JSON_EXTRACT(new_values, '$.shippingAddress') as new_shipping
FROM tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 10 MINUTE)
ORDER BY action_performed DESC;
```

The debug logging will help us understand exactly what's happening with the address detection!
