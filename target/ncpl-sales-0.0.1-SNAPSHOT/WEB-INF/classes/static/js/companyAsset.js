$(document).ready( function () {
	$('#companyAssetList thead tr').clone(true).appendTo( '#companyAssetList thead' );
    $('#companyAssetList thead tr:eq(1) th').each( function (i) {
        var title = $(this).text();
        $(this).html( '<input type="text" style="width:100%;" placeholder="Search '+title+'" />' );
        $( 'input', this ).on( 'keyup change', function () {
            if ( table.column(i).search() !== this.value ) {
                table
                    .column(i)
                    .search( this.value )
                    .draw();
            }
        } );
    } );
table= $('#companyAssetList').DataTable({
	orderCellsTop: true,
    fixedHeader: true,
	"aaData": companyAssetList,
	"aoColumns": [ {
		"mData" : "modelName",
	},{
		"mData" : "slNo",

	}, {
		"mData" : "employee",

	}, {
		"mData" : "features",
	},{
		"mData" : "brand",
	},{
		"mData" : "site",
	},
	{
		"mData" : "returnDate",
		
	},
	{
		"mData" : "warranty",
		
	},
	{
		"mData" : "value",
		
	},
	{
		"mData" : "date",
		
	}
	
	
	]
});

} );
