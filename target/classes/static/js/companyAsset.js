var employeeListCache = null;

/** Displays yyyy-MM-dd (the server's JSON date format) as dd-mm-yyyy. Leaves everything else untouched. */
function toDDMMYYYY(value){
	if(!value){
		return value;
	}
	var parts = String(value).split("-");
	if(parts.length !== 3){
		return value;
	}
	return parts[2] + "-" + parts[1] + "-" + parts[0];
}

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
		"mData" : "slNo",
	},{
		"mData" : "modelName",
	}, {
		"mData" : "features",
		"defaultContent": ""
	}, {
		"mData" : "assetType",
		"defaultContent": ""
	}, {
		"mData" : "site",
	},
	{
		"mData" : "created",
		"render": function(value, type){ return type === "display" ? toDDMMYYYY(value) : value; }
	}
	]
});

/** Put "Add Company Asset" on the same line as DataTables' built-in Search box. */
var addBtnDiv = $(".button-div-style").detach();
addBtnDiv.css({ "padding-bottom": 0, margin: 0 });
$("#companyAssetList_wrapper .dataTables_filter")
	.css({ display: "flex", "align-items": "center", "justify-content": "flex-end", gap: "16px" })
	.prepend(addBtnDiv);

$(document).on("click","#addCompanyAssetBtn",function(){
	$("#companyAssetForm")[0].reset();
	$("#companyAssetId").val("0");
	$("#assignmentRowsBody").empty();
	refreshAssignmentEmptyState();
	$("#modelId").text("Company Asset Details");
	loadCompanyAssetCandidates();
	loadAssetTypeList(function(){});
	withEmployeeList(function(){});
	$("#companyAssetModal").modal('show');
});

$(document).on("click",".editCompanyAssetBtn",function(e){
	e.stopPropagation();
	openCompanyAssetEdit($(this).data("id"));
});

$(document).on("click",".deleteCompanyAssetBtn",function(e){
	e.stopPropagation();
	var id = $(this).data("id");
	if(!confirm("Delete this asset and its full assignment history? This cannot be undone.")){
		return;
	}
	$.ajax({
		type: 'DELETE',
		url: api.DELETE_COMPANY_ASSET + id,
		success: function(){
			location.reload();
		},
		error: function(xhr){
			console.log(xhr);
			alert("Unable to delete asset.");
		}
	});
});

/** Double-click any row in the Assets table to open the read-only Asset Details page. */
$(document).on("dblclick","#companyAssetList tbody tr",function(){
	var rowData = table.row(this).data();
	if(rowData && rowData.id != null){
		window.location.href = contextRoot + "companyAssets/view/" + rowData.id;
	}
});

function openCompanyAssetEdit(id){
	var asset = companyAssetList.find(function(a){ return a.id == id; });
	if(!asset){
		alert("Unable to find asset data. Please refresh and try again.");
		return;
	}
	$("#companyAssetForm")[0].reset();
	$("#companyAssetId").val(asset.id);
	$("#model").val(asset.model || "");
	$("#features").val(asset.features || "");
	$("#brand").val(asset.brand || "");
	$("#site").val(asset.site || "");
	$("#warranty").val(asset.warranty || "");
	loadAssetTypeList(function(){
		$("#assetType").val(asset.assetType || "");
		toggleLaptopDetails();
	});
	$("#assetId").val(asset.assetId || "");
	$("#serialNo").val(asset.serialNo || "");
	$("#dateOfPurchase").val(asset.dateOfPurchase || "");
	$("#windowsVersion").val(asset.windowsVersion || "");
	$("#modelId").text("Company Asset Details - Edit");

	loadCompanyAssetCandidates();
	$("#assignmentRowsBody").empty();
	withEmployeeList(function(){
		$.each(asset.assignments || [], function(i, a){
			addAssignmentRow(a);
		});
		refreshAssignmentEmptyState();
	});
	$("#companyAssetModal").modal('show');
}

var companyAssetCandidates = [];

function loadCompanyAssetCandidates(){
	$.ajax({
		type: 'GET',
		url: api.COMPANY_ASSET_CANDIDATES,
		dataType: 'json',
		success: function(response){
			companyAssetCandidates = response || [];
			$("#modelCandidates").empty();
			$.each(companyAssetCandidates, function(index, value){
				$("#modelCandidates").append("<option value='"+value.soModelNo+"'>"
					+ value.modelName + " (DC-" + value.dcId + ")</option>");
			});
		},
		error: function(e){
			console.log(e);
		}
	});
}

/** Auto-fills Description from the Item Master record matching the typed/picked Model No. */
$(document).on("input change", "#model", function(){
	var modelNo = $(this).val();
	var match = companyAssetCandidates.find(function(c){ return c.soModelNo == modelNo; });
	if(match && match.itemDescription){
		$("#features").val(match.itemDescription);
	}
});

function loadAssetTypeList(callback){
	$.ajax({
		type: 'GET',
		url: api.ASSET_TYPE_LIST,
		dataType: 'json',
		success: function(response){
			var options = "<option value='' data-id=''>Select Asset Type</option>";
			$.each(response || [], function(index, value){
				options += "<option value='"+value.name+"' data-id='"+value.id+"'>"+value.name+"</option>";
			});
			$("#assetType").html(options);
			toggleLaptopDetails();
			callback();
		},
		error: function(e){
			console.log(e);
			callback();
		}
	});
}

/** Laptop Details only applies to the Asset Type with id=1 (Laptop). */
function toggleLaptopDetails(){
	var selectedId = $("#assetType option:selected").data("id");
	$("#laptopDetailsSection").toggle(String(selectedId) === "1");
}

$(document).on("change", "#assetType", toggleLaptopDetails);

$(document).on("click", "#addAssetTypeBtn", function(){
	$("#assetTypeNameInput").val("");
	$("#assetTypeModal").modal('show');
});

$(document).on("click", "#saveAssetTypeBtn", function(){
	var name = $.trim($("#assetTypeNameInput").val());
	if(!name){
		alert("Please enter an asset type name.");
		return;
	}
	$.ajax({
		type: 'POST',
		url: api.ASSET_TYPE_ADD,
		data: { name: name },
		success: function(){
			loadAssetTypeList(function(){
				$("#assetType").val(name);
				toggleLaptopDetails();
			});
			$("#assetTypeModal").modal('hide');
		},
		error: function(xhr){
			alert(xhr.responseText || "Unable to save asset type.");
		}
	});
});

function withEmployeeList(callback){
	if(employeeListCache){
		callback(employeeListCache);
		return;
	}
	$.ajax({
		type: 'GET',
		url: api.EMPLOYEE_LIST,
		dataType: 'json',
		success: function(response){
			employeeListCache = response;
			callback(response);
		},
		error: function(e){
			console.log(e);
			employeeListCache = [];
			callback([]);
		}
	});
}

function employeeOptionsHtml(selectedId){
	var html = "<option value=''>Select Employee:</option>";
	$.each(employeeListCache || [], function(index, emp){
		var selected = (selectedId != null && String(selectedId) === String(emp.id)) ? " selected" : "";
		html += "<option value='"+emp.id+"'"+selected+">"+emp.name+" ("+emp.empId+")</option>";
	});
	return html;
}

/** Adds one assignment row, optionally pre-filled from an existing LaptopAssignment (edit mode). */
function addAssignmentRow(existing){
	var id = existing ? existing.id : 0;
	var employeeId = existing && existing.employee ? existing.employee.id : "";
	var location = existing ? (existing.location || "") : "";
	var issuedBy = existing ? (existing.issuedBy || "") : "";
	var dateIssued = existing ? (existing.dateIssued || "") : "";
	var dateReturned = existing ? (existing.dateReturned || "") : "";
	var returnedTo = existing ? (existing.returnedTo || "") : "";
	var remarks = existing ? (existing.remarks || "") : "";

	var row = $(
		"<tr class='assignmentRow'>" +
			"<td><input type='hidden' class='assignmentIdInput' value='"+id+"'/>" +
				"<select class='form-control form-control-sm assignEmployeeSelect' required>"+employeeOptionsHtml(employeeId)+"</select></td>" +
			"<td><input type='text' class='form-control form-control-sm assignLocationInput' value='"+location+"' required/></td>" +
			"<td><input type='text' class='form-control form-control-sm assignIssuedByInput' value='"+issuedBy+"' required/></td>" +
			"<td><input type='date' class='form-control form-control-sm assignDateIssuedInput' value='"+dateIssued+"' required/></td>" +
			"<td><input type='date' class='form-control form-control-sm assignDateReturnedInput' value='"+dateReturned+"'/></td>" +
			"<td><input type='text' class='form-control form-control-sm assignReturnedToInput' value='"+returnedTo+"'/></td>" +
			"<td><input type='text' class='form-control form-control-sm assignRemarksInput' value='"+remarks+"'/></td>" +
			"<td><button type='button' class='btn btn-danger btn-xs removeAssignmentRowBtn'>&times;</button></td>" +
		"</tr>"
	);
	$("#assignmentRowsBody .ca-empty-row").remove();
	$("#assignmentRowsBody").append(row);
}

/** Shows a placeholder row when there are no assignments, so the table doesn't look broken/empty. */
function refreshAssignmentEmptyState(){
	$("#assignmentRowsBody .ca-empty-row").remove();
	if($("#assignmentRowsBody .assignmentRow").length === 0){
		$("#assignmentRowsBody").append(
			"<tr class='ca-empty-row'><td colspan='8'>No assignments yet — click \"+ Add Assignment\" to add one.</td></tr>"
		);
	}
}

$(document).on("click","#addAssignmentRowBtn",function(){
	withEmployeeList(function(){
		addAssignmentRow(null);
	});
});

$(document).on("click",".removeAssignmentRowBtn",function(){
	$(this).closest("tr").remove();
	refreshAssignmentEmptyState();
});

/** Contiguous 0..N-1 indices — Spring's list binder pads gaps with nulls otherwise. */
function reindexAssignmentRows(){
	$("#assignmentRowsBody .assignmentRow").each(function(i){
		$(this).find(".assignmentIdInput").attr("name", "assignments["+i+"].id");
		$(this).find(".assignEmployeeSelect").attr("name", "assignments["+i+"].employeeId");
		$(this).find(".assignLocationInput").attr("name", "assignments["+i+"].location");
		$(this).find(".assignIssuedByInput").attr("name", "assignments["+i+"].issuedBy");
		$(this).find(".assignDateIssuedInput").attr("name", "assignments["+i+"].dateIssued");
		$(this).find(".assignDateReturnedInput").attr("name", "assignments["+i+"].dateReturned");
		$(this).find(".assignReturnedToInput").attr("name", "assignments["+i+"].returnedTo");
		$(this).find(".assignRemarksInput").attr("name", "assignments["+i+"].remarks");
	});
}

$(document).on("click","#saveCompanyAssetBtn",function(e){

	reindexAssignmentRows();

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

	$("#assignmentRowsBody .assignmentRow").each(function(){
		let dateIssued = $(this).find(".assignDateIssuedInput").val();
		let dateReturnedField = $(this).find(".assignDateReturnedInput");
		let dateReturned = dateReturnedField.val();
		if (dateIssued && dateReturned && dateReturned < dateIssued) {
			dateReturnedField.css('border', '2px solid red');
			dateReturnedField.after('<span class="error" style="color:red;font-size:11px;display:block;">Before issue date</span>');
			isValid = false;
		}
	});

	if (!isValid) {
		e.preventDefault();
	}else{
		$('#companyAssetForm').submit();
	}
});

} );
