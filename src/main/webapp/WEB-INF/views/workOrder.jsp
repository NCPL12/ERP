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
<script src="<c:url value="/resources/js/workOrder.js" />"></script> 
<script src="${RESOURCES}js/common.js" ></script>
<script type="text/javascript">
var contractorPartyList =${contractorPartyList};
var salesList = ${salesOrderList};
var obj = '${workOrderObj}';
obj = obj.replace(/\&/g, "\'");
obj = obj.replace(/\&/g, "\"");
 var workOrderObj = "";
 if ('${workOrderObj}' != null && '${workOrderObj}' != "") {
	 workOrderObj= $.parseJSON(obj);
 }
</script>
<style>
#purchaseDiv{
	height: 400px;
}
#returnableTable>tbody>tr>td,  #returnableTable>thead>tr>th {
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
		<form id="workOrderForm" method="POST"  action="${pageContext.request.contextPath}/add/work_order">
    		<input type="hidden" name="salesOrder" id="soNumber">
			<input type="hidden" name="partyByType" id="partyId">
			<div id="salesDiv" class="card">
			<div class="card-body table-responsive p-0">
				<table id="workOrderTable" class="table table-head-fixed" style="width: 100%;">
					<thead id="table-header">
						<tr>
							<th width="5%">Sl.No</th>
							<th width="40%">SO Desc.</th>
							<th width="15%">SO Model No.</th>
							<th width="12%">Units</th>
							<th width="7%">Qty</th>
							<th width="7%">Rate</th>
							<th width="7%">Total</th>
						</tr>
					</thead>
           <tbody id="table-body">
						 
					</tbody> 
				</table>
 </div>
        <div class="button-div-style" id="buttonDiv" align="center">
			<button  type="submit" id="saveWorkOrderBtn"class="btn btn-primary btn-sm btn-inline"> Save 
				</button>
		      </div>
			</div>
			</form>
		</div>
		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->


</body>
</html>
