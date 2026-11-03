package com.ncpl.sales.model;

import java.util.LinkedHashSet;
import java.util.Set;

public enum ArchiveItem {
	SALES_ORDER("Sales Order"),
	PURCHASE_ORDER("Purchase Order"),
	DELIVERY_CHALLAN("Delivery Challan"),
	GRN("GRN");

	private final String label;

	ArchiveItem(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	/** Comma separated string saved on the role -> set of valid keys. */
	public static Set<String> visible(String csv) {
		Set<String> result = new LinkedHashSet<>();
		if (csv == null || csv.trim().isEmpty()) {
			return result;
		}
		for (String s : csv.split(",")) {
			String t = s.trim();
			if (t.isEmpty()) {
				continue;
			}
			try {
				result.add(ArchiveItem.valueOf(t).name());
			} catch (IllegalArgumentException ignore) {
				// unknown key - skip
			}
		}
		return result;
	}
}