package com.ncpl.sales.model;

import java.util.List;
import java.util.Map;

/** Snapshot of the 7 dashboard list cards — see {@link com.ncpl.sales.service.DashboardService}. */
public class DashboardListsDto {
    private List<SalesOrder> pendingSalesList;
    private List<PurchaseOrder> pendingPurchaseList;
    private List<Invoice> invoiceList;
    private List<SalesOrder> allSalesList;
    private List<TdsItems> tdsApprovedList;
    private List<Map<String, Object>> salesItemsWithoutDesignList;
    private List<SalesOrder> salesOrderWithDesignList;

    public List<SalesOrder> getPendingSalesList() {
        return pendingSalesList;
    }
    public void setPendingSalesList(List<SalesOrder> pendingSalesList) {
        this.pendingSalesList = pendingSalesList;
    }
    public List<PurchaseOrder> getPendingPurchaseList() {
        return pendingPurchaseList;
    }
    public void setPendingPurchaseList(List<PurchaseOrder> pendingPurchaseList) {
        this.pendingPurchaseList = pendingPurchaseList;
    }
    public List<Invoice> getInvoiceList() {
        return invoiceList;
    }
    public void setInvoiceList(List<Invoice> invoiceList) {
        this.invoiceList = invoiceList;
    }
    public List<SalesOrder> getAllSalesList() {
        return allSalesList;
    }
    public void setAllSalesList(List<SalesOrder> allSalesList) {
        this.allSalesList = allSalesList;
    }
    public List<TdsItems> getTdsApprovedList() {
        return tdsApprovedList;
    }
    public void setTdsApprovedList(List<TdsItems> tdsApprovedList) {
        this.tdsApprovedList = tdsApprovedList;
    }
    public List<Map<String, Object>> getSalesItemsWithoutDesignList() {
        return salesItemsWithoutDesignList;
    }
    public void setSalesItemsWithoutDesignList(List<Map<String, Object>> salesItemsWithoutDesignList) {
        this.salesItemsWithoutDesignList = salesItemsWithoutDesignList;
    }
    public List<SalesOrder> getSalesOrderWithDesignList() {
        return salesOrderWithDesignList;
    }
    public void setSalesOrderWithDesignList(List<SalesOrder> salesOrderWithDesignList) {
        this.salesOrderWithDesignList = salesOrderWithDesignList;
    }
}
