package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;

@Service
@EnableScheduling
public class ActiveSalesOrderWithItremsExcel {
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
	PurchaseItemService purchaseItemService;
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "active_so_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	static List<Party> partyList = null;
	
	@Scheduled(cron = "0 45 10 ? * MON", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		partyList = salesService.getActiveSalesOrderWithoutPO();
		if(partyList.size()!=0) {
	    	try {
	    		ActiveSOWithItremsExcel.buildExcelDocument(partyList, filePath,itemMasterService,salesService,purchaseItemService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail();
	    	emailService.sendEmailToServer(emailContents);
	    	//emailService.sendEmailToServerForActiveSO(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail() {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Active Sales Order"); 
		emailContents.put("template","active_sales_order.html"); 
		emailContents.put("to1", "prasadini@ncpl.co");
		emailContents.put("to2", "ramsy@ncpl.co");
		emailContents.put("cc1", "design@ncpl.co");
		emailContents.put("cc2", "vighneshwar@ncpl.co");
		emailContents.put("cc2", "quotes@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class ActiveSOWithItremsExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<Party> partyList, String filePath, ItemMasterService itemMasterService, SalesService salesService,PurchaseItemService purchaseItemService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Active SO");
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
		
		Cell clientNameHeader = header.createCell(0);
		clientNameHeader.setCellStyle(itemHeaderStyle);
		clientNameHeader.setCellValue("Client Name");
		
		Cell itemHeader = header.createCell(1);
		itemHeader.setCellStyle(itemHeaderStyle);
		itemHeader.setCellValue("SO Number");
		
		Cell poNumCell = header.createCell(2);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("SO Date");
		
		Cell soNumCell = header.createCell(3);
		soNumCell.setCellStyle(itemHeaderStyle);
		soNumCell.setCellValue("Client PO No");
		
		/*
		 * editAccountSheet.addMergedRegion(new CellRangeAddress(0, // first row 0, //
		 * last row 3, // first column 7 // last column ));
		 */
		Cell createdDateCell = header.createCell(4);
		createdDateCell.setCellStyle(itemHeaderStyle);
		createdDateCell.setCellValue("Item Description");
		
		
		Cell descCell = header.createCell(5);
		descCell.setCellStyle(itemHeaderStyle);
		descCell.setCellValue("Qty");
		
	
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(partyList, editAccountSheet, workbook,itemMasterService,salesService,purchaseItemService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	@SuppressWarnings("unused")
	private static void populateSalesRecords(List<Party> partyList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService,PurchaseItemService purchaseItemService ) {
		int rowCount =1;
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
		itemListFont.setFontHeightInPoints((short)8);
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
		fontColumns.setFontHeightInPoints((short)8);
		threeSideborderBold.setFont(fontColumns);
		//Collections.sort(salesItemsList);
		
		CellStyle threeSideborderRightAllign = workbook.createCellStyle();
		threeSideborderRightAllign.setWrapText(true);
		threeSideborderRightAllign.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderRightAllign.setBorderLeft(BORDER_THIN);
		threeSideborderRightAllign.setBorderRight(BORDER_THIN);
		threeSideborderRightAllign.setBorderBottom(BORDER_THIN);
		threeSideborderRightAllign.setBorderTop(BORDER_THIN);
		threeSideborderRightAllign.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRightAllign.setFont(itemListFont);
		
		Row partyRow;
		for (Party party : partyList) {
			Row row = editAccountSheet.createRow(rowCount);
			Cell clientNameCell = row.createCell(0);
			clientNameCell.setCellStyle(threeSideborderBold);
			clientNameCell.setCellValue(party.getPartyName());
			
			List<SalesOrder> salesOrderList = salesService.getSalesListByPartyId(party.getId());
			Collections.sort(salesOrderList);
			Row soRow;
			ArrayList<SalesItem> salesItemList = new ArrayList<SalesItem>();
				for (SalesOrder so : salesOrderList) {
					
					List<SalesItem> salesList=salesService.getSalesItemsBySalesOrderId(so.getId());
					
					for (SalesItem soItem : salesList) {
						if(soItem.getItem_units().getName()!="Heading") {
							List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemsBySalesItemId(soItem.getId());
							
									if(poItemList.isEmpty()) {
										salesItemList.add(soItem);
									}
								}	
					}
				}
				
				Collections.sort(salesItemList);
				Row salesRow;
				for (SalesItem salesItem : salesItemList) {
					
					
					Date created = salesItem.getSalesOrder().getCreated();
					String dateString = created.toString();
					String[] arr = dateString.split(" ");

					// Formatting date to a required format
					String formattedDate = arr[0];
					formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
							+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
							+ formattedDate.substring(0, 4);
					
					
					
					//String description=salesItem.getDescription();
					
					
					String clientName = salesItem.getSalesOrder().getParty().getPartyName();
					
					/*
					 * editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount+2, // first
					 * row rowCount+2, // last row 3, // first column 7 // last column ));
					 */
					Cell slNoCell = row.createCell(1);
					slNoCell.setCellStyle(threeSideborder);
					slNoCell.setCellValue(salesItem.getSalesOrder().getId());
					
					Cell soNoCell = row.createCell(2);
					soNoCell.setCellStyle(threeSideborder);
					soNoCell.setCellValue(formattedDate);
					
					Cell poNoCell = row.createCell(3);
					poNoCell.setCellStyle(threeSideborder);
					poNoCell.setCellValue(salesItem.getSalesOrder().getClientPoNumber());
					
					Cell crDtCell = row.createCell(4);
					crDtCell.setCellStyle(threeSideborder);
					crDtCell.setCellValue(salesItem.getDescription());
					
					Cell descCell = row.createCell(5);
					descCell.setCellStyle(threeSideborderRightAllign);
					descCell.setCellValue(salesItem.getQuantity());
					
					if(salesItem!=salesItemList.get(salesItemList.size()-1)) {
						rowCount++;
						salesRow=editAccountSheet.createRow(rowCount);
						row=salesRow;
						}
				}
				
				/*if(so!=salesOrderList.get(salesOrderList.size()-1)) {
					rowCount++;
					soRow=editAccountSheet.createRow(rowCount);
					row=soRow;
				}*/
			
			rowCount++;
			/*if(party!=partyList.get(partyList.size()-1)) {
				rowCount++;
				partyRow=editAccountSheet.createRow(rowCount);
				row=partyRow;
			}*/
			
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
			for (int j = 0; j <= 5; j++) {
				
			
				CellRangeAddress region = new CellRangeAddress(i,rowLastItemCount-1,j,5);
				
				RegionUtil.setBorderTop(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, region, sheet, workBook);
			}
		}
		
	}
}
