var grnDataTable;
$(document).ready(function(){
	$('#grnList thead tr').clone(true).appendTo( '#grnList thead' );
	    $('#grnList thead tr:eq(1) th').each( function (i) {
	        var title = $(this).text();
	        $(this).html( '<input type="text" style="width:100%;" placeholder="Search '+title+'" />' );
	        $( 'input', this ).on( 'keyup change', function () {
	            grnDataTable
	            	.search( this.value )
	                .draw();
	        } );
	    } );
	    grnDataTable= $('#grnList').DataTable({
	    	processing: true,
	    	serverSide: true,
	    	orderCellsTop: true,
		    fixedHeader: true,
		    order:[[ 2, "desc" ]],
		    'columnDefs': [ {
	    	    'targets': [0,1,2,3,4,5,6,7,8], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	"ajax": {
	    		"url": pageContext+"/api/grn/datatable",
	    		"type": "GET",
	    		"dataSrc": "data"
	    	},
	    	"columns": [ {
				"data" : "grnId",
				"defaultContent":"",
			}, {
				"data" : "poNumber",
				"defaultContent":"",

			}, {
				"data" : "created",
				"defaultContent":"",
				"class":"hideTd"
			}, {
				"data" : "poDate",
				"defaultContent":"",
				render: function (data) {
					if (!data) {
						return "";
					}
					return moment(new Date(data)).format("DD-MM-YY HH:mm:ss");
				}
			}, {
				"data" : "created",
				"defaultContent":"",
				render: function (data) {
					if (!data) {
						return "";
					}
					return moment(new Date(data)).format("DD-MM-YY HH:mm:ss");
				}
			}, {
				"data" : "vendor",
				"defaultContent":"",
				
				"class": "text-field-large-nowrap",
	    		render: function ( data, type, row ) {
	    		    return data.length > 35 ?
	    		    		data.substr( 0, 35 ) +'...' :
	    		    			data;
	    		}

			},
			{
				"data" : "invoiceNo",
				"defaultContent":"",
				
			},
			
			{
				"data" : "total",
				"defaultContent":""
			},
			
			{
				"mData" : "grnId"	,
				render : function(datam, type, row) {
					var url = null;
					//url ="/ncpl-sales/dc/details/"+row.dcId;
					return "<button type='button' id='"+row.grnId+"' class='btn btn-default btn-flat btn-xs grnView'><i class='fa fa-eye'></i></button>";;				
				}
			},
			{
				"mData" : "pdf"	,
				render : function(datam, type, row) {
					var url = null;
					url ="/ncpl-sales/grn/download/"+row.grnId;
					return "<a class='text-info ' href='" + url + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i></button></a>";				
				}
			},
			{
				"mData" : "archive"	,
				render : function(datam, type, row) {
					var archive;
					if(role=="SUPER ADMIN"){
						if(row.archive==true){
							return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' checked='checked'/>";				
						}else{
							return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' />";				
						}
					}else{
						if(row.archive==true){
							return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' checked='checked' disabled='disabled'/>";				
						}else{
							return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' disabled='disabled'/>";				
						}
					}
				}
			}
			]
	    });
	    //on double click of list navigate to view page
	    $('#grnList tbody').on('dblclick', 'tr', function () {
	 	   var data = grnDataTable.row(this).data();
	 	   var grnId = data.grnId;
	 	   window.location = pageContext+"/api/grn/view?grnId="+grnId;
	 	});
})

 $(document).on('click',".archiveCheckbox",function(){
	var grnNum=$(this).closest("tr").find("td:eq(0)").text();
	var archiveinput= $(this).closest("tr").find("td").eq(10).find('input');
	const isChecked = $(this).is(":checked");
		if(isChecked==true) {
		bootbox.confirm({
			message: "Do you want to archive this SO?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? archiveGrn(grnNum):window.location.reload();;
			}
		});	
	}else{
		bootbox.confirm({
			message: "Do you want to remove this SO from archive?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? unArchiveGrn(grnNum):window.location.reload();;
			}
		});	
	}
});



function archiveGrn(grnNum){
	//window.location.href = pageContext+"/api/update_so_archive?soId="+soId;
	//window.location=pageContext+"/salesList";
	$.ajax({
		type : "POST",  
		url : api.UPDATE_GRN_ARCHIVE +"?grnNum="+grnNum,
		success : function(response) {
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

function unArchiveGrn(grnNum){
	$.ajax({
		type : "POST",  
		url : api.UPDATE_GRN_UNARCHIVE +"?grnNum="+grnNum,
		success : function(response) {
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
$(document).on("click",".grnView",function(){
	var grnId = this.id;
	$("#grnViewModalHeader").text("GRN Preview"+"- "+grnId)
	$.ajax({
	    Type:'GET',
	    url : api.GET_GRN_ITEMLIST_BYGRNID+"?grnId="+grnId,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	 $("#grnViewModalTable tbody").empty();
				$("#grnViewModal").modal("show");
				var arrayCount=0;
				itemsList = response;
				var slno=1;
				$.each(response,function(index,value){
					var grnDescription;
					var modelNo;
					var totalQty;
					var units=value.unitName;
					var receivedQty=value.receivedQuantity;
					
					var unitPrice=value.unitPrice;
					var amount=value.amount;
					var poNumber;
					$.each(purchaseItemList, function (k, v) {
						if (value.description == v.purchase_item_id) {
							grnDescription = v.poDescription;
							modelNo = v.modelNo;
							totalQty = v.quantity;
							poNumber=v.purchaseOrder.poNumber;
						}
						

					});
					var modelName;
					var itemId;
					$.each(itemList, function (ky, val) {
						if (modelNo == val.id) {
							modelName = val.model;
							itemId=val.id;

						}

					})
					getRemainingQtyByCreatedGrn(poNumber,value.description,arrayCount,totalQty,slno,grnDescription,modelName,units,totalQty,receivedQty,unitPrice,amount,itemId);
					
		    		arrayCount++;
		    		slno++;
				
			
	    	});
				
				$("#grnViewBtn").on("click",function(){
				 	   window.location = pageContext+"/api/grn/view?grnId="+grnId;
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
	
});
function getRemainingQtyByCreatedGrn(poNo,description,rowKey,totQty,slno,grnDescription,modelName,units,totalQty,receivedQty,unitPrice,amount,itemId){
	$.ajax({
		Type: 'GET',
		url: api.GET_GRN_ITEMLIST_BYPONO + "?poNo=" + poNo,
		dataType: 'json',
		async: 'false',
		success: function (response) {
			var totalReceivedQty =0;
			$.each(response, function (key, value) {
				if(description == value.description){
					totalReceivedQty = totalReceivedQty +value.receivedQuantity
				
				}
					
				});
			var remainQty;
			remainQty=Math.round((totQty -totalReceivedQty) * 100) / 100;
			var grnItems = "<tr><td width='5%'>"+ slno+"</td><td width='35%' style='word-break: break-word; '>" + grnDescription + "</td>" +
			"<td width='10%' id='modelNo'>" + modelName + "</td><td width='7%'>"+units +"</td><td width='7%'>" +totalQty+ "</td>" +
			"<td width='7%'>" +receivedQty  + "</td><td width='7%'>"+remainQty+"</td>" +
					"<td width='10%'>" +unitPrice  + "</td><td width='10%'>" + amount + "</td>" +
							"<td width='3%' id='companyAssetCheckBoxTd' class='companyAssetCheckBoxTd'><input class='companyAssetCheckBox'  type='checkbox' id='companyAssetCheckBox' /></td>" +
							"<td class='hideTd' id='itemIdHiddenTd'><input class='itemIdHidden'  type='text' id='itemIdHidden' />"+itemId+"</td></tr>";
			$("#grnViewModalTable tbody").append(grnItems);
				
		}
		
});
}
$(document).on("change",".companyAssetCheckBox", function(){
	var index=$(this).closest("tr").index();
	var model=$(this).closest("tr").find("#modelNo").text();
	var itemHidden=$(this).closest("tr").find("#itemIdHiddenTd").text();
	//let inputText =$("input[name='items[" +index+ "].description']").val();
	if($(this).is(':checked')) {
		$( "#returnDate" ).datepicker({ dateFormat: 'dd-mm-yy' });
		$( "#dateAndTimeStamp" ).datepicker({ dateFormat: 'dd-mm-yy' });
        $("#model").val(model);
        $("#modelHidden").val(itemHidden);
        getEmployeeList();
        $("#companyAssetModal").modal('show');
	}else{
		$("#companyAssetForm")[0].reset();
	}
   
})	

$(document).on("click","#saveCompanyAssetBtn",function(e){
	
	var returnDate=$("#returnDate").val(); //display value in dd/mm/yyyy format
	var returnDateFormat=returnDate.split("-"); //split date by "/"
	returnDate= returnDateFormat[1]+"/"+returnDateFormat[0]+"/"+returnDateFormat[2]; //change the format to mm/dd/yyyy to work in next step
	returnDate=new Date(returnDate);
	returnDate=returnDate.toLocaleDateString();
	$("#returnDateHidden").val(returnDate);
	
	var dateAndTimeStamp=$("#dateAndTimeStamp").val(); //display value in dd/mm/yyyy format
	var dateAndTimeStampFormat=dateAndTimeStamp.split("-"); //split date by "/"
	dateAndTimeStamp= dateAndTimeStampFormat[1]+"/"+dateAndTimeStampFormat[0]+"/"+dateAndTimeStampFormat[2]; //change the format to mm/dd/yyyy to work in next step
	dateAndTimeStamp=new Date(dateAndTimeStamp);
	dateAndTimeStamp=dateAndTimeStamp.toLocaleDateString();
	$("#dateHidden").val(dateAndTimeStamp);
	 let isValid = true;

     // Remove old error messages
     $('.error').remove();

     $("#companyAssetForm").find('[required]').each(function() {
         let field = $(this);
         if ($.trim(field.val()) === '') {
             field.css('border', '2px solid red');
             field.after('<span class="error" style="color:red;font-size:12px;">This field is required</span>');
             isValid = false;
         } else {
             field.css('border', '');
         }
     });

     if (!isValid) {
         e.preventDefault();
     }else{
    	 $('#companyAssetForm').submit();
     }
})

function getEmployeeList(){
	$.ajax({
	    Type:'GET',
	    url : api.EMPLOYEE_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
			
			$("#custodianDropdown option:not(:first)").remove();
	    	$.each(response, function( key, value ) {
				
				$('.custodianDropdown').append('<option value=' + value.id + '>' + value.name + '</option>'); 
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


