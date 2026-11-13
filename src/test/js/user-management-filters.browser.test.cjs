const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const assert = require('node:assert/strict');
const {spawnSync} = require('node:child_process');
const main = path.resolve(__dirname, '../../main');
const read = file => fs.readFileSync(path.join(main, file), 'utf8');
const table = read('webapp/WEB-INF/views/userManagement.jsp').match(/<table id="userManagementTable"[\s\S]*?<\/table>/)[0];
const html = `${table}<pre id="result">PENDING</pre>
<script>${read('resources/static/plugins/jquery/jquery.min.js')}</script>
<script>${read('resources/static/plugins/datatables/jquery.dataTables.js')}</script>
<script>
var api={USER_MANAGEMENT_LIST:'/users',ROLE_API:'/roles'};
var sampleUsers=[
{id:1,username:'alice',emailId:'alice@example.test',number:'1111111111',role:'SALES',enabled:true},
{id:2,username:'alicia',emailId:'alicia@example.test',number:'2222222222',role:'STORE',enabled:false},
{id:3,username:'bob',emailId:'bob@example.test',number:'3333333333',role:'SALES',enabled:false},
{id:4,username:'carol',emailId:'carol@example.test',number:'4444444444',role:'QA (A+)',enabled:true}];
$.ajax=function(options){
    if(options.url===api.USER_MANAGEMENT_LIST)options.success(sampleUsers);
    else if(options.url===api.ROLE_API)options.success([{name:'SALES'},{name:'STORE'},{name:'QA (A+)'}]);
    else throw new Error('Unexpected request '+options.url);
};
window.alert=function(message){throw new Error(message);};
</script>
<script>${read('resources/static/js/userManagement.js')}</script>
<script>$(function(){setTimeout(function(){try{
function check(ok,message){if(!ok)throw new Error(message);}
function ids(){return userTable.rows({search:'applied'}).data().toArray().map(user=>user.id).sort().join(',');}
function filter(column,value){$('#userManagementTable thead input[data-column="'+column+'"]').val(value).trigger('input');}
function clear(){ $('#userManagementTable thead input').val('');applyUserFilters(); }
check(ids()==='1,2,3,4','Initial users missing');
filter(1,'ali');check(ids()==='1,2','Username filtering failed');
filter(4,'sales');check(ids()==='1','Combined filters failed');
clear();filter(2,'bob@example.test');check(ids()==='3','Email filtering failed');
clear();filter(3,'2222');check(ids()==='2','Mobile filtering failed');
clear();filter(4,'QA (A+)');check(ids()==='4','Role must treat punctuation literally');
clear();filter(5,'ACTIVE');check(ids()==='1,4','ACTIVE must exclude INACTIVE users');
filter(5,'INACTIVE');check(ids()==='2,3','INACTIVE filtering failed');
filter(5,'act');check(ids()==='1,4','Partial status search failed');
loadUsers();check(ids()==='1,4','Refreshing users lost active filters');
check($('#userManagementTable thead tr').length===2,'Refreshing duplicated filter rows');
check($('#userManagementTable thead input').length===5,'Each user field needs one filter');
clear();filter(1,'missing');check(ids()==='','No-match search returned users');
clear();check(ids()==='1,2,3,4','Clearing filters did not restore users');
check($('#userManagementTable tbody button').first().attr('onclick')==="editUser('1')",'User actions lost their correct user id');
document.getElementById('result').textContent='PASS: all five filters, combined searches, status isolation, refresh, clearing, and user actions';
}catch(error){document.getElementById('result').textContent='FAIL: '+error.message;}},0);});</script>`;
const temp = fs.mkdtempSync(path.join(os.tmpdir(), 'erp-user-filters-'));
const fixture = path.join(temp,'users.html');
fs.writeFileSync(fixture,html);
const run = spawnSync(process.env.CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe',
    ['--headless=new','--no-first-run','--disable-extensions','--disable-background-networking',
        '--user-data-dir='+path.join(temp,'profile'),'--virtual-time-budget=1000','--dump-dom',
        'file:///'+fixture.replaceAll('\\','/')], {encoding:'utf8',windowsHide:true,timeout:20000,maxBuffer:4*1024*1024});
if(run.error)throw run.error;
assert.equal(run.status,0,run.stderr);
const result = run.stdout.match(/<pre id="result">([^<]*)<\/pre>/);
assert.ok(result,'Browser did not report a result');
assert.ok(result[1].startsWith('PASS:'),result[1]);
console.log(result[1]);
