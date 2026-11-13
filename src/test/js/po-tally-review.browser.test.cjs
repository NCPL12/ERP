const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const assert = require('node:assert/strict');
const {spawnSync} = require('node:child_process');
const main = path.resolve(__dirname, '../../main');
const read = file => fs.readFileSync(path.join(main, file), 'utf8');
const jsp = read('webapp/WEB-INF/views/salesReport.jsp');
const source = jsp.slice(jsp.indexOf('    function refreshPoTallyConnection()'), jsp.lastIndexOf('</script>'))
    .replace(/<c:if test="\$\{not empty _csrf\}">[\s\S]*?<\/c:if>/g, "hidden('_csrf','test-token');")
    .replaceAll('${pageContext.request.contextPath}', '/ncpl-sales');
const libraries = ['jquery/jquery.min.js', 'bootstrap/js/bootstrap.bundle.min.js', 'bootbox/bootbox.min.js']
    .map(file => `<script>${read('resources/static/plugins/' + file)}</script>`).join('');
const html = `<style>${read('resources/static/dist/css/adminlte.css')}</style>${libraries}
<form id="poLostByDateForm"><button type="button" id="checkTallyPo">Check Tally</button>
<button type="submit" data-can-export="true" formaction="/ncpl-sales/po_list/tally/import">Send to Tally</button></form>
<p id="tallyPoConnectionStatus"></p><p id="tallyPoAutoSyncStatus"></p><div id="tallyPoSavedIssues"></div><pre id="result">PENDING</pre>
<script>
const sent=[];
HTMLFormElement.prototype.submit=function(){sent.push({action:this.action,fields:Object.fromEntries(new FormData(this))});};
window.confirm=window.prompt=function(){throw new Error('Native browser dialogs must not be used');};
window.fetch=async url=>({ok:true,json:async()=>url.endsWith('/sync-status')?
    {enabled:true,records:[{poNumber:'PO/1',status:'NEEDS_REVIEW',message:'Tally version changed'}]}:{company:'Test Company'}});
${source}
(async function(){
    const pause=()=>new Promise(resolve=>setTimeout(resolve,400));
    const check=(ok,message)=>{if(!ok)throw new Error(message);};
    const action=name=>Array.from(document.querySelectorAll('#tallyPoSavedIssues button')).find(button=>button.textContent===name);
    try{
        await pause();
        action('Accept Tally version').click();await pause();
        check(document.querySelector('.bootbox.modal.show'),'Accept dialog did not open');
        document.querySelector('.bootbox [data-bb-handler="cancel"]').click();await pause();
        check(sent.length===0,'Cancelling Accept submitted a request');
        action('Accept Tally version').click();await pause();
        document.querySelector('.bootbox [data-bb-handler="confirm"]').click();await pause();
        check(sent.length===1 && sent[0].action.endsWith('/review/accept'),'Accept did not submit its endpoint');
        check(sent[0].fields.poNumber==='PO/1' && sent[0].fields._csrf==='test-token','Accept fields were lost');
        action('Force ERP version').click();await pause();
        check(document.querySelector('.bootbox-input'),'Force prompt did not open');
        document.querySelector('.bootbox-input').value='wrong';
        document.querySelector('.bootbox-prompt [data-bb-handler="confirm"]').click();await pause();
        check(sent.length===1,'Wrong confirmation submitted Force');
        document.querySelector('.bootbox-alert [data-bb-handler="ok"]').click();await pause();
        document.querySelector('.bootbox-input').value='FORCE ERP VERSION';
        document.querySelector('.bootbox-prompt [data-bb-handler="confirm"]').click();await pause();
        check(sent.length===2 && sent[1].action.endsWith('/review/force-erp'),'Force did not submit its endpoint');
        check(sent[1].fields.confirmation==='FORCE ERP VERSION' && sent[1].fields.poNumber==='PO/1','Force fields were lost');
        document.getElementById('result').textContent='PASS: actual conflict buttons open dialogs, cancel safely, validate confirmation, and submit correct endpoints';
    }catch(error){document.getElementById('result').textContent='FAIL: '+error.message;}
})();
</script>`;
const temp=fs.mkdtempSync(path.join(os.tmpdir(),'erp-tally-review-'));
const fixture=path.join(temp,'review.html');fs.writeFileSync(fixture,html);
const run=spawnSync(process.env.CHROME_PATH||'C:/Program Files/Google/Chrome/Application/chrome.exe',
    ['--headless=new','--no-first-run','--disable-extensions','--disable-background-networking',
     '--user-data-dir='+path.join(temp,'profile'),'--virtual-time-budget=10000','--dump-dom','file:///'+fixture.replaceAll('\\','/')],
    {encoding:'utf8',windowsHide:true,timeout:25000,maxBuffer:8*1024*1024});
if(run.error)throw run.error;
assert.equal(run.status,0,run.stderr);
const result=run.stdout.match(/<pre id="result">([^<]*)<\/pre>/);
assert.ok(result,'Browser did not finish');
assert.ok(result[1].startsWith('PASS:'),result[1]);console.log(result[1]);
