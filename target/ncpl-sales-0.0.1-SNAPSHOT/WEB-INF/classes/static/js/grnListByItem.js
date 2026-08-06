$(document).ready(function () {
	loadGrnTable();
});
var grnDataTable = null;
function loadGrnTable() {

	grnDataTable= $('#grnList').DataTable({
    	orderCellsTop: true,
	    fixedHeader: true,
	    order:[[ 2, "desc" ]],
	    'columnDefs': [ {
    	    'targets': [0,1,2,3,4,5], /* table column index */
    	    'orderable': false, /* here set the true or false */
    	 }],
    	"data": grnList,
    	"columns": [ {
    		"title":"Grn No.",
			"data" : "grnId",
			"defaultContent":"",
			render : function(data, type, row) {
				  var grnId = row.grnId;
			 	   url = pageContext+"/api/grn/view?grnId="+grnId;
			 	  return "<a href='"+url+"'>"+grnId+"</a>";
			}
		}, {
			"title":"PO Number",
			"data" : "poNumber",
			"defaultContent":"",

		}, {
			"data" : "created",
			"defaultContent":"",
			"class":"hideTd",
			render : function(data, type, row) {
				var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
					return  newdate; 
			}

		}, {
			"title":"PO Date",
			"data" : "poDate",
			"defaultContent":"",
			render : function(data, type, row) {
				var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
					return  newdate; 
			}
		},
		{
			"title":"GRN Date",
			"data" : "created",
			"defaultContent":"",
			render : function(data, type, row) {
				var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
					return  newdate; 
			}
		},{
			"title":"Vendor",
			"data" : "vendor",
			"defaultContent":"",
			
			"class": "text-field-large-nowrap",
    		render: function ( data, type, row ) {
    		    return data.length > 35 ?
    		    		data.substr( 0, 35 ) +'...' :
    		    			data;
    		}

		}
		]
    });
	


}