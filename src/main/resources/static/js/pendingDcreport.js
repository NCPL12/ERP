$(document).ready(function () {
	loadItemTable();
});
var datatable = null;
function loadItemTable() {

	datatable = $('#pendingDcreportlist').DataTable({

		"data": pendingDcList,
		/* lengthChange: false,*/
		"destroy": true,
		"order": [[ 5, "desc" ]],
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
				"width":"41%",
				"class": "text-field-large-nowrap",
				render : function(data, type, row) {
					return  data.length > 85 ?
							data.substr( 0, 85 ) +'...' :
							data;

				}
				

			},
			{
				"title": "HSN",
				"data": "hsnCode",
				"defaultContent": "",
				"width":"7%"

			}, {
				"title": "Units",
				"data": "units",
				"defaultContent": "",
				"width":"10%"
			},
			{
				"title": "Unit Price",
				"data": "unitprice",
				"defaultContent": "",
				"width":"10%"
			},
			{
				"title": "Amount",
				"data": "amount",
				"defaultContent": "",
				"width":"10%"
			},
			{
				"data": "Date",
				"class":"hideTd",
				render : function(data, type, row) {
					var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
						return  newdate; 
				}

			},
			

			{
				"title": "Date",
				"data": "Date",
				"defaultContent": "",
				"width":"15%"

			},{
				"title": "Qty",
				"data": "Qty",
				"defaultContent": "",
				"width":"7%"

			},
			

		]

	})/*.buttons().container().appendTo( $('#itemMasterList_length') )*/;

	


}