const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const script = fs.readFileSync(path.join(__dirname,
    '../../main/resources/static/js/sidebar-state.js'), 'utf8');

function classes(initial = []) {
    const values = new Set(initial);
    return {
        contains: value => values.has(value),
        toggle(value, enabled) { enabled ? values.add(value) : values.delete(value); }
    };
}

function run(saved, unavailable = false, user = 'admin') {
    const handlers = {};
    const menus = ['partyList', 'purchase', 'salesList'].map((route, index) => ({
        classList: classes(index === 1 ? ['menu-open'] : []),
        menu: { style: {} },
        querySelector(selector) {
            return selector === '.nav-treeview' ? this.menu :
                { getAttribute: () => '/ncpl-sales/' + route };
        }
    }));
    const scroller = { scrollTop: 0 };
    const sidebar = {
        getAttribute: () => user,
        querySelectorAll: () => menus,
        querySelector: () => scroller
    };
    const body = { classList: classes() };
    const attributes = {};
    const toggle = {
        setAttribute(name, value) { attributes[name] = value; },
        getAttribute(name) { return attributes[name]; }
    };
    let written;
    let readKey;
    vm.runInNewContext(script, {
        URL,
        document: {
            body,
            currentScript: { src: 'http://localhost/ncpl-sales/resources/js/sidebar-state.js' },
            querySelector: () => sidebar,
            getElementById: () => toggle
        },
        window: { addEventListener(name, handler) { handlers[name] = handler; } },
        sessionStorage: {
            getItem(key) {
                if (unavailable) throw new Error('Storage disabled');
                readKey = key;
                return saved;
            },
            setItem(key, value) {
                if (unavailable) throw new Error('Storage disabled');
                written = { key, state: JSON.parse(value) };
            }
        }
    });
    return { handlers, menus, scroller, body, attributes,
        readKey, written: () => written };
}

const restored = run(JSON.stringify({ collapsed: true,
    open: ['/ncpl-sales/partyList'], scrollTop: 170 }));
assert.equal(restored.body.classList.contains('sidebar-collapse'), true);
assert.equal(restored.menus[0].menu.style.display, 'block');
assert.equal(restored.menus[1].menu.style.display, 'block', 'Destination group stays visible');
assert.equal(restored.menus[2].menu.style.display, 'none');
assert.equal(restored.scroller.scrollTop, 170);
restored.handlers.load();
assert.equal(restored.attributes['aria-expanded'], 'false');
restored.menus[0].classList.toggle('menu-open', false);
restored.scroller.scrollTop = 50;
restored.handlers.pagehide();
assert.deepEqual(restored.written().state,
    { collapsed: true, open: ['/ncpl-sales/purchase'], scrollTop: 50 });
assert.notEqual(restored.readKey, run(null, false, 'other-user').readKey);

for (const state of [null, 'invalid json', '{"open":42}']) {
    const page = run(state);
    page.handlers.load();
    page.handlers.pagehide();
}
const blockedStorage = run(null, true);
blockedStorage.handlers.load();
blockedStorage.handlers.pagehide();
console.log('Sidebar state restoration, navigation persistence, and storage fallback passed.');
