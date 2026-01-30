<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
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
<script src="<c:url value="/resources/js/salesReport.js" />"></script>
<script type="text/javascript">
var clientList=${clientList};
var vendorList=${vendorList};
</script>
</head>
<body>
<body class="hold-transition sidebar-mini">
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
						  	<form action="${pageContext.request.contextPath}/stock/summary_details/" id="stockSummaryForm" method="get">
							  <div class="card-body cardHeight">
								   <div class="form-group row">
									    <label for="staticEmail" class="col-sm-2 ">From</label>
									    <div class="col-sm-10">
									     <input type="text" autocomplete="off" name="reportFromDate" id="reportFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									     <label for="staticEmail" class="col-sm-2 ">To</label>
									    <div class="col-sm-10">
									     <input type="text" autocomplete="off" name="reportToDate" id="reportToDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									  </div>
							 </div>
							<div  class="card-footer">
								    <button type=submit id="downLoadStockSummaryReportBtn"
														class="btn btn-primary btn-sm btn-inline pull-right"><i class='fa fa-fw fa-download'></i> Download</button>
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
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   		<label for="staticEmail" class="col-sm-2 ">Region</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="region" id="region" style="padding: 0;">
									     <option value="" selected>Select Region</option>
									     <option value="Bangalore">Bangalore</option>
									     <option value="Mangalore">Mangalore</option>
									     
									     </select>
									    </div>
									    <label for="staticEmail" class="col-sm-2 ">From</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionFromDate" id="reportByRegionFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									    <label for="staticEmail" class="col-sm-2 ">To</label>
									    <div class="col-sm-10">
									     <input type="text" name="reportByRegionToDate" id="reportByRegionToDate" class="form-control PositionofTextbox" autocomplete="off">
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
						  	<form  id="poLostByDateForm" action="${pageContext.request.contextPath}/po_list/by_date" method="get">
								 <div class="card-body cardHeight">
									   <div class="form-group row">
										    <label for="staticEmail" class="col-sm-2 ">From</label>
										    <div class="col-sm-10">
										     <input type="text" autocomplete="off" name="poListByFromDate" id="poListByFromDate" class="form-control PositionofTextbox" autocomplete="off">
										    </div>
										     <label for="staticEmail" class="col-sm-2 ">To</label>
										    <div class="col-sm-10">
										     <input type="text" autocomplete="off" name="poListByToDate" id="poListByToDate" class="form-control PositionofTextbox" autocomplete="off">
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
							  <div class="card-body cardHeight">
								   <div class="form-group row">
								   			<!-- <label for="staticEmail" class="col-sm-2 ">Region</label>
									    <div class="col-sm-10">
									     <select class="form-control select2 PositionofTextbox" name="grnregion" id="grnregion" style="padding: 0;">
									     <option value="" selected>Select Region</option>
									     <option value="Bangalore">Bangalore</option>
									     <option value="Mangalore">Mangalore</option>
									     
									     </select>
									    </div> -->
									    <label for="staticEmail" class="col-sm-2 ">From</label>
									    <div class="col-sm-10">
									     <input type="text" name="grnreportByRegionFromDate" id="grnreportByRegionFromDate" class="form-control PositionofTextbox" autocomplete="off">
									    </div>
									    <label for="staticEmail" class="col-sm-2 ">To</label>
									    <div class="col-sm-10">
									     <input type="text" name="grnreportByRegionToDate" id="grnreportByRegionToDate" class="form-control PositionofTextbox" autocomplete="off">
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
			
		<!-- Dc List By Item Report ends -->
			
			
		</section>
		</div>
		</div>
		<tiles:insertAttribute name="footer" />
		</div>
</body>

</html>