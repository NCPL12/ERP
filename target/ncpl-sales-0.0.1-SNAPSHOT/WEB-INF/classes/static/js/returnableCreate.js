$(document).ready(function(){
  // populate DC dropdown — dcList is injected as JSON from controller
  if(typeof dcList !== 'undefined' && dcList){
    $.each(dcList, function(i, dc){
      var label = 'DC ' + dc.dcId + (dc.clientName ? ' - ' + dc.clientName : '') + (dc.soNumber ? ' ('+dc.soNumber+')' : '');
      $('#dcDropdown').append('<option value="'+dc.dcId+'">'+label+'</option>');
    });
  }
  $('#dcDropdown').select2({dropdownAutoWidth:true, width:'100%'});
  if(typeof preselectedDcId !== 'undefined' && preselectedDcId){
    $('#dcDropdown').val(String(preselectedDcId)).trigger('change');
  }
  // if preselected came via ?dcId query param fallback
  var qsDcId = new URLSearchParams(window.location.search).get('dcId');
  if(qsDcId && !$('#dcDropdown').val()){ $('#dcDropdown').val(qsDcId).trigger('change'); }
});

$(document).on('change', '#dcDropdown', function(){
  var dcId = $(this).val();
  $('#dcId').val(dcId);
  $('#returnableTable tbody').empty();
  $('#rcClientName').text('-'); $('#rcSoNumber').text('-');
  if(!dcId) return;
  $.ajax({
    type:'GET', url: api.GET_DC_ITEMLIST_BYDCID + '?id=' + dcId, dataType:'json',
    success: function(resp){
      // rc header from dcList entry if available
      var dc = (typeof dcList!=='undefined' && dcList) ? dcList.find(function(d){ return String(d.dcId)===String(dcId); }) : null;
      if(dc){ $('#rcClientName').text(dc.clientName||'-'); $('#rcSoNumber').text(dc.soNumber||'-'); }
      $.each(resp, function(index, value){
        var row = '<tr>'
          + "<td width='5%'>"+ (value.serialNo || value.slNo || (index+1)) +"</td>"
          + "<td width='35%' style='word-break:break-word'>"+ (value.description||'') +"</td>"
          + "<td width='12%'>"+ (value.soModelNo||'') +"</td>"
          + "<td width='8%'>"+ (value.unit||'') +"</td>"
          + "<td width='7%' class='totalQty'>"+ (value.totalQuantity!=null?value.totalQuantity:'') +"</td>"
          + "<td width='7%' class='deliveredQty'>"+ (value.deliveredQuantity!=null?value.deliveredQuantity:'') +"</td>"
          + "<td width='7%' class='todaysQtyCell'>"+ (value.todaysQty!=null?value.todaysQty:'') +"</td>"
          + "<td width='12%'><input type='text' id='returnedQty"+index+"' name='items["+index+"].returnedQty' class='form-control form-control-sm returnedQty' /></td>"
          + "<td style='display:none'><input type='hidden' name='items["+index+"].dcItemId' value='"+value.dcItemId+"' /></td>"
          + "</tr>";
        $('#returnableTable tbody').append(row);
      });
    },
    error: function(xhr){ console.error('Failed to load DC items', xhr.status); $.error('Failed to load DC items'); }
  });
});

$(document).on('submit', '#returnableCreateForm', function(e){
  var dcId = $('#dcId').val();
  if(!dcId){ e.preventDefault(); $.error('Please select a DC'); return false; }
  var rowCount = $('#returnableTable tbody tr').length;
  if(rowCount===0){ e.preventDefault(); $.error('No items found for this DC'); return false; }
  var hasError=false;
  $('#returnableTable tbody tr').each(function(index){
    var $tr=$(this);
    var returnedStr = $tr.find('input.returnedQty').val();
    if(returnedStr==null) returnedStr='';
    returnedStr=returnedStr.trim();
    if(returnedStr===''){ $tr.find('input.returnedQty').val(0); return; }
    var returnedQty=parseFloat(returnedStr);
    var todaysQty=parseFloat($tr.find('td.todaysQtyCell').text())||0;
    var totalQty=parseFloat($tr.find('td.totalQty').text())||0;
    if(isNaN(returnedQty) || returnedQty<0){ e.preventDefault(); $.error('Invalid returned qty at row '+(index+1)); hasError=true; return false; }
    if(returnedQty>todaysQty){ e.preventDefault(); $.error('Returned qty > Today\'s qty at row '+(index+1)+' (DC 9520 todays=1, you can return max 1)'); hasError=true; return false; }
    if(returnedQty>totalQty){ e.preventDefault(); $.error('Returned qty > Total qty at row '+(index+1)); hasError=true; return false; }
  });
  if(hasError) return false;
  $('#saveReturnableBtn').attr('disabled','disabled');
});
