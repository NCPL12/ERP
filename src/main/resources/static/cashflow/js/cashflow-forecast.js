(() => {
 let chart, granularity='weekly', savedRange, selectedPeriod, currentData, pipelineData, viewMode='cashflow', cashflowRange;
 const voucherCache=new Map(),voucherCacheTtl=5*60*1000;
 const $=id=>document.getElementById(id);
 const money=v=>new Intl.NumberFormat('en-IN',{style:'currency',currency:'INR',maximumFractionDigits:0}).format(Number(v||0));
 const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 const date=v=>v?new Date(v+'T00:00:00').toLocaleDateString('en-IN',{day:'2-digit',month:'2-digit',year:'numeric'}):'—';
 const weekLabel=r=>new Date(r.weekStart+'T00:00:00').toLocaleDateString('en-IN',{day:'numeric',month:'short'})+' – '+new Date(r.weekEnd+'T00:00:00').toLocaleDateString('en-IN',{day:'numeric',month:'short',year:'2-digit'});
 const dayLabel=r=>new Date(r.weekStart+'T00:00:00').toLocaleDateString('en-IN',{day:'numeric',month:'short',year:'2-digit'});
 const todayIso=()=>{const d=new Date();return d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0')};
 const isOverdue=b=>Boolean(b?.dueDate&&b.dueDate<todayIso());
 const isBacklogPeriod=p=>Boolean(p?.weekEnd&&p.weekEnd<todayIso());
 const hideDetail=()=>{$('forecastInvoicePanel').hidden=true;selectedPeriod=undefined};
 const hidePipelineDetail=()=>{$('pipelineDetailPanel').hidden=true};
 function summary(d){
  $('forecastReceivables').textContent=money(d.totalReceivables);$('forecastPayables').textContent=money(d.totalPayables);
  $('forecastOpening').textContent=d.openingBalance==null?'Not set':money(d.openingBalance);$('forecastNet').textContent=d.closingBalance==null?'Not set':money(d.closingBalance);
  const backlogBills=d.backlogBills||[];
  $('forecastBacklog').textContent=money(Number(d.backlogReceivables||0)+Number(d.backlogPayables||0));
  $('forecastBacklogCount').textContent=backlogBills.length+' overdue invoices · Receivable '+money(d.backlogReceivables)+' / Payable '+money(d.backlogPayables);
  $('forecastBacklogCard').disabled=!backlogBills.length;
  $('forecastReconciliation').textContent=d.openingBalance==null?'Set a dated opening cash/bank balance to show projected balances. Net invoice movement: '+money(d.finalCumulative):'Projected closing '+money(d.closingBalance)+' = opening '+money(d.openingBalance)+' + expected collections '+money(d.totalReceivables)+' − planned invoice payments '+money(d.totalPayables)+'. These are assumptions, not actual receipts/payments.';
  const n=Number(d.unscheduledCount||0);$('forecastWarning').hidden=!n;$('forecastWarning').textContent=n+' bills without a future expected/due date are excluded. Open overdue backlog to schedule them, or use Finance Plan for undated bills.';
 }
 function partyLines(period,datasetIndex){
  if(datasetIndex===2||period.openingOnly)return[];
  const parties=datasetIndex===0?period.receivableParties:period.payableParties;
  if(!parties?.length)return['No parties'];
  const lines=parties.slice(0,15).map(p=>p.name+' ('+p.billCount+') · '+money(p.amount));
  if(parties.length>15)lines.push('+ '+(parties.length-15)+' more parties');return lines;
 }
 function renderChart(d){
  const canvas=$('weeklyCashflowChart'),empty=$('forecastEmpty');if(chart)chart.destroy();
  if(!d.periods?.length){canvas.hidden=true;empty.hidden=false;empty.textContent='No outstanding Tally bills with due dates are available for this period.';return}
 canvas.hidden=false;empty.hidden=true;const label=granularity==='daily'?dayLabel:weekLabel;
  const plotted=[{weekStart:d.from,weekEnd:d.from,openingOnly:true,receivables:null,payables:null,closingBalance:d.openingBalance},...d.periods];
  chart=new Chart(canvas,{data:{labels:plotted.map(p=>p.openingOnly?'Opening':label(p)),datasets:[
   {type:'bar',label:'Expected collections',data:plotted.map(r=>r.receivables),backgroundColor:'#2563eb',borderRadius:5,order:2},
   {type:'bar',label:'Planned invoice payments',data:plotted.map(r=>r.payables),backgroundColor:'#f59e0b',borderRadius:5,order:2},
   {type:'line',label:'Projected closing balance',data:plotted.map(r=>r.closingBalance),borderColor:'#7c3aed',backgroundColor:'#7c3aed',borderWidth:3,tension:0,pointRadius:3,pointHoverRadius:6,order:1}
  ]},options:{responsive:true,maintainAspectRatio:false,interaction:{mode:'index',intersect:false},
   onClick:(_e,els)=>{if(!els.length)return;const p=plotted[els[0].index];if(!p||p.openingOnly)return;if(granularity==='daily')openInvoices(p);else if(isBacklogPeriod(p))openInvoices({...p,backlog:true});else openDaily(p)},
   onHover:(e,els)=>e.native.target.style.cursor=els.length?'pointer':'default',
   plugins:{legend:{position:'bottom'},tooltip:{callbacks:{label:c=>c.dataset.label+': '+money(c.raw),afterLabel:c=>partyLines(plotted[c.dataIndex],c.datasetIndex)}}},
   scales:{y:{ticks:{callback:money},title:{display:true,text:'Amount (₹)'}},x:{ticks:{maxRotation:0,autoSkip:true,maxTicksLimit:14},title:{display:true,text:granularity==='daily'?'Expected cash date':'Week of expected cash date'}}}}});
 }
 const pipelineStatusClass=s=>s==='COMPLETE'?'status-complete':s==='CLIENT_INVOICE_MISSING'?'status-client':s==='REVIEW_REQUIRED'?'status-review':'status-vendor';
 const pipelineWeekLabel=p=>weekLabel(p);
 const hidePipelineHoverSoon=()=>setTimeout(()=>{const card=$('pipelineHoverCard');if(!card.matches(':hover'))card.hidden=true},80);
 function renderPipelineHover(period,event){
  const card=$('pipelineHoverCard'),records=period?.records||[];if(!records.length){card.hidden=true;return}
  card.innerHTML='<h3>'+esc(pipelineWeekLabel(period))+' · '+records.length+' record'+(records.length===1?'':'s')+'</h3>'+records.map(r=>'<div class="pipeline-hover-row"><strong>'+esc(r.vendorName||'Unknown vendor')+'</strong><span>'+esc(r.vendorInvoiceNumber||'No invoice number')+' · '+esc(r.poNumber||'No PO')+' · '+esc(r.soNumber||'No SO')+'</span><br><span>'+esc(r.statusLabel)+' · '+Number(r.pendingDays||0)+' days</span></div>').join('');
  card.hidden=false;
  const wrap=card.parentElement,margin=12,gap=18,x=Number(event?.x??event?.native?.offsetX??0),y=Number(event?.y??event?.native?.offsetY??0);
  const width=Math.min(390,Math.max(240,wrap.clientWidth-(margin*2)));card.style.width=width+'px';
  let left=x+gap;if(left+width>wrap.clientWidth-margin)left=x-width-gap;left=Math.max(margin,Math.min(left,wrap.clientWidth-width-margin));
  const height=Math.min(card.scrollHeight,260);let top=y-(height/2);top=Math.max(margin,Math.min(top,wrap.clientHeight-height-margin));
  card.style.left=Math.round(left)+'px';card.style.top=Math.round(top)+'px';
 }
 function renderPipelineChart(d){
  const canvas=$('weeklyCashflowChart'),empty=$('forecastEmpty');if(chart)chart.destroy();$('pipelineHoverCard').hidden=true;
  if(!d.periods?.length){canvas.hidden=true;empty.hidden=false;empty.textContent='No GRN invoice-pipeline records are available for this period.';return}
  canvas.hidden=false;empty.hidden=true;
  chart=new Chart(canvas,{type:'bar',data:{labels:d.periods.map(pipelineWeekLabel),datasets:[
   {label:'Invoice number missing in ERP GRN',data:d.periods.map(p=>p.erpVendorInvoiceMissing),backgroundColor:'#e11d48',borderRadius:4},
   {label:'Vendor invoice not in Tally',data:d.periods.map(p=>p.vendorNotBooked),backgroundColor:'#dc2626',borderRadius:4},
   {label:'Client invoice missing',data:d.periods.map(p=>p.clientInvoiceMissing),backgroundColor:'#f59e0b',borderRadius:4},
   {label:'Needs review',data:d.periods.map(p=>p.reviewRequired),backgroundColor:'#7c3aed',borderRadius:4},
   {label:'Linked history',data:d.periods.map(p=>p.complete),backgroundColor:'#10b981',borderRadius:4}
  ]},options:{responsive:true,maintainAspectRatio:false,interaction:{mode:'index',intersect:false},
   onHover:(e,els)=>{e.native.target.style.cursor=els.length?'pointer':'default';if(els.length)renderPipelineHover(d.periods[els[0].index],e);else hidePipelineHoverSoon()},
   onClick:(_e,els)=>{if(els.length)renderPipelineDetails(d.periods[els[0].index].records,'Invoice pipeline · '+pipelineWeekLabel(d.periods[els[0].index]))},
   plugins:{legend:{position:'bottom'},tooltip:{enabled:false}},
   scales:{x:{stacked:true,ticks:{maxRotation:0,autoSkip:true,maxTicksLimit:14},title:{display:true,text:'GRN / vendor-invoice week'}},y:{stacked:true,beginAtZero:true,ticks:{precision:0},title:{display:true,text:'Linked PO/SO records'}}}}});
 }
function pipelineSummary(d){
  $('pipelineErpInvoiceMissing').textContent=Number(d.erpVendorInvoiceMissingCount||0).toLocaleString('en-IN');$('pipelineVendorNotBooked').textContent=Number(d.tallyVendorNotBookedCount||0).toLocaleString('en-IN');$('pipelineClientMissing').textContent=Number(d.clientInvoiceMissingCount||0).toLocaleString('en-IN');$('pipelineReview').textContent=Number(d.reviewRequiredCount||0).toLocaleString('en-IN');$('pipelineComplete').textContent=Number(d.completeCount||0).toLocaleString('en-IN');
  $('forecastReconciliation').innerHTML=d.tallyAvailable===false
   ?'<strong><i class="fas fa-triangle-exclamation me-1"></i>ERP-only view</strong> '+Number(d.records?.length||0)+' GRN-to-SO records loaded. Tally invoice matching will resume when Tally reconnects.'
   :'<strong><i class="fas fa-link me-1"></i>'+Number(d.tallyPurchaseVoucherCount||0)+' Tally Purchase vouchers checked</strong> against '+Number(d.records?.length||0)+' ERP GRN-to-SO records.';
  $('pipelineExplanation').textContent=d.note||'';$('pipelineExplanation').hidden=false;$('forecastWarning').hidden=true;
 }
 function pipelineLoadFailed(error){
  if(viewMode!=='pipeline')return;
  $('forecastTitle').textContent='Invoice pipeline unavailable';
  $('forecastSubtitle').textContent=error?.message||'Unable to load the invoice pipeline.';
  $('pipelineExplanation').textContent='Check the ERP connection and try Update forecast again.';$('pipelineExplanation').hidden=false;
  $('weeklyCashflowChart').hidden=true;$('forecastEmpty').hidden=false;$('forecastEmpty').textContent='Invoice pipeline could not be loaded.';
 }
 async function loadPipeline(reset=false){
  hideDetail();hidePipelineDetail();$('pipelineHoverCard').hidden=true;const from=$('forecastFrom'),to=$('forecastTo');
  if(reset||!from.value||!to.value){const end=new Date(),start=new Date();start.setDate(end.getDate()-89);const iso=d=>d.getFullYear()+'-'+String(d.getMonth()+1).padStart(2,'0')+'-'+String(d.getDate()).padStart(2,'0');from.value=iso(start);to.value=iso(end)}
  const q=new URLSearchParams({from:from.value,to:to.value});const response=await fetch('/api/cashflow/tally/invoice-pipeline?'+q,{credentials:'same-origin'}),d=await response.json();if(!response.ok)throw new Error(d.error||'Unable to load invoice pipeline.');
  if(viewMode!=='pipeline')return;
  pipelineData=d;pipelineSummary(d);renderPipelineChart(d);$('forecastTitle').textContent='PO / SO Invoice Pipeline';$('forecastSubtitle').textContent='GRN to Tally vendor invoice to ERP or Tally client invoice, grouped weekly. Click a week for vendor-wise history.';$('forecastNote').innerHTML='<i class="fas fa-link me-1"></i> Hover for a scrollable vendor list. Click a week for PO, SO and invoice details.';
 }
 function renderPipelineDetails(records,title){
  const rows=[...(records||[])].sort((a,b)=>(a.vendorName||'').localeCompare(b.vendorName||'')||String(a.eventDate||'').localeCompare(String(b.eventDate||'')));
  $('pipelineDetailTitle').textContent=title;$('pipelineDetailSubtitle').textContent=rows.length+' record'+(rows.length===1?'':'s')+' · vendors A–Z, then oldest date first.';
  $('pipelineDetailBody').innerHTML=rows.length?rows.map((r,i)=>'<tr data-pipeline-index="'+i+'"><td><strong>'+esc(r.vendorName||'—')+'</strong></td><td>'+date(r.eventDate)+'</td><td>'+esc(r.grnNumber||'—')+'</td><td>'+esc(r.poNumber||'—')+'</td><td>'+esc(r.soNumber||'—')+'</td><td>'+esc(r.clientName||'—')+'</td><td>'+(r.tallyVoucherNumber?'<button type="button" class="invoice-link" data-pipeline-voucher="'+i+'">'+esc(r.vendorInvoiceNumber||r.tallyVoucherNumber)+'</button>':esc(r.vendorInvoiceNumber||'—'))+'</td><td>'+esc(r.tallyVoucherNumber||'—')+'</td><td>'+pipelineClientLinks(r,i)+'</td><td>'+Number(r.pendingDays||0)+'</td><td><span class="status-pill '+pipelineStatusClass(r.status)+'">'+esc(r.statusLabel)+'</span></td></tr>').join(''):'<tr><td colspan="11" class="text-center py-4">No matching records.</td></tr>';
  $('pipelineDetailBody')._records=rows;$('pipelineDetailPanel').hidden=false;$('pipelineDetailPanel').scrollIntoView({behavior:'smooth',block:'start'});
 }
 function pipelineClientLinks(r,index){
  const tally=r.tallyClientVouchers||[],erp=r.erpClientInvoiceNumbers||[];
  const links=tally.map((v,j)=>'<button type="button" class="invoice-link" data-pipeline-client="'+index+'" data-client-voucher="'+j+'">'+esc(v.voucherNumber)+'</button>');
  erp.filter(n=>!tally.some(v=>v.voucherNumber===n)).forEach(n=>links.push(esc(n)+' (ERP)'));
  return links.join(', ')||'—';
 }
 function filterPipeline(status){
  if(!pipelineData)return;let rows=pipelineData.records||[],title='Invoice pipeline history';
  if(status==='ERP_VENDOR_INVOICE_MISSING'){rows=rows.filter(r=>r.status===status);title='Vendor invoice numbers missing in ERP GRNs'}
  else if(status==='VENDOR_NOT_BOOKED'){rows=rows.filter(r=>r.status===status);title='Vendor invoices not booked in Tally'}
  else if(status){rows=rows.filter(r=>r.status===status);title=status==='CLIENT_INVOICE_MISSING'?'Client invoices not linked to an ERP SO':status==='REVIEW_REQUIRED'?'Records requiring invoice-match review':'Linked invoice history'}
  renderPipelineDetails(rows,title);
 }
 function switchView(mode){
  if(mode===viewMode)return;hideDetail();hidePipelineDetail();$('pipelineHoverCard').hidden=true;
  if(mode==='pipeline'){cashflowRange={from:$('forecastFrom').value,to:$('forecastTo').value};viewMode='pipeline';granularity='weekly';$('cashflowViewBtn').classList.remove('active');$('pipelineViewBtn').classList.add('active');$('cashflowSummary').hidden=true;$('pipelineSummary').hidden=false;$('forecastOpeningControls').hidden=true;$('backToWeekly').hidden=true;$('resetForecast').hidden=false;$('resetForecast').textContent='Last 90 days';$('forecastTitle').textContent='Loading invoice pipeline…';$('forecastSubtitle').textContent='Reading the latest prepared ERP and Tally snapshot.';loadPipeline(true).catch(pipelineLoadFailed)}
  else{viewMode='cashflow';$('pipelineViewBtn').classList.remove('active');$('cashflowViewBtn').classList.add('active');$('pipelineSummary').hidden=true;$('cashflowSummary').hidden=false;$('forecastOpeningControls').hidden=false;$('pipelineExplanation').hidden=true;$('resetForecast').textContent='Next 90 days';$('forecastFrom').value=cashflowRange?.from||'';$('forecastTo').value=cashflowRange?.to||'';loadForecast().catch(e=>alert(e.message))}
 }
 function heading(){
  const daily=granularity==='daily';$('forecastTitle').textContent=daily?'Daily Cash Flow Detail':'Weekly Cash Flow Forecast';
  $('forecastSubtitle').textContent='Opening balance carries forward. Scheduled invoices use expected dates; future due dates are fallback assumptions. Click a period to inspect invoices.';
  $('backToWeekly').hidden=!daily;$('resetForecast').hidden=daily;
 }
 async function loadForecast(reset=false){
  const from=$('forecastFrom'),to=$('forecastTo');hideDetail();
  if(reset){from.value='';to.value='';granularity='weekly';savedRange=undefined}
  const q=new URLSearchParams();if(from.value)q.set('from',from.value);if(to.value)q.set('to',to.value);q.set('granularity',granularity);if($('forecastOpeningInput').value!==''){q.set('openingBalance',$('forecastOpeningInput').value);q.set('openingDate',$('forecastOpeningDate').value);}
  const response=await fetch('/api/cashflow/tally/projected-forecast?'+q,{credentials:'same-origin'}),d=await response.json();
  if(!response.ok)throw new Error(d.error||'Unable to load cash flow forecast.');if(viewMode!=='cashflow')return;if(!from.value)from.value=d.from;if(!to.value)to.value=d.to;if($('forecastOpeningInput').value===''&&d.enteredOpeningBalance!=null){$('forecastOpeningInput').value=d.enteredOpeningBalance;$('forecastOpeningDate').value=d.balanceAsOf;}currentData=d;summary(d);renderChart(d);heading();
 }
 function openDaily(p){
  savedRange={from:$('forecastFrom').value,to:$('forecastTo').value};granularity='daily';
  $('forecastFrom').value=savedRange.from&&savedRange.from>p.weekStart?savedRange.from:p.weekStart;$('forecastTo').value=savedRange.to&&savedRange.to<p.weekEnd?savedRange.to:p.weekEnd;
  loadForecast().catch(e=>alert(e.message));
 }
 function backWeekly(){granularity='weekly';$('forecastFrom').value=savedRange?.from||'';$('forecastTo').value=savedRange?.to||'';loadForecast().catch(e=>alert(e.message))}
 function openAllBacklog(){
  const bills=(currentData?.backlogBills||[]);if(!bills.length)return;
  const yesterday=new Date();yesterday.setDate(yesterday.getDate()-1);const end=yesterday.getFullYear()+'-'+String(yesterday.getMonth()+1).padStart(2,'0')+'-'+String(yesterday.getDate()).padStart(2,'0');
  openInvoices({weekStart:currentData.availableFrom||bills.map(b=>b.dueDate).sort()[0],weekEnd:end,bills,backlog:true,allBacklog:true});
 }
 function openAllDirection(direction){
  const bills=(currentData?.periods||[]).flatMap(p=>p.bills||[]).filter(b=>b.direction===direction);if(!bills.length)return;
  openInvoices({weekStart:currentData.from||currentData.availableFrom,weekEnd:currentData.to||currentData.availableTo,bills,direction});
 }
 function openInvoices(p){
  selectedPeriod=p;const bills=p.bills||[];$('forecastInvoiceTitle').textContent=p.direction==='RECEIVABLE'?'All receivable invoices':p.direction==='PAYABLE'?'All payable invoices':p.allBacklog?'All overdue backlog':p.backlog?'Backlog invoices · '+date(p.weekStart)+' – '+date(p.weekEnd):'Invoices due '+date(p.weekStart);
  $('forecastInvoiceSubtitle').textContent=bills.length+' outstanding invoice'+(bills.length===1?'':'s')+' · Click an invoice to load its live voucher from Tally.';
  $('forecastInvoiceBody').innerHTML=bills.length?bills.map((b,i)=>'<tr data-invoice-index="'+i+'" tabindex="0"><td><span class="direction-pill '+(b.direction==='PAYABLE'?'payable':'')+'">'+esc(b.direction)+'</span></td><td>'+esc(b.partyName||'—')+'</td><td><button type="button" class="invoice-link">'+esc(b.invoiceNumber||'—')+'</button></td><td>'+date(b.invoiceDate)+'</td><td>'+date(b.dueDate)+'</td><td class="amount-cell">'+money(b.amount)+'</td><td class="amount-cell">'+money(b.dueAmount)+'</td><td class="amount-cell">'+money(b.overdueAmount)+'</td><td>'+Number(b.ageingDays||0)+'</td><td><input type="date" data-schedule-date="'+i+'" value="'+esc(b.expectedCashDate||'')+'" min="'+todayIso()+'" aria-label="Expected date for '+esc(b.invoiceNumber)+'"><button type="button" data-save-schedule="'+i+'">Save</button></td></tr>').join(''):'<tr><td colspan="10" class="text-center py-4">No invoices found for this day.</td></tr>';
  $('forecastInvoicePanel').hidden=false;$('forecastInvoicePanel').scrollIntoView({behavior:'smooth',block:'start'});
 }
 const grid=rows=>'<div class="voucher-grid">'+rows.map(r=>'<div class="voucher-field"><label>'+esc(r[0])+'</label><strong>'+esc(r[1]||'—')+'</strong></div>').join('')+'</div>';
 const table=(title,headers,rows)=>rows?.length?'<section class="voucher-lines"><h3>'+esc(title)+'</h3><div class="table-responsive"><table><thead><tr>'+headers.map(esc).map(h=>'<th>'+h+'</th>').join('')+'</tr></thead><tbody>'+rows.map(r=>'<tr>'+r.map(v=>'<td>'+esc(v)+'</td>').join('')+'</tr>').join('')+'</tbody></table></div></section>':'';
 async function liveVoucher(b){
  const modal=bootstrap.Modal.getOrCreateInstance($('forecastVoucherModal'));$('forecastVoucherTitle').textContent=(b.invoiceNumber||'Invoice')+' · '+(b.partyName||'');$('forecastVoucherStatus').classList.remove('text-danger');$('forecastVoucherStatus').textContent='Loading voucher from the company currently open in Tally…';$('forecastVoucherContent').innerHTML='';modal.show();
  const direct=b.directTallyVoucher===true;
  const q=new URLSearchParams(direct?{voucherNumber:b.invoiceNumber||'',partyName:b.partyName||'',voucherDate:b.invoiceDate||''}:{billNumber:b.invoiceNumber||'',partyName:b.partyName||'',invoiceDate:b.invoiceDate||''});
  const endpoint=direct?'/api/cashflow/tally/cost-centres/voucher-details?':'/api/cashflow/tally/outstanding/details?';
  try{const cacheKey=endpoint+q.toString(),cached=voucherCache.get(cacheKey),useCached=Boolean(cached&&Date.now()-cached.savedAt<voucherCacheTtl);let payload;
   if(useCached){payload=cached.payload}else{const response=await fetch(endpoint+q,{credentials:'same-origin'});payload=await response.json();if(!response.ok)throw new Error(payload.error||payload.message||'Voucher unavailable');voucherCache.set(cacheKey,{payload,savedAt:Date.now()})}
   const v=payload.voucher||{};$('forecastVoucherStatus').textContent=(useCached?'Cached · ':'')+(v.message||'Live voucher details loaded from Tally.');
   $('forecastVoucherContent').innerHTML=grid([['Voucher type',v.voucherType],['Voucher number',v.voucherNumber],['Reference',v.reference],['Voucher date',date(v.voucherDate)],['Tally Master ID',v.masterId],['Matched using',v.matchedBy],['Original invoice value',Number(v.originalInvoiceValue)>0?money(v.originalInvoiceValue):'Not provided'],['Narration',v.narration||'No narration returned']])
   +table('Tax summary',['Tax ledger','Amount'],Object.entries(v.taxSummary||{}).map(x=>[x[0],money(x[1])]))
   +table('Ledger entries',['Ledger','Type','Amount'],(v.ledgerEntries||[]).map(x=>[x.name,x.tax?'Tax':'Ledger',money(x.amount)]))
   +table('Inventory entries',['Item','Quantity','Rate','Amount'],(v.inventoryEntries||[]).map(x=>[x.name,x.quantity,x.rate,money(x.amount)]))
   +table('Bill adjustments',['Bill','Type','Amount'],(v.billAdjustments||[]).map(x=>[x.billName,x.billType,money(x.amount)]));
  }catch(e){$('forecastVoucherStatus').textContent=e.message;$('forecastVoucherStatus').classList.add('text-danger')}
 }
 const syncTime=v=>{if(!v)return'Never';const d=Array.isArray(v)?new Date(v[0],v[1]-1,v[2],v[3]||0,v[4]||0,v[5]||0):new Date(v);return Number.isNaN(d.getTime())?String(v):d.toLocaleString('en-IN')};
 async function tallyStatus(){
  try{const response=await fetch('/api/cashflow/tally/status',{credentials:'same-origin'});if(!response.ok)throw new Error('HTTP '+response.status);const r=await response.json(),connected=r.config?.serverAvailable===true,s=r.syncStatus;$('syncStatusCard').classList.toggle('success',connected);$('syncIcon').classList.toggle('success',connected);$('syncStatusText').textContent=connected?'Tally Live'+(s?.invoicesSynced!=null?' • '+s.invoicesSynced+' bills':''):'Tally unavailable';$('syncTimestampValue').textContent=syncTime(s?.lastSyncEndTime||s?.lastSyncTime)}
  catch(e){$('syncStatusText').textContent='Tally status unavailable';$('syncTimestampValue').textContent=e.message}
 }
 async function syncNow(){
  const btn=$('syncNowBtn');btn.disabled=true;$('syncStatusCard').classList.add('syncing');$('syncIcon').classList.add('syncing');$('syncStatusText').textContent='Syncing with Tally…';$('syncTimestampValue').textContent='Keep TallyPrime open';
  try{const response=await fetch('/api/cashflow/tally/sync',{method:'POST',credentials:'same-origin'}),r=await response.json();if(!response.ok||r.success===false||r.status?.lastSyncStatus==='FAILED')throw new Error(r.message||r.status?.lastErrorMessage||'Sync failed');await tallyStatus();if(viewMode==='pipeline')await loadPipeline();else await loadForecast()}catch(e){$('syncStatusText').textContent='Tally sync failed';$('syncTimestampValue').textContent=e.message}finally{btn.disabled=false;$('syncStatusCard').classList.remove('syncing');$('syncIcon').classList.remove('syncing')}
 }
 document.addEventListener('DOMContentLoaded',()=>{
  const root=document.documentElement;if(localStorage.getItem('financeSidebarCollapsed')==='true')root.classList.add('sidebar-precollapsed');
  $('sidebarCollapseBtn')?.addEventListener('click',e=>{const c=root.classList.toggle('sidebar-precollapsed');localStorage.setItem('financeSidebarCollapsed',String(c));e.currentTarget.title=c?'Expand sidebar':'Collapse sidebar'});
  const nav=document.querySelector('.forecast-nav'),toggle=document.querySelector('[data-graphs-toggle]');if(nav&&toggle){const open=localStorage.getItem('neptuneGraphsExpanded')!=='false';nav.classList.toggle('graphs-expanded',open);toggle.setAttribute('aria-expanded',String(open));toggle.addEventListener('click',()=>{const x=nav.classList.toggle('graphs-expanded');toggle.setAttribute('aria-expanded',String(x));localStorage.setItem('neptuneGraphsExpanded',String(x))})}
  $('cashflowViewBtn').addEventListener('click',()=>switchView('cashflow'));$('pipelineViewBtn').addEventListener('click',()=>switchView('pipeline'));
  $('saveForecastOpening').addEventListener('click',async()=>{
   if($('forecastOpeningInput').value===''||!$('forecastOpeningDate').value||($('forecastFrom').value&&$('forecastOpeningDate').value>$('forecastFrom').value)){alert('Enter an opening balance and a date on or before the forecast From date.');return}
   const button=$('saveForecastOpening');button.disabled=true;
   try{const response=await fetch('/api/cashflow/finance-plan/settings');if(!response.ok)throw new Error('Could not read balance settings.');const settings=await response.json();settings.openingBalance=Number($('forecastOpeningInput').value);settings.balanceAsOf=$('forecastOpeningDate').value;
    const saved=await fetch('/api/cashflow/finance-plan/settings',{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify(settings)});if(!saved.ok)throw new Error('Could not save opening balance.');await loadForecast();
   }catch(e){alert(e.message)}finally{button.disabled=false}
  });
  $('applyForecast').addEventListener('click',()=>{(viewMode==='pipeline'?loadPipeline():loadForecast()).catch(e=>viewMode==='pipeline'?pipelineLoadFailed(e):alert(e.message))});$('resetForecast').addEventListener('click',()=>{(viewMode==='pipeline'?loadPipeline(true):loadForecast(true)).catch(e=>viewMode==='pipeline'?pipelineLoadFailed(e):alert(e.message))});$('backToWeekly').addEventListener('click',backWeekly);
  document.querySelectorAll('[data-pipeline-status]').forEach(button=>button.addEventListener('click',()=>filterPipeline(button.dataset.pipelineStatus)));
  $('forecastBacklogCard').addEventListener('click',openAllBacklog);
  $('forecastReceivablesCard').addEventListener('click',()=>openAllDirection('RECEIVABLE'));
  $('forecastPayablesCard').addEventListener('click',()=>openAllDirection('PAYABLE'));
  $('exportForecastInvoices').addEventListener('click',async()=>{
   if(!selectedPeriod)return;const btn=$('exportForecastInvoices'),q=new URLSearchParams({from:selectedPeriod.weekStart,to:selectedPeriod.weekEnd});btn.disabled=true;
   try{const response=await fetch('/api/cashflow/tally/projected-forecast/export',{method:'POST',credentials:'same-origin',headers:{'Content-Type':'application/json'},body:JSON.stringify(selectedPeriod.bills.map(b=>b.id))});if(!response.ok){let message='Unable to export invoice detail.';try{message=(await response.json()).error||message}catch(_ignored){}throw new Error(message)}
    const blob=await response.blob(),url=URL.createObjectURL(blob),link=document.createElement('a');link.href=url;link.download='Cash-Flow-Invoice-Detail-'+selectedPeriod.weekStart+'-to-'+selectedPeriod.weekEnd+'.xlsx';document.body.appendChild(link);link.click();link.remove();setTimeout(()=>URL.revokeObjectURL(url),1000);
   }catch(e){alert(e.message)}finally{btn.disabled=false}
  });
  $('exportPipeline').addEventListener('click',async()=>{
   const btn=$('exportPipeline'),from=$('forecastFrom').value,to=$('forecastTo').value;if(!from||!to)return;btn.disabled=true;
   try{const q=new URLSearchParams({from,to}),response=await fetch('/api/cashflow/tally/invoice-pipeline/export?'+q,{credentials:'same-origin'});if(!response.ok){let message='Unable to export invoice pipeline.';try{message=(await response.json()).error||message}catch(_ignored){}throw new Error(message)}
    const blob=await response.blob(),url=URL.createObjectURL(blob),link=document.createElement('a');link.href=url;link.download='PO-SO-Invoice-Pipeline-'+from+'-to-'+to+'.xlsx';document.body.appendChild(link);link.click();link.remove();setTimeout(()=>URL.revokeObjectURL(url),1000);
   }catch(e){alert(e.message)}finally{btn.disabled=false}
  });
  $('forecastInvoiceBody').addEventListener('click',async e=>{const save=e.target.closest('[data-save-schedule]');if(save){const b=selectedPeriod.bills[Number(save.dataset.saveSchedule)],input=$('forecastInvoiceBody').querySelector('[data-schedule-date="'+save.dataset.saveSchedule+'"]');if(!input.value||input.value<todayIso()){alert('Choose a date today or later.');return}save.disabled=true;try{const r=await fetch('/api/cashflow/finance-plan/invoice-schedule/'+b.id,{method:'PUT',headers:{'Content-Type':'application/json'},body:JSON.stringify({expectedCashDate:input.value,remarks:b.cashflowRemarks||null})});if(!r.ok)throw new Error('Could not save expected date.');await loadForecast();}catch(err){alert(err.message);save.disabled=false}return}if(e.target.closest('input'))return;const row=e.target.closest('tr[data-invoice-index]');if(row&&selectedPeriod)liveVoucher(selectedPeriod.bills[Number(row.dataset.invoiceIndex)])});
  $('pipelineDetailBody').addEventListener('click',e=>{const client=e.target.closest('[data-pipeline-client]');if(client){const r=$('pipelineDetailBody')._records?.[Number(client.dataset.pipelineClient)],v=r?.tallyClientVouchers?.[Number(client.dataset.clientVoucher)];if(v)liveVoucher({invoiceNumber:v.voucherNumber,partyName:v.partyName,invoiceDate:v.voucherDate,directTallyVoucher:true});return}const button=e.target.closest('[data-pipeline-voucher]'),rows=$('pipelineDetailBody')._records;if(button&&rows){const r=rows[Number(button.dataset.pipelineVoucher)];liveVoucher({invoiceNumber:r.tallyVoucherNumber,partyName:r.vendorName,invoiceDate:r.tallyVoucherDate||r.vendorInvoiceDate,directTallyVoucher:true})}});
  $('forecastInvoiceBody').addEventListener('keydown',e=>{if(e.key==='Enter'&&!e.target.closest('input,button'))e.target.closest('tr[data-invoice-index]')?.click()});$('syncNowBtn')?.addEventListener('click',syncNow);
  const pipelineCanvas=$('weeklyCashflowChart'),pipelineHover=$('pipelineHoverCard');
  pipelineCanvas.addEventListener('mouseleave',hidePipelineHoverSoon);
  pipelineHover.addEventListener('mouseleave',()=>{pipelineHover.hidden=true});
  tallyStatus();loadForecast().catch(e=>{$('forecastEmpty').hidden=false;$('forecastEmpty').textContent=e.message});
 });
})();
