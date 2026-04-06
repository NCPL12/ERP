-- Test script to verify audit action detection fix
-- After deploying the enhanced code, test various update scenarios

-- 1. Check recent audit entries with proper action types
SELECT 
    id,
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    CASE 
        WHEN action = 'ADDRESS_UPDATED' AND description LIKE 'Address updated:%' THEN '✅ Correct - Address update with proper description'
        WHEN action = 'UPDATE' AND description LIKE 'Sales Order updated:%' THEN '✅ Correct - General update with proper description'
        WHEN action = 'ADDRESS_UPDATED' AND description LIKE 'Sales Order updated:%' THEN '❌ Wrong - Address update with wrong description'
        WHEN action = 'UPDATE' AND description LIKE 'Address updated:%' THEN '❌ Wrong - General update with wrong description'
        ELSE '❓ Other'
    END as validation_status,
    LEFT(old_values, 100) as old_values_preview,
    LEFT(new_values, 100) as new_values_preview
FROM missindump.tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 30 MINUTE)
ORDER BY action_performed DESC;

-- 2. Test specific scenarios
-- Scenario 1: Update shipping address (should show ADDRESS_UPDATED with "Address updated:" description)
-- Scenario 2: Update billing address (should show ADDRESS_UPDATED with "Address updated:" description)  
-- Scenario 3: Update GST only (should show UPDATE with "Sales Order updated:" description)
-- Scenario 4: Update total only (should show UPDATE with "Sales Order updated:" description)

-- 3. Verify no false positives
SELECT 
    action,
    COUNT(*) as count,
    MAX(description) as latest_description
FROM missindump.tbl_sales_order_audit 
WHERE action_performed >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
GROUP BY action
ORDER BY count DESC;

-- Expected Results:
-- ADDRESS_UPDATED entries: Description should start with "Address updated:"
-- UPDATE entries: Description should start with "Sales Order updated:"
-- No false positive address detection
