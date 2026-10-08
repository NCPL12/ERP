(function () {
    const STORAGE_KEY = 'neptuneColorTheme';

    function preferredTheme() {
        const stored = localStorage.getItem(STORAGE_KEY);
        if (stored === 'dark' || stored === 'light') return stored;
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    }

    function updateButtons(theme) {
        document.querySelectorAll('[data-theme-toggle]').forEach(button => {
            const dark = theme === 'dark';
            button.setAttribute('aria-label', dark ? 'Switch to light mode' : 'Switch to dark mode');
            button.setAttribute('title', dark ? 'Switch to light mode' : 'Switch to dark mode');
            const icon = button.querySelector('[data-theme-icon]');
            const label = button.querySelector('[data-theme-label]');
            if (icon) icon.className = dark ? 'fas fa-sun' : 'fas fa-moon';
            if (label) label.textContent = dark ? 'Light mode' : 'Dark mode';
        });
    }

    function setTheme(theme, save) {
        document.documentElement.setAttribute('data-theme', theme);
        if (save) localStorage.setItem(STORAGE_KEY, theme);
        updateButtons(theme);
        applyChartTheme(theme);
        document.dispatchEvent(new CustomEvent('neptune:themechange', { detail: { theme } }));
    }

    function applyChartTheme(theme) {
        if (!window.Chart) return;
        const dark = theme === 'dark';
        Chart.defaults.color = dark ? '#aebed2' : '#64748b';
        Chart.defaults.borderColor = dark ? '#30435b' : 'rgba(148,163,184,.25)';
        const instances = Chart.instances ? Object.values(Chart.instances) : [];
        instances.forEach(chart => { try { chart.update('none'); } catch (ignored) {} });
    }

    function buildFloatingToggle() {
        if (document.querySelector('[data-theme-toggle]')) return;
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'theme-toggle theme-toggle-floating';
        button.setAttribute('data-theme-toggle', '');
        button.innerHTML = '<i data-theme-icon></i><span data-theme-label></span>';
        document.body.appendChild(button);
    }

    function buildErpReturnOverlay() {
        let overlay = document.getElementById('erpReturnTransition');
        if (overlay) return overlay;
        const style = document.createElement('style');
        style.textContent = '#erpReturnTransition{position:fixed;inset:0;z-index:12000;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#071d4f,#0b347b);color:#fff;opacity:0;visibility:hidden;transition:opacity .28s ease,visibility .28s ease}#erpReturnTransition.show{opacity:1;visibility:visible}#erpReturnTransition .erp-return-card{text-align:center;transform:translateY(12px);transition:transform .35s ease}#erpReturnTransition.show .erp-return-card{transform:translateY(0)}#erpReturnTransition .erp-return-icon{width:68px;height:68px;margin:0 auto 18px;border-radius:20px;display:grid;place-items:center;background:rgba(255,255,255,.13);font-size:27px;box-shadow:0 14px 34px rgba(0,0,0,.22)}#erpReturnTransition strong{display:block;font-size:21px;letter-spacing:.01em}#erpReturnTransition span{display:block;margin-top:7px;color:rgba(255,255,255,.75);font-size:13px}@media(prefers-reduced-motion:reduce){#erpReturnTransition,#erpReturnTransition .erp-return-card{transition:none}}';
        document.head.appendChild(style);
        overlay = document.createElement('div');
        overlay.id = 'erpReturnTransition';
        overlay.setAttribute('aria-live', 'polite');
        overlay.innerHTML = '<div class="erp-return-card"><div class="erp-return-icon"><i class="fas fa-arrow-left"></i></div><strong>Returning to ERP</strong><span>Opening your dashboard…</span></div>';
        document.body.appendChild(overlay);
        return overlay;
    }

    function returnToErp(event, link) {
        if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
        event.preventDefault();
        const destination = link.href;
        if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            window.location.href = destination;
            return;
        }
        const overlay = buildErpReturnOverlay();
        requestAnimationFrame(() => overlay.classList.add('show'));
        window.setTimeout(() => { window.location.href = destination; }, 1200);
    }

    document.addEventListener('DOMContentLoaded', function () {
        buildFloatingToggle();
        const currentTheme = document.documentElement.getAttribute('data-theme') || preferredTheme();
        updateButtons(currentTheme);
        applyChartTheme(currentTheme);
        document.addEventListener('click', function (event) {
            const logout = event.target.closest('a[href$="/logout"], form[action$="/logout"] button');
            if (logout) {
                localStorage.removeItem('neptuneWelcomeShown');
                sessionStorage.removeItem('neptuneWelcomeShown');
            }
            const erpLink = event.target.closest('.erp-back-link');
            if (erpLink) {
                returnToErp(event, erpLink);
                return;
            }
            const button = event.target.closest('[data-theme-toggle]');
            if (!button) return;
            const next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
            setTheme(next, true);
        });
    });

    window.neptuneTheme = { set: theme => setTheme(theme, true), get: preferredTheme };
})();
