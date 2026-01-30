package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
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
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.SalesOrder;

@Service
@EnableScheduling
public class SalesOrderWithoutDesignByCustomerExcel {
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
	
	
	
	
	static List<Party> partyListBan = null;
	static List<Party> partyListMan = null;
	
	@Scheduled(cron = "0 45 09 * * ?", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		partyListBan = salesService.getAllSalesOrderWithoutDesignForEmailBan();
		partyListMan = salesService.getAllSalesOrderWithoutDesignForEmailMan();
		
		SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
		formatter2.setTimeZone(TimeZone.getTimeZone("IST"));
		Date date = new Date();
		String formattedDate3=formatter2.format(date);
		
		String fileName = "so_without_design_dated-"+ formattedDate3 + ".xlsx";
		String filePath = Constants.FILE_LOCATION + File.separator + fileName;
		
		if(partyListBan.size()!=0 || partyListMan.size()!=0) {
	    	try {
	    		SalesOrderWithNoDesignByCostomerExcel.buildExcelDocument(partyListBan,partyListMan, filePath,itemMasterService,salesService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail(filePath);
	    	//emailService.sendEmailToServerForStockByCustomerEmail(emailContents);
	    	emailService.sendEmailToServer(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail(String filePath) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		Date created = new Date();
		String formattedDate1=formatter.format(created);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","SO without design dated "+formattedDate1); 
		emailContents.put("template","sales_items_without_design.html"); 
		emailContents.put("to1", "quotes@ncpl.co");
		emailContents.put("to2", "ramsy@ncpl.co");
		emailContents.put("cc1", "design@ncpl.co");
		//emailContents.put("cc2", "abhilash@ncpl.co");
		emailContents.put("cc2", "prasadini@ncpl.co");
		emailContents.put("cc3", "ashwini@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class SalesOrderWithNoDesignByCostomerExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<Party> partyListBan,List<Party> partyListMan, String filePath, ItemMasterService itemMasterService, SalesService salesService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("SO Without design Bangalore");
        Sheet editAccountSheet1 = workbook.createSheet("SO Without design Mangalore");
        editAccountSheet.setDefaultColumnWidth(11);
        editAccountSheet1.setDefaultColumnWidth(11);
		
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");

		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);
		
		Font fontColumn = workbook.createFont();
		fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumn.setFontHeightInPoints((short)12);
		
		
		Row header = editAccountSheet.createRow(0);
		Row header1 = editAccountSheet1.createRow(0);
		
		
		CellStyle itemHeaderStyle = workbook.createCellStyle();
		itemHeaderStyle.setWrapText(true);
		itemHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		itemHeaderStyle.setFont(fontColumn);
		itemHeaderStyle.setBorderBottom(BORDER_THIN);
		itemHeaderStyle.setBorderTop(BORDER_THIN);
		itemHeaderStyle.setBorderRight(BORDER_THIN);
		itemHeaderStyle.setBorderLeft(BORDER_THIN);
		
		Cell slNum = header.createCell(0);
		slNum.setCellStyle(itemHeaderStyle);
		slNum.setCellValue("Sl.No");
		
		Cell clientName = header.createCell(1);
		clientName.setCellStyle(itemHeaderStyle);
		clientName.setCellValue("Client Name");
		
		Cell itemHeader = header.createCell(2);
		itemHeader.setCellStyle(itemHeaderStyle);
		itemHeader.setCellValue("SO Number");
		
		Cell poNumCell = header.createCell(3);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Client PO No.");
		
		Cell soNumCell = header.createCell(4);
		soNumCell.setCellStyle(itemHeaderStyle);
		soNumCell.setCellValue("Client PO Date");
		
		Cell createdDateCell = header.createCell(5);
		createdDateCell.setCellStyle(itemHeaderStyle);
		createdDateCell.setCellValue("Created Date");
	
		
		Cell modelCell = header.createCell(6);
		modelCell.setCellStyle(itemHeaderStyle);
		modelCell.setCellValue("Client PO Value");
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(partyListBan, editAccountSheet, workbook,itemMasterService,salesService);
		
		Cell slNum1 = header1.createCell(0);
		slNum1.setCellStyle(itemHeaderStyle);
		slNum1.setCellValue("Sl.No");
       
		
		Cell clientName1 = header1.createCell(1);
		clientName1.setCellStyle(itemHeaderStyle);
		clientName1.setCellValue("Client Name");
		
		Cell itemHeader1 = header1.createCell(2);
		itemHeader1.setCellStyle(itemHeaderStyle);
		itemHeader1.setCellValue("SO Number");
		
		Cell poNumCell1 = header1.createCell(3);
		poNumCell1.setCellStyle(itemHeaderStyle);
		poNumCell1.setCellValue("Client PO No.");
		
		Cell soNumCell1 = header1.createCell(4);
		soNumCell1.setCellStyle(itemHeaderStyle);
		soNumCell1.setCellValue("Client PO Date");
		
		Cell createdDateCell1 = header1.createCell(5);
		createdDateCell1.setCellStyle(itemHeaderStyle);
		createdDateCell1.setCellValue("Created Date");
	
		
		Cell modelCell1 = header1.createCell(6);
		modelCell1.setCellStyle(itemHeaderStyle);
		modelCell1.setCellValue("Client PO Value");
		
		setBordersToMergedCells(workbook, editAccountSheet1, rowLastItemCount);
		populateSalesRecords1(partyListMan, editAccountSheet1, workbook,itemMasterService,salesService);
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	private static void populateSalesRecords(List<Party> partyList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		int rowCount =1;
		int slNumber=1;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		
		
		CellStyle threeSideborder = workbook.createCellStyle();
		threeSideborder.setWrapText(true);
		threeSideborder.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborder.setBorderLeft(BORDER_THIN);
		threeSideborder.setBorderRight(BORDER_THIN);
		threeSideborder.setBorderBottom(BORDER_THIN);
		threeSideborder.setBorderTop(BORDER_THIN);
		threeSideborder.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font itemListFont = workbook.createFont();
		itemListFont.setFontHeightInPoints((short)10);
		threeSideborder.setFont(itemListFont);
		
		
		
		CellStyle threeSideborderBold = workbook.createCellStyle();
		threeSideborderBold.setWrapText(true);
		threeSideborderBold.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderBold.setBorderLeft(BORDER_THIN);
		threeSideborderBold.setBorderRight(BORDER_THIN);
		threeSideborderBold.setBorderBottom(BORDER_THIN);
		threeSideborderBold.setBorderTop(BORDER_THIN);
		threeSideborderBold.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font fontColumns = workbook.createFont();
		fontColumns.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumns.setFontHeightInPoints((short)10);
		threeSideborderBold.setFont(fontColumns);
		
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
		
		for (Party party : partyList) {
			Row row = editAccountSheet.createRow(rowCount);
			
			Cell slNumberCell = row.createCell(0);
			slNumberCell.setCellStyle(threeSideborder);
			slNumberCell.setCellValue(slNumber);
			
			
			Cell clientNameCell = row.createCell(1);
			clientNameCell.setCellStyle(threeSideborder);
			clientNameCell.setCellValue(party.getPartyName());
			
			List<SalesOrder> salesOrderList = salesService.getSalesOrderWithoutDesignByPartyIdBan(party.getId());
			Collections.sort(salesOrderList);
			Row salesRow;
			for (SalesOrder so : salesOrderList) {
			Date created = so.getCreated();
			String dateString = created.toString();
			String[] arr = dateString.split(" ");

			// Formatting date to a required format
			String formattedDate = arr[0];
			formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
					+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
					+ formattedDate.substring(0, 4);
			
			
			
			//String description=salesItem.getDescription();
			
			
			String  clientPoNumber=so.getClientPoNumber();
			Date clientPoDate=so.getClientPoDate();
			System.out.println(clientPoDate);
			//String dateString1 = clientPoDate.toString();
			String formattedDate1;
			
			
			
			Cell slNoCell = row.createCell(2);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(so.getId());
			
			Cell soNoCell = row.createCell(3);
			soNoCell.setCellStyle(threeSideborder);
			soNoCell.setCellValue(clientPoNumber);
			
			Cell poNoCell = row.createCell(4);
			poNoCell.setCellStyle(threeSideborder);
			
			if(clientPoDate!=null) {
				 formattedDate1 = formatter.format(clientPoDate);
				 poNoCell.setCellValue(formattedDate1);
			}else {
				 poNoCell.setCellValue("");

			}
			Cell crDtCell = row.createCell(5);
			crDtCell.setCellStyle(threeSideborder);
			crDtCell.setCellValue(formattedDate);
			
			Cell modelCell = row.createCell(6);
			modelCell.setCellStyle(threeSideborderRightAllign);
			modelCell.setCellValue(so.getGrandTotal());
			
			
			if(so!=salesOrderList.get(salesOrderList.size()-1)) {
				rowCount++;
				salesRow=editAccountSheet.createRow(rowCount);
				row=salesRow;
				}
			}
			rowCount++;
			slNumber++;
		}
		int rowLastItemCount=rowCount;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		setBorders(workbook, editAccountSheet, rowLastItemCount);
	}
	
	private static void populateSalesRecords1(List<Party> partyList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		int rowCount =1;
		int slNumber=1;
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		
		
		CellStyle threeSideborder = workbook.createCellStyle();
		threeSideborder.setWrapText(true);
		threeSideborder.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborder.setBorderLeft(BORDER_THIN);
		threeSideborder.setBorderRight(BORDER_THIN);
		threeSideborder.setBorderBottom(BORDER_THIN);
		threeSideborder.setBorderTop(BORDER_THIN);
		threeSideborder.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font itemListFont = workbook.createFont();
		itemListFont.setFontHeightInPoints((short)10);
		threeSideborder.setFont(itemListFont);
		
		
		
		CellStyle threeSideborderBold = workbook.createCellStyle();
		threeSideborderBold.setWrapText(true);
		threeSideborderBold.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderBold.setBorderLeft(BORDER_THIN);
		threeSideborderBold.setBorderRight(BORDER_THIN);
		threeSideborderBold.setBorderBottom(BORDER_THIN);
		threeSideborderBold.setBorderTop(BORDER_THIN);
		threeSideborderBold.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font fontColumns = workbook.createFont();
		fontColumns.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumns.setFontHeightInPoints((short)10);
		threeSideborderBold.setFont(fontColumns);
		
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
		
		for (Party party : partyList) {
			Row row = editAccountSheet.createRow(rowCount);
			
			Cell slNumberCell = row.createCell(0);
			slNumberCell.setCellStyle(threeSideborder);
			slNumberCell.setCellValue(slNumber);
			
			Cell clientNameCell = row.createCell(1);
			clientNameCell.setCellStyle(threeSideborder);
			clientNameCell.setCellValue(party.getPartyName());
			
			List<SalesOrder> salesOrderList = salesService.getSalesOrderWithoutDesignByPartyIdMan(party.getId());
			Collections.sort(salesOrderList);
			Row salesRow;
			for (SalesOrder so : salesOrderList) {
			Date created = so.getCreated();
			String dateString = created.toString();
			String[] arr = dateString.split(" ");

			// Formatting date to a required format
			String formattedDate = arr[0];
			formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
					+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
					+ formattedDate.substring(0, 4);
			
			
			
			//String description=salesItem.getDescription();
			
			
			String  clientPoNumber=so.getClientPoNumber();
			Date clientPoDate=so.getClientPoDate();
			System.out.println(clientPoDate);
			//String dateString1 = clientPoDate.toString();
			String formattedDate1;
			if(clientPoDate!=null) {
				 formattedDate1 = formatter.format(clientPoDate);
			}else {
				 formattedDate1 = formatter.format(new Date());

			}
			
			
			Cell slNoCell = row.createCell(2);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(so.getId());
			
			Cell soNoCell = row.createCell(3);
			soNoCell.setCellStyle(threeSideborder);
			soNoCell.setCellValue(clientPoNumber);
			
			Cell poNoCell = row.createCell(4);
			poNoCell.setCellStyle(threeSideborder);
			poNoCell.setCellValue(formattedDate1);
			
			Cell crDtCell = row.createCell(5);
			crDtCell.setCellStyle(threeSideborder);
			crDtCell.setCellValue(formattedDate);
			
			Cell modelCell = row.createCell(6);
			modelCell.setCellStyle(threeSideborderRightAllign);
			modelCell.setCellValue(so.getGrandTotal());
			
			
			if(so!=salesOrderList.get(salesOrderList.size()-1)) {
				rowCount++;
				salesRow=editAccountSheet.createRow(rowCount);
				row=salesRow;
				}
			}
			rowCount++;
			slNumber++;
		}
		int rowLastItemCount=rowCount;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		setBorders(workbook, editAccountSheet, rowLastItemCount);
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
	
	private static void setBorders(Workbook workBook, Sheet sheet, int rowLastItemCount) {
		/*CellRangeAddress range = new CellRangeAddress(11,rowLastItemCount-1,0,11);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, range, sheet, workBook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workBook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range, sheet, workBook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, range, sheet, workBook);*/
		for (int i=1;i<rowLastItemCount;i++) {
			for (int j = 0; j <= 6; j++) {
				
			
				CellRangeAddress region = new CellRangeAddress(i,rowLastItemCount-1,j,6);
				
				RegionUtil.setBorderTop(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, region, sheet, workBook);
			}
		}
		
	}
}
