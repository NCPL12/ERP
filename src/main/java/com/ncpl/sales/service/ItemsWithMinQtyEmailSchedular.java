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
import com.ncpl.sales.model.ItemsWithMinQty;
import com.ncpl.sales.model.Stock;

@Service
@EnableScheduling
public class ItemsWithMinQtyEmailSchedular {
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
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "items_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	static List<ItemMaster> list = null;
	
	//@Scheduled(cron = "0 45 11 ? * MON", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		Date todayDate = new Date();  
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    formatter.setTimeZone(TimeZone.getTimeZone("IST"));
	    String date= formatter.format(todayDate);  
	    System.out.println(date);  	
	    list = salesService.getItemsWithMinQty();
		if(list.size()!=0) {
	    	try {
	    		ItemsWithMinQtyExcel.buildExcelDocument(list, filePath,itemMasterService,salesService,stockService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail();
	    	emailService.sendEmailToServer(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		Date created = new Date();
		String formattedDate1=formatter.format(created);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Items With Minimum Quantity "+formattedDate1); 
		emailContents.put("template","items_with_min_qty.html"); 
		emailContents.put("to1", "gopi@ncpl.co");
		emailContents.put("to2", "purchase@ncpl.co");
		emailContents.put("cc1", "design@ncpl.co");
		emailContents.put("cc2", "prasadini@ncpl.co");
		emailContents.put("cc3", "ramsy@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class ItemsWithMinQtyExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<ItemMaster> list, String filePath, ItemMasterService itemMasterService, SalesService salesService,StockService stockService) throws IOException {
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
		itemHeader.setCellValue("Model Number");
		
		Cell poNumCell = header.createCell(1);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Item Name");
		
		Cell modelCell = header.createCell(7);
		modelCell.setCellStyle(itemHeaderStyle);
		modelCell.setCellValue("Minimun Qty");
		
		Cell unitCell = header.createCell(8);
		unitCell.setCellStyle(itemHeaderStyle);
		unitCell.setCellValue("Unit");
		
		Cell qtyCell = header.createCell(9);
		qtyCell.setCellStyle(itemHeaderStyle);
		qtyCell.setCellValue("Current Qty");
		
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(list, editAccountSheet, workbook,itemMasterService,salesService,stockService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	@SuppressWarnings("rawtypes")
	private static void populateSalesRecords(List<ItemMaster> list, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService,StockService stockService) {
		
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
		
		CellStyle threeSideborderBold = workbook.createCellStyle();
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
		for (ItemMaster item : list) {
			
			Optional<ItemsWithMinQty> minQtyItem=itemMasterService.getItemsWithMinQtyById(item.getId());
			List<Stock> stockList = stockService.getStockList(item.getId());
			float stockQty=0;
			for (Stock stock : stockList) {
				stockQty=stockQty+stock.getQuantity();
			}
			
			String itemName = item.getItemName();
			String modelNum=item.getModel();
			String hsn=item.getHsnCode();
			String unit=item.getItem_units().getName();
			
			
			
			Row row = editAccountSheet.createRow(rowCount);
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					1, // first column
					6 // last column
			));
			
			Cell slNoCell = row.createCell(0);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(modelNum);
			
			Cell soNoCell = row.createCell(1);
			soNoCell.setCellStyle(threeSideborder);
			soNoCell.setCellValue(itemName);
			
			Cell hsnCell = row.createCell(7);
			hsnCell.setCellStyle(threeSideborder);
			hsnCell.setCellValue(minQtyItem.get().getQuantity());
			
			Cell unitCell = row.createCell(8);
			unitCell.setCellStyle(threeSideborder);
			unitCell.setCellValue(unit);
			
			Cell stockQtyCell = row.createCell(9);
			stockQtyCell.setCellStyle(threeSideborderRightAllign);
			stockQtyCell.setCellValue(stockQty);
			
			
			
			
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
