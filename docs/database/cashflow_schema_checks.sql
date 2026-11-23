-- Read-only schema inspection. All five tables must exist and match V001/entities.
SELECT DATABASE() AS selected_erp_database, VERSION() AS mysql_version;
SELECT table_name, column_name, column_type, is_nullable, column_default, extra
FROM information_schema.columns
WHERE table_schema=DATABASE() AND table_name IN
('cashflow_invoices','cashflow_recurring_transactions','cashflow_tally_sync_status',
 'cashflow_finance_plan_entries','cashflow_finance_plan_settings')
ORDER BY table_name, ordinal_position;
SELECT table_name, index_name, non_unique, seq_in_index, column_name
FROM information_schema.statistics
WHERE table_schema=DATABASE() AND table_name LIKE 'cashflow_%'
ORDER BY table_name,index_name,seq_in_index;