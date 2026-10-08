package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.common.Constants;
import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.Stock;

public class StockListExcel extends AbstractXlsxView {
	List<Stock> stockList;
	StockListExcel(List<Stock> stockList){
		this.stockList = stockList;
	}
	@Autowired
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	
	EmailService emailService = new EmailService() ;
	
	StockService stockService = new StockService();

	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	
	ExcelLogoService logoService = new ExcelLogoService();
	@SuppressWarnings("unchecked")
	@Override
	protected void buildExcelDocument(Map<String,Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String fileName = fileNameGenerator.generateFileNameAsDate() + "Stock_List.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		Map<String, Object> stockMap = (Map<String, Object>) model.get("stockData");
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
		headingCell.setCellValue("Stock Report");
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

		header.createCell(0).setCellValue("Client Name");
		header.createCell(1).setCellValue("Quantity");
		header.createCell(2).setCellValue("Reason");
		header.createCell(3).setCellValue("Created");
		header.createCell(4).setCellValue("Updated");
		header.createCell(5).setCellValue("Location in Store");
		header.createCell(6).setCellValue("Store Name");

		// Apply style to header
		for (int i = 0; i < header.getLastCellNum(); i++) {// For each cell in
															// the row
			header.getCell(i).setCellStyle(style);// Set the style
		}

		populateStockRecords(stockMap, itemsReportSheet, workbook);
		File file=new File(Constants.FILE_LOCATION+File.separator+fileName);
		 FileOutputStream out = new FileOutputStream(file);
         workbook.write(out);
         out.close();
         
         Map<String, Object> emailContents = null;
      	emailContents=stockService.stockInfoEmail(fileName);
      	emailService.sendEmailToServer(emailContents);
		/*
		 * File file = new File(Constants.FILE_LOCATION+File.separator+fileName);
		 * //InputStreamResource resource = new InputStreamResource(new
		 * FileInputStream(file)); OutputStream out = new FileOutputStream(file);
		 * out.close();
		 */
	}
	private void populateStockRecords(Map<String, Object> stockMap, Sheet itemsReportSheet, Workbook workbook) {
				int rowCount =4;
				@SuppressWarnings("unchecked")
				List<Stock> stockList=(List<Stock>) stockMap.get("stockList");
				for (Stock stock : stockList) {
					Row row = itemsReportSheet.createRow(rowCount++);
					
					row.createCell(0).setCellValue(stock.getParty().getPartyName());
					row.createCell(1).setCellValue(stock.getQuantity());
					row.createCell(2).setCellValue(stock.getReason());
					row.createCell(3).setCellValue(stock.getCreated().toString());
					row.createCell(4).setCellValue(stock.getUpdated().toString());
					row.createCell(5).setCellValue(stock.getLocationInStore());
					row.createCell(6).setCellValue(stock.getStoreName());
				}
		
		}
		
	

}
