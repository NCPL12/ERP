<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
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
<script src="${RESOURCES}js/common.js"></script>
<script type="text/javascript">
var returnableObj = ${returnableObjJson};
var returnableItems = ${returnableItemsJson};
</script>
<style>
#returnableViewTable>tbody>tr>td, #returnableViewTable>thead>tr>th{ padding:3px 2px!important; vertical-align:middle; }
.dc-info-card{ border:none; border-radius:10px; box-shadow:0 1px 4px rgba(0,0,0,.06); }
.dc-info-card .card-body{ padding:20px 28px; }
.dc-field-grid{ display:grid; grid-template-columns:180px 16px 1fr; row-gap:12px; column-gap:12px; align-items:center; }
.dc-field-label{ font-weight:700; color:#212529; }
.dc-header-value{ color:#495057; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; display:block; }
</style>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">
<tiles:insertAttribute name="header" />
<tiles:insertAttribute name="sideMenu" />
<div class="content-wrapper">
<div class="card mb-3 dc-info-card">
<div class="card-body">
<div class="dc-field-grid">
<span class="dc-field-label">Returnable No</span><span>:</span><div><span id="vReturnableNo" class="dc-header-value"></span></div>
<span class="dc-field-label">DC No</span><span>:</span><div><span id="vDcNo" class="dc-header-value"></span></div>
<span class="dc-field-label">SO Number</span><span>:</span><div><span id="vSoNumber" class="dc-header-value"></span></div>
<span class="dc-field-label">Client Name</span><span>:</span><div><span id="vClientName" class="dc-header-value"></span></div>
<span class="dc-field-label">Client PO</span><span>:</span><div><span id="vClientPo" class="dc-header-value"></span></div>
<span class="dc-field-label">Shipping Address</span><span>:</span><div><span id="vShipping" class="dc-header-value"></span></div>
<span class="dc-field-label">Date</span><span>:</span><div><span id="vDate" class="dc-header-value"></span></div>
</div>
</div>
</div>
<div class="card">
<div class="card-body table-responsive p-0">
<table id="returnableViewTable" class="table table-head-fixed" style="width:100%;">
<thead><tr>
<th width="5%">Sl.No</th><th width="35%">SO Desc.</th><th width="12%">Model</th><th width="8%">Unit</th><th width="10%">Total Qty</th><th width="10%">Delivered</th><th width="10%">Returned Qty</th>
</tr></thead>
<tbody id="tbody"></tbody>
</table>
</div>
<div style="padding:12px; text-align:center;">
<a href="${pageContext.request.contextPath}/returnableList" class="btn btn-default btn-sm">Back to List</a>
</div>
</div>
</div>
<tiles:insertAttribute name="footer" />
</div>
<script>
$(document).ready(function(){
  if(!returnableObj || returnableObj==='null'){ return; }
  var hdr = returnableObj;
  // header may have transient fields from enrich, fallback to direct
  $('#vReturnableNo').text(hdr.id || hdr.returnableId || '').attr('title', hdr.id||'');
  $('#vDcNo').text(hdr.dcId || hdr.dcNo || '').attr('title', hdr.dcId||'');
  // enriched items carry header copies — use first item if available
  var first = (returnableItems && returnableItems.length) ? returnableItems[0] : null;
  var soNumber = hdr.soNumber || (first && first.soNumber) || '';
  var clientName = hdr.clientName || (first && first.clientName) || '';
  var clientPo = hdr.clientPo || (first && first.clientPo) || '';
  var shipping = hdr.shippingAddress || (first && first.shippingAddress) || '';
  var dateVal = hdr.date || (first && first.date) || hdr.created || (first && first.created) || '';
  $('#vSoNumber').text(soNumber).attr('title', soNumber);
  $('#vClientName').text(clientName).attr('title', clientName);
  $('#vClientPo').text(clientPo).attr('title', clientPo);
  $('#vShipping').text(shipping).attr('title', shipping);
  if(dateVal){ var d=new Date(dateVal); if(!isNaN(d.getTime())){ var dd=("0"+d.getDate()).slice(-2); var m=d.getMonth()+1; var yyyy=d.getFullYear(); var hh=("0"+d.getHours()).slice(-2); var mm=("0"+d.getMinutes()).slice(-2); $('#vDate').text(dd+"-"+m+"-"+yyyy+" "+hh+":"+mm); } else $('#vDate').text(dateVal); }
  var tb=$('#tbody'); tb.empty();
  if(!returnableItems || returnableItems.length===0){ tb.append('<tr><td colspan="7" class="text-center">No items</td></tr>'); return; }
  $.each(returnableItems, function(i, r){
    tb.append('<tr><td>'+(i+1)+'</td><td style="word-break:break-word">'+(r.description||'')+'</td><td>'+(r.soModelNo||'')+'</td><td>'+(r.unit||'')+'</td><td>'+(r.totalQty||'')+'</td><td>'+(r.deliveredQty||'')+'</td><td>'+(r.returnedQty||'')+'</td></tr>');
  });
});
</script>
</body>
</html>
