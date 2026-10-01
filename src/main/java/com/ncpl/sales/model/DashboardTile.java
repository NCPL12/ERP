package com.ncpl.sales.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** The 7 stat tiles on /dashboard — which ones a role's users see is set on Role.dashboardTiles. */
public enum DashboardTile {
	SALES_ORDER("Client Sales Order"),
	PURCHASE_ORDER("Vendor Purchase Order"),
	INVOICE("Total Invoice"),
	PROJECTS("Projects"),
	TDS_APPROVED("Tds Approved Items"),
	SO_WITHOUT_DESIGN("SO Without Design"),
	SO_WITH_DESIGN("SO With Design");

	private final String label;

	DashboardTile(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	// null = never configured -> all 7 visible (default). Non-null (blank included) -> exactly what's listed.
	public static Set<String> visible(String dashboardTilesCsv) {
		if (dashboardTilesCsv == null) {
			Set<String> all = new HashSet<>();
			for (DashboardTile tile : values()) {
				all.add(tile.name());
			}
			return all;
		}
		return new HashSet<>(Arrays.asList(dashboardTilesCsv.split(",")));
	}
}
