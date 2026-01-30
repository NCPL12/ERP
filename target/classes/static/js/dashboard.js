var table;
$(document).ready( function () {
	
	if(role=="STORE"){
		 $(".addSO").on("click", function (event) {
			    event.preventDefault();
			});
			
	}
	uploadFileFormValidation();
	    // Setup - add a text input to each footer cell
		$('#salesList thead tr').clone(true).appendTo( '#salesList thead' );
	    $('#salesList thead tr:eq(1) th').each( function (i) {
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
    table= $('#salesList').DataTable({
    	orderCellsTop: true,
	    fixedHeader: true,
    	"aaData": dataObj,
    	"order": [[ 5, "desc" ]],
    	'columnDefs': [ {
    	    'targets': [0,1,2,3,4,5,6,7], 
    	    /* 'targets': [0,1,2,3,4,5,6,7],  table column index */
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
		}, {
			"mData" : "grandTotal",
			"class":"alignright",
			render : function(aaData, type, row) {
				var total = commaSeparateNumber( Math.round(row.grandTotal * 100) / 100);
				return total;
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
			
		},
		/*{
			"mData" : "clientPoNumber",
			render : function(datam, type, row) {
				return "<button type='button' class='btn  btn-primary btn-flat btn-xs statusButton'>Close</button>";				
			}
		},*/
		{
			"mData" : "edit"	,
			render : function(datam, type, row) {
				var url;
				url = pageContext+"/api/salesOrder_tds/view?salesOrderId="+row.id;
				return "<a class='text-info ' href='" + url + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-edit'></i></button></a>";			
			}
		},
		{
			"mData" : "pdf"	,
			render : function(datam, type, row) {
				var url;
				url ="/ncpl-sales/sales/downlaod/"+row.id;
				return "<a class='text-info ' href='" + url + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i> Download</button></a>";				
			}
		},
		{
			"mData" : "pdf"	,
			render : function(datam, type, row) {
				var url;
				url ="/ncpl-sales/material/tracker/"+row.id;
				return "<a class='text-info ' href='" + url + "'><button type='button'  class='btn btn-default btn-flat btn-xs' ><i class='fa fa-fw fa-download'></i> Download</button></a>";				
			}
		},
		{
			"mData" : "pdf"	,
			render : function(datam, type, row) {
				//var url;
				//url ="/ncpl-sales/api/clientPo/upload/"+row.id;
				return "<button type='button'  class='btn btn-default btn-flat btn-xs clientPoUploadBtn' ><i class='fa fa-fw fa-upload'></i> Upload</button>";				
			}
		},
		{
			"mData" : "pdf"	,
			render : function(datam, type, row) {
				//var url;
				//url ="/ncpl-sales/clientPo/download/"+row.id;
				return "<button type='button'  class='btn btn-default btn-flat btn-xs clientPoDownloadBtn' ><i class='fa fa-fw fa-download'></i> Download</button>";				
			}
		},
		{
			"mData" : "pdf"	,
			render : function(datam, type, row) {
				//var url;
				//url ="/ncpl-sales/api/clientPo/upload/"+row.id;
				return "<button type='button'  class='btn btn-default btn-flat btn-xs designUploadBtn' ><i class='fa fa-fw fa-upload'></i> Upload</button>";				
			}
		},
		{
			"mData" : "archive"	,
			render : function(datam, type, row) {
				var archive;
				if(role=="SUPER ADMIN"){
					if(row.archive==true){
						return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' checked='checked'/>";				
					}else{
						return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' />";				
					}
				}else{
					if(row.archive==true){
						return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' checked='checked' disabled='disabled'/>";				
					}else{
						return "<input style='width: 20%;margin-left:auto;margin-right:auto' type='checkbox' value='"+archive+"' name='archiveCheckbox' id='archiveCheckbox' class='form-control form-control-sm archiveCheckbox' disabled='disabled'/>";				
					}
				}
			}
		}
		
		
		
		]
    });
   
    //On double click of row navigate to edit page
   $('#salesList tbody').on('dblclick', 'tr', function () {
	   var data = table.row(this).data();
	   var salesOrderId = data.id;
	   window.location = pageContext+"/api/sales_order/view?salesOrderId="+salesOrderId;
	});
} );


 
$(document).on('click',".statusButton",function(){
	var clientPoNumber=$(this).closest("tr").find("td:eq(1)").text();
	$("#statusUpdateForm")[0].reset();
	$("#statusModal").modal('show');
	$("#clientPoNum").val(clientPoNumber);
	$("#actualClosureDate,#dlp").datepicker({
		dateFormat: 'dd-mm-yy'
		
	});
});

$(document).on('click',".archiveCheckbox",function(){
	var soNum=$(this).closest("tr").find("td:eq(0)").text();
	var archiveinput= $(this).closest("tr").find("td").eq(10).find('input');
	const isChecked = $(this).is(":checked");
		if(isChecked==true) {
		bootbox.confirm({
			message: "Do you want to archive this SO?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? archiveSalesOrder(soNum):window.location.reload();;
			}
		});	
	}else{
		bootbox.confirm({
			message: "Do you want to remove this SO from archive?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? unArchiveSalesOrder(soNum):window.location.reload();;
			}
		});	
	}
});



function archiveSalesOrder(soId){
	//window.location.href = pageContext+"/api/update_so_archive?soId="+soId;
	//window.location=pageContext+"/salesList";
	$.ajax({
		type : "POST",  
		url : api.UPDATE_SO_ARCHIVE +"?soId="+soId,
		success : function(response) {
				window.location.reload();
			
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  
	});
}

function unArchiveSalesOrder(soId){
	$.ajax({
		type : "POST",  
		url : api.UPDATE_SO_UNARCHIVE +"?soId="+soId,
		success : function(response) {
				window.location.reload();
			
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  
	});
}

/*$(document).on("click","#submitCloseProjectBtn",function (event) {

    //stop submit the form, we will post it manually.
    event.preventDefault();*/
function uploadFileFormValidation(){
    $('#statusUpdateForm').validate({
	       rules: {
	       },
	       submitHandler: function(form) {
	    	   var formData = new FormData(form);    
	    	   uploadFile(formData);
	    	  
	    	   
	       }
	   });
}

//})

function uploadFile(formData) {
	/*var form = $('#statusUpdateForm')[0];
	var data = new FormData(form); */
	
	//data.append('upload_file', $('input[type=file]')[0].files[0]);
	var closureDate = $("#actualClosureDate").val();
	var dlp = $("#dlp").val();

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: api.UPLOAD,
        data: data,
        processData: false, //prevent jQuery from automatically transforming the data into a query string
        contentType: false,
        cache: false,
        success: function (response) {

            console.log("SUCCESS : ",response);

        }
    });

}

$(document).on('click',".clientPoUploadBtn",function(){
	var salesOrderId=$(this).closest("tr").find("td:eq(0)").text();
	$("#clientPoUploadForm")[0].reset();
	$("#clientPoUploadModal").modal('show');
	$("#salesOrderNum").val(salesOrderId);
});

if(user=="admin" || user=="ashwini"){
	$(document).on('click',".soUploadButton",function(){
		$("#uploadForm")[0].reset();
		$('#response').empty();
		$("#soUploadModal").modal('show');
	});
	
	 $(document).on('click',".designUploadBtn",function(){
		 var clientPoNum=$(this).closest("tr").find("td:eq(1)").text();
			$("#designuploadForm")[0].reset();
			$('#designresponse').empty();
			$("#designUploadModal").modal('show');
			$("#clientPo").val(clientPoNum);
		});
	
}else{
	
	 $(".soUploadButton").on("click", function (event) {
		    event.preventDefault();
		});
	 
	
		
		 $(".designUploadBtn").on("click", function (event) {
			    event.preventDefault();
			});
		
}



$(document).on('submit', '#clientPoUploadForm', function(event) {
    event.preventDefault(); // Prevent the form from submitting via the browser

    var fileInput = $('#fileInput')[0];
    var file = fileInput.files[0];

    if (!file) {
        alert("Please select a PDF file to upload.");
        return;
    }

    // Ensure the selected file is a PDF
    if (file.type !== 'application/pdf') {
        alert("Only PDF files are allowed.");
        return;
    }
    var salesOrderId=$("#salesOrderNum").val();
    var formData = new FormData();
    formData.append("file", file);

    $.ajax({
        url: api.FILE_UPLOAD+"/"+salesOrderId,  // URL to your Spring Boot endpoint
        type: 'POST',
        data: formData,
        processData: false, // Don't process the files
        contentType: false, // Don't set any content type header
        success: function (response) {
        	$.success("File uploaded successfully");
        },
        /*error: function (jqXHR, textStatus, errorThrown) {
            $('#responseMessage').text("Failed to upload the PDF file.");
        }*/
		complete:function(response){
			if(response.status==500){
				  $.error("Failed to upload the PDF file.");
			}
		},
		error : function(e) {
			console.log(e);
		}
    });
});

$(document).on('click', '.clientPoDownloadBtn', function(event) {
    event.preventDefault();
    
    var salesOrderId = $(this).closest("tr").find("td:eq(0)").text();

    if (!salesOrderId) {
        alert("Please enter a file ID.");
        return;
    }

    $.ajax({
        url: api.FILE_DOWNLOAD +"/"+ salesOrderId,  // URL to the Spring Boot download endpoint
        type: 'GET',
        xhrFields: {
            responseType: 'blob'  // Important for binary data
        },
        success: function (blob) {
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(blob);
            link.download = 'file_' + salesOrderId + '.pdf';  // Suggest file name with ID
            link.click();
            $('#downloadResponseMessage').text("Download started for file ID: " + salesOrderId);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $('#downloadResponseMessage').text("Failed to download the PDF file.");
        }
    });
});
function uploadClientPOFile(formData,salesOrderId) {
	

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: api.FILE_UPLOAD,
        data: formData,
        processData: false, //prevent jQuery from automatically transforming the data into a query string
        contentType: false,
        cache: false,
        success: function (response) {

            console.log("SUCCESS : ",response);

        }
    });

}

$(document).on('submit',"#uploadForm", function (e) {
    e.preventDefault(); // Prevent form submission

    let formData = new FormData();
    formData.append('file', $('#file')[0].files[0]);

    $.ajax({
        url: api.SALES_UPLOAD,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
        	$('#response').empty();
            $("#response").html("<p style='color: green;'>" + response + "</p>");
            window.location.reload();
        },
        error: function(xhr) {
            var errorMessages = xhr.responseJSON;
            var errorHtml = "<p style='color: red;'>Errors:</p><ul>";
            errorMessages.forEach(error => {
                errorHtml += "<li>" + error + "</li>";
            });
            errorHtml += "</ul>";
            $("#response").html(errorHtml);
        }
    });
});

$(document).on('submit',"#designuploadForm", function (e) {
    e.preventDefault(); // Prevent form submission
    let formData = new FormData();
    formData.append('file', $('#excelfile')[0].files[0]);
    formData.append('clientPONum', $("#clientPo").val());
    $.ajax({
        url: api.DESIGN_UPLOAD,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
        	$('#designresponse').empty();
            $("#designresponse").html("<p style='color: green;'>" + response + "</p>");
            window.location.reload();
        },
        error: function(xhr) {
            var errorMessages = xhr.responseJSON;
            var errorHtml = "<p style='color: red;'>Errors:</p><ul>";
            errorMessages.forEach(error => {
                errorHtml += "<li>" + error + "</li>";
            });
            errorHtml += "</ul>";
            $("#designresponse").html(errorHtml);
        }
    });
});


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
