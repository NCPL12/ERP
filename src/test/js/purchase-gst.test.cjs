const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const test = require('node:test');

function load(file) {
  let config;
  const chain = new Proxy({}, { get: (_, key) => {
    if (key === 'html') return () => 'Vendor';
    if (key === 'DataTable') return value => { config = value; return chain; };
    return () => chain;
  }});
  const $ = () => chain;
  $.each = (rows, fn) => rows.forEach((row, i) => fn(i, row));
  $.ajax = options => options.success([]);
  const context = vm.createContext({ $, document: {}, window: {}, console,
    purchaseOrderObj: [], itemList: [{ id: 'ITEM', gst: 12 }], api: { PURCHASE_LIST_BY_POID: '/items' } });
  vm.runInContext(fs.readFileSync(file, 'utf8'), context);
  return { context, config: () => config };
}

test('edit form submits saved GST after master changes, including zero and decimal rates', () => {
  const app = load('src/main/resources/static/js/purchaseOrder.js');
  app.context.editPoTable();
  const tax = app.config().aoColumns.find(column => column.title === 'Tax %');
  for (const rate of [18, 0, 0.25]) {
    const html = tax.render('ITEM', 'display', { gstRate: rate }, { row: 2 });
    assert.ok(html.includes(`value="${rate}"`));
    assert.ok(html.includes('name="items[2].gstRate"'));
  }
  const legacy = tax.render('ITEM', 'display', { gstRate: null }, { row: 0 });
  assert.ok(legacy.includes('value="12"'));
  assert.ok(legacy.includes('Historical GST not recorded'));
});

test('PO preview displays saved GST and explicitly marks unknown historical rates', () => {
  const app = load('src/main/resources/static/js/po-dashboard.js');
  app.context.getPurchasecopyItems('PO');
  const tax = app.config().aoColumns.find(column => column.mData === 'gstRate');
  assert.equal(tax.render(18), '18%');
  assert.equal(tax.render(0), '0%');
  assert.equal(tax.render(0.25), '0.25%');
  assert.match(tax.render(null), /Unknown/);
});
