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


<script src="<c:url value="/resources/js/purchaseOrder.js" />"></script>
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>
<script type="text/javascript">
var pageContext = '${pageContext.request.contextPath}';
var partyList=${partyList};
//this object contains items only
var salesItemList=${salesItemList};
var purchaseOrderObj =${purchaseOrderObj};
var SalesOrder=${salesOrderList};
var itemList=${itemList};
var unitsList=${unitsList};
var role=${role};

$(document).ready(function () {
	
	$('.select2').select2({dropdownAutoWidth : true});
});

</script>
<style type="text/css">

     .select2-container--default .select2-selection--single {
    border:1px solid #ced4da!important;
	height: 24px!important;
   }
   .select2-container--default .select2-selection--single .select2-selection__rendered {
    margin-top: -5px!important;
 }  
#poTableEdit>tbody>tr>td,  #poTableEdit>thead>tr>th {
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
  white-space:pre-wrap;
  top:20px; 
  left:20px;
}

.CellWithComment:hover span.CellComment{
  display:block;
}





</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">

    <!--toast message for validating quantity in purchase order-->
     <div id="toast" class="hideToast alert alert-danger alert-dismissible fade show">
			<strong>Error!</strong> Entered Quantity is greater than sales Quantity
			<button type="button" class="close" data-dismiss="alert">&times;</button>
		</div>

		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
		<form id="editPurchaseOrderForm" method="POST" action="${pageContext.request.contextPath}/save/purchaseOrder">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-4">
         		
			  <span class="m-0 text-dark" > PO No. :  <span id="poNumberEdit">${purchaseOrder.poNumber}</span></span>
	
			<span class="nav-item d-none d-sm-inline-block">
	                              <a href="${pageContext.request.contextPath}/purchase" class="nav-link poList"><i
										title="Purchase List" class="fa fa-list-alt" style="font-size: 24px; color: rgba(0,0,0,.5);"></i></a>
								 </span>
							
								 <span class="nav-item d-none d-sm-inline-block">
									<a
									href="${pageContext.request.contextPath}/purchaseOrder"
									title="Add Purchase" id="Home" class="nav-link"
									style="font-size: 24px; color: rgba(0,0,0,.5);"> <i
										class="fa fa-plus-square"></i></a>
	                             </span>
		  </div>
		 
		 
          <input type="hidden" value="${salesObj}" name="salesOrder" />
          <input type="hidden" value="${partyId}" name="partyByType" id="partyByType" />
          <input type="hidden" value="${poNumber}" name="poNumber" id="poNumEdit" />
         
          <div class="col-sm-4" style="padding-top: 10px;">
           <div class="form-inline float-sm-right">
                <span class="m-0 text-dark" id="vendorName">  Vendor : ${purchaseOrder.party.partyName}</span>
            </div>
          </div>
              
               <div class="col-sm-4" style="padding-top: 8px;">
               <div class="form-inline float-sm-right">
               
               
          			<span class="m-0 text-dark">Versions: </span>
						<select class="form-control select2 PositionofTextbox" name="version" id="version" style="width:208px;height: 30px; padding: 1.5px; border-radius: 0;">
						
								<!-- if history not available then populate current version. NOTE this version will not be stored in history object -->
								<c:if test="${empty purchaseOrder.history}">
									<option value="0">
										   ${purchaseOrder.created}-V${purchaseOrder.version}
									</option>
								</c:if>
														

								<!-- Populate versions from history object -->
								<c:if test="${purchaseOrder.history.length() > 0 }">
								<!-- All previous versions -->
									<c:forEach begin="0" end="${purchaseOrder.history.length() - 1}" var="index">
		    								<option  value=${purchaseOrder.history.getJSONObject(index).get("version")}>
											 ${purchaseOrder.history.getJSONObject(index).get("created")}-V${purchaseOrder.history.getJSONObject(index).get("version")}
											</option>
										</c:forEach>
										<!-- Push Current version as selected with other versions -->
										<option value="${purchaseOrder.version}">
										 ${purchaseOrder.created}-V${purchaseOrder.version}
										</option>
								</c:if>
							
							
							
							
								
								
					</select>
				</div>
        	  </div>
          
        </div><!-- /.row -->
        
      </div><!-- /.container-fluid -->
    
			<div id="salesDiv" class="card">
			<div class="card-body table-responsive p-0">
				<table id="poTableEdit" class="table table-head-fixed" style="width: 100%;">
					<!-- <thead id="table-header">
						<tr>
							<th width="5%">Sl.No</th>
							<th width="28%">Description</th>
							<th width="15%">HSN Code</th>
							<th width="15%">Model No</th>
							<th width="8%">Qty</th>
							<th width="10%">Unit Price</th>
							<th width="15%">Amount</th>
							<th><i class="add fa fa-plus-square fa-2x text-center mx-auto" aria-hidden="true"></i></th>
						</tr>
					</thead>
           <tbody id="table-body" style="width:100%;">
						  <tr id="table-row">
								<td width="5%" align="center">1</td>
								<td width="28%" id="td-description"> <select class="form-control PositionofTextbox descriptionDropdown" name="items[0].description" 
                                id="descriptionDropdown0" style="padding: 0;">
                                <option>Select Description:</option>
                                </select>   
								</td>
								<td width="15%"><input type="text" id="hsnCode" name="items[0].hsnCode" 
									class="form-control PositionofTextbox num"  />
								</td>
								<td width="15%"><input type="text" id="modelNo" name="items[0].modelNo" 
									class="form-control PositionofTextbox modelNo" value="" />
								</td>
								<td width="8%"><input type="number" id="qty" name="items[0].quantity"
									class="form-control PositionofTextbox qty" value="" />
								<td width="10%"><input type="number" id="unitPrice" name="items[0].unitPrice" 
									class="form-control PositionofTextbox unitPrice" value="" />
								</td>
								<td width="15%"><input type="number" id="amount" name="items[0].amount" 
									class="form-control PositionofTextbox amount" value="" />
								<td align="center"><i class="deleteButton fa fa-trash"
									aria-hidden="true"></i></td>
							</tr>	
					</tbody>  -->
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
			<button  type="submit" id="savePurchaseOrder"class="btn btn-primary btn-sm btn-inline"> Update Purchase Order
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
							 <button type="button" id="itempriceclose" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
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
						
								

								<button type="button" class="btn btn-primary  btn-sm buttonDismiss" id="itemclose" data-dismiss="modal">Close</button> 

							</div>
						<%-- </form> --%>
						<div style="margin: 20px 20px 20px 20px;">
								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>	
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
<script>
var version = '${version}';
var highestVersion = '${poLastVersion}'
	$('#version').val(version);

</script>
</html>