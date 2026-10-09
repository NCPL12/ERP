<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />
<script src="<c:url value="/resources/js/reportDashboard.js" />"></script>
<script type="text/javascript">
var pageContext = '${pageContext.request.contextPath}';
var pendingSalesListDashboard = ${pendingSalesListDashboard};
var pendingPurchaseListDashboard = ${pendingPurchaseListDashboard};
var invoiceListDashboard = ${invoiceListDashboard};
var allSalesListDashboard = ${allSalesListDashboard};
var tdsApprovedListDashboard = ${tdsApprovedListDashboard};
var salesItemsWithoutDesignListDashboard = ${salesItemsWithoutDesignListDashboard};
var salesOrderWithDesignListDashboard = ${salesOrderWithDesignListDashboard};
</script>
<style>
.hideTd{
 display:none !important;
}
/* SO Without Design table: padding inside borders, consistent vertical alignment */
#salesItemsWithoutDesignTble thead th,
#salesItemsWithoutDesignTble tbody td {
  padding: 0.7rem 0.95rem;
  vertical-align: middle;
  box-sizing: border-box;
}
#salesItemsWithoutDesignTble .so-without-design-col-text {
  text-align: left;
  word-wrap: break-word;
  word-break: break-word;
}
#salesItemsWithoutDesignTble .so-without-design-col-created {
  text-align: left;
  white-space: nowrap;
}
#salesItemsWithoutDesignTble thead .so-without-design-col-numeric,
#salesItemsWithoutDesignTble tbody .so-without-design-col-numeric {
  text-align: center;
}
#salesItemsWithoutDesignTble thead .so-without-design-col-view,
#salesItemsWithoutDesignTble tbody .so-without-design-col-view {
  text-align: center;
  width: 3.75rem;
  white-space: nowrap;
}
#salesItemsWithoutDesignTble tbody .so-without-design-col-view .btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* Stat tiles: fill available width, reflow cleanly whatever the role hides */
.dash-tiles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 10px;
  margin-top: 10px;
}
.dash-tiles-grid .small-box {
  margin-bottom: 0;
  height: 100%;
  cursor: pointer;
}

/* List cards: one balanced flow instead of two hand-split columns, so a
   role missing several cards from one side doesn't leave a tall empty gap */
.dash-lists-grid {
  columns: 420px 2;
  column-gap: 10px;
  margin-top: 10px;
}
.dash-lists-grid .card {
  break-inside: avoid;
  display: inline-block;
  width: 100%;
  margin-bottom: 10px;
}
.dash-lists-grid .card-body {
  overflow-x: auto;
}
.dash-lists-grid .card-body table.dataTable {
  width: 100% !important;
}
.dash-lists-grid .dataTables_wrapper {
  width: 100%;
}
.dash-lists-grid .dataTables_filter {
  float: none;
  text-align: right;
  margin-bottom: 0.6rem;
}
.dash-lists-grid .dataTables_filter input {
  margin-left: 0.4rem;
}
.dash-lists-grid .dataTables_length {
  margin-bottom: 0.6rem;
}
</style>
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />
  <!-- Content Wrapper. Contains page content -->
  <div class="content-wrapper">
    <!-- Main content -->
    <section class="content">
      <div class="container-fluid">
        <!-- Small boxes (Stat box) -->
<security:authorize access="hasAnyAuthority('ADMIN','SUPER ADMIN')">
          <div class="col-lg-2 col-6"><div class="small-box bg-teal">
            <div class="inner"><h3 aria-hidden="true">&nbsp;</h3><p>Cashflow Analyzer</p></div>
            <div class="icon"><i class="fas fa-chart-line"></i></div>
            <a href="${pageContext.request.contextPath}/cashflow-analyzer/overview" class="small-box-footer">Open analysis <i class="fas fa-arrow-circle-right"></i></a>
          </div></div>
          </security:authorize>
        <div class="dash-tiles-grid">
          <c:if test="${tileVisible.contains('SALES_ORDER')}">
          <div>
            <!-- small box -->
            <div class="small-box bg-info" id="pendingSaleslink">
              <div class="inner">
                <h3 id="salesOrderCount">${salesOrderCount}</h3>

                <p>Client Sales Order</p>
              </div>
              <div class="icon">
              <i class="ion ion-stats-bars"></i>

              </div>
            </div>
          </div>
          </c:if>
          <!-- ./col -->
          <c:if test="${tileVisible.contains('PURCHASE_ORDER')}">
          <div>
            <!-- small box -->
            <div class="small-box bg-success" id="pendingPurchaselink">
              <div class="inner">
                <h3 id="purchaseOrderCount">${purchaseOrderCount}</h3>

                <p>Vendor Purchase Order</p>
              </div>
              <div class="icon">
               <i class="ion ion-ios-cart-outline"></i>
              </div>
            </div>
          </div>
          </c:if>
          <!-- ./col -->
          <c:if test="${tileVisible.contains('INVOICE')}">
          <div>
            <!-- small box -->
            <div class="small-box bg-warning" id="invoicelink">
              <div class="inner">
                <h3 id="invoiceCount">${invoiceCount}</h3>

                <p>Total Invoice</p>
              </div>
              <div class="icon">
                <i class="ion ion-person-add"></i>
              </div>
            </div>
          </div>
          </c:if>

          <!-- ./col -->
          <c:if test="${tileVisible.contains('PROJECTS')}">
          <div>
            <!-- small box -->
            <div class="small-box bg-danger" id="saleslink">
              <div class="inner">
                <h3 id="projectPreviewCount">${projectPreviewCount}</h3>

                <p>Projects</p>
              </div>
              <div class="icon">
                <i class="ion ion-pie-graph"></i>
              </div>
            </div>
          </div>
          </c:if>
          <c:if test="${tileVisible.contains('TDS_APPROVED')}">
            <div>
            <!-- small box -->
            <div class="small-box bg-red" id="tdsLink">
              <div class="inner">
                <h3 id="tdsItemsCount">${tdsItemsCount}</h3>

                <p>Tds Approved items</p>
              </div>
              <div class="icon">
                <i class="ion ion-checkmark"></i>
              </div>
            </div>
          </div>
          </c:if>
          <c:if test="${tileVisible.contains('SO_WITHOUT_DESIGN')}">
          <div>
            <!-- small box -->
            <div class="small-box bg-teal" id="sowithoutdesignlink">
              <div class="inner">
                <h3 id="sowithoutDesignCount">${sowithoutDesignCount}</h3>

                <p>SO Without Design</p>
              </div>
               <div class="icon">
               <i class="ion ion-laptop"></i>
              </div>
            </div>
          </div>
          </c:if>
          <c:if test="${tileVisible.contains('SO_WITH_DESIGN')}">
          <div>
            <!-- small box -->
            <div class="small-box bg-primary" id="sowithdesignlink">
              <div class="inner">
                <h3 id="sowithDesignCount">${sowithDesignCount}</h3>

                <p>SO With Design</p>
              </div>
               <div class="icon">
               <i class="ion ion-laptop"></i>
              </div>
            </div>
          </div>
          </c:if>

          <!-- ./col -->
        </div>
        <!-- /.row -->
        <div class="dash-lists-grid">
            <c:if test="${tileVisible.contains('SALES_ORDER')}">
            <div class="card">
              <div class="card-header border-0">
                <div class="d-flex justify-content-between">
                  <h3 class="card-title">Pending Sales Order</h3>
                </div>
              </div>
              <div class="card-body">
               <table id="salesListWithStatusNotClosed"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            </c:if>
            <!-- /.card -->

            <c:if test="${tileVisible.contains('INVOICE')}">
            <div class="card">
              <div class="card-header border-0">
                <h3 class="card-title">Invoice List (TBD)</h3>
              </div>
              <div class="card-body">
                  <table id="invoiceTable"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            </c:if>
            <c:if test="${tileVisible.contains('TDS_APPROVED')}">
            <div class="card">
              <div class="card-header border-0">
                <h3 class="card-title">Tds Approved Items</h3>
              </div>
              <div class="card-body">
                  <table id="tdsApprovedItemsTable"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            </c:if>
            <c:if test="${tileVisible.contains('SO_WITH_DESIGN')}">
            <div class="card">
              <div class="card-header border-0">
                <div class="d-flex justify-content-between">
                  <h3 class="card-title">SO With Design and PO Not Done</h3>
                </div>
              </div>
              <div class="card-body">
              <table id="soWithDesignTable"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            </c:if>
            <c:if test="${tileVisible.contains('PURCHASE_ORDER')}">
            <div class="card">
              <div class="card-header border-0">
                <div class="d-flex justify-content-between">
                  <h3 class="card-title">Pending Purchase List</h3>
                </div>
              </div>
              <div class="card-body">
              <table id="purchaseTable"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            </c:if>
            <!-- /.card -->
            <c:if test="${tileVisible.contains('PROJECTS')}">
            <div class="card">
              <div class="card-header border-0">
              <div class="d-flex justify-content-between">
                <h3 class="card-title">Project Preview</h3>
                </div>
              </div>
              <div class="card-body">
                <table id="salesListTable"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            </c:if>
            <c:if test="${tileVisible.contains('SO_WITHOUT_DESIGN')}">
            <div class="card">
              <div class="card-header border-0">
                <h3 class="card-title">SO Without Design</h3>
              </div>
              <div class="card-body">
                  <table id="salesItemsWithoutDesignTble"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            </c:if>

        </div>
        <!-- /.row (main row) -->
      </div><!-- /.container-fluid -->
    </section>
    <!-- /.content -->
  </div>
  <!-- /.content-wrapper -->
 <tiles:insertAttribute name="footer" />

</div>
<!-- ./wrapper -->
<!-- Pending sales view starts -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="pendingSalesModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%;">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="pendingSalesHeader">
						<b>Pending Sales Order</b>
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="pendingSalesTable" class="pendingSalesTable table table-bordered table-striped" style="width:100%">
					<thead id="table-header font">
								<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<!-- 	<th width="10%">Items</th> -->
										<th width="20%" rowspan="2" class="thStyle" >Description</th>
										<th width="10%" rowspan="2" class="thStyle">Model No</th>
										<th width="8%" rowspan="2" class="thStyle">HSN</th>
										<th width="8%" rowspan="2" class="thStyle">SAC</th>
										<th width="7%" rowspan="2" class="thStyle">Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Unit</th>
										<th width="14%" colspan="2" style="text-align: center;">Price</th>
										<th width="10%" rowspan="2" class="thStyle">Amount</th>
										<th width="5%" rowspan="2" class="thStyle">Design</th>
										<th width="5%" rowspan="2" class="thStyle">Design Qty</th>

									</tr>

									<!--dividing a cloumn into two rows-->
									<tr>
										<th width="7%">Supply </th>
										<th width="7%">Service</th>
									</tr>
							</thead>
							<tbody id="table-body">
							</tbody>
					</table>
				</div>
				 <div class="modal-footer">
					<div class="button-div-style" align="center">
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
				</div>

			</div>
		</div>
	</div>
<!-- Pending sales view ends -->
<!-- project view modal starts -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="projectModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="projectHeader">
						<b>Projects</b>
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="projectModalTable" class="projectModalTable table table-bordered table-striped" style="width:100%">
					<thead id="table-header font">
								<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<!-- 	<th width="10%">Items</th> -->
										<th width="20%" rowspan="2" class="thStyle" >Description</th>
										<th width="10%" rowspan="2" class="thStyle">Model No</th>
										<th width="8%" rowspan="2" class="thStyle">HSN</th>
										<th width="8%" rowspan="2" class="thStyle">SAC</th>
										<th width="7%" rowspan="2" class="thStyle">Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Unit</th>
										<th width="14%" colspan="2" style="text-align: center;">Price</th>
										<th width="10%" rowspan="2" class="thStyle">Amount</th>
										<th width="5%" rowspan="2" class="thStyle">Design</th>
										<th width="5%" rowspan="2" class="thStyle">Design Qty</th>

									</tr>

									<!--dividing a cloumn into two rows-->
									<tr>
										<th width="7%">Supply </th>
										<th width="7%">Service</th>
									</tr>
							</thead>
							<tbody id="table-body">
							</tbody>
					</table>
				</div>
				 <div class="modal-footer">
					<div class="button-div-style" align="center">
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
				</div>

			</div>
		</div>
	</div>
<!-- project view modal ends -->
<!-- sales item without design modal starts -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="salesItemwithoutDesignModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="salesItemwithoutDesignHeader">
						<b>Sales Items without Design</b>
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="salesItemWithoutDesignModalTable" class="salesItemWithoutDesignModalTable table table-bordered table-striped" style="width:100%">
					<thead id="table-header font">
								<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<!-- 	<th width="10%">Items</th> -->
										<th width="20%" rowspan="2" class="thStyle" >Description</th>
										<th width="10%" rowspan="2" class="thStyle">Model No</th>
										<th width="8%" rowspan="2" class="thStyle">HSN</th>
										<th width="8%" rowspan="2" class="thStyle">SAC</th>
										<th width="7%" rowspan="2" class="thStyle">Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Unit</th>
										<th width="14%" colspan="2" style="text-align: center;">Price</th>
										<th width="10%" rowspan="2" class="thStyle">Amount</th>
										<th width="5%" rowspan="2" class="thStyle">Design</th>
										<th width="5%" rowspan="2" class="thStyle">Design Qty</th>

									</tr>

									<!--dividing a cloumn into two rows-->
									<tr>
										<th width="7%">Supply </th>
										<th width="7%">Service</th>
									</tr>
							</thead>
							<tbody id="table-body">
							</tbody>
					</table>
				</div>
				 <div class="modal-footer">
					<div class="button-div-style" align="center">
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
				</div>

			</div>
		</div>
	</div>
<!-- sales item without design modal ends -->
<!-- purchase modal starts -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="pendingPurchaseModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%;">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="pendingPurchaseHeader">
						<b>Pending PO</b>
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="pendingPurchaseModalTable" class="pendingPurchaseModalTable table table-bordered table-striped" style="width:100%">
					 <thead id="table-header font">
								<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<th width="25%" rowspan="2" class="thStyle">PO desc.</th>
										<th width="10%" rowspan="2" class="thStyle">Model No.</th>
										<th width="8%" rowspan="2" class="thStyle">HSN</th>
										<th width="7%" rowspan="2" class="thStyle">Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Unit</th>
										<th width="12%" rowspan="2" class="thStyle">Unit Price</th>
										<th width="10%" rowspan="2" class="thStyle">Amount</th>

									</tr>

							</thead>
							<tbody id="table-body">
							</tbody>
					</table>
				</div>
				 <div class="modal-footer">
					<div class="button-div-style" align="center">
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
				</div>

			</div>
		</div>
	</div>
<!-- purchase modal ends -->
<!-- sales item with design modal starts -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="salesItemwithDesignModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="salesItemwithDesignHeader">
						<b>Sales Items with Design</b>
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="salesItemWithDesignModalTable" class="salesItemWithoutDesignModalTable table table-bordered table-striped" style="width:100%">
					<thead id="table-header font">
								<tr>
										<th width="5%" rowspan="2" class="thStyle">Sl.No</th>
										<!-- 	<th width="10%">Items</th> -->
										<th width="20%" rowspan="2" class="thStyle" >Description</th>
										<th width="10%" rowspan="2" class="thStyle">Model No</th>
										<th width="8%" rowspan="2" class="thStyle">HSN</th>
										<th width="8%" rowspan="2" class="thStyle">SAC</th>
										<th width="7%" rowspan="2" class="thStyle">Qty</th>
										<th width="7%" rowspan="2" class="thStyle">Unit</th>
										<th width="14%" colspan="2" style="text-align: center;">Price</th>
										<th width="10%" rowspan="2" class="thStyle">Amount</th>
										<th width="5%" rowspan="2" class="thStyle">Design</th>
										<th width="5%" rowspan="2" class="thStyle">Design Qty</th>

									</tr>

									<!--dividing a cloumn into two rows-->
									<tr>
										<th width="7%">Supply </th>
										<th width="7%">Service</th>
									</tr>
							</thead>
							<tbody id="table-body">
							</tbody>
					</table>
				</div>
				 <div class="modal-footer">
					<div class="button-div-style" align="center">
						<button class="btn btn-default btn-sm buttonDismiss" data-dismiss="modal">Close</button>
					</div>
				</div>

			</div>
		</div>
	</div>
<!-- sales item with design modal ends -->

</body>
</body>
</html>
