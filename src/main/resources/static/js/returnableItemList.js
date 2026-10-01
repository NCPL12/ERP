/**
 *
 */
// @D0014 lazy-loaded, paginated Returnables list (see README.md)
var returnableTable;

$(document).ready( function () {
	returnableTable = $('#returnableItemsList').DataTable({
		processing: true,
		serverSide: true,
		pageLength: 50,
		lengthMenu: [[50, 100, 250, 500], [50, 100, 250, 500]],
		"order": [],
		ajax: function (data, callback, settings) {
			var page = Math.floor(data.start / data.length);
			$.ajax({
				url: pageContext + "/returnableList/data",
				data: {
					page: page,
					size: data.length,
					keyword: data.search && data.search.value ? data.search.value : ""
				},
				success: function (resp) {
					callback({
						draw: data.draw,
						recordsTotal: resp.totalElements,
						recordsFiltered: resp.totalElements,
						data: resp.content
					});
				},
				error: function (xhr) {
					console.error("Failed to load returnable items list", xhr.status, xhr.responseText);
					callback({ draw: data.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
				}
			});
		},
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
