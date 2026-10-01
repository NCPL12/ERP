$(document).ready(function () {
	if (salesOrderObj != "") {
		getSalesItemListBySoItem(salesOrderObj);
		$("#soNumber").val(salesOrderObj.id);
	}
});

function getLotFieldsHtml(arrayCount, lotIndex, lotNumber, quantity, readonly, showAdd) {
	var lotNoVal = lotNumber || '';
	var qtyVal = (quantity !== null && quantity !== undefined) ? quantity : '';
	var qtyRoAttr = readonly ? 'readonly' : '';
	var actionBtn = '';
	if (readonly) {
		actionBtn = '';
	} else if (showAdd) {
		actionBtn = '<button type="button" class="btn btn-success btn-sm add-lot-btn" onclick="addLotRow(this)">+</button>';
	} else {
		actionBtn = '<i class="fa fa-trash" onclick="removeLot(this)" style="cursor:pointer;color:red;" aria-hidden="true"></i>';
	}
	return '<div class="lot-entry" style="display:flex;gap:4px;margin-bottom:2px;align-items:center;">' +
		'<input type="text" class="form-control form-control-sm PositionofTextbox" style="width:100px" name="items[' + arrayCount + '].lots[' + lotIndex + '].lotNumber" placeholder="Lot No" value="' + lotNoVal + '" readonly />' +
		'<input type="number" class="form-control form-control-sm PositionofTextbox" style="width:80px" name="items[' + arrayCount + '].lots[' + lotIndex + '].quantity" placeholder="Qty" value="' + qtyVal + '" step="any" ' + qtyRoAttr + ' />' +
		actionBtn +
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
		container.find('.add-lot-btn').show();
		return;
	}
	if (getTotalLotQty(container) >= poQty) {
		container.find('.add-lot-btn').hide();
	} else {
		container.find('.add-lot-btn').show();
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
	var nextLotNum = parseInt(container.data('nextlotnumber')) || (lotIndex + 1);
	container.append(getLotFieldsHtml(arrayCount, lotIndex, nextLotNum));
	container.data('lotindex', lotIndex + 1);
	container.data('nextlotnumber', nextLotNum + 1);
	updateAddBtn(container);
}

function addFirstLot(container) {
	var arrayCount = container.data('arraycount');
	container.append(getLotFieldsHtml(arrayCount, 0, 1, '', false, true));
	container.data('lotindex', 1);
	container.data('nextlotnumber', 2);
}

function removeLot(btn) {
	var entry = $(btn).closest('.lot-entry');
	var container = entry.closest('.lot-container');
	entry.remove();
	var entries = container.find('.lot-entry');
	if (entries.length == 0) {
		container.data('lotindex', 0);
		container.find('.add-lot-btn').hide();
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
			var entryEditable = !$(this).find('input[name$=".quantity"]').prop('readonly');
			if (idx == 0 && entryEditable) {
				if (!$(this).find('.add-lot-btn').length) {
					if ($(this).find('.fa-trash').length) {
						$(this).find('.fa-trash').remove();
					}
					$(this).append('<button type="button" class="btn btn-success btn-sm add-lot-btn" onclick="addLotRow(this)">+</button>');
				}
			} else {
				var delBtn = $(this).find('.fa-trash');
				if (delBtn.length) {
					delBtn.attr('onclick', 'removeLot(this)');
				}
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
		type: 'GET',
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
						"<td width='6%'><input type='hidden' name='items[" + arrayCount + "].tdsApproved' value='false'/><input type='checkbox' class='form-control form-control-sm PositionofTextbox tdsApproved' id='tdsApproved" + arrayCount + "' name='items[" + arrayCount + "].tdsApproved' value='true' disabled title='Add a design for this item before approving TDS'/></td>" +
						"<td width='15%'><div class='lot-container' data-arraycount='" + arrayCount + "' data-lotindex='0' data-poqty='" + poQty + "'></div></td>" +
						"<td width='5%'></td>" +
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
						"<td width='6%'><input type='hidden' name='items[" + arrayCount + "].tdsApproved' value='false'/><input type='checkbox' class='form-control form-control-sm PositionofTextbox tdsApproved' id='tdsApproved" + arrayCount + "' name='items[" + arrayCount + "].tdsApproved' value='true'/></td>" +
						"<td width='15%'><div class='lot-container' data-arraycount='" + arrayCount + "' data-lotindex='0' data-poqty='" + qty + "'></div></td>" +
						"<td width='5%'></td>" +
						"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='description" + arrayCount + "' name='items[" + arrayCount + "].description' path='items[" + arrayCount + "].description' value='" + value.id + "'/></td>" +
						"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='modelNumber" + arrayCount + "' name='items[" + arrayCount + "].modelNumber' path='items[" + arrayCount + "].modelNumber' value='" + itemId + "'/></td>" +
						"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='designQty" + arrayCount + "' name='items[" + arrayCount + "].designQty' path='items[" + arrayCount + "].designQty' value='" + qty + "'/></td>" +
						"</tr>";
					$("#salesTable tbody").append(soItems);
					arrayCount++;

					})
				}

			})
			loadExistingTdsItems(salesOrderObj.id);

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
	if ($(this).attr('data-saved') === 'true') {
		return false;
	}
	var tr = $(this).closest("tr");
	var container = tr.find('.lot-container');
	var checkbox = $(this);
	if (checkbox.is(':checked')) {
		var index = tr.index();
		var soNumber = $('#soNumber').val();
		var salesItemId = $('#description' + index).val();
		var itemMasterId = $('#modelNumber' + index).val();
		if (soNumber) {
			$.ajax({
				url: api.ALL_TDS_LOTS,
				data: { soNumber: soNumber },
				dataType: 'json',
				success: function (allLots) {
					// Find the max lot index across all items in this SO
					var maxLotIdx = 0;
					var itemLots = [];
					for (var i = 0; i < allLots.length; i++) {
						var lotNum = parseInt(allLots[i].lotNumber);
						if (!isNaN(lotNum) && lotNum > maxLotIdx) {
							maxLotIdx = lotNum;
						}
						if (allLots[i].description == salesItemId && (allLots[i].modelNumber || '') == (itemMasterId || '')) {
							itemLots.push(allLots[i]);
						}
					}
					if (itemLots.length > 0) {
						// Existing item — restore saved lots as readonly, allow adding more
						for (var j = 0; j < itemLots.length; j++) {
							container.append(getLotFieldsHtml(container.data('arraycount'), j, itemLots[j].lotNumber, itemLots[j].quantity, true));
						}
						container.append(getLotFieldsHtml(container.data('arraycount'), itemLots.length, maxLotIdx + 1, '', false, true));
						container.data('lotindex', itemLots.length + 1);
						container.data('nextlotnumber', maxLotIdx + 2);
						updateAddBtn(container);
					} else {
						// New item — one editable lot at the next available lot number
						var nextNum = maxLotIdx + 1;
						container.append(getLotFieldsHtml(container.data('arraycount'), 0, nextNum, '', false, true));
						container.data('lotindex', 1);
						container.data('nextlotnumber', nextNum + 1);
						updateAddBtn(container);
					}
					checkbox.attr('data-saved', 'true');
					checkbox.css('pointer-events', 'none');
				},
				error: function () {
					addFirstLot(container);
					checkbox.attr('data-saved', 'true');
					checkbox.css('pointer-events', 'none');
				}
			});
		} else {
			addFirstLot(container);
			checkbox.attr('data-saved', 'true');
			checkbox.css('pointer-events', 'none');
		}
	} else {
		clearLots(container);
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
	$('#salesTable tbody tr').each(function () {
		var checkbox = $(this).find('.tdsApproved');
		var hiddenInput = $(this).find("input[type='hidden'][name$='.tdsApproved']");
		if (checkbox.length && hiddenInput.length) {
			if (checkbox.is(':checked')) {
				hiddenInput.val('true');
			} else {
				hiddenInput.val('false');
			}
		}
	});
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

function loadExistingTdsItems(soNumber) {
	// Fetch both approved items and all lots in parallel
	$.when(
		$.ajax({ url: api.TDS_ITEMS, data: { soNumber: soNumber }, dataType: 'json' }),
		$.ajax({ url: api.ALL_TDS_LOTS, data: { soNumber: soNumber }, dataType: 'json' })
	).done(function (itemsResult, lotsResult) {
		var tdsItems = itemsResult[0];
		var allLots = lotsResult[0];
		if (!tdsItems || tdsItems.length === 0) return;

		// Index lots by "salesItemId|modelNumber" and find global max lot number
		var lotsByItem = {};
		var globalMaxLotIdx = 0;
		for (var i = 0; i < allLots.length; i++) {
			var key = allLots[i].description + '|' + (allLots[i].modelNumber || '');
			if (!lotsByItem[key]) lotsByItem[key] = [];
			lotsByItem[key].push(allLots[i]);
			var ln = parseInt(allLots[i].lotNumber);
			if (!isNaN(ln) && ln > globalMaxLotIdx) globalMaxLotIdx = ln;
		}

		$('#salesTable tbody tr').each(function () {
			var tr = $(this);
			var salesItemId = tr.find('input[id^="description"]').val();
			var modelNumber = tr.find('input[id^="modelNumber"]').val();
			for (var i = 0; i < tdsItems.length; i++) {
				var item = tdsItems[i];
				if (item.salesItemId == salesItemId && (!item.modelNumber || item.modelNumber == '' || item.modelNumber == modelNumber)) {
					var checkbox = tr.find('.tdsApproved');
					if (checkbox.length) {
						checkbox.prop('checked', true);
						checkbox.attr('data-saved', 'true');
						checkbox.css('pointer-events', 'none');
						var container = tr.find('.lot-container');
						var itemLots = lotsByItem[salesItemId + '|' + (modelNumber || '')] || [];
						for (var j = 0; j < itemLots.length; j++) {
							container.append(getLotFieldsHtml(container.data('arraycount'), j, itemLots[j].lotNumber, itemLots[j].quantity, true));
						}
						if (itemLots.length > 0) {
							container.append(getLotFieldsHtml(container.data('arraycount'), itemLots.length, globalMaxLotIdx + 1, '', false, true));
							container.data('lotindex', itemLots.length + 1);
						} else {
							container.data('lotindex', 0);
						}
						container.data('nextlotnumber', globalMaxLotIdx + 1);
						updateAddBtn(container);
					}
					break;
				}
			}
		});
	}).fail(function () {
		console.log('Error loading existing TDS items');
	});
}