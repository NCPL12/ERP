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
<script type="text/javascript" src="${pageContext.request.contextPath}/resources/js/partyAddress.js"></script>
<script src="${RESOURCES}js/common.js" ></script>

<script>


$("#deleteaddress").hover(function() {
    $(this).attr('title', 'Delete Address');
});

var data=${AddressList};
//var partyId='${partyId}';
var pageContext = '${pageContext.request.contextPath}';
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

.inputBox{
margin-top:0px!important;
    height: 20px!important;
}
.labelStyle{
text-align:right;
}
.textAreaStyle{
    height: 270px;
     width: 99%; 
     resize: none; 
     border: 1px solid #ced4da!important; 
}
.textAreaStyle:hover{
    border: 1px solid #ced4da!important; 
}
.border-color{
border-color: red;
}
.text-field-large-nowrap{
 white-space: nowrap;
 }
 .address2inputwidth{
 width:126%;
 }
 .deleteButton{
 cursor:pointer;  
   }
   #cityDropDown +.select2 select2-container select2-container--default select2-container--focus{
margin-top: 3px!important;
   }
 #cityDropDown + .select2-container--default .select2-selection--single {
    border: 1px solid #ced4da!important;
    height: 32px!important;
padding-top: 9px!important;
}
</style>
</head>
<body class="hold-transition sidebar-mini">
<div class="wrapper">
<tiles:insertAttribute name="header" />
<tiles:insertAttribute name="sideMenu" />

<div class="content-wrapper">
<div class="card">
<div class="card-body">
<form:form id="partyAddressForm" method="POST" modelAttribute="Party Address" action="${pageContext.request.contextPath}/add/partyAddress" >
 <input type="hidden" name="partyId" value="${partyId}" id="partyId"/>
 <input type="hidden" name="id" id="partyAddId"/>

 <div class="row">
<div class="col-sm-8">
<div class="row padding">
<label for="Name" class="col-form-label form-control-sm col-sm-2">Name</label>
<div class="col-sm-10">
<form:input type="text" path="partyName" name="partyName" id="PartyNme" value="${partyName}" readonly="true" class="form-control form-control-sm"/>
</div>
</div>
<div class="row padding">
<label for="Address1" class="col-form-label form-control-sm col-sm-2">Address1</label>
<div class="col-sm-10">
<form:input type="text" path="addr1" name="addr1" id="addr1" class="form-control form-control-sm"/>

</div>

</div>
<div class="row padding">
<label for="Address2" class="col-form-label form-control-sm col-sm-2">Address2</label>

<div class="col-sm-4">
<form:input type="text" path="addr2" name="addr2" id="addr2" class="form-control form-control-sm address2inputwidth"/>
                                    </div>
                                    <label for="contact" class="col-form-label form-control-sm col-sm-2 labelStyle">Contact</label>
                                    <div class="col-sm-4">
<form:input type="text" path="contact" name="contact" id="contact" class="form-control form-control-sm"/>
</div>
</div>
<div class="row padding">
<label for="City" class="col-form-label form-control-sm col-sm-2">City</label>
<div class="col-sm-2">
<form:select path="city" name="city" id="cityDropDown" class="form-control form-control-sm select2" style="margin-top: 3px!important;">
<option selected disabled>Select City</option>
 <c:forEach items="${cityList}" var="city">${city.name}
<form:option value="${city.id}">${city.name}</form:option>
</c:forEach> 
</form:select>
</div>
<label for="phone1" class="col-form-label form-control-sm col-sm-2 labelStyle">Phone1</label>
<div class="col-sm-1">
<form:input type="text" path="" name="" id="countrycode1" class="form-control form-control-sm"/>
</div>
<div class="col-sm-5">
<form:input type="text" path="phone1" name="phone1" id="phne1" class="form-control form-control-sm"/>
</div>
</div>
<div class="row padding">
<label for="Pin" class="col-form-label form-control-sm col-sm-2">PIN</label>
<div class="col-sm-2">
<form:input type="text" path="pin" name="pin" id="pincode" class="form-control form-control-sm" placeholder="Enter PIN Code"/>
<span id="spnpin" style="display:none; color: red; font-size: 12px; font-weight: bold;"></span>
</div>
<label for="phone2" class="col-form-label form-control-sm col-sm-2 labelStyle">Phone2</label>
<div class="col-sm-1">
<form:input type="text" path="" name="" id="countrycode2" class="form-control form-control-sm"/>
</div>
<div class="col-sm-5">
<form:input type="text" path="phone2" name="phone2" id="phone2" class="form-control form-control-sm"/>
</div>
</div>
<div class="row padding">
<label for="State" class="col-form-label form-control-sm col-sm-2">State</label>
<div class="col-sm-2">
<input type="text"  name="state" id="state" class="form-control form-control-sm" readonly>
</div>
<div id="stateErrDiv" ></div>
<label for="Fax" class="col-form-label form-control-sm col-sm-2 labelStyle">Fax</label>
<div class="col-sm-1">
<form:input type="text" path="" name="" class="form-control form-control-sm"/>
</div>
<div class="col-sm-5">
<form:input type="text" path="faxnumber" name="faxnumber" id="faxnumber" class="form-control form-control-sm"/>
</div>
</div>
<div class="row padding">
<label for="Country" class="col-form-label form-control-sm col-sm-2">Country</label>
<div class="col-sm-2">
<input type="text" name="country" id="country" class="form-control form-control-sm" readonly>
</div>
<label for="Email" class="col-form-label form-control-sm col-sm-2 labelStyle">Email1</label>
<div class="col-sm-6">
<form:input type="text" path="email1" name="email1" id="email1" class="form-control form-control-sm"/>
<span id="emailL1" ></span>
</div>
</div>
<div class="row padding">
<label for="Country" class="col-form-label form-control-sm col-sm-2 ">Website</label>
<div class="col-sm-2">
<form:input type="text"  path="website" name="website" id="websit" class="form-control form-control-sm"/>
</div>
<label for="Email" class="col-form-label form-control-sm col-sm-2 labelStyle">Email2</label>
<div class="col-sm-6">
<form:input type="text" path="email2" name="email2" id="email2" class="form-control form-control-sm"/>
</div>
</div> 

<div class="row padding">
<label for="Country" class="col-form-label form-control-sm col-sm-2 ">GST</label>
<div class="col-sm-2">
<form:input type="text"  path="gst" name="gst" id="gst" class="form-control form-control-sm"/>
</div>

</div> 
</div>
        <div class="col-sm-4">
            <textarea class="col-form-label form-control-sm textAreaStyle" placeholder=" Easy Notepad"></textarea>
        </div>   
    </div>


<div id="newBottomDiv" class="">
   <div id="content"> 
   <table style="width: 100%;" id="addressListTable" class="display address-list">
   <thead style="background: #EAF6F6;">
   <tr style="height:30px;">
<th colspan="8" style="font-weight: bold; border-bottom: 1px solid #337ab7; border-top: 1px solid #337ab7; color: #010101; font-size: 15px;">&nbsp;
Address Details
  </th>
</tr>       
    <tr style="height: 25px; font-size: 15px; background: rgba(242, 246, 248, 1);" id="listTblHeader">
     <th style="width: 0%; display: none; "></th>
     <th style="width: 10%;  border-bottom: 1px solid #CCC;">&nbsp;Person</th>
     <th style="width: 9%;  border-bottom: 1px solid #CCC;">&nbsp;City</th>
     <th style="width: 8%;  border-bottom: 1px solid #CCC;">&nbsp;State</th>
     <th style="width: 10%;  border-bottom: 1px solid #CCC;">Country</th> 
     <th style="width: 8%; border-bottom: 1px solid #CCC;">Phone No</th>
     <th style="width: 10%; border-bottom: 1px solid #CCC;">Email</th>
     <th style="width: 5%; border-bottom: 1px solid #CCC;">GST</th>
     <th style="width: 8%; border-bottom: 1px solid #CCC;">Website</th>
     <th style="width: 5%; border-bottom: 1px solid #CCC; text-align: center;">Delete</th>
    </tr>
   </thead>
    
    
    
    <tbody style="width: 100%;">
     
    </tbody>
 
    
   </table>
   
   
  </div> <!-- Content Div closed -->
  
  </div>

<div class="button-div-style" align="center">
<button type="submit" id="savePartyBtn" name="save" value="partyAddressSave"
class="btn btn-primary btn-sm btn-inline">SAVE</button>
<button type="submit" name="saveAndExit" value="partyAddressSaveAndExit" id="partyAddressSaveAndExit" class="btn btn-primary btn-sm btn-inline">SAVE
&amp; EXIT</button>
<input type="hidden" name="savePartyAddress" id="savePartyAddressBtn" value="" />
</div>

</form:form>
</div>
</div>
</div>

<tiles:insertAttribute name="footer" />
</div>
</body>
</html>
