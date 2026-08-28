package com.ncpl.sales.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ncpl.sales.model.DashboardCountDto;
import com.ncpl.sales.service.DashboardService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Dashboard API", description = "Endpoints for Dashboard counts")
@RestController
@RequestMapping("/api/dashboard")
public class SalesRestController {

    @Autowired
    private DashboardService dashboardService;

    @ApiOperation(value = "Get Dashboard Counts", notes = "Returns count of Sales Orders, Purchase Orders, Invoices, TDS Items, etc.")
    @GetMapping("/counts")
    public Map<String, Long> getDashboardCounts() {
        DashboardCountDto dto = dashboardService.getDashboardCounts();
        Map<String, Long> counts = new HashMap<>();
        counts.put("salesOrderCount", dto.getSalesOrderCount());
        counts.put("purchaseOrderCount", dto.getPurchaseOrderCount());
        counts.put("invoiceCount", dto.getInvoiceCount());
        counts.put("tdsItemsCount", dto.getTdsItemsCount());
        counts.put("projectPreviewCount", dto.getProjectPreviewCount());
        counts.put("sowithDesignCount", dto.getSowithDesignCount());
        counts.put("sowithoutDesignCount", dto.getSowithoutDesignCount());
        return counts;
    }
}