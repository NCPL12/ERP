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
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.SalesOrder;

@Service
@EnableScheduling
public class ItemMasterEmailSchedular {

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
	String fileName = fileNameGenerator.generateFileNameAsDate() + "item_list_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	static List<ItemMaster> itemList = null;
	
	//@Scheduled(cron = "0 0 12 * * ?", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		Date todayDate = new Date();  
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    formatter.setTimeZone(TimeZone.getTimeZone("IST"));
	    String date= formatter.format(todayDate);  
	    System.out.println(date);  
		
		itemList = itemMasterService.getItemsByDate(date);
		if(itemList.size()!=0) {
	    	try {
	    		ItemListByDateExcel.buildExcelDocument(itemList, filePath,itemMasterService,salesService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail();
	    	emailService.sendItemAddedEmailToServer(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail() {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Items Created"); 
		emailContents.put("template","items_created.html");
		emailContents.put("to1", "store@ncpl.co");
		emailContents.put("to2", "purchase@ncpl.co");
		emailContents.put("to3", "design@ncpl.co");
		emailContents.put("cc1", "ramsy@ncpl.co");
		emailContents.put("cc2", "prasadini@ncpl.co");
		emailContents.put("cc3", "surendra@ncpl.co");
		emailContents.put("cc4", "prashanth@ncpl.co");
		emailContents.put("cc5", "vighneshwar@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class ItemListByDateExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<ItemMaster> itemList, String filePath, ItemMasterService itemMasterService, SalesService salesService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Item List");
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
				6 // last column
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
		itemHeader.setCellValue("Model No.");
		
		Cell poNumCell = header.createCell(1);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Item Description");
		
		/*Cell soNumCell = header.createCell(7);
		soNumCell.setCellStyle(itemHeaderStyle);
		soNumCell.setCellValue("Created By");*/
		
		Cell createdDateCell = header.createCell(7);
		createdDateCell.setCellStyle(itemHeaderStyle);
		createdDateCell.setCellValue("Created Date");
		
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(itemList, editAccountSheet, workbook,itemMasterService,salesService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Item List By Date Report Sheets Has been Created successfully!");
	}
	
	private static void populateSalesRecords(List<ItemMaster> itemList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		int rowCount =1;
		//Collections.sort(salesItemsList);
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		for (ItemMaster item : itemList) {
			Date created = item.getCreated();
			String formattedDate1=formatter.format(created);
			String dateString = created.toString();
			String[] arr = dateString.split(" ");

			// Formatting date to a required format
			String formattedDate = arr[0];
			formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
					+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
					+ formattedDate.substring(0, 4);
			
			
			
			//String description=salesItem.getDescription();
			
			
			String modelNo = item.getModel();
			String  description=item.getItemName();
			String createdBy=item.getCreatedBy();
			
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

			
			Row row = editAccountSheet.createRow(rowCount);
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					1, // first column
					6 // last column
			));
			
			Cell slNoCell = row.createCell(0);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(modelNo);
			
			Cell soNoCell = row.createCell(1);
			soNoCell.setCellStyle(threeSideborder);
			soNoCell.setCellValue(description);
			
			/*Cell poNoCell = row.createCell(7);
			poNoCell.setCellStyle(threeSideborder);
			poNoCell.setCellValue(createdBy);*/
			
			Cell crDtCell = row.createCell(7);
			crDtCell.setCellStyle(threeSideborder);
			crDtCell.setCellValue(formattedDate1);
			
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

