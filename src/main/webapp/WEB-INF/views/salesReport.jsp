<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>


<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/purchaseOrder.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/salesReport.css" />">
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
<script defer src="<c:url value="/resources/js/salesReport.js?v=20260929-tally-po" />"></script>
<script type="text/javascript">
var clientList=${clientList};
var vendorList=${vendorList};
var stockSummaryError = '<c:out value="${stockSummaryError}" escapeXml="true"/>';
</script>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">
		<tiles:insertAttribute name="header" />

		<tiles:insertAttribute name="sideMenu" />
		
		<div class="content-wrapper">
		<div class="card-body">
		<section class="content">
		<security:authorize access="hasAnyAuthority('ADMIN','SUPER ADMIN','SALES','PURCHASE STORE')">
			<div class="row">

				<div class="col-md-6">
						<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="stock.history.by.date" /><small>&nbsp;Last 30 days from the selected date</small>
							  </h5>
						  	<form action="${pageContext.request.contextPath}/stock_history/Download" id="stockHistoryForm" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
									    <label for="staticEmail" class="col-sm-2 ">Date</label>
									    <div class="col-sm-10">
									     <input type="text" id="reportDate" name="reportDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									  </div>
							  </div>
							  <div  class="card-footer">
							  <button type=submit id="downLoadStockHistoryReportBtn"
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>

				<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="stock.summary.report.by.date" />
							  </h5>
						  	<form action="${pageContext.request.contextPath}/stock/summary_details/" id="stockSummaryForm" method="get" target="_blank">
							  <div class="card-body">
								   <div class="form-group row mb-1">
									    <label for="reportFromDate" class="col-sm-2 col-form-label">From</label>
									    <div class="col-sm-10">
									     <input type="text" autocomplete="off" name="reportFromDate" id="reportFromDate" class="form-control PositionofTextbox">
									    </div>
								   </div>
								   <div class="form-group row mb-1">
									    <label for="reportToDate" class="col-sm-2 col-form-label">To</label>
									    <div class="col-sm-10">
									     <input type="text" autocomplete="off" name="reportToDate" id="reportToDate" class="form-control PositionofTextbox">
									    </div>
								   </div>
							 </div>
							<div  class="card-footer">
								    <button type="submit" id="downLoadStockSummaryReportBtn"
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>

				</div>
			</div>
			</security:authorize>
			<security:authorize access="hasAnyAuthority('ADMIN','SUPER ADMIN')">
			<%-- Emergency Finance correction tool. Keep hidden during normal operation;
				 remove display:none when an approved snapshot must be imported. --%>
			<div id="approvedStockSnapshotCorrection" class="row" style="padding-top:10px; display:none;">
				<div class="col-md-12">
					<div class="card w-100">
						<h5 class="card-header bg-light" style="font-size: inherit;">
							Approved Month-End Stock Snapshot
						</h5>
						<form action="${pageContext.request.contextPath}/api/monthly_stock_report/baseline/import"
								method="post" enctype="multipart/form-data" target="_blank">
							<div class="card-body">
								<div class="form-group row mb-1">
									<label for="monthlyStockBaselineFile" class="col-sm-2 col-form-label">Approved Excel</label>
									<div class="col-sm-10">
										<input type="file" id="monthlyStockBaselineFile" name="file"
												class="form-control" accept=".xlsx" required>
									</div>
								</div>
								<div class="form-group row mb-1">
									<label for="monthlyStockClosingDate" class="col-sm-2 col-form-label">Closing Date</label>
									<div class="col-sm-10">
										<input type="text" id="monthlyStockClosingDate" name="closingDate"
												class="form-control PositionofTextbox" placeholder="dd-MM-yyyy"
												autocomplete="off" required>
									</div>
								</div>
								<small class="text-muted">Import only a Finance-approved month-end Current Item Stock file. This records the closing boundary; it does not alter live stock.</small>
							</div>
							<div class="card-footer">
								<button type="submit" class="btn btn-primary btn-sm btn-inline pull-right">
									<i class="fa fa-fw fa-upload"></i> Import Approved Closing
								</button>
							</div>
						</form>
					</div>
				</div>
			</div>
			</security:authorize>
			<security:authorize access="hasAnyAuthority('ADMIN','SUPER ADMIN','SALES','PURCHASE STORE')">
			<div class="row" style="padding-top:10px">

				<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="stock.report.by.region" />
							  </h5>
						  	<form action="${pageContext.request.contextPath}/stock_report_by_region/Download" id="stockRegionFrom" method="get">
							  <div class="card-body">
								   <div class="form-group row mb-1">
								   		<label for="region" class="col-sm-2 col-form-label">Region</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="region" id="region" style="padding: 0;">
									     <option value="" selected>Select Region</option>
									     <option value="Bangalore">Bangalore</option>
									     <option value="Mangalore">Mangalore</option>
									     </select>
									    </div>
								   </div>
								   <div class="form-group row mb-1">
									    <label for="reportByRegionFromDate" class="col-sm-2 col-form-label">From</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionFromDate" id="reportByRegionFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
								   </div>
								   <div class="form-group row mb-1">
									    <label for="reportByRegionToDate" class="col-sm-2 col-form-label">To</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionToDate" id="reportByRegionToDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
								   </div>
							  </div>
								<div  class="card-footer">		  
								    <button type="submit" id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		Daily Report
							  </h5>
						  	<form action="${pageContext.request.contextPath}/stock_report_by_date/Download" id="stockReportByDateForm" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
									    <label for="staticEmail" class="col-sm-2 ">Date</label>
									    <div class="col-sm-10">
									     <input type="text" id="date" name="date" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									  </div>
							  </div>
							  <div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				</div>
				</security:authorize>
			<div class="row" style="padding-top:10px">
			<security:authorize access="hasAnyAuthority('ADMIN','SUPER ADMIN','PURCHASE','SALES','PURCHASE STORE')">
			<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="outstanding.stock.report" />
							  </h5>
						  	<form  id="outtandingReportForm" action="${pageContext.request.contextPath}/stock/outstandingReport/" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="staticEmail" class="col-sm-2 ">Client</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="client" id="client" style="padding: 0;">
									     <option value="" selected>Select Client</option>
									   
									     
									     </select>
									    </div>
									    <!-- <label for="staticEmail" class="col-sm-2 ">From</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionFromDate" id="reportByRegionFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									    <label for="staticEmail" class="col-sm-2 ">To</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionToDate" id="reportByRegionToDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div> -->
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				</security:authorize>
				 <security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES')">
					<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="pending.report" />
							  </h5>
						  	<form  id="pendingReportForm" action="${pageContext.request.contextPath}/stock/pendingporeport/" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="staticEmail" class="col-sm-2 ">Vendor</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="clientInPendingReport" id="clientInPendingReport" style="padding: 0;">
									     <option value="" selected>Select Vendor</option>
									   
									     
									     </select>
									    </div>
									    <!-- <label for="staticEmail" class="col-sm-2 ">From</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionFromDate" id="reportByRegionFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									    <label for="staticEmail" class="col-sm-2 ">To</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionToDate" id="reportByRegionToDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div> -->
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				</security:authorize>
			</div>
			
			 
			<div class="row" style="padding-top:10px">
			<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE')">
			<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="pending.dc.report" />
							  </h5>
						  	<form  id="dcReportForm" action="${pageContext.request.contextPath}/stock/dcReport/" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="staticEmail" class="col-sm-2 ">Client</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="clientDcPEnding" id="clientDcPEnding" style="padding: 0;">
									     <option value="" selected>Select Client</option>
									   
									     
									     </select>
									    </div>
									    <!-- <label for="staticEmail" class="col-sm-2 ">From</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionFromDate" id="reportByRegionFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									    <label for="staticEmail" class="col-sm-2 ">To</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionToDate" id="reportByRegionToDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div> -->
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				</security:authorize>
				<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES')">
				
					<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="poitem.history" />
							  </h5>
						  	<form  id="poItemHistoryForm" action="${pageContext.request.contextPath}/poItem/History/" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="staticEmail" class="col-sm-2 ">Po Item</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="poItemHistoryReport" id="poItemHistoryReport" style="padding: 0;">
									     <option value="" selected>Select Item</option>
									   
									     
									     </select>
									    </div>
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				</security:authorize>
			</div>
			
			
			
			<!-- SO list by item report starts -->
			<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES')">
			<div class="row" style="padding-top:10px">
			<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="sales.list.by.item.id" />
							  </h5>
						  	<form  id="salesListForm" action="${pageContext.request.contextPath}/sales_list/by_item_id" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="item" class="col-sm-2 ">Items</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="item" id="itemId" style="padding: 0;">
									     <option value="" selected>Select Model Number:</option>
									   
									     
									     </select>
									    </div>
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
					<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="pending.report.by.ponumber" />
							  </h5>
						  	<form  id="pendingPoByPoNumberReportForm" action="${pageContext.request.contextPath}/stock/pendingporeport/byPoNumber" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="staticEmail" class="col-sm-2 ">PoNumber</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="poNumber" id="poNumber" style="padding: 0;">
									     <option value="" selected>Select PoNumber</option>
									   
									     
									     </select>
									    </div>
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
			</div>
			</security:authorize>
			<!-- /SO list by item report ends -->
			
			<!-- Dc List By Item Report Starts -->
			
			<div class="row" style="padding-top:10px">
			<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE')">
			<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="dc.list.by.item" />
							  </h5>
						  	<form  id="DcListByItemForm" action="${pageContext.request.contextPath}/dc_list/by_item" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="item" class="col-sm-2 ">Items</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="designItemId" id="designItemId" style="padding: 0;">
									     <option value="" selected>Select Model Number:</option>
									   
									     
									     </select>
									    </div>
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
			</security:authorize>		
			<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES')">
			
			<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="po.list.by.date" />
							  </h5>
						  	<form id="poLostByDateForm" action="${pageContext.request.contextPath}/po_list/by_date" method="get">
								 <div class="card-body">
								   <div class="form-group row mb-1">
								    <label for="poListByFromDate" class="col-sm-2 col-form-label">From</label>
								    <div class="col-sm-10"><input type="text" autocomplete="off" name="poListByFromDate" id="poListByFromDate" required class="form-control PositionofTextbox"></div>
								   </div>
								   <div class="form-group row mb-1">
								    <label for="poListByToDate" class="col-sm-2 col-form-label">To</label>
								    <div class="col-sm-10"><input type="text" autocomplete="off" name="poListByToDate" id="poListByToDate" required class="form-control PositionofTextbox"></div>
								   </div>
								   <p id="tallyPoAutoSyncStatus" class="text-info mb-2">Checking automatic PO sync status...</p>
                                   <p class="text-muted mb-2">Tally purchase and GST ledgers are selected automatically from each ERP PO and item master.</p>
								   <small id="tallyPoConnectionStatus" class="form-text ${empty tallyOptionsError ? 'text-success' : 'text-danger'}"><c:choose><c:when test="${not empty tallyOptionsError}"><c:out value="${tallyOptionsError}" /></c:when><c:otherwise><i class="fa fa-check-circle"></i> <c:out value="${tallyCompanyName}" /> is ready</c:otherwise></c:choose></small>
								 </div>
								 <div class="card-footer d-flex justify-content-end" style="gap: 6px;">
								  <a href="${pageContext.request.contextPath}/sales_report" class="btn btn-default btn-sm"><i class='fa fa-fw fa-refresh'></i> Check Tally</a>
								  <button type="submit" formaction="${pageContext.request.contextPath}/po_list/tally/import" formmethod="post" class="btn btn-success btn-sm" ${not empty tallyOptionsError ? 'disabled' : ''}><i class='fa fa-fw fa-exchange'></i> Send to Tally</button>
								  <button type="submit" class="btn btn-primary btn-sm"><i class='fa fa-fw fa-download'></i> Download</button>
								 </div>
								 <c:if test="${not empty tallyPoImportResult}">
								  <div class="alert alert-info m-2">Total: ${tallyPoImportResult.total} | Imported: ${tallyPoImportResult.imported} | Skipped: ${tallyPoImportResult.skipped} | Failed: ${tallyPoImportResult.failed}</div>
								  <div class="table-responsive tally-po-result m-2">
								   <table class="table table-sm table-bordered mb-0">
								    <thead><tr><th>ERP PO number</th><th>Status</th><th>Details</th></tr></thead>
								    <tbody>
								     <c:forEach items="${tallyPoImportResult.records}" var="record">
								      <tr class="${record.status eq 'FAILED' ? 'table-danger' : ((record.status eq 'IMPORTED' or record.status eq 'UPDATED') ? 'table-success' : 'table-warning')}">
								       <td><c:out value="${record.poNumber}" /></td>
								       <td><strong><c:out value="${record.status}" /></strong></td>
								       <td><c:out value="${record.message}" /></td>
								      </tr>
								     </c:forEach>
								    </tbody>
								   </table>
								  </div>
								 </c:if>
								 <c:if test="${not empty tallyPoImportError}"><div class="alert alert-danger m-2"><c:out value="${tallyPoImportError}" /></div></c:if>
							  </form>
						</div>
				</div>
				</security:authorize>
				</div>	
			
			<div class="row" style="padding-top:10px">
			<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES')">
			<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="active.sales.order.by.customer" />
							  </h5>
						  	<form  id="activeSalesOrderForm" action="${pageContext.request.contextPath}/active_sales/by_customer" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="item" class="col-sm-2 ">Client</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="clientName" id="clientName" style="padding: 0;">
									     <option value="" selected>Select Client Name:</option>
									   
									     
									     </select>
									    </div>
									  </div>
									</div>
								<div  class="card-footer">		  
								    <button type=submit id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				</security:authorize>
				<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE')">
				<div class="col-md-6">
					<div class="card w-100">
							  <h5 class="card-header  bg-light" style="font-size: inherit;">
							  		<spring:message code="itemwise.grn.report" />
							  </h5>
						  	<form  id="grnByDateForm" action="${pageContext.request.contextPath}/grn_itemwise/by_date" method="get">
							  <div class="card-body">
								   <div class="form-group row mb-1">
									    <label for="grnreportByRegionFromDate" class="col-sm-2 col-form-label">From</label>
									    <div class="col-sm-10">
									     <input type="text" name="grnreportByRegionFromDate" id="grnreportByRegionFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
								   </div>
								   <div class="form-group row mb-1">
									    <label for="grnreportByRegionToDate" class="col-sm-2 col-form-label">To</label>
									    <div class="col-sm-10">
									     <input type="text" name="grnreportByRegionToDate" id="grnreportByRegionToDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
								   </div>
							  </div>
								<div  class="card-footer">		  
								    <button type="submit" id=""
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
							  </div>
						  </form>
						</div>
				</div>
				</security:authorize>
				</div>
			
		<!-- Dc List By Item Report ends -->
			
			
		<!-- DC Report By Date + GRN and PO Details by Model Report -->
		<div class="row" style="padding-top:10px">
		<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE')">
		<div class="col-md-6">
				<div class="card w-100">
						  <h5 class="card-header  bg-light" style="font-size: inherit;">
						  		<spring:message code="dc.report.by.date" />
						  </h5>
					  	<form id="dcListByDateForm" action="${pageContext.request.contextPath}/dc_list/by_date" method="get">
							 <div class="card-body">
								   <div class="form-group row mb-1">
									    <label for="dcFromDate" class="col-sm-2 col-form-label">From</label>
									    <div class="col-sm-10">
									     <input type="text" autocomplete="off" name="fromDate" id="dcFromDate" class="form-control PositionofTextbox">
									    </div>
								   </div>
								   <div class="form-group row mb-1">
									    <label for="dcToDate" class="col-sm-2 col-form-label">To</label>
									    <div class="col-sm-10">
									     <input type="text" autocomplete="off" name="toDate" id="dcToDate" class="form-control PositionofTextbox">
									    </div>
								   </div>
							 </div>
							<div  class="card-footer">		  
							    <button type="submit" id=""
													class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
						  </div>
					  </form>
					</div>
			</div>
		</security:authorize>
		<security:authorize access="hasAnyAuthority('ADMIN','SALES','PURCHASE','PURCHASE STORE')">
		<div class="col-md-6">
				<div class="card w-100">
						  <h5 class="card-header  bg-light" style="font-size: inherit;">
						  		GRN and PONumber by Models
						  </h5>
					  	<form id="grnPoByModelForm">
						  <div class="card-body cardHeight">
							   <div class="form-group row">
							   		<label for="modelNo" class="col-sm-2 ">Model Number</label>
								    <div class="col-sm-10">
								     <select class="form-control select2 PositionofTextbox" name="modelNo" id="modelNoSelect" style="padding: 0;">
								     <option value="" selected>Select Model Number:</option>
								     </select>
								    </div>
								  </div>
							</div>
						<div  class="card-footer">		  
							    <button type="button" id="searchGrnPoBtn"
											class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-search'></i> Search</button>
						  </div>
					  </form>
					</div>
			</div>
		</security:authorize>
		</div>
		
		<!-- DC Report By Date + GRN and PO Details by Model Report ends -->


		<!-- Monthly Stock Movement Report -->
		<div class="row" style="padding-top:10px">
		<security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE')">
		<div class="col-md-6">
			<div class="card w-100">
				<h5 class="card-header bg-light" style="font-size: inherit;">
					Monthly Stock Movement Report
				</h5>
				<form id="monthlyMovementForm" action="${pageContext.request.contextPath}/monthly_report/by_date" method="get">
					<div class="card-body">
						<div class="form-group row mb-1">
							<label for="monthlyFromDate" class="col-sm-2 col-form-label">From</label>
							<div class="col-sm-10">
								<input type="text" autocomplete="off" name="fromDate" id="monthlyFromDate" class="form-control PositionofTextbox">
							</div>
						</div>
						<div class="form-group row mb-1">
							<label for="monthlyToDate" class="col-sm-2 col-form-label">To</label>
							<div class="col-sm-10">
								<input type="text" autocomplete="off" name="toDate" id="monthlyToDate" class="form-control PositionofTextbox">
							</div>
						</div>
					</div>
					<div class="card-footer">
						<button type="submit" class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
					</div>
				</form>
			</div>
		</div>
		</security:authorize>
		</div>
		<!-- Monthly Stock Movement Report ends -->


		</section>
		</div>
		</div>
		<tiles:insertAttribute name="footer" />
		</div>
		
		<!-- GRN/PO Results Modal -->
		<div class="modal fade" id="grnPoResultsModal" tabindex="-1" role="dialog">
			<div class="modal-dialog modal-xl" role="document">
				<div class="modal-content">
					<div class="modal-header custom-box-header-modal">
						<h5 class="modal-title">GRN and PO Details</h5>
						<button type="button" class="close" data-dismiss="modal" aria-label="Close">
							<span aria-hidden="true">&times;</span>
						</button>
					</div>
					<div class="modal-body">
						<div class="table-responsive">
							<table id="grnPoResultsTable" class="table table-bordered table-striped">
								<thead>
									<tr>
										<th>GRN Number</th>
										<th>PO Number</th>
										<th>GRN Date</th>
										<th>Vendor</th>
										<th>PO Date</th>
										<th>Invoice Number</th>
									</tr>
								</thead>
								<tbody>
									<!-- Results will be populated here -->
								</tbody>
							</table>
						</div>
						<div id="grnPoNoResults" class="alert alert-info" style="display: none;">
							No records found for the selected model number.
						</div>
						<div id="grnPoLoading" class="text-center" style="display: none;">
							<i class="fa fa-spinner fa-spin"></i> Loading...
						</div>
					</div>
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					</div>
				</div>
			</div>
		</div>

		<div class="modal fade" id="tallyPoResultModal" tabindex="-1" role="dialog">
			<div class="modal-dialog modal-lg" role="document"><div class="modal-content">
				<div class="modal-header custom-box-header-modal"><h5 class="modal-title">Tally Purchase Order Export</h5><button type="button" class="close" data-dismiss="modal"><span>&times;</span></button></div>
				<div class="modal-body">
					<div id="tallyPoSummary" class="alert alert-info"></div>
					<div class="table-responsive" style="max-height: 420px; overflow-y: auto;"><table class="table table-bordered table-sm">
						<thead><tr><th>PO Number</th><th>Status</th><th>Details</th></tr></thead><tbody id="tallyPoResultRows"></tbody>
					</table></div>
				</div>
				<div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">Close</button></div>
			</div></div>
		</div>
    <script>
    function refreshPoAutoSyncStatus() {
        fetch('${pageContext.request.contextPath}/po_list/tally/sync-status', {credentials:'same-origin'})
        .then(function(response) { if (!response.ok) throw new Error('Status unavailable'); return response.json(); })
        .then(function(status) {
            var message = !status.enabled ? 'Automatic PO sync is disabled.' : status.running ? 'Automatic PO sync is running...' : 'Automatic PO sync checks every 5 minutes.';
            if (status.lastResult) message += ' Last run: ' + status.lastResult.imported + ' exported, ' + status.lastResult.skipped + ' unchanged/skipped, ' + status.lastResult.failed + ' need attention.';
            if (status.lastError) message += ' ' + status.lastError;
            document.getElementById('tallyPoAutoSyncStatus').textContent = message;
        }).catch(function() {});
    }
    refreshPoAutoSyncStatus();
    setInterval(refreshPoAutoSyncStatus, 15000);
    document.getElementById('poLostByDateForm').addEventListener('submit', function(event) {
        var button = event.submitter || document.activeElement;
        if (!button || (button.getAttribute('formaction') || '').indexOf('/tally/import') < 0) return;
        if (this.dataset.exportRunning === 'true') { event.preventDefault(); return; }
        this.dataset.exportRunning = 'true';
        button.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Sending and verifying POs...';
        document.getElementById('tallyPoConnectionStatus').textContent = 'Export in progress. Please wait for the verified result.';
        // Disable after the browser has captured the submitter URL and method.
        setTimeout(function() { button.disabled = true; }, 0);
    });
    </script>
	</body>

</html>
