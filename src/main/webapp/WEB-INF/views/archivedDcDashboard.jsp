<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" isELIgnored="false"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/purchaseOrder.css" />">
<script src="<c:url value="/resources/js/dcDashboard.js" />"></script> 
<script type="text/javascript">
var dcLists =${dcLists};
var role = ${role};
var partyList = ${partyList};
var pageContext = '${pageContext.request.contextPath}';
</script>
</head>
<body>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />
		<div class="content-wrapper">
			
			<div id="salesDiv" class="card">
			<div class="card-body" style="padding-top: 10px;">
				<table id="dcList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
						 <th width="8%">Dc No.</th>
							<th width="14%">SO Number</th>
							<th width="15%">Client Name</th>
							<th width="12%">Client PO</th>
							<th width="21%">Shipping Address</th>
							<th width="10%">Date</th>
							<th width="10%">DC</th>
							<th width="3%">View</th>
							<th width="2%">Returnable</th>
							<th width="5%">Archive</th>
						</tr>
					</thead>
					<tbody style="width: 100%;">
					</tbody>
				</table>
				</div>
			</div>
			
		</div>
		<tiles:insertAttribute name="footer" />
		</div>
<!-- Tds modal starts -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="dcViewModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%;height:100%">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="dcViewModalHeader">
						<b>DC Preview</b>
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="dcViewModalTable" class="dcViewModalTable table table-bordered table-striped" style="width:100%">
					 <thead id="table-header font">
								<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<th width="45%" rowspan="2" class="thStyle">SO desc.</th>
										<th width="10%" rowspan="2" class="thStyle">SO Model No.</th>
										<th width="10%" rowspan="2" class="thStyle">Units</th>
										<th width="10%" rowspan="2" class="thStyle">Total Qty</th>
										<th width="10%" rowspan="2" class="thStyle">Delivered Qty</th>
										<th width="10%" rowspan="2" class="thStyle">Todays Qty</th>

										
									</tr>

							</thead> 
							<tbody>
							</tbody>
					</table>
				</div>
				 <div class="modal-footer">
					<div class="button-div-style" align="center">
					<button class="btn btn-sm btn-primary btn-inline" id="dcViewBtn">View</button>
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
				</div>
			
			</div>
		</div>
	</div>		
<!--dc excel model starts -->
		<div class="modal fade" tabindex="-1" role="dialog"
				id="dcExcelModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:400px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="dcExcelHeader"></h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						<input type="hidden" id="dcId" />
						 <table class="modalTableBar botHeader"
									id="dcExcelTable">
								
									<tbody>
										<tr>
											<td width="50%">Download Option : </td>
											<td><select	class="form-control PositionofTextbox select2 dcExcelDropdown" id="dcExcelDropdown" name="dcExcelDropdown" style="padding: 0;">
											<option value="">Select Option:</option>
											<option value="Original">Original</option>
											<option value="Duplicate">Duplicate</option>
											<option value="Triplicate">Triplicate</option>
											</select></td>
										</tr>
									</tbody>
								</table>
						</div>
							<div class="button-div-style" align="center">
						
								<button id="generateDcExcelBtn" 
									class="btn btn-primary btn-sm btn-inline">Download</button>

								<!-- <button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button> -->

							</div>
						<%-- </form> --%>
						<div style="margin: 20px 20px 20px 20px;">
								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>

</body>
</html>