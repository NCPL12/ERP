var dcTable;
$(document).ready(function(){
	$('#dcList thead tr').clone(true).appendTo( '#dcList thead' );
	    $('#dcList thead tr:eq(1) th').each( function (i) {
	        var title = $(this).text();
	        $(this).html( '<input type="text" style="width:100%;" placeholder="Search '+title+'" />' );
	        $( 'input', this ).on( 'keyup change', function () {
	            if ( dcTable.column(i).search() !== this.value ) {
	            	dcTable
	                    .column(i)
	                    .search( this.value )
	                    .draw();
	            }
	        } );
	    } );
	    dcTable= $('#dcList').DataTable({
	    	orderCellsTop: true,
		    fixedHeader: true,
		    processing: true,
		    serverSide: true,
		    pageLength: 100,
		    lengthMenu: [[50, 100, 250, 500, 1000], [50, 100, 250, 500, 1000]],
	    	"order": [[ 0, "desc" ]],
	    	ajax: function (data, callback, settings) {
	    		var page = Math.floor(data.start / data.length);
	    		var size = data.length;
	    		var keyword = data.search && data.search.value ? data.search.value : "";
	    		var sortDir = (data.order && data.order.length && data.order[0].dir) ? data.order[0].dir : "desc";
	    		var sortIdx = (data.order && data.order.length) ? data.order[0].column : 0;
	    		var sortField = (data.columns && data.columns[sortIdx] && data.columns[sortIdx].data) ? data.columns[sortIdx].data : "dcId";
	    		// Allow only backend-supported fields
	    		if(["dcId","soNumber","archive"].indexOf(sortField) === -1){ sortField = "dcId"; }
	    		// Collect per-column filters from the second header row inputs
	    		var dcIdFilter = ($('#dcList thead tr:eq(1) th:eq(0) input').val() || '').trim();
	    		var soFilter = ($('#dcList thead tr:eq(1) th:eq(1) input').val() || '').trim();
	    		var clientNameFilter = ($('#dcList thead tr:eq(1) th:eq(2) input').val() || '').trim();
	    		var clientPoFilter = ($('#dcList thead tr:eq(1) th:eq(3) input').val() || '').trim();
	    		var shippingFilter = ($('#dcList thead tr:eq(1) th:eq(4) input').val() || '').trim();
	    		// If any column filter is active, suppress global keyword to leverage advanced search on backend
	    		if (dcIdFilter || soFilter || clientNameFilter || clientPoFilter || shippingFilter) {
	    			keyword = "";
	    		}
	    		$.ajax({
	    			url: pageContext + "/dcList/data",
	    			data: {
	    				page: page,
	    				size: size,
	    				keyword: keyword,
	    				sortField: sortField,
	    				sortDir: sortDir,
	    				// per-column filters expected by backend
	    				dcId: dcIdFilter,
	    				soNumber: soFilter,
	    				clientName: clientNameFilter,
	    				clientPo: clientPoFilter,
	    				shipping: shippingFilter
	    			},
	    			success: function(resp){
	    				callback({
	    					draw: data.draw,
	    					recordsTotal: resp.totalElements,
	    					recordsFiltered: resp.totalElements,
	    					data: resp.content
	    				});
	    			}
	    		});
	    	},
	    	"columns": [ {
				"data" : "dcId",
				"defaultContent":"",
			}, {
				"data" : "soNumber",
				"defaultContent":"",

			},{
				"data" : "clientName",
				"defaultContent":"",
				"class": "text-field-large-nowrap",
	    		render: function ( data, type, row ) {
	    		    return data.length > 35 ?
	    		    		data.substr( 0, 35 ) +'...' :
	    		    			data;
	    		}

			},{
				"data" : "clientPoNumber",
				"defaultContent":"",

			},{
				"data" : "shippingAddress",
				"defaultContent":"",
				render: function ( data, type, row ) {
					if(data!=undefined){
					 return data.length > 35 ?
							 data.substr( 0, 35 ) +'...' :
								 data;
					}else{
						return "";
					}
				}

			},{
				"data" : "dcComment",
				"defaultContent":"",
				render: function ( data, type, row ) {
					if(data!=undefined && data!=""){
					 return data.length > 50 ?
							 data.substr( 0, 50 ) +'...' :
								 data;
					}else{
						return "";
					}
				}

			},
			{
				"data" : "createdDate",
				"defaultContent":"",
				render: function (data, type, row) {
					if (data == null || data === "") return "";
					var d = new Date(data);
					if (isNaN(d.getTime())) { return data; }
					var dd = ("0" + d.getDate()).slice(-2);
					var m = d.getMonth() + 1; // no leading zero
					var yyyy = d.getFullYear();
					var hh = ("0" + d.getHours()).slice(-2);
					var mm = ("0" + d.getMinutes()).slice(-2);
					var ss = ("0" + d.getSeconds()).slice(-2);
					return dd + "-" + m + "-" + yyyy + " " + hh + ":" + mm + ":" + ss;
				}
			},
			{
				"mData" : "pdf"	,
				render : function(datam, type, row) {
					var url = null;
					//url ="/ncpl-sales/dc/details/"+row.dcId;
					return "<button type='button' class='btn btn-default btn-flat btn-xs generateDc' ><i class='fa fa-fw fa-download'></i> Download DC</button>";				
				}
			},
			{
				"mData" : "dcId"	,
				render : function(datam, type, row) {
					var url = null;
					//url ="/ncpl-sales/dc/details/"+row.dcId;
					return "<button type='button' id='"+row.dcId+"' class='btn btn-default btn-flat btn-xs dcView'><i class='fa fa-eye'></i></button>";;				
				}
			},
			{
				"mData" : "dcId"	,
				render : function(datam, type, row) {
					var url = null;
					url ="/ncpl-sales/returnable/"+row.dcId;
					return "<a class='text-info ' href='" + url + "'><button type='button' class='btn btn-default btn-flat btn-xs' >Returnable</button></a>";				
				}
			}
			,
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
	    $('#dcList tbody').on('dblclick', 'tr', function () {
	 	   var data = dcTable.row(this).data();
	 	   var dcId = data.dcId;
	 	   window.location = pageContext+"/api/dc/view?dcId="+dcId;
	 	});
	    $('#dcList tbody').on('click', ".generateDc",function () {
	    	$("#dcExcelDropdown").val("").trigger("change");
	    	 var dcId = $(this).closest("tr").find("td:eq(0)").text();
		 	  $("#dcPartialItemsBtn").val(dcId);
		 	  $("#dcPartialItemsHeader").text("Partially delivered items for DC No. : "+dcId)
		 	  // $("#dcExcelModal").modal("show")
		 	 $.ajax({
		 	    Type:'GET',
		 	    url : api.GET_PARTIAL_DC_ITEMS+"?id="+dcId,
		 	    dataType:'json',
		 	    async: 'false',
		 	    success  : function(response){
		 	    	  $("#dcPartialItemsModal").modal({backdrop: 'static',
								keyboard: false})
		 	    	 $("#dcPartialItemsTable tbody").empty();
		 				var arrayCount=0;
		 				itemsList = response;
		 				var slno=1;
		 				$.each(response,function(index,value){
		 					var soDesc=value.description;
		 					var soModelNo=value.soModelNo;
		 					var totalQty=value.totalQuantity;
		 					var units=value.unit;
		 					var deliveredQty=value.deliveredQuantity;
		 					var todaysQty= value.todaysQty;
		 					var dcItems = "<tr><td width='5%'>"+ slno+"</td><td width='45%' style='word-break: break-word; '>" + soDesc + "</td>" +
		 					"<td width='10%'>" + soModelNo + "</td><td width='10%'>"+units +"</td><td width='10%'>" +totalQty+ "</td>" +
		 					"<td width='10%'>" +deliveredQty  + "</td><td width='10%'>" + todaysQty + "</td></tr>";
		 					$("#dcPartialItemsTable tbody").append(dcItems);
		 		    		arrayCount++;
		 		    		slno++;
		 				
		 			
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
	    
})

$(document).on("click","#dcPartialItemsBtn",function(){
		$("#dcExcelDropdown").val("").trigger("change");
		 var dcId = $(this).val();
	 	  $("#dcId").val(dcId);
	 	  $("#dcExcelHeader").text("DC Number : "+dcId)
	 	  $("#dcExcelModal").modal("show");
	 	  $("#dcPartialItemsModal").modal("hide");
})


$(document).on("click",".dcView",function(){
	var dcId = this.id;
	$.ajax({
	    Type:'GET',
	    url : api.GET_DC_ITEMLIST_BYDCID+"?id="+dcId,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	 $("#dcViewModalTable tbody").empty();
				$("#dcPreviewId").text(dcId);
				$("#dcViewModal").modal("show");
				var arrayCount=0;
				itemsList = response;
				var slno=1;
				$.each(response,function(index,value){
					var soDesc=value.description;
					var soModelNo=value.soModelNo;
					var totalQty=value.totalQuantity;
					var units=value.unit;
					var deliveredQty=value.deliveredQuantity;
					var todaysQty= value.todaysQty;
					var dcItems = "<tr><td width='5%'>"+ slno+"</td><td width='45%' style='word-break: break-word; '>" + soDesc + "</td>" +
					"<td width='10%'>" + soModelNo + "</td><td width='10%'>"+units +"</td><td width='10%'>" +totalQty+ "</td>" +
					"<td width='10%'>" +deliveredQty  + "</td><td width='10%'>" + todaysQty + "</td></tr>";
					$("#dcViewModalTable tbody").append(dcItems);
		    		arrayCount++;
		    		slno++;
				
			
	    	});
				
				$("#dcViewBtn").on("click",function(){
				 	   window.location = pageContext+"/api/dc/view?dcId="+dcId;
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

$(document).on("click","#generateDcExcelBtn",function(){
	var option=$("#dcExcelDropdown").val();
	var dcId = $("#dcId").val();
	if(option==""){
		$.error("Please select Generate Option");
	}else{
	window.location.href="/ncpl-sales/dc/details/"+dcId+"/"+option;
	}
});

$(document).on('click',".archiveCheckbox",function(){
	var dcNum=$(this).closest("tr").find("td:eq(0)").text();
	const isChecked = $(this).is(":checked");
		if(isChecked==true) {
		bootbox.confirm({
			message: "Do you want to archive this DC?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? archiveDC(dcNum):window.location.reload();;
			}
		});	
	}else{
		bootbox.confirm({
			message: "Do you want to remove this DC from archive?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? unArchiveDC(dcNum):window.location.reload();;
			}
		});	
	}
});
function archiveDC(dcNum){
	//window.location.href = pageContext+"/api/update_so_archive?soId="+soId;
	//window.location=pageContext+"/salesList";
	$.ajax({
		type : "POST",  
		url : api.UPDATE_DC_ARCHIVE +"?dcNum="+dcNum,
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

function unArchiveDC(dcNum){
	$.ajax({
		type : "POST",  
		url : api.UPDATE_DC_UNARCHIVE +"?dcNum="+dcNum,
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