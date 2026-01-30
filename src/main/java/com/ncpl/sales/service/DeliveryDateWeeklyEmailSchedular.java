package com.ncpl.sales.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;

@Service
@EnableScheduling
public class DeliveryDateWeeklyEmailSchedular {
	@Autowired
	EmailService emailService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	PurchaseOrderService purchaseService;
	@Autowired
	SalesService salesService;
	@Autowired
	GrnService grnService;
	@Autowired
	PurchaseItemService poItemService;
	@Autowired
	SalesOrderDesignService soDesignService;
	
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "purchase_items_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	static List<SalesItem> salesItemList = null;
	
	//@Scheduled(cron = "0 0 11 * * ?", zone="IST")
	//@Scheduled(cron = "0 18 17 ? * TUE", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		salesItemList = purchaseService.getSalesItemsWhereDelDateInNextWeek();
		if(salesItemList.size()!=0) {
	    	try {
				WeeklyPurchaseExcel.buildExcelDocument(salesItemList, filePath,itemMasterService,salesService,purchaseService,grnService,poItemService,soDesignService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail();
	    	emailService.sendEmailToServer(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail() {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Delivery Date Info Report For Next Week"); 
		emailContents.put("template","dl_date_weekly_info.html"); 
		emailContents.put("to1", "anithapoojary846@gmail.com");
		emailContents.put("to2", "anithapoojary846@gmail.com");
		emailContents.put("cc1", "anithapoojary846@gmail.com");
		emailContents.put("cc2", "anithapoojary846@gmail.com");
		emailContents.put("cc3", "anithapoojary846@gmail.com");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class WeeklyPurchaseExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List<SalesItem> salesItemsList, String filePath, ItemMasterService itemMasterService, SalesService salesService, PurchaseOrderService purchaseService, GrnService grnService, PurchaseItemService poItemService, SalesOrderDesignService soDesignService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Purchase Delivery Date Report");
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
				4, // first column
				8 // last column
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
		
		Cell vendorCell = header.createCell(3);
		vendorCell.setCellStyle(itemHeaderStyle);
		vendorCell.setCellValue("Client Name");
		
		Cell descCell = header.createCell(4);
		descCell.setCellStyle(itemHeaderStyle);
		descCell.setCellValue("Description");
		
		Cell modelCell = header.createCell(9);
		modelCell.setCellStyle(itemHeaderStyle);
		modelCell.setCellValue("Model No.");
		
		Cell dlDateCell = header.createCell(10);
		dlDateCell.setCellStyle(itemHeaderStyle);
		dlDateCell.setCellValue("PO Number");
		
	
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(salesItemsList, editAccountSheet, workbook,itemMasterService,salesService,purchaseService,grnService,poItemService,soDesignService);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void populateSalesRecords(List<SalesItem> salesItemsList, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService, PurchaseOrderService purchaseService, GrnService grnService, PurchaseItemService poItemService, SalesOrderDesignService soDesignService) {
		int rowCount =1;
		for (SalesItem salesItem : salesItemsList) {
			
			

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					4, // first column
					8 // last column
			));
			
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
			
			
			Row row1 = editAccountSheet.createRow(rowCount);
			Cell slno = row1.createCell(0);
			slno.setCellStyle(threeSideborder);
			slno.setCellValue(salesItem.getSlNo());
			
			Cell clientPo = row1.createCell(1);
			clientPo.setCellStyle(threeSideborder);
			clientPo.setCellValue(salesItem.getSalesOrder().getClientPoNumber());
			
			Cell created = row1.createCell(2);
			created.setCellStyle(threeSideborder);
			created.setCellValue(salesItem.getSalesOrder().getCreated());
			
			Cell client = row1.createCell(3);
			client.setCellStyle(threeSideborder);
			client.setCellValue(salesItem.getSalesOrder().getParty().getPartyName());
			
			Cell description = row1.createCell(4);
			CellStyle descriptionStyle = workbook.createCellStyle();
			descriptionStyle.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
			descriptionStyle.setWrapText(true);
			descriptionStyle.setFont(itemListFont);
			descriptionStyle.setBorderLeft(BORDER_THIN);
			descriptionStyle.setBorderRight(BORDER_THIN);
			descriptionStyle.setBorderTop(BORDER_THIN);
			descriptionStyle.setBorderBottom(BORDER_THIN);
			description.setCellStyle(descriptionStyle);
			description.setCellValue(salesItem.getDescription());
			
			
			
			
			String soItemId = salesItem.getId();
			
			List<DesignItems> designItemList =soDesignService.getDesignItemListBySOItemId(soItemId);
			
			Row modelNoRow;
			for (DesignItems designItem : designItemList ) {
				Set purchaseSet = new HashSet();
				String itemId = designItem.getItemId();
				Optional<ItemMaster> itemObj = itemMasterService.getItemById(itemId);
				
				
				
				Cell modelCell = row1.createCell(9);
				modelCell.setCellStyle(threeSideborder);
				modelCell.setCellValue(itemObj.get().getModel());
				
				List<PurchaseItem> purchaseItemList = poItemService.getPurchaseItemListBySalesItemIdAndItemId(soItemId, itemId);
				Collections.sort(purchaseItemList);
				for (PurchaseItem purchaseItem : purchaseItemList) {
					Date todayDate = new Date();
					List<GrnItems> grnItems = grnService.getGrnItemObjByPoItemId(Integer.toString(purchaseItem.getPurchase_item_id()));
					if(grnItems.size()==0){
						Date dlDate = purchaseItem.getDelivaryDate();
						if(dlDate!=null) {
							long difference_In_Time=  dlDate.getTime()-todayDate.getTime();
							long difference_In_Days = (difference_In_Time/ (1000 * 60 * 60 * 24))% 365;
							if(difference_In_Days<7 && difference_In_Days>=0) {
								String purchaseId = purchaseItem.getPurchaseOrder().getPoNumber();
								Optional<PurchaseOrder> purchaseOrder = purchaseService.findById(purchaseId);
								purchaseSet.add(purchaseOrder.get());
							}
						}
					}
					
				}
				ArrayList<PurchaseOrder> purchaseList = new ArrayList<PurchaseOrder>(purchaseSet);
				
				Row poRow;
				for (PurchaseOrder po : purchaseList) {
					String poNumb=po.getPoNumber();
					
					Date poDt = po.getCreated();
					
					String dateString1 = poDt.toString();
					String[] arr1 = dateString1.split(" ");

					// Formatting date to a required format
					String formattedDate1 = arr1[0];
					formattedDate1 = formattedDate1.substring(formattedDate1.length() - 2, formattedDate1.length()) + "/"
							+ formattedDate1.substring(formattedDate1.length() - 5, formattedDate1.length() - 3) + "/"
							+ formattedDate1.substring(0, 4);
					
					
					
					Cell poNumberCell = row1.createCell(10);
					poNumberCell.setCellStyle(threeSideborder);
					poNumberCell.setCellValue(poNumb);
					
					
					if(po!=purchaseList.get(purchaseList.size()-1)) {
					rowCount++;
					poRow=editAccountSheet.createRow(rowCount);
					row1=poRow;
					}
					
				}
				//dont increase rowCount for the last item in the list...
				if(designItem!=designItemList.get(designItemList.size()-1)) {
				rowCount++;
				modelNoRow=editAccountSheet.createRow(rowCount);
				row1=modelNoRow;
				}
			}
			
			
			rowCount++;
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
			for (int j = 0; j <= 10; j++) {
				
			
				CellRangeAddress region = new CellRangeAddress(i,rowLastItemCount-1,j,10);
				
				RegionUtil.setBorderTop(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, region, sheet, workBook);
			}
		}
		
	}
}
