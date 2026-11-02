// Resolve analyzer routes within ERP's context path; external URLs remain untouched.
(function () {
    const base = document.querySelector('meta[name="cashflow-base"]').content;
    window.cashflowBase = base;
    window.cashflowUrl = function (url) {
        return typeof url === 'string' && url.startsWith('/') && !url.startsWith('//') && !url.startsWith(base + '/')
            ? base + url : url;
    };
    const original = window.fetch.bind(window);
    window.fetch = function (url, options) {
        const resolved = window.cashflowUrl(url);
        const method = String(options?.method || 'GET').toUpperCase();
        const target = typeof resolved === 'string' ? new URL(resolved, window.location.origin) : null;
        if (target && target.origin === window.location.origin && target.pathname.startsWith(base + '/')
            && !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method)) {
            const token = document.querySelector('meta[name="csrf-token"]')?.content;
            const header = document.querySelector('meta[name="csrf-header"]')?.content;
            if (token && header) {
                options = Object.assign({}, options);
                options.headers = new Headers(options.headers || {});
                options.headers.set(header, token);
            }
        }
        return original(resolved, options);
    };
})();
