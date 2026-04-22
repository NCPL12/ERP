package com.ncpl.sales.model;

public class DashboardCountDto {
    private long salesOrderCount;
    private long purchaseOrderCount;
    private long invoiceCount;
    private long tdsItemsCount;
    private long sowithDesignCount;
    private long sowithoutDesignCount;
    private long projectPreviewCount;

    public long getSalesOrderCount() {
        return salesOrderCount;
    }

    public void setSalesOrderCount(long salesOrderCount) {
        this.salesOrderCount = salesOrderCount;
    }

    public long getPurchaseOrderCount() {
        return purchaseOrderCount;
    }

    public void setPurchaseOrderCount(long purchaseOrderCount) {
        this.purchaseOrderCount = purchaseOrderCount;
    }

    public long getInvoiceCount() {
        return invoiceCount;
    }

    public void setInvoiceCount(long invoiceCount) {
        this.invoiceCount = invoiceCount;
    }

    public long getTdsItemsCount() {
        return tdsItemsCount;
    }

    public void setTdsItemsCount(long tdsItemsCount) {
        this.tdsItemsCount = tdsItemsCount;
    }

    public long getSowithDesignCount() {
        return sowithDesignCount;
    }

    public void setSowithDesignCount(long sowithDesignCount) {
        this.sowithDesignCount = sowithDesignCount;
    }

    public long getSowithoutDesignCount() {
        return sowithoutDesignCount;
    }

    public void setSowithoutDesignCount(long sowithoutDesignCount) {
        this.sowithoutDesignCount = sowithoutDesignCount;
    }

    public long getProjectPreviewCount() {
        return projectPreviewCount;
    }

    public void setProjectPreviewCount(long projectPreviewCount) {
        this.projectPreviewCount = projectPreviewCount;
    }

    /** Defensive copy for cached responses. */
    public static DashboardCountDto copyOf(DashboardCountDto src) {
        if (src == null) {
            return null;
        }
        DashboardCountDto d = new DashboardCountDto();
        d.setSalesOrderCount(src.getSalesOrderCount());
        d.setPurchaseOrderCount(src.getPurchaseOrderCount());
        d.setInvoiceCount(src.getInvoiceCount());
        d.setTdsItemsCount(src.getTdsItemsCount());
        d.setSowithDesignCount(src.getSowithDesignCount());
        d.setSowithoutDesignCount(src.getSowithoutDesignCount());
        d.setProjectPreviewCount(src.getProjectPreviewCount());
        return d;
    }
}