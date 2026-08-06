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
 <script>
var poList=${poList};
var grnList=${grnList};
var role = ${role};
var pageContext = '${pageContext.request.contextPath}';
</script> 
<style>
.hideTd{
  display:none !important;
}
</style>
  <script src="<c:url value="/resources/js/goodsReceiptNote.js" />"></script> 
<%-- <script src="<c:url value="/resources/js/purchaseOrder.js" />"></script> --%>
<!--  <script type="text/javascript" src="resources/js/goodsReceiptNote.js"></script> -->
<!--   <script src="<c:url value="/resources/js/dashboard.js" />"></script> -->
<!-- <script src="https://editor.datatables.net/extensions/Editor/js/dataTables.editor.min.js"></script>
<script src="https://code.jquery.com/jquery-3.3.1.js"></script>
<script src="https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/buttons/1.6.1/js/dataTables.buttons.min.js"></script>
<script src="https://cdn.datatables.net/select/1.3.1/js/dataTables.select.min.js"></script> -->
</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
			
			<div id="salesDiv" class="card">
			<div class="card-body" style="padding-top: 10px;">
				<table id="grnList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
						 <th width="10%">Grn No.</th>
							<th width="20%">PO Number</th>
							<th class="hideTd"></th>
							<th width="12%">PO Date</th>
							<th width="12%">GRN date</th>
							<th width="25%">Vendor</th>
							<th width="8%">Inv No</th>
							<th width="5%">Total</th>
							<th width="3%">View</th>
							<th width="3%">GRN</th>
							<th width="3%">Archive</th>
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
	<!-- ./wrapper -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="grnViewModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%;height:100%">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="grnViewModalHeader">
						<!-- <b>GRN Preview</b> -->
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="grnViewModalTable" class="dcViewModalTable table table-bordered table-striped" style="width:100%">
					 <thead id="table-header font">
								<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<th width="35%" rowspan="2" class="thStyle">Description</th>
										<th width="10%" rowspan="2" class="thStyle">Model No.</th>
										<th width="7%" rowspan="2" class="thStyle">Units</th>
										<th width="7%" rowspan="2" class="thStyle">Total Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Received Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Remaining Qty</th>
										<th width="10%" rowspan="2" class="thStyle">Unit Price</th>
										<th width="10%" rowspan="2" class="thStyle">Amount</th>
										<th width="2%" rowspan="2" class="thStyle">CA</th>

										
									</tr>

							</thead> 
							<tbody>
							</tbody>
					</table>
				</div>
				 <div class="modal-footer">
					<div class="button-div-style" align="center">
					<button class="btn btn-sm btn-primary btn-inline" id="grnViewBtn">View</button>
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
				</div>
			
			</div>
		</div>
	</div>
	<div class="modal fade" tabindex="-1" role="dialog"
				id="companyAssetModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:900px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="modelId">Company Asset Details</h5>
							 <button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" id="itempriceclose" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button> 
							
						</div>
						 <form id="companyAssetForm"  method="POST" action="${pageContext.request.contextPath}/add/companyAssets">
						 <input type="hidden" name="model" id="modelHidden"/>
						 <input type="hidden" name="returnDate" id="returnDateHidden"/>
						 <input type="hidden" name="date" id="dateHidden"/>
						<div class="modal-body no-padding">
						
						 <div class="row" style="padding-top: 20px;">
						  <div class="col-md-2" id="modelDiv">
                <span class="lbl-biiling-popup"> Model No.</span>
                  <input type="text" id="model" readonly="readonly" class="form-control PositionofTextbox"/>
                  	
                </div>  
                <div class="col-md-2" id="slNoDiv">
                <span class="lbl-biiling-popup"> Sl.No </span>
                  <input type="text" name="slNo" id="slNo"  class="form-control PositionofTextbox" required/>
                  	
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
                  <input type="text" name="brand" id="brand"  class="form-control PositionofTextbox" required/>
                </div>  
                
                <div class="col-md-2" id="siteDiv">
                 <span class="lbl-biiling-popup"> Site : </span> 
                  <input type="text" name="site" id="site"  class="form-control PositionofTextbox" required/>
                </div>  
                
                <div class="col-md-2" id="returnDateDiv">
                 <span class="lbl-biiling-popup"> Return Date : </span> 
                  <input type="text" id="returnDate"  class="form-control PositionofTextbox" required/>
                </div>  
                
                 <div class="col-md-2" id="warrantyDiv">
                 <span class="lbl-biiling-popup">Warranty : </span> 
                  <input type="text" name="warranty" id="warranty"  class="form-control PositionofTextbox" required/>
                </div>  
                
                 <div class="col-md-2" id="valueDiv">
                 <span class="lbl-biiling-popup">Value : </span> 
                  <input type="text" name="value" id="value"  class="form-control PositionofTextbox" required/>
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
						<div style="margin: 20px 20px 20px 20px;">
								</div>
									
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>

</body>
</html>