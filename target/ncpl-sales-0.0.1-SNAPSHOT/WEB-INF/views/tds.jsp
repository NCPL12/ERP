<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>


<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<%
request.getSession().invalidate();
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
%>
<!DOCTYPE html>
<html>

<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />



<link rel="stylesheet" href="resources/css/salesOrder.css">
<script src="<c:url value="/resources/js/tds.js" />"></script>
<script src="<c:url value="/resources/js/jquery.tabletojson.js" />"></script>
<script src="${RESOURCES}js/common.js"></script>

<script type="text/javascript">
var customerPartyList =${customerPartyList};
var itemList=${itemList};
var unitsList=${unitsList};
var salesOrderList=${salesOrderList};
var obj = '${salesOrderObj}';
obj = obj.replace(/\&/g, "\'");
obj = obj.replace(/\&/g, "\"");
 var salesOrderObj = "";
 if ('${salesOrderObj}' != null && '${salesOrderObj}' != "") {
	 salesOrderObj= $.parseJSON(obj);
 }
 var userName=${userName};

</script>
<style type="text/css">
.lbl-biiling-popup{
	font-weight: 700;
}
/* .ui-autocomplete {
    max-height: 500px;
    overflow-y: auto;
    overflow-x: hidden;
    padding-right: 20px;
}

<<<<<<< Updated upstream
* html .ui-autocomplete {
    height: 100px;
} */

.CellWithComment{
  position:relative;
}

.CellComment{
  display:none;
  /* position:absolute;  */
  z-index:100;
  border:1px;
  background-color:white;
  border-style:solid;
  border-width:1px;
  border-color:black;
  padding:3px;
  color:black; 
  top:20px; 
  left:20px;
}

.CellWithComment:hover span.CellComment{
  display:block;
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
			<div id="salesDiv" class="card">
				<div class="card-body table-responsive p-0">
					<form:form id="salesOrderTdsForm" method="POST"
						modelAttribute="tds"
						action="${pageContext.request.contextPath}/add/tds">
						
						<input type="hidden" name="soNumber" id="soNumber">
						
						<table id="salesTable" class="table table-head-fixed table-hover">
							<thead id="table-header font">
								<tr>
									<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
									<!-- 	<th width="10%">Items</th> -->
									<th width="25%" rowspan="2" class="thStyle">Description</th>
									<th width="15%" rowspan="2" class="thStyle">Model No</th>
									<th width="8%" rowspan="2" class="thStyle">PO Qty</th>
									<th width="5%" rowspan="2" class="thStyle">Unit</th>
									<th width="15%" colspan="2" style="text-align: center;">Design</th>
									<th width="8%" rowspan="2" class="thStyle">TDS</th>
									<th width="8%" rowspan="2" class="thStyle">Site Qty</th>
								</tr>

								<!--dividing a cloumn into two rows-->
								<tr>
									<th width="9%">Model No</th>
									<th width="6%">Qty</th>
								</tr>
							</thead>
							<tbody id="table-body">
							</tbody>
						</table>
						<div class="button-div-style" align="center" id="buttonDiv">
							<button type="submit" id="saveSalesOrderTds"
								class="btn btn-primary btn-sm btn-inline">Save</button>

							<!-- <button type="button" id="resetSalesOrderTds"
								class="btn btn-primary btn-sm btn-inline">Cancel</button> -->

						</div>
					</form:form>

				</div>

			</div>

		</div>

		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->

</body>

</html>