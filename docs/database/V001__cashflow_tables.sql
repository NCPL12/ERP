-- V001: Cashflow MySQL schema. Select the existing ERP database before running.
-- Back up first; run on a test clone first. MySQL DDL auto-commits.
-- IF NOT EXISTS does not repair incompatible existing tables.
CREATE TABLE IF NOT EXISTS cashflow_invoices (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 customer_name VARCHAR(500) NOT NULL, invoice_number VARCHAR(200),
 invoice_date DATE, due_date DATE, expected_cash_date DATE,
 cashflow_remarks VARCHAR(1000), invoice_value DECIMAL(15,2) NOT NULL,
 invoice_type VARCHAR(20) NOT NULL, source VARCHAR(50),
 reconciled BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME, updated_at DATETIME,
 INDEX idx_customer_name (customer_name), INDEX idx_invoice_type (invoice_type),
 INDEX idx_invoice_date (invoice_date), INDEX idx_due_date (due_date),
 INDEX idx_invoice_number (invoice_number), INDEX idx_source (source)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS cashflow_recurring_transactions (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(255) NOT NULL, description VARCHAR(255) NOT NULL,
 type VARCHAR(255) NOT NULL, day_of_month INT NOT NULL,
 amount DECIMAL(19,2) NOT NULL, category VARCHAR(255) NOT NULL,
 is_active BIT(1) NOT NULL, next_due_date DATE NOT NULL,
 notes VARCHAR(255), created_date DATE NOT NULL, last_updated_date DATE
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS cashflow_tally_sync_status (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 last_sync_time DATETIME, last_sync_end_time DATETIME, invoices_synced INT,
 last_sync_status VARCHAR(20), last_error_message TEXT,
 created_at DATETIME, updated_at DATETIME
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS cashflow_finance_plan_entries (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 entry_type VARCHAR(40) NOT NULL, party_name VARCHAR(500) NOT NULL,
 description VARCHAR(1000), planned_date DATE NOT NULL,
 amount DECIMAL(15,2) NOT NULL, confidence_percent INT NOT NULL,
 notes VARCHAR(2000), active BIT(1) NOT NULL,
 created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
 INDEX idx_finance_plan_date (planned_date), INDEX idx_finance_plan_type (entry_type)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS cashflow_finance_plan_settings (
 id BIGINT NOT NULL PRIMARY KEY, opening_balance DECIMAL(15,2) NOT NULL,
 balance_as_of DATE, minimum_cash_reserve DECIMAL(15,2) NOT NULL,
 updated_at DATETIME
) ENGINE=InnoDB;
-- No seed data: application manages Finance settings; do not invent balances.