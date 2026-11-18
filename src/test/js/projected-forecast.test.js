const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict'),path=require('node:path');
const elements={};const document={getElementById(id){return elements[id] ||= {textContent:'',hidden:false};},addEventListener(){}};
let chartConfig;
const context={document,window:{},URLSearchParams,Intl,Date,Map,setTimeout,Chart:function(_canvas,config){chartConfig=config;this.destroy=()=>{};}};
let source=fs.readFileSync(path.join(__dirname,'../../main/resources/static/cashflow/js/cashflow-forecast.js'),'utf8');
source=source.replace(/\}\)\(\);\s*$/, 'window.testForecast={summary,renderChart};})();');vm.runInNewContext(source,context);
const periods=[{weekStart:'2026-10-09',weekEnd:'2026-10-11',receivables:500,payables:200,closingBalance:1300,bills:[]}];
const data={from:'2026-10-09',periods,openingBalance:1000,closingBalance:1300,totalReceivables:500,totalPayables:200,finalCumulative:300,backlogBills:[],unscheduledCount:2};
context.window.testForecast.summary(data);context.window.testForecast.renderChart(data);
assert.equal(chartConfig.data.labels[0],'Opening');assert.deepEqual(Array.from(chartConfig.data.datasets[2].data),[1000,1300]);
assert.equal(chartConfig.data.datasets[2].tension,0);assert.ok(elements.forecastReconciliation.textContent.includes('1,000'));assert.equal(elements.forecastWarning.hidden,false);
chartConfig.options.onClick(null,[{index:0}]); // Opening marker must not drill into invoices.
context.window.testForecast.summary({...data,openingBalance:null,closingBalance:null});assert.equal(elements.forecastNet.textContent,'Not set');
context.window.testForecast.renderChart({...data,openingBalance:null,periods:[{...periods[0],closingBalance:null}]});assert.ok(chartConfig.data.datasets[2].data.every(x=>x===null));
console.log('Projected forecast chart: opening marker, balance line, missing balance, warnings and opening click passed.');
