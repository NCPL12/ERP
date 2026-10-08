<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<c:url var="RESOURCES" value="/resources/"></c:url>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />   
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/purchaseOrder.css" />">
<script src="<c:url value="/resources/js/returnableCreate.js" />"></script>
<script src="${RESOURCES}js/common.js"></script>
<script type="text/javascript">
var dcList = ${dcListJson}; 
var preselectedDcId = ${preselectedDcIdJson};
var pageContextPath = '${pageContext.request.contextPath}';
</script>
<style>
#returnableTable>tbody>tr>td, #returnableTable>thead>tr>th { padding: 3px 2px!important; vertical-align: middle; }
.dc-info-card{ border:none; border-radius:10px; box-shadow:0 1px 4px rgba(0,0,0,.06); }
.dc-info-card .card-body{ padding:20px 28px; }
.dc-field-grid{ display:grid; grid-template-columns:180px 16px 1fr; row-gap:12px; column-gap:12px; align-items:center; }
.dc-field-label{ font-weight:700; color:#212529; }
</style>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">
<tiles:insertAttribute name="header" />
<tiles:insertAttribute name="sideMenu" />
<div class="content-wrapper">
<form id="returnableCreateForm" method="POST" action="${pageContext.request.contextPath}/add/returned_items">
<input type="hidden" name="dcId" id="dcId" />
<div class="card mb-3 dc-info-card">
<div class="card-body">
<div class="dc-field-grid">
<span class="dc-field-label">DC No.</span><span>:</span>
<div><select class="form-control select2" id="dcDropdown" style="width:100%"><option value="">Select DC No.</option></select></div>
<span class="dc-field-label">Client</span><span>:</span><div><span id="rcClientName">-</span></div>
<span class="dc-field-label">SO Number</span><span>:</span><div><span id="rcSoNumber">-</span></div>
</div>
</div>
</div>
<div id="salesDiv" class="card">
<div class="card-body table-responsive p-0">
<table id="returnableTable" class="table table-head-fixed" style="width:100%;">
<thead><tr>
<th width="5%">Sl.No</th><th width="35%">SO Desc.</th><th width="12%">Model</th><th width="8%">Unit</th><th width="7%">Total Qty</th><th width="7%">Delivered</th><th width="7%">Today's Qty</th><th width="12%">Returned Qty</th>
</tr></thead>
<tbody id="table-body"></tbody>
</table>
</div>
<div class="button-div-style" id="buttonDiv" align="center" style="padding:12px;">
<button type="submit" id="saveReturnableBtn" class="btn btn-primary btn-sm">Save</button>
<a href="${pageContext.request.contextPath}/returnableList" class="btn btn-default btn-sm">Cancel</a>
</div>
</div>
</form>
</div>
<tiles:insertAttribute name="footer" />
</div>
</body>
</html>
