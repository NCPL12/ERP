$(document).ready(function () {
	loadPoTable();
	$("#accountedQty").text(commaSeparateNumber(poList.accountedQty));
});
var datatable = null;
function loadPoTable() {

	datatable= $('#purchaseList').DataTable({
    	//"iDisplayLength": -1,
    	'columnDefs': [ {
    	    'targets': [0,1,2,3,4,5,6], /* table column index */
    	    'orderable': false, /* here set the true or false */
    	 }],
    	    "aaSorting": [[ 4, "desc" ]],
    	orderCellsTop: true,
	    fixedHeader: true,
	    
    	"aaData": poList.poList,
    	
    	"aoColumns": [ {
    		"title":"PO NO",
			"mData" : "poNumber",
			render : function(aaData, type, row) {
				var poNumber=row.poNumber;
				var version=row.version;
				var versionIndex=version;
				var url=pageContext+"/purchase/view?poNumber="+poNumber+"&version="+version+"&versionIndex="+versionIndex;
				return "<a href='"+url+"'>"+poNumber+"</a>";
				
			}
		}, {
			"title":"Vendor",
			"mData" : "party",

			render : function(aaData, type, row) {
				var company;
				if (aaData == null) {
					company = "";
				} else {
					company = aaData.partyName;
				
				}
				return company.length > 35 ?
				company.substr( 0, 35 ) +'...' :
				company;

				//return row.salesOrder.party.partyName;

			}

		}, {
			"title":"City",
			"mData" : "party",

			 "defaultContent":"NA",
				render : function(aaData, type, row) {
					//return "";
					return row.party.party_city.name;
					
				}
		}, {
			"title":"GrandTotal",
			"mData" : "grandTotal",
			"defaultContent":"NA"
		},{
			
			"mData" : "created",
			"class":"hideTd",
			render : function(data, type, row) {
				var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
					return  newdate; 
			}
		},
		{
			"title":"Date",
			"mData" : "created",
			render : function(data, type, row) {
				var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
					return  newdate; 
			}
			
		},{
			
			"mData" : "version",
			"visible": false,
		}
		]
	})
	


}
function commaSeparateNumber(val){
	 var x=val;
	 //x = x.replace(",","");
	 x=x.toString();
	 x = x.replace(/,/g,"");
	 var afterPoint = '';
	 if(x.indexOf('.') > 0)
	    afterPoint = x.substring(x.indexOf('.'),x.length);
	 x = Math.floor(x);
	 x=x.toString();
	 var lastThree = x.substring(x.length-3);
	 var otherNumbers = x.substring(0,x.length-3);
	 if(otherNumbers != '')
	     lastThree = ',' + lastThree;
	 var res = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + lastThree + afterPoint;
	 return res;

	 }
