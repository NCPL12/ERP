var partyBankTable;
$(document).ready(function () {
	var b = $("#savePartyBtn").val();
	//save value of save button and save&exit button in a hidden input so that based on value in backend written code to redirect
	//when save&exit redirecting to partylist page
	$(document).on("click","#savePartyBtn",function(){
		 $('#savePartyBankBtn').val("partyBankSave");
	})
	$(document).on("click","#savePartyBankBtn",function(){
		 $('#savePartyBankBtn').val("partyBankSaveAndExit");
	})
	
    $(document).on('submit', '#partyBankForm', function(e) {
    	/* var id =  $(this.submitButton).attr("id");
    	   if(id == "savePartyBtn"){
    		   $('#savePartyBankBtn').val("savePartyBank");
    	   }else {
    		   $('#savePartyBankBtn').val("partyBankSaveAndExit");
       }*/
    	
		let bankName=$("#bankName").val();
		let branch=$("#branch").val();
		let ifsc=$("#ifsc").val().trim();
		let accountNo=$("#accountNo").val().trim();
		var submit = $('#partyBankId').val();
		var ifscregex= new RegExp("^[a-zA-Z0-9]+$");
		var accregex= new RegExp("^[0-9]+$");
		if(bankName == ""){
			e.preventDefault(e);
			$.error("Please enter the bank name ");
		    $("#bankName").addClass('border-color');
		
		}  
		if(branch == ""){
			e.preventDefault(e);
			$.error("Please enter the branch ");
		    $("#branch").addClass('border-color');
		
		}  	
		if(ifsc == ""){
			e.preventDefault(e);
			$.error("Please enter the IFSC code ");
		    $("#ifsc").addClass('border-color');
		
		}else if(ifsc.length<=11){
			if (ifsc.match(ifscregex)) {
				
			}
			else{
				$("#ifsc").addClass('border-color');
		        e.preventDefault(e);
		    	$.error("ifsc is not valid");
			}
		}else{
			$("#ifsc").addClass('border-color');
	        e.preventDefault(e);
	    	$.error("ifsc numnber length is more");
		}
		
		
		if(accountNo == ""){
			e.preventDefault(e);
			$.error("Please enter the account number ");
		    $("#accountNo").addClass('border-color');
		
		}else if(accountNo.length<=20){
			if (accountNo.match(accregex)) {
				
			}
			else{
				$("#accountNo").addClass('border-color');
		        e.preventDefault(e);
		    	$.error("accountNo is not valid");
			}
		}else{
			$("#accountNo").addClass('border-color');
	        e.preventDefault(e);
	    	$.error("account numnber length is more");
		}  	
		if(submit == undefined || submit == ""){
		$.each(data,function(index,value){
			if(accountNo == value.account_no){
			$.error("Account Number already Exists");
			e.preventDefault(e);
			}
		});
		}
		
		if(submit!= undefined ){
			$.each(data,function(index,value){
				
					if(accountNo == value.account_no){
						if(submit == value.id){
						
						}
					  else{
						
						$.error("Account Number already Exists");
						e.preventDefault(e);
						}
					}
				
				
			});
			}
		$('input').change(function(){
			$("#bankName").removeClass('border-color');
			$("#branch").removeClass('border-color');
			$("#ifsc").removeClass('border-color');
			$("#accountNo").removeClass('border-color');
		})
		
		$('input:not(:button,:submit),textarea,select').on("focusout input",function () {
			$("#savePartyBtn").attr('disabled', false);
			$("#partyBankSaveAndExitBtn").attr('disabled', false);
			});
		
		 $("#savePartyBtn", this)
	     .attr('disabled', 'disabled');
		 
		 $("#partyBankSaveAndExitBtn", this)
	     .attr('disabled', 'disabled');

    });
	$(document).on('click', '#editPartyinBank', function () {
		var partyId=window.location.href.split("=")[1]; 
		window.location = pageContext+"/api/party/view?partyId="+partyId;
	});
   
    partyBankTable=$('#addressListTable').DataTable({
    	"aaData": data,
    	"autoWidth": true,
    	"pageLength": 15,
    	"aoColumns": [
    	{ 
			"data" : "bank_name",
			"class": "text-field-large-nowrap",
    		render: function ( data, type, row ) {
    		    return data.length > 25 ?
    		        data.substr( 0, 25 ) +'...' :
    		        data;
    		}
    	},
    	
		{ 
			"data": "branch_name",
			"class": "text-field-large-nowrap",
    		render: function ( data, type, row ) {
    		    return data.length > 25 ?
    		        data.substr( 0, 25 ) +'...' :
    		        data;
    		}
		},
		{ 
			"data": "ifsc",
    	},
    	{ 
			"data": "account_no"
		},
    	{
			"class":"styleOfSlNo",
			render : function () {
				return '<i class="deleteButton fa fa-trash " aria-hidden="true"></i>';
			}
		}
    	
    	]
    });
	//On dbclick go to edit mode 
	$('#addressListTable tbody').on('dblclick', 'tr', function () {
		$("#savePartyBtn").html("Update");
		$('#savePartyBankBtn').val("partyBankUpdate");
		   var row = partyBankTable.row($(this).closest("tr").get(0));
		   var rowData=row.data();  
		   $("#partyBankForm").append('<input type="hidden" name="id" id="partyBankId"/>');
		   $("#partyBankId").val(rowData.id);
		   $("#bankName").val(rowData.bank_name);
		   $("#branch").val(rowData.branch_name);
		   $("#ifsc").val(rowData.ifsc);
		   $("#accountNo").val(rowData.account_no);
	});	   

		//code to delete the item row.
		$(document).on("click",".deleteButton",function(){
			var row = partyBankTable.row($(this).closest("tr").get(0));
			   var rowData=row.data();
			   var id = rowData.id;
		   bootbox.confirm({
			   message: "Do you want to delete the bank details?",
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
	   

})

function deleteAltAddress(id){
	$.ajax({
		type : "POST",  
		url : api.DELETE_PARTY_BANK_DETAILS  +"?id="+id,
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

