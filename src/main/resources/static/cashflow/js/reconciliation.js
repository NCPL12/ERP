(function() {
    const ids = {
        modal: 'reconciliationModal',
        fromDate: ['reconciliationFilterFromDate', 'reconciliationModalFilterFromDate'],
        toDate: ['reconciliationFilterToDate', 'reconciliationModalFilterToDate'],
        search: ['reconciliationSearchName', 'reconciliationModalSearchName'],
        minAmount: ['reconciliationMinAmount', 'reconciliationModalMinAmount'],
        clearFilters: ['reconciliationClearFilters', 'reconciliationModalClearFilters'],
        receivablesTable: ['receivablesTableBody', 'reconciliationModalReceivablesTableBody'],
        payablesTable: ['payablesTableBody', 'reconciliationModalPayablesTableBody'],
        receivablesSummary: ['receivablesSummary', 'reconciliationModalReceivablesSummary'],
        payablesSummary: ['payablesSummary', 'reconciliationModalPayablesSummary'],
        debtorChart: 'reconciliationDebtorsChart',
        creditorChart: 'reconciliationCreditorsChart',
        debtorList: 'reconciliationDebtorsList',
        creditorList: 'reconciliationCreditorsList',
        highestDebtorName: 'highestDebtorName',
        highestDebtorAmount: 'highestDebtorAmount',
        highestDebtorDays: 'highestDebtorDays',
        highestCreditorName: 'highestCreditorName',
        highestCreditorAmount: 'highestCreditorAmount',
        highestCreditorDays: 'highestCreditorDays',
        editAdjustedBalance: 'editAdjustedBalanceBtn',
        saveAdjustedBalance: 'saveAdjustedBalanceBtn',
        manualAdjustedBalanceInput: 'manualAdjustedBalanceInput',
        editAdjustedBalanceModal: 'editAdjustedBalanceModal',
        exportReconciliationBtn: 'exportReconciliationBtn'
    };

    const state = {
        receivables: [],
        payables: []
    };

    const charts = {
        debtors: null,
        creditors: null
    };

    const formatCurrency = (value) => {
        const numeric = Number(value) || 0;
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 2
        }).format(numeric);
    };

    const formatDate = (value) => {
        if (!value) return '-';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return value;
        return date.toLocaleDateString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: '2-digit'
        });
    };

    const getElement = (idOrIds) => {
        if (!idOrIds) return null;
        if (Array.isArray(idOrIds)) {
            for (const id of idOrIds) {
                const element = document.getElementById(id);
                if (element) return element;
            }
            return null;
        }
        return document.getElementById(idOrIds);
    };

    window.markedReceivables = window.markedReceivables || new Map();
    window.markedPayables = window.markedPayables || new Map();
    window.currentBankBalance = 0;

    const parseAmount = (value) => {
        if (value == null) return 0;
        if (typeof value === 'string') {
            return Number(value.replace(/[^0-9.-]+/g, '')) || 0;
        }
        return Number(value) || 0;
    };

    const getFilters = () => {
        const query = (getElement(ids.search)?.value || '').trim().toLowerCase();
        const minAmount = Number(getElement(ids.minAmount)?.value || 0) || 0;
        const fromDate = getElement(ids.fromDate)?.value || '';
        const toDate = getElement(ids.toDate)?.value || '';
        return { query, minAmount, fromDate, toDate };
    };

    const buildUrl = (path, params) => {
        const url = new URL(window.cashflowUrl(path), window.location.origin);
        Object.entries(params).forEach(([key, value]) => {
            if (value) url.searchParams.set(key, value);
        });
        return url.toString();
    };

    const getItemId = (item, type, index) => {
        if (item.id != null) return String(item.id);
        if (item.invoiceId != null) return String(item.invoiceId);
        return `${type}-${index}`;
    };

    const isItemSelected = (item, type) => {
        const id = getItemId(item, type, 0);
        if (!id) return false;
        return type === 'receivables' ? window.markedReceivables.has(id) : window.markedPayables.has(id);
    };

    const createRow = (item, type, index) => {
        const displayName = item.name || item.customerName || item.vendorName || '-';
        const rawStatus = item.status || '';
        const status = rawStatus || (item.daysOverdue != null ? `Overdue ${item.daysOverdue} days` : (item.daysUntilDue != null ? `Due in ${item.daysUntilDue} days` : 'Pending'));
        const statusClass = rawStatus.toLowerCase() === 'pending' ? 'bg-secondary' : 'bg-success';
        const amount = formatCurrency(item.amount || 0);
        const due = formatDate(item.dueDate);
        const itemId = getItemId(item, type, index);
        const selected = isItemSelected(item, type);
        const selectedClass = selected ? ` ${type === 'receivables' ? 'received' : 'paid'}` : '';

        return `
            <tr class="invoice-row${selected ? ' selected' : ''}${selectedClass}" data-item-id="${itemId}" data-type="${type}" data-amount="${item.amount || 0}" data-name="${displayName}">
                <td class="table-align-checkbox">
                    <input type="checkbox" class="form-check-input row-checkbox" ${selected ? 'checked' : ''} data-item-id="${itemId}" data-type="${type}" data-amount="${item.amount || 0}" data-name="${displayName}">
                </td>
                <td class="table-align-name"><div class="fw-semibold">${displayName}</div></td>
                <td class="table-align-invoice">${item.invoiceNumber || '-'}</td>
                <td class="table-align-date">${due}</td>
                <td class="table-align-amount text-end">${amount}</td>
                <td class="table-align-status"><span class="badge ${statusClass}">${status}</span></td>
            </tr>
        `;
    };

    const updateSummaryBadge = (id, total, count, color) => {
        const badge = getElement(id);
        if (!badge) return;
        badge.className = `badge bg-light text-${color}`;
        badge.textContent = `${formatCurrency(total)} | ${count} items`;
    };

    const updateSelectionVisual = (row, type, selected) => {
        if (!row) return;
        row.classList.toggle(type === 'receivables' ? 'received' : 'paid', selected);
    };

    const calculateReconciliationTotals = () => {
        const totalReceivables = Array.from(window.markedReceivables.values())
            .reduce((sum, amount) => sum + parseAmount(amount), 0);
        const totalPayables = Array.from(window.markedPayables.values())
            .reduce((sum, amount) => sum + parseAmount(amount), 0);
        return {
            totalReceivables,
            totalPayables,
            adjustedBalance: window.currentBankBalance + totalReceivables - totalPayables
        };
    };

    const updateReconciliationSummary = () => {
        const { totalReceivables, totalPayables, adjustedBalance } = calculateReconciliationTotals();
        const balanceEls = document.querySelectorAll('#adjustedBankBalance, #reconciliationModalAdjustedBankBalance');
        const detailsEls = document.querySelectorAll('#adjustmentDetails, #reconciliationModalAdjustmentDetails');
        const recCountEls = document.querySelectorAll('#receivablesCount, #reconciliationModalReceivablesCount');
        const payCountEls = document.querySelectorAll('#payablesCount, #reconciliationModalPayablesCount');
        const recAmountEls = document.querySelectorAll('#receivablesAmount, #reconciliationModalReceivablesAmount');
        const payAmountEls = document.querySelectorAll('#payablesAmount, #reconciliationModalPayablesAmount');

        const details = [];
        if (totalReceivables > 0) {
            details.push(`<div><i class="fas fa-plus-circle text-success me-2"></i><strong>${formatCurrency(totalReceivables)}</strong> marked receivables</div>`);
        }
        if (totalPayables > 0) {
            details.push(`<div><i class="fas fa-minus-circle text-danger me-2"></i><strong>${formatCurrency(totalPayables)}</strong> marked payables</div>`);
        }
        if (details.length === 0) {
            details.push('<div class="text-muted">No items selected</div>');
        }

        balanceEls.forEach(el => {
            el.textContent = formatCurrency(adjustedBalance);
            el.style.color = adjustedBalance >= 0 ? '#10b981' : '#ef4444';
        });
        detailsEls.forEach(el => { el.innerHTML = details.join(''); });
        recCountEls.forEach(el => { el.textContent = window.markedReceivables.size; });
        payCountEls.forEach(el => { el.textContent = window.markedPayables.size; });
        recAmountEls.forEach(el => { el.textContent = formatCurrency(totalReceivables); });
        payAmountEls.forEach(el => { el.textContent = formatCurrency(totalPayables); });
    };

    window.calculateAdjustedBalance = updateReconciliationSummary;

    const initializeCurrentBankBalance = () => {
        const balanceEls = document.querySelectorAll('#currentBankBalance, #reconciliationModalCurrentBankBalance');
        const firstValue = balanceEls[0]?.textContent || '0';
        window.currentBankBalance = parseAmount(firstValue);
        balanceEls.forEach(el => { el.textContent = formatCurrency(window.currentBankBalance); });
        updateReconciliationSummary();
    };

    const populateTable = (tbodyId, items, type) => {
        const tbody = getElement(tbodyId);
        if (!tbody) return;
        
        // Clear only the tbody content, preserve thead
        while (tbody.firstChild) {
            tbody.removeChild(tbody.firstChild);
        }

        if (!items || items.length === 0) {
            const emptyRow = document.createElement('tr');
            emptyRow.innerHTML = `<td colspan="6" class="text-center text-muted py-4">No ${type} found for the selected filters.</td>`;
            tbody.appendChild(emptyRow);
            return;
        }

        items.forEach((item, index) => {
            const rowHtml = createRow(item, type, index + 1);
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = `<table><tbody>${rowHtml}</tbody></table>`;
            const row = tempDiv.querySelector('tr');
            tbody.appendChild(row);
        });

        const table = tbody.closest('table');
        if (table && !table.dataset.checkboxListener) {
            table.dataset.checkboxListener = 'true';
            table.addEventListener('change', (event) => {
                if (event.target.classList.contains('row-checkbox')) {
                    const checkbox = event.target;
                    
                    // Restricted for Viewers
                    if (window.userRole === 'ROLE_VIEWER') {
                        checkbox.checked = !checkbox.checked; // Undo the check
                        if (typeof showToast === 'function') showToast('Viewing only: You do not have permission to change reconciliation data.', 'warning');
                        return;
                    }

                    handleCheckboxChange(checkbox);
                }
            }, true);
        }
    };

    let pendingSelection = null;
    const confirmModalElement = getElement('reconciliationConfirmModal');
    const confirmModal = window.bootstrap && confirmModalElement ? new window.bootstrap.Modal(confirmModalElement, { backdrop: 'static', keyboard: false }) : null;
    const confirmMessage = getElement('reconciliationConfirmMessage');
    const confirmAmount = getElement('reconciliationConfirmAmount');
    const confirmYesBtn = getElement('reconciliationConfirmYesBtn');

    const finalizeSelection = (item, selected) => {
        if (!item || !item.invoiceId) return;
        const id = item.invoiceId;
        if (selected) {
            if (item.type === 'receivables') {
                window.markedReceivables.set(id, item.amount);
            } else {
                window.markedPayables.set(id, item.amount);
            }
            updateSelectionVisual(item.row, item.type, true);
        } else {
            if (item.type === 'receivables') {
                window.markedReceivables.delete(id);
            } else {
                window.markedPayables.delete(id);
            }
            updateSelectionVisual(item.row, item.type, false);
        }
        updateReconciliationSummary();

        // PERSIST TO DATABASE
        try {
            // Find invoice number from row if possible
            const invoiceNumber = item.row.cells[2].textContent.trim();
            fetch('/api/cashflow/reconcile', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: item.invoiceId,
                    invoiceNumber: invoiceNumber === '-' ? null : invoiceNumber,
                    customerName: item.name,
                    reconciled: selected
                })
            });
        } catch (e) {
            console.error('Failed to persist reconciliation state', e);
        }
    };

    const openSelectionConfirm = (checkbox, item) => {
        const isSelecting = checkbox.checked;
        if (!item || !item.invoiceId) {
            checkbox.checked = !isSelecting; // Revert
            return;
        }

        const label = item.type === 'receivables' ? 'Receivable' : 'Payable';
        const action = item.type === 'receivables' ? 'received' : 'paid';
        
        const actionText = isSelecting ? `Mark` : `Unmark`;
        const actionContext = isSelecting ? ` as ${action}` : ` (Pending)`;
        
        const message = `${actionText} this ${label.toLowerCase()}${actionContext}?\n\n${item.name || '-'}\n${formatCurrency(item.amount)}`;

        if (!confirmModal || !confirmMessage || !confirmAmount || !confirmYesBtn) {
            const accepted = window.confirm(message);
            if (accepted) {
                finalizeSelection(item, isSelecting);
            } else {
                checkbox.checked = !isSelecting; // Revert
            }
            return;
        }

        pendingSelection = { checkbox, item, isSelecting };
        confirmMessage.textContent = `${actionText} this ${label.toLowerCase()}${actionContext}?`;
        confirmAmount.textContent = `${item.name || '-'} · ${formatCurrency(item.amount)}`;
        confirmYesBtn.textContent = isSelecting ? "Confirm" : "Unmark";
        confirmYesBtn.className = isSelecting ? "btn btn-primary btn-sm" : "btn btn-warning btn-sm";
        confirmModal.show();
    };

    const handleCheckboxChange = (checkbox) => {
        const row = checkbox.closest('tr');
        if (!row) return;

        const item = {
            invoiceId: row.dataset.itemId,
            type: row.dataset.type,
            amount: parseAmount(row.dataset.amount),
            name: row.dataset.name || '-',
            row
        };

        if (!item.invoiceId || !item.type) return;
        openSelectionConfirm(checkbox, item);
    };

    if (confirmModalElement && confirmModal) {
        confirmYesBtn?.addEventListener('click', () => {
            if (!pendingSelection) return;
            finalizeSelection(pendingSelection.item, pendingSelection.isSelecting);
            confirmModal.hide();
            pendingSelection = null;
        });

        confirmModalElement.addEventListener('hidden.bs.modal', () => {
            if (pendingSelection) {
                // Modal was closed without confirming
                pendingSelection.checkbox.checked = !pendingSelection.isSelecting; // Revert visually
                pendingSelection = null;
            }
        });
    }

    const applyFiltersToData = () => {
        const { query, minAmount } = getFilters();
        const normalizedQuery = query.toLowerCase();

        const filteredReceivables = state.receivables.filter(item => {
            const name = (item.name || item.customerName || item.vendorName || '').toLowerCase();
            const amount = Number(item.amount) || 0;
            return (!normalizedQuery || name.includes(normalizedQuery)) && (!minAmount || amount >= minAmount);
        });

        const filteredPayables = state.payables.filter(item => {
            const name = (item.name || item.customerName || item.vendorName || '').toLowerCase();
            const amount = Number(item.amount) || 0;
            return (!normalizedQuery || name.includes(normalizedQuery)) && (!minAmount || amount >= minAmount);
        });

        populateTable(ids.receivablesTable, filteredReceivables, 'receivables');
        populateTable(ids.payablesTable, filteredPayables, 'payables');
        updateSummaryBadge(ids.receivablesSummary, filteredReceivables.reduce((sum, item) => sum + (Number(item.amount) || 0), 0), filteredReceivables.length, 'success');
        updateSummaryBadge(ids.payablesSummary, filteredPayables.reduce((sum, item) => sum + (Number(item.amount) || 0), 0), filteredPayables.length, 'danger');
    };

    const loadReconciliationData = async () => {
        const filters = getFilters();
        const url = buildUrl('/api/cashflow/reconciliation', {
            fromDate: filters.fromDate,
            toDate: filters.toDate
        });

        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to load reconciliation data');
            const payload = await response.json();
            state.receivables = Array.isArray(payload.receivables) ? payload.receivables : [];
            state.payables = Array.isArray(payload.payables) ? payload.payables : [];

            // SYNC GLOBAL MARKED SETS FROM DATABASE STATUS
            state.receivables.forEach(inv => {
                const id = getItemId(inv, 'receivables', 0);
                if (inv.reconciled) window.markedReceivables.set(id, inv.amount);
                else window.markedReceivables.delete(id);
            });
            state.payables.forEach(inv => {
                const id = getItemId(inv, 'payables', 0);
                if (inv.reconciled) window.markedPayables.set(id, inv.amount);
                else window.markedPayables.delete(id);
            });

            applyFiltersToData();
            updateReconciliationSummary();
        } catch (error) {
            state.receivables = [];
            state.payables = [];
            applyFiltersToData();
        }
    };

    const setText = (idOrIds, text) => {
        const element = getElement(idOrIds);
        if (element) element.textContent = text;
    };

    const renderTopList = (containerId, items) => {
        const container = getElement(containerId);
        if (!container) return;

        if (!items || items.length === 0) {
            container.innerHTML = `
                <div class="entity-grid no-data">
                    <div></div>
                    <div class="text-center">No records for the selected period.</div>
                    <div></div>
                    <div></div>
                </div>
            `;
            return;
        }

        container.innerHTML = items.map((item, index) => `
            <div class="entity-grid entity-row">
                <div class="text-center">${index + 1}</div>
                <div class="name"><strong>${item.name || '-'}</strong></div>
                <div class="text-end">${formatCurrency(item.amount || 0)}</div>
                <div class="text-end">${item.daysOverdue != null ? item.daysOverdue : item.daysUntilDue != null ? item.daysUntilDue : '-'}</div>
            </div>
        `).join('');
    };

    const destroyChart = (key) => {
        if (charts[key]) {
            charts[key].destroy();
            charts[key] = null;
        }
    };

    const hexToRgba = (hex, alpha = 1) => {
        const normalized = hex.replace('#', '');
        const bigint = parseInt(normalized, 16);
        const r = (bigint >> 16) & 255;
        const g = (bigint >> 8) & 255;
        const b = bigint & 255;
        return `rgba(${r}, ${g}, ${b}, ${alpha})`;
    };

    const renderChart = (canvasId, labels, data, color, key) => {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        destroyChart(key);

        const desiredHeight = Math.min(Math.max(labels.length * 28 + 120, 220), 260);
        if (canvas.parentElement) {
            canvas.parentElement.style.height = `${desiredHeight}px`;
            canvas.parentElement.style.overflow = 'visible';
            canvas.style.width = '100%';
            canvas.style.height = '100%';
            canvas.style.display = 'block';
        }

        const parent = canvas.parentElement;
        const width = parent ? parent.clientWidth : canvas.clientWidth;
        const height = parent ? parent.clientHeight : canvas.clientHeight;
        const dpr = window.devicePixelRatio || 1;

        canvas.width = Math.round(width * dpr);
        canvas.height = Math.round(height * dpr);
        canvas.style.width = `${width}px`;
        canvas.style.height = `${height}px`;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        ctx.clearRect(0, 0, width, height);

        const backgroundColor = hexToRgba(color, 0.92);
        const darkTheme = document.documentElement.getAttribute('data-theme') === 'dark';
        const chartTextColor = darkTheme ? '#b8c8dc' : '#475569';
        const chartLabelColor = darkTheme ? '#d3deec' : '#334155';
        const chartGridColor = darkTheme ? 'rgba(148,163,184,0.25)' : 'rgba(148,163,184,0.16)';

        charts[key] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    label: 'Amount',
                    data,
                    backgroundColor,
                    borderColor: hexToRgba(color, 1),
                    borderWidth: 0,
                    borderRadius: 14,
                    barThickness: 16,
                    maxBarThickness: 24,
                    categoryPercentage: 0.72,
                    barPercentage: 0.72,
                    borderSkipped: false,
                    hoverBackgroundColor: hexToRgba(color, 1)
                }]
            },
            options: {
                devicePixelRatio: 1,
                indexAxis: 'y',
                responsive: false,
                maintainAspectRatio: false,
                animation: { duration: 300, easing: 'easeOutQuart' },
                layout: {
                    padding: {
                        left: 10,
                        right: 10,
                        top: 10,
                        bottom: 10
                    }
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#ffffff',
                        bodyColor: '#f8fafc',
                        borderColor: 'rgba(148, 163, 184, 0.2)',
                        borderWidth: 1,
                        padding: 12,
                        displayColors: false,
                        callbacks: {
                            label: (context) => formatCurrency(context.parsed.x)
                        }
                    },
                    title: { display: false }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: {
                            color: chartTextColor,
                            callback: (value) => formatCurrency(value),
                            maxTicksLimit: 4,
                            padding: 10
                        },
                        grid: {
                            color: chartGridColor,
                            borderDash: [3, 3]
                        }
                    },
                    y: {
                        ticks: {
                            color: chartLabelColor,
                            font: { family: 'Segoe UI, sans-serif', size: 12, weight: '500' },
                            padding: 8,
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        },
                        grid: { display: false }
                    }
                }
            }
        });

        charts[key].resize();
        charts[key].update();
    };

    const loadTopDebtorsCreditors = async () => {
        const filters = getFilters();
        const url = buildUrl('/api/cashflow/debtors-creditors', {
            fromDate: filters.fromDate,
            toDate: filters.toDate
        });

        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error('Failed to load debtors/creditors');
            const payload = await response.json();

            const debtors = Array.isArray(payload.highestDebtors) ? payload.highestDebtors : [];
            const creditors = Array.isArray(payload.highestCreditors) ? payload.highestCreditors : [];
            const topDebtor = debtors[0] || null;
            const topCreditor = creditors[0] || null;

            setText(ids.highestDebtorName, topDebtor?.name || '-');
            setText(ids.highestDebtorAmount, topDebtor ? formatCurrency(topDebtor.amount) : '₹0');
            setText(ids.highestDebtorDays, topDebtor?.daysOverdue != null ? `${topDebtor.daysOverdue} days` : '-');
            setText(ids.highestCreditorName, topCreditor?.name || '-');
            setText(ids.highestCreditorAmount, topCreditor ? formatCurrency(topCreditor.amount) : '₹0');
            setText(ids.highestCreditorDays, topCreditor?.daysUntilDue != null ? `${topCreditor.daysUntilDue} days` : '-');

            renderChart(ids.debtorChart, debtors.map(item => item.name || 'Unknown'), debtors.map(item => Number(item.amount) || 0), '#dc2626', 'debtors');
            renderChart(ids.creditorChart, creditors.map(item => item.name || 'Unknown'), creditors.map(item => Number(item.amount) || 0), '#059669', 'creditors');
            renderTopList(ids.debtorList, debtors);
            renderTopList(ids.creditorList, creditors);
        } catch (error) {
            setText(ids.highestDebtorName, '-');
            setText(ids.highestDebtorAmount, '₹0');
            setText(ids.highestDebtorDays, '-');
            setText(ids.highestCreditorName, '-');
            setText(ids.highestCreditorAmount, '₹0');
            setText(ids.highestCreditorDays, '-');
            destroyChart('debtors');
            destroyChart('creditors');
            renderTopList(ids.debtorList, []);
            renderTopList(ids.creditorList, []);
        }
    };

    const reloadAll = async () => {
        await Promise.all([loadReconciliationData(), loadTopDebtorsCreditors()]);
    };
    window.reloadReconciliationData = reloadAll;

    const attachEventListeners = () => {
        const fromDateInput = getElement(ids.fromDate);
        const toDateInput = getElement(ids.toDate);
        const searchInput = getElement(ids.search);
        const minAmountInput = getElement(ids.minAmount);
        const clearButton = getElement(ids.clearFilters);

        if (fromDateInput) fromDateInput.addEventListener('change', reloadAll);
        if (toDateInput) toDateInput.addEventListener('change', reloadAll);
        if (searchInput) searchInput.addEventListener('input', applyFiltersToData);
        if (minAmountInput) minAmountInput.addEventListener('input', applyFiltersToData);
        if (clearButton) {
            clearButton.addEventListener('click', () => {
                if (searchInput) searchInput.value = '';
                if (fromDateInput) fromDateInput.value = '';
                if (toDateInput) toDateInput.value = '';
                if (minAmountInput) minAmountInput.value = '';
                reloadAll();
            });
        }

        window.addEventListener('resize', () => {
            charts.debtors?.resize();
            charts.creditors?.resize();
        });

        const reconciliationModal = document.getElementById(ids.modal);
        if (reconciliationModal) {
            reconciliationModal.addEventListener('shown.bs.modal', () => {
                console.log('🔄 Reconciliation Modal Opened: Fetching fresh shared data...');
                reloadAll();
                charts.debtors?.resize();
                charts.creditors?.resize();
            });
        }

        // --- Manual Bank Balance Adjustment Logic ---
        const editBtn = document.getElementById(ids.editAdjustedBalance);
        const saveBtn = document.getElementById(ids.saveAdjustedBalance);
        const manualInput = document.getElementById(ids.manualAdjustedBalanceInput);
        const balanceModalEl = document.getElementById(ids.editAdjustedBalanceModal);

        if (editBtn && balanceModalEl) {
            editBtn.addEventListener('click', () => {
                const bsModal = new bootstrap.Modal(balanceModalEl);
                if (manualInput) {
                    // Pre-fill with CURRENT bank balance
                    manualInput.value = window.currentBankBalance || 0;
                }
                bsModal.show();
            });
        }

        if (saveBtn) {
            saveBtn.addEventListener('click', async () => {
                const newBalance = parseFloat(manualInput?.value || 0);
                if (Number.isNaN(newBalance)) {
                    if (typeof showToast === 'function') showToast('Please enter a valid amount', 'warning');
                    return;
                }

                try {
                    const params = new URLSearchParams();
                    params.append('amount', newBalance);
                    
                    const response = await fetch(`/api/cashflow/bank-balance?${params.toString()}`, {
                        method: 'POST',
                        credentials: 'same-origin'
                    });

                    if (response.ok) {
                        const payload = await response.json();
                        const updatedAmount = payload.amount || 0;

                        // 1. Update global state
                        window.currentBankBalance = updatedAmount;

                        // 2. Update all bank balance displays in UI (dashboard + modal)
                        const balanceEls = document.querySelectorAll('#currentBankBalance, #reconciliationModalCurrentBankBalance, #bankBalanceDisplay');
                        balanceEls.forEach(el => { el.textContent = formatCurrency(updatedAmount); });

                        // 3. Update main dashboard input if it exists
                        const dashInput = document.getElementById('bankBalanceInput');
                        if (dashInput) dashInput.value = updatedAmount;

                        // 4. Recalculate reconciliation total
                        updateReconciliationSummary();

                        // 5. Success feedback
                        if (typeof showToast === 'function') showToast('Bank balance updated successfully!', 'success');

                        // 6. Close modal
                        const bsModal = bootstrap.Modal.getInstance(balanceModalEl);
                        if (bsModal) bsModal.hide();
                    } else {
                        throw new Error('Failed to save balance');
                    }
                } catch (error) {
                    console.error('Error saving bank balance:', error);
                    if (typeof showToast === 'function') showToast('Failed to update balance', 'danger');
                }
            });
        }

        // --- Export Reconciliation Logic ---
        const exportBtn = document.getElementById(ids.exportReconciliationBtn);
        if (exportBtn) {
            exportBtn.addEventListener('click', async () => {
                const { totalReceivables, totalPayables, adjustedBalance } = calculateReconciliationTotals();
                
                // Helper to get selected item details
                const getSelectedDetails = (markedMap, items) => {
                    const today = new Date();
                    return Array.from(markedMap.keys()).map(id => {
                        const item = items.find(i => getItemId(i, '', 0) === id);
                        if (!item) return null;

                        // Compute days overdue if not present on the item
                        let days = 0;
                        if (item.daysOverdue != null) {
                            days = Number(item.daysOverdue) || 0;
                        } else if (item.dueDate) {
                            const due = new Date(item.dueDate);
                            if (!Number.isNaN(due.getTime())) {
                                const diffDays = Math.floor((today - due) / (1000 * 60 * 60 * 24));
                                days = diffDays > 0 ? diffDays : 0;
                            }
                        }

                        return {
                            invoice: item.invoiceNumber || '-',
                            name: item.name || item.customerName || item.vendorName || '-',
                            amount: formatCurrency(item.amount || 0),
                            dueDate: formatDate(item.dueDate),
                            daysOverdue: String(days)
                        };
                    }).filter(Boolean);
                };

                const selectedReceivables = getSelectedDetails(window.markedReceivables, state.receivables);
                const selectedPayables = getSelectedDetails(window.markedPayables, state.payables);

                const requestData = {
                    selectedReceivables,
                    selectedPayables,
                    initialBankBalance: window.currentBankBalance,
                    adjustedBankBalance: formatCurrency(adjustedBalance),
                    exportDate: new Date().toLocaleString('en-IN')
                };

                try {
                    exportBtn.disabled = true;
                    exportBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>Exporting...';

                    const response = await fetch('/api/cashflow/reconciliation/export', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(requestData),
                        credentials: 'same-origin'
                    });

                    if (!response.ok) throw new Error('Export failed');

                    const blob = await response.blob();
                    const url = window.URL.createObjectURL(blob);
                    const a = document.createElement('a');
                    a.href = url;
                    a.download = `Reconciliation_Export_${new Date().toISOString().split('T')[0]}.xlsx`;
                    document.body.appendChild(a);
                    a.click();
                    window.URL.revokeObjectURL(url);
                    document.body.removeChild(a);

                    if (typeof showToast === 'function') showToast('Reconciliation exported successfully!', 'success');
                } catch (error) {
                    console.error('Export error:', error);
                    if (typeof showToast === 'function') showToast('Failed to export reconciliation', 'danger');
                } finally {
                    exportBtn.disabled = false;
                    exportBtn.innerHTML = '<i class="fas fa-file-excel me-1"></i>Export';
                }
            });
        }

        initializeCurrentBankBalance();
        reloadAll();
    };

    document.addEventListener('DOMContentLoaded', attachEventListeners);
})();
