/**
 * 
 */
var itemName;
var unitName;
var designTable
var getAmountBound = false;
$(document).ready(function () {
	// Show loader immediately on page init for new_salesOrder and tie to global AJAX
	if (typeof showLoader === 'function') { try { showLoader(); } catch(e) {} }
	if (typeof $(document).ajaxStart === 'function') {
		$(document).ajaxStart(function(){ if (typeof showLoader === 'function') { try { showLoader(); } catch(e) {} } });
		$(document).ajaxStop(function(){ if (typeof hideLoader === 'function') { try { hideLoader(); } catch(e) {} } });
	}
	// Fallback: if no AJAX is pending shortly after ready, hide loader
	setTimeout(function(){
		try {
			if (typeof $ !== 'undefined' && $.active === 0 && typeof hideLoader === 'function') { hideLoader(); }
		} catch(e) {}
	}, 400);
	
	if(role=="STORE"){
		
		$("#saveSalesOrder").prop("disabled",true);
	}
	//getPartyList();
	$("#SalesVal").hide();
	$(".amount").attr("readonly","readonly");
	$(".total").attr("readonly","readonly");
	$(".gst").attr("readonly","readonly");
	$(".grandTotal").attr("readonly","readonly");
	getAmount();
	//checkForEmptyValidation();
	//addSalesOrder();
	deleteSalesOrder();
	//getAmount();
	
	  $( "#clientPoDate" ).datepicker({ dateFormat: 'dd-mm-yy' });
	  $( "#projectClosureDate" ).datepicker({ dateFormat: 'dd-mm-yy' });
	  // Prevent bubbling of frequent key events from table inputs
	  $(document).on('keyup keypress input', '#salesTable input, #salesTable select', function(e){ e.stopPropagation(); });
	//to display sales list view page
	if(salesOrderObj!=""){
		getSalesOrderById(salesOrderObj.id);
		
	}
	
	$("#partyDropDown option:not(:first)").remove();
	$.each(customerPartyList, function( key, value ) {
		var customerPartyList = value.partyName;
		customerPartyList = customerPartyList.split(' ');
		if (customerPartyList.length > 100) {
			customerPartyList.splice(100);
		}
		customerPartyList = customerPartyList.join(' ');
		var length = $.trim(customerPartyList).length;
		if (length > 25) {
			customerPartyList = $.trim(customerPartyList).substring(0, 25) + "....";
		}
		$('#partyDropDown').append('<option value=' + value.id + '>' + customerPartyList + '</option>');
		});
	
	//get party 
	 $(document).on('change','#partyDropDown',function(){
		var partyId = $('#partyDropDown').val()
		$('#party').val(partyId);
		if(partyId=="C1143"){
		$("#gstRow").css("display","none");
		$("#taxDropDownRow").css("display","none");
		}else{
			$("#gstRow").show();
			$("#taxDropDownRow").show();
		}
	 })
	 $(document).on('change','#clientPoNumber',function(){
		var clientPo=$("#clientPoNumber").val();
		$('#clientPo').val(clientPo);
	 })
     $(document).on('change','#clientPoDate',function(){
		var clientPoDate=$("#clientPoDate").val();
		$('#poDate').val(clientPoDate);
	 })

	    itemName = itemList.map(({ itemName }) => itemName);
    $(".description").attr("autocomplete","off");
    $(".description").attr("spellcheck","false");
    $(".description").attr("autocapitalize","off");
    $(".description").attr("autocorrect","off");
    if ($.fn && typeof $.fn.autocomplete === 'function') {
        var $desc = $(".description");
        if ($desc.data('ui-autocomplete') || $desc.data('autocomplete')) {
            $desc.autocomplete('destroy');
        }
    }
    $(document).off('keyup keypress input', '.description');
    $(document).on('keyup keypress input', '.description', function(e){ e.stopImmediatePropagation(); });
	/* $(document).on("mouseover",".description",function(){
		 var description=$(this).val();
		 $(".description").tooltip({
			 content:description
		 });
	 })*/
	

	 $.each(unitsList,function(index,value){
			$("#unit0").append('<option value='+value.id+'>'+value.name+'</option>');
		});
		
		 $(document).on("click", ".add" , function() {
			 addSalesOrder();
		 });
		 
	 $.each(itemList,function(index,value){
			$("#itemModel0").append('<option value='+value.id+'>'+value.model+'</option>');
		});
	 if(salesOrderObj!=""){
	     // Removed per-keystroke qty handler to reduce typing lag; change handler below will handle UI updates
	 
	 $(document).on('change', '.qty', function () {
		 var index=$(this).closest("tr").index();
		 var salesItemId=$("#salesItemId"+index).val();
		 var qty=$(this).val();
		 if(salesItemId!=""){
		 if(qty==0){
			 $("#designTd"+index).hide();
		 }else{
			 $("#designTd"+index).show();
		 }
		 }
		 
	 });
	 }
	 
	 designValidation();
	
	 $("#designModal").draggable({
	      handle: ".modal-header"
	    });
});
$(document).on("blur", ".unitPrice,.servicePrice", function (e) {
    if(this.value.match(/\-/)) 
        this.value=this.value.replace(/\D/g,'')
});

//validation to enter only number or float value for qty. 

function getPartyList(){
	$.ajax({
	    Type:'GET',
	    url : api.PARTY_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
			
			$("#partyDropDown option:not(:first)").remove();
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
				$('#clientsDropdown').append('<option value=' + value.id + '>' + clientList + '</option>'); 
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


//check for empty value after clicking on save button
	function checkForEmptyValidation(){
		$("#save").on("click", function(){
			$('table > tbody  > tr > td > input').each(function(index, input) { 
				 if ($(this).val().trim() === '') {
		                $(this).addClass("has-error")
						//$('.para').text("please enter all fields").css("color","#ff0000")
		                $("#SalesVal").show();
		            }  
			});
		});
	} 

//validating quantity on only allowing numeric values
/*$(document).on("focusout",".qty,.designQty",function(e) {
        var qtyValue = this.value;
		var digits= new RegExp(/^[0-9]+$/);
		var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
		if (qtyValue.match(digits)||qtyValue=="") {

		}
		else{
        	$.error("only digits are allowed for qty");
		}
    });*/

	//adding rows on click of add button
	function addSalesOrder(){
	
		 var newRow = $("<tr>");
		 var row=$('#salesTable  > tbody  > tr').length;
		 var rowCount = row+1;
		 var arraycount = rowCount-1;
		 
        var columns = "";
	    columns += '<td width="5%"><input type="text" class="form-control PositionofTextbox slNo" id="slNo'+ arraycount +'" name="items['+arraycount+'].slNo" path="items['+arraycount+'].slNo"/></td>';
	    columns += '<td width="35%" class="CellWithComment"><input type="text" class="form-control PositionofTextbox description" autocomplete="off" id="description'+ arraycount +'" name="items['+arraycount+'].description" path="items['+arraycount+'].description"/><span id="descriptionDiv'+arraycount+'"></td>';
	    columns += '<td width="5%"><input type="text" class="form-control PositionofTextbox" maxlength = "100" id="modelNo'+ arraycount +'" name="items['+arraycount+'].modelNo" path="items['+arraycount+'].modelNo"/></td>';
	    columns += '<td width="5%"><input type="text" class="form-control PositionofTextbox" id="hsnCode'+ arraycount +'" name="items['+arraycount+'].hsnCode" path="items['+arraycount+'].hsnCode"/></td>';
	    columns += '<td width="5%"><input type="text" class="form-control PositionofTextbox" id="servicehsnCode'+ arraycount +'" name="items['+arraycount+'].servicehsnCode" path="items['+arraycount+'].servicehsnCode"/></td>';
	    columns += '<td width="8%"><input type="text" class="form-control PositionofTextbox qty" id="qty'+ arraycount +'" name="items['+arraycount+'].quantity" path="items['+arraycount+'].qty" value="0"/><span id="qtyDiv'+arraycount+'"></span></td>';
	    columns += '<td width="7%"><select class="form-control PositionofTextbox unit" id="unit'+arraycount+'" name="items['+arraycount+'].unit" path="items['+arraycount+'].unit" ><option value="">Select Unit:</option></select><span id="unitDiv'+arraycount+'"></span></td>';
	    columns += '<td width="9%"><input type="text" class="form-control PositionofTextbox unitPrice alignright"  id="unitPrice'+ arraycount +'" name="items['+arraycount+'].unitPrice" path="items['+arraycount+'].unitPrice"  value="0.0"/><span id="unitPriceDiv'+arraycount+'"></span></td>';
	    columns += '<td width="9%"><input type="text" class="form-control PositionofTextbox servicePrice alignright"  id="servicePrice'+ arraycount +'" name="items['+arraycount+'].servicePrice" path="items['+arraycount+'].serivicePrice"  value="0.0"/><span id="servicePriceDiv'+arraycount+'"></span></td>';
	    columns += '<td width="11%"><input type="text" class="form-control PositionofTextbox amount alignright"  id="amount'+ arraycount +'" name="items['+arraycount+'].amount" path="items['+arraycount+'].amount" readonly="readonly"  value="0.0"/><span id="amountDiv'+arraycount+'"></span></td>';

	    columns += '<td align="center"><i class="deleteButton fa fa-trash" aria-hidden="true"></i></td>';
	    columns += '<td class="hideTd" id="designTd'+arraycount+'" align="center"><a href="#" aria-hidden="true" class="design" id="design'+arraycount+'">Design</a></td>';
	    columns += '<td class="hideTd"><input type="hidden" id="salesItemId'+arraycount+'" name="items['+arraycount+'].id"/></td>';
	    columns +='<td style="display:none" id="toggleBriefTd'+arraycount+'" class="toggleBriefTd hideTd"><input type="checkbox" id="toggleBrief'+arraycount+'" class="toggleBrief"></td>';
        newRow.append(columns);
         $("#salesTable").append(newRow);
         // Check if expand all header should be shown/hidden after adding new row
         toggleExpandAllHeader();
        // Ensure no suggestions/autocomplete on description for new row as well
        $(".description").attr("autocomplete","off");
        $(".description").attr("spellcheck","false");
        $(".description").attr("autocapitalize","off");
        $(".description").attr("autocorrect","off");
        if ($.fn && typeof $.fn.autocomplete === 'function') {
            var $desc2 = $(".description");
            if ($desc2.data('ui-autocomplete') || $desc2.data('autocomplete')) {
                $desc2.autocomplete('destroy');
            }
        }
        $(document).off('keyup keypress input', '.description');
        $(document).on('keyup keypress input', '.description', function(e){ e.stopImmediatePropagation(); });
        
        		/*unitName = unitsList.map(({ name }) => name);
    		$(".unit").autocomplete({
    		       source: unitName
    		     });*/
     		$("#unit" + arraycount).select2({ dropdownAutoWidth: true });
    		//display unit list in dropdown
    		$.each(unitsList,function(index,value){
    			$("#unit"+ arraycount).append('<option value='+value.id+'>'+value.name+'</option>');
    		});
     		/**To display New Row **/
    		$.each(itemList,function(index,value){
    			$("#itemDropDown"+ arraycount).append('<option value='+value.id+'>'+value.model+'</option>');
    		});
    		
    		$(document).on("change", "#itemDropDown"+ arraycount, function() {
    			var itemId=$(this).val();
    			          $.ajax({
    		    				type : "GET",  
    		    				url : api.ITEM_LIST_BYID +"?id="+itemId,
    		    				success : function(response) {
    		    					$("input[name='items[" +arraycount+ "].hsnCode']").val(response.hsnCode);
    		    					$("input[name='items[" +arraycount+ "].modelNo']").val(response.model);
    		    					$("input[name='items[" +arraycount+ "].description']").val(response.itemName);
    		    					$("input[name='items[" +arraycount+ "].unitPrice']").val(response.sellPrice);
    		    					
    		    					
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
	} 
	
	$(document).on("change",".unit",function(){
		var index=$(this).closest("tr").index();
		var unitName=$("#unit"+index).find("option:selected").text()
		if(unitName=="Heading"){
			$("#qty"+index).val(0.0);
			$("#qty"+index).attr("readonly",true);
			$("#unitPrice"+index).val(0.0);
			$("#servicePrice"+index).val(0.0);
			$("#modelNo"+index).val("");
			$("#hsnCode"+index).val("");
			$("#servicehsnCode"+index).val("");
			$("#servicePrice"+index).attr("readonly",true);
			$("#unitPrice"+index).attr("readonly",true);
			$("#modelNo"+index).attr("readonly",true);
			$("#hsnCode"+index).attr("readonly",true);
			$("#servicehsnCode"+index).attr("readonly",true);
		}else{
			$("#qty"+index).attr("readonly",false);
			$("#servicePrice"+index).attr("readonly",false);
			$("#unitPrice"+index).attr("readonly",false);
			$("#modelNo"+index).attr("readonly",false);
			$("#hsnCode"+index).attr("readonly",false);
			$("#servicehsnCode"+index).attr("readonly",false);
		}
	})
	
	
	/**get sales item list by sales order id and display on double click of sales list **/
	function getSalesOrderById(soId){
		showLoader();
		//className String is used to differentiate. since this api is used many places 
		var className="so";
		var partyId=salesOrderObj.party.id;
   	 	if(partyId=="C1143"){
   			$("#gstRow").css("display","none");
   			$("#taxDropDownRow").css("display","none");
   			}else{
   				$("#gstRow").show();
   				$("#taxDropDownRow").show();
   			} 
   	 $("#soId").val(soId);
   	$("#saveSalesOrder").html("Update");
   	
	$('#party').val(salesOrderObj.party.id);
	
	var poDate=salesOrderObj.clientPoDate;
	$("#clientPoDate").replaceWith('<input type="text" class="form-control PositionofTextbox" style="width:150px" id="poDateVal" name="clientPoDate">');
	if(poDate==null){
		$("#poDateVal").val("");
	}else{
		poDate=new Date(poDate);
		var date= new Date(poDate).getUTCDate() ;
		var month= new Date(poDate).getUTCMonth()+1;
		var year=new Date(poDate).getUTCFullYear();
		//poDate=poDate.toLocaleDateString();
		//var poDateFormat = poDate.split("/"); //split date by "/"
		poDate=date+"-"+month+"-"+year; //change the format to dd/mm/yyyy to display in view page
		$("#poDateVal,#clientPoDate").val(poDate);
	}
	if(userName!="surendra"){
		$('#poDateVal,clientPoDate').attr("readonly","readonly");
		$('#clientPoNumber').attr("readonly","readonly");
	}else{
		$('#poDateVal,clientPoDate').attr("readonly",false);
		$('#clientPoNumber').attr("readonly",false);
		$('#poDateVal,clientPoDate').datepicker({ dateFormat: 'dd-mm-yy' });
	}

	//$('#clientPoDate').datepicker({ dateFormat: 'dd-mm-yy' });
	//$('#clientPoNumber').attr("readonly","readonly");
	$("#clientPoNumber,#clientPo").val(salesOrderObj.clientPoNumber);
	$("#taxDropDown").val(salesOrderObj.gstRate);
	
		$.ajax({
		    Type:'GET',
		    url : api.GET_SALES_ITEMS_LIST_BY_SALES_ORDER_ID+"?id="+soId+"&&className="+className,
		    dataType:'json',
		    async: 'false',
		    success  : function(response){
		    	//$(".add").hide();
		    	$("#resetSalesOrder").hide();
		    	$.each(response,function( key, value ){
				    	addSalesOrder();
				    	 getAmount()   	
				//$("#salesTable >tbody>tr>td").find("input").attr("readOnly","readOnly");
				
				var unitName=value.item_units.name;
				var qty=value.quantity;
			//	$("#designTd"+key).show();
				$("#toggleBriefTd"+key).show();
				$("#design"+key).val(value.id);
				
		    	
				
				$("#salesItemId"+key).val(value.id)
				$("input[name='items[" +key+ "].slNo']").val(value.slNo);
		    	$("input[name='items[" +key+ "].description']").val(value.description);
		    	$("#descriptionDiv"+key). addClass('CellComment');
		    	$("#descriptionDiv"+key).html(value.description);
		    	$("input[name='total']").val(commaSeparateNumber(salesOrderObj.total));
		    	$("input[name='gst']").val(commaSeparateNumber(salesOrderObj.gst));
		    	$("input[name='grandTotal']").val(commaSeparateNumber(salesOrderObj.grandTotal));
				$("input[name='items[" +key+ "].modelNo']").val(value.modelNo);
				$("input[name='items[" +key+ "].hsnCode']").val(value.hsnCode);
				$("input[name='items[" +key+ "].servicehsnCode']").val(value.servicehsnCode);
				$("input[name='items[" +key+ "].quantity']").val(value.quantity);
				var unitPrice = commaSeparateNumber(value.unitPrice);
				$("input[name='items[" +key+ "].unitPrice']").val(unitPrice);	
				var servicePrice = commaSeparateNumber(value.servicePrice);
				$("input[name='items[" +key+ "].servicePrice']").val(servicePrice);
				var amountPrice = commaSeparateNumber(value.amount);
				$("input[name='items[" +key+ "].amount']").val(amountPrice);
				
				/*$("#unit"+key).replaceWith('<input type="text" class="form-control PositionofTextbox unit" id="unitVal'+key+'" name="items[' +key+ '].unit">');
				$('#unitVal'+key).val(value.item_units.name);
				$('#unitVal'+ key).next(".select2-container").hide();*/
				//$('#unitVal'+key).attr("disabled",true);
				
				$("#unit"+key).val(value.item_units.id)
				$("#unit"+key).select2(value, {id: value.item_units.id, a_key:value.item_units.name});
				
				
				if(unitName=="Heading"){
					$("input[name='items[" +key+ "].servicePrice']").attr("readonly",true);
					$("input[name='items[" +key+ "].unitPrice']").attr("readonly",true);
					$("#qty"+key).attr("readonly",true);
					$("#modelNo"+key).attr("readonly",true);
					$("#hsnCode"+key).attr("readonly",true);
					$("#servicehsnCode"+key).attr("readonly",true);
					$("#designTd"+key).hide();
				}else{
					$("input[name='items[" +key+ "].servicePrice']").attr("readonly",false);
					$("input[name='items[" +key+ "].unitPrice']").attr("readonly",false);
					$("#qty"+key).attr("readonly",false);
					$("#modelNo"+key).attr("readonly",false);
					$("#hsnCode"+key).attr("readonly",false);
					$("#servicehsnCode"+key).attr("readonly",false);
					$("#designTd"+key).show();
				}
				 if(qty==0){
					 $("#designTd"+key).hide();
				 }else{
					 $("#designTd"+key).show();
				 }
				//$(".deleteButton").hide();
				checkForDcExists(value.id,key);
				
				
		    	});
		    	$('#partyDropDown').val(salesOrderObj.party.id);
		    	$('#partyDropDown').select2(salesOrderObj, {id: salesOrderObj.party.id, a_key:salesOrderObj.party.name});
		    	$('#partyDropDown').attr("disabled",true);
		    	$("#taxDropDown").attr("disabled",true);
		    	$("#salesTable >tbody>tr:last").remove();
		    	$("#taxDropDown").trigger('change');
		    	// Show expand all header since data is loaded
		    	showExpandAllHeader();
		    	$('input:not(:button,:submit),textarea,select').change(function () {
		    		isDirty = true;
		    		
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
		hideLoader();
	}
	
	
	//function to check dc exist for the particular sales item
	function checkForDcExists(salesItemId,key){
		var row=key+1;
		$.ajax({
			type : "POST",  
			url : api.DC_EXIST +"?salesItemId="+salesItemId,
			success : function(response) {
				if(response == true){
					$("#salesTable >tbody").find("tr:eq("+key+")").find("input,select,button").attr("disabled",true);
					$('.toggleBrief').attr('disabled', false);
					if(userName=="surendra" || userName=="ashwini"){
						$('.slNo').attr('disabled', false);
					}else{
						$('.slNo').attr('disabled', true);
					}
					disableDesignAddBtn(key);
				}else{
					$("#salesTable >tbody").find("tr:eq("+key+")").find("input,select,button").attr("disabled",false);
					enableDesignAddBtn(key);
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
	
	function disableDesignAddBtn(key){
		$(document).on("click","#design"+key,function(){
			$("#saveDesign").attr("disabled",true);
			$("#designTable >tbody").find("tr").find("input,select,button").attr("disabled",true);
			$(".addrow").hide();
		})
	}
	
	function enableDesignAddBtn(key){
		$(document).on("click","#design"+key,function(){
			$("#saveDesign").attr("disabled",false);
			$("#designTable >tbody").find("tr").find("input,select,button").attr("disabled",false);
			$(".addrow").show();
		})
	}
	
	
	$(document).on("change",".toggleBrief", function(){
		var index=$(this).closest("tr").index();
		let inputText =$("input[name='items[" +index+ "].description']").val();
		if($(this).is(':checked')) {
            
            $("#descriptionDiv"+index).addClass('CellComment');
	    	$("#descriptionDiv"+index).html(inputText).show();
	    	 $('#descriptionDiv'+index).css('display','block');
        } else {
        	$("#descriptionDiv"+index).addClass('CellComment');
          //  $('#descriptionDiv'+index).removeAttr('style');
            $("#descriptionDiv"+index).html(inputText).hide();
        }
	})
	 
	//on click of design link in sales order edit open design popup
	$(document).on("click",".design",function(){
		var modalHeaderDesc=$(this).closest("tr").find("td:eq(1)").find("input").val();
		var slNo=$(this).closest("tr").find("td:eq(0)").find("input").val();
		if (modalHeaderDesc.length > 35) {
			modalHeaderDesc = jQuery.trim(modalHeaderDesc).substring(0, 35) + "....";
		}
		var salesItemId=$(this).val();
		 $("#designModal").css({
		        top: "100px",
		        left: "100px"
		      }).modal("show");
		//$("#designModal").modal("show");
		$("#salesItemId").val(salesItemId);
		$("#designHeader").text(slNo+". "+modalHeaderDesc);
		$('#designForm')[0].reset();
		$(".itemModel").val("").trigger('change');
		$("#designTable").find("tbody>tr:not(:first)").remove();
		isDirty = false;
		getDesignItemList(salesItemId);
	})
	$(document).on("change","#itemModel0",function(){
        	var itemId=$(this).val();
	          $.ajax({
  				type : "GET",  
  				url : api.ITEM_LIST_BYID +"?id="+itemId,
  				success : function(response) {
  					$("#unitMod0").val(response.item_units.name)
  					
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
	//on click of addrow button in design modal
	$(document).on("click",".addrow",function(){
		addDesignRow();
	})
	
	//on click of addrow button in design modal add rows
	function addDesignRow(){
		 var newRow = $("<tr>");
		 var row=$('#designTable  > tbody  > tr').length;
		 var rowCount = row+1;
		 var arraycount = rowCount-1;
		 
        var columns = "";
        columns += '<td><select class="form-control PositionofTextbox itemModel" name="items['+arraycount+'].itemId" style="width: 100%; padding: 0px;" id="itemModel'+ arraycount +'"><option value="" selected>Select Model No:</option></td>';
        columns += '<td><input type="text" class="form-control PositionofTextbox unit" id="unitMod'+ arraycount +'" /></td>';
	    columns += '<td><input type="text" class="form-control PositionofTextbox designQty" id="quantity'+ arraycount +'" name="items['+arraycount+'].quantity"/></td>';
	    newRow.append(columns);
        $("#designTable").append(newRow);
        $("#itemModel" + arraycount).select2({ dropdownAutoWidth: true });
        
        //populate model number
        $.each(itemList,function(index,value){
			$("#itemModel"+ arraycount).append('<option value='+value.id+'>'+value.model+'</option>');
		});
        $(document).on("change","#itemModel"+ arraycount,function(){
        	var itemId=$(this).val();
	          $.ajax({
  				type : "GET",  
  				url : api.ITEM_LIST_BYID +"?id="+itemId,
  				success : function(response) {
  					$("#unitMod"+arraycount).val(response.item_units.name)
  					
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
        	
	
	}
	//validate design form
	function designValidation(){
		$('#designForm').validate({
			rules: {
			},
			submitHandler:function(form) {
				
				$("#saveDesign").attr('disabled', 'disabled');
				var designJson = {};
				var items = designTable2Json();
				designJson['salesItemId'] = $('#salesItemId').val();
				designJson['items'] = items;
				for (var i = 0; i < items.length; i++) {
					var itemId=items[i].itemId;
					if(itemId==""|| itemId==undefined){
						$.error("Please select Model Number");
						return false;
					}
				}
				for (var i = 0; i < items.length; i++) {
					var quantity=items[i].quantity;
					row=i+1;
					if(quantity==0|| quantity==undefined){
						$.error("Please enter Quantity greater than 0");
						return false;
					}
					/*var digits= new RegExp(/^[0-9]+$/);
					var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
			        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
					if (quantity.match(digits)||quantity=="") {

					}
					else{
			        	$.error("only digits are allowed for qty at row " + row);
			        	return false;
					}*/
					
				}
				//saveDesign(designJson);
				//var designJson=$(form).form2json();
				//saveDesign(designJson);
				var itemIdList=[];
				for (var i = 0; i < items.length; i++) {
					var itemId=items[i].itemId;
					itemIdList.push(itemId);
				}
				var itemListArray = itemIdList.sort(); 

				var dupItemArray = [];
				for (var i = 0; i < itemListArray.length - 1; i++) {
				    if (itemListArray[i + 1] == itemListArray[i]) {
				    	dupItemArray.push(itemListArray[i]);
				    }
				}
				if(dupItemArray.length>0){
					$.error("Please enter distinct model No");
					return false;
				}
				
				$.ajax({
					method:'GET',
					url :api.DESIGN_VALIDATE + "?itemIdList="+itemIdList+ "&&salesItemId="+designJson.salesItemId,
					success :function(response){
						if(response==false){
							saveDesign(designJson);

						}else{
							$.error("Model already exist");
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
   
				$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
					$("#saveDesign").attr('disabled', false);
					});
				
			}
		});
 	}

	
	
	
function designTable2Json(){

    var header = $('#designTable thead tr th').map(function () {
        return $(this).text();
    });

    var tableObj = $('#designTable tbody tr').map(function (i) {
        var row = {};
        $(this).find('td').each(function (i) {
            var rowName = header[i];
            if(rowName == 'Model No') {
            	row["itemId"] = $(this).find("select").val();
            }else if(rowName=='Unit'){
            	 row["unit"] = $(this).find("input").val();
            }
            
            else {
            	 row["quantity"] = $(this).find("input").val();
            	}
        	});
        	return row;
    	}).get();
    return tableObj;
	}


	//to save design
	function saveDesign(designJson){
		$.ajax({
			type : "POST",  
			url : api.ADD_SO_DESIGN,
			data : JSON.stringify(designJson),
			dataType  : "json",
			contentType: "application/json",
			success : function(response) {
				getDesignItemList(response.salesItemId);
				$('#designForm')[0].reset();
				$(".itemModel").val("").trigger('change');
				$("#designTable").find("tbody>tr:not(:first)").remove();
				$.success("Design saved successfully");
				isDirty = false;
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
	
	//ajax call to get all the design item list
	function getDesignItemList(salesItemId){
		$.ajax({
			method :'GET',
			url:api.SO_DESIGNITEM_LIST + "?salesItemId=" + salesItemId,
			success:function(response){
				console.log(response);
				designListTable(response);	
				
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
	function designListTable(designList){
		designTable= $('#designList').DataTable({
			destroy:true,
			"aaData": designList,
			"aoColumns": [
				{"title": "Model No",
					"width":"30%",
					"mData": "itemId"
				},
				{"title": "Unit",
					"width":"30%",
					"mData": "unit"
				},

				{ 	
					"title":'Quantity',
					"width":"10%",
					"mData":"quantity",
					
				},
				{	"class":"hideTd",
					"mData": "designId"
				},
				{	"class":"hideTd",
					"mData": "salesItemId"
				},
				{	"class":"hideTd",
					"mData": "itemMasterId"
				},
				{ 	
					"title":'Delete',
					"width":"10%",
					"class":"styleOfSlNo",
					render : function ( mData, type, row,meta ) {
						return '<i class="deleteDesignBtn fa fa-trash " aria-hidden="true"></i>';
					}
				}
				]
		});  
	}
	//on click of design display confirm bootbox
	$(document).on("click",".deleteDesignBtn",function(){
		var row = designTable.row($(this).closest("tr").get(0));
		var rowData=row.data();
		var id=rowData.id;
		var designId = rowData.designId;
		var salesItemId=rowData.salesItemId;
		var itemId=rowData.itemMasterId;
		bootbox.confirm({
			message: "Do you want to delete?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? deleteDesign(id,salesItemId,designId):"";
			}
		});		
	})

	//to delete the design
	function deleteDesign(id,salesItemId,designId){
		$.ajax({
			type : "POST",  
			url : api.DELETE_DESIGN +"?id="+id + "&designId="+designId,
			success : function(response) {
				if(response == true){
				getDesignItemList(salesItemId);
				$.success("Design deleted");
				}else{
					getDesignItemList(salesItemId);
					$.error("PO,Dc or Grn Created");
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

	//delete sales Order
	function deleteSalesOrder(){
	$("#salesTable").on("click", ".deleteButton", function() {
		let salesItemIdTd = $(this).closest("tr").find("td")[12];
		let salesItemId=$(salesItemIdTd).find("input").val();
		if(salesItemId!=""){
			getDesignList(salesItemId,salesItemIdTd);
		
		}else{
		   	 $(this).closest("tr").remove();
		   	adjustIndex();
		   	toggleExpandAllHeader();
		}
	});			
		getAmount();
	
	}
	
	function adjustIndex(){
		$('#salesTable tbody').find('tr').each(function (index) {
			let prev=index-1;
			let firstTdElement = $(this).find('td')[0];
			$(firstTdElement).find('input').attr('name','items['+index+'].slNo');
			$(firstTdElement).find('input').attr('path','items['+index+'].slNo');
			$(firstTdElement).find('input').attr('id','slNo'+ index);
			let secondTdElement=$(this).find('td')[1];
			$(secondTdElement).find('input').attr('name','items['+index+'].description');
			$(secondTdElement).find('input').attr('path','items['+index+'].description');
			$(secondTdElement).find('input').attr('id','description'+ index);
			let thirdTdElement=$(this).find('td')[2];
			$(thirdTdElement).find('input').attr('name', 'items['+index+'].modelNo');
			$(thirdTdElement).find('input').attr('path', 'items['+index+'].modelNo');
			$(thirdTdElement).find('input').attr('id', 'modelNo'+index);
			let forthTdElement=	$(this).find('td')[3];
			$(forthTdElement).find('input').attr('name', 'items['+index+'].hsnCode');
			$(forthTdElement).find('input').attr('path', 'items['+index+'].hsnCode');
			$(forthTdElement).find('input').attr('id', 'hsnCode'+index);
			let fifthTdElement=$(this).find('td')[4];
			$(fifthTdElement).find('input').attr('name', 'items['+index+'].servicehsnCode');
			$(fifthTdElement).find('input').attr('path', 'items['+index+'].servicehsnCode');
			$(fifthTdElement).find('input').attr('id', 'servicehsnCode'+index);
			let sixthTdElement=$(this).find('td')[5];
			$(sixthTdElement).find('input').attr('name', 'items['+index+'].quantity');
			$(sixthTdElement).find('input').attr('path', 'items['+index+'].quantity');
			$(sixthTdElement).find('input').attr('id', 'qty'+index);
			let seventhTdElement=$(this).find('td')[6];
			$(seventhTdElement).find('select').attr('name', 'items['+index+'].unit');
			$(seventhTdElement).find('select').attr('path', 'items['+index+'].unit');
			$(seventhTdElement).find('select').attr('id', 'unit'+index);
			$(seventhTdElement).find('select').select2({ dropdownAutoWidth: true });
	        let eighthTdElement=$(this).find('td')[7];
	        $(eighthTdElement).find('input').attr('name', 'items['+index+'].unitPrice');
	        $(eighthTdElement).find('input').attr('path', 'items['+index+'].unitPrice');
	        $(eighthTdElement).find('input').attr('id', 'unitPrice'+index);
	        let ninethTdElement=$(this).find('td')[8];
	        $(ninethTdElement).find('input').attr('name', 'items['+index+'].servicePrice');
	        $(ninethTdElement).find('input').attr('path', 'items['+index+'].servicePrice');
	        $(ninethTdElement).find('input').attr('id', 'servicePrice'+index);
			let tenthTdElement=$(this).find('td')[9];
			$(tenthTdElement).find('input').attr('name', 'items['+index+'].amount');
			$(tenthTdElement).find('input').attr('path', 'items['+index+'].amount');
			$(tenthTdElement).find('input').attr('id', 'amount'+index);
			let eleventhTdElement=$(this).find('td')[11];
		//	$(eleventhTdElement).find('input').attr('name', 'items['+index+'].amount');
		//	$(eleventhTdElement).find('input').attr('path', 'items['+index+'].amount');
			$(eleventhTdElement).find('input').attr('id', 'design'+index);
			let twelthTdElement=$(this).find('td')[12];
			$(twelthTdElement).find('input').attr('name', 'items['+index+'].id');
			$(twelthTdElement).find('input').attr('id', 'salesItemId'+index);
		});
	}
	
	//ajax call to get all the design item list
	function getDesignList(salesItemId,salesItemIdTd){
		$.ajax({
			method :'GET',
			url:api.DESIGN_LIST + "?salesItemId=" + salesItemId,
			success:function(response){
				console.log(response);
				if(response.length>0){
					$.error("design is mapped");
					return false;
				}else{
					bootbox.confirm({
						message: "Do you want to delete?",
						buttons: {
							cancel: {
								label: 'Cancel'
							},
							confirm: {
								label: 'Confirm'
							}
						},
						callback: function (result) {
							if(result){
								deleteSalesItemById(salesItemId);
								$(salesItemIdTd).closest("tr").remove();
								adjustIndex();
								// Check if expand all header should be shown/hidden after row deletion
								toggleExpandAllHeader();
							}
						}
					});		
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
	
	
	function deleteSalesItemById(salesItemId){
		$.ajax({
			type : "POST",  
			url : api.DELETE_SALESITEM +"?salesItemId="+salesItemId,
			success : function(response) {
				$.success("SalesItem Deleted");
				$("#salesTable").trigger("click");
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
	
	
	function getAmount(){
        $(document).on('change blur','.qty, .unitPrice, .servicePrice, #partyDropDown, .unit',function () {
            /*$(document).on('change','.servicePrice,.deleteButton,#salesTable,#partyDropDown',function () { */
            var index=$(this).closest("tr").index();
            var row=$('#salesTable > tbody  > tr').length;
            if(index == -1){
                index = row-1;
            }
            /*var index ;
             if(rowNum==0){
                 index=0;
             }else{
                 index= rowNum-1;
             }*/
            var parent = $(this).closest('tr');
            
            var up1 = $("#unitPrice"+index).val();
            up1 = up1.replace(/,/g,"");
            
            var sp1 = $("#servicePrice"+index).val();
            sp1 = sp1.replace(/,/g,"");
            
            //calculating total amount((Uprice+Sprice)*Quantity))..
           parent.find('.amount').val((parseFloat(parent.find('.qty').val()) * parseFloat(up1))+(parseFloat(sp1)*parseFloat(parent.find('.qty').val()) ))
         //  parent.find('.amount').val(parseFloat(parent.find('.qtyInput').val()) * parseFloat(up1))
            $("#unitPrice"+index).val(commaSeparateNumber(up1));
           $("#servicePrice"+index).val(commaSeparateNumber(sp1));
           var apWithCommas = $("#amount"+index).val();
           if(apWithCommas.includes(",")){
               apWithCommas = apWithCommas.replace(/,/g,"");}
           var amountPrice = parseFloat(apWithCommas);
          //$("#amount"+index).val(amountPrice);
            $("#amount"+index).val(commaSeparateNumber(amountPrice));
           
            /*var ap = $("#amount"+index).val();
            ap = ap.replace(/,/g,"");
            up1 = up1.replace(/,/g,"");*/
            if(parent.find('.amount').val()=="NaN"){
                parent.find('.amount').val("");
            }
           
           // $("#amount"+index).val(commaSeparateNumber(amountPrice));
          });
        
    }

	
	
	//For displayin first row of on selecting Model Number
	
	$(document).ready( function () {
		
		$.each(itemList,function(index,value){
			$("#itemDropDown0").append('<option value='+value.id+'>'+value.model+'</option>');
		});
		getFirstRowsalesOrder();

	});
	
	function getFirstRowsalesOrder(){
	$(document).on("click",'#salesTable>tbody>tr>td',function(){
		let index=$(this).closest('tr').index();
    		$(document).on("change", "#itemDropDown"+index , function() {
    			
    			var itemId = $(this).val();
    			var item =null;

    			$.ajax({
    				type : "GET",  
    				url : api.ITEM_LIST_BYID +"?id="+itemId,
    				success : function(response) {
    					$("input[name='items[" +index+ "].hsnCode']").val(response.hsnCode);
    					$("input[name='items[" +index+ "].modelNo']").val(response.model);
    					$("input[name='items[" +index+ "].description']").val(response.itemName);
    					$("input[name='items[" +index+ "].unitPrice']").val(response.sellPrice);
    					
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
	
	var checkDuplicate=false;
	//on click of save button in sales order open address selection pop-up
	$(document).on("click","#saveSalesOrder",function(e){
		//if(salesOrderObj==""){
		submitFormValidation(e);
		//}
		$("#billingAddressContent").empty();
		$("#clientsDropdown").empty()
		var partyId=$("#partyDropDown").val();
		var clientPoDate=$("#clientPoDate").val();
		var clientPoNumber=$("#clientPoNumber").val();
		var inputs=$("#salesTable >tbody>tr>td").find("input").hasClass("border-color");
		var selects=$("#salesTable >tbody>tr>td").find("select").hasClass("border-color");
		if(inputs== false && selects==false ){
			if(partyId!=""&& checkDuplicate==false){
				if(clientPoDate!="" && checkDuplicate==false){
					if(clientPoNumber!="" && checkDuplicate==false){
							$("#soAddressSlectionPopup").modal("show");
					
						 getBillingAddressTermsAndCondition(partyId);
						 getAllPartyList();
						 getUserList();
					}
				}
			}
		}
		
		
	});
	
	//on change of client dropdown in popup populate shipping address dropdown
	$(document).on("change","#clientsDropdown",function(){
		var clientId=$(this).val();
		/***To reset the shipping address on change of client value as null**/
		if (clientId == ""|| clientId==null ||clientId==undefined) {
			$("#shippingAddressDropdown").val('').change();
			$("#shippingAddressDropdown").empty();
			$("#shippingAddressDropdown").append('<option value="" selected>Select Shipping Address</option>')
		}
		else {
			getShippingAddressListByClientId(clientId);
		}
	})
	
	//ajax call to get all the party list and populate clients dropdown
	function getAllPartyList(){
	$.ajax({
	    Type:'GET',
	    url : api.PARTY_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$.each(response, function( key, value ) {
	    		  $('#clientsDropdown').append('<option value='+value.id+'>'+value.partyName+'</option>'); 
	    		});
	    	if(salesOrderObj!=""){
				
				$("#clientsDropdown").val(salesOrderObj.shippingAddress);
				$('#clientsDropdown').select2(salesOrderObj, {id: salesOrderObj.shippingAddress, a_key:salesOrderObj.shippingAddress});
				getShippingAddressListByClientId(salesOrderObj.shippingAddress);
				var projectClosureDate=salesOrderObj.projectClosureDate;
				if(projectClosureDate==null){
					$("#projectClosureDate").val("");
				}else{
					projectClosureDate=new Date(projectClosureDate);
					projectClosureDate=projectClosureDate.toLocaleDateString();
					var projectClosureDateFormat = projectClosureDate.split("/"); //split date by "/"
					projectClosureDate=projectClosureDateFormat[1]+"-"+projectClosureDateFormat[0]+"-"+projectClosureDateFormat[2]; //change the format to dd/mm/yyyy to display in view page
					$("#projectClosureDate").val(projectClosureDate);
				}
				$("#regionDropdown").val(salesOrderObj.region);
				$('#regionDropdown').select2(salesOrderObj, {id: salesOrderObj.region, a_key:salesOrderObj.region});
				$("#respPersonDropdown").val(salesOrderObj.responsiblePerson);
				$('#respPersonDropdown').select2(salesOrderObj, {id: salesOrderObj.responsiblePerson, a_key:salesOrderObj.responsiblePerson});
				$("#modeOfPaymentDropdown").val(salesOrderObj.modeOfPayment);
				$('#modeOfPaymentDropdown').select2(salesOrderObj, {id: salesOrderObj.modeOfPayment, a_key:salesOrderObj.modeOfPayment});
				$("#jurisdictionDropdown").val(salesOrderObj.jurisdiction);
				$('#jurisdictionDropdown').select2(salesOrderObj, {id: salesOrderObj.jurisdiction, a_key:salesOrderObj.jurisdiction});
				$("#freightDropdown").val(salesOrderObj.freight);
				$('#freightDropdown').select2(salesOrderObj, {id: salesOrderObj.freight, a_key:salesOrderObj.freight});
				$("#deliveryDropdown").val(salesOrderObj.delivery);
				$('#deliveryDropdown').select2(salesOrderObj, {id: salesOrderObj.delivery, a_key:salesOrderObj.delivery});
				$("#warrantyDropdown").val(salesOrderObj.warranty);
				$('#warrantyDropdown').select2(salesOrderObj, {id: salesOrderObj.warranty, a_key:salesOrderObj.warranty});
				$("#otherTermsAndCondition").val(salesOrderObj.otherTermsAndConditions);
				
				
			}else{ 
				$("#clientsDropdown").val('').trigger('change');
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
	
	var shippingAddressObj;
	//ajax call to get all the party Address list by partyId and populate shipping Address Dropdown
	function getShippingAddressListByClientId(clientId){
		$.ajax({
			method :'GET',
			url:api.GET_SO_ADDRESS+"?partyId="+clientId,
			success:function(response){
				console.log(response);
				shippingAddressObj=response;
		    	   //populate main party address for billing address dropdown.
				   $("#shippingAddressDropdown").empty();
				   var shiipingMainPartyAddress = response.shippingAddr.addr1;
					shiipingMainPartyAddress = shiipingMainPartyAddress.split(' ');
					$("#clientsDropdown").val(response.mainParty.id);
					$('#clientsDropdown').select2(response.mainParty, {id: response.mainParty.id, a_key:response.mainParty.id});
					if (shiipingMainPartyAddress.length > 100) {
						shiipingMainPartyAddress.splice(100);
					}
					shiipingMainPartyAddress = shiipingMainPartyAddress.join(' ');
					var length = $.trim(shiipingMainPartyAddress).length;
					if (length > 50) {
						shiipingMainPartyAddress = $.trim(shiipingMainPartyAddress).substring(0, 50) + "....";
					}
			       $("#shippingAddressDropdown").append('<option value=' + response.shippingAddr.id + '>' + shiipingMainPartyAddress + '</option>');
			       $("#shippingAddressPartyId").val(response.shippingAddr.id)
			       
			       if(response.shippingAddr.id!=response.mainParty.id){
			    	   $("#shippingAddressDropdown").append(new Option(response.mainParty.addr1));
			       }
		    	   //populate other alternative addresses for billing address dropdown.
		    	   var alternateAddressArr = response.addresses;
		           for(var i=0; i< alternateAddressArr.length; i++){
		         	  $("#shippingAddressDropdown").append(new Option(alternateAddressArr[i].addr1, alternateAddressArr[i].id));
		           }
		           
		           //Display auto selected address
		           $("#shippingAddressDropdown").change();
	   		
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
	
	$(document).on("change","#shippingAddressDropdown",function(){
		   var addressId = $("#shippingAddressPartyId").val();
		   if (addressId == "") {

		  }
		else {
		   $("#shippingAddressContent").empty();
		   
		   if(addressId == shippingAddressObj.mainParty.id) {
		   
			var addr2=shippingAddressObj.mainParty.addr2;
			if(addr2==null || addr2 == undefined) {
				addr2=' ';
				}
			
			var pincode = shippingAddressObj.mainParty.pin;
			if(pincode==null || pincode == undefined){
				pincode = ' ';
			}
			$("#shippingAddressContent").append( 
			          			"<span class=''>"+shippingAddressObj.mainParty.partyName+" ,</span>" +
			          			"<span class=''>"+shippingAddressObj.mainParty.addr1+" ,</span>" +
			          			"<span class=''>"+addr2+"</span>" +
			          			"<span class=''>"+shippingAddressObj.mainParty.party_city.name+" ,</span>"+
			          			"<span class=''>"+shippingAddressObj.mainParty.party_city.state.name+" ,</span>"+
			          			"<span class=''>"+shippingAddressObj.mainParty.party_city.state.country.name+" -</span>"+
			          			"<span class=''>"+pincode+" </span>");
		   }else {
			   var alternateAddressArr = shippingAddressObj.addresses;
		          for(var i=0; i< alternateAddressArr.length; i++){
		        	  if(addressId == alternateAddressArr[i].id) {
		        		  var addr2=alternateAddressArr[i].addr2;
		        			if(addr2==null || addr2 == undefined) {
		        				addr2=' ';
		        				}
		        			

		        			var altpincode = alternateAddressArr[i].pin;
		        			if(altpincode==null || altpincode == undefined){
		        				altpincode = '';
		        			}
		        	  $("#shippingAddressContent").append( 
			          			"<span class=''>"+shippingAddressObj.mainParty.partyName+" ,</span>" +
			          			"<span class=''>"+alternateAddressArr[i].addr1+" ,</span>" +
			          			"<span class=''>"+alternateAddressArr[i].addr2+" ,</span>" +
			          			"<span class=''>"+alternateAddressArr[i].partyaddr_city.name+" ,</span>"+
			          			"<span class=''>"+alternateAddressArr[i].partyaddr_city.state.name+" ,</span>"+
			          			"<span class=''>"+alternateAddressArr[i].partyaddr_city.state.country.name+" -</span>"+
			          			"<span class=''>"+altpincode+" </span>");
		        	  }
		          }
		   }
		}
	});
	
	//on change of biling address dropdown display full address below dropdown.
	$(document).on("change","#billingAddressDropdown",function(){
		   var addressId = $(this).val();
		   $("#billingAddressContent").empty();
		   
		   if(addressId == addressObj.mainParty.id) {
		   
			var addr2=addressObj.mainParty.addr2;
			if(addr2==null || addr2 == undefined) {
				addr2=' ';
				}
			
			var pincode = addressObj.mainParty.pin;
			if(pincode==null || pincode == undefined){
				pincode = '';
			}
			
			$("#billingAddressContent").append( 
			          			"<span class=''>"+addressObj.mainParty.partyName+" ,</span>" +
			          			"<span class=''>"+addressObj.mainParty.addr1+" ,</span>" +
			          			"<span class=''>"+addr2+"</span>" +
			          			"<span class=''>"+addressObj.mainParty.party_city.name+" ,</span>"+
			          			"<span class=''>"+addressObj.mainParty.party_city.state.name+" ,</span>"+
			          			"<span class=''>"+addressObj.mainParty.party_city.state.country.name+" -</span>"+
			          			"<span class=''>"+pincode+" </span>");
		   }else {
			   var alternateAddressArr = addressObj.addresses;
		          for(var i=0; i< alternateAddressArr.length; i++){
		        	  if(addressId == alternateAddressArr[i].id) {
		        		  var addr2=alternateAddressArr[i].addr2;
		        			if(addr2==null || addr2 == undefined) {
		        				addr2=' ';
		        				}
		        			
		        			var altpincode = alternateAddressArr[i].pin;
		        			if(altpincode==null || altpincode == undefined){
		        				altpincode = '';
		        			}
		        	  $("#billingAddressContent").append( 
			          			"<span class=''>"+addressObj.mainParty.partyName+" ,</span>" +
			          			"<span class=''>"+alternateAddressArr[i].addr1+" ,</span>" +
			          			"<span class=''>"+alternateAddressArr[i].addr2+" ,</span>" +
			          			"<span class=''>"+alternateAddressArr[i].partyaddr_city.name+" ,</span>"+
			          			"<span class=''>"+alternateAddressArr[i].partyaddr_city.state.name+" ,</span>"+
			          			"<span class=''>"+alternateAddressArr[i].partyaddr_city.state.country.name+" -</span>"+
			          			"<span class=''>"+altpincode+" </span>");
		        	  }
		          }
		   }
		});
	function getUserList(){
		$.ajax({
		    Type:'GET',
		    url : api.USER_LIST,
		    dataType:'json',
		    async: 'false',
		    success  : function(response){
				
				$("#respPersonDropdown option:not(:first)").remove();
		    	$.each(response, function( key, value ) {
					
					$('#respPersonDropdown').append('<option value=' + value.id + '>' + value.username + '</option>'); 
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


	var addressObj;
	//ajax call to get terms and condition
	function getBillingAddressTermsAndCondition(partyId){
	    $.ajax({
	       url:api.GET_SO_ADDRESS+"?partyId="+partyId,
	       type:'GET',
	       success:function(response) {
	    	   
	    	   addressObj = response;
	    	   
	    	   //populate main party address for billing address dropdown.
			   $("#billingAddressDropdown").empty();
			   var billingMainAddress = response.mainParty.addr1;

			   billingMainAddress = billingMainAddress.split(' ');
			   if (billingMainAddress.length > 100) {
				   billingMainAddress.splice(100);
			   }
			   billingMainAddress = billingMainAddress.join(' ');
			   var length = $.trim(billingMainAddress).length;
			   if (length > 50) {
				   billingMainAddress = $.trim(billingMainAddress).substring(0, 50) + "....";
			   }
   
			   $("#billingAddressDropdown").append('<option value=' + response.mainParty.id + '>' + billingMainAddress + '</option>');
	    	   
	    	  
	    	   //populate other alternative addresses for billing address dropdown.
	    	   var alternateAddressArr = response.addresses;
	           for(var i=0; i< alternateAddressArr.length; i++){
	         	  $("#billingAddressDropdown").append(new Option(alternateAddressArr[i].addr1, alternateAddressArr[i].id));
	           }
	           
	           //Display auto selected address
	           $("#billingAddressDropdown").change();
	           
	          //Populate mode of payment drop downs
	          var modeOfPaymentArr = response.modeOfPayment.split('$$');
	          var modeOfPaymentArr1 = response.modeOfPayment.split('&&');
	          $("#modeOfPaymentDropdown").empty();
	          var str =  modeOfPaymentArr[modeOfPaymentArr.length -1 ].split('&&')[0];
	          var str1 =  modeOfPaymentArr1[modeOfPaymentArr1.length -1 ];//This contains "days PDC from the date of delivery"
	          for(var i=0; i< modeOfPaymentArr.length-1; i++){
	        	  if(i==0) {
	        		  $("#modeOfPaymentDropdown").append(new Option(modeOfPaymentArr[i], modeOfPaymentArr[i]));
	        	  }else{
	        		  $("#modeOfPaymentDropdown").append(new Option(modeOfPaymentArr[i]+' '+str, modeOfPaymentArr[i]+' '+str));
	        	  }
	          }
	          $("#modeOfPaymentDropdown").append(new Option(str1, str1));
	        
	          //Populate jurisdictionDropdown drop downs
	          var jursidictionArr = response.jurisdiction.split('$$');
	          $("#jurisdictionDropdown").empty();
	          $("#regionDropdown").empty();
	          for(var i=0; i< jursidictionArr.length; i++){
	        	  $("#jurisdictionDropdown").append(new Option(jursidictionArr[i], jursidictionArr[i]));
	        	  $("#regionDropdown").append(new Option(jursidictionArr[i], jursidictionArr[i]));
	          }
	          
	          //Populate frieght drop downs
	          var frieghtArr = response.frieght.split('$$');
	          $("#freightDropdown").empty();
	          for(var i=0; i< frieghtArr.length; i++){
	        	  $("#freightDropdown").append(new Option(frieghtArr[i], frieghtArr[i]));
	          }
	          
	        //Populate delivery drop downs
	          var deliveryArr = response.delivery.split('$$');
	          $("#deliveryDropdown").empty();
	          for(var i=0; i< deliveryArr.length -1 ; i++){
	        	  var val;
	        	  if(i  == 0 ){
	        		  val = deliveryArr[i];
	        	    
	        	  }else {
	        		  val = deliveryArr[i]+' '+deliveryArr[deliveryArr.length -1];
	        	  }
	        	  $("#deliveryDropdown").append(new Option(val, val));
	          }
	          
	        //Populate warranty drop downs
	          var warrantyArr = response.warranty.split('$$');
	          var strMonths =  warrantyArr[warrantyArr.length -1 ]; //This contains "months"
	          $("#warrantyDropdown").empty();
	          for(var i=0; i< warrantyArr.length -1; i++){
	        	  $("#warrantyDropdown").append(new Option(warrantyArr[i]+' '+strMonths, warrantyArr[i]+' '+strMonths));
	          }
	          isDirty = false;
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
	
	//on click of save button in popup set shipping address billing address and other T&D values to hidden field and submit form with those values
	$(document).on("click","#saveSoBtn",function(e){
		
		$('#salesTable > tbody  > tr > td > select.unit').each(function (index, input) {
			$("#salesTable >tbody").find("tr:eq("+index+")").find("input,select,button").attr("disabled",false);
		});
		
		var shippingAddress = $("#shippingAddressDropdown").val();
		var billingAddress = $("#billingAddressDropdown").val();
		var otherTAndC=$.trim($("#otherTermsAndCondition").val());
		var modeOfPayment = $("#modeOfPaymentDropdown").val();
		var jurisdiction = $("#jurisdictionDropdown").val();
		var freight = $("#freightDropdown").val();
		var delivery = $("#deliveryDropdown").val();
		var warranty = $("#warrantyDropdown").val();
		var clientPo=$("#clientPoNumber").val();
		var region = $("#regionDropdown").val();
		var respPerson = $("#respPersonDropdown").val();
		var clientPoDate=$("#clientPoDate,#poDateVal").val(); //display value in dd/mm/yyyy format
		var clientPoFormat=clientPoDate.split("-"); //split date by "/"
		clientPoDate= clientPoFormat[1]+"/"+clientPoFormat[0]+"/"+clientPoFormat[2]; //change the format to mm/dd/yyyy to work in next step
		clientPoDate=new Date(clientPoDate);
		clientPoDate=clientPoDate.toDateString();
	
		
		var projectClosureDate=$("#projectClosureDate").val(); //display value in dd/mm/yyyy format
		var projectClosureDateFormat=projectClosureDate.split("-"); //split date by "/"
		projectClosureDate= projectClosureDateFormat[1]+"/"+projectClosureDateFormat[0]+"/"+projectClosureDateFormat[2]; //change the format to mm/dd/yyyy to work in next step
		projectClosureDate=new Date(projectClosureDate);
		projectClosureDate=projectClosureDate.toLocaleDateString();
		$("#closureDate").val(projectClosureDate);
		$("#region").val(region);
		$("#responsiblePerson").val(respPerson);
		$("#clientPo").val(clientPo);
		$("#poDate").val(clientPoDate);
		$("#shippingAddress").val(shippingAddress);
		$("#billingAddress").val(billingAddress);
		$("#modeOfPayment").val(modeOfPayment);
		$("#jurisdiction").val(jurisdiction);
		$("#freight").val(freight);
		$("#delivery").val(delivery);
		$("#warranty").val(warranty);
		$("#otherTermsAndConditions").val(otherTAndC);
		if(shippingAddress=="" || shippingAddress==undefined){
			$.error("Please select shipping address");
			e.preventDefault();
		}
		
		isDirty = false;
		$('#salesOrderForm1').submit();
		$(this).attr('disabled', 'disabled');
		$('input:not(:button,:submit),textarea,select').change(function () {
			$("#saveSoBtn").attr('disabled', false);
		});
	})
	//mandatory  validation for qty,description, unitprice and amount on submit of form
	$(document).on('submit', '#salesOrderForm1', function(e) {
		$("#taxDropDown").attr("disabled",false);
		submitFormValidation(e);
		isDirty = false;
		/* $("#saveSoBtn", this)
	     .attr('disabled', 'disabled');
		 return true;
		*/
	
	});
	function submitFormValidation(e){
		let partyValue=$('#partyDropDown option:selected').val();
		if( partyValue== ""){
			e.preventDefault(e);	
			$.error("Plaese select the party before submitting ");
			$("#partyDropDown").addClass('border-color');
		}
		var clientPoNumber=$("#clientPo").val();
		if(salesOrderObj==""){
		
		if(clientPoNumber == "" || clientPoNumber == null || clientPoNumber == undefined){
            e.preventDefault(e);	
			$.error("Plaese enter client PO Number ");
			$("#clientPoNumber").addClass('border-color');
		}
		$('input').change(function(){
				$("#clientPoNumber").removeClass('border-color');
		})   
		
		var clientPoDate=$("#poDate").val();
		if(clientPoDate == "" || clientPoDate == null || clientPoDate == undefined){
            e.preventDefault(e);	
			$.error("Plaese enter client PO Date ");
			$("#clientPoDate").addClass('border-color');
			checkDuplicate=true;
		}
		$('input').change(function(){
				$("#clientPoDate").removeClass('border-color');
				checkDuplicate=false;
		})   
		}
		var shippingAddress=$("#shippingAddress").val();
		if(shippingAddress=="" || shippingAddress==undefined){
			e.preventDefault();
		}
		
		$('#salesTable > tbody  > tr > td > input.qty').each(function(index, input) { 
			var qty = $("#qty"+index).val();
			let row=index+1
			if(qty==undefined || qty==""){
				$.error("Please enter the quantity at row " +row);
				 $("#qty"+index).addClass('border-color');
		        e.preventDefault(e);
			}
			$('input').change(function(){
				$("#qty"+index).removeClass('border-color');
			})
		});
		$('#salesTable > tbody  > tr > td > input.description').each(function(index, input) { 
            let row=index+1
			var description = $("#description"+index).val();
			if(description == "" || description==undefined){
				$.error("Please enter the description at row " +row);
				 $("#description"+index).addClass('border-color');
		        e.preventDefault(e); 
			}
			$('input').change(function(){
				$("#description"+index).removeClass('border-color');
			})
		});
		$('#salesTable > tbody  > tr > td > input.unitPrice').each(function(index, input) { 
			let row=index+1
			var unitPrice = $("#unitPrice"+index).val();
			unitPrice = unitPrice.replace(/,/g,"");
			$("#unitPrice"+index).val(unitPrice);
			var amount = $("#amount"+index).val();
			amount = amount.replace(/,/g,"");
			 $("#amount"+index).val(amount);
			 
			 var total = $("#total").val();
			 total = total.replace(/,/g,"");
			 $("#total").val(total);
			 
			 var gst = $("#gst").val();
			 gst = gst.replace(/,/g,"");
			 $("#gst").val(gst);
			 
			 var grandTotal = $("#grandTotal").val();
			 grandTotal = grandTotal.replace(/,/g,"");
			 $("#grandTotal").val(grandTotal);
			 
			
		});
		
		$('#salesTable > tbody  > tr > td > input.servicePrice').each(function(index, input) { 
			let row=index+1
			var servicePrice = $("#servicePrice"+index).val();
			servicePrice = servicePrice.replace(/,/g,"");
			$("#servicePrice"+index).val(servicePrice);
			var unitPrice = $("#unitPrice"+index).val();
			var unitName=$("#unit"+index).find("option:selected").text()
			if(unitName!="Heading"){
				if(servicePrice==0 && unitPrice==0){
					 $("#unitPrice"+index).addClass('border-color');
					 $("#servicePrice"+index).addClass('border-color');
						e.preventDefault(e);
		        	$.error("Please enter price more than 0 at row "+ row);
				}
			}
			$('input').change(function(){
				$("#unitPrice"+index).removeClass('border-color');
				$("#servicePrice"+index).removeClass('border-color');
			})	
			
		});
	/*	$('#salesTable > tbody  > tr > td > input.qty').each(function(index, input) { 
			let row=index+1
	        var qtyValue = this.value;
			var digits= new RegExp(/^[0-9]+$/);
			var qtyValueInt=parseInt(qtyValue);
			var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
	        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
			if (qtyValue.match(digits)||qtyValue=="") {
				if(qtyValueInt<=2147483647){
					
				}else{
					 $("#qty"+index).addClass('border-color');
						e.preventDefault(e);
		        	$.error("Qty limit exceeded at row "+ row);
				}

			}else{
				 $("#qty"+index).addClass('border-color');
					e.preventDefault(e);
	        	$.error("only digits are allowed for qty at row "+ row);
			}
			$('input').change(function(){
				$("#qty"+index).removeClass('border-color');
			})	
	    });*/
		
		$('#salesTable > tbody  > tr > td > input.qty').each(function(index, input) { 
			let row=index+1
	        var qtyValue = this.value;
			var qtyValueInt=parseInt(qtyValue);
			var floatNum= new RegExp(/^[+-]?\d+(\.\d+)?$/);
	        //if (qtyValue.match(digits)||qtyValue.match(floatNum)||qtyValue=="") {
				if(qtyValueInt<=2147483647){
					
				}else{
					 $("#qty"+index).addClass('border-color');
						e.preventDefault(e);
		        	$.error("Qty limit exceeded at row "+ row);
				}

			
			$('input').change(function(){
				$("#qty"+index).removeClass('border-color');
			})	
	    });

		
		$('#salesTable > tbody  > tr > td > input.amount').each(function(index, input) { 
			let row=index+1
			var amount = $("#amount"+index).val();
			if(amount==undefined || amount== ""||amount=="NaN"){
				 $("#amount"+index).addClass('border-color');
				 $.error("Please enter amount at row "+ row);
				e.preventDefault(e);
			}	
			$('input').change(function(){
				$("#amount"+index).removeClass('border-color');
			})	
		});
		$('#salesTable > tbody  > tr > td > select.unit').each(function(index, input) { 
			let row=index+1
			var unit = $("#unit"+index).val();
			if(unit == "" || unit==undefined){
				$.error("Please enter the unit at row " +row);
				$("#unit"+index).addClass('border-color');
		        e.preventDefault(e);
			}
			$('select').change(function(){
					$("#unit"+index).removeClass('border-color');
			})    
			
		});
		//if(salesOrderObj==""){
		//checck for duplicate validation of client po number
		if(clientPoNumber!=""){
		$.each(salesOrderList,function(index,value){
			if(clientPoNumber.trim() == value.clientPoNumber.trim() && salesOrderObj.id!=value.id){
				$.error("Client PO Number already exist");
				$("#clientPoNumber,#clientPo").addClass('border-color');
				checkDuplicate=true;
			e.preventDefault(e);
			return false;
			
		}else{
			checkDuplicate=false;
			$("#clientPoNumber,#clientPo").removeClass('border-color');
		}
		});
		}else{
			//$("#clientPoNumber,#clientPo").removeClass('border-color');
		}
	
//	}
	}
	

	 //Reset form
	 $(document).on("click", "#resetSalesOrder" , function() {
		 $("#salesOrderForm1")[0].reset();
		 $(".unit ").val("").trigger('change');
   });
	 
	 
	 function commaSeparateNumber(val){
		 var x=val;
		 //x = x.replace(",","");
		 x=x.toString();
		 x = x.replace(/,/g,"");
		 var afterPoint = '';
		 if(x.indexOf('.') > 0)
		    afterPoint = x.substring(x.indexOf('.'),x.length);
		 x = Math.floor(x);
		 x=x.toString();
		 var lastThree = x.substring(x.length-3);
		 var otherNumbers = x.substring(0,x.length-3);
		 if(otherNumbers != '')
		     lastThree = ',' + lastThree;
		 var res = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + lastThree + afterPoint;
		 return res;

		 }
	 
	 $(document).on('keyup mouseup click input change','.qty,.unitPrice,.servicePrice,.deleteButton,#salesTable,#partyDropDown',function () {
			 
			var sum=0;
	    var amountArr=[];
	    $('.amount').each(function(index,value) {
	    	/*amountArr.push(amountPrice);
	    	var amount=amountPrice;*/
	    	var c = $("#amount"+index).val();
	    	c = c.replace(/,/g,"");
	    	//amountArr.push(parseFloat($(this).val()));
	    	
	    	var amount=parseFloat(c);
		    sum+=amount;
		    var sumWithComma = commaSeparateNumber(sum);
		    $(".total").attr("value", sumWithComma);
		    $("#total").val(sumWithComma);
		    var gst=0;
		    var partyId=$("#partyDropDown option:selected").val();
		    var gstValue = $("#taxDropDown").val();
		    var gstRateValue = gstValue/100;
		    if(partyId!="C1143"){
		      gst=gstRateValue*sum;
		    gst = Math.round(gst * 100) / 100
		    var gstWithComma = commaSeparateNumber(gst);
		    $(".gst").attr("value", gstWithComma);
		    $("#gst").val(gstWithComma);
		    }else{
		    	gst=0;
		    }
		    var grandTotal=sum+gst;
		    grandTotal = Math.round(grandTotal * 100) / 100
		    grandTotal=commaSeparateNumber(grandTotal);
		    $(".grandTotal").attr("value", grandTotal);
		    $("#grandTotal").val(grandTotal);
		    
		    });
	    // Check if expand all header should be shown/hidden based on data
	    toggleExpandAllHeader();
	  });
	 
	 $(document).on('change', '#taxDropDown',function () {
		 var gstValue = $("#taxDropDown").val();
		 $("#gstrate").html("GST @ "+gstValue+"%");
		// $(".unitPrice").val("").trigger('change');
		 var sum=0;
		  $('.amount').each(function(index,value) {
		    	/*amountArr.push(amountPrice);
		    	var amount=amountPrice;*/
		    	var c = $("#amount"+index).val();
		    	c = c.replace(/,/g,"");
		    	//amountArr.push(parseFloat($(this).val()));
		    	
		    	var amount=parseFloat(c);
			    sum+=amount;
			    var sumWithComma = commaSeparateNumber(sum);
			    $(".total").attr("value", sumWithComma);
			    $("#total").val(sumWithComma);
			    var gst=0;
			    var partyId=$("#partyDropDown option:selected").val();
			    var gstValue = $("#taxDropDown").val();
			    var gstRateValue = gstValue/100;
			    if(partyId!="C1143"){
			      gst=gstRateValue*sum;
			    gst = Math.round(gst * 100) / 100
			    var gstWithComma = commaSeparateNumber(gst);
			    $(".gst").attr("value", gstWithComma);
			    $("#gst").val(gstWithComma);
			    }else{
			    	gst=0;
			    }
			    var grandTotal=sum+gst;
			    grandTotal = Math.round(grandTotal * 100) / 100
			    grandTotal=commaSeparateNumber(grandTotal);
			    $(".grandTotal").attr("value", grandTotal);
			    $("#grandTotal").val(grandTotal);
			    
			    });
		 });
		 // ==================== MASTER CHECKBOX FUNCTIONALITY ====================
		 // Master checkbox to toggle all description expansions
		 $(document).on('change', '#masterToggleBrief', function() {
		     var isChecked = $(this).prop('checked');
		     
		     // Check/uncheck all row checkboxes
		     $('.toggleBrief').each(function() {
		         $(this).prop('checked', isChecked);
		         
		         // Get index
		         var index = $(this).closest("tr").index();
		         let inputText = $("input[name='items[\" +index+ \"].description']").val();
		         
		         if(isChecked) {
		             $("#descriptionDiv"+index).addClass('CellComment');
		             $("#descriptionDiv"+index).html(inputText).show();
		             $('#descriptionDiv'+index).css('display','block');
		         } else {
		             $("#descriptionDiv"+index).addClass('CellComment');
		             $("#descriptionDiv"+index).html(inputText).hide();
		         }
		     });
		 });

		 // Update master checkbox when individual checkboxes change
		 $(document).on('change', '.toggleBrief', function() {
		     var totalCheckboxes = $('.toggleBrief').length;
		     var checkedCheckboxes = $('.toggleBrief:checked').length;
		     
		     // If all are checked, check master
		     if (totalCheckboxes > 0 && checkedCheckboxes === totalCheckboxes) {
		         $('#masterToggleBrief').prop('checked', true);
		     } else {
		         $('#masterToggleBrief').prop('checked', false);
		     }
		 });

// ==================== EXPAND ALL HEADER VISIBILITY FUNCTIONS ====================
// Function to check if table has meaningful data and show/hide expand all header
function toggleExpandAllHeader() {
	var hasData = false;
	
	// Check if there are any rows with data (excluding the first empty row)
	$('#salesTable tbody tr').each(function(index) {
		var description = $(this).find('.description').val();
		var qty = $(this).find('.qty').val();
		var unitPrice = $(this).find('.unitPrice').val();
		var servicePrice = $(this).find('.servicePrice').val();
		
		// Consider row has data if any of these fields have meaningful values
		if ((description && description.trim() !== '') || 
			(qty && qty.trim() !== '' && qty !== '0') ||
			(unitPrice && unitPrice.trim() !== '' && unitPrice !== '0.0') ||
			(servicePrice && servicePrice.trim() !== '' && servicePrice !== '0.0')) {
			hasData = true;
			return false; // Break out of each loop
		}
	});
	
	// Show or hide the expand all header based on data presence
	if (hasData) {
		$('#expandAllHeader').show();
	} else {
		$('#expandAllHeader').hide();
	}
}

// Function to show expand all header (called when data is loaded)
function showExpandAllHeader() {
	$('#expandAllHeader').show();
}

// Function to hide expand all header (called when table is empty)
function hideExpandAllHeader() {
	$('#expandAllHeader').hide();
}
