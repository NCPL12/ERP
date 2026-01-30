<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>



<c:url var="ROOT" value="/"></c:url>
<c:url var="RESOURCES" value="/resources/"></c:url>
<!DOCTYPE html> 
<html>
<head>
</head>
<body>
<!-- Main Sidebar Container -->
  <aside class="main-sidebar sidebar-dark-primary elevation-4">
    <!-- Brand Logo -->
    <a href="http://ncpl.co" class="brand-link">
    <!-- for circled image use img-circle class in below img tag -->
     <%--  <img src="${pageContext.request.contextPath}/resources/dist/img/ncpl.jpg"  class="brand-image  elevation-3"
           style="opacity: .8"> --%>
           <img src="${pageContext.request.contextPath}/resources/dist/img/NCPL_Logo_Long_White_new.png">
     <%-- <span class="brand-text font-weight-light" style="font-size: larger;"><spring:message code="app.name" /></span> --%>
    </a>

    <!-- Sidebar -->
    <div class="sidebar">
      <!-- Sidebar user panel (optional) -->
      <%-- <div class="user-panel mt-3 pb-3 mb-3 d-flex">
        <div class="image">
          <img src="${pageContext.request.contextPath}/resources/dist/img/user-icon.png" class="img-circle elevation-2" alt="User Image">
        </div>
        <div class="info">
          <a href="#" class="d-block"><security:authorize access="isAuthenticated()">
    			 <security:authentication property="principal.username" /> 
			</security:authorize></a>
        </div>
      </div>
       --%>
      <div class="card" style="background-color: transparent;color: white;">
	  <div class="row no-gutters">
	     <div class="col-md-4">
			<div class=""style="padding-top: 17px; padding-left: 20px;">
	          <img src="${pageContext.request.contextPath}/resources/dist/img/user-icon.png" class="img-circle elevation-2" height= 50px; alt="User Image">
	        </div>
	    </div> 
    <div class="col-md-8">
      <div class="card-body" style="padding-top: 8px;">
      
        <h5 class="card-title" style="width: max-content !important;">
        <small>Logged in user : </small>
         <a href="#" class="d-block"><security:authorize access="isAuthenticated()">
    			 <security:authentication property="principal.username" /> 
			</security:authorize></a>
        </h5>
       
        <p class="card-text"> <a href="<c:url value="/logout" />" class="d-block"><small class="">Logout</small></a></p>
      </div>
    </div>
  </div>
</div>

      <!-- Sidebar Menu -->
      <nav class="mt-2">
        <ul class="nav nav-pills nav-sidebar flex-column" data-widget="treeview" role="menu" data-accordion="false">
          <!-- Add icons to the links using the .nav-icon class
               with font-awesome or any other icon font library -->
       
          <security:authorize access="hasAnyAuthority('ITEMMASTER','STORE','STORE USER')">
          <li class="nav-item has-treeview menu-open">
            <a href="#" class="nav-link active">
              <i class="nav-icon fas fa-tachometer-alt"></i>
              
                <spring:message code="items" />
                <i class="right fas fa-angle-left"></i>
              
            </a>
            
            <ul class="nav nav-treeview">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/itemMaster" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.Item.dashboard" />
                </a>
              </li>
                             
              
             </ul>
             <ul class="nav nav-treeview">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/companyAssets" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.company.assets" />
                </a>
              </li>
                             
              
             </ul>
          </li>
          </security:authorize>
          
          <security:authorize access="hasAnyAuthority('PURCHASE STORE')">
          <li class="nav-item has-treeview menu-open">
            <a href="#" class="nav-link active">
              <i class="nav-icon fab fa-dochub"></i>
             
                <spring:message code="master.name"/>
                <i class="right fas fa-angle-left"></i>
             
            </a>
            <ul class="nav nav-treeview">
            
            <!-- Party Dash board -->
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/partyList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.party.dashboard" />
                </a>
             </li>
              
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/itemMaster" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.Item.dashboard" />
                </a>
              </li>
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/companyAssets" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.company.assets" />
                </a>
              </li>
                             
              
             </ul>
            
          </li>
          </security:authorize>
          <!-- Creating Masters -->
          <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','SUPER ADMIN')">
          
           <li class="nav-item has-treeview menu-open">
            <a href="#" class="nav-link active">
              <i class="nav-icon fas fa-tachometer-alt"></i>
              
                <spring:message code="projects" />
                <i class="right fas fa-angle-left"></i>
              
            </a>
             <ul class="nav nav-treeview">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/dashboard" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="dashboard" />
                </a>
              </li>
                             
              
             </ul>
          </li>
             <li class="nav-item has-treeview menu-open">
            <a href="#" class="nav-link active">
              <i class="nav-icon fab fa-dochub"></i>
             
                <spring:message code="master.name"/>
                <i class="right fas fa-angle-left"></i>
             
            </a>
            <ul class="nav nav-treeview">
            
            <!-- Party Dash board -->
            <li class="nav-item">
                <a href="${pageContext.request.contextPath}/partyList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.party.dashboard" />
                </a>
             </li>
              
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/itemMaster" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.Item.dashboard" />
                </a>
              </li>
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/companyAssets" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="master.company.assets" />
                </a>
              </li>
             </ul>
            
          </li>
            </security:authorize>
          
           <li class="nav-item has-treeview menu-open">
           <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN','PURCHASE STORE','STORE USER','SALES')">
            <a href="#" class="nav-link active">
              <i class="nav-icon fas fa-tachometer-alt"></i>
              
                <spring:message code="sales.name" />
                <i class="right fas fa-angle-left"></i>
              
            </a>
            </security:authorize>
            
            <ul class="nav nav-treeview">
            <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE','STORE')">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/salesList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="sales.dashboard" />
                </a>
              </li>
               </security:authorize>
               <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN','STORE USER','SALES','PURCHASE STORE')">
               <li class="nav-item">
                <a href="${pageContext.request.contextPath}/dcList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>

                  <p><spring:message code="delivery.challan" /></p>
                </a>
              </li>
              </security:authorize>
              <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE')">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/returnableList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>

                  <p><spring:message code="dc.returnable" /></p>
                </a>
              </li>
              </security:authorize>
              <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','SUPER ADMIN')">
               <li class="nav-item">
                <a href="${pageContext.request.contextPath}/invoiceList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <p><spring:message code="invoice" /></p>
                </a>
              </li>
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/workOrderList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <p><spring:message code="work.order" /></p>
                </a>
              </li>
              </security:authorize>
               
             </ul>
          </li> 
           
          <!-- Purchase section started here -->
          <li class="nav-item has-treeview menu-open">
          <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN','PURCHASE STORE','STORE USER')">
            <a href="#" class="nav-link active">
              <i class="nav-icon fas fa-tachometer-alt"></i>
              
                <spring:message code="purchase.name" />
                <i class="right fas fa-angle-left"></i>
              
            </a>
            </security:authorize>
            <ul class="nav nav-treeview">
            <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN','PURCHASE STORE','STORE USER')">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/purchase" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="sales.purchase" />
                </a>
              </li>
              </security:authorize>
              <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN','STORE USER','PURCHASE STORE')">            
               <li class="nav-item">
                <a href="${pageContext.request.contextPath}/grnLists" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <p><spring:message code="sales.grn" /></p>
                </a>
              </li>
              </security:authorize>
              <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN')">
               <li class="nav-item">
                <a href="${pageContext.request.contextPath}/nonBillableList" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <p><spring:message code="non.billable" /></p>
                </a>
              </li>
                </security:authorize>
             </ul>
          </li>
        
          <!--Report section starts  -->
          <security:authorize access="hasAnyAuthority('ADMIN','PURCHASE','SUPER ADMIN','SALES','PURCHASE STORE')">
          <li class="nav-item has-treeview menu-open">
            <a href="#" class="nav-link active">
              <i class="nav-icon fas fa-tachometer-alt"></i>
              
                <spring:message code="reporting" />
                <i class="right fas fa-angle-left"></i>
              
            </a>
            
            <ul class="nav nav-treeview">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/sales_report" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="sales.report" />
                </a>
              </li>
                             
              
             </ul>
          </li>
          </security:authorize>
          <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN')">
            <li class="nav-item has-treeview menu-open">
            <a href="#" class="nav-link active">
              <i class="nav-icon fas fa-tachometer-alt"></i>
              
                <spring:message code="charts" />
                <i class="right fas fa-angle-left"></i>
              
            </a>
            <ul class="nav nav-treeview">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/soChart" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="sales.chart" />
                </a>
              </li>
              </ul>
            </li>
            </security:authorize>
          
          <security:authorize access="hasAnyAuthority('ADMIN','NORMAL USER','PURCHASE','STORE','SUPER ADMIN','PURCHASE STORE')">
          <!-- Archived section started here -->
          <li class="nav-item has-treeview menu-open">
            <a href="#" class="nav-link active">
              <i class="nav-icon fas fa-tachometer-alt"></i>
              
                <spring:message code="archived.data" />
                <i class="right fas fa-angle-left"></i>
              
            </a>
            
            <ul class="nav nav-treeview">
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/salesList_archived" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="archive.sales.dashboard" />
                </a>
              </li>
               <li class="nav-item">
                <a href="${pageContext.request.contextPath}/purchase_archived" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="archive.sales.purchase" />
                </a>
              </li>
                <li class="nav-item">
                <a href="${pageContext.request.contextPath}/dc_archived" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="archive.delivery.challan" />
                </a>
              </li> 
              <li class="nav-item">
                <a href="${pageContext.request.contextPath}/grn_archived" class="nav-link">
                  <i class="far fa-circle nav-icon"></i>
                  <spring:message code="archive.grn" />
                </a>
              </li>           
                             
             </ul>
          </li>
          </security:authorize>
            
          
            <!--Items section starts  -->
          
          
          </ul> 
      </nav>
      <!-- /.sidebar-menu -->
    </div>
    <!-- /.sidebar -->
  </aside>

  

  <!-- Control Sidebar -->
  <aside class="control-sidebar control-sidebar-dark">
    <!-- Control sidebar content goes here -->
  </aside>
  <!-- /.control-sidebar -->


</body>
</html>