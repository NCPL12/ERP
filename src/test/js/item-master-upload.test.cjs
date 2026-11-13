const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const test = require('node:test');

test('upload displays validation, server, and non-JSON errors as text', () => {
  const source = fs.readFileSync('src/main/resources/static/js/itemMaster.js', 'utf8');
  const start = source.indexOf("$(document).on('submit',\"#itemUploadForm\"");
  assert.ok(start >= 0);
  let submit, request;
  const messages = [];
  const container = { empty() { messages.length = 0; return this; }, append(p) { messages.push(p.value); } };
  const $ = selector => {
    if (selector === '#file') return [{ files: [{}] }];
    if (selector === '#response') return container;
    if (selector === '<p>') return { text(value) { this.value = value; return this; } };
    return { on(event, target, handler) { submit = handler; } };
  };
  $.ajax = options => { request = options; };
  vm.runInNewContext(source.slice(start), { $, document: {}, api: { ITEMS_UPLOAD: '/upload' },
    FormData: class { append() {} } });
  submit({ preventDefault() {} });
  for (const [xhr, expected] of [
    [{ responseJSON: { errors: ['Bad model', '<img src=x onerror=alert(1)>'] } }, ['Bad model', '<img src=x onerror=alert(1)>']],
    [{ responseJSON: { error: 'File is empty' } }, ['File is empty']],
    [{}, ['Upload failed. Please try again.']]
  ]) {
    request.error(xhr);
    assert.deepEqual(messages, expected);
  }
});
