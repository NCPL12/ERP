var partyAddressTable;
$(document).ready(function () {
	
	//save value of save button and save&exit button in a hidden input so that based on value in backend written code to redirect
	//when save&exit redirecting to partylist page
	$(document).on("click","#savePartyBtn",function(){
		 $('#savePartyAddressBtn').val("partyAddressSave");
	})
	$(document).on("click","#savePartyBankBtn",function(){
		 $('#savePartyAddressBtn').val("partyAddressSaveAndExit");
	})
	$(document).on('submit', '#partyAddressForm', function(e) {
		var address=$('#addr1').val();
		var city=$('#cityDropDown').val();
		var phne1 =  $("#phne1").val().trim();
		var phne2 =  $("#phone2").val().trim();
		var email1 =  $("#email1").val();
		var email2=$("#email2").val();
		var phoneregex= new RegExp(/^[0-9-+\s]+$/);
		
		if(address == ""){
			e.preventDefault(e);
			$.error("Please enter the address1 ");
		    $("#addr1").addClass('border-color');
		
		}  	
		if(city == null){
			e.preventDefault(e);
			$.error("Plaese select the city");
			$("#cityDropDown").addClass('border-color');
		
		}  
		if (phne1.match(phoneregex)||phne1=="") {

		}
		else{
			$("#phne1").addClass('border-color');
	        e.preventDefault(e);
        	$.error("phone1 is not valid");
		}
		if (phne2.match(phoneregex)||phne2=="") {

		}
		else{
			$("#phone2").addClass('border-color');
	        e.preventDefault(e);
        	$.error("phone2 is not valid");
		}
		
		if(email1!="" &&IsEmail(email1)==false){
			 $.error("Please enter valid E-mail1 Id");
				$("#email1").addClass('border-color');
		        e.preventDefault(e);	
		}
		if(email2!="" &&IsEmail(email2)==false){
			 $.error("Please enter valid E-mail2 Id");
				$("#email2").addClass('border-color');
		        e.preventDefault(e);	
		}
		
		$('input').change(function(){
			$("#addr1").removeClass('border-color');
		})
		$('select').click(function(){
			$("#cityDropDown").removeClass('border-color');
		})
		
		$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
			$("#savePartyBtn").attr('disabled', false);
			$("#partyAddressSaveAndExit").attr('disabled', false);
			});
		
		 $("#savePartyBtn", this)
	     .attr('disabled', 'disabled');
		 
		 $("#partyAddressSaveAndExit", this)
	     .attr('disabled', 'disabled');
	});
	partyAddressTable=$('#addressListTable').DataTable({
    	"aaData": data,
    	"autoWidth": true,
    	"pageLength": 15,
    	"aoColumns": [
    	{
    		'data':"partyName",
    		"width": "320px",
    		"class": "text-field-large-nowrap",
    		render: function ( data, type, row ) {
    		    return data.length > 25 ?
    		        data.substr( 0, 25 ) +'...' :
    		        data;
    		}
    	},
    	{ 
    		"data" : "partyaddr_city.name",
    	},
    	{ "data": "partyaddr_city.state.name",
    	},
		{ 
			"data": "partyaddr_city.state.country.name",
	    },
    	{ "data": "phone1"},
    	{ 
			"data": "email1",
			"class":"text-field-large-nowrap",
			render: function ( data, type, row ) {
    		    return data.length > 25 ?
    		        data.substr( 0, 25 ) +'...' :
    		        data;
    		}
		},
		{ 
			"data": "gst",

    	},
    	{ 
			"data": "website",
			"class":"text-field-large-nowrap",
			render : function (data) {

				return data.length > 25 ?
				'<a href='+data+' target="_blank">'+ data.substr( 0, 25 )+'...' +'</a>' :'<a href='+data+' target="_blank">'+data+'</a>';
			},	
    	},
    	{
			"class":"styleOfSlNo",
			render : function () {
				return '<i class="deleteButton fa fa-trash " aria-hidden="true"></i>';
			}
		}
    	
    	]
    });
	
	$(document).on("change", "#cityDropDown", function(e) {
		var cityId = $(this).val();
		getCityById(cityId);
	});
	
	
	//On dbclick go to edit mode 
	$('#addressListTable tbody').on('dblclick', 'tr', function () {
		$("#savePartyBtn").html("Update");
		   var row = partyAddressTable.row($(this).closest("tr").get(0));
		   var rowData=row.data();
		      // $("#partyAddressForm").append('<input type="hidden" name="id" id="partyAddId"/>')
			   $("#partyAddId").val(rowData.id);
			   $("#PartyName").val(rowData.partyName);
			   $("#addr1").val(rowData.addr1);
			   $("#addr2").val(rowData.addr2);
			   $("#cityDropDown").val(rowData.partyaddr_city.id);
			   $('#cityDropDown').select2(rowData, {id: rowData.partyaddr_city.id, a_key:rowData.partyaddr_city.name});
			   $("#phne1").val(rowData.phone1);
			   $("#phone2").val(rowData.phone2);
			   $("#email1").val(rowData.email1);
			   $("#email2").val(rowData.email2);
			   $("#websit").val(rowData.website);
			   $("#state").val(rowData.partyaddr_city.state.name);
			   $("#country").val(rowData.partyaddr_city.state.country.name);  
			   $("#contact").val(rowData.contact);
			   $("#faxnumber").val(rowData.faxnumber);
			   $("#gst").val(rowData.gst);
			   $("#pincode").val(rowData.pin);
			   getCityById(rowData.partyaddr_city.id);
		  });
	
		$(document).on('click', '#editParty', function () {
			var partyId=window.location.href.split("=")[1]; 
			window.location = pageContext+"/api/party/view?partyId="+partyId;
	      });

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
			//$("#pincode").val(cityObj.areaCode);

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

	//code to delete the item row.
	$(document).on("click",".deleteButton",function(){
		var row = partyAddressTable.row($(this).closest("tr").get(0));
		   var rowData=row.data();
		   var id = rowData.id;
	   bootbox.confirm({
		   message: "Do you want to delete the address?",
		   buttons: {
			   cancel: {
				   label: 'Cancel'
			   },
			   confirm: {
				   label: 'Confirm'
			   }
		   },
		   callback: function (result) {
			   result ? deleteAltAddress(id):"";
		   }
	   });		
   })
   
   function deleteAltAddress(id){
   
		   $.ajax({
			   type : "POST",  
			   url : api.DELETE_PARTY_ALT_ADDRESS  +"?id="+id,
			   success : function(response) {
				   
				   window.location.reload();
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
	
	function IsEmail(email) {
		  var regex = /^([a-zA-Z0-9_\.\-\+])+\@(([a-zA-Z0-9\-])+\.)+([a-zA-Z0-9]{2,4})+$/;
		  if(!regex.test(email)) {
		    return false;
		  }else{
		    return true;
		  }
	}

	
    

