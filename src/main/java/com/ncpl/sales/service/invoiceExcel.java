package com.ncpl.sales.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.InvoiceItem;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;

@Component
public class invoiceExcel extends AbstractXlsxView{

	
	@Autowired
	SalesService salesService;
	
	
	
	short VERTICAL_TOP = 0x0;
	short VERTICAL_TOP1 = 0x1;
	short VERTICAL_JUSTIFY = 0x2;
	short BORDER_THIN = 0x1;

	String company = "Neptune controls pvt ltd No.8/2(Old No.114), 2nd Cross 7th Main Road Nandidurga Extension Bangalore-560046 Contact : 080-40904685,7624964492 "
			+ "E-Mail : accounts@ncpl.co";
	String companyName = "";
	
	String qtn = "";
	
	String contactPerson = " Ms Sumathy ";
	String contactNo =" 7624919715 ";
	String gstNo="GSTIN : 29AADCN5426F1ZG";
	String excelHeading1 = "";
	String itemList = "";

	String terms = "Commercial Terms & Conditions :";
	String delivery = "";
	String warranty = "";
	String payment = "";
	String taxes = "";
	String Jurisdiction = "";
	String quote = "";
	
	String billingAddress = "";
	String shippingAddress = "";
	
	String s1 = "Complete solution for BMS, Lighting Control, CCTV & Security Systems, DDC Panels, Automation Panels, Lighting,panels, MCC & Starter Panels";
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	
	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");

	InvoiceExcelLogoService logoService = new InvoiceExcelLogoService();
	@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
		
		String billingAddr1 = "";
		String billingAddr2  = "";
		String billingCity = "";
		String billingPin = "";
		String billingPan = "";
		String billingGst = "";
		
		String shippingAddr1 = "";
		String shippingAddr2  = "";
		String shippingCity = "";
		String shippingPin = "";
		String shippingPan = "";
		String shippingGst = "";
		
		
		
		/*//Generating File Name 
		String fileName = "INV" + "_purchaseOrder.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);*/

		Map invMap =  (Map) model.get("invoiceData");
		Optional<Invoice> invObject =  (Optional<Invoice>) invMap.get("invoiceObj");
		
		//Generating File Name 
		String fileName = invObject.get().getInvoiceId() + "_INVOICE.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		
		String dcNo = (String) request.getAttribute("dcNo");
		String type = invObject.get().getType();
		List<InvoiceItem> invItemsList = (List<InvoiceItem>) request.getAttribute("invoiceItemsList");
		
		Date d = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String invDate = formatter.format(d);
		String invNumber = invObject.get().getInvoiceId();
		double invTotal = invObject.get().getTotal();
		double grandTotalInv = invObject.get().getGrandTotal();
		double gst = invTotal*(invObject.get().getGstRate()/100);
		List<InvoiceItem> invoiceItems = invObject.get().getItems();
		Optional<SalesOrder> salesObj = (Optional<SalesOrder>) invMap.get("salesObj");
        List<SalesItem> salesItems = salesObj.get().getItems();
        List<SalesItem> salesItemsReq = new ArrayList();
 
        
        Optional<DeliveryChallan> dcObj = (Optional<DeliveryChallan>) invMap.get("dcObj");
     
        List<DeliveryChallanItems>  dcItemsList =  new ArrayList();
        //This is to differentiate the items of with dc and without
		  if(dcNo.equalsIgnoreCase("all") || dcNo.equalsIgnoreCase("")) {
			  for (SalesItem salesItem : salesItems) { 
				  salesItemsReq.add(salesItem);
				  } 
			  }else if (dcNo.contains(",")) {
				  for (SalesItem salesItem : salesItems) { 
					  dcItemsList=(List<DeliveryChallanItems>) request.getAttribute("dcItemsList");
					  for (DeliveryChallanItems deliveryChallanItems : dcItemsList) {
						  if(deliveryChallanItems.getDescription().equalsIgnoreCase(salesItem.getId())){
							  salesItemsReq.add(salesItem);
						  }
				  }
					
				}
				
			}    
		  else {
			  for (SalesItem salesItem : salesItems) { 
				  dcItemsList = dcObj.get().getItems();
				  for (DeliveryChallanItems deliveryChallanItems : dcItemsList) {
					  if(deliveryChallanItems.getDescription().equalsIgnoreCase(salesItem.getId())){
						  salesItemsReq.add(salesItem);
					  }
				}
			  
			  }
		  }
        
      //  Optional<DeliveryChallan> dcObj = (Optional<DeliveryChallan>) invMap.get("dcObj");
        
        Party billAddress = (Party) request.getAttribute("billAddress");
        String stateName;

        if(billAddress!=null) {
        	  stateName = billAddress.getParty_city().getState().getName();
	        if(billAddress.getAddr2() == null || billAddress.getAddr2().equalsIgnoreCase("")){
	        	billingAddr2 = "";
	        }else{
	        	billingAddr2 = billAddress.getAddr2()+"\n";
	        }
	        
	        if(billAddress.getParty_city().getAreaCode() == null || billAddress.getParty_city().getAreaCode() == ""){
	        	billingPin = ""+"\n";
	        }else{
	        	billingPin = billAddress.getParty_city().getAreaCode()+"\n";
	        }
	        
	        if(billAddress.getGst() == null || billAddress.getGst() == ""){
	        	billingGst = "GSTIN "+""+"\n";
	        }else{
	        	billingGst = "GSTIN "+billAddress.getGst()+"\n";
	        }
	        
	        if(billAddress.getPan() == null || billAddress.getPan() == ""){
	        	billingPan = "PAN"+"";
	        }else{
	        	billingPan = "PAN"+billAddress.getPan()+"\n";
	        }
	        
	        billingAddress = billAddress.getPartyName()+"\n"+billAddress.getAddr1()+"\n"+
	        		billingAddr2 + billAddress.getParty_city().getName()+"-"+
	        		billingPin+ billingGst;
        }else {
        	Optional<PartyAddress> partyAddressOpt = (Optional<PartyAddress>) request.getAttribute("partyBillAddress");
        	PartyAddress partyBillAddress =partyAddressOpt.get();
        	 stateName = partyBillAddress.getPartyaddr_city().getState().getName();
        	if(partyBillAddress.getAddr2() == null || partyBillAddress.getAddr2().equalsIgnoreCase("")){
	        	billingAddr2 = "";
	        }else{
	        	billingAddr2 = partyBillAddress.getAddr2()+"\n";
	        }
        	if(partyBillAddress.getPartyaddr_city().getAreaCode() == null || partyBillAddress.getPartyaddr_city().getAreaCode() == ""){
	        	billingPin = ""+"\n";
	        }else{
	        	billingPin = partyBillAddress.getPartyaddr_city().getAreaCode()+"\n";
	        }
	        
	        if(partyBillAddress.getGst() == null || partyBillAddress.getGst() == ""){
	        	billingGst = "GSTIN "+""+"\n";
	        }else{
	        	billingGst = "GSTIN "+partyBillAddress.getGst()+"\n";
	        }
	        
	        billingAddress = partyBillAddress.getPartyName()+"\n"+partyBillAddress.getAddr1()+"\n"+
	        		billingAddr2 + partyBillAddress.getPartyaddr_city().getName()+"-"+
	        		billingPin+ billingGst;
	        

        }
        
        
     
        Party shipAddress = (Party) request.getAttribute("shipAddress");
        if(shipAddress!=null) {
	        if(shipAddress.getAddr2() == null || shipAddress.getAddr2().equalsIgnoreCase("")){
	        	shippingAddr2 = "";
	        }else{
	        	shippingAddr2 = shipAddress.getAddr2()+"\n";
	        }
	        
	        if(shipAddress.getParty_city().getAreaCode() == null || shipAddress.getParty_city().getAreaCode() == ""){
	        	shippingPin = ""+"\n";
	        }else{
	        	shippingPin = shipAddress.getParty_city().getAreaCode()+"\n";
	        }
	        
	//        if(shipAddress.getGst() == null || shipAddress.getGst() == ""){
	//        	shippingGst = "GSTIN/UIN"+""+"\n";
	//        }else{
	//        	shippingGst = "GSTIN/UIN"+shipAddress.getGst()+"\n";
	//        }
	//        
	        if(shipAddress.getPan() == null || shipAddress.getPan() == ""){
	        	shippingPan = "PAN"+"";
	        }else{
	        	shippingPan = "PAN"+shipAddress.getPan();
	        }
	        
	        shippingAddress = shipAddress.getPartyName()+"\n"+shipAddress.getAddr1()+"\n"+
	        		shippingAddr2 + shipAddress.getParty_city().getName()+"-"+
	        		shippingPin;
        }else {
        	Optional<PartyAddress> partyAddressOpt = (Optional<PartyAddress>) request.getAttribute("partyShippAddress");
        	PartyAddress partyShippAddress =partyAddressOpt.get();
        	if(partyShippAddress.getAddr2() == null || partyShippAddress.getAddr2().equalsIgnoreCase("")){
        		shippingAddr2 = "";
	        }else{
	        	shippingAddr2 = partyShippAddress.getAddr2()+"\n";
	        }
        	if(partyShippAddress.getPartyaddr_city().getAreaCode() == null || partyShippAddress.getPartyaddr_city().getAreaCode() == ""){
        		shippingPin = ""+"\n";
	        }else{
	        	shippingPin = partyShippAddress.getPartyaddr_city().getAreaCode()+"\n";
	        }
	        
	        if(partyShippAddress.getGst() == null || partyShippAddress.getGst() == ""){
	        	shippingGst = "GSTIN "+""+"\n";
	        }else{
	        	shippingGst = "GSTIN "+partyShippAddress.getGst()+"\n";
	        }
	        
	        shippingAddress = partyShippAddress.getPartyName()+"\n"+partyShippAddress.getAddr1()+"\n"+
	        		shippingAddr2 + partyShippAddress.getPartyaddr_city().getName()+"-"+
	        		shippingPin+ shippingGst;
	        
        }
		/*
		 * .................................Edit Device excel sheet
		 * format.................................
		 */

		Sheet editAccountSheet = workbook.createSheet("Invoice");
		editAccountSheet.setDefaultColumnWidth(9);
		//editAccountSheet.autoSizeColumn(11);
		
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
				4, // last row
				0, // first column
				5 // last column
		));

		
		 logoService.insertLogoInTemplate(workbook, editAccountSheet,
				 request);
		
		CellStyle mergestyle = workbook.createCellStyle();
		mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		Font headingFont = workbook.createFont();
		headingFont.setFontName("Calibri");
		headingFont.setFontHeight((short) (15.5 * 20));
		headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		mergestyle.setFont(headingFont);
		
		// For logo
		/*editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				4, // last row
				2, // first column
				6 // last column
		));*/

		// Insert Logo
		// logoService.insertLogoInTemplate(workbook, editAccountSheet, request);

		 Row Quotation = editAccountSheet.createRow(4);
		 
		/*Cell descriptionCell = secondRow.createCell(2);
		descriptionCell.setCellValue("Neptune Controls Pvt Ltd");
		CellStyle descriptionmergestyle = workbook.createCellStyle();
		
		descriptionmergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		descriptionmergestyle.setVerticalAlignment((short) (VERTICAL_TOP));
		descriptionmergestyle.setBorderBottom(BORDER_THIN);
		descriptionmergestyle.setBorderTop(BORDER_THIN);
		descriptionmergestyle.setBorderRight(BORDER_THIN);
		descriptionmergestyle.setBorderLeft(BORDER_THIN);

		Font descriptionFont = workbook.createFont();
		descriptionFont.setColor(HSSFColor.BLUE.index);
		descriptionFont.setFontName("Calibri");
		descriptionFont.setFontHeight((short) (7.5 * 50));
		descriptionFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		descriptionmergestyle.setFont(descriptionFont);
		descriptionmergestyle.setWrapText(true);
		descriptionCell.setCellStyle(descriptionmergestyle);*/

		CellStyle desc2tyle = workbook.createCellStyle();
		desc2tyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		desc2tyle.setVerticalAlignment((short) (VERTICAL_TOP));
		desc2tyle.setBorderLeft(BORDER_THIN);
		Font desc2Font = workbook.createFont();
		desc2Font.setColor(HSSFColor.BLUE.index);
		desc2Font.setFontName("Calibri");
		desc2Font.setFontHeight((short) (7.5 * 35));
		//vendorFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		desc2tyle.setFont(desc2Font);
		desc2tyle.setWrapText(true);
	
		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				4, // last row
				6, // first column
				13 // last column
		));
		Cell descriptionDetailCell = secondRow.createCell(6);
		descriptionDetailCell.setCellStyle(desc2tyle);
		descriptionDetailCell.setCellValue(s1);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				6, // last row
				0, // first column
				13 // last column
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
		headerCell.setCellValue("TAX INVOICE");
		
	
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				16, // last row
				0, // first column
				6 // last column
		));

		Row vendorRow = editAccountSheet.createRow(7);
		//vendorRow.setHeightInPoints((float) (2.5*editAccountSheet.getDefaultRowHeightInPoints()));
		//vendorRow.setHeightInPoints(float) (3.5*editAccountSheet.getDefaultRowHeightInPoints());
		Cell vendor = vendorRow.createCell(0);
		//vendor.setCellValue("Supplier:" +"\n"+ companyName +"\nGSTIN : 29AADCN5426F1ZG");
		CellStyle vendorstyle = workbook.createCellStyle();
		vendorstyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		vendorstyle.setVerticalAlignment((short) (VERTICAL_TOP));
		vendorstyle.setBorderLeft(BORDER_THIN);
		vendorstyle.setBorderRight(BORDER_THIN);
		vendorstyle.setBorderTop(BORDER_THIN);
		vendorstyle.setBorderBottom(BORDER_THIN);
		Font vendorFont = workbook.createFont();
		vendorFont.setFontName("Calibri");
		vendorFont.setFontHeight((short) (7.5 * 35));
		//vendorFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		vendorstyle.setFont(vendorFont);
		vendorstyle.setWrapText(true);
		vendor.setCellStyle(vendorstyle);
		//vendor.setCellValue("Supplier:" +"\n"+ companyName +"\n"+gstNo+"\nContact Number:"+contactNo);
		vendor.setCellValue("Neptune Controls Pvt Ltd."+"\n"+ "No.8/2(Old No.114), 2nd Cross 7th Main Road, Nandidurga Extension"+
		"\n"+"Banglaore- 560046"+"\n"+"GSTIN: 29AADCN5426F1ZG"+"\n"+"State Name : Karnataka, Code : 29"+"\n"+"CIN: U31200KA2011PTC056705"+
		"\n"+"Contact No.:7624964492"+"\n"+"E-Mail : accounts@ncpl.co"	);

		
		/*editAccountSheet.addMergedRegion(new CellRangeAddress(6, // first row
				13, // last row
				7, // first column
				13 // last column
		));*/

		// Row vendorRow2 = editAccountSheet.createRow(6);
		/*Cell vendor2 = vendorRow.createCell(8);
		//vendor2.setCellValue("Billing & Invoice:" +"\n"+ companyName +"\nPAN NO:123456");
		CellStyle vendorstyle2 = workbook.createCellStyle();
		vendorstyle2.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		vendorstyle2.setVerticalAlignment((short) (VERTICAL_TOP));
		vendorstyle2.setBorderLeft(BORDER_THIN);
		vendorstyle2.setBorderRight(BORDER_THIN);
		vendorstyle2.setBorderTop(BORDER_THIN);
		vendorstyle2.setBorderBottom(BORDER_THIN);
		Font vendorFont2 = workbook.createFont();
		vendorFont2.setFontName("Calibri");
		vendorFont2.setFontHeight((short) (7.5 * 35));
		//vendorFont2.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		vendorstyle2.setFont(vendorFont2);
		vendorstyle2.setWrapText(true);
		vendor2.setCellStyle(vendorstyle2);
		//vendor2.setCellValue("Billing & Invoice:" +"\n"+ companyName +"\nPAN NO:123456"+"\n"+gstNo);
		vendor2.setCellValue("Billing & Invoice:" +"\n"+ ""+"\nPAN NO:123456"+"\n"+gstNo);*/
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(17, // first row
				24, // last row
				0, // first column
				6// last column
		));
		
		Row billingShippingRow = editAccountSheet.createRow(17);
		Cell billCell = billingShippingRow.createCell(0);
		CellStyle bill = workbook.createCellStyle();
		bill.setBorderLeft(BORDER_THIN);
		bill.setBorderRight(BORDER_THIN);
		bill.setBorderTop(BORDER_THIN);
		bill.setBorderBottom(BORDER_THIN);
		bill.setWrapText(true);
		billCell.setCellStyle(vendorstyle);
	//	billCell.setCellStyle(bill);
		/*billCell.setCellValue("Bill to"+"\n"+"Neptune Controls Pvt Ltd. -2019-2020"+"\n"+ "# 09, Ground Floor, 2nd Cross 7th Main, Nandidurg Extension"+
				"\n"+"Banglaore- 560047"+"\n"+"GSTIN/UIN: 29AADCN5426F1ZG"+"\n"+"State Name : Karnataka, Code : 29"+"\n"+" CIN: U31200KA2011PTC056705"+
				"\n"+"E-Mail : accounts@ncpl.co");*/
		billCell.setCellValue("Bill to"+"\n"+billingAddress);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(17, // first row
				24, // last row
				7, // first column
				13// last column
		));
		
		Cell shippingCell = billingShippingRow.createCell(7);
		shippingCell.setCellStyle(vendorstyle);
		//shippingCell.setCellStyle(bill);
		/*shippingCell.setCellValue("Ship to"+"\n"+"Neptune Controls Pvt Ltd. -2019-2020"+"\n"+ "# 09, Ground Floor, 2nd Cross 7th Main, Nandidurg Extension"+
				"\n"+"Banglaore- 560047"+"\n"+"GSTIN/UIN: 29AADCN5426F1ZG"+"\n"+"State Name : Karnataka, Code : 29"+"\n"+" CIN: U31200KA2011PTC056705"+
				"\n"+"E-Mail : accounts@ncpl.co");*/
		shippingCell.setCellValue("Ship to"+"\n"+shippingAddress);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				9, // last row
				7, // first column
				10// last column
		));
		Cell invoice = vendorRow.createCell(7);
		CellStyle descDetailStyle = workbook.createCellStyle();
		descDetailStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		descDetailStyle.setWrapText(true);
		descDetailStyle.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		Font descDetailFont = workbook.createFont();
		descDetailFont.setFontName("Calibri");
		descDetailFont.setFontHeight((short) (7.5 * 35));
		descDetailStyle.setFont(descDetailFont);
		invoice.setCellStyle(descDetailStyle);
		invoice.setCellValue("Invoice No :"+"\n"+"Invoice Dated :");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				9, // last row
				11, // first column
				13// last column
		));
	    
		Cell invoiceValues = vendorRow.createCell(11);
		invoiceValues.setCellStyle(descDetailStyle);
		invoiceValues.setCellValue(invNumber+"\n"+invDate);

		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				12, // last row
				7, // first column
				10// last column
		));
		
		Row delivery = editAccountSheet.createRow(10);
		Cell deliveryCell = delivery.createCell(7);
		deliveryCell.setCellStyle(descDetailStyle);
		deliveryCell.setCellValue("Delivery Date :"+"\n"+"Delivery Challan No :");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				12, // last row
				11, // first column
				13// last column
		));
		
		Cell deliveryCellValues = delivery.createCell(11);
		deliveryCellValues.setCellStyle(descDetailStyle);
		if(dcNo!=""){
		deliveryCellValues.setCellValue(invDate+"\n"+dcNo);
		}else{
		deliveryCellValues.setCellValue(""+"\n"+"");
		}
		editAccountSheet.addMergedRegion(new CellRangeAddress(13, // first row
				13, // last row
				7, // first column
				10// last column
		));
		
		Row poNO = editAccountSheet.createRow(13);
		Cell poCell = poNO.createCell(7);
		poCell.setCellStyle(descDetailStyle);
		poCell.setCellValue("Client PO NO/Qt");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(13, // first row
				13, // last row
				11, // first column
				13// last column
		));
		
		Cell poCellValues = poNO.createCell(11);
		poCellValues.setCellStyle(descDetailStyle);
		poCellValues.setCellValue(salesObj.get().getClientPoNumber());
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(14, // first row
				14, // last row
				7, // first column
				10// last column
		));
		
		Row poDate = editAccountSheet.createRow(14);
		Cell poDateCell = poDate.createCell(7);
		poDateCell.setCellStyle(descDetailStyle);
		poDateCell.setCellValue("PO Date:");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(14, // first row
				14, // last row
				11, // first column
				13// last column
		));
		
		Cell poDateCellValues = poDate.createCell(11);
		poDateCellValues.setCellStyle(descDetailStyle);
		Date poDateValue = salesObj.get().getClientPoDate();
		if(poDateValue!=null) {
		String poDateVal = formatter.format(poDateValue);
		poDateCellValues.setCellValue(poDateVal);
		}else {
			String poDateVal = formatter.format(new Date());
			poDateCellValues.setCellValue(poDateVal);

		}
		editAccountSheet.addMergedRegion(new CellRangeAddress(15, // first row
			16, // last row
			7, // first column
			13// last column
		));
		
//		editAccountSheet.addMergedRegion(new CellRangeAddress(12, // first row
//				12, // last row
//				7, // first column
//				10// last column
//		));
//		
//		Row quotation = editAccountSheet.createRow(12);
//		Cell quot = quotation.createCell(7);
//		quot.setCellStyle(descDetailStyle);
//		quot.setCellValue("Quaotation Number");
//		
//		editAccountSheet.addMergedRegion(new CellRangeAddress(12, // first row
//				12, // last row
//				11, // first column
//				13// last column
//		));
//		
//		Cell quotNumber = quotation.createCell(11);
//		quotNumber.setCellStyle(descDetailStyle);
//		quotNumber.setCellValue("");
//		
//		
//		
//		editAccountSheet.addMergedRegion(new CellRangeAddress(13, // first row
//				13, // last row
//				7, // first column
//				10// last column
//		));
//		
//		Row quotDate = editAccountSheet.createRow(13);
//		Cell quotD = quotDate.createCell(7);
//		quotD.setCellStyle(descDetailStyle);
//		quotD.setCellValue("Quotation Date");
//		
//		editAccountSheet.addMergedRegion(new CellRangeAddress(13, // first row
//				13, // last row
//				11, // first column
//				13// last column
//		));
//		
//		Cell quotDValues = quotDate.createCell(11);
//		quotDValues.setCellStyle(descDetailStyle);
//		quotDValues.setCellValue(invDate);
		
		Row itemListIntro = editAccountSheet.createRow(24);
		Cell itemStarter = itemListIntro.createCell(0);
		itemStarter.setCellValue(itemList);

		Font fontColumn = workbook.createFont();
		fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumn.setFontHeight((short) (7.5 * 35));
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(25, // first row
				27, // last row
				0, // first column
				0 // last column
		));
		
		
		Row itemListHeader = editAccountSheet.createRow(25);
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

		editAccountSheet.addMergedRegion(new CellRangeAddress(25, // first row
				27, // last row
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
		desc.setCellValue("Particulars");

		editAccountSheet.addMergedRegion(new CellRangeAddress(25, // first row
				27, // last row
				6, // first column
				6 // last column
		));
		
		Cell gstRate = itemListHeader.createCell(6);
		CellStyle gstRateStyle = workbook.createCellStyle();
		gstRateStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		gstRateStyle.setFont(fontColumn);
		gstRateStyle.setWrapText(true);
		gstRateStyle.setBorderBottom(BORDER_THIN);
		gstRateStyle.setBorderTop(BORDER_THIN);
		gstRateStyle.setBorderRight(BORDER_THIN);
		gstRateStyle.setBorderLeft(BORDER_THIN);
		gstRate.setCellStyle(gstRateStyle);
		gstRate.setCellValue("HSN/SAC Code");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(25, // first row
				27, // last row
				7, // first column
				7 // last column
		));
		
		Cell hsn = itemListHeader.createCell(7);
		CellStyle hsnStyle = workbook.createCellStyle();
		hsnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		hsnStyle.setFont(fontColumn);
		hsnStyle.setBorderBottom(BORDER_THIN);
		hsnStyle.setBorderTop(BORDER_THIN);
		hsnStyle.setBorderRight(BORDER_THIN);
		hsnStyle.setBorderLeft(BORDER_THIN);
		hsn.setCellStyle(hsnStyle);
		hsn.setCellValue("Qty");

		editAccountSheet.addMergedRegion(new CellRangeAddress(25, // first row
				27, // last row
				8, // first column
				9 // last column
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
	
		modelNo.setCellValue("Supply"+"\n"+"Rate/Unit");

		editAccountSheet.addMergedRegion(new CellRangeAddress(25, // first row
				27, // last row
				10, // first column
				11 // last column
		));
		
		Cell qty = itemListHeader.createCell(10);
		CellStyle qtyStyle = workbook.createCellStyle();
		qtyStyle.setWrapText(true);
		qtyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		qtyStyle.setFont(fontColumn);
		qtyStyle.setBorderBottom(BORDER_THIN);
		qtyStyle.setBorderTop(BORDER_THIN);
		qtyStyle.setBorderRight(BORDER_THIN);
		qtyStyle.setBorderLeft(BORDER_THIN);
		qty.setCellStyle(qtyStyle);
		qty.setCellValue("Installation"+"\n"+"Rate/Unit");

		editAccountSheet.addMergedRegion(new CellRangeAddress(25, // first row
				27, // last row
				12, // first column
				13 // last column
		));
		
		Cell Amount = itemListHeader.createCell(12);
		CellStyle AmountColumnStyle = workbook.createCellStyle();
		AmountColumnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		AmountColumnStyle.setBorderBottom(BORDER_THIN);
		AmountColumnStyle.setBorderTop(BORDER_THIN);
		AmountColumnStyle.setBorderRight(BORDER_THIN);
		AmountColumnStyle.setBorderLeft(BORDER_THIN);
		AmountColumnStyle.setFont(fontColumn);
		Amount.setCellStyle(AmountColumnStyle);
		Amount.setCellValue("Taxable Amount");
		
		
		//This Style is for the lower half after the items list
		CellStyle AmountStyle = workbook.createCellStyle();
		/*XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
	    AmountStyle.setDataFormat(format.getFormat("#,###.##"));*/
		//AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		AmountStyle.setBorderBottom(BORDER_THIN);
		AmountStyle.setBorderTop(BORDER_THIN);
		AmountStyle.setBorderRight(BORDER_THIN);
		AmountStyle.setBorderLeft(BORDER_THIN);
		Font amountFont = workbook.createFont();
		amountFont.setFontHeight((short) (7.5 * 35));
		AmountStyle.setFont(amountFont);
		System.out.println(AmountStyle.getFillForegroundColor());
		
		//This style is added to add comma in numbers in grandtotal Field
		CellStyle grandTotalStyle = workbook.createCellStyle();
		XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
		grandTotalStyle.setDataFormat(format.getFormat("#,###"));
		//AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		grandTotalStyle.setBorderBottom(BORDER_THIN);
		grandTotalStyle.setBorderTop(BORDER_THIN);
		grandTotalStyle.setBorderRight(BORDER_THIN);
		grandTotalStyle.setBorderLeft(BORDER_THIN);
		Font grandTotalStyleFont = workbook.createFont();
		grandTotalStyleFont.setFontHeight((short) (7.5 * 35));
		grandTotalStyle.setFont(grandTotalStyleFont);
		
		//This style is for displaying decimal points upto two decimal point
		CellStyle twoDecimalStyle = workbook.createCellStyle();
		XSSFDataFormat twoDecimalStyleformat = (XSSFDataFormat) workbook.createDataFormat();
		twoDecimalStyle.setDataFormat(twoDecimalStyleformat.getFormat("#,###.00"));
		//AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		twoDecimalStyle.setBorderBottom(BORDER_THIN);
		twoDecimalStyle.setBorderTop(BORDER_THIN);
		twoDecimalStyle.setBorderRight(BORDER_THIN);
		twoDecimalStyle.setBorderLeft(BORDER_THIN);
		Font twoDecimalStyleStyleFont = workbook.createFont();
		twoDecimalStyleStyleFont.setFontHeight((short) (7.5 * 35));
		twoDecimalStyle.setFont(twoDecimalStyleStyleFont);
		
		
		
        // This are for defining the borders with different cells wherever required
		CellStyle AmountcellStyle = workbook.createCellStyle();
		AmountcellStyle.setBorderRight(BORDER_THIN);
		AmountcellStyle.setBorderBottom(BORDER_THIN);
		
		CellStyle leftBorderStyle = workbook.createCellStyle();
		leftBorderStyle.setBorderLeft(BORDER_THIN);
		
		CellStyle threeSideborder = workbook.createCellStyle();
		threeSideborder.setWrapText(true);
		XSSFDataFormat threeSideborderformat = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborder.setDataFormat(threeSideborderformat.getFormat("#,###"));
		threeSideborder.setVerticalAlignment((short) (VERTICAL_TOP));
		//threeSideborder.setBorderLeft(BORDER_THIN);
		//threeSideborder.setBorderRight(BORDER_THIN);
		//threeSideborder.setBorderBottom(BORDER_THIN);
		threeSideborder.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		Font itemListFont = workbook.createFont();
		itemListFont.setFontHeight((short) (7.5 * 35));
		threeSideborder.setFont(itemListFont);
		
		CellStyle threeSideborderRight = workbook.createCellStyle();
		XSSFDataFormat threeSideborderRightformat = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRight.setDataFormat(threeSideborderRightformat.getFormat("#,###"));
		//threeSideborder.setBorderLeft(BORDER_THIN);
		//threeSideborder.setBorderRight(BORDER_THIN);
		//threeSideborder.setBorderBottom(BORDER_THIN);
		threeSideborderRight.setVerticalAlignment((short) (VERTICAL_TOP));
		threeSideborderRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRight.setFont(itemListFont);
		
		CellStyle fourSideborder = workbook.createCellStyle();
		fourSideborder.setVerticalAlignment((short) (VERTICAL_TOP));
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
		XSSFDataFormat DecimalStyleformat = (XSSFDataFormat) workbook.createDataFormat();
		rightborder.setDataFormat(DecimalStyleformat.getFormat("#,###.00"));
		rightborder.setFont(itemListFont);
		rightborder.setBorderRight(BORDER_THIN);
		rightborder.setVerticalAlignment((short) (VERTICAL_TOP));
		rightborder.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		
		CellStyle topborder = workbook.createCellStyle();
		topborder.setBorderTop(BORDER_THIN);
		
		
		int rowCount = 28;
		int slNumber =1;
		float totalAmount = 0;
		float gstAmount = 0;
		float grandTotal = 0;
		float sgstAmount =0;
		float cgstAmount =0;
		float roundoff =0;
		
		
		for (SalesItem salesItem : salesItemsReq) {
		
			float quantity =0;
			//This is to set dc quantity if invoice selected as supply and dc no..
			if(dcItemsList.size()>0) {
			for (DeliveryChallanItems dcItem : dcItemsList) {
				if(dcItem.getDescription().equalsIgnoreCase(salesItem.getId())){
					quantity =dcItem.getTodaysQty();
				}
			}}else {
				quantity = salesItem.getQuantity();
			}
		    
			if(quantity!=0) {
			//Optional<ItemMaster> itemMasterObject = itemMasterService.getItemById(purchaseItem.getModelNo());
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					1, // first column
					 5// last column
			));
//			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
//					rowCount, // last row
//					5, // first column
//					 6// last column
//			));
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					8, // first column
					 9// last column
			));
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					10, // first column
					 11// last column
			));
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					12, // first column
					13// last column
			));
			
			
		Row itemList = editAccountSheet.createRow(rowCount);
      
		if (salesItem.getDescription().length() > 37 && salesItem.getDescription().length() < 65) {
			// if(purchaseItem.getDescription().length()>80){
			itemList.setHeightInPoints((float) (2.5 * editAccountSheet.getDefaultRowHeightInPoints()));
		}
		if (salesItem.getDescription().length() > 65) {
			// if(purchaseItem.getDescription().length()>80){
			itemList.setHeightInPoints((float) (3.5 * editAccountSheet.getDefaultRowHeightInPoints()));
		}
		Cell slno = itemList.createCell(0);
		slno.setCellStyle(leftborder);
		Cell description = itemList.createCell(1);
		CellStyle descriptionStyle = workbook.createCellStyle();
		descriptionStyle.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		descriptionStyle.setWrapText(true);
		descriptionStyle.setFont(itemListFont);
		description.setCellStyle(descriptionStyle);
		Cell hsnCell = itemList.createCell(6);
		hsnCell.setCellStyle(fourSideborder);
		Cell qtyCell = itemList.createCell(7);
		qtyCell.setCellStyle(rightborder);
		Cell supplyRate = itemList.createCell(8);
		supplyRate.setCellStyle(rightborder);
		Cell installationRate = itemList.createCell(10);
		installationRate.setCellStyle(rightborder);
		Cell taxRate = itemList.createCell(12);
		taxRate.setCellStyle(rightborder);
		
		slno.setCellValue(salesItem.getSlNo());
		description.setCellValue(salesItem.getDescription());
		hsnCell.setCellValue(salesItem.getHsnCode());
		qtyCell.setCellValue(quantity);
		if(invObject.get().getType().equalsIgnoreCase("Supply")) {
			supplyRate.setCellValue(salesItem.getUnitPrice());
			installationRate.setCellValue(0.0);
			double taxableValue =salesItem.getUnitPrice()*quantity;
			taxRate.setCellValue(Math.round(taxableValue * 100.0) / 100.0);
		}else if(invObject.get().getType().equalsIgnoreCase("Service")){
			supplyRate.setCellValue(0.0);
			installationRate.setCellValue(salesItem.getServicePrice());
			double taxableValue =salesItem.getServicePrice()*quantity;
			taxRate.setCellValue(Math.round(taxableValue * 100.0) / 100.0);
		}
		
		rowCount++;
		slNumber++;
			}
		}
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				10, // first column
				 11// last column
		));
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				12, // first column
				 13// last column
		));
		int rowLastItemCount = rowCount;
		Row total = editAccountSheet.createRow(rowCount);
		Cell firstBorder = total.createCell(0);
		for(int i=1;i<10;i++){
			total.createCell(i).setCellStyle(topborder);
		}
		CellStyle afterItems = workbook.createCellStyle();
		afterItems.setBorderTop(BORDER_THIN);
		afterItems.setBorderLeft(BORDER_THIN);
		firstBorder.setCellStyle(afterItems);
		Cell totalCell = total.createCell(10);
		Cell totalvalue = total.createCell(12);
		
		totalvalue.setCellStyle(grandTotalStyle);
		totalCell.setCellStyle(AmountStyle);
		totalCell.setCellValue("Total");
		totalvalue.setCellValue(invTotal);
		rowCount++;
		
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				10, // first column
				 11// last column
		));
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				12, // first column
				 13// last column
		));
		
		Row gstInvRow = editAccountSheet.createRow(rowCount);
		Cell gstInvfirstBorder = gstInvRow.createCell(0);
		gstInvfirstBorder.setCellStyle(leftBorderStyle);
		Cell gstInvCell = gstInvRow.createCell(10);
		Cell gstInvvalue = gstInvRow.createCell(12);
		//cgstvalue.setCellStyle(twoDecimalStyle);
		gstInvCell.setCellStyle(AmountStyle);
		if(stateName.equalsIgnoreCase("karnataka")) {
		gstInvCell.setCellValue("GST @"+invObject.get().getGstRate()+"%");
		}else {
			gstInvCell.setCellValue("IGST @"+invObject.get().getGstRate()+"%");
		}
		//This is for displaying number with two decimal point only in the excel
		Double gsamount = Math.round(gst * 100.0) / 100.0;
		
		System.out.println(Math.round(cgstAmount * 100) / 100);
		if((cgstAmount%10)==0){
			gstInvvalue.setCellStyle(AmountStyle);
		}else{
			gstInvvalue.setCellStyle(twoDecimalStyle);
		}
		gstInvvalue.setCellValue(gsamount);
		rowCount++;
		
		
		
		if(stateName.equalsIgnoreCase("karnataka")) {
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				10, // first column
				 11// last column
		));
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				12, // first column
				 13// last column
		));
		Row sgst = editAccountSheet.createRow(rowCount);
		Cell gstfirstBorder = sgst.createCell(0);
		gstfirstBorder.setCellStyle(leftBorderStyle);
		Cell gstCell = sgst.createCell(10);
		Cell gstvalue = sgst.createCell(12);
		//gstvalue.setCellStyle(twoDecimalStyle);
		gstCell.setCellStyle(AmountStyle);
		gstCell.setCellValue("SGST @"+(invObject.get().getGstRate()/2)+"%");
		sgstAmount = (float) (invTotal*(invObject.get().getGstRate()/(2*100)));
		//This is for displaying number with two decimal point only in the excel
		Double sgsamount = Math.round(sgstAmount * 100.0) / 100.0;
		System.out.println(sgstAmount);
		if((sgstAmount%10)==0){
			gstvalue.setCellStyle(AmountStyle);
		}else{
		gstvalue.setCellStyle(twoDecimalStyle);
		}
		gstvalue.setCellValue(sgsamount);
		rowCount++;
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				10, // first column
				 11// last column
		));
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				12, // first column
				 13// last column
		));
		
		Row cgst = editAccountSheet.createRow(rowCount);
		Cell cgstfirstBorder = cgst.createCell(0);
		cgstfirstBorder.setCellStyle(leftBorderStyle);
		Cell cgstCell = cgst.createCell(10);
		Cell cgstvalue = cgst.createCell(12);
		//cgstvalue.setCellStyle(twoDecimalStyle);
		cgstCell.setCellStyle(AmountStyle);
		cgstCell.setCellValue("CGST @"+(invObject.get().getGstRate()/2)+"%");
		cgstAmount = (float) (invTotal*(invObject.get().getGstRate()/(2*100)));
		//This is for displaying number with two decimal point only in the excel
		Double cgsamount = Math.round(cgstAmount * 100.0) / 100.0;
		
		System.out.println(Math.round(cgstAmount * 100) / 100);
		if((cgstAmount%10)==0){
			cgstvalue.setCellStyle(AmountStyle);
		}else{
			cgstvalue.setCellStyle(twoDecimalStyle);
		}
		cgstvalue.setCellValue(cgsamount);
		rowCount++;
		}
		/*editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				10, // first column
				 11// last column
		));
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				12, // first column
				 13// last column
		));
		Row grandtotalRoundoff = editAccountSheet.createRow(rowCount);
		Cell grandtotalRoundofffirstBorder = grandtotalRoundoff.createCell(0);
		grandtotalRoundofffirstBorder.setCellStyle(leftBorderStyle);
		Cell grandtotalRoundoffCell = grandtotalRoundoff.createCell(10);
		Cell grandtotalRoundoffvalue = grandtotalRoundoff.createCell(12);
		AmountStyle.setWrapText(true);
		grandtotalRoundoffvalue.setCellStyle(twoDecimalStyle);
		grandtotalRoundoffCell.setCellStyle(AmountStyle);
		grandtotalRoundoffCell.setCellValue("Round Off");
		gstAmount = (float) (totalAmount*0.18);
		grandTotal = gstAmount+totalAmount;
		roundoff = grandTotal - Math.round(grandTotal * 100) / 100;
		Double roundOffamount = Math.round(roundoff * 100.0) / 100.0;
	    //grandTotal = gstAmount+totalAmount;
		if((roundoff%10)==0){
			grandtotalRoundoffvalue.setCellStyle(AmountStyle);
		}else{
			grandtotalRoundoffvalue.setCellStyle(twoDecimalStyle);
		}
		grandtotalRoundoffvalue.setCellValue(roundOffamount);
		rowCount++;*/
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				10, // first column
				 11// last column
		));
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				12, // first column
				 13// last column
		));
		Row grandtotal = editAccountSheet.createRow(rowCount);
		Cell grandtotalfirstBorder = grandtotal.createCell(0);
		grandtotalfirstBorder.setCellStyle(leftBorderStyle);
		Cell grandtotalCell = grandtotal.createCell(10);
		Cell grandtotalvalue = grandtotal.createCell(12);
		AmountStyle.setWrapText(true);
		grandtotalvalue.setCellStyle(grandTotalStyle);
		grandtotalCell.setCellStyle(AmountStyle);
		grandtotalCell.setCellValue("Grand Total");
		//grandtotalvalue.setCellValue(grandTotal);
		grandtotalvalue.setCellValue(Math.round(grandTotalInv * 100) / 100);
		rowCount++;

		
		/*Row grandtotalRoundoff = editAccountSheet.createRow(rowCount);
		Cell grandtotalRoundofffirstBorder = grandtotalRoundoff.createCell(0);
		grandtotalRoundofffirstBorder.setCellStyle(leftBorderStyle);
		Cell grandtotalRoundoffCell = grandtotalRoundoff.createCell(9);
		Cell grandtotalRoundoffvalue = grandtotalRoundoff.createCell(11);
		AmountStyle.setWrapText(true);
		grandtotalRoundoffvalue.setCellStyle(AmountStyle);
		grandtotalRoundoffCell.setCellStyle(AmountStyle);
		grandtotalRoundoffCell.setCellValue("Round Off");
		roundoff = grandTotal - Math.round(grandTotal * 100) / 100;
	    //grandTotal = gstAmount+totalAmount;
		grandtotalRoundoffvalue.setCellValue(roundoff);
		rowCount++;*/
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				13 // last column
		));
		String numberInWords =  convertToIndianCurrency((int) Math.round(grandTotalInv * 100) / 100);
		//numberInWords = numberInWords.replace("£ 00/100", "");
		//String s =numberInWords.substring(0, 1);
		String capitalnumberInWords = capitalize(numberInWords);
		Row grandtotalInWords = editAccountSheet.createRow(rowCount);
		Cell totalInwords = grandtotalInWords.createCell(0);
		//totalInwords.setCellValue("Rupees (In Words) :"+numberInWords +"Only");
	   CellStyle grandtotalInWordsstyle = workbook.createCellStyle();
	   grandtotalInWordsstyle.setBorderLeft(BORDER_THIN);
	   grandtotalInWordsstyle.setBorderRight(BORDER_THIN);
	   grandtotalInWordsstyle.setBorderBottom(BORDER_THIN);
	   grandtotalInWordsstyle.setBorderTop(BORDER_THIN);
       Font gtFont = workbook.createFont();
       //gtFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
       gtFont.setFontHeight((short) (7.5 * 35));
       grandtotalInWordsstyle.setFont(gtFont);
       totalInwords.setCellStyle(grandtotalInWordsstyle);
       totalInwords.setCellValue("Rupees (In Words) :"+capitalnumberInWords);
      
		
     //  totalInwords.setCellValue("Rupees (In Words) :"+numberInWords +"Only");
		rowCount++;
		
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		//Cell style for terms and condition
		CellStyle tncStyle = workbook.createCellStyle();
		Font tncFont = workbook.createFont();
		tncFont.setFontHeight((short) (7.5 * 30));
		tncStyle.setBorderLeft(BORDER_THIN);
		tncStyle.setBorderRight(BORDER_THIN);
		tncStyle.setBorderTop(BORDER_THIN);
		//tncStyle.setBorderBottom(BORDER_THIN);
		tncStyle.setFont(tncFont);
		
		//For making termsBold
		CellStyle termsStyle = workbook.createCellStyle();
		termsStyle.setBorderLeft(BORDER_THIN);
		termsStyle.setBorderRight(BORDER_THIN);
		termsStyle.setBorderBottom(BORDER_THIN);
		termsStyle.setBorderTop(BORDER_THIN);
		Font termsFont = workbook.createFont();
		termsFont.setFontHeight((short) (7.5 * 35));
		termsFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		termsStyle.setFont(termsFont);
		
		//For adding border on right..
		CellStyle borderRight = workbook.createCellStyle();
		borderRight.setBorderRight(BORDER_THIN);
		
		int rowStart = rowCount;
		Row term = editAccountSheet.createRow(rowCount);
		Cell termsCell = term.createCell(0);
		termsCell.setCellStyle(termsStyle);
		termsCell.setCellValue(terms);
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		//rowCount++;
		Row deliver = editAccountSheet.createRow(rowCount);
		Cell deliverCell = deliver.createCell(0);
		deliverCell.setCellStyle(tncStyle);
		deliverCell.setCellValue("E &O.E>");
		//deliverCell.setCellValue(delivery);
		rowCount++;
		Cell deliverRightBorder = deliver.createCell(11);
		deliverRightBorder.setCellStyle(borderRight);
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		Row Warranty = editAccountSheet.createRow(rowCount);
		Cell WarrantyCell = Warranty.createCell(0);
		WarrantyCell.setCellStyle(tncStyle);
		WarrantyCell.setCellValue("1.Subject to 'Bangalore' Jurisdiction Only");
		//WarrantyCell.setCellValue(warranty);
		Cell WarrantyRightBorder = Warranty.createCell(11);
		WarrantyRightBorder.setCellStyle(borderRight);
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		
		Row Payment = editAccountSheet.createRow(rowCount);
		Cell PaymentCell = Payment.createCell(0);
		PaymentCell.setCellStyle(tncStyle);
		PaymentCell.setCellValue("Interest of 2% per month for payments made after 30 days of bill date.");
		//PaymentCell.setCellValue(payment);
		Cell PaymentRightBorder = Payment.createCell(11);
		PaymentRightBorder.setCellStyle(borderRight);
		rowCount++;
		
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowStart, // first row
				rowCount-1, // last row
				8, // first column
				13 // last column
		));
		

	//	setBordersToMergedCells(workbook, editAccountSheet);
		Cell authName = term.createCell(8);
		CellStyle authStyle =workbook.createCellStyle();
		authStyle.setBorderLeft(BORDER_THIN);
		authStyle.setBorderRight(BORDER_THIN);
		authStyle.setBorderBottom(BORDER_THIN);
		authStyle.setBorderTop(BORDER_THIN);
		Font authFont = workbook.createFont();
		authFont.setFontHeight((short) (7.5 * 30));
		authStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		authStyle.setWrapText(true);
		authStyle.setFont(authFont);
		// authName.setCellValue(accountExcelDecrp);
		authName.setCellStyle(authStyle);
		authName.setCellValue("Neptune Controls Pvt Ltd" + "\n "+"\n "+"\n "+"  Authorized Signatory ");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowStart+4, // first row
				rowStart+4, // last row
				0, // first column
				13 // last column
		));
		
		
		
		Row bank = editAccountSheet.createRow(rowStart+4);
		CellStyle bankStyle = workbook.createCellStyle();
		bankStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		bankStyle.setFont(fontColumn);
		Cell bankInfo = bank.createCell(0);
		bankInfo.setCellStyle(bankStyle);
		bankInfo.setCellValue("Neptune Controls Pvt Ltd.State Bank of India- A/c No : 37658677572. IFSC Code : SBIN0011349.Branch : SBI Incube Bangalore");
		
		setBordersToMergedCells(workbook, editAccountSheet,rowLastItemCount);
	    //workbook.write(stream);

	}

	

	private void setBordersToMergedCells(Workbook workBook, Sheet sheet, int rowLastItemCount) {
		int numMerged = sheet.getNumMergedRegions();
		for (int i = 0; i < numMerged; i++) {
			CellRangeAddress mergedRegions = sheet.getMergedRegion(i);
			
			
			//RegionUtil.setRightBorderColor(IndexedColors.WHITE.getIndex(), mergedRegions, sheet, workBook);
			RegionUtil.setBorderTop(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			RegionUtil.setBorderRight(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			
		}
	}
	
	//For converting number to words..
	  @SuppressWarnings("unused")
	public  String convertToIndianCurrency(float num) {
	        BigDecimal bd = new BigDecimal(num);
	        long number = bd.longValue();
	        long no = bd.longValue();
	        int decimal = (int) (bd.remainder(BigDecimal.ONE).doubleValue() * 100);
	        int digits_length = String.valueOf(no).length();
	        int i = 0;
	        ArrayList<String> str = new ArrayList<>();
	        HashMap<Integer, String> words = new HashMap<>();
	        words.put(0, "");
	        words.put(1, "One");
	        words.put(2, "Two");
	        words.put(3, "Three");
	        words.put(4, "Four");
	        words.put(5, "Five");
	        words.put(6, "Six");
	        words.put(7, "Seven");
	        words.put(8, "Eight");
	        words.put(9, "Nine");
	        words.put(10, "Ten");
	        words.put(11, "Eleven");
	        words.put(12, "Twelve");
	        words.put(13, "Thirteen");
	        words.put(14, "Fourteen");
	        words.put(15, "Fifteen");
	        words.put(16, "Sixteen");
	        words.put(17, "Seventeen");
	        words.put(18, "Eighteen");
	        words.put(19, "Nineteen");
	        words.put(20, "Twenty");
	        words.put(30, "Thirty");
	        words.put(40, "Forty");
	        words.put(50, "Fifty");
	        words.put(60, "Sixty");
	        words.put(70, "Seventy");
	        words.put(80, "Eighty");
	        words.put(90, "Ninety");
	        String digits[] = {"", "Hundred", "Thousand", "Lakh", "Crore"};
	        while (i < digits_length) {
	            int divider = (i == 2) ? 10 : 100;
	            number = no % divider;
	            no = no / divider;
	            i += divider == 10 ? 1 : 2;
	            if (number > 0) {
	                int counter = str.size();
	                String plural = (counter > 0 && number > 9) ? "s" : "";
	                String tmp = (number < 21) ? words.get(Integer.valueOf((int) number)) + " " + digits[counter] + plural : words.get(Integer.valueOf((int) Math.floor(number / 10) * 10)) + " " + words.get(Integer.valueOf((int) (number % 10))) + " " + digits[counter] + plural;                
	                str.add(tmp);
	            } else {
	                str.add("");
	            }
	        }
	 
	        Collections.reverse(str);
	        String Rupees = String.join(" ", str).trim();
	 
	        String paise = (decimal) > 0 ? " And Paise " + words.get(Integer.valueOf((int) (decimal - decimal % 10))) + " " + words.get(Integer.valueOf((int) (decimal % 10))) : "";
	        return  Rupees +" Only" ;
	    }
	
	  public  String capitalize(String str) {
			String output = str.substring(0, 1).toUpperCase() + str.substring(1);

		    return output;
		}
	
	  
	}


