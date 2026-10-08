
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
 
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>


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
</script>
<style type="text/css">
.hideTd{
  display:none !important;
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
			<div class="card-body" style="padding-top: 10px;">
				<div class="button-div-style" align="right" style="padding-bottom: 10px;">
					<button type="button" class="btn btn-primary btn-sm" id="addCompanyAssetBtn">Add Company Asset</button>
				</div>
				<table id="companyAssetList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
							<th width="10%">Model No</th>
							<th width="3%">Sl.No</th>
							<th width="8%">Custodian</th>
							<th width="25%">Features</th>
							<th width="10%">Brand</th>
							<th width="10%">Site</th>
							<th width="7%">Return Date</th>
							<th width="10%">Warranty</th>
							<th width="10%">Value</th>
							<th width="7%">Date & TimeStamp</th>
							<th width="7%">DC No.</th>
						</tr>
					</thead>
					<tbody style="width: 100%;">
					</tbody>
				</table>
				</div>


		</div>
		</div>

		<div class="modal fade" tabindex="-1" role="dialog" id="companyAssetModal">
			<div class="modal-dialog" role="document">
				<div class="modal-content" style="width:900px">
					<div class="modal-header custom-box-header-modal">
						<h5 class="modal-title" id="modelId">Company Asset Details</h5>
						<button type="button" class="close buttonDismiss" data-dismiss="modal"
							aria-label="Close" id="itempriceclose" style="outline: none;">
							<span aria-hidden="true">&times;</span>
						</button>
					</div>
					<form id="companyAssetForm" method="POST" action="${pageContext.request.contextPath}/add/companyAssets">
					<input type="hidden" name="dcNumber" id="dcNumberHidden"/>
					<input type="hidden" name="returnDate" id="returnDateHidden"/>
					<input type="hidden" name="date" id="dateHidden"/>
					<div class="modal-body no-padding">
					<div class="row" style="padding-top: 20px;">
						<div class="col-md-2" id="modelDiv">
							<span class="lbl-biiling-popup"> Model No.</span>
							<select class="form-control PositionofTextbox select2" id="modelDropdown" name="model" style="padding: 0;" required>
								<option value="">Select Model:</option>
							</select>
						</div>
						<div class="col-md-2" id="slNoDiv">
							<span class="lbl-biiling-popup"> Sl.No </span>
							<input type="text" name="slNo" id="slNo" class="form-control PositionofTextbox" required/>
						</div>
						<div class="col-md-2" id="custodianDiv">
							<span class="lbl-biiling-popup">Custodian : </span>
							<select class="form-control PositionofTextbox select2 custodianDropdown"
								id="custodianDropdown" name="custodian" style="padding: 0;" required>
								<option value="">Select Custodian:</option>
							</select>
						</div>
						<div class="col-md-2" id="featuresDiv">
							<span class="lbl-biiling-popup">Features : </span>
							<input type="text" name="features" id="features" class="form-control PositionofTextbox" required/>
						</div>
						<div class="col-md-2" id="brandDiv">
							<span class="lbl-biiling-popup"> Brand : </span>
							<input type="text" name="brand" id="brand" class="form-control PositionofTextbox" required/>
						</div>
						<div class="col-md-2" id="siteDiv">
							<span class="lbl-biiling-popup"> Site : </span>
							<input type="text" name="site" id="site" class="form-control PositionofTextbox" required/>
						</div>
						<div class="col-md-2" id="returnDateDiv">
							<span class="lbl-biiling-popup"> Return Date : </span>
							<input type="text" id="returnDate" class="form-control PositionofTextbox" required/>
						</div>
						<div class="col-md-2" id="warrantyDiv">
							<span class="lbl-biiling-popup">Warranty : </span>
							<input type="text" name="warranty" id="warranty" class="form-control PositionofTextbox" required/>
						</div>
						<div class="col-md-2" id="valueDiv">
							<span class="lbl-biiling-popup">Value : </span>
							<input type="text" name="value" id="value" class="form-control PositionofTextbox" required/>
						</div>
						<div class="col-md-3" id="dateAndTimeStampDiv">
							<span class="lbl-biiling-popup"> Date & TimeStamp : </span>
							<input type="text" id="dateAndTimeStamp" class="form-control PositionofTextbox" required/>
						</div>
					</div>
					</div>
					<div class="button-div-style" align="center">
						<button type="button" class="btn btn-primary btn-sm" id="saveCompanyAssetBtn">Save</button>
						<button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
					</form>
					<div style="margin: 20px 20px 20px 20px;"></div>
				</div>
			</div>
		</div>

		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->
	<!-- ./box-body -->
	
</body>

</html>