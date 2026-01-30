<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>


<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<%
request.getSession().invalidate();
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
%>
<!DOCTYPE html>
<html>

<head>
	<meta charset="ISO-8859-1">
	<title>
		<tiles:insertAttribute name="title" />
	</title>
	<tiles:insertAttribute name="header-resources" />



	<link rel="stylesheet" href="resources/css/salesOrder.css">
	<script src="<c:url value="/resources/js/salesOrder.js" />"></script>
		<script src="<c:url value="/resources/js/jquery.tabletojson.js" />"></script>
	<script src="${RESOURCES}js/common.js" ></script>

	<!--  <link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
 <script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script> -->
<script type="text/javascript">
var customerPartyList =${customerPartyList};
var itemList=${itemList};
var unitsList=${unitsList};
var salesOrderList=${salesOrderList};
var obj = '${salesOrderObj}';
obj = obj.replace(/\&/g, "\'");
obj = obj.replace(/\&/g, "\"");

 var salesOrderObj = "";
 if ('${salesOrderObj}' != null && '${salesOrderObj}' != "") {
	 salesOrderObj= $.parseJSON(obj);
 }
 var userName=${userName};
 var role=${role};
 
/* if ('${salesOrderObj}' != null && '${salesOrderObj}' != "") {
	
	salesOrderObj= $.parseJSON('${salesOrderObj}');
}  */
</script>
<style type="text/css">
.lbl-biiling-popup{
	font-weight: 700;
}
/* .ui-autocomplete {
    max-height: 500px;
    overflow-y: auto;
    overflow-x: hidden;
    padding-right: 20px;
}

<<<<<<< Updated upstream
* html .ui-autocomplete {
    height: 100px;
} */

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

.modal-header {
  cursor: move;
  background: #007bff;
  color: #fff;
  padding: 10px;
  font-weight: bold;
}
.modal-body {
  padding: 10px;
}
</style>
</head>

<body>
	<body class="hold-transition sidebar-mini">
		<div class="wrapper">
			<tiles:insertAttribute name="header" />
			<tiles:insertAttribute name="sideMenu" />

			<div class="content-wrapper">
				<%-- <tiles:insertAttribute name="pageHeader" /> --%>
				<div id="salesDiv" class="card">
					<div class="card-body table-responsive p-0">
						<form:form id="salesOrderForm1" method="POST" modelAttribute="salesOrder"
							action="${pageContext.request.contextPath}/add/salesOrder">
							<input type="hidden" name="id" id="soId" />
							<input type="hidden" name="party" id="party" />
							<input type="hidden" name="item" id="item" />
							<input type="hidden" name="shippingAddress" id="shippingAddress" />
							<input type="hidden" name="billingAddress" id="billingAddress" />
							<input type="hidden" name="otherTermsAndConditions" id="otherTermsAndConditions" />
							<input type="hidden" name="modeOfPayment" id="modeOfPayment" />
							<input type="hidden" name="jurisdiction" id="jurisdiction" />
							<input type="hidden" name="freight" id="freight" />
							<input type="hidden" name="delivery" id="delivery" />
							<input type="hidden" name="warranty" id="warranty" />
							<input type="hidden" name="clientPoNumber" id="clientPo" />
							<input type="hidden" name="clientPoDate" id="poDate" />
							<input type="hidden" name="projectClosureDate" id="closureDate"/>
							<input type="hidden" name="region" id="region" />
							<input type="hidden" name="responsiblePerson" id="responsiblePerson" />
							<input type="hidden" name="shippingAddress1" id="shippingAddressPartyId" />
							<table id="salesTable" class="table table-head-fixed table-hover">
								<thead id="table-header font">
									<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<!-- 	<th width="10%">Items</th> -->
										<th width="35%" rowspan="2" class="thStyle" >Description</th>
										<th width="5%" rowspan="2" class="thStyle">Model No</th>
										<th width="5%" rowspan="2" class="thStyle">HSN</th>
										<th width="5%" rowspan="2" class="thStyle">SAC</th>
										<th width="8%" rowspan="2" class="thStyle">Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Unit</th>
										<th width="10%" colspan="2" style="text-align: center;">Price</th>
										<th width="11%" rowspan="2" class="thStyle">Amount</th>
										<th align="center" rowspan="2"><i class="add fa fa-plus-square fa-2x"
												aria-hidden="true"></i></th>
										<th align="center" rowspan="2" class="thStyle expandAllHeader" id="expandAllHeader" title="Expand All" width="3%" style="display:none;">
										<input type="checkbox" id="masterToggleBrief" title="Check to expand all descriptions" style="cursor:pointer;">
										</th>
									</tr>

									<!--dividing a cloumn into two rows-->
									<tr>
										<th width="9%">Supply </th>
										<th>Service</th>
									</tr>
								</thead>
								<tbody id="table-body">
									<tr>
										<td width="5%"><form:input type="text" id="slNo0" name="items[0].slNo"
												path="items[0].slNo" class="form-control PositionofTextbox slNo"
												value="" /></td>
										<!-- <td width="10%">  <select id="itemDropDown0" class="form-control" style="width:125px;height: 25px; padding: 1.5px; border-radius: 0;" >
                 <option>Select Model:</option>
                  </select>
                  </td> -->
										<td width="35%" class="CellWithComment">
											<form:input type="text" id="description0" path="items[0].description"
												name="items[0].description" 
												class="form-control PositionofTextbox description"  value="" />
											<form:errors path="items[0].description" cssClass="error" /><span
												id="descriptionDiv0" ></span>
										</td>

										<td width="5%">
											<form:input type="text" id="modelNo0" name="items[0].modelNo"
												path="items[0].modelNo" class="form-control PositionofTextbox" maxlength = "100"
												value="" />
											<form:errors path="items[0].modelNo" cssClass="error" />
										</td>
										<td width="5%">
											<form:input type="text" id="hsnCode0" name="items[0].hsnCode"
												path="items[0].hsnCode" class="form-control PositionofTextbox num" />
											<form:errors path="items[0].hsnCode" cssClass="error" />
										</td>
										<td width="5%">
											<form:input type="text" id="servicehsnCode0" name="items[0].servicehsnCode"
												path="items[0].servicehsnCode"
												class="form-control PositionofTextbox num" />
											<form:errors path="items[0].servicehsnCode" cssClass="error" />
										</td>
										<td width="8%">
											<form:input type="text" id="qty0" name="items[0].quantity"
												path="items[0].quantity" class="form-control PositionofTextbox qty"
												value="" />
											<form:errors path="items[0].quantity" cssClass="error" /><span
												id="qtyDiv0"></span></td>

										<td width="7%">
											<form:select class="form-control select2 PositionofTextbox unit" 
				                                id="unit0" name="items[0].unit" path="items[0].unit" style="padding: 0;">
				                               <option value="">Select Unit:</option>
								               </form:select> 
											<span id="unitDiv0"></span>
										</td>
										
										<td width="9%">
											<form:input type="text" step="0.01" id="unitPrice0"
												name="items[0].unitPrice" path="items[0].unitPrice"
												class="form-control PositionofTextbox unitPrice alignright" value="" />
											<form:errors path="items[0].unitPrice" cssClass="error" /><span
												id="unitPriceDiv0"></span>
										</td>

										<td width="9%">
											<form:input type="text" step="0.01" id="servicePrice0"
												name="items[0].servicePrice" path="items[0].servicePrice"
												class="form-control PositionofTextbox servicePrice alignright"
												value="" />
											<form:errors path="items[0].servicePrice" cssClass="error" /><span
												id="servicePriceDiv0"></span>
										</td>

										<td width="11%">
											<form:input type="text" id="amount0" name="items[0].amount"
												path="items[0].amount"
												class="form-control PositionofTextbox amount alignright" value="" />
											<form:errors path="items[0].amount" cssClass="error" /><span
												id="amountDiv0"></span></td>
										<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i>
										</td>
										<td style="display:none" id="designTd0" align="center"><a href="#" aria-hidden="true" class="design" id="design0">Design</a>
										</td>
										<td class="hideTd"><input type="hidden" id="salesItemId0" name="items[0].id"/></td>
										<td style="display:none" id="toggleBriefTd0" class="toggleBriefTd"><input type="checkbox" id="toggleBrief0" class="toggleBrief"></td>
									</tr>
									<!-- <tr></tr> -->
								</tbody>
								<tfoot id="table-footer">
									<tr>
										<td colspan="6" width="81%" style="text-align: right;">Total</td>
										<td colspan="2">
											<form:input type="text" id="total" name="total" path="total"
												class="form-control PositionofTextbox total alignright" value="" />
										</td>
										
									</tr>
									<tr id="taxDropDownRow">
									<td colspan="6" width="81%" style="text-align: right;">Tax
										Rate</td>
									<td colspan="2"><select id="taxDropDown" name="gstRate">
											<option value="18">GST@18%</option>
											<option value="12">GST@12%</option>
											<option value="0">GST@0%</option>

									</select></td>

								</tr>
									<tr id="gstRow">
										<td colspan="6" id="gstrate" style="text-align: right;">GST@</td>
										<td colspan="2">
											<form:input type="text" id="gst" name="gst" path="gst"
												class="form-control PositionofTextbox gst alignright" value="" />
										</td>
									</tr>
									<tr>
										<td colspan="6" style="text-align: right;">Grand Total</td>
										<td colspan="2">
											<form:input type="text" id="grandTotal" name="grandTotal" path="grandTotal"
												class="form-control PositionofTextbox grandTotal alignright" value="" />
										</td>
									</tr>
								</tfoot>
							</table>
							<div class="button-div-style" align="center" id="buttonDiv">
								<button type="button" id="saveSalesOrder" class="btn btn-primary btn-sm btn-inline">
									Save
								</button>

								<button type="button" id="resetSalesOrder"
									class="btn btn-primary btn-sm btn-inline">Cancel
								</button>

							</div>

						</form:form>
					</div>

				</div>

			</div>

			<tiles:insertAttribute name="footer" />
		</div>
	<!-- ./wrapper -->
	<div id="soAddressSlectionPopup" class="modal show" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" style="width: 950px;">
            <div class="modal-header custom-box-header-modal">
                <h5 style="height: 18px;"><span>Billing and Shipping Details : </span></h5><button type="button" class="close buttonDismiss" data-dismiss="modal" style="outline: none;">�</button>
            </div>
            <div class="modal-body">
            <div class="row" style="padding: 10px;">
            <label for="projectClosureDate" class="lbl-biiling-popup">Project Closure Date : &nbsp;</label>
            <div class="col-md-4 form-inline">
            <!-- <span style="padding-bottom: 5px;"> Project Closure Date : </span> -->
               <input type="text" name="projectClosureDate" id="projectClosureDate" readonly="readonly" class="form-control PositionofTextbox"/>
            </div>
            
              <span style="padding-bottom: 5px;" id="region" class="lbl-biiling-popup"> Region: &nbsp;</span>
                 <select id="regionDropdown" class="form-control select2 select2-hidden-accessible dropdownWidth230">
					    <option selected value="">Select Region</option>
                  </select>
                  <span style="padding-bottom: 5px;padding-top:5px" id="respPerson" class="lbl-biiling-popup"> Responsible Person: &nbsp;</span>
                 <select id="respPersonDropdown" class="form-control select2 select2-hidden-accessible dropdownWidth230">
					    <option selected value="">Select Responsible Person</option>
                  </select>
            </div>
              <div class="row">
               <!-- billing address div -->
               <div class="col-lg-6">
                <div class="card" style="padding: 10px; height: 180px;">
                  <span style="padding-bottom: 5px;" class="lbl-biiling-popup"> Billing Address : </span>
                  <select style="width: 100%; padding: 0px;" id="billingAddressDropdown" class="form-control form-control-sm select2">
                  <!-- <option value="" selected>Select Billing Address</option> -->
                  	</select>
                  <div class="" style="margin-top: 5px; font-size: 14px;" id="billingAddressContent">
			             
			      </div>
                </div><!-- /.billing address closed -->
               </div>
              
              <!-- Client Name dropdown Div -->
              <div class="col-lg-6">
              <div class="card" style="padding: 10px; height: 180px;">
                  <span style="padding-bottom: 5px;" class="lbl-biiling-popup"> Shipping Address : </span> 
                  <select style="width: 100%; padding: 0px;" id="clientsDropdown" class="form-control form-control-sm select2">
                  	<option value="" selected>Select Clients</option>
                  	</select>
			            <!-- /.contents div -->
                
              <!-- /.Client Name dropdown Div closed -->
                <!-- shipping address div -->
                
                 <span style="height: 10px;" class="lbl-biiling-popup"> </span>
                 <select style="width: 100%; padding: 0px;" id="shippingAddressDropdown" class="form-control form-control-sm select2">
                  		<option value="" selected>Select Shipping Address</option>
                  	</select>
                  	 <div class="" style="margin-top: 5px; font-size: 14px;" id="shippingAddressContent">
			             
			            </div>
                
                </div> <!-- /.shipping address closed -->
                </div>
                
                
              </div><!-- /.addresses row closed --> 
               <!-- row for terms and conditions -->
              <div class="row" style="padding-top: 40px;">
                 <!-- mode of payment div -->
                <div class="col-md-3" id="modeOfPayment">
                <span class="lbl-biiling-popup">  Mode of payment : </span>
                  <select style="width: 100%; padding: 0px;" id="modeOfPaymentDropdown" class="form-control form-control-sm select2">
                  	
                  </select>
                </div>  <!-- /.mode of payment div closed-->
                
                 <!-- jurisdiction div -->
                <div class="col-md-2" id="jurisdiction">
                  <span class="lbl-biiling-popup">Jurisdiction : </span>
                  <select style="width: 100%; padding: 0px;" id="jurisdictionDropdown" class="form-control form-control-sm select2">
                  	
                  	
                  </select>
                </div>  <!-- /.jurisdiction div closed-->
                
                 <!-- freight div -->
                <div class="col-md-2" id="freight">
                 <span class="lbl-biiling-popup"> Freight : </span> 
                  <select style="width: 100%; padding: 0px;" id="freightDropdown" class="form-control form-control-sm select2">
                 
                  </select>
                </div>  <!-- /.freight div closed-->
                
                 <!-- Delivery -->
                <div class="col-md-2" id="delivery">
                  <span class="lbl-biiling-popup">Delivery : </span> 
                  <select style="width: 100%; padding: 0px;" id="deliveryDropdown" class="form-control form-control-sm select2">
                  	
                  </select>
                </div>  <!-- /.Delivery closed-->
                
                 <!-- Warranty -->
                <div class="col-md-3" id="warranty">
                 <span class="lbl-biiling-popup"> Warranty : </span> 
                  <select style="width: 100%; padding: 0px;" id="warrantyDropdown" class="form-control form-control-sm select2">
                  	
                  </select>
                </div>  <!-- /.Warranty closed-->
                
                
                </div><!-- /.terms and conditions closed -->
                <div class="row" style="padding-top: 20px;">
              <!-- Other T & C Name  Div -->
              <div class="col-md-12">
                  <span class="lbl-biiling-popup">Other T &amp; C :</span> 
                  <textarea rows="4" id="otherTermsAndCondition" class="form-control">

                  	</textarea>
								<!-- /.contents div -->
							</div>
							<!-- /.Other T&C Div closed -->

						</div><!-- /.addresses row closed -->
					</div><!-- /.modal-body closed -->

					<div class="modal-footer">

						<button type="button" class="btn btn-primary btn-sm" id="saveSoBtn">Save</button>
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>

				</div>
			</div>
		</div>
		
		<!--Design model starts -->
		<div class="modal fade" tabindex="-1" role="dialog"
				id="designModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:600px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="designHeader"></h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
						<form class="form" id="designForm">	
						<div class="modal-body no-padding">
						
						<input type="hidden" id="salesItemId" name="salesItemId"/>
						 <table class="modalTableBar botHeader"
									id="designTable">
									<thead>
										<tr>
											<th class="col" style="width: 50%">Model No</th>
											<th class="col" style="width: 30%">Unit</th>
											<th class="col" style="width: 20%">Quantity</th>
											<th align="center"><i class="fa fa-plus-square addrow"
												aria-hidden="true"></i></th>
										</tr>
									</thead>
									<tbody>
										<tr>
										<td><select style="width: 100%; padding: 0px;" id="itemModel0" name="items[0].itemId" class="form-control form-control-sm select2 itemModel">
										<option value="" selected>Select Model No:</option></select></td>
										<td><input type="text" id="unitMod0" class="form-control PositionofTextbox unit" /></td>
										<td><input type="text" id="quantity0" name="items[0].quantity" class="form-control PositionofTextbox" /></td>
										</tr>
									</tbody>
								</table>
						</div>
							<div class="button-div-style" align="center">
						
								<button id="saveDesign" 
									class="btn btn-primary btn-sm btn-inline">Save</button>

								<button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button>

							</div>
						</form>
						<div style="margin: 20px 20px 20px 20px;">
							
							<table id="designList"
								class='table table-bordered table-striped dataTable'
								style="width: 100%">

							</table>


								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>

	</body>

</html>