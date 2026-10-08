package com.ncpl.sales.service;

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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.generator.FileNameGenerator;

public class MonthlyStockMovementExcel extends AbstractXlsxView {

    FileNameGenerator fileNameGenerator = new FileNameGenerator();

    @Override
    @SuppressWarnings("unchecked")
    protected void buildExcelDocument(Map model, Workbook workbook, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String fromDate = (String) model.get("fromDate");
        String toDate = (String) model.get("toDate");
        String fileName = fileNameGenerator.generateFileNameAsDate() + "monthly_stock_movement.xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        List<Map<String, Object>> records = (List<Map<String, Object>>) model.get("monthlyReport");

        Sheet sheet = workbook.createSheet("Monthly Stock Movement");
        sheet.setDefaultColumnWidth(14);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 30 * 256);

        // Styles
        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        Font titleFont = workbook.createFont();
        titleFont.setFontName("Calibri");
        titleFont.setFontHeight((short) (14 * 20));
        titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        titleStyle.setFont(titleFont);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        Font headerFont = workbook.createFont();
        headerFont.setFontName("Calibri");
        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        headerFont.setColor(HSSFColor.WHITE.index);
        headerStyle.setFont(headerFont);

        CellStyle decimalStyle = workbook.createCellStyle();
        XSSFDataFormat fmt = (XSSFDataFormat) workbook.createDataFormat();
        decimalStyle.setDataFormat(fmt.getFormat("#,###.00"));

        CellStyle totalStyle = workbook.createCellStyle();
        totalStyle.setDataFormat(fmt.getFormat("#,###.00"));
        Font totalFont = workbook.createFont();
        totalFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        totalStyle.setFont(totalFont);

        // Title row (row 1, merged across all 15 columns 0-14)
        Row titleRow = sheet.createRow(1);
        sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 14));
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Monthly Stock Movement Report (" + fromDate + " to " + toDate + ")");
        titleCell.setCellStyle(titleStyle);

        // Group header row (row 3)
        Row groupRow = sheet.createRow(3);
        sheet.addMergedRegion(new CellRangeAddress(3, 4, 0, 0));
        sheet.addMergedRegion(new CellRangeAddress(3, 4, 1, 1));
        sheet.addMergedRegion(new CellRangeAddress(3, 4, 2, 2));
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 3, 5));
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 6, 8));
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 9, 11));
        sheet.addMergedRegion(new CellRangeAddress(3, 3, 12, 14));

        String[] groupLabels = {"S/No", "Model No", "Description", "Opening Balance", "", "", "Inward (GRN)", "", "", "Outward (DC)", "", "", "Closing Balance", "", ""};
        for (int i = 0; i < groupLabels.length; i++) {
            Cell c = groupRow.createCell(i);
            c.setCellValue(groupLabels[i]);
            c.setCellStyle(headerStyle);
        }

        // Sub-header row (row 4)
        Row subRow = sheet.createRow(4);
        String[] subLabels = {"S/No", "Model No", "Description", "Qty", "Rate", "Value", "Qty", "Rate", "Value", "Qty", "Rate", "Value", "Qty", "Rate", "Value"};
        for (int i = 0; i < subLabels.length; i++) {
            Cell c = subRow.createCell(i);
            c.setCellValue(subLabels[i]);
            c.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 5;
        int sno = 1;
        double totalOpenQty = 0, totalOpenVal = 0;
        double totalInQty = 0, totalInVal = 0;
        double totalOutQty = 0, totalOutVal = 0;
        double totalClQty = 0, totalClVal = 0;

        for (Map<String, Object> rec : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(sno++);
            row.createCell(1).setCellValue(rec.get("modelNo") != null ? String.valueOf(rec.get("modelNo")) : "");
            row.createCell(2).setCellValue(rec.get("description") != null ? String.valueOf(rec.get("description")) : "");

            double openQty = toDouble(rec.get("openQty"));
            double openRate = toDouble(rec.get("openRate"));
            double openVal = toDouble(rec.get("openValue"));
            double inQty = toDouble(rec.get("inwardQty"));
            double inRate = toDouble(rec.get("inwardRate"));
            double inVal = toDouble(rec.get("inwardValue"));
            double outQty = toDouble(rec.get("outwardQty"));
            double outRate = toDouble(rec.get("outwardRate"));
            double outVal = toDouble(rec.get("outwardValue"));
            double clQty = toDouble(rec.get("closingQty"));
            double clRate = toDouble(rec.get("closingRate"));
            double clVal = toDouble(rec.get("closingValue"));

            setNumCell(row, 3, openQty, decimalStyle);
            setNumCell(row, 4, openRate, decimalStyle);
            setNumCell(row, 5, openVal, decimalStyle);
            setNumCell(row, 6, inQty, decimalStyle);
            setNumCell(row, 7, inRate, decimalStyle);
            setNumCell(row, 8, inVal, decimalStyle);
            setNumCell(row, 9, outQty, decimalStyle);
            setNumCell(row, 10, outRate, decimalStyle);
            setNumCell(row, 11, outVal, decimalStyle);
            setNumCell(row, 12, clQty, decimalStyle);
            setNumCell(row, 13, clRate, decimalStyle);
            setNumCell(row, 14, clVal, decimalStyle);

            totalOpenQty += openQty; totalOpenVal += openVal;
            totalInQty += inQty;    totalInVal += inVal;
            totalOutQty += outQty;  totalOutVal += outVal;
            totalClQty += clQty;    totalClVal += clVal;
        }

        // Totals row
        Row totalRow = sheet.createRow(rowNum);
        Cell totalLabel = totalRow.createCell(0);
        totalLabel.setCellValue("Total");
        totalLabel.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));

        setNumCell(totalRow, 3, totalOpenQty, totalStyle);
        setNumCell(totalRow, 5, totalOpenVal, totalStyle);
        setNumCell(totalRow, 6, totalInQty, totalStyle);
        setNumCell(totalRow, 8, totalInVal, totalStyle);
        setNumCell(totalRow, 9, totalOutQty, totalStyle);
        setNumCell(totalRow, 11, totalOutVal, totalStyle);
        setNumCell(totalRow, 12, totalClQty, totalStyle);
        setNumCell(totalRow, 14, totalClVal, totalStyle);
    }

    private void setNumCell(Row row, int col, double value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellStyle(style);
        c.setCellValue(value);
    }

    private double toDouble(Object obj) {
        return obj instanceof Number ? ((Number) obj).doubleValue() : 0.0;
    }
}
