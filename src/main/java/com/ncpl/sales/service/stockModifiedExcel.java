package com.ncpl.sales.service;

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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Stock;

public class stockModifiedExcel extends AbstractXlsxView {

	

	FileNameGenerator fileNameGenerator = new FileNameGenerator();

	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	

	
	
	
	 ExcelLogoService logoService = new ExcelLogoService();
	@SuppressWarnings({ "rawtypes" })
	@Override
	protected void buildExcelDocument(Map  model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String fileName = fileNameGenerator.generateFileNameAsDate() + "Items_List.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		List stockList = (List) model.get("stockData");
		
		Sheet itemsReportSheet = workbook.createSheet("Stock Report");
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
	   
     	header.createCell(0).setCellValue("Description");
		itemsReportSheet.addMergedRegion(new CellRangeAddress(3, // first row
				3, // last row
				1, // first column
				4 // last column
		));
		header.createCell(1).setCellValue("Old Value");
		itemsReportSheet.addMergedRegion(new CellRangeAddress(3, // first row
				3, // last row
				5, // first column
				8 // last column
		));
		header.createCell(5).setCellValue("Changed Value");
		header.createCell(9).setCellValue("Reason");
		header.createCell(10).setCellValue("Created");
		header.createCell(11).setCellValue("Updated");

		
		 Row header2 =  itemsReportSheet.createRow(4);
		 header2.createCell(1).setCellValue("Client Name");
		 header2.createCell(2).setCellValue("Quantity");
		 header2.createCell(3).setCellValue("Location");
		 header2.createCell(4).setCellValue("Store Name");
		 header2.createCell(5).setCellValue("Client Name");
		 header2.createCell(6).setCellValue("Quantity");
		 header2.createCell(7).setCellValue("Location");
		 header2.createCell(8).setCellValue("Store Name");
		
		// Apply style to header
		//for (int i = 0; i < header.getLastCellNum(); i++) {// For each cell in
															// the row
		//	header.getCell(i).setCellStyle(style);// Set the style
	//	}

		populateStocksRecords(stockList, itemsReportSheet, workbook);

	}
	@SuppressWarnings({ "unused", "rawtypes" })
	private void populateStocksRecords(List stockList, Sheet itemsReportSheet, Workbook workbook) {
		// TODO Auto-generated method stub
		int rowCount =5;
		for (int i = 0; i < stockList.size(); i++) {
			Map result = (Map) stockList.get(i);
			Stock oldStock = (Stock) result.get("oldStock");
			String oldclientName = oldStock.getClientName();
			if(oldclientName ==null){
				oldclientName ="";
			}
			float oldquantity =  oldStock.getQuantity();
			String oldLocation = oldStock.getLocationInStore();
			String oldstoreName = oldStock.getStoreName();
			ItemMaster item = oldStock.getItemMaster();
			String id = item.getId();
			String description=item.getItemName();
			String modelNo=item.getModel();
			String name = item.getItemName();
			Stock newStock  = (Stock) result.get("newStock");
			String newclientName = newStock.getClientName();
			if(newclientName == null){
				newclientName = "";
			}
			float newquantity =  newStock.getQuantity();
			String newLocation = newStock.getLocationInStore();
			String newstoreName = newStock.getStoreName();
			String reason = newStock.getReason();
			Date oldcreate = oldStock.getCreated();
			String oldDate = oldcreate.toString();
			Date newUpdate = newStock.getUpdated();
			String newDate= newUpdate.toString();
			
			CellStyle declimalStyle = workbook.createCellStyle();
			XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
			declimalStyle.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
			
			
			
				// for(int i=0;i<glcList.size()-2;i++){
				Row row = itemsReportSheet.createRow(rowCount++);
				
				Cell oldQtyCell = row.createCell(2);
				oldQtyCell.setCellStyle(declimalStyle);
				oldQtyCell.setCellValue(oldquantity);
				
				Cell newQtyCell = row.createCell(6);
				newQtyCell.setCellStyle(declimalStyle);
				newQtyCell.setCellValue(newquantity);
				
				row.createCell(0).setCellValue(description + " - " + modelNo);
				row.createCell(1).setCellValue(oldclientName);
				//row.createCell(2).setCellValue(oldquantity);
				row.createCell(3).setCellValue(oldLocation);
				row.createCell(4).setCellValue(oldstoreName);
				row.createCell(5).setCellValue(newclientName);
				//row.createCell(6).setCellValue(newquantity);
				row.createCell(7).setCellValue(newLocation);
				row.createCell(8).setCellValue(newstoreName);
				row.createCell(9).setCellValue(reason);
				row.createCell(10).setCellValue(oldDate);
				row.createCell(11).setCellValue(newDate);
				
				
	}
}
}
