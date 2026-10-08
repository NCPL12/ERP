/**
 * 
 */
$(document).ready(function () {
	$(".total").attr("readonly","readonly");
	$(".gst").attr("readonly","readonly");
	$(".gst1").attr("readonly","readonly");
	$(".grandTotal").attr("readonly","readonly");
	$('#clientPoDropdown option:not(:first)').remove();
	$.each(salesList, function( key, value ) {
		var clientPoNumber=value.clientPoNumber;
		if(clientPoNumber!=null){
			if(clientPoNumber!=""){
		
		  $('#clientPoDropdown').append('<option value='+value.id+'>'+clientPoNumber+'</option>'); 
			}
		}
	});
	
	getType();
	
	/***Amount Calculation for dc Items**/
	$(document).on('keyup mouseup', '.dcQty',function () {
		var dcIndex = $(this).closest("tr").index();
		var dcParent = $(this).closest('tr');
		
		var dcSupplyPriceVal=$(this).closest("tr").find("td").eq(6).text();
		var dcServicePriceVal=$(this).closest("tr").find("td").eq(7).text();
		var dcSuplyP = dcSupplyPriceVal.includes(",");
		var dcServP=dcServicePriceVal.includes(",");
		if (dcSuplyP == true) {
			dcSupplyPriceVal = dcSupplyPriceVal.replace(/,/g, "");
		}
		if (dcServP == true) {
			dcServicePriceVal = dcServicePriceVal.replace(/,/g, "");
		}
		dcParent.find('.dcAmount').text((parseFloat(dcParent.find('.dcQty').val()) * parseFloat(dcSupplyPriceVal))+(parseFloat(dcServicePriceVal)*parseFloat(dcParent.find('.dcQty').val()) ))

		var dcAmountPrice = parseFloat($("#dcAmount" + dcIndex).text());
		$("#dcAmount" + dcIndex).text(commaSeparateNumber(dcAmountPrice));
		if (dcParent.find('.dcAmount').text() == "NaN") {
			dcParent.find('.dcAmount').text("");
		}
		dcTotalCalculation();
		 
		
	});
	
	/***Amount Calculation for so Items**/
	$(document).on('keyup mouseup', '.soQty',function () {
		var soIndex = $(this).closest("tr").index();
		var soParent = $(this).closest('tr');
		
		var soSupplyPriceVal=$(this).closest("tr").find("td").eq(6).text();
		var soServicePriceVal=$(this).closest("tr").find("td").eq(7).text();
		var soSuplyP = soSupplyPriceVal.includes(",");
		var soServP=soServicePriceVal.includes(",");
		if (soSuplyP == true) {
			soSupplyPriceVal = soSupplyPriceVal.replace(/,/g, "");
		}
		if (soServP == true) {
			soServicePriceVal = soServicePriceVal.replace(/,/g, "");
		}
		soParent.find('.soAmount').text((parseFloat(soParent.find('.soQty').val()) * parseFloat(soSupplyPriceVal))+(parseFloat(soServicePriceVal)*parseFloat(soParent.find('.soQty').val()) ))

		var soAmountPrice = parseFloat($("#soAmount" + soIndex).text());
		$("#soAmount" + soIndex).text(commaSeparateNumber(soAmountPrice));
		if (soParent.find('.soAmount').text() == "NaN") {
			soParent.find('.soAmount').text("");
		}
		soTotalCalculation();
		
		
	});
	
	/***Quantity Validation for on change of soQty **/
	$('#invoiceTable').on("change", '.soQty', function () {
		let salesItemId = $(this).closest("tr").find("td").eq(1).text();
		let newQuantity = $(this).closest("tr").find("td").eq(4).find('input').val();
		let qtyInpt = $(this).closest("tr").find("td").eq(4).find('input');
		$.ajax({
			type: "GET",
			url: api.SALES_ITEM_BY_SALESITEMID + "?id=" + salesItemId,
			success: function (response) {
				console.log("edit response:", response);
				qtyInpt.removeClass('border-color');
				let salesQuantity = Math.round(response.quantity * 100) / 100;

				if (newQuantity > salesQuantity) {
					$.error("Entered Quantity is greater than " + salesQuantity);
					qtyInpt.addClass('border-color');
				}

			},  
			complete:function(resp){
				if(resp.status==500){
					$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
				}
			},
			error : function(e) {
				console.log(e);
			}  	
		});
	});
	
	/***Quantity Validation for on change of dcQty **/
	$('#invoiceTable').on("change", '.dcQty', function () {
		let dcItemId = $(this).closest("tr").find("td").eq(1).text();
		let newQuantity = $(this).closest("tr").find("td").eq(4).find('input').val();
		let qtyInpt = $(this).closest("tr").find("td").eq(4).find('input');
		$.ajax({
			type: "GET",
			url: api.GET_DCITEM_BYDCITEMID + "?id=" + dcItemId,
			success: function (response) {
				console.log("edit response:", response);
				qtyInpt.removeClass('border-color');
				let dcQuantity = Math.round(response.todaysQty * 100) / 100;

				if (newQuantity > dcQuantity) {
					$.error("Entered Quantity is greater than " + dcQuantity);
					qtyInpt.addClass('border-color');
				}

			},  
			complete:function(resp){
				if(resp.status==500){
					$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
				}
			},
			error : function(e) {
				console.log(e);
			}  	
		});
	});
	
	//on double click of list display view page
	if(invoiceObj!=""){
		
		$("#buttonDiv").hide();
		    	
		$("#paymentStatusDiv").show()
		if(invoiceObj.type=="Supply"){
			if(invoiceObj.dcNumber=="All"||invoiceObj.dcNumber.includes(",")){
				getAllDcItemsForAllDcOnDoubleClick(invoiceObj.soNumber);
				getStateNameBySoId(invoiceObj.soNumber);
				$("#dcDropdown").replaceWith('<input type="text" class="form-control PositionofTextbox dcNum" style="width:150px" id="dcNum" name="dcNumber">');
				$("#dcNum").val("All");
			}else{
			getAllDcItems(invoiceObj.dcNumber);
			getStateNameBySoId(invoiceObj.soNumber)
			$("#dcDropdown").replaceWith('<input type="text" class="form-control PositionofTextbox dcNum" style="width:150px" id="dcNum" name="dcNumber">');
			$("#dcNum").val(invoiceObj.dcNumber);
			}
		}
		if(invoiceObj.type=="Service"){
		getAllSoItems(invoiceObj.soNumber);
		$("#dcDropdown").replaceWith('<input type="text" class="form-control PositionofTextbox dcNum" style="width:150px" id="dcNum" name="dcNumber">');
		$("#dcNum").val(invoiceObj.dcNumber);
		}
		$('#clientPoDropdown').val(invoiceObj.soNumber);
		$('#clientPoDropdown').attr("disabled",true);
		$("#clientPoDropdown").select2(invoiceObj, {id: invoiceObj.soNumber, a_key:invoiceObj.soNumber});
		//$("#clientPoDropdown").val(invoiceObj.clientPoNumber);
		$("#typeDropdown").replaceWith('<input type="text" class="form-control PositionofTextbox typeVal" style="width:150px" id="typeVal" name="type">');
		$("#typeVal").val(invoiceObj.type);
		
		
		$("#dcNum,#typeVal,#soNum").next(".select2-container").hide();
		$("#dcNum,#typeVal,#soNum").attr("disabled",true);
		getinvoiceObjectByInvoiceId(invoiceObj.invoiceId);
	}
});

$(document).on("click","#updatePaymentStatusBtn",function(){
	$("#paymentDropdown").val("").trigger("change");
	$("#transactionNumber").val("");
	$("#paymentRemarks").val("");
	$("#paymentModal").modal("show");
})
$(document).on("change","#paymentDropdown",function(){
	$("#updatePaymentBtn").attr('disabled', false);
	var paymentOption = $(this).val();
	$("#transactionNumber").val("");
	$("#paymentRemarks").val("")
	if(paymentOption=="ByCash"){
		$("#remarksRow").show();
		$("#transactionNoRow").hide();
	}else if(paymentOption==""){
		$("#remarksRow").hide();
		$("#transactionNoRow").hide();
	}else{
		$("#remarksRow").hide();
		$("#transactionNoRow").show();
	}
})

$(document).on("click","#updatePaymentBtn",function(){
	$("#updatePaymentBtn").attr('disabled', 'disabled');
	var invoiceNo=$("#invoiceNum").val();
	var paymentMode = $("#paymentDropdown option:selected").val();
	var paymentRemarks = $.trim($("#paymentRemarks").val());
	var transactionNumber = $("#transactionNumber").val();
	if(paymentMode==""){
		$.error("Please select Payment mode");
	}else if(paymentMode=="ByCash"&& paymentRemarks==""){
		$.error("Please enter Payment Remarks");
	}else if(paymentMode!="ByCash" && transactionNumber==""){
		$.error("Please enter UTR")
	}else{
	
	$.ajax({
		type : "POST",  
		url : api.UPDATE_PAYMENT_STATUS +"?invoiceNo="+invoiceNo+"&paymentMode="+paymentMode+"&paymentRemarks="+paymentRemarks+"&transactionNumber="+transactionNumber,
		success : function(response) {
			if(response == true){
			$.success("Payment status updated successfully");
			}
			$("#paymentDropdown").val("").trigger('change');
			$("#transactionNumber").val("");
			$("#paymentRemarks").val("");
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  
	}); 
	}
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#updatePaymentBtn").attr('disabled', false);
		});
})

function getAllDcItemsForAllDcOnDoubleClick(soId){
	$.ajax({
		method : 'GET',
		url : api.GET_DCITEMLISTBYAllDC +"?soId="+soId,
		success : function(response) {
			$("#invoiceTable tbody").empty();
			$(".total").val("");
			$(".gst").val("");
			$(".gst1").val("");
			$(".grandTotal").val("");
			$.each(response,function(index,value){
				
				var rowNum=index+1;
				//var amountPrice=((value.todaysQty)*(value.supplyPrice)+(value.servicePrice)*(value.todaysQty))
				var amountPrice=((value.todaysQty)*(value.supplyPrice));
				var dcSupplyPrice=commaSeparateNumber(value.supplyPrice);
				//var dcServicePrice=commaSeparateNumber(value.servicePrice);
				var dcServicePrice=0.0;
				
				var dcAmount=commaSeparateNumber(amountPrice);
				
				
				var dcItems = "<tr><td width='5%'>"+value.slNo+"</td><td style='display:none'>" + value.dcItemId + "</td><td width='37%'>" + value.description + "</td>" +
						"<td width='12%'>" + value.hsnCode + "</td><td width='5%'><input name='items["+index+"].quantity' id='dcQty"+index+"' type='text' class='form-control PositionofTextbox dcQty' value='"+value.todaysQty+"'/></td><td width='9%'>" + value.unit + "</td>" +
						"<td width='10%'>" + dcSupplyPrice + "</td><td width='12%'>" + dcServicePrice + "</td><td class='dcAmount'id='dcAmount"+index+"'  width='10%'>" + dcAmount + "</td><td style='display:none'><input type='hidden' name='items["+index+"].amount' id='hiddenDcAmount"+index+"'  /></td>" +
								"<td style='display:none'><input type='hidden' id='dcQuantity" + index + "' value='"+value.todaysQty+"'></td></tr>";
				$("#invoiceTable tbody").append(dcItems);
				
			})	
			dcTotalCalculation();
			$('input').removeClass("has-error");
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	

	});
}


/*//validating quantity on only allowing numeric values
$(document).on("focusout",".dcQty,.soQty",function(e) {
        var qtyValue = this.value;
		var digits= new RegExp(/^[0-9]+$/);
		var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
		if (qtyValue.match(digits)||qtyValue=="") {

		}
		else{
        	$.error("only digits are allowed for qty");
		}
    });
*/
//total,gst and grand total calculation for dcItems
function dcTotalCalculation(){
	var sum=0;
    var amountArr=[];
    $('.dcAmount').each(function(index,value) {
    	var c = $("#dcAmount"+index).text();
    	c = c.replace(/,/g,"");
    	
    	var amount=parseFloat(c);
	    sum+=amount;
	    var sumWithComma = commaSeparateNumber(sum);
	    $(".total").attr("value", sumWithComma);
	    $("#total").val(sumWithComma);
	    var state=$("#state").val();
	    var gst=0;
	    var gst1=0;
	    if(state=="Karnataka"){
	    	var gstValue = $("#taxDropDown").val();
	    	var gstRate = gstValue/2;
	     gst=gstRate*sum;
	     gst = gst/100;
	     gst1= gst;
	    }else{
	    var gstValue = $("#taxDropDown").val();
	     gst=gstValue*sum;
	     gst = gst/100;
	    }
	    gst = Math.round(gst * 100) / 100
	    gst1 = Math.round(gst1 * 100) / 100
	    var gstWithComma = commaSeparateNumber(gst);
	    $(".gst").attr("value", gstWithComma);
	    $(".gst1").attr("value", gstWithComma);
	    $("#gst").val(gstWithComma);
	    $("#gst1").val(gstWithComma);
	    var grandTotal=sum+gst+gst1;
	    grandTotal = Math.round(grandTotal * 100) / 100
	    grandTotal=commaSeparateNumber(grandTotal);
	    $(".grandTotal").attr("value", grandTotal);
	    $("#grandTotal").val(grandTotal);
	    
	    });
}

//total,gst and grand total calculation for soItems
function soTotalCalculation(){
var sum=0;
var amountArr=[];
$('.soAmount').each(function(index,value) {
	var c = $("#soAmount"+index).text();
	c = c.replace(/,/g,"");
	
	var amount=parseFloat(c);
    sum+=amount;
    var sumWithComma = commaSeparateNumber(sum);
    $(".total").attr("value", sumWithComma);
    $("#total").val(sumWithComma);
    var state=$("#state").val();
    var gst=0;
    var gst1=0;
    if(state=="Karnataka"){
    	var gstValue = $("#taxDropDown").val();
    	var gstRate = gstValue/2;
     gst=gstRate*sum;
     gst = gst/100;
     gst1= gst;
    }else{
    	  var gstValue = $("#taxDropDown").val();
 	     gst=gstValue*sum;
 	     gst = gst/100;
    }
    gst = Math.round(gst * 100) / 100
    gst1 = Math.round(gst1 * 100) / 100
    var gstWithComma = commaSeparateNumber(gst);
    $(".gst").attr("value", gstWithComma);
    $("#gst").val(gstWithComma);
    $(".gst1").attr("value", gstWithComma);
    $("#gst1").val(gstWithComma);
    var grandTotal=sum+gst+gst1;
    grandTotal = Math.round(grandTotal * 100) / 100
    grandTotal=commaSeparateNumber(grandTotal);
    $(".grandTotal").attr("value", grandTotal);
    $("#grandTotal").val(grandTotal);
    
    });
}

//on change of client po number dropdown get id and check for type.
$(document).on("change","#clientPoDropdown",function(){
	
	 $('#taxDropDown').prop('selectedIndex',0);
	var soId = $(this).val();
	$("#soNumber").val(soId);
	$("#typeDropdown").val("").trigger('change');
	$("#dcDropdown").val("").trigger('change');
	$("#invoiceTable tbody").empty();
	$(".total").val("");
	$(".gst").val("");
	$(".gst1").val("");
	$(".grandTotal").val("");
	$("#state").val("");
	getType();
	getStateNameBySoId(soId);
});

function getStateNameBySoId(soId){
	$.ajax({
		method : 'GET',
		url : api.GET_SALESORDER_BYID  +"?salesOrderId="+soId,
		success : function(response) {
			console.log(response);
			if(response!=null){
				var stateName =null;
				if(response.party.id == response.billingAddress){
					 stateName=response.party.party_city.state.name;
					$("#state").val(stateName);
					if(stateName=="Karnataka"){
						$('table#invoiceTable tr#sgst').removeAttr("hidden","hidden");
						//$('table#invoiceTable').append('<tr id="sgst"><td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td><td colspan="2"><input type="text" id="gst1" name="gst1"class="form-control PositionofTextbox gst alignright" value="" />	</td></tr>');
						$("#gstColumn").html("CGST @ 9%");
						$("#gstColumn1").html("SGST @ 9%");
					

						$("#gstPercentage").text("9%");
						$("#gstPercentage1").text("9%");
					}else{
						$('table#invoiceTable tr#sgst').attr("hidden","hidden");
						
						var gstValue = $("#taxDropDown").val();
						//$('table#invoiceTable').append('<tr id="sgst hidden" ><td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td><td colspan="2"><input type="text" id="gst1" name="gst1"class="form-control PositionofTextbox gst alignright" value="" />	</td></tr>');
						$("#gstColumn").html("IGST @"+gstValue+"%");
						$("#gstPercentage").text(gstValue+"%");
						//$('table#invoiceTable tr#sgst').remove();
						$("#gstColumn").html("IGST @ 18%");
						$("#gstPercentage").text("18%");
					}
			}
			else{
			
				$("#gstPercentage").text("");
				getPartyAddressByID(response.billingAddress);
			}
				}
				
					//var stateName=response.party.party_city.state.name;
					//$("#state").val(stateName);
					
			
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	

	});
	
}

//on change of type if type is supply then populate dc dropdown else disable dc dropdown.
function getType(){
	$(document).on("change","#typeDropdown",function(){
	
		$("#dcDropdown").val("").trigger('change');;
		var type=$("#typeDropdown").val();
		$("#type").val(type);
		var soId=$("#clientPoDropdown").val();
		$("#invoiceTable tbody").empty();
		$(".total").val("");
		$(".gst").val("");
		$(".gst1").val("");
		$(".grandTotal").val("");
		if(type=='Supply'){
			$("#dcDropdown").attr("disabled",false);
			getAllDc(soId);
		}else if(type=='Service'){
			$("#dcDropdown").attr("disabled",true);
			getAllSoItems(soId);
		}else{
			$("#dcDropdown").attr("disabled",true);
		}
	});
}

//on chnage of dc dropdown if value is 'All' then populate all dc items for all dc by soId, if value is dc number then populate dc items in below table
$(document).on("change","#dcDropdown",function(){
	var dcId = $(this).val();
	$("#dcNumber").val(dcId);
	if(dcId=="All"){
		var soId=$("#clientPoDropdown").val();
		getAllDcItemsForAllDc(soId);
	}else if(dcId==""){
		$("#invoiceTable tbody").empty();
		$(".total").val("");
		$(".gst").val("");
		$(".gst1").val("");
		$(".grandTotal").val("");
	}else{
		getAllDcItems(dcId);
	}
	
});


//to display all the dc items for all dc by sales order id
function getAllDcItemsForAllDc(soId){
	$.ajax({
		method : 'GET',
		url : api.GET_DCITEMLISTByAllDC_BYSOID  +"?soId="+soId,
		success : function(response) {
			$("#invoiceTable tbody").empty();
			$(".total").val("");
			$(".gst").val("");
			$(".gst1").val("");
			$(".grandTotal").val("");
			$.each(response,function(index,value){
				
				var rowNum=index+1;
				//var amountPrice=((value.todaysQty)*(value.supplyPrice)+(value.servicePrice)*(value.todaysQty))
				var amountPrice=((value.todaysQty)*(value.supplyPrice));
				var dcSupplyPrice=commaSeparateNumber(value.supplyPrice);
				//var dcServicePrice=commaSeparateNumber(value.servicePrice);
				var dcServicePrice=0.0;
				
				var dcAmount=commaSeparateNumber(amountPrice);
				
				
				var dcItems = "<tr><td width='5%'>"+value.slNo+"</td><td style='display:none'>" + value.dcItemId + "</td><td width='37%'>" + value.description + "</td>" +
						"<td width='12%'>" + value.hsnCode + "</td><td width='5%'><input name='items["+index+"].quantity' id='dcQty"+index+"' type='text' class='form-control PositionofTextbox dcQty' value='"+value.todaysQty+"' readonly='readonly'/></td><td width='9%'>" + value.unit + "</td>" +
						"<td width='10%'>" + dcSupplyPrice + "</td><td width='12%'>" + dcServicePrice + "</td><td class='dcAmount'id='dcAmount"+index+"'  width='10%'>" + dcAmount + "</td><td style='display:none'><input type='hidden' name='items["+index+"].amount' id='hiddenDcAmount"+index+"'  /></td>" +
								"<td style='display:none'><input type='hidden' id='dcQuantity" + index + "' value='"+value.todaysQty+"'></td></tr>";
				$("#invoiceTable tbody").append(dcItems);
				
			})	
			dcTotalCalculation();
			$('input').removeClass("has-error");
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	

	});
}

//get All the dc list by SO id
function getAllDc(soId){

	$.ajax({
		method : 'GET',
		url : api.GET_DCLIST_BYSOID  +"?soId="+soId,
		success : function(response) {
			$('#dcDropdown option:not(:first)').remove();
			$('#dcDropdown').append('<option value="All">All</option>');
	    	$.each(response, function( key, value ) {
	    		  $('#dcDropdown').append('<option value='+value.dcId+'>'+value.dcId+'</option>'); 
	    	});
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	

	});
}

//get all the SO items
function getAllSoItems(soId){
	var className="invoice";
	  var row=$('table > tbody  > tr').length -1;		
		$.ajax({
			type : "GET",  
			url : api.SALES_LIST_BY_SOID +"?id="+soId+"&&className="+className,
			success : function(response) {
				
				console.log("response:",response);
				$("#invoiceTable tbody").empty();
				$(".total").val("");
				$(".gst").val("");
				$(".gst1").val("");
				$(".grandTotal").val("");
				$.each(response,function(index,value){
					var rowNum=index+1;
					var amountPrice=((value.quantity)*(value.servicePrice));
					//var soSupplyPrice=commaSeparateNumber(value.unitPrice);
					var soSupplyPrice=0.0;
					var soServicePrice=commaSeparateNumber(value.servicePrice);
					var soAmount=commaSeparateNumber(amountPrice);
					if(value.salesOrder.party.id == value.salesOrder.billingAddress){
						var stateName=value.salesOrder.party.party_city.state.name;
						$("#state").val(stateName);
						
					}else{
						getPartyAddressByID(value.salesOrder.billingAddress)
					}
					
				
					
					var soItems = "<tr><td width='5%'>"+value.slNo+"</td><td style='display:none'>" + value.id + "</td><td width='37%'>" + value.description + "</td>" +
							"<td width='12%'>" + value.hsnCode + "</td><td width='5%'><input type='text' id='soQty"+index+"' name='items["+index+"].quantity' class='form-control PositionofTextbox soQty' value='"+value.quantity+"'/></td><td width='9%'>" + value.item_units.name + "</td>" +
							"<td width='10%'>" + soSupplyPrice+ "</td><td width='12%'>" + soServicePrice + "</td><td class='soAmount' id='soAmount"+index+"' width='10%'>" + soAmount + "</td><td><input type='hidden' name='items["+index+"].amount' id='hiddenSoAmount"+index+"'  /></td>" +
									"<td style='display:none'><input type='hidden' id='salesQuantity" + index + "' value='"+value.quantity+"'></td></tr>";
					$("#invoiceTable tbody").append(soItems);
					if(value.item_units.name=="Heading"){
						$("#soQty"+index).attr("readonly",true);
					}else{
						$("#soQty"+index).attr("readonly",false)
					}
				})	
				
				//getStateNameBySoId(soId);
				soTotalCalculation();
				$('input').removeClass("has-error");
			},  
			complete:function(resp){
				if(resp.status==500){
					$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
				}
			},
			error : function(e) {
				console.log(e);
			}  	 
		}); 
	
}


//get all the dc items
function getAllDcItems(dcId){
	  var row=$('table > tbody  > tr').length -1;		
		$.ajax({
			type : "GET",  
			url : api.GET_DC_ITEMLIST_BYDCID +"?id="+dcId,
			success : function(response) {
				console.log("response:",response);
				
				$("#invoiceTable tbody").empty();
				$(".total").val("");
				$(".gst").val("");
				$(".gst1").val("");
				$(".grandTotal").val("");
				$.each(response,function(index,value){
					
					var rowNum=index+1;
					//var amountPrice=((value.todaysQty)*(value.supplyPrice)+(value.servicePrice)*(value.todaysQty))
					var amountPrice=((value.todaysQty)*(value.supplyPrice));
					var dcSupplyPrice=commaSeparateNumber(value.supplyPrice);
					//var dcServicePrice=commaSeparateNumber(value.servicePrice);
					var dcServicePrice=0.0;
					
					var dcAmount=commaSeparateNumber(amountPrice);
					
					
					var dcItems = "<tr><td width='5%'>"+value.slNo+"</td><td style='display:none'>" + value.dcItemId + "</td><td width='37%'>" + value.description + "</td>" +
							"<td width='12%'>" + value.hsnCode + "</td><td width='5%'><input name='items["+index+"].quantity' id='dcQty"+index+"' type='text' class='form-control PositionofTextbox dcQty' readonly='readonly' value='"+value.todaysQty+"'/></td><td width='9%'>" + value.unit + "</td>" +
							"<td width='10%'>" + dcSupplyPrice + "</td><td width='12%'>" + dcServicePrice + "</td><td class='dcAmount'id='dcAmount"+index+"'  width='10%'>" + dcAmount + "</td><td style='display:none'><input type='hidden' name='items["+index+"].amount' id='hiddenDcAmount"+index+"'  /></td>" +
									"<td style='display:none'><input type='hidden' id='dcQuantity" + index + "' value='"+value.todaysQty+"'></td></tr>";
					$("#invoiceTable tbody").append(dcItems);
					
				})	
				dcTotalCalculation();
				$('input').removeClass("has-error");
			},  
			complete:function(resp){
				if(resp.status==500){
					$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
				}
			},
			error : function(e) {
				console.log(e);
			}  	
		}); 
	
}


//on submit send value
$(document).on('submit', '#invoiceForm', function(e) {
	var rowCount=$('#invoiceTable >tbody  > tr').length;
	if(rowCount==0){
		e.preventDefault();
		$.error("no items found");
	}
	var soId=$("#clientPoDropdown").val();
	var type=$("#typeDropdown").val();
	var dcId=$("#dcDropdown").val();
	//this code is to list all the dc numbers when ALL is selected
	var dcarray=[];
	var dcNo;
	$("#dcDropdown option").each(function()
			{
		dcNo=$(this).val();
		dcarray.push(dcNo);
			});
	var dcObj=dcarray[2];
	if(dcId=="All"){
	for (var i = 2; i < dcarray.length-1; i++) {
		dcObj=dcObj+","+dcarray[i+1]
	}
	
		dcId=dcObj;
	}
	
	$("#soNumber").val(soId);
	$("#dcNumber").val(dcId);
	$("#type").val(type);
	
	var totalAmount=$("#total").val();
	//remove comma
	var totalAmt=totalAmount.replace(/,/g,"");
	$("#total").val(totalAmt);
	
	var gst=$("#gst").val();
	//remove comma
	var gstAmt=gst.replace(/,/g,"");
	$("#gst").val(gstAmt);
	$("#gst1").val(gstAmt);
	
	var grandTotal=$("#grandTotal").val();
	//remove comma
	var grandTotalAmt=grandTotal.replace(/,/g,"");
	$("#grandTotal").val(grandTotalAmt);
	
	
	var rowCount=$('#invoiceTable >tbody  > tr').length;
	for (var i = 0; i < rowCount; i++) {
		
		var soAmountVal=$("#soAmount"+i).text();
		//remove comma
		var soAmount=soAmountVal.replace(/,/g,"");
		
		var dcAmountVal=$("#dcAmount"+i).text();
		//remove comma
		var dcAmount=dcAmountVal.replace(/,/g,"");
		
		if(type=="Supply"){
			/*if(dcId=="All"){
				
				$("input[name='items[" + i + "].amount']").val(soAmount);
			}else{*/
				$("input[name='items[" + i + "].amount']").val(dcAmount);
			//}
		}else if(type=="Service"){
			$("input[name='items[" + i + "].amount']").val(soAmount);
		}

	}
	
	
	if( soId== ""){
		e.preventDefault(e);	
		$.error("Plaese select the client Po No. before submitting ");
		$("#clientPoDropDown").addClass('border-color');
	}
	
	if( type== ""){
		e.preventDefault(e);	
		$.error("Plaese select the type before submitting ");
		$("#typeDropDown").addClass('border-color');
	}
	if(type=="Supply"){
		if( dcId== ""){
			e.preventDefault(e);	
			$.error("Plaese select Dc Number before submitting ");
			$("#dcDropDown").addClass('border-color');
		}
	}
	
	
	/***Quantity Validation for soQty**/
	$('#invoiceTable > tbody  > tr > td > input.soQty').each(function (index, input) {
		var soQtyInput = $("#soQty" + index).val();
		let salesQuantity = parseFloat($("#salesQuantity" + index).val());
		let newQuantity = parseFloat($("#soQty" + index).val());
		let row = index + 1
		salesQuantity=Math.round(salesQuantity * 100) / 100;
		
		if (soQtyInput == "" || soQtyInput == undefined) {
			e.preventDefault(e);
			$("#soQty" + index).addClass('border-color')
		}

		if (newQuantity > salesQuantity) {
			e.preventDefault(e);
			$.error("Entered Quantity is greater than " + salesQuantity + " at row " + row);
			$(this).addClass('border-color');
		}
       /* var qtyValue = this.value;
		var digits= new RegExp(/^[0-9]+$/);
		var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
		if (qtyValue.match(digits)||qtyValue=="") {

		}
		else{
			 $("#soQty"+index).addClass('border-color');
				e.preventDefault(e);
        	$.error("only digits are allowed for qty at row "+ row);
		}*/
		$('input').change(function(){
			$("#soQty"+index).removeClass('border-color');
		})	
	});
		
	/***Quantity Validation for dcQty**/
	$('#invoiceTable > tbody  > tr > td > input.dcQty').each(function (index, input) {
		var dcQtyInput = $("#dcQty" + index).val();
		let dcQuantity = parseFloat($("#dcQuantity" + index).val());
		let newQuantity = parseFloat($("#dcQty" + index).val());
		let row = index + 1;
		dcQuantity=Math.round(dcQuantity * 100) / 100;
		
		if (dcQtyInput == "" || dcQtyInput == undefined) {
			e.preventDefault(e);
			$("#dcQty" + index).addClass('border-color')
		}

		if (newQuantity > dcQuantity) {
			e.preventDefault(e);
			$.error("Entered Quantity is greater than " + dcQuantity + " at row " + row);
			$(this).addClass('border-color');
		}
		
        /*var qtyValue = this.value;
		var digits= new RegExp(/^[0-9]+$/);
		var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
		if (qtyValue.match(digits)||qtyValue=="") {

		}
		else{
			 $("#dcQty"+index).addClass('border-color');
				e.preventDefault(e);
        	$.error("only digits are allowed for qty at row "+ row);
		}*/
		$('input').change(function(){
			$("#dcQty"+index).removeClass('border-color');
		})	
	});
	
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#saveInvoice").attr('disabled', false);
		});
	 $("#saveInvoice", this)
     .attr('disabled', 'disabled');
		
});

//on double click of list display invoice items by invoice id in view page
function getinvoiceObjectByInvoiceId(invoiceId){
	$.ajax({
	    Type:'GET',
	    url : api.GET_INVOICEITEMLIST_BY_INVOICEID+"?invoiceId="+invoiceId,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$.each(response,function( key, value ){
	    		
	    		$("input[name='items[" + key + "].quantity']").val(value.quantity);
	    		$("#dcAmount"+key).text(commaSeparateNumber(value.amount));
	    		$("#soAmount"+key).text(commaSeparateNumber(value.amount));
	    		$(".total").val(commaSeparateNumber(invoiceObj.total));
	    		$("#taxDropDown").val(invoiceObj.gstRate);
	    		$("#invoiceNum").val(invoiceObj.invoiceId);
	    		$("#taxDropDown")
	    		var state = $("#state").val();
	    		   if(state=="Karnataka"){
	    			   var gstValue = $("#taxDropDown").val();
	    		    	var gstRate = gstValue/2;
	    		     gst=gstRate*invoiceObj.total;
	    		     gst = gst/100;
	    		     gst1= gst;
	    		     $("#gstColumn").html("CGST @ "+gstRate+"%");
	    		     $("#gstColumn1").html("SGST @ "+gstRate+"%");
	    			   
							$("#gstPercentage").text(gstValue+"%");
							$("#gstPercentage1").text(gstValue+"%");
							$("#taxDropDown").trigger('change');
							
	    			    }else{
	    			    	  var gstValue = $("#taxDropDown").val();
	    			 	     gst=gstValue*invoiceObj.total;
	    			 	     gst = gst/100;
	    			   //  gst=0.18*invoiceObj.total;
	    			     $('table#invoiceTable tr#sgst').hide();
							$("#gstColumn").html("IGST @ 18%");
							$("#gstPercentage").text("18%");
							$("#taxDropDown").trigger('change');
	    			    }
//	    		$(".gst").val(commaSeparateNumber(invoiceObj.gst));
//	    		$(".gst1").val(commaSeparateNumber(invoiceObj.gst));
	    		$(".grandTotal").val(commaSeparateNumber(invoiceObj.grandTotal));
	    		$("input[name='items[" + key + "].quantity']").attr("disabled",true);
	    		$("#taxDropDown").attr("disabled",true);
			
			
	    	});
	    	
	    },  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	
	  }); 
}

function commaSeparateNumber(val) {
	var x = val;
	//x = x.replace(",","");
	x = x.toString();
	x = x.replace(/,/g, "");
	var afterPoint = '';
	if (x.indexOf('.') > 0)
		afterPoint = x.substring(x.indexOf('.'), x.length);
	x = Math.floor(x);
	x = x.toString();
	var lastThree = x.substring(x.length - 3);
	var otherNumbers = x.substring(0, x.length - 3);
	if (otherNumbers != '')
		lastThree = ',' + lastThree;
	var res = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + lastThree + afterPoint;
	return res;


}

function getPartyAddressByID(partyAddressId){
	$.ajax({
		type: "GET",
		url: api.GET_PARY_ADDRESS_BYADDRESSID + "?id=" + partyAddressId,
		success: function (response) {
			
				var stateName=response.partyaddr_city.state.name;
				$("#state").val(stateName);
				if(stateName=="Karnataka"){
				//	$('table#invoiceTable').append('<tr id="sgst" ><td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td><td colspan="2"><input type="text" id="gst1" name="gst1"class="form-control PositionofTextbox gst alignright" value="" />	</td></tr>');
					$('table#invoiceTable tr#sgst').removeAttr("hidden","hidden");
					$("#gstColumn").html("CGST @ 9%");
					$("#gstColumn1").html("SGST @ 9%");
					//$('table#invoiceTable').append('<tr id="sgst"><td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td><td colspan="2"><input type="text" id="gst1" name="gst1"class="form-control PositionofTextbox gst alignright" value="" />	</td></tr>');
					$("#gstPercentage").text("9%");
					$("#gstPercentage1").text("9%");
				}else{
					$('table#invoiceTable tr#sgst').attr("hidden","hidden");
					var gstValue = $("#taxDropDown").val();
					//$('table#invoiceTable').append('<tr id="sgst hidden" ><td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td><td colspan="2"><input type="text" id="gst1" name="gst1"class="form-control PositionofTextbox gst alignright" value="" />	</td></tr>');
					$("#gstColumn").html("IGST @"+gstValue+"%");
					$("#gstPercentage").text(gstValue+"%");
					
				}
		}
		});
	
	
}

$(document).on('change', '#taxDropDown',function () {
	var stateName= $("#state").val();
	if(stateName=="Karnataka"){
		var gstValue = $("#taxDropDown").val();
		var gstRate =  gstValue/2;
		$('table#invoiceTable tr#sgst').removeAttr("hidden","hidden");
		$("#gstColumn").html("CGST @ "+gstRate+"%");
		$("#gstColumn1").html("SGST @"+gstRate+"%");
		//$('table#invoiceTable').append('<tr id="sgst"><td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td><td colspan="2"><input type="text" id="gst1" name="gst1"class="form-control PositionofTextbox gst alignright" value="" />	</td></tr>');
		$("#gstPercentage").text(gstValue+"%");
		$("#gstPercentage1").text(gstValue+"%");
	}else{
		var gstValue = $("#taxDropDown").val();
		//$('table#invoiceTable').append('<tr id="sgst hidden" ><td colspan="6" width="81%" style="text-align: right;" id="gstColumn1">SGST @<span id="gstPercentage1"></span></td><td colspan="2"><input type="text" id="gst1" name="gst1"class="form-control PositionofTextbox gst alignright" value="" />	</td></tr>');
		$("#gstColumn").html("IGST @"+gstValue+"%");
		$("#gstPercentage").text(gstValue+"%");
	}
	
	dcTotalCalculation();
	soTotalCalculation();
	   
});

