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
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<script src="<c:url value="/resources/js/dcListByItemReport.js" />"></script>
<script>
var dcList=${dcList};
var pageContext = '${pageContext.request.contextPath}';
</script>
<style>
	/*styled to fix custom length**/
	.partyListTable{
		font-size:13px;
		margin: 0 auto;
		width: 100%;
		clear: both;
		border-collapse: collapse;
		table-layout: fixed;
		word-wrap:break-word;
	}
	.hideTd{
  display:none;
}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
			<div id="salesDiv" class="card">
				<div class="card-body" style="padding-top: 10px;">
					<table id="dcList" class="table table-bordered table-striped partyListTable">
					 <thead>
						<tr>
						 <th width="10%">Dc No.</th>
							<th width="15%">SO Number</th>
							<th width="23%">Client Name</th>
							<th width="14%">Client PO</th>
							<th width="23%%">Shipping Address</th>
							
							
						</tr>
					</thead>
						<tbody>
						</tbody>
					</table>
				</div>
			</div>

		</div>
		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->


</body>
</html>