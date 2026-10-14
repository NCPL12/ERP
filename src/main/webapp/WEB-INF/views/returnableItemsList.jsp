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
<script type="text/javascript" src="resources/js/returnableItemList.js"></script>
<script>
// @D0014 lazy-loaded, paginated Returnables list (see README.md)
var pageContext = '${pageContext.request.contextPath}';
</script>
<style>
	.content-wrapper{ overflow-x:hidden; }
	#salesDiv{ width:100%; max-width:100%; }
	#salesDiv .card-body{ width:100%; padding:10px 12px 8px; }
	.dataTables_wrapper{ width:100% !important; }
	.dataTables_wrapper .row:first-child{ margin:0 !important; }
	#returnableItemsList_wrapper .dataTables_filter{ margin:0 !important; }
	#returnableItemsList_wrapper .dataTables_filter label{ margin:0 !important; font-weight:400; }
	#returnableItemsList_wrapper .dataTables_filter input{ margin-left:6px !important; }
	table.dataTable{ width:100% !important; margin:6px 0 0 !important; }
	.partyListTable{
		font-size:13px;
		width:100% !important;
		border-collapse:collapse;
		table-layout:fixed;
	}
	.partyListTable th{
		white-space:nowrap;
		overflow:hidden;
		text-overflow:ellipsis;
		padding:8px 6px !important;
		font-weight:600;
		background:#f8f9fa;
	}
	.partyListTable td{
		padding:6px 6px !important;
		vertical-align:middle;
		overflow:hidden;
		text-overflow:ellipsis;
		white-space:nowrap;
	}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini layout-fixed">
	<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
			<div id="returnableCreateBtnWrap" style="display:none; padding:0; margin:0;">
				<a href="${pageContext.request.contextPath}/returnable/create" class="btn btn-primary btn-sm"><i class="fa fa-plus"></i> Create Returnable</a>
			</div>
			<div id="salesDiv" class="card" style="width:100%; margin-bottom:0;">
				<div class="card-body" style="padding:10px 12px 8px; width:100%;">
					<table id="returnableItemsList" class="table table-bordered table-striped partyListTable" style="width:100%;">
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