let charts = {};

// Format currency
function formatCurrency(value) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    }).format(value);
}

// Handle file upload
function handleFileUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    // Show loading state
    showLoading();

    fetch('/api/cashflow/overview', {
        method: 'POST',
        body: formData
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
                showError('Error: ' + data.error);
            } else {
                displayAnalysis(data);
                switchTab('overview');
                // Refresh all tabs to show new data
                setTimeout(() => {
                    if (typeof refreshAllTabs === 'function') {
                        refreshAllTabs();
                    }
                }, 500);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showError('Failed to analyze file: ' + error.message);
        });
}

// Display analysis results
function displayAnalysis(data) {
    console.log('displayAnalysis called with data:', data);
    console.log('totalInflow:', data.totalInflow, 'totalOutflow:', data.totalOutflow, 'netCashflow:', data.netCashflow);
    
    // Update KPI cards
    document.getElementById('totalInflow').textContent = formatCurrency(data.totalInflow);
    document.getElementById('totalOutflow').textContent = formatCurrency(data.totalOutflow);
    document.getElementById('netCashFlow').textContent = formatCurrency(data.netCashflow);
    document.getElementById('overdueReceivables').textContent = formatCurrency(data.overdueReceivablesAmount);
    document.getElementById('overduePayables').textContent = formatCurrency(data.overduePayablesAmount);

    const collectionRate = data.totalInflow > 0 ? 
        (1 - data.overdueReceivablesAmount / data.totalInflow) * 100 : 100;
    document.getElementById('collectionRate').textContent = collectionRate.toFixed(1) + '%';

    console.log('✓ Updated KPI cards');

    // Create charts
    createPieChart(data);
    createLineChart(data);
    createBarChart(data);
    createCustomersChart(data);
    createSuppliersChart(data);
    createAgingChart(data);
    updateAgingTable(data);
    
    console.log('✓ Created all charts');
}

// Create pie chart
function createPieChart(data) {
    const ctx = document.getElementById('pieChart').getContext('2d');
    
    if (charts.pie) {
        charts.pie.destroy();
    }

    charts.pie = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Inflow', 'Outflow'],
            datasets: [{
                data: [data.totalInflow, data.totalOutflow],
                backgroundColor: ['#FF6B6B', '#FFA500'],
                borderColor: ['#E55039', '#E88B00'],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        padding: 15,
                        font: {
                            size: 13,
                            weight: '600'
                        }
                    }
                },
                title: {
                    display: true,
                    text: 'Cash Flow Distribution',
                    font: { size: 16, weight: 'bold' }
                }
            }
        }
    });
}

// Create line chart
function createLineChart(data) {
    const ctx = document.getElementById('lineChart').getContext('2d');
    
    if (charts.line) {
        charts.line.destroy();
    }

    const months = Object.keys(data.monthlyInflow).sort();
    const inflowData = months.map(m => data.monthlyInflow[m] || 0);
    const outflowData = months.map(m => data.monthlyOutflow[m] || 0);

    charts.line = new Chart(ctx, {
        type: 'line',
        data: {
            labels: months,
            datasets: [
                {
                    label: 'Inflow',
                    data: inflowData,
                    borderColor: '#27AE60',
                    backgroundColor: 'rgba(39, 174, 96, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 4,
                    pointHoverRadius: 6
                },
                {
                    label: 'Outflow',
                    data: outflowData,
                    borderColor: '#E74C3C',
                    backgroundColor: 'rgba(231, 76, 60, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    labels: {
                        padding: 15,
                        font: { size: 12, weight: '600' }
                    }
                },
                title: {
                    display: true,
                    text: 'Monthly Inflow vs Outflow',
                    font: { size: 16, weight: 'bold' }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '₹' + (value / 100000).toFixed(1) + 'L';
                        }
                    }
                }
            }
        }
    });
}

// Create bar chart
function createBarChart(data) {
    const ctx = document.getElementById('barChart').getContext('2d');
    
    if (charts.bar) {
        charts.bar.destroy();
    }

    const months = Object.keys(data.monthlyInflow).sort();
    const netData = months.map(m => {
        const inflow = data.monthlyInflow[m] || 0;
        const outflow = data.monthlyOutflow[m] || 0;
        return inflow - outflow;
    });

    charts.bar = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: months,
            datasets: [{
                label: 'Net Cash Flow',
                data: netData,
                backgroundColor: netData.map(v => v >= 0 ? '#27AE60' : '#E74C3C'),
                borderColor: netData.map(v => v >= 0 ? '#229954' : '#C0392B'),
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    labels: {
                        padding: 15,
                        font: { size: 12, weight: '600' }
                    }
                },
                title: {
                    display: true,
                    text: 'Net Monthly Cash Flow',
                    font: { size: 16, weight: 'bold' }
                }
            },
            scales: {
                y: {
                    ticks: {
                        callback: function(value) {
                            return '₹' + (value / 100000).toFixed(1) + 'L';
                        }
                    }
                }
            }
        }
    });
}

// Create customers chart
function createCustomersChart(data) {
    const ctx = document.getElementById('customersChart').getContext('2d');
    
    if (charts.customers) {
        charts.customers.destroy();
    }

    const customers = Object.keys(data.topCustomers).slice(0, 10);
    const values = customers.map(c => data.topCustomers[c]);

    charts.customers = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: customers.map(c => c.substring(0, 30)),
            datasets: [{
                label: 'Revenue (₹)',
                data: values,
                backgroundColor: '#3498DB',
                borderColor: '#2980B9',
                borderWidth: 1
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: true },
                title: {
                    display: true,
                    text: 'Top 10 Customers',
                    font: { size: 16, weight: 'bold' }
                }
            }
        }
    });
}

// Create suppliers chart
function createSuppliersChart(data) {
    const ctx = document.getElementById('suppliersChart').getContext('2d');
    
    if (charts.suppliers) {
        charts.suppliers.destroy();
    }

    const suppliers = Object.keys(data.topSuppliers).slice(0, 10);
    const values = suppliers.map(s => data.topSuppliers[s]);

    charts.suppliers = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: suppliers.map(s => s.substring(0, 30)),
            datasets: [{
                label: 'Expense (₹)',
                data: values,
                backgroundColor: '#E74C3C',
                borderColor: '#C0392B',
                borderWidth: 1
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: true },
                title: {
                    display: true,
                    text: 'Top 10 Suppliers',
                    font: { size: 16, weight: 'bold' }
                }
            }
        }
    });
}

// Create aging chart
function createAgingChart(data) {
    const ctx = document.getElementById('agingChart').getContext('2d');
    
    if (charts.aging) {
        charts.aging.destroy();
    }

    const buckets = Object.keys(data.agingBuckets);
    const values = buckets.map(b => data.agingBuckets[b]);

    charts.aging = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: buckets,
            datasets: [{
                label: 'Overdue Amount (₹)',
                data: values,
                backgroundColor: ['#F39C12', '#E67E22', '#D35400', '#C0392B'],
                borderColor: ['#E67E22', '#D35400', '#C0392B', '#A93226'],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                title: {
                    display: true,
                    text: 'Overdue Receivables by Aging',
                    font: { size: 16, weight: 'bold' }
                }
            }
        }
    });
}

// Update aging table
function updateAgingTable(data) {
    const tbody = document.getElementById('agingTableBody');
    tbody.innerHTML = '';

    Object.keys(data.agingBuckets).forEach(bucket => {
        const row = tbody.insertRow();
        row.innerHTML = `
            <td>${bucket}</td>
            <td>${formatCurrency(data.agingBuckets[bucket])}</td>
        `;
    });
}

// Tab switching
function switchTab(tabName) {
    const contents = document.querySelectorAll('.tab-content');
    const buttons = document.querySelectorAll('.tab-btn');

    contents.forEach(content => content.classList.remove('active'));
    buttons.forEach(btn => btn.classList.remove('active'));

    document.getElementById(tabName).classList.add('active');
    const btn = document.querySelector(`.tab-btn[data-tab="${tabName}"]`);
    if (btn) btn.classList.add('active');
}

// Tab button event listeners
document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
        const tabName = btn.dataset.tab;
        const contents = document.querySelectorAll('.tab-content');
        const buttons = document.querySelectorAll('.tab-btn');

        contents.forEach(content => content.classList.remove('active'));
        buttons.forEach(b => b.classList.remove('active'));

        document.getElementById(tabName).classList.add('active');
        btn.classList.add('active');

        // Resize charts when tab is switched
        setTimeout(() => {
            Object.values(charts).forEach(chart => {
                if (chart) chart.resize();
            });
        }, 100);
    });
});

// Helper functions
function showLoading() {
    console.log('Loading...');
}

function showError(message) {
    alert(message);
}

function bindExcelUpload() {
    const fileInput = document.getElementById('excelFileInput');
    const selectExcelBtn = document.getElementById('selectExcelBtn');

    if (!fileInput || !selectExcelBtn) {
        console.warn('Excel upload UI not available on this page.');
        return;
    }

    selectExcelBtn.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', handleFileUpload);
}

// Initialize dashboard with Tally data if available
function initializeDashboard() {
    console.log('🚀 Dashboard initialization started');
    bindExcelUpload();
    
    // Check if Tally data is available
    fetch('/api/cashflow/tally/data-status')
        .then(response => {
            if (!response.ok) {
                console.warn('⚠️ Tally status check failed:', response.status);
                throw new Error('HTTP ' + response.status);
            }
            return response.json();
        })
        .then(status => {
            console.log('📊 Tally data status:', status);
            
            if (status.hasTallyData) {
                console.log('✅ Tally data found (' + status.invoiceCount + ' invoices), loading...');
                loadTallyOverview();
            } else {
                console.log('⏳ No Tally data available');
            }
        })
        .catch(error => {
            console.warn('⚠️ Could not fetch Tally status:', error.message);
        });
}

// Load and display Tally data
function loadTallyOverview() {
    console.log('📥 Loading Tally overview...');
    fetch('/api/cashflow/tally/overview')
        .then(response => {
            if (!response.ok) {
                console.error('❌ Tally overview failed:', response.status);
                throw new Error('HTTP ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            console.log('📦 Raw response:', data);
            
            if (!data || !data.hasData || !data.data) {
                console.error('❌ Missing data in response');
                return;
            }
            
            console.log('✅ Valid data received:');
            console.log('  - Invoices:', data.invoiceCount);
            console.log('  - Inflow:', data.data.totalInflow);
            console.log('  - Outflow:', data.data.totalOutflow);
            console.log('  - Net:', data.data.netCashflow);
            
            // Store in window
            window.lastOverviewData = data.data;
            console.log('💾 Stored in window.lastOverviewData');
            
            // Update the DOM with actual values
            console.log('🎯 Updating DOM elements...');
            
            // Try template functions first
            if (typeof updateTotalInflowDisplay === 'function') {
                console.log('  → Calling updateTotalInflowDisplay');
                try {
                    updateTotalInflowDisplay(data.data);
                } catch(e) {
                    console.error('  ✗ updateTotalInflowDisplay failed:', e.message);
                }
            }
            
            if (typeof updateNetCashFlowDisplay === 'function') {
                console.log('  → Calling updateNetCashFlowDisplay');
                try {
                    updateNetCashFlowDisplay(data.data);
                } catch(e) {
                    console.error('  ✗ updateNetCashFlowDisplay failed:', e.message);
                }
            }
            
            // Try displayAnalysis
            if (typeof displayAnalysis === 'function') {
                console.log('  → Calling displayAnalysis');
                try {
                    displayAnalysis(data.data);
                } catch(e) {
                    console.error('  ✗ displayAnalysis failed:', e.message);
                }
            }
            
            // Fallback: direct DOM updates
            console.log('🔧 Fallback: Direct DOM updates');
            const totalInflowEl = document.getElementById('totalInflow');
            const totalOutflowEl = document.getElementById('totalOutflow');
            const netCashflowEl = document.getElementById('netCashflow');
            
            if (totalInflowEl) {
                const formatted = new Intl.NumberFormat('en-IN', {style: 'currency', currency: 'INR'}).format(data.data.totalInflow);
                totalInflowEl.textContent = formatted;
                console.log('  ✓ Updated totalInflow to:', formatted);
            }
            
            if (totalOutflowEl) {
                const formatted = new Intl.NumberFormat('en-IN', {style: 'currency', currency: 'INR'}).format(data.data.totalOutflow);
                totalOutflowEl.textContent = formatted;
                console.log('  ✓ Updated totalOutflow to:', formatted);
            }
            
            if (netCashflowEl) {
                const formatted = new Intl.NumberFormat('en-IN', {style: 'currency', currency: 'INR'}).format(data.data.netCashflow);
                netCashflowEl.textContent = formatted;
                console.log('  ✓ Updated netCashflow to:', formatted);
            }
            
            console.log('✨ Dashboard initialization complete');
        })
        .catch(error => {
            console.error('❌ Error loading Tally overview:', error);
        });
}

// Start initialization with a delay to ensure page is fully loaded
console.log('⏱️ Scheduling dashboard initialization...');
setTimeout(function() {
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeDashboard);
    } else {
        initializeDashboard();
    }
}, 500);
