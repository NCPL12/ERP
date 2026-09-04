/**
 * 
 */
var workOrderTable;
$(document).ready(function(){
	$('#workOrderList thead tr').clone(true).appendTo( '#workOrderList thead' );
	    $('#workOrderList thead tr:eq(1) th').each( function (i) {
	        var title = $(this).text();
	        $(this).html( '<input type="text" style="width:100%;" placeholder="Search '+title+'" />' );
	        $( 'input', this ).on( 'keyup change', function () {
	            if ( workOrderTable.column(i).search() !== this.value ) {
	            	workOrderTable
	                    .column(i)
	                    .search( this.value )
	                    .draw();
	            }
	        } );
	    } );
	    workOrderTable= $('#workOrderList').DataTable({
	    	orderCellsTop: true,
		    fixedHeader: true,
	    	"aaData": data,
	    	"order": [[ 5, "desc" ]],
	    	'columnDefs': [ {
	    	    'targets': [0,1,2,3,4,5], 
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	"aoColumns": [ {
	    		"class":"hideTd",
				"mData" : "id",
			},{
				"mData" : "clientPoNumber",
			},{
				"mData" : "party",

				render : function(aaData, type, row) {
					var company;
					if (aaData == null) {
						company = "";
					} else {
						company = aaData.partyName;
					
					}
					return  company.length > 35 ?
					company.substr( 0, 35 ) +'...' :
					company;

				}

			}, {
				"mData" : "party",

				 "defaultContent":"NA",
					render : function(aaData, type, row) {
						
						return row.party.party_city.name;
					}
			},{
				"mData" : "created",
				"class":"hideTd",
				render : function(datam, type, row) {
					var date=datam.split("-");
					var formattedDate = date[1]+"-"+date[0]+"-"+date[2];
					var newdate = moment(new Date(formattedDate)).format("YYYY-MM-DD HH:mm:ss") ;
						return  newdate; 
				}
			},
			{
				"mData" : "created",
				
			}/*,
			{
				"mData" : "pdf"	,
				render : function(datam, type, row) {
					var url;
					//url ="/ncpl-sales/sales/details/"+row.id;
					return "<a class='text-info ' href='" + url + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i> Download</button></a>";				
				}
			}*/
			
			]
	    });
	   
	    //On double click of row navigate to edit page
	   $('#workOrderList tbody').on('dblclick', 'tr', function () {
		   var data = workOrderTable.row(this).data();
		   var workOrderId = data.id;
		   window.location = pageContext+"/api/work_order/view?workOrderId="+workOrderId;
		});
	 
})