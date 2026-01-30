/**
 * 
 */
$(document).ready(function () {
	loadSalesListTable();
});
var datatable = null;
function loadSalesListTable() {

	datatable = $('#salesOrderList').DataTable({

		"data": soList,
		"destroy": true,
		"order": [[ 3, "desc" ]],
		'columnDefs': [ {
    	    'targets': [0,1,2,3], /* table column index */
    	    'orderable': false, /* here set the true or false */
    	 }],
		
		"columns": [

			{
				"title": "Client Po Number",
				"data": "clientPoNumber",
				"defaultContent": "",
				"class": "text-field-large-nowrap"

			},
			{
				"title": "Client",
				"data" : "party",

				render : function(data, type, row) {
					var company;
					if (data == null) {
						company = "";
					} else {
						company = data.partyName;
					
					}
					return  company.length > 35 ?
					company.substr( 0, 35 ) +'...' :
					company;

				}


			}, {
				"title": "Date",
				"data": "created",
				"defaultContent": "",
				render : function(data, type, row) {
					var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
						return  newdate; 
				}
			},
			{
				"data" : "created",
				"class":"hideTd",
				render : function(data, type, row) {
					var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
					return  newdate; 
				}
			}
			

		]

	})

	


}