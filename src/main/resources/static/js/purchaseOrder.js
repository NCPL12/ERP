/**
 *
 */
var table;
//var purchaseOrderObj =null;
var itemName;
var currModel;
var salesOrderItemsCache = {};
var salesItemDetailCache = {};
function debounce(func, wait) {
	var timeout;
	return function () {
		var context = this;
		var args = arguments;
		clearTimeout(timeout);
		timeout = setTimeout(function () {
			func.apply(context, args);
		}, wait);
	};
}
function fetchSalesItemDetails(salesItemId){
	if(!salesItemId){
		return $.Deferred().reject("SALES_ITEM_ID_MISSING").promise();
	}
	if(salesItemDetailCache[salesItemId]){
		return salesItemDetailCache[salesItemId];
	}
	var request = $.ajax({
		type: "GET",
		url: api.SALES_ITEM_BY_SALESITEMID + "?id=" + salesItemId
	}).fail(function(resp){
		if(resp && resp.status==500 && resp.responseJSON){
			$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"]);
		}
		delete salesItemDetailCache[salesItemId];
	});
	salesItemDetailCache[salesItemId] = request;
	return request;
}
function parsePoNumber(value) {
	if (value == null || value === "") {
		return 0;
	}
	var normalized = value.toString().replace(/,/g, "");
	var parsed = parseFloat(normalized);
	return isNaN(parsed) ? 0 : parsed;
}

function calculatePoTotals() {
	var total = 0;
	$("#poTable tbody tr, #poTableEdit tbody tr").each(function () {
		var $row = $(this);
		var amount = parsePoNumber($row.find(".amtInput").val());
		if (!isNaN(amount)) {
			total += amount;
		}
	});
	var roundedTotal = Math.round(total * 100) / 100;
	$("#poTotal").val(commaSeparateNumber(roundedTotal));
}

$(document).ready(function () {
	
	$(".amount").attr("readonly", "readonly");
	$(".total").attr("readonly", "readonly");
	getSalesItemtbySalesItemId();
	getHsnUnitPriceByItemId();
	addSalesOrder();
	//enable_disable();

	/*Display vendor list*/
	$("#partyDropDown2 option:not(:first)").remove();
	$.each(partyList, function (index, value) {
		var vendorPartyList = value.partyName;
		$("#partyDropDown2").append(
			'<option value=' + value.id + ' title="' + vendorPartyList + '">' + vendorPartyList + '</option>'
		);
	});

	$(document).on("change", "#partyDropDown2", function () {
		//$("#salesOrderDropdown0").attr("disabled",false);
		$(this).removeClass('border-color');
	});
  
	/***Amount Calculation**/
	$(document).on('keyup mouseup input', '.qtyInput,.upInput', function () {
		var index = $(this).closest("tr").index();
		//var newUp = 
		var parent = $(this).closest('tr');
		/*
		 * Below code is checking the unit price depending on
		 * current mode (edit or create) and seaprating values
		 * by comma..
		 */
		var poNumber = $('#poNumberEdit').text();

		var up1;
		if (poNumber == null || poNumber == "") {
			up1 = $("#newPrice" + index).val();
			$("#newPrice" + index).val(commaSeparateNumber(up1));
		} else {
			up1 = $("#newUPrice" + index).val();
			$("#newUPrice" + index).val(commaSeparateNumber(up1));
		}
		var u = up1.includes(",");
		if (u == true) {
			up1 = up1.replace(/,/g, "");
		}
		//	parent.find('.amtInput').val(parseFloat(parent.find('.qtyInput').val()) * parseFloat(parent.find('.upInput').val()))
		var orderQty = $("#orderQuantity" + index).val();
		/*if(orderQty>=0 && orderQty!=""){
			parent.find('.amtInput').val(parseFloat(orderQty) * parseFloat(up1))
		}else{*/
		parent.find('.amtInput').val(parseFloat(parent.find('.qtyInput').val()) * parseFloat(up1))
	//}// var amountPrice = parseFloat($("#amount"+index).val()).toFixed(2);
		//var up = $("#newPrice"+index).val();

		$("#newPrice" + index).val(commaSeparateNumber(up1));
		var amountPrice = parseFloat($("#amount" + index).val());
		//$("#amount"+index).val(amountPrice);
		$("#amount" + index).val(commaSeparateNumber(amountPrice));
		if (parent.find('.amtInput').val() == "NaN") {
			parent.find('.amtInput').val("");
		}
		calculatePoTotals();
		var salesItemId=$("#descriptionDropdown"+index).val();
		var modelNumberPrese=$("#modelNo"+index).val();
		if(modelNumberPrese!=currModel && modelNumberPrese!=""){
		getLastPriceForThisModel(modelNumberPrese,index);
		getQtyForThisModel(modelNumberPrese,index,salesItemId);
		$("#hiddenfield").val(index);
		}
	});
	
	/*$(document).on('keyup mouseup input', '.upInput', function () {
		var index = $(this).closest("tr").index();
		var modelNumberPrese=$("#modelNo"+index).val();
		if(modelNumberPrese!=currModel && modelNumberPrese!=""){
		getLastPriceForThisModel(modelNumberPrese,index);
		$("#hiddenfield").val(index);
		}
	});*/
	/*$(document).on('keyup mouseup input', '.qtyInput', function () {
		var index = $(this).closest("tr").index();
		var modelNumberPrese=$("#modelNo"+index).val();
		var salesItemId=$("#descriptionDropdown"+index).val();
		var totalQty=$("#totalQty"+index).val();
		if(modelNumberPrese!=currModel && modelNumberPrese!=""){
		getQtyForThisModel(modelNumberPrese,index,totalQty,salesItemId);
		$("#hiddenfield").val(index);
		}
	});*/


	/**Display sales order in first row**/

	$.each(SalesOrder, function (index, value) {
		var clientPoNumber;
		var clientName = value.party.partyName;
		clientName = clientName.split(' ');
		if (clientName.length > 100) {
			clientName.splice(100);
		}
		clientName = clientName.join(' ');
		var length = $.trim(clientName).length;
		if (length > 25) {
			clientName = $.trim(clientName).substring(0, 25) + "....";
		}
		if(value.clientPoNumber == null || value.clientPoNumber == undefined ){
			clientPoNumber = ' ';
		}else{
			clientPoNumber = value.clientPoNumber;
		}
		$("#salesOrderDropdown0").append('<option value=' + value.id + '>' + clientName + "-" +clientPoNumber + '</option>');
	});

	/*$.each(itemList, function (index, value) {
		var modelNo = value.model;
		modelNo = modelNo.split(' ');
		if (modelNo.length > 100) {
			modelNo.splice(100);
		}
		modelNo = modelNo.join(' ');
		var length = $.trim(modelNo).length;
		if (length > 25) {
			modelNo = $.trim(modelNo).substring(0, 25) + "....";
		}
		$("#modelNo0").append('<option value=' + value.id + '>' + modelNo + '</option>');
	});*/
	itemName = itemList.map(({ itemName }) => itemName);
	$(".poDescription").autocomplete({
		source: itemName
	});
	/*unitName = unitsList.map(({ name }) => name);
	$(".unit").autocomplete({
		source: unitName
	});
*/


	/**Remove row from table**/
	$("#poTable").on("click", ".deleteButton", function () {
		var indexmod = $("table tr"). index(this);
		$(this).closest("tr").remove();
	
		/***Setting name and id to remaining rows**/
		$('tbody').find('tr').each(function (index) {
			let prev = index - 1;
			let firstTdElement = $(this).find('td')[0];
			$(firstTdElement).text(index + 1);
			let secondTdElement = $(this).find('td')[1];
			$(secondTdElement).find('select').attr('id', 'salesOrderDropdown' + index);
			$(secondTdElement).find('input').attr('id', 'salesOrder' + prev);
			let thirdTdElement = $(this).find('td')[2];
			$(thirdTdElement).find('select').attr('name', 'items[' + index + '].description');
			$(thirdTdElement).find('select').attr('id', 'descriptionDropdown' + index);
			let poDescriptionElement = $(this).find('td')[4];
			$(poDescriptionElement).find('input').attr('name', 'items[' + index + '].poDescription');
			$(poDescriptionElement).find('input').attr('id', 'poDescription' + index);
			let forthTdElement = $(this).find('td')[3];
			$(forthTdElement).find('select').attr('name', 'items[' + index + '].modelNo');
			$(forthTdElement).find('select').attr('id', 'modelNo' + index);
			let fifthTdElement = $(this).find('td')[5];
			$(fifthTdElement).find('input').attr('name', 'items[' + index + '].hsnCode');
			$(fifthTdElement).find('input').attr('id', 'hsnCode' + index);
			let sixthTdElement = $(this).find('td')[6];
			$(sixthTdElement).find('input').attr('name', 'items[' + index + '].quantity');
			$(sixthTdElement).find('input').attr('id', 'newQuantity' + index);
			let seventhTdElement = $(this).find('td')[7];
			$(seventhTdElement).find('input').attr('id', 'salesQuantity' + index);
			let eighthTdElement = $(this).find('td')[8];
			//$(eighthTdElement).find('input').attr('name', 'items[' + index + '].unit');
			$(eighthTdElement).find('input').attr('id', 'unit' + index);
			let ninthTdElement = $(this).find('td')[9];
			$(ninthTdElement).find('input').attr('name', 'items[' + index + '].unitPrice');
			$(ninthTdElement).find('input').attr('id', 'newPrice' + index);
			let tenthTdElement = $(this).find('td')[10];
			$(tenthTdElement).find('input').attr('id', 'salesUnitPrice' + index);
			let taxTdElement = $(this).find('td')[11];
			$(taxTdElement).find('input').attr('id', 'tax' + index);
			let eleventhTdElement = $(this).find('td')[12];
			$(eleventhTdElement).find('input').attr('name', 'items[' + index + '].amount');
			$(eleventhTdElement).find('input').attr('id', 'amount' + index);
		    
		});
		calculatePoTotals();
	});

	/**Quantity and unit price validation**/
	/**Quantity and unit price validation**/
	var debouncedQtyPriceChangeHandler = debounce(function (target) {
		var $row = $(target).closest("tr");
		let salesItemId = $row.find("td").eq(2).find('select').val();
		var qtyInputElement = $row.find("td").eq(6).find('input');
		var newQuantity = parseFloat(qtyInputElement.val());
		let modelNumber = $row.find("td").eq(3).find('select').val();
		var index = $row.index();
		checkPurchaseItemExists(modelNumber, salesItemId, index);
		if(!salesItemId){
			return;
		}
		fetchSalesItemDetails(salesItemId).done(function () {
			qtyInputElement.removeClass('border-color');
			var salesQuantity = parseFloat($("#salesQuantity" + index).val());
			if (newQuantity > salesQuantity) {
				$.error("Entered Quantity is greater than " + Math.round(salesQuantity * 100) / 100);
				qtyInputElement.addClass('border-color');
			}
		});
	}, 250);
	$('#poTable').on("change", '.qtyInput,.upInput', function () {
		debouncedQtyPriceChangeHandler(this);
	});
	$(document).on("submit", "form#purchaseOrderForm", function (event) {
		var rowCount=$('#poTable >tbody  > tr').length;
		if(rowCount==0){
			event.preventDefault();
			$.error("no items found");
		}
		/**Vendor valiadtion**/
		let vendorValue = $('#partyDropDown2 option:selected').val();
		$('#partyDropDownValue').val(vendorValue);
		if (vendorValue == "") {
			event.preventDefault();
			$.error("Plaese select the vendor before submitting ");
			$("#partyDropDown2").addClass('border-color');
		}

		/**Sales order validation***/
		$('#poTable > tbody  > tr > td > select.salesOrderDropdown').each(function (index, input) {
			if($(this).val()==null){
				var row = index + 1;
				event.preventDefault();
				$.error("Please select sales order dropdown before submitting at row" + row);
				$(this).addClass('border-color');
			}else if ($(this).val().trim() === '') {
				var row = index + 1;
				event.preventDefault();
				$.error("Please select sales order dropdown before submitting at row" + row);
				$(this).addClass('border-color');
			}

		});

		/***Description Validation***/
		$('#poTable > tbody  > tr > td > select.descriptionDropdown').each(function (index, input) {
			if($(this).val()==null){
				var row = index + 1;
				event.preventDefault();
				$.error("Please select description dropdown before submitting at row " + row);
				$(this).addClass('border-color');
			}else if ($(this).val().trim() === '') {
				var row = index + 1;
				event.preventDefault();
				$.error("Please select description dropdown before submitting at row " + row);
				$(this).addClass('border-color');
			}
			$("#descriptionDropdown" + index).change(function () {
				$("#unit" + index).removeClass('border-color');
				$("#newQuantity" + index).removeClass('border-color');
			});
		});
		$('#poTable > tbody  > tr > td > input.poDescription').each(function (index, input) {
			var row = index + 1;
			var poDesc = $("#poDescription" + index).val();
			if (poDesc == "" || poDesc == undefined) {
				event.preventDefault(event);
				$("#poDescription" + index).addClass('border-color');
				$.error("Please add PO Description before submitting at row" + row);
			}
		});
		
		var modelList=[];
		$('#poTable > tbody  > tr > td > select.modelNo').each(function (index, input) {
			var modelNumber=$("#modelNo"+index).val();
			var salesItemId=$("#descriptionDropdown"+index).val();
			var row = index + 1;
			var modelArray=modelNumber+salesItemId+"/"+row
			if ($(this).val() === ''||$(this).val()==null||$(this).val()==undefined) {
				event.preventDefault();
				$.error("Please select model dropdown before submitting at row " + row);
				$(this).addClass('border-color');
			}
			$("#modelNo" + index).change(function () {
				$("#hsnCode" + index).removeClass('border-color');
			});
			modelList.push(modelArray);
		});
		
		var modelListArray = modelList.sort(); 

		var dupModelArray = [];
		var duplicateRow;
		for (var i = 0; i < modelListArray.length - 1; i++) {
			 if (modelListArray[i + 1].split("/")[0] == modelListArray[i].split("/")[0]) {
			    	duplicateRow=modelListArray[i].split("/").reverse()[0]+" and "+modelListArray[i+1].split("/").reverse()[0];
			    	dupModelArray.push(modelListArray[i]);
			    }
		}
		if(dupModelArray.length>0){
			$.error("Same item selected at row " + duplicateRow);
			event.preventDefault();
			return false;
		}
		$('#poTable > tbody  > tr > td > input.hsnCode').each(function (index, input) {
			var row = index + 1;
			var hsnCode = $("#hsnCode" + index).val();
			if (hsnCode == "" || hsnCode == undefined) {
				event.preventDefault(event);
				$("#hsnCode" + index).addClass('border-color');
				$.error("Please add HSN before submitting at row" + row);
			}

		});
		/*$('#poTable > tbody  > tr > td > input.unit').each(function (index, input) {
			var row = index + 1;
			var unit = $("#unit" + index).val();
			if (unit == "" || unit == undefined) {
				event.preventDefault(event);
				$("#unit" + index).addClass('border-color');
			}

		});*/
		$('#poTable > tbody  > tr > td > input.amount').each(function (index, input) {
			var row = index + 1;
			/*
			 * resetting amount according to db datatype
			 * as they have comma separated values in it
			 */
			var amount = $("#amount" + index).val();
			amount = amount.replace(/,/g, "");
			$("#amount" + index).val(amount);
			if (amount == "" || amount == undefined) {
				event.preventDefault(event);
				$("#amount" + index).addClass('border-color');
				$.error("Please add Amount before submitting at row" + row);
			}
		});

		/***Quantity Validation**/
		$('#poTable > tbody  > tr > td > input.qtyInput').each(function (index, input) {
			//var row=index + 1;
			var qtyInput = $("#newQuantity" + index).val();
			let salesQuantity = parseFloat($("#salesQuantity" + index).val());
			let newQuantity = parseFloat($("#newQuantity" + index).val());
			let row = index + 1
			if (qtyInput == "" || qtyInput == undefined) {
				event.preventDefault(event);
				$("#newQuantity" + index).addClass('border-color')
				$.error("Please add Quantity before submitting at row" + row);
			}
			/*var digits= new RegExp(/^[0-9]+$/);
			var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
	        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
			if (qtyInput.match(digits)||qtyInput=="") {

			}
			else{
				 $("#newQuantity"+index).addClass('border-color');
				 event.preventDefault(event);
	        	$.error("only digits are allowed for qty at row "+ row);
			}*/

			if (newQuantity > salesQuantity) {
				event.preventDefault();
				$.error("Entered Quantity is greater than " + Math.round(salesQuantity * 100) / 100 + " at row " + row);
				$(this).addClass('border-color');
			}
		});
		/***Unit price validation***/
		$('#poTable > tbody  > tr > td > input.upInput').each(function (index, input) {
			/*
			 * resetting unit price according to db datatype
			 * as they have comma separated values in it
			 */
			var unitPrice = $("#newPrice" + index).val();
			unitPrice = unitPrice.replace(/,/g, "");
			$("#newPrice" + index).val(unitPrice);
			let salesPrice = parseFloat($("#salesUnitPrice" + index).val());
			let newUnitPrice = parseFloat($("#newPrice" + index).val());

			let row = index + 1
			if (unitPrice == "" || unitPrice == undefined ||unitPrice=="NaN") {
				$("#newPrice" + index).addClass('border-color')
				event.preventDefault(event);
				$.error("Please add Unit Price before submitting at row" + row);

			}
			
			/*else if (newUnitPrice > salesPrice ) {
					event.preventDefault();	
                  $.error("Entered Quantity is greater than " +salesPrice+ " at row " +row);
					$(this).addClass('border-color');
			   }*/
			$("#newPrice" + index).change(function () {
				$("#newPrice" + index).removeClass('border-color');
				$("#amount" + index).removeClass('border-color');
			});


		});
		
		
		$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
			$("#savePurchaseOrder").attr('disabled', false);
			});
		 $("#savePurchaseOrder", this)
	     .attr('disabled', 'disabled');
	})
	editPoTable();
	$('#poTableEdit').on("change", '.qtyInput,.upInput', function () {
		let index = $(this).closest("tr").index();
		let salesItemId = $(this).closest("tr").find("td").eq(3).find('select').val();
		var newQuantity = $(this).closest("tr").find("td").eq(7).find('input').val();
		let qtyInpt = $(this).closest("tr").find("td").eq(7).find('input');
		let newPrice = $(this).closest("tr").find("td").eq(10).find('input').val();
		let upInput = $(this).closest("tr").find("td").eq(10).find('input');
		newQuantity= parseFloat(newQuantity);
		var soItemId=$("#descriptionDropdown"+index).val();
		var modelNumberPrese=$("#modelNo"+index).val();
		if(modelNumberPrese!=currModel && modelNumberPrese!=""){
		getLastPriceForThisModel(modelNumberPrese,index);
		getQtyForThisModel(modelNumberPrese,index,soItemId);
		}
		
		$.ajax({
			type: "GET",
			url: api.SALES_ITEM_BY_SALESITEMID + "?id=" + salesItemId,
			success: function (response) {
				console.log("edit response:", response);
				qtyInpt.removeClass('border-color');
				var row = $('table > tbody  > tr').length;
				//var index = row - 1;
				//let salesQuantity =$("#salesQuantity" + index).val();
				//let salesQuantity = response.quantity;
				var salesQuantity =$('#remQty' + index).val();
				salesQuantity=parseFloat(salesQuantity);
				if (newQuantity > salesQuantity) {
					$.error("Entered Quantity is greater than " + Math.round(salesQuantity * 100) / 100);
					qtyInpt.addClass('border-color');
				}
				/*let salesPrice=response.unitPrice
				 $('#salesUPrice'+index).val(salesPrice);
			upInput.removeClass('border-color');
			if (newPrice > salesPrice) {
				$.error("Entered Unit price is greater than " +salesPrice);
				upInput.addClass('border-color');		
			} */

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


	$(document).on("submit", "form#editPurchaseOrderForm", function (event) {
		/***Quantity Validation**/
		$('#poTableEdit > tbody  > tr > td > input.qtyInput').each(function (index, input) {
			let row = index + 1;
			/*
			  * resetting amount according to db datatype
			  * as they have comma separated values in it
			  */
			var amount = $("#amount" + index).val();
			if (amount.includes(",")) {
				amount = amount.replace(/,/g, "");
			}
			$("#amount" + index).val(amount);
			var qtyInput=$("#newQty" + index).val();
			let salesQuantity = parseFloat($('#remQty' + index).val());
			let newQuantity = parseFloat($("#newQty" + index).val());
			if(qtyInput==""||qtyInput==undefined){
				event.preventDefault();
				$.error("Please enter qty at row " + row);
				$(this).addClass('border-color');
			}
			/*var digits= new RegExp(/^[0-9]+$/);
			var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
	        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
			if (qtyInput.match(digits)||qtyInput=="") {

			}
			else{
				 $("#newQty"+index).addClass('border-color');
				 event.preventDefault(event);
	        	$.error("only digits are allowed for qty at row "+ row);
			}*/
			if (newQuantity > salesQuantity) {
				event.preventDefault();
				$.error("Entered Quantity is greater than " + Math.round(salesQuantity * 100) / 100 + " at row " + row);
				$(this).addClass('border-color');
			}

		});
		/*unit price valiadtion**/
		$('#poTableEdit > tbody  > tr > td > input.upInput').each(function (index, input) {
			let row = index + 1;
			/*
			  * resetting unitprice according to db datatype
			  * as they have comma separated values in it
			  */
			var newUPrice = $("#newUPrice" + index).val();
			if (newUPrice.includes(",")) {
				newUPrice = newUPrice.replace(/,/g, "");
			}
			var unitPriceInput=$("#newUPrice" + index).val();
			$("#newUPrice" + index).val(newUPrice);
			/*let salesPrice = parseInt($('#salesUPrice' + index).val());
			let newUnitPrice = parseInt($("#newUPrice" + index).val());
			if (newUnitPrice > salesPrice) {
				event.preventDefault();
				$.error("Entered Quantity is greater than " + salesPrice + " at row " + row);
				$(this).addClass('border-color');
			}*/
			var amountInp=$("#amount" + index).val();
			if((unitPriceInput=="0"&&amountInp=="")||unitPriceInput==""||unitPriceInput==undefined ||unitPriceInput=="NaN"){
				event.preventDefault(event);
				$("#amount" + index).addClass('border-color');
				$.error("Please add Amount before submitting at row" + row);
			}

		});
		
		$('#poTableEdit > tbody  > tr > td > input.amount').each(function (index, input) {
			var row = index + 1;
			/*
			 * resetting amount according to db datatype
			 * as they have comma separated values in it
			 */
			var amount = $("#amount" + index).val();
			amount = amount.replace(/,/g, "");
			$("#amount" + index).val(amount);
			/*if (amount == "" || amount == undefined) {
				event.preventDefault(event);
				$("#amount" + index).addClass('border-color');
				$.error("Please add Amount before submitting at row" + row);
			}*/
		});
		

		$('#poTableEdit > tbody  > tr > td > input.delivaryDate').each(function (index, input) {
			var row = index + 1;
			var delDate = $("#delivaryDate" + index).val();
			if(delDate!==""){
				var delDateFormat=delDate.split("-"); //split date by "/"
				delDate= delDateFormat[1]+"/"+delDateFormat[0]+"/"+delDateFormat[2]; //change the format to mm/dd/yyyy to work in next step
				delDate=new Date(delDate);
				delDate=delDate.toDateString();
				$("#delivaryDate" + index).val(delDate);
			}else{
				$( "#delivaryDate" + index ).val(new Date(0));
				//$("#delivaryDate" + index).val("NULL");
			}
			
		});
		

		var modelList=[];
		$('#poTableEdit > tbody  > tr > td > select.modelNo').each(function (index, input) {
			var modelNumber=$("#modelNo"+index).val();
			var salesItemId=$("#descriptionDropdown"+index).val();
			var row = index + 1;
			var modelArray=modelNumber+salesItemId+"/"+row;
			if ($(this).val() === ''||$(this).val()==null||$(this).val()==undefined) {
				event.preventDefault();
				$.error("Please select model dropdown before submitting at row " + row);
				$(this).addClass('border-color');
			}
			$("#modelNo" + index).change(function () {
				$("#hsnCode" + index).removeClass('border-color');
			});
			modelList.push(modelArray);
		});
		
		var modelListArray = modelList.sort(); 

		var dupModelArray = [];
		var duplicateRow;
		for (var i = 0; i < modelListArray.length - 1; i++) {
		    if (modelListArray[i + 1].split("/")[0] == modelListArray[i].split("/")[0]) {
		    	duplicateRow=modelListArray[i].split("/").reverse()[0]+" and "+modelListArray[i+1].split("/").reverse()[0];;
		    	dupModelArray.push(modelListArray[i]);
		    }
		}
		if(dupModelArray.length>0){
			$.error("Same item selected at row "+ duplicateRow);
			event.preventDefault();
			return false;
		}
		$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
			$("#savePurchaseOrder").attr('disabled', false);
			});
		 $("#savePurchaseOrder", this)
	     .attr('disabled', 'disabled');
	})
});

function checkPurchaseItemExists(itemId,salesItemId,index){
	var salesQuantity = parseFloat($("#salesQuantity" + index).val());
	var salesQtyInString=$("#salesQuantity" + index).val();
	var newQuantity = parseFloat($("#newQuantity" + index).val());
	var salesQuantityEd = parseFloat($("#remQty" + index).val());
	var newQuantityEd = parseFloat($("#newQty" + index).val());
	var purchaseQty=0;
	var remainingQty=0;
	var remainingQtyEd=0;
	var row=index+1;
	$.ajax({
		type: "GET",
		url: api.GET_PURCHASEITEM_BY_ITEMID_SALESITEMID + "?salesItemId=" + salesItemId+ "&&itemId="+itemId,
		success: function (response) {
			if(response.length>0){
				$.each(response,function(index,value){
					purchaseQty=purchaseQty+value.quantity;
					
				})
				var remainingQty=Math.round((salesQuantity-purchaseQty) * 100) / 100;
				var remainingQtyEd=Math.round((salesQuantityEd-purchaseQty) * 100) / 100;
				if(salesQtyInString!=undefined){
					if(purchaseQty>=salesQuantity){
						$.error("Purchase Order already exist for item at row "+row);
					}else if(newQuantity>=remainingQty){
						$.error("Remaining Qty to be ordered is "+remainingQty + "at row "+row);
					}else{
						
					} 
				}else{	
					if(purchaseQty>=salesQuantityEd){
						$.error("Purchase Order already exist for item at row "+row);
					}else if(newQuantityEd>=remainingQtyEd){
						$.error("Remaining Qty to be ordered is "+remainingQtyEd+ "at row "+row);
					}else{
						
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
	
}

//validation to enter only number or float value for qty. 
/**$(document).on("keypress",".qty", function(e){
	var qtyVal=$(this).val();
	if(isNaN(qtyVal+""+String.fromCharCode(e.charCode))) return false;
  })
  .on("cut copy paste",function(e){
	e.preventDefault();
  });**/

//validating quantity on only allowing numeric values
/*$(document).on("focusout", ".qtyInput", function (e) {
	var qtyValue = this.value;
	var digits = new RegExp(/^[0-9]+$/);
	var floatNum = new RegExp(/^[+-]?\d+(\.\d+)?$/);
	if (qtyValue.match(digits) || qtyValue == "") {

	}
	else {
		$.error("only digits are allowed");
	}
});*/

$(document).on("keyup keypress input", ".unitPrice", function (e) {
	if(this.value.match(/\-/)) 
		this.value=this.value.replace(/\D/g,'')
});

/**On change of salesOrder in first row get Description**/
$(document).on("change", "#salesOrderDropdown0", function () {
	/**reset input values*/
	$("#descriptionDropdown0").children("option").filter(":not(:first)").remove();
	//$("input[name='items[0].hsnCode']").val("");
	$("input[name='items[0].modelNo']").val("");
	$("input[name='items[0].quantity']").val("");
	$("input[name='items[0].unitPrice']").val("");
	$("input[name='items[0].poDescription']").val("");
	$("#unit0").val("");
	$("input[name='items[0].amount']").val("");
	//on change of sales order dropdown remove the error border from salesOrderDropdown, qty and unit price.
	$("input[name='items[0].quantity']").removeClass("border-color");
	$("input[name='items[0].unitPrice']").removeClass("border-color");
	$(this).removeClass('border-color');

	$("#descriptionDropdown0").attr("disabled", false);
	var soId = $(this).val();
	var row=$(this).index();
	var soText=$(this).find('option:selected').text();
	if($("input[type='checkbox']#autoPOcheckbox").is(':checked')) {
		let vendorValue = $('#partyDropDown2 option:selected').val();
		if (vendorValue == "") {
			$.error("Plaese select the vendor before adding purchase Item ");
			$("#partyDropDown2").addClass('border-color');
			return false;
		}else{
		getAutoPopulateSalesOrdertbySalesOrderId(soId,row,soText);
		}
		
	}else{
		getSalesOrdertbySalesOrderId(soId);
	}
	
});


/**On change of description in first row reset other input values**/
$(document).on("change", "#descriptionDropdown0", function () {
	/**reset input values*/
	//$("input[name='items[0].hsnCode']").val("");
	$("input[name='items[0].modelNo']").val("");
	$("input[name='items[0].quantity']").val("");
	$("input[name='items[0].unitPrice']").val("");
	$("input[name='items[0].poDescription']").val("");
	$("#unit0").val("");
	$("input[name='items[0].amount']").val("");
	//on change of description dropdown remove the error border from descriptionDropdown, qty and unit price
	$("input[name='items[0].quantity']").removeClass("border-color");
	$("input[name='items[0].unitPrice']").removeClass("border-color");
	$(this).removeClass('border-color');



});
/**On change of modelNo in first row reset other input values**/
$(document).on("change", "#modelNo0", function () {
	/**reset input values*/
	$("input[name='items[0].hsnCode']").val("");
	$("input[name='items[0].unitPrice']").val("");
	$("input[name='items[0].amount']").val("");
	$("input[name='items[0].poDescription']").val("");
	$(this).removeClass('border-color');



});

function getSalesOrdertbySalesOrderId(soId) {
	//className String is used to differentiate. since this api is used many places
	var className="po";
	var poNumEdit=$("#poNumEdit").val();
	var row ;
	if(poNumEdit=="" || poNumEdit==undefined || poNumEdit==null){
		row = $('#poTable > tbody  > tr').length - 1;
	}else{
		row = $('#poTableEdit > tbody  > tr').length - 1;
	}
	if(!soId){
		return;
	}
	var cachedItems = salesOrderItemsCache[soId];
	if(cachedItems){
		populateDescriptionDropdown(row, cachedItems);
		return;
	}
	$.ajax({
		type: "GET",
		url: api.SALES_LIST_BY_SOID + "?id=" + soId+"&&className="+className,
		success: function (response) {
			salesOrderItemsCache[soId] = response;
			populateDescriptionDropdown(row, response);
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

function populateDescriptionDropdown(row, items){
	var dropdown = $("#descriptionDropdown" + row);
	if(dropdown.length===0){
		return;
	}
	dropdown.children("option").filter(":not(:first)").remove();
	$.each(items, function (index, value) {
		var description = value.description;
		description = description.split(' ');
		if (description.length > 100) {
			description.splice(100);
		}
		description = description.join(' ');
		var length = $.trim(description).length;
		if (length > 50) {
			//description = $.trim(description).substring(0, 50) + "....";
		}
		dropdown.append('<option value=' + value.id + '>' + value.slNo + '.' + description + '</option>');
	});
	$('input').removeClass("has-error");
}

/*function getAutoPopulateSalesOrdertbySalesOrderId(soId,row) {
	var vendorId=$("#partyDropDown2,#partyByType").val();
	//className String is used to differentiate. since this api is used many places
	var className=vendorId;
	//var row = $('#poTable > tbody  > tr').length - 1;
	$.ajax({
		type: "GET",
		url: api.SALES_LIST_BY_SOID + "?id=" + soId+"&&className="+className,
		success: function (response) {
			
			console.log("response:", response);
			
			$.each(response, function (index, value) {
				var description = value.description;
				description = description.split(' ');
				if (description.length > 100) {
					description.splice(100);
				}
				description = description.join(' ');
				var length = $.trim(description).length;
				if (length > 50) {
					//description = $.trim(description).substring(0, 50) + "....";
				}
				//$("#descriptionDropdown" + row).append('<option value=' + value.id + '>' +value.slNo+'.' +description + '</option>');
				//$("#descriptionDropdown" + row).val(value.id);
				//$("#descriptionDropdown" + row).append('<option value=' + value.id + '>' +value.slNo+'.' +description + '</option>');
				//$("#descriptionDropdown" + row).val(value.id);
				//populateModelList(value.id,true,row);
				//populateModelsWithDesigns(value.id,true,row,description);
			//	sleepThenAct();
				//row = row+1;
			    //$(".add").trigger('click');
				if(value.designItems.length==0){
					$("#descriptionDropdown" + row).append('<option value=' + value.id + '>' +value.slNo+'.' +description + '</option>');
					$("#descriptionDropdown" + row).val(value.id);
					populateModelList(value.id,true,row);
					row = row+1;
				    $(".add").trigger('click');
				//}else if(value.vendoritemsList.length==0){
					
				}else{
				$("#modelNo" + row).children("option").filter(":not(:first)").remove();
				$("#descriptionDropdown" + row).children("option").filter(":not(:first)").remove();
				$.each(value.designItems,function(i,v){
					$("#descriptionDropdown" + row).append('<option value=' + value.id + '>' +value.slNo+'.' +description + '</option>');
					$("#descriptionDropdown" + row).val(value.id);
					
					$("#modelNo" + row).append('<option value=' + v.itemMasterId + '>' +v.itemId+ '</option>');
					$("#modelNo" + row).val(v.itemMasterId);
					//populateModelList(value.id,true,row);
					var itemId = v.itemMasterId;
			        $("#salesQuantity" + row).val(v.quantity);
					$("#newQuantity" + row).val(v.quantity);
					$("#newQty" + row).val(v.quantity);
					$("#remQty" + row).val(v.quantity);
			        assignQtyAndHsn(itemId,row);
					
				    $(".add").trigger('click');
				    row = row+1;
				})
				}
			
			})
			
		$("#poTable >tbody>tr:last").remove();
		//	$("#poTableEdit >tbody>tr:last").remove();
			
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
}*/
function assignQtyAndHsn(itemId,index){
	$.ajax({
		type: "GET",
		url: api.ITEM_LIST_BYID + "?id=" + itemId,
		success: function (response) {
			$("input[name='items[" + index + "].hsnCode']").val(response.hsnCode);
			$("#tax"+index).val(response.gst);
			$("#unit"+index).val(response.item_units.name);
			$("#poDescription" + index).val(response.itemName);
		    //For displaying the quantity on change of model..
			
			//$("input[name='items[" +index+ "].unitPrice']").val(response.sellPrice);
			//$("#salesUnitPrice"+index).val(response.sellPrice);
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

/**Add rows to table**/
function addSalesOrder() {
	
	$("#poTable").on("click", ".add", function () {
		$("#itemTable tbody").empty();
		/**Vendor valiadtion**/
		let vendorValue = $('#partyDropDown2 option:selected').val();
		if (vendorValue == "") {
			$.error("Plaese select the vendor before adding purchase Item ");
			$("#partyDropDown2").addClass('border-color');
			return false;	
		}
		$("#partyDropDown2").removeClass('border-color');

		var newRow = $("<tr>");
		var row = $('#poTable > tbody  > tr').length;
		var rowCount = row + 1
		var arraycount = rowCount - 1;
		var previousRow = arraycount - 1
		/**Sales order validation***/
		var selectedSOid = $("#salesOrderDropdown" + previousRow).val();
		/*if (selectedSOid == "") {
			$.error("Plaese select the sales order before adding purchase Item ");
			$("#salesOrderDropdown" + previousRow).addClass('border-color');
			return false;
		}*/
		$("#salesOrderDropdown" + previousRow).removeClass('border-color');

		/***Description Validation**/
		var descriptionValue = $("#descriptionDropdown" + previousRow).val();
		/*if (descriptionValue == "") {

			$.error("Please select the description before adding purchase Item ");
			$("#descriptionDropdown" + previousRow).addClass('border-color');
			return false;
		}*/
		$("#descriptionDropdown" + previousRow).removeClass('border-color');
		var columns = "";
		var columns = "";
		columns += '<td width="5%" class="styleOfSlNo">' + rowCount + '</td>';

		columns += '<td width="15%"> <select class="form-control PositionofTextbox salesOrderDropdown dropdownMarginTop" name="salesOrder" id="salesOrderDropdown' + arraycount + '" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;><option value="">Select Client Name:</option></select></td>'

		columns += '<td width="11%"> <select class="form-control select2 PositionofTextbox  descriptionDropdown dropdownMarginTop" name="items[' + arraycount + '].description" id="descriptionDropdown' + arraycount + '" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="">Select Description:</option></select>   </td>';
		/*columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox poDescription" id="poDescription' + arraycount + '" name="items[' + arraycount + '].poDescription" /></td>';*/
		columns += '<td width="10%"><select class="form-control PositionofTextbox modelNo dropdownMarginTop" id="modelNo' + arraycount + '" name="items[' + arraycount + '].modelNo" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="">Select ModelNo:</option></select>   </td>';
		columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox poDescription" id="poDescription' + arraycount + '" name="items[' + arraycount + '].poDescription" /></td>';
		columns += '<td width="8%"><input type="text" class="form-control PositionofTextbox hsnCode" id="hsnCode' + arraycount + '" name="items[' + arraycount + '].hsnCode" /><span id="hsnCodeDiv' + arraycount + '"></span></td>';
		columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox qty qtyInput" id="newQuantity' + arraycount + '" name="items[' + arraycount + '].quantity" "/><span id="qtyDiv' + arraycount + '"></span></td>';
		columns += '<td class="hideTd"><input type="hidden" class="salesQtyInput" value="" id="salesQuantity' + arraycount + '" /></td>';

		columns += '<td width="6%"><input type="text" class="form-control PositionofTextbox unit" readonly="readonly" id="unit' + arraycount + '" /><span id="unitDiv' + arraycount + '"></span></td>';
		columns += '<td width="8%"><input type="text" class="form-control PositionofTextbox unitPrice upInput alignright" id="newPrice' + arraycount + '" name="items[' + arraycount + '].unitPrice"/><span id="unitPriceDiv' + arraycount + '"></span></td>';
		columns += '<td class="hideTd"><input type="hidden" class="salesUnitPrice" value="" id="salesUnitPrice' + arraycount + '" /></td>';
		columns += '<td width="7%"><input type="text" class="form-control PositionofTextbox tax alignright"  id="tax' + arraycount + '"  readonly="readonly"/></td>';
		columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox amount amtInput alignright"  id="amount' + arraycount + '" name="items[' + arraycount + '].amount" readonly="readonly"/><span id="amountDiv' + arraycount + '"></span></td>';

		columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>';
		columns += '<td class="hideTd"><input type="hidden" class="totalQty" value="" id="totalQty' + arraycount + '" /></td>';
		newRow.append(columns);
		$("#poTable").append(newRow);

		$("#salesOrderDropdown" + arraycount).select2({ dropdownAutoWidth: true });
		$("#descriptionDropdown" + arraycount).select2({ dropdownAutoWidth: true });
		$("#modelNo" + arraycount).select2({ dropdownAutoWidth: true });
		/**Replace select with input tag**/
		/*$("#salesOrderDropdown"+previousRow).replaceWith('<input type="text" class="form-control PositionofTextbox" id="salesOrder'+ previousRow +'" name="salesOrder">');
		 $('#salesOrder'+previousRow).next(".select2-container").hide();
		$("#salesOrder"+ previousRow).val(selectedSOid);*/
		//$("#salesOrderDropdown" + previousRow).attr("disabled", true);
		//$("#descriptionInput" + previousRow).attr("readonly", "readonly");

		itemName = itemList.map(({ itemName }) => itemName);
		$(".poDescription").autocomplete({
			source: itemName
		});
		/*unitName = unitsList.map(({ name }) => name);
		$(".unit").autocomplete({
			source: unitName
		});
*/
		/**Display sales order to added row**/
		$.each(SalesOrder, function (index, value) {
			var clientPoNumber;
			var clientName = value.party.partyName;
			clientName = clientName.split(' ');
			if (clientName.length > 100) {
				clientName.splice(100);
			}
			clientName = clientName.join(' ');
			var length = $.trim(clientName).length;
			if (length > 25) {
				clientName = $.trim(clientName).substring(0, 25) + "....";
			}
			if(value.clientPoNumber == null || value.clientPoNumber == undefined ){
				clientPoNumber = ' ';
			}else{
				clientPoNumber = value.clientPoNumber;
			}
			$("#salesOrderDropdown" + arraycount).append('<option value=' + value.id + '>' + clientName + "-" + clientPoNumber + '</option>');
		});

		/*$.each(itemList, function (index, value) {
			var modelNo = value.model;
			modelNo = modelNo.split(' ');
			if (modelNo.length > 100) {
				modelNo.splice(100);
			}
			modelNo = modelNo.join(' ');
			var length = $.trim(modelNo).length;
			if (length > 25) {
				modelNo = $.trim(modelNo).substring(0, 25) + "....";
			}
			$("#modelNo" + arraycount).append('<option value=' + value.id + '>' + modelNo + '</option>');
		});*/
		/**set previous value to current row**/
		$("#salesOrderDropdown" + arraycount).val(selectedSOid);
		/**On change of salesOrder in first row get Description**/
		$(document).on("change", "#salesOrderDropdown" + arraycount, function () {
			/**reset input values*/
			$("#descriptionDropdown" + arraycount).children("option").filter(":not(:first)").remove();
			//$("input[name='items[" +arraycount+ "].hsnCode']").val("");
			$("input[name='items[" + arraycount + "].modelNo']").val("");
			$("input[name='items[" + arraycount + "].quantity']").val("");
			$("#unit"+arraycount).val("");
			$("input[name='items[" + arraycount + "].poDescription']").val("");
			$("input[name='items[" + arraycount + "].unitPrice']").val("");
			$("input[name='items[" + arraycount + "].amount']").val("");
			//on change of sales order dropdown remove the error border from salesOrderDropdown, qty and unit price.
			$("input[name='items[" + arraycount + "].quantity']").removeClass("border-color");
			$("input[name='items[" + arraycount + "].unitPrice']").removeClass("border-color");
			$(this).removeClass('border-color');
			var soId = $(this).val();
			var row= arraycount;
			var soText=$(this).find('option:selected').text();
			if($("input[type='checkbox']#autoPOcheckbox").is(':checked')) {
				getAutoPopulateSalesOrdertbySalesOrderId(soId,row,soText);
			}else{
				getSalesOrdertbySalesOrderId(soId);
			}
			//getSalesOrdertbySalesOrderId(soId);
		});
		/**On change of description in every row reset other values**/
		$(document).on("change", "#descriptionDropdown" + arraycount, function () {
			/**reset input values*/
			//$("input[name='items[" +arraycount+ "].hsnCode']").val("");
			var soId = $(this).val();
			$("input[name='items[" + arraycount + "].modelNo']").val("");
			$("input[name='items[" + arraycount + "].quantity']").val("");
			$("input[name='items[" + arraycount + "].unitPrice']").val("");
			$("input[name='items[" + arraycount + "].poDescription']").val("");
			$("#unit"+arraycount).val("");
			$("input[name='items[" + arraycount + "].amount']").val("");
			//on change of description dropdown remove the error border from descriptionDropdown, qty and unit price
			$("input[name='items[" + arraycount + "].quantity']").removeClass("border-color");
			$("input[name='items[" + arraycount + "].unitPrice']").removeClass("border-color");
			$(this).removeClass('border-color');
		/*	if(soId!=undefined){
			populateModelList(soId,true);}*/
		});

		/**On change of model No in every row reset other values**/
		$(document).on("change", "#modelNo" + arraycount, function () {
			/**reset input values*/
			$("input[name='items[" + arraycount + "].hsnCode']").val("");
			$("input[name='items[" + arraycount + "].unitPrice']").val("");
			$("input[name='items[" + arraycount + "].amount']").val("");
			$("input[name='items[" + arraycount + "].poDescription']").val("");
			$(this).removeClass('border-color');
		});

		/**to append description of previous row sales order value in additional row*/
		getSalesOrdertbySalesOrderId(selectedSOid);
		getSalesItemtbySalesItemId();
		getHsnUnitPriceByItemId();

	});
}
/**on change of description item**/
function getSalesItemtbySalesItemId() {
	    var count =1;
		var debouncedDescriptionDropdownHandler = debounce(function (target) {
			var $target = $(target);
			let index = $target.closest('tr').index();
			var salesItemId = $target.val();
			if(salesItemId!=undefined && count==1){
				populateModelList(salesItemId,true,index);
			}
			$.ajax({
				type: "GET",
				url: api.SALES_ITEM_BY_SALESITEMID + "?id=" + salesItemId,
				success: function (response) {
					if(response!=""){
					var designQuantity = $("#salesQuantity" + index).val();
					$("#remQty" + index).val(response.quantity);
					let newQuantity = $("input[name='items[" + index + "].quantity']").val();
					let remainingQuantity = designQuantity - newQuantity;
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
		}, 250);
		$(document).on("change", '.descriptionDropdown', function () {
			debouncedDescriptionDropdownHandler(this);
		});
}
//populate hsn and unit price on change of model number.
function getHsnUnitPriceByItemId() {
	$(document).on("click", '#poTable>tbody>tr>td,#poTableEdit>tbody>tr>td', function () {
		let index = $(this).closest('tr').index()
		
		$(document).on("change", '#salesOrderDropdown' + index, function () {
			$("input[name='items[" + index + "].hsnCode']").val("");
			$("#tax"+index).val("");
			$("#unit"+index).val("");
			$("#modelNo"+index).empty();
		});
		
		$(document).on("change", '#modelNo' + index, function () {
			var tabIndex= $(this).closest('tr').index();
			//var salesItemId=$("#sales_item_id"+index).val();
			//var selectedSOid = $("#salesOrderDropdown" + index).val();
			var itemId = $(this).val();
			if(itemId!=undefined){
			var poObj = purchaseOrderObj;
			let salesItemId;
			if(poObj!=null){
			salesItemId = $(this).closest("tr").find("td").eq(3).find('select').val();
			}else{
				salesItemId = $(this).closest("tr").find("td").eq(2).find('select').val();
			}
			//var so = $("#soItemId"+index).val();
			//modelList = 
				populateModelList(salesItemId,false,index);

			$.ajax({
				type: "GET",
				url: api.ITEM_LIST_BYID + "?id=" + itemId,
				success: function (response) {
					$("input[name='items[" + index + "].hsnCode']").val(response.hsnCode);
					$("#tax"+index).val(response.gst);
					$("#unit"+index).val(response.item_units.name);
					$("#poDescription" + index).val(response.itemName);
				    //For displaying the quantity on change of model..
					$.each(modelList, function (i, value) {
						if(value.itemId == itemId){
							$("#salesQuantity" + index).val(value.quantity);
							$("#totalQty" + index).val(value.quantity);
							$("#newQuantity" + index).val(value.quantity);
							$("#newQty" + index).val(value.quantity);
							$("#remQty" + index).val(value.quantity);
							
						}
					});
					
					//$("input[name='items[" +index+ "].unitPrice']").val(response.sellPrice);
					//$("#salesUnitPrice"+index).val(response.sellPrice);
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
			
		});
		
	});

}
/**display purchase items**/
function editPoTable() {
	var vendorName=$("#vendorName").html();
	if(vendorName.length>35){
		vendorName= $.trim(vendorName).substring(0, 35) + "....";
	}
	$("#vendorName").html(vendorName);
	var table = $('#poTableEdit').DataTable({
		select: 'single',
		"bSortable": false,
		"searching": false,

		"paging": false,
		"bInfo": false,
		"ordering": false,
		destroy: true,
		"aaData": purchaseOrderObj,
		"aoColumnsDefs": [
		    { "sClass": "CellWithComment", "aTargets": [2,3,4] }
		],

		"aoColumns": [
			{
				"title": "Sl.No",
				"mData": "id",
				"class": "styleOfSlNo",
				render: function (mData, type, row, meta) {
					let slId = meta.row + 1;
					return slId;
				}
			},
			{
				"mData": "sales_item_id",
				"class": "hideTd",
				render: function (mData, type, row, meta) {
					//checkForDcInvoiceCreated(row.purchase_item_id,row.description,meta.row);
					return '<input type="hidden"  value="' + row.purchase_item_id + '"/>';
				}
			},
			
			{
				"title": "Client Name",
				"mData": "description",
				"class": "txtUnWrap CellWithComment",
				"width":"10%",
				render: function (mData, type, row, meta) {
					var clientName = null;
					var clientPoNumber;
					$.each(salesItemList, function (index, value) {
						if (mData == value.id) {
							clientName = value.salesOrder.party.partyName;
							soId = value.salesOrder.id;
							if(value.salesOrder.clientPoNumber == null || value.salesOrder.clientPoNumber == undefined ){
								clientPoNumber = ' ';
							}else{
								clientPoNumber = value.salesOrder.clientPoNumber;
							}
						}
					});
					$("#salesOrerDiv"+meta.row).addClass('CellComment');
			    	$("#salesOrerDiv"+meta.row).html(clientName + "-" + clientPoNumber);
					return '<select type="text"  class="form-control PositionofTextbox salesOrderDropdown" id="salesOrderDropdown' + meta.row + '" readonly="readonly" style="width:100%;padding:0" ><option  value="' + soId + '">' + clientName + "-" + clientPoNumber + '</option></select><span id="salesOrerDiv' + meta.row + '" ></span>';
					
				}
			},
			{
				"title": "Desc.",
				"mData": "description",
				"class": "txtUnWrap CellWithComment",
				"width":"12%",
				render: function (mData, type, row, meta) {
					var desc = null;
					var slNo=null;
					$.each(salesItemList, function (index, value) {
						if (mData == value.id) {
							desc = value.description;
							slNo=value.slNo;
							
						}
					});
					$("#discriptionDiv"+meta.row).addClass('CellComment');
			    	$("#discriptionDiv"+meta.row).html(desc);
					return '<select type="text"  class="form-control PositionofTextbox description" id="descriptionDropdown' + meta.row + '" readonly="readonly"  name="items[' + meta.row + '].description"  style="width:100%;padding:0" ><option  value="' + mData + '">' +slNo+". "+ desc + '</option></select><span id="discriptionDiv' + meta.row + '" ></span>';
				}
			},
		

			{
				"title": "Model No",
				"mData": "modelNo",
				"class":"CellWithComment",
				"width":"10%",
				render: function (mData, type, row, meta) {
					var modelNum = null;
					$.each(itemList, function (index, value) {
						if (mData == value.id) {
							modelNum = value.model;
						}
					});
					$("#modelDiv"+meta.row).addClass('CellComment');
			    	$("#modelDiv"+meta.row).html(modelNum);
					return '<select class="form-control PositionofTextbox modelNo" readonly="readonly" id="modelNo' + meta.row + '"  name="items[' + meta.row + '].modelNo" style="width:100%;padding:0" ><option  value="' + mData + '">' + modelNum + '</option></select><span id="modelDiv' + meta.row + '" ></span>';
				}
			},
			{
				"title": "PO Desc.",
				"mData": "poDescription",
				"width":"10%",
				render: function (mData, type, row, meta) {

					return '<input type="text"  class="form-control PositionofTextbox poDescription" readonly="readonly" id="poDescription' + meta.row + '"  name="items[' + meta.row + '].poDescription" value="' + mData + '" style="width:100%" />';
				}
			},
			{
				"title": "HSN",
				"mData": "hsnCode",
				"width":"6%",
				render: function (mData, type, row, meta) {
					return '<input type="text"  class="form-control PositionofTextbox hsnCode" readonly="readonly" id="hsnCode' + meta.row + '"  name="items[' + meta.row + '].hsnCode" value="' + mData + '" style="width:100%" />';
				}
			},

			{
				"title": "Qty",
				"mData": "quantity",
				"class": "qtyData",
				"width":"8%",
				render: function (mData, type, row, meta) {
					if(highestVersion == version){
						return '<input type="text"  class="form-control PositionofTextbox qtyInput" name="items[' + meta.row + '].quantity" value="' + mData + '" id="newQty' + meta.row + '" style="width:100%" />';
					}else{
					return '<input type="text"  class="form-control PositionofTextbox qtyInput" readonly="readonly" name="items[' + meta.row + '].quantity" value="' + mData + '" id="newQty' + meta.row + '" style="width:100%" />';
				}}
			},
			{
				"class": "hideTd",
				render: function (mData, type, row, meta) {
					return '<input type="hidden"  value="'+row.quantity+'" id="remQty' + meta.row + '" style="width:100%" />';
				}
			},

			{
				"title": "Unit",
				"mData": "description",
				"width":"5%",
				render: function (mData, type, row, meta) {
					var unitName = null;
					$.each(salesItemList, function (index, value) {
						if (mData == value.id) {
							unitName = value.item_units.name;
						}
					});
					return '<input type="text"  class="form-control PositionofTextbox unit" readonly="readonly" value="' + unitName + '" id="unit' + meta.row + '" style="width:100%"/>';
				}
			},
			{
				"title": "U.Price",
				"mData": "unitPrice",
				"width":"6%",
				render: function (mData, type, row, meta) {
					var mData = commaSeparateNumber(mData);
					if(highestVersion == version){
						return '<input type="text"  class="form-control PositionofTextbox upInput alignright unitprice" name="items[' + meta.row + '].unitPrice" value="' + mData + '" id="newUPrice' + meta.row + '" />';
					}else{
					return '<input type="text"  class="form-control PositionofTextbox upInput alignright unitprice" readonly="readonly" name="items[' + meta.row + '].unitPrice" value="' + mData + '" id="newUPrice' + meta.row + '" />';
				
					}}
			},
			{
				"class": "hideTd",
				render: function (mData, type, row, meta) {

					return '<input type="hidden"  class="form-control PositionofTextbox" value="" id="salesUPrice' + meta.row + '" style="width:100%;"/>';
				}
			},
			{
				"title": "Tax %",
				"mData": "modelNo",
				"width":"5%",
				render: function (mData, type, row, meta) {
					var tax = null;
					$.each(itemList, function (index, value) {
						if (mData == value.id) {
							tax = value.gst;
						}
					});
					return '<input type="text" class="form-control PositionofTextbox tax alignright"  value="' + tax + '" id="tax' + meta.row + '"  readonly="readonly" style="width:100%;"/>';
				}
			},
			{
				"title": "Del. Date",
				"mData": "delivaryDate",
				"width":"8%",
				render: function (mData, type, row, meta) {
					var delDate;
					if(mData==null){
						delDate=""
					}else{
						var delDateFormat=mData.split(" "); 
						delDate=delDateFormat[0];
					}
					return '<input type="text" class="form-control PositionofTextbox delivaryDate"  id="delivaryDate' + meta.row + '" name="items[' + meta.row + '].delivaryDate" value="' + delDate + '" style="width:100%;" autoComplete="off"/>';
				}
			},
			{
				"title": "LR No.",
				"mData": "lrNum",
				"width":"6%",
				render: function (mData, type, row, meta) {
					var lrNo;
					if(mData==null){
						lrNo=""
					}else{
						lrNo=mData;
					}
					return '<input type="text" class="form-control PositionofTextbox lrNum"  id="lrNum' + meta.row + '" name="items[' + meta.row + '].lrNum" value="'+lrNo+'" style="width:100%;"/>';
				}
			},

			{
				"title": "Amt.",
				"mData": "amount",
				"width":"6%",
				render: function (mData, type, row, meta) {
					var mData = commaSeparateNumber(mData);
					return '<input type="text" class="form-control PositionofTextbox amtInput alignright" name="items[' + meta.row + '].amount" value="' + mData + '" id="amount' + meta.row + '"  readonly="readonly" style="width:100%;"/>';
				}
			},
			{
				"mData": "po_item_id",
				"class": "hideTd",
				render: function (mData, type, row, meta) {
					return '<input type="hidden" name="items[' + meta.row + '].purchase_item_id" value="' + row.purchase_item_id + '"/>';
				}
			},


			{
				"title": '<i class="add fa fa-plus-square fa-2x text-center mx-auto" aria-hidden="true"></i>',
				"class": "styleOfSlNo",
				render: function (mData, type, row, meta) {
					return '<i class="deleteButton purchaseDeleteButton fa fa-trash " aria-hidden="true"></i>';
				}
			}
			
		]
	});


}


$(document).on("change", "#version", function () {
	var poNumber = $('#poNumberEdit').text();
	var version = $(this).val();
	var versionIndex = $(this).prop('selectedIndex')
	window.location = pageContext + "/purchase/view?poNumber=" + poNumber + "&&version=" + version+ "&versionIndex="+versionIndex;

});
function checkForDcInvoiceCreated(poItemId,salesItemId,index){
	var row=index+1;
	$.ajax({
		type : "POST",  
		url : api.CHECK_DC_INVOICE_EXIST +"?salesItemId="+salesItemId,
		success : function(response) {
			if(response == true){
				$("#poTableEdit >tbody").find("tr:eq("+index+")").find("input,select,button").attr("disabled",true);
				$("#poTableEdit >tbody").find("tr:eq("+index+")").find('.deleteButton ').addClass('ui-state-disabled');
			}else{
				$("#poTableEdit >tbody").find("tr:eq("+index+")").find("input,select,button").attr("disabled",false);
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
}


$(document).ready(function () {
	if(role=="STORE USER"){
		 $("#Home").on("click", function (event) {
			    event.preventDefault();
			});
			
		$("#savePurchaseOrder").prop("disabled",true);
	}
	$( ".delivaryDate" ).datepicker({ dateFormat: 'dd-mm-yy', minDate: 0, defaultDate: null});
	 $(".delivaryDate").on("cut copy paste", function (e) {
		    e.preventDefault();
		  });
	 $(".delivaryDate").on("keydown", function (e) {
		    if ((e.ctrlKey || e.metaKey) && (e.key === "v" || e.key === "c" || e.key === "x")) {
		      e.preventDefault();
		    }
		  });
	
	/**Remove row from table**/
	$("#poTableEdit").on("click", ".deleteButton", function () {
		var purchaseItemId = $(this).closest('tr').find('input[type=hidden]').val();
		var row=$(this).closest("tr");
		if(purchaseItemId==""){
			$(this).closest("tr").remove();
			settingNameAndId();
			calculatePoTotals();
		}else{
			deletePurchaseItem(purchaseItemId,row);
			
		}
		
		
	});


	/**on click of add button in po edit page add rows**/
	$("#poTableEdit").on("click", ".add", function () {
		$("#itemTable tbody").empty();
		$(".hiddenId").css("display", "none");
		$(".salesQtyData").css("display", "none");
		$(".dataTables_empty").closest("tr").remove();
		var newRow = $("<tr>");
		var row = $('#poTableEdit > tbody  > tr').length;
		var rowCount = row + 1;
		var arraycount = rowCount - 1;
		var previousRow = arraycount - 1;
		/**Sales order validation***/
		var selectedSOid = $("#salesOrderDropdown" + previousRow).val();
		/*if (selectedSOid == "") {
			$.error("Plaese select the sales order before adding purchase Item ");
			$("#salesOrderDropdown" + previousRow).addClass('border-color');
			return false;
		}*/
		$("#salesOrderDropdown" + previousRow).removeClass('border-color');

		/***Description Validation**/
		var descriptionValue = $("#descriptionDropdown" + previousRow).val();
		/*if (descriptionValue == "") {

			$.error("Please select the description before adding purchase Item ");
			$("#descriptionDropdown" + previousRow).addClass('border-color');
			return false;
		}*/
		$("#descriptionDropdown" + previousRow).removeClass('border-color');
		var columns = "";
		columns += '<td class="styleOfSlNo">' + rowCount + '</td>';

		columns += '<td class="hideTd"> <input type="hidden" class="form-control PositionofTextbox sales_item_id"  id="sales_item_id' + arraycount + '" style="padding: 0;"/></td>'
		columns += '<td width="10%" class=" txtUnWrap"> <select class="form-control PositionofTextbox salesOrderDropdown salect2 dropdownMarginTop"  id="salesOrderDropdown' + arraycount + '" style="padding: 0;"> <option value="">Select Client Name:</option></select>   </td>';
		columns += '<td width="12%" class=" txtUnWrap"> <select class="form-control PositionofTextbox description select2 dropdownMarginTop" name="items[' + arraycount + '].description" id="descriptionDropdown' + arraycount + '" style="padding: 0;"> <option value="">Select Description:</option></select>   </td>';
		/*columns += '<td width="12%"><input type="text" class="form-control PositionofTextbox poDescription" id="poDescription' + arraycount + '" name="items[' + arraycount + '].poDescription" /></td>';*/
		columns += '<td width="10%"><select class="form-control PositionofTextbox modelNo select2 dropdownMarginTop"  id="modelNo' + arraycount + '" name="items[' + arraycount + '].modelNo"  style="padding: 0;"> <option value="">Select ModelNo:</option></select> </td>';
		columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox poDescription" id="poDescription' + arraycount + '" name="items[' + arraycount + '].poDescription" /></td>';
		columns += '<td width="6%"><input type="text" class="form-control PositionofTextbox hsnCode" id="hsnCode' + arraycount + '" name="items[' + arraycount + '].hsnCode" /></td>';
		columns += '<td width="8%" class="qtyData"><input type="text" class="form-control PositionofTextbox qty qtyInput" id="newQty' + arraycount + '" name="items[' + arraycount + '].quantity" "/></td>';
		columns += '<td class="salesQtyData hideTd"><input type="hidden" class="form-control PositionofTextbox" id="remQty' + arraycount + '"/></td>';
		columns += '<td width="5%"><input type="text" class="form-control PositionofTextbox unit" id="unit' + arraycount + '"readonly="readonly"/></td>';
		columns += '<td width="6%"><input type="text" class="form-control PositionofTextbox unitPrice upInput" style= "text-align: right;" id="newUPrice' + arraycount + '" name="items[' + arraycount + '].unitPrice"/></td>';
		columns += '<td class="hideTd"><input type="hidden" class="form-control PositionofTextbox" id="salesUprice' + arraycount + '"/></td>';
		columns += '<td width="5%"><input type="text" class="form-control PositionofTextbox tax" style= "text-align: right;" id="tax' + arraycount + '" readonly="readonly"/></td>';
		columns += '<td width="8%"><input type="text" class="form-control PositionofTextbox delivaryDate" style= "text-align: right;" id="delivaryDate' + arraycount + '" name="items[' + arraycount + '].delivaryDate" autoComplete="off"/></td>';
		columns += '<td width="6%"><input type="text" class="form-control PositionofTextbox lrNum" style= "text-align: right;" id="lrNum' + arraycount + '" name="items[' + arraycount + '].lrNum"/></td>';
		columns += '<td width="6%"><input type="text" class="form-control PositionofTextbox amount amtInput" style= "text-align: right;" id="amount' + arraycount + '" name="items[' + arraycount + '].amount" readonly="readonly"/></td>';
		/*columns += '<td class="hideTd"><input type="hidden" class="form-control PositionofTextbox" name="items[' + arraycount + '].purchase_item_id"/></td>';*/

		columns += '<td align="center" class=" styleOfSlNo"><i class="deleteButton purchaseDeleteButton fa fa-trash" aria-hidden="true"></i></td>';
		newRow.append(columns);
		$("#poTableEdit").append(newRow);

		$( ".delivaryDate" ).datepicker({ dateFormat: 'dd-mm-yy', minDate: 0});
		$("#salesOrderDropdown" + arraycount).select2({ dropdownAutoWidth: true });
		$("#descriptionDropdown" + arraycount).select2({ dropdownAutoWidth: true });
		$("#modelNo" + arraycount).select2({ dropdownAutoWidth: true });
		//$("#salesOrderDropdown" + previousRow).attr("disabled", true);
		//$("#descriptionInput" + previousRow).attr("readonly", "readonly");

		itemName = itemList.map(({ itemName }) => itemName);
		$(".poDescription").autocomplete({
			source: itemName
		});
		/*unitName = unitsList.map(({ name }) => name);
		$(".unit").autocomplete({
			source: unitName
		});*/


		var selectedSOid = $("#salesOrderDropdown" + previousRow).val();
		$.each(SalesOrder, function (index, value) {
			var clientPoNumber;
			var clientName = value.party.partyName;
		clientName = clientName.split(' ');
		if (clientName.length > 100) {
			clientName.splice(100);
		}
		clientName = clientName.join(' ');
		var length = $.trim(clientName).length;
		if (length > 25) {
			clientName = $.trim(clientName).substring(0, 25) + "....";
		}
		if(value.clientPoNumber == null || value.clientPoNumber == undefined ){
			clientPoNumber = ' ';
		}else{
			clientPoNumber = value.clientPoNumber;
		}
			$("#salesOrderDropdown" + arraycount).append('<option value=' + value.id + '>' + clientName + "-" +  clientPoNumber + '</option>');
		});
		$("#salesOrderDropdown" + arraycount).val(selectedSOid);
		$(document).on("change", "#salesOrderDropdown" + arraycount, function () {
			$("#descriptionDropdown" + arraycount).children("option").filter(":not(:first)").remove();
			//$("input[name='items[" +arraycount+ "].hsnCode']").val("");
			$("input[name='items[" + arraycount + "].modelNo']").val("");
			$("input[name='items[" + arraycount + "].quantity']").val("");
			$("#unit"+arraycount).val("");
			$("input[name='items[" + arraycount + "].unitPrice']").val("");
			$("input[name='items[" + arraycount + "].amount']").val("");

			var soId = $(this).val();
			getSalesOrdertbySalesOrderId(soId);
		});

		/*$.each(itemList, function (index, value) {
			$("#modelNo" + arraycount).append('<option value=' + value.id + '>' + value.model + '</option>');
		});*/
		/**On change of description in every row reset other values**/
		$(document).on("change", "#descriptionDropdown" + arraycount, function () {
			/**reset input values*/
			$("input[name='items[" +arraycount+ "].hsnCode']").val("");
			$("input[name='items[" + arraycount + "].modelNo']").val("");
			$("input[name='items[" + arraycount + "].quantity']").val("");
			$("input[name='items[" + arraycount + "].unitPrice']").val("");
			$("#unit"+arraycount).val("");
			$("input[name='items[" + arraycount + "].amount']").val("");
			//on change of description dropdown remove the error border from descriptionDropdown, qty and unit price
			$("input[name='items[" + arraycount + "].quantity']").removeClass("border-color");
			$("input[name='items[" + arraycount + "].unitPrice']").removeClass("border-color");
			$(this).removeClass('border-color');
			let index = $(this).closest('tr').index();
			var salesItemId = $(this).val();
			populateModelList(salesItemId,true,index);
		});

		/**On change of model No in every row reset other values**/
		$(document).on("change", "#modelNo" + arraycount, function () {
			/**reset input values*/
			var model = 	$("#modelNo" + arraycount).val();
			$("input[name='items[" + arraycount + "].hsnCode']").val("");

			$(this).removeClass('border-color');
		});
		getSalesOrdertbySalesOrderId(selectedSOid);
		getSalesItemtbySalesItemId();
		getHsnUnitPriceByItemId();
	});



});

$(document).ready(function () {
	calculatePoTotals();
});

//delete row from edit purchase order
function deletePurchaseItem(id,row) {

	$.ajax({
		type: "POST",
		url: api.DELETE_PURCHASE_ITEM + "?id=" + id,
		success: function (response) {
			if(response==true){
				row.remove();
				//window.location.reload();
				settingNameAndId();
			}else{
				$.error("GRN or Dc created for this item");
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
}
function settingNameAndId(){
	/***Setting name and id to remaining rows**/
	$('tbody').find('tr').each(function (index) {
		let prev = index - 1;
		let firstTdElement = $(this).find('td')[0];
		$(firstTdElement).text(index + 1);
		let hiddenSalesItemId = $(this).find('td')[1];
		$(hiddenSalesItemId).find('input').attr('id', 'sales_item_id' + index);
		$(hiddenSalesItemId).find('input').attr('name', 'items[' + index + '].sales_item_id');
		let salesOrderTdElement = $(this).find('td')[2];
		$(salesOrderTdElement).find('select').attr('id', 'salesOrderDropdown' + index);
		let secondTdElement = $(this).find('td')[3];
		$(secondTdElement).find('select').attr('name', 'items[' + index + '].description');
		$(secondTdElement).find('select').attr('id', 'descriptionDropdown' + index);
		let thirdTdElement = $(this).find('td')[5];
		$(thirdTdElement).find('input').attr('name', 'items[' + index + '].poDescription');
		$(thirdTdElement).find('input').attr('id', 'poDescription' + index);
		let forthTdElement = $(this).find('td')[6];
		$(forthTdElement).find('input').attr('name', 'items[' + index + '].hsnCode');
		$(forthTdElement).find('input').attr('id', 'hsnCode' + index);
		let fifthTdElement = $(this).find('td')[4];
		$(fifthTdElement).find('select').attr('name', 'items[' + index + '].modelNo');
		$(fifthTdElement).find('select').attr('id', 'modelNo' + index);
		let sixthTdElement = $(this).find('td')[7];
		$(sixthTdElement).find('input').attr('name', 'items[' + index + '].quantity');
		$(sixthTdElement).find('input').attr('id', 'newQty' + index);
		let hiddenQtyElement = $(this).find('td')[8];
		$(hiddenQtyElement).find('input').attr('id', 'remQty' + index);
		let seventhTdElement = $(this).find('td')[9];
		$(seventhTdElement).find('input').attr('name', 'items[' + index + '].unit');
		$(seventhTdElement).find('input').attr('id', 'unit' + index);
		let eighthTdElement = $(this).find('td')[10];
		$(eighthTdElement).find('input').attr('name', 'items[' + index + '].unitPrice');
		$(eighthTdElement).find('input').attr('id', 'newUPrice' + index);
		let hiddenUnitPriceElement = $(this).find('td')[11];
		$(hiddenUnitPriceElement).find('input').attr('id', 'salesUPrice' + index);
		let ninthTdElement = $(this).find('td')[12];
		$(ninthTdElement).find('input').attr('id', 'tax' + index);
		let tenthTdElement = $(this).find('td')[13];
		$(tenthTdElement).find('input').attr('name', 'items[' + index + '].amount');
		$(tenthTdElement).find('input').attr('id', 'amount' + index);
		let eleventhTdElement = $(this).find('td')[14];
		$(eleventhTdElement).find('input').attr('name', 'items[' + index + '].purchase_item_id');
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
var modelList;
function populateModelList(salesItemid,value,index){
	
	$.ajax({
		type: "GET",
		url: api.DESIGNITEMS_BY_SALESITEMID + "?salesItemid=" + salesItemid,
		success: function (response) {
			modelList =response;
			if(response.length==0){
				$.error("Please assign design item");
			}

			if(value){
				

			var row = $('table > tbody  > tr').length;
			tabIndex = index;
			$("#modelNo"+tabIndex).empty();
			var itemId;
			$.each(response, function (index, value) {
				
				var modelNo = value.model;
				modelNo = modelNo.split(' ');
				if (modelNo.length > 100) {
					modelNo.splice(100);
				}
				modelNo = modelNo.join(' ');   
				var length = $.trim(modelNo).length;
				if (length > 25) {
					modelNo = $.trim(modelNo).substring(0, 25) + "....";
				}
				$("#modelNo"+tabIndex).append('<option value=' + value.itemId + '>' + modelNo + '</option>');
				$("#salesQuantity" + tabIndex).val(value.quantity);
				$("#totalQty" + tabIndex).val(value.quantity);
				$("#newQuantity" + tabIndex).val(value.quantity);
				$("#newQty" + tabIndex).val(value.quantity);
				$("#remQty" + tabIndex).val(value.quantity);
		        itemId = value.itemId;
				//$("input[name='items[" + tabIndex + "].quantity']").val(value.quantity);
			
			});

			
			$("#modelNo"+tabIndex).val(itemId).trigger('change');
			checkPurchaseItemExists(itemId,salesItemid,tabIndex);
		}},  
		complete:function(resp){
			
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	

	
	});
	//return modelList;
}

function getLastPriceForThisModel(modelNumberPrese){
	currModel = modelNumberPrese;
	$.ajax({
		type: "GET",
		url: api.POITEM_LASTPRICE + "?modelNo=" + modelNumberPrese,
		success: function (response) {
			if(response!=null && response.length>0){
				$("#itemsModal").modal("show");
				loadTable(response);
			//	$.success("Last Purchased Price is : Rs "+response);
			}
			
//			$.each(response,function(index,value){
//			var designItems = "<tr><td width='40%'>" + value.poDescription + "</td>" +
//			"<td width='30%'>" + value.unitPrice + "</td>" +
//			"<td width='30%'><input type='text' class='form-control PositionofTextbox'  readonly='readonly' value='"+value.purchaseOrder.created+"'/></td></tr>";
//			$("#itemTable tbody").append(designItems);
//			});
		//	$("#itemTable").empty();
			
			
		
}
	});
}


function getQtyForThisModel(modelNumberPrese,index,salesItemId){
	currModel = modelNumberPrese;
	
	$.ajax({
		type: "GET",
		url: api.MODEL_QTY_DETAILS + "?modelNo=" + modelNumberPrese+"&salesItemId="+salesItemId,
		success: function (response) {
				$("#qtyModal").modal("show");
				//loadQtyTable(response,totalQty);
				$("#qtyTable tbody").empty();
				var details = "<tr><td width='50%'> " + response.orderedQty + "</td>" +
				"<td width='50%'> " + response.deliveredQty + "</td></tr>";
				$("#qtyTable tbody").append(details);
			
			
		
		}
	});
}

var datatable = null;
function loadTable(response){
	
	datatable = $('#itemTable').DataTable({

		"data": response,
		/* lengthChange: false,*/
		"destroy": true,
		//"order": [[ 5, "desc" ]],
		
		
		"columns": [

			{
				"title": "Description",
				"data": "descript",
				"defaultContent": "",
				"width":"40%",
				
				render : function(data, type, row) {
					return row.poDescription;

				}
				

			},
		
			{
				"title": "Unit Price",
				"data": "unitPrice",
				"defaultContent": "",
				"width":"30%",
			},
		
			{
				"title": "Po Date",
				"data": "Date",
				"width":"10%",
				render : function(data, type, row) {
					var newdate = moment(new Date(row.purchaseOrder.created)).format("YYYY-MM-DD HH:mm:ss") ;
						return  newdate; 
				}

			},
			{
				"title": "Vendor",
				"data" : "vendor",
				"width":"20%",
				render : function(data, type, row) {
					
					return  data.length > 35 ?
							data.substr( 0, 35 ) +'...' :
							data;

				}

			}
			

		
			

		]

	})/*.buttons().container().appendTo( $('#itemMasterList_length') )*/;
}
function getAutoPopulateSalesOrdertbySalesOrderId(soId,row,soText){
	
	var className = "po";
	$.ajax({
	    Type:'GET',
	    url : api.SALES_LIST_BY_SOID+"?id="+soId+"&&className="+className,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	var arraycount=row;
	    	var rowCount=arraycount+1;
	    	$("#modelNo" + row).children("option").filter(":not(:first)").remove();
			$("#descriptionDropdown" + row).children("option").filter(":not(:first)").remove();
	    	$.each(response,function(index,value){
	    		var description = value.description;
				description = description.split(' ');
				if (description.length > 100) {
					description.splice(100);
				}
				description = description.join(' ');
				var length = $.trim(description).length;
				var soNum=value.soNum;
				var client = value.client;
				var clientPo=value.clientPo;
				
	    		if(value.designItems.length==0){
	    			if(value.item_units.name=="Heading"){
	    				var slNo=value.slNo;
		    			var itemId="";
		    			var modelNum = "";
		    			var qty="";
		    			var itemId ="";
		    			var tax="";
	    				var unitName="";
	    				var poDescription="";
	    				var hsn= "";
	    			}else{
	    				var slNo=value.slNo;
	    				var itemId="";
		    			var modelNum = "";
		    			var qty="";
		    			var itemId ="";
		    			var tax="";
	    				var unitName="";
	    				var poDescription="";
	    				var hsn= "";
	    			}
	    			 
	    			var soItems = '<tr><td width="5%" class="styleOfSlNo">' + rowCount + '</td>'+
    				'<td width="15%"> <select class="form-control select2 PositionofTextbox salesOrderDropdown dropdownMarginTop" name="salesOrder" id="salesOrderDropdown' + arraycount + '" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;"> <option value="' + soNum + '">' + client + '-' +  clientPo + '</option></select> </td>'+	
    				'<td width="11%"> <select class="form-control select2 PositionofTextbox  descriptionDropdown dropdownMarginTop" name="items[' + arraycount + '].description" id="descriptionDropdown' + arraycount + '" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="'+value.id+'">' +value.slNo+'.' +description + '</option></select>   </td>'+
	    			'<td width="10%"><select class="form-control PositionofTextbox modelNo dropdownMarginTop" id="modelNo' + arraycount + '" name="items[' + arraycount + '].modelNo" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="'+itemId+'">'+modelNum+'</option></select>   </td>'+
	    			'<td width="10%"><input type="text" class="form-control PositionofTextbox poDescription" id="poDescription' + arraycount + '" name="items[' + arraycount + '].poDescription" value="'+poDescription+'"/></td>'+
	    			'<td width="8%"><input type="text" class="form-control PositionofTextbox hsnCode" id="hsnCode' + arraycount + '" name="items[' + arraycount + '].hsnCode" value="'+hsn+'"/><span id="hsnCodeDiv' + arraycount + '"></span></td>'+
	    			'<td width="10%"><input type="text" class="form-control PositionofTextbox qty qtyInput" id="newQuantity' + arraycount + '" name="items[' + arraycount + '].quantity" value="'+qty+'"/><span id="qtyDiv' + arraycount + '"></span></td>'+
	    			'<td class="hideTd"><input type="hidden" class="salesQtyInput" id="salesQuantity' + arraycount + '" value="'+qty+'"/></td>'+
	    			'<td width="6%"><input type="text" class="form-control PositionofTextbox unit" readonly="readonly" id="unit' + arraycount + '"  value="'+unitName+'"/><span id="unitDiv' + arraycount + '"></span></td>'+
	    			'<td width="8%"><input type="text" class="form-control PositionofTextbox unitPrice upInput alignright" id="newPrice' + arraycount + '" name="items[' + arraycount + '].unitPrice"/><span id="unitPriceDiv' + arraycount + '"></span></td>'+
	    			'<td class="hideTd"><input type="hidden" class="salesUnitPrice" value="" id="salesUnitPrice' + arraycount + '" /></td>'+
	    			'<td width="7%"><input type="text" class="form-control PositionofTextbox tax alignright"  id="tax' + arraycount + '"  readonly="readonly"  value="'+tax+'"/></td>'+
	    			'<td width="10%"><input type="text" class="form-control PositionofTextbox amount amtInput alignright"  id="amount' + arraycount + '" name="items[' + arraycount + '].amount" readonly="readonly"/><span id="amountDiv' + arraycount + '"></span></td>'+
	    			'<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>'+
	    			'<td class="hideTd"><input type="hidden" class="totalQty" id="totalQty' + arraycount + '" value="'+qty+'"/></td></tr>';
		    		$('#poTable tbody').append(soItems);
		    		
		    		arraycount++;
		    		rowCount++;
	    		}else{
	    			$.each(value.designItems,function(i,v){
		    			
		    			
		    				var slNo=value.slNo;
			    			var itemId=v.itemId;
			    			var modelNum = v.model;
			    			var qty=v.quantity;
			    			var itemId = v.itemMasterId;
			    			var tax=v.tax;
		    				var unitName=v.unit;
		    				var poDescription=v.itemName;
		    				var hsn= v.hsnCode;
		    				

		    				var soItems = '<tr><td width="5%" class="styleOfSlNo">' + rowCount + '</td>'+
		    				'<td width="15%"> <select class="form-control select2 PositionofTextbox salesOrderDropdown dropdownMarginTop" name="salesOrder" id="salesOrderDropdown' + arraycount + '" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;"> <option value="' + soNum + '">' + client + '-' +  clientPo + '</option></select> </td>'+	
		    				'<td width="11%"> <select class="form-control select2 PositionofTextbox  descriptionDropdown dropdownMarginTop" name="items[' + arraycount + '].description" id="descriptionDropdown' + arraycount + '" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="'+value.id+'">' +value.slNo+'.' +description + '</option></select>   </td>'+
			    			'<td width="10%"><select class="form-control PositionofTextbox modelNo dropdownMarginTop" id="modelNo' + arraycount + '" name="items[' + arraycount + '].modelNo" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="'+itemId+'">'+modelNum+'</option></select>   </td>'+
			    			'<td width="10%"><input type="text" class="form-control PositionofTextbox poDescription" id="poDescription' + arraycount + '" name="items[' + arraycount + '].poDescription" value="'+poDescription+'"/></td>'+
			    			'<td width="8%"><input type="text" class="form-control PositionofTextbox hsnCode" id="hsnCode' + arraycount + '" name="items[' + arraycount + '].hsnCode" value="'+hsn+'"/><span id="hsnCodeDiv' + arraycount + '"></span></td>'+
			    			'<td width="10%"><input type="text" class="form-control PositionofTextbox qty qtyInput" id="newQuantity' + arraycount + '" name="items[' + arraycount + '].quantity" value="'+qty+'"/><span id="qtyDiv' + arraycount + '"></span></td>'+
			    			'<td class="hideTd"><input type="hidden" class="salesQtyInput" id="salesQuantity' + arraycount + '" value="'+qty+'"/></td>'+
			    			'<td width="6%"><input type="text" class="form-control PositionofTextbox unit" readonly="readonly" id="unit' + arraycount + '"  value="'+unitName+'"/><span id="unitDiv' + arraycount + '"></span></td>'+
			    			'<td width="8%"><input type="text" class="form-control PositionofTextbox unitPrice upInput alignright" id="newPrice' + arraycount + '" name="items[' + arraycount + '].unitPrice"/><span id="unitPriceDiv' + arraycount + '"></span></td>'+
			    			'<td class="hideTd"><input type="hidden" class="salesUnitPrice" value="" id="salesUnitPrice' + arraycount + '" /></td>'+
			    			'<td width="7%"><input type="text" class="form-control PositionofTextbox tax alignright"  id="tax' + arraycount + '"  readonly="readonly"  value="'+tax+'"/></td>'+
			    			'<td width="10%"><input type="text" class="form-control PositionofTextbox amount amtInput alignright"  id="amount' + arraycount + '" name="items[' + arraycount + '].amount" readonly="readonly"/><span id="amountDiv' + arraycount + '"></span></td>'+
			    			'<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>'+
			    			'<td class="hideTd"><input type="hidden" class="totalQty" id="totalQty' + arraycount + '" value="'+qty+'"/></td></tr>';
				    		$('#poTable tbody').append(soItems);
				    		
				    		arraycount++;
				    		rowCount++
		    			
		    		})	
	    		}
	    		
	    	})
	    	$("#poTable tbody").find("tr:eq('"+row+"')").remove()
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
	//$("#poTable tbody").find("tr:eq('"+row+"')").remove()
}
