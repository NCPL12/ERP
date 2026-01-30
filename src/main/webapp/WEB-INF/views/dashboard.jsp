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
</script>
<style>
.hideTd{
 display:none !important;
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
        <div class="row">
          <security:authorize access="!hasAuthority('PURCHASE')">
          <div class="col-lg-2 col-6">
            <!-- small box -->
            <div class="small-box bg-info">
              <div class="inner">
                <h3 id="salesOrderCount"></h3>

                <p>Client Sales Order</p>
              </div>
              <div class="icon">
              <i class="ion ion-stats-bars"></i>
               
              </div>
            <a href="#" id="pendingSaleslink" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a>
            </div>
          </div>
          </security:authorize>
          <!-- ./col -->
          <div class="col-lg-2 col-6">
            <!-- small box -->
            <div class="small-box bg-success">
              <div class="inner">
                <h3 id="purchaseOrderCount"></h3>

                <p>Vendor Purchase Order</p>
              </div>
              <div class="icon">
               <i class="ion ion-ios-cart-outline"></i>
              </div>
           <a href="#" id="pendingPurchaselink" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> 
            </div>
          </div>
          <!-- ./col -->
          <security:authorize access="!hasAuthority('PURCHASE')">
          <div class="col-lg-2 col-6">
            <!-- small box -->
            <div class="small-box bg-warning">
              <div class="inner">
                <h3 id="invoiceCount"></h3>

                <p>Total Invoice</p>
              </div>
              <div class="icon">
                <i class="ion ion-person-add"></i>
              </div>
               <a href="#" id="invoicelink" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> 
            </div>
          </div>
         
          <!-- ./col -->
          <div class="col-lg-2 col-6">
            <!-- small box -->
            <div class="small-box bg-danger">
              <div class="inner">
                <h3 id="projectPreviewCount"></h3>

                <p>Projects</p>
              </div>
              <div class="icon">
                <i class="ion ion-pie-graph"></i>
              </div>
        <a href="#" id="saleslink" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> 
            </div>
          </div>
           <div class="col-lg-2 col-6">
            <!-- small box -->
            <div class="small-box bg-red">
              <div class="inner">
                <h3 id="tdsItemsCount"></h3>

                <p>Tds Approved items</p>
              </div>
              <div class="icon">
                <i class="ion ion-checkmark"></i>
              </div>
            <a href="#" id="tdsLink" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> 
            </div>
          </div>
          <div class="col-lg-2 col-6">
            <!-- small box -->
            <div class="small-box bg-primary">
              <div class="inner">
                <h3 id="sowithoutDesignCount"></h3>

                <p>SO Without Design</p>
              </div>
               <div class="icon">
               <i class="ion ion-laptop"></i>
              </div>
              <a href="#" id="sowithoutdesignlink" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> 
            </div>
          </div>
          </security:authorize>
          <div class="col-lg-2 col-6">
            <!-- small box -->
            <div class="small-box bg-primary">
              <div class="inner">
                <h3 id="sowithDesignCount"></h3>

                <p>SO With Design</p>
              </div>
               <div class="icon">
               <i class="ion ion-laptop"></i>
              </div>
              <a href="#" id="sowithdesignlink" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> 
            </div>
          </div>
          
          <!-- ./col -->
        </div>
        <!-- /.row -->
        <div class="row">
        <div class="col-lg-6">
        <security:authorize access="!hasAuthority('PURCHASE')">
          
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
            <!-- /.card -->

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
             </security:authorize>
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
            <!-- /.card -->
          </div>
         
          <!-- /.col-md-6 -->
          <div class="col-lg-6">
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
            <!-- /.card -->
		<security:authorize access="!hasAuthority('PURCHASE')">
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
            </security:authorize>
            
          
          </div>
          <!-- /.col-md-6 -->
        </div>
        
        <!-- /.row -->
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
<!-- Tds modal starts -->
<div class="modal show" tabindex="-1" role="dialog" aria-hidden="true" id="tdsApprovedModal">
		<div class="modal-dialog modal-lg"
			style="margin-left: 33%; margin-top: 0%;">
			<div class="modal-content" style="width: 100%">
				<div class="modal-header custom-box-header-modal">
					<h6 class="modal-title" id="tdsApprovedHeader">
						<b>TDS Approved</b>
					</h6>
					<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
				</div>
				<div class="modal-body" style="overflow:scroll">

					<table id="tdsApprovedModalTable" class="tdsApprovedModalTable table table-bordered table-striped" style="width:100%">
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
<!-- tds modal ends -->
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