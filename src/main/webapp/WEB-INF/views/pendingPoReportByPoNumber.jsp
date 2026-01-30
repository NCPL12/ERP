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
<script src="<c:url value="/resources/js/pendingPoReportByPoNumber.js" />"></script>
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<%-- <script src="<c:url value="resources/js/oustandingStock.js" />"></script> --%>
<script>
var pendingPoList=${pendingPoList};
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
					<table id="pendingreportByPoNumberlist" class="table table-bordered table-striped partyListTable">
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