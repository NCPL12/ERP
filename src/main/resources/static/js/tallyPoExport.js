(function(root, factory) {
    if (typeof module === 'object' && module.exports) module.exports = factory;
    else root.attachTallyPoExport = factory;
}(this, function(options) {
    var form = options.form, button = options.button, label = options.label;
    var fetch = options.fetch, delay = options.delay, base = options.base;
    var original = button.innerHTML, timer, stopped = false;
    function busy(value) {
        if (value) form.dataset.exportRunning = 'true'; else delete form.dataset.exportRunning;
        button.disabled = value || button.getAttribute('data-can-export') === 'false';
        button.innerHTML = value ? 'Sending and verifying POs...' : original;
    }
    function draw(job) {
        var p = job.progress || {};
        label.textContent = (p.stage || job.stage || 'Preparing export') +
            ' | Processed: ' + (p.processed || 0) + '/' + (p.total || 0) +
            ' | New: ' + (p.imported || 0) + ' | Updated: ' + (p.updated || 0) +
            ' | Skipped: ' + (p.skipped || 0) + ' | Failed: ' + (p.failed || 0);
        if (!job.running) {
            busy(false);
            if (job.error) label.textContent = 'Export stopped: ' + job.error + ' Review the result before retrying.';
            else if (job.result) {
                label.textContent = 'Completed: ' + job.result.total + ' POs checked. See verified results below.';
                options.render(job.result);
            }
        } else busy(true);
    }
    function schedule(id) {timer = delay(function() {monitor(id);}, 2000);}
    function monitor(id) {
        return fetch(base + '/' + id, {credentials:'same-origin',cache:'no-store'})
            .then(function(response) {
                if (response.status === 404) {
                    stopped = true;
                    throw new Error('Export status is no longer available. Check Tally before retrying.');
                }
                if (!response.ok) throw new Error('Monitoring unavailable');
                return response.json();
            }).then(function(job) {
                if (stopped) return;
                if (!job.jobId) {
                    if (form.dataset.exportRunning === 'true') label.textContent = 'No export status was found. Refresh this page and check Tally before retrying.';
                    return;
                }
                draw(job);
                if (job.running) schedule(job.jobId);
            }).catch(function(error) {
                if (stopped) {label.textContent = error.message;return;}
                label.textContent = 'Progress connection interrupted. Reconnecting; do not send again.';
                schedule(id);
            });
    }
    form.addEventListener('submit', function(event) {
        var submitter = event.submitter || options.activeElement();
        if (!submitter || (submitter.getAttribute('formaction') || '').indexOf('/tally/import') < 0) return;
        event.preventDefault();
        if (form.dataset.exportRunning === 'true' || button.getAttribute('data-can-export') === 'false') return;
        busy(true);
        label.textContent = 'Starting background export...';
        fetch(base, {method:'POST',credentials:'same-origin',body:options.formData(form)})
            .then(function(response) {
                return response.json().then(function(body) {
                    if (!response.ok) {
                        // A conflict can mean another tab already started the export.
                        busy(false);label.textContent = body.message || 'Export was not started.';
                        if (response.status === 409) monitor('current');
                        return;
                    }
                    draw(body);
                    if (body.running) schedule(body.jobId);
                });
            }).catch(function() {
                // A lost HTTP response is not proof that nothing was imported.
                // Never retry the POST automatically.
                label.textContent = 'Start response lost. Checking existing export; do not send again.';
                monitor('current');
            });
    });
    // Recover monitoring after navigation or a browser timeout without another POST.
    monitor('current');
    return {monitor:monitor, stop:function() {stopped=true;options.cancel(timer);}};
}));
