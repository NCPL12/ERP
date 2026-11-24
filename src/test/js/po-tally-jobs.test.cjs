const assert=require('assert');
const attach=require('../../main/resources/static/js/tallyPoExport.js');
const flush=()=>new Promise(resolve=>setImmediate(resolve));
async function check() {
  const calls=[],timers=[],rendered=[];
  let listener, nextJob={};
  let failPost=false,failPoll=false;
  const button={innerHTML:'Send to Tally',disabled:false,getAttribute:name=>name==='formaction'?'/po_list/tally/import':'true'};
  const form={dataset:{},addEventListener:(type,fn)=>listener=fn};
  const label={};
  const fetch=async(url,opts={})=>{
    calls.push([url,opts.method||'GET']);
    if(opts.method==='POST') {
      if(failPost) throw Error('lost response');
      return {ok:true,json:async()=>({jobId:'one',running:true,stage:'Queued'})};
    }
    if(failPoll) throw Error('offline');
    return {ok:true,json:async()=>nextJob};
  };
  const controller=attach({form,button,label,fetch,delay:fn=>{timers.push(fn);return timers.length;},cancel:()=>{},
    base:'/jobs',activeElement:()=>button,formData:()=>({}),render:r=>rendered.push(r)});
  await flush();
  const submit=()=>listener({submitter:button,preventDefault(){this.prevented=true;}});
  submit();await flush();
  assert.strictEqual(form.dataset.exportRunning,'true');assert.strictEqual(button.disabled,true);
  submit();await flush();assert.strictEqual(calls.filter(c=>c[1]==='POST').length,1);
  nextJob={jobId:'one',running:true,progress:{stage:'Reading Tally stock items',processed:0,total:81}};
  timers.shift()();await flush();assert(label.textContent.includes('Reading Tally stock items'));
  nextJob.progress={stage:'Verifying Tally results',processed:30,total:81,imported:18,updated:12};
  timers.shift()();await flush();assert(label.textContent.includes('Processed: 30/81'));assert(label.textContent.includes('Updated: 12'));
  failPoll=true;timers.shift()();await flush();assert(label.textContent.includes('Reconnecting'));assert.strictEqual(button.disabled,true);
  failPoll=false;nextJob={jobId:'one',running:false,result:{total:81,imported:80,skipped:1,failed:0,records:[]}};
  timers.shift()();await flush();assert.strictEqual(rendered.length,1);assert.strictEqual(button.disabled,false);
  assert.strictEqual(calls.filter(c=>c[1]==='POST').length,1);
  // A lost start response must only poll; it must never retry POST.
  failPost=true;nextJob={jobId:'two',running:true,stage:'Sending'};
  submit();await flush();assert.strictEqual(calls.filter(c=>c[1]==='POST').length,2);
  assert.strictEqual(form.dataset.exportRunning,'true');
  nextJob={jobId:'two',running:false,error:'Tally request timed out'};
  timers.shift()();await flush();assert(label.textContent.includes('Tally request timed out'));assert.strictEqual(button.disabled,false);
  assert.strictEqual(calls.filter(c=>c[1]==='POST').length,2);
  controller.stop();
  // Lost start response with no known job stays explicit, rather than claiming success.
  nextJob={};
  const unknown=attach({form,button,label,fetch,delay:()=>0,cancel:()=>{},base:'/jobs',activeElement:()=>button,formData:()=>({}),render:()=>{}});
  await flush();submit();await flush();
  assert(label.textContent.includes('No export status was found'));assert.strictEqual(button.disabled,true);unknown.stop();
  // Reopening the page attaches to a running job without any POST.
  calls.length=0;nextJob={jobId:'existing',running:true,stage:'Verifying'};
  const reopened=attach({form,button,label,fetch,delay:()=>0,cancel:()=>{},base:'/jobs',activeElement:()=>button,formData:()=>({}),render:()=>{}});
  await flush();assert.strictEqual(form.dataset.exportRunning,'true');assert(calls.every(c=>c[1]==='GET'));reopened.stop();
}
check().then(()=>console.log('PO background export tests passed: progress, duplicate guard, reconnect, completion, failure, no automatic resubmission.')).catch(e=>{console.error(e);process.exit(1);});
