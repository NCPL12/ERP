var table;
var popreviewitemstable;
var vendorState;
$(document).ready( function () {
	
	var id ="sunil";
	    // Setup - add a text input to each footer cell
		$('#purchaseList thead tr').clone(true).appendTo( '#purchaseList thead' );
	    $('#purchaseList thead tr:eq(1) th').each( function (i) {
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
    table= $('#purchaseList').DataTable({
    	//"iDisplayLength": -1,
    	'columnDefs': [ {
    	    'targets': [0,1,2,3,4,5,6,7], /* table column index */
    	    'orderable': false, /* here set the true or false */
    	 }],
    	    "aaSorting": [[ 4, "desc" ]],
    	orderCellsTop: true,
	    fixedHeader: true,
	    
    	"aaData": dataObj,
    	
    	"aoColumns": [ {
			"mData" : "poNumber",
		}, {
			"mData" : "party",

			render : function(aaData, type, row) {
				var company;
				if (aaData == null) {
					company = "";
				} else {
					company = aaData.partyName;
				
				}
				return company.length > 35 ?
				company.substr( 0, 35 ) +'...' :
				company;

				//return row.salesOrder.party.partyName;

			}

		}, {
			"mData" : "party",

			 "defaultContent":"NA",
				render : function(aaData, type, row) {
					//return "";
					return row.party.party_city.name;
					
				}
		}, {
			"mData" : "grandTotal",
			"defaultContent":"NA"
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
			
		},{
			"mData" : "version",
			"visible": false,
		},
		
		
		{
			"mData" : "pdf"	,
			render : function(datam, type, row) {
				var url = null;
					url ="/ncpl-sales/purchaseOrder/details/"+row.poNumber;
				return "<button type='button' id='"+row.poNumber+"' class='btn btn-default btn-flat btn-xs btnGeneratePo'><i class='fa fa-folder'></i> Generate PO</button>";				
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
    var purchaseOrderId;
    var  version;
    //On double click of row navigate to edit page
   $('#purchaseList tbody').on('dblclick', 'tr', function () {
	   var data1 = table.row(this).data();
	   var poNumber = data1.poNumber;
	    purchaseOrderId = data1.poNumber;
	    version = data1.version;
	    var versionIndex = version;
	   getPurchasecopy(poNumber,purchaseOrderId,version,versionIndex);
	   //getPurchasecopyItems(poNumber);
	  // getVendorAddressForPreview(poNumber);
	  // 	 $("#poPreviewModal").modal("show");
	//   $("#pono").html(purchaseOrderId);
	  /* var data1 = table.row(this).data();
	   var purchaseOrderId = data1.poNumber;
	   var version = data1.version;
	   $('#poNumber').val(purchaseOrderId);
	   window.location = pageContext+"/purchase/view?poNumber="+purchaseOrderId+"&&version="+version;*/
	});
  
   if(role=="STORE USER"){
	   $(".addPO").on("click", function (event) {
		    event.preventDefault();
		});
		
	}
   if(user=="admin"){
		$(document).on('click',".poUploadButton",function(){
			$("#poUploadForm")[0].reset();
			$('#response').empty();
			$("#poUploadModal").modal('show');
		});
	}else{
		
		 $(".poUploadButton").on("click", function (event) {
			    event.preventDefault();
			});
			
	}
   
   /*  $(document).on("click","#generatePrevPoBtn",function(){
	   $('#generatePreviousPurchaseOrder').attr("href", "/ncpl-sales/prevpurchaseOrder/file/"+purchaseOrderId);
});
  
   if(poExist == false){
	   $.error("PO Not generated Yet");
   }*/
   $("#purchaseList").on("click", ".btnGeneratePo", function(){
	   var poNumber = this.id;
	   checkPoGenerated(poNumber);
	   $("#quoteRefNoInput").val("");
		$("#quoteDateInput").val("");
		$("#modeOfPaymentDropdown").val("");
	});
//   $(".btnGeneratePo").click(function (){
//	   var poNumber = this.id;
//	   getAddresses(poNumber);
//	  
//   })
   
   
   function checkPoGenerated(poNumber){
	   $.ajax({
			type:'GET',
			url : api.PO_PURCHASECOPY  + poNumber,
			success : function(response) {
				if(response == undefined || response.poNumber == undefined || response ==""){
					
					 getAddresses(poNumber);
				}else{
					
					 getAddresses(poNumber);
					if(response.quoteRefNo==null || response.quoteRefNo==""){
						$("#quoteRefNoInput").val("");
					}else{
						$("#quoteRefNoInput").val(response.quoteRefNo);
					}
					if(response.quoteDate==null  || response.quoteDate==""){
						$("#quoteDateInput").val("");
					}else{
						$("#quoteDateInput").val(response.quoteDate.replaceAll("/","-"));
					}
					
					$("#modeOfPaymentDropdown").val(response.modeOfPayment.split(":")[1].trim());
				
				
				
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
   
   $("#vendorAddressDropdown").change(function (){
	  
	  
	   var addressId = $(this).val();
	   $("#vendorAddressContent").empty();
	   //Party main address
	   if(addressId == addressObj.vendor.id) {
		   vendorState = addressObj.vendor.party_city.state.name;
		var addr2=addressObj.vendor.addr2;
		if(addr2==null || addr2 == undefined) {
			addr2=' ';
			}
		$("#vendorAddressContent").append( 
		          			"<span class=''>"+addressObj.vendor.partyName+" ,</span>" +
		          			"<span class=''>"+addressObj.vendor.addr1+" ,</span>" +
		          			"<span class=''>"+addr2+"</span>" +
		          			"<span class=''>"+addressObj.vendor.party_city.name+" ,</span>");
	   }else {
		   var alternateAddressArr = addressObj.addresses; //Party alternate address
	          for(var i=0; i< alternateAddressArr.length; i++){
	        	  if(addressId == alternateAddressArr[i].id) {
	        		  vendorState = alternateAddressArr[i].partyaddr_city.state.name;
	        		  var addr2=alternateAddressArr[i].addr2;
	        			if(addr2==null || addr2 == undefined) {
	        				addr2=' ';
	        				}
	        	  $("#vendorAddressContent").append( 
		          			"<span class=''>"+addressObj.vendor.partyName+" ,</span>" +
		          			"<span class=''>"+alternateAddressArr[i].addr1+" ,</span>" +
		          			"<span class=''>"+alternateAddressArr[i].addr2+" ,</span>" +
		          			"<span class=''>"+alternateAddressArr[i].partyaddr_city.name+"</span>");
	        	  }
	          }
	   }
	   setGstRegion();
	});

   
   $("#shippingAddressDropdown").change(function (){
	   var addressId = $(this).val();
	   $("#shippingAddressContent").empty();
	   if(addressId.includes("key")){
		   
		   var shippingAddress = addressObj.shippingAddress;
	       var shippingAddressArr = shippingAddress.split('^^');
	       for(var i=0; i< shippingAddressArr.length; i++){
	    	 var shippingAddrArr = shippingAddressArr[i].split('$$');
	    	 if(addressId == shippingAddrArr[0]){
	     	 $("#shippingAddressContent").append( 
	       			"<span class=''>"+shippingAddrArr[1]+" ,</span>" +
	       			"<span class=''>"+shippingAddrArr[2]+" ,</span>" +
	       			"<span class=''>"+shippingAddrArr[3]+" ,</span>" +
	       			"<span class=''>"+shippingAddrArr[4]+" ,</span>" +
	       			"<span class=''>"+shippingAddrArr[7]+" </span>");
	    	 }
	       }
			
	   }else{
		   for(var i=0; i< addressObj.alternateShippingAdrress.length; i++){
			   if(addressId==addressObj.alternateShippingAdrress[i].id){
			   var addr2=addressObj.alternateShippingAdrress[i].addr2;
				if(addr2==null || addr2 == undefined) {
					addr2=' ';
					}
				$("#shippingAddressContent").append( 
				          			"<span class=''>"+addressObj.alternateShippingAdrress[i].partyName+" ,</span>" +
				          			"<span class=''>"+addressObj.alternateShippingAdrress[i].addr1+" ,</span>" +
				          			"<span class=''>"+addr2+"</span>" +
				          			"<span class=''>"+addressObj.alternateShippingAdrress[i].party_city.name+" ,</span>");
   				}
		   }
	   }
		
	  
	});
   
   $("#billingAddressDropdown").change(function (){
	   var addressId = $(this).val();
	   $("#billingAddressContent").empty();
	   
	   var billingAddress = addressObj.billingAddress;
       var billingAddressArr = billingAddress.split('^^');
       for(var i=0; i< billingAddressArr.length; i++){
    	 var billingAddrArr = billingAddressArr[i].split('$$');
    	 if(addressId == billingAddrArr[0]){
     	 $("#billingAddressContent").append( 
       			"<span class=''>"+billingAddrArr[1]+" ,</span>" +
       			"<span class=''>"+billingAddrArr[2]+" ,</span>" +
       			"<span class=''>"+billingAddrArr[3]+" ,</span>" +
       			"<span class=''>"+billingAddrArr[4]+" ,</span>" +
       			"<span class=''>"+billingAddrArr[7]+" ,</span>" +
       			"<span class='billingState'>"+billingAddrArr[5]+" </span>");
    	 }
       }
		
	  
	});
   
   $(document).on("click","#generatePoBtn",function(e){
	   if( $("#modeOfPaymentDropdown").val()==""){
		   $.error("Please select mode of payment");
		   e.preventDefault();
	   }else{
		sendingPoAddress();
		$("#poAddressSlectionPopup").modal("hide");
		window.location.reload();
	   }
		
});
   $(document).on("click","#generatePoPdfBtn",function(e){
	   if( $("#modeOfPaymentDropdown").val()==""){
		   $.error("Please select mode of payment");
		   e.preventDefault();
	   }else{
		sendingPoPdfAddress();
		$("#poAddressSlectionPopup").modal("hide");
		window.location.reload();
	   }
		
});
   $( "#quoteDateInput" ).datepicker({ dateFormat: 'dd-mm-yy' });
} );  


var addressObj;
//ajax call to get the list of stocks
function getAddresses(poNumber){
    $.ajax({
       url:api.PO_ADDRESSES + poNumber,
       type:'GET',
       success:function(response) {
    	   addressObj = response;
          //set po number on popup header
          $('#poNumberOnBillingPopup').html(poNumber);
          
          //Populate vendor drop down
          $("#vendorAddressDropdown").empty();
          $("#vendorAddressDropdown").append(new Option(response.vendor.addr1, response.vendor.id));
          var alternateAddressArr = response.addresses;
          for(var i=0; i< alternateAddressArr.length; i++){
        	  $("#vendorAddressDropdown").append(new Option(alternateAddressArr[i].addr1, alternateAddressArr[i].id));
          }
          //Display auto selected address
          $("#vendorAddressDropdown").change();
          
          //Populate shipping drop down
          $("#shippingAddressDropdown").empty();
          var shippingAddress = response.shippingAddress;
          var alternateShippingAdrress = response.alternateShippingAdrress;
          var shippingAddressArr = shippingAddress.split('^^');
          for(var i=0; i< shippingAddressArr.length; i++){
        	  var shippingAddrArr = shippingAddressArr[i].split('$$');
        	  $("#shippingAddressDropdown").append(new Option(shippingAddrArr[2], shippingAddrArr[0]));
          }
          if(alternateShippingAdrress!=[]){
        	  for(var i=0; i< alternateShippingAdrress.length; i++){
        		  $("#shippingAddressDropdown").append(new Option(alternateShippingAdrress[i].addr1, alternateShippingAdrress[i].id));
        	  }
          }
          $("#shippingAddressDropdown").change();
          
          //Populate billin drop down
          $("#billingAddressDropdown").empty();
          var billingAddress = response.billingAddress;
          var billingAddressArr = billingAddress.split('^^');
          for(var i=0; i< billingAddressArr.length; i++){
        	  var billingAddrArr = billingAddressArr[i].split('$$');
        	  $("#billingAddressDropdown").append(new Option(billingAddrArr[2], billingAddrArr[0]));
          }
          $("#billingAddressDropdown").change();
          
          //Populate mode of payment drop downs
          var modeOfPaymentArr = response.modeOfPayment.split('$$');
          /*$("#modeOfPaymentDropdown").empty();
          var str =  modeOfPaymentArr[modeOfPaymentArr.length -1 ]; //This contains "days PDC from the date of delivery"
          for(var i=0; i< modeOfPaymentArr.length-1; i++){
        	  if(i==0) {
        		  $("#modeOfPaymentDropdown").append(new Option(modeOfPaymentArr[i], modeOfPaymentArr[i]));
        	  }else{
        		  $("#modeOfPaymentDropdown").append(new Option(modeOfPaymentArr[i]+' '+str, modeOfPaymentArr[i]+' '+str));
        	  }
        	 
          }*/
        
          //Populate jurisdictionDropdown drop downs
          var jursidictionArr = response.jurisdiction.split('$$');
          $("#jurisdictionDropdown").empty();
          for(var i=0; i< jursidictionArr.length; i++){
        	  $("#jurisdictionDropdown").append(new Option(jursidictionArr[i], jursidictionArr[i]));
          }
          
          //Populate frieght drop downs
          var frieghtArr = response.frieght.split('$$');
          $("#freightDropdown").empty();
          for(var i=0; i< frieghtArr.length; i++){
        	  $("#freightDropdown").append(new Option(frieghtArr[i], frieghtArr[i]));
          }
          
        //Populate delivery drop downs
          var deliveryArr = response.delivery.split('$$');
          $("#deliveryDropdown").empty();
          for(var i=0; i< deliveryArr.length -1 ; i++){
        	  var val;
        	  if(i  == 0 ){
        		  val = deliveryArr[i];
        	    
        	  }else {
        		  val = deliveryArr[i]+' '+deliveryArr[deliveryArr.length -1];
        	  }
        	  $("#deliveryDropdown").append(new Option(val, val));
          }
          
        //Populate warranty drop downs
          var warrantyArr = response.warranty.split('$$');
          var strMonths =  warrantyArr[warrantyArr.length -1 ]; //This contains "months"
          $("#warrantyDropdown").empty();
          for(var i=0; i< warrantyArr.length -1; i++){
        	  $("#warrantyDropdown").append(new Option(warrantyArr[i]+' '+strMonths, warrantyArr[i]+' '+strMonths));
          }
          
          
          setGstRegion();
          
          //Open popup jurisdictionDropdown
   	   		$('#poAddressSlectionPopup').modal({
   	   			'show': true
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
function setGstRegion(){
	var billingState = $('.billingState').html();
	
	   if((billingState!= undefined) && (vendorState.trim() == billingState.trim())){
		   $('#gstRegion').html("State / Center GST");
	   }else {
		   $('#gstRegion').html("Interstate GST");
	   }
   
}
function sendingPoAddress(){
	var gstRegion = $("#gstRegion").text();
	gstRegion = gstRegion.replace('/', '$');
	var poNumber = $("#poNumberOnBillingPopup").text();
	var vendorAddressId = $("#generatePoBtn").parents("div").find("#vendorAddressDropdown").val();
	var shippingAddressId =  $("#generatePoBtn").parents("div").find("#shippingAddressDropdown").val();
	var billingAddressId = $("#generatePoBtn").parents("div").find("#billingAddressDropdown").val();
	var modeOfPayment = $("#generatePoBtn").parents("div").find("#modeOfPaymentDropdown").val();
	var jurisdiction = $("#generatePoBtn").parents("div").find("#jurisdictionDropdown").val();
	var freight = $("#generatePoBtn").parents("div").find("#freightDropdown").val();
	var delivery = $("#generatePoBtn").parents("div").find("#deliveryDropdown").val();
	var warranty = $("#generatePoBtn").parents("div").find("#warrantyDropdown").val();
	var quoteRefNo = $("#generatePoBtn").parents("div").find("#quoteRefNoInput").val();
	var quoteDate = $("#generatePoBtn").parents("div").find("#quoteDateInput").val();
	var mop=modeOfPayment.replaceAll("%","$").replaceAll("/","|");
	if(vendorAddressId.indexOf('/') > -1) {
		vendorAddressId = vendorAddressId.replace('/', '$');
	}
	if(shippingAddressId.indexOf('/') > -1) {
		shippingAddressId = shippingAddressId.replace('/', '$');
	}
	var jsonObj = '{"poNumber": "'+poNumber+'" ,"gstRegion":"'+gstRegion+'","vendorAddressId":"'+vendorAddressId+'","shippingAddressId":"'+shippingAddressId+'","billingAddressId":"'+billingAddressId+'","modeOfPayment":"'+mop+'","jurisdiction":"'+jurisdiction+'","freight":"'+freight+'","delivery":"'+delivery+'","warranty":"'+warranty+'","quoteRefNo":"'+quoteRefNo+'","quoteDate":"'+quoteDate+'"}';
	JSON.stringify(jsonObj);
	$('#generatePurchaseOrder').attr("href", "/ncpl-sales/purchaseOrder/details/"+jsonObj);
	
}
function sendingPoPdfAddress(){
	var gstRegion = $("#gstRegion").text();
	gstRegion = gstRegion.replace('/', '$');
	var poNumber = $("#poNumberOnBillingPopup").text();
	var vendorAddressId = $("#generatePoBtn").parents("div").find("#vendorAddressDropdown").val();
	var shippingAddressId =  $("#generatePoBtn").parents("div").find("#shippingAddressDropdown").val();
	var billingAddressId = $("#generatePoBtn").parents("div").find("#billingAddressDropdown").val();
	var modeOfPayment = $("#generatePoBtn").parents("div").find("#modeOfPaymentDropdown").val();
	var jurisdiction = $("#generatePoBtn").parents("div").find("#jurisdictionDropdown").val();
	var freight = $("#generatePoBtn").parents("div").find("#freightDropdown").val();
	var delivery = $("#generatePoBtn").parents("div").find("#deliveryDropdown").val();
	var warranty = $("#generatePoBtn").parents("div").find("#warrantyDropdown").val();
	var quoteRefNo = $("#generatePoBtn").parents("div").find("#quoteRefNoInput").val();
	var quoteDate = $("#generatePoBtn").parents("div").find("#quoteDateInput").val();
	var mop=modeOfPayment.replaceAll("%","$").replaceAll("/","|");
	
	if(vendorAddressId.indexOf('/') > -1) {
		vendorAddressId = vendorAddressId.replace('/', '$');
	}
	if(shippingAddressId.indexOf('/') > -1) {
		shippingAddressId = shippingAddressId.replace('/', '$');
	}
	
	var jsonObj = '{"poNumber": "'+poNumber+'" ,"gstRegion":"'+gstRegion+'","vendorAddressId":"'+vendorAddressId+'","shippingAddressId":"'+shippingAddressId+'","billingAddressId":"'+billingAddressId+'","modeOfPayment":"'+mop+'","jurisdiction":"'+jurisdiction+'","freight":"'+freight+'","delivery":"'+delivery+'","warranty":"'+warranty+'","quoteRefNo":"'+quoteRefNo+'","quoteDate":"'+quoteDate+'"}';
	JSON.stringify(jsonObj);
	$('#generatePurchaseOrder').attr("href", "/ncpl-sales/purchaseOrder_pdf/details/"+jsonObj);
	
}


function getPurchasecopy(poNumber,purchaseOrderId,version,versionIndex){
	$.ajax({
		type:'GET',
		url : api.PO_PURCHASECOPY  + poNumber,
		success : function(response) {
			if(response == undefined || response.poNumber == undefined || response ==""){
				 window.location = pageContext+"/purchase/view?poNumber="+purchaseOrderId+"&version="+version+"&versionIndex="+versionIndex;
			}else{
			$("#PonoInPreview").html("PO No: "+response.poNumber);
			$("#podateInPreview").html("Po Date: "+response.created);
			//$("#contactpersonInPreview").html(response.contactPerson);
			//$("#contactnumberInPreview").html(response.contactNumber);
			//$("#emailInPreview").html(response.email);
			if(response.quoteRefNo==null){
				$("#quoteRefNoInPreview").html("Quote Ref No.: ");
			}else{
				$("#quoteRefNoInPreview").html("Quote Ref No.: "+response.quoteRefNo);
			}
			if(response.quoteDate==null){
				$("#quoteDateInPreview").html("Quote Date: ");
			}else{
				$("#quoteDateInPreview").html("Quote Date: "+response.quoteDate);
			}
			
			$("#billingaddressinPreview").html(response.billingAddress+"<br>"+"PAN : AADCN5426F"
					+"<br>"+"GSTIN : 29AADCN5426F1ZG");
			if(response.deliveryAddress.includes("Email")){
				$("#deliveryaddressinPreview").html(response.deliveryAddress);
			}else{
			$("#deliveryaddressinPreview").html(response.deliveryAddress+"<br>"+response.contactPerson
					+"<br>"+response.contactNumber);
			}
			$("#vendorAddressinPreview").html(response.vendorAddress);
			$("#totalInPreview").html(commaSeparateNumber(response.total));
			$("#grandtotalInPreview").html(commaSeparateNumber(response.grandTotal));
			$("#sgstInPreview").html(commaSeparateNumber(response.gst));
			//$("#cgstInPreview").html(commaSeparateNumber(response.cgst));
			$("#amountInwordsPreview").html(response.amountinwords);
			$("#deliverypreview").html(response.deliveyTerm);
			$("#warrantypreview").html(response.warranty);
			$("#paymentpreview").html(response.modeOfPayment);
			$("#taxpreview").html(response.taxesTerm);
			$("#jurisdictionpreview").html(response.restrictions);
			
			getPurchasecopyItems(poNumber);
			getCurrentVersion(poNumber);
			// $("#poPreviewModal").modal("show");
			
			
			
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


var itemsList;
function getPurchasecopyItems(poNumber){
	var className="po";
	//Sending class name to differentiate between api called in grn and po..
	$.ajax({
		type:'GET',
		url : api.PURCHASE_LIST_BY_POID  + "?id="+poNumber+ "&&className="+className,
		success : function(response) {
			$("#poPreviewModal").modal("show");
			itemsList = response;
			var slno=0;
			//version=response[0].purchaseOrder.version;
			//purchaseOrderId=response[0].purchaseOrder.poNumber;
			popreviewitemstable= $('#purchaseprevieItems').DataTable({
			    	orderCellsTop: true,
				    fixedHeader: true,
				    "bDestroy": true,
				    paging: false,
				    searching: false,
				    dom: 't',
			    	"aaData": itemsList,
			    	"aoColumns": [ {
						"mData" : "poNumber",
						"defaultContent":"NA",
						render : function(aaData, type, row) {
							//return "";
							slno++;
							return slno;
							
							
						}
						
					}, {
						"mData" : "poDescription",
						"width": "320px",
			    		
					}, {
						"mData" : "modelNo",

					}, {
						"mData" : "hsnCode",
						
					},{
						"defaultContent":"18%",
					},{
						"mData" : "quantity",
					},{
						"mData" : "modelNo",
						render: function (mData, type, row, meta) {
							var unitName = null;
							$.each(itemList, function (index, value) {
								if (mData.trim() == value.model.trim()) {
									unitName = value.item_units.name;
								}
							});
							return unitName;
						}
					},
				   {
						"mData" : "unitPrice",
						render : function(aaData, type, row) {
							//return "";
							var up = commaSeparateNumber(row.unitPrice);
							return up;
							
							
						}
					},{
						"mData" : "amount",
						render : function(aaData, type, row) {
							//return "";
							var ap = commaSeparateNumber(row.amount);
							return ap;
							
							
						}
					}
					]
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

function getCurrentVersion(poNumber){
	$.ajax({
		method :'GET',
		url:api.GET_PO_BY_PONUMBER + "?poNumber=" + poNumber,
		success:function(response){
			console.log(response);
			version=response.version;
			purchaseOrderId=poNumber;
			
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

$(document).on("click","#editPoBtn",function(){
	   var versionIndex = version;
	   window.location = pageContext+"/purchase/view?poNumber="+purchaseOrderId+"&version="+version+"&versionIndex="+versionIndex;
});



$(document).on('click',".archiveCheckbox",function(){
	var poNum=$(this).closest("tr").find("td:eq(0)").text();
	const isChecked = $(this).is(":checked");
		if(isChecked==true) {
		bootbox.confirm({
			message: "Do you want to archive this PO?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? archivePurchaseOrder(poNum):window.location.reload();;
			}
		});	
	}else{
		bootbox.confirm({
			message: "Do you want to remove this PO from archive?",
			buttons: {
				cancel: {
					label: 'Cancel'
				},
				confirm: {
					label: 'Confirm'
				}
			},
			callback: function (result) {
				result ? unArchivePurchaseOrder(poNum):window.location.reload();;
			}
		});	
	}
});

function archivePurchaseOrder(poNum){
	$.ajax({
		type : "POST",  
		url : api.UPDATE_PO_ARCHIVE +"?poNum="+poNum,
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

function unArchivePurchaseOrder(poNum){
	$.ajax({
		type : "POST",  
		url : api.UPDATE_PO_UNARCHIVE +"?poNum="+poNum,
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



$(document).on('submit',"#poUploadForm", function (e) {
    e.preventDefault(); // Prevent form submission

    let formData = new FormData();
    formData.append('file', $('#file')[0].files[0]);

    $.ajax({
        url: api.PURCHASE_UPLOAD,
        type: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
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

//Separating values by comma
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
