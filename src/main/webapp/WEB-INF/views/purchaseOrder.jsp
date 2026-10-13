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
 
<script type="text/javascript">

var partyList=${partyList};
var SalesOrder=${salesOrderList};
var itemList = ${itemList};
var unitsList=${unitsList};
var purchaseOrderObj =null;
var role=${role};
</script>
<script src="<c:url value="/resources/js/purchaseOrder.js" />"></script>
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>


</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		<%-- <tiles:insertAttribute name="header" /> --%>
		 
 
       <tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
		<form id="purchaseOrderForm" method="POST" action="${pageContext.request.contextPath}/save/purchaseOrder">
			<input type="hidden" name="partyByType" id="partyDropDownValue"/>
			<!--  <div class="content-header content-header-padding">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0 text-dark">Purchase Order &nbsp;&nbsp;&nbsp;
           <a href="/ncpl-sales/purchase" title="Purchase Order Dashboard" id="purchaseDashBrd" style="font-size: 16px; color: black;"> 
            <i class="fa fa-bars" style="font-size:21px"></i>
            </a>
            </h1>
          </div>/.col
         
          <div class="col-sm-6" style="padding-top: 4px;">
           <div class="form-inline float-sm-right">
                  <h1 class="m-0 text-dark" style="font-size: x-large;">Vendor : </h1>
                <select class="form-control" name="partyByType" id="partyDropDown2" style="width:208px;height: 30px; padding: 1.5px; border-radius: 0;">
			   <option value="">Select Vendor:</option>
				</select>
                </div>
              </div>    
        </div>/.row
        
      </div>/.container-fluid
    </div> -->
    
			<div id="purchaseDiv" class="card">
			<div class="card-body table-responsive p-0">
				<table id="poTable" class="table table-head-fixed" style="width: 100%;">
				
					<thead id="table-header" style="text-align:center">
						<tr>
							<th width="5%" align="center">Sl.No</th>
							<th width="15%">Client Name</th>
							<th width="11%">Desc.</th>
							<th width="10%">Model No</th>
							<th width="10%">PO Desc.</th>
							<th width="8%">HSN</th>
							<th width="10%">Qty</th>
							<th class="hideTd">salesQty</th>
							<th width="6%">Unit</th>
							<th width="8%">U.Price</th>
							<th class="hideTd">salesUnitPrice</th>
							<th width="7%">Tax %</th>
							<th width="10%">Amt.</th>
							<th align="center"><i class="add fa fa-plus-square fa-2x text-center mx-auto" aria-hidden="true"></i></th>
						</tr>
					</thead>
           <tbody id="table-body">
						  <tr id="table-row">
								<td width="5%" align="center">1</td>
								<td width="15%"><select class="form-control select2 PositionofTextbox salesOrderDropdown dropdownMarginTop" 
                                id="salesOrderDropdown0" name="salesOrder"  style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;">
                               <option value="">Select Client Name:</option>
				               </select> 	
								</td>
								<td width="11%"> <select class="form-control select2 PositionofTextbox descriptionDropdown dropdownMarginTop" name="items[0].description" 
                                id="descriptionDropdown0" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;" disabled>
                                <option value="">Select Description:</option>
                                </select>   
								</td>
								<!-- <td width="10%"> <input type="text" class="form-control PositionofTextbox poDescription" name="items[0].poDescription" 
                                id="poDescription0" />
								</td> -->
								<td width="10%"><select id="modelNo0" name="items[0].modelNo" 
									class="form-control PositionofTextbox modelNo select2"  style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;" >
									<option value="">Select ModelNo:</option></select>
								</td>
								<td width="10%"> <input type="text" class="form-control PositionofTextbox poDescription" name="items[0].poDescription" 
                                id="poDescription0" />
								</td>
								<td width="8%"><input type="text" id="hsnCode0" name="items[0].hsnCode" 
									class="form-control PositionofTextbox hsnCode"  /><span id="hsnCodeDiv0"></span>
								</td>
								<td width="10%"><input type="text"  id="newQuantity0"  name="items[0].quantity"
									class="form-control PositionofTextbox qty qtyInput" value="" />
									<span id="qtyDiv0"></span>
							 	</td>

								 <td class="hideTd"><input type="hidden" class="salesQtyInput"
								  value="" id="salesQuantity0" />
								</td>
								
									
								
								<td width="6%"><input type="text"  id="unit0"
									class="form-control PositionofTextbox unit" value="" readonly="readonly"/>
									<span id="unitDiv0"></span>
							 	</td>
								<td width="8%"><input type="text" id="newPrice0" name="items[0].unitPrice"
									class="form-control PositionofTextbox unitPrice upInput alignright"  value="" />
									<span id="unitPriceDiv0"></span>
								</td>
								<td class="hideTd">
								<input type="hidden" class="salesUnitPrice" value="" id="salesUnitPrice0" />
								</td>
								<td width="7%"><input type="text" id="tax0" 
									class="form-control PositionofTextbox tax " readonly="readonly" style= "text-align: right;" value="" /></td>
								<td width="10%"><input type="text" id="amount0" name="items[0].amount" 
									class="form-control PositionofTextbox amount amtInput " style= "text-align: right;" value="" /><span id="amountDiv0"></span></td>
								<td align="center"><i class="purchaseDeleteButton deleteButton fa fa-trash" id="delete0"
									aria-hidden="true"></i></td>
									 <td class="hideTd"><input type="hidden" class="totalQty"
								  value="" id="totalQty0" />
								</td>
							</tr>	
					</tbody> 
				</table>
 </div>
        <div class="row" style="padding: 8px 12px 0 12px;">
          <div class="col-md-6"></div>
          <div class="col-md-6">
            <table class="table" style="margin-bottom: 0;">
              <tbody>
                <tr>
                  <th style="width: 50%;">Total</th>
                  <td><input type="text" class="form-control total alignright" id="poTotal" name="total" readonly="readonly" /></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="button-div-style" align="center">
			<button  type="submit" id="savePurchaseOrder"class="btn btn-primary btn-sm btn-inline"> Save Purchase Order
				</button>
		      </div>
			</div>
			</form>
		</div>
		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->
	

   		<div class="modal fade" tabindex="-1" role="dialog"
				id="itemsModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:800px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title">Items Purchased Price</h5>
							 <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" id="itempriceclose" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> 
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						
						 <table class="table table-bordered table-striped dataTable"
									id="itemTable"  style="width: 100%;">
									<thead>
										<tr>
											<th  style="width: 40%">Model No</th>
											<th  style="width: 30%">Price</th>
											<th  style="width: 10%">Date</th>
											<th  style="width: 20%">Vendor</th>
										
										</tr>
									</thead>
									<tbody>
										
									</tbody>
								</table>
						</div>
							<div class="button-div-style" align="center">
						
								

								<button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button> 

							</div>
						<%-- </form> --%>
						<div style="margin: 20px 20px 20px 20px;">
								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>	
			<!-- modal starts -->
			<div class="modal fade" tabindex="-1" role="dialog"
				id="qtyModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:800px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="modelId">Quantity Details</h5>
							 <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" id="itempriceclose" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> 
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						
						 <table class="table table-bordered table-striped dataTable"
									id="qtyTable"  style="width: 100%;">
									<thead>
										<tr>
											
											<th  style="width: 50%">Ordered Qty</th>
											<th  style="width: 50%">Delivered Qty</th>
										</tr>
									</thead>
									<tbody>
										
									</tbody>
								</table>
						</div>
							<div class="button-div-style" align="center">
						
								

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