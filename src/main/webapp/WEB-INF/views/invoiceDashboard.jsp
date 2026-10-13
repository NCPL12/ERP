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
<style>
.hideTd{
  display:none !important;
}
</style>
<script src="<c:url value="/resources/js/invoiceDashboard.js" />"></script> 

<script type="text/javascript">
var invoiceList=${invoiceList};
var pageContext = '${pageContext.request.contextPath}';
</script>
</head>
<body>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />
		<div class="content-wrapper">
		<div id="salesDiv" class="card">
			<div class="card-body" style="padding-top: 10px;">
				<table id="invoiceList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
						 <th width="15%">Invoice No.</th>
						 	<th class="hideTd"></th>
							<th width="15%">SO Number</th>
							<th width="10%">Type</th>
							<th width="10%">Dc No.</th>
							<th width="15%">Client PO No.</th>
							<th width="15%">GrandTotal</th>
							<th width="20%">Invoice</th>
							
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
</body>
</html>