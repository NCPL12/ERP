package com.ncpl.sales.cashflow.service;

import com.ncpl.sales.cashflow.dto.AnalyticsDTO;
import com.ncpl.sales.cashflow.dto.OverviewAnalysisDTO;
import com.ncpl.sales.cashflow.model.Invoice;
import com.ncpl.sales.cashflow.service.AgingAnalysisService.AgingAnalysisDTO;
import com.ncpl.sales.cashflow.service.CustomerVendorAnalysisService.CustomerVendorAnalysisDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Session-scoped service to store uploaded Excel data across all tabs
 * This ensures data persists when switching between Overview, Analytics, Customer-Vendor, and Aging Analysis
 */
@Service
@SessionScope
public class SessionDataService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String selectedMode = "TALLY";
    private String uploadedFileName;
    private String savedFilePath;
    private List<Invoice> invoices;
    private OverviewAnalysisDTO overviewData;
    private AnalyticsDTO analyticsData;
    private CustomerVendorAnalysisDTO customerVendorData; // For Due Date Tracker (ALL invoices)
    private CustomerVendorAnalysisDTO overdueData;         // For Overdue Payments (overdue only)
    private AgingAnalysisDTO agingAnalysisData;
    private BigDecimal bankBalance = BigDecimal.ZERO;
    
    // Filename methods
    public String getUploadedFileName() {
        return uploadedFileName;
    }
    
    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }
    
    public boolean hasUploadedFile() {
        return uploadedFileName != null && !uploadedFileName.isEmpty();
    }
    
    // Saved file path methods
    public String getSavedFilePath() {
        return savedFilePath;
    }
    
    public void setSavedFilePath(String savedFilePath) {
        this.savedFilePath = savedFilePath;
    }
    
    // Invoice data methods
    public List<Invoice> getInvoices() {
        return invoices;
    }
    
    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }
    
    // Overview data methods
    public OverviewAnalysisDTO getOverviewData() {
        return overviewData;
    }
    
    public void setOverviewData(OverviewAnalysisDTO overviewData) {
        this.overviewData = overviewData;
    }
    
    // Analytics data methods
    public AnalyticsDTO getAnalyticsData() {
        return analyticsData;
    }
    
    public void setAnalyticsData(AnalyticsDTO analyticsData) {
        this.analyticsData = analyticsData;
    }
    
    // Customer-Vendor data methods (ALL invoices - for Due Date Tracker)
    public CustomerVendorAnalysisDTO getCustomerVendorData() {
        return customerVendorData;
    }
    
    public void setCustomerVendorData(CustomerVendorAnalysisDTO customerVendorData) {
        this.customerVendorData = customerVendorData;
    }
    
    // Overdue data methods (OVERDUE only - for Overdue Payments)
    public CustomerVendorAnalysisDTO getOverdueData() {
        return overdueData;
    }
    
    public void setOverdueData(CustomerVendorAnalysisDTO overdueData) {
        this.overdueData = overdueData;
    }
    
    // Aging Analysis data methods
    public AgingAnalysisDTO getAgingAnalysisData() {
        return agingAnalysisData;
    }
    
    public void setAgingAnalysisData(AgingAnalysisDTO agingAnalysisData) {
        this.agingAnalysisData = agingAnalysisData;
    }

    public BigDecimal getBankBalance() {
        return bankBalance;
    }

    public void setBankBalance(BigDecimal bankBalance) {
        this.bankBalance = bankBalance != null ? bankBalance : BigDecimal.ZERO;
    }

    // Mode methods
    public String getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(String selectedMode) {
        this.selectedMode = selectedMode;
    }

    public boolean isExcelMode() {
        return "EXCEL".equals(selectedMode);
    }

    public boolean isTallyMode() {
        return "TALLY".equals(selectedMode);
    }
    
    /**
     * Clear all session data
     */
    public void clearAll() {
        this.selectedMode = "TALLY";
        this.uploadedFileName = null;
        this.savedFilePath = null;
        this.invoices = null;
        this.overviewData = null;
        this.analyticsData = null;
        this.customerVendorData = null;
        this.overdueData = null;
        this.agingAnalysisData = null;
        this.bankBalance = BigDecimal.ZERO;
    }
    
    /**
     * Check if any data exists in session
     */
    public boolean hasData() {
        return uploadedFileName != null || invoices != null;
    }
}
