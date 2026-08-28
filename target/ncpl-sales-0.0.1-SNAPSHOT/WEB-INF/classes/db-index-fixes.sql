-- =============================================================
-- Performance Fix: Indexes for DC Report By Date
-- Run these against your production database
-- =============================================================

-- Indexes for Purchase Dashboard (13s → ms)
CREATE INDEX IF NOT EXISTS idx_po_archive ON tbl_purchase_order (archive, created);
CREATE INDEX IF NOT EXISTS idx_pi_po_number ON tbl_purchase_items (po_number);
CREATE INDEX IF NOT EXISTS idx_pi_model_no ON tbl_purchase_items (model_no);
CREATE INDEX IF NOT EXISTS idx_im_gst ON tbl_item_master (id, gst);

-- Index for date-range queries on tbl_dc_items (DC Report By Date filters on created)
CREATE INDEX IF NOT EXISTS idx_dc_items_created ON tbl_dc_items (created);

-- Index for description lookups (SalesItemId joins in N+1 queries)
CREATE INDEX IF NOT EXISTS idx_dc_items_description ON tbl_dc_items (description);

-- Composite index for the most common query pattern: date range + description
CREATE INDEX IF NOT EXISTS idx_dc_items_created_desc ON tbl_dc_items (created, description);

-- Index for so_number lookups on tbl_dc (used in getDcListBySoId)
CREATE INDEX IF NOT EXISTS idx_dc_so_number ON tbl_dc (so_number);

-- =============================================================
-- Fix: action_performed timezone issue in audit table
-- MySQL TIMESTAMP converts to/from UTC, causing wrong time.
-- DATETIME stores values as-is, preserving the correct time.
-- =============================================================
ALTER TABLE tbl_sales_order_audit MODIFY action_performed DATETIME NOT NULL;
