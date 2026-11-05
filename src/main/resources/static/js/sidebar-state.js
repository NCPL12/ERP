/* Keep the sidebar steady while the server renders the next page. */
(function () {
    'use strict';

    // Only the persistent, visible sidebar owns navigation state.
    if (window.frameElement && window.frameElement.id === 'erpContentFrame') return;

    var sidebar = document.querySelector('.main-sidebar');
    if (!sidebar) return;

    var key = 'erp.sidebar:' + new URL(document.currentScript.src).pathname + ':' +
        sidebar.getAttribute('data-sidebar-user');
    var groups = sidebar.querySelectorAll('.nav-sidebar > .has-treeview');
    var scroller = sidebar.querySelector('.sidebar');
    var saved;
    try {
        saved = JSON.parse(sessionStorage.getItem(key));
    } catch (ignored) {
        // Storage can be disabled by the browser; navigation still works.
    }

    function groupKey(group) {
        var link = group.querySelector('.nav-treeview a[href]');
        return link ? link.getAttribute('href') : '';
    }

    if (saved) {
        document.body.classList.toggle('sidebar-collapse', saved.collapsed === true);
        Array.prototype.forEach.call(groups, function (group) {
            // Keep the destination's active group visible, as well as saved groups.
            var open = group.classList.contains('menu-open') ||
                (Array.isArray(saved.open) && saved.open.indexOf(groupKey(group)) !== -1);
            group.classList.toggle('menu-open', open);
            var menu = group.querySelector('.nav-treeview');
            if (menu) menu.style.display = open ? 'block' : 'none';
        });
        if (scroller) scroller.scrollTop = Number(saved.scrollTop) || 0;
    }

    function save() {
        var open = [];
        Array.prototype.forEach.call(groups, function (group) {
            if (group.classList.contains('menu-open')) open.push(groupKey(group));
        });
        try {
            sessionStorage.setItem(key, JSON.stringify({
                collapsed: document.body.classList.contains('sidebar-collapse'),
                open: open,
                scrollTop: scroller ? scroller.scrollTop : 0
            }));
        } catch (ignored) {}
    }

    // Save on actual navigation, including confirmed unsaved-change dialogs.
    window.addEventListener('pagehide', save);
    window.addEventListener('load', function () {
        if (saved && scroller) scroller.scrollTop = Number(saved.scrollTop) || 0;
        var toggle = document.getElementById('sidebarToggleBtn');
        var collapsed = document.body.classList.contains('sidebar-collapse');
        if (toggle) {
            toggle.setAttribute('aria-expanded', String(!collapsed));
            toggle.setAttribute('title', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
            toggle.setAttribute('aria-label', toggle.getAttribute('title'));
        }
    });
}());
