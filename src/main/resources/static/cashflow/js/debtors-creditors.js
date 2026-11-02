(() => {
    const charts = {
        debtors: null,
        creditors: null
    };

    const destroyChart = (key) => {
        if (charts[key]) {
            charts[key].destroy();
            charts[key] = null;
        }
    };

    const renderTable = (tbodyId, entities, isDebtor = true) => {
        const tbody = document.getElementById(tbodyId);
        if (!tbody) {
            return;
        }
        tbody.innerHTML = '';

        if (!entities || entities.length === 0) {
            const row = document.createElement('tr');
            row.innerHTML = `<td colspan="4" class="text-center text-muted py-3">No records for the selected period.</td>`;
            tbody.appendChild(row);
            return;
        }

        entities.forEach((entity, index) => {
            const row = document.createElement('tr');
            const rank = index + 1;
            const amountClass = isDebtor ? 'amount-negative' : 'amount-positive';
            const daysLabel = isDebtor ? 'Days Overdue' : 'Days Until Due';
            const daysValue = isDebtor ? entity.daysOverdue : entity.daysUntilDue;

            row.innerHTML = `
                <td><span class="rank-badge">${rank}</span></td>
                <td>${entity.name || 'Unknown'}</td>
                <td class="text-end ${amountClass}">${dashboardFilters.formatCurrency(entity.amount)}</td>
                <td class="text-end">${daysValue || 0}</td>
            `;
            tbody.appendChild(row);
        });
    };

    const renderBarChart = (canvasId, labels, data, color) => {
        const ctx = document.getElementById(canvasId);
        if (!ctx) {
            return;
        }
        const key = canvasId.includes('Debtors') ? 'debtors' : 'creditors';
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
                                const label = labels[index];
                                return label.length > 20 ? label.substring(0, 17) + '...' : label;
                            }
                        },
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    };

    const loadDebtorsCreditorsData = async () => {
        const fromDate = document.getElementById('debtors-creditors-FromDate')?.value;
        const toDate = document.getElementById('debtors-creditors-ToDate')?.value;

        const params = new URLSearchParams();
        if (fromDate) params.append('fromDate', fromDate);
        if (toDate) params.append('toDate', toDate);

        try {
            const response = await fetch(`/api/cashflow/debtors-creditors?${params.toString()}`);
            if (!response.ok) {
                throw new Error('Failed to load debtors and creditors data');
            }

            const data = await response.json();

            // Update summary values
            document.getElementById('totalReceivablesValue').textContent = dashboardFilters.formatCurrency(data.totalReceivables || 0);
            document.getElementById('totalPayablesValue').textContent = dashboardFilters.formatCurrency(data.totalPayables || 0);
            document.getElementById('netOutstandingValue').textContent = dashboardFilters.formatCurrency((data.totalReceivables || 0) - (data.totalPayables || 0));

            // Render debtors chart and table
            const debtorLabels = data.highestDebtors?.map(d => d.name || 'Unknown') || [];
            const debtorData = data.highestDebtors?.map(d => d.amount || 0) || [];
            renderBarChart('highestDebtorsChart', debtorLabels, debtorData, '#dc2626');
            renderTable('highestDebtorsTable', data.highestDebtors, true);

            // Render creditors chart and table
            const creditorLabels = data.highestCreditors?.map(c => c.name || 'Unknown') || [];
            const creditorData = data.highestCreditors?.map(c => c.amount || 0) || [];
            renderBarChart('highestCreditorsChart', creditorLabels, creditorData, '#059669');
            renderTable('highestCreditorsTable', data.highestCreditors, false);

        } catch (error) {
            console.error('Error loading debtors and creditors data:', error);
            // Show error in tables
            renderTable('highestDebtorsTable', [], true);
            renderTable('highestCreditorsTable', [], false);
        }
    };

    const downloadDebtorsCreditors = (type) => {
        const fromDate = document.getElementById('debtors-creditors-FromDate')?.value;
        const toDate = document.getElementById('debtors-creditors-ToDate')?.value;

        let url = `/api/cashflow/debtors-creditors/download?type=${type}`;
        if (fromDate) url += `&fromDate=${fromDate}`;
        if (toDate) url += `&toDate=${toDate}`;

        window.open(url, '_blank');
    };

    // Fallback: if card is clicked, trigger load (useful if Bootstrap triggers are not available yet)
    const debCredTile = document.querySelector('[data-bs-target="#debtorsCreditorsModal"]');
    if (debCredTile) {
        debCredTile.addEventListener('click', () => {
            loadDebtorsCreditorsData();
        });
    }

    // Initialize when modal is shown
    document.getElementById('debtorsCreditorsModal')?.addEventListener('show.bs.modal', () => {
        loadDebtorsCreditorsData();
    });

    // Set up filter change listeners
    document.getElementById('debtors-creditors-FromDate')?.addEventListener('change', loadDebtorsCreditorsData);
    document.getElementById('debtors-creditors-ToDate')?.addEventListener('change', loadDebtorsCreditorsData);

    // Expose download function globally
    window.downloadDebtorsCreditors = downloadDebtorsCreditors;

})();