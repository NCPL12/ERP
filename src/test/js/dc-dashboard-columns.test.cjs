const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.join(__dirname,
  '../../main/resources/static/js/dcDashboard.js'), 'utf8');
const stopAfterConfiguration = {};

for (const archivePage of [false, true]) {
  let config;
  const chain = {
    clone() { return this; },
    appendTo() { return this; },
    each() { return this; },
    DataTable(options) {
      config = options;
      throw stopAfterConfiguration;
    }
  };
  const document = {};
  function $(target) {
    return target === document ? { ready(fn) { fn(); } } : chain;
  }
  try {
    vm.runInNewContext(source, { $, document, archivePage });
  } catch (error) {
    if (error !== stopAfterConfiguration) throw error;
  }
  assert.ok(config, 'DC table configuration was captured');
  const view = archivePage ? 'archivedDcDashboard.jsp' : 'dcDashboard.jsp';
  const markup = fs.readFileSync(path.join(__dirname,
    '../../main/webapp/WEB-INF/views', view), 'utf8');
  const header = markup.match(/<table\b[^>]*id="dcList"[\s\S]*?<thead>([\s\S]*?)<\/thead>/)[1];
  const labels = [...header.matchAll(/<th\b[^>]*>([^<]*)<\/th>/g)].map(match => match[1].trim());
  assert.equal(labels.length, config.columns.length,
    `${view}: every DataTables column must have a header for width calculation`);
  const expected = [
    ['Dc No.', 'dcId'], ['SO Number', 'soNumber'], ['Client Name', 'clientName'],
    ['Client PO', 'clientPoNumber'], ['Shipping Address', 'shippingAddress'],
    ['Comment', 'dcComment'], ['Date', 'createdDate'], ['DC', 'pdf'],
    ['View', 'dcId'], ['Returnable', 'dcId'], ['Archive', 'archive']
  ];
  expected.forEach(([label, field], index) => {
    assert.equal(labels[index], label, `${view}: header ${index}`);
    assert.equal(config.columns[index].data || config.columns[index].mData, field,
      `${view}: data field for ${label}`);
  });
}
console.log('Active and archived DC headers match the DataTables column configuration');
