/**
 * 
 */
$(document).ready( function () {
	//alert(PARTY_LIST);
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

$.ajax({
    Type:'GET',
    url : api.PURCHASEORDER_LIST,
    dataType:'json',
    async: 'false',
    success  : function(response){
    	$.each(response, function( key, value ) {
    		  $('#partyDropDown').append('<option value='+value.id+'>'+value.partyName+'</option>'); 
    		});
    	//get party 
		var purchaseId = $('#partyDropDown').val()
		$('#party').val(purchaseId);
    }
  }); 

});