-- Run once before deploying if automatic Hibernate schema updates are disabled.
ALTER TABLE tbl_purchase_items ADD COLUMN gst_rate DECIMAL(5,2) NULL;

-- New PO lines save the GST rate used. 0.00 is a confirmed zero rate.
-- Leave legacy rows NULL: today's Item Master is not historical evidence.
-- Legacy reads retain the existing master fallback until rates are verified.
