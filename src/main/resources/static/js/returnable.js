$(document).ready(function() {
	
	loadReturnableTable();
});

function loadReturnableTable(){
	$.each(dcItemList,function(index,value){
		$("#partyId").val(value.clientId);
		$("#dcId").val(value.dcNum);
		var returnableItems = "<tr><td width='5%' class='styleOfSlNo quotationNo' id='quotationNo"+index+"'>"+value.slNo+"</td>" +
		"<td width='40%'>" + value.description + "</td>" +
		"<td width='15%' id='modelNo"+index+"'>" +value.soModelNo + "</td>" +
		"<td width='12%' id='units"+index+"'>" +value.unit + "</td>" +
		"<td width='7%' id='totalQty"+index+"'>" + value.totalQuantity + "</td>" +
		"<td width='7%' id='todaysQty"+index+"' >"+value.todaysQty+"</td>" +
		"<td width='7%'><input type='text' id='returnedQty"+index+"' name='items["+index+"].returnedQty' class='form-control PositionofTextbox returnedQty'/></td>" +
		"<td style='display:none'><input class='dcItemId' id='dcItemId"+index+"' name='items["+index+"].dcItemId' value='" + value.dcItemId + "' /></td></tr>"
		$("#returnableTable tbody").append(returnableItems);
	})	
}

$(document).on('submit', '#returnableForm', function(e) {
	var rowCount=$('#returnableTable >tbody  > tr').length;
	if(rowCount==0){
		e.preventDefault();
		$.error("no items found");
	}
	
	$('#returnableTable > tbody  > tr > td > input.returnedQty').each(function(index, input) { 
		var returnedQty=parseFloat($("#returnedQty"+index).val());
		var returnedQtyInString=$("#returnedQty"+index).val();
		let totalQty=parseFloat($("#totalQty"+index).text());
		var deliveredQty=parseFloat($("#todaysQty"+index).text());
		var remainingQty=parseFloat(totalQty-deliveredQty);
		var digits= new RegExp(/^[0-9]+$/);
		let row=index+1
		if(returnedQtyInString==""){
			$("#returnedQty"+index).val(0);
		}else if(returnedQty > totalQty ) {
			e.preventDefault();	
            $.error("Entered returned Quantity is greater than Total Quantity at row " +row);
            $("#returnedQty"+index).addClass('border-color');
		}else if(returnedQty > deliveredQty ) {
			e.preventDefault();	
            $.error("Entered returned Quantity is greater than delivered Quantity at row " +row);
            $("#returnedQty"+index).addClass('border-color');
		}/*else if(!returnedQtyInString.match(digits)){
			e.preventDefault();	
			$.error("Only Digits are allowed for returned quantity at row " +row);
			$("#returnedQty"+index).addClass('border-color');
		}*/else{
			$("#todaysQty"+index).removeClass('border-color');
			
		}
    });	
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#savereturnedItemsBtn").attr('disabled', false);
		});
	 $("#savereturnedItemsBtn", this)
     .attr('disabled', 'disabled');
	
	
});