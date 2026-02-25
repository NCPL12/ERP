/**
 * 
 */
$(document).ready(function () {
	if (typeof clientList === 'undefined') clientList = [];
	if (typeof vendorList === 'undefined') vendorList = [];

	if (typeof stockSummaryError !== 'undefined' && stockSummaryError) {
		$.error(stockSummaryError);
	}

	$("#reportFromDate,#reportToDate,#reportDate,#reportByRegionFromDate,#reportByRegionToDate,#date,#poListByFromDate,#poListByToDate,#grnreportByRegionFromDate,#grnreportByRegionToDate").datepicker({
		dateFormat: 'dd-mm-yy'
		
	});

	$("#reportFromDate").change(function(){ $("#reportFromDate").removeClass("border-color"); });
	$("#reportToDate").change(function(){ $("#reportToDate").removeClass("border-color"); });
	$("#reportDate").change(function(){ $("#reportDate").removeClass("border-color"); });
	$("#reportByRegionFromDate").change(function(){ $("#reportByRegionFromDate").removeClass("border-color"); });
	$("#reportByRegionToDate").change(function(){ $("#reportByRegionToDate").removeClass("border-color"); });
	$("#date").change(function(){ $("#date").removeClass("border-color"); });
	$("#region").change(function(){ $("#region").removeClass("border-color"); });
	$("#client").change(function(){ $("#client").removeClass("border-color"); });
	$("#clientDcPEnding").change(function(){ $("#clientDcPEnding").removeClass("border-color"); });
	$("#clientInPendingReport").change(function(){ $("#clientInPendingReport").removeClass("border-color"); });
	$("#poNumber").change(function(){ $("#poNumber").removeClass("border-color"); });
	$("#poItemHistoryReport").change(function(){ $("#poItemHistoryReport").removeClass("border-color"); });
	$("#itemId").change(function(){ $("#itemId").removeClass("border-color"); });
	$("#designItemId").change(function(){ $("#designItemId").removeClass("border-color"); });
	$("#poListByFromDate").change(function(){ $("#poListByFromDate").removeClass("border-color"); });
	$("#poListByToDate").change(function(){ $("#poListByToDate").removeClass("border-color"); });
	$("#clientName").change(function(){ $("#clientName").removeClass("border-color"); });
	$("#grnreportByRegionFromDate").change(function(){ $("#grnreportByRegionFromDate").removeClass("border-color"); });
	$("#grnreportByRegionToDate").change(function(){ $("#grnreportByRegionToDate").removeClass("border-color"); });

	//getPartyList();
	getPoItemList();
	getDesignItemList();
	getPoNumberList();
	$.each(clientList, function (index, value) {
		var clientPartyList = value.partyName;
		clientPartyList = clientPartyList.split(' ');
		if (clientPartyList.length > 100) {
			clientPartyList.splice(100);
		}
		clientPartyList = clientPartyList.join(' ');
		var length = $.trim(clientPartyList).length;
		if (length > 30) {
			clientPartyList = $.trim(clientPartyList).substring(0, 30) + "....";
		}
		$('#client').append('<option value=' + value.id + '>' + clientPartyList + '</option>'); 
		$('#clientDcPEnding').append('<option value=' + value.id + '>' + clientPartyList + '</option>');
		$('#clientName').append('<option value=' + value.id + '>' + clientPartyList + '</option>'); 
	});
	$.each(vendorList, function (index, value) {
		var vendorPartyList = value.partyName;
		vendorPartyList = vendorPartyList.split(' ');
		if (vendorPartyList.length > 100) {
			vendorPartyList.splice(100);
		}
		vendorPartyList = vendorPartyList.join(' ');
		var length = $.trim(vendorPartyList).length;
		if (length > 30) {
			vendorPartyList = $.trim(vendorPartyList).substring(0, 30) + "....";
		}
		$('#clientInPendingReport').append('<option value=' + value.id + '>' + vendorPartyList + '</option>'); 
	});
});
//on submit of stockHistory by date form
$(document).on('submit', '#stockHistoryForm',function(e){
	var reportDate=$("#reportDate").val();
	if(reportDate=="" || reportDate==undefined){
		$.error("Please select the Date");
		e.preventDefault();
		$("#reportDate").addClass("border-color");
	}
});

//on submit of pending dc report
$(document).on('submit', '#dcReportForm',function(e){
	var clientName=$("#clientDcPEnding").val();
	if(clientName=="" || clientName==undefined){
		$.error("Please select the Client");
		e.preventDefault();
		$("#clientDcPEnding").addClass("border-color");
	}
});

//on submit of pending po report
$(document).on('submit', '#pendingReportForm',function(e){
	var vendorName=$("#clientInPendingReport").val();
	if(vendorName=="" || vendorName==undefined){
		$.error("Please select the Vendor");
		e.preventDefault();
		$("#clientInPendingReport").addClass("border-color");
	}
});

$(document).on('submit', '#pendingPoByPoNumberReportForm',function(e){
	var poNumber=$("#poNumber").val();
	if(poNumber=="" || poNumber==undefined){
		$.error("Please select the poNumber");
		e.preventDefault();
		$("#poNumber").addClass("border-color");
	}
});

//on submit of oustanding stock report
$(document).on('submit', '#outtandingReportForm',function(e){
	var client=$("#client").val();
	if(client=="" || client==undefined){
		$.error("Please select the Client");
		e.preventDefault();
		$("#client").addClass("border-color");
	}
});
//on submit of stock summary by date form
$(document).on('submit', '#stockSummaryForm',function(e){
	var reportFromDate=$("#reportFromDate").val();
	var reportToDate=$("#reportToDate").val();
	var isValid = true;

	if(reportFromDate=="" || reportFromDate==undefined){
		$.error("Please select the From Date");
		e.preventDefault();
		$("#reportFromDate").addClass("border-color");
		isValid = false;
	}
	if(reportToDate=="" || reportToDate==undefined){
		$.error("Please select the To Date");
		e.preventDefault();
		$("#reportToDate").addClass("border-color");
		isValid = false;
	}

	if(isValid){
		var toDate = new Date(reportToDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDate = new Date(reportFromDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDateInMillis = fromDate.getTime();
		var toDateInMillis  = toDate.getTime();

		if(toDateInMillis < fromDateInMillis){
			$.error("To Date cannot be lesser than From Date");
			e.preventDefault();
		}
	}
});

$(document).on('submit', '#stockRegionFrom',function(e){
	var reportByRegionFromDate=$("#reportByRegionFromDate").val();
	var reportByRegionToDate=$("#reportByRegionToDate").val();
	var isValid = true;

	var region=$("#region").val();
	if(region=="" || region==undefined){
		$.error("Please select the Region");
		e.preventDefault();
		$("#region").addClass("border-color");
		isValid = false;
	}
	if(reportByRegionFromDate=="" || reportByRegionFromDate==undefined){
		$.error("Please select the From Date");
		e.preventDefault();
		$("#reportByRegionFromDate").addClass("border-color");
		isValid = false;
	}
	if(reportByRegionToDate=="" || reportByRegionToDate==undefined){
		$.error("Please select the To Date");
		e.preventDefault();
		$("#reportByRegionToDate").addClass("border-color");
		isValid = false;
	}

	if(isValid){
		var toDate = new Date(reportByRegionToDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDate = new Date(reportByRegionFromDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDateInMillis = fromDate.getTime();
		var toDateInMillis  = toDate.getTime();

		if(toDateInMillis < fromDateInMillis){
			$.error("To Date cannot be lesser than From Date");
			e.preventDefault();
		}
	}
});

$(document).on('submit', '#stockReportByDateForm',function(e){
	var reportByDate=$("#date").val();
	if(reportByDate=="" || reportByDate==undefined){
		$.error("Please select the Date");
		e.preventDefault();
		$("#date").addClass("border-color");
	}
});

$(document).on('submit', '#poItemHistoryForm',function(e){
	var poitemName=$("#poItemHistoryReport").val();
	if(poitemName=="" || poitemName==undefined){
		$.error("Please select the PO item");
		e.preventDefault();
		$("#poItemHistoryReport").addClass("border-color");
	}
});

$(document).on('submit', '#salesListForm',function(e){
	var item=$("#itemId").val();
	if(item=="" || item==undefined){
		$.error("Please select the item");
		e.preventDefault();
		$("#itemId").addClass("border-color");
	}
});
$(document).on('submit', '#DcListByItemForm',function(e){
	var item=$("#designItemId").val();
	if(item=="" || item==undefined){
		$.error("Please select the item");
		e.preventDefault();
		$("#designItemId").addClass("border-color");
	}
});

function getPartyList(){
	$.ajax({
	    Type:'GET',
	    url : api.PARTY_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	
	    	$("#clientDcPEnding option:not(:first)").remove();
	    	$("#clientInPendingReport option:not(:first)").remove();
			$("#client option:not(:first)").remove();
	    	$.each(response, function( key, value ) {
				var clientList = value.partyName;
				clientList = clientList.split(' ');
				if (clientList.length > 100) {
					clientList.splice(100);
				}
				clientList = clientList.join(' ');
				var length = $.trim(clientList).length;
				if (length > 30) {
					clientList = $.trim(clientList).substring(0, 30) + "....";
				}
				$('#clientDcPEnding').append('<option value=' + value.id + '>' + clientList + '</option>'); 
				$('#clientInPendingReport').append('<option value=' + value.id + '>' + clientList + '</option>'); 
				$('#client').append('<option value=' + value.id + '>' + clientList + '</option>'); 
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

function getPoItemList(){
	$.ajax({
	    Type:'GET',
	    url : api.GET_POITEMSINPO,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	
	    	$("#poItemHistoryReport option:not(:first)").remove();
	    	
	    	$.each(response, function( key, value ) {
	    		$('#poItemHistoryReport').append('<option value=' + key + '>' + value + '</option>'); 
	    		
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



function getDesignItemList(){
	$.ajax({
	    Type:'GET',
	    url : api.GET_ITEM_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	
	    	$("#itemId option:not(:first)").remove();
	    	$("#designItemId option:not(:first)").remove();
	    	$.each(response, function( key, value ) {
	    		$('#itemId').append('<option value=' + value.id + '>' + value.model + '</option>'); 
	    		$('#designItemId').append('<option value=' + value.id + '>' + value.model + '</option>'); 
	    		
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
function getPoNumberList(){
	$.ajax({
	    Type:'GET',
	    url : api.PURCHASE_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	
	    	$("#poNumber option:not(:first)").remove();
	    	
	    	$.each(response, function( key, value ) {
	    		$('#poNumber').append('<option value=' + value.poNumber + '>' + value.poNumber + '</option>'); 
	    		
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

$(document).on('submit', '#poLostByDateForm',function(e){
	var reportFromDate=$("#poListByFromDate").val();
	var reportToDate=$("#poListByToDate").val();
	var isValid = true;

	if(reportFromDate=="" || reportFromDate==undefined){
		$.error("Please select the From Date");
		e.preventDefault();
		$("#poListByFromDate").addClass("border-color");
		isValid = false;
	}
	if(reportToDate=="" || reportToDate==undefined){
		$.error("Please select the To Date");
		e.preventDefault();
		$("#poListByToDate").addClass("border-color");
		isValid = false;
	}

	if(isValid){
		var toDate = new Date(reportToDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDate = new Date(reportFromDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDateInMillis = fromDate.getTime();
		var toDateInMillis  = toDate.getTime();

		if(toDateInMillis < fromDateInMillis){
			$.error("To Date cannot be lesser than From Date");
			e.preventDefault();
		}
	}
});

$(document).on('submit', '#activeSalesOrderForm',function(e){
	var item=$("#clientName").val();
	if(item=="" || item==undefined){
		$.error("Please select the Client Name");
		e.preventDefault();
		$("#clientName").addClass("border-color");
	}
});

$(document).on('submit', '#grnByDateForm',function(e){
	var grnreportByRegionFromDate=$("#grnreportByRegionFromDate").val();
	var grnreportByRegionToDate=$("#grnreportByRegionToDate").val();
	var isValid = true;

	if(grnreportByRegionFromDate=="" || grnreportByRegionFromDate==undefined){
		$.error("Please select the From Date");
		e.preventDefault();
		$("#grnreportByRegionFromDate").addClass("border-color");
		isValid = false;
	}
	if(grnreportByRegionToDate=="" || grnreportByRegionToDate==undefined){
		$.error("Please select the To Date");
		e.preventDefault();
		$("#grnreportByRegionToDate").addClass("border-color");
		isValid = false;
	}

	if(isValid){
		var toDate = new Date(grnreportByRegionToDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDate = new Date(grnreportByRegionFromDate.replace( /(\d{2})-(\d{2})-(\d{4})/, "$2/$1/$3"));
		var fromDateInMillis = fromDate.getTime();
		var toDateInMillis  = toDate.getTime();

		if(toDateInMillis < fromDateInMillis){
			$.error("To Date cannot be lesser than From Date");
			e.preventDefault();
		}
	}
});
