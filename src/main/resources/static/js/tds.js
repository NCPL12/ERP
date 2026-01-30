
$(document).ready( function () {
	if(salesOrderObj!=""){
			
		getSalesItemListBySoItem(salesOrderObj);
		$("#soNumber").val(salesOrderObj.id);
	}
	 
});

function getSalesItemListBySoItem(salesOrderObj){
	var partyId=salesOrderObj.party.id;
	$('#party').val(salesOrderObj.party.id);
	
	$("#clientPoDate").replaceWith('<input type="text" class="form-control PositionofTextbox" style="width:150px" id="poDateVal" name="clientPoDate">');
	var poDate=salesOrderObj.clientPoDate;
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
	
	$("#clientPoNumber,#clientPo").val(salesOrderObj.clientPoNumber);
	$('#poDateVal,clientPoDate').attr("readonly","readonly");
	$('#clientPoNumber').attr("readonly","readonly");
	
	$('#partyDropDown').replaceWith('<input type="text" class="form-control PositionofTextbox" style="width:250px" id="partyVal" name="partyVal">');
	$('#partyVal').val(salesOrderObj.party.partyName);
	$('#partyVal').attr("disabled",true);
	var className = "so";
	var soId = salesOrderObj.id;
	$.ajax({
	    Type:'GET',
	    url : api.SALES_LIST_BY_SOID+"?id="+soId+"&&className="+className,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	var arrayCount=0;
	    	$.each(response,function(index,value){
	    		
	    		if(value.designItems.length==0){
	    			if(value.item_units.name=="Heading"){
	    				slNo=value.slNo;
	    	    		 description=value.description;
	    	    		 salesItemId =  value.id;
	    	    		 modelNumber ="";
	    	    		 poQty="";
	    	    		 unitName="";
	    	    		 model="";
	    	    		 tds="";
	    	    		 siteQty="";
	    	    		 qty="";
	    			}else{
	    				slNo=value.slNo;
	    	    		 description=value.description;
	    	    		 salesItemId =  value.id;
	    	    		 modelNumber = value.modelNo;
	    	    		 poQty=value.quantity;
	    	    		 unitName=value.item_units.name;
	    	    		 model="";
	    	    		 tds="";
	    	    		 siteQty="";
	    	    		 qty="";
	    			}
	    			 
	    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='25%'>" + description + "</td>" +
					"<td width='15%'>" + modelNumber + "</td><td width='8%'>"+poQty +"</td><td width='5%'>" +unitName+ "</td>" +
					"<td width='9%'>" +model  + "</td><td width='6%'>" + qty + "</td>"+
					"<td width='8%'>" + tds + "</td><td width='8%'>" + siteQty+ "</td>"+
					"<td  style='display:none'><input type='checkbox' class='form-control form-control-sm PositionofTextbox tdsApproved' id='tdsApproved"+ arrayCount +"' name='items["+arrayCount+"].tdsApproved' path='items["+arrayCount+"].tdsApproved' value=0/></td>" +
					"<td style='display:none'><input type='text' class='form-control PositionofTextbox siteQuantity' id='siteQuantity"+ arrayCount +"' name='items["+arrayCount+"].siteQuantity' path='items["+arrayCount+"].siteQuantity' value=0 readonly/></td>" +
					"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='description"+ arrayCount +"' name='items["+arrayCount+"].description' path='items["+arrayCount+"].description' value='"+value.id+"'/></td>" +
					"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='modelNumber"+arrayCount +"' name='items["+arrayCount+"].modelNumber' path='items["+arrayCount+"].modelNumber' value=''/></td>" +
					"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='designQty"+ arrayCount +"' name='items["+arrayCount+"].designQty' path='items["+arrayCount+"].designQty' value=0 ></td>" +
					"</tr>";
		    		$("#salesTable tbody").append(soItems);
		    		arrayCount++;
	    		}else{
	    			$.each(value.designItems,function(i,v){
		    			
		    			if(i!=0){
		    				var	slNo="";
		    				var description="";
		    				var modelNumber="";
		    				var poQty="";
		    				var unitName="";
		    			}else{
		    				 slNo=value.slNo;
		    	    		 description=value.description;
		    	    		 salesItemId =  value.id;
		    	    		 modelNumber = value.modelNo;
		    	    		 poQty=value.quantity;
		    	    		 unitName=value.item_units.name;
		    			}
			    			var model=v.itemId;
			    			var qty=v.quantity;
			    			var itemId = v.itemMasterId;
			    		
				    		var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='25%'>" + description + "</td>" +
							"<td width='15%'>" + modelNumber + "</td><td width='8%'>"+poQty+"</td><td width='5%'>" +unitName + "</td>" +
							"<td width='9%'>" + model + "</td><td width='6%'>" + qty + "</td>"+
							"<td width='8%'><input type='checkbox' class='form-control form-control-sm PositionofTextbox tdsApproved' id='tdsApproved"+ arrayCount +"' name='items["+arrayCount+"].tdsApproved' path='items["+arrayCount+"].tdsApproved'/></td>" +
							"<td width='8%'><input type='text' class='form-control PositionofTextbox siteQuantity' id='siteQuantity"+ arrayCount +"' name='items["+arrayCount+"].siteQuantity' path='items["+arrayCount+"].siteQuantity' value='"+qty+"' readonly/></td>" +
							"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='description"+ arrayCount +"' name='items["+arrayCount+"].description' path='items["+arrayCount+"].description' value='"+value.id+"'/></td>" +
							"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='modelNumber"+arrayCount +"' name='items["+arrayCount+"].modelNumber' path='items["+arrayCount+"].modelNumber' value='"+itemId+"'/></td>" +
							"<td style='display:none'><input type='hidden' class='form-control PositionofTextbox' id='designQty"+ arrayCount +"' name='items["+arrayCount+"].designQty' path='items["+arrayCount+"].designQty' value='"+qty+"'/></td>" +
							"</tr>";
				    		$("#salesTable tbody").append(soItems);
				    		arrayCount++;
		    			
		    		})	
	    		}
	    		
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


if($("input[type='checkbox']").is(':checked')) {		
      $(".siteQuantity").attr("readonly",false);
} else{
	$(".siteQuantity").attr("readonly","readonly");
} 

$(document).on("click",".tdsApproved",function(){
	var index = $(this).closest("tr").index();
	var qty=$("#designQty"+index).val();
	if($(this).is(':checked')){
        $('#siteQuantity'+index).attr('readonly', false);
    } else {
        $('#siteQuantity'+index).attr('readonly', true);
        $('#siteQuantity'+index).val(qty)
    }
	
})

function getDesignItemList(salesItemId,value){
	$.ajax({
		method :'GET',
		url:api.GET_DESIGN_ITEMS_BY_SALESITEM_ID + "?salesItemId=" + salesItemId,
		success:function(response){
			console.log(response);
	    		
	    		
	    		$.each(response,function(i,v){
	    			var model=v.modelNo;
	    			var qty=v.quantity;
	    			var designItems = "<tr><td width='9%'>" + model + "</td><td width='6%'>" + qty + "</td></tr>"
	    			$("#designItemsTable tbody").append(designItems);
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
	})
}

$(document).on('submit', '#salesOrderTdsForm', function(e) {
	
	if($("input[type='checkbox']#tdsApproved").is(':checked')) {		
	      $("#tdsApproved").val(true);  
	} else{
		$("#tdsApproved").val(false);
	}  
})