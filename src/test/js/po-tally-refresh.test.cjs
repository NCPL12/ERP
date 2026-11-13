const fs = require('fs');
const vm = require('vm');
const assert = require('assert');
const path = require('path');
const jsp = fs.readFileSync(path.join(__dirname, '../../main/webapp/WEB-INF/views/salesReport.jsp'), 'utf8');
const source = jsp.slice(jsp.indexOf('    function refreshPoTallyConnection()'), jsp.lastIndexOf('</script>'))
  .replace(/<c:if test="\$\{not empty _csrf\}">[\s\S]*?<\/c:if>/g, "hidden('_csrf','test-token');")
  .replaceAll('${pageContext.request.contextPath}', '/ncpl-sales');
async function check(failure, canEdit = true) {
  const elements = {};
  ['checkTallyPo','tallyPoConnectionStatus','tallyPoAutoSyncStatus','tallyPoSavedIssues','poLostByDateForm','send'].forEach(id => elements[id] = { dataset:{}, disabled:false, getAttribute(name) { return name === 'formaction' ? '/ncpl-sales/po_list/tally/import' : String(canEdit); }, textContent:'', addEventListener(type, fn) { this[type] = fn; }});
  elements.poLostByDateForm.fromDate = '01-09-2026';
  elements.poLostByDateForm.toDate = '30-09-2026';
  const calls = [];
  const timers = [], submitted = [];
  let dialog, alerts = [];
  const createElement = tag => ({tag, children: [], appendChild(child) { this.children.push(child); },
    submit() { submitted.push(this); }});
  const sandbox = {
    document: {getElementById: id=>elements[id],querySelector:()=>elements.send,
      createElement, body: { appendChild() {} }},
    $: () => ({text(value) { this.value = value; return this; }}),
    bootbox: {confirm:options=>{dialog=options;},prompt:options=>{dialog=options;},alert:message=>alerts.push(message)},
    AbortController,
    setTimeout:(fn, delay)=>{if(delay === 0) timers.push(fn);return 1;},clearTimeout:()=>{},setInterval:()=>{},
    fetch: async url=> {
      calls.push(url);
      if(url.endsWith('/sync-status')) return {ok:true,json:async()=>({enabled:true,configurationError:'Starting date missing'})};
      return {ok:!failure,json:async()=>failure ? {message:'Tally offline'} : {company:'Neptune'}};
    }
  };
  vm.runInNewContext(source,sandbox);
  await new Promise(resolve=>setImmediate(resolve));
  assert.strictEqual(elements.send.disabled,failure || !canEdit);
  assert.strictEqual(elements.tallyPoAutoSyncStatus.textContent,'Starting date missing');
  await elements.checkTallyPo.click();
  await new Promise(resolve=>setImmediate(resolve));
  assert.strictEqual(elements.poLostByDateForm.fromDate,'01-09-2026');
  assert.strictEqual(elements.poLostByDateForm.toDate,'30-09-2026');
  assert(!calls.some(url=>url.endsWith('/sales_report')));
  assert(!source.includes('location.reload'));
  assert.strictEqual(elements.checkTallyPo.disabled,false);
  if(failure) assert.strictEqual(elements.tallyPoConnectionStatus.textContent,'Tally offline');
  else assert.strictEqual(elements.tallyPoConnectionStatus.textContent,'Neptune is ready');
  // A delegated date validator can cancel after the form's own submit listener.
  const form = elements.poLostByDateForm, send = elements.send;
  send.innerHTML = 'Send to Tally';
  const event = {submitter:send,defaultPrevented:false,preventDefault(){this.defaultPrevented=true;}};
  form.submit(event);event.preventDefault();timers.splice(0).forEach(fn=>fn());
  assert.strictEqual(form.dataset.exportRunning,undefined);
  assert.strictEqual(send.innerHTML,'Send to Tally');
  if (!failure && canEdit) {
    form.submit({submitter:send});timers.splice(0).forEach(fn=>fn());
    assert.strictEqual(form.dataset.exportRunning,'true');assert.strictEqual(send.disabled,true);
    const duplicate={submitter:send,preventDefault(){this.cancelled=true;}};
    form.submit(duplicate);assert.strictEqual(duplicate.cancelled,true);
  }
  sandbox.submitTallyReview('PO/1','accept');
  assert.strictEqual(submitted.length,0);dialog.callback(true);
  assert.strictEqual(submitted[0].action,'/ncpl-sales/po_list/tally/review/accept');
  assert.strictEqual(submitted[0].method,'post');
  assert.deepStrictEqual(submitted[0].children.map(x=>[x.name,x.value]),[['poNumber','PO/1'],['_csrf','test-token']]);
  sandbox.submitTallyReview('PO/1','force-erp');
  assert.strictEqual(submitted.length,1);
  assert.strictEqual(dialog.callback('wrong'),false);
  assert.strictEqual(submitted.length,1);assert.strictEqual(alerts.length,1);
  dialog.callback('FORCE ERP VERSION');
  assert.strictEqual(submitted[1].action,'/ncpl-sales/po_list/tally/review/force-erp');
  assert(submitted[1].children.some(x=>x.name==='confirmation' && x.value==='FORCE ERP VERSION'));
  sandbox.submitTallyReview('PO/1','accept');dialog.callback(false);
  sandbox.submitTallyReview('PO/1','force-erp');dialog.callback(null);
  assert.strictEqual(submitted.length,2);
}
(async()=>{await check(false);await check(true);await check(false, false);console.log('PO connection refresh tests passed: success/offline, preserved dates, no navigation, visible sync configuration.');})().catch(error=>{console.error(error);process.exit(1);});
