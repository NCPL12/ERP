$(document).ready(function () {
	loadItemTable();
});
var datatable = null;
function loadItemTable() {

	datatable = $('#poItemHistoryTable').DataTable({

		"data": poItemHistoryList,
		/* lengthChange: false,*/
		"destroy": true,
		//"order": [[ 5, "desc" ]],
		"columnDefs": [
			{ "width": "85px", "targets": 0 }
		],
		"dom": '<"panel panel-default"<"panel-heading"<"row"<"col-md-6"l><"col-md-6 text-right"f>>>t<"panel-footer"<"row"<"col-md-6"i><"col-md-6 text-right"p>>>>',
		buttons: [{
			extend: 'excel',
			filename: 'Current Item Stock',
			title: 'Current Item Stock'
		}
		],
		"columns": [

			{
				"title": "Description",
				"data": "Description",
				"defaultContent": "",
				"width":"35%",
				"class": "text-field-large-nowrap",
				render : function(data, type, row) {
					return  data.length > 75 ?
							data.substr( 0, 75 ) +'...' :
							data;

				}
				

			},{
				"title": "PO No.",
				"data": "poNumber",
				"defaultContent": "",
				"width":"10%",
			},
		
			{
				"title": "Unit Price",
				"data": "unitPrice",
				"defaultContent": "",
				"width":"10%",
			},
		
			{
				"title": "Po Date",
				"data": "Date",
				"width":"10%",
				render : function(data, type, row) {
					var newdate = moment(new Date(row.created)).format("YYYY-MM-DD HH:mm:ss") ;
						return  newdate; 
				}

			},{
				"title": "Vendor",
				"data" : "vendor",
				"width":"25%",
				render : function(data, type, row) {
					
					return  data.length > 35 ?
							data.substr( 0, 35 ) +'...' :
							data;

				}

			}
			

		
			

		]

	})/*.buttons().container().appendTo( $('#itemMasterList_length') )*/;

	


}