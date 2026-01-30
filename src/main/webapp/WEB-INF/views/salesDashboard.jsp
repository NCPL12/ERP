<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
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
 <script>
var dataObj=${salesOrderList};
var role = ${role};
var user = ${user};
var pageContext = '${pageContext.request.contextPath}';
</script> 

<script src="<c:url value="/resources/js/dashboard.js" />"></script>
<script src="<c:url value="/resources/js/tds.js" />"></script>

<style type="text/css">
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
									href="${pageContext.request.contextPath}/new_salesOrder"
									title="Add New SalesOrder" id="Home"
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
			<div class="card-body" style="padding-top: 10px;">
				<table id="salesList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
						 <th class="hideTd"><spring:message code="so.header"/></th>
						 <th width="20%"><spring:message code="client.po"/></th> 
							<th width="22%"><spring:message code="so.company"/></th>
							<th width="8%"><spring:message code="so.city"/></th>
							<%-- <th width="14%"><spring:message code="so.state"/></th>
							<th width="14%"><spring:message code="so.country"/></th>
							<th width="5%"><spring:message code="so.quantity"/></th> --%>
							<th width="10%"><spring:message code="so.total"/></th>
							<th class="hideTd"></th> 
							<th width="15%"><spring:message code="date"/></th> 
							<th width="5%"><spring:message code="site.input"/></th> 
							<th width="4%">SO Download</th>
							<th width="4%">Material Tracker</th>
							<th width="4%">Client PO Upload</th>
							<th width="3%">Client PO Download</th>
							<th width="3%">Design Upload</th>
							<th width="2%">Archive</th>
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
<!--Status popup Starts  -->
<div class="modal fade" tabindex="-1" role="dialog"
				id="statusModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:800px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="statusHeader">Close Project</h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
						<form class="form" id="statusUpdateForm">	
						<div class="modal-body no-padding">
						
						 	 <div class="row" style="padding: 10px;">
    						<label for="clientPoNumber" class="col-form-label form-control-sm col-sm-1.2">Client PO</label>
   								 <div class="col-sm-2.6">
     								 <input type="text" id="clientPoNum" name="clientPoNumber" class="form-control form-control-sm" readonly />
   						 		</div>
   						 		
   						 		<label for="actualClosureDate" class="col-form-label form-control-sm col-sm-1.2">Actual Closure Date</label>
   								 <div class="col-sm-2">
     								 <input type="text" id="actualClosureDate" name="actualClosureDate" class="form-control form-control-sm" autoComplete="off" />
   						 		</div>
   						 		
   						 	<label for="dlp" class="col-form-label form-control-sm col-sm-1">DLP</label>
   								 <div class="col-sm-2">
     								 <input type="text" id="dlp" name="dlp" class="form-control form-control-sm" autoComplete="off"/>
   						 	</div>
 						 </div>
 						  <div class="row" style="padding: 10px;">
 						  <label for="certificate"  class="col-form-label form-control-sm col-sm-1.5">Certificate</label>
   								 <div class="col-sm-6">
     								 <input type="file"  name="cartificate" id="certificate" class="form-control form-control-sm" />
   						 		</div>
 						  </div>
						</div>
						<div style="margin: 0px 0px 10px 10px;">
							<div class="button-div-style" align="center">
						
								<button type="submit"
									class="btn btn-primary btn-sm btn-inline" id="submitCloseProjectBtn">Save</button>

								<button type="button" class="btn btn-primary btn-sm buttonDismiss" data-dismiss="modal">Close</button>

							</div>
							</div>
						</form>
					</div>
					<!-- /.modal-content -->
				</div>
				
			</div>
<div class="modal fade" tabindex="-1" role="dialog"
				id="clientPoUploadModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:500px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="clientPoUploadHeader">Upload Client PO File</h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
						<input type="hidden" id="salesOrderNum" name="salesOrderNum" class="form-control form-control-sm" />
				<form id="clientPoUploadForm" style="padding: 5px;">
					<input type="file" id="fileInput" accept="application/pdf" required>
					<button type="submit" class="btn btn-primary btn-sm" >Upload</button>
				</form>
				<div class="response" id="responseMessage"></div>
				
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
			
			<div class="modal fade" tabindex="-1" role="dialog"
				id="soUploadModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:500px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="soUploadModalHeader">Upload Sales Order File</h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
				<form id="uploadForm" style="padding: 5px; " enctype="multipart/form-data">
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
			
			<div class="modal fade" tabindex="-1" role="dialog"
				id="designUploadModal" >
				<div class="modal-dialog" role="document">
					<div class="modal-content" style="width:500px">
						<div class="modal-header custom-box-header-modal">
						<h5  class="modal-title" id="designUploadHeader">Upload Design File</h5>
							<button type="button" class="close buttonDismiss" data-dismiss="modal"
								aria-label="Close" style="outline: none;">
								<span aria-hidden="true" >&times;</span>
								
							</button>
							
						</div>
				<form id="designuploadForm" style="padding: 5px; " enctype="multipart/form-data">
				<input type="hidden" name="clientPoNumber" id="clientPo" />
					<input type="file" name="file" id="excelfile"  accept=".xlsx, .xls" required>
					<button type="submit" class="btn btn-primary btn-sm" >Upload</button>
				</form>
				<div id="designresponse"></div>
				
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
</body>
</html>