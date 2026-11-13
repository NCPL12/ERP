const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const assert = require('node:assert/strict');
const {spawnSync} = require('node:child_process');
const main = path.resolve(__dirname, '../../main');
const read = file => fs.readFileSync(path.join(main, file), 'utf8');
const create = read('webapp/WEB-INF/views/purchaseOrder.jsp');
const edit = read('webapp/WEB-INF/views/editPurchaseOrder.jsp');
const script = read('resources/static/js/purchaseOrder.js');
const editStart = script.indexOf('function editPoTable()');
const editEnd = script.indexOf('$(document).on("change", "#version"', editStart);
assert.ok(editStart >= 0 && editEnd > editStart);
function table(page, id) {
    const wrapper = page.match(/<div class="card-body table-responsive p-0[^>]*>/)[0];
    assert.ok(wrapper.includes('po-items-scroll'), id + ' needs a scroll region');
    const markup = page.match(new RegExp('<table id="' + id + '"[\\s\\S]*?</table>'))[0]
        .replace(/<!--[\s\S]*?-->/g, '');
    return wrapper + markup + '</div>';
}
const css = ['resources/static/dist/css/adminlte.css', 'resources/static/css/salesOrder.css',
    'resources/static/css/purchaseOrder.css'].map(read).join('\n');
const html = `<style>${css}</style>
<div id="vendorName">Test vendor</div>
<div id="purchaseDiv" class="card">${table(create, 'poTable')}<div>Total and Save</div></div>
<div id="salesDiv" class="card">${table(edit, 'poTableEdit')}<div>Total and Save</div></div>
<pre id="result">PENDING</pre>
<script>${read('resources/static/plugins/jquery/jquery.min.js')}</script>
<script>${read('resources/static/plugins/datatables/jquery.dataTables.js')}</script>
<script>
var canDeletePurchase=true;
var salesItemList=[{id:'SALES',description:'Sales item',slNo:1,item_units:{name:'Nos'},
    salesOrder:{id:'SO',party:{partyName:'Client'},clientPoNumber:'CLIENT-PO'}}];
var itemList=[{id:'ITEM',model:'Model',gst:18}];
var purchaseOrderObj=Array.from({length:40},function(_,i){return {id:i,sales_item_id:'SALES',
    description:'SALES',modelNo:'ITEM',poDescription:'Purchased item',hsnCode:'8537',quantity:1,
    unitPrice:100,gstRate:18,delivaryDate:null,lrNum:null,amount:118,purchase_item_id:i,po_item_id:i};});
function commaSeparateNumber(value){return String(value);}
${script.slice(editStart, editEnd)}
try {
    var createBody=document.querySelector('#poTable tbody');
    var row=createBody.rows[0].cloneNode(true);createBody.innerHTML='';
    for(var i=0;i<40;i++)createBody.appendChild(row.cloneNode(true));
    editPoTable();
    function check(ok,message){if(!ok)throw new Error(message);}
    for (var id of ['poTable','poTableEdit']) {
        var table=document.getElementById(id), region=table.closest('.po-items-scroll');
        region.scrollIntoView();region.scrollTop=0;
        var header=table.tHead.rows[0].cells[0];
        var initial=header.getBoundingClientRect().top;
        var bodyTop=table.tBodies[0].rows[0].getBoundingClientRect().top;
        region.scrollTop=300;
        check(region.scrollTop>0,id+' must scroll internally');
        check(Math.abs(header.getBoundingClientRect().top-initial)<2,id+' header moved while scrolling');
        check(table.tBodies[0].rows[0].getBoundingClientRect().top<bodyTop-100,id+' body must move below the header');
        var input=table.tBodies[0].rows[20].querySelector('input.poDescription');
        input.focus();check(document.activeElement===input,id+' item input cannot receive focus');
        region.style.width='480px';table.style.minWidth='1800px';region.scrollLeft=150;
        check(region.scrollLeft>0,id+' must allow horizontal scrolling');
        check(Math.abs(header.getBoundingClientRect().left-table.tBodies[0].rows[0].cells[0].getBoundingClientRect().left)<2,
            id+' header and body columns became misaligned');
    }
    document.getElementById('result').textContent='PASS: create and edit headers stay fixed, columns align, and item inputs remain accessible';
} catch(error) {document.getElementById('result').textContent='FAIL: '+error.message;}
</script>`;
const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'erp-po-sticky-'));
const fixture = path.join(temp, 'purchase.html');
fs.writeFileSync(fixture, html);
const run = spawnSync(process.env.CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe',
    ['--headless=new','--no-first-run','--disable-extensions','--disable-background-networking',
        '--user-data-dir='+path.join(temp,'profile'),'--virtual-time-budget=1000','--dump-dom',
        'file:///'+fixture.replaceAll('\\','/')], {encoding:'utf8',windowsHide:true,timeout:20000,maxBuffer:8*1024*1024});
if(run.error)throw run.error;
assert.equal(run.status,0,run.stderr);
const result=run.stdout.match(/<pre id="result">([^<]*)<\/pre>/);
assert.ok(result,'Browser did not finish the checks');
assert.ok(result[1].startsWith('PASS:'),result[1]);
console.log(result[1]);
