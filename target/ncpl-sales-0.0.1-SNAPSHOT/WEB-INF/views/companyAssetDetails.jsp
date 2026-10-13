
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<security:authorize access="hasAuthority('COMPANY_ASSETS_EDIT')" var="canEditCompanyAssets" />

<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />
<script type="text/javascript">
var companyAsset = ${companyAsset};
var role = ${role};
var user = ${user};
var canEditCompanyAssets = ${canEditCompanyAssets};
</script>
<style type="text/css">
.ca-page{ max-width: 1280px; margin: 0 auto; padding: 20px 24px 32px; }

.ca-card{
	background: #fff; border: 1px solid #e3e6ea; border-radius: 8px;
	margin-bottom: 24px; box-shadow: 0 1px 2px rgba(0,0,0,.04);
}
.ca-card-header{
	display: flex; align-items: center; gap: 10px;
	padding: 14px 24px; border-bottom: 1px solid #e9ecef; background: #f8f9fb;
	border-radius: 8px 8px 0 0;
}
.ca-card-header i{ color: #007bff; font-size: 14px; }
.ca-card-header h6{
	margin: 0; font-size: 13px; font-weight: 700; text-transform: uppercase;
	letter-spacing: .04em; color: #495057;
}
.ca-card-body{ padding: 24px; }

.ca-grid{
	display: grid;
	column-gap: 24px;
	row-gap: 20px;
}
.ca-grid-6{ grid-template-columns: repeat(6, minmax(0, 1fr)); }
.ca-grid-4{ grid-template-columns: repeat(4, minmax(0, 1fr)); }
.ca-field{ min-width: 0; }
.ca-field label{
	display: block; font-size: 11.5px; font-weight: 700; text-transform: uppercase;
	letter-spacing: .03em; color: #8a94a3; margin-bottom: 6px;
}
.ca-detail-value{ font-size: 14px; color: #212529; min-height: 20px; line-height: 1.4; word-break: break-word; }
.ca-detail-value.is-empty{ color: #c1c8d0; }

.ca-table-wrap{ border-top: 1px solid #e9ecef; overflow-x: auto; }
.ca-table{ width: 100%; min-width: 760px; border-collapse: collapse; font-size: 13px; }
.ca-table thead th{
	background: #f8f9fb; color: #495057; font-weight: 700; text-transform: uppercase;
	font-size: 11px; letter-spacing: .03em; padding: 12px 16px;
	border-bottom: 2px solid #e9ecef; text-align: left; white-space: nowrap;
}
.ca-table td{ padding: 12px 16px; border-bottom: 1px solid #f1f3f5; vertical-align: middle; }
.ca-table tbody tr:last-child td{ border-bottom: none; }
.ca-table tbody tr:hover{ background: #fafbfc; }
.ca-status-badge{
	display: inline-block; padding: 3px 10px; border-radius: 12px;
	font-size: 11px; font-weight: 700; letter-spacing: .02em; white-space: nowrap;
}
.ca-status-badge.current{ background: #d4edda; color: #155724; }
.ca-status-badge.returned{ background: #e2e3e5; color: #383d41; margin-left: 6px; }
.ca-status-badge.instock{ background: #cfe2ff; color: #084298; margin-left: 6px; }
.ca-empty-row td{ text-align: center; color: #8a94a3; padding: 24px 16px; font-style: italic; }
.ca-card-header .ca-add-assignee-btn{ margin-left: auto; }

@media (max-width: 992px){
	.ca-grid-6{ grid-template-columns: repeat(3, minmax(0, 1fr)); }
	.ca-grid-4{ grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 576px){
	.ca-grid-6, .ca-grid-4{ grid-template-columns: repeat(2, minmax(0, 1fr)); }
	.ca-card-body{ padding: 18px; }
}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini layout-fixed">
	<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
			<div class="ca-page">

				<div class="ca-card">
					<div class="ca-card-header"><i class="fas fa-box"></i><h6>Asset Details</h6><span id="assetStockStatusBadge" style="display:none;" class="ca-status-badge instock">In Stock</span></div>
					<div class="ca-card-body">
						<div class="ca-grid ca-grid-6">
							<div class="ca-field">
								<label>Sl.No</label>
								<div class="ca-detail-value" id="detailSlNo"></div>
							</div>
							<div class="ca-field">
								<label>Model No.</label>
								<div class="ca-detail-value" id="detailModel"></div>
							</div>
							<div class="ca-field">
								<label>Description</label>
								<div class="ca-detail-value" id="detailFeatures"></div>
							</div>
							<div class="ca-field">
								<label>Brand</label>
								<div class="ca-detail-value" id="detailBrand"></div>
							</div>
							<div class="ca-field">
								<label>Site</label>
								<div class="ca-detail-value" id="detailSite"></div>
							</div>
							<div class="ca-field">
								<label>Warranty</label>
								<div class="ca-detail-value" id="detailWarranty"></div>
							</div>
						</div>
					</div>
				</div>

				<div class="ca-card" id="laptopDetailsCard" style="display:none;">
					<div class="ca-card-header"><i class="fas fa-laptop"></i><h6>Laptop Details</h6></div>
					<div class="ca-card-body">
						<div class="ca-grid ca-grid-4">
							<div class="ca-field">
								<label>Asset ID</label>
								<div class="ca-detail-value" id="detailAssetId"></div>
							</div>
							<div class="ca-field">
								<label>Serial No</label>
								<div class="ca-detail-value" id="detailSerialNo"></div>
							</div>
							<div class="ca-field">
								<label>Date of Purchase</label>
								<div class="ca-detail-value" id="detailDateOfPurchase"></div>
							</div>
							<div class="ca-field">
								<label>Windows</label>
								<div class="ca-detail-value" id="detailWindowsVersion"></div>
							</div>
						</div>
					</div>
				</div>

				<div class="ca-card">
					<div class="ca-card-header">
						<i class="fas fa-history"></i><h6>Assignment History</h6>
						<security:authorize access="hasAuthority('COMPANY_ASSETS_EDIT')">
						<button type="button" class="btn btn-primary btn-sm ca-add-assignee-btn" id="addAssigneeBtn">+ Add Assignee</button>
						</security:authorize>
					</div>
					<div class="ca-table-wrap">
						<table class="ca-table">
							<colgroup>
								<col style="width:17%"><col style="width:13%"><col style="width:13%">
								<col style="width:11%"><col style="width:13%"><col style="width:14%"><col style="width:19%">
							</colgroup>
							<thead>
								<tr>
									<th>Employee</th><th>Location</th><th>Issued By</th>
									<th>Date Issued</th><th>Date Returned</th><th>Returned To</th><th>Remarks</th>
								</tr>
							</thead>
							<tbody id="detailsAssignmentBody"></tbody>
						</table>
					</div>
				</div>

			</div>
		</div>

		<tiles:insertAttribute name="footer" />
	</div>

	<div class="modal fade" tabindex="-1" role="dialog" id="addAssigneeModal">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title">Add Assignee</h5>
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
				</div>
				<div class="modal-body">
					<div id="currentAssigneesSection"></div>
					<div class="form-group" id="assigneeModeGroup" style="display:none;">
						<label>This Asset Is</label>
						<div>
							<div class="custom-control custom-radio custom-control-inline">
								<input type="radio" id="assigneeModeAdd" name="assigneeMode" class="custom-control-input" value="add" checked>
								<label class="custom-control-label" for="assigneeModeAdd">Add Assignee</label>
							</div>
							<div class="custom-control custom-radio custom-control-inline">
								<input type="radio" id="assigneeModeInStock" name="assigneeMode" class="custom-control-input" value="instock">
								<label class="custom-control-label" for="assigneeModeInStock">In Stock</label>
							</div>
						</div>
					</div>
					<div id="newAssigneeFields">
						<div class="form-group">
							<label for="assigneeEmployee">Employee</label>
							<select id="assigneeEmployee" class="form-control"></select>
						</div>
						<div class="form-group">
							<label for="assigneeLocation">Location</label>
							<input type="text" id="assigneeLocation" class="form-control"/>
						</div>
						<div class="form-group">
							<label for="assigneeIssuedBy">Issued By</label>
							<input type="text" id="assigneeIssuedBy" class="form-control"/>
						</div>
						<div class="form-group">
							<label for="assigneeDateIssued">Date Issued</label>
							<input type="date" id="assigneeDateIssued" class="form-control"/>
						</div>
						<div class="form-group">
							<label for="assigneeRemarks">Remarks</label>
							<input type="text" id="assigneeRemarks" class="form-control"/>
						</div>
					</div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary btn-sm" data-dismiss="modal">Cancel</button>
					<button type="button" class="btn btn-primary btn-sm" id="saveAssigneeBtn">Save</button>
				</div>
			</div>
		</div>
	</div>
</body>
</html>

<script type="text/javascript">
var CA_DASH = String.fromCharCode(8212);

function detailText(value){
	return (value != null && value !== "") ? value : "";
}

/** Displays yyyy-MM-dd (the server's JSON date format) as dd-mm-yyyy. */
function toDDMMYYYY(value){
	if(!value){
		return value;
	}
	var parts = String(value).split("-");
	if(parts.length !== 3){
		return value;
	}
	return parts[2] + "-" + parts[1] + "-" + parts[0];
}

$(document).ready(function(){
	var asset = companyAsset;

	$("#detailModel").text(detailText(asset.model));
	$("#detailSlNo").text(detailText(asset.slNo));
	$("#detailFeatures").text(detailText(asset.features));
	$("#detailBrand").text(detailText(asset.brand));
	$("#detailSite").text(detailText(asset.site));
	$("#detailWarranty").text(detailText(asset.warranty));
	$("#detailAssetId").text(detailText(asset.assetId));
	$("#detailSerialNo").text(detailText(asset.serialNo));
	$("#detailDateOfPurchase").text(detailText(toDDMMYYYY(asset.dateOfPurchase)));
	$("#detailWindowsVersion").text(detailText(asset.windowsVersion));

	$(".ca-detail-value").each(function(){
		if($.trim($(this).text()) === ""){
			$(this).text(CA_DASH).addClass("is-empty");
		}
	});

	// No open assignment left = the asset isn't with anyone right now.
	var hasOpenAssignment = (asset.assignments || []).some(function(a){ return !a.dateReturned; });
	$("#assetStockStatusBadge").toggle(!hasOpenAssignment);

	/** Laptop Details only applies to the Asset Type with id=1 (Laptop). */
	$.ajax({
		type: 'GET',
		url: api.ASSET_TYPE_LIST,
		dataType: 'json',
		success: function(response){
			var match = (response || []).find(function(t){ return t.name === asset.assetType; });
			if(match && String(match.id) === "1"){
				$("#laptopDetailsCard").show();
			}
		},
		error: function(e){
			console.log(e);
		}
	});

	var rows = "";
	var assignmentsOldestFirst = (asset.assignments || []).slice().reverse();
	$.each(assignmentsOldestFirst, function(index, a){
		var isCurrent = !a.dateReturned;
		var wentToStock = !isCurrent && $.trim(a.returnedTo || "").toLowerCase() === "in stock";
		rows += "<tr>"
			+ "<td>" + (a.employee ? a.employee.name : CA_DASH) + "</td>"
			+ "<td>" + (a.location || CA_DASH) + "</td>"
			+ "<td>" + (a.issuedBy || CA_DASH) + "</td>"
			+ "<td>" + (toDDMMYYYY(a.dateIssued) || CA_DASH) + "</td>"
			+ "<td>" + (isCurrent
				? "<span class='ca-status-badge current'>Current</span>"
				: (toDDMMYYYY(a.dateReturned) + " <span class='ca-status-badge "
					+ (wentToStock ? "instock'>In Stock" : "returned'>Returned") + "</span>")) + "</td>"
			+ "<td>" + (a.returnedTo || CA_DASH) + "</td>"
			+ "<td>" + (a.remarks || CA_DASH) + "</td>"
			+ "</tr>";
	});
	if(!rows){
		rows = "<tr class='ca-empty-row'><td colspan='7'>No assignment history for this asset.</td></tr>";
	}
	$("#detailsAssignmentBody").html(rows);
});

var assigneeEmployeeList = null;

function loadAssigneeEmployeeList(callback){
	if(assigneeEmployeeList){
		callback();
		return;
	}
	$.ajax({
		type: 'GET',
		url: api.EMPLOYEE_LIST,
		dataType: 'json',
		success: function(response){
			assigneeEmployeeList = response || [];
			callback();
		},
		error: function(e){
			console.log(e);
			assigneeEmployeeList = [];
			callback();
		}
	});
}

$(document).on("click", "#addAssigneeBtn", function(){
	loadAssigneeEmployeeList(function(){
		var options = "<option value=''>Select Employee:</option>";
		$.each(assigneeEmployeeList, function(index, emp){
			options += "<option value='"+emp.id+"'>"+emp.name+" ("+emp.empId+")</option>";
		});
		$("#assigneeEmployee").html(options);
		$("#assigneeLocation, #assigneeIssuedBy, #assigneeDateIssued, #assigneeRemarks").val("");

		var openAssignments = $.grep(companyAsset.assignments || [], function(a){ return !a.dateReturned; });
		var section = "";
		$.each(openAssignments, function(index, a){
			var who = a.employee ? a.employee.name : "current assignee";
			section += "<div class='form-group'>"
				+ "<label>Return Date for " + who + "</label>"
				+ "<input type='date' class='form-control currentAssigneeReturnDate' data-assignment-id='" + a.id + "' required/>"
				+ "</div>"
				+ "<div class='form-group'>"
				+ "<label>Returned To (for " + who + ")</label>"
				+ "<input type='text' class='form-control currentAssigneeReturnedTo' data-assignment-id='" + a.id + "' required/>"
				+ "</div>";
		});
		$("#currentAssigneesSection").html(section);

		// Only meaningful when something is actually being returned - otherwise there's
		// nothing to choose between (you're always adding the first assignee).
		$("#assigneeModeGroup").toggle(openAssignments.length > 0);
		$("#assigneeModeAdd").prop("checked", true);
		$("#newAssigneeFields").show();

		$("#addAssigneeModal").modal('show');
	});
});

$(document).on("change", "input[name='assigneeMode']", function(){
	var isInStock = $("#assigneeModeInStock").is(":checked");
	$("#newAssigneeFields").toggle(!isInStock);
	// Tags the closed-out assignment's "Returned To" with "In Stock" so it's visible
	// afterwards in the Assignment History table instead of a blank/arbitrary value.
	$(".currentAssigneeReturnedTo").each(function(){
		if(isInStock){
			$(this).data("priorValue", $(this).val()).val("In Stock").prop("readonly", true);
		} else {
			$(this).prop("readonly", false).val($(this).data("priorValue") || "");
		}
	});
});

function addHiddenInput(form, name, value){
	form.append($("<input>").attr("type", "hidden").attr("name", name).val(value != null ? value : ""));
}

$(document).on("click", "#saveAssigneeBtn", function(){
	// "In Stock" = the asset is just being returned, not handed to anyone new.
	var isInStock = $("#assigneeModeGroup").is(":visible") && $("#assigneeModeInStock").is(":checked");

	var employeeId = "", location = "", issuedBy = "", dateIssued = "";
	if(!isInStock){
		employeeId = $("#assigneeEmployee").val();
		location = $.trim($("#assigneeLocation").val());
		issuedBy = $.trim($("#assigneeIssuedBy").val());
		dateIssued = $("#assigneeDateIssued").val();

		if(!employeeId || !location || !issuedBy || !dateIssued){
			alert("Employee, Location, Issued By and Date Issued are required.");
			return;
		}
	}

	var missingReturnInfo = false;
	$(".currentAssigneeReturnDate, .currentAssigneeReturnedTo").each(function(){
		if(!$.trim($(this).val())){
			missingReturnInfo = true;
		}
	});
	if(missingReturnInfo){
		alert("Please enter the return date and who it was returned to for the current assignee(s).");
		return;
	}

	var asset = companyAsset;
	var form = $("<form>").attr("method", "POST").attr("action", contextRoot + "add/companyAssets");

	addHiddenInput(form, "id", asset.id);
	addHiddenInput(form, "model", asset.model);
	addHiddenInput(form, "slNo", asset.slNo);
	addHiddenInput(form, "features", asset.features);
	addHiddenInput(form, "brand", asset.brand);
	addHiddenInput(form, "site", asset.site);
	addHiddenInput(form, "warranty", asset.warranty);
	addHiddenInput(form, "assetType", asset.assetType);
	addHiddenInput(form, "assetId", asset.assetId);
	addHiddenInput(form, "serialNo", asset.serialNo);
	addHiddenInput(form, "dateOfPurchase", asset.dateOfPurchase);
	addHiddenInput(form, "windowsVersion", asset.windowsVersion);

	var rowIndex = 0;

	/** Close out any still-open assignment(s), using the return date entered for each in the modal. */
	$.each(asset.assignments || [], function(index, a){
		if(!a.dateReturned){
			var returnDate = $(".currentAssigneeReturnDate[data-assignment-id='"+a.id+"']").val();
			var returnedTo = $.trim($(".currentAssigneeReturnedTo[data-assignment-id='"+a.id+"']").val());
			addHiddenInput(form, "assignments["+rowIndex+"].id", a.id);
			addHiddenInput(form, "assignments["+rowIndex+"].employeeId", a.employee ? a.employee.id : "");
			addHiddenInput(form, "assignments["+rowIndex+"].location", a.location);
			addHiddenInput(form, "assignments["+rowIndex+"].issuedBy", a.issuedBy);
			addHiddenInput(form, "assignments["+rowIndex+"].dateIssued", a.dateIssued);
			addHiddenInput(form, "assignments["+rowIndex+"].dateReturned", returnDate);
			addHiddenInput(form, "assignments["+rowIndex+"].returnedTo", returnedTo);
			addHiddenInput(form, "assignments["+rowIndex+"].remarks", a.remarks);
			rowIndex++;
		}
	});

	if(!isInStock){
		addHiddenInput(form, "assignments["+rowIndex+"].id", "0");
		addHiddenInput(form, "assignments["+rowIndex+"].employeeId", employeeId);
		addHiddenInput(form, "assignments["+rowIndex+"].location", location);
		addHiddenInput(form, "assignments["+rowIndex+"].issuedBy", issuedBy);
		addHiddenInput(form, "assignments["+rowIndex+"].dateIssued", dateIssued);
		addHiddenInput(form, "assignments["+rowIndex+"].remarks", $.trim($("#assigneeRemarks").val()));
	}

	addHiddenInput(form, "returnTo", "/companyAssets/view/" + asset.id);

	$("body").append(form);
	form.submit();
});
</script>
