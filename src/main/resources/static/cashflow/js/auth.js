/**
 * Authentication lives in Spring Security's HttpOnly session cookie. This
 * bridge attaches the server-issued CSRF token to same-origin mutations and
 * exposes the rendered role only for UI affordances.
 */
window.auth = (function() {
    function getCookie(name) {
        const prefix = encodeURIComponent(name) + '=';
        return document.cookie.split(';').map(value => value.trim())
            .find(value => value.startsWith(prefix))
            ?.substring(prefix.length) || null;
    }

    function getUserRole() {
        return document.querySelector('meta[name="user-role"]')?.content || null;
    }

    function getCsrf() {
        const cookieToken = getCookie('XSRF-TOKEN');
        const renderedToken = document.querySelector('meta[name="csrf-token"]')?.content;
        const headerName = document.querySelector('meta[name="csrf-header"]')?.content || 'X-XSRF-TOKEN';
        return { token: renderedToken, headerName };
    }

    function clearLegacyAuthStorage() {
        window.localStorage.removeItem('jwtToken');
        window.localStorage.removeItem('userRole');
        document.cookie = 'jwtToken=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; samesite=strict';
        document.cookie = 'userRole=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; samesite=strict';
    }

    function securedFetch(resource, init = {}) {
        const method = String(init.method || (resource instanceof Request ? resource.method : 'GET')).toUpperCase();
        const unsafe = !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method);
        const sameOrigin = typeof resource === 'string'
            ? resource.startsWith('/') || resource.startsWith(window.location.origin)
            : resource instanceof Request && new URL(resource.url, window.location.href).origin === window.location.origin;
        if (unsafe && sameOrigin) {
            const csrf = getCsrf();
            if (csrf.token) {
                init = Object.assign({}, init);
                init.headers = new Headers(init.headers || (resource instanceof Request ? resource.headers : undefined));
                init.headers.set(csrf.headerName, csrf.token);
            }
        }
        return window.__originalFetch__(resource, init);
    }

    function init() {
        clearLegacyAuthStorage();
        window.userRole = getUserRole();
        if (!window.__originalFetch__) {
            window.__originalFetch__ = window.fetch.bind(window);
            window.fetch = securedFetch;
        }
    }

    return { init, getUserRole, clearAuthStorage: clearLegacyAuthStorage };
})();

window.auth.init();
