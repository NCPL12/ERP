$(document).ready(function () {
	loadItemTable();
});
var datatable = null;
function loadItemTable() {

	datatable = $('#pendingreportlist').DataTable({

		"data": pendingList,
		/* lengthChange: false,*/
		"destroy": true,
		"order": [[ 4, "desc" ]],
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
				"title": "Model No",
				"data": "model",
				"defaultContent": "",
				"width": "15%",
				

			},
			{
				"title": "Description",
				"data": "itemName",
				"defaultContent": "",
				"width": "36%",
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
				"width": "7%",
				

			}, {
				"title": "Units",
				"data": "units",
				"defaultContent": "",
				"width": "10%",
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
				"width": "15%",

			},{
				"title": "Qty",
				"data": "Qty",
				"defaultContent": "",
				"width": "7%",
				render : function(data, type, row) {
					var newdata = Math.round(data * 100) / 100
						return  newdata; 
				}
				

			},
			

			{
				"title": "PO Number",
				"data": "poNum",
				"defaultContent": "",
				"width": "10%",

			}
			

		]

	})/*.buttons().container().appendTo( $('#itemMasterList_length') )*/;

	


}