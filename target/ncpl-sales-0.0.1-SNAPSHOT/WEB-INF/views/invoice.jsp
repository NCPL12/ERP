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
<script type="text/javascript" src="resources/js/invoice.js"></script>
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>
<script type="text/javascript">
var salesList = ${salesList};
var obj = '${invoiceObj}';
obj = obj.replace(/\&/g, "\'");
obj = obj.replace(/\&/g, "\"");
var invoiceObj = "";
if ('${invoiceObj}' != null && '${invoiceObj}' != "") {
	invoiceObj= $.parseJSON(obj);
}
</script>
<style type="text/css">

#invoiceTable>tbody>tr>td,  #invoiceTable>thead>tr>th {
    padding: 3px 2px!important;
    vertical-align: middle;
}

</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
		<tiles:insertAttribute name="header" />

		<tiles:insertAttribute name="sideMenu" />
		<div class="content-wrapper">
		<form id="invoiceForm" method="POST"
				action="${pageContext.request.contextPath}/add/invoice">
				<input type="hidden" name="soNumber" id="soNumber"/>
				<input type="hidden" name="dcNumber" id="dcNumber"/>
				<input type="hidden" name="type" id="type"/>
				<input type="hidden" name="state" id="state"/>
				
				<div id="salesDiv" class="card">
					<div class="card-body table-responsive p-0">
						<table id="invoiceTable" class="table table-head-fixed"
							style="width: 100%;">
							<thead id="table-header">
								<tr>
									<th width="5%">Sl.No</th>
									<th width="37%">Desc.</th>
									<th width="12%">HSN/SAC Code</th>
									<th colspan="2" width="14%">Qty</th>
									<th width="10%">Supply Rate/unit</th>
									<th width="12%">Installation Rate/unit</th>
									<th width="10%">Taxable Amount</th>
								</tr>
							</thead>
							<tbody id="table-body">
								
							</tbody>
							<tfoot id="table-footer">
									<tr>
										<td colspan="6" style="text-align: right;">Total</td>
										<td colspan="2">
											<input type="text" id="total" name="total"
												class="form-control PositionofTextbox total alignright" value="" />
										</td>
										
									</tr>
								<tr id="taxDropDownRow">
									<td colspan="6" width="81%" style="text-align: right;">Tax
										Rate</td>
									<td colspan="2"><select  id="taxDropDown" name="gstRate">
											<option value="18">GST@18%</option>
											<option value="12">GST@12%</option>
											<option value="0">GST@0%</option>

									</select></td>

								</tr>
								<tr>
										<td colspan="6" width="81%" style="text-align: right;" id="gstColumn">CGST @<span id="gstPercentage"></span></td>
										<td colspan="2">
											<input type="text" id="gst" name="gst"
												class="form-control PositionofTextbox gst alignright" value="" />
										</td>
									</tr> 
									 <tr id="sgst">
										<td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td>
										<td colspan="2">
											<input type="text" id="gst1" name="gst1"
												class="form-control PositionofTextbox gst alignright" value="" />
										</td>
									</tr>
									<tr>
										<td colspan="6" style="text-align: right;">Grand Total</td>
										<td colspan="2">
											<input type="text" id="grandTotal" name="grandTotal"
												class="form-control PositionofTextbox grandTotal alignright" value="" />
										</td>
									</tr>
								</tfoot>
						</table>
					</div>
					<div class="button-div-style" id="buttonDiv" align="center">
						<button type="submit" id="saveInvoice"
							class="btn btn-primary btn-sm btn-inline">Save</button>
							
					</div>
					<div class="button-div-style" id="paymentStatusDiv" align="center" style="display:none">
							<button type="button" id="updatePaymentStatusBtn" 
							class="btn btn-primary btn-sm btn-inline">Update Payment Status</button>
					</div>
				</div>
				</form>
		</div>
		<tiles:insertAttribute name="footer" />
		</div>
		
		<!--Update Payment model starts -->
		<div class="modal fade" tabindex="-1" role="dialog"
				id="paymentModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:600px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title">Update Payment Status</h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
						<%-- <form class="form" id="paymentForm"> --%>
					<div class="modal-body no-padding">
					<input type="hidden" id="invoiceNum" name="invoiceNum">
						<div class="row">
							<div class="col-md-12 form-inline">
								<label for="payment" class="col-form-label form-control-sm col-sm-3" style="justify-content: left !important">Payment
									Mode</label> <select
									class="form-control PositionofTextbox select2 paymentDropdown dropdownWidth250"
									id="paymentDropdown" name="paymentMode" style="padding: 0;">
									<option value="">Select Option:</option>
									<option value="ByCash">By Cash</option>
									<option value="ByCheque">By Cheque</option>
									<option value="ByNeft">By NEFT</option>
								</select>
							</div>
						</div>
						<div class="row" style="padding-top: 5px;">
							<div class="col-md-12 form-inline" style="display: none"
								id="remarksRow">
								<label for="remarks" class="col-form-label form-control-sm col-sm-3" style="justify-content: left !important">Remarks</label>
								<textarea rows="4" cols="100" class="form-control" id="paymentRemarks" name="paymentRemarks"></textarea>
							</div>
						</div>
						<div class="row" style="padding-top: 5px;">
							<div class="col-md-12 form-inline" style="display: none"
								id="transactionNoRow">
								<label for="transactionNo" class="col-form-label form-control-sm col-sm-3" style="justify-content: left !important">UTR</label> <input type="text"
									class="form-control PositionofTextbox transactionNo"
									id="transactionNumber" name="transactionNumber" style="width:250px!important">
							</div>
						</div>
					</div>
					<div class="button-div-style" align="center">
						
								<button id="updatePaymentBtn" 
									class="btn btn-primary btn-sm btn-inline">Update</button>

								<button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button>

							</div>
						<%-- </form> --%>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>
			<!--Update Payment model ends -->
</body>
</html>