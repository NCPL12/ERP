package com.ncpl.sales.model;

public enum Module {
	// A null path means the module has no sidebar entry and is omitted from Roles & Permissions.
	DASHBOARD("/dashboard"), PARTY("/partyList"), ITEM_MASTER("/itemMaster"),
	COMPANY_ASSETS(null), SALES_ORDER("/salesList"), DELIVERY_CHALLAN("/dcList"),
	RETURNABLE("/returnableList"), INVOICE("/invoiceList"), WORK_ORDER(null),
	PURCHASE("/purchase"), GRN("/grnLists"), NON_BILLABLE(null),
	REPORTS("/sales_report"), ARCHIVES("/salesList_archived"),
	USER_MANAGEMENT("/user-management");

	private final String menuPath;

	Module(String menuPath) {
		this.menuPath = menuPath;
	}

	public String menuPath() {
		return menuPath;
	}

	public boolean inMenu() {
		return menuPath != null;
	}

	public static java.util.List<Module> menuModules() {
		return java.util.Arrays.stream(values()).filter(Module::inMenu)
				.collect(java.util.stream.Collectors.toList());
	}

	public String label() {
		return name().replace('_', ' ');
	}

	/** Whether a Delete checkbox is meaningful for this module in the permission grid. */
	public boolean deletable() {
		return this != DASHBOARD && this != REPORTS && this != ARCHIVES && this != COMPANY_ASSETS && this != DELIVERY_CHALLAN && this != GRN;
	}

	/** Whether an Edit checkbox is meaningful for this module (DASHBOARD/REPORTS/ARCHIVES are view-only). */
	public boolean editable() {
		return this != DASHBOARD && this != REPORTS && this != ARCHIVES;
	}
}
