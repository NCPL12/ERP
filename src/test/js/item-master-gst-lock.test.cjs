const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const test = require('node:test');

test('opening an existing item locks its GST for every editing role', () => {
  const source = fs.readFileSync('src/main/resources/static/js/itemMaster.js', 'utf8');
  const start = source.indexOf("$('#itemMasterList tbody').on('dblclick', 'tr', function () {");
  const end = source.indexOf('\n\t});', start) + 5;
  assert.ok(start >= 0 && end > start);
  for (const role of ['SUPER ADMIN', 'ADMIN', 'PURCHASE USER', 'STORE USER']) {
    const props = {};
    let open;
    const chain = {
      on(event, selector, handler) { open = handler; return this; },
      closest() { return this; }, get() { return {}; },
      prop(name, value) { (props[this.selector] ||= {})[name] = value; return this; },
      attr() { return this; }, val() { return this; }, select2() { return this; }, show() { return this; }, html() { return this; }
    };
    const $ = selector => Object.assign(Object.create(chain), { selector });
    $.each = (obj, fn) => Object.entries(obj).forEach(([key, value]) => fn(key, value));
    const row = { id: 'ITEM', gst: 18, item_units: null };
    const context = { $, role, canEditItemMaster: true, hasItemMasterAccess: () => true,
      getStockList() {}, setCostPrice() {}, datatable: { row: () => ({ length: 1, data: () => row }) } };
    vm.runInNewContext(source.slice(start, end), context);
    open.call({});
    assert.equal(props['#gst'].readonly, true, role);
  }
});
