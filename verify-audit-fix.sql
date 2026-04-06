-- Test script to verify audit logging for address updates
-- Run this after updating a sales order with address changes

-- 1. Check if audit entries are being created
SELECT 
    COUNT(*) as total_updates,
    MAX(action_performed) as last_update_time
FROM missindump.tbl_sales_order_audit 
WHERE action = 'UPDATE';

-- 2. Check recent updates with address information
SELECT 
    sales_order_id,
    performed_by,
    action_performed,
    description,
    CASE 
        WHEN old_values LIKE '%shippingAddress%' THEN 'YES'
        ELSE 'NO'
    END as has_old_shipping,
    CASE 
        WHEN new_values LIKE '%shippingAddress%' THEN 'YES'
        ELSE 'NO'
    END as has_new_shipping,
    CASE 
        WHEN old_values LIKE '%billingAddress%' THEN 'YES'
        ELSE 'NO'
    END as has_old_billing,
    CASE 
        WHEN new_values LIKE '%billingAddress%' THEN 'YES'
        ELSE 'NO'
    END as has_new_billing,
    LEFT(old_values, 200) as old_values_preview,
    LEFT(new_values, 200) as new_values_preview
FROM missindump.tbl_sales_order_audit 
WHERE action = 'UPDATE' 
    AND action_performed >= DATE_SUB(NOW(), INTERVAL 1 HOUR)
ORDER BY action_performed DESC;

-- 3. Detailed view of a specific sales order audit trail
SELECT 
    id,
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    old_values,
    new_values
FROM missindump.tbl_sales_order_audit 
WHERE sales_order_id = 'SO-BLR-ELT-46301-2026'
ORDER BY action_performed DESC;

-- 4. Check for any errors in console output
-- (This would be in application logs, not database)
