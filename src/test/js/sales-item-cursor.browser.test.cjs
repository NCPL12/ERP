const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const { spawn } = require('node:child_process');
const root = path.resolve(__dirname, '../../main');
const sales = fs.readFileSync(path.join(root, 'resources/static/js/salesOrder.js'), 'utf8');
const jsp = fs.readFileSync(path.join(root, 'webapp/WEB-INF/views/welcome.jsp'), 'utf8');
const selection = sales.slice(sales.indexOf('// ==================== MULTI-CELL SELECT'));
assert.ok(selection.includes('salesTableSelection'));
const completion = sales.slice(sales.indexOf('function updateDesignCompletion('),
    sales.indexOf('//ajax call to get all the design item list'));
const html = `<style>${fs.readFileSync(path.join(root, 'resources/static/css/salesOrder.css'), 'utf8')}
${jsp.match(/\.CellWithComment\{[\s\S]*?(?=\.modal-header)/)[0]}</style>
<table id="salesTable" style="width:600px"><tbody><tr>
<td class="CellWithComment"><input type="text" id="description0" value="Editable description">
<span class="CellComment">${'Long description preview '.repeat(50)}</span></td>
<td><input type="text" id="modelNo0" value="MODEL-123"></td>
<td><input type="text" id="amount0" value="100.00" readonly></td>
<td></td><td></td>
<td><input type="checkbox" id="toggleBrief0">
<a href="#" class="design" id="design0">Design</a>
<a href="#" class="design" id="design1">Design</a></td></tr></tbody></table>
<script>${fs.readFileSync(path.join(root, 'resources/static/plugins/jquery/jquery.min.js'), 'utf8')}</script>
<script>${completion}\n${selection}</script>`;

(async () => {
    let chrome, socket;
    const profile = fs.mkdtempSync(path.join(os.tmpdir(), 'erp-sales-cursor-'));
    try {
        chrome = spawn(process.env.CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe',
            ['--headless=new', '--no-first-run', '--disable-extensions', '--disable-background-networking',
                '--remote-debugging-port=0', '--user-data-dir=' + profile, 'about:blank'],
            { windowsHide: true, stdio: ['ignore', 'ignore', 'pipe'] });
        const url = await new Promise((resolve, reject) => {
            let output = '';
            const timer = setTimeout(() => reject(new Error('Chrome did not start')), 15000);
            chrome.once('error', error => { clearTimeout(timer); reject(error); });
            chrome.stderr.on('data', chunk => {
                output += chunk;
                const match = output.match(/DevTools listening on (ws:\/\/\S+)/);
                if (match) { clearTimeout(timer); resolve(match[1]); }
            });
        });
        const origin = url.replace('ws:', 'http:').split('/devtools/')[0];
        const pages = await (await fetch(origin + '/json/list')).json();
        socket = new WebSocket(pages.find(page => page.type === 'page').webSocketDebuggerUrl);
        await new Promise(resolve => socket.addEventListener('open', resolve, { once: true }));
        let id = 0;
        const pending = new Map();
        socket.addEventListener('message', ({ data }) => {
            const message = JSON.parse(data);
            if (pending.has(message.id)) { pending.get(message.id)(message); pending.delete(message.id); }
        });
        function cdp(method, params = {}) {
            return new Promise((resolve, reject) => {
                const timer = setTimeout(() => reject(new Error(method + ' timed out')), 10000);
                pending.set(++id, message => {
                    clearTimeout(timer);
                    if (message.error) reject(new Error(JSON.stringify(message.error)));
                    else resolve(message.result);
                });
                socket.send(JSON.stringify({ id, method, params }));
            });
        }
        async function evaluate(expression) {
            const result = await cdp('Runtime.evaluate', { expression, returnByValue: true, awaitPromise: true });
            if (result.exceptionDetails) throw new Error(JSON.stringify(result.exceptionDetails));
            return result.result.value;
        }
        await cdp('Page.enable');
        const { frameTree } = await cdp('Page.getFrameTree');
        await cdp('Page.setDocumentContent', { frameId: frameTree.frame.id, html });
        async function point(id) {
            return evaluate(`(() => {const r=document.getElementById('${id}').getBoundingClientRect();
                return {x:r.x+r.width/2,y:r.y+r.height/2};})()`);
        }
        async function click(id) {
            const position = await point(id);
            await cdp('Input.dispatchMouseEvent', { type: 'mouseMoved', ...position });
            await cdp('Input.dispatchMouseEvent', { type: 'mousePressed', button: 'left', clickCount: 1, ...position });
            await cdp('Input.dispatchMouseEvent', { type: 'mouseReleased', button: 'left', clickCount: 1, ...position });
            await evaluate('new Promise(resolve => setTimeout(resolve, 30))');
        }
        const before = await point('description0');
        await cdp('Input.dispatchMouseEvent', { type: 'mouseMoved', ...before });
        assert.deepEqual(await point('description0'), before, 'Hover must not move the description away from the cursor');
        await click('description0');
        assert.equal(await evaluate('document.activeElement.id'), 'description0');
        const textStart = await evaluate(`(() => {const r=description0.getBoundingClientRect();
            return {x:r.x+3,y:r.y+r.height/2};})()`);
        await cdp('Input.dispatchMouseEvent', {type:'mousePressed',button:'left',clickCount:1,...textStart});
        await cdp('Input.dispatchMouseEvent', {type:'mouseReleased',button:'left',clickCount:1,...textStart});
        assert.equal(await evaluate('description0.selectionStart'), 0, 'Click positions the caret inside existing text');
        await cdp('Input.insertText', { text: 'typed' });
        assert.ok((await evaluate('description0.value')).startsWith('typed'));
        await click('modelNo0');
        assert.equal(await evaluate('document.activeElement.id'), 'modelNo0');
        await click('amount0');
        assert.equal(await evaluate('document.activeElement.id'), 'amount0', 'Readonly amounts retain focus for copying');
        await click('description0');
        await click('toggleBrief0');
        assert.equal(await evaluate('document.activeElement.id'), 'toggleBrief0', 'Checkbox must retain focus');
        await click('description0');
        await evaluate(`document.getElementById('modelNo0').dispatchEvent(new MouseEvent('mousedown',
            {bubbles:true,button:0,shiftKey:true}))`);
        assert.equal(await evaluate('salesGetFillableCells().length'), 2, 'Shift selection still works');
        await evaluate(`$('#design0').val('ITEM-1'); $('#design1').val('ITEM-2');
            updateDesignCompletion('ITEM-1', [{id:'DESIGN-1'}]);`);
        const tick = () => evaluate(`getComputedStyle(design0, '::after').content`);
        assert.equal(await tick(), '"\u2713"', 'Saved design displays a tick');
        assert.equal(await evaluate(`getComputedStyle(design1, '::after').content`), 'none', 'Other items stay unticked');
        await evaluate(`updateDesignCompletion('ITEM-1', [{id:'DESIGN-1'}]);`);
        assert.equal(await tick(), '"\u2713"', 'Refreshing does not duplicate the tick');
        await evaluate(`updateDesignCompletion('ITEM-1', []);`);
        assert.equal(await tick(), 'none', 'Deleting the last design removes the tick');
        console.log('Passed: item cursor, field focus, Shift selection, saved design tick, and tick removal.');
    } finally {
        if (socket) socket.close();
        if (chrome) chrome.kill();
    }
})().catch(error => { console.error(error); process.exitCode = 1; });
