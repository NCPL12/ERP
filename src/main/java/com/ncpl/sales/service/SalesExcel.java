package com.ncpl.sales.service;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;

public class SalesExcel{
	static short VERTICAL_TOP = 0x0;
	static short VERTICAL_JUSTIFY = 0x2;
	static short BORDER_THIN = 0x1;
// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	
	protected static void buildExcelDocument(SalesOrder salesOrder, String filePath) throws Exception {
		// TODO Auto-generated method stub
		Workbook  workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Sales Items");
        List<SalesItem> salesItemList = salesOrder.getItems();
        
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
		itemHeader.setCellValue("SlNo.");
		
		Cell poNumCell = header.createCell(1);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Description");
		
		Cell soNumCell = header.createCell(7);
		soNumCell.setCellStyle(itemHeaderStyle);
		soNumCell.setCellValue("Model No");
		
		Cell createdDateCell = header.createCell(8);
		createdDateCell.setCellStyle(itemHeaderStyle);
		createdDateCell.setCellValue("HSN");
		
		Cell qtyCell = header.createCell(9);
		qtyCell.setCellStyle(itemHeaderStyle);
		qtyCell.setCellValue("Qty");
		
		Cell unitCell = header.createCell(10);
		unitCell.setCellStyle(itemHeaderStyle);
		unitCell.setCellValue("Unit");
		
		Cell supplyPriceCell = header.createCell(11);
		supplyPriceCell.setCellStyle(itemHeaderStyle);
		supplyPriceCell.setCellValue("Supply Price");
		
		Cell servicePriceCell = header.createCell(12);
		servicePriceCell.setCellStyle(itemHeaderStyle);
		servicePriceCell.setCellValue("Service Price");
		
		Cell amountCell = header.createCell(13);
		amountCell.setCellStyle(itemHeaderStyle);
		amountCell.setCellValue("Amount");
		
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(salesItemList, editAccountSheet, workbook);
       
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Item List By Date Report Sheets Has been Created successfully!");
	}
	
	private static void populateSalesRecords(List<SalesItem> salesItemList, Sheet editAccountSheet, Workbook workbook) {
		int rowCount =1;
		//Collections.sort(salesItemsList);
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		for (SalesItem soitem : salesItemList) {
			String slNo=soitem.getSlNo();
			String description=soitem.getDescription();
			String modelNo=soitem.getModelNo();
			String hsn=soitem.getHsnCode();
			String unit=soitem.getItem_units().getName();
			float qty=soitem.getQuantity();
			float supplyPrice=soitem.getUnitPrice();
			float servicePrice=soitem.getServicePrice();
			float amount=soitem.getAmount();
			
			
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
			slNoCell.setCellValue(slNo);
			
			Cell soNoCell = row.createCell(1);
			soNoCell.setCellStyle(threeSideborder);
			soNoCell.setCellValue(description);
			
			Cell poNoCell = row.createCell(7);
			poNoCell.setCellStyle(threeSideborder);
			poNoCell.setCellValue(modelNo);
			
			Cell crDtCell = row.createCell(8);
			crDtCell.setCellStyle(threeSideborder);
			crDtCell.setCellValue(hsn);
			
			Cell qtCell = row.createCell(9);
			qtCell.setCellStyle(threeSideborder);
			qtCell.setCellValue(qty);
			
			Cell unCell = row.createCell(10);
			unCell.setCellStyle(threeSideborder);
			unCell.setCellValue(unit);
			
			Cell supplyCell = row.createCell(11);
			supplyCell.setCellStyle(threeSideborder);
			supplyCell.setCellValue(supplyPrice);
			
			Cell serviceCell = row.createCell(12);
			serviceCell.setCellStyle(threeSideborder);
			serviceCell.setCellValue(servicePrice);
			
			Cell amtCell = row.createCell(13);
			amtCell.setCellStyle(threeSideborder);
			amtCell.setCellValue(amount);
			
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
