package com.ncpl.sales.model;

import java.util.HashSet;
import java.util.Set;

public enum ReportItem {
	STOCK_HISTORY_BY_DATE("Stock History by Date"),
	MONTHLY_REPORT("Monthly Report"),
	STOCK_REPORT_BY_REGION("Stock Report by Region"),
	DAILY_REPORT("Daily Report"),
	OUTSTANDING_STOCK_REPORT("Outstanding Stock Report"),
	PENDING_PO_REPORT("Pending PO Report"),
	PENDING_DC_REPORT("Pending DC Report"),
	PURCHASE_ITEM_HISTORY("Purchase Item History"),
	SALES_LIST_BY_ITEM("Sales List By Item"),
	PENDING_PO_REPORT_BY_PONUMBER("Pending PO Report By PoNumber"),
	DC_LIST_BY_ITEM("DC List By Item"),
	PO_LIST_BY_DATE("PO List By Date"),
	ACTIVE_SALES_ORDER_BY_CUSTOMER("Active Sales Order By Customer"),
	GRN_REPORT_BY_DATE("GRN Report By Date"),
	DC_REPORT_BY_DATE("DC Report By Date"),
	GRN_AND_PONUMBER_BY_MODELS("GRN and PONumber by Models"),
	MONTHLY_STOCK_MOVEMENT_REPORT("Monthly Stock Movement Report");

	private final String label;

	ReportItem(String label) { this.label = label; }

	public String label() { return label; }

	/** null = never configured -> all reports visible, so existing roles keep working. */
	public static Set<String> visible(String csv) {
		Set<String> out = new HashSet<>();
		if (csv == null) {
			for (ReportItem r : values()) out.add(r.name());
			return out;
		}
		for (String s : csv.split(",")) {
			if (!s.trim().isEmpty()) out.add(s.trim());
		}
		return out;
	}
}