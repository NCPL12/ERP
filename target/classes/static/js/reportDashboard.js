$(document).ready(function () {
	
	//getAllSalesListWithStatusNotClosed();
	//getAllPurchaseList();
	//getAllInvoiceList();
	//getAllSalesList();
	//getTdsApprovedList();
	//getSalesItemsWithoutDesignList();
	getPartialRecordOfPendingSalesList();
	getPartialPurchaseList();
	getPartialInvoiceList();
	getPartialSalesList();
	getTdsApprovedListPartial();
	getSalesItemsWithoutDesignListPartial();
	getAllSOWithDesignAndPONotDoneListPartial();
	
	$(document).on("click","#pendingSaleslink",function(){
		getAllSalesListWithStatusNotClosed();
		$(this).hide();
	})
	$(document).on("click","#pendingPurchaselink",function(){
		getAllPurchaseList();
		$(this).hide();
	})
	$(document).on("click","#invoicelink",function(){
		getAllInvoiceList();
		$(this).hide();
	})
	$(document).on("click","#saleslink",function(){
		getAllSalesList();
		$(this).hide();
	})
	$(document).on("click","#tdsLink",function(){
		getTdsApprovedList();
		$(this).hide();
	})
	$(document).on("click","#sowithoutdesignlink",function(){
		getSalesItemsWithoutDesignList();
		$(this).hide();
	})
	$(document).on("click","#sowithdesignlink",function(){
		getAllSOWithDesignAndPONotDoneList();
		$(this).hide();
	})
});

function getAllSalesListWithStatusNotClosed(){
	showLoader()
	$.ajax({
		url: api.SALES_LIST_PENDING,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadSalesTableWithStatusNotClosed(response);
			$("#salesOrderCount").html(response.length)
			hideLoader();
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

var salesTableWithStatusNotClosed = null;
function loadSalesTableWithStatusNotClosed(response) {
	if (response != undefined || response != null) {
		salesTableWithStatusNotClosed = $('#salesListWithStatusNotClosed').DataTable({


			processing : true,
			'columnDefs': [ {
	    	    'targets': [0,1,2,3,4], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'Pending SO List',
	 			title: 'Pending SO List',
	 			exportOptions: {
	                columns: [0,1,3,4]
	            }
	 		}
	 		],

			"order": [[ 2, "desc" ]],
			 "ajax": {
				   'url': api.SALES_LIST_PENDING,
				   'dataSrc': ''
				},
			"destroy": true,
			"columns": [

				{
					"title": "So Number",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.id;

					}

				},
				{
					"title": "Client Name",
					"data": "client Name",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								row.party.partyName.substr( 0, 15 ) +'...' :
									row.party.partyName;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "Client Po Number",
					"data": "clientPoNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.clientPoNumber;

					}

				},
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs pendingSalesView'><i class='fa fa-eye'></i></button>";;
					}

				},
				
			]
		});
	}

}

function getAllPurchaseList(){
	showLoader();
	$.ajax({
		url: api.PURCHASE_LIST_PENDING,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadPurchaseTable(response);
			$("#purchaseOrderCount").html(response.length);
			hideLoader();
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

var purchaseTable = null;
function loadPurchaseTable(response) {
	if (response != undefined || response != null) {
		purchaseTable = $('#purchaseTable').DataTable({

			processing : true,
			'columnDefs': [ {
	    	    'targets': [0,1,2,3], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'Pending Po List',
	 			title: 'Pending Po List',
	 			exportOptions: {
	                columns: [0,1,3]
	            }
	 		}
	 		],
			"order": [[ 2, "desc" ]],
			 "ajax": {
				   'url': api.PURCHASE_LIST_PENDING,
				   'dataSrc': ''
				},
			"destroy": true,
			"columns": [

				{
					"title": "Po Number",
					"data": "poNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.poNumber;

					}

				},
				{
					"title": "Client Name",
					"data": "clientName",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								row.party.partyName.substr( 0, 15 ) +'...' :
									row.party.partyName;


					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "View",
					"data": "poNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.poNumber+"' class='btn btn-default btn-flat btn-xs pendingPurchaseView'><i class='fa fa-eye'></i></button>";;
					}

				},
			]
		});
	}

}


function getAllInvoiceList(){
	showLoader();
	$.ajax({
		url: api.INVOICE_LIST,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadInvoiceTable(response);
			$("#invoiceCount").html(response.length);
			hideLoader();
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

var invoiceTable = null;
function loadInvoiceTable(response) {
	if (response != undefined || response != null) {
		invoiceTable = $('#invoiceTable').DataTable({
			orderCellsTop: true,
			"order": [[ 1, "desc" ]],
			processing : true,
			 "ajax": {
				   'url': api.INVOICE_LIST,
				   'dataSrc': ''
				},
			"destroy": true,
			"columns": [

				{
					"title": "Invoice Number",
					"data": "invoiceId",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.invoiceId;

					}

				},
				{
					"data" : "invoiceId",
					"defaultContent":"",
					"class":"hideTd",
					render : function(aaData,type, row) {
						var date=aaData.split("-");
						var formattedDate = date.reverse()[1]+date[0];
					
							return  formattedDate; 
					}

				},
				{
					"title": "Client Name",
					"data": "clientName",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.clientName.length > 15 ?
								row.clientName.substr( 0, 15 ) +'...' :
									row.clientName;


					}
				},
				{
					"title": "Client Po No.",
					"data": "clientPoNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.clientPoNumber;

					}
				}
			]
		});
	}

}

function getAllSalesList(){
	showLoader();
	$.ajax({
		url: api.GET_ALL_SALES_LIST,
		type: 'GET',

		success: function (response) {
			var projectCount=response.length;
			console.log(response);

			loadSalesTable(response);
			$.each(response,function(index,value){
				if(value.clientPoNumber.includes("A1") || value.clientPoNumber.includes("Non Billable")
						|| value.clientPoNumber.includes("non billable") || value.clientPoNumber.includes("Nonbillable") || value.clientPoNumber.includes("Non-Billable")){
					projectCount=projectCount-1;
				}
			})
			$("#projectPreviewCount").html(projectCount);
			hideLoader();
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

var salesTable = null;
function loadSalesTable(response) {
	if (response != undefined || response != null) {
		salesTable = $('#salesListTable').DataTable({


			processing : true,
			'columnDefs': [ {
	    	    'targets': [0,1,2,3], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'SO List',
	 			title: 'SO List',
	 			exportOptions: {
	                columns: [0,1,2,3]
	            }
	 		}
	 		],
			"order": [[ 2, "desc" ]],
			 "ajax": {
				   'url': api.GET_ALL_SALES_LIST,
				   'dataSrc': ''
				},
			"destroy": true,
			"columns": [

				{
					"title": "So Number",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.id;

					}

				},
				{
					"title": "Client Name",
					"data": "client Name",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								row.party.partyName.substr( 0, 15 ) +'...' :
									row.party.partyName;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;
					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "Client Po No.",
					"data": "clientPoNumber",
					"defaultContent": ""

				}
				,
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs projectSalesView'><i class='fa fa-eye'></i></button>";;
					}

				},
			]
		});
		   $('#salesListTable tbody').on('dblclick', 'tr', function () {
			   var data = salesTable.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/project_preview?salesOrderId="+salesOrderId;
			});
	}

}

function getTdsApprovedList(){
	showLoader();
	$.ajax({
		url: api.GET_TDS_APPROVED_LIST,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadTdsApprovedTable(response);
			$("#tdsItemsCount").html(response.length);
			hideLoader();
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

var tdsTabe = null;
function loadTdsApprovedTable(response) {
	if (response != undefined || response != null) {
		tdsTabe = $('#tdsApprovedItemsTable').DataTable({


			processing : true,
			'columnDefs': [ {
	    	    'targets': [0,1,2,3], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
			 "ajax": {
				   'url': api.GET_TDS_APPROVED_LIST,
				   'dataSrc': ''
				},
			"destroy": true,
			"columns": [

				{
					"title": "Client PO No.",
					"data": "clientPoNumber",
					"defaultContent": ""
				},
				{
					"title": "Client Name",
					"data": "salesOrderObj",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								 row.party.partyName.substr( 0, 15 ) +'...' :
									 row.party.partyName;

					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				}
				,
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs tdsApprovedView'><i class='fa fa-eye'></i></button>";;
					}

				},
			]
		});
		   $('#tdsApprovedItemsTable tbody').on('dblclick', 'tr', function () {
			   var data = tdsTabe.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/sales_order/view?salesOrderId="+salesOrderId;
			});
	}

}
function getSalesItemsWithoutDesignList(){
	showLoader();
	$.ajax({
		url: api.GET_SALES_ITEMS_LIST_WITHOUT_DESIGN,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadSalesItemsWithoutDesignTable(response);
			$("#sowithoutDesignCount").html(response.length);
			hideLoader();
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

var salesItemsWithoutDesignTabe = null;
function loadSalesItemsWithoutDesignTable(response) {
	if (response != undefined || response != null) {
		salesItemsWithoutDesignTabe = $('#salesItemsWithoutDesignTble').DataTable({


			processing : true,
			'columnDefs': [ {
	    	    'targets': [0,1,2], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'SO without Design',
	 			title: 'SO without Design',
	 			exportOptions: {
	                columns: [0,1,3]
	            }
	 		}
	 		],
	    	 "order": [[ 2, "desc" ]],
			 "ajax": {
				   'url': api.GET_SALES_ITEMS_LIST_WITHOUT_DESIGN,
				   'dataSrc': ''
				},
			"destroy": true,
			"columns": [

				{
					"title": "Client PO No.",
					"data": "clientPoNumber",
					"defaultContent": ""
				},
				{
					"title": "Client Name",
					"data": "salesOrderObj",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								 row.party.partyName.substr( 0, 15 ) +'...' :
									 row.party.partyName;

					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs salesItemWithoutDesignView'><i class='fa fa-eye'></i></button>";;
					}

				},
				
			]
		});
		   $('#salesItemsWithoutDesignTble tbody').on('dblclick', 'tr', function () {
			   var data = salesItemsWithoutDesignTabe.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/sales_order/view?salesOrderId="+salesOrderId;
			});
	}

}

function getAllSOWithDesignAndPONotDoneList(){
	showLoader();
	$.ajax({
		url: api.GET_SALES_LIST_WITH_DESIGN,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadSalesItemsWithDesignTable(response);
			$("#sowithDesignCount").html(response.length);
			hideLoader();
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
var salesItemsWithDesignTabe = null;
function loadSalesItemsWithDesignTable(response) {
	if (response != undefined || response != null) {
		salesItemsWithDesignTabe = $('#soWithDesignTable').DataTable({


			processing : true,
			'columnDefs': [ {
	    	    'targets': [0,1,2], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'SO with Design',
	 			title: 'SO with Design',
	 			exportOptions: {
	                columns: [0,1,3]
	            }
	 		}
	 		],
	    	 "order": [[ 2, "desc" ]],
			 "ajax": {
				   'url': api.GET_SALES_LIST_WITH_DESIGN,
				   'dataSrc': ''
				},
			"destroy": true,
			"columns": [

				{
					"title": "Client PO No.",
					"data": "clientPoNumber",
					"defaultContent": ""
				},
				{
					"title": "Client Name",
					"data": "salesOrderObj",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								 row.party.partyName.substr( 0, 15 ) +'...' :
									 row.party.partyName;

					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs salesItemWithDesignView'><i class='fa fa-eye'></i></button>";;
					}

				},
				
			]
		});
		   $('#soWithDesignTable tbody').on('dblclick', 'tr', function () {
			   var data = salesItemsWithDesignTabe.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/sales_order/view?salesOrderId="+salesOrderId;
			});
	}

}




$(document).on("click",".pendingSalesView",function(){
	
	 
	var soId = this.id;
	var className = "so";
	$.ajax({
	    Type:'GET',
	    url : api.SALES_LIST_BY_SOID+"?id="+soId+"&&className="+className,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	
	    	$("#pendingSalesTable tbody").empty();
	    	$("#pendingSalesModal").modal("show");
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
	    			}
	    			 
	    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word; '>" + description + "</td>" +
					"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
					"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
					"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
					"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
					"</tr>";
		    		$("#pendingSalesTable tbody").append(soItems);
		    		arrayCount++;
	    		}else{
	    			$.each(value.designItems,function(i,v){
		    			
		    			if(i!=0){
		    				var	slNo="";
		    				var description="";
		    				var modelNumber="";
		    				var poQty="";
		    				var unitName="";
		    				var hsn="";
		    	    		var sac="";
		    	    		var supplyprice="";
		    	    		var servicePrice="";
		    	    		var amount="";
		    			}else{
		    				 slNo=value.slNo;
		    	    		 description=value.description;
		    	    		 salesItemId =  value.id;
		    	    		 modelNumber = value.modelNo;
		    	    		 poQty=value.quantity;
		    	    		 unitName=value.item_units.name;
		    	    		 hsn=value.hsnCode;
		    	    		 sac=value.servicehsnCode;
		    	    		 supplyprice=value.unitPrice;
		    	    		 servicePrice=value.servicePrice;
		    	    		 amount=value.amount;
		    			}
			    			var model=v.itemId;
			    			var qty=v.quantity;
			    			var itemId = v.itemMasterId;
			    		
			    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%'  style='word-break: break-word; '>" + description + "</td>" +
							"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
							"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
							"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
							"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
							"</tr>";
				    		$("#pendingSalesTable tbody").append(soItems);
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
	
	
})

$(document).on("click",".projectSalesView",function(){
	
	
	var soId = this.id;
	var className = "so";
	$.ajax({
	    Type:'GET',
	    url : api.SALES_LIST_BY_SOID+"?id="+soId+"&&className="+className,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	 $("#projectModalTable tbody").empty();
	    	 $("#projectModal").modal("show");
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
	    			}
	    			 
	    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word; '>" + description + "</td>" +
					"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
					"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
					"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
					"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
					"</tr>";
		    		$("#projectModalTable tbody").append(soItems);
		    		arrayCount++;
	    		}else{
	    			$.each(value.designItems,function(i,v){
		    			
		    			if(i!=0){
		    				var	slNo="";
		    				var description="";
		    				var modelNumber="";
		    				var poQty="";
		    				var unitName="";
		    				var hsn="";
		    	    		var sac="";
		    	    		var supplyprice="";
		    	    		var servicePrice="";
		    	    		var amount="";
		    			}else{
		    				 slNo=value.slNo;
		    	    		 description=value.description;
		    	    		 salesItemId =  value.id;
		    	    		 modelNumber = value.modelNo;
		    	    		 poQty=value.quantity;
		    	    		 unitName=value.item_units.name;
		    	    		 hsn=value.hsnCode;
		    	    		 sac=value.servicehsnCode;
		    	    		 supplyprice=value.unitPrice;
		    	    		 servicePrice=value.servicePrice;
		    	    		 amount=value.amount;
		    			}
			    			var model=v.itemId;
			    			var qty=v.quantity;
			    			var itemId = v.itemMasterId;
			    		
			    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word; '>" + description + "</td>" +
							"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
							"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
							"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
							"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
							"</tr>";
				    		$("#projectModalTable tbody").append(soItems);
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
	
	
})

$(document).on("click",".salesItemWithoutDesignView",function(){
	
	 
	var soId = this.id;
	var className = "so";
	$.ajax({
	    Type:'GET',
	    url : api.SALES_LIST_BY_SOID+"?id="+soId+"&&className="+className,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	
	    	$("#salesItemWithoutDesignModalTable tbody").empty();
	    	$("#salesItemwithoutDesignModal").modal("show");
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
	    			}
	    			 
	    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word !important; '>" + description + "</td>" +
					"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
					"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
					"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
					"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
					"</tr>";
		    		$("#salesItemWithoutDesignModalTable tbody").append(soItems);
		    		arrayCount++;
	    		}else{
	    			$.each(value.designItems,function(i,v){
		    			
		    			if(i!=0){
		    				var	slNo="";
		    				var description="";
		    				var modelNumber="";
		    				var poQty="";
		    				var unitName="";
		    				var hsn="";
		    	    		var sac="";
		    	    		var supplyprice="";
		    	    		var servicePrice="";
		    	    		var amount="";
		    			}else{
		    				 slNo=value.slNo;
		    	    		 description=value.description;
		    	    		 salesItemId =  value.id;
		    	    		 modelNumber = value.modelNo;
		    	    		 poQty=value.quantity;
		    	    		 unitName=value.item_units.name;
		    	    		 hsn=value.hsnCode;
		    	    		 sac=value.servicehsnCode;
		    	    		 supplyprice=value.unitPrice;
		    	    		 servicePrice=value.servicePrice;
		    	    		 amount=value.amount;
		    			}
			    			var model=v.itemId;
			    			var qty=v.quantity;
			    			var itemId = v.itemMasterId;
			    		
			    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word !important; '>" + description + "</td>" +
							"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
							"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
							"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
							"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
							"</tr>";
				    		$("#salesItemWithoutDesignModalTable tbody").append(soItems);
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
	
	
})

$(document).on("click",".salesItemWithDesignView",function(){
	
	 
	var soId = this.id;
	var className = "so";
	$.ajax({
	    Type:'GET',
	    url : api.SALES_LIST_BY_SOID+"?id="+soId+"&&className="+className,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	
	    	$("#salesItemWithDesignModalTable tbody").empty();
	    	$("#salesItemwithDesignModal").modal("show");
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
	    			}
	    			 
	    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word !important; '>" + description + "</td>" +
					"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
					"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
					"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
					"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
					"</tr>";
		    		$("#salesItemWithDesignModalTable tbody").append(soItems);
		    		arrayCount++;
	    		}else{
	    			$.each(value.designItems,function(i,v){
		    			
		    			if(i!=0){
		    				var	slNo="";
		    				var description="";
		    				var modelNumber="";
		    				var poQty="";
		    				var unitName="";
		    				var hsn="";
		    	    		var sac="";
		    	    		var supplyprice="";
		    	    		var servicePrice="";
		    	    		var amount="";
		    			}else{
		    				 slNo=value.slNo;
		    	    		 description=value.description;
		    	    		 salesItemId =  value.id;
		    	    		 modelNumber = value.modelNo;
		    	    		 poQty=value.quantity;
		    	    		 unitName=value.item_units.name;
		    	    		 hsn=value.hsnCode;
		    	    		 sac=value.servicehsnCode;
		    	    		 supplyprice=value.unitPrice;
		    	    		 servicePrice=value.servicePrice;
		    	    		 amount=value.amount;
		    			}
			    			var model=v.itemId;
			    			var qty=v.quantity;
			    			var itemId = v.itemMasterId;
			    		
			    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word !important; '>" + description + "</td>" +
							"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
							"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
							"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
							"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
							"</tr>";
				    		$("#salesItemWithDesignModalTable tbody").append(soItems);
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
	
	
})

$(document).on("click",".tdsApprovedView",function(){
	
	
	var soId = this.id;
	var className = "so";
	$.ajax({
	    Type:'GET',
	    url : api.SALES_LIST_BY_SOID+"?id="+soId+"&&className="+className,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	 $("#tdsApprovedModalTable tbody").empty();
	    	 $("#tdsApprovedModal").modal("show");
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
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
	    	    		 hsn=value.hsnCode;
	    	    		 sac=value.servicehsnCode;
	    	    		 supplyprice=value.unitPrice;
	    	    		 servicePrice=value.servicePrice;
	    	    		 amount=value.amount;
	    			}
	    			 
	    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word; '>" + description + "</td>" +
					"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
					"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
					"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
					"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
					"</tr>";
		    		$("#tdsApprovedModalTable tbody").append(soItems);
		    		arrayCount++;
	    		}else{
	    			$.each(value.designItems,function(i,v){
		    			
		    			if(i!=0){
		    				var	slNo="";
		    				var description="";
		    				var modelNumber="";
		    				var poQty="";
		    				var unitName="";
		    				var hsn="";
		    	    		var sac="";
		    	    		var supplyprice="";
		    	    		var servicePrice="";
		    	    		var amount="";
		    			}else{
		    				 slNo=value.slNo;
		    	    		 description=value.description;
		    	    		 salesItemId =  value.id;
		    	    		 modelNumber = value.modelNo;
		    	    		 poQty=value.quantity;
		    	    		 unitName=value.item_units.name;
		    	    		 hsn=value.hsnCode;
		    	    		 sac=value.servicehsnCode;
		    	    		 supplyprice=value.unitPrice;
		    	    		 servicePrice=value.servicePrice;
		    	    		 amount=value.amount;
		    			}
			    			var model=v.itemId;
			    			var qty=v.quantity;
			    			var itemId = v.itemMasterId;
			    		
			    			var soItems = "<tr><td width='5%'>"+ slNo+"</td><td width='20%' style='word-break: break-word; '>" + description + "</td>" +
							"<td width='10%'>" + modelNumber + "</td><td width='8%'>"+hsn +"</td><td width='8%'>" +sac+ "</td>" +
							"<td width='7%'>" +poQty  + "</td><td width='7%'>" + unitName + "</td>"+
							"<td width='7%'>" + supplyprice + "</td><td width='7%'>" + servicePrice + "</td><td width='10%'>" + amount+ "</td>"+
							"<td width='5%'>" + model + "</td><td width='5%'>" + qty+ "</td>"+
							"</tr>";
				    		$("#tdsApprovedModalTable tbody").append(soItems);
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
	
	
})

$(document).on("click",".pendingPurchaseView",function(){
	var className="po";
	var poNumber = this.id;
	//Sending class name to differentiate between api called in grn and po..
	$.ajax({
		type:'GET',
		url : api.PURCHASE_LIST_BY_POID  + "?id="+poNumber+ "&&className="+className,
		success : function(response) {
			 $("#pendingPurchaseModalTable tbody").empty();
			$("#pendingPurchaseModal").modal("show");
			var arrayCount=0;
			itemsList = response;
			var slno=1;
			$.each(response,function(index,value){
				var poDesc=value.poDescription;
				var modelNo=value.modelNo;
				var hsn=value.hsnCode;
				var qty=value.quantity;
				var unit=value.unitName;
				var unitPrice=value.unitPrice;
				var amount= value.amount;
				var poItems = "<tr><td width='5%'>"+ slno+"</td><td width='25%' style='word-break: break-word; '>" + poDesc + "</td>" +
				"<td width='10%'>" + modelNo + "</td><td width='8%'>"+hsn +"</td><td width='7%'>" +qty+ "</td>" +
				"<td width='7%'>" +unit  + "</td><td width='12%'>" + unitPrice + "</td>"+
				"<td width='10%'>" + amount + "</td>"+
				"</tr>";
				$("#pendingPurchaseModalTable tbody").append(poItems);
	    		arrayCount++;
	    		slno++;
			})
			/*popreviewitemstable= $('#pendingPurchaseModalTable').DataTable({
			    	orderCellsTop: true,
				    fixedHeader: true,
				    "bDestroy": true,
				    paging: false,
				    searching: false,
				    dom: 't',
			    	"aaData": itemsList,
			    	"aoColumns": [ {
						"mData" : "poNumber",
						"defaultContent":"NA",
						render : function(aaData, type, row) {
							//return "";
							slno++;
							return slno;
							
							
						}
						
					}, {
						"mData" : "poDescription",
			    		
					}, {
						"mData" : "modelNo",

					}, {
						"mData" : "hsnCode",
						
					},{
						"mData" : "quantity",
					},{
						"mData" : "modelNo",
						render: function (mData, type, row, meta) {
							
							return row.unitName;
						}
					},
				   {
						"mData" : "unitPrice",
						render : function(aaData, type, row) {
							//return "";
							var up = commaSeparateNumber(row.unitPrice);
							return up;
							
							
						}
					},{
						"mData" : "amount",
						render : function(aaData, type, row) {
							//return "";
							var ap = commaSeparateNumber(row.amount);
							return ap;
							
							
						}
					}
					]
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
	});  
})

function getPartialRecordOfPendingSalesList(){
	$.ajax({
		url: api.SALES_LIST_PENDING_PARTIAL,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadPartialPendingSalesTable(response);
			//$("#salesOrderCount").html(response.length)
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

var partialPendingSalesTable=null;
function loadPartialPendingSalesTable(response){
	if (response != undefined || response != null) {
		partialPendingSalesTable = $('#salesListWithStatusNotClosed').DataTable({


			'columnDefs': [ {
	    	    'targets': [0,1,2,3,4], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'Pending SO List',
	 			title: 'Pending SO List',
	 			exportOptions: {
	                columns: [0,1,3,4]
	            }
	 		}
	 		],

			"order": [[ 2, "desc" ]],
			 'data':response,
			"destroy": true,
			"columns": [

				{
					"title": "So Number",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.id;

					}

				},
				{
					"title": "Client Name",
					"data": "client Name",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								row.party.partyName.substr( 0, 15 ) +'...' :
									row.party.partyName;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "Client Po Number",
					"data": "clientPoNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.clientPoNumber;

					}

				},
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs pendingSalesView'><i class='fa fa-eye'></i></button>";;
					}

				},
				
			]
		});
	}
}

function getPartialPurchaseList(){
	$.ajax({
		url: api.PURCHASE_LIST_PENDING_PARTIAL,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadPurchaseTablePartial(response);
			//$("#purchaseOrderCount").html(response.length);
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

var partialPurchaseTable = null;
function loadPurchaseTablePartial(response) {
	if (response != undefined || response != null) {
		partialPurchaseTable = $('#purchaseTable').DataTable({

			'columnDefs': [ {
	    	    'targets': [0,1,2,3], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'Pending Po List',
	 			title: 'Pending Po List',
	 			exportOptions: {
	                columns: [0,1,3]
	            }
	 		}
	 		],
			"order": [[ 2, "desc" ]],
			"data":response,
			"destroy": true,
			"columns": [

				{
					"title": "Po Number",
					"data": "poNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.poNumber;

					}

				},
				{
					"title": "Client Name",
					"data": "clientName",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								row.party.partyName.substr( 0, 15 ) +'...' :
									row.party.partyName;


					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "View",
					"data": "poNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.poNumber+"' class='btn btn-default btn-flat btn-xs pendingPurchaseView'><i class='fa fa-eye'></i></button>";;
					}

				},
			]
		});
	}

}

function getPartialInvoiceList(){
	$.ajax({
		url: api.INVOICE_LIST_PARTIAL,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadInvoiceTablePartial(response);
			//$("#invoiceCount").html(response.length);
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

var invoiceTablePartial = null;
function loadInvoiceTablePartial(response) {
	if (response != undefined || response != null) {
		invoiceTablePartial = $('#invoiceTable').DataTable({
			orderCellsTop: true,
			"order": [[ 1, "desc" ]],
			 "data":response,
			"destroy": true,
			"columns": [

				{
					"title": "Invoice Number",
					"data": "invoiceId",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.invoiceId;

					}

				},
				{
					"data" : "invoiceId",
					"defaultContent":"",
					"class":"hideTd",
					render : function(aaData,type, row) {
						var date=aaData.split("-");
						var formattedDate = date.reverse()[1]+date[0];
					
							return  formattedDate; 
					}

				},
				{
					"title": "Client Name",
					"data": "clientName",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.clientName.length > 15 ?
								row.clientName.substr( 0, 15 ) +'...' :
									row.clientName;


					}
				},
				{
					"title": "Client Po No.",
					"data": "clientPoNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.clientPoNumber;

					}
				}
			]
		});
	}

}

function getPartialSalesList(){
	
	$.ajax({
		url: api.GET_PARTIAL_SALES_LIST,
		type: 'GET',

		success: function (response) {
			var projectCount=response.length;
			console.log(response);

			loadSalesTablePartial(response);
			$.each(response,function(index,value){
				if(value.clientPoNumber.includes("A1") || value.clientPoNumber.includes("Non Billable")
						|| value.clientPoNumber.includes("non billable") || value.clientPoNumber.includes("Nonbillable") || value.clientPoNumber.includes("Non-Billable")){
					projectCount=projectCount-1;
				}
			})
			//$("#projectPreviewCount").html(projectCount);
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

function loadSalesTablePartial(response) {
	if (response != undefined || response != null) {
		salesTable = $('#salesListTable').DataTable({


			'columnDefs': [ {
	    	    'targets': [0,1,2,3], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'SO List',
	 			title: 'SO List',
	 			exportOptions: {
	                columns: [0,1,2,3]
	            }
	 		}
	 		],
			"order": [[ 2, "desc" ]],
			 "data":response,
			"destroy": true,
			"columns": [

				{
					"title": "So Number",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.id;

					}

				},
				{
					"title": "Client Name",
					"data": "client Name",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								row.party.partyName.substr( 0, 15 ) +'...' :
									row.party.partyName;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;
					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "Client Po No.",
					"data": "clientPoNumber",
					"defaultContent": ""

				}
				,
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs projectSalesView'><i class='fa fa-eye'></i></button>";;
					}

				}
			]
		});
		   $('#salesListTable tbody').on('dblclick', 'tr', function () {
			   var data = salesTable.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/project_preview?salesOrderId="+salesOrderId;
			});
	}

}

function getTdsApprovedListPartial(){
	$.ajax({
		url: api.GET_TDS_APPROVED_LIST_PARTIAL,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadTdsApprovedTablePartial(response);
			//$("#tdsItemsCount").html(response.length);
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

function loadTdsApprovedTablePartial(response) {
	if (response != undefined || response != null) {
		tdsTabe = $('#tdsApprovedItemsTable').DataTable({


			'columnDefs': [ {
	    	    'targets': [0,1,2,3], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
			 "data":response,
			"destroy": true,
			"columns": [

				{
					"title": "Client PO No.",
					"data": "clientPoNumber",
					"defaultContent": ""
				},
				{
					"title": "Client Name",
					"data": "salesOrderObj",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								 row.party.partyName.substr( 0, 15 ) +'...' :
									 row.party.partyName;

					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				}
				,
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs tdsApprovedView'><i class='fa fa-eye'></i></button>";;
					}

				},
			]
		});
		   $('#tdsApprovedItemsTable tbody').on('dblclick', 'tr', function () {
			   var data = tdsTabe.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/sales_order/view?salesOrderId="+salesOrderId;
			});
	}

}
function getSalesItemsWithoutDesignListPartial(){
	$.ajax({
		url: api.GET_SALES_ITEM_WITHOUT_DESIGN_PARTIAL,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadSalesItemsWithoutDesignTablePartial(response);
			//$("#sowithoutDesignCount").html(response.length);
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

function loadSalesItemsWithoutDesignTablePartial(response) {
	if (response != undefined || response != null) {
		salesItemsWithoutDesignTabe = $('#salesItemsWithoutDesignTble').DataTable({


			'columnDefs': [ {
	    	    'targets': [0,1,2], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'SO without Design',
	 			title: 'SO without Design',
	 			exportOptions: {
	                columns: [0,1,3]
	            }
	 		}
	 		],
	    	 "order": [[ 2, "desc" ]],
			"data":response,
			"destroy": true,
			"columns": [

				{
					"title": "Client PO No.",
					"data": "clientPoNumber",
					"defaultContent": ""
				},
				{
					"title": "Client Name",
					"data": "salesOrderObj",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								 row.party.partyName.substr( 0, 15 ) +'...' :
									 row.party.partyName;

					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs salesItemWithoutDesignView'><i class='fa fa-eye'></i></button>";;
					}

				},
				
			]
		});
		   $('#salesItemsWithoutDesignTble tbody').on('dblclick', 'tr', function () {
			   var data = salesItemsWithoutDesignTabe.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/sales_order/view?salesOrderId="+salesOrderId;
			});
	}

}

function getAllSOWithDesignAndPONotDoneListPartial(){
	$.ajax({
		url: api.GET_SALES_ITEM_WITH_DESIGN_PARTIAL,
		type: 'GET',

		success: function (response) {
			console.log(response);

			loadSalesItemsWithDesignTablePartial(response);
			//$("#sowithoutDesignCount").html(response.length);
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

function loadSalesItemsWithDesignTablePartial(response) {
	if (response != undefined || response != null) {
		salesItemsWithDesignTabe = $('#soWithDesignTable').DataTable({


			'columnDefs': [ {
	    	    'targets': [0,1,2], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	 dom: 'Bfrtip',
	    	 buttons: [{
	    		 text : 'Download',
	 			extend: 'excel',
	 			filename: 'SO with Design',
	 			title: 'SO with Design',
	 			exportOptions: {
	                columns: [0,1,3]
	            }
	 		}
	 		],
	    	 "order": [[ 2, "desc" ]],
			"data":response,
			"destroy": true,
			"columns": [

				{
					"title": "Client PO No.",
					"data": "clientPoNumber",
					"defaultContent": ""
				},
				{
					"title": "Client Name",
					"data": "salesOrderObj",
					"defaultContent": "",
					render: function (aaData, type, row) {
						return row.party.partyName.length > 15 ?
								 row.party.partyName.substr( 0, 15 ) +'...' :
									 row.party.partyName;

					}
				},
				
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					"class":"hideTd",
					render: function (aaData, type, row) {
						//var date= new Date(row.created).toLocaleDateString();
						var newdate = moment(row.created).format("YYYY-MM-DD HH:mm:ss") ;
						return newdate;

					}
				},
				{
					"title": "Created",
					"data": "createdDate",
					"defaultContent": "",
					render: function (aaData, type, row) {
						var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						var format= newdate.split(" ");
						var dateFormat = format[0].split("-");
						dateFormat = dateFormat[2]+"-"+dateFormat[1]+"-"+dateFormat[0]+" "+format[1];
						return dateFormat;

					}
				},
				{
					"title": "View",
					"data": "soNumber",
					"defaultContent": "",
					render: function (aaData, type, row) {
						url="";
						//url = pageContext+"/api/salesOrder/view?salesOrderId="+row.id;
						return "<button type='button' id='"+row.id+"' class='btn btn-default btn-flat btn-xs salesItemWithDesignView'><i class='fa fa-eye'></i></button>";;
					}

				}
				
			]
		});
		   $('#soWithDesignTable tbody').on('dblclick', 'tr', function () {
			   var data = salesItemsWithDesignTabe.row(this).data();
			   var salesOrderId = data.id;
			   window.location = pageContext+"/api/sales_order/view?salesOrderId="+salesOrderId;
			});
	}

}

