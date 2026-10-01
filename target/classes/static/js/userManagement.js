var userTable;
var selectedUserId = null;
var employeeOptionsCache = [];

$(document).ready(function () {
	loadUsers();
	loadEmployeesForNameDropdown();
});

/** Employee dropdown for the Name field - picking one auto-fills Mobile from the Employee table. */
function loadEmployeesForNameDropdown() {
	$.ajax({
		url: api.EMPLOYEE_MANAGEMENT_LIST,
		type: 'GET',
		success: function (employees) {
			employeeOptionsCache = employees || [];
			var options = '<option value="">Select Employee</option>';
			$.each(employeeOptionsCache, function (i, emp) {
				options += '<option value="' + emp.name + '">' + emp.name + ' (' + (emp.empId || '') + ')</option>';
			});
			$("#editName").html(options);
			$("#editName").select2({
				theme: "bootstrap4",
				tags: true,
				allowClear: true,
				placeholder: "Select Employee",
				dropdownParent: $("#editUserModal")
			});
		},
		error: function (xhr) {
			console.log("Unable to load employee list for Name dropdown.", xhr);
		}
	});
}

$(document).on("change", "#editName", function () {
	var selectedName = $(this).val();
	var match = null;
	$.each(employeeOptionsCache, function (i, emp) {
		if (emp.name === selectedName) {
			match = emp;
			return false;
		}
	});
	if (match) {
		$("#editNumber").val(match.contactNo || "");
	}
});

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

function isValidName(name) {
	if (!name) {
		return true;
	}
	return /^[a-zA-Z\s]{3,}$/.test(name);
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
		setEditNameValue(userData.name || "");
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

/** Sets the Name select2's value, adding it as an ad-hoc option first if it's not one of the
 *  known employees (e.g. an existing user's name typed in before this dropdown existed). */
function setEditNameValue(name) {
	var $select = $("#editName");
	if (name && $select.find("option[value='" + name.replace(/'/g, "\\'") + "']").length === 0) {
		$select.append('<option value="' + name + '">' + name + '</option>');
	}
	$select.val(name || "").trigger("change");
}

function addNewUser() {
	selectedUserId = null;

	$("#editUsername").val("");
	setEditNameValue("");
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

	var name = $("#editName").val();
	if (name) {
		name = name.trim();
	}
	if (!isValidName(name)) {
		alert("Name must be at least 3 characters long and contain only letters and spaces.");
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
			name: name || "",
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
			name: name || "",
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
