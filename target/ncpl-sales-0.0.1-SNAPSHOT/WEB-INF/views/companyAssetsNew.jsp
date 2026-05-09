
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
	<link rel="stylesheet"
	href="<c:url value="/resources/css/item-master.css" />">
<!--  <script src="<c:url value="/resources/js/salesOrder.js" />"></script> -->
 <%-- <script src="<c:url value="/resources/js/itemMaster.js" />"></script>  --%>
<script src="${RESOURCES}js/common.js" ></script>


<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>
<script type="text/javascript">
/* var itemList=${companyAssetsList};
var customerPartyList=${customerPartyList};
var supplierPartyList=${supplierPartyList};
var allSupplierslist = ${allSupplierslist};
var allStocksList = ${allStocksList};
var makeList=${makeList}; */
var role = ${role};
var user = ${user};
</script>
<style type="text/css">
.custom-box-header {
	color: color: #343a40 !important;
	padding: 4px 4px 4px 4px !important;
	background: #4e595f !important;
	background-color: rgba(45, 147, 203, 0.84) !important;
	margin:0;
}
.header-font {
	font-size: 21px !important;
}
#units + .select2-container--default .select2-selection--single .select2-selection__rendered {
    margin-top: -5px!important;
 }
 #units + .select2-container--default .select2-selection--single {
    border: 1px solid #ced4da!important;
    height: 32px!important;
	padding-top: 10px!important;
}
#clientName + .select2-container--default .select2-selection--single {
    border: 1px solid #ced4da!important;
    height: 32px!important;
	padding-top: 10px!important;
}
#supplierName + .select2-container--default .select2-selection--single {
    border: 1px solid #ced4da!important;
    height: 32px!important;
	padding-top: 10px!important;
}
.footerDivinType{
    width: 180px;
    margin-left: auto;
    margin-right: auto;
} 
.makeTable{
	width:100%!important;
}
.disabled {
  color: #666;
  cursor: not-allowed;
}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		 <tiles:insertAttribute name="header" /> 
		
		<tiles:insertAttribute name="sideMenu" />
		

		<div class="content-wrapper">
			

			<div style="margin: 20px 20px 20px 20px;">
			
				<table id="companyAssetList"
					class='table table-bordered table-striped dataTable'
					style="width: 100%; margin-top: -15px; font-size:13px">

				</table>


			</div>


		</div>

		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->
	<!-- ./box-body -->
	
	

</body>

</html>