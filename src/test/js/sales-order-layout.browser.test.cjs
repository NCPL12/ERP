const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const assert = require('node:assert/strict');
const {spawnSync} = require('node:child_process');
const main = path.resolve(__dirname, '../../main');
const read = file => fs.readFileSync(path.join(main, file), 'utf8');
const jsp = read('webapp/WEB-INF/views/welcome.jsp');
const header = read('webapp/WEB-INF/tiles/header.jsp');
const table = jsp.match(/<table id="salesTable"[\s\S]*?<\/table>/)[0]
    .replace(/<!--[\s\S]*?-->/g, '').replace(/<form:errors\b[^>]*\/>/g, '')
    .replace(/<(\/?)form:(input|select)\b/g, '<$1$2');
const css = ['resources/static/dist/css/adminlte.css', 'resources/static/css/common.css',
    'resources/static/css/custom-select2.css', 'resources/static/css/salesOrder.css',
    'resources/static/plugins/select2/css/select2.min.css'].map(read).join('\n');
const sharedStyles = header.match(/<style type="text\/css">([\s\S]*?)<\/style>/)[1];
const pageStyles = jsp.match(/<style type="text\/css">\s*\.lbl-biiling-popup[\s\S]*?<\/style>/)[0];
const html = `<style>${css}</style><style>${sharedStyles}</style>${pageStyles}
<style>.fa-plus-square::before{content:'+';font-size:24px}.fa-trash::before{content:'x'}</style>
<div class="wrapper" id="layoutCanvas">${jsp.match(/<div class="content-wrapper[^"]*">/)[0]}<div class="card"><div class="card-body p-0">
<div class="sales-items-scroll">${table}</div><div class="button-div-style text-center"><button class="btn btn-primary btn-sm">Update</button></div>
</div></div></div></div><pre id="result">PENDING</pre>
<script>${read('resources/static/plugins/jquery/jquery.min.js')}</script>
<script>${read('resources/static/plugins/select2/js/select2.full.min.js')}</script>
<script>
try {
function check(ok,message){if(!ok)throw new Error(message);}
function rect(element){return element.getBoundingClientRect();}
check(getComputedStyle(document.querySelector('.content-wrapper')).backgroundColor===getComputedStyle(document.body).backgroundColor,'Page background has a different color below the sales card');
const table=document.getElementById('salesTable'), body=table.tBodies[0];
const first=body.rows[0], second=first.cloneNode(true);body.appendChild(second);
for(const [index,row] of Array.from(body.rows).entries()){
    row.querySelector('.slNo').value=String(index+1);
    row.querySelector('.description').value=index?'A longer sample description for the second item':'Sample item';
    row.querySelector('.qty').value=index?'12':'11';
    row.querySelector('.unitPrice').value=index?'11':'10';
    row.querySelector('.servicePrice').value='0';
    row.querySelector('.amount').value=index?'132':'110';
    const unit=row.querySelector('select.unit');unit.innerHTML='<option>'+(index?'Box of 100':'Ltr')+'</option>';unit.id='layoutUnit'+index;
    $(unit).select2({dropdownAutoWidth:true});
    row.querySelector('[id^="designTd"]').style.display='table-cell';
    row.querySelector('.toggleBriefTd').style.display='table-cell';
}
document.getElementById('designColumnHeader').style.display='table-cell';
document.getElementById('expandAllHeader').style.display='table-cell';
document.getElementById('total').value='242';document.getElementById('gst').value='43.56';
document.getElementById('grandTotal').value='285.56';
const text=first.querySelector('.qty'), selection=first.querySelector('.select2-selection--single');
check(Math.abs(rect(text).height-rect(selection).height)<1,'Unit dropdown and input heights differ');
check(Math.abs(rect(text).top-rect(selection).top)<1,'Unit dropdown is vertically misaligned');
const rendered=first.querySelector('.select2-selection__rendered');
check(getComputedStyle(rendered).marginTop==='0px','Shared negative dropdown margin still applies');
for(const row of body.rows){
    const label=row.querySelector('.select2-selection__rendered');
    const control=row.querySelector('.select2-selection--single');
    check(label.scrollWidth<=label.clientWidth,'Unit names are clipped');
    const range=document.createRange();range.selectNodeContents(label);
    const textBounds=range.getBoundingClientRect(), controlBounds=rect(control);
    check(textBounds.top>=controlBounds.top && textBounds.bottom<=controlBounds.bottom,'Unit text falls outside its control');
    check(Math.abs((textBounds.top+textBounds.bottom-controlBounds.top-controlBounds.bottom)/2)<2,'Unit text is not vertically centered');
    check(getComputedStyle(row.querySelector('.slNo')).textAlign==='center','Serial numbers are not centered');
}
const heads=table.tHead.rows[0].cells;
for(const index of [0,1,2,5,6,8,9,10,11]){
    check(getComputedStyle(heads[index]).verticalAlign==='middle','A rowspan header is not centered');
    check(getComputedStyle(heads[index]).paddingBottom==='8px','Unequal header padding remains');
}
const box=document.getElementById('masterToggleBrief'), boxHeader=box.closest('th');
check(Math.abs(rect(box).top+rect(box).height/2-rect(boxHeader).top-rect(boxHeader).height/2)<2,'Header checkbox is off-center');
const amount=first.querySelector('.amount');
for(const input of table.tFoot.querySelectorAll('input,select')){
    check(Math.abs(rect(input).right-rect(amount).right)<2,'Totals do not align under Amount');
    check(Math.abs(rect(input).height-rect(text).height)<1,'Totals controls have different heights');
}
for(const spacer of table.tFoot.querySelectorAll('td[colspan]'))
    check(getComputedStyle(spacer).borderTopWidth==='0px','Empty totals area has full-width divider lines');
const canvas=document.getElementById('layoutCanvas'), region=table.closest('.sales-items-scroll');
canvas.style.width='700px';
check(region.scrollWidth>region.clientWidth,'Narrow screens squeeze the table instead of scrolling');
canvas.style.width='';
const saved=second.cloneNode(true);
for(let i=0;i<25;i++)body.appendChild(saved.cloneNode(true));
const before=rect(table.tHead).top;region.scrollTop=240;
check(region.scrollTop>0 && Math.abs(rect(table.tHead).top-before)<2,'Sticky header alignment broke');
while(body.rows.length>2)body.deleteRow(body.rows.length-1);region.scrollTop=0;
document.getElementById('result').textContent='PASS: row controls, header centering, totals, narrow screens, and sticky headers';
}catch(error){document.getElementById('result').textContent='FAIL: '+error.message;}
</script>`;
const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'erp-sales-layout-'));
const fixture = path.join(temp,'sales.html');fs.writeFileSync(fixture,html);
const args = ['--headless=new','--no-first-run','--disable-extensions','--disable-background-networking',
    '--user-data-dir='+path.join(temp,'profile'),'--window-size=1547,980','--force-device-scale-factor=1',
    '--virtual-time-budget=1000','--dump-dom'];
if(process.env.SALES_LAYOUT_SCREENSHOT)args.push('--screenshot='+path.resolve(process.env.SALES_LAYOUT_SCREENSHOT));
args.push('file:///'+fixture.replaceAll('\\','/'));
const run=spawnSync(process.env.CHROME_PATH||'C:/Program Files/Google/Chrome/Application/chrome.exe',args,
    {encoding:'utf8',windowsHide:true,timeout:20000,maxBuffer:8*1024*1024});
if(run.error)throw run.error;
assert.equal(run.status,0,run.stderr);
const result=run.stdout.match(/<pre id="result">([^<]*)<\/pre>/);
assert.ok(result,'Browser did not finish the checks');
assert.ok(result[1].startsWith('PASS:'),result[1]);
console.log(result[1]);
