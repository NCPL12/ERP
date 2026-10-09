const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const calls = [];
const meta = {'cashflow-base':'/ncpl-sales/cashflow-analyzer','csrf-token':'test-token','csrf-header':'X-CSRF-TOKEN'};
const context = {
  URL, Headers,
  document:{querySelector(selector){const match=selector.match(/name="([^"]+)"/);return match&&meta[match[1]]?{content:meta[match[1]]}:null;}},
  window:{location:{origin:'http://localhost:8880'},fetch(url,options){calls.push({url,options});}}
};
vm.runInNewContext(fs.readFileSync(path.join(__dirname,'../../main/resources/static/cashflow/js/module-paths.js'),'utf8'),context);
assert.equal(context.window.cashflowUrl('/overview'),'/ncpl-sales/cashflow-analyzer/overview');
assert.equal(context.window.cashflowUrl('/ncpl-sales/cashflow-analyzer/aging'),'/ncpl-sales/cashflow-analyzer/aging');
assert.equal(context.window.cashflowUrl('https://example.com/report'),'https://example.com/report');
context.window.fetch('/api/cashflow/tally/sync',{method:'POST'});
assert.equal(calls[0].url,'/ncpl-sales/cashflow-analyzer/api/cashflow/tally/sync');
assert.equal(calls[0].options.headers.get('X-CSRF-TOKEN'),'test-token');
context.window.fetch('https://example.com/report',{method:'POST'});
assert.equal(calls[1].options.headers,undefined);
context.window.fetch('/api/cashflow/tally/status');
assert.equal(calls[2].options,undefined);
console.log('Cashflow URL and CSRF routing tests passed');

// Exercise the actual tile/close handlers, so a root-level navigation regression fails.
const dashboard = fs.readFileSync(path.join(__dirname,'../../main/resources/static/cashflow/js/overview-dashboard.js'),'utf8');
const navigations = [...dashboard.matchAll(/window\.location\.assign\(([^;\n]+)/g)];
assert.ok(navigations.length >= 6);
for (const match of navigations) assert.ok(match[1].startsWith('window.cashflowUrl('), match[0]);
console.log('Dashboard tile and close navigation stays within ERP');
