(() => {
    let fromDateInputEl = null;
    let toDateInputEl = null;
    let changeHandler = null;

    const formatCurrency = (value) => {
        const amount = Number.isFinite(value) ? value : 0;
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    };

    const getState = () => {
        const fromDate = fromDateInputEl && fromDateInputEl.value ? fromDateInputEl.value : '';
        const toDate = toDateInputEl && toDateInputEl.value ? toDateInputEl.value : '';
        return { fromDate, toDate };
    };

    const notifyChange = () => {
        if (typeof changeHandler === 'function') {
            changeHandler(getState());
        }
    };

    const initTimeFilters = ({ onChange } = {}) => {
        fromDateInputEl = document.getElementById('filterFromDate');
        toDateInputEl = document.getElementById('filterToDate');
        changeHandler = onChange;

        const notify = () => {
            notifyChange();
        };
        
        // Add event listeners for date range inputs
        fromDateInputEl?.addEventListener('change', notify);
        toDateInputEl?.addEventListener('change', notify);

        notifyChange();
    };

    const setFilterState = ({ fromDate, toDate } = {}) => {
        if (fromDateInputEl) {
            fromDateInputEl.value = fromDate || '';
        }
        if (toDateInputEl) {
            toDateInputEl.value = toDate || '';
        }
    };

    window.dashboardFilters = {
        initTimeFilters,
        formatCurrency,
        setFilterState
    };
})();

