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
<script type="text/javascript" src="resources/js/workOrderList.js"></script>
<script>

var data=${workOrderList};
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
					<table id="workOrderList" class="table table-bordered table-striped partyListTable">
						 <thead>
						<tr>
						 <th class="hideTd">id</th>
						 <th width="30%">Client Po.No</th> 
							<th width="24%">Company</th>
							<th width="20%">City</th>
							<th class="hideTd"></th>
							<th width="17%">Date</th>
							<!-- <th width="8%">Download</th> -->
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


</body>
</html>