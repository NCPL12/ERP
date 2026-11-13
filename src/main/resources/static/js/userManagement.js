var userTable;
var selectedUserId = null;

function escapeHtml(text) {
	if (text === null || text === undefined) {
		return "";
	}
	var div = document.createElement("div");
	div.textContent = String(text);
	return div.innerHTML;
}

$(document).ready(function () {
	loadUsers();
	loadRolesForDropdown();
	$('#userManagementTable thead').on('input change', 'input[data-column]', applyUserFilters);
});

function applyUserFilters() {
	if (!userTable) return;
	$('#userManagementTable thead input[data-column]').each(function () {
		var column = Number($(this).data('column'));
		var value = $.fn.dataTable.util.escapeRegex(this.value.trim());
		if (column === 5 && value) value = '^' + value;
		userTable.column(column).search(value, true, false);
	});
	userTable.draw();
}

/** Role dropdown in Add/Edit User - sourced from the same roles Role Management manages,
 *  instead of a hardcoded list that drifts from whatever roles actually exist. */
function loadRolesForDropdown() {
	$.ajax({
		url: api.ROLE_API,
		type: "GET",
		success: function (roles) {
			var options = '<option value="">Select Role</option>';
			$.each(roles, function (i, role) {
				var rn = role && role.name ? String(role.name) : '';
				if (rn === '') return;
				options += '<option value="' + escapeHtml(rn) + '">' + escapeHtml(rn) + '</option>';
			});
			$("#editRole").html(options);
		},
		error: function (xhr) {
			console.log("Unable to load roles for dropdown.", xhr);
		}
	});
}

function updateUserStatusInDatabase(userId, username, newStatus, enabled) {
	$.ajax({
		url: api.USER_MANAGEMENT_UPDATE + "/" + encodeURIComponent(userId) +
			"?username=" + encodeURIComponent(username) +
			"&enabled=" + enabled,
		type: 'PUT',
		success: function (response) {
			checkAndLogoutIfSameUser(userId, username);
		},
		error: function (xhr) {
			alert("Unable to update user status.");
			loadUsers();
		}
	});
}

function checkAndLogoutIfSameUser(userId, username) {
	$.ajax({
		url: api.USER_CURRENT,
		type: 'GET',
		success: function (currentUser) {
			if (!currentUser) {
				loadUsers();
				return;
			}
			var currentUserId = currentUser.id;
			if (String(currentUserId) === String(userId)) {
				window.location.replace(pageContext + "/login");
			} else {
				alert(username + " status changed successfully.");
				loadUsers();
			}
		},
		error: function (xhr) {
			if (xhr && xhr.status === 401) {
				window.location.replace(pageContext + "/login");
				return;
			}
			loadUsers();
		}
	});
}

function loadUsers() {
	$.ajax({
		url: api.USER_MANAGEMENT_LIST,
		type: 'GET',
		success: function (users) {
			userTable = $('#userManagementTable').DataTable({
				destroy: true,
				data: users,
				autoWidth: false,
				orderCellsTop: true,
				columns: [
					{
						data: null,
						orderable: false,
						searchable: false,
						render: function (data, type, row, meta) {
							return meta.row + 1;
						}
					},
					{ data: 'username', orderable: false, defaultContent: '' },
					{ data: 'emailId', orderable: false, defaultContent: '' },
					{ data: 'number', orderable: false, defaultContent: '' },
					{ data: 'role', orderable: false, defaultContent: '' },
					{
						data: 'enabled',
						orderable: false,
						defaultContent: true,
						render: function (data, type, row, meta) {
							return (data === true || data === 1 || data === 'true') ? "ACTIVE" : "INACTIVE";
						}
					},
					{
						data: null,
						orderable: false,
						searchable: false,
						className: 'text-center',
						render: function (data, type, row, meta) {
							var userId = row.id;
							if (!userId) {
								return '<span class="text-danger">ID Missing</span>';
							}
							return (
								'<button type="button" class="btn btn-info btn-sm mr-1" onclick="editUser(\'' + userId + '\')">' +
									'<i class="fas fa-edit"></i></button>' +
								'<button type="button" class="btn btn-light border btn-sm mr-1" style="color:#000;background-color:#fff;" onclick="changePassword(\'' + userId + '\')">' +
									'<i class="fas fa-key"></i></button>' +
								'<button type="button" class="btn btn-danger btn-sm" onclick="deleteUser(\'' + userId + '\')">' +
									'<i class="fas fa-trash"></i></button>'
							);
						}
					}
				]
			});
			applyUserFilters();
		},
		error: function (xhr) {
			alert("Unable to load users.");
		}
	});
}

function isValidMobileNumber(mobile) {
	if (!mobile) {
		return true;
	}
	return /^[0-9]{10}$/.test(mobile);
}

function isValidPassword(password) {
	return password.length >= 6;
}

function isValidEmail(email) {
	return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function isValidUsername(username) {
	return /^[a-zA-Z0-9_]{3,}$/.test(username);
}

function editUser(userId) {
	if (!userId || userId === "undefined" || userId === "null") {
		alert("User ID is missing.");
		return;
	}

	selectedUserId = userId;

	var rowData = userTable.rows().data();
	var userData = null;
	for (var i = 0; i < rowData.length; i++) {
		if (rowData[i].id == userId) {
			userData = rowData[i];
			break;
		}
	}

	if (userData) {
		$("#editUsername").val(userData.username || "");
		$("#editEmailId").val(userData.emailId || "");
		$("#editNumber").val(userData.number || "");
		$("#editRole").val(userData.role || "");
		$("#editStatus").val(userData.enabled === true || userData.enabled === 1 ? "ACTIVE" : "INACTIVE");

		$("#editPasswordGroup").hide();
		$("#editPassword").val("");

		$("#editUserModalLabel").text("Edit User");
		$(".modal-footer .btn-primary").html('<i class="fas fa-save"></i> Update User');

		$("#editUserModal").modal("show");
	} else {
		alert("Unable to find user data. Please refresh and try again.");
	}
}

function addNewUser() {
	selectedUserId = null;

	$("#editUsername").val("");
	$("#editEmailId").val("");
	$("#editNumber").val("");
	$("#editRole").val("");
	$("#editStatus").val("ACTIVE");

	$("#editPasswordGroup").show();
	$("#editPassword").val("");

	$("#editUserModalLabel").text("Add New User");
	$(".modal-footer .btn-primary").html('<i class="fas fa-plus"></i> Add User');

	$("#editUserModal").modal("show");
}

function updateUser() {
	var username = $("#editUsername").val();
	var emailId = $("#editEmailId").val();
	var role = $("#editRole").val();
	var statusValue = $("#editStatus").val();
	var number = $("#editNumber").val();

	if (!username) {
		alert("Username is required.");
		return;
	}
	username = username.trim();
	if (!isValidUsername(username)) {
		alert("Username must be at least 3 characters long and contain only letters, numbers, and underscores.");
		return;
	}

	if (!emailId) {
		alert("Email is required.");
		return;
	}
	emailId = emailId.trim();
	if (!isValidEmail(emailId)) {
		alert("Please enter a valid email address.");
		return;
	}

	if (number) {
		number = number.trim();
	}
	if (number && !isValidMobileNumber(number)) {
		alert("Mobile number must be exactly 10 digits.");
		return;
	}

	if (!role) {
		alert("Role is required.");
		return;
	}
	role = role.trim();

	if (!statusValue) {
		alert("Status is required.");
		return;
	}
	statusValue = statusValue.trim();
	var enabled = statusValue === "ACTIVE" ? 1 : 0;

	if (selectedUserId) {
		var updateParams = $.param({
			username: username,
			emailId: emailId,
			number: number || "",
			role: role,
			enabled: enabled
		});

		$.ajax({
			url: api.USER_MANAGEMENT_UPDATE + "/" + encodeURIComponent(selectedUserId) + "?" + updateParams,
			type: "PUT",
			success: function (response) {
				$("#editUserModal").modal("hide");
				var currentTargetUserId = selectedUserId;
				selectedUserId = null;

				if (enabled === 0) {
					checkAndLogoutIfSameUser(currentTargetUserId, username);
					return;
				}

				alert("User updated successfully.");
				loadUsers();
			},
			error: function (xhr) {
				if (xhr.status === 409) {
					alert(xhr.responseText);
				} else {
					alert("Unable to update user. Status: " + xhr.status);
				}
			}
		});
	} else {
		var password = $("#editPassword").val().trim();
		if (!password) {
			alert("Password is required.");
			return;
		}
		if (!isValidPassword(password)) {
			alert("Password must be at least 6 characters long.");
			return;
		}

		var createParams = $.param({
			user_name: username,
			password: password,
			role: role,
			number: number || "",
			emailId: emailId,
			enabled: enabled
		});

		$.ajax({
			url: api.USER_MANAGEMENT_UPDATE + "?" + createParams,
			type: "POST",
			success: function (response) {
				alert("User added successfully.");
				$("#editUserModal").modal("hide");
				selectedUserId = null;
				loadUsers();
			},
			error: function (xhr) {
				if (xhr.status === 409) {
					alert(xhr.responseText);
				} else {
					alert("Unable to add user. Status: " + xhr.status);
				}
			}
		});
	}
}

function changePassword(userId) {
	if (!userId || userId === "undefined" || userId === "null") {
		alert("User ID is missing.");
		return;
	}

	selectedUserId = userId;

	var rowData = userTable.rows().data();
	var userData = null;
	for (var i = 0; i < rowData.length; i++) {
		if (rowData[i].id == userId) {
			userData = rowData[i];
			break;
		}
	}

	if (userData) {
		$("#changePassUsername").val(userData.username || "");
		$("#changePassNewPassword").val("");
		$("#changePassConfirmPassword").val("");
		$("#changePasswordModal").modal("show");
	} else {
		alert("Unable to find user data. Please refresh and try again.");
	}
}

function updatePassword() {
	if (!selectedUserId) {
		alert("User ID is missing.");
		return;
	}

	var newPassword = $("#changePassNewPassword").val().trim();
	var confirmPassword = $("#changePassConfirmPassword").val().trim();

	if (!newPassword) {
		alert("New password is required.");
		return;
	}
	if (newPassword.length < 6) {
		alert("Password must be at least 6 characters long.");
		return;
	}
	if (!confirmPassword) {
		alert("Confirm password is required.");
		return;
	}
	if (newPassword !== confirmPassword) {
		alert("Passwords do not match.");
		return;
	}

	$.ajax({
		url: api.USER_MANAGEMENT_CHANGE_PASSWORD + "/" + encodeURIComponent(selectedUserId) +
			"/password?password=" + encodeURIComponent(newPassword),
		type: "PUT",
		success: function (response) {
			alert("Password changed successfully.");
			$("#changePasswordModal").modal("hide");
			selectedUserId = null;
			loadUsers();
		},
		error: function (xhr) {
			alert("Unable to change password. Status: " + xhr.status);
		}
	});
}

function deleteUser(userId) {
	if (!userId || userId === "undefined" || userId === "null") {
		alert("User ID is missing.");
		return;
	}

	if (!confirm("Are you sure you want to delete this user?")) {
		return;
	}

	$.ajax({
		url: api.USER_MANAGEMENT_DELETE + "/" + encodeURIComponent(userId),
		type: 'DELETE',
		success: function () {
			alert("User deleted successfully.");
			loadUsers();
		},
		error: function (xhr) {
			alert("Unable to delete user. Status: " + xhr.status);
		}
	});
}
