var employeeTable;
var selectedEmployeeId = null;

$(document).ready(function () {
	loadEmployees();
});

function loadEmployees() {
	$.ajax({
		url: api.EMPLOYEE_MANAGEMENT_LIST,
		type: 'GET',
		success: function (employees) {
			employeeTable = $('#employeeManagementTable').DataTable({
				destroy: true,
				data: employees,
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
					{ data: 'empId', orderable: false, defaultContent: '' },
					{ data: 'name', orderable: false, defaultContent: '' },
					{ data: 'contactNo', orderable: false, defaultContent: '' },
					{
						data: null,
						orderable: false,
						searchable: false,
						className: 'text-center',
						render: function (data, type, row, meta) {
							var empId = row.id;
							return (
								'<button type="button" class="btn btn-info btn-sm mr-1" onclick="editEmployee(' + empId + ')">' +
									'<i class="fas fa-edit"></i></button>' +
								'<button type="button" class="btn btn-danger btn-sm" onclick="deleteEmployee(' + empId + ')">' +
									'<i class="fas fa-trash"></i></button>'
							);
						}
					}
				]
			});
		},
		error: function (xhr) {
			alert("Unable to load employees.");
		}
	});
}

function addNewEmployee() {
	selectedEmployeeId = null;
	$("#editEmployeeForm")[0].reset();
	$("#editEmployeeModalLabel").text("Add New Employee");
	$("#editEmployeeModal").modal("show");
}

function editEmployee(id) {
	selectedEmployeeId = id;
	var rowData = employeeTable.rows().data();
	var employee = null;
	for (var i = 0; i < rowData.length; i++) {
		if (rowData[i].id == id) {
			employee = rowData[i];
			break;
		}
	}
	if (!employee) {
		alert("Unable to find employee data. Please refresh and try again.");
		return;
	}
	$("#editEmpId").val(employee.empId || "");
	$("#editEmpName").val(employee.name || "");
	$("#editEmpContactNo").val(employee.contactNo || "");
	$("#editEmployeeModalLabel").text("Edit Employee");
	$("#editEmployeeModal").modal("show");
}

function saveEmployee() {
	var empId = $("#editEmpId").val().trim();
	var name = $("#editEmpName").val().trim();
	var contactNo = $("#editEmpContactNo").val().trim();

	if (!empId) {
		alert("Emp ID is required.");
		return;
	}
	if (!name) {
		alert("Name is required.");
		return;
	}
	if (contactNo && !/^[0-9]{10}$/.test(contactNo)) {
		alert("Contact number must be exactly 10 digits.");
		return;
	}

	var params = { empId: empId, name: name, contactNo: contactNo };

	if (selectedEmployeeId) {
		$.ajax({
			url: api.EMPLOYEE_MANAGEMENT_SAVE + "/" + encodeURIComponent(selectedEmployeeId),
			type: "PUT",
			data: params,
			success: function () {
				alert("Employee updated successfully.");
				$("#editEmployeeModal").modal("hide");
				selectedEmployeeId = null;
				loadEmployees();
			},
			error: function (xhr) {
				alert(xhr.status === 409 ? xhr.responseText : "Unable to update employee.");
			}
		});
	} else {
		$.ajax({
			url: api.EMPLOYEE_MANAGEMENT_SAVE,
			type: "POST",
			data: params,
			success: function () {
				alert("Employee added successfully.");
				$("#editEmployeeModal").modal("hide");
				loadEmployees();
			},
			error: function (xhr) {
				alert(xhr.status === 409 ? xhr.responseText : "Unable to add employee.");
			}
		});
	}
}

function deleteEmployee(id) {
	if (!confirm("Are you sure you want to delete this employee?")) {
		return;
	}
	$.ajax({
		url: api.EMPLOYEE_MANAGEMENT_SAVE + "/" + encodeURIComponent(id),
		type: 'DELETE',
		success: function () {
			alert("Employee deleted successfully.");
			loadEmployees();
		},
		error: function (xhr) {
			alert("Unable to delete employee. Status: " + xhr.status);
		}
	});
}
