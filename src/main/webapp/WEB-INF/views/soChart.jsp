<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>

<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />

<!-- CSS Files -->
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/purchaseOrder.css" />">

<!-- amCharts 4 - Load FIRST -->
<script src="https://cdn.amcharts.com/lib/4/core.js"></script>
<script src="https://cdn.amcharts.com/lib/4/charts.js"></script>
<script src="https://cdn.amcharts.com/lib/4/themes/animated.js"></script>

<!-- Define API endpoints BEFORE loading soChart.js -->
<script type="text/javascript">
var contextPath = "${pageContext.request.contextPath}";
var api = {
    SO_MOTNHLY_REPORT: contextPath + "/api/so_monthly_chart"
};
</script>

<!-- Custom Scripts -->
<script src="${RESOURCES}js/common.js"></script>
<script src="<c:url value="/resources/js/pageHeader.js" />"></script>
<script src="<c:url value="/resources/js/soChart.js" />"></script>

<style>
/* AGGRESSIVE FIX for sidebar collapse */
.content-wrapper {
    margin-left: 250px !important;
    transition: margin-left 0.3s ease-in-out !important;
    width: auto !important;
    padding: 20px;
}

.main-footer {
    margin-left: 250px !important;
    transition: margin-left 0.3s ease-in-out !important;
}

.sidebar-collapse .content-wrapper,
.sidebar-mini.sidebar-collapse .content-wrapper,
body.sidebar-collapse .content-wrapper,
body.sidebar-mini.sidebar-collapse .content-wrapper {
    margin-left: 4.6rem !important;
}

.sidebar-collapse .main-footer,
.sidebar-mini.sidebar-collapse .main-footer,
body.sidebar-collapse .main-footer,
body.sidebar-mini.sidebar-collapse .main-footer {
    margin-left: 4.6rem !important;
}

/* Chart Container */
#chartdiv {
    width: 100%;
    height: 100%;
}

/* Pie Chart Container */
#pieChartdiv {
    width: 100%;
    height: 100%;
}

/* Donut Chart Container */
#donutChartdiv {
    width: 100%;
    height: 100%;
}

/* Purchase Div */
#purchaseDiv {
    height: 400px;
}

/* Table Header Styling */
#soMonthlyTotalTable thead th {
    background: #17a2b8;
    color: #fff;
    padding: 15px;
    font-weight: 600;
    text-align: center;
    font-size: 18px;
}

/* Table Body Styling */
#soMonthlyTotalTable tbody tr:hover {
    background-color: #f8f9fa;
    transition: background-color 0.3s ease;
}

#soMonthlyTotalTable tbody td {
    padding: 10px;
    font-weight: 500;
    border: 1px solid #ddd;
}

/* Total Table Styling in TFOOT */
#soMonthlyTotalTable tfoot tr {
    background: #f8f9fa;
    font-weight: 700;
    font-size: 16px;
}

#soMonthlyTotalTable tfoot td {
    padding: 12px;
    border: 1px solid #ddd;
    border-top: 2px solid #17a2b8 !important;
}

/* Non-billable Table */
#nonBillableTable>tbody>tr>td,  
#nonBillableTable>thead>tr>th {
    padding: 3px 2px !important;
    vertical-align: middle;
}

/* Card Style for Chart and Table */
.chart-card, .table-card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    padding: 20px;
    margin-bottom: 20px;
}

/* Hide amCharts logo/watermark */
a[href*="amcharts.com"] {
    display: none !important;
    visibility: hidden !important;
    opacity: 0 !important;
}

div[aria-label*="Chart created using amCharts"] {
    display: none !important;
}

.amcharts-amexport-menu,
.amcharts-chart-div a[title*="JavaScript charts"] {
    display: none !important;
}

/* Responsive adjustments */
@media (max-width: 768px) {
    .chart-card, .table-card {
        height: auto !important;
    }
    
    #chartdiv, #pieChartdiv, #donutChartdiv {
        height: 350px !important;
    }
    
    .col-sm-6 {
        padding-top: 20px !important;
    }
    
    .content-wrapper,
    .sidebar-collapse .content-wrapper {
        margin-left: 0 !important;
    }
}
</style>
</head>
<body class="hold-transition sidebar-mini">
<div class="wrapper">

 <tiles:insertAttribute name="header" />
 <tiles:insertAttribute name="sideMenu" />

<div class="content-wrapper">
    <!-- Page Header -->
    <div class="content-header">
        <div class="container-fluid">
            <div class="row mb-2">
                <div class="col-sm-12">
                    <h1 class="m-0" style="color: #17a2b8; font-weight: 600;">
                        <i class="fas fa-chart-bar"></i> Sales Order Monthly Chart
                    </h1>
                </div>
            </div>
        </div>
    </div>

    <!-- Main Content -->
    <section class="content">
        <div class="container-fluid">
            
            <!-- First Row: Table (with Total) and Bar Chart -->
            <div class="row">
                <!-- Monthly Data Table WITH Total -->
                <div class="col-md-4 col-sm-12">
                    <div class="table-card" style="height: 580px; overflow-y: auto;">
                        <table id="soMonthlyTotalTable" class="table table-bordered table-hover" style="width: 100%;">
                            <thead>
                                <tr>
                                    <th colspan="2" style="text-align:center;">SALES ORDER MONTHLY CHART</th>
                                </tr>
                            </thead>
                            <tbody></tbody>
                            <!-- TOTAL ROW IN TFOOT -->
                            <tfoot id="total">
                            </tfoot>
                        </table>
                    </div>
                </div>
                
                <!-- Bar Chart -->
                <div class="col-md-8 col-sm-12">
                    <div class="chart-card" style="height: 580px;">
                        <div id="chartdiv"></div>
                    </div>
                </div>
            </div>
            
            <!-- Second Row: Pie Chart and Donut Chart SIDE BY SIDE -->
            <div class="row">
                <!-- Pie Chart -->
                <div class="col-md-6 col-sm-12">
                    <div class="chart-card" style="height: 550px;">
                        <div id="pieChartdiv"></div>
                    </div>
                </div>
                
                <!-- Donut Chart -->
                <div class="col-md-6 col-sm-12">
                    <div class="chart-card" style="height: 550px;">
                        <div id="donutChartdiv"></div>
                    </div>
                </div>
            </div>
            
        </div>
    </section>
</div>

<tiles:insertAttribute name="footer" />
</div>
<!-- ./wrapper -->

<!-- JavaScript Fix for Sidebar Toggle -->
<script type="text/javascript">
$(document).ready(function() {
    // Force sidebar toggle fix
    $('[data-widget="pushmenu"]').on('click', function() {
        setTimeout(function() {
            if ($('body').hasClass('sidebar-collapse')) {
                $('.content-wrapper').css('margin-left', '4.6rem');
                $('.main-footer').css('margin-left', '4.6rem');
            } else {
                $('.content-wrapper').css('margin-left', '250px');
                $('.main-footer').css('margin-left', '250px');
            }
        }, 350);
    });
});
</script>

</body>
</html>
