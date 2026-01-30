/**
 * 
 */
/**
 * 
 */
$(document).ready(function () {
	loadDcListTable();
});
var datatable = null;
function loadDcListTable() {

	datatable = $('#dcList').DataTable({

		"data": dcList,
		"destroy": true,
		"order": [[ 0, "desc" ]],
		'columnDefs': [ {
    	    'targets': [0,1,2,3], /* table column index */
    	    'orderable': false, /* here set the true or false */
    	 }],
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

			}
			]
	});
	  $('#dcList tbody').on('dblclick', 'tr', function () {
	 	   var data = datatable.row(this).data();
	 	   var dcId = data.dcId;
	 	   window.location = pageContext+"/api/dc/view?dcId="+dcId;
	 	});

	


}