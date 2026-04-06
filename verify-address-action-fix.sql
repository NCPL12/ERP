-- Test script to verify ADDRESS_UPDATED action for address changes
-- After deploying the fix, update a sales order address and run this query

-- 1. Check for ADDRESS_UPDATED action specifically
SELECT 
    id,
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    CASE 
        WHEN action = 'ADDRESS_UPDATED' THEN '✅ Correct - Address update detected'
        WHEN action = 'UPDATE' THEN 'ℹ️ General update (no address change)'
        WHEN action = 'CREATE_SALES_ITEM' THEN '❌ Wrong - Still showing creation'
        ELSE '❓ Other action'
    END as action_status,
    LEFT(old_values, 200) as old_values_preview,
    LEFT(new_values, 200) as new_values_preview
FROM missindump.tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 30 MINUTE)
ORDER BY action_performed DESC;

-- 2. Verify address changes are properly detected
SELECT 
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    CASE 
        WHEN action = 'ADDRESS_UPDATED' THEN
            CASE 
                WHEN old_values LIKE '%shippingAddress%' AND new_values LIKE '%shippingAddress%' THEN '✅ Shipping address changed'
                WHEN old_values LIKE '%billingAddress%' AND new_values LIKE '%billingAddress%' THEN '✅ Billing address changed'
                ELSE '✅ Address change detected'
            END
        ELSE '❌ Not address update'
    END as address_change_status,
    JSON_EXTRACT(old_values, '$.shippingAddress') as old_shipping,
    JSON_EXTRACT(new_values, '$.shippingAddress') as new_shipping,
    JSON_EXTRACT(old_values, '$.billingAddress') as old_billing,
    JSON_EXTRACT(new_values, '$.billingAddress') as new_billing
FROM missindump.tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY action_performed DESC;

-- 3. Count different action types in recent updates
SELECT 
    action,
    COUNT(*) as count,
    MAX(action_performed) as latest_occurrence
FROM missindump.tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY action
ORDER BY count DESC;

-- Expected Results After Fix:
-- 1. Address updates should show action = 'ADDRESS_UPDATED'
-- 2. Other updates should show action = 'UPDATE'
-- 3. No more 'CREATE_SALES_ITEM' for updates
-- 4. old_values and new_values should contain address JSON
