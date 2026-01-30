<%-- <%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head>
<body>



</body>
</html> --%>


<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>

<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />
<script type="text/javascript" src="resources/js/user.js"></script>
<script type="text/javascript">

</script>
<style type="text/css">
.positionOfTextBox {
	height: 24px !important;
	display: inherit;
	/* position: relative;
 top: 8px;
 width: 99%; */
	border-radius: 0 !important;
}
.table td, .table th {
    vertical-align: unset;
}
</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
			<tiles:insertAttribute name="pageHeader" />
			<div class="card">
			<div class="card-body">
			 <form:form id="userForm" method="POST"  action="${pageContext.request.contextPath}/add/user" >

					<div class="row">

						<div class="col-md-12">
							<table id="userMasterTable" class="table table-bordered paddingTbl">
								<tbody style="display: table">
									
										<tr>
										<td>Name</td>
										<td colspan="2"><form:input path="name" name="name" style="width: 71%;" type="text"
											value="" class="form-control positionOfTextBox" /></td>
									</tr>
									
									<tr>
										<td>Mobile</td>
										<td colspan="2"><form:input path="mobile" name="mobile" style="width: 71%;" type="text"
											value="" class="form-control positionOfTextBox" /></td>
									</tr>
									<tr>
									<td>PhotoId</td>
										<td colspan="2"><form:select path="photoId" name="photoId" id="photoIdDropDown" style="width: 71%; padding: 0px;"
											class="form-control positionOfTextBox">

											<c:forEach items="${photoIdList}" var="city">${photoId.name}
												<form:option value="${photoId.id}">${photoId.name}</form:option>
												</c:forEach>
										</form:select>${photoId.size}<i class="add fa fa-plus-square" style="padding-left: 5px"

											aria-hidden="true"></i></td>
											
									</tr>		
									<tr>
										<td>TemporaryAddress</td>
										<td colspan="5"><form:input path="Taddr" name="Taddr" style="width: 100%;" type="text"
											value="" class="form-control positionOfTextBox" /></td>
									</tr>
									<tr>
										<td>PermanentAddress</td>
										<td colspan="5"><form:input path="Paddr" name="Paddr" style="width: 100%;" type="text"
											value="" class="form-control positionOfTextBox" /></td>
									</tr>
									<tr>
										
										<td>Contact</td>
										<td colspan="2"><form:input path="contact" name="contact"   style="width: 27%;" type="text"
											value="" class="form-control positionOfTextBox" /> </td>
									</tr>
									
									
								</tbody>
							
                             </table>

						</div> 
						<!-- Content Div closed -->

					</div>
					<div class="button-div-style" align="center">
						<button type="submit" id="save"
							class="btn btn-primary btn-sm btn-inline">SAVE</button>
						<button type="button" class="btn btn-primary btn-sm btn-inline">SAVE
							&amp; EXIT</button>
					</div>

				</form:form>
				</div>
			</div>
		</div>
		<tiles:insertAttribute name="footer" />
	</div>
</body>
</html>