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

        Map<String, List<DeliveryChallanItems>> dcByItem = getDcItemsBySalesItem(soItems);

        int maxLots = 2;
        int fixedCols = 7;
        int lotCols = 3 * maxLots;
        int deliveryCols = 2;
        int totalCols = fixedCols + lotCols + deliveryCols;

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

        // Row 1 - Title
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalCols - 1));
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("SITE QUANTITY REPORT");
        titleCell.setCellStyle(titleStyle);

        // Row 2 - Main Headers
        Row headerRow = sheet.createRow(1);
        String[] mainHeaders = {"SI NO", "DESCRIPTION", "PO QTY", "UNIT", "DESIGN STATUS", "", "TDS STATUS"};
        for (int i = 0; i < fixedCols; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(mainHeaders[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 4, 5));

        String[] qtyHeaders = {"SITE QUANTITY", "DELIVERED QTY", "PENDING QTY"};
        for (int q = 0; q < 3; q++) {
            int startCol = fixedCols + (q * maxLots);
            int endCol = startCol + maxLots - 1;
            sheet.addMergedRegion(new CellRangeAddress(1, 1, startCol, endCol));
            Cell cell = headerRow.createCell(startCol);
            cell.setCellValue(qtyHeaders[q]);
            cell.setCellStyle(headerStyle);
            for (int c = startCol + 1; c <= endCol; c++) {
                headerRow.createCell(c).setCellStyle(headerStyle);
            }
        }

        int dcDateCol = fixedCols + lotCols;
        int dcQtyCol = dcDateCol + 1;
        Cell dcDateHeader = headerRow.createCell(dcDateCol);
        dcDateHeader.setCellValue("DC DATE");
        dcDateHeader.setCellStyle(headerStyle);
        Cell dcQtyHeader = headerRow.createCell(dcQtyCol);
        dcQtyHeader.setCellValue("DC QTY");
        dcQtyHeader.setCellStyle(headerStyle);

        // Row 3 - Sub Headers
        Row subRow = sheet.createRow(2);
        String[] subHeaders = {"", "", "", "", "MODEL NUMBER", "QTY", ""};
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
        Cell dcDateSub = subRow.createCell(dcDateCol);
        dcDateSub.setCellStyle(subHeaderStyle);
        Cell dcQtySub = subRow.createCell(dcQtyCol);
        dcQtySub.setCellStyle(subHeaderStyle);

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

        int rowNum = 3;
        int slNo = 1;

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

            // DC items for this sales item
            List<DeliveryChallanItems> itemDcList = dcByItem.getOrDefault(salesItemId, new ArrayList<>());

            // Lot data
            List<Lot> lots = (tdsItem != null) ? tdsItem.getLots() : null;
            float[] lotSiteQtys = new float[maxLots];
            float totalSiteQty = 0;
            if (lots != null) {
                for (int l = 0; l < maxLots && l < lots.size(); l++) {
                    lotSiteQtys[l] = lots.get(l).getQuantity();
                    totalSiteQty += lotSiteQtys[l];
                }
            }

            if (itemDcList.isEmpty()) {
                Row row = sheet.createRow(rowNum++);
                fillItemRow(row, salesItem, slNo++, unit, tdsItem, modelDisplay, dataCenter, dataStyle, dataRight);
                fillLotData(row, fixedCols, maxLots, lotSiteQtys, totalSiteQty, 0, dataRight);
                row.createCell(dcDateCol).setCellStyle(dataCenter);
                row.createCell(dcQtyCol).setCellStyle(dataRight);
            } else {
                int itemSlNo = slNo++;
                float cumulativeDelivered = 0;
                for (int d = 0; d < itemDcList.size(); d++) {
                    Row row = sheet.createRow(rowNum++);
                    DeliveryChallanItems dcItem = itemDcList.get(d);
                    if (d == 0) {
                        fillItemRow(row, salesItem, itemSlNo, unit, tdsItem, modelDisplay, dataCenter, dataStyle, dataRight);
                    } else {
                        fillItemRow(row, salesItem, null, unit, tdsItem, modelDisplay, dataCenter, dataStyle, dataRight);
                    }

                    float todaysQty = dcItem.getTodaysQty();
                    cumulativeDelivered += todaysQty;
                    fillLotData(row, fixedCols, maxLots, lotSiteQtys, totalSiteQty, cumulativeDelivered, dataRight);

                    Date dcCreated = dcItem.getCreated();
                    Cell dateCell = row.createCell(dcDateCol);
                    if (dcCreated != null) {
                        dateCell.setCellValue(new SimpleDateFormat("dd-MM-yyyy").format(dcCreated));
                    }
                    dateCell.setCellStyle(dataCenter);

                    Cell qtyCell = row.createCell(dcQtyCol);
                    qtyCell.setCellValue(todaysQty);
                    qtyCell.setCellStyle(dataRight);
                }
            }
        }

        int lastRow = sheet.getLastRowNum();
        setBordersToMergedCells(wb, sheet, lastRow);

        FileOutputStream fos = new FileOutputStream(filePath);
        wb.write(fos);
        fos.close();
        wb.close();
    }

    private void fillItemRow(Row row, SalesItem salesItem, Integer slNo, String unit,
            TdsItems tdsItem, String modelDisplay, CellStyle dataCenter,
            CellStyle dataStyle, CellStyle dataRight) {
        if (slNo != null) {
            Cell cell = row.createCell(0);
            cell.setCellValue(String.valueOf(slNo));
            cell.setCellStyle(dataCenter);
        } else {
            row.createCell(0).setCellStyle(dataCenter);
        }

        String desc = salesItem.getDescription() != null ? salesItem.getDescription() : "";
        Cell descCell = row.createCell(1);
        descCell.setCellValue(desc);
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

        float designQty = (tdsItem != null) ? tdsItem.getDesignQty() : 0;
        Cell designCell = row.createCell(5);
        designCell.setCellValue(designQty);
        designCell.setCellStyle(dataRight);

        Cell tdsCell = row.createCell(6);
        tdsCell.setCellValue(tdsItem != null && tdsItem.isTdsApproved() ? "Yes" : "No");
        tdsCell.setCellStyle(dataCenter);
    }

    private void fillLotData(Row row, int fixedCols, int maxLots, float[] lotSiteQtys,
            float totalSiteQty, float cumulativeDelivered, CellStyle dataRight) {
        for (int l = 0; l < maxLots; l++) {
            float lotSiteQty = lotSiteQtys[l];
            float lotDelivered = 0;
            if (totalSiteQty > 0) {
                lotDelivered = cumulativeDelivered * (lotSiteQty / totalSiteQty);
                if (lotDelivered > lotSiteQty) lotDelivered = lotSiteQty;
            }
            float lotPending = lotSiteQty - lotDelivered;
            if (lotPending < 0) lotPending = 0;

            int siteCol = fixedCols + l;
            Cell siteCell = row.createCell(siteCol);
            siteCell.setCellValue(lotSiteQty);
            siteCell.setCellStyle(dataRight);

            int delCol = fixedCols + maxLots + l;
            Cell delCell = row.createCell(delCol);
            delCell.setCellValue(lotDelivered);
            delCell.setCellStyle(dataRight);

            int pendCol = fixedCols + (2 * maxLots) + l;
            Cell pendCell = row.createCell(pendCol);
            pendCell.setCellValue(lotPending);
            pendCell.setCellStyle(dataRight);
        }
    }

    private Map<String, List<DeliveryChallanItems>> getDcItemsBySalesItem(List<SalesItem> soItems) {
        Map<String, List<DeliveryChallanItems>> result = new HashMap<>();
        List<String> salesItemIds = soItems.stream().map(SalesItem::getId).collect(Collectors.toList());
        if (salesItemIds.isEmpty()) return result;
        List<DeliveryChallanItems> dcItems = dcItemRepo.findBySalesItemIdIn(salesItemIds);
        for (DeliveryChallanItems dci : dcItems) {
            result.computeIfAbsent(dci.getDescription(), k -> new ArrayList<>()).add(dci);
        }
        return result;
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
