const fs = require('fs');
const vm = require('vm');
const assert = require('assert');
const path = require('path');
const jsp = fs.readFileSync(path.join(__dirname, '../../main/webapp/WEB-INF/views/salesReport.jsp'), 'utf8');
const source = jsp.slice(jsp.indexOf('    function refreshPoTallyConnection()'), jsp.lastIndexOf('</script>'));
async function check(failure) {
  const elements = {};
  ['checkTallyPo','tallyPoConnectionStatus','tallyPoAutoSyncStatus','poLostByDateForm','send'].forEach(id => elements[id] = { dataset:{}, disabled:false, textContent:'', addEventListener(type, fn) { this[type] = fn; }});
  elements.poLostByDateForm.fromDate = '01-09-2026';
  elements.poLostByDateForm.toDate = '30-09-2026';
  const calls = [];
  const sandbox = {
    document: {getElementById: id=>elements[id],querySelector:()=>elements.send},
    AbortController,
    setTimeout:()=>1,clearTimeout:()=>{},setInterval:()=>{},
    fetch: async url=> {
      calls.push(url);
      if(url.endsWith('/sync-status')) return {ok:true,json:async()=>({enabled:true,configurationError:'Starting date missing'})};
      return {ok:!failure,json:async()=>failure ? {message:'Tally offline'} : {company:'Neptune'}};
    }
  };
  vm.runInNewContext(source,sandbox);
  await new Promise(resolve=>setImmediate(resolve));
  assert.strictEqual(elements.send.disabled,failure);
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
}
(async()=>{await check(false);await check(true);console.log('PO connection refresh tests passed: success/offline, preserved dates, no navigation, visible sync configuration.');})().catch(error=>{console.error(error);process.exit(1);});
