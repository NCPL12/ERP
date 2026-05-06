/**
 * 
 */
var returnableTable;

$(document).ready( function () {
	console.log(data[173]);
	returnableTable=$('#returnableItemsList').DataTable({
    	"aaData": data,
    	"aoColumns": [
    	{ "title":"DC No.",
			"data": "dcNo",
			"width":"10%"
		},

    	{   "title":"Description",
    		'data':"description",
    		"width": "24%",
    		"class": "text-field-large-nowrap",
    		render: function ( data, type, row ) {
    		    return data.length > 25 ?
    		        data.substr( 0, 25 ) +'...' :
    		        data;
    		}
    	},
    	{   "title":"Unit",
			"data" : "unit",
			"width":"10%",
           "defaultContent":"NA"
    	},
		{ "title":"Total Qty",
		   "width":"10%",
			"data": "totalQty",
    	"defaultContent":"NA"
    	},
		{   "title":"Delivered Qty",
		    "width":"10%",
			"data": "deliveredQty",
    	
    		 "defaultContent":"NA"
    	
    	},
    	
		{ "title":"Returned QTy",
		   "width":"10%",
			"data": "returnedQty"
		}
    	
    	]
    });
	
    
} );