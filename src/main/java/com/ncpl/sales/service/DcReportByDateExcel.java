package com.ncpl.sales.service;

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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DeliveryChallanItems;

public class DcReportByDateExcel extends AbstractXlsxView {
	FileNameGenerator fileNameGenerator = new FileNameGenerator();

	@Override
	protected void buildExcelDocument(Map model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String fileName = fileNameGenerator.generateFileNameAsDate() + "dc_by_date.xlsx";
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		List<DeliveryChallanItems> dcItemList = (List<DeliveryChallanItems>) model.get("dcItemList");

		Sheet itemsReportSheet = workbook.createSheet("DC Report");
		itemsReportSheet.setDefaultColumnWidth(23);

		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");
		style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);

		Row secondRow = itemsReportSheet.createRow(1);
		itemsReportSheet.addMergedRegion(new CellRangeAddress(1, 2, 0, 6));

		Cell headingCell = secondRow.createCell(0);
		headingCell.setCellValue("DC Report By Date");
		CellStyle mergestyle = workbook.createCellStyle();
		mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		Font headingFont = workbook.createFont();
		headingFont.setFontName("Calibri");
		headingFont.setFontHeight((short) (15.5 * 20));
		headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		mergestyle.setFont(headingFont);
		headingCell.setCellStyle(mergestyle);

		Row header = itemsReportSheet.createRow(3);

		header.createCell(0).setCellValue("DC No");
		header.createCell(1).setCellValue("DC Date");
		header.createCell(2).setCellValue("Model No");
		header.createCell(3).setCellValue("Description");
		header.createCell(4).setCellValue("Quantity");
		header.createCell(5).setCellValue("Sales Price");
		header.createCell(6).setCellValue("Value");

		populateRecords(dcItemList, itemsReportSheet, workbook);
	}

	private void populateRecords(List<DeliveryChallanItems> dcItemList, Sheet itemsReportSheet, Workbook workbook) {
		int rowCount = 4;
		SimpleDateFormat dateFmt = new SimpleDateFormat("dd-MM-yyyy");
		CellStyle decimalStyle = workbook.createCellStyle();
		XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
		decimalStyle.setDataFormat(format.getFormat("#,###.00"));

		for (DeliveryChallanItems dcItem : dcItemList) {
			String modelNo = (String) dcItem.get("modelNo");
			String itemDescription = (String) dcItem.get("itemDescription");
			Object salesPriceObj = dcItem.get("salesPrice");
			Object dcNumObj = dcItem.get("dcNum");
			Date dcDate = (Date) dcItem.get("dcDate");

			if (modelNo == null) continue;

			float salesPrice = salesPriceObj instanceof Number ? ((Number) salesPriceObj).floatValue() : 0;
			float qty = dcItem.getTodaysQty();
			float amount = qty * salesPrice;

			Row row = itemsReportSheet.createRow(rowCount++);

			row.createCell(0).setCellValue(dcNumObj != null ? String.valueOf(dcNumObj) : "");
			row.createCell(1).setCellValue(dcDate != null ? dateFmt.format(dcDate) : "");
			row.createCell(2).setCellValue(modelNo);
			row.createCell(3).setCellValue(itemDescription != null ? itemDescription : "");

			Cell qtyCell = row.createCell(4);
			qtyCell.setCellStyle(decimalStyle);
			qtyCell.setCellValue(qty);

			Cell priceCell = row.createCell(5);
			priceCell.setCellStyle(decimalStyle);
			priceCell.setCellValue(salesPrice);

			Cell amountCell = row.createCell(6);
			amountCell.setCellStyle(decimalStyle);
			amountCell.setCellValue(amount);
		}
	}
}
