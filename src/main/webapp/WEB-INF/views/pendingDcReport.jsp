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
<script src="<c:url value="/resources/js/pendingDcreport.js" />"></script>
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<%-- <script src="<c:url value="resources/js/oustandingStock.js" />"></script> --%>
<script>
var pendingDcList=${pendingDcList};
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
			<%-- <tiles:insertAttribute name="pageHeader" /> --%>
			<%-- <div class="content-header content-header-padding">
				<div class="container-fluid">
					<div class="row mb-2">
						<div class="col-sm-6">
							<h1 class="m-0 text-dark">${pageHeader}
								&nbsp;&nbsp;&nbsp;&nbsp; <small> <a
									href="${pageContext.request.contextPath}/party"
									title="Add New Party" id="Home"
									style="font-size: 16px; color: black;"> <i
										class="fa fa-plus-square" style="font-size: 24px"></i></a>
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
					<table id="pendingDcreportlist" class="table table-bordered table-striped partyListTable">
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