$(document).ready(function () {
	getAllDcBySalesOrder();
	getAllInvoiceBySalesOrder();
	getAllPurchaseOrderBySalesOrder();
	getAllGrnBySalesOrder();
	$("#invoiceCount").html(invoiceList.length);
	$("#dcCount").html(dcList.length);
	$("#purchaseCount").html(purchaseList.length);
	$("#grnCount").html(grnList.length);
});

var invoiceTable=null
function getAllInvoiceBySalesOrder(){
	invoiceTable = $('#invoiceList').DataTable({

		processing : true,
		"data":invoiceList,
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

var dcTable=null
function getAllDcBySalesOrder(){
	dcTable = $('#dcList').DataTable({

		processing : true,
		"data":dcList,
		"destroy": true,
		"columns": [

			{
				"title": "Dc Number",
				"data": "dcId",
				"defaultContent": "",
				render: function (aaData, type, row) {
					return row.dcId;

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

var purchaseTable=null
function getAllPurchaseOrderBySalesOrder(){
	purchaseTable = $('#purchaseList').DataTable({

		processing : true,
		"data":purchaseList,
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
				"data": "created",
				"defaultContent": "",
				render: function (aaData, type, row) {
					var date= new Date(row.created).toLocaleDateString();
					return date;

				}
			}
		]
	});
	
}

var grnTable=null;
function getAllGrnBySalesOrder(){
	grnTable = $('#grnList').DataTable({

		processing : true,
		"data":grnList,
		"destroy": true,
		"columns": [

			{
				"title": "Grn No",
				"data": "grnId",
				"defaultContent": "",
				render: function (aaData, type, row) {
					return row.grnId;

				}

			},
			{
				"title": "PO Number",
				"data": "poNumber",
				"defaultContent": "",
				render: function (aaData, type, row) {
					return row.poNumber;


				}
			},
			{
				"title": "Created",
				"data": "created",
				"defaultContent": "",
				render: function (aaData, type, row) {
					var date= new Date(row.created).toLocaleDateString();
					return date;

				}
			}
		]
	});
	
}