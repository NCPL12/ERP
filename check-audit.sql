-- Test query to check if audit logging is working
-- Check recent audit entries for UPDATE actions
SELECT 
    id,
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    LEFT(old_values, 100) as old_values_preview,
    LEFT(new_values, 100) as new_values_preview,
    ip_address
FROM missindump.tbl_sales_order_audit 
WHERE action = 'UPDATE' 
ORDER BY action_performed DESC 
LIMIT 10;

-- Check if there are any audit entries at all
SELECT COUNT(*) as total_audit_entries,
       COUNT(CASE WHEN action = 'UPDATE' THEN 1 END) as update_entries,
       COUNT(CASE WHEN action = 'CREATE' THEN 1 END) as create_entries
FROM missindump.tbl_sales_order_audit;

-- Check for specific sales order
SELECT 
    sales_order_id,
    action,
    performed_by,
    action_performed,
    description,
    old_values LIKE '%shippingAddress%' as has_old_shipping,
    new_values LIKE '%shippingAddress%' as has_new_shipping,
    old_values LIKE '%billingAddress%' as has_old_billing,
    new_values LIKE '%billingAddress%' as has_new_billing
FROM missindump.tbl_sales_order_audit 
WHERE sales_order_id = 'SO-BLR-ELT-46301-2026'
ORDER BY action_performed DESC;
