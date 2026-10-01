package com.ncpl.sales.model;

public enum Module {
	DASHBOARD, PARTY, ITEM_MASTER, COMPANY_ASSETS, SALES_ORDER,
	DELIVERY_CHALLAN, RETURNABLE, INVOICE, WORK_ORDER, PURCHASE,
	GRN, NON_BILLABLE, REPORTS, ARCHIVES, USER_MANAGEMENT;

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
