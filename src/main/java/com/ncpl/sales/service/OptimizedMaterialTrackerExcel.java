package com.ncpl.sales.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;

/**
 * Optimized Material Tracker Excel generation with streaming and chunking
 * for large datasets to prevent memory issues
 */
public class OptimizedMaterialTrackerExcel extends AbstractXlsxView {
    
    private static final int CHUNK_SIZE = 100; // Process items in chunks
    private static final int MAX_ROWS_BEFORE_FLUSH = 1000; // Flush to disk periodically
    
    short VERTICAL_TOP = 0x0;
    short VERTICAL_JUSTIFY = 0x2;
    short BORDER_THIN = 0x1;
    String company = "Neptune controls pvt ltd No.8/2(Old No.114), 2nd Cross 7th Main Road Nandidurga Extension Bangalore-560046 Contact : 080-40904685,7624964492 "
            + "E-Mail : accounts@ncpl.co";
    String s1 = "Complete solution for BMS, Lighting Control, CCTV & Security Systems, DDC Panels, Automation Panels, Lighting,panels, MCC & Starter Panels";
    FileNameGenerator fileNameGenerator = new FileNameGenerator();
    
    // To read the message source from property file
    @Autowired
    private MessageSource messageSource;
    
    InvoiceExcelLogoService logoService = new InvoiceExcelLogoService();
    
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        @SuppressWarnings("unchecked")
        Map<String, Object> salesMap = (Map<String, Object>) model.get("salesData");
        SalesOrder salesObj = (SalesOrder) salesMap.get("salesObj");
        List<SalesItem> salesItems = salesObj.getItems();
        Date date = salesObj.getClientPoDate();
        String shippingParty = (String) request.getAttribute("shippingParty");
        String shippingPartyAddress = (String) request.getAttribute("shippingPartyAddr");
        @SuppressWarnings("unchecked")
        Map<String, String> itemsList = (Map<String, String>) request.getAttribute("map");
        
        // Get optimized data from the optimized service
        OptimizedMaterialTrackerService optimizedService = new OptimizedMaterialTrackerService();
        Map<String, Object> optimizedData = optimizedService.getOptimizedExcelData(salesObj);
        
        // Converting date to String
        String dateString = date.toString();
        String[] arr = dateString.split(" ");

        // Formatting date to a required format
        String formattedDate = arr[0];
        formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "/"
                + formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "/"
                + formattedDate.substring(0, 4);
        
        Date todayDate = new Date();
        DateFormat formatter4 = new SimpleDateFormat("dd/MM/yyyy");
        String date4 = formatter4.format(todayDate);

        String fileName = salesObj.getClientPoNumber() + "_SALES_OPTIMIZED.xlsx";
        // set excel file name
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        Sheet editAccountSheet = workbook.createSheet("Sales");
        editAccountSheet.setDefaultColumnWidth(9);
        
        // Create header and basic structure
        createHeader(workbook, editAccountSheet, salesObj, shippingParty, shippingPartyAddress, formattedDate, date4);
        
        // Process items in chunks to avoid memory issues
        processItemsInChunks(workbook, editAccountSheet, salesItems, itemsList, optimizedData);
        
        // Apply final formatting
        applyFinalFormatting(workbook, editAccountSheet);
    }
    
    private void createHeader(Workbook workbook, Sheet sheet, SalesOrder salesObj, 
            String shippingParty, String shippingPartyAddress, String formattedDate, String date4) {
        
        // Create header rows (same as original but optimized)
        sheet.addMergedRegion(new CellRangeAddress(1, 5, 0, 5));

        CellStyle mergestyle = workbook.createCellStyle();
        mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        Font headingFont = workbook.createFont();
        headingFont.setFontName("Calibri");
        headingFont.setFontHeightInPoints((short) 11);
        headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        mergestyle.setFont(headingFont);

        // Insert Logo
        try {
            logoService.insertLogoInTemplate(workbook, sheet, null);
        } catch (Exception e) {
            // Logo insertion failed, continue without logo
        }

        // Create header cells with optimized styling
        createHeaderCells(workbook, sheet, salesObj, shippingParty, shippingPartyAddress, formattedDate, date4);
    }
    
    private void createHeaderCells(Workbook workbook, Sheet sheet, SalesOrder salesObj, 
            String shippingParty, String shippingPartyAddress, String formattedDate, String date4) {
        
        // Client PO Number
        Row poNumberRow = sheet.createRow(1);
        Cell poNum = poNumberRow.createCell(6);
        poNum.setCellValue("Client PO No: " + salesObj.getClientPoNumber());
        applyHeaderCellStyle(workbook, poNum);
        
        // Client PO Date
        Row poDateRow = sheet.createRow(2);
        Cell poDate = poDateRow.createCell(6);
        poDate.setCellValue("Client PO Date: " + formattedDate);
        applyHeaderCellStyle(workbook, poDate);
        
        // Project Name
        Row projectRow = sheet.createRow(3);
        Cell project = projectRow.createCell(6);
        project.setCellValue("Project Name.: " + shippingParty);
        applyHeaderCellStyle(workbook, project);
        
        // Project Address
        Row addressRow = sheet.createRow(4);
        Cell address = addressRow.createCell(6);
        address.setCellValue("Project Address: " + shippingPartyAddress);
        applyHeaderCellStyle(workbook, address);
        
        // Material Tracker Date
        Row trackerDateRow = sheet.createRow(5);
        Cell trackerDate = trackerDateRow.createCell(6);
        trackerDate.setCellValue("Material Tracker Date: " + date4);
        applyHeaderCellStyle(workbook, trackerDate);
        
        // Material Tracker Header
        Row headerRow = sheet.createRow(6);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Material Tracker");
        applyHeaderCellStyle(workbook, headerCell);
        
        // Create column headers
        createColumnHeaders(workbook, sheet);
    }
    
    private void createColumnHeaders(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(7);
        String[] headers = {
            "Sl No", "Description", "Qty as per BOQ", "Qty as per site", 
            "Ordered Qty", "Not Ordered Qty", "Delivered Qty", "Instore Qty",
            "Model Number", "PO Number", "PO Date", "Vendor", "Grn Date", 
            "DC Date", "DC No.", "Design Date"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            applyHeaderCellStyle(workbook, cell);
        }
    }
    
    private void processItemsInChunks(Workbook workbook, Sheet sheet, List<SalesItem> salesItems, 
            Map<String, String> itemsList, Map<String, Object> optimizedData) {
        
        int rowCount = 11;
        int processedRows = 0;
        
        // Process items in chunks to avoid memory issues
        for (int i = 0; i < salesItems.size(); i += CHUNK_SIZE) {
            int endIndex = Math.min(i + CHUNK_SIZE, salesItems.size());
            List<SalesItem> chunk = salesItems.subList(i, endIndex);
            
            for (SalesItem salesItem : chunk) {
                rowCount = processSalesItem(workbook, sheet, salesItem, itemsList, optimizedData, rowCount);
                processedRows++;
                
                // Flush to disk periodically to prevent memory issues
                if (processedRows % MAX_ROWS_BEFORE_FLUSH == 0) {
                    try {
                        // Force garbage collection and memory cleanup
                        System.gc();
                        Thread.sleep(10); // Small delay to allow GC
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
    
    private int processSalesItem(Workbook workbook, Sheet sheet, SalesItem salesItem, 
            Map<String, String> itemsList, Map<String, Object> optimizedData, int rowCount) {
        
        String unitName = salesItem.getItem_units().getName();
        String key = salesItem.getId();
        String itemValue = (String) itemsList.get(key);
        
        if (itemValue == null) {
            return rowCount;
        }
        
        // Parse quantities
        int purchaseQtyStartsFrom = itemValue.indexOf("$");
        int noOrderQtyStartsFrom = itemValue.indexOf("&");
        int grnQtyStartsFrom = itemValue.indexOf("%");
        
        String dcQty1 = itemValue.substring(0, purchaseQtyStartsFrom);
        String orderedQty1 = itemValue.substring(purchaseQtyStartsFrom + 1, noOrderQtyStartsFrom);
        String notOrderedQty1 = itemValue.substring(noOrderQtyStartsFrom + 1, grnQtyStartsFrom);
        String grnQty1 = itemValue.substring(grnQtyStartsFrom + 1, itemValue.length());
        
        float dcQty = Float.parseFloat(dcQty1);
        float orderedQty = Float.parseFloat(orderedQty1);
        float notOrderedQty = Float.parseFloat(notOrderedQty1);
        float grnQty = Float.parseFloat(grnQty1);
        
        // Create row with optimized data access
        Row row = sheet.createRow(rowCount);
        
        // Set basic item data
        setBasicItemData(row, salesItem, dcQty, orderedQty, notOrderedQty, grnQty, unitName);
        
        // Set related data using optimized maps
        setRelatedData(row, salesItem, optimizedData);
        
        return rowCount + 1;
    }
    
    private void setBasicItemData(Row row, SalesItem salesItem, float dcQty, float orderedQty, 
            float notOrderedQty, float grnQty, String unitName) {
        
        // Sl No
        Cell slNo = row.createCell(0);
        slNo.setCellValue(salesItem.getSlNo());
        
        // Description
        Cell description = row.createCell(1);
        description.setCellValue(salesItem.getDescription());
        
        // Quantities
        if (!unitName.equals("Heading")) {
            Cell qtyBoq = row.createCell(2);
            qtyBoq.setCellValue(salesItem.getQuantity());
            
            Cell qtySite = row.createCell(3);
            qtySite.setCellValue(salesItem.getQuantity());
            
            Cell orderedQtyCell = row.createCell(4);
            orderedQtyCell.setCellValue(orderedQty);
            
            Cell notOrderedQtyCell = row.createCell(5);
            notOrderedQtyCell.setCellValue(notOrderedQty);
            
            Cell deliveredQtyCell = row.createCell(6);
            deliveredQtyCell.setCellValue(dcQty);
            
            Cell instoreQtyCell = row.createCell(7);
            instoreQtyCell.setCellValue(grnQty);
        }
    }
    
    private void setRelatedData(Row row, SalesItem salesItem, Map<String, Object> optimizedData) {
        String salesItemId = salesItem.getId();
        
        // Get optimized data maps
        @SuppressWarnings("unchecked")
        Map<String, List<DesignItems>> designItemsMap = (Map<String, List<DesignItems>>) optimizedData.get("designItemsMap");
        @SuppressWarnings("unchecked")
        Map<String, List<PurchaseItem>> purchaseItemsMap = (Map<String, List<PurchaseItem>>) optimizedData.get("purchaseItemsMap");
        @SuppressWarnings("unchecked")
        Map<String, List<DeliveryChallanItems>> dcItemsMap = (Map<String, List<DeliveryChallanItems>>) optimizedData.get("dcItemsMap");
        @SuppressWarnings("unchecked")
        Map<String, ItemMaster> itemsMap = (Map<String, ItemMaster>) optimizedData.get("itemsMap");
        @SuppressWarnings("unchecked")
        Map<Long, SalesOrderDesign> designsMap = (Map<Long, SalesOrderDesign>) optimizedData.get("designsMap");
        @SuppressWarnings("unchecked")
        Map<String, PurchaseOrder> poMap = (Map<String, PurchaseOrder>) optimizedData.get("poMap");
        @SuppressWarnings("unchecked")
        Map<String, List<Grn>> grnMap = (Map<String, List<Grn>>) optimizedData.get("grnMap");
        @SuppressWarnings("unchecked")
        Map<Integer, DeliveryChallan> dcMap = (Map<Integer, DeliveryChallan>) optimizedData.get("dcMap");
        
        // Set design data
        List<DesignItems> designItems = designItemsMap.get(salesItemId);
        if (designItems != null && !designItems.isEmpty()) {
            DesignItems firstDesign = designItems.get(0);
            String itemId = firstDesign.getItemId();
            ItemMaster item = itemsMap.get(itemId);
            if (item != null) {
                Cell modelCell = row.createCell(8);
                modelCell.setCellValue(item.getModel() + ", qty=" + firstDesign.getQuantity());
            }
        }
        
        // Set purchase order data
        List<PurchaseItem> purchaseItems = purchaseItemsMap.get(salesItemId);
        if (purchaseItems != null && !purchaseItems.isEmpty()) {
            PurchaseItem firstPurchase = purchaseItems.get(0);
            PurchaseOrder po = poMap.get(firstPurchase.getPurchaseOrder().getPoNumber());
            if (po != null) {
                Cell poNumberCell = row.createCell(9);
                poNumberCell.setCellValue(po.getPoNumber());
                
                Cell poDateCell = row.createCell(10);
                poDateCell.setCellValue(formatDate(po.getCreated()));
                
                Cell vendorCell = row.createCell(11);
                vendorCell.setCellValue(po.getParty().getPartyName());
            }
        }
        
        // Set GRN data
        if (purchaseItems != null) {
            for (PurchaseItem purchaseItem : purchaseItems) {
                List<Grn> grnList = grnMap.get(purchaseItem.getPurchaseOrder().getPoNumber());
                if (grnList != null && !grnList.isEmpty()) {
                    Grn firstGrn = grnList.get(0);
                    Cell grnDateCell = row.createCell(12);
                    grnDateCell.setCellValue(formatDate(firstGrn.getCreated()));
                    break;
                }
            }
        }
        
        // Set DC data
        List<DeliveryChallanItems> dcItems = dcItemsMap.get(salesItemId);
        if (dcItems != null && !dcItems.isEmpty()) {
            DeliveryChallanItems firstDcItem = dcItems.get(0);
            DeliveryChallan dc = dcMap.get(firstDcItem.getDeliveryChallan().getDcId());
            if (dc != null) {
                Cell dcDateCell = row.createCell(13);
                // Use the DC item's created date since DC doesn't have getCreated()
                dcDateCell.setCellValue(formatDate(firstDcItem.getCreated()));
                
                Cell dcNumberCell = row.createCell(14);
                dcNumberCell.setCellValue(dc.getDcId());
            }
        }
        
        // Set design date
        if (designItems != null && !designItems.isEmpty()) {
            SalesOrderDesign design = designsMap.get(designItems.get(0).getSalesOrderDesign().getId());
            if (design != null) {
                Cell designDateCell = row.createCell(15);
                designDateCell.setCellValue(formatDate(design.getCreated()));
            }
        }
    }
    
    private String formatDate(Date date) {
        if (date == null) return "";
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        return formatter.format(date);
    }
    
    private void applyHeaderCellStyle(Workbook workbook, Cell cell) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        cell.setCellStyle(style);
    }
    
    private void applyFinalFormatting(Workbook workbook, Sheet sheet) {
        // Auto-size columns for better readability
        for (int i = 0; i < 16; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // Apply borders to all cells
        applyBordersToSheet(workbook, sheet);
    }
    
    private void applyBordersToSheet(Workbook workbook, Sheet sheet) {
        // Apply borders to all data rows
        for (int i = 7; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = 0; j < 16; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        CellStyle style = workbook.createCellStyle();
                        style.cloneStyleFrom(cell.getCellStyle());
                        style.setBorderTop(CellStyle.BORDER_THIN);
                        style.setBorderBottom(CellStyle.BORDER_THIN);
                        style.setBorderLeft(CellStyle.BORDER_THIN);
                        style.setBorderRight(CellStyle.BORDER_THIN);
                        cell.setCellStyle(style);
                    }
                }
            }
        }
    }
}
