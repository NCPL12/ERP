/**
 * 
 */
var isDirty=false;
$(document).ready( function () {
	$('input:not(:button,:submit),textarea,select').change(function () {
		isDirty = true;
		$("#saveDeliveryChallan").attr('disabled', false);
		$("#saveInvoice").attr('disabled', false);
		$("#savePurchaseOrder").attr('disabled', false);
		$("#saveGrn").attr('disabled', false);
		$("#savePartyBtn").attr('disabled', false);
		$("#partyBankSaveAndExitBtn").attr('disabled', false);
		$("#savePartyBtn").attr('disabled', false);
		$("#saveAndExit").attr('disabled', false);
		$("#savePartyAndExitBtn").attr('disabled', false);
		$("#saveDesign").attr('disabled', false);
		$("#saveWorkOrderBtn").attr('disabled', false);
		$("#savereturnedItemsBtn").attr('disabled', false);
		});
	
	$(document).on("click",'input:not(:button,:submit),textarea,select',function(){
		$("#saveDesign").attr('disabled', false);
	})
	
	$(document).on("click",".purchaseDeleteButton",function(){
		$("#savePurchaseOrder").attr('disabled', false);
	})
	
	$('#resetSalesOrder,#cncl,button[type=reset]').on("click",function () {
		isDirty = false;
		
		});
	$('.modal').on('hidden.bs.modal', function () {
		isDirty = false;
		})
	$('.modal').on('show.bs.modal', function () {
		$(".buttonDismiss").attr('data-dismiss','modal');
		isDirty = false;
		})
	$('input[type=search]').change(function () {
		isDirty = false;
	
	});

	$(".nav-link").on("click", function(e){
		if (isDirty == true) {
			e.preventDefault();
			bootbox.confirm({
				message: "The current changes are not saved. Are you sure to leave this page without save?",
				buttons: {
					confirm: {
						label: 'Confirm'
					},
					cancel: {
						label: 'Cancel'
					}
					
				},
				callback: function (result) {
					if(result){
						var navigationLink=e.currentTarget.href;
						window.location.assign(navigationLink);
					}else{
						e.preventDefault();
					}
				
				}
			});
		}
		
	});
	$(".buttonDismiss").on("click", function(e){
		if (isDirty == true) {
			e.preventDefault();
			$(this).removeAttr('data-dismiss');
			bootbox.confirm({
				message: "The current changes are not saved. Are you sure to leave this page without save?",
				buttons: {
					confirm: {
						label: 'Confirm'
					},
					cancel: {
						label: 'Cancel'
					}
					
				},
				callback: function (result) {
					if(result){
						$(this).attr('data-dismiss','modal');
						$(".modal").modal('hide');
					}else{
						$(this).removeAttr('data-dismiss');
					}
				
				}
			});
		}
		
	});
});
