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
 <script>
var dataObj=${poList};
var itemList=${itemList};
var role = ${role};
var user=${user};
var pageContext = '${pageContext.request.contextPath}';
</script> 
<script src="<c:url value="/resources/js/po-dashboard.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>
<style type="text/css">
.lbl-biiling-popup{
	font-weight: 500;
}

.previewTbl td, .previewTbl th{
 padding:0rem !important;
}
.hideTd{
  display:none !important;
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
			<%-- <div class="content-header content-header-padding">
				<div class="container-fluid">
					<div class="row mb-2">
						<div class="col-sm-6">
							<h1 class="m-0 text-dark">${pageHeader}
								&nbsp;&nbsp;&nbsp;&nbsp; <small> <a
									href="${pageContext.request.contextPath}/purchaseOrder"
									title="Add New Purchase Order" id="Home"
									style="font-size: 16px; color: black;"> <i class="fa fa-plus-square" style="font-size:24px"></i></a>
								</small>
							</h1>
						</div>
						<!-- /.col -->

						<div class="col-sm-6 " style="padding-top: 4px;">


							<div class="form-inline float-sm-right">


								<h1 class="m-0 text-dark" style="font-size: x-large;">Party
									:</h1>
								<select id="partyDropDown" name="party" class="form-control"
									style="width: 230px; height: 30px; padding: 1.5px; border-radius: 0;">

								</select>
							</div>

						</div>
						<!-- /.col -->
					</div>
					<!-- /.row -->
				</div>
				<!-- /.container-fluid -->
			</div> --%>
			<div id="salesDiv" class="card">
			<div class="card-body"  style="padding-top: 10px;">
				<table id="purchaseList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
						 <th width="20%"><spring:message code="po.header"/></th>
							<th width="30%"><spring:message code="po.company"/></th>
							<th width="12%"><spring:message code="po.city"/></th>
							<th width="10%"><spring:message code="so.total"/></th>
							<th class="hideTd"></th>
							<th width="10%"><spring:message code="date"/></th>  
							<th width="2%" style ="visibility: hidden"><spring:message code="po.version"/></th>
							<th width="12%"><spring:message code="po"/></th>
							<th width="5%"><spring:message code="archive"/></th>
						</tr>
					</thead>
					<tbody style="width: 100%;">
					</tbody>
				</table>
				</div>
			</div>
			<form id="dashboard-purchase-form" action="">
				<input type="hidden" name="poNumber" id="poNumber">
			</form>
		</div>
		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->

 <!--.po address selection modal-dialog -->
<div id="poAddressSlectionPopup" class="modal show" tabindex="-1" role="dialog" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content" style="width: 950px;">
            <div class="modal-header custom-box-header-modal">
                <h5 style="height: 18px;"><span>Terms &amp; Conditions : </span><span id="poNumberOnBillingPopup"></span> </h5><span style="padding-left: 30%;font-weight: bold;font-size: 17px;" id="gstRegion"></span><button type="button" class="close buttonDismiss" data-dismiss="modal">×</button>
            </div>
            <div class="modal-body">
              <div class="row">
                 <!-- Vendor address div -->
                <div class="col-md-4" id="vendorAddress">
                  <span class="lbl-biiling-popup">Vendor Address :</span> 
                  <select style="width: 100%; padding: 0px;" id="vendorAddressDropdown" class="form-control form-control-sm">
                  	
                  	</select>
			             <div class="" style="margin-top: 5px; font-size: 14px;" id="vendorAddressContent">
			             
			            </div> 
			            <!-- /.contents div -->
                </div>
                <!-- Vendor div close -->
                
                <!-- Shipping address div -->
                <div class="col-md-4">
                 <span style="text-align: center" class="lbl-biiling-popup">  Shipping Address : </span>
                 <select style="width: 100%; padding: 0px;" id="shippingAddressDropdown" class="form-control form-control-sm">
                  		
                  	</select>
                  <div class="" style="margin-top: 5px; font-size: 14px;" id="shippingAddressContent">
			             
			            </div>
			          
                </div> <!-- /.Shipping address closed -->
                
                 <!-- Billing address div -->
                <div class="col-md-4">
                  <span style="text-align: center" class="lbl-biiling-popup"> Billing Address : </span>
                  <select style="width: 100%; padding: 0px;" id="billingAddressDropdown" class="form-control form-control-sm">
                  	</select>
                  <div class="" style="margin-top: 5px; font-size: 14px;" id="billingAddressContent">
			             
			      </div>
                </div><!-- /.Billing address closed -->
                
              </div><!-- /.addresses row closed --> 
              
              <!-- row for terms and conditions -->
              <div class="row" style="padding-top: 40px;">
                 <!-- mode of payment div -->
                <div class="col-md-2" id="modeOfPayment">
                <span class="lbl-biiling-popup">  Mode of payment : </span>
                <input type="text" id="modeOfPaymentDropdown" class="form-control PositionofTextbox" style="height: 32px !important"/>
                 <!--  <select style="width: 100%; padding: 0px;" id="modeOfPaymentDropdown" class="form-control form-control-sm">
                  	
                  </select> -->
                </div>  <!-- /.mode of payment div closed-->
                
                 <!-- jurisdiction div -->
                <div class="col-md-2" id="jurisdiction">
                  <span class="lbl-biiling-popup">Jurisdiction : </span>
                  <select style="width: 100%; padding: 0px;" id="jurisdictionDropdown" class="form-control form-control-sm">
                  	
                  	
                  </select>
                </div>  <!-- /.jurisdiction div closed-->
                
                 <!-- freight div -->
                <div class="col-md-2" id="freight">
                 <span class="lbl-biiling-popup"> Freight : </span> 
                  <select style="width: 100%; padding: 0px;" id="freightDropdown" class="form-control form-control-sm">
                 
                  </select>
                </div>  <!-- /.freight div closed-->
                
                 <!-- Delivery -->
                <div class="col-md-2" id="delivery">
                  <span class="lbl-biiling-popup">Delivery : </span> 
                  <select style="width: 100%; padding: 0px;" id="deliveryDropdown" class="form-control form-control-sm">
                  	
                  </select>
                </div>  <!-- /.Delivery closed-->
                
                 <!-- Warranty -->
                <div class="col-md-4" id="warranty">
                 <span class="lbl-biiling-popup"> Warranty : </span> 
                  <select style="width: 100%; padding: 0px;" id="warrantyDropdown" class="form-control form-control-sm">
                  	
                  </select>
                </div>  <!-- /.Warranty closed-->
                
                 <div class="col-md-2" id="quoteRefNo">
                <span class="lbl-biiling-popup"> Quote Ref No.: </span>
                <input type="text" id="quoteRefNoInput" class="form-control PositionofTextbox" style="height: 32px !important"/>
                 
                </div>  <!-- /.Quote ref no div closed-->
                
                <div class="col-md-2" id="quoteDate">
                <span class="lbl-biiling-popup"> Quote Date : </span>
                <input type="text" id="quoteDateInput" readonly class="form-control PositionofTextbox" style="height: 32px !important"/>
                
                </div><!-- /.Quote date div closed-->
                
                </div><!-- /.terms and conditions closed -->
              
     		</div><!-- /.modal-body closed -->
            
            <div class="modal-footer">

             	<a href = "" id="generatePurchaseOrder">
             		<button type="button" class="btn btn-primary btn-sm" id="generatePoBtn">Generate PO</button>
             		<button type="button" class="btn btn-primary btn-sm" id="generatePoPdfBtn">Generate PO PDF</button>
                </a>

                <button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
            </div>
            
        </div>
    </div>
</div>
			<!-- /.supplier modal-dialog -->
			<!-- Po preview modal starts -->
	<div id="poPreviewModal" class="modal show" tabindex="-1" role="dialog"
		aria-hidden="true">
		<div class="modal-dialog modal-lg">
			<div class="modal-content" style="width: 950px;">
				<div class="modal-header custom-box-header-modal">
					<h5 style="height: 18px;">Purchase Order</h5>
					<button type="button" class="close" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body">
				<div class="row">
				<div class="col-md-2">
				<img src="${pageContext.request.contextPath}/resources/dist/img/ncpl_logo.png" >
           		</div>
           		<div class="col-md-4 previewTbl">
           		<!-- <h4>
				 <br>
                  Neptune controls pvt ltd<br>
                                   
                 </h4> -->
          		</div>
           		<div class="col-md-6">

           		 <table class="table" style="float:right;width: 57%; line-height: 10px; font-size: 14px;">
                     <tr>
                       <td id="PonoInPreview" style="border-top: none;"></td>
                     </tr>                      
                     <tr>
                        <td id="podateInPreview" style="border-top: none;"></td>

                      </tr>
                     <tr>
                     <td id="quoteRefNoInPreview" style="border-top: none;"></td>
                       <!-- <td id="contactpersonInPreview" style="border-top: none;"></td>  -->                    </tr>
                     <tr>
                     <td id="quoteDateInPreview" style="border-top: none;"></td>
                      <!--  <td  id="contactnumberInPreview" style="border-top: none;"></td> -->
                   </tr>
                     <!--  <tr>
                        <td  id="emailInPreview" style="border-top: none;"></td>
                      </tr> -->
                    </table>
           		</div>
				</div>
					<div class="row" style="font-size: 14px;">
						<div class="col-md-4" >
							 <span style="text-align: center;padding: 7px;" class="lbl-biiling-popup">Supplier:</span>
							 <address class="col-md-6 previewTbl" id="vendorAddressinPreview" style="max-width: 100% !important;">
			                 	
			                  </address>
						</div>
						<!-- Vendor div close -->

						<!-- Deliver TO address div -->
						<div class="col-md-4">
							<span style="text-align: center" class="lbl-biiling-popup">
								Deliver To : </span>
								<address id="deliveryaddressinPreview">
								
			                   </address>
						</div>
						<!-- /.Deliver TO address div closed -->

						<!-- Billing address div -->
						<div class="col-md-4">
							<span style="text-align: center" class="lbl-biiling-popup">
								Billing &amp; Invoice : </span>
							 <address id="billingaddressinPreview">
								 <!--  Neptune controls pvt ltd<br>
			                   Nandidurga main road jayamahal extension<br>
			                   Bangalore 560046  <br> -->
			                   </address>
						</div>
						<!-- /.Billing address closed -->
					</div>
					
					 <!-- Table row -->
              <div class="row" >
                <div class="col-12 table-responsive">
                  <table id="purchaseprevieItems" class="table table-striped">
                    <thead>
                    <tr>
                      <th>Sl.No</th>
                      <th>Description</th>
                      <th>Model No</th>
                      <th>HSN Code</th>
                      <th>GST RATE </th>
                      <th>Qty</th>
                      <th>Unit</th>
                      <th>Unit Price</th>
                      <th>Amount</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tbody style="width: 100%;">
					</tbody>
                  
                  </table>
                </div>
                <!-- /.col -->
              </div>
              <!-- /.row -->
              <div class="row" style="font-size: 14px;">
               <div class="col-6">
               <table class="table previewTbl" style="font-size: 14px;">
                      <tr>
                        <th style="width:50%;">Commercial Terms &amp; Conditions :</th>
                      </tr>
                      <tr>
                        <td id="deliverypreview"></td>
                      </tr>
                      <tr>
                        <td id="warrantypreview"></td>
                      </tr>
                      <tr>
                        <td id="paymentpreview"></td>
                      </tr>
                      <tr>
                        <td id="taxpreview"></td>
                      </tr>
                      <tr>
                        <td id="jurisdictionpreview"></td>
                      </tr>
                      <tr>
                        <td><b>INVOICE WILL BE ACCEPTED WITH TEST CERTIFICATES ONLY</b></td>
                      </tr>
                    </table>
               </div>
               <div class="col-6">
                  <div class="table-responsive">
                    <table class="table previewTbl">
                      <tr>
                        <td style="width:50%">Total:</td>
                        <td id="totalInPreview"></td>                      
                        </tr>
                      <tr>
                      <td>GST</td>
                       <td id=sgstInPreview></td>                     
                        </tr>
                    <!--   <tr>
                      <td>CGST @9%</td>
                       <td id=cgstInPreview></td>                     
                        </tr> -->
                      <tr>
                        <th>Grand Total:</th>
                        <th id="grandtotalInPreview"></th>
                      </tr>
                        <tr>
                        <th>Amount in Words:</th>
                        <th id="amountInwordsPreview"></th>
                      </tr>
                    </table>
                  </div>
                </div>
                </div>
                
                <div class="button-div-style" align="center">
						 <button type="button" class="btn btn-primary btn-sm" id="editPoBtn">Edit</button>

                          <button class="btn btn-default btn-sm" data-dismiss="modal">Close</button>
					</div>
                
                <!-- /.col -->
                </div><!-- terms and condition end -->
			</div>			
			
			
			
		</div>
	
</div>
<div class="modal fade" tabindex="-1" role="dialog"
				id="poUploadModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:500px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="poUploadModalHeader">Upload Purchase Order File</h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
				<form id="poUploadForm" style="padding: 5px; " enctype="multipart/form-data">
					<input type="file" name="file" id="file"  accept=".xlsx, .xls" required>
					<button type="submit" class="btn btn-primary btn-sm" >Upload</button>
				</form>
				<div id="response"></div>
				
				<%-- <h1>Upload Sales Order Excel</h1>
    <form id="uploadForm" enctype="multipart/form-data">
        <input type="file" name="file" id="file" accept=".xlsx, .xls">
        <button type="submit">Upload</button>
    </form>
    <div id="response"></div> --%>
			</div>
					<!-- /.modal-content -->
				</div>
				
			</div>
	<!-- Po preview modal ends -->
</body>
</html>