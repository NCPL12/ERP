<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>User Management</title>
<tiles:insertAttribute name="header-resources" />
<script>
var pageContext = '${pageContext.request.contextPath}';
</script>
</head>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
	<tiles:insertAttribute name="header" />
	<tiles:insertAttribute name="sideMenu" />

	<div class="content-wrapper">
		<section class="content">
			<div class="container-fluid">
				<ul class="nav nav-tabs" id="userManagementTabs">
					<li class="nav-item"><a class="nav-link active" data-toggle="tab" href="#usersTabPane">Users</a></li>
					<li class="nav-item"><a class="nav-link" data-toggle="tab" href="#employeesTabPane">Employees</a></li>
				</ul>
				<div class="tab-content">
				<div class="tab-pane fade show active" id="usersTabPane">
				<div class="card">
					<div class="card-header">
						<div class="card-tools">
							<button type="button" class="btn btn-primary btn-sm" onclick="addNewUser()">
								<i class="fas fa-plus"></i> Add User
							</button>
						</div>
					</div>
					<div class="card-body">
						<table id="userManagementTable" class="table table-bordered table-striped" style="width:100%">
							<thead>
								<tr>
									<th>S.No.</th>
									<th>Username</th>
									<th>Email</th>
									<th>Mobile</th>
									<th>Role</th>
									<th>Status</th>
									<th>Action</th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
				</div>
				<div class="tab-pane fade" id="employeesTabPane">
				<div class="card">
					<div class="card-header">
						<div class="card-tools">
							<button type="button" class="btn btn-primary btn-sm" onclick="addNewEmployee()">
								<i class="fas fa-plus"></i> Add Employee
							</button>
						</div>
					</div>
					<div class="card-body">
						<table id="employeeManagementTable" class="table table-bordered table-striped" style="width:100%">
							<thead>
								<tr>
									<th>S.No.</th>
									<th>Emp ID</th>
									<th>Name</th>
									<th>Contact No</th>
									<th>Action</th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
				</div>
				</div>
			</div>
		</section>
	</div>

	<tiles:insertAttribute name="footer" />
</div>

<!-- CHANGE PASSWORD MODAL -->
<div class="modal fade" id="changePasswordModal" tabindex="-1" role="dialog"
	aria-labelledby="changePasswordModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg" role="document">
		<div class="modal-content">
			<div class="modal-header bg-primary">
				<h5 class="modal-title text-white" id="changePasswordModalLabel">Change Password</h5>
				<button type="button" class="close text-white" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<form id="changePasswordForm" name="changePasswordForm">
					<div class="row">
						<div class="col-md-12">
							<div class="form-group">
								<label for="changePassUsername">Username</label>
								<input type="text" id="changePassUsername" name="username" class="form-control" disabled>
							</div>
						</div>
						<div class="col-md-12">
							<div class="form-group">
								<label for="changePassNewPassword">New Password <span class="text-danger">*</span></label>
								<input type="password" id="changePassNewPassword" name="newPassword" class="form-control"
									required minlength="6" placeholder="Enter new password">
								<small class="form-text text-muted"><i class="fas fa-info-circle"></i>
									Password must be at least 6 characters long.</small>
							</div>
						</div>
						<div class="col-md-12">
							<div class="form-group">
								<label for="changePassConfirmPassword">Confirm Password <span class="text-danger">*</span></label>
								<input type="password" id="changePassConfirmPassword" name="confirmPassword" class="form-control"
									required minlength="6" placeholder="Confirm new password">
							</div>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer bg-light">
				<button type="button" class="btn btn-secondary" data-dismiss="modal"><i class="fas fa-times"></i> Cancel</button>
				<button type="button" class="btn btn-primary" onclick="updatePassword()"><i class="fas fa-lock"></i> Change Password</button>
			</div>
		</div>
	</div>
</div>

<!-- EDIT USER MODAL -->
<div class="modal fade" id="editUserModal" tabindex="-1" role="dialog" aria-labelledby="editUserModalLabel" aria-hidden="true">
	<div class="modal-dialog modal-lg" role="document">
		<div class="modal-content">
			<div class="modal-header bg-light">
				<h5 class="modal-title" id="editUserModalLabel">Edit User</h5>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<form id="editUserForm" name="editUserForm">
					<div class="row">
						<div class="col-md-6">
							<div class="form-group">
								<label for="editUsername">Username <span class="text-danger">*</span></label>
								<input type="text" id="editUsername" name="username" class="form-control" required
									minlength="3" pattern="[a-zA-Z0-9_]{3,}"
									placeholder="Enter username (min 3 chars, letters/numbers/underscore)"
									title="Username must be at least 3 characters with letters, numbers, or underscores">
								<small class="form-text text-muted"><i class="fas fa-info-circle"></i>
									Min 3 characters. Use letters, numbers, underscores only.</small>
							</div>
						</div>

						<!-- Password field, only shown/required when adding a new user -->
						<div class="col-md-6" id="editPasswordGroup">
							<div class="form-group">
								<label for="editPassword">Password <span class="text-danger">*</span></label>
								<input type="password" id="editPassword" name="password" class="form-control"
									minlength="6" placeholder="Enter password (minimum 6 characters)">
								<small class="form-text text-muted"><i class="fas fa-info-circle"></i>
									Password must be at least 6 characters long.</small>
							</div>
						</div>

						<div class="col-md-6">
							<div class="form-group">
								<label for="editName">Name <span class="text-muted">(Optional)</span></label>
								<select id="editName" name="name" class="form-control select2-employee">
									<option value="">Select Employee</option>
								</select>
								<small class="form-text text-muted"><i class="fas fa-info-circle"></i>
									Pulled from the Employee list - picking one fills in Mobile too.</small>
							</div>
						</div>

						<div class="col-md-6">
							<div class="form-group">
								<label for="editEmailId">Email <span class="text-danger">*</span></label>
								<input type="email" id="editEmailId" name="emailId" class="form-control" required
									placeholder="Enter email">
							</div>
						</div>

						<div class="col-md-6">
							<div class="form-group">
								<label for="editNumber">Mobile <span class="text-muted">(10 digits)</span></label>
								<input type="text" id="editNumber" name="number" class="form-control" pattern="[0-9]{10}"
									maxlength="10" placeholder="Enter 10-digit mobile number"
									title="Mobile number must be exactly 10 digits">
								<small class="form-text text-muted"><i class="fas fa-info-circle"></i>
									Enter exactly 10 digits.</small>
							</div>
						</div>

						<div class="col-md-6">
							<div class="form-group">
								<label for="editRole">Role <span class="text-danger">*</span></label>
								<select id="editRole" name="role" class="form-control" required>
									<option value="">Select Role</option>
								</select>
							</div>
						</div>

						<div class="col-md-6">
							<div class="form-group">
								<label for="editStatus">Status <span class="text-danger">*</span></label>
								<select id="editStatus" name="status" class="form-control" required>
									<option value="">Select Status</option>
									<option value="ACTIVE">ACTIVE</option>
									<option value="INACTIVE">INACTIVE</option>
								</select>
							</div>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer bg-light">
				<button type="button" class="btn btn-secondary" data-dismiss="modal"><i class="fas fa-times"></i> Cancel</button>
				<button type="button" class="btn btn-primary" onclick="updateUser()"><i class="fas fa-save"></i> Update User</button>
			</div>
		</div>
	</div>
</div>

<!-- EDIT EMPLOYEE MODAL -->
<div class="modal fade" id="editEmployeeModal" tabindex="-1" role="dialog" aria-labelledby="editEmployeeModalLabel" aria-hidden="true">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header bg-light">
				<h5 class="modal-title" id="editEmployeeModalLabel">Edit Employee</h5>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">
				<form id="editEmployeeForm" name="editEmployeeForm">
					<div class="row">
						<div class="col-md-12">
							<div class="form-group">
								<label for="editEmpId">Emp ID <span class="text-danger">*</span></label>
								<input type="text" id="editEmpId" name="empId" class="form-control" required
									placeholder="Enter employee ID">
							</div>
						</div>
						<div class="col-md-12">
							<div class="form-group">
								<label for="editEmpName">Name <span class="text-danger">*</span></label>
								<input type="text" id="editEmpName" name="name" class="form-control" required
									placeholder="Enter employee name">
							</div>
						</div>
						<div class="col-md-12">
							<div class="form-group">
								<label for="editEmpContactNo">Contact No</label>
								<input type="text" id="editEmpContactNo" name="contactNo" class="form-control" pattern="[0-9]{10}"
									maxlength="10" placeholder="Enter 10-digit contact number">
							</div>
						</div>
					</div>
				</form>
			</div>
			<div class="modal-footer bg-light">
				<button type="button" class="btn btn-secondary" data-dismiss="modal"><i class="fas fa-times"></i> Cancel</button>
				<button type="button" class="btn btn-primary" onclick="saveEmployee()"><i class="fas fa-save"></i> Save Employee</button>
			</div>
		</div>
	</div>
</div>

<script src="${pageContext.request.contextPath}/resources/js/userManagement.js"></script>
<script src="${pageContext.request.contextPath}/resources/js/employeeManagement.js"></script>
<script>
	$(document).ready(function () {
		$(".pageHeader").text("User Management");
	});
	$(document).on('shown.bs.tab', 'a[href="#employeesTabPane"]', function () {
		if (employeeTable) {
			employeeTable.columns.adjust();
		}
	});
</script>
</body>
</html>
