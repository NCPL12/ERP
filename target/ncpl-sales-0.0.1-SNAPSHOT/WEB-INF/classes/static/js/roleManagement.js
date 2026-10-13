var roles = [];
var selectedRoleId = null;
var currentGrid = [];
var currentTiles = [];

var MODULE_URLS = {
	DASHBOARD: "/dashboard",
	PARTY: "/partyList",
	ITEM_MASTER: "/itemMaster",
	COMPANY_ASSETS: "/companyAssets",
	SALES_ORDER: "/salesList",
	DELIVERY_CHALLAN: "/dcList",
	RETURNABLE: "/returnableList",
	INVOICE: "/invoiceList",
	WORK_ORDER: "/workOrderList",
	PURCHASE: "/purchase",
	GRN: "/grnLists",
	NON_BILLABLE: "/nonBillableList",
	REPORTS: "/sales_report",
	ARCHIVES: "/salesList_archived",
	USER_MANAGEMENT: "/user-management"
};

$(document).ready(function () {
	loadRoles();

	$("#newRoleBtn").on("click", function () {
		$("#newRoleName").val("");
		$("#newRoleModal").modal("show");
	});

	$("#createRoleBtn").on("click", createRole);
	$("#savePermissionsBtn").on("click", savePermissions);
});

function loadRoles() {
	$.ajax({
		url: api.ROLE_API,
		type: "GET",
		success: function (response) {
			roles = response;
			renderRoleList();
			if (!selectedRoleId && roles.length > 0) {
				selectRole(roles[0].roleId);
			}
		},
		error: function (xhr) {
			alert("Unable to load roles. Status: " + xhr.status);
		}
	});
}

function escapeHtml(text) {
	if (text === null || text === undefined) {
		return "";
	}
	var div = document.createElement("div");
	div.textContent = String(text);
	return div.innerHTML;
}

function renderRoleList() {
	var html = "";
	$.each(roles, function (i, role) {
		var activeClass = (String(role.roleId) === String(selectedRoleId)) ? "active" : "";
		var roleId = String(role.roleId);
		var roleNameEsc = escapeHtml(role.name);
		var userCountEsc = escapeHtml(role.userCount);
		html += '<a href="#" class="role-row ' + activeClass + '" data-role-id="' + roleId + '">' +
			'<span>' + roleNameEsc + '</span>' +
			'<span class="role-row-right">' +
			'<span class="role-count" title="users with this role">' + userCountEsc + '</span>' +
			'<i class="fa fa-trash role-delete-btn" data-role-id="' + roleId + '" data-role-name="' + roleNameEsc + '" title="Delete role"></i>' +
			'</span>' +
			'</a>';
	});
	$("#roleList").html(html);

	$(".role-row").on("click", function (e) {
		e.preventDefault();
		selectRole($(this).data("role-id"));
	});

	$(".role-delete-btn").on("click", function (e) {
		e.preventDefault();
		e.stopPropagation();
		deleteRole($(this).data("role-id"), $(this).text() || $(this).attr("data-role-name"));
	});
}

function deleteRole(roleId, roleName) {
	if (!confirm("Delete role '" + roleName + "'? This cannot be undone.")) {
		return;
	}
	$.ajax({
		url: api.ROLE_API + "/" + roleId,
		type: "DELETE",
		success: function () {
			if (String(selectedRoleId) === String(roleId)) {
				selectedRoleId = null;
			}
			loadRoles();
		},
		error: function (xhr) {
			if (xhr.status === 409) {
				alert(xhr.responseText);
			} else {
				alert("Unable to delete role. Status: " + xhr.status);
			}
		}
	});
}

function selectRole(roleId) {
	selectedRoleId = roleId;
	renderRoleList();
	var hasEdit = window && window.hasUserManagementEdit ? window.hasUserManagementEdit : false;
	$("#savePermissionsBtn").prop("disabled", !hasEdit);
	$("#newRoleBtn").prop("disabled", !hasEdit);

	var currentRid = selectedRoleId;
	currentGrid = [];
	currentTiles = [];

	$.when(
		$.ajax({
			url: api.ROLE_API + "/" + roleId + "/permissions",
			type: "GET",
			cache: false
		}),
		$.ajax({
			url: api.ROLE_API + "/" + roleId + "/dashboard-tiles",
			type: "GET",
			cache: false
		})
	).done(function (permResp, tilesResp) {
		if (String(currentRid) !== String(selectedRoleId)) {
			return;
		}
		currentGrid = permResp[0];
		currentTiles = tilesResp[0];
		renderGrid();
		updateRoleSummary();
	}).fail(function (xhr) {
		if (xhr && xhr.status) {
			alert("Unable to load role details. Status: " + xhr.status);
		} else {
			alert("Unable to load role details.");
		}
	});
}

function updateRoleSummary() {
	var role = roles.find(function (r) { return String(r.roleId) === String(selectedRoleId); });
	var viewable = currentGrid.filter(function (row) { return row.canView; }).length;
	if (!role) {
		$("#roleSummary").text("Select a role");
		return;
	}
	$("#roleSummary").text(role.name + " · " + viewable + " of " + currentGrid.length + " modules · " +
		role.userCount + " user" + (role.userCount === 1 ? "" : "s") + " hold this role");
}

function renderGrid() {
	var rows = "";
	$.each(currentGrid, function (i, row) {
		var url = MODULE_URLS[row.module] || "";
		rows += '<tr data-module="' + row.module + '" data-editable="' + row.editable + '" data-deletable="' + row.deletable + '">' +
			'<td><span class="ra-module-name">' + row.label + '</span><span class="ra-module-url">' + url + '</span></td>' +
			'<td><input type="checkbox" class="view-box" ' + (row.canView ? "checked" : "") + '></td>' +
			editDeleteCell("edit-box", row.canView && row.editable, row.canEdit) +
			editDeleteCell("delete-box", row.canView && row.deletable, row.canDelete) +
			'</tr>';
	});
	$("#permissionGridBody").html(rows);
	renderDashboardTilesRow();
}

// No View, or the module has no such permission at all (e.g. DASHBOARD has no
// Edit/Delete) -> show a dash instead of a dead/meaningless checkbox.
function editDeleteCell(cssClass, applicable, checked) {
	if (!applicable) {
		return '<td class="ra-not-applicable">&ndash;</td>';
	}
	return '<td><input type="checkbox" class="' + cssClass + '" ' + (checked ? "checked" : "") + '></td>';
}

// Which of the 7 /dashboard tiles this role sees — only shown while the
// DASHBOARD module's View box is ticked.
function renderDashboardTilesRow() {
	$("#permissionGridBody .dashboard-tiles-row").remove();
	var dashViewOn = $('#permissionGridBody tr[data-module="DASHBOARD"] .view-box').is(":checked");
	if (!dashViewOn || currentTiles.length === 0) {
		return;
	}
	var boxes = "";
	$.each(currentTiles, function (i, tile) {
		var key = tile && tile.key ? escapeHtml(tile.key) : "";
		var label = tile && tile.label ? escapeHtml(tile.label) : key;
		boxes += '<label class="ra-tile-check"><input type="checkbox" class="tile-box" data-tile="' + key + '" ' +
			(tile.visible ? "checked" : "") + '>&nbsp;' + label + '</label>';
	});
	var row = '<tr class="dashboard-tiles-row"><td colspan="4"><div class="ra-tiles-inline">' +
		'<span class="ra-tiles-label">Dashboard tiles</span>' +
		'<div class="ra-tiles-checks">' + boxes + '</div></div></td></tr>';
	$('#permissionGridBody tr[data-module="DASHBOARD"]').after(row);
}

// Ticking Edit or Delete switches View on and locks it.
$(document).on("change", "#permissionGridBody .edit-box, #permissionGridBody .delete-box", function () {
	var row = $(this).closest("tr");
	if ($(this).is(":checked")) {
		row.find(".view-box").prop("checked", true);
	}
	var stillForced = row.find(".edit-box:checked, .delete-box:checked").length > 0;
	row.find(".view-box").prop("disabled", stillForced);
});
$(document).on("change", "#permissionGridBody .view-box", function () {
	var row = $(this).closest("tr");
	var checked = $(this).is(":checked");
	if (checked) {
		// Was showing dashes — turn them into real boxes,
		// unless the module doesn't support that permission at all (e.g. DASHBOARD).
		var editable = row.data("editable") === true || row.data("editable") === "true";
		var deletable = row.data("deletable") === true || row.data("deletable") === "true";
		row.find("td.ra-not-applicable").each(function () {
			var $td = $(this);
			var isEditCol = $td.index() === 2;
			if (isEditCol ? !editable : !deletable) {
				return;
			}
			$td.removeClass("ra-not-applicable").html('<input type="checkbox" class="' + (isEditCol ? "edit-box" : "delete-box") + '">');
		});
		var stillForced = row.find(".edit-box:checked, .delete-box:checked").length > 0;
		row.find(".view-box").prop("disabled", stillForced);
	} else {
		// No View => Edit/Delete are meaningless again — collapse back to dashes, preserving visual state by clearing checks
		row.find(".edit-box, .delete-box").prop("checked", false);
		row.find(".edit-box, .delete-box").closest("td").addClass("ra-not-applicable").html("&ndash;");
		row.find(".view-box").prop("disabled", false);
	}
	if (row.data("module") === "DASHBOARD") {
		renderDashboardTilesRow();
	}
});

function createRole() {
	var name = $("#newRoleName").val().trim();
	if (!name) {
		alert("Role name is required.");
		return;
	}

	$.ajax({
		url: api.ROLE_API + "?name=" + encodeURIComponent(name),
		type: "POST",
		success: function (response) {
			$("#newRoleModal").modal("hide");
			loadRoles();
		},
		error: function (xhr) {
			if (xhr.status === 409) {
				alert(xhr.responseText);
			} else {
				alert("Unable to create role. Status: " + xhr.status);
			}
		}
	});
}

function savePermissions() {
	if (!selectedRoleId) {
		return;
	}

	var rows = [];
	$("#permissionGridBody tr[data-module]").each(function () {
		var tr = $(this);
		var module = tr.data("module");
		if (!module) {
			return;
		}
		rows.push({
			module: module,
			canView: tr.find(".view-box").is(":checked"),
			canEdit: tr.find(".edit-box").is(":checked"),
			canDelete: tr.find(".delete-box").is(":checked")
		});
	});

	var tileKeys = [];
	$("#permissionGridBody .tile-box:checked").each(function () {
		var t = $(this).data("tile");
		if (t) {
			tileKeys.push(String(t));
		}
	});

	if ($("#permissionGridBody .dashboard-tiles-row").length && tileKeys.length === 0) {
		alert("At least one dashboard tile must be enabled.");
		return;
	}

	$.ajax({
		url: api.ROLE_API + "/" + selectedRoleId + "/permissions",
		type: "PUT",
		contentType: "application/json",
		data: JSON.stringify(rows),
		success: function () {
			$.ajax({
				url: api.ROLE_API + "/" + selectedRoleId + "/dashboard-tiles",
				type: "PUT",
				contentType: "application/json",
				data: JSON.stringify(tileKeys),
				success: function () {
					alert("Permissions saved.");
					loadRoles();
					selectRole(selectedRoleId);
				},
				error: function (xhr) {
					alert(xhr.responseText || ("Permissions saved, but dashboard tiles failed. Status: " + xhr.status));
				}
			});
		},
		error: function (xhr) {
			alert("Unable to save permissions. Status: " + xhr.status);
		}
	});
}
