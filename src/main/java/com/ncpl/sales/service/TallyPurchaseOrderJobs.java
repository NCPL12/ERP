package com.ncpl.sales.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.annotation.PreDestroy;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;

/** Bounded, process-local manual exports. Polling never submits or retries a PO. */
@Service
public class TallyPurchaseOrderJobs {
    private final TallyPurchaseOrderService exporter;
    private final PurchaseOrderService orders;
    private final TransactionTemplate transaction;
    private final ExecutorService executor;
    private final Map<String,Job> jobs = new LinkedHashMap<>();
    private Job latest;

    @Autowired
    public TallyPurchaseOrderJobs(TallyPurchaseOrderService exporter, PurchaseOrderService orders,
                                  PlatformTransactionManager manager) {
        this(exporter,orders,manager,Executors.newSingleThreadExecutor(task -> {
            Thread worker=new Thread(task,"tally-manual-po-export");worker.setDaemon(true);return worker;
        }));
    }

    TallyPurchaseOrderJobs(TallyPurchaseOrderService exporter, PurchaseOrderService orders,
                           PlatformTransactionManager manager, ExecutorService executor) {
        this.exporter=exporter;this.orders=orders;this.executor=executor;
        this.transaction=new TransactionTemplate(manager);this.transaction.setReadOnly(true);
    }

    public synchronized Map<String,Object> start(Timestamp from,Timestamp to) {
        exporter.requireConfiguredCompany();
        if ((latest!=null && latest.running) || exporter.isExportRunning())
            throw new IllegalStateException("A PO export is already running. Monitor it before sending again.");
        Job job=new Job();
        jobs.put(job.id,job);latest=job;
        while(jobs.size()>20) jobs.remove(jobs.keySet().iterator().next());
        // Copy mutable dates before crossing threads. Load lazy ERP relationships
        // inside the worker transaction, not from a closed browser request session.
        Timestamp start=new Timestamp(from.getTime()),end=new Timestamp(to.getTime());
        try { executor.execute(() -> run(job,start,end)); }
        catch(RejectedExecutionException error) {
            job.error="ERP is shutting down; the export was not started.";job.running=false;
            throw new IllegalStateException(job.error,error);
        }
        return status(job);
    }

    private void run(Job job,Timestamp from,Timestamp to) {
        try {
            transaction.execute(ignored -> {
                job.phase="Loading ERP purchase orders";
                try {
                    job.result=exporter.exportAllPending(orders.getPurchaseOrderListByDate(from,to),
                        progress -> job.progress=progress);
                } catch(Exception error) { throw new ExportFailure(error); }
                return null;
            });
            job.phase="Completed";
        } catch(Exception error) {
            Throwable cause=error instanceof ExportFailure?error.getCause():error;
            job.error=cause.getMessage()==null?cause.getClass().getSimpleName():cause.getMessage();
            job.phase="Stopped";
        } finally { job.running=false; }
    }

    public synchronized Map<String,Object> current() {return latest==null?Collections.emptyMap():status(latest);}
    public synchronized Map<String,Object> get(String id) {
        Job job=jobs.get(id);return job==null?null:status(job);
    }
    private Map<String,Object> status(Job job) {
        // Read running first: its volatile completion write publishes result/error.
        boolean running=job.running;
        Map<String,Object> value=new LinkedHashMap<>();
        value.put("jobId",job.id);value.put("running",running);value.put("stage",job.phase);
        value.put("progress",job.progress);value.put("result",job.result);value.put("error",job.error);
        return value;
    }
    @PreDestroy public void shutdown() {executor.shutdown();}
    private static final class Job {
        final String id=UUID.randomUUID().toString();
        volatile boolean running=true;
        volatile String phase="Queued",error;
        volatile Map<String,Object> progress=Collections.emptyMap();
        volatile TallyPurchaseOrderService.ExportResult result;
    }
    private static final class ExportFailure extends RuntimeException {ExportFailure(Exception cause){super(cause);}}
}
