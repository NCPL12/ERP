
/**
 * 
 */
var categoryTable;
var typeTable;
var cityTable;
var ajaxforSaveCat;
var designationTable;
$(document).ready(function () {
	//getPartyList();
	$('.select2').select2({});
	$("#stateDropdown").select2({dropdownAutoWidth : true});
	var partyId=$("#partyId").val();
	getCategoryList();
	getCityList();
	getTypeList();
	var cityId = $("#cityDropDown").val();
	getCityById(cityId);
	addType();
	addCity();
	addState();
	addCategory();
	designationValidation();
	addState();
	
	//code to set values to the inputs on double click of party list row
	 if(partyObj!=""){
		 getContactListByParty(partyObj.id);
		 $.each( partyObj, function( key, value ) {
			if(partyObj.interState=="true"){
				$("#interState").prop("checked",true);
			}else{
				$("#interState").prop("checked",false);
			}
			 getCityById(partyObj.party_city.id);
			// $("#categoryDropDown").val(partyObj.party_category.id);
			 // $("[name="+key+"]").val(value);
			 $('#partyForm').find('input[name='+key+']').val(value);
	        });
		 $("#pincode").val(partyObj.pin);
		 $("#party_intial").val(partyObj.party_intial);
		$("#addCategory").removeClass('hideIcon');	
		 $("#savePartyBtn").html("Update");
		 
		 
		 
	 }
	
	 //add row on click of add contact button
	 $(document).on("click", "#addRow" , function() {
		 addContactRows();
    });
	

	 $("#contactTable").on("click", ".deleteButton", function() {
   	 $(this).closest("tr").remove();
   	 $('tbody').find('tr').each(function (index) {
            var firstTDDomEl = $(this).find('td')[0];
            //Creating jQuery object
            var $firstTDJQObject = $(firstTDDomEl);
        });
      });

	$(document).on("click","#addCategory",function(){
		getCategoryList();
		let partyId=$("#partyId").val();
		getPartyCategoryById(partyId);
		$("#saveCategory").html("Save");
		$("#categoryHeader").html("Add/Delete Category");
		$("#categoryForm")[0].reset();
		$("#categoryModel").modal();
		$("#categoryForm").find('input[name=id]').val("");
	}) 
	//on click of plus icon beside designation show popup
	$(document).on("click","#addDesignation",function(){
		$("#designationModel").modal("show");
		$("#saveDesignation").html("Save");
		   $("#designationHeader").html("Add/Delete Designation");
		   $("#designationForm")[0].reset();
		   $("#designationForm").find('input[name=id]').val("");
		   getDesignationList();

	}) 
	//on click of cancel button in category popup reset form and close popup
	$("#categoryResetBtn").on("click",function(e){
		  $("#saveCategory").html("Save");
		  $("#categoryHeader").html("Add/Delete Category");
	      $("#categoryModel").modal("hide");	
		  $("#categoryForm")[0].reset();
		  $("#categoryForm").find('input[name=id]').val("");
	  });
	//on click of cancel button in the designation popup close popup and reset
	$("#designationResetBtn").on("click",function(e){
	      $("#saveDesignation").html("Save");
	      $("#designationHeader").html("Add/Delete Designation");
	      $("#designationModel").modal("hide");	
	      $("#designationForm")[0].reset();
		  $("#designationForm").find('input[name=id]').val("");
	  });
    //on click of cancel button in the type popup close popup and reset
	$("#typeResetBtn").on("click",function(e){
		$("#typeHeader").html("Add/Delete Type");
		$("#saveType").html("Save");
		$("#typeModel").modal("hide");	
		$("#typeForm")[0].reset();
		$("#typeForm").find('input[name=id]').val("");
	});		
	//on click of cancel button in the city popup close popup and reset
	$("#cityResetBtn").on("click",function(e){
		$("#cityHeader").html("Add/Delete City");
		$("#saveCity").html("Save");
		$("#cityModel").modal("hide");
		$("#stateDropdown").val('').trigger('change');	
		$("#cityForm")[0].reset();
		$("#cityForm").find('input[name=id]').val("");
	});			
     //on click of cancel button in the state popup close popup and reset
	 $("#stateResetBtn").on("click",function(e){
		$("#stateModel").modal("hide");	
		$("#countryDropdown").val('').trigger('change');
		$("#stateForm")[0].reset();
	}); 		          
	/***mapping category with party**/
	$(document).on("click","#categoryMapBtn",function(){
			var checkedCategoryArr = [];
					/**
					* The checked value from all the pages
					*/
					var otable=$('#category').DataTable();
					var rowcollection = otable.$(".categoryCheckbox:checked", {"page": "all"});
					rowcollection.each(function(index,elem){
						var checkbox_value = $(elem).attr('value');
						//alert(checkbox_value);
						checkedCategoryArr.push(checkbox_value);
					});
					$("#categoryInput").val(checkedCategoryArr.length);
					let partyId=$("#partyId").val();
					$.ajax({
						type : "GET",  
						url:api.SAVE_PARTY_CATEGORY+"?id="+partyId+"&categoryId="+checkedCategoryArr,    
					})		
         	}) 				

	$(document).on("click","#addType",function(){
		    $("#typeHeader").html("Add/Delete Type");
			$("#saveType").html("Save")
			$("#typeForm")[0].reset();
			$("#typeForm").find('input[name=id]').val("");
			$("#typeModel").modal();
			getTypeList();
	}) 

	$(document).on("click","#addCity",function(){
		$("#cityHeader").html("Add/Delete City");
		$("#saveCity").html("Save");
		$("#stateDropdown").val('').trigger('change');	
		$("#cityForm")[0].reset();
		$("#cityForm").find('input[name=id]').val("");
		cityListTable('d');
	}) 
	$(document).on("click","#addState",function(){
		$("#countryDropdown").val('').trigger('change');
        $("#stateForm")[0].reset();
		$("#stateModel").modal();
		getCountryList();
	}) 
	$(document).on("change","#stateDropdown",function(){
		var stateId=$("#stateDropdown option:selected").val();
		if(stateId!=""){
		getStateById(stateId);
		}
	})
	
	/**$(document).on("click","#saveType",function(){
		var serialize=$("#typeForm").serialize();
		var serializeArray=$("#typeForm").serializeArray();
		var typeName=serializeArray[0].value;
		//var typeName=$("#typeInput").val();
		$.ajax({
			method:'GET',
			url :api.VALIDATE_TYPE + "?name="+typeName,
			success :function(response){
				if(response==false){
					if(typeName == ""){
						$.error("Please enter the type");
						$("#errorDiv").css("color","red");
					}  
		            else{
                        saveType(serialize)
					}
				}else{
					$.error("Type already exist");
				}
			},
			error : function(e) {
				console.log(e);
			}
		});    
		
	   });**/
	   $(document).on('dblclick', '#category>tbody>tr', function () {
			$("#saveCategory").html("Update");
			$("#categoryHeader").html("Update Category");
			var row = categoryTable.row($(this).closest("tr").get(0));
			var rowData=row.data();
			$("#categoryId").val(rowData.id);
			$("#categoryName").val(rowData.name);
   });
	   $(document).on('dblclick', '#typeTable>tbody>tr', function () {
		    $("#typeHeader").html("Update Type"); 
			$("#saveType").html("Update");
			var row = typeTable.row($(this).closest("tr").get(0));
			var rowData=row.data();
			$("#typeId").val(rowData.id);
			$("#typeInput").val(rowData.name);
	   });
	   $(document).on('dblclick', '#cityTable>tbody>tr', function () {
		$("#cityHeader").html("Update City");   
		$("#saveCity").html("Update");
		var row = cityTable.row($(this).closest("tr").get(0));
		var rowData=row.data();
			$("#cityId").val(rowData.id);
			$("#newCity").val(rowData.name);
			$("#newCode").val(rowData.code);
			$("#stateDropdown").val(rowData.state.id);
			$('#stateDropdown').select2(rowData, {id: rowData.state.id, a_key:rowData.state.name});
			$("#getCountry").val(rowData.state.country.name);
         });
         
});

//code to add row in add contact table
function addContactRows(){
	getDesignationList();
	 var newRow = $("<tr>");
	 var rowCount=$('#contactTable >tbody  > tr').length;
	 var arrayCount = rowCount;
    var columns = "";
    columns += '<td style="width: 23%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].name"  path="contacts['+arrayCount+'].name" type="text" style="width: 100%;" /><span id="contactName'+arrayCount+'" ></span></td>';
    columns += '<td style="width: 15%;"><select class="form-control form-control-sm designationDropdown select2" name="contacts['+arrayCount+'].designation" path="contacts['+arrayCount+'].designation" type="text" style="width: 100%;padding: 0px;" id="designation'+arrayCount+'" ><option value="" selected disabled>Not Selected</option></select></td>';
    columns += '<td style="width: 15%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].mobile_no"  path="contacts['+arrayCount+'].mobile_no" type="text" style="width: 100%;"  /></td>';
    columns += '<td style="width: 3%;" ><input name="contacts['+arrayCount+'].whats_app" path="contacts['+arrayCount+'].whats_app" id="whatsApp'+arrayCount+'" value="No" type="checkbox" style="width: 100%;" /></td>';
    columns += '<td style="width: 12%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].phone_no"  path="contacts['+arrayCount+'].phone_no" type="text" style="width: 100%;"   /></td>';
    columns += '<td style="width: 15%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].email_id"  path="contacts['+arrayCount+'].email_id" type="text" style="width: 100%;"    /></td>';
    columns += '<td style="width: 15%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].skype_id"  path="contacts['+arrayCount+'].skype_id" type="text"  style="width: 100%;" /></td>';
    
    columns += '<td align="center" style="width: 2%;"><i class="deleteButton positionOfTextBox fa fa-trash"  aria-hidden="true" style="width: 100%;" ></i></td>';
	newRow.append(columns);
	
	$("#contactTable").append(newRow);
	$("#designation"+arrayCount).select2({dropdownAutoWidth : true});
	 $("#whatsApp"+arrayCount).change(function() {
		 console.log("changing");
        if(this.checked) {
             $(this).val("Yes");  
		}     
    });
	
}

//get contact list on double click of party and populate in contact table
function getContactListByParty(partyId){
	$.ajax({
	    Type:'GET',
	    url : api.CONTACT_BY_PARTY+"?partyId="+partyId,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$.each(response,function( key, value ){
	    		addContactRows();
	    	
			$("input[name='contacts[" +key+ "].name']").val(value.name);
			var designationId=value.party_contact_designation.id;
			var designationName=value.party_contact_designation.name;
			$("#designation"+key).val(designationId);
			//$("#designation"+key).select2(value, {id: designationId, a_key:designationName});
			$("input[name='contacts[" +key+ "].mobile_no']").val(value.mobile_no);
			$("input[name='contacts[" +key+ "].whats_app']").val(value.whats_app);
			  if(value.whats_app=="Yes"){
				  $('#whatsApp'+key).prop('checked',true)
			  }
			  else{
				$('#whatsApp'+key).prop('checked',false)
			  }
			$("input[name='contacts[" +key+ "].phone_no']").val(value.phone_no);
			$("input[name='contacts[" +key+ "].email_id']").val(value.email_id);
			$("input[name='contacts[" +key+ "].skype_id']").val(value.skype_id);
	    	});
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

/*function getPartyList(){
	$.ajax({
	    Type:'GET',
	    url : api.PARTY_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$.each(response, function( key, value ) {
	    		  $('#partyDropDown').append('<option value='+value.id+'>'+value.partyName+'</option>'); 
	    		});
	    	//get party 
			var partyId = $('#partyDropDown').val()
			$('#party').val(partyId);
	    }
	  }); 
}*/

$(document).on("change", "#cityDropDown", function(e) {
    var cityId = $(this).val();
	getCityById(cityId);
});


// Getting city object using ajax to set state country and country code
function getCityById(cityId) {

	$.ajax({
		method : 'GET',
		url : api.GETCITYBY_ID + cityId,
		success : function(cityObj) {
			$("#state").val(cityObj.state.name);
			$("#country").val(cityObj.state.country.name);
			$("#countrycode1").val(cityObj.state.country.code);
			$("#countrycode2").val(cityObj.state.country.code);
			$("#fax").val(cityObj.state.country.code);
			//$("#fax").val();
			
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



/*  This code is for creating drop down  for category */

$(document).on("change", "#categoryDropDown", function(e) {

	var categoryId = $(this).val();
	getCategoryById(categoryId);
});

/* This function is called by getCategoryById */

function getCategoryById(categoryId) {
	
	$.ajax({
		method :'GET',
		url:api.GETCATEGORYBY_ID + catId,
		success:function(categoryObj){
			
			//$("#categorydropdown").val(categoryObj);
			
			
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
	})
}


//For validating form
$(document).on('submit', '#partyForm', function(e) {
	
	if($("input[type='checkbox']#interState").is(':checked')) {		
	      $("#interStateHidden").val("true");  
	} else{
		$("#interStateHidden").val("false");
	}  
	var name = $("#PartyNme").val().trim();
	var addr1 = $("#addr1").val();
	var phne1 =  $("#phne1").val().trim();
	var email =  $("#eml1").val();
	var email2=$("#email2").val();
	var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
	var type = $("#typeDropdown").val();
	var city=$("#cityDropDown").val();
	var pan=$("#pan").val().trim();
    var rowCount=$('#contactTable >tbody  > tr').length;
    var submit = $("#partyId").val();
    var phoneregex= new RegExp(/^[0-9-+\s]+$/);
    var phne2 =  $("#phne2").val().trim();
    //var panregex= new RegExp(/([A-Z]){5}([0-9]){4}([A-Z]){1}$/);
    var panregex= new RegExp("^[a-zA-Z0-9]+$");
    
	if(name == "" || name==undefined){
		$.error("Please enter the party name");
		$("#PartyNme").addClass('border-color');
        e.preventDefault(e);
	}
	
	if(submit == "" || submit==undefined){
		$.each(partyList,function(index,value){
			if(name == value.partyName.trim()){
			$.error("Party Name already Exists");
			e.preventDefault(e);
			}
		});
		}
	
	if(addr1 == "" || addr1==undefined){
		$.error("Please enter the address1");
		$("#addr1").addClass('border-color');
        e.preventDefault(e);
	}
	if(phne1 == "" || phne1==undefined){
		$.error("Please enter the phone1");
		$("#phne1").addClass('border-color');
        e.preventDefault(e);
	}else if (phne1.match(phoneregex)) {

		}
		else{
			$("#phne1").addClass('border-color');
	        e.preventDefault(e);
        	$.error("phone1 is not valid");
		}
	if (phne2.match(phoneregex)||phne2=="") {

	}
	else{
			$("#phne2").addClass('border-color');
	        e.preventDefault(e);
        	$.error("phone2 is not valid");
	}
	if(email == "" || email==undefined){
		$.error("Please enter the email1");
		$("#eml1").addClass('border-color');
        e.preventDefault(e);
		}
	 else if(IsEmail(email.trim())==false){
		 $.error("Please enter valid E-mail Id");
			$("#eml1").addClass('border-color');
	        e.preventDefault(e);	
	}
	 $("#eml1").change(function () {
			$("#eml1").removeClass('border-color');
		});
	 if(email2!="" && IsEmail(email2.trim())==false){
		 $.error("Please enter valid E-mail Id")
			$("#email2").addClass('border-color');
	        e.preventDefault(e);	
	}
	 $("#email2").change(function () {
			$("#email2").removeClass('border-color');
		});
	if(type == "" || type==undefined){
		$.error("Please select the type");
		$("#type").addClass('border-color');
        e.preventDefault(e);
	}
	if(city == "" || city==undefined){
		$.error("Please select the city");
		$("#cityDropDown").addClass('border-color');
        e.preventDefault(e);
	}
	if(pan == "" || pan==undefined){
		$.error("Please enter the pan ");
		$("#pan").addClass('border-color');
        e.preventDefault(e);
	}else if(pan.length<=10){
		if (pan.match(panregex)) {
	
		}
		else{
			$("#pan").addClass('border-color');
	        e.preventDefault(e);
	    	$.error("pan is not valid");
		}
	}else{
		$("#pan").addClass('border-color');
        e.preventDefault(e);
    	$.error("pan numnber length is more");
	}
	$('input,select').change(function(){
		$("#PartyNme").removeClass('border-color');
		$("#cityDropDown").removeClass('border-color');
		$("#type").removeClass('border-color');
		$("#eml1").removeClass('border-color');
		$("#phne1").removeClass('border-color');
		$("#addr1").removeClass('border-color');
		$("#pan").removeClass('border-color');
	})

	for(var i = 0 ; i<rowCount;i++){
	let row=i + 1;	
	var contactName = 	$("input[name='contacts[" +i+ "].name']").val();
	var designation= $('#designation'+i ).val();
	if(contactName == "" || contactName==undefined){
		$.error("Please enter the contact name at row "+row);
		$("input[name='contacts[" +i+ "].name']").addClass('border-color');
        e.preventDefault(e);
	}
	if(designation == "" || designation==undefined){
		$.error("Please select the designation at row " +row);
		$("input[name='contacts[" +i+ "].designation']").addClass('border-color');
        e.preventDefault(e);
	}
	}
	
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#savePartyBtn").attr('disabled', false);
		$("#savePartyAndExitBtn").attr('disabled', false);
		});
	
	 $("#savePartyBtn", this)
     .attr('disabled', 'disabled');
	 
	 $("#savePartyAndExitBtn", this)
     .attr('disabled', 'disabled');
	
});

function getCategoryList(){
		$.ajax({
		method :'GET',
		url:api.CATEGORY_LIST,
		success:function(response){
		console.log(response);
	      categoryListTable(response);	
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
	})
	}
/***Category List in Modal***/
function categoryListTable(categoryList){
	categoryTable= $('#category').DataTable({
		   destroy:true,
		    "aaData": categoryList,
		    "aoColumns": [
		    {"title": "Select",
			"width":"10%",
		    "mData":"id",
		    render : function ( mData, type, row, meta ) {
		           return '<input type="checkbox" name="id" value="'+mData+'" class="categoryCheckbox" id="categoryCheckbox">'
		           }
		    },
		    {"title": "Category",
			 "width":"30%",
		    "mData": "name"
		    },
		    
		    { 	
		    	"title":'Delete',
				"width":"10%",
		    	"class":"styleOfSlNo",
		    		render : function ( mData, type, row,meta ) {
		                    return '<i class="deleteCategory fa fa-trash " aria-hidden="true"></i>';
		                }
		    }
		    ]
		   });  
}
function getPartyCategoryById(partyId){
                    $.ajax({
						type : "GET",  
						url:api.PARTY_CATEGORY_BY_PARTYID+"?id="+partyId,
	                     async: 'false',
						success  : function(response){
							console.log(response);
							var otable=$('#category').DataTable();
						    $.each(response, function( key, value ) {
							  otable.$('input.categoryCheckbox[type=checkbox][value='+value.category_id.id+']').prop('checked', true);
							//var bool = check;
							   // $(":checkbox[value='" + value.category_id.id + "']").prop("checked", true);
	    	               	});
						},  
						complete:function(resp){
							if(resp.status==500){
								$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
							}
						},
						error : function(e) {
							console.log(e);
						}  	
					})	

}
function addCategory(){
	$('#categoryForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var categoryJson=$(form).form2json()
			categoryJson['id']=+categoryJson['id'];
			//check for duplicate category
			$.ajax({
				method:'GET',
				url :api.VALIDATE_CATEGORY + "?name="+categoryJson.name+ "&id="+categoryJson.id,
				success :function(response){
					if(response==false){
						if(categoryJson.name == null){
							$.error("Please enter the category");
							$("#categoryName").addClass("has-error");
							$('input').on("change",function(){
								$("#categoryName").removeClass("has-error")
							})
						}  
						else{
							saveCategory(categoryJson)
						}
					}else{
						$.error("Category already exist");
					}
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
	});
}
   
 

function saveCategory(category){
	showLoader(); 
			$.ajax({
					type : "POST",  
					url : api.ADD_CATEGORY,
					dataType  : "json",
					data:category,
					
					success : function(response) {
							$("#categoryForm")[0].reset();
							$("#categoryForm").find('input[name=id]').val("");
							$("#saveCategory").html("Save");
							$("#categoryHeader").html("Add/Delete Category");
							getCategoryList();
							//let partyId=$("#partyId").val();
							//getPartyCategoryById(partyId);
							hideLoader();
							$.success("Category saved successfully");

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
function getCityList(){
	$.ajax({
	    method:'GET',
	    url : api.CITY_LIST,
	    async: false,
	    success  : function(response){
			//cityListTable(response); 
			$('#cityDropDown option:not(:first)').remove();
	    	$.each(response, function( key, value ) {
	    		  $('#cityDropDown').append('<option value='+value.id+'>'+value.name+'</option>'); 
	    		});
			 $("#cityDropDown").val(partyObj.party_city.id);
			
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


$(document).on("click",".deleteCategory",function(){
	var row = categoryTable.row($(this).closest("tr").get(0));
	   var rowData=row.data();
	   var id = rowData.id;
   bootbox.confirm({
	   message: "Do you want to delete the category?",
	   buttons: {
		   cancel: {
			   label: 'Cancel'
		   },
		   confirm: {
			   label: 'Confirm'
		   }
	   },
	   callback: function (result) {
		   result ? deleteCategory(id):"";
	   }
   });		
})

function deleteCategory(id){
	//showLoader();
	   $.ajax({
		   type : "POST",  
		   url : api.DELETE_CATEGORY +"?id="+id,
		   success : function(response) {
			getCategoryList();
			let partyId=$("#partyId").val();
			getPartyCategoryById(partyId);
			$.success("Category deleted successfully");
			//hideLoader();
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

function getTypeList(){
	$.ajax({
	method :'GET',
	url:api.TYPE_LIST,
	success:function(response){
	console.log(response);
	typeListTable(response);
	$('#typeDropdown option:not(:first)').remove();
	$.each(response, function( key, value ) {
		$('#typeDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
	  });
   $("#typeDropdown").val(partyObj.party_type.id);	
	},  
	complete:function(resp){
		if(resp.status==500){
			$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
		}
	},
	error : function(e) {
		console.log(e);
	}  	
})
}
/***Type List in Modal***/
function typeListTable(typeList){
	typeTable= $('#typeTable').DataTable({
		   destroy:true,
		    "aaData": typeList,
		    "aoColumns": [
		    {"title": "Type",
		    "mData": "name"
		    },
		    
		    { 	
		    	"title":'Delete',
		    	"class":"styleOfSlNo",
		    		render : function ( mData, type, row,meta ) {
		                    return '<i class="deleteType fa fa-trash " aria-hidden="true"></i>';
		                }
		    }
		    ]
		   });  
}

$(document).on("click",".deleteType",function(){
	var row = typeTable.row($(this).closest("tr").get(0));
	   var rowData=row.data();
	   var id = rowData.id;
   bootbox.confirm({
	   message: "Do you want to delete the type?",
	   buttons: {
		   cancel: {
			   label: 'Cancel'
		   },
		   confirm: {
			   label: 'Confirm'
		   }
	   },
	   callback: function (result) {
		   result ? deleteType(id):"";
	   }
   });		
})
function deleteType(id){
	   $.ajax({
		   type : "POST",  
		   url : api.DELETE_TYPE +"?id="+id,
		   success : function(response) {
			$.success("Type deleted successfully");   
			getTypeList();
		   }, 
		   error : function(e) {
			$.error("Type cannot be deleted because it is already mapped");
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

function addType(){
		$('#typeForm').validate({
			rules: {
	
			},
			submitHandler:function(form) {
				var typeJson=$(form).form2json()
				typeJson['id']=+typeJson['id'];
				//check for duplicate type
				$.ajax({
					method:'GET',
					url :api.VALIDATE_TYPE+ typeJson.name+"/"+typeJson.id,
					success :function(response){
						if(response==false){
							if(typeJson.name == null){
								$.error("Please enter the type");
								$("#typeInput").addClass("has-error")
								$('input').on("change",function(){
								$("#typeInput").removeClass("has-error")
							})
							}  
							else{
								saveType(typeJson)
							}
						}else{
							$.error("Type already exist");
						}
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
		});   
}
function saveType(type){
	showLoader(); 
			$.ajax({
					type : "POST",  
					url : api.ADD_TYPE,
					dataType  : "json",
					data:type,
					
					success : function(response) {
							$("#typeForm")[0].reset();
							$("#typeForm").find('input[name=id]').val("");
							$("#typeHeader").html("Add/Delete Type");
							$("#saveType").html("Save");
							getTypeList();
							hideLoader();
							$.success("Type saved Successfully");

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
function getStateList(){
		$.ajax({
			method :'GET',
			url:api.STATE_LIST,
			success:function(response){
			console.log(response);
			$('#stateDropdown option:not(:first)').remove();
			$.each(response, function( key, value ) {
				$('#stateDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
			});
			},  
			complete:function(resp){
				if(resp.status==500){
					$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
				}
			},
			error : function(e) {
				console.log(e);
			}  		
	})
}

/**Country list***/
function getCountryList(){
	$.ajax({
		method :'GET',
		url:api.COUNTRY_LIST,
		success:function(response){
		console.log(response);
		$('#countryDropdown option:not(:first)').remove();
		$.each(response, function( key, value ) {
			$('#countryDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
		});
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
})
}

function addState(){
	$('#stateForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var stateJson=$(form).form2json()
			stateJson['country']=+stateJson['country'];
			if(stateJson.name==null){
				$.error("Please enter State");
				$("#newState").addClass("has-error")
				$('input').on("change",function(){
								$("#newState").removeClass("has-error")
							})
			}else if(stateJson.country.toString()=="NaN"){
				$.error("Please enter Country");
				$("#countryDropdown").addClass("has-error")
			}else{
			saveState(stateJson);
			}

		}
	});
}
/***save state***/
function saveState(state){
	showLoader(); 
	$.ajax({
			type : "POST",  
			url : api.SAVE_STATE,
			dataType  : "json",
			data:state,
			
			success : function(response) {
				$("#countryDropdown").val('').trigger('change');
				getStateList();
					$("#stateForm")[0].reset();
					
					$("#saveState").html("Save");
					hideLoader();
					$.success("State saved Successfully");

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


/***get state by id**/
function getStateById(id){
	$.ajax({
		method :'GET',
		url:api.STATE_BY_ID+"?id="+id,
		success:function(response){
		console.log(response);
		  $("#getCountry").val(response.country.name)
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
})
}

function cityListTable(cityList){
	cityTable= $('#cityTable').DataTable({
		destroy:true,
		processing : true,
		
		 "ajax": {
			   'url': api.CITY_LIST,
			   'dataSrc': ''
			},
		 "columns": [
		 {
			"title": "City",
			"data": "name"
		 },
		 {
			"title": "Code",
			"data": "code"
		 },
		 {
			"title": "State",
			"data": "state.name"
		 },
		 {
			"title": "Country",
			"data": "state.country.name"
		 },
		 { 	
			 "title":'Delete',
			 "class":"styleOfSlNo",
				 render : function ( data, type, row,meta ) {
						 return '<i class="deleteCity fa fa-trash " aria-hidden="true"></i>';
					 }
		 }
		 ]
		});

	$("#cityModel").modal({
		'show': true
	})
	getStateList();
}

function addCity(){
	
	$('#cityForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var cityJson=$(form).form2json()
			cityJson['id']=+cityJson['id'];

			//check for duplicate city
			$.ajax({
				method:'GET',
				url :api.CITY_VALIDATE + "?name="+cityJson.name+ "&&id="+cityJson.id,
				success :function(response){
					var stateName = $("#stateDropdown option:selected").val();
					if(response==false){
						if(cityJson.name==null){
							$.error("Please enter the City");
							$("#newCity").addClass("has-error")
							
							$('input').on("change",function(){
								$("#newCity").removeClass("has-error")
							})
						}else if(stateName==""){
							$.error("Please enter State");
							$("#stateDropdown").addClass("has-error")
						}else{
						saveCity(cityJson);
						}

					}else{
						$.error("City already exist");
					}
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
	});
}
function saveCity(city){
	//showLoader(); 
	$.ajax({
			type : "POST",  
			url : api.SAVE_CITY,
			dataType  : "json",
			data:city,
			
			success : function(response) {
					$("#stateDropdown").val('').trigger('change');
					$("#cityForm")[0].reset();
					$("#cityForm").find('input[name=id]').val("");
					$("#cityHeader").html("Add/Delete City");
					$("#saveCity").html("Save");
					cityListTable();
				//	hideLoader();
					$.success("City saved Successfully");

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

///on click of delete city display confirm bootbox
$(document).on("click",".deleteCity",function(){
	var row = cityTable.row($(this).closest("tr").get(0));
	var rowData=row.data();
	var id = rowData.id;
	bootbox.confirm({
		message: "Do you want to delete the city?",
		buttons: {
			cancel: {
				label: 'Cancel'
			},
			confirm: {
				label: 'Confirm'
			}
		},
		callback: function (result) {
			result ? deleteCity(id):"";
		}
	});		
})

//to delete the designation
function deleteCity(id){
	$.ajax({
		type : "POST",  
		url : api.DELETE_CITY +"?id="+id,
		success : function(response) {
			//getCityList();
			cityListTable();
			$.success("City deleted");
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

//validate designation form
function designationValidation(){

	$('#designationForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var designationJson=$(form).form2json();
			designationJson['id']=+designationJson['id'];

			//check for duplicate designation
			$.ajax({
				method:'GET',
				url :api.DESIGNATION_VALIDATE + "?name="+designationJson.name+ "&&id="+designationJson.id,
				success :function(response){
					if(response==false){
						if(designationJson.name==null){
							$.error("Please enter Designation");
							$("#designation").addClass("has-error")
							$('input').on("change",function(){
								$("#designation").removeClass("has-error")
							})
						}else{
						saveDesignation(designationJson);
						}

					}else{
						$.error("Designation already exist");
					}
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
	});
}


//to save designation
function saveDesignation(designationJson){
	$.ajax({
		type : "POST",  
		url : api.SAVE_DESIGNATION,
		data : designationJson,
		dataType  : "json",
		success : function(response) {
			getDesignationList();
			$("#saveDesignation").html("Save");
			$("#designationHeader").html("Add/Delete Designation");
			$("#designationForm")[0].reset();
			$("#designationForm").find('input[name=id]').val("");


			$.success("saved");
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

//ajax call to get all the designation list
function getDesignationList(){
	$.ajax({
		method :'GET',
		url:api.DESIGNATION_LIST,
		success:function(response){
			console.log(response);
			designationListTable(response);	
			$('.designationDropdown option:not(:first)').remove();
			$.each(response, function( key, value ) {
				$('.designationDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
			});
   		
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
	})
}

//get all the designation list and display in datatable
function designationListTable(designationList){
	designationTable= $('#designationTable').DataTable({
		destroy:true,
		"aaData": designationList,
		"aoColumns": [
			{"title": "Designation",
				"width":"30%",
				"mData": "name"
			},

			{ 	
				"title":'Delete',
				"width":"10%",
				"class":"styleOfSlNo",
				render : function ( mData, type, row,meta ) {
					return '<i class="deleteDesignation fa fa-trash " aria-hidden="true"></i>';
				}
			}
			]
	});  
	$('#designationTable tbody').on('dblclick', 'tr', function () {
		var row = designationTable.row($(this).closest("tr").get(0));
		var rowData=row.data();
		$("#designationId").val(rowData.id);
		$.each( rowData, function( key, value ) {
			$("[name="+key+"]").val(value);
		});
		$("#designationHeader").html("Update Designation");
		$("#saveDesignation").html("Update");
	});
}
//on click of designation display confirm bootbox
$(document).on("click",".deleteDesignation",function(){
	var row = designationTable.row($(this).closest("tr").get(0));
	var rowData=row.data();
	var id = rowData.id;
	bootbox.confirm({
		message: "Do you want to delete the designation?",
		buttons: {
			cancel: {
				label: 'Cancel'
			},
			confirm: {
				label: 'Confirm'
			}
		},
		callback: function (result) {
			result ? deleteDesignation(id):"";
		}
	});		
})

//to delete the designation
function deleteDesignation(id){
	$.ajax({
		type : "POST",  
		url : api.DELETE_DESIGNATION +"?id="+id,
		success : function(response) {
			getDesignationList();
			$.success("Designation deleted");
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
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


$(document).ready(function () {
	$(function() {
		
		 var partynames  =new Array();
		$.each(partyList,function(index,value){
			partynames.push(value.partyName);	
			
		});
	  /*  var availableTutorials  =  [
	       "ActionScript",
	       "Bootstrap",
	       "C",
	       "C++",
	    ];*/
	    $( "#PartyNme" ).autocomplete({
	       source: partynames
	    });
	 });
	});

function IsEmail(email) {
	  var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
	  if(!regex.test(email)) {
	    return false;
	  }else{
	    return true;
	  }
}


/**
 * 
 */
var categoryTable;
var typeTable;
var cityTable;
var ajaxforSaveCat;
var designationTable;
$(document).ready(function () {
	//getPartyList();
	$('.select2').select2({});
	$("#stateDropdown").select2({dropdownAutoWidth : true});
	var partyId=$("#partyId").val();
	getCategoryList();
	getCityList();
	getTypeList();
	var cityId = $("#cityDropDown").val();
	getCityById(cityId);
	addType();
	addCity();
	addState();
	addCategory();
	designationValidation();
	addState();
	
	//code to set values to the inputs on double click of party list row
	 if(partyObj!=""){
		 getContactListByParty(partyObj.id);
		 $.each( partyObj, function( key, value ) {
			if(partyObj.interState=="true"){
				$("#interState").prop("checked",true);
			}else{
				$("#interState").prop("checked",false);
			}
			 getCityById(partyObj.party_city.id);
			// $("#categoryDropDown").val(partyObj.party_category.id);
			 // $("[name="+key+"]").val(value);
			 $('#partyForm').find('input[name='+key+']').val(value);
	        });
		 $("#pincode").val(partyObj.pin);
		 $("#party_intial").val(partyObj.party_intial);
		$("#addCategory").removeClass('hideIcon');	
		 $("#savePartyBtn").html("Update");
		 
		 
		 
	 }
	
	 //add row on click of add contact button
	 $(document).on("click", "#addRow" , function() {
		 addContactRows();
    });
	

	 $("#contactTable").on("click", ".deleteButton", function() {
   	 $(this).closest("tr").remove();
   	 $('tbody').find('tr').each(function (index) {
            var firstTDDomEl = $(this).find('td')[0];
            //Creating jQuery object
            var $firstTDJQObject = $(firstTDDomEl);
        });
      });

	$(document).on("click","#addCategory",function(){
		getCategoryList();
		let partyId=$("#partyId").val();
		getPartyCategoryById(partyId);
		$("#saveCategory").html("Save");
		$("#categoryHeader").html("Add/Delete Category");
		$("#categoryForm")[0].reset();
		$("#categoryModel").modal();
		$("#categoryForm").find('input[name=id]').val("");
	}) 
	//on click of plus icon beside designation show popup
	$(document).on("click","#addDesignation",function(){
		$("#designationModel").modal("show");
		$("#saveDesignation").html("Save");
		   $("#designationHeader").html("Add/Delete Designation");
		   $("#designationForm")[0].reset();
		   $("#designationForm").find('input[name=id]').val("");
		   getDesignationList();

	}) 
	//on click of cancel button in category popup reset form and close popup
	$("#categoryResetBtn").on("click",function(e){
		  $("#saveCategory").html("Save");
		  $("#categoryHeader").html("Add/Delete Category");
	      $("#categoryModel").modal("hide");	
		  $("#categoryForm")[0].reset();
		  $("#categoryForm").find('input[name=id]').val("");
	  });
	//on click of cancel button in the designation popup close popup and reset
	$("#designationResetBtn").on("click",function(e){
	      $("#saveDesignation").html("Save");
	      $("#designationHeader").html("Add/Delete Designation");
	      $("#designationModel").modal("hide");	
	      $("#designationForm")[0].reset();
		  $("#designationForm").find('input[name=id]').val("");
	  });
    //on click of cancel button in the type popup close popup and reset
	$("#typeResetBtn").on("click",function(e){
		$("#typeHeader").html("Add/Delete Type");
		$("#saveType").html("Save");
		$("#typeModel").modal("hide");	
		$("#typeForm")[0].reset();
		$("#typeForm").find('input[name=id]').val("");
	});		
	//on click of cancel button in the city popup close popup and reset
	$("#cityResetBtn").on("click",function(e){
		$("#cityHeader").html("Add/Delete City");
		$("#saveCity").html("Save");
		$("#cityModel").modal("hide");
		$("#stateDropdown").val('').trigger('change');	
		$("#cityForm")[0].reset();
		$("#cityForm").find('input[name=id]').val("");
	});			
     //on click of cancel button in the state popup close popup and reset
	 $("#stateResetBtn").on("click",function(e){
		$("#stateModel").modal("hide");	
		$("#countryDropdown").val('').trigger('change');
		$("#stateForm")[0].reset();
	}); 		          
	/***mapping category with party**/
	$(document).on("click","#categoryMapBtn",function(){
			var checkedCategoryArr = [];
					/**
					* The checked value from all the pages
					*/
					var otable=$('#category').DataTable();
					var rowcollection = otable.$(".categoryCheckbox:checked", {"page": "all"});
					rowcollection.each(function(index,elem){
						var checkbox_value = $(elem).attr('value');
						//alert(checkbox_value);
						checkedCategoryArr.push(checkbox_value);
					});
					$("#categoryInput").val(checkedCategoryArr.length);
					let partyId=$("#partyId").val();
					$.ajax({
						type : "GET",  
						url:api.SAVE_PARTY_CATEGORY+"?id="+partyId+"&categoryId="+checkedCategoryArr,    
					})		
         	}) 				

	$(document).on("click","#addType",function(){
		    $("#typeHeader").html("Add/Delete Type");
			$("#saveType").html("Save")
			$("#typeForm")[0].reset();
			$("#typeForm").find('input[name=id]').val("");
			$("#typeModel").modal();
			getTypeList();
	}) 

	$(document).on("click","#addCity",function(){
		$("#cityHeader").html("Add/Delete City");
		$("#saveCity").html("Save");
		$("#stateDropdown").val('').trigger('change');	
		$("#cityForm")[0].reset();
		$("#cityForm").find('input[name=id]').val("");
		cityListTable('d');
	}) 
	$(document).on("click","#addState",function(){
		$("#countryDropdown").val('').trigger('change');
        $("#stateForm")[0].reset();
		$("#stateModel").modal();
		getCountryList();
	}) 
	$(document).on("change","#stateDropdown",function(){
		var stateId=$("#stateDropdown option:selected").val();
		if(stateId!=""){
		getStateById(stateId);
		}
	})
	
	/**$(document).on("click","#saveType",function(){
		var serialize=$("#typeForm").serialize();
		var serializeArray=$("#typeForm").serializeArray();
		var typeName=serializeArray[0].value;
		//var typeName=$("#typeInput").val();
		$.ajax({
			method:'GET',
			url :api.VALIDATE_TYPE + "?name="+typeName,
			success :function(response){
				if(response==false){
					if(typeName == ""){
						$.error("Please enter the type");
						$("#errorDiv").css("color","red");
					}  
		            else{
                        saveType(serialize)
					}
				}else{
					$.error("Type already exist");
				}
			},
			error : function(e) {
				console.log(e);
			}
		});    
		
	   });**/
	   $(document).on('dblclick', '#category>tbody>tr', function () {
			$("#saveCategory").html("Update");
			$("#categoryHeader").html("Update Category");
			var row = categoryTable.row($(this).closest("tr").get(0));
			var rowData=row.data();
			$("#categoryId").val(rowData.id);
			$("#categoryName").val(rowData.name);
   });
	   $(document).on('dblclick', '#typeTable>tbody>tr', function () {
		    $("#typeHeader").html("Update Type"); 
			$("#saveType").html("Update");
			var row = typeTable.row($(this).closest("tr").get(0));
			var rowData=row.data();
			$("#typeId").val(rowData.id);
			$("#typeInput").val(rowData.name);
	   });
	   $(document).on('dblclick', '#cityTable>tbody>tr', function () {
		$("#cityHeader").html("Update City");   
		$("#saveCity").html("Update");
		var row = cityTable.row($(this).closest("tr").get(0));
		var rowData=row.data();
			$("#cityId").val(rowData.id);
			$("#newCity").val(rowData.name);
			$("#newCode").val(rowData.code);
			$("#stateDropdown").val(rowData.state.id);
			$('#stateDropdown').select2(rowData, {id: rowData.state.id, a_key:rowData.state.name});
			$("#getCountry").val(rowData.state.country.name);
         });
         
});

//code to add row in add contact table
function addContactRows(){
	getDesignationList();
	 var newRow = $("<tr>");
	 var rowCount=$('#contactTable >tbody  > tr').length;
	 var arrayCount = rowCount;
    var columns = "";
    columns += '<td style="width: 23%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].name"  path="contacts['+arrayCount+'].name" type="text" style="width: 100%;" /><span id="contactName'+arrayCount+'" ></span></td>';
    columns += '<td style="width: 15%;"><select class="form-control form-control-sm designationDropdown select2" name="contacts['+arrayCount+'].designation" path="contacts['+arrayCount+'].designation" type="text" style="width: 100%;padding: 0px;" id="designation'+arrayCount+'" ><option value="" selected disabled>Not Selected</option></select></td>';
    columns += '<td style="width: 15%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].mobile_no"  path="contacts['+arrayCount+'].mobile_no" type="text" style="width: 100%;"  /></td>';
    columns += '<td style="width: 3%;" ><input name="contacts['+arrayCount+'].whats_app" path="contacts['+arrayCount+'].whats_app" id="whatsApp'+arrayCount+'" value="No" type="checkbox" style="width: 100%;" /></td>';
    columns += '<td style="width: 12%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].phone_no"  path="contacts['+arrayCount+'].phone_no" type="text" style="width: 100%;"   /></td>';
    columns += '<td style="width: 15%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].email_id"  path="contacts['+arrayCount+'].email_id" type="text" style="width: 100%;"    /></td>';
    columns += '<td style="width: 15%;"><input class="form-control form-control-sm" name="contacts['+arrayCount+'].skype_id"  path="contacts['+arrayCount+'].skype_id" type="text"  style="width: 100%;" /></td>';
    
    columns += '<td align="center" style="width: 2%;"><i class="deleteButton positionOfTextBox fa fa-trash"  aria-hidden="true" style="width: 100%;" ></i></td>';
	newRow.append(columns);
	
	$("#contactTable").append(newRow);
	$("#designation"+arrayCount).select2({dropdownAutoWidth : true});
	 $("#whatsApp"+arrayCount).change(function() {
		 console.log("changing");
        if(this.checked) {
             $(this).val("Yes");  
		}     
    });
	
}

//get contact list on double click of party and populate in contact table
function getContactListByParty(partyId){
	$.ajax({
	    Type:'GET',
	    url : api.CONTACT_BY_PARTY+"?partyId="+partyId,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$.each(response,function( key, value ){
	    		addContactRows();
	    	
			$("input[name='contacts[" +key+ "].name']").val(value.name);
			var designationId=value.party_contact_designation.id;
			var designationName=value.party_contact_designation.name;
			$("#designation"+key).val(designationId);
			//$("#designation"+key).select2(value, {id: designationId, a_key:designationName});
			$("input[name='contacts[" +key+ "].mobile_no']").val(value.mobile_no);
			$("input[name='contacts[" +key+ "].whats_app']").val(value.whats_app);
			  if(value.whats_app=="Yes"){
				  $('#whatsApp'+key).prop('checked',true)
			  }
			  else{
				$('#whatsApp'+key).prop('checked',false)
			  }
			$("input[name='contacts[" +key+ "].phone_no']").val(value.phone_no);
			$("input[name='contacts[" +key+ "].email_id']").val(value.email_id);
			$("input[name='contacts[" +key+ "].skype_id']").val(value.skype_id);
	    	});
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

/*function getPartyList(){
	$.ajax({
	    Type:'GET',
	    url : api.PARTY_LIST,
	    dataType:'json',
	    async: 'false',
	    success  : function(response){
	    	$.each(response, function( key, value ) {
	    		  $('#partyDropDown').append('<option value='+value.id+'>'+value.partyName+'</option>'); 
	    		});
	    	//get party 
			var partyId = $('#partyDropDown').val()
			$('#party').val(partyId);
	    }
	  }); 
}*/

$(document).on("change", "#cityDropDown", function(e) {
    var cityId = $(this).val();
	getCityById(cityId);
});


// Getting city object using ajax to set state country and country code
function getCityById(cityId) {

	$.ajax({
		method : 'GET',
		url : api.GETCITYBY_ID + cityId,
		success : function(cityObj) {
			$("#state").val(cityObj.state.name);
			$("#country").val(cityObj.state.country.name);
			$("#countrycode1").val(cityObj.state.country.code);
			$("#countrycode2").val(cityObj.state.country.code);
			$("#fax").val(cityObj.state.country.code);
			//$("#fax").val();
			
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



/*  This code is for creating drop down  for category */

$(document).on("change", "#categoryDropDown", function(e) {

	var categoryId = $(this).val();
	getCategoryById(categoryId);
});

/* This function is called by getCategoryById */

function getCategoryById(categoryId) {
	
	$.ajax({
		method :'GET',
		url:api.GETCATEGORYBY_ID + catId,
		success:function(categoryObj){
			
			//$("#categorydropdown").val(categoryObj);
			
			
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
	})
}


//For validating form
$(document).on('submit', '#partyForm', function(e) {
	
	if($("input[type='checkbox']#interState").is(':checked')) {		
	      $("#interStateHidden").val("true");  
	} else{
		$("#interStateHidden").val("false");
	}  
	var name = $("#PartyNme").val().trim();
	var addr1 = $("#addr1").val();
	var phne1 =  $("#phne1").val().trim();
	var email =  $("#eml1").val();
	var email2=$("#email2").val();
	var reg = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
	var type = $("#typeDropdown").val();
	var city=$("#cityDropDown").val();
	var pan=$("#pan").val().trim();
    var rowCount=$('#contactTable >tbody  > tr').length;
    var submit = $("#partyId").val();
    var phoneregex= new RegExp(/^[0-9-+\s]+$/);
    var phne2 =  $("#phne2").val().trim();
    //var panregex= new RegExp(/([A-Z]){5}([0-9]){4}([A-Z]){1}$/);
    var panregex= new RegExp("^[a-zA-Z0-9]+$");
    
	if(name == "" || name==undefined){
		$.error("Please enter the party name");
		$("#PartyNme").addClass('border-color');
        e.preventDefault(e);
	}
	
	if(submit == "" || submit==undefined){
		$.each(partyList,function(index,value){
			if(name == value.partyName.trim()){
			$.error("Party Name already Exists");
			e.preventDefault(e);
			}
		});
		}
	
	if(addr1 == "" || addr1==undefined){
		$.error("Please enter the address1");
		$("#addr1").addClass('border-color');
        e.preventDefault(e);
	}
	// ==================== ADD PIN VALIDATION HERE ====================
	var pin = $("#pincode").val().trim();
	if(pin == "" || pin==undefined){
	    $.error("Please enter the pin");
	    $("#pincode").addClass('border-color');
	    e.preventDefault(e);
	}
	// ==================== END PIN VALIDATION ====================

	if(phne1 == "" || phne1==undefined){
		$.error("Please enter the phone1");
		$("#phne1").addClass('border-color');
        e.preventDefault(e);
	}else if (phne1.match(phoneregex)) {

		}
		else{
			$("#phne1").addClass('border-color');
	        e.preventDefault(e);
        	$.error("phone1 is not valid");
		}
	if (phne2.match(phoneregex)||phne2=="") {

	}
	else{
			$("#phne2").addClass('border-color');
	        e.preventDefault(e);
        	$.error("phone2 is not valid");
	}
	if(email == "" || email==undefined){
		$.error("Please enter the email1");
		$("#eml1").addClass('border-color');
        e.preventDefault(e);
		}
	 else if(IsEmail(email.trim())==false){
		 $.error("Please enter valid E-mail Id");
			$("#eml1").addClass('border-color');
	        e.preventDefault(e);	
	}
	 $("#eml1").change(function () {
			$("#eml1").removeClass('border-color');
		});
	 if(email2!="" && IsEmail(email2.trim())==false){
		 $.error("Please enter valid E-mail Id")
			$("#email2").addClass('border-color');
	        e.preventDefault(e);	
	}
	 $("#email2").change(function () {
			$("#email2").removeClass('border-color');
		});
	if(type == "" || type==undefined){
		$.error("Please select the type");
		$("#type").addClass('border-color');
        e.preventDefault(e);
	}
	if(city == "" || city==undefined){
		$.error("Please select the city");
		$("#cityDropDown").addClass('border-color');
        e.preventDefault(e);
	}
	if(pan == "" || pan==undefined){
		$.error("Please enter the pan ");
		$("#pan").addClass('border-color');
        e.preventDefault(e);
	}else if(pan.length<=10){
		if (pan.match(panregex)) {
	
		}
		else{
			$("#pan").addClass('border-color');
	        e.preventDefault(e);
	    	$.error("pan is not valid");
		}
	}else{
		$("#pan").addClass('border-color');
        e.preventDefault(e);
    	$.error("pan numnber length is more");
	}
	$('input,select').change(function(){
		$("#PartyNme").removeClass('border-color');
		$("#cityDropDown").removeClass('border-color');
		$("#type").removeClass('border-color');
		$("#eml1").removeClass('border-color');
		$("#phne1").removeClass('border-color');
		$("#addr1").removeClass('border-color');
		$("#pan").removeClass('border-color');
		$("#pincode").removeClass('border-color');
	})

	for(var i = 0 ; i<rowCount;i++){
	let row=i + 1;	
	var contactName = 	$("input[name='contacts[" +i+ "].name']").val();
	var designation= $('#designation'+i ).val();
	if(contactName == "" || contactName==undefined){
		$.error("Please enter the contact name at row "+row);
		$("input[name='contacts[" +i+ "].name']").addClass('border-color');
        e.preventDefault(e);
	}
	if(designation == "" || designation==undefined){
		$.error("Please select the designation at row " +row);
		$("input[name='contacts[" +i+ "].designation']").addClass('border-color');
        e.preventDefault(e);
	}
	}
	
	$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
		$("#savePartyBtn").attr('disabled', false);
		$("#savePartyAndExitBtn").attr('disabled', false);
		});
	
	 $("#savePartyBtn", this)
     .attr('disabled', 'disabled');
	 
	 $("#savePartyAndExitBtn", this)
     .attr('disabled', 'disabled');
	
});

function getCategoryList(){
		$.ajax({
		method :'GET',
		url:api.CATEGORY_LIST,
		success:function(response){
		console.log(response);
	      categoryListTable(response);	
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
	})
	}
/***Category List in Modal***/
function categoryListTable(categoryList){
	categoryTable= $('#category').DataTable({
		   destroy:true,
		    "aaData": categoryList,
		    "aoColumns": [
		    {"title": "Select",
			"width":"10%",
		    "mData":"id",
		    render : function ( mData, type, row, meta ) {
		           return '<input type="checkbox" name="id" value="'+mData+'" class="categoryCheckbox" id="categoryCheckbox">'
		           }
		    },
		    {"title": "Category",
			 "width":"30%",
		    "mData": "name"
		    },
		    
		    { 	
		    	"title":'Delete',
				"width":"10%",
		    	"class":"styleOfSlNo",
		    		render : function ( mData, type, row,meta ) {
		                    return '<i class="deleteCategory fa fa-trash " aria-hidden="true"></i>';
		                }
		    }
		    ]
		   });  
}
function getPartyCategoryById(partyId){
                    $.ajax({
						type : "GET",  
						url:api.PARTY_CATEGORY_BY_PARTYID+"?id="+partyId,
	                     async: 'false',
						success  : function(response){
							console.log(response);
							var otable=$('#category').DataTable();
						    $.each(response, function( key, value ) {
							  otable.$('input.categoryCheckbox[type=checkbox][value='+value.category_id.id+']').prop('checked', true);
							//var bool = check;
							   // $(":checkbox[value='" + value.category_id.id + "']").prop("checked", true);
	    	               	});
						},  
						complete:function(resp){
							if(resp.status==500){
								$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
							}
						},
						error : function(e) {
							console.log(e);
						}  	
					})	

}
function addCategory(){
	$('#categoryForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var categoryJson=$(form).form2json()
			categoryJson['id']=+categoryJson['id'];
			//check for duplicate category
			$.ajax({
				method:'GET',
				url :api.VALIDATE_CATEGORY + "?name="+categoryJson.name+ "&id="+categoryJson.id,
				success :function(response){
					if(response==false){
						if(categoryJson.name == null){
							$.error("Please enter the category");
							$("#categoryName").addClass("has-error");
							$('input').on("change",function(){
								$("#categoryName").removeClass("has-error")
							})
						}  
						else{
							saveCategory(categoryJson)
						}
					}else{
						$.error("Category already exist");
					}
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
	});
}
   
 

function saveCategory(category){
	showLoader(); 
			$.ajax({
					type : "POST",  
					url : api.ADD_CATEGORY,
					dataType  : "json",
					data:category,
					
					success : function(response) {
							$("#categoryForm")[0].reset();
							$("#categoryForm").find('input[name=id]').val("");
							$("#saveCategory").html("Save");
							$("#categoryHeader").html("Add/Delete Category");
							getCategoryList();
							//let partyId=$("#partyId").val();
							//getPartyCategoryById(partyId);
							hideLoader();
							$.success("Category saved successfully");

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
function getCityList(){
	$.ajax({
	    method:'GET',
	    url : api.CITY_LIST,
	    async: false,
	    success  : function(response){
			//cityListTable(response); 
			$('#cityDropDown option:not(:first)').remove();
	    	$.each(response, function( key, value ) {
	    		  $('#cityDropDown').append('<option value='+value.id+'>'+value.name+'</option>'); 
	    		});
			 $("#cityDropDown").val(partyObj.party_city.id);
			
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


$(document).on("click",".deleteCategory",function(){
	var row = categoryTable.row($(this).closest("tr").get(0));
	   var rowData=row.data();
	   var id = rowData.id;
   bootbox.confirm({
	   message: "Do you want to delete the category?",
	   buttons: {
		   cancel: {
			   label: 'Cancel'
		   },
		   confirm: {
			   label: 'Confirm'
		   }
	   },
	   callback: function (result) {
		   result ? deleteCategory(id):"";
	   }
   });		
})

function deleteCategory(id){
	//showLoader();
	   $.ajax({
		   type : "POST",  
		   url : api.DELETE_CATEGORY +"?id="+id,
		   success : function(response) {
			getCategoryList();
			let partyId=$("#partyId").val();
			getPartyCategoryById(partyId);
			$.success("Category deleted successfully");
			//hideLoader();
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

function getTypeList(){
	$.ajax({
	method :'GET',
	url:api.TYPE_LIST,
	success:function(response){
	console.log(response);
	typeListTable(response);
	$('#typeDropdown option:not(:first)').remove();
	$.each(response, function( key, value ) {
		$('#typeDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
	  });
   $("#typeDropdown").val(partyObj.party_type.id);	
	},  
	complete:function(resp){
		if(resp.status==500){
			$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
		}
	},
	error : function(e) {
		console.log(e);
	}  	
})
}
/***Type List in Modal***/
function typeListTable(typeList){
	typeTable= $('#typeTable').DataTable({
		   destroy:true,
		    "aaData": typeList,
		    "aoColumns": [
		    {"title": "Type",
		    "mData": "name"
		    },
		    
		    { 	
		    	"title":'Delete',
		    	"class":"styleOfSlNo",
		    		render : function ( mData, type, row,meta ) {
		                    return '<i class="deleteType fa fa-trash " aria-hidden="true"></i>';
		                }
		    }
		    ]
		   });  
}

$(document).on("click",".deleteType",function(){
	var row = typeTable.row($(this).closest("tr").get(0));
	   var rowData=row.data();
	   var id = rowData.id;
   bootbox.confirm({
	   message: "Do you want to delete the type?",
	   buttons: {
		   cancel: {
			   label: 'Cancel'
		   },
		   confirm: {
			   label: 'Confirm'
		   }
	   },
	   callback: function (result) {
		   result ? deleteType(id):"";
	   }
   });		
})
function deleteType(id){
	   $.ajax({
		   type : "POST",  
		   url : api.DELETE_TYPE +"?id="+id,
		   success : function(response) {
			$.success("Type deleted successfully");   
			getTypeList();
		   }, 
		   error : function(e) {
			$.error("Type cannot be deleted because it is already mapped");
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

function addType(){
		$('#typeForm').validate({
			rules: {
	
			},
			submitHandler:function(form) {
				var typeJson=$(form).form2json()
				typeJson['id']=+typeJson['id'];
				//check for duplicate type
				$.ajax({
					method:'GET',
					url :api.VALIDATE_TYPE+ typeJson.name+"/"+typeJson.id,
					success :function(response){
						if(response==false){
							if(typeJson.name == null){
								$.error("Please enter the type");
								$("#typeInput").addClass("has-error")
								$('input').on("change",function(){
								$("#typeInput").removeClass("has-error")
							})
							}  
							else{
								saveType(typeJson)
							}
						}else{
							$.error("Type already exist");
						}
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
		});   
}
function saveType(type){
	showLoader(); 
			$.ajax({
					type : "POST",  
					url : api.ADD_TYPE,
					dataType  : "json",
					data:type,
					
					success : function(response) {
							$("#typeForm")[0].reset();
							$("#typeForm").find('input[name=id]').val("");
							$("#typeHeader").html("Add/Delete Type");
							$("#saveType").html("Save");
							getTypeList();
							hideLoader();
							$.success("Type saved Successfully");

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
function getStateList(){
		$.ajax({
			method :'GET',
			url:api.STATE_LIST,
			success:function(response){
			console.log(response);
			$('#stateDropdown option:not(:first)').remove();
			$.each(response, function( key, value ) {
				$('#stateDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
			});
			},  
			complete:function(resp){
				if(resp.status==500){
					$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
				}
			},
			error : function(e) {
				console.log(e);
			}  		
	})
}

/**Country list***/
function getCountryList(){
	$.ajax({
		method :'GET',
		url:api.COUNTRY_LIST,
		success:function(response){
		console.log(response);
		$('#countryDropdown option:not(:first)').remove();
		$.each(response, function( key, value ) {
			$('#countryDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
		});
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
})
}

function addState(){
	$('#stateForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var stateJson=$(form).form2json()
			stateJson['country']=+stateJson['country'];
			if(stateJson.name==null){
				$.error("Please enter State");
				$("#newState").addClass("has-error")
				$('input').on("change",function(){
								$("#newState").removeClass("has-error")
							})
			}else if(stateJson.country.toString()=="NaN"){
				$.error("Please enter Country");
				$("#countryDropdown").addClass("has-error")
			}else{
			saveState(stateJson);
			}

		}
	});
}
/***save state***/
function saveState(state){
	showLoader(); 
	$.ajax({
			type : "POST",  
			url : api.SAVE_STATE,
			dataType  : "json",
			data:state,
			
			success : function(response) {
				$("#countryDropdown").val('').trigger('change');
				getStateList();
					$("#stateForm")[0].reset();
					
					$("#saveState").html("Save");
					hideLoader();
					$.success("State saved Successfully");

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


/***get state by id**/
function getStateById(id){
	$.ajax({
		method :'GET',
		url:api.STATE_BY_ID+"?id="+id,
		success:function(response){
		console.log(response);
		  $("#getCountry").val(response.country.name)
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
})
}

function cityListTable(cityList){
	cityTable= $('#cityTable').DataTable({
		destroy:true,
		processing : true,
		
		 "ajax": {
			   'url': api.CITY_LIST,
			   'dataSrc': ''
			},
		 "columns": [
		 {
			"title": "City",
			"data": "name"
		 },
		 {
			"title": "Code",
			"data": "code"
		 },
		 {
			"title": "State",
			"data": "state.name"
		 },
		 {
			"title": "Country",
			"data": "state.country.name"
		 },
		 { 	
			 "title":'Delete',
			 "class":"styleOfSlNo",
				 render : function ( data, type, row,meta ) {
						 return '<i class="deleteCity fa fa-trash " aria-hidden="true"></i>';
					 }
		 }
		 ]
		});

	$("#cityModel").modal({
		'show': true
	})
	getStateList();
}

function addCity(){
	
	$('#cityForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var cityJson=$(form).form2json()
			cityJson['id']=+cityJson['id'];

			//check for duplicate city
			$.ajax({
				method:'GET',
				url :api.CITY_VALIDATE + "?name="+cityJson.name+ "&&id="+cityJson.id,
				success :function(response){
					var stateName = $("#stateDropdown option:selected").val();
					if(response==false){
						if(cityJson.name==null){
							$.error("Please enter the City");
							$("#newCity").addClass("has-error")
							
							$('input').on("change",function(){
								$("#newCity").removeClass("has-error")
							})
						}else if(stateName==""){
							$.error("Please enter State");
							$("#stateDropdown").addClass("has-error")
						}else{
						saveCity(cityJson);
						}

					}else{
						$.error("City already exist");
					}
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
	});
}
function saveCity(city){
	//showLoader(); 
	$.ajax({
			type : "POST",  
			url : api.SAVE_CITY,
			dataType  : "json",
			data:city,
			
			success : function(response) {
					$("#stateDropdown").val('').trigger('change');
					$("#cityForm")[0].reset();
					$("#cityForm").find('input[name=id]').val("");
					$("#cityHeader").html("Add/Delete City");
					$("#saveCity").html("Save");
					cityListTable();
				//	hideLoader();
					$.success("City saved Successfully");

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

///on click of delete city display confirm bootbox
$(document).on("click",".deleteCity",function(){
	var row = cityTable.row($(this).closest("tr").get(0));
	var rowData=row.data();
	var id = rowData.id;
	bootbox.confirm({
		message: "Do you want to delete the city?",
		buttons: {
			cancel: {
				label: 'Cancel'
			},
			confirm: {
				label: 'Confirm'
			}
		},
		callback: function (result) {
			result ? deleteCity(id):"";
		}
	});		
})

//to delete the designation
function deleteCity(id){
	$.ajax({
		type : "POST",  
		url : api.DELETE_CITY +"?id="+id,
		success : function(response) {
			//getCityList();
			cityListTable();
			$.success("City deleted");
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

//validate designation form
function designationValidation(){

	$('#designationForm').validate({
		rules: {

		},
		submitHandler:function(form) {
			var designationJson=$(form).form2json();
			designationJson['id']=+designationJson['id'];

			//check for duplicate designation
			$.ajax({
				method:'GET',
				url :api.DESIGNATION_VALIDATE + "?name="+designationJson.name+ "&&id="+designationJson.id,
				success :function(response){
					if(response==false){
						if(designationJson.name==null){
							$.error("Please enter Designation");
							$("#designation").addClass("has-error")
							$('input').on("change",function(){
								$("#designation").removeClass("has-error")
							})
						}else{
						saveDesignation(designationJson);
						}

					}else{
						$.error("Designation already exist");
					}
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
	});
}


//to save designation
function saveDesignation(designationJson){
	$.ajax({
		type : "POST",  
		url : api.SAVE_DESIGNATION,
		data : designationJson,
		dataType  : "json",
		success : function(response) {
			getDesignationList();
			$("#saveDesignation").html("Save");
			$("#designationHeader").html("Add/Delete Designation");
			$("#designationForm")[0].reset();
			$("#designationForm").find('input[name=id]').val("");


			$.success("saved");
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

//ajax call to get all the designation list
function getDesignationList(){
	$.ajax({
		method :'GET',
		url:api.DESIGNATION_LIST,
		success:function(response){
			console.log(response);
			designationListTable(response);	
			$('.designationDropdown option:not(:first)').remove();
			$.each(response, function( key, value ) {
				$('.designationDropdown').append('<option value='+value.id+'>'+value.name+'</option>'); 
			});
   		
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
		}  		
	})
}

//get all the designation list and display in datatable
function designationListTable(designationList){
	designationTable= $('#designationTable').DataTable({
		destroy:true,
		"aaData": designationList,
		"aoColumns": [
			{"title": "Designation",
				"width":"30%",
				"mData": "name"
			},

			{ 	
				"title":'Delete',
				"width":"10%",
				"class":"styleOfSlNo",
				render : function ( mData, type, row,meta ) {
					return '<i class="deleteDesignation fa fa-trash " aria-hidden="true"></i>';
				}
			}
			]
	});  
	$('#designationTable tbody').on('dblclick', 'tr', function () {
		var row = designationTable.row($(this).closest("tr").get(0));
		var rowData=row.data();
		$("#designationId").val(rowData.id);
		$.each( rowData, function( key, value ) {
			$("[name="+key+"]").val(value);
		});
		$("#designationHeader").html("Update Designation");
		$("#saveDesignation").html("Update");
	});
}
//on click of designation display confirm bootbox
$(document).on("click",".deleteDesignation",function(){
	var row = designationTable.row($(this).closest("tr").get(0));
	var rowData=row.data();
	var id = rowData.id;
	bootbox.confirm({
		message: "Do you want to delete the designation?",
		buttons: {
			cancel: {
				label: 'Cancel'
			},
			confirm: {
				label: 'Confirm'
			}
		},
		callback: function (result) {
			result ? deleteDesignation(id):"";
		}
	});		
})

//to delete the designation
function deleteDesignation(id){
	$.ajax({
		type : "POST",  
		url : api.DELETE_DESIGNATION +"?id="+id,
		success : function(response) {
			getDesignationList();
			$.success("Designation deleted");
		},  
		complete:function(resp){
			if(resp.status==500){
				$.error("Error occurred with error code : " + resp.responseJSON["errorCode"] + " and error message : "+ resp.responseJSON["errorMessage"])
			}
		},
		error : function(e) {
			console.log(e);
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


$(document).ready(function () {
	$(function() {
		
		 var partynames  =new Array();
		$.each(partyList,function(index,value){
			partynames.push(value.partyName);	
			
		});
	  /*  var availableTutorials  =  [
	       "ActionScript",
	       "Bootstrap",
	       "C",
	       "C++",
	    ];*/
	    $( "#PartyNme" ).autocomplete({
	       source: partynames
	    });
	 });
	});

function IsEmail(email) {
	  var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
	  if(!regex.test(email)) {
	    return false;
	  }else{
	    return true;
	  }
}
