package com.ncpl.sales.model;

/** One row of the role-management permission grid, as posted from roleManagement.js. */
public class RolePermissionDto {

	private String module;
	private boolean canView;
	private boolean canEdit;
	private boolean canDelete;

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public boolean isCanView() {
		return canView;
	}

	public void setCanView(boolean canView) {
		this.canView = canView;
	}

	public boolean isCanEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	public boolean isCanDelete() {
		return canDelete;
	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}
}
