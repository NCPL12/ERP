-- V002: Add known newer nullable columns to older Cashflow tables.
-- Run V001 first. Select the ERP database; back up and test first.
-- Dynamic SQL uses information_schema for repeatable MySQL-compatible checks.
SET @cashflow_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='cashflow_invoices' AND column_name='expected_cash_date')=0, 'ALTER TABLE cashflow_invoices ADD COLUMN expected_cash_date DATE NULL', 'SELECT ''cashflow_invoices.expected_cash_date already exists'' AS migration_status');
PREPARE cashflow_stmt FROM @cashflow_ddl;
EXECUTE cashflow_stmt;
DEALLOCATE PREPARE cashflow_stmt;

SET @cashflow_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='cashflow_invoices' AND column_name='cashflow_remarks')=0, 'ALTER TABLE cashflow_invoices ADD COLUMN cashflow_remarks VARCHAR(1000) NULL', 'SELECT ''cashflow_invoices.cashflow_remarks already exists'' AS migration_status');
PREPARE cashflow_stmt FROM @cashflow_ddl;
EXECUTE cashflow_stmt;
DEALLOCATE PREPARE cashflow_stmt;

SET @cashflow_ddl = IF((SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='cashflow_tally_sync_status' AND column_name='last_sync_end_time')=0, 'ALTER TABLE cashflow_tally_sync_status ADD COLUMN last_sync_end_time DATETIME NULL', 'SELECT ''cashflow_tally_sync_status.last_sync_end_time already exists'' AS migration_status');
PREPARE cashflow_stmt FROM @cashflow_ddl;
EXECUTE cashflow_stmt;
DEALLOCATE PREPARE cashflow_stmt;
