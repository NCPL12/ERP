package com.ncpl.sales.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

@Entity
@Table(name = "role")
@Audited
public class Role extends TimeStampEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "role_id")
	private Long roleId;

	@Column(nullable = false, unique = true, length = 50)
	private String name;

	// Comma-separated DashboardTile keys this role's users see on /dashboard.
	// null = never configured, show all (default/backward-compatible); "" = explicitly none.
	@Column(name = "dashboard_tiles", length = 255)
	private String dashboardTiles;

	@Column(name = "report_items", length = 1000)
	private String reportItems;

	// When true, users with this role may change the quantity of a sales item even
	// after a PO / DC has been created for it (the sales order row is otherwise locked).
	@Column(name = "sales_qty_edit_on_locked")
	private Boolean salesQtyEditOnLocked;

	// Username of the user who gets the amendment mail when a user with this role changes AMDQTY.
	@Column(name = "amendment_mail_username", length = 50)
	private String amendmentMailUsername;

	public String getAmendmentMailUsername() {
		return amendmentMailUsername;
	}

	public void setAmendmentMailUsername(String amendmentMailUsername) {
		this.amendmentMailUsername = amendmentMailUsername;
	}

	public boolean isSalesQtyEditOnLocked() {
		return Boolean.TRUE.equals(salesQtyEditOnLocked);
	}

	public void setSalesQtyEditOnLocked(boolean salesQtyEditOnLocked) {
		this.salesQtyEditOnLocked = salesQtyEditOnLocked;
	}

	public String getDashboardTiles() {
		return dashboardTiles;
	}

	public void setDashboardTiles(String dashboardTiles) {
		this.dashboardTiles = dashboardTiles;
	}

	public String getReportItems() {
		return reportItems;
	}

	public void setReportItems(String reportItems) {
		this.reportItems = reportItems;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
