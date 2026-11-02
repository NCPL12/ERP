package com.ncpl.sales.service;

import java.util.concurrent.*;

/** Bounds caller latency and worker count, including blocked HTTP writes/reads. */
final class TallyRequestDeadline {
    private static final ThreadPoolExecutor WORKERS = new ThreadPoolExecutor(
            0, 2, 30, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), task -> {
                Thread thread = new Thread(task, "tally-po-http");
                thread.setDaemon(true);
                return thread;
            });

    static <T> T call(Callable<T> request, long timeoutMs) throws Exception {
        if (timeoutMs <= 0) throw new IllegalArgumentException("Tally request timeout must be positive.");
        Future<T> pending;
        try { pending = WORKERS.submit(request); }
        catch (RejectedExecutionException error) {
            throw new IllegalStateException("Previous Tally connections are still closing. Retry shortly.", error);
        }
        try { return pending.get(timeoutMs, TimeUnit.MILLISECONDS); }
        catch (TimeoutException error) {
            pending.cancel(true);
            throw new IllegalStateException("Tally request timed out after " + timeoutMs + " ms. Check the server connection before retrying.", error);
        } catch (InterruptedException error) {
            pending.cancel(true); Thread.currentThread().interrupt(); throw error;
        } catch (ExecutionException error) {
            Throwable cause = error.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw new IllegalStateException("Tally request failed", cause);
        }
    }
}
