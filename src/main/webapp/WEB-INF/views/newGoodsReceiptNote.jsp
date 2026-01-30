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
var poList=${poList};
var itemList=${itemList};
var purchaseItemList=${purchaseItemList}; 
var grnList = ${grnList};
var grnObj = "";
if ('${grnObj}' != null && '${grnObj}' != "") {
	grnObj= $.parseJSON('${grnObj}');
}
</script>
 <script src="<c:url value="/resources/js/newGoodsReceiptNote.js" />"></script> 
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>

<style>
#purchaseDiv{
	height: 400px;
}
#grnTable>tbody>tr>td,  #grnTable>thead>tr>th {
    padding: 3px 2px!important;
    vertical-align: middle;
}
.alignright{
	text-align: right !important;
}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
			
		 <tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
		 <div class="row mb-2">
      <!--   <div class="col-sm-2">
        </div> -->
          <div class="col-sm-6" style="padding-top: 4px;">
          <div class="form-inline float-sm-left"  style="margin-right: 14px !important;">
            <span class="m-0 text-dark marginLeft7">Invoice Date : &nbsp;</span>
			<input type="text" name="invoiceDate" readonly="readonly" id="invoiceDate" class="form-control PositionofTextbox"/>
			</div>
          </div>
         <div class="col-sm-2">
        </div>
          <div class="col-sm-4" style="padding-top: 4px;">
           <div class="form-inline float-sm-right" style="margin-right: 14px !important;">
                  <span class="m-0 text-dark ">Invoice Number : &nbsp;</span>

                  <input type="text" name="invoiceNo"  id="invoiceNo" class="form-control PositionofTextbox" style="width:230px !important;"/>
                </div>
              </div>    
        </div>
		<form id="grnForm" method="POST"  action="${pageContext.request.contextPath}/add/grn">
		<input type="hidden" name="poNumber" id="poNumber">
		<input type="hidden" name="poDate" id="poDate">
		<input type="hidden" name="invoiceDate" id="invDate">
		<input type="hidden" name="invoiceNo" id="invNo">
			<!--  <div class="content-header content-header-padding">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0 text-dark">New Grn &nbsp;&nbsp;&nbsp;
           <a href="/ncpl-sales/grnLists" title="Grn View" id="purchaseDashBrd" style="font-size: 16px; color: black;"> 
            <i class="fa fa-bars" style="font-size:21px"></i>
            </a>
            </h1>
          </div>/.col
         
          <div class="col-sm-6" style="padding-top: 4px;">
           <div class="form-inline float-sm-right">
                  <h1 class="m-0 text-dark" style="font-size: x-large;">Purchase Order : </h1>
                <select class="form-control" name="partyByType" id="partyDropDown2" style="width:208px;height: 30px; padding: 1.5px; border-radius: 0;">
			   <option value="">Select Purchase Order:</option>
			  
				</select>
                </div>
              </div>    
        </div>/.row
        
      </div>/.container-fluid
    </div> -->
    
			<div id="salesDiv" class="card">
			<div class="card-body table-responsive p-0">
				<table id="grnTable" class="table table-head-fixed" style="width: 100%;">
					<thead id="table-header">
						<tr>
							<th width="5%" style="position:inherit;">Sl.No</th>
							<th width="25%" style="position:inherit;">Description</th>
							<th width="10%" style="position:inherit;">Model No</th>
							<th width="10%" style="position:inherit;">Units</th>
							<th width="7%" style="position:inherit;">Total Qty</th>
							<th width="7%" style="position:inherit;">Received Qty</th>
							<th width="7%" style="position:inherit;">Remaining Qty</th>
							<th width="12%" style="position:inherit;">Unit Price</th>
							<th width="14%" style="position:inherit;">Amount</th>
							<!-- <th><i class="add fa fa-plus-square fa-2x text-center mx-auto" aria-hidden="true"></i></th> -->
						</tr>
					</thead>
           <tbody id="table-body">
						  <!-- <tr id="table-row">
								<td width="5%" align="center">1</td>
								<td width="25%"> <select class="form-control PositionofTextbox select2 descriptionDropdown"  
                                id="descriptionDropdown0" name="items[0].description" style="padding: 0;" >
                                <option value="">Select Description:</option>
                                </select>   
								</td>
								<td width="10%"><input type="text" id="modelNo0" 
									readonly="readonly" class="form-control PositionofTextbox modelNo"/>
								</td>
								<td width="10%"><input type="text" id="units0" 
									readonly="readonly" class="form-control PositionofTextbox units" value="" />
								</td>
								<td width="7%"><input type="text" id="totalQty0"  
									readonly="readonly" class="form-control PositionofTextbox totalQty" value="" />
								<td width="7%"><input type="text" id="receivedQty0"  name="items[0].receivedQuantity"
									class="form-control PositionofTextbox receivedQty" value="" />
								</td>
								<td width="7%"><input type="text" id="RemainingQty0" readonly="readonly"
									class="form-control PositionofTextbox remainingQty" value="" />
								</td>
								<td width="12%"><input type="text" id="unitPrice0"  name="items[0].unitPrice"
									class="form-control PositionofTextbox unitPrice alignright" value="" />
								</td>
								<td width="14%"><input type="text" id="amount0"  name="items[0].amount"
									readonly="readonly" class="form-control PositionofTextbox amount alignright" value="" />
									</td>
									<td style="display:none"><input type="hidden" id="remainingQty0"/>
									</td>
								<td align="center"><i class="deleteButton fa fa-trash"
									aria-hidden="true"></i></td>
							</tr>	 -->
					</tbody>
					<tfoot id="table-footer">
									<tr id="totalTr">
										<td colspan="6" width="81%" style="text-align: right;">Total</td>
										<td colspan="2">
											<input type="text" id="total" class="form-control PositionofTextbox total" readonly="readonly" value="" />
										</td>
										
									</tr>
								
								</tfoot> 
				</table>
 </div>
        <div class="button-div-style" id="buttonDiv" align="center">
			<button  type="submit" id="saveGrn"class="btn btn-primary btn-sm btn-inline"> Save 
				</button>
		      </div>
			</div>
			</form>
		</div>
		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->
<div class="modal fade" tabindex="-1" role="dialog"
				id="stockQtyModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:400px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="modelId">Stock Quantity Details</h5>
							 <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" id="itempriceclose" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> 
							
						</div>
						<%-- <form class="form" id="designForm">	 --%>
						<div class="modal-body no-padding">
						
						 <table class="table table-bordered table-striped dataTable"
									id="stockQtyTable"  style="width: 100%;">
									<thead>
										<tr>
											
											<th  style="width: 100%">Present Qty</th>
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
