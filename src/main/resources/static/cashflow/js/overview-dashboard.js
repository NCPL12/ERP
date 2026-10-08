/**
 * Overview Dashboard Script (Externalized)
 * Full Interactive Version
 */

console.log('🏁 Overview Dashboard Script: STARTING');

// ========== GLOBAL DASHBOARD STATE ==========
window.lastOverviewData = window.lastOverviewData || {};
window.currentFilters = window.currentFilters || { fromDate: '', toDate: '' };
window.overdueModal = window.overdueModal || null;
window.dueModal = window.dueModal || null;
window.activeCustomersModal = window.activeCustomersModal || null;
window.tallyLiveRefreshTimer = window.tallyLiveRefreshTimer || null;
window.outstandingState = window.outstandingState || { type: null, rows: [], total: 0 };
window.advanceState = window.advanceState || { rows: [] };
window.currentAdvanceRow = window.currentAdvanceRow || null;
window.advanceRenderedRows = window.advanceRenderedRows || [];
window.costCentreState = window.costCentreState || { sites: [], selectedSite: null, selectedCentre: null, siteRows: [], selectedCategory: null, selectedVendor: null };
window.snapshotOutstanding = window.snapshotOutstanding || { receivables: null, payables: null };
window.snapshotUpcomingType = window.snapshotUpcomingType || 'receivables';
window.snapshotUpcomingRows = window.snapshotUpcomingRows || [];
window.overviewSnapshotChartInstance = window.overviewSnapshotChartInstance || null;

// ========== UTILITY FUNCTIONS ==========
function formatCurrency(amount) {
    const n = Number(amount) || 0;
    return '₹' + n.toLocaleString('en-IN', { minimumFractionDigits: 0, maximumFractionDigits: 0 });
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const d = new Date(dateString);
    return isNaN(d) ? dateString : d.toLocaleDateString('en-IN');
}

function handleFileUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    const selectBtn = document.getElementById('selectExcelBtn');
    if (selectBtn) {
        selectBtn.disabled = true;
    }

    try {
        fetch('/api/cashflow/overview', {
            method: 'POST',
            body: formData,
            credentials: 'same-origin'
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text || 'Upload failed');
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.error) {
                    alert('Error: ' + data.error);
                } else {
                    updateDashboard(data);
                    if (typeof refreshAllTabs === 'function') {
                        refreshAllTabs();
                    }
                    const disp = document.getElementById('fileNameDisplay');
                    if (disp) disp.classList.remove('d-none');
                    const txt = document.getElementById('fileNameText');
                    if (txt) txt.textContent = file.name;
                }
            })
            .catch(error => {
                console.error('Excel upload failed:', error);
                alert('Failed to analyze file: ' + error.message);
            })
            .finally(() => {
                if (selectBtn) selectBtn.disabled = false;
                if (event.target) event.target.value = '';
            });
    } catch (error) {
        console.error('Upload handler error:', error);
        if (selectBtn) selectBtn.disabled = false;
    }
}

function bindExcelUpload() {
    const fileInput = document.getElementById('excelFileInput');
    const selectExcelBtn = document.getElementById('selectExcelBtn');
    if (!fileInput || !selectExcelBtn) {
        return;
    }
    selectExcelBtn.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', handleFileUpload);
}

async function downloadExcel(url, filename) {
    const response = await fetch(url, { credentials: 'same-origin' });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || `Download failed (HTTP ${response.status})`);
    }
    const blob = await response.blob();
    const objectUrl = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = objectUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(objectUrl);
}

async function downloadOverdueExcel() {
    try {
        await downloadExcel('/api/cashflow/overview/export-overdue?type=all', 'Overview-Overdue-Invoices.xlsx');
    } catch (error) {
        console.error('Overdue export failed:', error);
        alert(`Failed to download overdue invoices: ${error.message}`);
    }
}

async function downloadDueExcel() {
    try {
        await downloadExcel('/api/cashflow/overview/export-due?type=all', 'Overview-Due-Invoices.xlsx');
    } catch (error) {
        console.error('Due export failed:', error);
        alert(`Failed to download due invoices: ${error.message}`);
    }
}

window.downloadOverdueExcel = downloadOverdueExcel;
window.downloadDueExcel = downloadDueExcel;

function parseAmount(val) {
    if (val === undefined || val === null) return 0;
    if (typeof val === 'number') return val;
    return parseFloat(String(val).replace(/[^\d.-]/g, '')) || 0;
}

function escapeHtml(value) {
    return String(value ?? '')
        .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;').replaceAll("'", '&#039;');
}

async function fetchOutstanding(type) {
    const response = await fetch(`/api/cashflow/tally/outstanding?type=${encodeURIComponent(type)}`, {
        credentials: 'same-origin'
    });
    if (!response.ok) throw new Error(await response.text() || `HTTP ${response.status}`);
    return response.json();
}

function snapshotBucket(rows, predicate) {
    const matched = (rows || []).filter(predicate);
    return {
        count: matched.length,
        amount: matched.reduce((sum, row) => sum + (Number(row.amount) || 0), 0)
    };
}

function renderSnapshotAging(type, payload) {
    const host = document.getElementById(type === 'receivables'
        ? 'snapshotReceivablesAging' : 'snapshotPayablesAging');
    if (!host) return;
    const rows = payload?.rows || [];
    const overdue = snapshotBucket(rows, row => String(row.status || '').toLowerCase() === 'overdue');
    const week = snapshotBucket(rows, row => String(row.status || '').toLowerCase() !== 'overdue'
        && row.daysUntilDue != null && Number(row.daysUntilDue) <= 7);
    const month = snapshotBucket(rows, row => String(row.status || '').toLowerCase() !== 'overdue'
        && row.daysUntilDue != null && Number(row.daysUntilDue) > 7 && Number(row.daysUntilDue) <= 30);
    const later = snapshotBucket(rows, row => String(row.status || '').toLowerCase() !== 'overdue'
        && (row.daysUntilDue == null || Number(row.daysUntilDue) > 30));
    const cards = [
        { label: 'Overdue', value: overdue, tone: 'overdue' },
        { label: 'Due in 7 days', value: week, tone: 'week' },
        { label: 'Due in 8–30 days', value: month, tone: '' },
        { label: 'Later / no date', value: later, tone: '' }
    ];
    host.innerHTML = cards.map(card => `<button type="button" class="snapshot-aging-item ${card.tone}"
        data-snapshot-open="${type}" aria-label="Open ${escapeHtml(card.label)} ${type}">
        <div class="snapshot-aging-label">${card.label}</div>
        <div class="snapshot-aging-value" title="${formatCurrency(card.value.amount)}">${formatCurrency(card.value.amount)}</div>
        <div class="snapshot-aging-count">${card.value.count} ${card.value.count === 1 ? 'bill' : 'bills'}</div>
    </button>`).join('');
}

function renderSnapshotAttention() {
    const host = document.getElementById('snapshotAttentionList');
    if (!host) return;
    const recRows = window.snapshotOutstanding.receivables?.rows || [];
    const payRows = window.snapshotOutstanding.payables?.rows || [];
    const overdueRec = snapshotBucket(recRows, row => String(row.status || '').toLowerCase() === 'overdue');
    const overduePay = snapshotBucket(payRows, row => String(row.status || '').toLowerCase() === 'overdue');
    const dueRec = snapshotBucket(recRows, row => String(row.status || '').toLowerCase() !== 'overdue'
        && row.daysUntilDue != null && Number(row.daysUntilDue) <= 7);
    const duePay = snapshotBucket(payRows, row => String(row.status || '').toLowerCase() !== 'overdue'
        && row.daysUntilDue != null && Number(row.daysUntilDue) <= 7);
    const items = [
        { type: 'receivables', label: 'Overdue customer invoices', data: overdueRec, warning: false },
        { type: 'payables', label: 'Overdue vendor bills', data: overduePay, warning: false },
        { type: 'receivables', label: 'Receipts due within 7 days', data: dueRec, warning: true },
        { type: 'payables', label: 'Payments due within 7 days', data: duePay, warning: true }
    ];
    host.innerHTML = items.map(item => `<button type="button" class="snapshot-attention-item" data-snapshot-open="${item.type}">
        <span class="snapshot-attention-label">${item.label}<small>${formatCurrency(item.data.amount)}</small></span>
        <span class="snapshot-attention-count ${item.warning ? 'warning' : ''}">${item.data.count}</span>
    </button>`).join('');
}

function renderSnapshotUpcoming() {
    const host = document.getElementById('snapshotUpcomingList');
    if (!host) return;
    const type = window.snapshotUpcomingType;
    const rows = (window.snapshotOutstanding[type]?.rows || [])
        .filter(row => String(row.status || '').toLowerCase() !== 'overdue' && row.dueDate)
        .sort((left, right) => String(left.dueDate).localeCompare(String(right.dueDate)))
        .slice(0, 5);
    window.snapshotUpcomingRows = rows;
    document.querySelectorAll('[data-upcoming-type]').forEach(button =>
        button.classList.toggle('active', button.dataset.upcomingType === type));
    if (!rows.length) {
        host.innerHTML = '<div class="snapshot-empty">No upcoming bills with due dates.</div>';
        return;
    }
    host.innerHTML = rows.map((row, index) => `<div class="snapshot-upcoming-row" role="button" tabindex="0"
        data-upcoming-index="${index}" aria-label="Open bill ${escapeHtml(row.invoiceNumber || '')} for ${escapeHtml(row.partyName || '')}">
        <div><div class="snapshot-upcoming-party" title="${escapeHtml(row.partyName || 'Unknown party')}">${escapeHtml(row.partyName || 'Unknown party')}</div>
        <div class="snapshot-upcoming-meta">${escapeHtml(row.invoiceNumber || 'No bill number')} • ${formatDate(row.dueDate)}</div></div>
        <div class="snapshot-upcoming-amount">${formatCurrency(row.amount)} <i class="fas fa-chevron-right" aria-hidden="true"></i></div>
    </div>`).join('');
}

function renderFinanceSnapshot() {
    renderSnapshotAging('receivables', window.snapshotOutstanding.receivables);
    renderSnapshotAging('payables', window.snapshotOutstanding.payables);
    renderSnapshotAttention();
    renderSnapshotUpcoming();
}

function localIsoDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

async function loadOverviewForecast() {
    const canvas = document.getElementById('overviewCashflowChart');
    const empty = document.getElementById('overviewCashflowEmpty');
    if (!canvas) return;
    try {
        const from = new Date();
        const to = new Date(from);
        to.setDate(to.getDate() + 90);
        const query = new URLSearchParams({ from: localIsoDate(from), to: localIsoDate(to), granularity: 'weekly' });
        const response = await fetch(`/api/cashflow/tally/weekly-cashflow-forecast?${query}`, { credentials: 'same-origin' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || `HTTP ${response.status}`);
        const periods = data.periods || [];
        const summary = document.getElementById('snapshotForecastSummary');
        if (summary) summary.textContent = `${data.includedBillCount || 0} bills • ${formatCurrency(data.totalReceivables)} in • ${formatCurrency(data.totalPayables)} out`;
        if (!periods.length) {
            if (empty) {
                empty.classList.remove('snapshot-loading');
                empty.textContent = 'No bills are due during the next 90 days.';
            }
            if (window.overviewSnapshotChartInstance) window.overviewSnapshotChartInstance.destroy();
            window.overviewSnapshotChartInstance = null;
            return;
        }
        if (empty) empty.style.display = 'none';
        if (window.overviewSnapshotChartInstance) window.overviewSnapshotChartInstance.destroy();
        window.overviewSnapshotChartInstance = new Chart(canvas, {
            type: 'bar',
            data: {
                labels: periods.map(period => new Date(`${period.weekStart}T00:00:00`).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' })),
                datasets: [
                    { label: 'Cash in', data: periods.map(period => Number(period.receivables) || 0), backgroundColor: '#10b981', borderRadius: 4, maxBarThickness: 24 },
                    { label: 'Cash out', data: periods.map(period => Number(period.payables) || 0), backgroundColor: '#f59e0b', borderRadius: 4, maxBarThickness: 24 }
                ]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { position: 'bottom', labels: { usePointStyle: true, boxWidth: 8, padding: 16 } },
                    tooltip: { callbacks: { label: context => `${context.dataset.label}: ${formatCurrency(context.raw)}` } }
                },
                scales: {
                    x: { grid: { display: false }, ticks: { maxRotation: 0, autoSkip: true, maxTicksLimit: 8 } },
                    y: { beginAtZero: true, ticks: { callback: value => `₹${Number(value).toLocaleString('en-IN', { notation: 'compact', maximumFractionDigits: 1 })}` } }
                }
            }
        });
    } catch (error) {
        console.warn('Unable to load overview forecast:', error.message);
        if (empty) {
            empty.classList.remove('snapshot-loading');
            empty.textContent = 'Cash flow preview is temporarily unavailable.';
        }
    }
}

async function refreshOutstandingCounts() {
    try {
        const [receivables, payables] = await Promise.all([
            fetchOutstanding('receivables'), fetchOutstanding('payables')
        ]);
        const receivableCount = document.getElementById('outstandingReceivablesCount');
        const payableCount = document.getElementById('outstandingPayablesCount');
        if (receivableCount) receivableCount.textContent = `${receivables.count || 0} bills`;
        if (payableCount) payableCount.textContent = `${payables.count || 0} bills`;
        const receivableTotal = Number(receivables.total) || 0;
        const payableTotal = Number(payables.total) || 0;
        const totalInflow = document.getElementById('totalInflow');
        const totalOutflow = document.getElementById('totalOutflow');
        const netCashflow = document.getElementById('netCashflow');
        if (totalInflow) totalInflow.textContent = formatCurrency(receivableTotal);
        if (totalOutflow) totalOutflow.textContent = formatCurrency(payableTotal);
        if (netCashflow) {
            const net = receivableTotal - payableTotal;
            netCashflow.textContent = formatCurrency(net);
            netCashflow.className = `tile-value ${net >= 0 ? 'text-success' : 'text-danger'}`;
        }
        window.snapshotOutstanding = { receivables, payables };
        renderFinanceSnapshot();
    } catch (error) {
        console.warn('Unable to load outstanding bill counts:', error.message);
        ['snapshotReceivablesAging', 'snapshotPayablesAging', 'snapshotAttentionList', 'snapshotUpcomingList'].forEach(id => {
            const host = document.getElementById(id);
            if (host) host.innerHTML = '<div class="snapshot-empty">Snapshot data is temporarily unavailable.</div>';
        });
    }
}

// ========== CUSTOMER AND SUPPLIER ADVANCES ==========
function renderAdvancesTable() {
    const body = document.getElementById('advancesTableBody');
    if (!body) return;
    const type = document.getElementById('advanceTypeFilter')?.value || 'all';
    const search = (document.getElementById('advanceSearch')?.value || '').trim().toLowerCase();
    const rows = (window.advanceState.rows || []).filter(row =>
        (type === 'all' || row.type === type)
        && (!search || `${row.partyName || ''} ${row.ledgerGroup || ''}`.toLowerCase().includes(search)));

    const count = document.getElementById('advanceResultCount');
    if (count) count.textContent = `${rows.length} ${rows.length === 1 ? 'advance' : 'advances'}`;
    if (!rows.length) {
        window.advanceRenderedRows = [];
        body.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">No advances match the selected filters.</td></tr>';
        return;
    }
    window.advanceRenderedRows = rows;
    body.innerHTML = rows.map((row, index) => {
        const customer = row.type === 'CUSTOMER_ADVANCE_RECEIVED';
        const typeClass = customer ? 'customer' : 'supplier';
        const label = customer ? 'Customer advance received' : 'Supplier advance paid';
        return `<tr data-advance-index="${index}" tabindex="0" role="button" aria-label="Open advance details for ${escapeHtml(row.partyName)}">
            <td class="advance-party" title="${escapeHtml(row.partyName)}">${escapeHtml(row.partyName)}</td>
            <td class="advance-group">${escapeHtml(row.ledgerGroup)}</td>
            <td class="advance-type"><span class="advance-type-badge ${typeClass}">${label}</span></td>
            <td class="advance-amount">${formatCurrency(row.amount)}</td>
        </tr>`;
    }).join('');
}

function openAdvanceDrawer(row) {
    if (!row) return;
    window.currentAdvanceRow = row;
    const customer = row.type === 'CUSTOMER_ADVANCE_RECEIVED';
    document.getElementById('advanceDrawerTitle').textContent = row.partyName || 'Advance';
    document.getElementById('advanceDrawerType').textContent = customer
        ? 'Advance received from customer' : 'Advance paid to supplier';
    document.getElementById('advanceDrawerBalance').textContent = formatCurrency(row.amount);
    document.getElementById('advanceDrawerGroup').textContent = row.ledgerGroup || '—';
    document.getElementById('advanceDrawerNetLedger').textContent = formatCurrency(row.netLedgerBalance);
    document.getElementById('advanceDrawerReferenceCount').textContent = String(row.referenceCount || 0);
    renderAdvanceReferences(row.advanceReferences || []);
    document.getElementById('advanceDetailStatus').textContent = 'Not loaded yet.';
    document.getElementById('advanceLiveDetails')?.classList.add('d-none');
    document.getElementById('advanceDetailDrawer')?.classList.add('show');
    document.getElementById('advanceDrawerBackdrop')?.classList.add('show');
    document.getElementById('advanceDetailDrawer')?.setAttribute('aria-hidden', 'false');
}

function renderAdvanceReferences(references) {
    const host = document.getElementById('advanceReferences');
    if (!host) return;
    if (!references.length) {
        host.innerHTML = '<div class="invoice-detail-empty">No pending advance references found.</div>';
        return;
    }
    host.innerHTML = `<div class="table-responsive"><table class="invoice-detail-table">
        <thead><tr><th>Date</th><th>Reference</th><th>Pending amount</th></tr></thead>
        <tbody>${references.map(item => `<tr>
            <td>${escapeHtml(formatDate(item.billDate))}</td>
            <td>${escapeHtml(item.referenceNumber || '—')}</td>
            <td>${formatCurrency(item.amount)}</td>
        </tr>`).join('')}</tbody>
    </table></div>`;
}

function closeAdvanceDrawer() {
    document.getElementById('advanceDetailDrawer')?.classList.remove('show');
    document.getElementById('advanceDrawerBackdrop')?.classList.remove('show');
    document.getElementById('advanceDetailDrawer')?.setAttribute('aria-hidden', 'true');
    window.currentAdvanceRow = null;
}

function renderAdvanceTransactions(transactions) {
    const host = document.getElementById('advanceTransactions');
    if (!host) return;
    if (!transactions?.length) {
        host.innerHTML = '<div class="invoice-detail-empty">No linked vouchers were returned. The balance may come from an opening balance or vouchers without a matching Party Ledger reference.</div>';
        return;
    }
    host.innerHTML = `<div class="table-responsive"><table class="invoice-detail-table tally-transaction-table">
        <thead><tr><th>Date</th><th>Voucher</th><th>Type</th><th>Adjustment</th><th>Project / Cost Centre</th></tr></thead>
        <tbody>${transactions.map(item => {
            const voucher = item.voucherNumber || item.reference || '—';
            const adjustments = (item.billAdjustments || []).map(a =>
                `${escapeHtml(a.billType || 'Allocation')}: ${escapeHtml(a.billName || '—')}`).join('<br>') || '—';
            const projects = (item.costCentreAllocations || []).map(c => escapeHtml(c.name)).join('<br>') || 'Unallocated';
            return `<tr>
                <td>${escapeHtml(formatDate(item.voucherDate))}</td>
                <td title="${escapeHtml(voucher)}">${escapeHtml(voucher)}</td>
                <td>${escapeHtml(item.voucherType || '—')}</td>
                <td>${adjustments}</td>
                <td>${projects}</td>
            </tr>`;
        }).join('')}</tbody>
    </table></div>`;
}

async function loadAdvanceTallyDetails() {
    const row = window.currentAdvanceRow;
    if (!row) return;
    const button = document.getElementById('loadAdvanceTallyDetails');
    const status = document.getElementById('advanceDetailStatus');
    button.disabled = true;
    status.textContent = 'Loading transactions from Tally…';
    try {
        const query = new URLSearchParams({ partyName: row.partyName, type: row.type });
        const response = await fetch(`/api/cashflow/tally/advances/details?${query}`, { credentials: 'same-origin' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || `HTTP ${response.status}`);
        renderAdvanceTransactions(data.transactions || []);
        document.getElementById('advanceLiveDetails')?.classList.remove('d-none');
        status.textContent = data.transactions?.length
            ? `${data.transactions.length} linked voucher${data.transactions.length === 1 ? '' : 's'} loaded.`
            : 'No linked vouchers found; this may be an opening or unallocated balance.';
    } catch (error) {
        status.textContent = `Unable to load Tally transactions: ${error.message}`;
    } finally {
        button.disabled = false;
    }
}

async function loadAdvances() {
    const body = document.getElementById('advancesTableBody');
    const refreshButton = document.getElementById('refreshAdvancesBtn');
    if (body) body.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">Loading live Tally advances…</td></tr>';
    if (refreshButton) refreshButton.disabled = true;
    try {
        const response = await fetch('/api/cashflow/tally/advances', { credentials: 'same-origin' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || `HTTP ${response.status}`);
        window.advanceState.rows = [...(data.customerAdvances || []), ...(data.supplierAdvances || [])];
        document.getElementById('customerAdvanceTotal').textContent = formatCurrency(data.customerAdvanceTotal);
        document.getElementById('supplierAdvanceTotal').textContent = formatCurrency(data.supplierAdvanceTotal);
        document.getElementById('customerAdvanceCount').textContent = `${data.customerAdvanceCount || 0} parties`;
        document.getElementById('supplierAdvanceCount').textContent = `${data.supplierAdvanceCount || 0} parties`;
        renderAdvancesTable();
    } catch (error) {
        window.advanceState.rows = [];
        if (body) body.innerHTML = `<tr><td colspan="4" class="text-center text-danger py-4">${escapeHtml(error.message)}</td></tr>`;
        const count = document.getElementById('advanceResultCount');
        if (count) count.textContent = 'Unable to load advances';
    } finally {
        if (refreshButton) refreshButton.disabled = false;
    }
}

async function exportAdvancesExcel() {
    const type = document.getElementById('advanceTypeFilter')?.value || 'all';
    const search = document.getElementById('advanceSearch')?.value || '';
    const query = new URLSearchParams({ type, search });
    const filename = type === 'CUSTOMER_ADVANCE_RECEIVED' ? 'Customer-Advances-Received.xlsx'
        : type === 'SUPPLIER_ADVANCE_PAID' ? 'Supplier-Advances-Paid.xlsx' : 'All-Advances.xlsx';
    const button = document.getElementById('exportAdvancesBtn');
    if (button) button.disabled = true;
    try {
        await downloadExcel(`/api/cashflow/tally/advances/export?${query}`, filename);
    } catch (error) {
        alert(`Failed to export advances: ${error.message}`);
    } finally {
        if (button) button.disabled = false;
    }
}

async function openAdvancesPanel(scrollIntoView = true) {
    document.getElementById('outstandingDrilldown')?.classList.remove('show');
    const panel = document.getElementById('advancesPanel');
    panel?.classList.add('show');
    if (scrollIntoView) panel?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    await loadAdvances();
}

// ========== COST CENTRES / SITE ALLOCATIONS ==========
function costCentreLabel(value, tallyName = '') {
    return ({ BILLABLE: 'Billable', NON_BILLABLE: 'Non-Billable', MATERIAL: 'Material',
        TRANSPORTATION: 'Material transportation', SITE_EXPENSES: 'Site expenses', RENTAL: 'Rental charges',
        CONTRACT_LABOUR: 'Contract labour', REPAIRS_MAINTENANCE: 'Repairs & maintenance',
        INSURANCE: 'Insurance', STAFF_SALARY: 'Staff salary', BACKEND_SALARY: 'Backend salary',
        REVENUE: 'Revenue' })[value] || String(tallyName || '').trim() || 'Other Expenses';
}

function defaultFinancialPeriod() {
    const today = new Date();
    return { from: '2024-04-01', to: today.toISOString().slice(0, 10) };
}

function nestedCostCentreSite(name) {
    const wanted = String(name || '').trim().toLowerCase();
    return (window.costCentreState.sites || []).find(site => site.siteName.trim().toLowerCase() === wanted
        && (site.costCentres || []).some(child => child.name.trim().toLowerCase() !== wanted));
}

function costCentreMonthRanges(from, to) {
    const ranges = [];
    let cursor = new Date(`${from}T00:00:00`);
    const end = new Date(`${to}T00:00:00`);
    while (cursor <= end) {
        const monthEnd = new Date(cursor.getFullYear(), cursor.getMonth() + 1, 0);
        if (monthEnd > end) monthEnd.setTime(end.getTime());
        const localIso = date => `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
        ranges.push({ from: localIso(cursor), to: localIso(monthEnd), label: cursor.toLocaleDateString('en-IN', { month: 'short', year: 'numeric' }) });
        cursor = new Date(monthEnd.getFullYear(), monthEnd.getMonth(), monthEnd.getDate() + 1);
    }
    return ranges;
}

function renderCostCentreProgress(host, completed, total, label) {
    const percentage = total ? Math.round(completed / total * 100) : 0;
    host.innerHTML = `<div class="cost-centre-progress">
        <div class="cost-centre-progress-ring" style="--progress:${percentage}%"><strong>${percentage}%</strong></div>
        <div class="fw-bold mb-2">${completed < total ? `Loading ${escapeHtml(label)} from Tally…` : 'Preparing results…'}</div>
        <div class="cost-centre-progress-track"><div class="cost-centre-progress-bar" style="width:${percentage}%"></div></div>
        <div class="small mt-2">${completed} of ${total} monthly batches completed. Keep TallyPrime open.</div>
    </div>`;
}

function renderCostCentreSites() {
    const host = document.getElementById('costCentreSitesList');
    if (!host) return;
    const search = (document.getElementById('costCentreSearch')?.value || '').trim().toLowerCase();
    const sites = (window.costCentreState.sites || []).filter(site => !search
        || site.siteName.toLowerCase().includes(search)
        || (site.costCentres || []).some(item => item.name.toLowerCase().includes(search)));
    document.getElementById('costCentreSiteCount').textContent = `${sites.length} of ${window.costCentreState.sites.length} sites`;
    if (!sites.length) {
        host.innerHTML = '<div class="text-muted text-center p-4">No matching sites or Cost Centres.</div>';
        return;
    }
    host.innerHTML = sites.map(site => `<button type="button" class="cost-site-button ${site === window.costCentreState.selectedSite ? 'active' : ''}" data-site-index="${window.costCentreState.sites.indexOf(site)}">
        <span title="${escapeHtml(site.siteName)}">${escapeHtml(site.siteName)}</span><small>${site.costCentres?.length || 0}</small>
    </button>`).join('');
}

function selectCostCentreSite(site) {
    if (!site) return;
    window.costCentreState.selectedSite = site;
    window.costCentreState.selectedCentre = null;
    renderCostCentreSites();
    const detail = document.getElementById('costCentreDetail');
    const period = defaultFinancialPeriod();
    detail.innerHTML = `<div class="cost-centre-breadcrumb">Site / parent Cost Centre</div>
        <h5 class="mb-1">${escapeHtml(site.siteName)}</h5>
        <div class="small text-muted">Select a Cost Centre to load its vendor and voucher allocations.</div>
        <div class="finance-schedule site-centre-list mt-3"><div class="finance-schedule-title">Project / site Cost Centres</div><table class="table"><thead><tr><th class="centre-name">Cost Centre</th><th class="centre-classification">Classification</th><th class="centre-action">Action</th></tr></thead><tbody>${(site.costCentres || []).map((item, index) => {
            const nested = nestedCostCentreSite(item.name);
            const classification = nested ? 'Parent Cost Centre' : costCentreLabel(item.classification, item.name);
            const action = nested ? `View ${nested.costCentres.length} sub-centres ›` : 'Select for vouchers';
            return `<tr class="clickable-row" data-centre-index="${index}" tabindex="0"><td class="centre-name" title="${escapeHtml(item.name)}">${escapeHtml(item.name)}</td><td class="centre-classification">${escapeHtml(classification)}</td><td class="centre-action">${escapeHtml(action)}</td></tr>`;
        }).join('')}</tbody></table></div>
        <div class="cost-centre-period">
            <div><label for="costCentreFrom">From</label><input type="date" class="form-control" id="costCentreFrom" value="${period.from}"></div>
            <div><label for="costCentreTo">To</label><input type="date" class="form-control" id="costCentreTo" value="${period.to}"></div>
            <button type="button" class="btn btn-primary" id="loadSiteProfitability"><i class="fas fa-chart-line me-2"></i>Load Complete Site P&amp;L</button>
            <button type="button" class="btn btn-outline-primary" id="loadCostCentreTransactions" disabled><i class="fas fa-bolt me-2"></i>View Selected Cost Centre Transactions</button>
        </div>
        <div id="costCentreTransactionHost" class="cost-centre-empty"><div>Select one of the Cost Centres above.</div></div>`;
}

function selectCostCentre(index) {
    const centre = window.costCentreState.selectedSite?.costCentres?.[index];
    if (!centre) return;
    const nested = nestedCostCentreSite(centre.name);
    if (nested) { selectCostCentreSite(nested); return; }
    window.costCentreState.selectedCentre = centre;
    document.querySelectorAll('.cost-centre-chip').forEach((chip, chipIndex) => chip.classList.toggle('active', chipIndex === index));
    const button = document.getElementById('loadCostCentreTransactions');
    if (button) button.disabled = false;
    const host = document.getElementById('costCentreTransactionHost');
    if (host) host.innerHTML = `<div><strong>${escapeHtml(centre.name)}</strong><br>Choose the period and load its allocations from Tally.</div>`;
}

async function loadCostCentreTransactions() {
    const centre = window.costCentreState.selectedCentre;
    if (!centre) return;
    const from = document.getElementById('costCentreFrom')?.value;
    const to = document.getElementById('costCentreTo')?.value;
    const host = document.getElementById('costCentreTransactionHost');
    const button = document.getElementById('loadCostCentreTransactions');
    if (!from || !to) { host.innerHTML = '<div class="text-danger p-3">Select both From and To dates.</div>'; return; }
    button.disabled = true;
    host.className = '';
    renderCostCentreProgress(host, 0, 1, 'Tally vouchers');
    try {
        const query = new URLSearchParams({ costCentre: centre.name, from, to });
        const response = await fetch(`/api/cashflow/tally/cost-centres/transactions?${query}`, { credentials: 'same-origin' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || `HTTP ${response.status}`);
        const rows = data.transactions || [];
        rows.sort((left, right) => String(right.voucherDate || '').localeCompare(String(left.voucherDate || '')));
        window.costCentreState.visibleSiteRows = rows.map(row => ({
            ...row,
            amount: Number(row.allocatedAmount || 0),
            costCentreName: row.costCentreName || centre.name,
            classification: row.classification || row.ledgerName || centre.name
        }));
        const totalAllocated = rows.reduce((sum, row) => sum + Number(row.allocatedAmount || 0), 0);
        const vendorCount = new Set(rows.map(row => row.partyName).filter(name => name && name !== 'Internal journal / No party specified')).size;
        host.innerHTML = `<div class="cost-centre-summary"><strong>${rows.length} allocations</strong><span>${vendorCount} vendors/parties</span><span>${formatCurrency(totalAllocated)} allocated</span></div>
            <div class="cost-centre-table-wrap"><table class="table table-hover cost-centre-table">
            <colgroup><col style="width:11%"><col style="width:13%"><col style="width:12%"><col style="width:24%"><col style="width:22%"><col style="width:18%"></colgroup>
            <thead><tr><th>Date</th><th>Voucher</th><th>Type</th><th>Vendor / party</th><th>Ledger</th><th class="text-end">Allocated amount</th></tr></thead>
            <tbody>${rows.length ? rows.map((row, index) => `<tr class="cost-centre-detail-row" tabindex="0" role="button" data-site-allocation-index="${index}" aria-label="Open transaction details for ${escapeHtml(row.ledgerName || centre.name)}" title="Open transaction details">
                <td>${escapeHtml(formatDate(row.voucherDate))}</td><td>${escapeHtml(row.voucherNumber || row.reference || '—')}</td>
                <td>${escapeHtml(row.voucherType || '—')}</td><td title="${escapeHtml(row.partyName)}">${escapeHtml(row.partyName)}</td>
                <td title="${escapeHtml(row.ledgerName || '')}">${escapeHtml(row.ledgerName || '—')}</td><td class="text-end fw-bold">${formatCurrency(row.allocatedAmount)}</td>
            </tr>`).join('') : '<tr><td colspan="6" class="text-center text-muted py-4">No allocations found for this Cost Centre in the selected period.</td></tr>'}</tbody>
            </table></div>`;
    } catch (error) {
        host.innerHTML = `<div class="text-danger p-3">Unable to load Cost Centre transactions: ${escapeHtml(error.message)}</div>`;
    } finally { button.disabled = false; }
}

function siteReportRows(section) {
    const rows = window.costCentreState.siteRows || [];
    const salary = rows.filter(row => ['STAFF_SALARY', 'BACKEND_SALARY'].includes(row.classification));
    if (section === 'sales') return rows.filter(row => row.nature === 'REVENUE');
    if (section === 'salary') return salary;
    if (section === 'costs') return rows.filter(row => row.nature === 'EXPENSE' && !salary.includes(row));
    return rows;
}

function renderSiteProfitabilityBreakdown() {
    const host = document.getElementById('sitePnlBreakdown');
    if (!host) return;
    const section = window.costCentreState.siteReportSection || 'pnl';
    const rows = window.costCentreState.siteRows || [];
    if (section === 'pnl') {
        const report = window.costCentreState.siteReportData || {};
        const revenue = Number(report.revenue || 0);
        const expense = Number(report.expense || 0);
        const pnlGroups = new Set(['SALES ACCOUNTS', 'DIRECT INCOMES', 'INDIRECT INCOMES',
            'PURCHASE ACCOUNTS', 'DIRECT EXPENSES', 'INDIRECT EXPENSES']);
        const nativeLines = (report.pnlLines || []).filter(line => pnlGroups.has(String(line.particular || '').toUpperCase()));
        const particulars = [
            ...nativeLines.map(line => [line.particular, Number(line.closingBalance || 0),
                String(line.particular || '').toUpperCase().includes('SALES') || String(line.particular || '').toUpperCase().includes('INCOME')
                    ? 'finance-positive' : '']),
            ['Total expense', expense, 'finance-negative'],
            ['Net profit / (loss)', revenue - expense, revenue - expense >= 0 ? 'finance-positive' : 'finance-negative']
        ].filter(([,value]) => Number(value) !== 0);
        host.innerHTML = `<div class="finance-schedule"><div class="finance-schedule-title">P&amp;L statement</div><table class="table"><thead><tr><th>Particulars</th><th class="text-end">Amount</th></tr></thead><tbody>
            ${particulars.map(([label,value,css]) => `<tr><td class="site-report-particular">${escapeHtml(label)}</td><td class="text-end fw-bold ${css}">${formatCurrency(value)}</td></tr>`).join('')}
        </tbody></table></div>`;
        return;
    }
    const sectionRows = siteReportRows(section);
    window.costCentreState.visibleSiteRows = sectionRows;
    const title = section === 'sales' ? 'Sales details' : section === 'salary' ? 'Salary details' : 'Cost details';
    const total = sectionRows.reduce((sum,row) => sum + Number(row.amount || 0), 0);
    host.innerHTML = `<div class="cost-centre-summary"><strong>${sectionRows.length} supporting Tally allocations</strong><span>${formatCurrency(total)}</span></div>
        <div class="finance-schedule site-detail-schedule"><div class="finance-schedule-title">${title}</div><div class="cost-centre-table-wrap"><table class="table table-hover cost-centre-table">
        <colgroup><col style="width:11%"><col style="width:12%"><col style="width:11%"><col style="width:20%"><col style="width:17%"><col style="width:15%"><col style="width:14%"></colgroup>
        <thead><tr><th>Date</th><th>Voucher</th><th>Type</th><th>Party / vendor</th><th>Ledger</th><th>Classification</th><th class="text-end">Amount</th></tr></thead>
        <tbody>${sectionRows.map((row, index) => `<tr class="cost-centre-detail-row" tabindex="0" role="button" data-site-allocation-index="${index}" aria-label="Open transaction details for ${escapeHtml(row.ledgerName || '')}" title="Open transaction details"><td>${escapeHtml(formatDate(row.voucherDate))}</td><td>${escapeHtml(row.voucherNumber || row.reference || '—')}</td><td>${escapeHtml(row.voucherType || '—')}</td><td title="${escapeHtml(row.partyName || '')}">${escapeHtml(row.partyName || '—')}</td><td title="${escapeHtml(row.ledgerName || '')}">${escapeHtml(row.ledgerName || '—')}</td><td>${escapeHtml(costCentreLabel(row.classification, row.ledgerName || row.costCentreName))}</td><td class="text-end fw-bold">${formatCurrency(row.amount)}</td></tr>`).join('')}</tbody>
        </table></div></div>`;
}

function openCostCentreVoucherDrawer(row) {
    if (!row) return;
    window.selectedCostCentreAllocation = row;
    window.costCentreDrawerPreviousFocus = document.activeElement;
    const voucherIdentity = row.voucherNumber || row.reference || 'Cost Centre transaction';
    const partyKnown = row.partyName && !String(row.partyName).toLowerCase().includes('open in tally');
    document.getElementById('costCentreVoucherTitle').textContent = voucherIdentity;
    document.getElementById('costCentreVoucherParty').textContent = partyKnown ? row.partyName : row.ledgerName || 'Tally transaction';
    const summary = [
        ['Date', formatDate(row.voucherDate)], ['Voucher', row.voucherNumber || row.reference || 'Not exported'],
        ['Voucher type', row.voucherType || 'Not exported'], ['Party / vendor', partyKnown ? row.partyName : 'Not exported'],
        ['Ledger', row.ledgerName || '—'], ['Cost Centre', row.costCentreName || '—'],
        ['Classification', costCentreLabel(row.classification, row.ledgerName || row.costCentreName)],
        ['Reconciled amount', formatCurrency(row.amount)]
    ];
    document.getElementById('costCentreVoucherSummary').innerHTML = summary.map(([label, value]) =>
        `<div class="invoice-summary-item"><span>${escapeHtml(label)}</span><strong>${escapeHtml(value || '—')}</strong></div>`).join('');
    document.getElementById('costCentreVoucherNarration').textContent = row.narration || 'No narration exported by Tally.';
    document.getElementById('costCentreVoucherLiveDetails').classList.add('d-none');
    const status = document.getElementById('costCentreVoucherStatus');
    const button = document.getElementById('loadCostCentreVoucherDetails');
    if (row.voucherNumber || row.reference) {
        button.disabled = false;
        button.classList.remove('d-none');
        button.innerHTML = '<i class="fas fa-arrows-rotate me-2"></i>Load complete voucher from Tally';
        status.textContent = 'Full voucher details have not been loaded yet.';
        status.className = 'invoice-detail-status';
    } else {
        button.disabled = true;
        button.classList.add('d-none');
        status.textContent = 'Tally’s native Cost Centre report does not export the voucher number for this row. The reconciled date, ledger, Cost Centre and amount are shown above; open this entry in Tally for the complete voucher.';
        status.className = 'invoice-detail-status text-warning';
    }
    document.getElementById('costCentreVoucherDrawer').classList.add('show');
    document.getElementById('costCentreVoucherDrawer').setAttribute('aria-hidden', 'false');
    document.getElementById('costCentreVoucherBackdrop').classList.add('show');
    document.getElementById('closeCostCentreVoucherDrawer').focus();
}

function closeCostCentreVoucherDrawer() {
    const drawer = document.getElementById('costCentreVoucherDrawer');
    if (!drawer?.classList.contains('show')) return;
    drawer.classList.remove('show');
    drawer.setAttribute('aria-hidden', 'true');
    document.getElementById('costCentreVoucherBackdrop').classList.remove('show');
    window.costCentreDrawerPreviousFocus?.focus?.();
}

async function loadCostCentreVoucherDetails() {
    const row = window.selectedCostCentreAllocation;
    if (!row || !(row.voucherNumber || row.reference)) return;
    const button = document.getElementById('loadCostCentreVoucherDetails');
    const status = document.getElementById('costCentreVoucherStatus');
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-circle-notch fa-spin me-2"></i>Loading from Tally…';
    status.textContent = 'Matching the voucher safely in the company currently open in Tally…';
    status.className = 'invoice-detail-status text-primary';
    try {
        const partyKnown = row.partyName && !String(row.partyName).toLowerCase().includes('open in tally');
        const query = new URLSearchParams({
            voucherNumber: row.voucherNumber || row.reference || '', partyName: partyKnown ? row.partyName : '',
            voucherDate: row.voucherDate || ''
        });
        const response = await fetch(`/api/cashflow/tally/cost-centres/voucher-details?${query}`, { credentials: 'same-origin' });
        const payload = await response.json();
        if (!response.ok) throw new Error(payload.error || 'Unable to load voucher details.');
        if (!payload.voucher?.found) {
            status.textContent = payload.voucher?.message || 'No unique matching voucher was found in Tally.';
            status.className = 'invoice-detail-status text-warning';
            button.disabled = false;
            button.innerHTML = '<i class="fas fa-rotate me-2"></i>Try again';
            return;
        }
        const voucher = payload.voucher;
        const live = document.getElementById('costCentreVoucherLiveDetails');
        live.classList.remove('d-none');
        document.getElementById('costCentreVoucherLiveSummary').innerHTML = [
            ['Voucher type', voucher.voucherType], ['Voucher number', voucher.voucherNumber],
            ['Reference', voucher.reference], ['Voucher date', formatDate(voucher.voucherDate)],
            ['Tally Master ID', voucher.masterId], ['Matched using', voucher.matchedBy]
        ].map(([label, value]) => `<div class="invoice-summary-item"><span>${escapeHtml(label)}</span><strong>${escapeHtml(value || '—')}</strong></div>`).join('');
        document.getElementById('costCentreVoucherLedgers').innerHTML = detailTable(['Ledger', 'Classification', 'Amount'],
            (voucher.ledgerEntries || []).map(line => [line.name, line.tax ? 'Tax' : 'Ledger', formatCurrency(line.amount)]));
        document.getElementById('costCentreVoucherInventory').innerHTML = detailTable(['Item', 'Quantity', 'Rate', 'Amount'],
            (voucher.inventoryEntries || []).map(item => [item.name, item.quantity, item.rate, formatCurrency(item.amount)]));
        document.getElementById('costCentreVoucherAdjustments').innerHTML = detailTable(['Bill reference', 'Adjustment type', 'Amount'],
            (voucher.billAdjustments || []).map(item => [item.billName, item.billType, formatCurrency(item.amount)]));
        status.textContent = voucher.message || 'Complete voucher loaded from Tally.';
        status.className = 'invoice-detail-status text-success';
        button.innerHTML = '<i class="fas fa-check me-2"></i>Details loaded';
    } catch (error) {
        status.textContent = error.message;
        status.className = 'invoice-detail-status text-danger';
        button.disabled = false;
        button.innerHTML = '<i class="fas fa-rotate me-2"></i>Try again';
    }
}

function renderSiteProfitability(data) {
    const host = document.getElementById('costCentreTransactionHost');
    const rows = data.allocations || [];
    window.costCentreState.siteRows = rows;
    window.costCentreState.siteReportData = data;
    window.costCentreState.siteReportSection = 'pnl';
    const revenue = Number(data.revenue || 0);
    const expense = Number(data.expense || 0);
    const profit = revenue - expense;
    const margin = revenue ? profit / revenue * 100 : 0;
    const siteName = window.costCentreState.selectedSite?.siteName || 'Selected site';
    const available = [
        ['pnl', 'P&L', rows.length],
        ['costs', 'Costs', siteReportRows('costs').length],
        ['sales', 'Sales', siteReportRows('sales').length],
        ['salary', 'Salary', siteReportRows('salary').length]
    ].filter(([, , count]) => count > 0);
    const from = document.getElementById('costCentreFrom')?.value || '';
    const to = document.getElementById('costCentreTo')?.value || '';
    const exportUrl = `/api/cashflow/tally/cost-centres/site-report/export?${new URLSearchParams({ site: siteName, from, to })}`;
    host.innerHTML = `<div class="finance-schedule"><div class="finance-schedule-title">Tally Cost Centre Summary</div><table class="table">
        <thead><tr><th style="width:34%">Site</th><th class="text-end">Cost</th><th class="text-end">Total sales</th><th class="text-end">Closing balance</th></tr></thead>
        <tbody><tr class="schedule-total"><td>${escapeHtml(siteName)}</td><td class="text-end">${formatCurrency(data.debitTransactions)}</td><td class="text-end">${formatCurrency(data.creditTransactions)}</td><td class="text-end">${formatCurrency(data.closingBalance)}</td></tr></tbody></table></div>
    <div class="finance-schedule mt-2"><div class="finance-schedule-title">Allocated P&amp;L analysis</div><table class="table">
        <thead><tr><th style="width:30%">Site</th><th class="text-end">Revenue</th><th class="text-end">Expense</th><th class="text-end">Profit / (loss)</th><th class="text-end">Margin</th></tr></thead>
        <tbody><tr class="schedule-total"><td>${escapeHtml(siteName)}</td><td class="text-end">${formatCurrency(revenue)}</td><td class="text-end">${formatCurrency(expense)}</td><td class="text-end ${profit >= 0 ? 'finance-positive' : 'finance-negative'}">${formatCurrency(profit)}</td><td class="text-end ${margin >= 0 ? 'finance-positive' : 'finance-negative'}">${margin.toFixed(1)}%</td></tr></tbody></table></div>
    <div class="site-report-head"><div class="site-report-tabs">${available.map(([key,label]) => `<button type="button" class="site-report-tab ${key === 'pnl' ? 'active' : ''}" data-site-report-tab="${key}">${label}</button>`).join('')}</div>
    <a class="btn btn-primary btn-sm" href="${escapeHtml(window.cashflowUrl(exportUrl))}"><i class="fas fa-file-excel me-2"></i>Export Excel</a></div>
    <div class="site-report-note"><strong>Cost and Total Sales</strong> are the Debit and Credit transaction totals from Tally's native Cost Centre report for ${escapeHtml(from)} to ${escapeHtml(to)}. The Costs, Sales and Salary tabs contain supporting voucher rows exposed by Tally and may not contain every historical drill-down row.</div>
    <div id="sitePnlBreakdown"></div>`;
    renderSiteProfitabilityBreakdown();
}

async function loadSiteProfitability() {
    const site = window.costCentreState.selectedSite;
    const from = document.getElementById('costCentreFrom')?.value;
    const to = document.getElementById('costCentreTo')?.value;
    const host = document.getElementById('costCentreTransactionHost');
    const button = document.getElementById('loadSiteProfitability');
    if (!site || !from || !to) return;
    button.disabled = true;
    host.className = '';
    renderCostCentreProgress(host, 0, 1, 'Tally vouchers and ledger groups');
    try {
        const query = new URLSearchParams({ site: site.siteName, from, to });
        const response = await fetch(`/api/cashflow/tally/cost-centres/site-profitability?${query}`, { credentials: 'same-origin' });
        const body = await response.text();
        let data;
        try { data = body ? JSON.parse(body) : null; }
        catch (_) { throw new Error(`The server returned an invalid report (HTTP ${response.status}). Please retry.`); }
        if (!response.ok) throw new Error(data?.error || `The server rejected the report request (HTTP ${response.status}). Check the selected dates and try again.`);
        if (!data) throw new Error('The server returned an empty site report. Please retry.');
        (data.allocations || []).sort((a,b) => String(b.voucherDate || '').localeCompare(String(a.voucherDate || '')));
        renderSiteProfitability(data);
    } catch (error) {
        host.innerHTML = `<div class="text-danger p-3">Unable to load site P&amp;L: ${escapeHtml(error.message)}</div>`;
    } finally { button.disabled = false; }
}

async function loadCostCentres() {
    const host = document.getElementById('costCentreSitesList');
    const refresh = document.getElementById('refreshCostCentresBtn');
    if (host) host.innerHTML = '<div class="text-muted text-center p-4"><i class="fas fa-spinner fa-spin me-2"></i>Loading Cost Centres…</div>';
    if (refresh) refresh.disabled = true;
    try {
        const response = await fetch('/api/cashflow/tally/cost-centres', { credentials: 'same-origin' });
        const data = await response.json();
        if (!response.ok) throw new Error(data.error || `HTTP ${response.status}`);
        window.costCentreState = { sites: data.sites || [], selectedSite: null, selectedCentre: null, siteRows: [], selectedCategory: null, selectedVendor: null };
        document.getElementById('costCentreSubtitle').textContent = `${data.siteCount || 0} sites • ${data.costCentreCount || 0} Cost Centres from live Tally`;
        renderCostCentreSites();
    } catch (error) {
        if (host) host.innerHTML = `<div class="text-danger p-3">${escapeHtml(error.message)}</div>`;
    } finally { if (refresh) refresh.disabled = false; }
}

async function openCostCentresPanel(scrollIntoView = true) {
    document.getElementById('outstandingDrilldown')?.classList.remove('show');
    document.getElementById('advancesPanel')?.classList.remove('show');
    const panel = document.getElementById('costCentresPanel');
    panel?.classList.add('show');
    if (scrollIntoView) panel?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    await loadCostCentres();
}

function renderOutstandingAnalysis() {
    const state = window.outstandingState;
    const buckets = [
        { key: 'current', label: 'Current', color: '#16a34a' },
        { key: '0-30', label: '0–30 days', color: '#3b82f6' },
        { key: '31-60', label: '31–60 days', color: '#f59e0b' },
        { key: '61-90', label: '61–90 days', color: '#f97316' },
        { key: '90+', label: '90+ days', color: '#dc2626' }
    ];
    const totals = Object.fromEntries(buckets.map(bucket => [bucket.key, { amount: 0, count: 0 }]));
    state.rows.forEach(row => {
        const bucket = totals[row.agingBucket] || totals.current;
        bucket.amount += Number(row.amount) || 0;
        bucket.count += 1;
    });

    const cards = document.getElementById('outstandingAgingCards');
    if (cards) cards.innerHTML = buckets.map(bucket => `
        <button type="button" class="aging-card text-start" data-aging="${bucket.key}">
            <div class="aging-card-label">${bucket.label}</div>
            <div class="aging-card-value" style="color:${bucket.color}">${formatCurrency(totals[bucket.key].amount)}</div>
            <div class="aging-card-count">${totals[bucket.key].count} bills</div>
        </button>`).join('');

    const bar = document.getElementById('outstandingAgingBar');
    if (bar) bar.innerHTML = buckets.map(bucket => {
        const width = state.total > 0 ? totals[bucket.key].amount / state.total * 100 : 0;
        return `<span class="aging-segment" title="${bucket.label}: ${width.toFixed(1)}%" style="width:${width}%;background:${bucket.color}"></span>`;
    }).join('');

    const note = document.getElementById('outstandingAgingNote');
    const overdue = state.rows.filter(row => row.status === 'Overdue');
    const overdueAmount = overdue.reduce((sum, row) => sum + (Number(row.amount) || 0), 0);
    if (note) note.textContent = `${overdue.length} overdue bills • ${formatCurrency(overdueAmount)} overdue`;

    const parties = {};
    state.rows.forEach(row => { parties[row.partyName || 'Unknown'] = (parties[row.partyName || 'Unknown'] || 0) + (Number(row.amount) || 0); });
    const top = Object.entries(parties).sort((a, b) => b[1] - a[1]).slice(0, 3);
    const partyFilter = document.getElementById('outstandingPartyFilter');
    if (partyFilter) {
        const selected = partyFilter.value;
        partyFilter.innerHTML = '<option value="all">All parties</option>' + Object.keys(parties)
            .sort((a, b) => a.localeCompare(b))
            .map(name => `<option value="${escapeHtml(name)}">${escapeHtml(name)}</option>`).join('');
        partyFilter.value = Object.hasOwn(parties, selected) ? selected : 'all';
    }
    const concentration = document.getElementById('outstandingConcentration');
    if (concentration) concentration.innerHTML = top.length ? top.map(([name, amount]) => {
        const share = state.total > 0 ? amount / state.total * 100 : 0;
        return `<div class="concentration-row"><div class="concentration-label"><span title="${escapeHtml(name)}">${escapeHtml(name)}</span><strong>${share.toFixed(1)}%</strong></div><div class="concentration-track"><div class="concentration-fill" style="width:${Math.min(share, 100)}%"></div></div></div>`;
    }).join('') : '<div class="text-muted small">No party data available</div>';

    cards?.querySelectorAll('[data-aging]').forEach(button => button.addEventListener('click', () => {
        const filter = document.getElementById('outstandingAgingFilter');
        if (filter) filter.value = button.dataset.aging;
        renderOutstandingTable();
    }));
}

function getFilteredOutstandingRows() {
    const aging = document.getElementById('outstandingAgingFilter')?.value || 'all';
    const status = document.getElementById('outstandingStatusFilter')?.value || 'all';
    const party = document.getElementById('outstandingPartyFilter')?.value || 'all';
    const search = (document.getElementById('outstandingSearch')?.value || '').trim().toLowerCase();
    return window.outstandingState.rows.filter(row =>
        (aging === 'all' || row.agingBucket === aging) &&
        (status === 'all' || String(row.status || '').toLowerCase() === status) &&
        (party === 'all' || row.partyName === party) &&
        (!search || String(row.partyName || '').toLowerCase().includes(search)
            || String(row.invoiceNumber || '').toLowerCase().includes(search)));
}

function renderOutstandingTable() {
    const rows = getFilteredOutstandingRows();
    window.outstandingRenderedRows = rows;
    const tbody = document.getElementById('outstandingTableBody');
    const resultCount = document.getElementById('outstandingResultCount');
    if (resultCount) resultCount.textContent = `${rows.length} of ${window.outstandingState.rows.length} bills`;
    if (!tbody) return;
    if (!rows.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">No matching bills found</td></tr>';
        return;
    }
    tbody.innerHTML = rows.map((row, index) => {
        const isOverdue = row.status === 'Overdue';
        const days = isOverdue ? `${Number(row.daysOverdue) || 0} overdue`
            : (row.daysUntilDue == null ? 'Due date missing' : `${Number(row.daysUntilDue) || 0} remaining`);
        return `<tr tabindex="0" role="button" data-outstanding-index="${index}" aria-label="Open bill ${escapeHtml(row.invoiceNumber || '')} for ${escapeHtml(row.partyName || '')}">
            <td class="col-party party-cell" title="${escapeHtml(row.partyName || '-')}">${escapeHtml(row.partyName || '-')}</td>
            <td class="col-bill bill-cell" title="${escapeHtml(row.invoiceNumber || '-')}">${escapeHtml(row.invoiceNumber || '-')}</td>
            <td class="col-date">${escapeHtml(formatDate(row.invoiceDate))}</td>
            <td class="col-date">${escapeHtml(formatDate(row.dueDate))}</td>
            <td class="col-age">${row.billAgeDays == null ? '—' : `${Number(row.billAgeDays) || 0} days`}</td>
            <td class="col-amount fw-semibold">${formatCurrency(row.amount)}</td>
            <td class="col-days ${isOverdue ? 'text-danger' : ''}">${escapeHtml(days)}</td>
            <td class="col-status"><span class="badge ${isOverdue ? 'bg-danger' : 'bg-success'}">${escapeHtml(row.status)}</span></td>
        </tr>`;
    }).join('');
}

function openInvoiceDrawer(row) {
    if (!row) return;
    window.selectedOutstandingRow = row;
    window.invoiceDrawerPreviousFocus = document.activeElement;
    document.getElementById('invoiceDrawerTitle').textContent = row.invoiceNumber || 'Bill details';
    document.getElementById('invoiceDrawerParty').textContent = row.partyName || 'Party not available';
    document.getElementById('invoiceOutstandingValue').textContent = formatCurrency(row.amount);
    document.getElementById('invoiceOriginalValue').textContent = 'Load from Tally';
    document.getElementById('invoiceSummaryDate').textContent = formatDate(row.invoiceDate);
    document.getElementById('invoiceSummaryDue').textContent = formatDate(row.dueDate);
    document.getElementById('invoiceSummaryBillAge').textContent = row.billAgeDays == null ? '—' : `${Number(row.billAgeDays) || 0} days`;
    document.getElementById('invoiceSummaryStatus').textContent = row.status || '—';
    document.getElementById('invoiceSummaryAging').textContent = row.status === 'Overdue'
        ? `${Number(row.daysOverdue) || 0} days overdue`
        : (row.daysUntilDue == null ? 'Due date unavailable' : `${Number(row.daysUntilDue) || 0} days remaining`);
    document.getElementById('invoiceDetailStatus').textContent = 'Not loaded yet.';
    document.getElementById('invoiceDetailStatus').className = 'invoice-detail-status';
    document.getElementById('invoiceLiveDetails').classList.add('d-none');
    const loadButton = document.getElementById('loadInvoiceTallyDetails');
    loadButton.disabled = false;
    loadButton.innerHTML = '<i class="fas fa-arrows-rotate me-2"></i>Load invoice details from Tally';
    document.getElementById('invoiceDetailDrawer').classList.add('show');
    document.getElementById('invoiceDetailDrawer').setAttribute('aria-hidden', 'false');
    document.getElementById('invoiceDrawerBackdrop').classList.add('show');
    document.getElementById('closeInvoiceDrawer').focus();
}

function closeInvoiceDrawer() {
    const drawer = document.getElementById('invoiceDetailDrawer');
    if (!drawer?.classList.contains('show')) return;
    drawer.classList.remove('show');
    drawer.setAttribute('aria-hidden', 'true');
    document.getElementById('invoiceDrawerBackdrop').classList.remove('show');
    window.invoiceDrawerPreviousFocus?.focus?.();
}

function detailTable(headers, rows) {
    if (!rows?.length) return '<div class="invoice-detail-empty">No data returned by Tally for this section.</div>';
    return `<div class="table-responsive"><table class="invoice-detail-table"><thead><tr>${headers.map(item => `<th>${escapeHtml(item)}</th>`).join('')}</tr></thead><tbody>${rows.map(row =>
        `<tr>${row.map(cell => `<td>${escapeHtml(cell == null || cell === '' ? '—' : String(cell))}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`;
}

function renderLiveVoucherDetails(voucher) {
    const live = document.getElementById('invoiceLiveDetails');
    live.classList.remove('d-none');
    document.getElementById('invoiceOriginalValue').textContent = Number(voucher.originalInvoiceValue) > 0
        ? formatCurrency(voucher.originalInvoiceValue) : 'Not provided';
    const summary = [
        ['Voucher type', voucher.voucherType], ['Voucher number', voucher.voucherNumber],
        ['Reference', voucher.reference], ['Voucher date', formatDate(voucher.voucherDate)],
        ['Tally Master ID', voucher.masterId], ['Matched using', voucher.matchedBy]
    ];
    document.getElementById('voucherSummaryGrid').innerHTML = summary.map(([label, value]) =>
        `<div class="invoice-summary-item"><span>${escapeHtml(label)}</span><strong>${escapeHtml(value || '—')}</strong></div>`).join('');

    const taxes = Object.entries(voucher.taxSummary || {}).map(([name, amount]) => [name, formatCurrency(amount)]);
    document.getElementById('invoiceTaxSummary').innerHTML = detailTable(['Tax ledger', 'Amount'], taxes);
    document.getElementById('invoiceLedgerEntries').innerHTML = detailTable(['Ledger', 'Classification', 'Amount'],
        (voucher.ledgerEntries || []).map(line => [line.name, line.tax ? 'Tax' : 'Ledger', formatCurrency(line.amount)]));
    document.getElementById('invoiceInventoryEntries').innerHTML = detailTable(['Item', 'Quantity', 'Rate', 'Amount'],
        (voucher.inventoryEntries || []).map(item => [item.name, item.quantity, item.rate, formatCurrency(item.amount)]));
    document.getElementById('invoiceBillAdjustments').innerHTML = detailTable(['Bill reference', 'Adjustment type', 'Amount'],
        (voucher.billAdjustments || []).map(item => [item.billName, item.billType, formatCurrency(item.amount)]));
    document.getElementById('invoiceNarration').textContent = voucher.narration || 'No narration returned by Tally.';
}

async function loadInvoiceTallyDetails() {
    const row = window.selectedOutstandingRow;
    if (!row) return;
    const button = document.getElementById('loadInvoiceTallyDetails');
    const status = document.getElementById('invoiceDetailStatus');
    button.disabled = true;
    button.innerHTML = '<i class="fas fa-circle-notch fa-spin me-2"></i>Loading from Tally…';
    status.textContent = 'Connecting to the company currently open in TallyPrime…';
    status.className = 'invoice-detail-status text-primary';
    try {
        const query = new URLSearchParams({
            billNumber: row.invoiceNumber || '', partyName: row.partyName || '', invoiceDate: row.invoiceDate || ''
        });
        const response = await fetch(`/api/cashflow/tally/outstanding/details?${query}`, { credentials: 'same-origin' });
        const contentType = response.headers.get('content-type') || '';
        if (!contentType.includes('application/json')) {
            throw new Error('The backend is running an older build. Restart the application, refresh this page, and try again.');
        }
        const payload = await response.json();
        if (!response.ok) throw new Error(payload.error || 'Unable to load voucher details.');
        if (!payload.voucher?.found) {
            status.textContent = payload.voucher?.message || 'No matching voucher was found in Tally.';
            status.className = 'invoice-detail-status text-warning';
            return;
        }
        renderLiveVoucherDetails(payload.voucher);
        status.textContent = payload.voucher.message || 'Live voucher details loaded.';
        status.className = 'invoice-detail-status text-success';
        button.innerHTML = '<i class="fas fa-check me-2"></i>Details loaded';
    } catch (error) {
        status.textContent = error.message;
        status.className = 'invoice-detail-status text-danger';
        button.disabled = false;
        button.innerHTML = '<i class="fas fa-rotate me-2"></i>Try again';
    }
}

async function openOutstandingPanel(type, scrollIntoView = true, resetFilters = true) {
    const panel = document.getElementById('outstandingDrilldown');
    if (!panel) return;
    if (resetFilters) {
        const agingFilter = document.getElementById('outstandingAgingFilter');
        const statusFilter = document.getElementById('outstandingStatusFilter');
        const partyFilter = document.getElementById('outstandingPartyFilter');
        const searchInput = document.getElementById('outstandingSearch');
        if (agingFilter) agingFilter.value = 'all';
        if (statusFilter) statusFilter.value = 'all';
        if (partyFilter) partyFilter.value = 'all';
        if (searchInput) searchInput.value = '';
    }
    panel.classList.add('show');
    panel.classList.toggle('payables', type === 'payables');
    document.querySelectorAll('#tileOutstandingReceivables,#tileOutstandingPayables')
        .forEach(tile => tile.classList.toggle('selected-drilldown', tile.id === (type === 'receivables' ? 'tileOutstandingReceivables' : 'tileOutstandingPayables')));
    const title = type === 'receivables' ? 'Outstanding Receivables' : 'Outstanding Payables';
    document.getElementById('outstandingPanelTitle').textContent = title;
    document.getElementById('outstandingPanelSubtitle').textContent = 'Loading current Tally outstanding bills…';
    document.getElementById('outstandingTableBody').innerHTML = '<tr><td colspan="8" class="text-center py-4">Loading…</td></tr>';
    if (scrollIntoView) panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    try {
        const data = await fetchOutstanding(type);
        window.outstandingState = { type, rows: data.rows || [], total: Number(data.total) || 0 };
        document.getElementById('outstandingPanelSubtitle').textContent = `${formatCurrency(data.total)} across ${data.count || 0} outstanding bills`;
        const countEl = document.getElementById(type === 'receivables' ? 'outstandingReceivablesCount' : 'outstandingPayablesCount');
        if (countEl) countEl.textContent = `${data.count || 0} bills`;
        renderOutstandingAnalysis();
        renderOutstandingTable();
    } catch (error) {
        document.getElementById('outstandingPanelSubtitle').textContent = 'Unable to load outstanding bills';
        document.getElementById('outstandingTableBody').innerHTML = `<tr><td colspan="8" class="text-center text-danger py-4">${escapeHtml(error.message)}</td></tr>`;
    }
}

async function exportOutstandingExcel() {
    const type = window.outstandingState.type;
    if (!type) return;
    const button = document.getElementById('exportOutstandingBtn');
    const aging = document.getElementById('outstandingAgingFilter')?.value || 'all';
    const status = document.getElementById('outstandingStatusFilter')?.value || 'all';
    const party = document.getElementById('outstandingPartyFilter')?.value || 'all';
    const search = document.getElementById('outstandingSearch')?.value || '';
    button.disabled = true;
    try {
        const query = new URLSearchParams({ type, aging, status, party, search });
        const filename = type === 'receivables' ? 'Outstanding-Receivables.xlsx' : 'Outstanding-Payables.xlsx';
        await downloadExcel(`/api/cashflow/tally/outstanding/export?${query}`, filename);
    } catch (error) {
        console.error('Outstanding export failed:', error);
        alert(`Failed to export outstanding bills: ${error.message}`);
    } finally {
        button.disabled = false;
    }
}

// ========== DASHBOARD UPDATES ==========
function updateDashboard(data) {
    if (!data) return;
    window.lastOverviewData = data;
    console.log('📊 Updating dashboard visuals...');

    const sets = [
        ['activeCustomers', data.activeCustomers || 0],
        ['totalInflow', formatCurrency(data.totalInflow)],
        ['totalOutflow', formatCurrency(data.totalOutflow)],
        ['overviewBankBalance', formatCurrency(data.bankBalance || 0)],
        ['inflowAmount', formatCurrency(data.totalInflow)],
        ['bankBalanceInTile', formatCurrency(data.bankBalance || 0)],
        ['tileAllCount', data.totalOverdueCount || 0],
        ['tileAllAmount', formatCurrency((data.overdueReceivables || []).reduce((s,i) => s + i.amount, 0) + (data.overduePayables || []).reduce((s,i) => s + i.amount, 0))]
    ];

    sets.forEach(([id, val]) => {
        const el = document.getElementById(id);
        if (el) el.textContent = val;
    });

    const netEl = document.getElementById('netCashflow');
    if (netEl) {
        const net = (data.totalInflow || 0) - (data.totalOutflow || 0);
        netEl.textContent = formatCurrency(net);
        netEl.className = 'tile-value ' + (net >= 0 ? 'text-success' : 'text-danger');
    }

    const rateEl = document.getElementById('collectionRate');
    if (rateEl) {
        const rate = data.collectionSuccessRate || 0;
        rateEl.textContent = rate.toFixed(1) + '%';
        const pbar = document.getElementById('collectionProgressBar');
        if (pbar) pbar.style.width = rate + '%';
    }

    // Secondary tiles
    const recCount = (data.overdueReceivables || []).length;
    const recAmt = (data.overdueReceivables || []).reduce((s,i) => s + i.amount, 0);
    const payCount = (data.overduePayables || []).length;
    const payAmt = (data.overduePayables || []).reduce((s,i) => s + i.amount, 0);

    const txts = [
        ['tileReceivablesCount', recCount],
        ['tileReceivablesAmount', formatCurrency(recAmt)],
        ['tilePayablesCount', payCount],
        ['tilePayablesAmount', formatCurrency(payAmt)],
        ['tileDueReceivablesCount', (data.dueReceivables || []).length],
        ['tileDueReceivablesAmount', formatCurrency((data.dueReceivables || []).reduce((s,i) => s + i.amount, 0))],
        ['tileDuePayablesCount', (data.duePayables || []).length],
        ['tileDuePayablesAmount', formatCurrency((data.duePayables || []).reduce((s,i) => s + i.amount, 0))]
    ];
    txts.forEach(([id, val]) => {
        const el = document.getElementById(id);
        if (el) el.textContent = val;
    });
    
    console.log('📊 Dashboard updated with fresh data');
}

// ========== MODAL POPULATION ==========
function populateTable(tbodyId, rows, badgeText = 'Overdue', badgeClass = 'bg-danger') {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    tbody.innerHTML = '';

    // Find the table header to determine the exact column structure and classes
    const table = tbody.closest('table');
    const headerEls = table ? Array.from(table.querySelectorAll('thead th')) : [];
    const headers = headerEls.map(th => th.textContent.trim().toLowerCase());
    const headerClasses = headerEls.map(th => (th.className || '').trim());
    const colCount = headers.length || 1;

    if (!rows || rows.length === 0) {
        tbody.innerHTML = `<tr><td colspan="${colCount}" class="text-center text-muted">No records found</td></tr>`;
        return;
    }

    rows.forEach(inv => {
        // Build cells in the same order as headers so alignment and widths match
        const cells = headers.map((h, idx) => {
            const cls = headerClasses[idx] ? ` ${headerClasses[idx]}` : '';
            if (/type/.test(h)) return `<td class="${cls}">${inv.type || inv.direction || '-'}</td>`;
            if (/invoice/.test(h)) return `<td class="${cls}">${inv.invoiceNumber || inv.invoice || inv.invoiceNo || '-'}</td>`;
            if (/name/.test(h)) return `<td class="${cls}">${inv.customerName || inv.name || '-'}</td>`;
            if (/amount/.test(h)) return `<td class="text-end${cls}">${formatCurrency(inv.amount)}</td>`;
            if (/due date|due\b/.test(h)) return `<td class="${cls}">${formatDate(inv.dueDate || inv.due)}</td>`;
            if (/days/.test(h)) return `<td class="text-end${cls}">${(inv.daysOverdue !== undefined ? inv.daysOverdue : (inv.daysUntilDue !== undefined ? inv.daysUntilDue : 0))} days</td>`;
            if (/status|severity/.test(h)) {
                const sev = (inv.severity || inv.status || '').toString().toLowerCase();
                let sevClass = badgeClass;
                if (/minor|low/.test(sev)) sevClass = 'bg-success';
                else if (/moderate|medium/.test(sev)) sevClass = 'bg-warning';
                else if (/serious|high/.test(sev)) sevClass = 'bg-danger';
                return `<td class="${cls}"><span class="badge ${sevClass}">${inv.severity || inv.status || badgeText}</span></td>`;
            }
            // Fallback: try common properties using header text or class key
            const key = h.replace(/\s+/g,'');
            const fallback = inv[key] !== undefined ? inv[key] : (inv[h] !== undefined ? inv[h] : '-');
            return `<td class="${cls}">${fallback}</td>`;
        });

        const rowHtml = `<tr>${cells.join('')}</tr>`;
        tbody.insertAdjacentHTML('beforeend', rowHtml);
    });
}

function openDetailsModal(type) {
    console.log('🔓 Opening Modal:', type);
    const data = window.lastOverviewData;
    if (!data) return;

    if (type === 'activeCustomers') {
        const tbody = document.getElementById('activeCustomersModalBody');
        if (tbody) {
            tbody.innerHTML = '';
            // Aggregate totals from overdueReceivables and dueReceivables
            const allRec = [...(data.overdueReceivables || []), ...(data.dueReceivables || [])];
            const stats = {};
            
            allRec.forEach(inv => {
                const name = inv.customerName || 'Unknown';
                if (!stats[name]) stats[name] = { count: 0, amount: 0 };
                stats[name].count++;
                stats[name].amount += (inv.amount || 0);
            });

            Object.entries(stats)
                .sort((a,b) => b[1].amount - a[1].amount)
                .forEach(([name, s]) => {
                    tbody.insertAdjacentHTML('beforeend', `<tr>
                        <td>${name}</td>
                        <td class="text-end">${s.count}</td>
                        <td class="text-end">${formatCurrency(s.amount)}</td>
                    </tr>`);
                });
        }
        if (activeCustomersModal) activeCustomersModal.show();
        return;
    }

    if (type === 'dueReceivables' || type === 'duePayables' || type === 'dueAll') {
        populateTable('dueModalReceivablesBody', data.dueReceivables || [], 'Due', 'bg-warning');
        populateTable('dueModalPayablesBody', data.duePayables || [], 'Due', 'bg-warning');
        populateTable('dueModalAllBody', [...(data.dueReceivables || []), ...(data.duePayables || [])], 'Due', 'bg-warning');
        if (dueModal) dueModal.show();
        return;
    }

    populateTable('modalReceivablesBody', data.overdueReceivables || []);
    populateTable('modalPayablesBody', data.overduePayables || []);
    if (overdueModal) overdueModal.show();
}

// ========== OVERDUE DETAILS MODAL FILTERING ==========
function applyModalFilters() {
    const searchName = (document.getElementById('modalSearchName')?.value || '').toLowerCase();
    const fromDate = document.getElementById('modalFromDate')?.value || '';
    const toDate = document.getElementById('modalToDate')?.value || '';
    const minAmount = parseFloat(document.getElementById('modalMinAmount')?.value) || 0;
    
    const filterTable = (tbodyId) => {
        const tbody = document.getElementById(tbodyId);
        if (!tbody) return;
        
        Array.from(tbody.querySelectorAll('tr')).forEach(row => {
            if (row.cells.length < 5) return;
            
            const name = (row.cells[1]?.textContent || '').toLowerCase();
            const amount = parseAmount(row.cells[2]?.textContent);
            const dueDate = row.cells[3]?.textContent || '';
            
            let visible = true;
            if (searchName && !name.includes(searchName)) visible = false;
            if (minAmount && amount < minAmount) visible = false;
            if (fromDate && dueDate) {
                const rowDate = new Date(dueDate.split('/').reverse().join('-'));
                const from = new Date(fromDate);
                if (rowDate < from) visible = false;
            }
            if (toDate && dueDate) {
                const rowDate = new Date(dueDate.split('/').reverse().join('-'));
                const to = new Date(toDate);
                if (rowDate > to) visible = false;
            }
            
            row.style.display = visible ? '' : 'none';
        });
    };
    
    filterTable('modalReceivablesBody');
    filterTable('modalPayablesBody');
    
    // Also filter the All tab content
    const allBody = document.getElementById('modalAllBody');
    if (allBody) {
        Array.from(allBody.querySelectorAll('tr')).forEach(row => {
            if (row.cells.length < 5) return;
            
            const name = (row.cells[1]?.textContent || '').toLowerCase();
            const amount = parseAmount(row.cells[2]?.textContent);
            const dueDate = row.cells[3]?.textContent || '';
            
            let visible = true;
            if (searchName && !name.includes(searchName)) visible = false;
            if (minAmount && amount < minAmount) visible = false;
            if (fromDate && dueDate) {
                const rowDate = new Date(dueDate.split('/').reverse().join('-'));
                const from = new Date(fromDate);
                if (rowDate < from) visible = false;
            }
            if (toDate && dueDate) {
                const rowDate = new Date(dueDate.split('/').reverse().join('-'));
                const to = new Date(toDate);
                if (rowDate > to) visible = false;
            }
            
            row.style.display = visible ? '' : 'none';
        });
    }
}

// ========== ACTIVE CUSTOMERS MODAL FILTERING ==========
function applyCustomersModalFilters() {
    const searchName = (document.getElementById('customersModalSearchName')?.value || '').toLowerCase();
    const minInvoices = parseInt(document.getElementById('customersModalMinInvoices')?.value) || 0;
    const minAmount = parseFloat(document.getElementById('customersModalMinAmount')?.value) || 0;
    
    const tbody = document.getElementById('activeCustomersModalBody');
    if (!tbody) return;
    
    Array.from(tbody.querySelectorAll('tr')).forEach(row => {
        if (row.cells.length < 3) return;
        
        const name = (row.cells[0].textContent || '').toLowerCase();
        const invoiceCount = parseInt(row.cells[1].textContent) || 0;
        const amount = parseAmount(row.cells[2].textContent);
        
        let visible = true;
        if (searchName && !name.includes(searchName)) visible = false;
        if (minInvoices && invoiceCount < minInvoices) visible = false;
        if (minAmount && amount < minAmount) visible = false;
        
        row.style.display = visible ? '' : 'none';
    });
}

// ========== API CALLS ==========
async function fetchOverviewData() {
    try {
        const response = await fetch('/api/cashflow/overview/cached', { credentials: 'same-origin' });
        if (response.ok) {
            updateDashboard(await response.json());
        }
    } catch (e) { console.error('Fetch error:', e); }
}

async function checkSessionData() {
    try {
        const response = await fetch('/api/cashflow/session-info', { credentials: 'same-origin' });
        const data = await response.json();
        if (data.hasData) {
            const disp = document.getElementById('fileNameDisplay');
            if (disp) disp.classList.remove('d-none');
            const txt = document.getElementById('fileNameText');
            if (txt) txt.textContent = data.fileName || 'Shared Records';
            await fetchOverviewData();
        }
    } catch (e) {}
}

// ========== LIVE TALLY STATUS ==========
function formatSyncTime(value) {
    if (!value) return 'Never';
    const date = Array.isArray(value)
        ? new Date(value[0], value[1] - 1, value[2], value[3] || 0, value[4] || 0, value[5] || 0)
        : new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    const datePart = date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    const timePart = date.toLocaleTimeString('en-IN', {
        hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true
    });
    return `${datePart}, ${timePart}`;
}

async function loadTallySyncStatus() {
    const card = document.getElementById('syncStatusCard');
    if (!card) return;

    const statusText = document.getElementById('syncStatusText');
    const timestampLabel = document.querySelector('#syncTimestamp .sync-timestamp-label');
    const timestampValue = document.getElementById('syncTimestampValue');
    const icon = document.getElementById('syncIcon');
    try {
        const response = await fetch('/api/cashflow/tally/status', { credentials: 'same-origin' });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const result = await response.json();
        const connected = result.config?.serverAvailable === true;
        const sync = result.syncStatus;

        card.classList.toggle('success', connected);
        icon?.classList.toggle('success', connected);
        statusText.textContent = connected
            ? `Tally Live${sync?.invoicesSynced != null ? ` • ${sync.invoicesSynced} bills` : ''}`
            : 'Tally unavailable';
        const formattedTime = formatSyncTime(sync?.lastSyncEndTime || sync?.lastSyncTime);
        if (timestampLabel) timestampLabel.textContent = 'Last sync';
        if (timestampValue) timestampValue.textContent = formattedTime;
        card.title = `${statusText.textContent}\nLast sync: ${formattedTime}`;
    } catch (error) {
        card.classList.remove('success');
        icon?.classList.remove('success');
        statusText.textContent = 'Tally status unavailable';
        if (timestampLabel) timestampLabel.textContent = 'Connection status';
        if (timestampValue) timestampValue.textContent = `Error: ${error.message}`;
        card.title = `${statusText.textContent}\nConnection error: ${error.message}`;
    }
}

async function triggerManualSync() {
    const card = document.getElementById('syncStatusCard');
    const button = document.getElementById('syncNowBtn');
    const icon = document.getElementById('syncIcon');
    const statusText = document.getElementById('syncStatusText');
    const timestampLabel = document.querySelector('#syncTimestamp .sync-timestamp-label');
    const timestampValue = document.getElementById('syncTimestampValue');
    if (!card || !button) return;

    button.disabled = true;
    card.classList.remove('success');
    card.classList.add('syncing');
    icon?.classList.remove('success');
    icon?.classList.add('syncing');
    statusText.textContent = 'Syncing with Tally…';
    if (timestampLabel) timestampLabel.textContent = 'Sync status';
    if (timestampValue) timestampValue.textContent = 'Keep TallyPrime open';
    card.title = `${statusText.textContent}\nPlease keep TallyPrime open`;

    try {
        const response = await fetch('/api/cashflow/tally/sync', {
            method: 'POST',
            credentials: 'same-origin'
        });
        const result = await response.json();
        if (!response.ok || result.success === false || result.status?.lastSyncStatus === 'FAILED') {
            throw new Error(result.message || result.status?.lastErrorMessage || `HTTP ${response.status}`);
        }
        await loadTallySyncStatus();
        await fetchOverviewData();
    } catch (error) {
        statusText.textContent = 'Tally sync failed';
        if (timestampLabel) timestampLabel.textContent = 'Sync error';
        if (timestampValue) timestampValue.textContent = error.message;
    } finally {
        card.classList.remove('syncing');
        icon?.classList.remove('syncing');
        button.disabled = false;
    }
}

window.triggerManualSync = triggerManualSync;

async function refreshLiveTallyDashboard() {
    if (document.hidden || !document.getElementById('syncStatusCard')) return;
    try {
        const response = await fetch('/api/cashflow/tally/overview', { credentials: 'same-origin' });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const result = await response.json();
        if (result.hasData && result.data) updateDashboard(result.data);
    } catch (error) {
        console.warn('Live Tally dashboard refresh failed:', error.message);
    } finally {
        await loadTallySyncStatus();
        await refreshOutstandingCounts();
        if (window.outstandingState.type && document.getElementById('outstandingDrilldown')?.classList.contains('show')) {
            await openOutstandingPanel(window.outstandingState.type, false, false);
        }
    }
}

function startTallyLiveRefresh() {
    if (!document.getElementById('syncStatusCard') || window.tallyLiveRefreshTimer) return;
    window.tallyLiveRefreshTimer = window.setInterval(refreshLiveTallyDashboard, 60000);
}

// ========== INITIALIZATION ==========
function currentFinanceSection() {
    const section = document.body?.dataset.initialSection || 'overview';
    return ['overview', 'receivables', 'payables', 'advances', 'cost-centres', 'aging', 'reconciliation'].includes(section)
        ? section : 'overview';
}

function activateFinanceNav(section) {
    document.querySelectorAll('[data-finance-nav]').forEach(nav =>
        nav.classList.toggle('active', nav.dataset.financeNav === section));
}

function wireEvents() {
    console.log('🔌 Wiring event listeners to tiles...');

    document.getElementById('sidebarCollapseBtn')?.addEventListener('click', event => {
        const root = document.documentElement;
        const collapsed = root.classList.toggle('sidebar-precollapsed');
        localStorage.setItem('financeSidebarCollapsed', String(collapsed));
        event.currentTarget.setAttribute('aria-label', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
        event.currentTarget.setAttribute('title', collapsed ? 'Expand sidebar' : 'Collapse sidebar');
    });

    document.getElementById('overviewWorkspace')?.addEventListener('click', event => {
        const upcomingTab = event.target.closest('[data-upcoming-type]');
        if (upcomingTab) {
            window.snapshotUpcomingType = upcomingTab.dataset.upcomingType;
            renderSnapshotUpcoming();
            return;
        }
        const upcomingRow = event.target.closest('[data-upcoming-index]');
        if (upcomingRow) {
            openInvoiceDrawer(window.snapshotUpcomingRows?.[Number(upcomingRow.dataset.upcomingIndex)]);
            return;
        }
        const openButton = event.target.closest('[data-snapshot-open]');
        if (openButton) void openOutstandingPanel(openButton.dataset.snapshotOpen, true);
    });
    document.getElementById('overviewWorkspace')?.addEventListener('keydown', event => {
        if (event.key !== 'Enter' && event.key !== ' ') return;
        const upcomingRow = event.target.closest('[data-upcoming-index]');
        if (!upcomingRow) return;
        event.preventDefault();
        openInvoiceDrawer(window.snapshotUpcomingRows?.[Number(upcomingRow.dataset.upcomingIndex)]);
    });

    const financeNav = document.querySelector('.finance-nav');
    const graphsToggle = document.querySelector('[data-graphs-toggle]');
    if (financeNav && graphsToggle) {
        const graphStateKey = 'neptuneGraphsExpanded';
        const shouldExpand = localStorage.getItem(graphStateKey) === 'true';
        financeNav.classList.toggle('graphs-expanded', shouldExpand);
        graphsToggle.setAttribute('aria-expanded', String(shouldExpand));
        graphsToggle.addEventListener('click', () => {
            const expanded = financeNav.classList.toggle('graphs-expanded');
            graphsToggle.setAttribute('aria-expanded', String(expanded));
            localStorage.setItem(graphStateKey, String(expanded));
        });
    }

    ['reconciliationModal', 'agingAnalysisModal'].forEach(modalId => {
        document.getElementById(modalId)?.addEventListener('hidden.bs.modal', () => {
            if (['/reconciliation', '/aging'].includes(window.location.pathname.substring(window.cashflowBase.length))) {
                window.location.assign(window.cashflowUrl('/overview'));
            } else {
                activateFinanceNav('overview');
            }
        });
    });
    
    const mappings = [
        ['tileActiveCustomers', () => openDetailsModal('activeCustomers')],
        ['tileOverdueReceivables', () => openDetailsModal('receivables')],
        ['tileOverduePayables', () => openDetailsModal('payables')],
        ['tileOverdueAll', () => openDetailsModal('all')],
        ['tileDueReceivables', () => openDetailsModal('dueReceivables')],
        ['tileDuePayables', () => openDetailsModal('duePayables')]
    ];

    mappings.forEach(([id, fn]) => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('click', fn);
            el.style.cursor = 'pointer';
        }
    });

    const outstandingTiles = [
        ['tileOutstandingReceivables', 'receivables'],
        ['tileOutstandingPayables', 'payables']
    ];
    outstandingTiles.forEach(([id, type]) => {
        const tile = document.getElementById(id);
        if (!tile) return;
        tile.addEventListener('click', () => window.location.assign(window.cashflowUrl(`/${type}`)));
        tile.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                window.location.assign(window.cashflowUrl(`/${type}`));
            }
        });
    });

    document.getElementById('closeOutstandingPanel')?.addEventListener('click', () => {
        if (['/receivables', '/payables'].includes(window.location.pathname.substring(window.cashflowBase.length))) {
            window.location.assign(window.cashflowUrl('/overview'));
            return;
        }
        document.getElementById('outstandingDrilldown')?.classList.remove('show');
        document.querySelectorAll('#tileOutstandingReceivables,#tileOutstandingPayables')
            .forEach(tile => tile.classList.remove('selected-drilldown'));
        window.outstandingState.type = null;
    });
    document.getElementById('closeAdvancesPanel')?.addEventListener('click', () => window.location.assign(window.cashflowUrl('/overview')));
    document.getElementById('advanceTypeFilter')?.addEventListener('change', renderAdvancesTable);
    document.getElementById('advanceSearch')?.addEventListener('input', renderAdvancesTable);
    document.getElementById('refreshAdvancesBtn')?.addEventListener('click', loadAdvances);
    document.getElementById('exportAdvancesBtn')?.addEventListener('click', exportAdvancesExcel);
    document.getElementById('closeCostCentresPanel')?.addEventListener('click', () => window.location.assign(window.cashflowUrl('/overview')));
    document.getElementById('refreshCostCentresBtn')?.addEventListener('click', loadCostCentres);
    document.getElementById('costCentreSearch')?.addEventListener('input', renderCostCentreSites);
    document.getElementById('costCentreSitesList')?.addEventListener('click', event => {
        const button = event.target.closest('[data-site-index]');
        if (button) selectCostCentreSite(window.costCentreState.sites[Number(button.dataset.siteIndex)]);
    });
    document.getElementById('costCentreDetail')?.addEventListener('click', event => {
        const allocationRow = event.target.closest('[data-site-allocation-index]');
        if (allocationRow) {
            openCostCentreVoucherDrawer(window.costCentreState.visibleSiteRows?.[Number(allocationRow.dataset.siteAllocationIndex)]);
            return;
        }
        const chip = event.target.closest('[data-centre-index]');
        if (chip) selectCostCentre(Number(chip.dataset.centreIndex));
        if (event.target.closest('#loadCostCentreTransactions')) loadCostCentreTransactions();
        if (event.target.closest('#loadSiteProfitability')) loadSiteProfitability();
        const reportTab = event.target.closest('[data-site-report-tab]');
        if (reportTab) {
            window.costCentreState.siteReportSection = reportTab.dataset.siteReportTab;
            document.querySelectorAll('[data-site-report-tab]').forEach(tab =>
                tab.classList.toggle('active', tab === reportTab));
            renderSiteProfitabilityBreakdown();
        }
        const category = event.target.closest('[data-profit-category]');
        if (category) {
            window.costCentreState.selectedCategory = category.dataset.profitCategory;
            window.costCentreState.selectedVendor = null;
            document.querySelectorAll('[data-profit-category]').forEach(card => card.classList.toggle('active', card === category));
            renderSiteProfitabilityBreakdown();
        }
        const vendor = event.target.closest('[data-profit-vendor]');
        if (vendor) {
            window.costCentreState.selectedVendor = vendor.dataset.profitVendor;
            renderSiteProfitabilityBreakdown();
        }
    });
    document.getElementById('costCentreDetail')?.addEventListener('keydown', event => {
        if (!['Enter', ' '].includes(event.key)) return;
        const row = event.target.closest('[data-site-allocation-index]');
        if (!row) return;
        event.preventDefault();
        openCostCentreVoucherDrawer(window.costCentreState.visibleSiteRows?.[Number(row.dataset.siteAllocationIndex)]);
    });
    document.getElementById('advancesTableBody')?.addEventListener('click', event => {
        const row = event.target.closest('tr[data-advance-index]');
        if (row) openAdvanceDrawer(window.advanceRenderedRows[Number(row.dataset.advanceIndex)]);
    });
    document.getElementById('advancesTableBody')?.addEventListener('keydown', event => {
        if (!['Enter', ' '].includes(event.key)) return;
        const row = event.target.closest('tr[data-advance-index]');
        if (!row) return;
        event.preventDefault();
        openAdvanceDrawer(window.advanceRenderedRows[Number(row.dataset.advanceIndex)]);
    });
    document.getElementById('closeAdvanceDrawer')?.addEventListener('click', closeAdvanceDrawer);
    document.getElementById('advanceDrawerBackdrop')?.addEventListener('click', closeAdvanceDrawer);
    document.getElementById('loadAdvanceTallyDetails')?.addEventListener('click', loadAdvanceTallyDetails);
    document.getElementById('outstandingAgingFilter')?.addEventListener('change', renderOutstandingTable);
    document.getElementById('outstandingStatusFilter')?.addEventListener('change', renderOutstandingTable);
    document.getElementById('outstandingPartyFilter')?.addEventListener('change', renderOutstandingTable);
    document.getElementById('outstandingSearch')?.addEventListener('input', renderOutstandingTable);
    document.getElementById('exportOutstandingBtn')?.addEventListener('click', exportOutstandingExcel);
    document.getElementById('outstandingTableBody')?.addEventListener('click', event => {
        const rowElement = event.target.closest('tr[data-outstanding-index]');
        if (rowElement) openInvoiceDrawer(window.outstandingRenderedRows?.[Number(rowElement.dataset.outstandingIndex)]);
    });
    document.getElementById('outstandingTableBody')?.addEventListener('keydown', event => {
        if (event.key !== 'Enter' && event.key !== ' ') return;
        const rowElement = event.target.closest('tr[data-outstanding-index]');
        if (!rowElement) return;
        event.preventDefault();
        openInvoiceDrawer(window.outstandingRenderedRows?.[Number(rowElement.dataset.outstandingIndex)]);
    });
    document.getElementById('closeInvoiceDrawer')?.addEventListener('click', closeInvoiceDrawer);
    document.getElementById('invoiceDrawerBackdrop')?.addEventListener('click', closeInvoiceDrawer);
    document.getElementById('loadInvoiceTallyDetails')?.addEventListener('click', loadInvoiceTallyDetails);
    document.getElementById('closeCostCentreVoucherDrawer')?.addEventListener('click', closeCostCentreVoucherDrawer);
    document.getElementById('costCentreVoucherBackdrop')?.addEventListener('click', closeCostCentreVoucherDrawer);
    document.getElementById('loadCostCentreVoucherDetails')?.addEventListener('click', loadCostCentreVoucherDetails);
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') {
            closeInvoiceDrawer();
            closeAdvanceDrawer();
            closeCostCentreVoucherDrawer();
        }
    });
}

async function init() {
    if (window.didInit) return;
    window.didInit = true;

    try {
        const om = document.getElementById('overdueDetailsModal');
        if (om) overdueModal = new bootstrap.Modal(om);
        const dm = document.getElementById('dueDetailsModal');
        if (dm) dueModal = new bootstrap.Modal(dm);
        const am = document.getElementById('activeCustomersModal');
        if (am) activeCustomersModal = new bootstrap.Modal(am);
    } catch (e) {}

    // Setup real-time filtering for overdue modal
    ['modalSearchName', 'modalFromDate', 'modalToDate', 'modalMinAmount'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('input', applyModalFilters);
            el.addEventListener('change', applyModalFilters);
        }
    });

    // Setup real-time filtering for customers modal
    ['customersModalSearchName', 'customersModalMinInvoices', 'customersModalMinAmount'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener('input', applyCustomersModalFilters);
        }
    });

    wireEvents();
    const initialSection = currentFinanceSection();
    activateFinanceNav(initialSection);
    bindExcelUpload();
    // Tally may be offline or on another PC. Do not make local pages wait for
    // the network timeout before their own panels and dialogs can open.
    void loadTallySyncStatus();
    const startupTasks = [checkSessionData(), refreshOutstandingCounts()];
    if (initialSection === 'overview') startupTasks.push(loadOverviewForecast());
    await Promise.all(startupTasks);
    if (initialSection === 'receivables' || initialSection === 'payables') {
        await openOutstandingPanel(initialSection, false);
    } else if (initialSection === 'advances') {
        await openAdvancesPanel(false);
    } else if (initialSection === 'cost-centres') {
        await openCostCentresPanel(false);
    } else if (initialSection === 'aging') {
        bootstrap.Modal.getOrCreateInstance(document.getElementById('agingAnalysisModal')).show();
    } else if (initialSection === 'reconciliation') {
        bootstrap.Modal.getOrCreateInstance(document.getElementById('reconciliationModal')).show();
    }
    startTallyLiveRefresh();
}

window.addEventListener('load', init);
document.addEventListener('DOMContentLoaded', init);
setTimeout(init, 1000);
