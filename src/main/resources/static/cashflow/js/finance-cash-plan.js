(function () {
    'use strict';

    const API = '/api/cashflow/finance-plan';
    const state = { plan: null, editing: false };
    const money = value => new Intl.NumberFormat('en-IN', {
        style: 'currency', currency: 'INR', maximumFractionDigits: 0
    }).format(Number(value || 0));
    const esc = value => String(value == null ? '' : value).replace(/[&<>'"]/g,
        ch => ({'&':'&amp;','<':'&lt;','>':'&gt;',"'":'&#39;','"':'&quot;'}[ch]));

    function status(message, kind) {
        const element = document.getElementById('planStatus');
        element.textContent = message || '';
        element.className = `plan-status ${kind || ''}`;
    }

    async function request(url, options) {
        const response = await fetch(url, Object.assign({ credentials: 'same-origin' }, options || {}));
        const body = await response.text();
        let data = {};
        if (body) {
            try { data = JSON.parse(body); }
            catch (_error) { throw new Error(`Server returned an invalid response (HTTP ${response.status}).`); }
        }
        if (!response.ok || data.success === false) throw new Error(data.error || data.message || `HTTP ${response.status}`);
        return data;
    }

    function query() {
        return new URLSearchParams({
            startMonth: document.getElementById('startMonth').value,
            months: document.getElementById('monthCount').value
        });
    }

    function displaySyncTime(value) {
        if (!value) return 'Never';
        const parsed = Array.isArray(value)
            ? new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0, value[5] || 0)
            : new Date(value);
        return Number.isNaN(parsed.getTime()) ? 'Never' : parsed.toLocaleString('en-IN');
    }

    async function loadTallyStatus() {
        const card = document.getElementById('syncStatusCard');
        const icon = document.getElementById('syncIcon');
        const label = document.getElementById('syncStatusText');
        const timestamp = document.getElementById('syncTimestampValue');
        try {
            const data = await request('/api/cashflow/tally/status');
            const connected = data.config && data.config.serverAvailable === true;
            card.classList.toggle('success', connected);
            icon.classList.toggle('success', connected);
            label.textContent = connected ? `Tally Live${data.syncStatus?.invoicesSynced != null ? ` • ${data.syncStatus.invoicesSynced} bills` : ''}` : 'Tally unavailable';
            timestamp.textContent = displaySyncTime(data.syncStatus?.lastSyncEndTime || data.syncStatus?.lastSyncTime);
        } catch (error) {
            card.classList.remove('success');
            icon.classList.remove('success');
            label.textContent = 'Tally status unavailable';
            timestamp.textContent = error.message;
        }
    }

    async function triggerManualSync() {
        const button = document.getElementById('syncNowBtn');
        const icon = document.getElementById('syncIcon');
        button.disabled = true;
        icon.classList.add('syncing');
        document.getElementById('syncStatusText').textContent = 'Syncing Tally…';
        try {
            await request('/api/cashflow/tally/sync', { method: 'POST' });
            await refreshAll();
            status('Tally data synchronized.', 'success');
        } catch (error) {
            status(error.message, 'error');
            await loadTallyStatus();
        } finally {
            button.disabled = false;
            icon.classList.remove('syncing');
        }
    }

    function manualInput(period, field) {
        const value = Number(period[field] || 0).toFixed(2);
        return `<input class="manual-input" type="number" min="0" step="0.01" value="${value}" data-month="${esc(period.month)}" data-field="${field}" aria-label="${field === 'manualCashIn' ? 'Manual cash in' : 'Manual cash out'} for ${esc(period.label)}" />`;
    }

    function renderSummary() {
        const periods = state.plan?.periods || [];
        document.getElementById('summaryHead').innerHTML = `<tr><th>Particulars</th>${periods.map(p => `<th>${esc(p.label)}</th>`).join('')}<th>Total</th></tr>`;
        const rows = [
            { label: 'Cash in — Tally', field: 'tallyCashIn', className: 'cash-in' },
            { label: 'Manual cash in', field: 'manualCashIn', className: 'manual cash-in', editable: true },
            { label: 'Total cash in', field: 'totalCashIn', className: 'total cash-in' },
            { label: 'Cash out — Tally', field: 'tallyCashOut', className: 'cash-out' },
            { label: 'Manual cash out', field: 'manualCashOut', className: 'manual cash-out', editable: true },
            { label: 'Total cash out', field: 'totalCashOut', className: 'total cash-out' },
            { label: 'Net flow', field: 'netFlow', className: 'net' }
        ];

        document.getElementById('summaryBody').innerHTML = rows.map(row => {
            const values = periods.map(period => Number(period[row.field] || 0));
            const total = values.reduce((sum, value) => sum + value, 0);
            const cells = periods.map((period, index) => {
                if (row.editable && state.editing) return `<td>${manualInput(period, row.field)}</td>`;
                return `<td class="${values[index] < 0 ? 'negative' : ''}">${money(values[index])}</td>`;
            }).join('');
            return `<tr class="${row.className}"><td>${row.label}</td>${cells}<td class="${total < 0 ? 'negative' : ''}">${money(total)}</td></tr>`;
        }).join('');

        const notices = [];
        if (state.plan.overdueMovedToFirstMonthCount) notices.push(`${state.plan.overdueMovedToFirstMonthCount} overdue Tally bills are included in the first month`);
        if (state.plan.missingCashDateCount) notices.push(`${state.plan.missingCashDateCount} Tally bills are excluded because no due date is available`);
        if (!notices.length) notices.push('All synchronized Tally bills have forecast dates');
        document.getElementById('planNotices').innerHTML = notices.map((text, index) => `<span class="notice ${index === 0 && !state.plan.overdueMovedToFirstMonthCount && !state.plan.missingCashDateCount ? 'ok' : ''}">${esc(text)}</span>`).join('');
    }

    async function loadPlan() {
        status('Loading monthly cash flow…');
        state.plan = await request(`${API}/summary?${query()}`);
        renderSummary();
        status('Statement refreshed.', 'success');
    }

    function setEditing(editing) {
        state.editing = editing;
        document.getElementById('manualEdit').hidden = editing;
        document.getElementById('saveManual').hidden = !editing;
        document.getElementById('cancelManual').hidden = !editing;
        document.getElementById('refreshPlan').disabled = editing;
        document.getElementById('startMonth').disabled = editing;
        document.getElementById('monthCount').disabled = editing;
        renderSummary();
        if (editing) document.querySelector('.manual-input')?.focus();
    }

    async function saveManualValues() {
        const saveButton = document.getElementById('saveManual');
        const periods = state.plan?.periods || [];
        const months = periods.map(period => {
            const cashIn = document.querySelector(`[data-month="${period.month}"][data-field="manualCashIn"]`);
            const cashOut = document.querySelector(`[data-month="${period.month}"][data-field="manualCashOut"]`);
            return {
                month: period.month,
                manualCashIn: Number(cashIn?.value || 0),
                manualCashOut: Number(cashOut?.value || 0)
            };
        });
        if (months.some(item => item.manualCashIn < 0 || item.manualCashOut < 0)) {
            status('Manual amounts cannot be negative.', 'error');
            return;
        }

        saveButton.disabled = true;
        try {
            await request(`${API}/manual-values`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ months })
            });
            state.editing = false;
            document.getElementById('manualEdit').hidden = false;
            document.getElementById('saveManual').hidden = true;
            document.getElementById('cancelManual').hidden = true;
            document.getElementById('refreshPlan').disabled = false;
            document.getElementById('startMonth').disabled = false;
            document.getElementById('monthCount').disabled = false;
            await loadPlan();
            status('Manual monthly values saved.', 'success');
        } catch (error) {
            status(error.message, 'error');
        } finally {
            saveButton.disabled = false;
        }
    }

    function exportCsv() {
        const periods = state.plan?.periods || [];
        if (!periods.length) return;
        const rows = [
            ['Cash in - Tally', 'tallyCashIn'], ['Manual cash in', 'manualCashIn'], ['Total cash in', 'totalCashIn'],
            ['Cash out - Tally', 'tallyCashOut'], ['Manual cash out', 'manualCashOut'], ['Total cash out', 'totalCashOut'],
            ['Net flow', 'netFlow']
        ];
        const data = [['Particulars'].concat(periods.map(period => period.label)).concat('Total')];
        rows.forEach(row => {
            const values = periods.map(period => Number(period[row[1]] || 0));
            data.push([row[0]].concat(values.map(value => value.toFixed(2))).concat(values.reduce((sum, value) => sum + value, 0).toFixed(2)));
        });
        const csv = data.map(row => row.map(value => `"${String(value).replace(/"/g, '""')}"`).join(',')).join('\r\n');
        const link = document.createElement('a');
        link.href = URL.createObjectURL(new Blob([csv], { type: 'text/csv' }));
        link.download = `monthly-cash-flow-${document.getElementById('startMonth').value}.csv`;
        link.click();
        URL.revokeObjectURL(link.href);
    }

    async function refreshAll() {
        try { await Promise.all([loadPlan(), loadTallyStatus()]); }
        catch (error) { status(error.message, 'error'); }
    }

    document.addEventListener('DOMContentLoaded', () => {
        document.getElementById('startMonth').value = new Date().toISOString().slice(0, 7);

        const root = document.documentElement;
        const collapseButton = document.getElementById('sidebarCollapseBtn');
        const updateCollapseLabel = () => {
            const collapsed = root.classList.contains('sidebar-precollapsed');
            collapseButton.setAttribute('aria-label', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
            collapseButton.title = collapsed ? 'Expand sidebar' : 'Collapse sidebar';
        };
        updateCollapseLabel();
        collapseButton.addEventListener('click', () => {
            root.classList.toggle('sidebar-precollapsed');
            localStorage.setItem('financeSidebarCollapsed', String(root.classList.contains('sidebar-precollapsed')));
            updateCollapseLabel();
        });

        const graphsToggle = document.querySelector('[data-graphs-toggle]');
        const graphsSubmenu = document.querySelector('[data-graphs-submenu]');
        const setGraphsExpanded = expanded => {
            graphsSubmenu.classList.toggle('open', expanded);
            graphsToggle.setAttribute('aria-expanded', String(expanded));
            localStorage.setItem('neptuneGraphsExpanded', String(expanded));
        };
        setGraphsExpanded(localStorage.getItem('neptuneGraphsExpanded') === 'true');
        graphsToggle.addEventListener('click', () => setGraphsExpanded(graphsToggle.getAttribute('aria-expanded') !== 'true'));

        document.getElementById('syncNowBtn').addEventListener('click', triggerManualSync);
        document.getElementById('refreshPlan').addEventListener('click', refreshAll);
        document.getElementById('manualEdit').addEventListener('click', () => setEditing(true));
        document.getElementById('cancelManual').addEventListener('click', () => setEditing(false));
        document.getElementById('saveManual').addEventListener('click', saveManualValues);
        document.getElementById('downloadCsv').addEventListener('click', exportCsv);
        refreshAll();
    });
})();
