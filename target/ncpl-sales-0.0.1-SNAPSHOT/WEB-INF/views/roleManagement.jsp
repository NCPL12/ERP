<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Role &amp; Access</title>
<tiles:insertAttribute name="header-resources" />
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<security:authorize access="hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_EDIT')" var="canEditUserMgmt" />
<script>
var pageContext = '${pageContext.request.contextPath}';
window.hasUserManagementEdit = ${canEditUserMgmt};
</script>
<style>
.ra-page {
	background: #fff;
	border: 1px solid #e5e7eb;
	border-radius: 6px;
	overflow: hidden;
}
.ra-header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	padding: 18px 20px;
	border-bottom: 1px solid #e5e7eb;
}
.ra-header h2 {
	margin: 0;
	font-size: 1.15rem;
	font-weight: 700;
	color: #111827;
}
.ra-subtitle {
	margin-top: 4px;
	font-size: 0.8rem;
	color: #6b7280;
}
.ra-actions {
	display: flex;
	gap: 10px;
	flex-shrink: 0;
}
.ra-btn-outline {
	background: #fff;
	border: 1px solid #d1d5db;
	color: #111827;
	border-radius: 5px;
	padding: 6px 14px;
	font-size: 0.85rem;
}
.ra-btn-outline:hover {
	background: #f9fafb;
}
.ra-btn-primary {
	background: #2563eb;
	border: 1px solid #2563eb;
	color: #fff;
	border-radius: 5px;
	padding: 6px 14px;
	font-size: 0.85rem;
}
.ra-btn-primary:hover {
	background: #1d4ed8;
}
.ra-btn-primary:disabled {
	background: #93c5fd;
	border-color: #93c5fd;
	cursor: not-allowed;
}
.ra-body {
	display: flex;
	align-items: stretch;
}
.ra-roles-pane {
	width: 260px;
	flex-shrink: 0;
	border-right: 1px solid #e5e7eb;
}
.ra-col-header {
	background: #f9fafb;
	color: #6b7280;
	font-size: 0.7rem;
	font-weight: 700;
	letter-spacing: 0.05em;
	text-transform: uppercase;
	padding: 12px 16px;
	border-bottom: 1px solid #e5e7eb;
}
.role-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 52px;
	padding: 0 16px;
	box-sizing: border-box;
	cursor: pointer;
	border-bottom: 1px solid #f1f3f5;
	border-left: 3px solid transparent;
	color: #111827;
	font-size: 0.85rem;
	text-decoration: none;
}
.role-row:hover {
	background: #f9fafb;
}
.role-row.active {
	background: #eff6ff;
	border-left-color: #2563eb;
	color: #2563eb;
	font-weight: 600;
}
.role-row .role-count {
	color: #9ca3af;
	font-size: 0.8rem;
}
.role-row .role-row-right {
	display: flex;
	align-items: center;
	gap: 10px;
}
.role-row .role-delete-btn {
	color: #9ca3af;
	font-size: 0.85rem;
	padding: 2px 4px;
	border-radius: 4px;
	visibility: hidden;
}
.role-row:hover .role-delete-btn {
	visibility: visible;
}
.role-row .role-delete-btn:hover {
	color: #dc2626;
	background: #fee2e2;
}
.ra-grid-pane {
	flex: 1;
	overflow-x: auto;
}
.ra-grid {
	width: 100%;
	table-layout: fixed;
	border-collapse: collapse;
}
.ra-grid th:first-child,
.ra-grid td:first-child {
	width: auto;
}
.ra-grid th:not(:first-child),
.ra-grid td:not(:first-child) {
	width: 100px;
}
.ra-grid thead th {
	background: #f9fafb;
	color: #6b7280;
	font-size: 0.7rem;
	font-weight: 700;
	letter-spacing: 0.05em;
	text-transform: uppercase;
	padding: 12px 16px;
	border-bottom: 1px solid #e5e7eb;
	text-align: center;
	white-space: nowrap;
}
.ra-grid thead th:first-child {
	text-align: left;
}
.ra-grid tbody td {
	height: 52px;
	padding: 8px 16px;
	border-bottom: 1px solid #f1f3f5;
	vertical-align: middle;
}
.ra-grid tbody td:first-child {
	text-align: left;
}
.ra-grid tbody td:not(:first-child) {
	text-align: center;
}
.ra-module-name {
	display: block;
	font-weight: 600;
	color: #111827;
	font-size: 0.85rem;
	line-height: 1.3;
}
.ra-module-url {
	display: block;
	font-size: 0.72rem;
	color: #9ca3af;
	line-height: 1.3;
}
.ra-grid input[type=checkbox] {
	width: 17px;
	height: 17px;
	margin: 0;
	vertical-align: middle;
}
.ra-grid input.view-box {
	accent-color: #2563eb;
}
.ra-grid input.edit-box {
	accent-color: #eab308;
}
.ra-grid input.delete-box {
	accent-color: #e11d48;
}
.ra-not-applicable {
	color: #d1d5db;
	font-weight: 700;
}
.dashboard-tiles-row td {
	background: #f9fafb;
	height: auto;
	padding: 12px 16px;
}
.ra-tiles-inline {
	text-align: left;
}
.ra-tiles-label {
	display: block;
	margin-bottom: 10px;
	font-size: 0.75rem;
	font-weight: 700;
	color: #6b7280;
	text-transform: uppercase;
	letter-spacing: 0.05em;
}
  .ra-tiles-checks {
  	display: flex;
  	flex-wrap: wrap;
  	column-gap: 18px;
  	row-gap: 10px;
  	max-width: 100%;
  	align-items: center;
  }
  .ra-tile-check {
  	display: inline-flex;
  	align-items: center;
  	gap: 6px;
  	font-weight: 400;
  	font-size: 0.82rem;
  	color: #374151;
  	margin: 0;
  	white-space: nowrap;
  	line-height: 1;
  }
  .ra-tile-check input {
  	accent-color: #2563eb;
  	margin: 0;
  	vertical-align: middle;
  	position: relative;
  	top: 0;
  }
.ra-legend {
	display: flex;
	align-items: center;
	gap: 6px;
	background: #f9fafb;
	border-top: 1px solid #e5e7eb;
	padding: 12px 16px;
	font-size: 0.8rem;
	color: #374151;
}
.ra-swatch {
	display: inline-block;
	width: 12px;
	height: 12px;
	border-radius: 2px;
	margin-left: 16px;
}
.ra-swatch:first-child {
	margin-left: 0;
}
.ra-swatch-view {
	background: #2563eb;
}
.ra-swatch-edit {
	background: #eab308;
}
.ra-swatch-delete {
	background: #e11d48;
}
.ra-legend-note {
	margin-left: auto;
	color: #9ca3af;
}
</style>
</head>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
	<tiles:insertAttribute name="header" />
	<tiles:insertAttribute name="sideMenu" />

	<div class="content-wrapper">
		<section class="content">
			<div class="container-fluid">
				<div class="ra-page">
					<div class="ra-header">
						<div>
							<h2>Roles &amp; Permissions</h2>
							<div class="ra-subtitle" id="roleSummary">Select a role</div>
						</div>
						<div class="ra-actions">
							<button type="button" class="ra-btn-outline" id="newRoleBtn" ${canEditUserMgmt ? '' : 'disabled'}>New Role</button>
							<button type="button" class="ra-btn-primary" id="savePermissionsBtn" disabled>Save Permissions</button>
						</div>
					</div>
					<div class="ra-body">
						<div class="ra-roles-pane">
							<div class="ra-col-header">Roles</div>
							<div id="roleList"></div>
						</div>
						<div class="ra-grid-pane">
							<table class="ra-grid">
								<thead>
									<tr>
										<th>Module</th>
										<th>View</th>
										<th>Edit</th>
										<th>Delete</th>
									</tr>
								</thead>
								<tbody id="permissionGridBody"></tbody>
							</table>
						</div>
					</div>
					<div class="ra-legend">
						<span class="ra-swatch ra-swatch-view"></span> View
						<span class="ra-swatch ra-swatch-edit"></span> Edit
						<span class="ra-swatch ra-swatch-delete"></span> Delete
						<span class="ra-legend-note">Edit or Delete implies View</span>
					</div>
				</div>
			</div>
		</section>
	</div>

	<tiles:insertAttribute name="footer" />
</div>

<!-- NEW ROLE MODAL -->
<div class="modal fade" id="newRoleModal" tabindex="-1" role="dialog" aria-hidden="true">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<h5 class="modal-title">New Role</h5>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<div class="form-group">
					<label for="newRoleName">Role name</label>
					<input type="text" id="newRoleName" class="form-control" placeholder="e.g. SALES MANAGER">
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
				<button type="button" class="btn btn-primary" id="createRoleBtn">Create</button>
			</div>
		</div>
	</div>
</div>

<script src="${pageContext.request.contextPath}/resources/js/roleManagement.js"></script>
<script>
	$(document).ready(function () {
		$(".pageHeader").text("Role & Access");
	});
</script>
</body>
</html>
