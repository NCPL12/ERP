/**
 * 
 */
var rowCountofAddeditems;
$(document).ready(function() {
	
	//display client po number list.
	$.each(salesList,function(index,value){
		var clientPoNumber=value.clientPoNumber;
		if(clientPoNumber!=null){
			if(clientPoNumber!=""){
		
			$("#clientPoDropdown").append('<option value='+value.id+'>'+value.clientPoNumber+'</option>');
			}
		}
	});
	
	$("#partyDropDown option:not(:first)").remove();
	$.each(contractorPartyList, function( key, value ) {
		var contractorPartyList = value.partyName;
		contractorPartyList = contractorPartyList.split(' ');
		if (contractorPartyList.length > 100) {
			contractorPartyList.splice(100);
		}
		contractorPartyList = contractorPartyList.join(' ');
		var length = $.trim(contractorPartyList).length;
		if (length > 25) {
			contractorPartyList = $.trim(contractorPartyList).substring(0, 25) + "....";
		}
		$('#partyDropDown').append('<option value=' + value.id + '>' + contractorPartyList + '</option>');
		});
	
	$(document).on('keyup mouseup input', '.quantity', function () {
		var index = $(this).closest("tr").index();
		var parent = $(this).closest('tr');
		
		//For displaying remaining qty
		var quantity = $("#quantity" + index).val();
		
		var unitPrice = $("#unitPrice" + index).text();
		$("#unitPrice" + index).text(commaSeparateNumber(unitPrice));
		var u = unitPrice.includes(",");
		if (u == true) {
			unitPrice = unitPrice.replace(/,/g, "");
		}
		parent.find('.amount').text(parseFloat(parent.find('.quantity').val()) * parseFloat(unitPrice))
		var amountPrice = parseFloat($("#amount" + index).text()).toFixed(2);
		$("#amount" + index).text(commaSeparateNumber(amountPrice));
		if (parent.find('.amount').text() == "NaN") {
			parent.find('.amount').text("");
		}
	});
	
	$('#workOrderTable').on("change", '.quantity', function () {
		let salesItemId = $(this).closest("tr").find("td").eq(7).find('input').val();
		var newQuantity = $(this).closest("tr").find("td").eq(4).find('input').val();
		let qtyInpt = $(this).closest("tr").find("td").eq(4).find('input');
		newQuantity=parseFloat(newQuantity);
		var index=$(this).closest("tr").index();
		$.ajax({
			type: "GET",
			url: api.GET_SALES_ITEM_BY_ID + "?id=" + salesItemId,
			success: function (response) {
				console.log("edit response:", response);
				qtyInpt.removeClass('border-color');
				var salesQuantity =response.quantity;
				
				salesQuantity=parseFloat(salesQuantity);
				if (newQuantity > salesQuantity) {
					$.error("Entered Quantity is greater than " + Math.round(salesQuantity * 100) / 100);
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
	
	if(workOrderObj!=""){
		
		getAllDescription(workOrderObj.salesOrder.id);
		
		$('#clientPoDropdown').val(workOrderObj.salesOrder.id);
		$('#clientPoDropdown').attr("disabled",true);
		$("#clientPoDropdown").select2(workOrderObj, {id: workOrderObj.salesOrder.id, a_key:workOrderObj.salesOrder.id});
		
		$('#partyDropDown').val(workOrderObj.party.id);
		$('#partyDropDown').attr("disabled",true);
		$("#partyDropDown").select2(workOrderObj, {id: workOrderObj.party.id, a_key:workOrderObj.party.id});
		$("#buttonDiv").hide();
		
	}
	
});

$(document).on("change","#clientPoDropdown",function(){
	if(rowCountofAddeditems>0){
		bootbox.confirm({
			   message: "are you sure to change the Client Po No?",
			   buttons: {
				   cancel: {
					   label: 'Cancel'
				   },
				   confirm: {
					   label: 'Confirm'
				   }
			   },
			   callback: function (result) {
				   result ? deleteAllRows():"";
			   }
		   });		
		
		
	}
	var soId = $(this).val();
	$("#soNumber").val(soId);
	
	getAllDescription(soId);
	$("#descriptionDropdown0").children("option").filter(":not(:first)").remove();
	$("#totalQty0").val("");
	$("#units0").val("");
	$("#modelNo0").val("");
	$("#clientPoNumber").val("");
	
});
$(document).on("change","#partyDropDown",function(){
	
	var partyId = $(this).val();
	$("#partyId").val(partyId);
	
});

function getAllDescription(soId){
	//className String is used to differentiate. since this api is used many places
	var className="wo";
	  var row=$('#workOrderTable > tbody  > tr').length -1;		
		$.ajax({
			type : "GET",  
			url : api.SALES_LIST_BY_SOID +"?id="+soId+"&&className="+className,
			success : function(response) {
				console.log("response:",response);
				/*if(response== null || response ==''){
					$.error("supply price is zero")
				}*/
				$("#workOrderTable tbody").empty();
				$.each(response,function(index,value){
					var amountPrice=(value.quantity)*(value.unitPrice);
					var unitPrice=commaSeparateNumber(value.unitPrice);
					var amount=commaSeparateNumber(amountPrice);
					var woItems = "<tr><td width='5%' class='styleOfSlNo quotationNo' id='quotationNo"+index+"'>"+value.slNo+"</td>" +
					"<td width='40%'>" + value.description + "</td>" +
					"<td width='15%' id='modelNo"+index+"'>" +value.modelNo + "</td>" +
					"<td width='12%' id='units"+index+"'>" +value.unitName + "</td>" +
					"<td width='7%'><input type='text' id='quantity"+index+"' name='items["+index+"].quantity' class='form-control PositionofTextbox quantity' value='"+value.quantity+"'/></td>" +
					"<td width='7%' id='unitPrice"+index+"' class='unitPrice'>" + unitPrice + "</td>" +
					"<td width='7%' id='amount"+index+"' class='amount'>" + amount + "</td>"+
					"<td style='display:none'><input class='salesItemId' id='salesItemId"+index+"' name='items["+index+"].salesItemId' value='" + value.id + "' /></td>"+
					"<td style='display:none'><input class='woUnitPrice' id='woUnitPrice"+index+"' name='items["+index+"].unitPrice' /></td>"+
					"<td style='display:none'><input class='woAmount' id='woAmount"+index+"' name='items["+index+"].amount' /></td>"+
					"<td style='display:none'><input class='salesQuantity' id='salesQuantity"+index+"' value="+value.quantity+"' /></td></tr>";
					$("#workOrderTable tbody").append(woItems);
					if(workOrderObj!=""){
						getWorkOrderById(workOrderObj.id);
						$("#quantity"+index).attr("disabled",true);
					}
					if(value.unitName=="Heading"){
						$("#quantity"+index).attr("readonly",true);
					}else{
						$("#quantity"+index).attr("readonly",false)
					}
				})	
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

$(document).on('submit', '#workOrderForm', function (e) {
	var rowCount=$('#workOrderTable >tbody  > tr').length;
	if(rowCount==0){
		e.preventDefault();
		$.error("no items found");
	}
	for (var i = 0; i < rowCount; i++) {
		
		var unitPriceVal=$("#unitPrice"+i).text();
		//remove comma
		var unitprice=unitPriceVal.replace(/,/g,"");
		
		var amountVal=$("#amount"+i).text();
		//remove comma
		var amount=amountVal.replace(/,/g,"");
		
		$("input[name='items[" + i + "].unitPrice']").val(unitprice);
		$("input[name='items[" + i + "].amount']").val(amount);
	}
	//purchase order dropdown validation
	var clientPoNumber = $('#clientPoDropdown option:selected').val();
	$('#soNumber').val(clientPoNumber);
	if (clientPoNumber == "") {
		e.preventDefault();
		$.error("Plaese select the Client Po Number before submitting ");
		$("#clientPoDropdown").addClass('border-color');
	}
	
	var party = $('#partyDropDown option:selected').val();
	$('#partyId').val(party);
	if (party == "") {
		e.preventDefault();
		$.error("Plaese select Party before submitting ");
		$("#partyDropDown").addClass('border-color');
	}
	

	//receivedQty
	var count = $("#workOrderTable").find("tr:gt(0)").length;
	$('#workOrderTable > tbody  > tr > td > input.quantity').each(function (index, input) {
		var quantity = $("#quantity" + index).val();
		var row = index + 1;
		if (quantity == "" || quantity == undefined) {
			
			$.error("Please enter the quantity at row " + row);
			$("#quantity" + index).addClass('border-color');
			e.preventDefault(e);
		}
		$("#quantity" + index).change(function () {
			$("#quantity" + index).removeClass('border-color');
		});
	});

	$('#workOrderTable > tbody  > tr > td > input.quantity').each(function (index, input) {
		var quantity = parseFloat($("#quantity" + index).val());
		let salesQuantity = parseFloat($("#salesQuantity" + index).val());
		var row = index + 1
		if(quantity > salesQuantity){
			$.error("Entered Quantity is greater than " + Math.round(salesQuantity * 100) / 100 + " at row "+row);
			$("#quantity" + index).addClass('border-color');
			e.preventDefault(e);
		}
	
	});
	
	$('#workOrderTable > tbody  > tr > td > .unitPrice').each(function (index, input) {
		var unitPrice = $("#unitPrice" + index).text();
		unitPrice = unitPrice.replace(/,/g, "");
		$("#unitPrice" + index).text(unitPrice);
		
		var amount = $("#amount" + index).text();
		amount = amount.replace(/,/g, "");
		$("#amount" + index).text(amount);
		
	});
	
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#saveWorkOrderBtn").attr('disabled', false);
		});
	 $("#saveWorkOrderBtn", this)
     .attr('disabled', 'disabled');
});


function getWorkOrderById(woId){
	$.ajax({
	    Type:'GET',
	    url : api.GET_WORK_ORDER_ITEM_LIST_BY_WO_ID+"?woId="+woId,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$.each(response,function( key, value ){
	    		$("input[name='items[" + key + "].quantity']").val(value.quantity);
	    		$("#amount"+key).text(commaSeparateNumber(value.amount));
	    		
	    		
			
			
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


