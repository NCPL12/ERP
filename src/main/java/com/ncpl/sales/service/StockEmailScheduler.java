
package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.lang.SystemUtils;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ncpl.common.Constants;
import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Stock;

@Service
@EnableScheduling
public class StockEmailScheduler {
	@Autowired
	EmailService emailService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	StockService stockService;
	@Value("${stock.toEmail1}")
	String toEmail1;
	@Value("${stock.toEmail2}")
	String toEmail2;
	@Value("${stock.ccEmail1}")
	String ccEmail1;
	@Value("${stock.ccEmail2}")
	String ccEmail2;
	@Value("${stock.ccEmail3}")
	String ccEmail3;
	@Value("${stock.ccEmail4}")
	String ccEmail4;
	@Value("${stock.ccEmail5}")
	String ccEmail5;
	
	

	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "Current_Items_Stock.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	
	@SuppressWarnings("rawtypes")
	static List stockList = null;
	//send stock info daily by 6pm

	 //@Scheduled(cron = "0 0 10 * * ?", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void stockScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		
		    Date todayDate = new Date();  
		    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
		    formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		    String date= formatter.format(todayDate);  
		    System.out.println(date);  
		
		//String date = dateWithoutTime.toString();
		    boolean byEmail=true;
    	stockList = stockService.getStockHistoryByDate(date,byEmail);
    	try {
			DailyStockExcel.buildExcelDocument(stockList, filePath,itemMasterService);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
    	Map<String, Object> emailContents = null;
    	emailContents=stockInfoEmail();
    	emailService.sendEmailToServerForStockByCustomerEmail(emailContents);
	}


	//preparing contents for sending stock info report.
	public Map<String, Object> stockInfoEmail() {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Daily Stock Report"); 
		emailContents.put("template","stock-info.html"); 
		emailContents.put("to1", toEmail1);
		emailContents.put("to2", toEmail2);
		emailContents.put("cc1", ccEmail1);
		emailContents.put("cc2", ccEmail2);
		emailContents.put("cc3", ccEmail3);
		emailContents.put("cc4", ccEmail4);
		emailContents.put("cc5", ccEmail5);
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}

}

class DailyStockExcel{
	
	static FileNameGenerator fileNameGenerator = new FileNameGenerator();
	
	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	//ExcelLogoService logoService = new ExcelLogoService();
	
	@SuppressWarnings({ "unused", "rawtypes" })
	public static void buildExcelDocument(List stockList, String filePath, ItemMasterService itemMasterService) throws Exception {
		ExcelLogoService logoService = new ExcelLogoService();
        Workbook workbook = new XSSFWorkbook();
        Sheet itemsReportSheet = workbook.createSheet("Stock Report");
		itemsReportSheet.setDefaultColumnWidth(18);

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
				2, // first column
				5 // last column
		));
		header.createCell(1).setCellValue("Item Name");
		header.createCell(2).setCellValue("Old Value");
		itemsReportSheet.addMergedRegion(new CellRangeAddress(3, // first row
				3, // last row
				6, // first column
				9 // last column
		));
		header.createCell(3).setCellValue("");
		header.createCell(4).setCellValue("");
		header.createCell(5).setCellValue("");
		header.createCell(6).setCellValue("Changed Value");
		header.createCell(7).setCellValue("");
		header.createCell(8).setCellValue("");
		header.createCell(9).setCellValue("");
		header.createCell(10).setCellValue("Reason");
		header.createCell(11).setCellValue("Created");
		header.createCell(12).setCellValue("Updated");

		
		 Row header2 =  itemsReportSheet.createRow(4);
		 header2.createCell(0).setCellValue("");
		 header2.createCell(1).setCellValue("");
		 header2.createCell(2).setCellValue("Client Name");
		 header2.createCell(3).setCellValue("Quantity");
		 header2.createCell(4).setCellValue("Location");
		 header2.createCell(5).setCellValue("Store Name");
		 header2.createCell(6).setCellValue("Client Name");
		 header2.createCell(7).setCellValue("Quantity");
		 header2.createCell(8).setCellValue("Location");
		 header2.createCell(9).setCellValue("Store Name");
		 header2.createCell(10).setCellValue("");
		 header2.createCell(11).setCellValue("");
		 header2.createCell(12).setCellValue("");
		 short s = header.getLastCellNum();

		// Apply style to header
		for (int i = 0; i < header.getLastCellNum(); i++) {// For each cell in
															// the row
			header.getCell(i).setCellStyle(style);// Set the style
		}
		
		for (int i = 0; i < header2.getLastCellNum(); i++) {// For each cell in
			// the row
           header2.getCell(i).setCellStyle(style);// Set the style
             }
		

		populateStocksRecords(stockList, itemsReportSheet, workbook,itemMasterService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Stock Sheets Has been Created successfully!");
        
	}
	
	
	@SuppressWarnings("rawtypes")
	private static void populateStocksRecords(List stockList, Sheet itemsReportSheet, Workbook workbook, ItemMasterService itemMasterService) {
		
		int rowCount =5;
		for (int i = 0; i < stockList.size(); i++) {
			Map result = (Map) stockList.get(i);
			Stock oldStock = (Stock) result.get("oldStock");
			int stockSize= (int) result.get("stockSize");
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
			Optional<ItemMaster> itemmastetObj = itemMasterService.getItemById(id);
			String name = itemmastetObj.get().getItemName();
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
				
				Cell oldQtyCell = row.createCell(3);
				oldQtyCell.setCellStyle(declimalStyle);
				
				Cell newQtyCell = row.createCell(7);
				newQtyCell.setCellStyle(declimalStyle);
				newQtyCell.setCellValue(newquantity);
				
				row.createCell(0).setCellValue(description + " - " + modelNo);
				row.createCell(1).setCellValue(name);
				row.createCell(2).setCellValue(oldclientName);
				if(stockSize==1 && oldquantity == newquantity) {
					oldQtyCell.setCellValue(oldquantity);
				}else {
					oldQtyCell.setCellValue(oldquantity);
				}
				
				row.createCell(4).setCellValue(oldLocation);
				row.createCell(5).setCellValue(oldstoreName);
				row.createCell(6).setCellValue(newclientName);
				//row.createCell(7).setCellValue(newquantity);
				row.createCell(8).setCellValue(newLocation);
				row.createCell(9).setCellValue(newstoreName);
				row.createCell(10).setCellValue(reason);
				row.createCell(11).setCellValue(oldDate);
				row.createCell(12).setCellValue(newDate);
				
				
	}
}
	
	
	
}
