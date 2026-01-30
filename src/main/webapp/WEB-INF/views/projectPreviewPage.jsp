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
<script src="<c:url value="/resources/js/projectPreview.js" />"></script>
<script type="text/javascript">
var dcobj = '${dcList}';
dcobj = dcobj.replace(/\&/g, "\'");
dcobj = dcobj.replace(/\&/g, "\"");
var invoiceobj = '${invoiceList}';
invoiceobj = invoiceobj.replace(/\&/g, "\'");
invoiceobj = invoiceobj.replace(/\&/g, "\"");
var purchaseObj = '${purchaseList}';
purchaseObj = purchaseObj.replace(/\&/g, "\'");
purchaseObj = purchaseObj.replace(/\&/g, "\"");
var dcList = "";
if ('${dcList}' != null && '${dcList}' != "") {
dcList= $.parseJSON(dcobj);
}
var invoiceList = "";
if ('${invoiceList}' != null && '${invoiceList}' != "") {
	invoiceList= $.parseJSON(invoiceobj);
}
var purchaseList = "";
if ('${purchaseList}' != null && '${purchaseList}' != "") {
	purchaseList= $.parseJSON(purchaseObj);
}
var grnList = "";
if ('${grnList}' != null && '${grnList}' != "") {
	grnList= $.parseJSON('${grnList}');
}

</script>
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
          <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-info">
              <div class="inner">
                <h3 id="purchaseCount">0</h3>

                <p>Purchase Order</p>
              </div>
              <div class="icon">
                <i class="ion ion-bag"></i>
              </div>
              <!-- <a href="#" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> -->
            </div>
          </div>
          <!-- ./col -->
          <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-success">
              <div class="inner">
                <h3 id="grnCount">0</h3>

                <p>GRN</p>
              </div>
              <div class="icon">
                <i class="ion ion-stats-bars"></i>
              </div>
              <!-- <a href="#" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> -->
            </div>
          </div>
          <!-- ./col -->
          <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-warning">
              <div class="inner">
                <h3 id="dcCount"></h3>

                <p>DC</p>
              </div>
              <div class="icon">
                <i class="ion ion-person-add"></i>
              </div>
             <!--  <a href="#" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> -->
            </div>
          </div>
          <!-- ./col -->
          <div class="col-lg-3 col-6">
            <!-- small box -->
            <div class="small-box bg-danger">
              <div class="inner">
                <h3 id="invoiceCount"></h3>

                <p>Invoice</p>
              </div>
              <div class="icon">
                <i class="ion ion-pie-graph"></i>
              </div>
             <!--  <a href="#" class="small-box-footer">More info <i class="fas fa-arrow-circle-right"></i></a> -->
            </div>
          </div>
          <!-- ./col -->
        </div>
        <!-- /.row -->
        <div class="row">
          <div class="col-lg-6">
            <div class="card">
              <div class="card-header border-0">
                <div class="d-flex justify-content-between">
                  <h3 class="card-title">Purchase Order</h3>
                </div>
              </div>
              <div class="card-body">
               <table id="purchaseList"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            <!-- /.card -->

            <div class="card">
              <div class="card-header border-0">
                <h3 class="card-title">DC</h3>
              </div>
              <div class="card-body">
                  <table id="dcList"
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
                  <h3 class="card-title">GRN</h3>
                </div>
              </div>
              <div class="card-body">
              <table id="grnList"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
            <!-- /.card -->

            <div class="card">
              <div class="card-header border-0">
                <h3 class="card-title">Invoice</h3>
              </div>
              <div class="card-body">
                <table id="invoiceList"
					class='table table-bordered table-striped dataTable' style="width: 100%">
				</table>
              </div>
            </div>
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
</body>

</body>
</html>