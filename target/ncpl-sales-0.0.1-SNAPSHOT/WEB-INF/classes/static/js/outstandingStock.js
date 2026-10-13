$(document).ready(function () {
	loadItemTable();
});
var datatable = null;
function loadItemTable() {

	datatable = $('#oustandingStockList').DataTable({

		"data": outstandingStockList,
		/* lengthChange: false,*/
		"destroy": true,
		"order": [[ 4, "desc" ]],
		"columnDefs": [
			{ "width": "85px", "targets": 0 }
		],
		 dom: 'Bfrtip',
    	 buttons: [{
    		 text : 'Download',
 			extend: 'excel',
 			filename: 'Pending SO List',
 			title: 'Pending SO List'
 		}
 		],
		"columns": [

			{
				"title": "Model No",
				"data": "model",
				"defaultContent": "",
				"width":"17%"
				
				

			},
			{
				"title": "Description",
				"data": "itemName",
				"defaultContent": "",
				"width":"22%",
				"class": "text-field-large-nowrap",
				render : function(data, type, row) {
					return  data.length > 35 ?
							data.substr( 0, 35 ) +'...' :
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
				"width":"7%"
			},
			{
				"data": "grnDate",
				"class":"hideTd",
				render : function(data, type, row) {
					var newdate = moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss") ;
						return  newdate; 
				}

			},

			{
				"title": "Date",
				"data": "grnDate",
				"defaultContent": "",
				"width":"13%"

			},{
				"title": "Qty",
				"data": "grnQty",
				"defaultContent": "",
				"width":"7%",
				render : function(data, type, row) {
					var newdata = Math.round(data * 100) / 100
						return  newdata; 
				}

			}/*,{
				"title": "clientpoName",
				"data": "clientPo",
				"defaultContent": "",
				"width":"15%"
				

			},*/
			,{
				"title": "Price",
				"data": "price",
				"defaultContent": "",
				"width":"7%"
				
			}
			,{
				"title": "Amount",
				"data": "amount",
				"defaultContent": "",
				"width":"7%",
				render : function(data, type, row) {
					var amount;
					if(row.price!=""){
						amount=row.grnQty*row.price;
						amount=Math.round(amount * 100) / 100
					}else{
						amount="";
					}
					return amount;

				}
				
			}
			
			

		]

	})/*.buttons().container().appendTo( $('#itemMasterList_length') )*/;

	


}