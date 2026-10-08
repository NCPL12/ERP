(() => {
    const ids = {
        totalInflow: 'summaryTotalInflow',
        totalOutflow: 'summaryTotalOutflow',
        netCashFlow: 'summaryNetCashFlow',
        overdueAmount: 'summaryOverdueAmount',
        upcomingAmount: 'summaryUpcomingAmount',
        highlights: 'highlightsList',
        actions: 'actionItemsList',
        calendar: 'calendarEvents'
    };

    const formatDate = (value) => {
        if (!value) {
            return '-';
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return date.toLocaleDateString('en-IN', {
            weekday: 'short',
            month: 'short',
            day: 'numeric'
        });
    };

    const applyMetric = (id, value) => {
        const el = document.getElementById(id);
        if (el) {
            el.textContent = dashboardFilters.formatCurrency(value);
        }
    };

    const renderHighlights = (highlights) => {
        const container = document.getElementById(ids.highlights);
        if (!container) {
            return;
        }
        container.innerHTML = '';

        if (!highlights || highlights.length === 0) {
            const li = document.createElement('li');
            li.textContent = 'No highlights available for the selected period.';
            container.appendChild(li);
            return;
        }

        highlights.forEach((text) => {
            const li = document.createElement('li');
            li.textContent = text;
            container.appendChild(li);
        });
    };

    const renderActions = (actions) => {
        const container = document.getElementById(ids.actions);
        if (!container) {
            return;
        }
        container.innerHTML = '';

        if (!actions || actions.length === 0) {
            container.innerHTML = '<div class="text-muted">All clear! No action items for this window.</div>';
            return;
        }

        actions.forEach((action) => {
            const priorityClass = action.priority === 'HIGH'
                ? 'bg-danger text-white'
                : action.priority === 'MEDIUM'
                    ? 'bg-warning text-dark'
                    : 'bg-secondary text-white';

            const wrapper = document.createElement('div');
            wrapper.className = 'border-start border-4 border-primary ps-3 mb-3';
            wrapper.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <h6 class="mb-0">${action.title}</h6>
                    <span class="action-badge ${priorityClass}">${action.priority || 'LOW'}</span>
                </div>
                <p class="mb-1 text-muted small">${action.description}</p>
                <div class="text-muted small"><i class="fas fa-clock me-1"></i>Due ${formatDate(action.dueDate)}</div>
            `;
            container.appendChild(wrapper);
        });
    };

    const renderCalendar = (events) => {
        const container = document.getElementById(ids.calendar);
        if (!container) {
            return;
        }
        container.innerHTML = '';

        if (!events || events.length === 0) {
            container.innerHTML = '<div class="text-muted">No upcoming items yet. Great job staying current!</div>';
            return;
        }

        events.forEach((event) => {
            const badgeClass = event.type === 'RECEIVABLE' ? 'text-success' : 'text-danger';
            const wrapper = document.createElement('div');
            wrapper.className = 'calendar-event';
            const date = new Date(event.date);
            const dateLabel = Number.isNaN(date.getTime())
                ? event.date
                : `${date.toLocaleString('en-IN', { month: 'short' })}<br>${date.getDate()}`;

            wrapper.innerHTML = `
                <div class="date">${dateLabel}</div>
                <div class="label">${event.label}</div>
                <div class="type ${badgeClass}">${event.type || ''}</div>
            `;
            container.appendChild(wrapper);
        });
    };

    const fetchSummary = async ({ fromDate, toDate }) => {
        const params = new URLSearchParams();
        if (fromDate) {
            params.append('fromDate', fromDate);
        }
        if (toDate) {
            params.append('toDate', toDate);
        }

        const response = await fetch(`/api/cashflow/executive-summary?${params.toString()}`);
        if (!response.ok) {
            throw new Error('Failed to load summary');
        }
        return response.json();
    };

    const applySummary = (payload) => {
        applyMetric(ids.totalInflow, payload.totalInflow);
        applyMetric(ids.totalOutflow, payload.totalOutflow);
        applyMetric(ids.netCashFlow, payload.netCashFlow);
        applyMetric(ids.overdueAmount, payload.overdueAmount);
        applyMetric(ids.upcomingAmount, payload.upcomingAmount);

        renderHighlights(payload.highlights);
        renderActions(payload.actionItems);
        renderCalendar(payload.calendarEvents);
    };

    document.addEventListener('DOMContentLoaded', () => {
        const loader = document.createElement('div');
        loader.className = 'position-fixed top-50 start-50 translate-middle text-center';
        loader.style.zIndex = 1080;
        loader.innerHTML = `
            <div class="spinner-border text-primary mb-3" role="status"></div>
            <div class="text-muted">Building your AI summary...</div>
        `;
        document.body.appendChild(loader);

        const showLoader = () => loader.classList.remove('d-none');
        const hideLoader = () => loader.classList.add('d-none');

        dashboardFilters.initTimeFilters({
            onChange: (filters) => {
                showLoader();
                fetchSummary(filters)
                    .then((payload) => {
                        hideLoader();
                        applySummary(payload);
                    })
                    .catch(() => {
                        hideLoader();
                        applySummary({
                            totalInflow: 0,
                            totalOutflow: 0,
                            netCashFlow: 0,
                            overdueAmount: 0,
                            upcomingAmount: 0,
                            highlights: [],
                            actionItems: [],
                            calendarEvents: []
                        });
                        const container = document.getElementById(ids.highlights);
                        if (container) {
                            container.innerHTML = '<li>Upload a workbook on the Overview tab to generate insights.</li>';
                        }
                    });
            }
        });
    });
})();

