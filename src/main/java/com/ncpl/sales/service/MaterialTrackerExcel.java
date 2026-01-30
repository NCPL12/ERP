package com.ncpl.sales.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;

public class MaterialTrackerExcel extends AbstractXlsxView{
	short VERTICAL_TOP = 0x0;
	short VERTICAL_JUSTIFY = 0x2;
	short BORDER_THIN = 0x1;
	String company = "Neptune controls pvt ltd No.8/2(Old No.114), 2nd Cross 7th Main Road Nandidurga Extension Bangalore-560046 Contact : 080-40904685,7624964492 "
			+ "E-Mail : accounts@ncpl.co";
	String s1 = "Complete solution for BMS, Lighting Control, CCTV & Security Systems, DDC Panels, Automation Panels, Lighting,panels, MCC & Starter Panels";
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
		InvoiceExcelLogoService logoService = new InvoiceExcelLogoService();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		Map salesMap =  (Map) model.get("salesData");
		SalesOrder salesObj =  (SalesOrder) salesMap.get("salesObj");
		List<SalesItem> salesItems = salesObj.getItems();
		Date date = salesObj.getClientPoDate();
		String shippingParty=(String) request.getAttribute("shippingParty");
		String shippingPartyAddress= (String) request.getAttribute("shippingPartyAddr");
		Map itemsList = (Map) request.getAttribute("map");
		DeliveryChallanService dcService=(DeliveryChallanService) request.getAttribute("dcService");
		GrnService grnService=(GrnService) request.getAttribute("grnSrvce");
		PurchaseItemService poItemService=(PurchaseItemService) request.getAttribute("poItemService");
		SalesOrderDesignService soDesignService=(SalesOrderDesignService) request.getAttribute("designService");
		PurchaseOrderService purchaseOrderService=(PurchaseOrderService) request.getAttribute("poService");
		ItemMasterService itemService=(ItemMasterService) request.getAttribute("itemMasterService");
		
		// Converting date to String
		String dateString = date.toString();
		String[] arr = dateString.split(" ");

		// Formatting date to a required format
		String formattedDate = arr[0];
		formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "/"
				+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "/"
				+ formattedDate.substring(0, 4);
		
		Date todayDate = new Date();
		DateFormat formatter4 = new SimpleDateFormat("dd/MM/yyyy");
		String date4=formatter4.format(todayDate);

		
		String fileName = salesObj.getClientPoNumber() + "_SALES.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		Sheet editAccountSheet = workbook.createSheet("Sales");
		editAccountSheet.setDefaultColumnWidth(9);
		//editAccountSheet.autoSizeColumn(11);
		if (editAccountSheet.getPhysicalNumberOfRows() > 0) {
            Row firstRow = editAccountSheet.getRow(editAccountSheet.getFirstRowNum());
            if (firstRow != null) {
                int lastColumn = firstRow.getLastCellNum(); // number of columns in the first row
                for (int col = 0; col < lastColumn; col++) {
                	editAccountSheet.autoSizeColumn(col);
                }
            }
        }

	//	setBordersToMergedCells(workbook, editAccountSheet);
		// create style for header cells
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");

		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);

		Row secondRow = editAccountSheet.createRow(1);
		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				5, // last row
				0, // first column
				5 // last column
		));

		CellStyle mergestyle = workbook.createCellStyle();
		mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		Font headingFont = workbook.createFont();
		headingFont.setFontName("Calibri");
		headingFont.setFontHeightInPoints((short)11);
		headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		mergestyle.setFont(headingFont);

		// For logo
		/*editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				4, // last row
				2, // first column
				5 // last column
		));*/

		// Insert Logo
		logoService.insertLogoInTemplate(workbook, editAccountSheet, request);

		Row Quotation = editAccountSheet.createRow(3);

		/*Cell descriptionCell = secondRow.createCell(2);
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
		descriptionFont.setFontHeightInPoints((short)11);
		descriptionFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		descriptionmergestyle.setFont(descriptionFont);
		descriptionmergestyle.setWrapText(true);
		descriptionCell.setCellStyle(descriptionmergestyle);*/

		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				1, // last row
				6, // first column
				20 // last column
		));
		// setBordersToMergedCells(workbook, editAccountSheet);

		// Row poNumber = editAccountSheet.createRow(1);
		Cell poNum = secondRow.createCell(6);
		poNum.setCellValue("Client PO No: " + salesObj.getClientPoNumber());

		CellStyle poNumStyle = workbook.createCellStyle();
		poNumStyle.setBorderLeft(BORDER_THIN);
		poNumStyle.setBorderRight(BORDER_THIN);
		poNumStyle.setBorderTop(BORDER_THIN);
		poNumStyle.setBorderBottom(BORDER_THIN);
		Font poNumFont = workbook.createFont();
		poNumFont.setFontName("Calibri");
		poNumFont.setFontHeightInPoints((short)10);
		poNumFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		poNumStyle.setFont(poNumFont);
		poNumStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		poNumStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		// poNumStyle.setFillForegroundColor(IndexedColors.CORAL.index );
		poNum.setCellStyle(poNumStyle);
		editAccountSheet.addMergedRegion(new CellRangeAddress(2, // first row
				2, // last row
				6, // first column
				20 // last column
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
		poDateFont.setFontHeightInPoints((short)10);
		poDateStyle.setFont(poDateFont);
		poDateStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		poDateStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		podte.setCellStyle(poDateStyle);

		editAccountSheet.addMergedRegion(new CellRangeAddress(3, // first row
				3, // last row
				6, // first column
				20 // last column
		));

		// Row Quotation = editAccountSheet.createRow(3);
		Cell quotation = Quotation.createCell(6);
		quotation.setCellValue("Project Name.: " +shippingParty);

		CellStyle quotStyle = workbook.createCellStyle();
		quotStyle.setBorderLeft(BORDER_THIN);
		quotStyle.setBorderRight(BORDER_THIN);
		quotStyle.setBorderTop(BORDER_THIN);
		quotStyle.setBorderBottom(BORDER_THIN);
		Font quotFont = workbook.createFont();
		quotFont.setFontName("Calibri");
		quotFont.setFontHeightInPoints((short)10);
		quotStyle.setFont(quotFont);
		quotStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		quotStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		quotation.setCellStyle(quotStyle);

		editAccountSheet.addMergedRegion(new CellRangeAddress(4, // first row
				4, // last row
				6, // first column
				20 // last column
		));
		Row QuotationDate = editAccountSheet.createRow(4);
		Cell qtndate = QuotationDate.createCell(6);
		qtndate.setCellValue("Project Address: " +shippingPartyAddress);

		CellStyle QuotationDatestyle = workbook.createCellStyle();
		QuotationDatestyle.setWrapText(true);
		QuotationDatestyle.setBorderLeft(BORDER_THIN);
		QuotationDatestyle.setBorderRight(BORDER_THIN);
		QuotationDatestyle.setBorderTop(BORDER_THIN);
		QuotationDatestyle.setBorderBottom(BORDER_THIN);
		Font QuotationDateFont = workbook.createFont();
		QuotationDateFont.setFontName("Calibri");
		QuotationDateFont.setFontHeightInPoints((short)10);
		QuotationDatestyle.setFont(QuotationDateFont);
		QuotationDatestyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		QuotationDatestyle.setVerticalAlignment((short) (VERTICAL_TOP));
		qtndate.setCellStyle(QuotationDatestyle);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				5, // last row
				6, // first column
				20 // last column
		));
		Row todaysDate = editAccountSheet.createRow(5);
		Cell datetoday = todaysDate.createCell(6);
		datetoday.setCellValue("Material Tracker Date: " +date4);

		CellStyle datetodaystyle = workbook.createCellStyle();
		datetodaystyle.setWrapText(true);
		datetodaystyle.setBorderLeft(BORDER_THIN);
		datetodaystyle.setBorderRight(BORDER_THIN);
		datetodaystyle.setBorderTop(BORDER_THIN);
		datetodaystyle.setBorderBottom(BORDER_THIN);
		Font datetodaystyleFont = workbook.createFont();
		datetodaystyleFont.setFontName("Calibri");
		datetodaystyleFont.setFontHeightInPoints((short)10);
		datetodaystyle.setFont(datetodaystyleFont);
		datetodaystyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		datetodaystyle.setVerticalAlignment((short) (VERTICAL_TOP));
		datetoday.setCellStyle(datetodaystyle);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(6, // first row
				6, // last row
				0, // first column
				20 // last column
		));

		Row header = editAccountSheet.createRow(6);
		Cell headerCell = header.createCell(0);
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		headerStyle.setFillBackgroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		headerStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		Font headerFont = workbook.createFont();
		headerFont.setFontName("Calibri");
		headerFont.setFontHeightInPoints((short)11);
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		headerCell.setCellStyle(headerStyle);
		headerCell.setCellValue("Material Tracker");
		
		Font fontColumn = workbook.createFont();
		fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumn.setFontHeightInPoints((short)10);
		
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
				6 // last column
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
				7, // first column
				7 // last column
		));
		
		Cell hsn = itemListHeader.createCell(7);
		CellStyle hsnStyle = workbook.createCellStyle();
		hsnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		hsnStyle.setFont(fontColumn);
		hsnStyle.setWrapText(true);
		hsnStyle.setBorderBottom(BORDER_THIN);
		hsnStyle.setBorderTop(BORDER_THIN);
		hsnStyle.setBorderRight(BORDER_THIN);
		hsnStyle.setBorderLeft(BORDER_THIN);
		hsn.setCellStyle(hsnStyle);
		hsn.setCellValue("Qty as per BOQ");

		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				8, // first column
				8 // last column
		));
		
		Cell modelNo = itemListHeader.createCell(8);
		
		CellStyle modelNoStyle = workbook.createCellStyle();
		modelNoStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		modelNoStyle.setFont(fontColumn);
		modelNoStyle.setWrapText(true);
		modelNoStyle.setBorderBottom(BORDER_THIN);
		modelNoStyle.setBorderTop(BORDER_THIN);
		modelNoStyle.setBorderRight(BORDER_THIN);
		modelNoStyle.setBorderLeft(BORDER_THIN);
		modelNo.setCellStyle(modelNoStyle);
		modelNo.setCellValue("Qty as per site ");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				9, // first column
				9 // last column
		));
		
		Cell qty = itemListHeader.createCell(9);
		CellStyle qtyStyle = workbook.createCellStyle();
		qtyStyle.setWrapText(true);
		qtyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		qtyStyle.setFont(fontColumn);
		qtyStyle.setBorderBottom(BORDER_THIN);
		qtyStyle.setBorderTop(BORDER_THIN);
		qtyStyle.setBorderRight(BORDER_THIN);
		qtyStyle.setBorderLeft(BORDER_THIN);
		qty.setCellStyle(qtyStyle);
		qty.setCellValue("Ordered Qty");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				10, // first column
				10 // last column
		));
		
		Cell Amount = itemListHeader.createCell(10);
		CellStyle AmountColumnStyle = workbook.createCellStyle();
		AmountColumnStyle.setWrapText(true);
		AmountColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		AmountColumnStyle.setBorderBottom(BORDER_THIN);
		AmountColumnStyle.setBorderTop(BORDER_THIN);
		AmountColumnStyle.setBorderRight(BORDER_THIN);
		AmountColumnStyle.setBorderLeft(BORDER_THIN);
		AmountColumnStyle.setFont(fontColumn);
		Amount.setCellStyle(AmountColumnStyle);
		Amount.setCellValue("Not Ordered Qty");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				11, // first column
				11 // last column
		));
		
		Cell deliveredQty = itemListHeader.createCell(11);
		CellStyle deliveredQtyColumnStyle = workbook.createCellStyle();
		deliveredQtyColumnStyle.setWrapText(true);
		deliveredQtyColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		deliveredQtyColumnStyle.setBorderBottom(BORDER_THIN);
		deliveredQtyColumnStyle.setBorderTop(BORDER_THIN);
		deliveredQtyColumnStyle.setBorderRight(BORDER_THIN);
		deliveredQtyColumnStyle.setBorderLeft(BORDER_THIN);
		deliveredQtyColumnStyle.setFont(fontColumn);
		deliveredQty.setCellStyle(deliveredQtyColumnStyle);
		deliveredQty.setCellValue("Delivered Qty");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				12, // first column
				12 // last column
		));
		
		Cell inStoreQty = itemListHeader.createCell(12);
		CellStyle inStoreQtyColumnStyle = workbook.createCellStyle();
		inStoreQtyColumnStyle.setWrapText(true);
		inStoreQtyColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		inStoreQtyColumnStyle.setBorderBottom(BORDER_THIN);
		inStoreQtyColumnStyle.setBorderTop(BORDER_THIN);
		inStoreQtyColumnStyle.setBorderRight(BORDER_THIN);
		inStoreQtyColumnStyle.setBorderLeft(BORDER_THIN);
		inStoreQtyColumnStyle.setFont(fontColumn);
		inStoreQty.setCellStyle(inStoreQtyColumnStyle);
		inStoreQty.setCellValue("Instore Qty");
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				13, // first column
				13 // last column
		));
		
		Cell modelCll = itemListHeader.createCell(13);
		CellStyle modelCllStyle = workbook.createCellStyle();
		modelCllStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		modelCllStyle.setFont(fontColumn);
		modelCllStyle.setWrapText(true);
		modelCllStyle.setBorderBottom(BORDER_THIN);
		modelCllStyle.setBorderTop(BORDER_THIN);
		modelCllStyle.setBorderRight(BORDER_THIN);
		modelCllStyle.setBorderLeft(BORDER_THIN);
		modelCll.setCellStyle(modelCllStyle);
		modelCll.setCellValue("Model Number");

		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				14, // first column
				14 // last column
		));
		
		Cell poNumber = itemListHeader.createCell(14);
		
		CellStyle poNumberColumnStyle = workbook.createCellStyle();
		poNumberColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		poNumberColumnStyle.setFont(fontColumn);
		poNumberColumnStyle.setWrapText(true);
		poNumberColumnStyle.setBorderBottom(BORDER_THIN);
		poNumberColumnStyle.setBorderTop(BORDER_THIN);
		poNumberColumnStyle.setBorderRight(BORDER_THIN);
		poNumberColumnStyle.setBorderLeft(BORDER_THIN);
		poNumber.setCellStyle(poNumberColumnStyle);
		poNumber.setCellValue("PO Number");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				15, // first column
				15 // last column
		));
		
		Cell poDate = itemListHeader.createCell(15);
		CellStyle poDateColumnStyle = workbook.createCellStyle();
		poDateColumnStyle.setWrapText(true);
		poDateColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		poDateColumnStyle.setFont(fontColumn);
		poDateColumnStyle.setBorderBottom(BORDER_THIN);
		poDateColumnStyle.setBorderTop(BORDER_THIN);
		poDateColumnStyle.setBorderRight(BORDER_THIN);
		poDateColumnStyle.setBorderLeft(BORDER_THIN);
		poDate.setCellStyle(poDateColumnStyle);
		poDate.setCellValue("PO Date");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				16, // first column
				16 // last column
		));
		
		Cell vendor = itemListHeader.createCell(16);
		CellStyle vendorColumnStyle = workbook.createCellStyle();
		vendorColumnStyle.setWrapText(true);
		vendorColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		vendorColumnStyle.setBorderBottom(BORDER_THIN);
		vendorColumnStyle.setBorderTop(BORDER_THIN);
		vendorColumnStyle.setBorderRight(BORDER_THIN);
		vendorColumnStyle.setBorderLeft(BORDER_THIN);
		vendorColumnStyle.setFont(fontColumn);
		vendor.setCellStyle(vendorColumnStyle);
		vendor.setCellValue("Vendor");
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				17, // first column
				17 // last column
		));
		
		Cell dcDateQty = itemListHeader.createCell(17);
		CellStyle dcDateColumnStyle = workbook.createCellStyle();
		dcDateColumnStyle.setWrapText(true);
		dcDateColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		dcDateColumnStyle.setBorderBottom(BORDER_THIN);
		dcDateColumnStyle.setBorderTop(BORDER_THIN);
		dcDateColumnStyle.setBorderRight(BORDER_THIN);
		dcDateColumnStyle.setBorderLeft(BORDER_THIN);
		dcDateColumnStyle.setFont(fontColumn);
		dcDateQty.setCellStyle(dcDateColumnStyle);
		dcDateQty.setCellValue("Grn Date");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				18, // first column
				18 // last column
		));
		
		Cell clientName = itemListHeader.createCell(18);
		CellStyle clientNameColumnStyle = workbook.createCellStyle();
		clientNameColumnStyle.setWrapText(true);
		clientNameColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		clientNameColumnStyle.setBorderBottom(BORDER_THIN);
		clientNameColumnStyle.setBorderTop(BORDER_THIN);
		clientNameColumnStyle.setBorderRight(BORDER_THIN);
		clientNameColumnStyle.setBorderLeft(BORDER_THIN);
		clientNameColumnStyle.setFont(fontColumn);
		clientName.setCellStyle(clientNameColumnStyle);
		clientName.setCellValue("DC Date");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				19, // first column
				19 // last column
		));
		
		Cell dcNum = itemListHeader.createCell(19);
		CellStyle dcNumColumnStyle = workbook.createCellStyle();
		dcNumColumnStyle.setWrapText(true);
		dcNumColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		dcNumColumnStyle.setBorderBottom(BORDER_THIN);
		dcNumColumnStyle.setBorderTop(BORDER_THIN);
		dcNumColumnStyle.setBorderRight(BORDER_THIN);
		dcNumColumnStyle.setBorderLeft(BORDER_THIN);
		dcNumColumnStyle.setFont(fontColumn);
		dcNum.setCellStyle(dcNumColumnStyle);
		dcNum.setCellValue("DC No.");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				10, // last row
				20, // first column
				20 // last column
		));
		
		Cell designDateCell = itemListHeader.createCell(20);
		CellStyle designDateolumnStyle = workbook.createCellStyle();
		designDateolumnStyle.setWrapText(true);
		designDateolumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		designDateolumnStyle.setBorderBottom(BORDER_THIN);
		designDateolumnStyle.setBorderTop(BORDER_THIN);
		designDateolumnStyle.setBorderRight(BORDER_THIN);
		designDateolumnStyle.setBorderLeft(BORDER_THIN);
		designDateolumnStyle.setFont(fontColumn);
		designDateCell.setCellStyle(designDateolumnStyle);
		designDateCell.setCellValue("Design Date");
		
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
		itemListFont.setFontHeightInPoints((short)8);
		threeSideborder.setFont(itemListFont);

		CellStyle threeSideborderRight = workbook.createCellStyle();
		XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRight.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
		threeSideborderRight.setBorderLeft(BORDER_THIN);
		threeSideborderRight.setBorderRight(BORDER_THIN);
		threeSideborderRight.setBorderBottom(BORDER_THIN);
		threeSideborderRight.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRight.setFont(itemListFont);
		
		CellStyle qtyborder = workbook.createCellStyle();
		XSSFDataFormat qtyborderformat = (XSSFDataFormat) workbook.createDataFormat();
		qtyborder.setDataFormat(qtyborderformat.getFormat("#,###.00"));
		qtyborder.setBorderLeft(BORDER_THIN);
		qtyborder.setBorderRight(BORDER_THIN);
		qtyborder.setBorderBottom(BORDER_THIN);
		qtyborder.setVerticalAlignment((short) (VERTICAL_TOP));
		qtyborder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		qtyborder.setFont(itemListFont);
		
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
		
		int rowCount = 11;
		for (SalesItem salesItem : salesItems) {
			String unitName=salesItem.getItem_units().getName();
			String key = salesItem.getId();
			String itemValue = (String) itemsList.get(key);
			int purchaseQtyStartsFrom = itemValue.indexOf("$");
			int noOrderQtyStartsFrom = itemValue.indexOf("&");
			int grnQtyStartsFrom = itemValue.indexOf("%");
			String dcQty1 = itemValue.substring(0,purchaseQtyStartsFrom);
			String orderedQty1= itemValue.substring(purchaseQtyStartsFrom + 1, noOrderQtyStartsFrom);
			String notOrderedQty1= itemValue.substring(noOrderQtyStartsFrom + 1, grnQtyStartsFrom);
			String grnQty1 = itemValue.substring(grnQtyStartsFrom + 1, itemValue.length());
			float dcQty=Float.parseFloat(dcQty1);
			float orderedQty=Float.parseFloat(orderedQty1);
			float notOrderedQty=Float.parseFloat(notOrderedQty1);
			float grnQty=Float.parseFloat(grnQty1);
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					1, // first column
					6// last column
			));
			
			
			
			Row row1 = editAccountSheet.createRow(rowCount);
			int length = salesItem.getDescription().length();
			if (salesItem.getDescription().length() > 35 && salesItem.getDescription().length() < 80) {
				// if(purchaseItem.getDescription().length()>80){
				row1.setHeightInPoints((float) (5.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}
			if (salesItem.getDescription().length() > 80) {
				// if(purchaseItem.getDescription().length()>80){
				row1.setHeightInPoints((float) (13.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}
			
			Cell slno = row1.createCell(0);
			slno.setCellStyle(threeSideborder);
			slno.setCellValue(salesItem.getSlNo());
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
			description.setCellValue(salesItem.getDescription());
			
			
			Cell qtyBoqCell = row1.createCell(7);
			qtyBoqCell.setCellStyle(qtyborder);
			Cell qtySiteCell = row1.createCell(8);
			qtySiteCell.setCellStyle(qtyborder);
			Cell orderedQtyCell = row1.createCell(9);
			orderedQtyCell.setCellStyle(qtyborder);
			Cell notOrderedQtyCell = row1.createCell(10);
			notOrderedQtyCell.setCellStyle(qtyborder);
			Cell deliveredQtyCell = row1.createCell(11);
			deliveredQtyCell.setCellStyle(qtyborder);
			Cell instoreQtyCell = row1.createCell(12);
			instoreQtyCell.setCellStyle(qtyborder);
			if(unitName.equals("Heading")) {
				qtyBoqCell.setCellValue("");
				qtySiteCell.setCellValue("");
				orderedQtyCell.setCellValue("");
				notOrderedQtyCell.setCellValue("");
				deliveredQtyCell.setCellValue("");
				instoreQtyCell.setCellValue("");
			}else {
				if(salesItem.getQuantity()==0) {
					qtyBoqCell.setCellValue(0);
					qtySiteCell.setCellValue(0);
				}else {
					qtyBoqCell.setCellValue(salesItem.getQuantity());
					qtySiteCell.setCellValue(salesItem.getQuantity());
				}
				
				orderedQtyCell.setCellValue(orderedQty);
				notOrderedQtyCell.setCellValue(notOrderedQty);
				deliveredQtyCell.setCellValue(dcQty);
				instoreQtyCell.setCellValue(grnQty);
			
			}
			
			String soItemId = salesItem.getId();
			
			List<DesignItems> designItemList =soDesignService.getDesignItemListBySOItemId(soItemId);
			if(unitName.equals("Heading")) {
				Cell modelCell = row1.createCell(13);
				modelCell.setCellStyle(threeSideborder);
				modelCell.setCellValue("");
				
				Cell poNumberCell = row1.createCell(14);
				poNumberCell.setCellStyle(threeSideborder);
				poNumberCell.setCellValue("");
				
				
				Cell poDtCell = row1.createCell(15);
				poDtCell.setCellStyle(threeSideborder);
				poDtCell.setCellValue("");
				
				
				Cell vednorCell = row1.createCell(16);
				vednorCell.setCellStyle(threeSideborder);
				vednorCell.setCellValue("");
				
				Cell grnNumCell = row1.createCell(17);
				grnNumCell.setCellStyle(threeSideborder);
				grnNumCell.setCellValue("");
				
				Cell dcClientCell = row1.createCell(18);
				dcClientCell.setCellStyle(threeSideborder);
				dcClientCell.setCellValue("");
				
				Cell dcNumCell = row1.createCell(19);
				dcNumCell.setCellStyle(threeSideborder);
				dcNumCell.setCellValue("");
			}else {
			
			Row modelNoRow;
			for (DesignItems designItem : designItemList ) {
				Set purchaseSet = new HashSet();
				String itemId = designItem.getItemId();
				Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
				Optional<SalesOrderDesign> designObj=soDesignService.findSalesOrderDesignById(designItem.getSalesOrderDesign().getId());
				
				Date designDate=designObj.get().getCreated();
				String designDateString1 = designDate.toString();
				String[] designDatearr1 = designDateString1.split(" ");

				// Formatting date to a required format
				String formattedDate5 = designDatearr1[0];
				formattedDate5 = formattedDate5.substring(formattedDate5.length() - 2, formattedDate5.length()) + "/"
						+ formattedDate5.substring(formattedDate5.length() - 5, formattedDate5.length() - 3) + "/"
						+ formattedDate5.substring(0, 4);
				
				Cell modelCell = row1.createCell(13);
				modelCell.setCellStyle(threeSideborder);
				modelCell.setCellValue(itemObj.get().getModel()+", qty="+designItem.getQuantity());
				
				Cell designDtCell =row1.createCell(20);
				designDtCell.setCellStyle(threeSideborder);
				designDtCell.setCellValue(formattedDate5);
				
				List<PurchaseItem> purchaseItemList = poItemService.getPurchaseItemListBySalesItemIdAndItemId(soItemId, itemId);
			
				for (PurchaseItem purchaseItem : purchaseItemList) {
					if(purchaseItem.getPurchaseOrder()!=null) {
					String purchaseId = purchaseItem.getPurchaseOrder().getPoNumber();
					System.out.println(purchaseId);
					if(purchaseId!=null) {
					Optional<PurchaseOrder> purchaseOrder = purchaseOrderService.findById(purchaseId);
					purchaseSet.add(purchaseOrder.get());
					}
					}
				}
				ArrayList<PurchaseOrder> purchaseList = new ArrayList<PurchaseOrder>(purchaseSet);
				Collections.sort(purchaseList);
				Row poRow;
				for (PurchaseOrder po : purchaseList) {
					String poNumb=po.getPoNumber();
					
					List<Grn> grnList = grnService.findGrnByPoNumber(poNumb);
					Date poDt = po.getCreated();
					String vendorPartyName = po.getParty().getPartyName();
					
					String dateString1 = poDt.toString();
					String[] arr1 = dateString1.split(" ");

					// Formatting date to a required format
					String formattedDate1 = arr1[0];
					formattedDate1 = formattedDate1.substring(formattedDate1.length() - 2, formattedDate1.length()) + "/"
							+ formattedDate1.substring(formattedDate1.length() - 5, formattedDate1.length() - 3) + "/"
							+ formattedDate1.substring(0, 4);
					
					
					
					Cell poNumberCell = row1.createCell(14);
					poNumberCell.setCellStyle(threeSideborder);
					poNumberCell.setCellValue(poNumb);
					
					
					Cell poDtCell = row1.createCell(15);
					poDtCell.setCellStyle(threeSideborder);
					poDtCell.setCellValue(formattedDate1);
					
					
					Cell vednorCell = row1.createCell(16);
					vednorCell.setCellStyle(threeSideborder);
					vednorCell.setCellValue(vendorPartyName);
					
					Row grnRow;
					for (Grn grn : grnList) {
						Date grnDt = grn.getCreated();
						String dateString2 = grnDt.toString();
						String[] arr2 = dateString2.split(" ");
						String formattedDate2 = arr2[0];
						formattedDate2 = formattedDate2.substring(formattedDate2.length() - 2, formattedDate2.length()) + "/"
								+ formattedDate2.substring(formattedDate2.length() - 5, formattedDate2.length() - 3) + "/"
								+ formattedDate2.substring(0, 4);
						
						Cell grnNumCell = row1.createCell(17);
						grnNumCell.setCellStyle(threeSideborder);
						grnNumCell.setCellValue(formattedDate2);
						
						//Cell grnDtCell = row1.createCell(11);
						//grnDtCell.setCellStyle(threeSideborder);
						//grnDtCell.setCellValue(formattedDate2);
						
						if(grn!=grnList.get(grnList.size()-1)) {
						rowCount++;
						grnRow=editAccountSheet.createRow(rowCount);
						row1=grnRow;
						}
					}
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
			
			
			List<DeliveryChallanItems> dcItemList =dcService.getDcItemListBySoItemId(soItemId); 
			Set dcSet = new HashSet(); 
			for(DeliveryChallanItems dcItem : dcItemList) { 
				if(dcItem.getTodaysQty()!=0) {
					int dcId =dcItem.getDeliveryChallan().getDcId(); 
					Optional<DeliveryChallan> dcObj =dcService.getDcById(dcId); 
					dcSet.add(dcObj.get()); 
					}
				}

				ArrayList<DeliveryChallan> dcList = new ArrayList<DeliveryChallan>(dcSet);
				Row dcRow; 
				for (DeliveryChallan dc : dcList) { 
					Date dcDt =dc.getItems().get(0).getCreated();
					String dateString3 = dcDt.toString();
					String[] arr3 = dateString3.split(" ");
					String formattedDate3 = arr3[0];
					formattedDate3 = formattedDate3.substring(formattedDate3.length() - 2,
							formattedDate3.length()) + "/" +
							formattedDate3.substring(formattedDate3.length() - 5, formattedDate3.length()
									- 3) + "/" + formattedDate3.substring(0, 4);


					//Cell dcDtCell = row1.createCell(10);
					//dcDtCell.setCellStyle(threeSideborder);
					//dcDtCell.setCellValue(formattedDate3);


					Cell dcClientCell = row1.createCell(18);
					dcClientCell.setCellStyle(threeSideborder);
					dcClientCell.setCellValue(formattedDate3);
					
					Cell dcNumCell = row1.createCell(19);
					dcNumCell.setCellStyle(threeSideborder);
					dcNumCell.setCellValue(dc.getDcId());
					
					if(dc!=dcList.get(dcList.size()-1)) {
					rowCount++; 
					dcRow=editAccountSheet.createRow(rowCount);
					row1=dcRow;
					}
					}
		}
			rowCount++;
 		}
		int rowLastItemCount = rowCount;
		
		
		
		
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		setBorders(workbook, editAccountSheet, rowLastItemCount);
		
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
	
	private void setBorders(Workbook workBook, Sheet sheet, int rowLastItemCount) {
		/*CellRangeAddress range = new CellRangeAddress(11,rowLastItemCount-1,0,11);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, range, sheet, workBook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, range, sheet, workBook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, range, sheet, workBook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, range, sheet, workBook);*/
		for (int i=11;i<rowLastItemCount;i++) {
			for (int j = 0; j <= 20; j++) {
				
			
				CellRangeAddress region = new CellRangeAddress(i,rowLastItemCount-1,j,20);
				
				RegionUtil.setBorderTop(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderRight(CellStyle.BORDER_THIN, region, sheet, workBook);
				RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, region, sheet, workBook);
			}
		}
		
	}
}
