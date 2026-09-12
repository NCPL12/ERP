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

<title>
    <tiles:insertAttribute name="title" />
</title>

<tiles:insertAttribute name="header-resources" />

<script src="<c:url value="/resources/js/reportDashboard.js"/>"></script>

<script type="text/javascript">
    var pageContext = '${pageContext.request.contextPath}';
</script>

<style>

    .hideTd {
        display: none !important;
    }

    /* SO Without Design table */
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

    /* Education Box */
    .education-box {
        cursor: pointer;
    }

    /* Education Modal */
    #educationModal .education-details h5 {
        margin-top: 15px;
    }

    #educationModal .education-details p {
        margin-bottom: 8px;
    }

</style>
```

</head>

<body class="hold-transition sidebar-mini">

<div class="wrapper">

```
<tiles:insertAttribute name="header" />

<tiles:insertAttribute name="sideMenu" />


<!-- Content Wrapper -->
<div class="content-wrapper">

    <!-- Main content -->
    <section class="content">

        <div class="container-fluid">

            <!-- Small boxes -->
            <div class="row mt-3">


                <!-- CLIENT SALES ORDER -->
                <security:authorize access="!hasAuthority('PURCHASE')">

                    <div class="col-lg-2 col-6">

                        <div class="small-box bg-info">

                            <div class="inner">

                                <h3 id="salesOrderCount">
                                    ${salesOrderCount}
                                </h3>

                                <p>Client Sales Order</p>

                            </div>

                            <div class="icon">
                                <i class="ion ion-stats-bars"></i>
                            </div>

                            <a href="#"
                               id="pendingSaleslink"
                               class="small-box-footer">

                                More info
                                <i class="fas fa-arrow-circle-right"></i>

                            </a>

                        </div>

                    </div>

                </security:authorize>


                <!-- VENDOR PURCHASE ORDER -->
                <div class="col-lg-2 col-6">

                    <div class="small-box bg-success">

                        <div class="inner">

                            <h3 id="purchaseOrderCount">
                                ${purchaseOrderCount}
                            </h3>

                            <p>Vendor Purchase Order</p>

                        </div>

                        <div class="icon">
                            <i class="ion ion-ios-cart-outline"></i>
                        </div>

                        <a href="#"
                           id="pendingPurchaselink"
                           class="small-box-footer">

                            More info
                            <i class="fas fa-arrow-circle-right"></i>

                        </a>

                    </div>

                </div>


                <security:authorize access="!hasAuthority('PURCHASE')">


                    <!-- TOTAL INVOICE -->
                    <div class="col-lg-2 col-6">

                        <div class="small-box bg-warning">

                            <div class="inner">

                                <h3 id="invoiceCount">
                                    ${invoiceCount}
                                </h3>

                                <p>Total Invoice</p>

                            </div>

                            <div class="icon">
                                <i class="ion ion-person-add"></i>
                            </div>

                            <a href="#"
                               id="invoicelink"
                               class="small-box-footer">

                                More info
                                <i class="fas fa-arrow-circle-right"></i>

                            </a>

                        </div>

                    </div>


                    <!-- PROJECTS -->
                    <div class="col-lg-2 col-6">

                        <div class="small-box bg-danger">

                            <div class="inner">

                                <h3 id="projectPreviewCount">
                                    ${projectPreviewCount}
                                </h3>

                                <p>Projects</p>

                            </div>

                            <div class="icon">
                                <i class="ion ion-pie-graph"></i>
                            </div>

                            <a href="#"
                               id="saleslink"
                               class="small-box-footer">

                                More info
                                <i class="fas fa-arrow-circle-right"></i>

                            </a>

                        </div>

                    </div>


                    <!-- TDS APPROVED -->
                    <div class="col-lg-2 col-6">

                        <div class="small-box bg-red">

                            <div class="inner">

                                <h3 id="tdsItemsCount">
                                    ${tdsItemsCount}
                                </h3>

                                <p>Tds Approved items</p>

                            </div>

                            <div class="icon">
                                <i class="ion ion-checkmark"></i>
                            </div>

                            <a href="#"
                               id="tdsLink"
                               class="small-box-footer">

                                More info
                                <i class="fas fa-arrow-circle-right"></i>

                            </a>

                        </div>

                    </div>


                    <!-- SO WITHOUT DESIGN -->
                    <div class="col-lg-2 col-6">

                        <div class="small-box bg-primary">

                            <div class="inner">

                                <h3 id="sowithoutDesignCount">
                                    ${sowithoutDesignCount}
                                </h3>

                                <p>SO Without Design</p>

                            </div>

                            <div class="icon">
                                <i class="ion ion-laptop"></i>
                            </div>

                            <a href="#"
                               id="sowithoutdesignlink"
                               class="small-box-footer">

                                More info
                                <i class="fas fa-arrow-circle-right"></i>

                            </a>

                        </div>

                    </div>


                </security:authorize>


                <!-- SO WITH DESIGN -->
                <div class="col-lg-2 col-6">

                    <div class="small-box bg-primary">

                        <div class="inner">

                            <h3 id="sowithDesignCount">
                                ${sowithDesignCount}
                            </h3>

                            <p>SO With Design</p>

                        </div>

                        <div class="icon">
                            <i class="ion ion-laptop"></i>
                        </div>

                        <a href="#"
                           id="sowithdesignlink"
                           class="small-box-footer">

                            More info
                            <i class="fas fa-arrow-circle-right"></i>

                        </a>

                    </div>

                </div>


                <!-- ================================= -->
                <!-- MY EDUCATION BOX -->
                <!-- ================================= -->

                <div class="col-lg-2 col-6">

                    <div class="small-box bg-secondary education-box">

                        <div class="inner">

                            <h3>Subha</h3>

                            <p>My Education</p>

                        </div>

                        <div class="icon">

                            <i class="fas fa-graduation-cap"></i>

                        </div>


                        <!-- IMPORTANT:
                             ID is educationLink.
                             Modal ID is educationModal.
                        -->

                        <a href="#"
                           id="educationLink"
                           class="small-box-footer"
                           data-toggle="modal"
                           data-target="#educationModal">

                            More info

                            <i class="fas fa-arrow-circle-right"></i>

                        </a>

                    </div>

                </div>

                <!-- MY EDUCATION BOX END -->


            </div>

            <!-- /.row -->


            <!-- MAIN TABLE ROW -->
            <div class="row">


                <!-- LEFT COLUMN -->
                <div class="col-lg-6">


                    <security:authorize access="!hasAuthority('PURCHASE')">


                        <!-- PENDING SALES ORDER -->
                        <div class="card">

                            <div class="card-header border-0">

                                <div class="d-flex justify-content-between">

                                    <h3 class="card-title">
                                        Pending Sales Order
                                    </h3>

                                </div>

                            </div>


                            <div class="card-body">

                                <table id="salesListWithStatusNotClosed"
                                       class="table table-bordered table-striped dataTable"
                                       style="width: 100%">

                                </table>

                            </div>

                        </div>


                        <!-- INVOICE LIST -->
                        <div class="card">

                            <div class="card-header border-0">

                                <h3 class="card-title">
                                    Invoice List (TBD)
                                </h3>

                            </div>

                            <div class="card-body">

                                <table id="invoiceTable"
                                       class="table table-bordered table-striped dataTable"
                                       style="width: 100%">

                                </table>

                            </div>

                        </div>


                        <!-- TDS APPROVED ITEMS -->
                        <div class="card">

                            <div class="card-header border-0">

                                <h3 class="card-title">
                                    Tds Approved Items
                                </h3>

                            </div>

                            <div class="card-body">

                                <table id="tdsApprovedItemsTable"
                                       class="table table-bordered table-striped dataTable"
                                       style="width: 100%">

                                </table>

                            </div>

                        </div>


                    </security:authorize>


                    <!-- SO WITH DESIGN AND PO NOT DONE -->
                    <div class="card">

                        <div class="card-header border-0">

                            <div class="d-flex justify-content-between">

                                <h3 class="card-title">
                                    SO With Design and PO Not Done
                                </h3>

                            </div>

                        </div>

                        <div class="card-body">

                            <table id="soWithDesignTable"
                                   class="table table-bordered table-striped dataTable"
                                   style="width: 100%">

                            </table>

                        </div>

                    </div>


                </div>


                <!-- RIGHT COLUMN -->
                <div class="col-lg-6">


                    <!-- PENDING PURCHASE -->
                    <div class="card">

                        <div class="card-header border-0">

                            <div class="d-flex justify-content-between">

                                <h3 class="card-title">
                                    Pending Purchase List
                                </h3>

                            </div>

                        </div>

                        <div class="card-body">

                            <table id="purchaseTable"
                                   class="table table-bordered table-striped dataTable"
                                   style="width: 100%">

                            </table>

                        </div>

                    </div>


                    <security:authorize access="!hasAuthority('PURCHASE')">


                        <!-- PROJECT PREVIEW -->
                        <div class="card">

                            <div class="card-header border-0">

                                <div class="d-flex justify-content-between">

                                    <h3 class="card-title">
                                        Project Preview
                                    </h3>

                                </div>

                            </div>

                            <div class="card-body">

                                <table id="salesListTable"
                                       class="table table-bordered table-striped dataTable"
                                       style="width: 100%">

                                </table>

                            </div>

                        </div>


                        <!-- SO WITHOUT DESIGN -->
                        <div class="card">

                            <div class="card-header border-0">

                                <h3 class="card-title">
                                    SO Without Design
                                </h3>

                            </div>

                            <div class="card-body">

                                <table id="salesItemsWithoutDesignTble"
                                       class="table table-bordered table-striped dataTable"
                                       style="width: 100%">

                                </table>

                            </div>

                        </div>


                    </security:authorize>


                </div>

            </div>

            <!-- /.row -->

        </div>

    </section>

</div>


<!-- Footer -->
<tiles:insertAttribute name="footer" />
```

</div>

<!-- ====================================================== -->

<!-- PENDING SALES MODAL -->

<!-- ====================================================== -->

<div class="modal fade"
     id="pendingSalesModal"
     tabindex="-1"
     role="dialog"
     aria-hidden="true">

```
<div class="modal-dialog modal-lg"
     style="margin-left: 33%; margin-top: 0%;">

    <div class="modal-content">

        <div class="modal-header custom-box-header-modal">

            <h6 class="modal-title" id="pendingSalesHeader">
                <b>Pending Sales Order</b>
            </h6>

            <button type="button"
                    class="close buttonDismiss"
                    data-dismiss="modal">

                &times;

            </button>

        </div>


        <div class="modal-body"
             style="overflow:scroll">

            <table id="pendingSalesTable"
                   class="pendingSalesTable table table-bordered table-striped"
                   style="width:100%">

                <thead>

                    <tr>

                        <th width="5%" rowspan="2" class="thStyle">
                            Sl.No
                        </th>

                        <th width="20%" rowspan="2" class="thStyle">
                            Description
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Model No
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            HSN
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            SAC
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Qty
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Unit
                        </th>

                        <th width="14%" colspan="2"
                            style="text-align:center;">
                            Price
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Amount
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design Qty
                        </th>

                    </tr>


                    <tr>

                        <th width="7%">
                            Supply
                        </th>

                        <th width="7%">
                            Service
                        </th>

                    </tr>

                </thead>

                <tbody id="pendingSalesTableBody">
                </tbody>

            </table>

        </div>


        <div class="modal-footer">

            <button class="btn btn-default btn-sm buttonDismiss"
                    data-dismiss="modal">

                Close

            </button>

        </div>

    </div>

</div>
```

</div>

<!-- ====================================================== -->

<!-- PROJECT MODAL -->

<!-- ====================================================== -->

<div class="modal fade"
     id="projectModal"
     tabindex="-1"
     role="dialog"
     aria-hidden="true">

```
<div class="modal-dialog modal-lg"
     style="margin-left:33%; margin-top:0%;">

    <div class="modal-content">

        <div class="modal-header custom-box-header-modal">

            <h6 class="modal-title" id="projectHeader">
                <b>Projects</b>
            </h6>

            <button type="button"
                    class="close buttonDismiss"
                    data-dismiss="modal">

                &times;

            </button>

        </div>


        <div class="modal-body"
             style="overflow:scroll">

            <table id="projectModalTable"
                   class="projectModalTable table table-bordered table-striped"
                   style="width:100%">

                <thead>

                    <tr>

                        <th width="5%" rowspan="2" class="thStyle">
                            Sl.No
                        </th>

                        <th width="20%" rowspan="2" class="thStyle">
                            Description
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Model No
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            HSN
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            SAC
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Qty
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Unit
                        </th>

                        <th width="14%" colspan="2">
                            Price
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Amount
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design Qty
                        </th>

                    </tr>

                    <tr>

                        <th width="7%">
                            Supply
                        </th>

                        <th width="7%">
                            Service
                        </th>

                    </tr>

                </thead>

                <tbody id="projectModalTableBody">
                </tbody>

            </table>

        </div>


        <div class="modal-footer">

            <button class="btn btn-default btn-sm buttonDismiss"
                    data-dismiss="modal">

                Close

            </button>

        </div>

    </div>

</div>
```

</div>

<!-- ====================================================== -->

<!-- SALES ITEM WITHOUT DESIGN MODAL -->

<!-- ====================================================== -->

<div class="modal fade"
     id="salesItemwithoutDesignModal"
     tabindex="-1"
     role="dialog"
     aria-hidden="true">

```
<div class="modal-dialog modal-lg"
     style="margin-left:33%; margin-top:0%;">

    <div class="modal-content">

        <div class="modal-header custom-box-header-modal">

            <h6 class="modal-title"
                id="salesItemwithoutDesignHeader">

                <b>Sales Items without Design</b>

            </h6>

            <button type="button"
                    class="close buttonDismiss"
                    data-dismiss="modal">

                &times;

            </button>

        </div>


        <div class="modal-body"
             style="overflow:scroll">

            <table id="salesItemWithoutDesignModalTable"
                   class="salesItemWithoutDesignModalTable table table-bordered table-striped"
                   style="width:100%">

                <thead>

                    <tr>

                        <th width="5%" rowspan="2" class="thStyle">
                            Sl.No
                        </th>

                        <th width="20%" rowspan="2" class="thStyle">
                            Description
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Model No
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            HSN
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            SAC
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Qty
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Unit
                        </th>

                        <th width="14%" colspan="2">
                            Price
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Amount
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design Qty
                        </th>

                    </tr>

                    <tr>

                        <th width="7%">
                            Supply
                        </th>

                        <th width="7%">
                            Service
                        </th>

                    </tr>

                </thead>

                <tbody id="salesItemWithoutDesignTableBody">
                </tbody>

            </table>

        </div>


        <div class="modal-footer">

            <button class="btn btn-default btn-sm buttonDismiss"
                    data-dismiss="modal">

                Close

            </button>

        </div>

    </div>

</div>
```

</div>

<!-- ====================================================== -->

<!-- TDS APPROVED MODAL -->

<!-- ====================================================== -->

<div class="modal fade"
     id="tdsApprovedModal"
     tabindex="-1"
     role="dialog"
     aria-hidden="true">

```
<div class="modal-dialog modal-lg"
     style="margin-left:33%; margin-top:0%;">

    <div class="modal-content">

        <div class="modal-header custom-box-header-modal">

            <h6 class="modal-title"
                id="tdsApprovedHeader">

                <b>TDS Approved</b>

            </h6>

            <button type="button"
                    class="close buttonDismiss"
                    data-dismiss="modal">

                &times;

            </button>

        </div>


        <div class="modal-body"
             style="overflow:scroll">

            <table id="tdsApprovedModalTable"
                   class="tdsApprovedModalTable table table-bordered table-striped"
                   style="width:100%">

                <thead>

                    <tr>

                        <th width="5%" rowspan="2" class="thStyle">
                            Sl.No
                        </th>

                        <th width="20%" rowspan="2" class="thStyle">
                            Description
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Model No
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            HSN
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            SAC
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Qty
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Unit
                        </th>

                        <th width="14%" colspan="2">
                            Price
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Amount
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design Qty
                        </th>

                    </tr>

                    <tr>

                        <th width="7%">
                            Supply
                        </th>

                        <th width="7%">
                            Service
                        </th>

                    </tr>

                </thead>

                <tbody id="tdsApprovedTableBody">
                </tbody>

            </table>

        </div>


        <div class="modal-footer">

            <button class="btn btn-default btn-sm buttonDismiss"
                    data-dismiss="modal">

                Close

            </button>

        </div>

    </div>

</div>
```

</div>

<!-- ====================================================== -->

<!-- PENDING PURCHASE MODAL -->

<!-- ====================================================== -->

<div class="modal fade"
     id="pendingPurchaseModal"
     tabindex="-1"
     role="dialog"
     aria-hidden="true">

```
<div class="modal-dialog modal-lg"
     style="margin-left:33%; margin-top:0%;">

    <div class="modal-content">

        <div class="modal-header custom-box-header-modal">

            <h6 class="modal-title"
                id="pendingPurchaseHeader">

                <b>Pending PO</b>

            </h6>

            <button type="button"
                    class="close buttonDismiss"
                    data-dismiss="modal">

                &times;

            </button>

        </div>


        <div class="modal-body"
             style="overflow:scroll">

            <table id="pendingPurchaseModalTable"
                   class="pendingPurchaseModalTable table table-bordered table-striped"
                   style="width:100%">

                <thead>

                    <tr>

                        <th width="5%" rowspan="2" class="thStyle">
                            Sl.No
                        </th>

                        <th width="25%" rowspan="2" class="thStyle">
                            PO desc.
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Model No.
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            HSN
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Qty
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Unit
                        </th>

                        <th width="12%" rowspan="2" class="thStyle">
                            Unit Price
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Amount
                        </th>

                    </tr>

                </thead>

                <tbody id="pendingPurchaseTableBody">
                </tbody>

            </table>

        </div>


        <div class="modal-footer">

            <button class="btn btn-default btn-sm buttonDismiss"
                    data-dismiss="modal">

                Close

            </button>

        </div>

    </div>

</div>
```

</div>

<!-- ====================================================== -->

<!-- SALES ITEM WITH DESIGN MODAL -->

<!-- ====================================================== -->

<div class="modal fade"
     id="salesItemwithDesignModal"
     tabindex="-1"
     role="dialog"
     aria-hidden="true">

```
<div class="modal-dialog modal-lg"
     style="margin-left:33%; margin-top:0%;">

    <div class="modal-content">

        <div class="modal-header custom-box-header-modal">

            <h6 class="modal-title"
                id="salesItemwithDesignHeader">

                <b>Sales Items with Design</b>

            </h6>

            <button type="button"
                    class="close buttonDismiss"
                    data-dismiss="modal">

                &times;

            </button>

        </div>


        <div class="modal-body"
             style="overflow:scroll">

            <table id="salesItemWithDesignModalTable"
                   class="salesItemWithDesignModalTable table table-bordered table-striped"
                   style="width:100%">

                <thead>

                    <tr>

                        <th width="5%" rowspan="2" class="thStyle">
                            Sl.No
                        </th>

                        <th width="20%" rowspan="2" class="thStyle">
                            Description
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Model No
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            HSN
                        </th>

                        <th width="8%" rowspan="2" class="thStyle">
                            SAC
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Qty
                        </th>

                        <th width="7%" rowspan="2" class="thStyle">
                            Unit
                        </th>

                        <th width="14%" colspan="2">
                            Price
                        </th>

                        <th width="10%" rowspan="2" class="thStyle">
                            Amount
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design
                        </th>

                        <th width="5%" rowspan="2" class="thStyle">
                            Design Qty
                        </th>

                    </tr>

                    <tr>

                        <th width="7%">
                            Supply
                        </th>

                        <th width="7%">
                            Service
                        </th>

                    </tr>

                </thead>

                <tbody id="salesItemWithDesignTableBody">
                </tbody>

            </table>

        </div>


        <div class="modal-footer">

            <button class="btn btn-default btn-sm buttonDismiss"
                    data-dismiss="modal">

                Close

            </button>

        </div>

    </div>

</div>
```

</div>

<!-- ====================================================== -->

<!-- MY EDUCATION MODAL -->

<!-- ====================================================== -->

<div class="modal fade"
     id="educationModal"
     tabindex="-1"
     role="dialog"
     aria-hidden="true">

```
<div class="modal-dialog modal-lg"
     style="margin-top:5%;">


    <div class="modal-content">


        <!-- Modal Header -->
        <div class="modal-header custom-box-header-modal">

            <h5 class="modal-title">

                <i class="fas fa-graduation-cap"></i>

                <b>My Education</b>

            </h5>


            <button type="button"
                    class="close"
                    data-dismiss="modal"
                    aria-label="Close">

                <span aria-hidden="true">
                    &times;
                </span>

            </button>

        </div>


        <!-- Modal Body -->
        <div class="modal-body">

            <div class="education-details">


                <!-- NAME -->
                <h4>

                    <i class="fas fa-user"></i>

                    <b>Subhashree Nayak</b>

                </h4>


                <hr>


                <!-- B.TECH -->
                <h5>

                    <i class="fas fa-graduation-cap"></i>

                    B.Tech  Computer Science Engineering

                </h5>


                <p>

                    <b>College:</b>

                    Konark Institute of Science and Technology (KIST)

                </p>


                <p>

                    <b>Duration:</b>

                    2021 to 2025

                </p>


                <hr>


                <!-- PGDCA -->
                <h5>

                    <i class="fas fa-laptop-code"></i>

                    PGDCA

                </h5>


                <p>

                    <b>Institute:</b>

                    Kensoft Institute

                </p>


                <hr>


                <!-- DATA SCIENCE -->
                <h5>

                    <i class="fas fa-database"></i>

                    Data Science

                </h5>


                <p>

                    <b>Institute:</b>

                    Naresh IT

                </p>


            </div>

        </div>


        <!-- Modal Footer -->
        <div class="modal-footer">

            <button type="button"
                    class="btn btn-default btn-sm"
                    data-dismiss="modal">

                Close

            </button>

        </div>


    </div>

</div>


</div>

</body>

</html>
