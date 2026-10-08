package com.ncpl.sales.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.MonthlyReportStock;

public class MonthlyStockReportExcel extends AbstractXlsxView {

	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		@SuppressWarnings("unchecked")
		List<MonthlyReportStock> stockList = (List<MonthlyReportStock>) model.get("monthlyStockReport");
		ItemMasterService itemMasterService = (ItemMasterService) request.getAttribute("itemMasterService");
		LocalDate reportDate = (LocalDate) request.getAttribute("reportDate");

		String fileName = "Monthly_Stock_Report_" + reportDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".xlsx";
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		Sheet sheet = workbook.createSheet("Monthly Stock Report");
		sheet.setDefaultColumnWidth(23);

		CellStyle headerStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");
		headerStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
		headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		headerStyle.setFont(font);

		Row titleRow = sheet.createRow(1);
		sheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 4));
		Cell titleCell = titleRow.createCell(0);
		titleCell.setCellValue("Monthly Stock Report - " + reportDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		CellStyle titleStyle = workbook.createCellStyle();
		titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		Font titleFont = workbook.createFont();
		titleFont.setFontName("Calibri");
		titleFont.setFontHeight((short) (15.5 * 20));
		titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(titleFont);
		titleCell.setCellStyle(titleStyle);

		Row header = sheet.createRow(3);
		for (int i = 0; i < 5; i++) {
			header.createCell(i).setCellStyle(headerStyle);
		}
		header.getCell(0).setCellValue("S/No");
		header.getCell(1).setCellValue("Model No");
		header.getCell(2).setCellValue("Description");
		header.getCell(3).setCellValue("Outstanding Qty");
		header.getCell(4).setCellValue("Outstanding Value");

		CellStyle decimalStyle = workbook.createCellStyle();
		XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
		decimalStyle.setDataFormat(format.getFormat("#,###.00"));

		int rowNum = 4;
		int serialNo = 1;
		for (MonthlyReportStock stock : stockList) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(serialNo++);

			String itemId = stock.getItemMasterId();
			Optional<ItemMaster> itemOpt = itemMasterService.getItemById(itemId);
			if (itemOpt.isPresent()) {
				ItemMaster item = itemOpt.get();
				row.createCell(1).setCellValue(item.getModel());
				row.createCell(2).setCellValue(item.getItemName());
			} else {
				row.createCell(1).setCellValue(itemId);
				row.createCell(2).setCellValue("");
			}

			BigDecimal qty = stock.getOutstandingQty();
			if (qty != null) {
				Cell qtyCell = row.createCell(3);
				qtyCell.setCellStyle(decimalStyle);
				qtyCell.setCellValue(qty.doubleValue());
			} else {
				row.createCell(3).setCellValue(0);
			}

			BigDecimal value = stock.getOutstandingValue();
			if (value != null) {
				Cell valueCell = row.createCell(4);
				valueCell.setCellStyle(decimalStyle);
				valueCell.setCellValue(value.doubleValue());
			} else {
				row.createCell(4).setCellValue(0);
			}
		}
	}
}
