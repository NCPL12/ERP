/**

 * 
 */
var rowCountofAddeditems;
var designHiddenField;
var modalOpenFlag=false;
var designLengthCheckFlag=false;
var inputFlag=false;
var clientPOTable;
$(document).ready(function() {
	// Tie global loader to AJAX lifecycle so users see feedback while items load
	if (typeof showLoader === 'function') {
		try { showLoader(); } catch (e) {}
	}
	if (typeof $(document).ajaxStart === 'function') {
		$(document).ajaxStart(function () {
			if (typeof showLoader === 'function') {
				try { showLoader(); } catch (e) {}
			}
		});
		$(document).ajaxStop(function () {
			if (typeof hideLoader === 'function') {
				try { hideLoader(); } catch (e) {}
			}
		});
	}
	
	//display client po number list.
	$.each(salesList,function(index,value){
		var clientPoNumber=value.clientPoNumber;
		if(clientPoNumber!=null){
			if(clientPoNumber!=""){
		
			$("#clientPoDropdown").append('<option value='+value.id+'>'+value.clientPoNumber+'</option>');
			}
		}
	});
	
	$.each(clientList,function(index,value){
		
			$("#partyName").append('<option value='+value.id+'>'+value.partyName+'</option>');
		
	});
	
	//getSalesItemsbySalesItemId();

	//Remove row from table
	$("#dcTable").on("click", ".deleteButton", function() {
		$(this).closest("tr").remove();
		//Setting name and id to remaining rows
		$('tbody').find('tr').each(function (index) {
			let prev=index-1;
			let firstTdElement = $(this).find('td')[0];
			$(firstTdElement).find('input').attr('name','items['+index+'].slNo');
			$(firstTdElement).find('input').attr('id','slNo'+ index);
			//$(firstTdElement).text(index + 1);
			let secondTdElement=$(this).find('td')[1];
			$(secondTdElement).find('select').attr('name','items['+index+'].description');
			$(secondTdElement).find('select').attr('id','descriptionDropdown'+ index);
			let thirdTdElement=$(this).find('td')[2];
			$(thirdTdElement).find('input').attr('name','items['+index+'].soModelNo');
			$(thirdTdElement).find('input').attr('id','modelNo'+ index);
			$(fifthTdElement).find('input').attr('id', 'totalQty'+index);
			let sixthTdElement=$(this).find('td')[5];
			$(sixthTdElement).find('input').attr('name', 'items['+index+'].deliveredQuantity');
			$(sixthTdElement).find('input').attr('id', 'deliveredQty'+index);
			let seventhTdElement=$(this).find('td')[6];
			$(seventhTdElement).find('input').attr('name', 'items['+index+'].todaysQty');
			$(seventhTdElement).find('input').attr('id', 'todaysQty'+index);
			let eigthTdElement=$(this).find('td')[7];
			$(eigthTdElement).attr('id', 'designTd'+index);
			$(eigthTdElement).find('a').attr('id', 'design'+index);
			let ninthTdElement=$(this).find('td')[8];
			$(ninthTdElement).find('input').attr('id', 'remainingQty'+index);
			let tenthTdElement=$(this).find('td')[9];
			$(tenthTdElement).find('input').attr('id', 'designArrData'+index);
			let eleventhTdElement=$(this).find('td')[10];
			$(eleventhTdElement).find('input').attr('id', 'availableStockQty'+index);
		});
	});
	
	//validation on change of delivered qty more than total qty
	$('#dcTable').on("change",'.todaysQty',function(){
	    var index=$(this).closest("tr").index();
	    var row = index+1;
	    var totalQtyInString=$(this).closest("tr").find("td").eq(4).text();
	    var todaysQtyInString=$(this).closest("tr").find("td").eq(6).find('input').val();
	    var salesItemId=$(this).closest("tr").find("td:eq(8)").find("input").val();
		var description = $(this).closest("tr").find("td:eq(1)").text();
		designHiddenField = $(this).closest("tr").find("td:eq(9)").find("input[name='designArrData']");
		var deliveredQty =parseFloat($(this).closest("tr").find("td").eq(5).text());
	    var totalQty=parseFloat(totalQtyInString);
	    var todaysQty=parseFloat(todaysQtyInString);
	   // var salesItemId=$(this).closest("tr").find("td").eq(1).find('select').val();
	    var remainingQty = Math.round((totalQty-deliveredQty) * 100) / 100;
	    var digits= new RegExp(/^[0-9]+$/);
	    var stockQty =parseFloat($("#availableStockQty"+index).val())
	    stockQty=Math.round(stockQty * 100) / 100;
		if(todaysQty>remainingQty){
			$.error("Entered Today's Quantity is greater than " +remainingQty + " (remaining Quantity to be Delivered) at row " + row);
			$("#todaysQty"+index).addClass('border-color');
			modalOpenFlag=false;
		}else if(todaysQty>totalQty){
			$.error("Entered Today's Quantity is greater than Total Quantity at row " + row);
			$("#todaysQty"+index).addClass('border-color');
			modalOpenFlag=false;
		}else if(todaysQtyInString==""){
	    	$.error("Please enter Todays's quantity at row " + row);
			$("#todaysQty"+index).addClass('border-color');
			modalOpenFlag=false;
		}/*else if(!todaysQtyInString.match(digits)){
			$.error("Only Digits are allowed for Today's quantity at row " +row);
			$("#todaysQty"+index).addClass('border-color');
			modalOpenFlag=false;
		}*/else if(todaysQty>stockQty){
			$.error("Entered Today's Quantity is greater than " +stockQty + " (available stock Quantity) at row " + row);
			$("#todaysQty"+index).addClass('border-color');
			modalOpenFlag=false;
		}else{
				$("#todaysQty"+index).removeClass('border-color');
				modalOpenFlag=true;
		}
		checkDesignItems(salesItemId,index,todaysQtyInString);
		$("#designHeader").text(description);
		$("#remainingdcqty").text(remainingQty);
		$("#todaysdcqty").text(todaysQty);
	})
	
	if(dcObj==""){
		$('#dcTable').on("click",'.todaysQty',function(){
			inputFlag=false;
		})
	
		$('#dcTable tbody').on("click",'tr',function(){
			inputFlag=true;
			 var index=$(this).index();
			 var salesItemId=$(this).find("td:eq(8)").find("input").val();
			 var description = $(this).find("td:eq(1)").text();
			 $("#signleDesignHeader").text("Design List for: "+description);
			 displayDesignItems(salesItemId,index);
		})
	}else{
		$('#dcTable tbody').on("click",'tr',function(){
			inputFlag=true;
			 var index=$(this).index();
			 var salesItemId=$(this).find("td:eq(11)").find("input").val();
			 var description = $(this).find("td:eq(1)").find("input").val();
			 $("#signleDesignHeader").text("Design List for: "+description);
			 displayDesignItems(salesItemId,index);
		})
	}
	
	$('#deliveryChallanForm').on('keyup keypress', function(e) {
		var keyCode = e.keyCode || e.which;
		if (keyCode === 13) { 
			e.preventDefault();
			return false;
			
		}
	});
	
	if(dcObj!=""){
		getDcObjectByDcId(dcObj.dcId);
		
	}
	
});


/*//validating quantity on only allowing numeric values
$(document).on("focusout",".deliveredQty",function(e) {
	var qtyValue = this.value;
	var digits= new RegExp(/^[0-9]+$/);
	var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
	if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {

	}
	else{
		$.error("only digits are allowed");
	}
});*/

//on change of client po dropdown get id and populate description dropdown
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
	getClientNameBySoId(soId);
	
	getItemsWithoutDesignList(soId);
	getAllDescription(soId);
	$("#descriptionDropdown0").children("option").filter(":not(:first)").remove();
	$("#totalQty0").val("");
	$("#units0").val("");
	$("#modelNo0").val("");
	$("#clientPoNumber").val("");
	$("#deliveredQty0").val("");
	$("#todaysQty0").val("");
	$("#designArrData0").val("");
	$("#availableStockQty0").val("");
	
});

function getItemsWithoutDesignList(soId){
	$.ajax({
		method : 'GET',
		url : api.GET_SALES_ITEMS_WITHOUT_DESIGN  +"?salesOrderId="+soId,
		success : function(response) {
			loadItemsList(response);
			if(response!=""){
				$("#itemsModal").modal("show");
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
var itemstable;
function loadItemsList(response){
	itemstable = $('#itemsTable').DataTable({


		"data": response,
		"destroy": true,
		"columns": [

			{
				"title":"Sl.No",
				"data": response,
				"defaultContent": "",
				render: function (data, type, row, meta) {
					return row.slNo;
				}

			},
			{  
				"title":"Description",
				"data": response,
				"defaultContent": "",
				render: function (data, type, row, meta) {
					return row.description;
				}

			},
			{
				"title":"Model No",
				"data": response,
				"defaultContent": "",
				render: function (data, type, row, meta) {
					return row.modelNo;
				}

			},
			{
				"title":"HSN",
				"data": response,
				"defaultContent": "",
				render: function (data, type, row, meta) {
					return row.hsnCode;
				}

			},
			{
				"title":"Qty",
				"data": response,
				"defaultContent": "",
				render: function (data, type, row, meta) {
					return row.quantity;
				}

			},
			{
				"title":"Unit",
				"data": response,
				"defaultContent": "",
				render: function (data, type, row, meta) {
					return row.item_units.name;
				}

			},
			{
				"title":"Amount",
				"data": response,
				"defaultContent": "",
				render: function (data, type, row, meta) {
					return row.amount;
				}

			}
			
		]
	});

}

//get client name on change of Client po order dropdown
function getClientNameBySoId(soId){
	$.ajax({
		method : 'GET',
		url : api.GET_SALESORDER_BYID  +"?salesOrderId="+soId,
		success : function(response) {
		$("#clientId").val(response.party.id);
		$("#clientName").text(response.party.partyName);
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

function deleteAllRows(){
	
	 $("#dcTable").find("tr:gt(0)").remove();
	 
	 rowCountofAddeditems=0;
	
	
	
}
//ajax call to populate description dropdown on change of Client po dropdown
function getAllDescription(soId){
	//className String is used to differentiate. since this api is used many places
	var className="dc";
	  var row=$('#dcTable > tbody  > tr').length -1;		
		$.ajax({
			type : "GET",  
			url : api.SALES_LIST_BY_SOID +"?id="+soId+"&&className="+className,
			success : function(response) {
				console.log("response:",response);
				/*if(response== null || response ==''){
					$.error("supply price is zero")
				}*/
				$("#dcTable tbody").empty();
				$.each(response,function(index,value){
					
					var dcItems = "<tr><td width='5%' class='styleOfSlNo quotationNo' id='quotationNo"+index+"'>"+value.slNo+"</td>" +
					"<td width='45%'>" + value.description + "</td>" +
					"<td width='10%' id='modelNo"+index+"'>" +value.modelNo + "</td>" +
					"<td width='10%' id='units"+index+"'>" +value.unitName + "</td>" +
					"<td width='7%' id='totalQty"+index+"'>" + value.quantity + "</td>" +
					"<td width='7%' id='deliveredQty"+index+"'>" + value.deliveredQty + "</td>" +
					"<td width='7%'><input type='text' id='todaysQty"+index+"' name='items["+index+"].todaysQty' class='form-control PositionofTextbox todaysQty'/></td>" +
					"<td style='display:none' id='designTd"+index+"' align='center'><a href='#' aria-hidden='true' class='design' id='design"+index+"'>Design</a></td>" +
					"<td style='display:none'><input class='descriptionDropdown' id='descriptionDropdown"+index+"' name='items["+index+"].description' value='" + value.id + "' /></td>" +
					"<td style='display:none'><input type='hidden' class='designArrData' id='designArrData"+index+"' name='designArrData'/></td>" +
					"<td style='display:none'><input class='modelNo' type='hidden' id='modelNo"+index+"' name='items["+index+"].soModelNo' value='"+value.modelNo+"'/></td>" +
					"<td style='display:none'><input class='dcTotalQty'  type='hidden' id='dcTotalQty"+index+"' name='items["+index+"].totalQuantity' value='"+value.quantity+"'/></td>" +
					"<td style='display:none'><input class='dcDeliveredQty'  type='hidden' id='dcDeliveredQty"+index+"' name='items["+index+"].deliveredQuantity' value='"+value.deliveredQty+"'/></td>" +
					"<td style='display:none'><input type='hidden' name='items["+index+"].slNo' id='slNo"+index+"' class='form-control PositionofTextbox slNo' value='"+value.slNo+"'/></td>" +
					"<td style='display:none'><input type='hidden' id='remainingQty"+index+"'/></td>" +
					"<td style='display:none'><input type='hidden' class='availableStockQty' id='availableStockQty"+index+"'/></td></tr>";
					$("#dcTable tbody").append(dcItems);
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

//on click of add row button called function to add rows
$(document).on("click",".add",function(){
	//var salesItemId=$("#dcTable").find('tr:last').find('td:eq(1)').find('option:selected').val();
	
	addDcRow();
});

//add rows on click of add button
function addDcRow(){
	var soId = $("#clientPoDropdown").val();
	if( soId== "" && dcObj==""){
		$.error("Plaese select the Client PO No. before adding row");
		$("#clientPoDropdown").addClass('border-color');
		return false;
	}
	$("#clientPoDropdown").removeClass('border-color');

	var newRow = $("<tr>");
	var row=$('#dcTable > tbody  > tr').length;
	var rowCount=row+1
	rowCountofAddeditems = rowCount;
	var arraycount = rowCount-1;
	var previousRow=arraycount-1;
	

	//description  validation
	var descriptionId=$("#descriptionDropdown"+previousRow).val();
	if( descriptionId== "" && dcObj==""){
		$.error("Plaese select description before adding row ");
		$("#descriptionDropdown"+previousRow).addClass('border-color');
		return false;
	}
	$("#descriptionDropdown"+previousRow).removeClass('border-color');

	
	var columns = "";
	var columns = "";

//	columns += '<td width="5%" class="styleOfSlNo" id="slNo'+ arraycount +'"></td>';
	columns += '<td width="5%" class="styleOfSlNo"><input type="text" name="items['+arraycount+'].slNo" id="slNo'+ arraycount +'" readonly="readonly" class="form-control PositionofTextbox"/></td>';
    columns += '<td width="45%"> <select class="form-control PositionofTextbox descriptionDropdown" name="items['+arraycount+'].description" id="descriptionDropdown'+ arraycount +'"  style="padding: 0;"><option value="">Select Description:</option></select><span id="descriptionDiv'+arraycount+'"></span></td>'
    columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox modelNo" readonly="readonly" name="items['+arraycount+'].soModelNo" id="modelNo'+ arraycount +'" /></td>';
    columns += '<td width="10%"><input type="text" class="form-control PositionofTextbox units" readonly="readonly" id="units'+ arraycount +'" /></td>';

	columns += '<td width="7%"><input type="text" class="form-control PositionofTextbox totalQty" readonly="readonly" name="items['+arraycount+'].totalQuantity" id="totalQty'+ arraycount +'"/> </td>';
	columns += '<td width="7%"><input type="text" class="form-control PositionofTextbox deliveredQty" readonly="readonly" name="items['+arraycount+'].deliveredQuantity" id="deliveredQty'+ arraycount +'" /></td>';
	columns += '<td width="7%"><input type="text" class="form-control PositionofTextbox todaysQty" readonly="readonly"  name="items['+arraycount+'].todaysQty" id="todaysQty'+ arraycount +'" /></td>';
	columns += '<td style="display:none" id="designTd'+arraycount+'" align="center"><a href="#" aria-hidden="true" class="design" id="design'+arraycount+'">Design</a></td>';
	columns += '<td style="display:none"><input type="hidden" id="remainingQty'+ arraycount +'" /></td>';
	columns += '<td style="display:none"><input type="hidden" class="designArrData" id="designArrData'+ arraycount +'" name="designArrData"/></td>';
	columns += '<td style="display:none"><input type="hidden" class="availableStockQty" id="availableStockQty'+ arraycount +'"/></td>';
	columns += '<td style="display:none"><input class="hiddenSalesItemId" id="hiddenSalesItemId'+ arraycount +'" type="hidden" value="" /></td>' ;
	
	/*columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>';*/
	newRow.append(columns);
	$("#dcTable").append(newRow);
	$("#descriptionDropdown"+arraycount).select2({dropdownAutoWidth : true});
	if(dcObj==""){
	getAllDescription(soId);
	}
	//getSalesItemsbySalesItemId();
	
	//reset inputs on change of description.
	$(document).on("change", "#descriptionDropdown"+arraycount , function() {
		var presentDesc = $("#descriptionDropdown"+arraycount).val();
		var i;
		for(i=0;i<arraycount;i++){
			var previousDesc = $("#descriptionDropdown"+i).val();

		if(previousDesc == presentDesc){
			$.error("Please remove duplicate items");
		}
		}
		//reset input values
		$("#slNo"+arraycount).val("");
		$("#totalQty"+arraycount).val("");
		$("#units"+arraycount).val("");
		$("#modelNo"+arraycount).val("");
		$("#deliveredQty"+arraycount).val("");
		$("#todaysQty"+arraycount).attr('readonly',false);
		$("#todaysQty"+arraycount).val("");
		$("#designArrData"+arraycount).val("");
		$("#availableStockQty"+arraycount).val("");
		
		

	});
}

//reset 1st row on change of description
$(document).on("change", "#descriptionDropdown0" , function() {
	//reset input values
	$("#slNo0").val("");
	$("#totalQty0").val("");
	$("#units0").val("");
	$("#modelNo0").val("");
	$("#todaysQty0").attr('readonly',false);
	$("#designArrData0").val("");
	$("#deliveredQty0").val("");
	$("#todaysQty0").val("");
	$("#availableStockQty0").val("");

});

//on change of description item
function getSalesItemsbySalesItemId(salesItemId,e,index){
		//$(document).on("change",'.descriptionDropdown',function(){
			//let index=$(this).closest('tr').index();
			/*var salesItemId=$(this).val();
			var description = $(this).find("option:selected").text();
			var todaysQtyInString="";
			checkDesignItems(salesItemId,index,todaysQtyInString);
			$("#designHeader").text(description);*/
			$.ajax({
				type : "GET",  
				url : api.SALES_ITEM_BY_SALESITEMID +"?id="+salesItemId,
				success : function(response) {
					if(response!=""){
						if(response.mapDesign=="mapDesign"){
							$.error("Please map design to item");
							$("#todaysQty"+index).attr("readonly","readonly");
							errorFlag=true;
							e.preventDefault();
							return false;
						}else if(response.stockError=="stockError"){
							$.error("Please assign stock to client");
							$("#todaysQty"+index).attr("readonly","readonly");
							errorFlag=true;
							return false;
							e.preventDefault();
						}else{
							/*var s = $("#slNoid"+index).val();
							$("#slNo"+index).val(response.slNo);
						$("#totalQty"+index).val(response.quantity);
						$("#units"+index).val(response.item_units.name);
						$("#modelNo"+index).val(response.modelNo);
						$("#deliveredQty"+index).val(response.deliveredQty);
						$("#remainingQty"+index).val(response.todaysQty);*/
						/*$("#todaysQty"+index).val(response.todaysQty);*/
						errorFlag=false;
						$("#todaysQty"+index).attr("readonly",false);
						}
					}else{
					
						$.error("Stock is not available");
						errorFlag=true;
						$("#todaysQty"+index).attr("readonly","readonly");
						return false;
						e.preventDefault();
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
		//});
}
/*$(document).on("change",".todaysQty", function(e){
	var index=$(this).closest("tr").index();
	var row=index+1;
	var todaysQty = $(this).val();
	var salesItemId=$(this).closest("tr").find("td:eq(1)").find("option:selected").val();
	var description = $(this).closest("tr").find("td:eq(1)").find("option:selected").text();
	designHiddenField = $(this).closest("tr").find("td:eq(9)").find("input[name='designArrData']");
		if(todaysQty!=""){
		checkDesignItems(salesItemId);
		//$("#designModal").modal("show");
		$("#designModal").modal({
			backdrop: 'static',
    		keyboard: false
		})
		$("#designHeader").text(description);
		}else{
			$.error("Please enter Todays's quantity at row " + row);
		}
		
})*/

/*$(document).on("click",".design", function(e){
	var todaysQty = $(this).closest("tr").find(".todaysQty").val();
	var salesItemId=$(this).closest("tr").find("td:eq(1)").find("option:selected").val();
	var description = $(this).closest("tr").find("td:eq(1)").find("option:selected").text();
	designHiddenField = $(this).closest("tr").find("td:eq(9)").find("input[name='designArrData']");
	if(todaysQty=="" || todaysQty==0){
		$.error("Please enter Today's Qty");
		e.preventDefault();
	}else{
		
		checkDesignItems(salesItemId);
		$("#designModal").modal("show");
		$("#designHeader").text(description);
	}
})*/
function checkDesignItems(salesItemId,rowIndex,todaysQtyInString){
	var clientId=$("#clientId").val();
		$.ajax({
			method :'GET',
			url:api.SO_DESIGNITEM_LIST + "?salesItemId=" + salesItemId,
			success:function(response){
				console.log(response);
					if(response.length==1){
						designLengthCheckFlag=false;
						checkStockAvailable(response[0].itemMasterId,clientId,rowIndex);
						$("#designArrData"+rowIndex).val("[]");
					}else if(response.length>1){
						designLengthCheckFlag=true;
						$("#designTable tbody").empty();
						$.each(response,function(index,value){
							var todaysQty=parseFloat(value.quantity)-parseFloat(value.deliveredQty);
							checkStockAvailable(value.itemMasterId,clientId,index)
							var designItems = "<tr><td width='20%'>" + value.itemId + "</td>" +
							"<td width='20%' id='totalDesignQty"+index+"'>" + value.quantity + "</td>" +
							"<td width='20%'><input type='text' class='form-control PositionofTextbox deliveredQtyDesignQty' id='deliveredQtyDesignQty"+index+"' readonly='readonly' value='"+value.deliveredQty+"'/></td>" +
							"<td width='20%'><input type='text' class='form-control PositionofTextbox todaysDesignQty' id='todaysDesignQty"+index+"'/></td>" +
							"<td style='display:none'><input type='hidden' id='itemId"+index+"' class='itemId' value='"+value.itemMasterId+"'></td>" +
							"<td style='display:none'><input type='hidden' value='"+value.designId+"'></td>" +
							"<td style='display:none'><input type='hidden' id='remainingDesignQty"+index+"' value='"+todaysQty+"'></td>" +
							"<td style='display:none'><input type='hidden' id='stockQty"+index+"'></td></tr>";
							$("#designTable tbody").append(designItems);
						});
					}else{
						errorFlag=true;
					}
				
			},  
			complete:function(resp){
				if(resp.status==500){
					$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
				}
				if(modalOpenFlag==true && designLengthCheckFlag==true && todaysQtyInString!=""){
					
					$("#designModal").modal({
						backdrop: 'static',
						keyboard: false
					})
					
					
				}
			},
			error : function(e) {
				console.log(e);
			}  	
		})
	
}
function  displayDesignItems(salesItemId,index){
	$.ajax({
		method :'GET',
		url:api.SO_DESIGNITEM_LIST_BY_SOITEMID + "?salesItemId=" + salesItemId,
		success:function(response){
			console.log(response);
				
				
					
					$("#singleDesignTable tbody").empty();
					$.each(response,function(index,value){
						
						var designItems = "<tr><td width='50%''> " + value.itemId + "</td>" +
								"<td width='50%''> " + value.presentQty + "</td></tr>";
						$("#singleDesignTable tbody").append(designItems);
					});
				
			
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
			if(inputFlag==true){
				$("#signleDesignModal").modal("show")
			}
			
		},
		error : function(e) {
			console.log(e);
		}  	
	})
}


$(document).on("click","#saveDesign",function(e){
	var errorCheckFlag=true;
	var designArrData=[];
	var clientId=$("#clientId").val();
	
		/*$('#designTable > tbody  > tr > td > input.todaysDesignQty').each(function(index, input) { 
			var todaysDesignQty=$("#todaysDesignQty"+index).val();
			let row=index+1
			if(todaysDesignQty==""){
				e.preventDefault();	
				$.error("Pleaser enter Today's Design Quantity at row " +row);
				$("#todaysDesignQty"+index).addClass('border-color');
				errorCheckFlag=true;
			}else{
				$("#todaysDesignQty"+index).removeClass('border-color');
				errorCheckFlag=false;
			}
		});*/
	   $('#designTable > tbody  > tr > td > input.todaysDesignQty').each(function(index, input) { 
		   var todaysDesignQtyInString=$("#todaysDesignQty"+index).val();
			var todaysDesignQty=parseFloat($("#todaysDesignQty"+index).val());
			let totalDesignQty=parseFloat($("#totalDesignQty"+index).text());
			var remainingDesignQty=parseFloat($("#remainingDesignQty"+index).val());
			var itemMasterId = $("#itemId"+index).val();
			var stockQty=parseFloat($("#stockQty"+index).val());
			var digits= new RegExp(/^[0-9]+$/);
			let row=index+1;
			var todaysdcqty=parseFloat($("#todaysdcqty").text());
			var remainingdcqty=parseFloat($("#remainingdcqty").text());
			remainingDesignQty=Math.round(remainingDesignQty * 100) / 100;
			stockQty=Math.round(stockQty * 100) / 100;
			
			if(todaysDesignQty>remainingDesignQty){
				e.preventDefault();	
				$.error("Entered today's Design Quantity is greater than " +remainingDesignQty+ " (remaining quantity to be delivered) at row " +row);
				$("#todaysDesignQty"+index).addClass('border-color');
				errorCheckFlag=true;
				return false;
			}else if (todaysDesignQty > totalDesignQty ) {
					e.preventDefault();	
	              $.error("Entered today's Design Quantity is greater than Total Design Quantity at row " +row);
	              $("#todaysDesignQty"+index).addClass('border-color');
	              errorCheckFlag=true;
	              return false;
			}else if(todaysDesignQtyInString==""){
					e.preventDefault();	
					$.error("Pleaser enter Today's Design Quantity at row " +row);
					$("#todaysDesignQty"+index).addClass('border-color');
					errorCheckFlag=true;
					return false;
			}else if(todaysDesignQty>stockQty){
					e.preventDefault();	
					$.error("Entered today's Design Quantity is greater than " + stockQty+ " (available Stock) at row " +row);
					$("#todaysDesignQty"+index).addClass('border-color');
					errorCheckFlag=true;
					return false;
			}/*else if(!todaysDesignQtyInString.match(digits)){
				e.preventDefault();	
				$.error("Only Digits are allowed for Today's Design quantity at row " +row);
				$("#todaysDesignQty"+index).addClass('border-color');
				errorCheckFlag=true;
				return false;
			}*//*else if(remainingdcqty==todaysdcqty){
				if(remainingDesignQty!=todaysDesignQty){
					e.preventDefault();	
					$.error("Deliver complete quantity at row " +row);
					$("#todaysDesignQty"+index).addClass('border-color');
					errorCheckFlag=true;
					return false;
				}else{
					$("#todaysDesignQty"+index).removeClass('border-color');
					errorCheckFlag=false;
				}
			}*/
			else{
				$("#todaysDesignQty"+index).removeClass('border-color');
				errorCheckFlag=false;
			}
		
			
			
	    });	
	  
	   if(errorCheckFlag==false){
			 
		   // loop over each table row (tr)
		   $("#designTable tbody>tr").each(function(index,value){
		        var currentRow=$(this);
		    
		        var model=currentRow.find("td:eq(0)").text();
		        var designQty=currentRow.find("td:eq(1)").text();
		        var deliveredQty=currentRow.find("td:eq(2)").find("input").val();
		        var todayQty=currentRow.find("td:eq(3)").find("input").val();
		        var itemId=currentRow.find("td:eq(4)").find("input").val();
		        var designId= currentRow.find("td:eq(5)").find("input").val();
		        
		         var designObj={};
		         designObj.model=model;
		         designObj.designQty=designQty;
		         designObj.deliveredQty=deliveredQty;
		         designObj.todayQty=todayQty;
		         designObj.itemId=itemId;
		         designObj.designId= designId

		        designArrData.push(designObj);
			   })
			   
			   $(designHiddenField).val(JSON.stringify(designArrData));
		   	   $("#designModal").modal('hide');
			}
});
function checkStockAvailable(itemMasterId,clientId,rowIndex){
	var stockQty=0;
	$.ajax({
		method :'GET',
		url:api.STOCK_LIST_BY_ITEMID_AND_CLIENTID + "?itemId=" + itemMasterId +"&&clientId=" + clientId,
		success:function(response){
			console.log(response);
				 $.each(response,function(index,value){
					 stockQty=stockQty+value.quantity;
				});
				 
				 if(designLengthCheckFlag==false){
					 $("#availableStockQty"+rowIndex).val(stockQty);
				 }else{
					 $("#stockQty"+rowIndex).val(stockQty);
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
/*$(document).on('click', '#saveDeliveryChallan', function(e) {
			$("#deliveryChallanForm").submit();
});*/

var errorFlag =false;
$(document).on('submit', '#deliveryChallanForm', function(e) {
	var rowCount=$('#dcTable >tbody  > tr').length;
	if(rowCount==0){
		e.preventDefault();
		$.error("no items found");
	}
	if(errorFlag ==true){
		e.preventDefault();	
		$.error("Stock is not available,Please assign stock to client");
		$(".todaysQty").attr("readonly","readonly")
	}else{
		$(".todaysQty").attr("readonly",false);
	}
	//Client Po Number dropdown validation
	var soNumber=$('#clientPoDropdown option:selected').val();
	$('#soNumber').val(soNumber);
	if( soNumber== ""){
		e.preventDefault();	
		$.error("Plaese select the Client PO No. before submitting ");
		$("#clientPoDropdown").addClass('border-color');
	}
	
	//Description Validation
	/*$('#dcTable > tbody  > tr > td > input.descriptionDropdown').each(function(index, input) { 
		var salesItemId=$(this).val();
		var description = $(this).closest("tr").find("td:eq(1)").text();
		var todaysQtyInString="";
		checkDesignItems(salesItemId,index,todaysQtyInString);
		$("#designHeader").text(description);
		getSalesItemsbySalesItemId(salesItemId,e,index);
		     
	});*/
	
	//empty validation for delivered qty on submit
	/*$('#dcTable > tbody  > tr > td > input.deliveredQty').each(function(index, input) { 
		var deliveredQty = $("#deliveredQty"+index).val();
		if(deliveredQty == "" || deliveredQty == undefined){
			var row = index+1;
		$.error("Please enter the Delivered quantity at row "+ row);
		$("#deliveredQty"+index).addClass('border-color');
        e.preventDefault(e);
		}
		$("#deliveredQty"+index).change(function(){
			$("#deliveredQty"+index).removeClass('border-color');
		});  
	});
	*/
	
	//validation for delivered qty should be less than total qty on submit
	$('#dcTable > tbody  > tr > td > input.todaysQty').each(function(index, input) { 
		var todaysQty=parseFloat($("#todaysQty"+index).val());
		var todaysQtyInString=$("#todaysQty"+index).val();
		let totalQty=parseFloat($("#totalQty"+index).text());
		var deliveredQty=parseFloat($("#deliveredQty"+index).text());
		var remainingQty=parseFloat(totalQty-deliveredQty);
		//var digits = new RegExp(/^[-+]?[0-9]*\.?[0-9]+$/);
		var digits= new RegExp(/^[0-9]+$/);
		var stockQty =parseFloat($("#availableStockQty"+index).val());
		let row=index+1
		remainingQty=Math.round(remainingQty * 100) / 100;
		stockQty=Math.round(stockQty * 100) / 100;
		
		if(todaysQtyInString==""){
			$("#todaysQty"+index).val(0);
			$("#designArrData"+index).val("[]")
			//e.preventDefault();	
			//$.error("Today's Quantity should not be empty at row " +row);
			//$("#todaysQty"+index).addClass('border-color');
		}else if(todaysQty>remainingQty){
			e.preventDefault();	
			$.error("Entered today's Quantity is greater than " +remainingQty+ " (remaining Quantity to be Delivered) at row " +row);
			$("#todaysQty"+index).addClass('border-color');
			
		}else if(todaysQty > totalQty ) {
			e.preventDefault();	
            $.error("Entered today's Quantity is greater than Total Quantity at row " +row);
            $("#todaysQty"+index).addClass('border-color');
		}/*else if(!todaysQtyInString.match(digits)){
			e.preventDefault();	
			$.error("Only Digits are allowed for Today's quantity at row " +row);
			$("#todaysQty"+index).addClass('border-color');
		}*/else if(todaysQty>stockQty){
			e.preventDefault();	
			$.error("Entered Today's Quantity is greater than " +stockQty + " (available stock Quantity) at row " + row);
			$("#todaysQty"+index).addClass('border-color');
		}else{
			$("#todaysQty"+index).removeClass('border-color');
			
		}
    });	
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#saveDeliveryChallan").attr('disabled', false);
		});
	 $("#saveDeliveryChallan", this)
     .attr('disabled', 'disabled');
	
	/*//empty validation for design array on submit
	$('#dcTable > tbody  > tr > td > input.designArrData').each(function(index, input) { 
		var designArrData = $("#designArrData"+index).val();
		if(designArrData == "" || designArrData == undefined){
			var row = index+1;
		$.error("Please click on Design link and add quantity of Design to be delivered at row"+ row);
        e.preventDefault(e);
		}
	});*/
	
	
});

//on double click of list page display items in view page
function getDcObjectByDcId(dcId){
	$.ajax({
	    Type:'GET',
	    url : api.GET_DC_ITEMLIST_BYDCID+"?id="+dcId,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$(".add").hide();
	    	$("#buttonDiv").hide();
	    	// Set DC-level comment and make it read-only for view
	    	$("#dcComment").val(dcObj.dcComment || "");
	    	$("#dcComment").attr("readonly", true);
	    	$.each(response,function( key, value ){
	    		addDcRow();
			    	
			$("#dcTable >tbody>tr>td").find("input").attr("readOnly","readOnly");
			$("input[name='items[" +key+ "].slNo']").val(value.serialNo);
			$("input[name='items[" +key+ "].soModelNo']").val(value.soModelNo);
			$("#units"+key).val(value.unit);
			$("input[name='items[" +key+ "].deliveredQuantity']").val(value.deliveredQuantity);
			$("input[name='items[" +key+ "].totalQuantity']").val(value.totalQuantity);
			$("input[name='items[" +key+ "].todaysQty']").val(value.todaysQty);
			$('#clientPoDropdown').val(dcObj.soNumber);
			$('#clientPoDropdown').attr("disabled",true);
			$('#clientPoDropdown').select2(dcObj, {id: dcObj.soNumber, a_key:dcObj.soNumber});
			$("#clientName").text(dcObj.clientName);
			$('#clientPoDropdown,#clientName').attr("disabled",true);
            $("#descriptionDropdown" + key).replaceWith('<input type="text" class="form-control PositionofTextbox description" id="description' + key + '" name="items['+key+'].description">');
            $("#description" + key).val(value.description);
            $("#hiddenSalesItemId" + key).val(value.salesItemIdHidden);
            $('#description' + key).next(".select2-container").hide();
            $("#description"+key).attr("disabled",true);
            $("#descriptionDiv"+key). addClass('CellComment');
            $("#descriptionDiv"+key).html(value.description);
            $(".deleteButton").hide();
            $(".design").hide();
			
			
			
	    	});
	    	//$("#dcTable >tbody>tr:last").remove();
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

$(document).on("change", "#partyName" , function() {
	$("#clientModal").modal("show");
	var partyId=$(this).val();
	loadClientPoTable(partyId)
})

function  loadClientPoTable(partyId){
	$.ajax({
		method :'GET',
		url:api.SO_LIST_BY_CLIENTID + "?partyId=" + partyId,
		success:function(response){
			console.log(response);
			getClientPOList(response);	
				
				/*	
					$("#clientPOTable tbody").empty();
					$.each(response,function(index,value){
						
						var soObjs = "<tr><td width='60%'> " + value.clientPoNumber + "</td>" +
								"<td width='20%'> " + value.clientPoDate + "</td>" +
								"<td width='20%'> " + value.created + "</td></tr>";
						$("#clientPOTable tbody").append(soObjs);
					});
				*/
			
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

//get all the designation list and display in datatable
function getClientPOList(clientPoList){
	clientPOTable= $('#clientPOTable').DataTable({
		"order": [[ 2, "desc" ]],
		destroy:true,
		"aaData": clientPoList,
		"aoColumns": [
			{
				"mData": "clientPoNumber"
			},

			{ 	
				
				"mData":"clientPoDate",
				"mRender": function (data, type, row) {
					var newdate = moment(new Date(row.clientPoDate)).format("YYYY-MM-DD HH:mm:ss") ;
					var format= newdate.split(" ");
					var dateFormat = format[0].split("-");
					dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
					return dateFormat;

				}
			},
			{
				"mData": "created",
				"visible": false,
					"mRender": function ( data, type, row ) {
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
			},
{ 	
				
				"mData":"created",
				"mRender": function (data, type, row) {
					var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
					var format= newdate.split(" ");
					var dateFormat = format[0].split("-");
					dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
					return dateFormat;

				}
			}
			]
	});  
}