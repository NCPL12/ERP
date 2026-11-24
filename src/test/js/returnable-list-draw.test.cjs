const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const script = fs.readFileSync(
  path.join(__dirname, '../../main/resources/static/js/returnableItemList.js'),
  'utf8'
);

let config;
let drawHandler;
let requests = 0;
let adjustments = 0;

const table = {
  columns: {
    adjust() {
      adjustments++;
      return {
        draw() {
          requestPage();
        }
      };
    }
  },
  on(event, handler) {
    if (event === 'draw') drawHandler = handler;
  }
};

function requestPage() {
  if (++requests > 2) throw new Error('A table draw triggered repeated data requests');
  config.ajax({ start: 0, length: 50, draw: requests, search: { value: '' } }, () => {
    if (drawHandler) drawHandler();
  }, {});
}

const chain = {
  on() { return this; },
  css() { return this; },
  show() { return this; },
  detach() { return this; },
  prepend() { return this; },
  find() { return this; },
  DataTable(options) { config = options; return table; }
};
const document = {};
const window = {};
function $(target) {
  if (target === document) return { ready(fn) { fn(); }, on() {} };
  return chain;
}
$.ajax = ({ success }) => success({ totalElements: 0, content: [] });

vm.runInNewContext(script, { $, document, window, pageContext: '/ncpl-sales', console, setTimeout });
requestPage();
assert.equal(requests, 1, 'initial draw should request one page');
assert.equal(adjustments, 1, 'initial draw should adjust column widths');
assert.equal(config.processing, false, 'the table should not show a processing overlay');
assert.equal(config.language.loadingRecords, '', 'the table should not show a loading message');
console.log('Returnable list draw requests one page');
