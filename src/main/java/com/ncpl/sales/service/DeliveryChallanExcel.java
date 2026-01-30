package com.ncpl.sales.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.security.User;

@Component
public class DeliveryChallanExcel extends AbstractXlsxView{

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

	DcExcellogoservice logoService = new DcExcellogoservice();
	DcSignatureLogo signService = new DcSignatureLogo();
	@SuppressWarnings("unused")
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
		
		
		
		
		@SuppressWarnings("unchecked")
		Map<String,Optional<DeliveryChallan> > dcMap =  (Map<String, Optional<DeliveryChallan>>) model.get("dcData");
		Optional<DeliveryChallan> dcObject =  dcMap.get("dcObj");
		Date dateCreated = dcObject.get().getItems().get(0).getCreated();
		String poNum = (String) dcObject.get().get("clientPoNumber");
		//Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String dcCurrentDate = formatter.format(dateCreated);
		//String dcCurrentDate  = date.toString();
		System.out.println(dcCurrentDate);
		List<DeliveryChallanItems> items = dcObject.get().getItems();
		String option = (String) request.getAttribute("option");
		//Generating File Name 
		String fileName = "Dc" + "_deliveryChallan"+"-"+ option+".xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		User user = (User) request.getAttribute("user");
		Map itemsList = (Map) request.getAttribute("map");

		 Party billAddress = (Party) request.getAttribute("billAddress");
		   if(billAddress!=null) {
		        if(billAddress.getAddr2() == null || billAddress.getAddr2() == ""){
		        	billingAddr2 = "";
		        }else{
		        	billingAddr2 = billAddress.getAddr2()+"\n";
		        }
		        
		        // Determine billing pin: prefer Party.pin, fallback to City.areaCode
		        String partyPin = billAddress.getPin();
		        if(partyPin != null && !partyPin.trim().isEmpty()){
		        	billingPin = partyPin + "\n";
		        }else if(billAddress.getParty_city() != null && billAddress.getParty_city().getAreaCode() != null
		        		&& !billAddress.getParty_city().getAreaCode().trim().isEmpty()){
		        		billingPin = billAddress.getParty_city().getAreaCode()+"\n";
		        }else{
		        		billingPin = ""+"\n";
		        }
		        
		        if(billAddress.getGst() == null || billAddress.getGst() == ""){
		        	billingGst = "GSTIN/UIN"+""+"\n";
		        }else{
		        	billingGst = "GSTIN/UIN"+billAddress.getGst()+"\n";
		        }
		        
		        if(billAddress.getPan() == null || billAddress.getPan() == ""){
		        	billingPan = "PAN"+""; 
		        }else{
		        	billingPan = "PAN"+billAddress.getPan()+"\n";
		        }
		        
		        billingAddress = billAddress.getPartyName()+"\n"+billAddress.getAddr1()+"\n"+
		        		billingAddr2 + billAddress.getParty_city().getName()+"-"+
		        		billingPin+ billingGst+billingPan;
		   }else {
			   @SuppressWarnings("unchecked")
			   	Optional<PartyAddress> partyAddressOpt = (Optional<PartyAddress>) request.getAttribute("partyBillAddress");
		        	PartyAddress partyBillAddress =partyAddressOpt.get();
		        	if(partyBillAddress.getAddr2() == null || partyBillAddress.getAddr2().equalsIgnoreCase("")){
			        	billingAddr2 = "";
			        }else{
			        	billingAddr2 = partyBillAddress.getAddr2()+"\n";
			        }
		        	// Determine billing pin: prefer PartyAddress.pin, fallback to City.areaCode
		        	String partyAddrPin = partyBillAddress.getPin();
		        	if(partyAddrPin != null && !partyAddrPin.trim().isEmpty()){
		        		billingPin = partyAddrPin + "\n";
		        	}else if(partyBillAddress.getPartyaddr_city() != null && partyBillAddress.getPartyaddr_city().getAreaCode() != null
		        			&& !partyBillAddress.getPartyaddr_city().getAreaCode().trim().isEmpty()){
		        		billingPin = partyBillAddress.getPartyaddr_city().getAreaCode()+"\n";
		        	}else{
		        		billingPin = ""+"\n";
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
			        if(shipAddress.getAddr2() == null || shipAddress.getAddr2() == ""){
			        	shippingAddr2 = "";
			        }else{
			        	shippingAddr2 = shipAddress.getAddr2()+"\n";
			        }
			        
			        // Determine shipping pin: prefer Party.pin, fallback to City.areaCode
			        String shipPartyPin = shipAddress.getPin();
			        if(shipPartyPin != null && !shipPartyPin.trim().isEmpty()){
			        	shippingPin = shipPartyPin + "\n";
			        }else if(shipAddress.getParty_city() != null && shipAddress.getParty_city().getAreaCode() != null
			        		&& !shipAddress.getParty_city().getAreaCode().trim().isEmpty()){
			        		shippingPin = shipAddress.getParty_city().getAreaCode()+"\n";
			        }else{
			        		shippingPin = ""+"\n";
			        }
			        
			        if(shipAddress.getGst() == null || shipAddress.getGst() == ""){
			        		shippingGst = "GSTIN/UIN"+""+"\n";
			        	}else{
			        		shippingGst = "GSTIN/UIN"+shipAddress.getGst()+"\n";
			        }
			        
			        if(shipAddress.getPan() == null || shipAddress.getPan() == ""){
			        		shippingPan = "PAN"+""; 
			        	}else{
			        		shippingPan = "PAN"+shipAddress.getPan();
			        	}
			        
			        shippingAddress = shipAddress.getPartyName()+"\n"+shipAddress.getAddr1()+"\n"+
			        		shippingAddr2 + shipAddress.getParty_city().getName()+"-"+
			        		shippingPin+ shippingGst+shippingPan;
		        }else {
		        	@SuppressWarnings("unchecked")
					Optional<PartyAddress> partyAddressOpt = (Optional<PartyAddress>) request.getAttribute("partyShippAddress");
		        	PartyAddress partyShippAddress =partyAddressOpt.get();
		        	if(partyShippAddress.getAddr2() == null || partyShippAddress.getAddr2().equalsIgnoreCase("")){
		        			shippingAddr2 = "";
			        	}else{
			        		shippingAddr2 = partyShippAddress.getAddr2()+"\n";
			        	}
		        	// Determine shipping pin: prefer PartyAddress.pin, fallback to City.areaCode
		        	String shipPartyAddrPin = partyShippAddress.getPin();
		        	if(shipPartyAddrPin != null && !shipPartyAddrPin.trim().isEmpty()){
		        		shippingPin = shipPartyAddrPin + "\n";
		        	}else if(partyShippAddress.getPartyaddr_city() != null && partyShippAddress.getPartyaddr_city().getAreaCode() != null
		        			&& !partyShippAddress.getPartyaddr_city().getAreaCode().trim().isEmpty()){
		        		shippingPin = partyShippAddress.getPartyaddr_city().getAreaCode()+"\n";
		        	}else{
		        		shippingPin = ""+"\n";
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

		Sheet editAccountSheet = workbook.createSheet("Delivery Challan");
		PrintSetup ps = editAccountSheet.getPrintSetup();
		//editAccountSheet.getPrintSetup().setLandscape(true);
		editAccountSheet.setFitToPage(true);
		editAccountSheet.setAutobreaks(true);

		//ps.setFitWidth((short) 1);
	//	ps.setFitHeight((short) 1);
		editAccountSheet.getPrintSetup().setPaperSize(HSSFPrintSetup.A4_PAPERSIZE); 
		editAccountSheet.setDefaultColumnWidth(11);
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
				4 // last column
		));

		
		 logoService.insertLogoInTemplate(workbook, editAccountSheet,
				 request);
		
		CellStyle mergestyle = workbook.createCellStyle();
		mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		Font headingFont = workbook.createFont();
		headingFont.setFontName("Calibri");
		headingFont.setFontHeightInPoints((short)16);
		headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		mergestyle.setFont(headingFont);
		
		// For logo
		/*editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				2, // last row
				2, // first column
				9 // last column
		));

		// Insert Logo
		// logoService.insertLogoInTemplate(workbook, editAccountSheet, request);

		
		 
		Cell descriptionCell = secondRow.createCell(2);
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
		//descriptionFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		descriptionmergestyle.setFont(descriptionFont);
		descriptionmergestyle.setWrapText(true);
		descriptionCell.setCellStyle(descriptionmergestyle);*/


		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				1, // last row
				5, // first column
				9 // last column
		));
		
		String addr ="No.8/2(Old No.114), 2nd Cross 7th Main Road";
		Row address = editAccountSheet.createRow(1);
		Cell addressCell = address.createCell(5);
		Font descDetailFont = workbook.createFont();
		descDetailFont.setColor(HSSFColor.BLUE.index);
		descDetailFont.setFontName("Calibri");
		descDetailFont.setFontHeightInPoints((short)16);
		CellStyle addressCellStyle = workbook.createCellStyle();
		addressCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		addressCellStyle.setBorderRight(BORDER_THIN);
		addressCellStyle.setBorderLeft(BORDER_THIN);
		addressCellStyle.setFont(descDetailFont);
		addressCell.setCellStyle(addressCellStyle);
		addressCell.setCellValue(addr);
		editAccountSheet.addMergedRegion(new CellRangeAddress(2, // first row
				2, // last row
				5, // first column
				9 // last column
		));
		String addr2 ="Nandidurga Extension Bangalore-560046 ";
		Row address2 = editAccountSheet.createRow(2);
		Cell addressCell2 = address2.createCell(5);
		
		CellStyle addressCellStyle2 = workbook.createCellStyle();
		addressCellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		addressCellStyle2.setBorderRight(BORDER_THIN);
		addressCellStyle2.setBorderLeft(BORDER_THIN);
		addressCellStyle2.setFont(descDetailFont);
		addressCell2.setCellStyle(addressCellStyle2);
		addressCell2.setCellValue(addr2);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(3, // first row
				3, // last row
				5, // first column
				9 // last column
		));
		
		String contactDetails = "Ph:080-23332160/40904685";
		Row contact = editAccountSheet.createRow(3);
		Cell contactCell = contact.createCell(5);
		Font contactDetailFont = workbook.createFont();
		contactDetailFont.setFontName("Calibri");
		contactDetailFont.setFontHeightInPoints((short)16);
		CellStyle contactCellStyle = workbook.createCellStyle();
		contactCellStyle.setFont(contactDetailFont);
		contactCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		contactCellStyle.setBorderRight(BORDER_THIN);
		contactCellStyle.setBorderLeft(BORDER_THIN);
		contactCellStyle.setBorderBottom(BORDER_THIN);
		contactCell.setCellStyle(contactCellStyle);
		contactCell.setCellValue(contactDetails);
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(4, // first row
				4, // last row
				5, // first column
				9 // last column
		));
		
		String contactDetails2 = "Email: Accounts@ncpl.co/admin@ncpl.co";
		Row contact2 = editAccountSheet.createRow(4);
		Cell contactCell2 = contact2.createCell(5);
		
		CellStyle contactCellStyle2 = workbook.createCellStyle();
		contactCellStyle2.setFont(contactDetailFont);
		contactCellStyle2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		contactCellStyle2.setBorderRight(BORDER_THIN);
		contactCellStyle2.setBorderLeft(BORDER_THIN);
		contactCellStyle2.setBorderBottom(BORDER_THIN);
		contactCell2.setCellStyle(contactCellStyle2);
		contactCell2.setCellValue(contactDetails2);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				6, // last row
				0, // first column
				9 // last column
		));

		Row header = editAccountSheet.createRow(5);
		Cell headerCell = header.createCell(0);
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		Font headerFont = workbook.createFont();
		headerFont.setFontName("Calibri");
		headerFont.setFontHeightInPoints((short)18);
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		headerCell.setCellStyle(headerStyle);
		headerCell.setCellValue("Delivery Challan" + "-" + option);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				13, // last row
				0, // first column
				2// last column
		));
		
		Font billFont = workbook.createFont();
		billFont.setFontName("Calibri");
		billFont.setFontHeightInPoints((short)16);
		
		Row billingShippingRow = editAccountSheet.createRow(7);
		Cell billCell = billingShippingRow.createCell(0);
		CellStyle bill = workbook.createCellStyle();
		bill.setBorderRight(BORDER_THIN);
		bill.setBorderLeft(BORDER_THIN);
		bill.setBorderBottom(BORDER_THIN);
		bill.setBorderTop(BORDER_THIN);
		bill.setFont(billFont);
		bill.setWrapText(true);
		billCell.setCellStyle(bill);
		billCell.setCellValue("\n"+"Bill to"+"\n"+billingAddress);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				13, // last row
				3, // first column
				5// last column
		));
		
		Cell shippingCell = billingShippingRow.createCell(3);
		shippingCell.setCellStyle(bill);
		shippingCell.setCellValue("\n"+"Ship to"+"\n"+shippingAddress);
		
		
		  Font dcDetailsFont = workbook.createFont();
		  dcDetailsFont.setFontName("Calibri");
		  dcDetailsFont.setFontHeightInPoints((short)16);
		 
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				7, // last row
				6, // first column
				7// last column
		));
		
		Cell dcNo = billingShippingRow.createCell(6);
		CellStyle dcNoStyle = workbook.createCellStyle();
		dcNoStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		dcNoStyle.setFont(dcDetailsFont);
		dcNo.setCellStyle(dcNoStyle);
		dcNo.setCellValue("DC No");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
				7, // last row
				8, // first column
				9// last column
		));
		Cell dcNoValue= billingShippingRow.createCell(8);
		dcNoValue.setCellStyle(dcNoStyle);
		dcNoValue.setCellValue(dcObject.get().getDcId());
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(8, // first row
				8, // last row
				6, // first column
				7// last column
		));
		Row dcDateRow = editAccountSheet.createRow(8);
		Cell dcDate = dcDateRow.createCell(6);
		dcDate.setCellStyle(dcNoStyle);
		dcDate.setCellValue("Date");
		
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(8, // first row
				8, // last row
				8, // first column
				9// last column
		));
		
		Cell dcDateValue = dcDateRow.createCell(8);
		dcDateValue.setCellStyle(dcNoStyle);
		dcDateValue.setCellValue(dcCurrentDate);
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(9, // first row
				9, // last row
				6, // first column
				7// last column
		));
		
		Row  quoteRow = editAccountSheet.createRow(9);
		Cell quotecell = quoteRow.createCell(6);
		quotecell.setCellStyle(dcNoStyle);
		quotecell.setCellValue("Quote/P.O Number:");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(9, // first row
				9, // last row
				8, // first column
				9// last column
		));
		
		Cell quotecellValue = quoteRow.createCell(8);
		quotecellValue.setCellStyle(dcNoStyle);
		quotecellValue.setCellValue(poNum);
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				11, // last row
				6, // first column
				7// last column
		));
		
		Row dispatch = editAccountSheet.createRow(10);
		Cell dispatchCell = dispatch.createCell(6);
		dispatchCell.setCellStyle(dcNoStyle);
		dispatchCell.setCellValue("Dispatched:");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				11, // last row
				8, // first column
				9// last column
		));
		
		Cell dispatchCellValue = dispatch.createCell(8);
		dispatchCellValue.setCellStyle(dcNoStyle);
		dispatchCellValue.setCellValue("");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(12, // first row
			   13, // last row
				6, // first column
				7// last column
		));
		
		Row eway = editAccountSheet.createRow(12);
		Cell ewayBill = eway.createCell(6);
		ewayBill.setCellStyle(dcNoStyle);
		ewayBill.setCellValue("E-Way Bill No:");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(12, // first row
				13, // last row
				8, // first column
				9// last column
		));
		
		Cell ewayBillValue = eway.createCell(8);
		ewayBillValue.setCellStyle(dcNoStyle);
		ewayBillValue.setCellValue("");
		
		//Applying style to column header
		
		Font fontColumn = workbook.createFont();
		fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumn.setFontHeightInPoints((short)15);
		CellStyle columnStyle = workbook.createCellStyle();
		columnStyle.setFont(fontColumn);
		columnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		columnStyle.setBorderLeft(BORDER_THIN);
		columnStyle.setBorderRight(BORDER_THIN);
		columnStyle.setBorderBottom(BORDER_THIN);
		columnStyle.setBorderTop(BORDER_THIN);
		Row headerItems = editAccountSheet.createRow(15);
		Cell poNo = headerItems.createCell(0);
		poNo.setCellStyle(columnStyle);
		poNo.setCellValue("PO SL NO");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(15, // first row
				15, // last row
				1, // first column
				5// last column
		));
		Cell particulars = headerItems.createCell(1);
		particulars.setCellStyle(columnStyle);
		particulars.setCellValue("PARTICULARS");
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(15, // first row
				15, // last row
				6, // first column
				7// last column
		));
		Cell modelNo = headerItems.createCell(6);
		modelNo.setCellStyle(columnStyle);
		modelNo.setCellValue("MODEL NUMBER");
		
		Cell unit = headerItems.createCell(8);
		unit.setCellStyle(columnStyle);
		unit.setCellValue("UNIT");
		
		/*Cell design = headerItems.createCell(9);
		design.setCellStyle(columnStyle);
		design.setCellValue("DESIGNS");*/
		
		Cell quantity = headerItems.createCell(9);
		quantity.setCellStyle(columnStyle);
		quantity.setCellValue("QUANTITY");
		
		int rowCount = 16;
		int slNumber =1;
		
		//Styles for columns inside Dc
		//Font inside itemdetails
		Font itemdetails = workbook.createFont();
		itemdetails.setFontName("Calibri");
		itemdetails.setFontHeightInPoints((short)15);
		
		CellStyle itemStyle = workbook.createCellStyle();
		itemStyle.setBorderLeft(BORDER_THIN);
		itemStyle.setBorderRight(BORDER_THIN);
		itemStyle.setBorderBottom(BORDER_THIN);
		itemStyle.setBorderTop(BORDER_THIN);
		//itemStyle.setBorderLeft(BORDER_THIN);
		itemStyle.setWrapText(true);
		itemStyle.setFont(itemdetails);
		
		CellStyle fourSideborderForValuesRightAligned = workbook.createCellStyle();
		XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
		fourSideborderForValuesRightAligned.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
		fourSideborderForValuesRightAligned.setWrapText(true);
		fourSideborderForValuesRightAligned.setBorderLeft(BORDER_THIN);
		fourSideborderForValuesRightAligned.setBorderRight(BORDER_THIN);
		fourSideborderForValuesRightAligned.setBorderBottom(BORDER_THIN);
		fourSideborderForValuesRightAligned.setBorderTop(BORDER_THIN);
		fourSideborderForValuesRightAligned.setFont(itemdetails);
		
		CellStyle fourSideborderWithoutDecValuesRightAligned = workbook.createCellStyle();
		fourSideborderWithoutDecValuesRightAligned.setWrapText(true);
		fourSideborderWithoutDecValuesRightAligned.setBorderLeft(BORDER_THIN);
		fourSideborderWithoutDecValuesRightAligned.setBorderRight(BORDER_THIN);
		fourSideborderWithoutDecValuesRightAligned.setBorderBottom(BORDER_THIN);
		fourSideborderWithoutDecValuesRightAligned.setBorderTop(BORDER_THIN);
		fourSideborderWithoutDecValuesRightAligned.setFont(itemdetails);
		
		CellStyle quantStyle = workbook.createCellStyle();
		quantStyle.setBorderRight(BORDER_THIN);
		quantStyle.setFont(itemdetails);
		
		for (DeliveryChallanItems dcItem : items) {
			if(dcItem.getTodaysQty()!=0) {
				
			String key = Integer.toString(dcItem.getDcItemId());
			String designitems = (String) itemsList.get(key);	
			designitems=designitems.replaceAll("\\[", "").replaceAll("\\]","");
			Row itemsRow = editAccountSheet.createRow(rowCount);
			if (dcItem.getDescription().length() > 35 && dcItem.getDescription().length() < 65) {
				// if(purchaseItem.getDescription().length()>80){
				itemsRow.setHeightInPoints((float) (3.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}
			if (dcItem.getDescription().length() > 65) {
				// if(purchaseItem.getDescription().length()>80){
				itemsRow.setHeightInPoints((float) (5.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}
			Cell slno = itemsRow.createCell(0);
			slno.setCellStyle(itemStyle);
			//slno.setCellValue(slNumber);
			slno.setCellValue(dcItem.getSlNo());
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					1, // first column
					5// last column
			));
			
			
			Cell particular = itemsRow.createCell(1);
			particular.setCellStyle(itemStyle);
			particular.setCellValue(dcItem.getDescription());
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					6, // first column
					7// last column
			));
			
			
			Cell modelNum = itemsRow.createCell(6);
			modelNum.setCellStyle(itemStyle);
		//	modelNum.setCellValue(dcItem.getSoModelNo());
			modelNum.setCellValue(designitems);
			Cell units = itemsRow.createCell(8);
			units.setCellStyle(itemStyle);
			String unitsValue = (String) dcItem.get("unit");
		    units.setCellValue(unitsValue);
		  /*  Cell designCell = itemsRow.createCell(9);
		    designCell.setCellStyle(itemStyle);
		    designCell.setCellValue(designitems);*/
			Cell quant = itemsRow.createCell(9);
			if(unitsValue.equalsIgnoreCase("Nos")) {
				quant.setCellStyle(fourSideborderWithoutDecValuesRightAligned);
			}else {
				quant.setCellStyle(fourSideborderForValuesRightAligned);
			}
			
			if(dcItem.getTodaysQty() <=0) {
				quant.setCellValue(dcItem.getDeliveredQuantity());
			}else {
			quant.setCellValue(dcItem.getTodaysQty());
			}
			rowCount++;
			slNumber++;
		}
		}
		if(slNumber<17) {
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount+17, // last row
					0, // first column
					9// last column
			));
		}
		for(int i=0;i<17;i++) {
			rowCount++;
		}
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				9// last column
		));
		
		Row acknow = editAccountSheet.createRow(rowCount);
		Cell acknowledgmetn = acknow.createCell(0);
		Font ackFont = workbook.createFont();
		ackFont.setFontHeightInPoints((short)15);
		ackFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		CellStyle ackStyle = workbook.createCellStyle();
	    ackStyle.setFont(ackFont);
	    acknowledgmetn.setCellStyle(ackStyle);
		acknowledgmetn.setCellValue("Acknowledgement:-");
		rowCount++;
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount+1, // last row
				0, // first column
				9// last column
		));
		Row ackDetails = editAccountSheet.createRow(rowCount);
		Font ackDetailsFont = workbook.createFont();
		ackDetailsFont.setFontHeightInPoints((short)15);
		CellStyle ackDetailStyle = workbook.createCellStyle();
		ackDetailStyle.setWrapText(true);
		ackDetailStyle.setFont(ackDetailsFont);
		Cell ackDetailsCell = ackDetails.createCell(0);
		ackDetailsCell.setCellStyle(ackDetailStyle);
		ackDetailsCell.setCellValue("Please acknowledgement the receipt of the above material  one copy duly signed."+"\n"+"Received the above the materials in order and good condition.");
		setBordersToMergedCells(workbook, editAccountSheet);
		
		CellStyle tncStyle = workbook.createCellStyle();
		Font tncFont = workbook.createFont();
		tncFont.setFontHeightInPoints((short)15);
		tncStyle.setFont(tncFont);
		//tncStyle.setBottomBorderColor(IndexedColors.WHITE.getIndex());
		tncStyle.setBorderLeft(BORDER_THIN);
		//tncStyle.setBorderRight(BORDER_THIN);
		//tncStyle.setBorderTop(BORDER_THIN);
		//tncStyle.setBorderBottom(BORDER_THIN);
		
		int rowCountTerms = rowCount+3;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCountTerms, // first row
				rowCountTerms, // last row
				0, // first column
				5// last column
		));
		
		Row consignee = editAccountSheet.createRow(rowCountTerms);
		Cell consigneeDetails = consignee.createCell(0);
		CellStyle consigneeStyle = workbook.createCellStyle();
		Font consignFont = workbook.createFont();
		consignFont.setFontHeightInPoints((short)15);
		consignFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		consigneeStyle.setFont(consignFont);
		consigneeDetails.setCellStyle(consigneeStyle);
		consigneeDetails.setCellValue("CONSIGNEE'S DETAILS:");
	    rowCountTerms++;
	    setBordersToMergedCells(workbook, editAccountSheet);
	    editAccountSheet.addMergedRegion(new CellRangeAddress(rowCountTerms, // first row
				rowCountTerms, // last row
				0, // first column
				5// last column
		));
	    Row receiveSign = editAccountSheet.createRow(rowCountTerms);
		Cell receiveSignDetails = receiveSign.createCell(0);
		receiveSignDetails.setCellStyle(tncStyle);
		receiveSignDetails.setCellValue("Receiver Signature:");
        rowCountTerms++;
	    
	    editAccountSheet.addMergedRegion(new CellRangeAddress(rowCountTerms, // first row
		rowCountTerms, // last row
				0, // first column
				5// last column
		));
	    Row receiver = editAccountSheet.createRow(rowCountTerms);
		Cell receiverDetails = receiver.createCell(0);
		receiverDetails.setCellStyle(tncStyle);
		receiverDetails.setCellValue("Receiver Name:");
        rowCountTerms++;
	    
	    editAccountSheet.addMergedRegion(new CellRangeAddress(rowCountTerms, // first row
				rowCountTerms, // last row
				0, // first column
				5// last column
		));
	    Row mobile = editAccountSheet.createRow(rowCountTerms);
		Cell mobileDetails = mobile.createCell(0);
		mobileDetails.setCellStyle(tncStyle);
		mobileDetails.setCellValue("Mobile Number:");
        rowCountTerms++;
	    
        
		/*
		 * editAccountSheet.addMergedRegion(new CellRangeAddress(rowCountTerms, // first
		 * row rowCountTerms, // last row 0, // first column 5// last column ));
		 */
	    Row companySeal = editAccountSheet.createRow(rowCountTerms);
		Cell compSealCell = companySeal.createCell(0);
		compSealCell.setCellStyle(tncStyle);
		compSealCell.setCellValue("Company Seal::");
		
		CellRangeAddress cellRangeAddressCompny = new CellRangeAddress(rowCountTerms, rowCountTerms, 0, 5);
		editAccountSheet.addMergedRegion(cellRangeAddressCompny);

		// Creates the cell
		

		// Sets the borders to the merged cell
		RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, cellRangeAddressCompny, editAccountSheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, cellRangeAddressCompny, editAccountSheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, cellRangeAddressCompny, editAccountSheet, workbook);
		//RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, cellRangeAddress1, editAccountSheet, workbook);
		
		
		/*
		 * editAccountSheet.addMergedRegion(new CellRangeAddress(rowCountTerms-4, //
		 * first row rowCountTerms, // last row 6, // first column 9 // last column ));
		 */		 
		
		/*
		 * Cell authName = consignee.createCell(6); CellStyle authStyle
		 * =workbook.createCellStyle(); authStyle.setBorderLeft(BORDER_THIN);
		 * authStyle.setBorderRight(BORDER_THIN);
		 * authStyle.setBorderBottom(BORDER_THIN); authStyle.setBorderTop(BORDER_THIN);
		 * Font authFont = workbook.createFont(); authFont.setFontHeight((short) (7.5 *
		 * 35)); authStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		 * authStyle.setWrapText(true); authStyle.setFont(authFont); //
		 * authName.setCellValue(accountExcelDecrp); authName.setCellStyle(authStyle);
		 * authName.setCellValue("Neptune Controls Pvt Ltd" +
		 * "\n "+"\n "+"\n "+"  Authorized Signatory ");
		 */
		
		/*
		 * CellRangeAddress cellRangeAddress1 = new CellRangeAddress(rowCountTerms-4,
		 * rowCountTerms, 6, 9); editAccountSheet.addMergedRegion(cellRangeAddress1);
		 * 
		 * // Creates the cell
		 * 
		 * 
		 * // Sets the borders to the merged cell
		 * RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, cellRangeAddress1,
		 * editAccountSheet, workbook); RegionUtil.setBorderLeft(CellStyle.BORDER_THIN,
		 * cellRangeAddress1, editAccountSheet, workbook);
		 * RegionUtil.setBorderRight(CellStyle.BORDER_THIN, cellRangeAddress1,
		 * editAccountSheet, workbook);
		 * RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, cellRangeAddress1,
		 * editAccountSheet, workbook);
		 */
		signService.insertLogoInTemplate(workbook, editAccountSheet, request, rowCountTerms,user);
		Cell authName = consignee.createCell(6);
		CellStyle authStyle =workbook.createCellStyle();
		authStyle.setBorderLeft(BORDER_THIN);
		authStyle.setBorderRight(BORDER_THIN);
		authStyle.setBorderBottom(BORDER_THIN);
		authStyle.setBorderTop(BORDER_THIN);
		Font authFont = workbook.createFont();
		authFont.setFontHeightInPoints((short)15);
		authStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		authStyle.setWrapText(true);
		authStyle.setFont(authFont);
		// authName.setCellValue(accountExcelDecrp);
		authName.setCellStyle(authStyle);
		authName.setCellValue("Neptune Controls Pvt Ltd" +"\n"+ "\n "+"\n "+"\n "+"  Authorized Signatory ");
		
		CellRangeAddress cellRangeAddress1 = new CellRangeAddress(rowCountTerms-4, rowCountTerms, 6, 9);
		editAccountSheet.addMergedRegion(cellRangeAddress1);

		// Creates the cell
		

		// Sets the borders to the merged cell
		RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, cellRangeAddress1, editAccountSheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, cellRangeAddress1, editAccountSheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_THIN, cellRangeAddress1, editAccountSheet, workbook);
		RegionUtil.setBorderTop(CellStyle.BORDER_THIN, cellRangeAddress1, editAccountSheet, workbook);
	}

	

	private void setBordersToMergedCells(Workbook workBook, Sheet sheet) {
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
	
		
	}


