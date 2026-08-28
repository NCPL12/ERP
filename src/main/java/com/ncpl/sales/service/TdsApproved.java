package com.ncpl.sales.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Tds;
import com.ncpl.sales.model.TdsItems;

public class TdsApproved {
	short VERTICAL_TOP = 0x0;
	short VERTICAL_JUSTIFY = 0x2;
	short BORDER_THIN = 0x1;
	public void buildExcelDocument(Tds tdsObj, String filePath, SalesService salesService,
			Optional<SalesOrder> salesOrder, HttpServletRequest request,Party party,ItemMasterService itemService) throws IOException {
		
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		InvoiceExcelLogoService logoService = new InvoiceExcelLogoService();
		Date date = salesOrder.get().getClientPoDate();
		
		List<TdsItems> tdsItemsList = tdsObj.getItems();
		// Converting date to String
		String dateString = date.toString();
		String[] arr = dateString.split(" ");

		// Formatting date to a required format
		String formattedDate = arr[0];
		formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "/"
				+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "/"
				+ formattedDate.substring(0, 4);
		Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Tds Approved Report");
        editAccountSheet.setDefaultColumnWidth(9);
        
        CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");

		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);
		
		Row secondRow = editAccountSheet.createRow(1);
		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				4, // last row
				0, // first column
				1 // last column
		));

		CellStyle mergestyle = workbook.createCellStyle();
		mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		Font headingFont = workbook.createFont();
		headingFont.setFontName("Calibri");
		headingFont.setFontHeight((short) (15.5 * 20));
		headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		mergestyle.setFont(headingFont);

		// For logo
		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				4, // last row
				2, // first column
				5 // last column
		));

		// Insert Logo
		logoService.insertLogoInTemplate(workbook, editAccountSheet, request);

		Row Quotation = editAccountSheet.createRow(3);

		Cell descriptionCell = secondRow.createCell(2);
		descriptionCell.setCellValue(" \n  Neptune Controls Pvt Ltd");
		CellStyle descriptionmergestyle = workbook.createCellStyle();
		descriptionmergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		descriptionmergestyle.setVerticalAlignment((short) (VERTICAL_TOP));
		descriptionmergestyle.setBorderBottom(BORDER_THIN);
		descriptionmergestyle.setBorderTop(BORDER_THIN);
		descriptionmergestyle.setBorderRight(BORDER_THIN);
		descriptionmergestyle.setBorderLeft(BORDER_THIN);

		Font descriptionFont = workbook.createFont();
		descriptionFont.setFontName("Calibri");
		descriptionFont.setFontHeight((short) (7.5 * 45));
		descriptionFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		descriptionmergestyle.setFont(descriptionFont);
		descriptionmergestyle.setWrapText(true);
		descriptionCell.setCellStyle(descriptionmergestyle);

		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				1, // last row
				6, // first column
				11 // last column
		));
		// setBordersToMergedCells(workbook, editAccountSheet);

		// Row poNumber = editAccountSheet.createRow(1);
		Cell poNum = secondRow.createCell(6);
		poNum.setCellValue("Client PO No: " + salesOrder.get().getClientPoNumber());

		CellStyle poNumStyle = workbook.createCellStyle();
		poNumStyle.setBorderLeft(BORDER_THIN);
		poNumStyle.setBorderRight(BORDER_THIN);
		poNumStyle.setBorderTop(BORDER_THIN);
		poNumStyle.setBorderBottom(BORDER_THIN);
		Font poNumFont = workbook.createFont();
		poNumFont.setFontName("Calibri");
		poNumFont.setFontHeight((short) (7.5 * 35));
		poNumFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		poNumStyle.setFont(poNumFont);
		poNumStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		poNumStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		// poNumStyle.setFillForegroundColor(IndexedColors.CORAL.index );
		poNum.setCellStyle(poNumStyle);
		editAccountSheet.addMergedRegion(new CellRangeAddress(2, // first row
				2, // last row
				6, // first column
				11 // last column
		));
		Row podate = editAccountSheet.createRow(2);
		Cell podte = podate.createCell(6);
		podte.setCellValue("Client PO Date: " + formattedDate);

		Cell c9Podte = podate.createCell(9);
		CellStyle c9Podtestyle = workbook.createCellStyle();
		c9Podtestyle.setBorderRight(BORDER_THIN);
		c9Podte.setCellStyle(c9Podtestyle);

		CellStyle poDateStyle = workbook.createCellStyle();
		poDateStyle.setBorderLeft(BORDER_THIN);
		poDateStyle.setBorderRight(BORDER_THIN);
		poDateStyle.setBorderTop(BORDER_THIN);
		poDateStyle.setBorderBottom(BORDER_THIN);
		Font poDateFont = workbook.createFont();
		poDateFont.setFontName("Calibri");
		poDateFont.setFontHeight((short) (7.5 * 35));
		poDateStyle.setFont(poDateFont);
		poDateStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		poDateStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		podte.setCellStyle(poDateStyle);

		editAccountSheet.addMergedRegion(new CellRangeAddress(3, // first row
				3, // last row
				6, // first column
				11 // last column
		));

		// Row Quotation = editAccountSheet.createRow(3);
		Cell quotation = Quotation.createCell(6);
		quotation.setCellValue("Project Name.: " +party.getPartyName());

		CellStyle quotStyle = workbook.createCellStyle();
		quotStyle.setBorderLeft(BORDER_THIN);
		quotStyle.setBorderRight(BORDER_THIN);
		quotStyle.setBorderTop(BORDER_THIN);
		quotStyle.setBorderBottom(BORDER_THIN);
		Font quotFont = workbook.createFont();
		quotFont.setFontName("Calibri");
		quotFont.setFontHeight((short) (7.5 * 35));
		quotStyle.setFont(quotFont);
		quotStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		quotStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		quotation.setCellStyle(quotStyle);

		editAccountSheet.addMergedRegion(new CellRangeAddress(4, // first row
				4, // last row
				6, // first column
				11 // last column
		));
		Row QuotationDate = editAccountSheet.createRow(4);
		Cell qtndate = QuotationDate.createCell(6);
		qtndate.setCellValue("Project Address: " +party.getAddr1());

		CellStyle QuotationDatestyle = workbook.createCellStyle();
		QuotationDatestyle.setWrapText(true);
		QuotationDatestyle.setBorderLeft(BORDER_THIN);
		QuotationDatestyle.setBorderRight(BORDER_THIN);
		QuotationDatestyle.setBorderTop(BORDER_THIN);
		QuotationDatestyle.setBorderBottom(BORDER_THIN);
		Font QuotationDateFont = workbook.createFont();
		QuotationDateFont.setFontName("Calibri");
		QuotationDateFont.setFontHeight((short) (7.5 * 35));
		QuotationDatestyle.setFont(QuotationDateFont);
		QuotationDatestyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		QuotationDatestyle.setVerticalAlignment((short) (VERTICAL_TOP));
		qtndate.setCellStyle(QuotationDatestyle);
		editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				6, // last row
				0, // first column
				11 // last column
		));

		Row header = editAccountSheet.createRow(5);
		Cell headerCell = header.createCell(0);
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		headerStyle.setFillBackgroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		Font headerFont = workbook.createFont();
		headerFont.setFontName("Calibri");
		headerFont.setFontHeight((short) (7.5 * 45));
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		headerCell.setCellStyle(headerStyle);
		headerCell.setCellValue("Tds Approved Items");
		
		Font fontColumn = workbook.createFont();
		fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumn.setFontHeight((short) (7.5 * 35));
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				0, // first column
				0 // last column
		));
		
		
		Row itemListHeader = editAccountSheet.createRow(7);
		Cell itemHeader = itemListHeader.createCell(0);
		CellStyle itemHeaderStyle = workbook.createCellStyle();
		itemHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		itemHeaderStyle.setFont(fontColumn);
		itemHeaderStyle.setBorderBottom(BORDER_THIN);
		itemHeaderStyle.setBorderTop(BORDER_THIN);
		itemHeaderStyle.setBorderRight(BORDER_THIN);
		itemHeaderStyle.setBorderLeft(BORDER_THIN);
		itemHeader.setCellStyle(itemHeaderStyle);
		itemHeader.setCellValue("Sl No");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				1, // first column
				5 // last column
		));
		Cell desc = itemListHeader.createCell(1);
		/*for(int i=1;i<9;i++){
			itemListHeader.createCell(i).setCellStyle(topborder);
		}*/
		CellStyle descStyle = workbook.createCellStyle();
		descStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		descStyle.setFont(fontColumn);
		descStyle.setBorderBottom(BORDER_THIN);
		descStyle.setBorderTop(BORDER_THIN);
		descStyle.setBorderRight(BORDER_THIN);
		descStyle.setBorderLeft(BORDER_THIN);
		desc.setCellStyle(descStyle);
		desc.setCellValue("Description");
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				6, // first column
				6 // last column
		));
		
		Cell hsn = itemListHeader.createCell(6);
		CellStyle hsnStyle = workbook.createCellStyle();
		hsnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		hsnStyle.setFont(fontColumn);
		hsnStyle.setWrapText(true);
		hsnStyle.setBorderBottom(BORDER_THIN);
		hsnStyle.setBorderTop(BORDER_THIN);
		hsnStyle.setBorderRight(BORDER_THIN);
		hsnStyle.setBorderLeft(BORDER_THIN);
		hsn.setCellStyle(hsnStyle);
		hsn.setCellValue("PO Qty");

		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				7, // first column
				7 // last column
		));
		
		Cell poNumber = itemListHeader.createCell(7);
		
		CellStyle poNumberColumnStyle = workbook.createCellStyle();
		poNumberColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		poNumberColumnStyle.setFont(fontColumn);
		poNumberColumnStyle.setWrapText(true);
		poNumberColumnStyle.setBorderBottom(BORDER_THIN);
		poNumberColumnStyle.setBorderTop(BORDER_THIN);
		poNumberColumnStyle.setBorderRight(BORDER_THIN);
		poNumberColumnStyle.setBorderLeft(BORDER_THIN);
		poNumber.setCellStyle(poNumberColumnStyle);
		poNumber.setCellValue("Unit");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				8, // first column
				8 // last column
		));
		
		Cell poDate = itemListHeader.createCell(8);
		CellStyle poDateColumnStyle = workbook.createCellStyle();
		poDateColumnStyle.setWrapText(true);
		poDateColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		poDateColumnStyle.setFont(fontColumn);
		poDateColumnStyle.setBorderBottom(BORDER_THIN);
		poDateColumnStyle.setBorderTop(BORDER_THIN);
		poDateColumnStyle.setBorderRight(BORDER_THIN);
		poDateColumnStyle.setBorderLeft(BORDER_THIN);
		poDate.setCellStyle(poDateColumnStyle);
		poDate.setCellValue("Design Items");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				9, // first column
				9 // last column
		));
		
		Cell vendor = itemListHeader.createCell(9);
		CellStyle vendorColumnStyle = workbook.createCellStyle();
		vendorColumnStyle.setWrapText(true);
		vendorColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		vendorColumnStyle.setBorderBottom(BORDER_THIN);
		vendorColumnStyle.setBorderTop(BORDER_THIN);
		vendorColumnStyle.setBorderRight(BORDER_THIN);
		vendorColumnStyle.setBorderLeft(BORDER_THIN);
		vendorColumnStyle.setFont(fontColumn);
		vendor.setCellStyle(vendorColumnStyle);
		vendor.setCellValue("Design Qty");
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				10, // first column
				10 // last column
		));
		
		Cell dcDateQty = itemListHeader.createCell(10);
		CellStyle dcDateColumnStyle = workbook.createCellStyle();
		dcDateColumnStyle.setWrapText(true);
		dcDateColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		dcDateColumnStyle.setBorderBottom(BORDER_THIN);
		dcDateColumnStyle.setBorderTop(BORDER_THIN);
		dcDateColumnStyle.setBorderRight(BORDER_THIN);
		dcDateColumnStyle.setBorderLeft(BORDER_THIN);
		dcDateColumnStyle.setFont(fontColumn);
		dcDateQty.setCellStyle(dcDateColumnStyle);
		dcDateQty.setCellValue("Tds");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				11, // first column
				11 // last column
		));
		
		Cell clientName = itemListHeader.createCell(11);
		CellStyle clientNameColumnStyle = workbook.createCellStyle();
		clientNameColumnStyle.setWrapText(true);
		clientNameColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		clientNameColumnStyle.setBorderBottom(BORDER_THIN);
		clientNameColumnStyle.setBorderTop(BORDER_THIN);
		clientNameColumnStyle.setBorderRight(BORDER_THIN);
		clientNameColumnStyle.setBorderLeft(BORDER_THIN);
		clientNameColumnStyle.setFont(fontColumn);
		clientName.setCellStyle(clientNameColumnStyle);
		clientName.setCellValue("Site Qty");
		
		CellStyle AmountcellStyle = workbook.createCellStyle();
		AmountcellStyle.setBorderRight(BORDER_THIN);
		AmountcellStyle.setBorderBottom(BORDER_THIN);

		CellStyle leftBorderStyle = workbook.createCellStyle();
		leftBorderStyle.setBorderLeft(BORDER_THIN);

		CellStyle threeSideborder = workbook.createCellStyle();
		threeSideborder.setWrapText(true);
		threeSideborder.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborder.setBorderLeft(BORDER_THIN);
		threeSideborder.setBorderRight(BORDER_THIN);
		threeSideborder.setBorderBottom(BORDER_THIN);
		threeSideborder.setBorderTop(BORDER_THIN);
		threeSideborder.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font itemListFont = workbook.createFont();
		itemListFont.setFontHeight((short) (7.5 * 35));
		threeSideborder.setFont(itemListFont);

		CellStyle threeSideborderRight = workbook.createCellStyle();
		XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRight.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
		threeSideborderRight.setBorderLeft(BORDER_THIN);
		threeSideborderRight.setBorderRight(BORDER_THIN);
		threeSideborderRight.setBorderBottom(BORDER_THIN);
		threeSideborderRight.setVerticalAlignment((short) (VERTICAL_TOP));
		threeSideborderRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRight.setFont(itemListFont);
		
		CellStyle threeSideborderRightWithoutDec = workbook.createCellStyle();
		XSSFDataFormat threeSideborderRightformatWithoutDec = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRightWithoutDec.setDataFormat(threeSideborderRightformatWithoutDec.getFormat("#,###"));
		threeSideborderRightWithoutDec.setBorderLeft(BORDER_THIN);
		threeSideborderRightWithoutDec.setBorderRight(BORDER_THIN);
		threeSideborderRightWithoutDec.setBorderBottom(BORDER_THIN);
		//threeSideborderRightWithoutDec.setVerticalAlignment((short) (VERTICAL_TOP));
		threeSideborderRightWithoutDec.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderRightWithoutDec.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRightWithoutDec.setFont(itemListFont);

		CellStyle fourSideborder = workbook.createCellStyle();
		fourSideborder.setFont(itemListFont);
		fourSideborder.setBorderLeft(BORDER_THIN);
		fourSideborder.setBorderRight(BORDER_THIN);
		fourSideborder.setBorderBottom(BORDER_THIN);
		fourSideborder.setBorderTop(BORDER_THIN);

		CellStyle leftborder = workbook.createCellStyle();
		leftborder.setFont(itemListFont);
		leftborder.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		leftborder.setBorderLeft(BORDER_THIN);
		leftborder.setVerticalAlignment((short) (VERTICAL_TOP));

		CellStyle rightborder = workbook.createCellStyle();
		rightborder.setWrapText(true);
		rightborder.setFont(itemListFont);
		rightborder.setBorderRight(BORDER_THIN);
		rightborder.setBorderLeft(BORDER_THIN);
		rightborder.setBorderBottom(BORDER_THIN);
		//rightborder.setVerticalAlignment((short) (VERTICAL_TOP));
		rightborder.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		rightborder.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle topborder = workbook.createCellStyle();
		topborder.setBorderTop(BORDER_THIN);
		
		int rowCount=11;
		for (TdsItems tdsItem : tdsItemsList) {
			
			
			if(tdsItem.isTdsApproved()==true) {
				editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
						rowCount, // last row
						1, // first column
						5// last column
				));
				
				String salesItemId = tdsItem.getDescription();
				Optional<SalesItem> salesItem = salesService.getSalesItemObjById(salesItemId);
				String itemId = tdsItem.getModelNumber();
				Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
				
				Row row1 = editAccountSheet.createRow(rowCount);
				Cell slno = row1.createCell(0);
				slno.setCellStyle(threeSideborder);
				slno.setCellValue(salesItem.get().getSlNo());
				Cell description = row1.createCell(1);
				CellStyle descriptionStyle = workbook.createCellStyle();
				descriptionStyle.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
				descriptionStyle.setWrapText(true);
				descriptionStyle.setFont(itemListFont);
				descriptionStyle.setBorderLeft(BORDER_THIN);
				descriptionStyle.setBorderRight(BORDER_THIN);
				descriptionStyle.setBorderTop(BORDER_THIN);
				descriptionStyle.setBorderBottom(BORDER_THIN);
				description.setCellStyle(descriptionStyle);
				description.setCellValue(salesItem.get().getDescription());
				
				Cell poQtyCell = row1.createCell(6);
				poQtyCell.setCellStyle(threeSideborder);
				poQtyCell.setCellValue(salesItem.get().getQuantity());
				
				Cell unitCell = row1.createCell(7);
				unitCell.setCellStyle(threeSideborder);
				unitCell.setCellValue(salesItem.get().getItem_units().getName());
				
				Cell modelCell = row1.createCell(8);
				modelCell.setCellStyle(threeSideborder);
				modelCell.setCellValue(itemObj.get().getModel());
				
				Cell designQtyCell = row1.createCell(9);
				designQtyCell.setCellStyle(threeSideborder);
				designQtyCell.setCellValue(tdsItem.getDesignQty());
				
				Cell tdsCell = row1.createCell(10);
				tdsCell.setCellStyle(threeSideborder);
				tdsCell.setCellValue("Yes");
				
				Cell siteQtyCell = row1.createCell(11);
				siteQtyCell.setCellStyle(threeSideborder);
				siteQtyCell.setCellValue(tdsItem.getSiteQuantity());
				rowCount++;
			}
			
		}
		int rowLastItemCount = rowCount;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Stock Sheets Has been Created successfully!");
	}
	
	private void setBordersToMergedCells(Workbook workBook, Sheet sheet, int rowLastItemCount) {
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
