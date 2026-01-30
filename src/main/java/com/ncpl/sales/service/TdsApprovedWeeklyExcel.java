package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.TdsItems;

@Service
@EnableScheduling
public class TdsApprovedWeeklyExcel {
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
	String fileName = fileNameGenerator.generateFileNameAsDate() + "so_items_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	static List<TdsItems> tdsItemList = null;
	
	@Scheduled(cron = "0 30 09 ? * MON", zone="IST")
	//@Scheduled(cron = "0 51 17 ? * TUE", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		tdsItemList = tdsService.getTdsItemsListWhereTdsApprovedAndPoNotDone();
		if(tdsItemList.size()!=0) {
	    	try {
	    		WeeklyTdsApprovedExcel.buildExcelDocument(tdsItemList, filePath,itemMasterService,salesService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail();
	    	emailService.sendSOWithDesignwherePONotDoneEmailToServer(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail() {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Tds Approved Items"); 
		emailContents.put("template","tds_approved_items.html"); 
		emailContents.put("to1", "prasadini@ncpl.co");
		emailContents.put("to2", "ramsy@ncpl.co");
		emailContents.put("to3", "design@ncpl.co");
		emailContents.put("cc1", "purchase@ncpl.co");
		emailContents.put("cc2", "surendra@ncpl.co");
		emailContents.put("cc3", "vighneshwar@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class WeeklyTdsApprovedExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<TdsItems> tdsItemList, String filePath, ItemMasterService itemMasterService, SalesService salesService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Items");
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
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(0, // first row
				0, // last row
				3, // first column
				7 // last column
		));
		
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
		itemHeader.setCellValue("Sl No");
		
		Cell poNumCell = header.createCell(1);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Client PO No.");
		
		Cell createdDateCell = header.createCell(2);
		createdDateCell.setCellStyle(itemHeaderStyle);
		createdDateCell.setCellValue("Created Date");
		
		
		Cell descCell = header.createCell(3);
		descCell.setCellStyle(itemHeaderStyle);
		descCell.setCellValue("Description");
		
		Cell modelCell = header.createCell(8);
		modelCell.setCellStyle(itemHeaderStyle);
		modelCell.setCellValue("Model No.");
		
		Cell dlDateCell = header.createCell(9);
		dlDateCell.setCellStyle(itemHeaderStyle);
		dlDateCell.setCellValue("Site Qty");
		
		Cell vendorCell = header.createCell(10);
		vendorCell.setCellStyle(itemHeaderStyle);
		vendorCell.setCellValue("Client Name");
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(tdsItemList, editAccountSheet, workbook,itemMasterService,salesService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	private static void populateSalesRecords(List<TdsItems> tdsItemList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		int rowCount =1;
		for (TdsItems tdsItem : tdsItemList) {
			String salesItemId = tdsItem.getDescription();
			Optional<SalesItem> salesItemObj=salesService.getSalesItemObjById(salesItemId);
			Optional<ItemMaster> itemObj = itemMasterService.getItemById(tdsItem.getModelNumber());
			String itemName =  itemObj.get().getModel();
			Date created = salesItemObj.get().getSalesOrder().getCreated();
			String dateString = created.toString();
			String[] arr = dateString.split(" ");

			// Formatting date to a required format
			String formattedDate = arr[0];
			formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
					+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
					+ formattedDate.substring(0, 4);
			
			
			
			String description=salesItemObj.get().getDescription();
			
			
			String clientName = salesItemObj.get().getSalesOrder().getParty().getPartyName();
			
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
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					3, // first column
					7 // last column
			));
			
			Cell slNoCell = row.createCell(0);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(salesItemObj.get().getSlNo());
			
			Cell poNoCell = row.createCell(1);
			poNoCell.setCellStyle(threeSideborder);
			poNoCell.setCellValue(salesItemObj.get().getSalesOrder().getClientPoNumber());
			
			Cell crDtCell = row.createCell(2);
			crDtCell.setCellStyle(threeSideborder);
			crDtCell.setCellValue(formattedDate);
			
			Cell descCell = row.createCell(3);
			descCell.setCellStyle(threeSideborder);
			descCell.setCellValue(description);
			
			Cell modelCell = row.createCell(8);
			modelCell.setCellStyle(threeSideborder);
			modelCell.setCellValue(itemName);
			
			Cell dlDtCell = row.createCell(9);
			dlDtCell.setCellStyle(threeSideborderRightAllign);
			dlDtCell.setCellValue(tdsItem.getSiteQuantity());
			
			Cell vendorCell = row.createCell(10);
			vendorCell.setCellStyle(threeSideborder);
			vendorCell.setCellValue(clientName);
			
			
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
