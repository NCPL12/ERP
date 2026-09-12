var userTable;
var selectedUserId = null;
var selectedUserData = null;


$(document).ready(function () {

    loadUsers();

});


function loadUsers() {

    $.ajax({

        url: api.USER_MANAGEMENT_LIST,

        type: 'GET',

        success: function (users) {

            console.log("Users:", users);

            userTable = $('#userManagementTable').DataTable({

                destroy: true,

                data: users,

                autoWidth: false,

                columns: [

                    // =========================
                    // S.NO.
                    // =========================
                    {
                        data: null,
                        orderable: false,
                        searchable: false,

                        render: function (data, type, row, meta) {

                            return meta.row + 1;

                        }
                    },


                    // =========================
                    // USERNAME
                    // =========================
                    {
                        data: 'username',
                        orderable: false,
                        defaultContent: ''
                    },


                    // =========================
                    // EMAIL
                    // =========================
                    {
                        data: 'emailId',
                        orderable: false,
                        defaultContent: ''
                    },


                    // =========================
                    // MOBILE
                    // =========================
                    {
                        data: 'number',
                        orderable: false,
                        defaultContent: ''
                    },


                    // =========================
                    // ROLE
                    // =========================
                    {
                        data: 'role',
                        orderable: false,
                        defaultContent: ''
                    },


                    // =========================
                    // STATUS
                    // =========================
                  {
    data: 'enabled',
    orderable: false,
    defaultContent: true,
    render: function (data, type, row, meta) {
        
        if (data === false) {
            return "INACTIVE";
        } else if (data === true) {
            return "ACTIVE";
        } else {
            return "UNKNOWN";
        }

    }
},


                    // =========================
                    // ACTION
                    // =========================
                    {
                        data: null,
                        orderable: false,
                        searchable: false,
                        className: 'text-center',

                        render: function (data, type, row, meta) {

                            console.log("FULL ROW:", row);

                            /*
                             * IMPORTANT:
                             * Your API MUST return one of these:
                             *
                             * user_id
                             * userId
                             * id
                             */

                            var userId =
                                row.user_id ||
                                row.userId ||
                                row.id;


                            console.log("USER ID:", userId);


                            if (!userId) {

                                return '<span class="text-danger">ID Missing</span>';

                            }


                            return (

                                '<button type="button" ' +

                                'class="btn btn-info btn-sm mr-1" ' +

                                'onclick="editUser(\'' +
                                userId +
                                '\')">' +

                                '<i class="fas fa-edit"></i>' +

                                '</button>' +


                                '<button type="button" ' +

                                'class="btn btn-light border btn-sm mr-1" ' +

                                'style="color:#000;background-color:#fff;" ' +

                                'onclick="changePassword(\'' +
                                userId +
                                '\')">' +

                                '<i class="fas fa-key"></i>' +

                                '</button>' +


                                '<button type="button" ' +

                                'class="btn btn-danger btn-sm" ' +

                                'onclick="deleteUser(\'' +
                                userId +
                                '\')">' +

                                '<i class="fas fa-trash"></i>' +

                                '</button>'

                            );

                        }

                    }

                ]

            });

        },


        error: function (xhr) {

            console.log(
                "Error loading users:",
                xhr.responseText
            );

            alert("Unable to load users.");

        }

    });

}


/* =====================================================
   VALIDATION FUNCTIONS - ADDED FOR VALIDATION
   ===================================================== */

function isValidMobileNumber(mobile) {
    if (!mobile) {
        return true;
    }
    var mobileRegex = /^[0-9]{10}$/;
    return mobileRegex.test(mobile);
}

function isValidPassword(password) {
    return password.length >= 6;
}

function isValidEmail(email) {
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function isValidUsername(username) {
    var usernameRegex = /^[a-zA-Z0-9_]{3,}$/;
    return usernameRegex.test(username);
}

function isValidName(name) {
    if (!name) {
        return true;
    }
    var nameRegex = /^[a-zA-Z\s]{3,}$/;
    return nameRegex.test(name);
}


/* =====================================================
   EDIT USER - NO API CALL NEEDED
   ===================================================== */

function editUser(userId) {

    console.log("EDIT USER ID:", userId);

    if (!userId ||
        userId === "undefined" ||
        userId === "null") {

        alert("User ID is missing.");
        return;

    }

    selectedUserId = userId;

    // Get the row data from DataTable without making API call
    var rowData = userTable.rows().data();
    var userData = null;

    for (var i = 0; i < rowData.length; i++) {
        var row = rowData[i];
        var rowUserId = row.user_id || row.userId || row.id;
        
        if (rowUserId == userId) {
            userData = row;
            break;
        }
    }

    if (userData) {
        // Populate modal with existing data from table
        $("#editUsername").val(userData.username || "");
        $("#editName").val(userData.name || "");
        $("#editEmailId").val(userData.emailId || "");
        $("#editNumber").val(userData.number || "");
        $("#editRole").val(userData.role || "");
        // Handle boolean enabled value
        $("#editStatus").val(userData.enabled === true || userData.enabled === 1 ? "ACTIVE" : "INACTIVE");

        // editing an existing user: password isn't set here, hide the field
        $("#editPasswordGroup").hide();
        $("#editPassword").val("");

        // Change modal title
        $("#editUserModalLabel").text("Edit User");

        // Change button text
        $(".modal-footer .btn-primary").html('<i class="fas fa-save"></i> Update User');

        // Open modal
        $("#editUserModal").modal("show");

    } else {

        alert("Unable to find user data. Please refresh and try again.");

    }

}


/* =====================================================
   ADD NEW USER
   ===================================================== */

function addNewUser() {

    selectedUserId = null;

    // Clear modal fields
    $("#editUsername").val("");
    $("#editName").val("");
    $("#editEmailId").val("");
    $("#editNumber").val("");
    $("#editRole").val("");
    $("#editStatus").val("ACTIVE");

    // creating a new user: password is required
    $("#editPasswordGroup").show();
    $("#editPassword").val("");

    // Change modal title
    $("#editUserModalLabel").text("Add New User");

    // Change button text
    $(".modal-footer .btn-primary").html('<i class="fas fa-plus"></i> Add User');

    // Open modal
    $("#editUserModal").modal("show");

}


/* =====================================================
   UPDATE OR ADD USER - HANDLES BOTH OPERATIONS
   ===================================================== */

function updateUser() {

    var username = $("#editUsername").val();
    var emailId = $("#editEmailId").val();
    var role = $("#editRole").val();
    var statusValue = $("#editStatus").val();
    var number = $("#editNumber").val();

    if (!username || username === null) {
        alert("Username is required.");
        return;
    }

    username = username.trim();

    // Validate username format
    if (!isValidUsername(username)) {
        alert("Username must be at least 3 characters long and contain only letters, numbers, and underscores.");
        return;
    }

    var name = $("#editName").val();
    if (name) {
        name = name.trim();
    }

    // Validate name format
    if (!isValidName(name)) {
        alert("Name must be at least 3 characters long and contain only letters and spaces.");
        return;
    }

    if (!emailId || emailId === null) {
        alert("Email is required.");
        return;
    }

    emailId = emailId.trim();

    // Validate email format
    if (!isValidEmail(emailId)) {
        alert("Please enter a valid email address.");
        return;
    }

    if (number) {
        number = number.trim();
    }

    // Validate mobile number - must be 10 digits
    if (number && !isValidMobileNumber(number)) {
        alert("Mobile number must be exactly 10 digits.");
        return;
    }

    if (!role || role === null) {
        alert("Role is required.");
        return;
    }

    role = role.trim();

    if (!statusValue || statusValue === null) {
        alert("Status is required.");
        return;
    }

    statusValue = statusValue.trim();
    var enabled = statusValue === "ACTIVE" ? 1 : 0;

    var userData = {
        username: username,
        name: name || "",
        emailId: emailId,
        number: number || "",
        role: role,
        enabled: enabled
    };

    console.log("USER DATA:", userData);

    // If selectedUserId is null, it's a new user (POST), otherwise update (PUT)
    if (selectedUserId) {

        // UPDATE existing user — sent as query string params (matches tested working URL)
        var updateParams = $.param({
            username: username,
            name: userData.name,
            emailId: emailId,
            number: userData.number,
            role: role,
            enabled: enabled
        });

        $.ajax({

            url: api.USER_MANAGEMENT_UPDATE +
                 "/" +
                 encodeURIComponent(selectedUserId) +
                 "?" + updateParams,

            type: "PUT",

            success: function (response) {

                console.log("UPDATE RESPONSE:", response);

                alert("User updated successfully.");

                $("#editUserModal").modal("hide");

                selectedUserId = null;

                loadUsers();

            },

            error: function (xhr) {

                console.log("UPDATE ERROR:", xhr.status);
                console.log("UPDATE RESPONSE:", xhr.responseText);

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

        // Validate password length - minimum 6 characters
        if (!isValidPassword(password)) {
            alert("Password must be at least 6 characters long.");
            return;
        }

        // ADD new user — sent as query string params (matches tested working URL)
        var createParams = $.param({
            user_name: username,
            password: password,
            role: role,
            number: userData.number,
            emailId: emailId,
            name: userData.name,
            enabled: enabled
        });

        $.ajax({

            url: api.USER_MANAGEMENT_UPDATE + "?" + createParams,

            type: "POST",

            success: function (response) {

                console.log("ADD RESPONSE:", response);

                alert("User added successfully.");

                $("#editUserModal").modal("hide");

                selectedUserId = null;

                loadUsers();

            },

            error: function (xhr) {

                console.log("ADD ERROR:", xhr.status);
                console.log("ADD RESPONSE:", xhr.responseText);

                if (xhr.status === 409) {

                    alert(xhr.responseText);

                } else {

                    alert("Unable to add user. Status: " + xhr.status);

                }

            }

        });

    }

}


/* =====================================================
   CHANGE PASSWORD
   ===================================================== */

function changePassword(userId) {

    console.log("CHANGE PASSWORD USER ID:", userId);

    if (!userId ||
        userId === "undefined" ||
        userId === "null") {

        alert("User ID is missing.");
        return;

    }

    selectedUserId = userId;

    // Get the row data from DataTable
    var rowData = userTable.rows().data();
    var userData = null;

    for (var i = 0; i < rowData.length; i++) {
        var row = rowData[i];
        var rowUserId = row.user_id || row.userId || row.id;
        
        if (rowUserId == userId) {
            userData = row;
            break;
        }
    }

    if (userData) {
        // Set username in password change modal
        $("#changePassUsername").val(userData.username || "");
        
        // Clear password fields
        $("#changePassNewPassword").val("");
        $("#changePassConfirmPassword").val("");

        // Open password change modal
        $("#changePasswordModal").modal("show");

    } else {

        alert("Unable to find user data. Please refresh and try again.");

    }

}


/* =====================================================
   UPDATE PASSWORD
   ===================================================== */

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

    console.log("CHANGE PASSWORD FOR USER:", selectedUserId);

    // Password is sent as a query param (matches tested working URL), not a JSON body
    $.ajax({

        url: api.USER_MANAGEMENT_CHANGE_PASSWORD +
             "/" +
             encodeURIComponent(selectedUserId) +
             "/password?password=" +
             encodeURIComponent(newPassword),

        type: "PUT",

        success: function (response) {

            console.log("PASSWORD CHANGE RESPONSE:", response);

            alert("Password changed successfully.");

            $("#changePasswordModal").modal("hide");

            selectedUserId = null;

            loadUsers();

        },

        error: function (xhr) {

            console.log("PASSWORD CHANGE ERROR:", xhr.status);
            console.log("PASSWORD CHANGE RESPONSE:", xhr.responseText);

            alert("Unable to change password. Status: " + xhr.status);

        }

    });

}


/* =====================================================
   DELETE USER
   ===================================================== */

function deleteUser(userId) {

    if (!userId ||
        userId === "undefined" ||
        userId === "null") {

        alert("User ID is missing.");
        return;

    }

    if (!confirm(
        "Are you sure you want to delete this user?"
    )) {

        return;

    }

    $.ajax({

        url: api.USER_MANAGEMENT_DELETE +
             "/" +
             encodeURIComponent(userId),

        type: 'DELETE',

        success: function () {

            alert("User deleted successfully.");

            loadUsers();

        },

        error: function (xhr) {

            console.log("Delete error:", xhr.responseText);

            alert("Unable to delete user. Status: " + xhr.status);

        }

    });

}