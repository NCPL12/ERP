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

<script src="<c:url value="/resources/js/returnable.js" />"></script> 
<script src="${RESOURCES}js/common.js" ></script>
<script type="text/javascript">
var dcItemList=${dcItemList};
</script>
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
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
		<form id="returnableForm" method="POST"  action="${pageContext.request.contextPath}/add/returned_items">
    		<input type="hidden" id="partyId">
    		<input type="hidden" name="dcId" id="dcId">
			<div id="salesDiv" class="card">
			<div class="card-body table-responsive p-0">
				<table id="returnableTable" class="table table-head-fixed" style="width: 100%;">
					<thead id="table-header">
						<tr>
							<th width="5%">Sl.No</th>
							<th width="40%">SO Desc.</th>
							<th width="15%">SO Model No.</th>
							<th width="12%">Units</th>
							<th width="7%">Total Qty</th>
							<th width="7%">Delivered Qty</th>
							<th width="7%">Returned Qty</th>
						</tr>
					</thead>
           <tbody id="table-body">
						 
					</tbody> 
				</table>
 </div>
        <div class="button-div-style" id="buttonDiv" align="center">
			<button  type="submit" id="savereturnedItemsBtn"class="btn btn-primary btn-sm btn-inline"> Save 
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
