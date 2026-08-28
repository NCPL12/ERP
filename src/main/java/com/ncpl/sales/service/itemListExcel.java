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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;

public class itemListExcel extends AbstractXlsxView {

	

	FileNameGenerator fileNameGenerator = new FileNameGenerator();

	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	
	 ExcelLogoService logoService = new ExcelLogoService();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String fileName = fileNameGenerator.generateFileNameAsDate() + "Items_List.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		Map<String, Map> invoicemap = (Map<String, Map>) model.get("itemsData");
		
		Sheet itemsReportSheet = workbook.createSheet("Item List");
		itemsReportSheet.setDefaultColumnWidth(23);

		// create style for header cells
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");
		style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);

		Row secondRow = itemsReportSheet.createRow(1);
		itemsReportSheet.addMergedRegion(new CellRangeAddress(1, // first row
				2, // last row
				0, // first column
				3 // last column
		));

		Cell headingCell = secondRow.createCell(0);
		headingCell.setCellValue("Items List");
		CellStyle mergestyle = workbook.createCellStyle();
		mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		Font headingFont = workbook.createFont();
		headingFont.setFontName("Calibri");
		headingFont.setFontHeight((short) (15.5 * 20));
		headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		mergestyle.setFont(headingFont);
		headingCell.setCellStyle(mergestyle);

		// create header row
		Row header = itemsReportSheet.createRow(3);

		header.createCell(0).setCellValue("Item Id");
		header.createCell(1).setCellValue("Description");
		header.createCell(2).setCellValue("Cost Price");
		header.createCell(3).setCellValue("Quantity");

		// Apply style to header
		for (int i = 0; i < header.getLastCellNum(); i++) {// For each cell in
															// the row
			header.getCell(i).setCellStyle(style);// Set the style
		}

		populateItemRecords(invoicemap, itemsReportSheet, workbook);

	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void populateItemRecords(Map<String, Map> invoicemap, Sheet itemsReportSheet, Workbook workbook) {
		// TODO Auto-generated method stub
		int rowCount =4;
		List<String> id = (List<String>) invoicemap.get("itemId");
		List<String> description = (List<String>) invoicemap.get("itemDesc");
		List<Float> quantity = (List<Float>) invoicemap.get("itemqQuantity");
		List<Double> costprice = (List<Double>) invoicemap.get("costList");
		
		if (id != null) {
			for (int i = 0; i < id.size(); i++) {
				// for(int i=0;i<glcList.size()-2;i++){
				Row row = itemsReportSheet.createRow(rowCount++);
				row.createCell(0).setCellValue(id.get(i));
				row.createCell(1).setCellValue(description.get(i));
				row.createCell(2).setCellValue(costprice.get(i));
				row.createCell(3).setCellValue(new Double(quantity.get(i)));
				
				
	}
}
}
}