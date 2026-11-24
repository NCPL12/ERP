package com.ncpl.sales.service;

/** Keeps the application's scheduled Tally reads and PO writes from overlapping. */
public final class TallySyncCoordinator {
    private static final java.util.concurrent.locks.ReentrantLock LOCK = new java.util.concurrent.locks.ReentrantLock();
    private TallySyncCoordinator() { }
    public static boolean tryAcquire() { return LOCK.tryLock(); }
    public static void release() { LOCK.unlock(); }
}
