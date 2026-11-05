/* A persistent sidebar with isolated, normally rendered JSP content pages. */
(function () {
    'use strict';
    var embedded = window.frameElement && window.frameElement.id === 'erpContentFrame';
    if (embedded) {
        document.documentElement.classList.add('erp-content-frame');
        document.addEventListener('DOMContentLoaded', function () {
            window.jQuery(document).on('show.bs.modal', function (event) {
                if (event.isDefaultPrevented()) return;
                window.parent.erpSidebarNavigation.setPopupOpen(true);
                // Bootbox removes its node on close before the event reaches document.
                window.jQuery(event.target).off('hidden.bs.modal.erpViewport')
                    .one('hidden.bs.modal.erpViewport', function () {
                        window.parent.erpSidebarNavigation.setPopupOpen(
                            window.jQuery('.modal:visible').not(this).length > 0);
                    });
            });
        });
        // The visible toggle lives in the content page's contextual header.
        document.addEventListener('click', function (event) {
            if (event.target.closest('[data-widget="pushmenu"]')) {
                event.preventDefault();
                event.stopImmediatePropagation();
                window.parent.erpSidebarNavigation.toggle();
            }
            var link = event.target.closest('a[href]');
            if (link && /\/logout(?:[?#]|$)/.test(link.href)) {
                event.preventDefault();
                window.parent.location.assign(link.href);
            }
        }, true);
        return;
    }

    var frame;
    var sidebar;
    var pending;
    var backNavigation = false;
    var popupOpen = false;

    function positionFrame() {
        if (!frame) return;
        var edge = Math.max(0, sidebar.getBoundingClientRect().right);
        frame.style.left = (popupOpen ? 0 : edge) + 'px';
        frame.style.width = popupOpen ? '100%' : 'calc(100% - ' + edge + 'px)';
        frame.classList.toggle('erp-popup-open', popupOpen);
        var root = frame.contentDocument && frame.contentDocument.documentElement;
        if (root) {
            root.classList.toggle('erp-popup-viewport', popupOpen);
            root.style.setProperty('--erp-sidebar-offset', edge + 'px');
        }
    }

    function pageWindow() {
        return frame ? frame.contentWindow : window;
    }

    function confirmLeaving(accept, cancel) {
        var page = pageWindow();
        if (page.isDirty !== true) { accept(); return; }
        var message = 'The current changes are not saved. Are you sure to leave this page without save?';
        if (page.bootbox) {
            page.bootbox.confirm({ message: message, callback: function (confirmed) {
                if (confirmed) { page.isDirty = false; accept(); }
                else if (cancel) cancel();
            } });
        } else if (page.confirm(message)) {
            page.isDirty = false;
            accept();
        } else if (cancel) cancel();
    }

    function syncPage() {
        var page = frame.contentWindow;
        // Session expiry must replace the shell, so an old user's menu is removed.
        if (/\/login(?:[/?#]|$)/.test(page.location.href)) {
            window.location.replace(page.location.href);
            return;
        }
        document.title = page.document.title;
        var current = page.location.href;
        if (current !== window.location.href) {
            if (pending && !backNavigation) window.history.pushState(null, '', current);
            else window.history.replaceState(null, '', current);
        }
        pending = null;
        backNavigation = false;

        // Copy only active-link markers; keep existing sidebar nodes and open menus.
        var active = page.document.querySelectorAll('.main-sidebar a.nav-link.active');
        var paths = Array.prototype.map.call(active, function (link) { return link.getAttribute('href'); });
        Array.prototype.forEach.call(sidebar.querySelectorAll('a.nav-link'), function (link) {
            var href = link.getAttribute('href');
            if (href !== '#') link.classList.toggle('active', paths.indexOf(href) !== -1);
        });
        Array.prototype.forEach.call(sidebar.querySelectorAll('.nav-sidebar > .has-treeview'), function (group) {
            var selected = group.querySelector('.nav-treeview .nav-link.active');
            var heading = group.querySelector(':scope > .nav-link');
            if (heading) heading.classList.toggle('active', !!selected);
            if (selected) {
                group.classList.add('menu-open');
                group.querySelector('.nav-treeview').style.display = 'block';
            }
        });
        // Keep browser Back/Forward in sync with links and redirects inside a page.
        page.addEventListener('popstate', function () {
            window.history.replaceState(null, '', page.location.href);
        });
        page.document.addEventListener('click', function (event) {
            var link = event.target.closest('a[href]');
            if (!link || event.defaultPrevented || event.button !== 0 ||
                event.ctrlKey || event.metaKey || event.shiftKey || event.altKey ||
                link.hasAttribute('download') || (link.target && link.target !== '_self')) return;
            var url = new URL(link.href, current);
            if (url.origin !== window.location.origin || url.hash ||
                !/^https?:$/.test(url.protocol) || /\/logout(?:[?#]|$)/.test(url.href)) return;
            event.preventDefault();
            navigate(url.href);
        });
        updateToggle();
        positionFrame();
    }

    function updateToggle() {
        if (!frame) return;
        var collapsed = document.body.classList.contains('sidebar-collapse');
        var toggle = frame.contentDocument.getElementById('sidebarToggleBtn');
        if (toggle) {
            toggle.setAttribute('aria-expanded', String(!collapsed));
            toggle.setAttribute('title', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
            toggle.setAttribute('aria-label', toggle.getAttribute('title'));
        }
    }

    function load(url) {
        popupOpen = false;
        pending = url;
        if (!frame) {
            frame = document.createElement('iframe');
            frame.id = 'erpContentFrame';
            frame.title = 'Application page';
            frame.addEventListener('load', syncPage);
            frame.src = url;
            document.body.classList.add('erp-navigation-shell');
            document.body.appendChild(frame);
            positionFrame();
            // The initial page is now the shell; its discarded form is no longer dirty.
            window.isDirty = false;
        } else {
            // Avoid extra iframe history entries for sidebar navigation.
            frame.contentWindow.location.replace(url);
        }
        document.body.classList.remove('sidebar-open');
        positionFrame();
    }

    function navigate(url) {
        confirmLeaving(function () { load(url); });
    }

    window.erpSidebarNavigation = {
        navigate: navigate,
        setPopupOpen: function (open) {
            popupOpen = open;
            positionFrame();
        },
        toggle: function () {
            if (window.jQuery && window.jQuery.fn.PushMenu) {
                window.jQuery('#sidebarToggleBtn').PushMenu('toggle');
            } else {
                document.body.classList.toggle(window.innerWidth < 992 ? 'sidebar-open' : 'sidebar-collapse');
            }
            updateToggle();
            positionFrame();
        }
    };

    document.addEventListener('DOMContentLoaded', function () {
        sidebar = document.querySelector('.main-sidebar');
        if (!sidebar) return;
        document.addEventListener('click', function (event) {
            var link = event.target.closest('.main-sidebar a.nav-link, .main-sidebar a.brand-link');
            if (!link || event.button !== 0 || event.ctrlKey || event.metaKey ||
                event.shiftKey || event.altKey || (link.target && link.target !== '_self')) return;
            var group = link.parentElement;
            if (group.classList.contains('has-treeview') &&
                document.body.classList.contains('sidebar-collapse')) {
                event.preventDefault();
                event.stopImmediatePropagation();
                if (window.jQuery && window.jQuery.fn.PushMenu) {
                    window.jQuery('#sidebarToggleBtn').PushMenu('expand');
                } else {
                    document.body.classList.remove('sidebar-collapse');
                    if (window.innerWidth < 992) document.body.classList.add('sidebar-open');
                }
                var menu = group.querySelector('.nav-treeview');
                if (menu) {
                    if (window.jQuery) window.jQuery(menu).stop(true, true);
                    group.classList.add('menu-open');
                    menu.style.display = 'block';
                }
                updateToggle();
                positionFrame();
                return;
            }
            var href = link.getAttribute('href');
            if (!href || href === '#' || link.parentElement.classList.contains('has-treeview')) return;
            var url = new URL(link.href, window.location.href);
            if (url.origin !== window.location.origin || !/^https?:$/.test(url.protocol)) return;
            // Handle confirmation here before legacy form handlers can navigate the shell.
            event.preventDefault();
            event.stopImmediatePropagation();
            navigate(url.href);
        }, true);
        sidebar.addEventListener('transitionend', positionFrame);
        window.addEventListener('resize', positionFrame);
        window.addEventListener('popstate', function () {
            if (!frame) return;
            backNavigation = true;
            load(window.location.href);
        });
    });
}());
