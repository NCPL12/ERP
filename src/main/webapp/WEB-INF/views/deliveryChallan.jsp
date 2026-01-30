
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

<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/purchaseOrder.css" />">
<script type="text/javascript" src="resources/js/deliveryChallan.js"></script>
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>
<script type="text/javascript">
var salesList = ${salesOrderList};
var partyList = ${partyList};
var clientList=${clientList};
var itemList =${itemList};
var obj = '${dcObj}';
obj = obj.replace(/\&/g, "\'");
obj = obj.replace(/\&/g, "\"");
var dcObj = "";
if ('${dcObj}' != null && '${dcObj}' != "") {
	dcObj= $.parseJSON(obj);
}
</script>

<style type="text/css">

#dcTable>tbody>tr>td,  #dcTable>thead>tr>th {
    padding: 3px 2px!important;
    vertical-align: middle;
}
.CellWithComment{
  position:relative;
}

.CellComment{
  display:none;
  /* position:absolute;  */
  z-index:100;
  border:1px;
  background-color:white;
  border-style:solid;
  border-width:1px;
  border-color:black;
  padding:3px;
  color:black; 
  top:20px; 
  left:20px;
}

.CellWithComment:hover span.CellComment{
  display:block;
}
#dcTable tr:hover {
    background-color:#8080801c;
}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		<tiles:insertAttribute name="header" />

		<tiles:insertAttribute name="sideMenu" />
		<div class="content-wrapper">
			<form id="deliveryChallanForm" method="POST"
				action="${pageContext.request.contextPath}/add/delivery_challan">
				<input type="hidden" name="soNumber" id="soNumber">
				<input type="hidden" name="clientId" id="clientId">
				
				<!-- DC Comment Box -->
				<div class="card mb-3">
					<div class="card-header">
						<h5>DC Comment</h5>
					</div>
					<div class="card-body">
						<textarea class="form-control" name="dcComment" id="dcComment" rows="3" placeholder="Add comment for this DC (optional)"></textarea>
					</div>
				</div>
				
				<div id="salesDiv" class="card">
					<div class="card-body table-responsive p-0">
						<table id="dcTable" class="table table-head-fixed"
							style="width: 100%;">
							<thead id="table-header">
								<tr>
									<th width="5%">Sl.No</th>
									<th width="45%">SO Desc.</th>
									<th width="10%">SO Model No.</th>
									<th width="10%">Units</th>
									<th width="7%">Total Qty</th>
									<th width="7%">Delivered Qty</th>
									<th width="7%">Today's Qty</th>
									
									<!-- <th><i
										class="add fa fa-plus-square fa-2x text-center mx-auto"
										aria-hidden="true"></i></th> -->
								</tr>
							</thead>
							<tbody id="table-body">
								<!-- <tr id="table-row">

										<td width="5%" align="center"><input type="text" name="items[0].slNo" id="slNo0" readonly="readonly"
										class="form-control PositionofTextbox slNo" value="" /></td>
								<td width="35%" class="CellWithComment"><select	class="form-control PositionofTextbox select2 descriptionDropdown"
										id="descriptionDropdown0" name="items[0].description"
										style="padding: 0;">
											<option value="">Select Description:</option>
									</select><span
												id="descriptionDiv0" ></span></td>
									<td width="15%"><input type="text" id="modelNo0"
										name="items[0].soModelNo" readonly="readonly"
										class="form-control PositionofTextbox modelNo" value="" /></td>
									<td width="10%"><input type="text" id="units0" readonly="readonly"
										class="form-control PositionofTextbox units" value="" /></td>
									<td width="10%"><input type="text" id="totalQty0"
										name="items[0].totalQuantity" readonly="readonly"
										class="form-control PositionofTextbox totalQty" value="" /></td>
									<td width="10%"><input type="text" id="deliveredQty0"
										name="items[0].deliveredQuantity"
										class="form-control PositionofTextbox deliveredQty" value="" readonly="readonly"/>
									</td>
									<td width="10%"><input type="text" id="todaysQty0"
										name="items[0].todaysQty"
										class="form-control PositionofTextbox todaysQty" value="" readonly="readonly"/>
									</td>
									<td style="display:none" id="designTd0" align="center"><a href="#" aria-hidden="true" class="design" id="design0">Design</a>
										</td>
									<td style="display:none"><input type="hidden" id="remainingQty0"/>
									</td>
									<td style="display:none"><input type="hidden" class="designArrData" id="designArrData0" name="designArrData"/>
									</td>
									<td style="display:none"><input type="hidden" class="availableStockQty" id="availableStockQty0"/>
									</td>
									<td align="center"><i class="deleteButton fa fa-trash"
										aria-hidden="true"></i></td>
										
								</tr> -->
							</tbody>
						</table>
					</div>
					<div class="button-div-style" id="buttonDiv" align="center">
						<button type="submit" id="saveDeliveryChallan"
							class="btn btn-primary btn-sm btn-inline">Save</button>
					</div>
				</div>
			</form>
		</div>
		<tiles:insertAttribute name="footer" />
	</div>
	<!--Sales items without design popup starts-->
	<div class="modal fade" tabindex="-1" role="dialog"
				id="itemsModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:700px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="itemsHeader">Sales Items without Design</h5>
							<!-- <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> -->
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						
						 <table class="table table-bordered table-striped"
									id="itemsTable">
									
									<tbody>
										
									</tbody>
								</table>
						</div>
							<div class="button-div-style" align="center">
						
								<button 
									class="btn btn-primary btn-sm btn-inline buttonDismiss" data-dismiss="modal">Ok</button>

								<!-- <button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button> -->

							</div>
						<%-- </form> --%>
						<div style="margin: 20px 20px 20px 20px;">
								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>
	
	<!-- Sales Items without design popup ends-->
	
	<!--Design model starts -->
		<div class="modal fade" tabindex="-1" role="dialog"
				id="designModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:800px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="designHeader"></h5>
							<!-- <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> -->
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						<input type='hidden' id='remainingdcqty'>
						<input type='hidden' id='todaysdcqty'>
						 <table class="modalTableBar botHeader table-bordered"
									id="designTable">
									<thead>
										<tr>
											<th class="col" style="width: 40%">Model No</th>
											<th class="col" style="width: 20%">Design Qty</th>
											<th class="col" style="width: 20%">Delivered Qty</th>
											<th class="col" style="width: 20%">Today's Delivery</th>
										</tr>
									</thead>
									<tbody>
										
									</tbody>
								</table>
						</div>
							<div class="button-div-style" align="center">
						
								<button id="saveDesign" 
									class="btn btn-primary btn-sm btn-inline saveDesign">Ok</button>

								<!-- <button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button> -->

							</div>
						<%-- </form> --%>
						<div style="margin: 20px 20px 20px 20px;">
								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>
			<!-- Design modal ends -->
			
			<!--Single Design model starts -->
		<div class="modal fade" tabindex="-1" role="dialog"
				id="signleDesignModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:400px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="signleDesignHeader"></h5>
							<!-- <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> -->
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						<input type='hidden' id='remainingdcqty'>
						<input type='hidden' id='todaysdcqty'>
						 <table class="modalTableBar botHeader table-bordered"
									id="singleDesignTable"  style="width: 100%;">
									<thead>
										<tr>
											<th style="width: 50%;">Model No</th>
											<th style="width: 50%;">Present Qty</th>
										</tr>
									</thead>
									<tbody>
										
									</tbody>
								</table>
						</div>
							<div class="button-div-style" align="center">
						
								<!-- <button id="signleDesignBtn" 
									class="btn btn-primary btn-sm btn-inline signleDesignBtn">Ok</button> -->

							<button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button>

							</div>
						<%-- </form> --%>
						<div style="margin: 20px 20px 20px 20px;">
								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>
			<!-- Single Design Modal ends -->
			
			<div class="modal fade" tabindex="-1" role="dialog"
				id="clientModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:800px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="clientHeader">Sales Order History</h5>
							<!-- <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> -->
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						
						
							<table id="clientPOTable"
								class='table table-bordered table-striped dataTable'
								style="width: 100%">
									<thead>
										<tr>
											<th class="col" style="width: 60%">Client PO Number</th>
											<th class="col" style="width: 20%">Client PO Date</th>
											<th class="col" style="width: 20%" class="hideTd">Created Date</th>
											<th class="col" style="width: 20%">Created Date</th>
										</tr>
									</thead>
									<tbody>
							</table>
						</div>
							<div class="button-div-style" align="center">
						
								<!-- <button id="clientNameBtn" 
									class="btn btn-primary btn-sm btn-inline clientBtn">Ok</button> -->

							 <button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button> 

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