$(document).ready(function () {
	 $(document).on("click", "#addRow" , function() {
		  var newRow = $("<tr>");
		 var rowCount=$('table > tbody  > tr').length;
        var columns = "";
	    rowCount++;
	    columns += '<td  style="width: 23%;"><input class="form-control positionOfTextBox" type="text" style="width: 100%;" /></td>';
	    columns += '<td style="width: 15%;"><select class="form-control positionOfTextBox" type="text" style="width: 100%;padding: 0px;" ><option value="Not Selected">Not Selected</option></select></td>';
	    columns += '<td style="width: 15%;"><input class="form-control positionOfTextBox" type="text" style="width: 100%;"  /></td>';
	    columns += '<td  style="width: 3%;" ><input class="form-control positionOfTextBox" type="checkbox" style="width: 100%;" /></td>';
	    columns += '<td style="width: 12%;"><input class="form-control positionOfTextBox" type="text" style="width: 100%;"   /></td>';
	    columns += '<td style="width: 15%;"><input class="form-control positionOfTextBox" type="text" style="width: 100%;"    /></td>';
	    columns += '<td style="width: 15%;"><input class="form-control positionOfTextBox" type="text"  style="width: 100%;" /></td>';
	    
	    columns += '<td align="center" style="width: 2%;"><i class="deleteButton positionOfTextBox fa fa-trash"  aria-hidden="true" style="width: 100%;" ></i></td>';
        newRow.append(columns);
        $("#userMasterTable").append(newRow);
    });
	

	 $("#userMasterTable").on("click", ".deleteButton", function() {
   	 $(this).closest("tr").remove();
   	 $('tbody').find('tr').each(function (index) {
            var firstTDDomEl = $(this).find('td')[0];
            //Creating jQuery object
            var $firstTDJQObject = $(firstTDDomEl);
        });
      });
	 

});


$(document).on("change", "#photoIdDropDown", function(e) {

	var photoId = $(this).val();
	getPhotoIdById(photoId);
});


// Getting city object using ajax to set state country and country code
function getPhotoIdById(photoId) {

	$.ajax({
		method : 'GET',
		url : api.GETPHOTOIDBY_ID + photoId,
		success : function(photoIdObj) {
			

		}

	});

}
