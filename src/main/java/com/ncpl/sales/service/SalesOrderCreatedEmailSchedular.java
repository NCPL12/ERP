package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ncpl.common.Constants;
import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.SalesOrder;

@Service
@EnableScheduling
public class SalesOrderCreatedEmailSchedular {
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
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "sales_order_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	static List<SalesOrder> salesList = null;
	
	//@Scheduled(cron = "0 15 09 * * ?", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		Date todayDate = new Date();  
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    formatter.setTimeZone(TimeZone.getTimeZone("IST"));
	    String date= formatter.format(todayDate);  
	    System.out.println(date);  	
		salesList = salesService.getAllSoCreatedToday(date);
		if(salesList.size()!=0) {
	    	try {
	    		SalesOrderCreatedExcel.buildExcelDocument(salesList, filePath,itemMasterService,salesService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail();
	    	emailService.sendSOEmailToServer(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		Date created = new Date();
		String formattedDate1=formatter.format(created);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Sales Order Created dated "+formattedDate1); 
		emailContents.put("template","so_created.html"); 
		emailContents.put("to1", "prasadini@ncpl.co");
		emailContents.put("to2", "ramsy@ncpl.co");
		emailContents.put("to3", "surendra@ncpl.co");
		emailContents.put("to4", "prashanth@ncpl.co");
		emailContents.put("to5", "design@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class SalesOrderCreatedExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<SalesOrder> salesList, String filePath, ItemMasterService itemMasterService, SalesService salesService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Sales Order");
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
		itemHeader.setCellValue("SO Number");
		
		Cell poNumCell = header.createCell(1);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Client PO No.");
		
		Cell soNumCell = header.createCell(2);
		soNumCell.setCellStyle(itemHeaderStyle);
		soNumCell.setCellValue("Client PO Date");
		
		Cell createdDateCell = header.createCell(3);
		createdDateCell.setCellStyle(itemHeaderStyle);
		createdDateCell.setCellValue("Created Date");
		
		
		Cell descCell = header.createCell(4);
		descCell.setCellStyle(itemHeaderStyle);
		descCell.setCellValue("Client Name");
		
		Cell modelCell = header.createCell(5);
		modelCell.setCellStyle(itemHeaderStyle);
		modelCell.setCellValue("Client PO Value (without GST)");
		
		Cell gstCell = header.createCell(6);
		gstCell.setCellStyle(itemHeaderStyle);
		gstCell.setCellValue("GST Amount");
		
		Cell totalClientPOCell = header.createCell(7);
		totalClientPOCell.setCellStyle(itemHeaderStyle);
		totalClientPOCell.setCellValue("Client Total PO Value");
		
		Cell updatedDateCell = header.createCell(8);
		updatedDateCell.setCellStyle(itemHeaderStyle);
		updatedDateCell.setCellValue("Updated Date");
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(salesList, editAccountSheet, workbook,itemMasterService,salesService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	private static void populateSalesRecords(List<SalesOrder> salesList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		int rowCount =1;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		//Collections.sort(salesItemsList);
		for (SalesOrder so : salesList) {
			Date created = so.getCreated();
			String dateString = created.toString();
			String[] arr = dateString.split(" ");
			
			Date created2 = so.getUpdated();
			String dateString2 = created2.toString();
			String[] arr2 = dateString2.split(" ");

			// Formatting date to a required format
			String formattedDate = arr[0];
			formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
					+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
					+ formattedDate.substring(0, 4);
			
			String formattedDate2 = arr2[0];
			formattedDate2 = formattedDate2.substring(formattedDate2.length() - 2, formattedDate2.length()) + "-"
					+ formattedDate2.substring(formattedDate2.length() - 5, formattedDate2.length() - 3) + "-"
					+ formattedDate2.substring(0, 4);
			
			//String description=salesItem.getDescription();
			
			
			String clientName = so.getParty().getPartyName();
			String  clientPoNumber=so.getClientPoNumber();
			Date clientPoDate=so.getClientPoDate();
			System.out.println(clientPoDate);
			//String dateString1 = clientPoDate.toString();
			String formattedDate1;
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
			threeSideborderRightAllign.setWrapText(true);
			threeSideborderRightAllign.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
			threeSideborderRightAllign.setBorderLeft(BORDER_THIN);
			threeSideborderRightAllign.setBorderRight(BORDER_THIN);
			threeSideborderRightAllign.setBorderBottom(BORDER_THIN);
			threeSideborderRightAllign.setBorderTop(BORDER_THIN);
			threeSideborderRightAllign.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			threeSideborderRightAllign.setFont(itemListFont);

			
			Row row = editAccountSheet.createRow(rowCount);
			
			Cell slNoCell = row.createCell(0);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(so.getId());
			
			Cell soNoCell = row.createCell(1);
			soNoCell.setCellStyle(threeSideborder);
			soNoCell.setCellValue(clientPoNumber);
			
			
			Cell poNoCell = row.createCell(2);
			poNoCell.setCellStyle(threeSideborder);
			
			if(clientPoDate!=null) {
				 formattedDate1 = formatter.format(clientPoDate);
				 poNoCell.setCellValue(formattedDate1);
			}else {
				 poNoCell.setCellValue("");

			}
			
			
			Cell crDtCell = row.createCell(3);
			crDtCell.setCellStyle(threeSideborder);
			crDtCell.setCellValue(formattedDate);
			
			Cell descCell = row.createCell(4);
			descCell.setCellStyle(threeSideborder);
			descCell.setCellValue(clientName);
			
			Cell modelCell = row.createCell(5);
			modelCell.setCellStyle(threeSideborderRightAllign);
			modelCell.setCellValue(so.getGrandTotal());
			
			Cell gstCell = row.createCell(6);
			gstCell.setCellStyle(threeSideborderRightAllign);
			gstCell.setCellValue(so.getGst());
			
			Cell totalPOcell = row.createCell(7);
			totalPOcell.setCellStyle(threeSideborderRightAllign);
			totalPOcell.setCellValue(so.getGrandTotal()-so.getGst());
			
			Cell updatedDateCell = row.createCell(8);
			updatedDateCell.setCellStyle(threeSideborder);
			updatedDateCell.setCellValue(formattedDate2);
			
			
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
}

