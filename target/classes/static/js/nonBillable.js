$(document).ready(function(){
	
	$.each(nonBillableList, function (index, value) {
		
		$("#nonBillableDropdown").append('<option value=' + value.id + '>' + value.name + '</option>');
	});
})

$(document).on("change","#nonBillableDropdown",function(){
	var id=$(this).val();
	$.ajax({
	    Type:'GET',
	    url : api.NON_BILLABLE_ITEMS_BY_ID+"?id="+id,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	var arraycount=0;
	    	var rowCount=arraycount+1;
	    	//$("#modelNo" + row).children("option").filter(":not(:first)").remove();
			//$("#descriptionDropdown" + row).children("option").filter(":not(:first)").remove();
	    	$.each(response,function(index,value){
	    		
	    			 
	    			var soItems = '<tr><td width="5%" class="styleOfSlNo">' + rowCount + '</td>'+
    				'<td width="40%"> <select class="form-control select2 PositionofTextbox  descriptionDropdown dropdownMarginTop" name="items[' + arraycount + '].description" id="descriptionDropdown' + arraycount + '" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="'+value.id+'">' +value.description + '</option></select>   </td>'+
	    			'<td width="10%"><select class="form-control PositionofTextbox modelNo dropdownMarginTop" id="modelNo' + arraycount + '" name="items[' + arraycount + '].modelNo" style="padding: 0;white-space: nowrap!important;overflow: hidden!important;text-overflow: ellipsis;margin-top:4px!important;"> <option value="'+value.modelNo+'">'+value.modelNo+'</option></select>   </td>'+
	    			'<td width="10%"><input type="text" class="form-control PositionofTextbox hsnCode" id="hsnCode' + arraycount + '" name="items[' + arraycount + '].hsn" value="'+value.hsn+'" readonly/><span id="hsnCodeDiv' + arraycount + '"></span></td>'+
	    			'<td width="7%"><input type="text" class="form-control PositionofTextbox qty qtyInput" id="newQuantity' + arraycount + '" name="items[' + arraycount + '].quantity"/><span id="qtyDiv' + arraycount + '"></span></td>'+
	    			'<td class="hideTd"><input type="hidden" class="salesQtyInput" id="salesQuantity' + arraycount + '" value="'+value.qty+'"/></td>'+
	    			'<td width="10%"><input type="text" class="form-control PositionofTextbox unit" readonly="readonly" id="unit' + arraycount + '"  value="'+value.unit+'"/><span id="unitDiv' + arraycount + '"></span></td>'+
	    			'<td width="12%"><input type="text" class="form-control PositionofTextbox unitPrice upInput alignright" id="newPrice' + arraycount + '" name="items[' + arraycount + '].unitPrice" value="'+value.unitPrice+'" readonly /><span id="unitPriceDiv' + arraycount + '" ></span></td>'+
	    			'<td class="hideTd"><input type="hidden" class="salesUnitPrice" value="" id="salesUnitPrice' + arraycount + '" /></td>'+
	    			'<td width="14%"><input type="text" class="form-control PositionofTextbox amount amtInput alignright"  id="amount' + arraycount + '" name="items[' + arraycount + '].amount" readonly="readonly"/><span id="amountDiv' + arraycount + '" readonly></span></td>'+
	    			'</tr>';
		    		$('#nonBillableTable tbody').append(soItems);
		    		
		    		arraycount++;
		    		rowCount++;
	    		
	    		
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
})