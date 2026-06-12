$(document).ready(function () {
	if (salesOrderObj != "") {
		getSalesItemListBySoItem(salesOrderObj);
		$("#soNumber").val(salesOrderObj.id);
	}
});

function getLotFieldsHtml(arrayCount, lotIndex, lotNumber, quantity, readonly) {
	var lotNoVal = lotNumber || '';
	var qtyVal = quantity || '';
	var qtyRoAttr = readonly ? 'readonly' : '';
	var delBtn = readonly ? '' : '<i class="fa fa-trash" onclick="removeLot(this)" style="cursor:pointer;color:red;padding-top:6px;" aria-hidden="true"></i>';
	return '<div class="lot-entry" style="display:flex;gap:4px;margin-bottom:2px;">' +
		'<input type="text" class="form-control form-control-sm PositionofTextbox" style="width:100px" name="items[' + arrayCount + '].lots[' + lotIndex + '].lotNumber" placeholder="Lot No" value="' + lotNoVal + '" readonly />' +
		'<input type="number" class="form-control form-control-sm PositionofTextbox" style="width:80px" name="items[' + arrayCount + '].lots[' + lotIndex + '].quantity" placeholder="Qty" value="' + qtyVal + '" step="any" ' + qtyRoAttr + ' />' +
		delBtn +
		'</div>';
}

function getTotalLotQty(container) {
	var total = 0;
	container.find('.lot-entry input[name$=".quantity"]').each(function () {
		var val = parseFloat($(this).val());
		if (!isNaN(val)) {
			total += val;
		}
	});
	return total;
}

function updateAddBtn(container) {
	var poQty = parseFloat(container.data('poqty'));
	if (isNaN(poQty) || poQty <= 0) {
		container.closest('tr').find('.add-lot-btn').show();
		return;
	}
	if (getTotalLotQty(container) >= poQty) {
		container.closest('tr').find('.add-lot-btn').hide();
	} else {
		container.closest('tr').find('.add-lot-btn').show();
	}
}

function addLotRow(button) {
	var container = $(button).closest('tr').find('.lot-container');
	var poQty = parseFloat(container.data('poqty'));
	if (!isNaN(poQty) && poQty > 0 && getTotalLotQty(container) >= poQty) {
		return;
	}
	var arrayCount = container.data('arraycount');
	var lotIndex = container.data('lotindex');
	container.append(getLotFieldsHtml(arrayCount, lotIndex, lotIndex + 1));
	container.data('lotindex', lotIndex + 1);
	updateAddBtn(container);
}

function addFirstLot(container) {
	var arrayCount = container.data('arraycount');
	container.append(getLotFieldsHtml(arrayCount, 0, 1));
	container.data('lotindex', 1);
}

function removeLot(btn) {
	var entry = $(btn).closest('.lot-entry');
	var container = entry.closest('.lot-container');
	entry.remove();
	var entries = container.find('.lot-entry');
	if (entries.length == 0) {
		container.data('lotindex', 0);
		container.closest('tr').find('.add-lot-btn').hide();
	} else {
		entries.each(function (idx) {
			var inputs = $(this).find('input');
			inputs.each(function () {
				var name = $(this).attr('name');
				if (name) {
					name = name.replace(/lots\[\d+\]/, 'lots[' + idx + ']');
					$(this).attr('name', name);
				}
			});
			var delBtn = $(this).find('.fa-trash');
			if (delBtn.length) {
				delBtn.attr('onclick', 'removeLot(this)');
			}
		});
		container.data('lotindex', entries.length);
		updateAddBtn(container);
	}
}

function clearLots(container) {
	container.empty();
	container.data('lotindex', 0);
}



function getSalesItemListBySoItem(salesOrderObj) {
	var partyId = salesOrderObj.party.id;
	$('#party').val(salesOrderObj.party.id);

	$("#clientPoDate").replaceWith('<input type="text" class="form-control PositionofTextbox" style="width:150px" id="poDateVal" name="clientPoDate">');
	var poDate = salesOrderObj.clientPoDate;
	if (poDate == null) {
		$("#poDateVal").val("");
	} else {
		poDate = new Date(poDate);
		var date = new Date(poDate).getUTCDate();
		var month = new Date(poDate).getUTCMonth() + 1;
		var year = new Date(poDate).getUTCFullYear();
		poDate = date + "-" + month + "-" + year;
		$("#poDateVal,#clientPoDate").val(poDate);
	}

	$("#clientPoNumber,#clientPo").val(salesOrderObj.clientPoNumber);
	$('#poDateVal,clientPoDate').attr("readonly", "readonly");
	$('#clientPoNumber').attr("readonly", "readonly");

	$('#partyDropDown').replaceWith('<input type="text" class="form-control PositionofTextbox" style="width:250px" id="partyVal" name="partyVal">');
	$('#partyVal').val(salesOrderObj.party.partyName);
	$('#partyVal').attr("disabled", true);
	var className = "so";
	var soId = salesOrderObj.id;
	$.ajax({
		Type: 'GET',
		url: api.SALES_LIST_BY_SOID + "?id=" + soId + "&&className=" + className,
		dataType: 'json',
		async: 'false',
		success: function (response) {
			var arrayCount = 0;
			$.each(response, function (index, value) {

				if (value.designItems.length == 0) {
					if (value.item_units.name == "Heading") {
						slNo = value.slNo;
						description = value.description;
						salesItemId = value.id;
						modelNumber = "";
						poQty = "";
						unitName = "";
						model = "";
						tds = "";
						siteQty = "";
						qty = "";
					} else {
						slNo = value.slNo;
						description = value.description;
						salesItemId = value.id;
						modelNumber = value.modelNo;
						poQty = value.quantity;
						unitName = value.item_units.name;
						model = "";
						tds = "";
						qty = "";
					}

					var soItems = "<tr><td width='4%'>" + slNo + "</td><td width='20%'>" + description + "</td>" +
						"<td width='12%'>" + modelNumber + "</td><td width='6%'>" + poQty + "</td><td width='4%'>" + unitName + "</td>" +
						"<td width='7%'>" + model + "</td><td width='5%'>" + qty + "</td>" +
						"<td width='6%'>" + tds + "</td>" +
						"<td width='15%'><div class='lot-container' data-arraycount='" + arrayCount + "' data-lotindex='0' data-poqty='" + poQty + "'></div></td>" +
						"<td width='5%'><button type='button' class='btn btn-success btn-sm add-lot-btn' style='display:none;' onclick='addLotRow(this)'>+</button></td>" +
						"<td style='display:none'><input type='checkbox' class='form-control form-control-sm PositionofTextbox tdsApproved' id='tdsApproved" + arrayCount + "' name='items[" + arrayCount + "].tdsApproved' path='items[" + arrayCount + "].tdsApproved' value=0/></td>" +
						"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='description" + arrayCount + "' name='items[" + arrayCount + "].description' path='items[" + arrayCount + "].description' value='" + value.id + "'/></td>" +
						"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='modelNumber" + arrayCount + "' name='items[" + arrayCount + "].modelNumber' path='items[" + arrayCount + "].modelNumber' value=''/></td>" +
						"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='designQty" + arrayCount + "' name='items[" + arrayCount + "].designQty' path='items[" + arrayCount + "].designQty' value=0 ></td>" +
						"</tr>";
					$("#salesTable tbody").append(soItems);
					arrayCount++;
				} else {
					$.each(value.designItems, function (i, v) {

						if (i != 0) {
							var slNo = "";
							var description = "";
							var modelNumber = "";
							var poQty = "";
							var unitName = "";
						} else {
							slNo = value.slNo;
							description = value.description;
							salesItemId = value.id;
							modelNumber = value.modelNo;
							poQty = value.quantity;
							unitName = value.item_units.name;
						}
						var model = v.itemId;
						var qty = v.quantity;
						var itemId = v.itemMasterId;

						var soItems = "<tr><td width='4%'>" + slNo + "</td><td width='20%'>" + description + "</td>" +
							"<td width='12%'>" + modelNumber + "</td><td width='6%'>" + poQty + "</td><td width='4%'>" + unitName + "</td>" +
							"<td width='7%'>" + model + "</td><td width='5%'>" + qty + "</td>" +
							"<td width='6%'><input type='checkbox' class='form-control form-control-sm PositionofTextbox tdsApproved' id='tdsApproved" + arrayCount + "' name='items[" + arrayCount + "].tdsApproved' path='items[" + arrayCount + "].tdsApproved'/></td>" +
							"<td width='15%'><div class='lot-container' data-arraycount='" + arrayCount + "' data-lotindex='0' data-poqty='" + qty + "'></div></td>" +
							"<td width='5%'><button type='button' class='btn btn-success btn-sm add-lot-btn' style='display:none;' onclick='addLotRow(this)'>+</button></td>" +
							"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='description" + arrayCount + "' name='items[" + arrayCount + "].description' path='items[" + arrayCount + "].description' value='" + value.id + "'/></td>" +
							"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='modelNumber" + arrayCount + "' name='items[" + arrayCount + "].modelNumber' path='items[" + arrayCount + "].modelNumber' value='" + itemId + "'/></td>" +
							"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='designQty" + arrayCount + "' name='items[" + arrayCount + "].designQty' path='items[" + arrayCount + "].designQty' value='" + qty + "'/></td>" +
							"</tr>";
						$("#salesTable tbody").append(soItems);
						arrayCount++;

					})
				}

			})

		},
		complete: function (resp) {
			if (resp.status == 500) {
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : " + resp.responseJSON["errorMessage"])
			}
		},
		error: function (e) {
			console.log(e);
		}
	});
}


$(document).on("click", ".tdsApproved", function () {
	var tr = $(this).closest("tr");
	var container = tr.find('.lot-container');
	var addBtn = tr.find('.add-lot-btn');
	if ($(this).is(':checked')) {
		var index = tr.index();
		var soNumber = $('#soNumber').val();
		var salesItemId = $('#description' + index).val();
		var itemMasterId = $('#modelNumber' + index).val();
		if (soNumber && salesItemId) {
			$.ajax({
				url: api.TDS_LOTS,
				data: { soNumber: soNumber, salesItemId: salesItemId, itemMasterId: itemMasterId || '' },
				dataType: 'json',
				success: function (lots) {
					if (lots && lots.length > 0) {
						for (var j = 0; j < lots.length; j++) {
							container.append(getLotFieldsHtml(container.data('arraycount'), j, lots[j].lotNumber, lots[j].quantity, true));
						}
						container.data('lotindex', lots.length);
						updateAddBtn(container);
					} else {
						addFirstLot(container);
						addBtn.show();
					}
				},
				error: function () {
					addFirstLot(container);
					addBtn.show();
				}
			});
		} else {
			addFirstLot(container);
			addBtn.show();
		}
	} else {
		clearLots(container);
		addBtn.hide();
	}

})

function getDesignItemList(salesItemId, value) {
	$.ajax({
		method: 'GET',
		url: api.GET_DESIGN_ITEMS_BY_SALESITEM_ID + "?salesItemId=" + salesItemId,
		success: function (response) {
			console.log(response);
			$.each(response, function (i, v) {
				var model = v.modelNo;
				var qty = v.quantity;
				var designItems = "<tr><td width='7%'>" + model + "</td><td width='5%'>" + qty + "</td></tr>"
				$("#designItemsTable tbody").append(designItems);
			})
		},
		complete: function (resp) {
			if (resp.status == 500) {
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : " + resp.responseJSON["errorMessage"])
			}
		},
		error: function (e) {
			console.log(e);
		}
	})
}

$(document).on('submit', '#salesOrderTdsForm', function (e) {

	if ($("input[type='checkbox']#tdsApproved").is(':checked')) {
		$("#tdsApproved").val(true);
	} else {
		$("#tdsApproved").val(false);
	}
})

$(document).on('input', '.lot-container input[name$=".quantity"]', function () {
	var container = $(this).closest('.lot-container');
	updateAddBtn(container);
});

$(document).on('blur', '.lot-container input[name$=".quantity"]', function () {
	var $input = $(this);
	var container = $input.closest('.lot-container');
	var poQty = parseFloat(container.data('poqty'));
	if (isNaN(poQty) || poQty <= 0) return;
	var currentVal = parseFloat($input.val()) || 0;
	var otherTotal = 0;
	container.find('.lot-entry input[name$=".quantity"]').each(function () {
		if (this !== $input[0]) {
			var val = parseFloat($(this).val());
			if (!isNaN(val)) {
				otherTotal += val;
			}
		}
	});
	var maxAllowed = Math.max(0, poQty - otherTotal);
	if (currentVal > maxAllowed) {
		$input.val(maxAllowed);
	}
	updateAddBtn(container);
});