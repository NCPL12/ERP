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
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title><tiles:insertAttribute name="title" /></title>
<tiles:insertAttribute name="header-resources" />
<script type="text/javascript" src="resources/js/userList.js"></script>
<script>

var data=${userList};
</script>

</head>
<body>
<body class="hold-transition sidebar-mini">
	<div class="wrapper">
		<tiles:insertAttribute name="header" />
		<tiles:insertAttribute name="sideMenu" />

		<div class="content-wrapper">
			<%-- <tiles:insertAttribute name="pageHeader" /> --%>
			 <div class="content-header content-header-padding">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0 text-dark">${pageHeader}  
            &nbsp;&nbsp;&nbsp;&nbsp;  
      <small>
      <a href="${pageContext.request.contextPath}/user" title="Add New User" id="Home" style="font-size: 16px;color: black;">
  <spring:message code="so.neworder"/></a>
      </small>
            </h1> 
            



          </div><!-- /.col -->
      
          <div class="col-sm-6 " style="padding-top: 4px;">
          
    
          <div class="form-inline float-sm-right">
          
          
                  
                 
                
                </div>
           
          </div><!-- /.col -->
        </div><!-- /.row -->
      </div><!-- /.container-fluid -->
    </div>
			<div id="salesDiv" class="card">
			<div class="card-body">
				<table id="userList" class="table table-bordered table-striped" style="width:100%">
					 <thead>
						<tr>
						 	<th width="10%">Name</th> 
						 	<th width="20%">Mobile</th>
						 	<th width="10%">PhotoId</th> 
						 	<th width="10%">PermanentAddress</th> 
						 	<th width="10%">TemporaryAddress</th> 
							<th width="10%">Contact</th>
							
						</tr>
					</thead>
					<tbody style="width: 100%;">
					</tbody>
				</table>
				</div>
			</div>
			
		</div>
		<tiles:insertAttribute name="footer" />
	</div>
	<!-- ./wrapper -->


</body>
</html>