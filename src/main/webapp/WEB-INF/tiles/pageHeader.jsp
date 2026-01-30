<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<script type="text/javascript" src="resources/js/pageHeader.js"></script>
</head>
<body>
<!-- Content Header (Page header) -->
    <div class="content-header content-header-padding">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0 text-dark">${pageHeader} &nbsp;&nbsp;&nbsp;
            <a href="/ncpl-sales/salesList" title="Sales List Dashboard" id="purchaseDashBrd" style="font-size: 16px; color: black;"> 
            <i class="fa fa-bars" style="font-size:21px"></i>
            </a></h1>
          </div><!-- /.col -->
          <div class="col-sm-6 " style="padding-top: 4px;">
          <div class="form-inline float-sm-right">
                  <h1 class="m-0 text-dark" style="font-size: x-large;">Party : </h1>
                  <select id="partyDropDown" class="form-control" style="width:230px;height: 30px; padding: 1.5px; border-radius: 0;">
                 
                  </select>
                </div>
           
          </div><!-- /.col -->
        </div><!-- /.row -->
      </div><!-- /.container-fluid -->
    </div>
</body>
</html>