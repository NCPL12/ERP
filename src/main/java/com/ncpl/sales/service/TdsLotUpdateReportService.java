package com.ncpl.sales.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Lot;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Tds;
import com.ncpl.sales.model.TdsItems;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;

@Service
public class TdsLotUpdateReportService {

    @Autowired
    private SalesService salesService;

    @Autowired
    private ItemMasterService itemService;

    @Autowired
    private DeliveryChallanItemsRepo dcItemRepo;

    private static final short BORDER_THIN = 1;
    private static final int   FIXED_COLS  = 7;

    public void generateReport(SalesOrder salesOrder, Tds tdsObj, String filePath) throws IOException {
        List<SalesItem> soItems = salesOrder.getItems();
        if (soItems == null || soItems.isEmpty()) return;

        List<TdsItems> tdsItems = tdsObj.getItems();
        Map<String, TdsItems> tdsItemMap = new HashMap<>();
        if (tdsItems != null) {
            for (TdsItems ti : tdsItems) {
                tdsItemMap.put(ti.getDescription(), ti);
            }
        }

        // Determine max lots dynamically
        int maxLots = 1;
        if (tdsItems != null) {
            for (TdsItems ti : tdsItems) {
                if (ti.getLots() != null && ti.getLots().size() > maxLots) {
                    maxLots = ti.getLots().size();
                }
            }
        }

        // Total delivered qty per sales item — same as Material Tracker
        Map<String, Float> deliveredQtyMap = getDeliveredQtyBySalesItem(soItems);

        // Column layout:
        // [0-6] fixed | [7..7+maxLots-1] SITE QTY lots | [7+maxLots] DELIVERED QTY | [7+maxLots+1] PENDING QTY
        int colSiteQtyStart = FIXED_COLS;
        int colDeliveredQty = FIXED_COLS + maxLots;
        int colPendingQty   = FIXED_COLS + maxLots + 1;
        int totalCols       = FIXED_COLS + maxLots + 2;

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Site Quantity Report");
        sheet.setDefaultColumnWidth(12);

        Font boldFont = wb.createFont();
        boldFont.setFontName("Calibri");
        boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        boldFont.setFontHeight((short) (10.5 * 20));

        Font titleFont = wb.createFont();
        titleFont.setFontName("Calibri");
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        titleFont.setFontHeight((short) (15.5 * 20));

        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        titleStyle.setFont(titleFont);
        titleStyle.setBorderBottom(BORDER_THIN);
        titleStyle.setBorderTop(BORDER_THIN);
        titleStyle.setBorderLeft(BORDER_THIN);
        titleStyle.setBorderRight(BORDER_THIN);

        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setFont(boldFont);
        headerStyle.setBorderBottom(BORDER_THIN);
        headerStyle.setBorderTop(BORDER_THIN);
        headerStyle.setBorderLeft(BORDER_THIN);
        headerStyle.setBorderRight(BORDER_THIN);
        headerStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle subHeaderStyle = wb.createCellStyle();
        subHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        subHeaderStyle.setFont(boldFont);
        subHeaderStyle.setBorderBottom(BORDER_THIN);
        subHeaderStyle.setBorderTop(BORDER_THIN);
        subHeaderStyle.setBorderLeft(BORDER_THIN);
        subHeaderStyle.setBorderRight(BORDER_THIN);

        // Row 0 — Title
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalCols - 1));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SITE QUANTITY REPORT - SO No: " + salesOrder.getId());
        titleCell.setCellStyle(titleStyle);

        // Row 1 — Main headers
        Row headerRow = sheet.createRow(1);
        String[] fixedHeaders = {"SI NO", "DESCRIPTION", "PO QTY", "UNIT", "DESIGN STATUS", "", "TDS STATUS"};
        for (int i = 0; i < FIXED_COLS; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(fixedHeaders[i]);
            c.setCellStyle(headerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 5));

        // SITE QUANTITY — spans all lot columns
        if (maxLots > 1) {
            sheet.addMergedRegion(new CellRangeAddress(1, 1, colSiteQtyStart, colSiteQtyStart + maxLots - 1));
        }
        Cell siteQtyHeader = headerRow.createCell(colSiteQtyStart);
        siteQtyHeader.setCellValue("SITE QUANTITY");
        siteQtyHeader.setCellStyle(headerStyle);
        for (int c = colSiteQtyStart + 1; c < colSiteQtyStart + maxLots; c++) {
            headerRow.createCell(c).setCellStyle(headerStyle);
        }

        Cell delHeader = headerRow.createCell(colDeliveredQty);
        delHeader.setCellValue("DELIVERED QTY");
        delHeader.setCellStyle(headerStyle);

        Cell pendHeader = headerRow.createCell(colPendingQty);
        pendHeader.setCellValue("PENDING QTY");
        pendHeader.setCellStyle(headerStyle);

        // Row 2 — Sub-headers
        Row subRow = sheet.createRow(2);
        String[] fixedSub = {"", "", "", "", "MODEL NUMBER", "QTY", ""};
        for (int i = 0; i < FIXED_COLS; i++) {
            Cell c = subRow.createCell(i);
            if (!fixedSub[i].isEmpty()) c.setCellValue(fixedSub[i]);
            c.setCellStyle(subHeaderStyle);
        }
        for (int l = 0; l < maxLots; l++) {
            Cell c = subRow.createCell(colSiteQtyStart + l);
            c.setCellValue("LOT " + (l + 1));
            c.setCellStyle(subHeaderStyle);
        }
        subRow.createCell(colDeliveredQty).setCellStyle(subHeaderStyle);
        subRow.createCell(colPendingQty).setCellStyle(subHeaderStyle);

        // Data styles
        Font dataFont = wb.createFont();
        dataFont.setFontName("Calibri");
        dataFont.setFontHeight((short) (10.5 * 20));

        CellStyle dataStyle = wb.createCellStyle();
        dataStyle.setFont(dataFont);
        dataStyle.setBorderBottom(BORDER_THIN);
        dataStyle.setBorderTop(BORDER_THIN);
        dataStyle.setBorderLeft(BORDER_THIN);
        dataStyle.setBorderRight(BORDER_THIN);
        dataStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);

        CellStyle dataRight = wb.createCellStyle();
        dataRight.cloneStyleFrom(dataStyle);
        dataRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

        CellStyle dataCenter = wb.createCellStyle();
        dataCenter.cloneStyleFrom(dataStyle);
        dataCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        CellStyle highlightRight = wb.createCellStyle();
        highlightRight.cloneStyleFrom(dataRight);
        highlightRight.setFillForegroundColor(HSSFColor.YELLOW.index);
        highlightRight.setFillPattern(CellStyle.SOLID_FOREGROUND);

        int rowNum = 3;
        int slNo   = 1;

        for (SalesItem salesItem : soItems) {
            String salesItemId = salesItem.getId();
            String unit = salesItem.getItem_units() != null && salesItem.getItem_units().getName() != null
                    ? salesItem.getItem_units().getName() : "";

            TdsItems tdsItem = tdsItemMap.get(salesItemId);

            String modelDisplay = "";
            String itemMasterId = (tdsItem != null) ? tdsItem.getModelNumber() : "";
            if (itemMasterId != null && !itemMasterId.isEmpty()) {
                try {
                    Optional<ItemMaster> imOpt = itemService.getItemById(itemMasterId);
                    if (imOpt.isPresent() && imOpt.get().getModel() != null) {
                        modelDisplay = imOpt.get().getModel();
                    }
                } catch (Exception e) { }
            }

            // Per-lot site quantities
            List<Lot> lots = (tdsItem != null) ? tdsItem.getLots() : null;
            float[] lotQtys = new float[maxLots];
            float totalSiteQty = 0;
            if (lots != null) {
                for (int l = 0; l < lots.size() && l < maxLots; l++) {
                    lotQtys[l] = lots.get(l).getQuantity();
                    totalSiteQty += lotQtys[l];
                }
            }

            // Delivered qty = total sum of all DC todaysQty (same as Material Tracker)
            float deliveredQty = deliveredQtyMap.getOrDefault(salesItemId, 0f);
            float pendingQty   = Math.max(0, totalSiteQty - deliveredQty);

            Row row = sheet.createRow(rowNum++);
            fillItemRow(row, salesItem, slNo++, unit, tdsItem, modelDisplay, dataCenter, dataStyle, dataRight);

            for (int l = 0; l < maxLots; l++) {
                if (lotQtys[l] > 0) {
                    createNumCell(row, colSiteQtyStart + l, lotQtys[l], highlightRight);
                } else {
                    createNumCell(row, colSiteQtyStart + l, lotQtys[l], dataRight);
                }
            }
            createNumCell(row, colDeliveredQty, deliveredQty, dataRight);
            createNumCell(row, colPendingQty, pendingQty, dataRight);
        }

        int lastRow = sheet.getLastRowNum();
        setBordersToMergedCells(wb, sheet, lastRow);

        FileOutputStream fos = new FileOutputStream(filePath);
        wb.write(fos);
        fos.close();
        wb.close();
    }

    // Total delivered qty per sales item — mirrors Material Tracker's calculateDeliveredQty
    private Map<String, Float> getDeliveredQtyBySalesItem(List<SalesItem> soItems) {
        Map<String, Float> result = new HashMap<>();
        List<String> salesItemIds = soItems.stream().map(SalesItem::getId).collect(Collectors.toList());
        if (salesItemIds.isEmpty()) return result;
        List<DeliveryChallanItems> dcItems = dcItemRepo.findBySalesItemIdIn(salesItemIds);
        for (DeliveryChallanItems dci : dcItems) {
            result.merge(dci.getDescription(), dci.getTodaysQty(), Float::sum);
        }
        return result;
    }

    private void createNumCell(Row row, int col, float value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void fillItemRow(Row row, SalesItem salesItem, Integer slNo, String unit,
            TdsItems tdsItem, String modelDisplay, CellStyle dataCenter,
            CellStyle dataStyle, CellStyle dataRight) {
        Cell slNoCell = row.createCell(0);
        if (slNo != null) slNoCell.setCellValue(String.valueOf(slNo));
        slNoCell.setCellStyle(dataCenter);

        Cell descCell = row.createCell(1);
        descCell.setCellValue(salesItem.getDescription() != null ? salesItem.getDescription() : "");
        descCell.setCellStyle(dataStyle);

        Cell poQtyCell = row.createCell(2);
        poQtyCell.setCellValue(salesItem.getQuantity());
        poQtyCell.setCellStyle(dataRight);

        Cell unitCell = row.createCell(3);
        unitCell.setCellValue(unit);
        unitCell.setCellStyle(dataCenter);

        Cell modelCell = row.createCell(4);
        modelCell.setCellValue(modelDisplay);
        modelCell.setCellStyle(dataStyle);

        Cell designCell = row.createCell(5);
        designCell.setCellValue(tdsItem != null ? tdsItem.getDesignQty() : 0);
        designCell.setCellStyle(dataRight);

        Cell tdsCell = row.createCell(6);
        tdsCell.setCellValue(tdsItem != null && tdsItem.isTdsApproved() ? "Yes" : "No");
        tdsCell.setCellStyle(dataCenter);
    }

    private void setBordersToMergedCells(Workbook wb, Sheet sheet, int lastRow) {
        int numMerged = sheet.getNumMergedRegions();
        for (int i = 0; i < numMerged; i++) {
            CellRangeAddress merged = sheet.getMergedRegion(i);
            RegionUtil.setBorderTop(CellStyle.BORDER_THIN, merged, sheet, wb);
            RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, merged, sheet, wb);
            RegionUtil.setBorderRight(CellStyle.BORDER_THIN, merged, sheet, wb);
            RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, merged, sheet, wb);
        }
    }
}
