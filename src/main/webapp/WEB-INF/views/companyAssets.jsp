
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<security:authorize access="hasAuthority('COMPANY_ASSETS_EDIT')" var="canEditCompanyAssets" />
<security:authorize access="hasAuthority('COMPANY_ASSETS_DELETE')" var="canDeleteCompanyAssets" />


<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />


<!-- <link rel="stylesheet" href="resources/css/salesOrder.css"> -->
<link rel="stylesheet"
	href="<c:url value="/resources/css/salesOrder.css" />">

<!--  <script src="<c:url value="/resources/js/salesOrder.js" />"></script> -->
<script src="<c:url value="/resources/js/companyAsset.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>


<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<script type="text/javascript">
var companyAssetList=${companyAssetList};
var role = ${role};
var user = ${user};
var canEditCompanyAssets = ${canEditCompanyAssets};
var canDeleteCompanyAssets = ${canDeleteCompanyAssets};
</script>
<style type="text/css">
.hideTd{
  display:none !important;
}

/* ---- Company Assets toolbar + table polish ---- */
#companyAssetDiv .card-body{
	padding: 16px 20px 20px;
}
#companyAssetList_wrapper{
	margin: 0;
}
#companyAssetList_wrapper > .row:first-child{
	align-items: center;
	margin-bottom: 14px;
}
#companyAssetList_wrapper .dataTables_length{
	display: flex;
	align-items: center;
}
#companyAssetList_wrapper .dataTables_length select{
	height: 34px;
	padding: 2px 8px;
	margin: 0 6px;
}
#companyAssetList_wrapper .dataTables_filter label{
	display: flex;
	align-items: center;
	gap: 8px;
	margin: 0;
	white-space: nowrap;
}
#companyAssetList_wrapper .dataTables_filter input{
	height: 34px;
	margin-left: 0;
}
#companyAssetList_wrapper .button-div-style{
	display: flex;
	align-items: center;
}
@media (max-width: 767.98px){
	#companyAssetList_wrapper .dataTables_filter{
		flex-wrap: wrap;
		justify-content: flex-start !important;
		gap: 10px;
		margin-top: 10px;
	}
	#companyAssetList_wrapper .button-div-style{
		margin-right: 0 !important;
	}
}

#companyAssetList{
	table-layout: fixed;
}
#companyAssetList thead th,
#companyAssetList tbody td{
	vertical-align: middle;
}
#companyAssetList tbody td{
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
#companyAssetList thead tr:nth-child(2) th{
	padding: 4px 6px;
	background: #fff;
}
#companyAssetList thead tr:nth-child(2) input{
	box-sizing: border-box;
	width: 100%;
	height: 30px;
	padding: 2px 8px;
	font-size: 12px;
	border: 1px solid #ced4da;
	border-radius: 4px;
}
#companyAssetList_wrapper > .row:last-child{
	align-items: center;
	margin-top: 14px;
}
#companyAssetList_wrapper .dataTables_info{
	padding-top: 0;
}
#companyAssetList_wrapper .dataTables_paginate{
	padding-top: 0;
}

#companyAssetList tbody tr{
	cursor: pointer;
}
#companyAssetList tbody tr:hover{
	background-color: #f1f5fb;
}

/* ---- Company Asset modals (Edit + read-only Details) ---- */
.ca-styled-modal .modal-dialog{
	max-width: 1000px;
	width: 95%;
	margin: 1.75rem auto;
}
.ca-styled-modal .modal-content{
	border-radius: 6px;
	border: none;
	box-shadow: 0 6px 24px rgba(0,0,0,.12);
}
.ca-styled-modal .modal-header{
	background: #f8f9fb;
	border-bottom: 1px solid #e3e6ea;
	padding: 14px 24px;
}
.ca-styled-modal .modal-title{
	font-size: 16px;
	font-weight: 600;
}
.ca-styled-modal .modal-body{
	padding: 24px;
	max-height: 70vh;
	overflow-y: auto;
}
.ca-styled-modal .ca-section + .ca-section{
	margin-top: 28px;
}
.ca-styled-modal .ca-section-header{
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 16px;
	padding-bottom: 8px;
	border-bottom: 2px solid #e9ecef;
}
.ca-styled-modal .ca-section-header h6{
	margin: 0;
	font-size: 13px;
	font-weight: 700;
	text-transform: uppercase;
	letter-spacing: .04em;
	color: #495057;
}
.ca-styled-modal .ca-section-header small{
	color: #8a94a3;
	font-weight: 400;
	text-transform: none;
	letter-spacing: normal;
	margin-left: 8px;
}
.ca-styled-modal .ca-field{
	margin-bottom: 18px;
}
.ca-styled-modal .ca-field label{
	display: block;
	font-size: 12px;
	font-weight: 600;
	color: #495057;
	margin-bottom: 6px;
}
.ca-styled-modal .ca-field .form-control{
	height: 38px;
	font-size: 13px;
	border-radius: 4px;
	border: 1px solid #ced4da;
}
.ca-styled-modal .ca-field .form-control:focus{
	border-color: #80bdff;
	box-shadow: 0 0 0 .15rem rgba(0,123,255,.15);
}
.ca-styled-modal .row.ca-row{
	margin-left: -10px;
	margin-right: -10px;
}
.ca-styled-modal .row.ca-row > [class*="col-"]{
	padding-left: 10px;
	padding-right: 10px;
}

/* Assignments table (Edit modal) */
.ca-styled-modal .ca-assignments-wrap{
	border: 1px solid #e3e6ea;
	border-radius: 4px;
	overflow-x: auto;
}
.ca-styled-modal .ca-assignments-wrap table{
	width: 100%;
	min-width: 760px;
	table-layout: fixed;
	margin-bottom: 0;
	font-size: 12.5px;
}
.ca-styled-modal .ca-assignments-wrap thead th{
	background: #f8f9fb;
	font-weight: 600;
	color: #495057;
	white-space: nowrap;
	vertical-align: middle;
	border-bottom-width: 1px;
}
.ca-styled-modal .ca-assignments-wrap td{
	vertical-align: middle;
	padding: 8px;
}
.ca-styled-modal .ca-assignments-wrap .form-control{
	height: 34px;
	font-size: 12.5px;
	padding: 4px 8px;
}
.ca-styled-modal .ca-assignments-wrap .removeAssignmentRowBtn{
	width: 28px;
	height: 28px;
	padding: 0;
	line-height: 1;
	border-radius: 4px;
}
.ca-styled-modal .ca-empty-row td{
	text-align: center;
	color: #8a94a3;
	padding: 16px 8px;
	font-style: italic;
}
.ca-styled-modal .modal-footer{
	padding: 14px 24px;
	border-top: 1px solid #e3e6ea;
	background: #f8f9fb;
	justify-content: flex-end;
	gap: 10px;
}
.ca-styled-modal .modal-footer .btn{
	min-width: 100px;
}

@media (max-width: 991.98px){
	.ca-styled-modal .modal-dialog{ max-width: 96%; }
}
@media (max-width: 767.98px){
	.ca-styled-modal .col-lg-2,
	.ca-styled-modal .col-lg-3,
	.ca-styled-modal .col-lg-4{
		flex: 0 0 50%;
		max-width: 50%;
	}
}
@media (max-width: 575.98px){
	.ca-styled-modal .modal-body{ padding: 16px; }
	.ca-styled-modal .col-lg-2,
	.ca-styled-modal .col-lg-3,
	.ca-styled-modal .col-lg-4{
		flex: 0 0 100%;
		max-width: 100%;
	}
}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini layout-fixed">
	<div class="wrapper">
		 <tiles:insertAttribute name="header" />

		<tiles:insertAttribute name="sideMenu" />


		<div class="content-wrapper">

		<div id="companyAssetDiv" class="card">
			<div class="card-body">
				<div class="button-div-style" align="right">
					<security:authorize access="hasAuthority('COMPANY_ASSETS_EDIT')">
					<button type="button" class="btn btn-primary btn-sm" id="addCompanyAssetBtn">Add Company Asset</button>
					</security:authorize>
				</div>
				<table id="companyAssetList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
							<th width="8%">Sl.No</th>
							<th width="18%">Model No</th>
							<th width="26%">Description</th>
							<th width="14%">Asset Type</th>
							<th width="16%">Site</th>
							<th width="18%">Created On</th>
						</tr>
					</thead>
					<tbody style="width: 100%;">
					</tbody>
				</table>
				</div>
		</div>

		<div class="modal fade ca-styled-modal" tabindex="-1" role="dialog" id="companyAssetModal">
			<div class="modal-dialog" role="document">
				<div class="modal-content">
					<div class="modal-header">
						<h5 class="modal-title" id="modelId">Company Asset Details</h5>
						<button type="button" class="close buttonDismiss" data-dismiss="modal"
							aria-label="Close" id="itempriceclose" style="outline: none;">
							<span aria-hidden="true">&times;</span>
						</button>
					</div>
					<form id="companyAssetForm" method="POST" action="${pageContext.request.contextPath}/add/companyAssets">
					<input type="hidden" name="id" id="companyAssetId" value="0"/>
					<div class="modal-body">

					<div class="ca-section">
						<div class="ca-section-header">
							<h6>Asset Details</h6>
						</div>
						<div class="row ca-row">
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="modelDiv">
								<label for="model">Model No.</label>
								<input type="text" name="model" id="model" class="form-control" list="modelCandidates" required/>
								<datalist id="modelCandidates"></datalist>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="featuresDiv">
								<label for="features">Description</label>
								<input type="text" name="features" id="features" class="form-control" required/>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="brandDiv">
								<label for="brand">Brand</label>
								<input type="text" name="brand" id="brand" class="form-control" required/>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="siteDiv">
								<label for="site">Site</label>
								<input type="text" name="site" id="site" class="form-control" required/>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="warrantyDiv">
								<label for="warranty">Warranty</label>
								<input type="text" name="warranty" id="warranty" class="form-control" required/>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="assetTypeDiv">
								<label for="assetType">Asset Type</label>
								<div style="display:flex; gap:6px; align-items:center;">
									<select name="assetType" id="assetType" class="form-control" required>
										<option value="">Select Asset Type</option>
									</select>
									<i class="fas fa-plus-square" id="addAssetTypeBtn" title="Add Asset Type"
										style="cursor:pointer; font-size:20px; color:#007bff;"></i>
								</div>
							</div>
						</div>
					</div>

					<div class="ca-section" id="laptopDetailsSection" style="display:none;">
						<div class="ca-section-header">
							<h6>Laptop Details</h6>
						</div>
						<div class="row ca-row">
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="assetIdDiv">
								<label for="assetId">Asset ID</label>
								<input type="text" name="assetId" id="assetId" class="form-control"/>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="serialNoDiv">
								<label for="serialNo">Serial No</label>
								<input type="text" name="serialNo" id="serialNo" class="form-control"/>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="dateOfPurchaseDiv">
								<label for="dateOfPurchase">Date of Purchase</label>
								<input type="date" name="dateOfPurchase" id="dateOfPurchase" class="form-control"/>
							</div>
							<div class="col-lg-2 col-md-4 col-sm-6 col-12 ca-field" id="windowsVersionDiv">
								<label for="windowsVersion">Windows</label>
								<input type="text" name="windowsVersion" id="windowsVersion" class="form-control"/>
							</div>
						</div>
					</div>

					<div class="ca-section">
						<div class="ca-section-header">
							<h6>Assignments</h6>
							<button type="button" class="btn btn-primary btn-sm" id="addAssignmentRowBtn">+ Add Assignment</button>
						</div>
						<div class="ca-assignments-wrap">
							<table id="assignmentRowsTable">
								<colgroup>
									<col style="width:17%">
									<col style="width:12%">
									<col style="width:12%">
									<col style="width:10%">
									<col style="width:10%">
									<col style="width:14%">
									<col style="width:17%">
									<col style="width:8%">
								</colgroup>
								<thead>
									<tr>
										<th>Employee</th>
										<th>Location</th>
										<th>Issued By</th>
										<th>Date Issued</th>
										<th>Date Returned</th>
										<th>Returned To</th>
										<th>Remarks</th>
										<th class="text-center">Action</th>
									</tr>
								</thead>
								<tbody id="assignmentRowsBody"></tbody>
							</table>
						</div>
					</div>

					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-secondary btn-sm buttonDismiss" data-dismiss="modal">Close</button>
						<button type="button" class="btn btn-primary btn-sm" id="saveCompanyAssetBtn">Save</button>
					</div>
					</form>
				</div>
			</div>
		</div>
	</div>
	<!-- ./wrapper -->
	<!-- ./box-body -->

	<div class="modal fade" tabindex="-1" role="dialog" id="assetTypeModal">
		<div class="modal-dialog" role="document">
			<div class="modal-content">
				<div class="modal-header">
					<h5 class="modal-title">Add Asset Type</h5>
					<button type="button" class="close" data-dismiss="modal" aria-label="Close">
						<span aria-hidden="true">&times;</span>
					</button>
				</div>
				<div class="modal-body">
					<div class="form-group">
						<label for="assetTypeNameInput">Asset Type Name</label>
						<input type="text" id="assetTypeNameInput" class="form-control"/>
					</div>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary btn-sm" data-dismiss="modal">Cancel</button>
					<button type="button" class="btn btn-primary btn-sm" id="saveAssetTypeBtn">Save</button>
				</div>
			</div>
		</div>
	</div>
</body>

</html>
