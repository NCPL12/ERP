package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.SalesItem;

@Service
@EnableScheduling
public class DcPartialItemsEmailSchedular {
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
	DeliveryChallanService dcService;
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "dc_items_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	static List<DeliveryChallanItems> dcItemList = null;
	
	//@Scheduled(cron = "0 15 10 * * ?", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		dcItemList = dcService.getPartialDcItems();
		if(dcItemList.size()!=0) {
	    	try {
	    		PartialDCExcel.buildExcelDocument(dcItemList, filePath,itemMasterService,salesService);
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
		emailContents.put("subject","Sales Items with Design and PO not done"); 
		emailContents.put("template","sales_items_with_design_and_po_notdone.html"); 
		emailContents.put("to1", "anitha@tek-nika.com");
		emailContents.put("to2", "anitha@tek-nika.com");
		emailContents.put("to3", "anitha@tek-nika.com");
		emailContents.put("cc1", "anitha@tek-nika.com");
		emailContents.put("cc2", "anitha@tek-nika.com");
		emailContents.put("cc3", "anitha@tek-nika.com");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class PartialDCExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<DeliveryChallanItems> dcItemList, String filePath, ItemMasterService itemMasterService, SalesService salesService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Sales Items");
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
				1, // first column
				5 // last column
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
		poNumCell.setCellValue("SO Description");
		
		Cell soNumCell = header.createCell(6);
		soNumCell.setCellStyle(itemHeaderStyle);
		soNumCell.setCellValue("SO Model No.");
		
		Cell createdDateCell = header.createCell(7);
		createdDateCell.setCellStyle(itemHeaderStyle);
		createdDateCell.setCellValue("Units");
		
		
		Cell descCell = header.createCell(8);
		descCell.setCellStyle(itemHeaderStyle);
		descCell.setCellValue("Total Qty");
		
		Cell modelCell = header.createCell(9);
		modelCell.setCellStyle(itemHeaderStyle);
		modelCell.setCellValue("Delivered Qty");
		
		Cell dlDateCell = header.createCell(10);
		dlDateCell.setCellStyle(itemHeaderStyle);
		dlDateCell.setCellValue("Todays Qty");
	
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(dcItemList, editAccountSheet, workbook,itemMasterService,salesService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	private static void populateSalesRecords(List<DeliveryChallanItems> dcItemList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		int rowCount =1;
		int slNo=1;
		//Collections.sort(salesItemsList);
		for (DeliveryChallanItems dcItem : dcItemList) {
			Optional<SalesItem> salesItem=salesService.getSalesItemObjById(dcItem.getDescription());
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
					1, // first column
					5 // last column
			));
			
			Cell slNoCell = row.createCell(0);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(slNo);
			
			Cell soNoCell = row.createCell(1);
			soNoCell.setCellStyle(threeSideborder);
			soNoCell.setCellValue(salesItem.get().getDescription());
			
			Cell poNoCell = row.createCell(6);
			poNoCell.setCellStyle(threeSideborder);
			poNoCell.setCellValue(salesItem.get().getModelNo());
			
			Cell crDtCell = row.createCell(7);
			crDtCell.setCellStyle(threeSideborder);
			crDtCell.setCellValue(salesItem.get().getItem_units().getName());
			
			Cell descCell = row.createCell(8);
			descCell.setCellStyle(threeSideborder);
			descCell.setCellValue(dcItem.getTotalQuantity());
			
			Cell modelCell = row.createCell(9);
			modelCell.setCellStyle(threeSideborderRightAllign);
			modelCell.setCellValue(dcItem.getDeliveredQuantity());
			
			Cell dlDtCell = row.createCell(10);
			dlDtCell.setCellStyle(threeSideborder);
			dlDtCell.setCellValue(dcItem.getTodaysQty());
			
			
			
			slNo++;
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
