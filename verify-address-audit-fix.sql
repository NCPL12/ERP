-- Test script to verify proper audit logging for address updates
-- After deploying the fix, update a sales order address and run this query

-- 1. Check recent audit entries - should now show proper UPDATE with old/new values
SELECT 
    id,
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    CASE 
        WHEN action = 'UPDATE' AND old_values IS NOT NULL AND new_values IS NOT NULL 
        THEN 'CORRECT - Has old/new values'
        WHEN action = 'CREATE_SALES_ITEM' 
        THEN 'INCORRECT - Still showing creation'
        ELSE 'OTHER'
    END as logging_status,
    LEFT(old_values, 150) as old_values_preview,
    LEFT(new_values, 150) as new_values_preview
FROM missindump.tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 30 MINUTE)
ORDER BY action_performed DESC;

-- 2. Specifically check for address updates
SELECT 
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    CASE 
        WHEN old_values LIKE '%shippingAddress%' AND new_values LIKE '%shippingAddress%'
        THEN '✅ Address change detected'
        WHEN old_values LIKE '%billingAddress%' AND new_values LIKE '%billingAddress%'
        THEN '✅ Billing address change detected'
        ELSE '❌ No address change found'
    END as address_update_status,
    JSON_EXTRACT(old_values, '$.shippingAddress') as old_shipping,
    JSON_EXTRACT(new_values, '$.shippingAddress') as new_shipping,
    JSON_EXTRACT(old_values, '$.billingAddress') as old_billing,
    JSON_EXTRACT(new_values, '$.billingAddress') as new_billing
FROM missindump.tbl_sales_order_audit 
WHERE action = 'UPDATE'
    AND action_performed >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY action_performed DESC;

-- 3. Verify no duplicate entries for the same operation
SELECT 
    sales_order_id,
    action,
    COUNT(*) as duplicate_count,
    MAX(action_performed) as latest_time
FROM missindump.tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY sales_order_id, action
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC;

-- Expected Results After Fix:
-- 1. Action should be 'UPDATE' (not 'CREATE_SALES_ITEM')
-- 2. old_values should contain previous address JSON
-- 3. new_values should contain updated address JSON
-- 4. No duplicate entries for the same operation
