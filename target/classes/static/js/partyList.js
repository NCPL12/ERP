/**
 * 
 */
var partyTable;

$(document).ready( function () {
	console.log(data[173]);
	partyTable=$('#partyList').DataTable({
    	"aaData": data,
    	//"autoWidth": true,
    	//"pageLength": 15,
    	"aoColumns": [
    	{ "title":"Type",
			"data": "type",
			"width":"10%",
			render: function ( data, type, row ) {
    		    return row.party_type.name;
    		}
		},

    	{   "title":"Party Name",
    		'data':"partyName",
    		"width": "24%",
    		"class": "text-field-large-nowrap",
    		render: function ( data, type, row ) {
    		    return data.length > 25 ?
    		        data.substr( 0, 25 ) +'...' :
    		        data;
    		}
    	},
    	{   "title":"City",
			"data" : "city",
			"width":"10%",
           "defaultContent":"NA",
			render : function(aaData, type, row) {
				var city=row.party_city.name;
				return city.length > 10 ?
				city.substr( 0, 10 ) +'...' :
				city;
			}
    	},
		{ "title":"State",
		   "width":"10%",
			"data": "state",
    	"defaultContent":"NA",
 			render : function(aaData, type, row) {
 				var state=row.party_city.state.name
 				return state.length > 10 ?
				 state.substr( 0, 10 ) +'...' :
				 state;
 			}
    	},
		{   "title":"Country",
		    "width":"10%",
			"data": "country",
    	
    		 "defaultContent":"NA",
  			render : function(aaData, type, row) {
  				var country=row.party_city.state.country.name;
  				return country.length > 10 ?
				country.substr( 0, 10 ) +'...' :
				country;
  			}
    	
    	},
    	
		{ "title":"Phone",
		   "width":"10%",
			"data": "phone1"},
    	{ 
			"title":"Website",
			"width":"18%",
			"data": "website",
			"class":"WebWidth",
    		"defaultContent":"NA",
  			render : function(aaData, type, row) {
  				if(aaData == null || aaData== undefined)
  					{aaData="";}
				  return aaData.length > 15 ? '<a href='+aaData+' target="_blank">'+ aaData.substr( 0, 15 )+'...' +'</a>' :'<a href='+aaData+' target="_blank">'+aaData+'</a>';
  			}
    			
    	},
		{   "title":"Category",
		    "width":"10%",
			"data": "category",
        	  "defaultContent":"NA",
    			render : function(aaData, type, row) {
    				var categoryName = row.category;
    				  
                    return categoryName;
    			}
        	},{
    			"data": "remarks",
            	  "defaultContent":"NA",
            	  "class":"hideTd",
        			render : function(aaData, type, row) {
        				var remarks = row.remarks;
        				  
                        return remarks;
        			}
            	},
    	
    	]
    });
	
	//redirect to the party page on double click of row.
    $('#partyList tbody').on('dblclick', 'tr', function () {
    	var data1 = partyTable.row(this).data();
    	var partyId = data1.id;
    	window.location = pageContext+"/api/party/view?partyId="+partyId;
 	});
    
} );

