// Run with node src/test/js/csrf-forms.browser.test.cjs (requires Chrome).
const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const assert = require('node:assert/strict');
const {spawnSync} = require('node:child_process');
const main = path.resolve(__dirname, '../../main');
const header = fs.readFileSync(path.join(main, 'webapp/WEB-INF/tiles/header.jsp'), 'utf8');
const report = fs.readFileSync(path.join(main, 'webapp/WEB-INF/views/salesReport.jsp'), 'utf8');
const protect = header.match(/<script type="text\/javascript">([\s\S]*?)<\/script>/)[1];
assert.ok(protect.includes('protectForm'));
const tallyForm = report.match(/<form id="poLostByDateForm"[^>]*>/)[0];
const tallyButton = report.match(/<button data-can-export="[^\n]*?<\/button>/)[0].replace(/\$\{[^}]*\}/g, '');
const jquery = fs.readFileSync(path.join(main, 'resources/static/plugins/jquery/jquery.min.js'), 'utf8');
const html = `<meta name="_csrf" content="test-token"><meta name="_csrf_header" content="X-XSRF-TOKEN">
<script>${jquery}</script><script>${protect}</script>
${tallyForm}${tallyButton}</form>
<form id="ordinary" method="post" action="/write"><button>Save</button></form>
<form id="download" method="get" action="/download"><button>Download</button></form>
<form id="upload" method="post" enctype="multipart/form-data" action="/upload"><button>Upload</button></form>
<form id="override" method="get" action="/download"><button formmethod="post" formenctype="multipart/form-data" formaction="/import?kind=items">Import</button></form>
<form id="external" method="get" action="/download"><button formmethod="post" formaction="https://example.invalid/import">External</button></form>
<pre id="result">PENDING</pre>
<script>
document.addEventListener('submit', event => event.preventDefault(), true);
$(function(){setTimeout(function(){try{
function check(ok,message){if(!ok)throw new Error(message);}
function token(form){return form.querySelector('input[name="_csrf"]');}
const ordinary=document.getElementById('ordinary');
const download=document.getElementById('download');
const upload=document.getElementById('upload');
const override=document.getElementById('override');
const externalForm=document.getElementById('external');
check(token(ordinary).value==='test-token','Ordinary POST is protected');
download.requestSubmit(download.querySelector('button'));
check(!token(download),'GET-only download is unchanged');
const tally=document.getElementById('poLostByDateForm');
tally.requestSubmit(tally.querySelector('button'));
check(token(tally).value==='test-token','Tally POST override needs a token');
tally.requestSubmit(tally.querySelector('button'));
check(tally.querySelectorAll('input[name="_csrf"]').length===1,'Repeated submission duplicates tokens');
check(upload.getAttribute('action')==='/upload?_csrf=test-token','Multipart form action is protected');
const importer=override.querySelector('button');override.requestSubmit(importer);
check(importer.getAttribute('formaction')==='/import?kind=items&_csrf=test-token','Multipart submitter action must receive token');
externalForm.requestSubmit(externalForm.querySelector('button'));
check(!token(externalForm),'CSRF token must not be sent to another origin');
document.getElementById('result').textContent='PASS: Tally override, POST, GET, multipart, repeat submissions, and cross-origin target';
}catch(error){document.getElementById('result').textContent='FAIL: '+error.message;}},0);});
</script>`;
const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'erp-csrf-test-'));
const fixture = path.join(temp, 'forms.html');
fs.writeFileSync(fixture, html);
const run = spawnSync(process.env.CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe',
    ['--headless=new', '--no-first-run', '--disable-extensions', '--disable-background-networking',
        '--user-data-dir='+path.join(temp, 'profile'), '--virtual-time-budget=1000', '--dump-dom',
        'file:///'+fixture.replaceAll('\\','/')], {encoding:'utf8',windowsHide:true,timeout:20000});
if(run.error) throw run.error;
assert.equal(run.status, 0, run.stderr);
const result = run.stdout.match(/<pre id="result">([^<]*)<\/pre>/);
assert.ok(result, 'Browser did not report a result');
assert.ok(result[1].startsWith('PASS:'), result[1]);
console.log(result[1]);
