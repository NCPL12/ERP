/**
 * 
 */
var rowCountofAddeditems;
var prevReceievedQty =0;
$(document).ready(function () {
	//populate purchase order dropdown
	$("#purchaseOrderDropDown option:not(:first)").remove();
	$.each(poList, function (index, value) {
		$("#purchaseOrderDropDown").append('<option value=' + value.poNumber + '>' + value.poNumber + '</option>');

	});
	 $( "#invoiceDate" ).datepicker({ dateFormat: 'dd-mm-yy' });
	getPurchaseItemsByPurchaseItemId();

	/***Amount Calculation**/
	$(document).on('keyup mouseup', '.receivedQty,.unitPrice', function () {
		var index = $(this).closest("tr").index();
		var parent = $(this).closest('tr');
		
		//For displaying remaining qty
		var receivedQty = $("#receivedQty" + index).val();
		var totQty = $("#totalQty" + index).text();
		var prev = $("#remainingQty"+index).val();
		var remainingQty =  $("#RemainingQty" + index).text(Math.round((totQty-receivedQty-prev) * 100) / 100);
		var total = parseFloat(totQty);
		var remainingQtyValue = total -receivedQty-prev;
		//$("#remainingQty"+index).val(remainingQtyValue);
		var unitPrice = $("#unitPrice" + index).text();
		$("#unitPrice" + index).text(commaSeparateNumber(unitPrice));
		var u = unitPrice.includes(",");
		if (u == true) {
			unitPrice = unitPrice.replace(/,/g, "");
		}
		parent.find('.amount').text(parseFloat(parent.find('.receivedQty').val()) * parseFloat(unitPrice))
		var amountPrice = parseFloat($("#amount" + index).text()).toFixed(2);
		$("#amount" + index).text(commaSeparateNumber(amountPrice));
		if (parent.find('.amount').text() == "NaN") {
			parent.find('.amount').text("");
		}
	});
	$(document).on("click", ".add", function () {
		addGrnRow();
	})
	/**Remove row from table**/
	$("#grnTable").on("click", ".deleteButton", function () {
		$(this).closest("tr").remove();
		/***Setting name and id to remaining rows**/
		$('tbody').find('tr').each(function (index) {
			let prev = index - 1;
			let firstTdElement = $(this).find('td')[0];
			$(firstTdElement).text(index + 1);
			let secondTdElement = $(this).find('td')[1];
			$(secondTdElement).find('select').attr('name', 'items[' + index + '].description');
			$(secondTdElement).find('select').attr('id', 'descriptionDropdown' + index);
			let thirdTdElement = $(this).find('td')[2];
			$(thirdTdElement).find('input').attr('id', 'modelNo' + index);
			let forthTdElement = $(this).find('td')[3];
			$(forthTdElement).find('input').attr('id', 'unit' + index);
			let fifthTdElement = $(this).find('td')[4];
			$(fifthTdElement).find('input').attr('id', 'totalQty' + index);
			let sixthTdElement = $(this).find('td')[5];
			$(sixthTdElement).find('input').attr('name', 'items[' + index + '].receivedQuantity');
			$(sixthTdElement).find('input').attr('id', 'receivedQty' + index);
			let seventhTdElement = $(this).find('td')[6];
			$(seventhTdElement).find('input').attr('name', 'items[' + index + '].unitPrice');
			$(seventhTdElement).find('input').attr('id', 'unitPrice' + index);
			let eighthTdElement = $(this).find('td')[7];
			$(eighthTdElement).find('input').attr('name', 'items[' + index + '].amount');
			$(eighthTdElement).find('input').attr('id', 'amount' + index);
		});
	});

	$('#grnTable').on("change", '.receivedQty', function () {
		var index = $(this).closest("tr").index();
		var totalQty = $(this).closest("tr").find("td").eq(4).text();
		var receivedQty = $(this).closest("tr").find("td").eq(5).find('input').val();
		totalQty = parseFloat(totalQty);
		receivedQty = parseFloat(receivedQty);
		 var purchaseItemId=$(this).closest("tr").find("td").eq(10).find('input').val();
		 $.ajax({
				type : "GET",  
				url: api.GET_PURCHASE_ITEMS_BYID + "?id=" + purchaseItemId,
				success : function(response) {
					if(response.receivedQty!=0){
						if(receivedQty>response.receivedQty){
							$.error("Entered ReceivedQty Quantity is greater than " +Math.round(response.receivedQty * 100) / 100);
							$("#receivedQty"+index).addClass('border-color');
						}else{
							$("#receivedQty"+index).removeClass('border-color');
						}
					}else{
						if (totalQty != "") {
							if (receivedQty > totalQty) {
								$.error("Entered Received Quantity is greater than Total Quantity");
								$("#receivedQty" + index).addClass('border-color');
							} else {
								$("#receivedQty" + index).removeClass('border-color');
							}
						}
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

	if (grnObj != "") {
		//displayAllPurchaseItemByPoNumber(grnObj.poNumber);
		getGrnObjById(grnObj.grnId);
		
		$(document).on("click", ".receivedQty", function (e) {
			var model=$(this).closest("tr").find(".modelNo").val();
			var poItemId=$(this).closest("tr").find(".poItemId").val();
			getStockQtyForThisModel(model,poItemId);
		});
	}

});

/*//validating quantity on only allowing numeric values
$(document).on("focusout", ".receivedQty", function (e) {
	var qtyValue = this.value;
	var digits = new RegExp(/^[0-9]+$/);
	var floatNum = new RegExp(/^[+-]?\d+(\.\d+)?$/);
	if (qtyValue.match(digits) || qtyValue.match(floatNum) || qtyValue == "") {

	}
	else {
		$.error("only digits are allowed");
	}
});*/

//on change of purchase order dropdown replace it by input and made non editable
$(document).on("change", "#purchaseOrderDropDown", function () {
	showLoader()
	if (rowCountofAddeditems > 0) {
		bootbox.confirm({
			message: "are you sure to change the PO No?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
			
			result ? deleteAllRows() : "";
			}
		});


	}
	var row = $('table > tbody  > tr').length - 1;
	var poNumber = $("#purchaseOrderDropDown option:selected").val();
	var updatedDate;
	$.each(poList, function (index, value) {
		if (poNumber == value.poNumber) {
			updatedDate = value.updated;
		}
	});
	$("#poDate,#poDateVal").val(updatedDate);

	//$("#purchaseOrderDropDown").replaceWith('<input type="text" class="form-control PositionofTextbox" id="purchaseOrder" name="purchaseOrder">');
	// $('#purchaseOrder').next(".select2-container").hide();
	$("#purchaseOrder").val(poNumber);
	//$("#purchaseOrder").attr("readonly","readonly");
	$("#poNumber").val(poNumber);
	getPurchaseItemListByPoNumber();
	displayAllPurchaseItemByPoNumber(poNumber);
	
});



function displayAllPurchaseItemByPoNumber(poNumber){
	var className="grn";
	$.ajax({
	    type : "GET",  
	    url : api.PURCHASE_LIST_BY_POID +"?id="+poNumber+ "&&className="+className,
	    success : function(response) {
		console.log("response:",response);
		$("#grnTable tbody").empty();
		//$("#descriptionDropdown"+row).children("option").filter(":not(:first)").remove();
		$.each(response,function(index,value){
			var rowNum=index+1;
			var amountPrice=(value.receivedQty)*(value.unitPrice);
			var unitPrice=commaSeparateNumber(value.unitPrice);
			var grnAmount=commaSeparateNumber(amountPrice);
			
			var rcvedQty=Math.round(value.receivedQty * 100) / 100;
			var grnItems = "<tr><td width='5%'>"+rowNum+"</td>" +
			"<td width='25%'>" + value.poDescription + "</td>" +
			"<td width='10%' id='modelNo"+index+"'>" +value.modelNo + "</td>" +
			"<td width='10%' id='units"+index+"'>" +value.unitName + "</td>" +
			"<td width='7%' id='totalQty"+index+"'>" + value.quantity + "</td>" +
			"<td width='7%'><input type='text' id='receivedQty"+index+"' name='items["+index+"].receivedQuantity' class='form-control PositionofTextbox receivedQty' value=0></td>" +
			"<td width='7%' class='RemainingQty' id='RemainingQty"+index+"'></td>" +
			"<td width='12%' id='unitPrice"+index+"' class='unitPrice'>" + unitPrice + "</td>" +
			"<td width='14%' id='amount"+index+"' class='amount'>" +grnAmount+ "</td>" +
			"<td style='display:none'><input type='hidden' id='remainingQty"+index+"' value='"+rcvedQty+"'/></td>" +
			"<td style='display:none'><input class='descriptionDropdown' id='descriptionDropdown"+index+"' name='items["+index+"].description' value='" + value.purchase_item_id + "' /></td>" +
			"<td style='display:none'><input class='grnAmount' type='hidden' id='grnAmount"+index+"' name='items["+index+"].amount' /></td>" +
			"<td style='display:none'><input class='grnUnitPrice'  type='hidden' id='grnUnitPrice"+index+"' name='items["+index+"].unitPrice' /></td></tr>";
			$("#grnTable tbody").append(grnItems);
			if (grnObj == "") {
			if(value.receivedQty == 0){
				$("#RemainingQty"+index).text(Math.round((value.quantity - value.receivedQty) * 100) / 100);
			    
				}else{
					$("#RemainingQty"+index).text(Math.round((value.quantity - value.receivedQty) * 100) / 100);
					$("#remainingQty"+index).val(Math.round((value.quantity - value.receivedQty) * 100) / 100)
					//prevReceievedQty = value.quantity - value.receivedQty;
				}
			}
			$(".receivedQty").trigger("keyup");
		})	
		hideLoader();
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


//var rowCountofAddeditems;
//add row on click of add button
function addGrnRow() {
	var poDropDownVal = $('#purchaseOrderDropDown option:selected').val();
	if (poDropDownVal == "" && grnObj == "") {
		$.error("Plaese select the Purchase Order before adding row");
		$("#purchaseOrderDropDown").addClass('border-color');
		return false;
	}
	$("#purchaseOrderDropDown").removeClass('border-color');



	var newRow = $("<tr>");
	var row = $('table > tbody  > tr').length;
	var rowCount = row + 1
	rowCountofAddeditems = rowCount;
	var arraycount = rowCount - 1;
	var previousRow = arraycount - 1


	/**description  validation***/
	var descriptionId = $("#descriptionDropdown" + previousRow).val();
	if (descriptionId == "" && grnObj == "") {
		$.error("Plaese select description before adding row ");
		$("#descriptionDropdown" + previousRow).addClass('border-color');
		return false;
	}
	$("#descriptionDropdown" + previousRow).removeClass('border-color');



	var columns = "";
	var columns = "";
	columns += '<td width="5%" class="styleOfSlNo">' + rowCount + '</td>';

	columns += '<td width="25%"> <select class="form-control PositionofTextbox descriptionDropdown" name="items[' + arraycount + '].description" id="descriptionDropdown' + arraycount + '"  style="padding: 0;"><option value="">Select Description:</option></select></td>'

	columns += '<td width="10%"> <input type="text" class="form-control  PositionofTextbox  modelNo" readonly="readonly" id="modelNo' + arraycount + '"/> </td>';
	columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox units" readonly="readonly"  id="units' + arraycount + '" /></td>';
	columns += '<td width="7%"><input type="text" class="form-control PositionofTextbox totalQty" readonly="readonly"  id="totalQty' + arraycount + '"/> </td>';
	columns += '<td width="7%"><input type="text" class="form-control PositionofTextbox receivedQty" name="items[' + arraycount + '].receivedQuantity" id="receivedQty' + arraycount + '" /></td>';
	columns += '<td width="7%"><input type="text" class="form-control PositionofTextbox RemainingQty" readonly="readonly"  id="RemainingQty' + arraycount + '" /></td>';
	columns += '<td width="12%"><input type="text" class="form-control PositionofTextbox unitPrice alignright" name="items[' + arraycount + '].unitPrice" id="unitPrice' + arraycount + '"/></td>';
	columns += '<td width="14%"><input type="text" class="form-control PositionofTextbox amount alignright" readonly="readonly" name="items[' + arraycount + '].amount" id="amount' + arraycount + '" /></td>';
	columns += '<td style="display:none"><input type="hidden" id="remainingQty'+ arraycount +'" /></td>';
	columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>';
	columns += '<td style="display:none"><input type="hidden" class="poItemId" id="poItemId'+ arraycount +'" /></td>';
	newRow.append(columns);
	$("#grnTable").append(newRow);
	$("#descriptionDropdown" + arraycount).select2({ dropdownAutoWidth: true });
	getPurchaseItemListByPoNumber();
	getPurchaseItemsByPurchaseItemId();
	if (grnObj == "") {
		$("#descriptionDropdown" + previousRow).attr("disabled", true);
	}

	//reset all the items in row on change of description
	$(document).on("change", "#descriptionDropdown" + arraycount, function () {
		var presentDesc = $("#descriptionDropdown" + arraycount).val();
		var i;
		for (i = 0; i < arraycount; i++) {
			var previousDesc = $("#descriptionDropdown" + i).val();

			if (previousDesc == presentDesc) {
				$.error("Please remove duplicate items");
			}
		}
		/**reset input values*/
		$("#modelNo" + arraycount).val("");
		$("#totalQty" + arraycount).val("");
		$("#units" + arraycount).val("");
		$("#unitPrice" + arraycount).val("");
		$("#amount" + arraycount).val("");
		$("#receivedQty" + arraycount).val("");

	});

}

//get items by purchase itemId
function getPurchaseItemListByPoNumber(){
var row=$('table > tbody  > tr').length -1;	
var poNumber=  $('#poNumber').val();
//Sending class name to differentiate between api called in grn and po..
var className="grn";
$.ajax({
    type : "GET",  
    url : api.PURCHASE_LIST_BY_POID +"?id="+poNumber+ "&&className="+className,
    success : function(response) {
	console.log("response:",response);
	$("#descriptionDropdown"+row).children("option").filter(":not(:first)").remove();
	$.each(response,function(index,value){
		var poDescription = value.poDescription;
		poDescription = poDescription.split(' ');
		if (poDescription.length > 100) {
			poDescription.splice(100);
		}
		poDescription = poDescription.join(' ');
		var length = $.trim(poDescription).length;
		if (length > 50) {
			poDescription = $.trim(poDescription).substring(0, 50) + "....";
		}
		$("#descriptionDropdown"+row).append('<option value='+value.purchase_item_id+'>'+poDescription+'</option>');
		
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

//reset 1st row on change of description
$(document).on("change", "#descriptionDropdown0", function () {
	/**reset input values*/
	$("#modelNo0").val("");
	$("#totalQty0").val("");
	$("#units0").val("");
	$("#unitPrice0").val("");
	$("#amount0").val("");
	$("#receivedQty0").val("");

});

/**on change of description item**/
function getPurchaseItemsByPurchaseItemId() {
	$(document).on("click", '#grnTable>tbody>tr>td', function () {
		let index = $(this).closest('tr').index()
		$(document).on("change", '#descriptionDropdown' + index, function () {
			var purchaseItemId = $(this).val();

			$.ajax({
				type: "GET",
				url: api.GET_PURCHASE_ITEMS_BYID + "?id=" + purchaseItemId,
				success: function (response) {
					var modelNum = null;
					$.each(itemList, function (index, value) {
						if (response.modelNo == value.id) {
							modelNum = value.model;
						}
					});
					$("#modelNo" + index).val(modelNum);
					$("#totalQty" + index).val(response.quantity);
					$("#units" + index).val(response.unitName);
					$("#unitPrice" + index).val(commaSeparateNumber(response.unitPrice));
					$("#receivedQty" + index).val(Math.round(response.receivedQty * 100) / 100);
					if(response.receivedQty == 0){
					$("#RemainingQty"+index).val(Math.round((value.quantity - value.receivedQty) * 100) / 100);
				
					}else{
						$("#RemainingQty"+index).text(Math.round((value.quantity - value.receivedQty) * 100) / 100);
						$("#remainingQty"+index).val(Math.round((value.quantity - value.receivedQty) * 100) / 100)
					}
				
					var amount=(response.receivedQty)*(response.unitPrice);
					$("#amount" + index).val(commaSeparateNumber(amount));
					


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
	});
}

/*$(document).on('click', '#grnTable>tbody>tr', function () {
	
	var index=$(this).closest("tr").index();
	$(document).on("change",'#receivedQty'+index,function(){
	$("#receivedQty"+index).removeClass('border-color');
});
});*/

//on submit enable description dropdown
$(document).on('submit', '#grnForm', function (e) {
	var rowCount=$('#grnTable >tbody  > tr').length;
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
	var poDropDownVal = $('#purchaseOrderDropDown option:selected').val();
	$('#poNumber').val(poDropDownVal);
	if (poDropDownVal == "") {
		e.preventDefault();
		$.error("Plaese select the Purchase Order before submitting ");
		$("#purchaseOrderDropDown").addClass('border-color');
	}
	var invoiceDate=$("#invoiceDate").val();
	var invoiceDateFormat=invoiceDate.split("-"); //split date by "/"
	invoiceDate= invoiceDateFormat[1]+"/"+invoiceDateFormat[0]+"/"+invoiceDateFormat[2]; //change the format to mm/dd/yyyy to work in next step
	invoiceDate=new Date(invoiceDate);
	invoiceDate=invoiceDate.toDateString();
	var invDateVal=$("#invoiceDate").val();
	$('#invDate').val(invoiceDate);
	var invoiceNumber=$("#invoiceNo").val();
	$('#invNo').val(invoiceNumber);
	
	if (invDateVal == "") {
		e.preventDefault();
		$.error("Plaese select Invoice Date before submitting ");
		$("#invoiceDate").addClass('border-color');
	}
	
	if (invoiceNumber == "") {
		e.preventDefault();
		$.error("Plaese Enter Invoice Number before submitting ");
		$("#invoiceNo").addClass('border-color');
	}
	
	if(invoiceNumber!=""){
		$.each(grnList,function(index,value){
			if(invoiceNumber.trim() == value.invoiceNo){
				$.error("Invoice Number already exist");
				$("#invoiceNo,#invNo").addClass('border-color');
				e.preventDefault(e);
				return false;
			
		}else{
			$("#invoiceNo,#invNo").removeClass('border-color');
		}
		});
		}else{
		}
	
	/***Description Validation***/
	$('#grnTable > tbody  > tr > td > .descriptionDropdown').each(function (index, input) {
		if ($(this).val().trim() === '') {
			var row = index + 1;

			$.error("Please select Description dropdown before submitting at row " + row);
			$("#descriptionDropdown" + index).addClass('border-color');
			e.preventDefault();
		}
		$("#descriptionDropdown" + index).change(function () {
			$("#unitPrice" + index).removeClass('border-color');
		});
	});

	//receivedQty
	var count = $("#grnTable").find("tr:gt(0)").length;
	$('#grnTable > tbody  > tr > td > input.receivedQty').each(function (index, input) {
		var receivedQuantity = $("#receivedQty" + index).val();
		var row = index + 1;
		if (receivedQuantity == "" || receivedQuantity == undefined) {
			
			$.error("Please enter the Received quantity at row " + row);
			$("#receivedQty" + index).addClass('border-color');
			e.preventDefault(e);
		}
		/*var qtyValue = this.value;
		var digits= new RegExp(/^[0-9]+$/);
		var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
		if (qtyValue.match(digits)||qtyValue=="") {

		}
		else{
			 $("#receivedQty"+index).addClass('border-color');
				e.preventDefault(e);
        	$.error("only digits are allowed for qty at row "+ row);
		}*/
		$("#receivedQty" + index).change(function () {
			$("#receivedQty" + index).removeClass('border-color');
		});
	});

	$('#grnTable > tbody  > tr > td > .unitPrice').each(function (index, input) {
		var unitPrice = $("#unitPrice" + index).text();
		unitPrice = unitPrice.replace(/,/g, "");
		$("#unitPrice" + index).text(unitPrice);
		
		var amount = $("#amount" + index).text();
		amount = amount.replace(/,/g, "");
		$("#amount" + index).text(amount);
		if (unitPrice == "" || unitPrice == undefined) {
			var row = index + 1;
			$.error("Please enter the Unit Price at row " + row);
			$("#unitPrice" + index).addClass('border-color');
			e.preventDefault(e);
			$("#descriptionDropdown" + index).attr("disabled", true);
		}
		$("#unitPrice" + index).change(function () {
			$("#unitPrice" + index).removeClass('border-color');
		});
	});

	$('input,select').change(function(){
		$("#receivedQty").removeClass('border-color');
	});

	$('#grnTable > tbody  > tr > td > .descriptionDropdown').each(function (index, input) {
		$("#descriptionDropdown" + index).attr("disabled", false);
	});

	$('#grnTable > tbody  > tr > td > input.receivedQty').each(function (index, input) {
		var receivedQty = parseFloat($("#receivedQty" + index).val());
		var totalQty = parseFloat($("#totalQty" + index).text());
		var remainingQty=parseFloat($("#remainingQty"+index).val());
		var presentExactRemQty = totalQty - remainingQty;
		if(receivedQty > presentExactRemQty){
			$.error("Entered Received Quantity is greater than Remaining Quantity");
			$("#receivedQty" + index).addClass('border-color');
			e.preventDefault(e);
		}
		var row = index + 1
		/*if(receivedQty==0){
			$("#descriptionDropdown" + index).attr("disabled", true);
			$.error("ReceivedQty Quantity should be greater than 0 at row " +row);
			$("#receivedQty"+index).addClass('border-color');
			e.preventDefault(e);
		}else{
			$("#receivedQty"+index).removeClass('border-color');
		
		}*/
		if(remainingQty!=0){
//			if(receivedQty>remainingQty){
//				$("#descriptionDropdown" + index).attr("disabled", true);
//				$.error("Entered ReceivedQty Quantity is greater than " +remainingQty+ " at row " +row);
//				$("#receivedQty"+index).addClass('border-color');
//				e.preventDefault(e);
//			}else{
//				$("#receivedQty"+index).removeClass('border-color');
//			}
		}else{
			if (receivedQty > totalQty) {
				e.preventDefault();
				$("#descriptionDropdown" + index).attr("disabled", true);
				$.error("Entered Received Quantity is greater than Total Quantity at row " + row);
				$("#receivedQty" + index).addClass('border-color');
				e.preventDefault(e);
			}
		}
	});
	
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#saveGrn").attr('disabled', false);
		});
	 $("#saveGrn", this)
     .attr('disabled', 'disabled');
});


function deleteAllRows() {

	$("#grnTable").find("tr:gt(0)").remove();

	rowCountofAddeditems = 0;



}
$(document).on('keyup mouseup click input change','.receivedQty,#grnTable',function () {
	 
	var sum=0;
var amountArr=[];
$('.amount').each(function(index,value) {
	/*amountArr.push(amountPrice);
	var amount=amountPrice;*/
	var c = $("#amount"+index).text();
	c = c.replace(/,/g,"");
	//amountArr.push(parseFloat($(this).val()));
	
	var amount=parseFloat(c);
    sum+=amount;
    var sumWithComma = commaSeparateNumber(sum);
    $(".total").attr("value", sumWithComma);
    var rcvedQty=$("#receivedQty"+index).val();
    
    if(rcvedQty==""){
    	$("#total").val("");
    }else{
    	 $("#total").val(sumWithComma);
    }
   
    
    });

});

/**get grn item list by grn id and display on double click of grn list **/
function getGrnObjById(grnId) {
	showLoader();
	 $("#totalTr").css("display","none");
	var invDate=grnObj.invoiceDate;
	$("#invoiceDate").replaceWith('<input type="text" class="form-control PositionofTextbox" id="invDateVal" name="invoiceDate">');
	if(invDate==null){
		$("#invDateVal").val("");
	}else{
		invDate=invDate.split(" ");
		invDate=invDate[0];
		$("#invDateVal,#invoiceDate").val(invDate);
	}
	$('#invDateVal,#invoiceDate').attr("readonly","readonly");
	var invNo=grnObj.invoiceNo;
	$("#invoiceNo").val(invNo);
	$("#invoiceNo").attr("readonly","readonly");
	$.ajax({
		Type: 'GET',
		url: api.GET_GRN_ITEMLIST_BYGRNID + "?grnId=" + grnId,
		dataType: 'json',
		async: 'false',
		success: function (response) {
			$(".add").hide();
			$("#buttonDiv").hide();
			$.each(response, function (key, value) {
				addGrnRow();

				$("#grnTable >tbody>tr>td").find("input").attr("readOnly", "readOnly");


				$("#receivedQty"+key).val(Math.round(value.receivedQuantity * 100) / 100);
			
				$("#unitPrice"+key).val(commaSeparateNumber(value.unitPrice));
				$("#amount"+key).val(commaSeparateNumber(value.amount));
				$("#poDateVal").val(grnObj.poDate);
				$("#purchaseOrderDropDown").val(grnObj.poNumber);
				$("#purchaseOrderDropDown").select2(grnObj, { id: grnObj.poNumber, a_key: grnObj.poNumber });
				$("#poNumber").val(grnObj.poNumber);
				$("#purchaseOrderDropDown").attr("disabled", true);
				
				
				var grnDescription;
				var modelNo;
				var qty;
				$.each(purchaseItemList, function (k, v) {
					if (value.description == v.purchase_item_id) {
						grnDescription = v.poDescription;
						modelNo = v.modelNo;
						qty = v.quantity;
						poItemId=v.purchase_item_id;
					}

				});
				var modelName;
				$.each(itemList, function (ky, val) {
					if (modelNo == val.id) {
						modelName = val.model;

					}

				})
				$("#poItemId" + key).val(poItemId);
				$("#modelNo" + key).val(modelName);
				$("#totalQty" + key).val(qty);
				var totQty = $("#totalQty"+key).val();
			//	$("#RemainingQty"+key).val(totQty - value.receivedQuantity);
				$("#units" + key).val(value.unitName);
				$("#descriptionDropdown" + key).replaceWith('<input type="text" class="form-control PositionofTextbox description" id="description' + key + '" name="description">');
				$("#description" + key).val(grnDescription);
				$('#description' + key).next(".select2-container").hide();
				$("#description" + key).attr("disabled", true);
				$(".deleteButton ").hide();
			//	$("#RemainingQty"+key).val(totQty -value.receivedQuantity);
                getRemainingQtyByCreatedGrn(grnObj.poNumber,value.description,key,totQty);
			});
			//$("#grnTable >tbody>tr:last").remove();
			$("#addGRN").removeClass("hideIcon");
			hideLoader();
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


function getRemainingQtyByCreatedGrn(poNo,description,rowKey,totQty){
	$.ajax({
		Type: 'GET',
		url: api.GET_GRN_ITEMLIST_BYPONO + "?poNo=" + poNo,
		dataType: 'json',
		async: 'false',
		success: function (response) {
			var receivedQty =0;
			$.each(response, function (key, value) {
				if(description == value.description){
					receivedQty = receivedQty +value.receivedQuantity
				
				}
					
				});
			$("#RemainingQty"+rowKey).val(Math.round((totQty -receivedQty) * 100) / 100);
				
				
		}
		
});
}
function getStockQtyForThisModel(model,poItemId){
		$.ajax({
			type: "GET",
			url: api.STOCK_QTY_DETAILS + "?model=" + model+"&poItemId="+poItemId,
			success: function (response) {
					$("#stockQtyModal").modal("show");
					$("#stockQtyTable tbody").empty();
					var details = "<tr><td width='100%'> " + response.presentQty + "</td></tr>";
					$("#stockQtyTable tbody").append(details);
				
				
			
			}
		});
	
}