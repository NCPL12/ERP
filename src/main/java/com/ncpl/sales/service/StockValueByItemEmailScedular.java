package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ncpl.common.Constants;
import com.ncpl.sales.config.LangConfig;

@Service
@EnableScheduling
public class StockValueByItemEmailScedular {
	@Autowired
	EmailService emailService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	PurchaseOrderService purchaseService;
	@Autowired
	SalesService salesService;
	@Autowired
	TdsService tdsService;
	@Autowired
	StockService stockService;
	
	
	
	
	
	static List list = null;
	
	//@Scheduled(cron = "0 15 12 ? * MON", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		Date todayDate = new Date();  
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    formatter.setTimeZone(TimeZone.getTimeZone("IST"));
	    String date= formatter.format(todayDate);  
	    
	    SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
	    formatter2.setTimeZone(TimeZone.getTimeZone("IST"));
		Date filedate = new Date();
		String formattedDate3=formatter2.format(filedate);
	    
	    String fileName = "stock_itemwise_as_on-"+formattedDate3 + ".xlsx";
		String filePath = Constants.FILE_LOCATION + File.separator + fileName;
		
	    System.out.println(date);  	
	    list = stockService.getStockValueForAllItems();
		if(list.size()!=0) {
	    	try {
	    		StockListByItemExcel.buildExcelDocument(list, filePath,itemMasterService,salesService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail(filePath);
	    	emailService.sendEmailToServerForStockByItems(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail(String filePath) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		Date created = new Date();
		String formattedDate1=formatter.format(created);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Stock By Items "+formattedDate1); 
		emailContents.put("template","stock_by_items.html"); 
		emailContents.put("to1", "surendra@ncpl.co");
		emailContents.put("cc1", "aparna@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class StockListByItemExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List list, String filePath, ItemMasterService itemMasterService, SalesService salesService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Stock");
        editAccountSheet.setDefaultColumnWidth(11);
		
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");

		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);
		
		Font fontColumn = workbook.createFont();
		fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumn.setFontHeightInPoints((short)10);
		
		
		Row header = editAccountSheet.createRow(0);
		
		
		CellStyle itemHeaderStyle = workbook.createCellStyle();
		itemHeaderStyle.setWrapText(true);
		itemHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		itemHeaderStyle.setFont(fontColumn);
		itemHeaderStyle.setBorderBottom(BORDER_THIN);
		itemHeaderStyle.setBorderTop(BORDER_THIN);
		itemHeaderStyle.setBorderRight(BORDER_THIN);
		itemHeaderStyle.setBorderLeft(BORDER_THIN);
		
		
		Cell itemHeader = header.createCell(0);
		itemHeader.setCellStyle(itemHeaderStyle);
		itemHeader.setCellValue("Model No");
		
		Cell poNumCell = header.createCell(1);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Stock Qty");
		
		/*Cell priceCell = header.createCell(2);
		priceCell.setCellStyle(itemHeaderStyle);
		priceCell.setCellValue("price");*/
		
		Cell valueCell = header.createCell(2);
		valueCell.setCellStyle(itemHeaderStyle);
		valueCell.setCellValue("Avg Stock Value");
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(list, editAccountSheet, workbook,itemMasterService,salesService);
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	@SuppressWarnings("rawtypes")
	private static void populateSalesRecords(List list, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		
		CellStyle threeSideborder = workbook.createCellStyle();
		threeSideborder.setWrapText(true);
		threeSideborder.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborder.setBorderLeft(BORDER_THIN);
		threeSideborder.setBorderRight(BORDER_THIN);
		threeSideborder.setBorderBottom(BORDER_THIN);
		threeSideborder.setBorderTop(BORDER_THIN);
		threeSideborder.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font itemListFont = workbook.createFont();
		itemListFont.setFontHeightInPoints((short)8);
		threeSideborder.setFont(itemListFont);
		
		CellStyle threeSideborderRightAllign = workbook.createCellStyle();
		XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRightAllign.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
		threeSideborderRightAllign.setWrapText(true);
		threeSideborderRightAllign.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderRightAllign.setBorderLeft(BORDER_THIN);
		threeSideborderRightAllign.setBorderRight(BORDER_THIN);
		threeSideborderRightAllign.setBorderBottom(BORDER_THIN);
		threeSideborderRightAllign.setBorderTop(BORDER_THIN);
		threeSideborderRightAllign.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRightAllign.setFont(itemListFont);
		
		CellStyle threeSideborderBold = workbook.createCellStyle();
		threeSideborderBold.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
		threeSideborderBold.setWrapText(true);
		threeSideborderBold.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderBold.setBorderLeft(BORDER_THIN);
		threeSideborderBold.setBorderRight(BORDER_THIN);
		threeSideborderBold.setBorderBottom(BORDER_THIN);
		threeSideborderBold.setBorderTop(BORDER_THIN);
		threeSideborderBold.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		Font fontColumns = workbook.createFont();
		fontColumns.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumns.setFontHeightInPoints((short)8);
		threeSideborderBold.setFont(fontColumns);

		int rowCount =1;
		for (int i=0;i<list.size()-1;i++) {
			
			
			String objectString = list.get(i).toString().split("=")[0];
			String valueString=list.get(i).toString().split("=")[1];
			String qty=objectString.split("qty&")[1];
			String itemName=objectString.split("qty&")[0];
			//String itemName=objectString.split("&Qty")[0];
			//String priceString=objectString.split("unitPrice$")[1];
			//String valueString=list.get(i).toString().split("=")[1];
			
			float value=Float.parseFloat(valueString);
			//float price=Float.parseFloat(priceString);
			float quantity=Float.parseFloat(qty);
			
			Row row = editAccountSheet.createRow(rowCount);
			
			Cell slNoCell = row.createCell(0);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(itemName);
			
			Cell soNoCell = row.createCell(1);
			soNoCell.setCellStyle(threeSideborderRightAllign);
			soNoCell.setCellValue(quantity);
			
			/*Cell priceCell = row.createCell(2);
			priceCell.setCellStyle(threeSideborderRightAllign);
			priceCell.setCellValue(price);*/
			
			Cell valueCell = row.createCell(2);
			valueCell.setCellStyle(threeSideborderRightAllign);
			valueCell.setCellValue(value);
			
			
			
			
			rowCount++;
			
			
		}
		
		
		
		int rowLastItemCount=rowCount;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
	}
	
	private static void setBordersToMergedCells(Workbook workBook, Sheet sheet, int rowLastItemCount) {
		int numMerged = sheet.getNumMergedRegions();
		for (int i = 0; i < numMerged; i++) {
			CellRangeAddress mergedRegions = sheet.getMergedRegion(i);

//			if (mergedRegions.getFirstRow() == 18
//					|| (mergedRegions.getFirstRow() < rowLastItemCount && mergedRegions.getFirstRow() > 18)) {
//
//			} else {
				// RegionUtil.setRightBorderColor(IndexedColors.WHITE.getIndex(), mergedRegions,
				// sheet, workBook);
				RegionUtil.setBorderTop(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
				RegionUtil.setBorderRight(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
				RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			//}
		}
	}
	
	private static String formatLakh(double d) {
	    String s = String.format(Locale.UK, "%1.2f", Math.abs(d));
	    s = s.replaceAll("(.+)(...\\...)", "$1,$2");
	    while (s.matches("\\d{3,},.+")) {
	        s = s.replaceAll("(\\d+)(\\d{2},.+)", "$1,$2");
	    }
	    return d < 0 ? ("-" + s) : s;
	}
}
