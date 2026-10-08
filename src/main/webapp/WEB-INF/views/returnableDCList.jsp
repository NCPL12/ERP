<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" isELIgnored="false"%>
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
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<link rel="stylesheet" href="<c:url value="/resources/css/purchaseOrder.css" />">
<script type="text/javascript">
var pageContext = '${pageContext.request.contextPath}';

$(document).ready(function () {
	var tableId = '#returnableDcList';

	// second header row holding one search box per column (same look as DC dashboard)
	$(tableId + ' thead tr').clone(true).addClass('filters').appendTo(tableId + ' thead');

	var table = $(tableId).DataTable({
		orderCellsTop : true,
		pageLength : 100,
		order : [ [ 0, 'desc' ] ]
	});

	$(tableId + ' thead tr.filters th').each(function (i) {
		var title = $.trim($(this).text());
		if (title === 'View') {
			$(this).html('');
			return;
		}
		$(this).html('<input type="text" class="form-control form-control-sm" style="width:100%" placeholder="Search ' + title + '" />');
		$('input', this).on('keyup change', function () {
			if (table.column(i).search() !== this.value) {
				table.column(i).search(this.value).draw();
			}
		});
	});
});
</script>
</head>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
	<tiles:insertAttribute name="header" />
	<tiles:insertAttribute name="sideMenu" />
	<div class="content-wrapper">

		<div id="salesDiv" class="card">
			<div class="card-body" style="padding-top: 10px;">
				<table id="returnableDcList" class="table table-bordered table-striped dataTable" style="width: 100%; font-size:13px">
					<thead>
						<tr>
							<th width="15%">Returnable DC No.</th>
							<th width="12%">DC No.</th>
							<th width="25%">Client Name</th>
							<th width="28%">Shipping Address</th>
							<th width="12%">Date</th>
							<th width="8%">View</th>
						</tr>
					</thead>
					<tbody style="width: 100%;">
						<c:forEach items="${returnableDcList}" var="rdc">
							<tr>
								<td>${rdc.returnableDcNo}</td>
								<td>${rdc.dcNo}</td>
								<td>${rdc.clientName}</td>
								<td>${rdc.shippingAddress}</td>
								<td>${rdc.date}</td>
								<td align="center">
									<a href="${pageContext.request.contextPath}/viewReturnableDC?id=${rdc.id}" class="btn btn-sm btn-light">
										<i class="fa fa-eye"></i>
									</a>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>

	</div>
	<tiles:insertAttribute name="footer" />
</div>
</body>
</html>
