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
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Lot;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Tds;
import com.ncpl.sales.model.TdsItems;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;

public class SiteQuantityReportExcel {

    short BORDER_THIN = 1;

    public void buildExcelDocument(Tds tdsObj, String filePath,
            SalesService salesService, Optional<SalesOrder> salesOrder,
            ItemMasterService itemService,
            DeliveryChallanItemsRepo dcItemRepo) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Site Quantity Report");
        sheet.setDefaultColumnWidth(12);

        List<TdsItems> tdsItemsList = tdsObj.getItems();
        if (tdsItemsList == null || tdsItemsList.isEmpty()) {
            workbook.close();
            return;
        }

        int maxLots = 0;
        for (TdsItems item : tdsItemsList) {
            if (item.getLots() != null && item.getLots().size() > maxLots) {
                maxLots = item.getLots().size();
            }
        }
        if (maxLots == 0) maxLots = 1;

        Map<String, Float> deliveredQtyMap = getDeliveredQtyBySalesItem(salesOrder, dcItemRepo);

        int fixedCols = 7;
        int totalCols = fixedCols + (3 * maxLots);

        createHeader(workbook, sheet, maxLots, totalCols, fixedCols, salesOrder);

        populateData(workbook, sheet, tdsItemsList, maxLots, fixedCols, salesService, itemService, deliveredQtyMap);

        int lastRow = sheet.getLastRowNum();
        setBordersToMergedCells(workbook, sheet, lastRow);

        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    private Map<String, Float> getDeliveredQtyBySalesItem(Optional<SalesOrder> salesOrder,
            DeliveryChallanItemsRepo dcItemRepo) {
        Map<String, Float> result = new HashMap<>();
        if (!salesOrder.isPresent() || salesOrder.get().getItems() == null) return result;

        List<String> salesItemIds = salesOrder.get().getItems().stream()
                .map(SalesItem::getId)
                .collect(Collectors.toList());

        if (salesItemIds.isEmpty()) return result;

        List<DeliveryChallanItems> dcItems = dcItemRepo.findBySalesItemIdIn(salesItemIds);

        for (DeliveryChallanItems dcItem : dcItems) {
            String desc = dcItem.getDescription();
            result.merge(desc, dcItem.getTodaysQty(), Float::sum);
        }
        return result;
    }

    private void createHeader(Workbook workbook, Sheet sheet, int maxLots, int totalCols, int fixedCols,
            Optional<SalesOrder> salesOrder) {

        Font boldFont = workbook.createFont();
        boldFont.setFontName("Calibri");
        boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        boldFont.setFontHeight((short) (10.5 * 20));

        Font headerFont = workbook.createFont();
        headerFont.setFontName("Calibri");
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setFontHeight((short) (15.5 * 20));

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        titleStyle.setFont(headerFont);
        titleStyle.setBorderBottom(BORDER_THIN);
        titleStyle.setBorderTop(BORDER_THIN);
        titleStyle.setBorderLeft(BORDER_THIN);
        titleStyle.setBorderRight(BORDER_THIN);

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerCellStyle.setFont(boldFont);
        headerCellStyle.setBorderBottom(BORDER_THIN);
        headerCellStyle.setBorderTop(BORDER_THIN);
        headerCellStyle.setBorderLeft(BORDER_THIN);
        headerCellStyle.setBorderRight(BORDER_THIN);
        headerCellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        headerCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle subHeaderStyle = workbook.createCellStyle();
        subHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        subHeaderStyle.setFont(boldFont);
        subHeaderStyle.setBorderBottom(BORDER_THIN);
        subHeaderStyle.setBorderTop(BORDER_THIN);
        subHeaderStyle.setBorderLeft(BORDER_THIN);
        subHeaderStyle.setBorderRight(BORDER_THIN);

        // Row 1 - Title
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalCols - 1));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SITE QUANTITY REPORT");
        titleCell.setCellStyle(titleStyle);

        // Row 2 - Main Headers
        Row headerRow = sheet.createRow(1);
        String[] mainHeaders = {"SI NO", "DESCRIPTION", "PO QTY", "UNIT", "DESIGN STATUS", "", "TDS STATUS",
                "SITE QUANTITY", "", "", "DELIVERED QTY", "", "", "PENDING QTY", "", ""};

        int h = 0;
        for (int i = 0; i < fixedCols; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(mainHeaders[i]);
            cell.setCellStyle(headerCellStyle);
            h++;
        }

        String[] qtyHeaders = {"SITE QUANTITY", "DELIVERED QTY", "PENDING QTY"};
        for (int q = 0; q < 3; q++) {
            int startCol = fixedCols + (q * maxLots);
            int endCol = startCol + maxLots - 1;
            if (maxLots > 1) {
                sheet.addMergedRegion(new CellRangeAddress(1, 1, startCol, endCol));
            }
            Cell cell = headerRow.createCell(startCol);
            cell.setCellValue(qtyHeaders[q]);
            cell.setCellStyle(headerCellStyle);
            for (int c = startCol + 1; c <= endCol; c++) {
                headerRow.createCell(c).setCellStyle(headerCellStyle);
            }
        }

        // Merge DESIGN STATUS (E-F)
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 5));
        headerRow.getCell(4).setCellValue("DESIGN STATUS");

        // Row 3 - Sub Headers
        Row subRow = sheet.createRow(2);
        String[] subHeaders = {"", "", "", "", "MODEL NO", "QTY", ""};
        for (int i = 0; i < fixedCols; i++) {
            Cell cell = subRow.createCell(i);
            if (!subHeaders[i].isEmpty()) {
                cell.setCellValue(subHeaders[i]);
            }
            cell.setCellStyle(subHeaderStyle);
        }

        for (int q = 0; q < 3; q++) {
            for (int l = 0; l < maxLots; l++) {
                int col = fixedCols + (q * maxLots) + l;
                Cell cell = subRow.createCell(col);
                cell.setCellValue("LOT " + (l + 1));
                cell.setCellStyle(subHeaderStyle);
            }
        }
    }

    private void populateData(Workbook workbook, Sheet sheet, List<TdsItems> tdsItemsList,
            int maxLots, int fixedCols, SalesService salesService, ItemMasterService itemService,
            Map<String, Float> deliveredQtyMap) {

        Font dataFont = workbook.createFont();
        dataFont.setFontName("Calibri");
        dataFont.setFontHeight((short) (10.5 * 20));

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setFont(dataFont);
        dataStyle.setBorderBottom(BORDER_THIN);
        dataStyle.setBorderTop(BORDER_THIN);
        dataStyle.setBorderLeft(BORDER_THIN);
        dataStyle.setBorderRight(BORDER_THIN);
        dataStyle.setVerticalAlignment((short) 0x0);

        CellStyle dataStyleRight = workbook.createCellStyle();
        dataStyleRight.cloneStyleFrom(dataStyle);
        dataStyleRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

        CellStyle dataStyleCenter = workbook.createCellStyle();
        dataStyleCenter.cloneStyleFrom(dataStyle);
        dataStyleCenter.setAlignment(HSSFCellStyle.ALIGN_CENTER);

        int rowNum = 3;
        int slNo = 1;

        for (TdsItems tdsItem : tdsItemsList) {
            Row row = sheet.createRow(rowNum++);

            String salesItemId = tdsItem.getDescription();
            SalesItem salesItem = null;
            try {
                Optional<SalesItem> opt = salesService.getSalesItemObjById(salesItemId);
                if (opt.isPresent()) salesItem = opt.get();
            } catch (Exception e) {}

            String modelNo = "";
            String unit = "";
            if (salesItem != null) {
                modelNo = salesItem.getModelNo() != null ? salesItem.getModelNo() : "";
                if (salesItem.getItem_units() != null) {
                    unit = salesItem.getItem_units().getName() != null
                            ? salesItem.getItem_units().getName() : "";
                }
            }

            String itemMasterId = tdsItem.getModelNumber();
            String modelDisplay = "";
            if (itemMasterId != null && !itemMasterId.isEmpty()) {
                try {
                    Optional<ItemMaster> imOpt = itemService.getItemById(itemMasterId);
                    if (imOpt.isPresent() && imOpt.get().getModel() != null) {
                        modelDisplay = imOpt.get().getModel();
                    }
                } catch (Exception e) {}
            }

            row.createCell(0).setCellValue(String.valueOf(slNo++));
            row.getCell(0).setCellStyle(dataStyleCenter);

            String desc = salesItem != null && salesItem.getDescription() != null
                    ? salesItem.getDescription() : "";
            row.createCell(1).setCellValue(desc);
            row.getCell(1).setCellStyle(dataStyle);

            float poQty = salesItem != null ? salesItem.getQuantity() : 0;
            Cell poQtyCell = row.createCell(2);
            poQtyCell.setCellValue(poQty);
            poQtyCell.setCellStyle(dataStyleRight);

            row.createCell(3).setCellValue(unit);
            row.getCell(3).setCellStyle(dataStyleCenter);

            row.createCell(4).setCellValue(modelDisplay);
            row.getCell(4).setCellStyle(dataStyle);

            Cell designQtyCell = row.createCell(5);
            designQtyCell.setCellValue(tdsItem.getDesignQty());
            designQtyCell.setCellStyle(dataStyleRight);

            row.createCell(6).setCellValue(tdsItem.isTdsApproved() ? "Yes" : "No");
            row.getCell(6).setCellStyle(dataStyleCenter);

            List<Lot> lots = tdsItem.getLots();
            float totalSiteQty = 0;
            if (lots != null) {
                for (Lot lot : lots) {
                    totalSiteQty += lot.getQuantity();
                }
            }

            float totalDeliveredQty = deliveredQtyMap.getOrDefault(salesItemId, 0f);

            for (int l = 0; l < maxLots; l++) {
                float lotSiteQty = 0;
                if (lots != null && l < lots.size()) {
                    lotSiteQty = lots.get(l).getQuantity();
                }

                float lotDeliveredQty = 0;
                if (totalSiteQty > 0) {
                    lotDeliveredQty = totalDeliveredQty * (lotSiteQty / totalSiteQty);
                }
                float lotPendingQty = lotSiteQty - lotDeliveredQty;

                int siteCol = fixedCols + l;
                Cell siteCell = row.createCell(siteCol);
                siteCell.setCellValue(lotSiteQty);
                siteCell.setCellStyle(dataStyleRight);

                int delCol = fixedCols + maxLots + l;
                Cell delCell = row.createCell(delCol);
                delCell.setCellValue(lotDeliveredQty);
                delCell.setCellStyle(dataStyleRight);

                int pendCol = fixedCols + (2 * maxLots) + l;
                Cell pendCell = row.createCell(pendCol);
                pendCell.setCellValue(lotPendingQty);
                pendCell.setCellStyle(dataStyleRight);
            }
        }
    }

    private void setBordersToMergedCells(Workbook workBook, Sheet sheet, int lastRow) {
        int numMerged = sheet.getNumMergedRegions();
        for (int i = 0; i < numMerged; i++) {
            CellRangeAddress mergedRegions = sheet.getMergedRegion(i);
            RegionUtil.setBorderTop(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
            RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
            RegionUtil.setBorderRight(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
            RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
        }
    }
}
