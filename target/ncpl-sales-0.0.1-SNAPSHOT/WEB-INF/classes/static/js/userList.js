$(document).ready( function () {
	
    $('#userList').DataTable({
    	"aaData": data,
    	 "autoWidth": false,
    	"aoColumns": [
    	{ "data": "type"},
    	{'data':"Name"},
    	{ 
    		"data" : "photoId",
           "defaultContent":"NA",
			render : function(aaData, type, row) {
				
				return row.user_photoId.name;
			}
    	},
    	
    	{ "data": "mobile"},
    	{ "data": "temporaryAddress"},
    	{ "data": "permanentAddress",
        	  
        	}
    	
    	]
    });
    
   
} );

