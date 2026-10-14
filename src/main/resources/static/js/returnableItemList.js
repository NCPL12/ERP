/**
 *
 */
// @D0014 lazy-loaded, paginated Returnables list (see README.md)
var returnableTable;

$(document).ready( function () {
	returnableTable = $('#returnableItemsList').DataTable({
		processing: true,
		serverSide: true,
		autoWidth: false,
		responsive: false,
		scrollX: false,
		pageLength: 50,
		lengthMenu: [[50, 100, 250, 500], [50, 100, 250, 500]],
		"order": [],
		ajax: function (data, callback, settings) {
			var page = Math.floor(data.start / data.length);
			$.ajax({
				url: pageContext + "/returnableList/data",
				data: {
					page: page,
					size: data.length,
					keyword: data.search && data.search.value ? data.search.value : ""
				},
				success: function (resp) {
					callback({
						draw: data.draw,
						recordsTotal: resp.totalElements,
						recordsFiltered: resp.totalElements,
						data: resp.content
					});
				},
				error: function (xhr) {
					console.error("Failed to load returnable items list", xhr.status, xhr.responseText);
					callback({ draw: data.draw, recordsTotal: 0, recordsFiltered: 0, data: [] });
				}
			});
		},
    	"aoColumns": [
		{ "title":"Returnable No", "data":"returnableId", "width":"8%", "defaultContent":"", "className":"text-center" },
		{ "title":"SO Number", "data":"soNumber", "width":"14%", "defaultContent":"",
		  render: function(d,type,row){ if(!d) return ""; if(type==='display' && d.length>20) return '<span title="'+d+'">'+d.substr(0,20)+'...</span>'; return d; }
		},
		{ "title":"Client Name", "data":"clientName", "width":"19%", "defaultContent":"",
		  render: function(d,type,row){ if(!d) return ""; if(type==='display' && d.length>28) return '<span title="'+d+'">'+d.substr(0,28)+'...</span>'; return d; }
		},
		{ "title":"Client PO", "data":"clientPo", "width":"14%", "defaultContent":"",
		  render: function(d,type,row){ if(!d) return ""; if(type==='display' && d.length>22) return '<span title="'+d+'">'+d.substr(0,22)+'...</span>'; return d; }
		},
		{ "title":"Shipping Address", "data":"shippingAddress", "width":"21%", "defaultContent":"",
		  render: function(d,type,row){ if(!d) return ""; if(type==='display' && d.length>34) return '<span title="'+d+'">'+d.substr(0,34)+'...</span>'; return d; }
		},
		{ "title":"DC No", "data":"dcNo", "width":"6%", "defaultContent":"", "className":"text-center" },
		{ "title":"Date", "data":"date", "width":"10%", "defaultContent":"", "className":"text-nowrap",
		  render: function(d){ if(!d) return ""; var dt=new Date(d); if(isNaN(dt.getTime())) return d; var dd=("0"+dt.getDate()).slice(-2); var m=dt.getMonth()+1; var yyyy=dt.getFullYear(); var hh=("0"+dt.getHours()).slice(-2); var mm=("0"+dt.getMinutes()).slice(-2); return dd+"-"+m+"-"+yyyy+" "+hh+":"+mm; }
		},
		{ "title":"Returned Qty", "data":"returnedQty", "width":"8%", "className":"text-center text-nowrap" }
    	]
    });

    // double-click → view page like DC: /api/dc/view?dcId= → /deliveryChallan (read-only)
    $('#returnableItemsList tbody').on('dblclick', 'tr', function(){
        var data = returnableTable.row(this).data();
        if(!data) return;
        var returnableId = data.returnable ? data.returnable.id : data.returnableId;
        if(!returnableId && data.returnableId) returnableId = data.returnableId;
        if(!returnableId) return;
        window.location = pageContext + '/api/returnable/view?returnableId=' + returnableId;
    });

    // Header/toolbar: put Create button on same line as Search, same responsive container (like Company Assets)
    var btnWrap = $("#returnableCreateBtnWrap").detach();
    btnWrap.css({ display:"block", padding:0, margin:0 }).show();
    var $filter = $("#returnableItemsList_wrapper .dataTables_filter");
    $filter.css({ display:"flex", "align-items":"center", "justify-content":"flex-end", gap:"16px", "flex-wrap":"wrap", width:"100%" }).prepend(btnWrap);
    // keep filter input compact
    $filter.find("label").css({ margin:0, "white-space":"nowrap" });

    // AdminLTE sidebar collapse/expand and window resize: recalc widths so table fills full content width (no blank on right)
    function adjustReturnableTable(){ if(returnableTable) { returnableTable.columns.adjust().draw(false); } }
    $(document).on('collapsed.lte.pushmenu expanded.lte.pushmenu collapsed.pushMenu expanded.pushMenu', function(){ setTimeout(adjustReturnableTable, 350); });
    $(window).on('resize', adjustReturnableTable);
    // also after initial draw
    returnableTable.on('draw', function(){ adjustReturnableTable(); });
} );
