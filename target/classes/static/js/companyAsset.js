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

	},
	{
		"mData" : "dcNumber",
		"defaultContent": ""
	}

	]
});

$(document).on("click","#addCompanyAssetBtn",function(){
	$("#companyAssetForm")[0].reset();
	$("#modelDropdown").val("");
	$("#dcNumberHidden").val("");
	loadCompanyAssetCandidates();
	getEmployeeList();
	$( "#returnDate" ).datepicker({ dateFormat: 'dd-mm-yy' });
	$( "#dateAndTimeStamp" ).datepicker({ dateFormat: 'dd-mm-yy' });
	$("#companyAssetModal").modal('show');
});

function loadCompanyAssetCandidates(){
	$.ajax({
		type: 'GET',
		url: api.COMPANY_ASSET_CANDIDATES,
		dataType: 'json',
		success: function(response){
			$("#modelDropdown option:not(:first)").remove();
			$.each(response, function(index, value){
				$("#modelDropdown").append("<option value='"+value.soModelNo+"' data-dc-id='"+value.dcId+"'>"
					+ value.modelName + " (DC-" + value.dcId + ")</option>");
			});
		},
		error: function(e){
			console.log(e);
		}
	});
}

$(document).on("change","#modelDropdown",function(){
	var dcId = $(this).find(':selected').data('dc-id');
	$("#dcNumberHidden").val(dcId != null ? dcId : "");
});

function getEmployeeList(){
	$.ajax({
	    Type:'GET',
	    url : api.EMPLOYEE_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
			$("#custodianDropdown option:not(:first)").remove();
			$.each(response, function(index, value){
				$("#custodianDropdown").append("<option value='"+value.id+"'>"+value.name+"</option>");
			});
	    },
		error : function(e) {
			console.log(e);
		}
	});
}

$(document).on("click","#saveCompanyAssetBtn",function(e){

	var returnDate=$("#returnDate").val();
	var returnDateFormat=returnDate.split("-");
	returnDate= returnDateFormat[1]+"/"+returnDateFormat[0]+"/"+returnDateFormat[2];
	returnDate=new Date(returnDate);
	returnDate=returnDate.toLocaleDateString();
	$("#returnDateHidden").val(returnDate);

	var dateAndTimeStamp=$("#dateAndTimeStamp").val();
	var dateAndTimeStampFormat=dateAndTimeStamp.split("-");
	dateAndTimeStamp= dateAndTimeStampFormat[1]+"/"+dateAndTimeStampFormat[0]+"/"+dateAndTimeStampFormat[2];
	dateAndTimeStamp=new Date(dateAndTimeStamp);
	dateAndTimeStamp=dateAndTimeStamp.toLocaleDateString();
	$("#dateHidden").val(dateAndTimeStamp);
	let isValid = true;

	$('.error').remove();

	$("#companyAssetForm").find('[required]').each(function() {
		let field = $(this);
		if ($.trim(field.val()) === '') {
			field.css('border', '2px solid red');
			field.after('<span class="error" style="color:red;font-size:12px;">This field is required</span>');
			isValid = false;
		} else {
			field.css('border', '');
		}
	});

	if (!isValid) {
		e.preventDefault();
	}else{
		$('#companyAssetForm').submit();
	}
});

} );
