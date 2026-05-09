
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


<!-- <link rel="stylesheet" href="resources/css/salesOrder.css"> -->
<link rel="stylesheet"
	href="<c:url value="/resources/css/salesOrder.css" />">
	
<!--  <script src="<c:url value="/resources/js/salesOrder.js" />"></script> -->
<script src="<c:url value="/resources/js/companyAsset.js" />"></script>
<script src="${RESOURCES}js/common.js" ></script>


<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<script type="text/javascript">
var companyAssetList=${companyAssetList};
var role = ${role};
var user = ${user};
</script>
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
			
		<div id="companyAssetDiv" class="card">
			<div class="card-body" style="padding-top: 10px;">
				<table id="companyAssetList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					 <thead>
						<tr>
							<th width="10%">Model No</th>
							<th width="3%">Sl.No</th>
							<th width="8%">Custodian</th>
							<th width="25%">Features</th>
							<th width="10%">Brand</th>
							<th width="10%">Site</th>
							<th width="7%">Return Date</th>
							<th width="10%">Warranty</th>
							<th width="10%">Value</th>
							<th width="7%">Date & TimeStamp</th>
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
	<!-- ./box-body -->
	
</body>

</html>