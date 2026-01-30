var itemName;
var rowSpanId;
var ttlStockQty;
var rowId;
var totalSupplier;
var makeTable;
var stockSummaryByItem = {};
var supplierSummaryByItem = {};
var clientSummaryByItem = {};
var costSummaryByItem = {};
function precomputeItemMasterAggregations() {
	if(window.itemMasterLazyConfig && window.itemMasterLazyConfig.enabled){
		stockSummaryByItem = window.itemMasterLazyConfig.stockSummaryMap || {};
		supplierSummaryByItem = window.itemMasterLazyConfig.supplierSummaryMap || {};
		clientSummaryByItem = window.itemMasterLazyConfig.clientSummaryMap || {};
		costSummaryByItem = window.itemMasterLazyConfig.costSummaryMap || {};
		return;
	}

	stockSummaryByItem = {};
	supplierSummaryByItem = {};
	clientSummaryByItem = {};
	costSummaryByItem = {};

	if(Array.isArray(allStocksList)){
		allStocksList.forEach(function (stock) {
			if(!stock || !stock.itemMaster){ return; }
			var itemId = stock.itemMaster.id;
			var currentQty = stockSummaryByItem[itemId] || 0;
			stockSummaryByItem[itemId] = currentQty + (parseFloat(stock.quantity) || 0);

			if(stock.party && stock.quantity){
				var clients = clientSummaryByItem[itemId] || [];
				if(parseFloat(stock.quantity) !== 0){
					clients.push(stock.party.partyName);
				}
				clientSummaryByItem[itemId] = clients;
			}
		});
	}

	if(Array.isArray(allSupplierslist)){
		allSupplierslist.forEach(function (supplier) {
			if(!supplier || !supplier.itemMaster){ return; }
			var itemId = supplier.itemMaster.id;
			var currentCount = supplierSummaryByItem[itemId] || 0;
			supplierSummaryByItem[itemId] = currentCount + 1;

			var costCollection = costSummaryByItem[itemId] || [];
			var updatedDate = supplier.updated;
			if(updatedDate){
				var normalized = updatedDate.replaceAll("-","/").replace(/(\d\d)\/(\d\d)\/(\d{4})/, "$3-$2-$1").replace("/","-");
				var timestamp = Date.parse(normalized);
				costCollection.push(timestamp + "$" + supplier.costPrice);
				costSummaryByItem[itemId] = costCollection;
			}
		});
	}

	Object.keys(costSummaryByItem).forEach(function (itemId) {
		var priceList = costSummaryByItem[itemId];
		if(Array.isArray(priceList) && priceList.length > 0){
			priceList.sort().reverse();
			costSummaryByItem[itemId] = priceList[0].split("$")[1];
		}else{
			costSummaryByItem[itemId] = "";
		}
	});

	Object.keys(stockSummaryByItem).forEach(function (itemId) {
		var qty = stockSummaryByItem[itemId];
		if(typeof qty === "number"){
			stockSummaryByItem[itemId] = Math.round(qty * 100) / 100;
		}
	});
}
//var userName;
$(document).ready(function () {
  

	$("#SalesVal").hide();
	$(".amount").attr("readonly", "readonly");
	$(".total").attr("readonly", "readonly");
	$(".gst").attr("readonly", "readonly");
	$(".grandTotal").attr("readonly", "readonly");
	$(".preferredCheckBox").attr('readonly', 'readonly');
	getAmount();
	checkForEmptyValidation();
	addItemMaster();
	deleteItemMaster();
	//getItemMaster();
	getMakeList();
	addMake();
    
	precomputeItemMasterAggregations();
	loadItemTable();
	if(role == "ITEMMASTER" || user=="praveen" || role=="STORE"||user=="abhishek" || user=="jagadish"|| user=="rakesh"|| user=="sushma"||user=="savitha"||user=="santosh"){
		$("#saveItemMaster").attr("disabled",false);
		$("#saveMake").attr("disabled",false);
		$("#addMake").show();
	}else{
		$("#saveItemMaster").attr("disabled",true);
		$("#saveMake").attr("disabled",true);
		$("#addMake").hide();
		
	}
	
	$(document).on("click","#addMake",function(){
	    $("#makeHeader").html("Add/Delete Make");
		$("#saveMake").html("Save")
		$("#makeForm")[0].reset();
		$("#makeForm").find('input[name=id]').val("");
		$("#makeModal").modal();
		getMakeList();
	}) 
	
	$("#makeResetBtn").on("click",function(e){
		$("#makeHeader").html("Add/Delete Make");
		$("#saveMake").html("Save");
		$("#makeModal").modal("hide");	
		$("#makeForm")[0].reset();
		$("#makeForm").find('input[name=id]').val("");
	});	
	$('#toolTracker').change(function() {
	    if ($(this).is(':checked')) {
	      $('#companyAssets').prop('disabled', true);
	    } else {
	      $('#companyAssets').prop('disabled', false);
	    }
	  });

	  $('#companyAssets').change(function() {
	    if ($(this).is(':checked')) {
	      $('#toolTracker').prop('disabled', true);
	    } else {
	      $('#toolTracker').prop('disabled', false);
	    }
	  });
	
	
	$(document).on('submit', '#itemMasterForm1', function (e) {
		if($("input[type='checkbox']#toolTracker").is(':checked')) {		
		      $("#toolTracker").val("true");  
		} else{
			$("#toolTracker").val("false");
		} 
		
		if($("input[type='checkbox']#companyAssets").is(':checked')) {		
		      $("#companyAssets").val("true");  
		} else{
			$("#companyAssets").val("false");
		} 
		
		let itemModelNo = $("#itemModelNo").val().trim();
		let itemDescription = $("#itemDescription").val();
		let itemHSNCode = $("#itemHSNCode").val();
		let gst = $("#gst").val();
		let sellPrice = $("#sellPrice").val();
		let minSellPrice = $("#minSellPrice").val();
		let itemId = $('#id').val();
		var units = $("#units").val();
		var taxRate = $("#gst").val();
		var digits = new RegExp(/^[0-9]+$/);

		/**validation for duplicated model number**/
		$.each(itemList, function (index, value) {
			if (itemId == "" && value.model.trim() == itemModelNo) {
				e.preventDefault(e);
				$("#itemModelNoDiv").html("Model already exists!")
				$("#itemModelNoDiv").css("color", "red")
			}

		});
		/**validation for empty fields**/
		if (itemModelNo === '') {
			e.preventDefault(e);
			$("#itemModelNoDiv").html("Model no is required.")
			$("#itemModelNoDiv").css("color", "red")
		}

		//description validation
		if (itemDescription === '') {
			e.preventDefault(e);
			$("#itemDescriptionDiv").html("Description is required.")
			$("#itemDescriptionDiv").css("color", "red")
		}
		//units validation
		if (units === '') {
			e.preventDefault(e);
			$("#itemUnitsDiv").html("Units is required.")
			$("#itemUnitsDiv").css("color", "red")
		}
		if(taxRate ===''){
			e.preventDefault(e);
			$("#taxRateDiv").html("Tax Rate is required.")
			$("#taxRateDiv").css("color", "red")
		}/*else 
		if (!taxRate.match(digits)) {
			e.preventDefault(e);
			$.error("Only digits are allowed");
		}*/


		/**Reset error messages**/
		$("#itemModelNo").change(function () {
			$("#itemModelNoDiv").html("");
		});
		$("#itemDescription").change(function () {
			$("#itemDescriptionDiv").html("");
		});
		$("select").change(function () {
			$("#itemUnitsDiv").html("");
		});
		$("#gst").change(function () {
			$("#taxRateDiv").html("");
		});
		
			$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
					if(role == "ITEMMASTER" || user=="praveen" ||role=="STORE"||user=="abhishek" || user=="jagadish"|| user=="rakesh"||user=="sushma"||user=="savitha"||user=="santosh"){
						$("#saveItemMaster").attr('disabled', false);
						$("#saveMake").attr("disabled",false);
						$("#addMake").show();
						
					}else{
						$("#saveItemMaster").attr('disabled', true);
						$("#saveMake").attr("disabled",true);
						$("#addMake").hdie();
					}
				});
		
		
		 $("#saveItemMaster", this)
	     .attr('disabled', 'disabled');
		 
		 $("#saveMake", this)
	     .attr('disabled', 'disabled');
		 
		 $("#addMake", this)
	     .attr('disabled', 'disabled');
	});
	/*var newRow = $("<tr>");
	var rowCount=$('#itemTable > tbody  > tr').length;
	var arraycount = rowCount-1;
   var columns = "";


   columns += '<td style="width:5%;text-align:center;">' + rowCount + '</td>';
   columns += '<td style="width:15%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="modelNo" name="items['+arraycount+'].modelNo" path="items['+arraycount+'].modelNo" style="width:100%;"/></td>';
   columns += '<td style="width:30%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox description" id="description" name="items['+arraycount+'].description" path="items['+arraycount+'].description" style="width:100%;"/></td>';
   columns += '<td style="width:15%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="hsnCode" name="items['+arraycount+'].hsnCode" path="items['+arraycount+'].hsnCode" style="width:100%;"/></td>';

   columns += '<td style="width:6%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox qty" id="qty" name="items['+arraycount+'].gst" path="items['+arraycount+'].qty" style="width:100%;"/></td>';
   columns += '<td style="width:10%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox unitPrice" id="unitPrice" name="items['+arraycount+'].sellPrice" path="items['+arraycount+'].unitPrice" style="width:100%;"/></td>';
   columns += '<td style="width:15%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox amount" id="amount" name="items['+arraycount+'].minSellPrice" path="items['+arraycount+'].amount" style=""/></td>';

	
   columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>';  
   
   columns += '<td style="width:5%;text-align:center;">' + rowCount + '</td>';
   columns += '<td style="width:15%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="modelNo" name="model"  style="width:100%;"/></td>';
   columns += '<td style="width:30%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox description" id="description" name="itemName" style="width:100%;"/></td>';
   columns += '<td style="width:15%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="hsnCode" name="hsnCode"  style="width:100%;"/></td>';

   columns += '<td style="width:6%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox qty" id="gst" name="gst"  style="width:100%;"/></td>';
   columns += '<td style="width:10%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox sellPrice" id="sellPrice" name="sellPrice" style="width:100%;"/></td>';
   columns += '<td style="width:15%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox minSellPrice" id="minSellPrice" name="minSellPrice"  style="width:100%;"/></td>';

	
   columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>';
   newRow.append(columns);
	$("#itemTable").append(newRow);*/
	/*$("#resetBtn").on("click", function (e) {
		$("#saveStock").html("Assign");
		$(".hideInput").hide();
		$("#reason-error").html("");
		$("#clientName").prop('selectedIndex', 0);
		$("#storeName-error").hide();
		$("#quantity-error").hide();
		$("#stockErrorDiv").hide();
		$("#clientName").val("").trigger('change');
		$("#stockclientName").val("").trigger('change');
		$("#stockModel").modal("hide");
		$("#stockModelForm")[0].reset();
		
		
		var itemid = $("#stockItemId").val();
		getStockList(itemid);

		// $("#stockHeader").html("Add Stock");
		$("[name=stockId]").val("");
	});*/
	$("#resetSupplierBtn").on("click", function (e) {
		$("#saveSupplier").html("Save");
		var itemId = $("#itemId").val();
		$("#supplierName").val("").trigger('change');
		$("#costPrice-error").hide()
		$("#supplierErrorDiv").hide();
		$("#supplierModelForm")[0].reset();
		getSupplierList(itemId);
		$("[name=supplierId]").val("");
	});
	getUnitList();
	/*Display clientName list*/
	$.each(customerPartyList, function (index, value) {
		var customerPartyList = value.partyName;
		customerPartyList = customerPartyList.split(' ');
		if (customerPartyList.length > 100) {
			customerPartyList.splice(100);
		}
		customerPartyList = customerPartyList.join(' ');
		var length = $.trim(customerPartyList).length;
		if (length > 125) {
			customerPartyList = $.trim(customerPartyList).substring(0, 125) + "....";
		}
		$("#clientName").append('<option value=' + value.id + '>' + customerPartyList + '</option>');
	});
	/*Display supplierName list*/
	$.each(supplierPartyList, function (index, value) {
		var supplierPartyList = value.partyName;
		supplierPartyList = supplierPartyList.split(' ');
		if (supplierPartyList.length > 100) {
			supplierPartyList.splice(100);
		}
		supplierPartyList = supplierPartyList.join(' ');
		var length = $.trim(supplierPartyList).length;
		if (length > 25) {
			supplierPartyList = $.trim(supplierPartyList).substring(0, 25) + "....";
		}
		$("#supplierName").append('<option value=' + value.id + '>' + supplierPartyList + '</option>');
	});
	stockValidation();
	//getStockList()
	supplierValidation();

	$(".hideInput").css("display", 'none');
	//getSupplierList();
	//loadStockTable();
	if(user=="admin" || user=="praveen"){
		$(document).on('click',".itemUploadButton",function(){
			$("#itemUploadForm")[0].reset();
			$('#response').empty();
			$("#itemUploadModal").modal('show');
		});
	}
	else{
		 $(".itemUploadButton").on("click", function (event) {
			    event.preventDefault();
			});
			
	}
});


//check for empty value after clicking on save button
function checkForEmptyValidation() {
	$("#save").on("click", function () {
		$('table > tbody  > tr > td > input').each(function (index, input) {
			if ($(this).val().trim() === '') {
				$(this).addClass("has-error")
				//$('.para').text("please enter all fields").css("color","#ff0000")
				$("#SalesVal").show();
			}
		});
	});
}

//working on tooltip
$("#HSNCode").keypress(function () {
	var dInput = this.value;
	var alpha = new RegExp(/^[a-zA-Z]+$/);
	if (dInput.match(alpha)) {
		$('.paraHSNCode').tooltip();
	}
});

//adding rows on click of add button
function addItemMaster() {
	$(document).on("click", ".add", function () {
		var newRow = $("<tr>");
		var rowCount = $('#itemTable > tbody  > tr').length;
		var arraycount = rowCount - 1;
		var columns = "";


		/*    columns += '<td style="width:5%;text-align:center;">' + rowCount + '</td>';
			columns += '<td style="width:15%; padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="modelNo" name="items['+arraycount+'].modelNo" path="items['+arraycount+'].modelNo" style="width:100%;"/></td>';
			columns += '<td style="width:30%; padding: .75rem;"><input type="text" class="form-control PositionofTextbox description" id="description" name="items['+arraycount+'].description" path="items['+arraycount+'].description" style="width:100%;"/></td>';
			columns += '<td style="width:15%; padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="hsnCode" name="items['+arraycount+'].hsnCode" path="items['+arraycount+'].hsnCode" style="width:100%;"/></td>';
		   
			columns += '<td style="width:6%; padding: .75rem;"><input type="number" class="form-control PositionofTextbox qty" id="qty" name="items['+arraycount+'].gst" path="items['+arraycount+'].qty" style="width:100%;"/></td>';
			columns += '<td style="width:10%; padding: .75rem;"><input type="number" class="form-control PositionofTextbox unitPrice" id="unitPrice" name="items['+arraycount+'].sellPrice" path="items['+arraycount+'].unitPrice" style="width:100%;"/></td>';
			columns += '<td style="width:15%; padding: .75rem;"><input type="number" class="form-control PositionofTextbox amount" id="amount" name="items['+arraycount+'].minSellPrice" path="items['+arraycount+'].amount"/></td>';
	
			
			columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>'; */

		columns += '<td style="width:5%;text-align:center;">' + rowCount + '</td>';
		columns += '<td style="width:15%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="modelNo" name="model"  style="width:100%;"/></td>';
		columns += '<td style="width:30%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox description" id="description" name="itemName" style="width:100%;"/></td>';
		columns += '<td style="width:15%;padding: .75rem;"><input type="text" class="form-control PositionofTextbox" id="hsnCode" name="hsnCode"  style="width:100%;"/></td>';

		columns += '<td style="width:6%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox qty" id="gst" name="gst"  style="width:100%;"/></td>';
		columns += '<td style="width:10%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox sellPrice" id="sellPrice" name="sellPrice" style="width:100%;"/></td>';
		columns += '<td style="width:15%;padding: .75rem;"><input type="number" class="form-control PositionofTextbox minSellPrice" id="minSellPrice" name="minSellPrice"  style="width:100%;"/></td>';


		columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>';
		newRow.append(columns);
		$("#itemTable").append(newRow);
		itemName = itemList.map(({ itemName }) => itemName);
		$(".description").autocomplete({
			source: itemName
		});
	});
}

//delete sales Order
function deleteItemMaster() {
	$("#itemTable").on("click", ".deleteButton", function () {
		$(this).closest("tr").remove();
		$('tbody').find('tr').each(function (index) {
			var firstTDDomEl = $(this).find('td')[0];
			//Creating jQuery object
			var $firstTDJQObject = $(firstTDDomEl);
			$firstTDJQObject.text((index - 1) + 1);
		});
	});
}
function getAmount() {
	$(document).on('keyup mouseup', '.qty, .unitPrice', function () {
		var parent = $(this).closest('tr');
		parent.find('.amount').val(parseFloat(parent.find('.qty').val()) * parseFloat(parent.find('.unitPrice').val()))
		var sum = 0;
		var amountArr = [];
		$('.amount').each(function () {
			amountArr.push(parseFloat($(this).val()));
			var amount = parseFloat($(this).val());
			sum += amount;
			$(".total").attr("value", sum);
			var gst = 0.18 * sum;
			$(".gst").attr("value", gst);
			var grandTotal = sum + gst;
			$(".grandTotal").attr("value", grandTotal);
		});
	});

}


//load item list
var datatable = null;
function loadItemTable() {
	var domLayout = '<"panel panel-default"<"panel-heading"<"row"<"col-md-6"l><"col-md-6 text-right"f>>>t<"panel-footer"<"row"<"col-md-6"i><"col-md-6 text-right"p>>>>';
	var baseConfig = {
		"destroy": true,
		"columnDefs": [
			{ "width": "85px", "targets": 0 }
		],
		"dom": domLayout,
		buttons: [{
			extend: 'excel',
			filename: 'Current Item Stock',
			title: 'Current Item Stock'
		}],
		"columns": [
			{
				"title": "Id",
				"data": "id",
				"class": "buttonAllign",
				"visible": false
			},
			{
				"title": "Model No",
				"data": "model",
				"defaultContent": "",
				"width": "30%",
				"class": "text-field-large-nowrap"
			},
			{
				"title": "Description",
				"data": "itemName",
				"defaultContent": "",
				"width": "30%",
				"class": "text-field-large-nowrap",
				render: function (data) {
					if (!data) { return ""; }
					return data.length > 35 ?
						data.substr(0, 35) + '...' :
						data;
				}
			},
			{
				"title": "HSN",
				"data": "hsnCode",
				"width": "6%",
				"defaultContent": ""
			},
			{
				"title": "Units",
				"data": "units",
				"defaultContent": "",
				"width": "6%",
				render: function (aaData, type, row) {
					if (row.item_units == null) {
						return "";
					} else {
						return row.item_units.name;
					}
				}
			},
			{
				"title": "Location",
				"data": "location",
				"defaultContent": "",
				"width": "7%",
				"class": "text-field-large-nowrap",
				render: function (data) {
					if (!data) { return ""; }
					return data.length > 35 ?
						data.substr(0, 35) + '...' :
						data;
				}
			},
			{
				"title": "Make",
				"data": "make",
				"defaultContent": "",
				"width": "7%",
				"class": "text-field-large-nowrap",
				render: function (data) {
					var makeName = null;
					$.each(makeList, function (index, value) {
						if (data == value.id) {
							makeName = value.name;
						}
					});
					return makeName;
				}
			},
			{
				"title": "Stock",
				"data": "name",
				"width": "7%",
				"class": "buttonAllign",
				render: function (datam, type, row, index) {
					var quantity = stockSummaryByItem[row.id] || 0;
					quantity = Math.round(quantity * 100) / 100;
					if(role == "ITEMMASTER"){
						return '<button type="button" class="btn btn-primary item-master-count-btn addInfo" disabled><span class="badge" id="stockBadge' + index.row + '">' + quantity + '</span></button>';
					}else{
						return '<button type="button" class="btn btn-primary item-master-count-btn addInfo"><span class="badge" id="stockBadge' + index.row + '">' + quantity + '</span></button>';
					}
				}
			},
			{
				"title": "Supplier",
				"data": "name",
				"width": "7%",
				"class": "buttonAllign",
				render: function (datam, type, row, index) {
					var supplierCount = supplierSummaryByItem[row.id] || 0;
					if(role == "ITEMMASTER"){
						return '<button type="button" class="btn btn-primary item-master-count-btn mapInfo" disabled><span class="badge" id="supplierBadge' + index.row + '">' + supplierCount + '</span></button>';
					}else{
						return '<button type="button" class="btn btn-primary item-master-count-btn mapInfo"><span class="badge" id="supplierBadge' + index.row + '">' + supplierCount + '</span></button>';
					}
				}
			},
			{
				"title": "Client Name",
				"data": "name",
				"class": "buttonAllign",
				"visible": false,
				render: function (datam, type, row) {
					var clientName = clientSummaryByItem[row.id] || [];
					return clientName;
				}
			},
			{
				"title": "Supply Price",
				"data": "name",
				"visible": false,
				render: function (datam, type, row) {
					return costSummaryByItem[row.id] || "";
				}
			}
		]
	};

	if(window.itemMasterLazyConfig && window.itemMasterLazyConfig.enabled){
		$('#itemMasterList').empty();
		var serverConfig = $.extend(true, {}, baseConfig, {
			processing: true,
			serverSide: true,
			ajax: function (data, callback) {
				var pageNo = Math.floor(data.start / data.length);
				var searchValue = data.search ? data.search.value : "";
				searchValue = searchValue == null ? "" : searchValue;

				var requestPayload = {
					pageNo,
					pageSize: data.length,
					searchValue,
					toolTrackerOnly: !!window.itemMasterLazyConfig.toolTrackerOnly
				};

				$.ajax({
					url: window.itemMasterLazyConfig.api.list,
					type: "GET",
					data: requestPayload,
					success: function (response) {
						var totalRecords = response.totalItems || 0;
						var items = response.items || [];
						var stockMap = response.stockSummaryMap || {};
						var supplierMap = response.supplierSummaryMap || {};
						var customerMap = response.customerSummaryMap || {};
						var sellPriceMap = response.costPriceSummaryMap || {};

						stockSummaryByItem = stockMap;
						supplierSummaryByItem = supplierMap;
						clientSummaryByItem = customerMap;
						costSummaryByItem = sellPriceMap;
						itemList = items;

						callback({
							draw: data.draw,
							data: items,
							recordsTotal: totalRecords,
							recordsFiltered: totalRecords
						});
					},
					error: function () {
						callback({
							draw: data.draw,
							data: [],
							recordsTotal: 0,
							recordsFiltered: 0
						});
					}
				});
			}
		});
		datatable = $('#itemMasterList').DataTable(serverConfig);
		datatable = $('#itemMasterList').DataTable(serverConfig);
	}else{
		var clientConfig = $.extend(true, {}, baseConfig, {
			"data": itemList
		});
		datatable = $('#itemMasterList').DataTable(clientConfig);
	}
	datatable.buttons().container().appendTo($('#itemMasterList_length'));

	$('#itemMasterList tbody').on('dblclick', 'tr', function () {
		var row = datatable.row($(this).closest("tr").get(0));
		var rowData = row.data();

		var itemId = rowData.id;
		// call stock list and supplier list to set quantity and cost price in item master page
		getStockList(itemId);
		setCostPrice(itemId);
		$('#itemModelNo').prop('readonly', true)
		$.each(rowData, function (key, value) {
			if (rowData.item_units != null) {
				$("#units").val(rowData.item_units.id);
				$('#units').select2(rowData, { id: rowData.item_units.id, a_key: rowData.item_units.name });

			}
			if(rowData.toolTracker==true){
				$("#toolTracker").prop("checked",true);
			}else{
				$("#toolTracker").prop("checked",false);
			}
			if(rowData.companyAssets==true){
				$("#companyAssets").prop("checked",true);
			}else{
				$("#companyAssets").prop("checked",false);
			}
			$("[name=" + key + "]").val(value);
		});
		if (row.length != 0) {
			$(".hideInput").show();
			$("#saveItemMaster").html("Update");
			$("#boxHeader").html("Update Item");
		}
		if(role=="STORE" || user=="praveen"||user=="abhishek" || user=="jagadish"|| user=="rakesh"||user=="sushma"||user=="savitha"||user=="santosh"){
			$("#saveItemMaster").attr("disabled",false);
		}
	});


}




//setting total quantity on add button
function getStockQuantity(itemId, rowIndex) {

	$.ajax({
		url: api.STOCK_LIST + "?itemId=" + itemId,
		type: 'GET',

		success: function (response) {
			console.log(response);
			var quantity = null;
			var length = response.length;
			$.each(response, function (index, value) {
				quantity = quantity + value.quantity;
			});
			if (response.length == 0) {
				$("#stockBadge" + rowIndex).html(0);

			} else {

				$("#stockBadge" + rowIndex).html(quantity);

			}
			return response;
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	
	})
}
//setting costprice on map button
function setSupplierCostPrice(itemId, rowIndex) {
	$.ajax({
		url: api.SUPPLIER_LIST + "?itemId=" + itemId,
		type: 'GET',

		success: function (response) {
			if (response.length == 0) {
				$("#supplierBadge" + rowIndex).html(0);
			} else {
				$("#supplierBadge" + rowIndex).html(response.length);
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
	})
}
//on click of add button in item list display stock pop-up 
$(document).on("click", ".addInfo", function () {
	rowSpanId = $(this).closest("tr").find("span").attr('id');
	ttlStockQty = $(this).closest("tr").find("span").html();

	var row = datatable.row($(this).closest("tr").get(0));
	var rowData = row.data();
	var itemId = rowData.id;
	$("#clientName").val('').trigger('change');
	$("#stockItemId").val(itemId);
	$("#stockModelItemId").val(itemId);
	$("#clientName").prop('selectedIndex', 0);
	$("#storeName-error").hide();
	$("#quantity-error").hide();
	$("#stockModelForm")[0].reset();
	
	$("#stockModelClientForm")[0].reset();
	$("#stockErrorDiv").html("");

	$("#saveStock").html("Assign");
	$(".hideInput").hide();
	$(".hideInput").css("display", 'none')
	$("[name=stockId]").val("");
	$("#stockModel").modal("show");
	getStockList(itemId);
	appendClientName(itemId);
})

//on click of map button in item list display supplier pop-up 
$(document).on("click", ".mapInfo", function () {

	rowId = $(this).closest("td").find("span").attr('id');
	totalSupplier = $(this).closest("td").find("span").html();

	var row = datatable.row($(this).closest("tr").get(0));
	var rowData = row.data();
	var itemId = rowData.id;
	$("#itemId").val(itemId);
	$("#supplierName").val("").trigger('change');
	$("#costPrice-error").hide()
	$("#supplierModelForm")[0].reset();
	$("#supplierErrorDiv").html("");
	$("#saveSupplier").html("Save");

	$("[name=supplierId]").val("");
	$("#supplierModal").modal("show");
	getSupplierList(itemId);
})


//code to delete the item row.
$(document).on("click", ".deleteInfo", function () {
	var row = datatable.row($(this).closest("tr").get(0));
	var rowData = row.data();
	var id = rowData.id;
	bootbox.confirm({
		message: "The selected Item will be deleted. Are you sure?",
		buttons: {
			cancel: {
				label: 'Cancel'
			},
			confirm: {
				label: 'Confirm'
			}
		},
		callback: function (result) {
			result ? deleteItem(id) : "";
		}
	});
})

function deleteItem(id) {

	$.ajax({
		type: "POST",
		url: api.DELETE_ITEM + "?id=" + id,
		success: function (response) {

			window.location.reload();
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

//code to validate add stock form
function stockValidation() {

	$('#stockModelForm').validate({
		rules: {

		},

		submitHandler: function (form) {
			var assignedStockJson = $(form).form2json();
			var stockClientName = $("#stockclientName").val();
			if(assignedStockJson.clientName == null || assignedStockJson.clientName == undefined){
				$.error("client Name is Required");
			}else{
					if(assignedStockJson.clientName == stockClientName){
						$.error("Clients are same");
					}else{
						var stockQuantity = parseFloat($("#stockClientquantity").val());
						var assignedQuantityInString =$("#quantity").val();
						var assignedQuantity =parseFloat( $("#quantity").val());
						var digits= new RegExp(/^[0-9]+$/);
						var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
				        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
						//if (assignedQuantityInString.match(digits)||assignedQuantityInString=="") {
							if(assignedQuantity > stockQuantity){
								$.error("Quantity is greater");
		        	 // e.preventDefault(e);
								}else{
										saveStock(assignedStockJson);
										isDirty = false;
										}
									
						/*}
						else{
				        	$.error("only digits are allowed for qty");
						}*/
						
						}		
					}
			/**
			 * check for duplicates in stock
			 */
		/*	$.ajax({
				method: 'GET',
				url: api.VALIDATE_CLIENT + "?itemId=" + stockJson.itemId + "&&clientName=" + stockJson.clientName + "&&stockId=" + stockJson.stockId,
				success: function (response) {
					if (response == false) {
						$("#stockErrorDiv").hide();
						saveStock(stockJson);
					} else {
						$("#stockErrorDiv").show();
						$("#stockErrorDiv").html("Client already exists!");
						$("#stockErrorDiv").css('color', 'red');
					}
				}
			});*/

		}
	});
}


//code to save stock form data
function saveStock(stockJson) {
	var existingclienName =  $("#stockclientName").val();
	var existingQuantity = $("#stockClientquantity").val();
	if(existingclienName==null){
		$.error("Stock not available");
		return false;
	}
	showLoader();
	$.ajax({
		type: "POST",
		url: api.ADD_STOCK+ "?existingquantity=" + existingQuantity + "&&existingclientName=" + existingclienName,
		dataType: "json",
		data: stockJson,

		success: function (response) {
			isDirty = false;
			var stockBtn = $("#saveStock").html();
			if (stockBtn == "Assign") {
				var stockAvailable = parseFloat(ttlStockQty) + parseFloat(response.quantity);
				$('#' + rowSpanId).html(stockAvailable);
			}
			$("#clientName").val("").trigger('change');
			$('#clientName').select2('', { id: '', text: '' });
			$("#stockclientName").val("").trigger('change');
			$("#stockclientName").select2('', { id: '', text: '' });
			$("#clientName").select2({dropdownAutoWidth : true});
			$("#stockModelForm")[0].reset();
			
			
			$("#saveStock").html("Assign");
			$(".hideInput").hide();
			$("[name=stockId]").val("");
			//$('#resetBtn').click();
			appendClientName(response.itemMaster.id);

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

var stockClientList = null;
//ajax call to get the list of stocks
function getStockList(itemId) {
	$.ajax({
		url: api.STOCK_LIST + "?itemId=" + itemId,
		type: 'GET',

		success: function (response) {
			
			
			
			
			
			console.log(response);
			var quantity = null;
			var model = null;
			var desc = null;
			if (response.length == 0) {
				$.each(itemList, function (index, value) {
					if (itemId == value.id) {
						model = value.model;
						desc = value.itemName;
						if (desc.length > 35) {
							desc = jQuery.trim(desc).substring(0, 35) + "....";
						}
					}
				});
			} else {
				$.each(response, function (index, value) {
					quantity = quantity + value.quantity;
					model = value.itemMaster.model
					desc = value.itemMaster.itemName;
					if (desc.length > 35) {
						desc = jQuery.trim(desc).substring(0, 35) + "....";
					}
					if (value.itemMaster.item_units != null) {
						units = value.itemMaster.item_units.name;
					} else {
						units = "";
					}
				});
			}
			quantity=Math.round(quantity * 100) / 100
			//set total quantity in item master page
			setQuantity(quantity);
			if (response.length == 0) {
				$("#stockHeader").html('Stock' + '(' + 0 + ')' + '/' + model + '/' + desc);

			} else {

				$("#stockHeader").html('Stock' + '-' + model + '/' + desc + '/' + quantity + " " + units);
				$("#qtyunits").html(units);
				$("#assignQtyunits").html(units);
				$('#' + rowSpanId).html(quantity);
			}
			loadStockTable(response);

		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	
	})
}

//set total quantity in item master page
function setQuantity(quantity) {
	if (quantity == null) {
		$("#stockQuantity").val(0);
	} else {
		var quanUnits = quantity + " " + units;
		$("#stockQuantity").val(quanUnits);
	}
}


//display the stock list in datatable
var addStockTable = null;
function loadStockTable(response) {
	if (response != undefined || response != null) {
		addStockTable = $('#stockList').DataTable({
			"data": response,
			"destroy": true,
			"columns": [
				{
					"data": "clientName",
					"title": "Client Name",
					"class": "clientWidth",
					"defaultContent": "",
					render: function (data, type, row) {
						var party;
						if (row.party == null || row.party == undefined) {
							party = "";
						} else {
							party = row.party.partyName;

						}
						return party;
					}
				},
				{
					"data": "quantity",
					"title": "Qty",
					"class": "qtyWidth",
					"defaultContent": "",
					render: function (data, type, row) {
						if (row.itemMaster.item_units != null) {
							var units = row.itemMaster.item_units.name;
						} else {
							units = "";
						}
						var quantunits = null;
						if (units == null || units == undefined || units == "") {
							quantunits = row.quantity;
						} else {
							quantunits = row.quantity + " " +
								row.itemMaster.item_units.name;
						}
						return quantunits;
					}


				},
				{
					"data": "storeName",
					"title": "Store Name",
					"width": "180px",
					"class": "text-field-large-nowrap",
					"defaultContent": "",
					render: function (data, type, row) {
						if(data!=null){
						return data.length > 20 ?
							data.substr(0, 20) + '...' :
							data;
					}}
				},
				{ "data": "locationInStore", "title": "Location in Store", "class": "locInStrWidth", "defaultContent": "" }
			]


		});
		//On dbclick go to edit mode 
		/*$('#stockList tbody').on('dblclick', 'tr', function () {
			var row = addStockTable.row($(this).closest("tr").get(0));
			var rowData = row.data();

			$.each(rowData, function (key, value) {
				if (rowData.party != null) {
					$("#clientName").val(rowData.party.id);
					$('#clientName').select2(rowData, { id: rowData.party.id, a_key: rowData.party.name });
				}
				$("[name=" + key + "]").val(value);
				$("#reason").val('');
			});
			if (row.length != 0) {
				$(".hideInput").show();

				$("#saveStock").html("Update");
				$("#stockHeader").html("Update Stock");
			}

		});*/

	}

}

//code to populate unit dropdown
function getUnitList() {
	$.ajax({
		url: api.UNITS_LIST,
		type: 'GET',

		success: function (response) {
			console.log(response);
			$.each(response, function (index, value) {
				$("#units").append('<option value=' + value.id + '>' + value.name + '</option>');
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
	})
}

$(document).ready(function () {
	$('#cncl').click(function () {

		var saveBtn = $("#saveItemMaster").text();
		if (saveBtn == "Save") {
			$('#itemMasterForm1')[0].reset();
		} else {
			// redirect
			window.location.href = '/ncpl-sales/itemMaster';
			return false;
		}
	});
});

//code for supplier validation

function supplierValidation() {
	$('#supplierModelForm').validate({
		rules: {

		},

		submitHandler: function (form) {
			var supplierJson = $(form).form2json();

			$.ajax({
				method: 'GET',
				url: api.VALIDATE_SUPPLIER_EXIST + "?itemId=" + supplierJson.itemId + "&&supplierName=" + supplierJson.supplierName + "&&supplierId=" + supplierJson.supplierId,
				success: function (response) {
					if (response == false) {
						$("#supplierErrorDiv").hide();
						saveSupplier(supplierJson);
					} else {
						$("#supplierErrorDiv").show();
						$("#supplierErrorDiv").html("Supplier Already Exists");
						$("#supplierErrorDiv").css('color', 'red');
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

	})
}

//code to save supplier
function saveSupplier(supplierJson) {
	if ($("input[type='radio']#preferred").is(':checked')) {
		supplierJson['preferred'] = "Yes";
	} else {
		supplierJson['preferred'] = "No";
	}
	showLoader();
	$.ajax({
		type: "POST",
		url: api.ADD_SUPPLIER,
		dataType: "json",
		data: supplierJson,

		success: function (response) {
			var supplierBtn = $("#saveSupplier").html();
			if (supplierBtn == "Save") {
				var supplierAvailable = parseFloat(totalSupplier) + 1;
				$('#' + rowId).html(supplierAvailable);
			}
			$("#supplierName").val("").trigger('change');
			$("#supplierModelForm")[0].reset();
			$("#saveSupplier").html("Save");
			$("[name=supplierId]").val("");
			$('#resetSupplierBtn').click();


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

//code to get list of supplier
function getSupplierList(itemId) {
	$.ajax({
		url: api.SUPPLIER_LIST_WITH_PO_HISTORY + "?itemId=" + itemId,
		type: 'GET',

		success: function (response) {
			console.log(response);
			var model = null;
			if (response.length == 0) {
				$.each(itemList, function (index, value) {
					if (itemId == value.id) {
						model = value.model;
					}
				});
			}
			else {
				$.each(response, function (index, value) {
					model = value.itemMaster.model
				});
			}
			if (response.length == 0) {
				$("#supplierHeader").html('Supplier' + '(' + 0 + ')' + '/' + model);
			} else {
				$("#supplierHeader").html('Supplier' + '(' + response.length + ')' + '/' + model);
			}

			loadSupplierTable(response);
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	
	})
}

//get list of supplier with preferred yes and get cost price
function setCostPrice(itemId) {
	$.ajax({
		url: api.PREFERRED_SUPPLIER_LIST + "?itemId=" + itemId,
		type: 'GET',

		success: function (response) {
			console.log(response);
			var costPrice = null;
			$.each(response, function (index, value) {
				costPrice = costPrice + value.costPrice;
				costPrice=Math.round(costPrice * 100) / 100;
			});
			if (response.length == 0) {
				$("#supplierCostPrice").val(0);
			} else {
				$("#supplierCostPrice").val(costPrice);
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
	})
}

//datatable to list supplier
var supplierTable = null;
function loadSupplierTable(response) {
	if (response != undefined || response != null) {
		supplierTable = $('#supplierList').DataTable({


			"data": response,
			"destroy": true,
			"columns": [

				{
					"title": "Supplier Name",
					"data": "supplierName",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var name;
						if (row.party == null) {
							name = "";
						} else {
							name = row.party.partyName;

						}
						return name;

					}

				},
				{
					"title": "Cost Price",
					"data": "costPrice",
					"defaultContent": "",
					
					render: function (data, type, row, meta) {
						data=Math.round(data * 100) / 100;
						return data;
					}
				},
				{
					"title": "Preferred",
					"data": "preferred",
					"defaultContent": "",
					render: function (data, type, row, meta) {
						if (row.preferred == "Yes") {
							return '<input type="checkbox" name ="preferred" class="preferredCheckBox" checked disabled>'
						} else {
							return '<input type="checkbox" name ="preferred" class="preferredCheckBox" disabled>'
						}
					}

				},
				{
					"title": "Date",
					"data": "updated",
					"defaultContent": "",
					render : function(datam, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}

				}/*,
			    {
			        "title" : "Edit",
			        "mData" : "name",
			       
			        render : function(datam, type, row) {
			             return '<button type="button" class="btn btn-default btn-sm editSupplier"><i class="fa fa-edit "></i></button>'
			             
			        }
			    },
					*/
			]
		});


		//On dbclick goto edit mode
		$('#supplierList tbody').on('dblclick', 'tr', function () {
			var row = supplierTable.row($(this).closest("tr").get(0));
			var rowData = row.data();
			$.each(rowData, function (key, value) {
				if (rowData.party != null) {
					$("#supplierName").val(rowData.party.id);
					$('#supplierName').select2(rowData, { id: rowData.party.id, a_key: rowData.party.name });
				} else {
					$("#supplierName").val("").trigger("change");
				}

				if (rowData.preferred == "Yes") {
					$("#preferred").prop("checked", true);
				} else {
					$("#preferred").prop("checked", false);
				}
				$("[name=" + key + "]").val(value);
			});
			if (row.length != 0) {
				$("#saveSupplier").html("Update");
				$("#supplierHeader").html("Update Supplier");
			}
		});
	}

}

$(document).on('click', '#supplierReset', function (e) {
	$("#saveSupplier").html("Save");
	$("#supplierErrorDiv").html("");
	$('#supplierModelForm')[0].reset();

});

$(document).on('click', '#stockReset', function (e) {
	$("#saveStock").html("Assign");
	$("#stockErrorDiv").html("");
	$("#reason-error").html("");
	$('#stockModelForm')[0].reset();
	$('#stockModelClientForm')[0].reset();
	

});

//on click of history button display popup with previous reasons table
$(document).on('click', '#historyButton', function () {
	$("#reasonModal").modal("show");
	var stockId = $("#stockId").val();
	$.ajax({
		method: 'GET',
		url: api.STOCK_HISTORY_BYID + "?stockId=" + stockId,
		success: function (response) {
			$("#previousReasonTable tbody").empty();
			$.each(response, function (index, value) {
				var date = value.updated;
				var formattedDate = Date.parse(date);
				var newDate = new Date(formattedDate);
				var localDate = newDate.toLocaleString();
				var reasonWithDate = "<tr><td>" + localDate + "</td><td>" + value.reason + "</td></tr>";
				$("#previousReasonTable tbody").append(reasonWithDate);
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
});

function appendClientName(itemId){

	$.ajax({
		url: api.STOCK_LIST + "?itemId=" + itemId,
		type: 'GET',

		success: function (response) {
			stockClientList = response;
			/*if(stockClientList.length > 0){
				$("#stockModel").modal("show");
			}*/
			$("#stockclientName").empty();
			$.each(response, function (index, value) {
				
			
			
			var customerPartyList = value.party.partyName;
			customerPartyList = customerPartyList.split(' ');
			if (customerPartyList.length > 100) {
				customerPartyList.splice(100);
			}
			customerPartyList = customerPartyList.join(' ');
			var length = $.trim(customerPartyList).length;
			if (length > 25) {
				customerPartyList = $.trim(customerPartyList).substring(0, 25) + "....";
			}
			$("#stockclientName").append('<option value=' + value.party.id + '>' + customerPartyList + '</option>');
			$("#stockclientName").val(value.party.id).trigger('change');
			isDirty=false;
			
			
			});
			loadStockTable(response);
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  	
	})
	
}

$(document).on("change", '#stockclientName', function () {
	var clientId = $(this).val();
	$.each(stockClientList, function (index, value) {
		var partyId = value.party.id;
		if(partyId == clientId){
		$("#stockClientquantity").val(value.quantity);
		$("#stockstoreName").val(value.storeName);
		$("#stocklocationInStore").val(value.locationInStore);
		
	
		}
	});
});

function getMakeList(){
	$.ajax({
	method :'GET',
	url:api.MAKE_LIST,
	success:function(response){
	console.log(response);
	makeListTable(response);
	$('#makeDropdown option:not(:first)').remove();
	$.each(response, function( key, value ) {
		$('#makeDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
	  });
  // $("#makeDropdown").val(partyObj.party_type.id);	
	},  
	complete:function(resp){
		if(resp.status==500){
			$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
		}
	},
	error : function(e) {
		console.log(e);
	}  	
})
}

function makeListTable(makeList){
	makeTable= $('#makeTable').DataTable({
		   destroy:true,
		    "aaData": makeList,
		    "aoColumns": [
		    {"title": "Make",
		    "mData": "name"
		    },
		    
		    { 	
		    	"title":'Delete',
		    	"class":"styleOfSlNo",
		    		render : function ( mData, type, row,meta ) {
		                    return '<i class="deleteMake fa fa-trash " aria-hidden="true"></i>';
		                }
		    }
		    ]
		   });  
}

$(document).on("click",".deleteMake",function(){
	var row = makeTable.row($(this).closest("tr").get(0));
	   var rowData=row.data();
	   var id = rowData.id;
   bootbox.confirm({
	   message: "Do you want to delete the Make?",
	   buttons: {
		   cancel: {
			   label: 'Cancel'
		   },
		   confirm: {
			   label: 'Confirm'
		   }
	   },
	   callback: function (result) {
		   result ? deleteMake(id):"";
	   }
   });		
})
function deleteMake(id){
	   $.ajax({
		   type : "POST",  
		   url : api.DELETE_MAKE +"?id="+id,
		   success : function(response) {
			$.success("Make deleted successfully");   
			getMakeList();
		   }, 
		   error : function(e) {
			$.error("Make cannot be deleted because it is already mapped");
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

function addMake(){
	$('#makeForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var makeJson=$(form).form2json()
			makeJson['id']=+makeJson['id'];
			//check for duplicate type
			$.ajax({
				method:'GET',
				url :api.VALIDATE_MAKE+ makeJson.name+"/"+makeJson.id,
				success :function(response){
					if(response==false){
						if(makeJson.name == null){
							$.error("Please enter the make");
							$("#makeInput").addClass("has-error")
							$('input').on("change",function(){
							$("#makeInput").removeClass("has-error")
						})
						}  
						else{
							saveMake(makeJson)
						}
					}else{
						$.error("Make already exist");
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
	});   
}
function saveMake(make){
showLoader(); 
		$.ajax({
				type : "POST",  
				url : api.ADD_MAKE,
				dataType  : "json",
				data:make,
				
				success : function(response) {
						$("#makeForm")[0].reset();
						$("#makeForm").find('input[name=id]').val("");
						$("#makeHeader").html("Add/Delete Make");
						$("#saveMake").html("Save");
						getMakeList();
						hideLoader();
						$.success("Make saved Successfully");

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




$(document).on('submit',"#itemUploadForm", function (e) {
    e.preventDefault(); // Prevent form submission

    let formData = new FormData();
    formData.append('file', $('#file')[0].files[0]);

    $.ajax({
        url: api.ITEMS_UPLOAD,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
        	$('#response').empty();
            alert(response.message);
            window.location.href="/ncpl-sales/itemMaster";
        },
        error: function (xhr) {
            let errors = xhr.responseJSON.errors;
            $("#response").html(errors.map(e => `<p>${e}</p>`).join(""));
        }
    });
});
