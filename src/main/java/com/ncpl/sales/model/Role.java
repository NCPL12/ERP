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

	public String getDashboardTiles() {
		return dashboardTiles;
	}

	public void setDashboardTiles(String dashboardTiles) {
		this.dashboardTiles = dashboardTiles;
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
