// Browser integration test: node src/test/js/sidebar-navigation.browser.test.cjs
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const os = require('node:os');
const { spawn } = require('node:child_process');
const root = path.resolve(__dirname, '../../main/resources/static');
const pause = ms => new Promise(resolve => setTimeout(resolve, ms));
const salesScript = fs.readFileSync(path.join(root, 'js/salesOrder.js'), 'utf8');
const salesClickStart = salesScript.indexOf('$(document).on("click",".design",function(event){');
const salesClickEnd = salesScript.indexOf('$(document).on("change","[id^=\'itemModel\']"', salesClickStart);
assert.ok(salesClickStart >= 0 && salesClickEnd > salesClickStart);
const salesDesignClick = salesScript.slice(salesClickStart, salesClickEnd);

function fixture(route) {
    return `<!doctype html><html><head><title>${route}</title>
    <link rel="stylesheet" href="/resources/dist/css/adminlte.min.css">
    <link rel="stylesheet" href="/resources/css/sidebar-navigation.css">
    <link rel="stylesheet" href="/resources/css/common.css">
    <script src="/resources/js/sidebar-navigation.js"></script>
    <script src="/resources/plugins/jquery/jquery.min.js"></script>
    <script src="/resources/plugins/bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="/resources/plugins/bootbox/bootbox.min.js"></script>
    <script src="/resources/dist/js/adminlte.js"></script></head>
    <body class="sidebar-mini sidebar-mini-md layout-fixed"><div class="wrapper">
    <nav class="main-header"><a id="sidebarToggleBtn" data-widget="pushmenu" href="#">Toggle</a></nav>
    <aside class="main-sidebar sidebar-dark-primary sidebar-no-expand"><div class="sidebar"><ul class="nav nav-pills nav-sidebar flex-column nav-collapse-hide-child" data-widget="treeview">
    <li class="nav-item has-treeview menu-open"><a class="nav-link" href="#"><svg class="nav-icon" width="24" height="24"><rect width="24" height="24" fill="white"/></svg><p>Group</p></a><ul class="nav-treeview">
    ${['first', 'second', 'third'].map(name => `<li><a id="${name}" class="nav-link ${route === '/' + name ? 'active' : ''}" href="/${name}">${name}</a></li>`).join('')}
    </ul></li></ul></div></aside><main class="content-wrapper">
    <input id="page-input"><a id="content-link" href="/third">Open third</a>
    <a id="logout" href="/logout">Logout</a></main></div>
    <div id="test-modal" class="modal" tabindex="-1"><div class="modal-dialog modal-lg" style="margin-left:33%;margin-top:0">
    <div class="modal-content" style="width:520px"><div class="modal-body" style="height:160px">Popup</div></div></div></div>
    <table><tr><td><input value="2"></td><td><input value="Small Almirah"></td>
    <td><button class="design" value="123">Design</button></td></tr></table>
    <div id="designModal" class="modal fade" tabindex="-1"><div class="modal-dialog">
    <div class="modal-content" style="width:600px"><h5 id="designHeader"></h5>
    <form id="designForm"><input id="salesItemId"></form><table id="designTable"><tbody><tr><td>Model</td></tr></tbody></table>
    </div></div></div>
    <script>window.initialized = true; window.isDirty = false; window.realBootbox = window.bootbox;
    window.bootbox = { confirm(options) { window.prompted = true; options.callback(window.allowLeave); } };
    function getDesignItemList() {}
    ${salesDesignClick};
    </script></body></html>`;
}

const server = http.createServer((req, res) => {
    if (req.url.startsWith('/resources/')) {
        const filename = path.resolve(root, req.url.slice('/resources/'.length));
        if (!filename.startsWith(root + path.sep) || !fs.existsSync(filename)) {
            res.writeHead(404).end(); return;
        }
        res.setHeader('Content-Type', filename.endsWith('.css') ? 'text/css' : 'text/javascript');
        res.end(fs.readFileSync(filename)); return;
    }
    if (req.url === '/logout') { res.writeHead(302, { Location: '/login' }).end(); return; }
    res.setHeader('Content-Type', 'text/html');
    res.setHeader('X-Frame-Options', 'SAMEORIGIN');
    res.end(req.url === '/login' ? '<title>Login</title><h1>Login</h1>' : fixture(req.url));
});

(async function () {
    let chrome;
    let socket;
    try {
        await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
        const base = 'http://127.0.0.1:' + server.address().port;
        const executable = process.env.CHROME_PATH || 'C:/Program Files/Google/Chrome/Application/chrome.exe';
        const profile = fs.mkdtempSync(path.join(os.tmpdir(), 'erp-sidebar-test-'));
        chrome = spawn(executable, ['--headless=new', '--no-first-run', '--disable-extensions',
            '--disable-background-networking', '--remote-debugging-port=0', '--user-data-dir=' + profile,
            'about:blank'], { windowsHide: true, stdio: ['ignore', 'ignore', 'pipe'] });
        const debuggerUrl = await new Promise((resolve, reject) => {
            let stderr = '';
            const timer = setTimeout(() => reject(new Error('Chrome debugger did not start')), 15000);
            chrome.once('error', reject);
            chrome.stderr.on('data', data => {
                stderr += data.toString();
                const match = stderr.match(/DevTools listening on (ws:\/\/\S+)/);
                if (match) { clearTimeout(timer); resolve(match[1]); }
            });
        });
        const debugOrigin = debuggerUrl.replace(/^ws:/, 'http:').split('/devtools/')[0];
        const pages = await (await fetch(debugOrigin + '/json/list')).json();
        socket = new WebSocket(pages.find(page => page.type === 'page').webSocketDebuggerUrl);
        await new Promise(resolve => socket.addEventListener('open', resolve, { once: true }));
        let id = 0;
        const calls = new Map();
        socket.addEventListener('message', ({ data }) => {
            const message = JSON.parse(data);
            if (calls.has(message.id)) {
                calls.get(message.id)(message);
                calls.delete(message.id);
            }
        });
        function cdp(method, params) {
            return new Promise((resolve, reject) => {
                const timer = setTimeout(() => reject(new Error('CDP timeout: ' + method)), 10000);
                calls.set(++id, message => { clearTimeout(timer); resolve(message); });
                socket.send(JSON.stringify({ id, method, params }));
            });
        }
        async function evaluate(expression) {
            const response = await cdp('Runtime.evaluate', { expression, returnByValue: true });
            if (response.result.exceptionDetails) throw new Error(JSON.stringify(response.result.exceptionDetails));
            return response.result.result.value;
        }
        async function until(expression) {
            for (let i = 0; i < 100; i++) {
                if (await evaluate(expression)) return;
                await pause(50);
            }
            throw new Error('Timed out: ' + expression);
        }
        async function checkPopup(embedded, bootbox = false, salesDesign = false) {
            const page = embedded ? 'document.getElementById("erpContentFrame").contentWindow' : 'window';
            const modalSelector = bootbox ? '.bootbox' : salesDesign ? '#designModal' : '#test-modal';
            await evaluate('void ' + page + (bootbox ? '.realBootbox.alert("Confirmation")' : salesDesign ? '.jQuery(".design").trigger("click")' : '.jQuery("#test-modal").modal("show")'));
            const selector = modalSelector + ' .modal-content';
            await until('!!' + page + '.document.querySelector(' + JSON.stringify(selector) + ')');
            await pause(700);
            await until('!' + page + '.jQuery(' + JSON.stringify(modalSelector) + ').data("bs.modal")._isTransitioning');
            const geometry = await evaluate(`(() => {
                const page = ${page};
                const popup = page.document.querySelector(${JSON.stringify(selector)}).getBoundingClientRect();
                const frame = document.getElementById('erpContentFrame');
                const offset = ${embedded} ? frame.getBoundingClientRect().left : 0;
                return { x: offset + popup.left + popup.width / 2, y: popup.top + popup.height / 2,
                    width: popup.width, viewportWidth: innerWidth, viewportHeight: innerHeight,
                    contentLeft: ${embedded} ? offset + page.document.querySelector('.content-wrapper').getBoundingClientRect().left : 0,
                    sidebarRight: document.querySelector('.main-sidebar').getBoundingClientRect().right };
            })()`);
            assert.ok(Math.abs(geometry.x - geometry.viewportWidth / 2) < 10, 'Popup is horizontally centered: ' + JSON.stringify(geometry));
            assert.ok(Math.abs(geometry.y - geometry.viewportHeight / 2) < 10, 'Popup is vertically centered: ' + JSON.stringify(geometry));
            assert.ok(geometry.width <= geometry.viewportWidth, 'Popup fits the viewport');
            if (embedded) {
                assert.ok(Math.abs(geometry.contentLeft - Math.max(0, geometry.sidebarRight)) < 1, 'Opening a popup does not move page content: ' + JSON.stringify(geometry));
                assert.equal(await evaluate('document.getElementById("erpContentFrame").classList.contains("erp-popup-open")'), true);
                const backgrounds = await evaluate(`(() => {
                    const frame = document.getElementById('erpContentFrame');
                    return [frame, frame.contentDocument.documentElement, frame.contentDocument.body]
                        .map(el => getComputedStyle(el).backgroundColor);
                })()`);
                assert.deepEqual(backgrounds, Array(3).fill('rgba(0, 0, 0, 0)'), 'Sidebar remains visible beneath the popup backdrop');
                if (!bootbox && geometry.viewportWidth > 1000) {
                    const screenshot = await cdp('Page.captureScreenshot', { format: 'png' });
                    const output = path.resolve(__dirname, '../../../target/sidebar-popup-preview.png');
                    fs.writeFileSync(output, Buffer.from(screenshot.result.data, 'base64'));
                }
            }
            await until('!' + page + '.jQuery(' + JSON.stringify(modalSelector) + ').data("bs.modal")._isTransitioning');
            await evaluate('void ' + page + '.jQuery(' + JSON.stringify(modalSelector) + ').modal("hide")');
            await pause(200);
            if (embedded) await until('!document.getElementById("erpContentFrame").classList.contains("erp-popup-open")');
        }
        await cdp('Emulation.setDeviceMetricsOverride', { width: 1440, height: 900, deviceScaleFactor: 1, mobile: false });
        await cdp('Page.navigate', { url: base + '/first' });
        await until('window.initialized === true');
        await checkPopup(false);
        await evaluate('window.originalSidebar = document.querySelector(".main-sidebar"); window.shellToken = 123; document.getElementById("second").click()');
        await until('location.pathname === "/second" && !!document.getElementById("erpContentFrame").contentWindow.initialized');
        assert.equal(await evaluate('window.shellToken === 123 && originalSidebar === document.querySelector(".main-sidebar")'), true);
        assert.equal(await evaluate('getComputedStyle(document.getElementById("erpContentFrame").contentDocument.querySelector(".main-sidebar")).display'), 'none');
        assert.equal(await evaluate('getComputedStyle(document.getElementById("erpContentFrame").contentDocument.querySelector(".content-wrapper")).marginLeft'), '0px');
        assert.equal(await evaluate('getComputedStyle(document.getElementById("erpContentFrame").contentDocument.querySelector(".main-header")).marginLeft'), '0px');
        await checkPopup(true);
        await checkPopup(true, true);
        await evaluate('document.getElementById("erpContentFrame").contentDocument.querySelector(".wrapper").style.zoom = "80%"');
        await checkPopup(true);
        await checkPopup(true, false, true);
        await evaluate('document.getElementById("erpContentFrame").contentDocument.querySelector(".wrapper").style.zoom = ""');
        await evaluate('document.getElementById("erpContentFrame").contentWindow.isDirty = true; document.getElementById("third").click()');
        assert.equal(await evaluate('location.pathname'), '/second', 'Cancel keeps the current content');
        assert.equal(await evaluate('document.getElementById("erpContentFrame").contentWindow.prompted'), true);
        await evaluate('document.getElementById("erpContentFrame").contentWindow.allowLeave = true; document.getElementById("third").click()');
        await until('location.pathname === "/third"');
        await evaluate('history.back()');
        await until('location.pathname === "/second" && document.getElementById("erpContentFrame").contentWindow.location.pathname === "/second"');
        await evaluate('history.forward()');
        await until('location.pathname === "/third" && document.getElementById("erpContentFrame").contentWindow.location.pathname === "/third"');
        await until('!!document.getElementById("erpContentFrame").contentDocument.getElementById("sidebarToggleBtn")');
        await evaluate('document.getElementById("erpContentFrame").contentDocument.getElementById("sidebarToggleBtn").click()');
        assert.equal(await evaluate('document.body.classList.contains("sidebar-collapse")'), true);
        await pause(400);
        await cdp('Input.dispatchMouseEvent', { type: 'mouseMoved', x: 1000, y: 50 });
        const collapsedMenuHeight = await evaluate('document.querySelector(".nav-sidebar").getBoundingClientRect().height');
        await cdp('Input.dispatchMouseEvent', { type: 'mouseMoved', x: 35, y: 25 });
        await pause(400);
        assert.equal(await evaluate('document.querySelector(".nav-sidebar").getBoundingClientRect().height'), collapsedMenuHeight, 'Hover does not reveal collapsed submenus or move icons');
        assert.equal(await evaluate('getComputedStyle(document.querySelector(".nav-treeview")).display'), 'none');
        await evaluate('document.querySelector(".has-treeview > .nav-link").focus()');
        assert.equal(await evaluate('getComputedStyle(document.querySelector(".nav-treeview")).display'), 'none', 'Keyboard focus also keeps collapsed submenus hidden');
        const railGeometry = await evaluate(`(() => {
            const rail = document.querySelector('.main-sidebar').getBoundingClientRect();
            const button = document.querySelector('.has-treeview > .nav-link').getBoundingClientRect();
            const icon = document.querySelector('.has-treeview > .nav-link .nav-icon').getBoundingClientRect();
            return { left: rail.left, right: rail.right, buttonLeft: button.left, buttonRight: button.right,
                iconLeft: icon.left, iconRight: icon.right };
        })()`);
        assert.ok(railGeometry.iconLeft >= railGeometry.left && railGeometry.iconRight <= railGeometry.right, 'Sidebar icon stays inside its rail: ' + JSON.stringify(railGeometry));
        assert.ok(Math.abs((railGeometry.iconLeft + railGeometry.iconRight) / 2 - (railGeometry.left + railGeometry.right) / 2) < 1, 'Sidebar icon is centered: ' + JSON.stringify(railGeometry));
        await evaluate('document.activeElement.blur()');
        await cdp('Input.dispatchMouseEvent', { type: 'mouseMoved', x: 1000, y: 50 });
        for (const wasOpen of [true, false]) {
            await evaluate('document.querySelector(".has-treeview").classList.toggle("menu-open", ' + wasOpen + ')');
            await evaluate('document.querySelector(".has-treeview > .nav-link .nav-icon").dispatchEvent(new MouseEvent("click", {bubbles:true, button:0}))');
            assert.equal(await evaluate('document.body.classList.contains("sidebar-collapse")'), false, 'Clicking a collapsed section expands the sidebar');
            assert.equal(await evaluate('getComputedStyle(document.querySelector(".nav-treeview")).display'), 'block', 'Clicked section opens even when previously closed');
            assert.equal(await evaluate('document.querySelector(".has-treeview").classList.contains("menu-open")'), true);
            assert.equal(await evaluate('document.getElementById("erpContentFrame").contentDocument.getElementById("sidebarToggleBtn").getAttribute("aria-expanded")'), 'true');
            await pause(400);
            assert.equal(await evaluate('Math.round(document.getElementById("erpContentFrame").getBoundingClientRect().left)'), 240);
            assert.equal(await evaluate('window.shellToken'), 123, 'Opening a section does not reload the shell');
            await evaluate('document.getElementById("erpContentFrame").contentDocument.getElementById("sidebarToggleBtn").click()');
            await pause(400);
        }
        assert.equal(await evaluate('getComputedStyle(document.getElementById("erpContentFrame").contentDocument.querySelector(".content-wrapper")).marginLeft'), '0px');
        assert.equal(await evaluate('Math.abs(document.getElementById("erpContentFrame").getBoundingClientRect().left - document.querySelector(".main-sidebar").getBoundingClientRect().right) < 1'), true, 'Content frame touches the collapsed sidebar');
        await checkPopup(true);
        await evaluate('document.getElementById("erpContentFrame").contentDocument.body.classList.add("sidebar-collapse")');
        assert.equal(await evaluate('getComputedStyle(document.getElementById("erpContentFrame").contentDocument.querySelector(".content-wrapper")).marginLeft'), '0px', 'Child collapse classes cannot add another sidebar offset');
        await evaluate('document.getElementById("second").click()');
        await until('location.pathname === "/second"');
        await evaluate('document.getElementById("erpContentFrame").contentDocument.getElementById("content-link").click()');
        await until('location.pathname === "/third"');
        assert.equal(await evaluate('originalSidebar === document.querySelector(".main-sidebar")'), true);
        await cdp('Emulation.setDeviceMetricsOverride', { width: 390, height: 844, deviceScaleFactor: 1, mobile: true });
        await evaluate('document.getElementById("erpContentFrame").contentDocument.getElementById("sidebarToggleBtn").click()');
        assert.equal(await evaluate('document.body.classList.contains("sidebar-open")'), true);
        await evaluate('document.getElementById("second").click()');
        await until('location.pathname === "/second"');
        assert.equal(await evaluate('document.body.classList.contains("sidebar-open")'), false);
        await checkPopup(true);
        await evaluate('document.getElementById("erpContentFrame").contentWindow.location.replace("/login")');
        await until('location.pathname === "/login" && !document.getElementById("erpContentFrame")');
        await cdp('Page.navigate', { url: base + '/first' });
        await until('window.initialized === true');
        await evaluate('document.getElementById("second").click()');
        await until('location.pathname === "/second"');
        await evaluate('document.getElementById("erpContentFrame").contentDocument.getElementById("logout").click()');
        await until('location.pathname === "/login" && !document.getElementById("erpContentFrame")');
        console.log('Browser checks passed: centered popups and confirmations, persistent sidebar, isolated content, dirty forms, Back/Forward, desktop/mobile toggle, content links, session expiry, logout.');
    } finally {
        if (socket) socket.close();
        if (chrome) chrome.kill();
        server.close();
    }
}()).catch(error => { console.error(error); process.exitCode = 1; });
