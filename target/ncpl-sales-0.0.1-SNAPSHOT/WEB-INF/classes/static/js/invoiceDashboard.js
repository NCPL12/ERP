var invoiceTable;
$(document).ready(function(){
	$('#invoiceList thead tr').clone(true).appendTo( '#invoiceList thead' );
	    $('#invoiceList thead tr:eq(1) th').each( function (i) {
	        var title = $(this).text();
	        $(this).html( '<input type="text" style="width:100%;" placeholder="Search '+title+'" />' );
	        $( 'input', this ).on( 'keyup change', function () {
	            if ( invoiceTable.column(i).search() !== this.value ) {
	            	invoiceTable
	                    .column(i)
	                    .search( this.value )
	                    .draw();
	            }
	        } );
	    } );
	    invoiceTable= $('#invoiceList').DataTable({
	    	orderCellsTop: true,
		    fixedHeader: true,
	    	"data": invoiceList,
	    	  
	    	"order": [[ 1, "desc" ]],
	    	'columnDefs': [ {
	    	    'targets': [0,1,2,3,4,5,6,7], /* table column index */
	    	    'orderable': false, /* here set the true or false */
	    	 }],
	    	
	    	"columns": [ {
				"data" : "invoiceId",
				"defaultContent":"",
			
			}, 
			{
				"data" : "invoiceId",
				"defaultContent":"",
				"class":"hideTd",
				render : function(data,type, row) {
					var date=data.split("-");
					var formattedDate = date.reverse()[1]+date[0];
				
						return  formattedDate; 
				}

			},
			
			{
				"data" : "soNumber",
				"defaultContent":"",

			},
			{
				"data" : "type",
				"defaultContent":"",

			},{
				"data" : "dcNumber",
				"defaultContent":"",

			},{
				"data" : "clientPoNumber",
				"defaultContent":"",

			},{
				"data" : "grandTotal",
				"defaultContent":"",
				render: function ( data, type, row ) {
					var grandTotal=commaSeparateNumber(data);
	    		    return grandTotal;
	    		}

			},
			{
				"mData" : "pdf"	,
				render : function(datam, type, row) {
					var url;
					url ="/ncpl-sales/invoice/details/"+row.invoiceId;
					pdfUrl="/ncpl-sales/invoice/pdf/details/"+row.invoiceId;
					//var res = isInvoiceGenerated(row.invoiceId);
					if(row.invCopyCreated == true){
					return "<a class='text-info ' href='" + url + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i> Download Invoice</button></a><a class='text-info pull-right' href='" + pdfUrl + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i>PDF</button></a>";				
				}else{
					return "<a class='text-info ' href='" + url + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i> Generate Invoice</button></a><a class='text-info pull-right' href='" + pdfUrl + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i>PDF</button></a>";	
				}
					}
			}
			]
	    });
	    
	    //on double click of list navigate to view page
	    $('#invoiceList tbody').on('dblclick', 'tr', function () {
	 	   var data = invoiceTable.row(this).data();
	 	   var invoiceId = data.invoiceId;
	 	   window.location = pageContext+"/api/invoice/view?invoiceId="+invoiceId;
	 	});
})

function commaSeparateNumber(val) {
	var x = val;
	//x = x.replace(",","");
	x = x.toString();
	x = x.replace(/,/g, "");
	var afterPoint = '';
	if (x.indexOf('.') > 0)
		afterPoint = x.substring(x.indexOf('.'), x.length);
	x = Math.floor(x);
	x = x.toString();
	var lastThree = x.substring(x.length - 3);
	var otherNumbers = x.substring(0, x.length - 3);
	if (otherNumbers != '')
		lastThree = ',' + lastThree;
	var res = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ",") + lastThree + afterPoint;
	return res;


}

/*function isInvoiceGenerated(invoiceNo) {
	var result;
	 $.ajax({
	       url:api.ISINVOICE_GENERATED  + "?invId=" + invoiceNo,
	       type:'GET',
	       dataType:'json',
		    async: 'false',
	       success: function(response) {
	    	   result=response;
	    	
	       }
	
	 });
	 return result;
}
*/

