(() => {
    const charts = {
        customers: null,
        vendors: null,
        trend: null
    };

    const destroyChart = (key) => {
        if (charts[key]) {
            charts[key].destroy();
            charts[key] = null;
        }
    };

    const renderTable = (tbodyId, entities) => {
        const tbody = document.getElementById(tbodyId);
        if (!tbody) {
            return;
        }
        tbody.innerHTML = '';

        if (!entities || entities.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="3" class="text-center text-muted py-3">No records for the selected period.</td>`;
            tbody.appendChild(row);
            return;
        }

        entities.forEach((entity) => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${entity.name}</td>
                <td class="text-end">${entity.invoiceCount}</td>
                <td class="text-end">${dashboardFilters.formatCurrency(entity.amount)}</td>
            `;
            tbody.appendChild(row);
        });
    };

    const renderBarChart = (canvasId, labels, data, color) => {
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            return;
        }
        const key = canvasId.includes('Customers') ? 'customers' : 'vendors';
        destroyChart(key);

        charts[key] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels,
                datasets: [{
                    data,
                    backgroundColor: color,
                    borderRadius: 8,
                    maxBarThickness: 32,
                    minBarLength: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                layout: {
                    padding: {
                        left: 16,
                        right: 8,
                        top: 8,
                        bottom: 8
                    }
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            title: (context) => labels[context[0].dataIndex],
                            label: (context) => dashboardFilters.formatCurrency(context.raw)
                        }
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        ticks: {
                            font: { size: 10 },
                            callback: (value) => dashboardFilters.formatCurrency(value),
                            maxTicksLimit: 8
                        },
                        grid: {
                            color: 'rgba(0,0,0,0.05)',
                            drawBorder: false
                        }
                    },
                    y: {
                        ticks: {
                            autoSkip: false,
                            font: { size: 11, weight: '500' },
                            maxRotation: 0,
                            minRotation: 0,
                            padding: 8,
                            callback: (value, index) => {
                                const label = labels[index] || '';
                                const maxLen = 18;
                                return label.length > maxLen ? label.substring(0, maxLen - 1) + '…' : label;
                            }
                        },
                        grid: {
                            display: false,
                            drawBorder: false
                        }
                    }
                }
            }
        });
    };

    const renderTrendChart = (data) => {
        const ctx = document.getElementById('dailyTrendChart');
        if (!ctx) {
            return;
        }
        destroyChart('trend');

        const labels = data.map(point => new Date(point.date).toLocaleDateString('en-IN', {
            month: 'short',
            day: 'numeric'
        }));
        const inflows = data.map(point => point.inflow);
        const outflows = data.map(point => point.outflow);

        charts.trend = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Inflow',
                        data: inflows,
                        borderColor: '#1e3c72',
                        backgroundColor: 'rgba(30, 60, 114, 0.1)',
                        tension: 0.3,
                        fill: true,
                        pointRadius: 3
                    },
                    {
                        label: 'Outflow',
                        data: outflows,
                        borderColor: '#dc2626',
                        backgroundColor: 'rgba(220, 38, 38, 0.1)',
                        tension: 0.3,
                        fill: true,
                        pointRadius: 3
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top' },
                    tooltip: {
                        callbacks: {
                            label: (context) => `${context.dataset.label}: ${dashboardFilters.formatCurrency(context.raw)}`
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: (value) => dashboardFilters.formatCurrency(value)
                        }
                    }
                }
            }
        });
    };

    const updateTotals = (data) => {
        document.getElementById('totalInflowValue').textContent = dashboardFilters.formatCurrency(data.totalInflow);
        document.getElementById('totalOutflowValue').textContent = dashboardFilters.formatCurrency(data.totalOutflow);
        document.getElementById('netCashFlowValue').textContent = dashboardFilters.formatCurrency(data.totalInflow - data.totalOutflow);
    };

    const handleResponse = (payload) => {
        updateTotals(payload);

        renderTable('customersTableBody', payload.topCustomers);
        renderTable('vendorsTableBody', payload.topVendors);

        renderBarChart(
            'topCustomersChart',
            payload.topCustomers.map(item => item.name),
            payload.topCustomers.map(item => item.amount),
            '#1e3c72'
        );

        renderBarChart(
            'topVendorsChart',
            payload.topVendors.map(item => item.name),
            payload.topVendors.map(item => item.amount),
            '#dc2626'
        );

        renderTrendChart(payload.trend || []);
    };

    const fetchData = async ({ fromDate, toDate }) => {
        const params = new URLSearchParams();
        if (fromDate) {
            params.append('fromDate', fromDate);
        }
        if (toDate) {
            params.append('toDate', toDate);
        }

        const response = await fetch(`/api/cashflow/top-performers?${params.toString()}`);
        if (!response.ok) {
            throw new Error('Failed to load top performers');
        }
        return response.json();
    };

    const downloadTopPerformers = (type) => {
        const params = new URLSearchParams({ type });
        const fromDate = document.getElementById('top-performers-FromDate')?.value;
        const toDate = document.getElementById('top-performers-ToDate')?.value;
        if (fromDate) params.set('fromDate', fromDate);
        if (toDate) params.set('toDate', toDate);
        window.location.assign(window.cashflowUrl(`/api/cashflow/top-performers/export?${params.toString()}`));
    };

    window.downloadTopPerformers = downloadTopPerformers;

    document.addEventListener('DOMContentLoaded', () => {
        const loadingOverlay = document.createElement('div');
        loadingOverlay.className = 'position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center bg-white bg-opacity-75';
        loadingOverlay.style.zIndex = 1080;
        loadingOverlay.innerHTML = `
            <div class="text-center">
                <div class="spinner-border text-primary mb-3" role="status"></div>
                <div class="text-muted">Loading insights...</div>
            </div>
        `;
        document.body.appendChild(loadingOverlay);

        const hideLoader = () => loadingOverlay.classList.add('d-none');
        const showLoader = () => loadingOverlay.classList.remove('d-none');

        dashboardFilters.initTimeFilters({
            onChange: (filters) => {
                showLoader();
                fetchData(filters)
                    .then((payload) => {
                        hideLoader();
                        handleResponse(payload);
                    })
                    .catch(() => {
                        hideLoader();
                        updateTotals({ totalInflow: 0, totalOutflow: 0 });
                        renderTable('customersTableBody', []);
                        renderTable('vendorsTableBody', []);
                        destroyChart('customers');
                        destroyChart('vendors');
                        destroyChart('trend');
                        const tbody = document.getElementById('customersTableBody');
                        if (tbody) {
                            tbody.innerHTML = `<tr><td colspan="3" class="text-center text-muted py-4">Upload data on the Overview tab first.</td></tr>`;
                        }
                    });
            }
        });
    });
})();


