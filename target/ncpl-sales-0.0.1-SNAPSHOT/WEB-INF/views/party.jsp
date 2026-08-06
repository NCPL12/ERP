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
<link rel="stylesheet" href="<c:url value="/resources/css/salesOrder.css" />">
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/party.js"></script>
<script src="${RESOURCES}js/common.js" ></script>

<script type="text/javascript">
   // var cityList=${cityList}
   var obj = '${partyObj}';
   obj = obj.replace(/\&/g, "\'");
   obj = obj.replace(/\&/g, "\"");
var partyObj = "";
if ('${partyObj}' != null && '${partyObj}' != "") {
partyObj= $.parseJSON(obj);
}
var categList=${categoryList}
var partyList  = ${partyList}


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
.padding{
padding:1px;
}
.modalHeaderStyle{
color: #343a40 ;
padding: 4px 4px 4px 4px ;
background: #4e595f ;
background-color: rgba(45, 147, 203, 0.84) ;
margin:0;
}
.contact-list{
border-collapse: collapse;
}
.hideIcon{
display:none;
}
table.contact-list>tbody>tr>td, table.contact-list>thead>tr>th {
    padding: 0px !important;
    padding-left: 2px !important
}
.select{
text-align:center
}

.footerDiv{
margin-left: auto;
    margin-right: auto;
}
.inputBox{
margin-top:0px!important;
    height: 20px!important;
}
.labelStyle{
text-align:right;
}

.border-color{
border-color: red;
}
.typeTable,.categoryTable,.designationTable{
width:100%!important;
}
.select2{
width:100% !important;
}
.form-control {
    margin-top: 1px!important;
    height: 24px!important;
}

.cursor{
 cursor:pointer;  
   }
.deleteButton{
 cursor:pointer;  
   }
 table.cityInputTable>tbody>tr>th{
padding: 2px 5px;
  }
  table.cityInputTable>tbody>tr>td{
padding: 2px 12px!important;
  }
  .select2-container--default .select2-selection--single .select2-selection__rendered {
    margin-top: -6px!important;
 }
 .designationDropdown + .select2 {
    width: 154px!important;
}
#stateDropdown + .select2 {
    width: 100px!important;
}
.footerDivinCity {
    width: 200px;
    margin-left: auto;
    margin-right: auto;
} 
.footerDivinCategory {
    width: 250px;
    margin-left: auto;
    margin-right: auto;
} 
.footerDivinType{
    width: 180px;
    margin-left: auto;
    margin-right: auto;
} 
</style>
</head>
<body>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
<tiles:insertAttribute name="header" />
<tiles:insertAttribute name="sideMenu" />

<div class="content-wrapper">
<div class="card">
<div class="card-body">
<form:form id="partyForm" method="POST" modelAttribute="party" action="${pageContext.request.contextPath}/add/party" >
<input type="hidden" name="id" id="partyId"/>
 <div class="row">
<div class="col-sm-8">
<div class="row padding">
<label for="Name" class="col-form-label form-control-sm col-sm-2">Name</label>
<div class="col-sm-2">
<select name="party_intial" id="party_intial" class="form-control form-control-sm" style="padding-top: 0px!important;">
<option value="Mr">Mr</option>
<option value="Mrs">Mrs</option>
<option value="Miss">Miss</option>
<option value="M/S">M/S</option>
<option value="Dr">Dr</option>
</select>
</div>
<div class="col-sm-8">
<form:input type="text" path="partyName" name="partyName" id="PartyNme" class="form-control form-control-sm"/>
<span id="spnFirstName" ></span>
</div>
</div>
<div class="row padding">
<label for="Address1" class="col-form-label form-control-sm col-sm-2">Address1</label>
		 
								
																											 
									  
			   
		 
		  
							 
																							  
		 
								
																											 
									  
			   
		  
							 
																					  
															 
																											   
														   
						 
			 
																		  
								  
			   
																									  
							   
																										  
			   
							  
																											   
									
			  
		  
							 
																					
							   
																						  
			   
																									  
							   
																										  
			   
							  
																											   
			  
		  
							 
																						
							   
																									
			   
																								
							   
																						
			   
							  

<div class="col-sm-10">
<form:input type="text" path="addr1" name="addr1" id="addr1" class="form-control form-control-sm"/>
<span id="spnaddr1" ></span>
</div>

</div>
<div class="row padding">
<label for="Address2" class="col-form-label form-control-sm col-sm-2">Address2</label>

<div class="col-sm-10">
<form:input type="text" path="addr2" name="addr2" id="addr2" class="form-control form-control-sm"/>
<span id="spnaddr2" ></span>
</div>
</div>
<div class="row padding">
<label for="City" class="col-form-label form-control-sm col-sm-2">City</label>
<div class="col-sm-2" style="display: inline-flex;">
<form:select path="city" name="city" id="cityDropDown" class="form-control form-control-sm select2">
<option selected disabled>Select City</option>
</form:select>
<i
class="add fa fa-plus-square" style="padding: 3px" id="addCity"
aria-hidden="true"></i>
</div>
<label for="phone1" class="col-form-label form-control-sm col-sm-2 labelStyle">Phone1</label>
<div class="col-sm-1">
<form:input type="text" path="" name="" id="countrycode1" class="form-control form-control-sm"/>
</div>
<div class="col-sm-5">
<form:input type="text" path="phone1" name="phone1" id="phne1" class="form-control form-control-sm"/>
<span id="phone1" ></span>
</div>
</div>
<div class="row padding">
<label for="Pin" class="col-form-label form-control-sm col-sm-2">PIN</label>
<div class="col-sm-2">
<form:input type="text" path="pin" name="pin" id="pincode" class="form-control form-control-sm" placeholder="Enter PIN Code"/>
<span id="spnpin" ></span>
</div>
<label for="phone2" class="col-form-label form-control-sm col-sm-2 labelStyle">Phone2</label>
<div class="col-sm-1">
<form:input type="text" path="" name="" id="countrycode2" class="form-control form-control-sm"/>
</div>
<div class="col-sm-5">
<form:input type="text" path="phone2" name="phone2" id="phne2" class="form-control form-control-sm"/>
</div>
</div>
<div class="row padding">
<label for="State" class="col-form-label form-control-sm col-sm-2">State</label>
<div class="col-sm-2">
<input type="text"  name="state" id="state" class="form-control form-control-sm" readonly>
</div>
<label for="Fax" class="col-form-label form-control-sm col-sm-2 labelStyle">Fax</label>
<div class="col-sm-1">
<form:input type="text" path="" name="" class="form-control form-control-sm"/>
</div>
<div class="col-sm-5">

<form:input type="text" path="" name="fax"  class="form-control form-control-sm"/>


</div>
</div>
<div class="row padding">
<label for="Country" class="col-form-label form-control-sm col-sm-2">Country</label>
<div class="col-sm-2">
<input type="text" name="country" id="country" class="form-control form-control-sm" readonly>
</div>
<label for="Email" class="col-form-label form-control-sm col-sm-2 labelStyle">Email1</label>
<div class="col-sm-6">
<form:input type="text" path="email1" name="email1" id="eml1" class="form-control form-control-sm"/>
<span id="emailL1" ></span>
</div>
</div>
<div class="row padding">
<label for="Country" class="col-form-label form-control-sm col-sm-2">Website</label>
<div class="col-sm-2">
<form:input type="text"  path="website" name="website" id="websit" class="form-control form-control-sm"/>
</div>
<label for="Email" class="col-form-label form-control-sm col-sm-2 labelStyle">Email2</label>
<div class="col-sm-6">
<form:input type="text" path="email2" name="email2" id="email2" class="form-control form-control-sm"/>
</div>
</div> 
</div>
      <div class="col-sm-4">
<div class="row padding">
<label for="Type" class="col-form-label form-control-sm col-sm-4">Type</label>
<div class="col-sm-6" style="display: inline-flex;">
<select id="typeDropdown" name="type" style=" padding: 0px;"
id="type"  class="form-control form-control-sm select2">
<option selected disabled>Select Type</option>
</select>
<div style="padding-left: 3px;">
<i
class="add fa fa-plus-square" id="addType"
aria-hidden="true"></i>
</div>
</div>
</div>
<div class="row padding">
<label for="Interstate" class="col-form-label form-control-sm col-sm-4">Interstate</label>
<div class="col-sm-1">
<input style="width: 100%;" type="checkbox"
value="" name="interState" id="interState" class="form-control form-control-sm" />
<input type="hidden" name="interStateHidden" id="interStateHidden" />
</div>
                </div>
<div class="row padding">
<label for="Country" class="col-form-label form-control-sm col-sm-4">Company</label>
<div class="col-sm-6">
<form:select path="" name=""
style="width: 100%; padding: 0px;"
class="form-control form-control-sm"><form:option
value="Neptune Controls"> Neptune Controls</form:option></form:select>
</div>
                </div>
<div class="row padding" >
<label for="Category" class="col-form-label form-control-sm col-sm-4">Category</label>

<div class="col-sm-6 categoryDiv ">
<input style="width: 87%; display: inline;" class="form-control form-control-sm" id="categoryInput" tabindex="" value="${partyCategoryListCount}" readonly>
 <i class="add fa fa-plus-square hideIcon  cursor" id="addCategory"
aria-hidden="true"></i>
</div>

                </div>
<div class="row padding">
<label for="Abbreviation" class="col-form-label form-control-sm col-sm-4">Abbreviation</label>
<div class="col-sm-6">
<form:input path="abbrivation" name="abbrivation" style="width: 100%;" type="text"
value="" class="form-control form-control-sm" />
</div>
                </div>
<div class="row padding" >
<label for="PAN" class="col-form-label form-control-sm col-sm-4">PAN</label>
<div class="col-sm-6">
<form:input path="pan" name="pan" style="width: 100%;" type="text"
value="" class="form-control form-control-sm" />
</div>
                </div>
<div class="row padding" >
<label for="CIN" class="col-form-label form-control-sm col-sm-4">CIN</label>
<div class="col-sm-6">
<form:input  path="cin" name="cin"  style="width: 100%;" type="text"
value="" class="form-control form-control-sm" />
</div>
                </div>
<div class="row padding" >
<label for="GST" class="col-form-label form-control-sm col-sm-4">GST</label>
<div class="col-sm-6">
<form:input  path="gst" name="gst" style="width: 100%;" type="text"
value="" class="form-control form-control-sm" />
</div>
                </div>
<div class="row padding">
<label for="Remarks" class="col-form-label form-control-sm col-sm-4">Remarks</label>
<div class="col-sm-6">
<form:input  path="remarks" name="remarks" style="width: 100%;" type="text"
value="" class="form-control form-control-sm" />
</div>
                </div>

                
</div>
    </div>
   


								 
																			  

<div id="" class="col-md-12"
style="height: 100%; max-height: 300px; width: 100%;padding: 27px 0px;">
													
															 
					
		   

<div
style="background: #EAF6F6; height: 23px; width: 100%;">
<p style="font-weight: bold; color: #010101">
&nbsp;Contacts : <a href="#" title="" id="addRow">Add
Contact</a>
</p>

																				
			   
			
												
</div>
																
																						 
								  
			   
																
																										   
															   
 <div style="width: 100%; height: 130px;">
															   
															  
			  
				
																						  

<table style="width: 100%;" id="contactTable" class="table contact-list">
<thead>
<tr
style="height: 27px; display: block;">
<th style="width: 23%; padding-left: 5px;">&nbsp;Name</th>
<th style="width: 15%; padding-left: 5px;">Designation
<i class="add fa fa-plus-square" id="addDesignation" style="padding-left: 3px"
aria-hidden="true"></i>
</th>
<th style="width: 15%; padding-left: 5px;">Mobile</th>
<th style="width: 3%; padding-left: 5px;"><i class="fab fa-whatsapp" aria-hidden="true"></i></th>
<th style="width: 12%; padding-left: 5px;">Phone</th>
<th style="width: 15%; padding-left: 5px;">Email</th>
<th style="width: 15%; padding-left: 5px;">Skype</th>
<th style="width: 2%; padding-right: 10px;">Del</th>
</tr>
</thead>
 <tbody style="width: 100%; max-height: 100px; display: block; overflow-y:scroll">

</tbody>

</table>


			 
								 

</div> 
<!-- Content Div closed -->

												  
											 
															  
																									
						  
</div>

				
<div class="button-div-style" align="center">
<button type="submit" id="savePartyBtn"
class="btn btn-primary btn-sm btn-inline">SAVE</button>
<button type="submit"  id="savePartyAndExitBtn" class="btn btn-primary btn-sm btn-inline">SAVE
&amp; EXIT</button>
</div>

</form:form>
														   
									
											 
													
											   
												 
								
		  
																												
		  
										 
													 
							
																					
			 
											
						 
														
														   
											   
										 
								 
			 
		   
			 
		  
												  
											  
										 
									   
											
													   
																					   
		  
		   
		 
</div>
	   
 
								  
															 
									
											 
													
</div>
													
								   
		  
																												
</div>
											
							

<!---model for adding and deleting category-->
<div class="modal fade" id="categoryModel" role="dialog">
<div class="modal-dialog modal-lg"
style="margin-left: 33%; margin-top: 0%;">
<div class="modal-content" style="width: 520px;">
<div class="modal-header modalHeaderStyle">
<h6 class="modal-title" id="categoryHeader">
<b>Add/Delete Category</b>
</h6>
<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
</div>
<form class="form" id="categoryForm">
<input type="hidden" name="id" id="categoryId"/>
<div class="modal-body">
<table id="category" class="categoryTable table table-bordered table-striped" >
</table>
<table class="modalTableBar botHeader">
<tr class="newRow">
<td><b>&nbsp;&nbsp;&nbsp;Category &nbsp;</b></td>
<td><input type="text" name="name" id="categoryName"
style="width: 350px; margin-left: 10%;"
class="newPartyTxtBox txtCaps" />
<div id="errorDiv"></div>
</td>
</tr>
</table>
</div>
<div class="modal-footer footerDivinCategory">
<button id="categoryMapBtn" type="button"
class="btn btn-primary autoRefresh"
data-dismiss="modal">Map</button>
<button id="saveCategory" type="submit"
class="btn btn-primary autoRefresh">Save</button>
<button type="reset" class="btn btn-primary" id="categoryResetBtn">Cancel</button>
</div>
</form>
</div>
</div>
</div>

<!-- designation modal starts -->
<div class="modal fade" id="designationModel" role="dialog">
<div class="modal-dialog modal-lg"
style="margin-left: 33%; margin-top: 0%;">
<div class="modal-content" style="width: 520px;">
<div class="modal-header modalHeaderStyle">
<h6 class="modal-title" id="designationHeader">
<b>Add/Delete Designation</b>
</h6>
<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
</div>
<form class="form" id="designationForm">
<div class="modal-body">

<table id="designationTable" class="designationTable table table-bordered table-striped">
</table>

<input type="hidden" name="id" id="designationId"/>
<table class="modalTableBar botHeader">
<tr class="newRow">
<td><b>&nbsp;&nbsp;&nbsp;Designation &nbsp;</b></td>
<td><input type="text" name="name" id="designation"
style="width: 350px;"
 class="newPartyTxtBox txtCaps" />
<div id="errorDiv"></div>
</td>
</tr>
</table>

</div>
<div class="button-div-style" align="center">
<button id="saveDesignation" type="submit"
class="btn btn-primary autoRefresh">Save</button>
<button type="reset" class="btn btn-primary" id="designationResetBtn">Cancel</button>
</div>
</form>

</div>
</div>
</div>


    <!---model for adding and deleting type-->
<div class="modal fade" id="typeModel" role="dialog">
<div class="modal-dialog modal-lg"
style="margin-left: 33%; margin-top: 0%;">
<div class="modal-content" style="width: 520px;">
<div class="modal-header modalHeaderStyle">
<h6 class="modal-title" id="typeHeader">
<b>Add/Delete Type</b>
</h6>
<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
</div>
<form class="form" id="typeForm">
<input type="hidden" name="id" id="typeId"/>
<div class="modal-body">
<table id="typeTable" class="typeTable table table-bordered table-striped">
</table>
<table class="modalTableBar botHeader">
<tr class="newRow">
<td><b>&nbsp;&nbsp;&nbsp;Type &nbsp;</b></td>
<td><input type="text" name="name" id="typeInput"
style="width: 350px; margin-left: 10%;"
class="newPartyTxtBox txtCaps" />
<div id="errorDiv"></div>
</td>
</tr>
</table>
</div>
<div class="modal-footer footerDivinType">
<button id="saveType" type="submit"
class="btn btn-primary autoRefresh">Save</button>
<button type="reset" class="btn btn-primary" id="typeResetBtn">Cancel</button>
	  
		   
			
		 
		
	   
 
								  

										   
</div>
</form> 
											 
													
											   
											 
</div>
</div>
																												
</div>
									 
							

<!-- /.designation modal ends -->
			 
	 
													 
														   
		  
								 
								 
								  
									
		   

<!---model for adding and deleting city-->
<div class="modal fade" id="cityModel" role="dialog">
<div class="modal-dialog modal-lg"
style="margin-left: 28%; margin-top: 0%;">
<div class="modal-content" style="width: 800px;">
<div class="modal-header modalHeaderStyle">
<h6 class="modal-title" id="cityHeader">
<b>Add/Delete City</b>
</h6>
<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
</div>
<form class="form" id="cityForm">
<div class="modal-body">

<table id="cityTable" class="typeTable table table-bordered table-striped">
											   
</table>

   <input type="hidden" name="id" id="cityId"/>
<table class="cityInputTable modalTableBar botHeader">
<tr>
<th>&nbsp;City &nbsp;</th>
<th>&nbsp;Code &nbsp;</th>
<th>&nbsp;State &nbsp;</th>
<th>&nbsp;Country &nbsp;</th>
</tr>

<tr class="newRow">
<td>
<!-- <select class="newPartyTxtBox" style="width: 85%; font-size:16px; " name="city_name" id="city_name" > </select> -->
				   
																		
									

<input type="text" name="name" id="newCity"
class="form-control form-control-sm" />
</td>
			 
		  
															  
										
													   
																				   
		  
		   
		 
		
	   

<td><input type="text" name="code" id="newCode"
 class="form-control form-control-sm" /></td>

<td style="width:20%;display: inline-flex;" ><select id="stateDropdown" name="state"
 class="form-control form-control-sm select2" style="padding-top: 0px!important;" >
<option value="" selected disabled>Select State</option>
</select><i
class="add fa fa-plus-square" style="padding: 3px" id="addState"
aria-hidden="true"></i></td>

<td><input type="text" id="getCountry"
class="form-control form-control-sm" readonly/></td>
</tr>
</table>
</div>
<div class="modal-footer footerDivinCity" align="center" >
<button id="saveCity" type="submit"
class="btn btn-primary autoRefresh">Save</button>
<button type="reset" class="btn btn-primary" id="cityResetBtn">Cancel</button>
</div>
</form>
</div>
</div>
</div>

<!-- state modal starts -->
<div class="modal fade" id="stateModel" role="dialog">
<div class="modal-dialog modal-lg" role="document"
style="margin-left: 33%; margin-top: 0%;">
<div class="modal-content" style="width: 520px;">
<div class="modal-header modalHeaderStyle">
<h6 class="modal-title">
<b id="stateHeader">Add State</b>
</h6>
<button type="button" class="close buttonDismiss" style="float:right" data-dismiss="modal">&times;</button>
</div>
<form class="form" id="stateForm">
<div class="modal-body">
<table class="stateInputTable modalTableBar botHeader">
<tr>
<th>&nbsp;State &nbsp;</th>
<th>&nbsp;Code &nbsp;</th>
<th>&nbsp;Country &nbsp;</th>
</tr>

<tr class="newRow">
<td>
<input type="text" name="name" id="newState"
class="form-control form-control-sm" />
</td>

<td><input type="text" name="code" id="newCode"
 class="form-control form-control-sm" /></td>

<td style="display: inline-flex;"><select id="countryDropdown" 
  name="country" class="form-control form-control-sm select2" style="padding-top: 0px!important;" >
<option value="" selected disabled>Select Country</option>
</select><i
class="add fa fa-plus-square" style="padding: 3px" id="addCountry"
aria-hidden="true"></i></td>
</tr>
</table>
<div class="button-div-style" align="center">
<button id="saveState" type="submit"
class="btn btn-primary autoRefresh">Save</button>
<button type="reset" class="btn btn-primary" id="stateResetBtn">Cancel</button>
</div>
</div>
            </form> 
</div>
</div>
</div>
    
<tiles:insertAttribute name="footer" />
</div>
</body>
</html>
