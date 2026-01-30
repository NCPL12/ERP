package com.ncpl.sales.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.security.User;

import pl.allegro.finance.tradukisto.MoneyConverters;

@Component
public class purchaseOrderExcel extends AbstractXlsxView {

	short VERTICAL_TOP = 0x0;
	short VERTICAL_JUSTIFY = 0x2;
	short BORDER_THIN = 0x1;

	FileNameGenerator fileNameGenerator = new FileNameGenerator();

	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	String company = "Neptune controls pvt ltd No.8/2(Old No.114),2nd Cross 7th Main Road Nandidurga Extension Bangalore-560046 Contact : 080-40904685,7624964492 "
			+ "E-Mail : accounts@ncpl.co";
	String companyName = "";
	String billingGst = "";
	String billingPan = "";
	String qtn = "";

	String contactPerson = " Ms Sumathy ";
	String contactNo = " 7624919715 ";
	String contactPerson1 = " Mr Shawn ";
	String contactNo1 = " 9901725778 ";
	String gstNo = "GSTIN : 29AADCN5426F1ZG";
	String excelHeading1 = "";
	String itemList = "";

	String terms = "";
	String delivery = "";
	String warranty = "";
	String payment = "";
	String taxes = "";
	String Jurisdiction = "";
	String quote = "";

	InvoiceExcelLogoService logoService = new InvoiceExcelLogoService();
	PurchaseSignatureLogo signService = new PurchaseSignatureLogo();

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String billingAddress = null;
		String shippingAddress = null;
		String vendorAddress = null;
		String companyAddress = null;
		User user = (User) request.getAttribute("user");
		Map<String, String> map = (Map<String, String>) request.getAttribute("termsAndConditions");
		// String vendorAddressId = map.get("vendorAddressId");

		// Model list
		Map modelList = (Map) request.getAttribute("modelMaps");

		String shippingAddressKey = map.get("shippingAddressId");
		String billingAddressKey = map.get("billingAddressId");
		String gstRegion = map.get("gstRegion");
		String quoteRefNo = map.get("quoteRefNo");
		String quoteDate = map.get("quoteDate").replace("-", "/");
		// String shippingAddressKey = "key1";
		// String billingAddressKey = "key2";

		PurchaseOrderCustomProperty customProperty = (PurchaseOrderCustomProperty) request
				.getAttribute("customProperty");
		companyAddress = customProperty.getCompanyAddress();

		Party party = (Party) request.getAttribute("party");

		if (party != null) {
			String addr2 = party.getAddr2();
			String gst = party.getGst();
			String contactNo = party.getPhone1();
			String pincode=party.getPin();
			if(pincode==null) {
				pincode="";
			}
			if (contactNo == null || contactNo == "") {
				contactNo = "NA";
			}
			if (gst == null || gst == "") {
				gst = "";
			}
			if (addr2 == null || addr2.equalsIgnoreCase("")) {
				// addr2 = "";
				vendorAddress = party.getPartyName() + "\n" + party.getAddr1() + "\n" + party.getParty_city().getName()
						+ " " + party.getParty_city().getCode() +" " +pincode+ "\n" + "Contact: " + contactNo + "\nGSTIN: " + gst;
			} else {
				vendorAddress = party.getPartyName() + "\n" + party.getAddr1() + "\n" + addr2 + "\n"
						+ party.getParty_city().getName() +" " +pincode+  "\n" + "Contact: " + contactNo + "\nGSTIN: " + gst;
			}
		} else {
			Optional<PartyAddress> partyAddressOpt = (Optional<PartyAddress>) request.getAttribute("partyAddress");
			PartyAddress partyAddress = partyAddressOpt.get();
			String addr2 = partyAddress.getAddr2();
			if (addr2 == null) {
				addr2 = "";
			}
			vendorAddress = partyAddress.getPartyName() + "\n" + partyAddress.getAddr1() + "\n" + addr2 + "\n"
					+ partyAddress.getPartyaddr_city().getName() + "\n" + "Contact: " + partyAddress.getPhone1();
		}

		String billingAddressesWithDelimiter = customProperty.getBillingAddress();
		String[] billingAddressesArray = billingAddressesWithDelimiter.split("\\^\\^");

		for (int i = 0; i < billingAddressesArray.length; i++) {
			String[] singleAddressArr = billingAddressesArray[i].split("\\$\\$");
			String key = singleAddressArr[0].replace("\n", "").trim();
			if (key.equalsIgnoreCase(billingAddressKey)) {
				billingAddress = billingAddressesArray[i].replaceAll("\\$\\$", ",").replace(key + ",", "").trim();
			}
		}
		// billingAddress =billingAddress.replace(",Karnataka", "");
		// billingAddress =billingAddress.replace(",India", "");
		if (billingAddress.contains("Mangalore")) {
			billingAddress = billingAddress.replace(",Karnataka", "");
			billingAddress = billingAddress.replace(",India", "");
			billingAddress = billingAddress.replace("Sri Ganesh Kripa,", "Sri Ganesh Kripa,");
			billingAddress = billingAddress.replace("Venkatesh Sadana", "Venkatesh Sadana,");
			billingAddress = billingAddress.replace(",575015", "");
			billingAddress=  billingAddress.replace(",Near Ganapathi Temple", "Near Ganapathi Temple,");
			billingAddress=  billingAddress.replace(",Perlaguri", "");
			billingAddress=  billingAddress.replace(",Kavoor Post", "Perlaguri Kavoor Post");
			billingAddress = billingAddress.replace("Mangalore", "Mangalore 575015");
		} else {
			billingAddress = billingAddress.replace("No 8/2(Old No 114), 2nd Cross 7th Main Road,Nandi",
					"No 8/2(Old No 114). 2nd Cross 7th Main Road.Nandi,");
			billingAddress = billingAddress.replace("durga Extension,Bengaluru,",
					"Durga Extension. Bengaluru.,");
			billingAddress = billingAddress.replace("Karnataka,India,560046", "Karnataka 560046");
			// billingAddress = billingAddress.replace("Bangalore", "Bangalore 560046");
		}
		billingAddress = billingAddress.replace(",", "\n");
		billingAddress = billingAddress.replace(".", ",");
		Party altshippingParty = (Party) request.getAttribute("altshippingParty");

		if(altshippingParty!= null) {
			String addr2 = altshippingParty.getAddr2();
			String gst = altshippingParty.getGst();
			String contactNo = altshippingParty.getPhone1();
			String pincode=altshippingParty.getPin();
			if(pincode==null) {
				pincode="";
			}
			
			if(contactNo == null || contactNo ==""){
				contactNo="NA";
			}
			if(gst == null || gst ==""){
				gst="";
			}
			if(addr2 == null || addr2.equalsIgnoreCase("")) {
				//addr2 = "";
				shippingAddress = altshippingParty.getPartyName() + "\n" + altshippingParty.getAddr1() + "," + altshippingParty.getParty_city().getName()
						+ " " + altshippingParty.getParty_city().getCode() +" " +pincode+ "\n" + "Contact: " + contactNo + "\nEmail: " + altshippingParty.getEmail1();
			}else{
				shippingAddress = altshippingParty.getPartyName() + "\n" + altshippingParty.getAddr1() + "\n" + addr2 + ","
						+ altshippingParty.getParty_city().getName() +" " +pincode+  "\n" + "Contact: " + contactNo + "\nEmail: " + altshippingParty.getEmail1();
			}
		}else {
			String shippingAddressesWithDelimiter = customProperty.getShippingAddress();
			String[] shippingAddressesArray = shippingAddressesWithDelimiter.split("\\^\\^");
	
			for (int i = 0; i < shippingAddressesArray.length; i++) {
				String[] singleShippingAddressArr = shippingAddressesArray[i].split("\\$\\$");
				String key = singleShippingAddressArr[0].replace("\n", "").trim();
				if (key.equalsIgnoreCase(shippingAddressKey)) {
					shippingAddress = shippingAddressesArray[i].replaceAll("\\$\\$", ",").replace(key + ",", "").trim();
				}
			}
			/*
			 * shippingAddress =shippingAddress.replace(",Karnataka", ""); shippingAddress
			 * =shippingAddress.replace(",India", "");
			 */
			if (shippingAddress.contains("Mangalore")) {
				shippingAddress = shippingAddress.replace(",Karnataka", "");
				shippingAddress = shippingAddress.replace(",India", "");
				shippingAddress = shippingAddress.replace("Sri Ganesh Kripa,", "Sri Ganesh Kripa,");
				shippingAddress = shippingAddress.replace("Venkatesh Sadana", "Venkatesh Sadana,");
				shippingAddress = shippingAddress.replace(",575015", "");
				shippingAddress=  shippingAddress.replace(",Near Ganapathi Temple", "Near Ganapathi Temple,");
				shippingAddress=  shippingAddress.replace(",Perlaguri", "");
				shippingAddress=  shippingAddress.replace(",Kavoor Post", "Perlaguri Kavoor Post");
				shippingAddress = shippingAddress.replace("Mangalore", "Mangalore 575015");
			} else {
				shippingAddress = shippingAddress.replace("No 8/2(Old No 114),2nd Cross 7th Main Road, Nandi",
						"No 8/2(Old No 114).2nd Cross 7th Main Road.Nandi,");
				shippingAddress = shippingAddress.replace("durga Extension,Bengaluru,",
						"Durga Extension. Bengaluru.,");
				shippingAddress = shippingAddress.replace("Karnataka,India,560046", "Karnataka 560046");
				// shippingAddress = shippingAddress.replace("Jayamahal extension,Bangalore,",
				// "Durga Road Extension. Bengaluru., ");
				// shippingAddress =shippingAddress.replace("Karnataka,India,560046", "Karnataka
				// 560046");
			}
			shippingAddress = shippingAddress.replace(",", "\n");
			shippingAddress = shippingAddress.replace(".", ",");
		}
		companyName = messageSource.getMessage("company.name", null, null);
		qtn = messageSource.getMessage("qtn", null, null);
		// contactPerson = messageSource.getMessage("contact.person", null,null);
		itemList = messageSource.getMessage("itemList", null, null);
		terms = messageSource.getMessage("terms", null, null);
		delivery = messageSource.getMessage("delivery", null, null);
		warranty = messageSource.getMessage("warranty", null, null);
		payment = messageSource.getMessage("payment", null, null);
		taxes = messageSource.getMessage("taxes", null, null);
		Jurisdiction = messageSource.getMessage("Jurisdiction", null, null);
		quote = messageSource.getMessage("quote", null, null);

		billingGst = messageSource.getMessage("company.gst", null, null);
		billingPan = messageSource.getMessage("company.pan", null, null);
		// get po details to populate in excel
		Map<String, Optional<PurchaseOrder>> poMap = (Map<String, Optional<PurchaseOrder>>) model.get("poData");
		Optional<PurchaseOrder> poObject = poMap.get("purchaseData");
		Date date = poObject.get().getCreated();
		// Converting date to String
		String dateString = date.toString();
		String[] arr = dateString.split(" ");

		// Formatting date to a required format
		String formattedDate = arr[0];
		formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "/"
				+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "/"
				+ formattedDate.substring(0, 4);

		// SalesOrder salesOrderObject = poObject.get().getSalesOrder();
		List<PurchaseItem> items = poObject.get().getItems();
		 System.out.println("Items List"+items.size());
		Map<String,PurchaseItem> poItemMap = new HashMap<String,PurchaseItem>();
		List<PurchaseItem> newItems = new ArrayList();
		
		/*
		 *  This below code is to add the quantities if po item is having the same
		 * models added..
		 */
		
		for (PurchaseItem purchaseItem : items) {
		
			String modelValue = (String) modelList.get(purchaseItem.getModelNo());
			int arrOfStr = modelValue.indexOf("$");
			String gst = modelValue.substring(arrOfStr + 1, modelValue.length());
			int len = modelValue.length();
			int len1 = modelValue.length() - 1;
			int len2 = modelValue.length() - 2;
			String[] m = modelValue.split("$");
			modelValue = modelValue.substring(0, modelValue.length() - 3);
			System.out.println("model No"+modelValue);
			String modelKey = " "+modelValue;
			System.out.println("boolean1"+poItemMap.containsKey(modelKey));
             
		    
	
			if(poItemMap.containsKey(modelKey)) {
				PurchaseItem prevValueWithUpdates =poItemMap.get(modelKey);
				float prevQty  = prevValueWithUpdates.getQuantity();
				float newQty = prevQty + purchaseItem.getQuantity();
				float newAmount = newQty * purchaseItem.getUnitPrice();
				newAmount = (float) (Math.round(newAmount * 100.0) / 100.0);
				prevValueWithUpdates.setQuantity(newQty);
				prevValueWithUpdates.setAmount(newAmount);
				poItemMap.put(modelKey, prevValueWithUpdates);
			}else {
				poItemMap.put(modelKey, purchaseItem);
			}
			
		}
		
	
		
		poItemMap.forEach((key, value) -> newItems.add(value));
		Collections.sort(newItems, Comparator.comparingInt(obj -> obj.getPurchase_item_id()));
        System.out.println("newItems List"+newItems.size());
		
		// Po Number for inserting into Excel
		String ponum = poObject.get().getPoNumber();

		// Generating File Name
		String fileName = ponum + "_purchaseOrder.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		/*
		 * .................................Edit Device excel sheet
		 * format.................................
		 */

		Sheet editAccountSheet = workbook.createSheet("Purchase Order");
		editAccountSheet.setDefaultColumnWidth(9);
		// editAccountSheet.autoSizeColumn(11);

		// setBordersToMergedCells(workbook, editAccountSheet);
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
		descriptionFont.setFontHeight((short) (7.5 * 45));
		descriptionFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		descriptionmergestyle.setFont(descriptionFont);
		descriptionmergestyle.setWrapText(true);
		descriptionCell.setCellStyle(descriptionmergestyle);*/

		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				1, // last row
				6, // first column
				12 // last column
		));
		// setBordersToMergedCells(workbook, editAccountSheet);

		// Row poNumber = editAccountSheet.createRow(1);
		Cell poNum = secondRow.createCell(6);
		poNum.setCellValue("PO No: " + ponum);

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
				12 // last column
		));
		Row podate = editAccountSheet.createRow(2);
		Cell podte = podate.createCell(6);
		podte.setCellValue("PO Date: " + formattedDate);

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
				12 // last column
		));

		// Row Quotation = editAccountSheet.createRow(3);
		Cell quotation = Quotation.createCell(6);
		quotation.setCellValue("Quote Ref No.: " + quoteRefNo);

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
				12 // last column
		));
		Row QuotationDate = editAccountSheet.createRow(4);
		Cell qtndate = QuotationDate.createCell(6);
		qtndate.setCellValue("Quote Date:" + quoteDate);

		CellStyle QuotationDatestyle = workbook.createCellStyle();
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

		/*editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				5, // last row
				6, // first column
				11 // last column
		));*/
		/*Row contact = editAccountSheet.createRow(5);
		Cell contctPerson = contact.createCell(6);

		contctPerson.setCellValue("Email: " + "purchase@ncpl.co");
		CellStyle contactStyle = workbook.createCellStyle();
		contactStyle.setBorderLeft(BORDER_THIN);
		contactStyle.setBorderRight(BORDER_THIN);
		contactStyle.setBorderTop(BORDER_THIN);
		contactStyle.setBorderBottom(BORDER_THIN);
		Font contactFont = workbook.createFont();
		contactFont.setFontName("Calibri");
		contactFont.setFontHeight((short) (7.5 * 35));
		contactStyle.setFont(contactFont);
		contactStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		contactStyle.setVerticalAlignment((short) (VERTICAL_TOP));
		contctPerson.setCellStyle(contactStyle);*/

		editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				14, // last row
				0, // first column
				3 // last column
		));

		Row vendorRow = editAccountSheet.createRow(5);
		vendorRow.setHeightInPoints((float) (2.5 * editAccountSheet.getDefaultRowHeightInPoints()));
		// vendorRow.setHeightInPoints(float)
		// (3.5*editAccountSheet.getDefaultRowHeightInPoints());
		Cell vendor = vendorRow.createCell(0);
		// vendor.setCellValue("Supplier:" +"\n"+ companyName +"\nGSTIN :
		// 29AADCN5426F1ZG");
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
		// vendorFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		vendorstyle.setFont(vendorFont);
		vendorstyle.setWrapText(true);
		vendor.setCellStyle(vendorstyle);
		// vendor.setCellValue("Supplier:" +"\n"+ companyName +"\n"+gstNo+"\nContact
		// Number:"+contactNo);
		vendor.setCellValue("Supplier:" + "\n" + vendorAddress);

		editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				14, // last row
				4, // first column
				7 // last column
		));

		// Row vendorRow1 = editAccountSheet.createRow(6);
		Cell vendor1 = vendorRow.createCell(4);
		// vendor1.setCellValue("Deliver To:" + "\n"+companyName+"\nContact:
		// "+contactPerson+"\nContact Number:"+contactNo);
		CellStyle vendorstyle1 = workbook.createCellStyle();
		// vendorstyle1.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		vendorstyle1.setVerticalAlignment((short) (VERTICAL_TOP));
		vendorstyle1.setBorderLeft(BORDER_THIN);
		vendorstyle1.setBorderRight(BORDER_THIN);
		vendorstyle1.setBorderTop(BORDER_THIN);
		vendorstyle1.setBorderBottom(BORDER_THIN);
		Font vendorFont1 = workbook.createFont();
		vendorFont1.setFontName("Calibri");
		vendorFont1.setFontHeight((short) (7.5 * 35));
		// vendorFont1.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		vendorstyle1.setFont(vendorFont);

		vendorstyle1.setWrapText(true);
		vendor1.setCellStyle(vendorstyle1);
		// vendor1.setCellValue("Deliver To:" + "\n"+companyName+"\nContact:
		// "+contactPerson+"\nContact Number:"+contactNo);
		String loginUserName=user.getUsername();
		if (shippingAddress.contains("Mangalore")) {
			if(shippingAddressKey.contains("key")) {
				vendor1.setCellValue("Deliver To:" + "\n" + shippingAddress + "\nContact: " + contactPerson1
						+ "\n@:" + contactNo1+ "\nEmail:" +  "purchase@ncpl.co");
				}else {
					vendor1.setCellValue("Deliver To:" + "\n" + shippingAddress);
				}
		}else {
			if(shippingAddressKey.contains("key")) {
			vendor1.setCellValue("Deliver To:" + "\n" + shippingAddress + "\nContact: " + user.getName()
					+ "\n@:" + user.getNumber()+ "\nEmail:" + user.getEmailId());
			}else {
				vendor1.setCellValue("Deliver To:" + "\n" + shippingAddress);
			}
		}
		editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
				14, // last row
				8, // first column
				12 // last column
		));

		// Row vendorRow2 = editAccountSheet.createRow(6);
		Cell vendor2 = vendorRow.createCell(8);
		// vendor2.setCellValue("Billing & Invoice:" +"\n"+ companyName +"\nPAN
		// NO:123456");
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
		// vendorFont2.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		vendorstyle2.setFont(vendorFont2);
		vendorstyle2.setWrapText(true);
		vendor2.setCellStyle(vendorstyle2);
		// vendor2.setCellValue("Billing & Invoice:" +"\n"+ companyName +"\nPAN
		// NO:123456"+"\n"+gstNo);
		vendor2.setCellValue(
				"Billing & Invoice:" + "\n" + billingAddress + "\nPAN NO: " + billingPan + "\nGSTIN : " + billingGst);

		editAccountSheet.addMergedRegion(new CellRangeAddress(15, // first row
				15, // last row
				0, // first column
				12 // last column
		));

		Row itemListIntro = editAccountSheet.createRow(16);
		Cell itemStarter = itemListIntro.createCell(0);
		itemStarter.setCellValue(itemList);

		Font fontColumn = workbook.createFont();
		fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumn.setFontHeight((short) (7.5 * 35));

		Row itemListHeader = editAccountSheet.createRow(17);
		Cell itemHeader = itemListHeader.createCell(0);
		CellStyle itemHeaderStyle = workbook.createCellStyle();
		itemHeaderStyle.setFont(fontColumn);
		itemHeaderStyle.setBorderBottom(BORDER_THIN);
		itemHeaderStyle.setBorderTop(BORDER_THIN);
		itemHeaderStyle.setBorderRight(BORDER_THIN);
		itemHeaderStyle.setBorderLeft(BORDER_THIN);
		itemHeader.setCellStyle(itemHeaderStyle);
		itemHeader.setCellValue("Sl No");

		editAccountSheet.addMergedRegion(new CellRangeAddress(17, // first row
				17, // last row
				1, // first column
				4 // last column
		));
		Cell desc = itemListHeader.createCell(1);
		/*
		 * for(int i=1;i<9;i++){ itemListHeader.createCell(i).setCellStyle(topborder); }
		 */
		CellStyle descStyle = workbook.createCellStyle();
		descStyle.setFont(fontColumn);
		descStyle.setBorderBottom(BORDER_THIN);
		descStyle.setBorderTop(BORDER_THIN);
		descStyle.setBorderRight(BORDER_THIN);
		descStyle.setBorderLeft(BORDER_THIN);
		desc.setCellStyle(descStyle);
		desc.setCellValue("Description of Goods");

		Cell gstRate = itemListHeader.createCell(8);
		CellStyle gstRateStyle = workbook.createCellStyle();
		gstRateStyle.setFont(fontColumn);
		gstRateStyle.setBorderBottom(BORDER_THIN);
		gstRateStyle.setBorderTop(BORDER_THIN);
		gstRateStyle.setBorderRight(BORDER_THIN);
		gstRateStyle.setBorderLeft(BORDER_THIN);
		gstRate.setCellStyle(gstRateStyle);
		gstRate.setCellValue("GST(%)");

		Cell hsn = itemListHeader.createCell(7);
		CellStyle hsnStyle = workbook.createCellStyle();
		hsnStyle.setFont(fontColumn);
		hsnStyle.setBorderBottom(BORDER_THIN);
		hsnStyle.setBorderTop(BORDER_THIN);
		hsnStyle.setBorderRight(BORDER_THIN);
		hsnStyle.setBorderLeft(BORDER_THIN);
		hsn.setCellStyle(hsnStyle);
		hsn.setCellValue("HSN Code");

		editAccountSheet.addMergedRegion(new CellRangeAddress(17, // first row
				17, // last row
				5, // first column
				6 // last column
		));

		Cell modelNo = itemListHeader.createCell(5);

		CellStyle modelNoStyle = workbook.createCellStyle();
		modelNoStyle.setFont(fontColumn);
		modelNoStyle.setBorderBottom(BORDER_THIN);
		modelNoStyle.setBorderTop(BORDER_THIN);
		modelNoStyle.setBorderRight(BORDER_THIN);
		modelNoStyle.setBorderLeft(BORDER_THIN);
		modelNo.setCellStyle(modelNoStyle);
		modelNo.setCellValue("Model No");

		Cell qty = itemListHeader.createCell(9);
		CellStyle qtyStyle = workbook.createCellStyle();
		qtyStyle.setFont(fontColumn);
		//qtyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		qtyStyle.setBorderBottom(BORDER_THIN);
		qtyStyle.setBorderTop(BORDER_THIN);
		qtyStyle.setBorderRight(BORDER_THIN);
		qtyStyle.setBorderLeft(BORDER_THIN);
		qty.setCellStyle(qtyStyle);
		qty.setCellValue("Qty");
		
		Cell unit = itemListHeader.createCell(10);
		CellStyle unitStyle = workbook.createCellStyle();
		unitStyle.setFont(fontColumn);
		//unitStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		unitStyle.setBorderBottom(BORDER_THIN);
		unitStyle.setBorderTop(BORDER_THIN);
		unitStyle.setBorderRight(BORDER_THIN);
		unitStyle.setBorderLeft(BORDER_THIN);
		unit.setCellStyle(unitStyle);
		unit.setCellValue("Unit");

		Cell unitPrice = itemListHeader.createCell(11);
		CellStyle unitPriceStyle = workbook.createCellStyle();
		unitPriceStyle.setFont(fontColumn);
		unitPriceStyle.setBorderBottom(BORDER_THIN);
		unitPriceStyle.setBorderTop(BORDER_THIN);
		unitPriceStyle.setBorderRight(BORDER_THIN);
		unitPriceStyle.setBorderLeft(BORDER_THIN);
		unitPrice.setCellStyle(unitPriceStyle);
		unitPrice.setCellValue("Unit Price");

		Cell Amount = itemListHeader.createCell(12);
		CellStyle AmountColumnStyle = workbook.createCellStyle();
		AmountColumnStyle.setBorderBottom(BORDER_THIN);
		AmountColumnStyle.setBorderTop(BORDER_THIN);
		AmountColumnStyle.setBorderRight(BORDER_THIN);
		AmountColumnStyle.setBorderLeft(BORDER_THIN);
		AmountColumnStyle.setFont(fontColumn);
		Amount.setCellStyle(AmountColumnStyle);
		Amount.setCellValue("Amount");

		// This Style is for the lower half after the items list
		CellStyle AmountStyle = workbook.createCellStyle();
		XSSFDataFormat amformat = (XSSFDataFormat) workbook.createDataFormat();
		AmountStyle.setDataFormat(amformat.getFormat("#,###"));
		/*
		 * XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
		 * AmountStyle.setDataFormat(format.getFormat("#,###"));
		 */
		// AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		AmountStyle.setBorderBottom(BORDER_THIN);
		AmountStyle.setBorderTop(BORDER_THIN);
		AmountStyle.setBorderRight(BORDER_THIN);
		AmountStyle.setBorderLeft(BORDER_THIN);
		Font amountFont = workbook.createFont();
		amountFont.setFontHeight((short) (7.5 * 35));
		AmountStyle.setFont(amountFont);
		System.out.println(AmountStyle.getFillForegroundColor());
		
		CellStyle totalStyle = workbook.createCellStyle();
		XSSFDataFormat totalFormat = (XSSFDataFormat) workbook.createDataFormat();
		AmountStyle.setDataFormat(totalFormat.getFormat("#,###"));
		/*
		 * XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
		 * AmountStyle.setDataFormat(format.getFormat("#,###"));
		 */
		// AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		totalStyle.setBorderBottom(BORDER_THIN);
		totalStyle.setBorderTop(BORDER_THIN);
		totalStyle.setBorderRight(BORDER_THIN);
		totalStyle.setBorderLeft(BORDER_THIN);
		Font totalFont = workbook.createFont();
		totalFont.setFontHeight((short) (7.5 * 35));
		totalFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		totalStyle.setFont(totalFont);
		System.out.println(totalStyle.getFillForegroundColor());

		// This style is added to add comma in numbers in grandtotal Field
		CellStyle grandTotalStyle = workbook.createCellStyle();
		XSSFDataFormat format = (XSSFDataFormat) workbook.createDataFormat();
		grandTotalStyle.setDataFormat(format.getFormat("#,###"));
		// AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		grandTotalStyle.setBorderBottom(BORDER_THIN);
		grandTotalStyle.setBorderTop(BORDER_THIN);
		grandTotalStyle.setBorderRight(BORDER_THIN);
		grandTotalStyle.setBorderLeft(BORDER_THIN);
		Font grandTotalStyleFont = workbook.createFont();
		grandTotalStyleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		grandTotalStyleFont.setFontHeight((short) (7.5 * 35));
		grandTotalStyle.setFont(grandTotalStyleFont);

		// This style is for displaying decimal points upto two decimal point with comma
		CellStyle twoDecimalStyle = workbook.createCellStyle();
		XSSFDataFormat twoDecimalStyleformat = (XSSFDataFormat) workbook.createDataFormat();
		twoDecimalStyle.setDataFormat(twoDecimalStyleformat.getFormat("#,###.00"));
		// AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		twoDecimalStyle.setBorderBottom(BORDER_THIN);
		twoDecimalStyle.setBorderTop(BORDER_THIN);
		twoDecimalStyle.setBorderRight(BORDER_THIN);
		twoDecimalStyle.setBorderLeft(BORDER_THIN);
		Font twoDecimalStyleStyleFont = workbook.createFont();
		twoDecimalStyleStyleFont.setFontHeight((short) (7.5 * 35));
		twoDecimalStyle.setFont(twoDecimalStyleStyleFont);

		// This style is for displaying decimal points upto two decimal
		CellStyle DecimalStyle = workbook.createCellStyle();
		XSSFDataFormat DecimalStyleformat = (XSSFDataFormat) workbook.createDataFormat();
		DecimalStyle.setDataFormat(DecimalStyleformat.getFormat("0.00"));
		// AmountStyle.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0,00"));
		DecimalStyle.setBorderBottom(BORDER_THIN);
		DecimalStyle.setBorderTop(BORDER_THIN);
		DecimalStyle.setBorderRight(BORDER_THIN);
		DecimalStyle.setBorderLeft(BORDER_THIN);
		Font DecimalStyleFont = workbook.createFont();
		DecimalStyleFont.setFontHeight((short) (7.5 * 35));
		DecimalStyle.setFont(DecimalStyleFont);

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
		threeSideborder.setBorderLeft(BORDER_THIN);
		threeSideborder.setBorderRight(BORDER_THIN);
		threeSideborder.setBorderBottom(BORDER_THIN);
		threeSideborder.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font itemListFont = workbook.createFont();
		itemListFont.setFontHeight((short) (7.5 * 35));
		threeSideborder.setFont(itemListFont);
		
		CellStyle threeSideborder1 = workbook.createCellStyle();
		threeSideborder.setWrapText(true);
		XSSFDataFormat threeSideborderformat1 = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborder1.setDataFormat(threeSideborderformat1.getFormat("#,###"));
		threeSideborder1.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborder1.setBorderLeft(BORDER_THIN);
		threeSideborder1.setBorderRight(BORDER_THIN);
		threeSideborder1.setBorderBottom(BORDER_THIN);
		threeSideborder1.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font itemListFont1 = workbook.createFont();
		itemListFont1.setFontHeight((short) (7.5 * 35));
		threeSideborder1.setFont(itemListFont1);

		CellStyle threeSideborderRight = workbook.createCellStyle();
		XSSFDataFormat threeSideborderRightformat = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRight.setDataFormat(threeSideborderRightformat.getFormat("#,###.00"));
		threeSideborderRight.setBorderLeft(BORDER_THIN);
		threeSideborderRight.setBorderRight(BORDER_THIN);
		threeSideborderRight.setBorderBottom(BORDER_THIN);
		threeSideborderRight.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderRight.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRight.setFont(itemListFont);
		
		CellStyle threeSideborderRightWithoutDec = workbook.createCellStyle();
		XSSFDataFormat threeSideborderRightformatWithoutDec = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRightWithoutDec.setDataFormat(threeSideborderRightformatWithoutDec.getFormat("#,###"));
		threeSideborderRightWithoutDec.setBorderLeft(BORDER_THIN);
		threeSideborderRightWithoutDec.setBorderRight(BORDER_THIN);
		threeSideborderRightWithoutDec.setBorderBottom(BORDER_THIN);
		threeSideborderRightWithoutDec.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderRightWithoutDec.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRightWithoutDec.setFont(itemListFont);

		CellStyle fourSideborder = workbook.createCellStyle();
		fourSideborder.setFont(grandTotalStyleFont);
		XSSFDataFormat foursideformat = (XSSFDataFormat) workbook.createDataFormat();
		fourSideborder.setDataFormat(foursideformat.getFormat("#,###.00"));
		fourSideborder.setBorderLeft(BORDER_THIN);
		fourSideborder.setBorderRight(BORDER_THIN);
		fourSideborder.setBorderBottom(BORDER_THIN);
		fourSideborder.setBorderTop(BORDER_THIN);

		CellStyle leftborder = workbook.createCellStyle();
		leftborder.setFont(itemListFont);
		leftborder.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		leftborder.setBorderLeft(BORDER_THIN);
		leftborder.setVerticalAlignment((short) (VERTICAL_JUSTIFY));

		CellStyle rightborder = workbook.createCellStyle();
		XSSFDataFormat aformat = (XSSFDataFormat) workbook.createDataFormat();
		rightborder.setDataFormat(aformat.getFormat("#,###.00"));
		rightborder.setFont(itemListFont);
		rightborder.setBorderRight(BORDER_THIN);
		rightborder.setBorderLeft(BORDER_THIN);
		rightborder.setBorderBottom(BORDER_THIN);
		rightborder.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		rightborder.setAlignment(HSSFCellStyle.ALIGN_RIGHT);

		CellStyle topborder = workbook.createCellStyle();
		topborder.setBorderTop(BORDER_THIN);

		int rowCount = 18;
		int slNumber = 1;
		double totalAmount = 0;
		double gstAmount = 0;
		double grandTotal = 0;
		float sgstAmount = 0;
		float cgstAmount = 0;
		double roundoff = 0;
		float sgst9 = 0;
		float sgst14 = 0;
		float sgst025 = 0;
		float cgst9 = 0;
		float cgst14 = 0;
		float cgst025 = 0;
		float igst = 0;
		double tGst = 0;
		double cGst = 0;
		double sGst = 0;

		for (PurchaseItem purchaseItem : newItems) {
			if(purchaseItem.getQuantity()!=0) {
			System.out.println(purchaseItem.getModelNo());
			System.out.println(purchaseItem.getDescription());
			// Optional<ItemMaster> itemMasterObject =
			// itemMasterService.getItemById(purchaseItem.getModelNo());
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					1, // first column
					4// last column
			));
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					5, // first column
					6// last column
			));
			Row itemList = editAccountSheet.createRow(rowCount);
            int length = purchaseItem.getPoDescription().length();
			if (purchaseItem.getPoDescription().length() > 35 && purchaseItem.getPoDescription().length() < 65) {
				// if(purchaseItem.getDescription().length()>80){
				itemList.setHeightInPoints((float) (2.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}
			if (purchaseItem.getPoDescription().length() > 65) {
				// if(purchaseItem.getDescription().length()>80){
				itemList.setHeightInPoints((float) (3.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}
			Cell slno = itemList.createCell(0);
			slno.setCellStyle(threeSideborder1);
			Cell description = itemList.createCell(1);
			CellStyle descriptionStyle = workbook.createCellStyle();
			descriptionStyle.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
			descriptionStyle.setWrapText(true);
			descriptionStyle.setFont(itemListFont);
			descriptionStyle.setBorderLeft(BORDER_THIN);
			descriptionStyle.setBorderRight(BORDER_THIN);
			descriptionStyle.setBorderTop(BORDER_THIN);
			descriptionStyle.setBorderBottom(BORDER_THIN);
//			descriptionStyle.setBorderRight(BORDER_THIN);
//			descriptionStyle.setBorderLeft(BORDER_THIN);
//			descriptionStyle.setBottomBorderColor(BORDER_THIN);
			description.setCellStyle(descriptionStyle);
			Cell gstrate = itemList.createCell(8);
			gstrate.setCellStyle(threeSideborder1);
			Cell hsncde = itemList.createCell(7);
			hsncde.setCellType(Cell.CELL_TYPE_NUMERIC);
			hsncde.setCellStyle(threeSideborder1);
			Cell modelnumber = itemList.createCell(5);
			//modelnumber.setCellType(Cell.CELL_TYPE_NUMERIC);
			modelnumber.setCellStyle(descriptionStyle);
			Cell quantity = itemList.createCell(9);
			quantity.setCellStyle(threeSideborderRight);
			Cell unitCell = itemList.createCell(10);
			unitCell.setCellStyle(threeSideborder1);
			Cell up = itemList.createCell(11);
			up.setCellStyle(threeSideborderRight);
			Cell amountTot = itemList.createCell(12);

			// amountTot.setCellStyle(threeSideborder);
			amountTot.setCellStyle(rightborder);

			slno.setCellValue(slNumber);
			description.setCellValue(purchaseItem.getPoDescription());

			// description.setCellValue("Connector cable connector Connector cable connector
			// cable connector cable connector cable connector ");
			// description.setCellValue("Connector camera Connector camera Connector camera
			// Connector camera");
			// gstrate.setCellValue("18%");
			// hsncde.setCellValue("hsn2345");
			/*
			 * String desc1 =
			 * "Connector camera Connector camera Connector camera Connector camera"; int
			 * len = desc1.length(); description.setCellValue(desc1);
			 */

			hsncde.setCellValue(purchaseItem.getHsnCode());
			
			// modelnumber.setCellValue("WW65RKOLETWOKN/KOS");
			String key = purchaseItem.getModelNo();
			String modelValue = (String) modelList.get(key);
			int arrOfStr = modelValue.indexOf("$");
			int unitEndIndex = modelValue.indexOf("&");
			String gst = modelValue.substring(arrOfStr + 1, modelValue.length());
			String unitName = modelValue.substring(0,unitEndIndex);
			int len = modelValue.length();
			int len1 = modelValue.length() - 1;
			int len2 = modelValue.length() - 2;
			modelValue = modelValue.substring(unitEndIndex+1, modelValue.length() - 3);

			if (modelValue == null || modelValue == "") {
				modelnumber.setCellValue(purchaseItem.getModelNo());

			} else {
				modelnumber.setCellValue(modelValue);
			}
			if (Integer.parseInt(gst) > 0) {
				double gstValue = Float.parseFloat(gst) / 2;
				gstValue = gstValue / 100;
				cGst = (cGst + (gstValue * purchaseItem.getAmount()));
				sGst = (sGst + (gstValue * purchaseItem.getAmount()));
				igst = (igst + ((Float.parseFloat(gst) / 100) * purchaseItem.getAmount()));
			}
			/*
			 * if(Integer.parseInt(gst) == 18){ cgst9 = (float)
			 * (cgst9+(purchaseItem.getAmount()*0.09)); sgst9 = (float)
			 * (sgst9+(purchaseItem.getAmount()*0.09)); igst = (float)
			 * (igst+(purchaseItem.getAmount()*0.18));
			 * 
			 * } if(Integer.parseInt(gst) == 28){ cgst14 = (float)
			 * (cgst14+(purchaseItem.getAmount()*0.14)); sgst14 = (float)
			 * (sgst14+(purchaseItem.getAmount()*0.14)); igst = (float)
			 * (igst+(purchaseItem.getAmount()*0.28));
			 * 
			 * }
			 * 
			 * if(Integer.parseInt(gst) == 5){ cgst025 = (float)
			 * (cgst025+(purchaseItem.getAmount()*0.025)); sgst025 = (float)
			 * (sgst025+(purchaseItem.getAmount()*0.025)); igst = (float)
			 * (igst+(purchaseItem.getAmount()*0.05));
			 * 
			 * }
			 */
			unitCell.setCellValue(unitName);
			gstrate.setCellValue(gst);
			quantity.setCellValue(purchaseItem.getQuantity());
			up.setCellValue(purchaseItem.getUnitPrice());
			amountTot.setCellStyle(rightborder);
			amountTot.setCellValue(purchaseItem.getAmount());
			float amount = purchaseItem.getAmount();
			// amount = Math.round(amount * 100) / 100;
			totalAmount = totalAmount + amount;
			rowCount++;
			slNumber++;
		}
		}

		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				9, // first column
				11// last column
		));
		int rowLastItemCount = rowCount;
		Row total = editAccountSheet.createRow(rowCount);
		Cell firstBorder = total.createCell(0);
		for (int i = 1; i < 9; i++) {
			total.createCell(i).setCellStyle(topborder);
		}
		CellStyle afterItems = workbook.createCellStyle();
		afterItems.setBorderTop(BORDER_THIN);
		afterItems.setBorderLeft(BORDER_THIN);
		firstBorder.setCellStyle(afterItems);
		Cell totalCell = total.createCell(9);
		Cell totalvalue = total.createCell(12);

		totalvalue.setCellStyle(fourSideborder);
		totalCell.setCellStyle(AmountStyle);
		totalCell.setCellValue("Total");
		totalvalue.setCellValue(totalAmount);
		rowCount++;

		if (gstRegion.contains("State")) {
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					9, // first column
					11// last column
			));

			Row igstRow = editAccountSheet.createRow(rowCount);
			Cell igstRowfirstBorder = igstRow.createCell(0);
			igstRowfirstBorder.setCellStyle(leftBorderStyle);
			Cell igstCell = igstRow.createCell(9);
			Cell igstvalue = igstRow.createCell(12);
			AmountStyle.setWrapText(true);
			igstvalue.setCellStyle(twoDecimalStyle);
			igstCell.setCellStyle(AmountStyle);
			igstCell.setCellValue("GST");
			// grandTotal = gstAmount+totalAmount;

			igstvalue.setCellStyle(twoDecimalStyle);
			tGst = cGst + sGst;
			// tGst =sgst9+cgst9+sgst14+cgst14+sgst025+cgst025;
			igstvalue.setCellValue(tGst);
			rowCount++;

		}

		if (gstRegion.contains("Inter")) {
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					9, // first column
					11// last column
			));

			Row igstRow = editAccountSheet.createRow(rowCount);
			Cell igstRowfirstBorder = igstRow.createCell(0);
			igstRowfirstBorder.setCellStyle(leftBorderStyle);
			Cell igstCell = igstRow.createCell(9);
			Cell igstvalue = igstRow.createCell(12);
			AmountStyle.setWrapText(true);
			igstvalue.setCellStyle(twoDecimalStyle);
			igstCell.setCellStyle(AmountStyle);
			igstCell.setCellValue("IGST");
			// grandTotal = gstAmount+totalAmount;
			igstvalue.setCellStyle(twoDecimalStyle);

			igstvalue.setCellValue(igst);
			rowCount++;

		}

		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				9, // first column
				11// last column
		));

		Row grandtotalRoundoff = editAccountSheet.createRow(rowCount);
		Cell grandtotalRoundofffirstBorder = grandtotalRoundoff.createCell(0);
		grandtotalRoundofffirstBorder.setCellStyle(leftBorderStyle);
		Cell grandtotalRoundoffCell = grandtotalRoundoff.createCell(9);
		Cell grandtotalRoundoffvalue = grandtotalRoundoff.createCell(12);
		AmountStyle.setWrapText(true);
		grandtotalRoundoffvalue.setCellStyle(twoDecimalStyle);
		grandtotalRoundoffCell.setCellStyle(AmountStyle);
		grandtotalRoundoffCell.setCellValue("Round Off");
		if (gstRegion.contains("Inter")) {
			gstAmount = igst;
		} else {
			gstAmount = cGst + sGst;
			// gstAmount = sgst9+cgst9+sgst14+cgst14+sgst025+cgst025;
		}
		grandTotal = gstAmount + totalAmount;
		roundoff = grandTotal - Math.round(grandTotal * 100) / 100;
		Double roundOffamount = Math.round(roundoff * 100.0) / 100.0;
		// grandTotal = gstAmount+totalAmount;
		if ((roundoff % 10) == 0) {
			grandtotalRoundoffvalue.setCellStyle(AmountStyle);
		} else {
			grandtotalRoundoffvalue.setCellStyle(DecimalStyle);
		}
		grandtotalRoundoffvalue.setCellValue(roundOffamount);
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				9, // first column
				11// last column
		));

		Row grandtotal = editAccountSheet.createRow(rowCount);
		Cell grandtotalfirstBorder = grandtotal.createCell(0);
		grandtotalfirstBorder.setCellStyle(leftBorderStyle);
		Cell grandtotalCell = grandtotal.createCell(9);
		Cell grandtotalvalue = grandtotal.createCell(12);
		AmountStyle.setWrapText(true);
		grandtotalvalue.setCellStyle(grandTotalStyle);
		grandtotalCell.setCellStyle(totalStyle);
		grandtotalCell.setCellValue("Grand Total");
		if (gstRegion.contains("Inter")) {
			gstAmount = igst;
		} else {
			// gstAmount = sgst9+cgst9+sgst14+cgst14+sgst025+cgst025;
			gstAmount = cGst + sGst;
		}
		// gstAmount = sgst9+cgst9+sgst14+cgst14+sgst025+cgst025;
		grandTotal = gstAmount + totalAmount;
		grandTotal = Math.round(grandTotal * 100) / 100;
		// grandtotalvalue.setCellValue(grandTotal);
		if (roundoff >= 0.5) {
			grandTotal = grandTotal + 1;
		}
		grandtotalvalue.setCellValue(grandTotal);
		rowCount++;

		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				12 // last column
		));

		String numberInWords = convertToIndianCurrency((int) grandTotal);
		numberInWords = numberInWords.replace("£ 00/100", "");
		// String s =numberInWords.substring(0, 1);
		String capitalnumberInWords = capitalize(numberInWords);
		Row grandtotalInWords = editAccountSheet.createRow(rowCount);
		Cell totalInwords = grandtotalInWords.createCell(0);
		// totalInwords.setCellValue("Rupees (In Words) :"+numberInWords +"Only");
		CellStyle grandtotalInWordsstyle = workbook.createCellStyle();
		grandtotalInWordsstyle.setBorderLeft(BORDER_THIN);
		grandtotalInWordsstyle.setBorderRight(BORDER_THIN);
		grandtotalInWordsstyle.setBorderBottom(BORDER_THIN);
		grandtotalInWordsstyle.setBorderTop(BORDER_THIN);
		Font gtFont = workbook.createFont();
		// gtFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		gtFont.setFontHeight((short) (7.5 * 35));
		grandtotalInWordsstyle.setFont(gtFont);
		totalInwords.setCellStyle(grandtotalInWordsstyle);
		totalInwords.setCellValue("Rupees (In Words) :" + capitalnumberInWords);

		// totalInwords.setCellValue("Rupees (In Words) :"+numberInWords +"Only");
		rowCount++;

		rowCount++;
		if (gstRegion.contains("State")) {
			Font taxFont = workbook.createFont();
			taxFont.setFontHeight((short) (7.5 * 35));
			taxFont.setFontName("Calibri");
			taxFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			
			Font taxableFont = workbook.createFont();
			taxableFont.setFontHeight((short) (7.5 * 35));
			taxableFont.setFontName("Calibri");

			CellStyle taxStyle = workbook.createCellStyle();
			taxStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			taxStyle.setWrapText(true);
			taxStyle.setFont(taxFont);

			// Style for rate and amount
			CellStyle rateStyle = workbook.createCellStyle();
			rateStyle.setFont(taxFont);
			rateStyle.setBorderRight(BORDER_THIN);
			rateStyle.setBorderBottom(BORDER_THIN);

			// For Tax break up table
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount + 1, // last row
					0, // first column
					3 // last column
			));
			Row hsnTax = editAccountSheet.createRow(rowCount);
			Cell hsnCell = hsnTax.createCell(0);
			hsnCell.setCellStyle(taxStyle);
			hsnCell.setCellValue("Tax Rate");
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount + 1, // last row
					4, // first column
					4 // last column
			));
			// Row taxValue = editAccountSheet.createRow(rowCount);
			Cell taxCell = hsnTax.createCell(4);
			taxCell.setCellStyle(taxStyle);
			taxCell.setCellValue("Taxable" + "\n" + "Value");

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					5, // first column
					6 // last column
			));
			// Row central = editAccountSheet.createRow(rowCount);
			Cell centraltaxCell = hsnTax.createCell(5);
			centraltaxCell.setCellStyle(taxStyle);
			centraltaxCell.setCellValue("Central Tax");

			Row centralRate = editAccountSheet.createRow(rowCount + 1);
			Cell centralRateCell = centralRate.createCell(5);
			centralRateCell.setCellStyle(rateStyle);
			centralRateCell.setCellValue("Rate");

			// Row centralAmount = editAccountSheet.createRow(rowCount+1);
			Cell centralAmountCell = centralRate.createCell(6);
			centralAmountCell.setCellStyle(rateStyle);
			centralAmountCell.setCellValue("Amount");

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					7, // first column
					8 // last column
			));
			// Row state = editAccountSheet.createRow(rowCount);
			Cell statetaxCell = hsnTax.createCell(7);
			statetaxCell.setCellStyle(taxStyle);
			statetaxCell.setCellValue("State Tax");

			// Row stateRate = editAccountSheet.createRow(rowCount+1);
			Cell stateRateCell = centralRate.createCell(7);
			stateRateCell.setCellStyle(rateStyle);
			stateRateCell.setCellValue("Rate");

			// Row stateAmount = editAccountSheet.createRow(rowCount+1);
			Cell stateAmountCell = centralRate.createCell(8);
			stateAmountCell.setCellStyle(rateStyle);
			stateAmountCell.setCellValue("Amount");

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount + 1, // last row
					9, // first column
					12 // last column
			));

			// Row totalTax = editAccountSheet.createRow(rowCount);
			Cell totalTaxCell = hsnTax.createCell(9);
			totalTaxCell.setCellStyle(taxStyle);
			totalTaxCell.setCellValue("Total" + "\n" + "Tax Amount");

			rowCount++;
			rowCount++;

			double totalTaxableAmount = 0;
			double totalTax = 0;
			double stateTax = 0;
			double centralTax = 0;
			Map<String, Float> taxMap = new HashMap<String, Float>();
			for (PurchaseItem purchaseItem : newItems) {

				String key = purchaseItem.getModelNo();
				String modelValue = (String) modelList.get(key);
				int arrOfStr = modelValue.indexOf("$");
				String gst = modelValue.substring(arrOfStr + 1, modelValue.length());
				int igstRate = Integer.parseInt(gst);
				String gstKey = "GST" + "$" + igstRate;

				if (taxMap.containsKey(gstKey)) {
					float gstValue = (float) taxMap.get(gstKey);
					float currGst = (purchaseItem.getAmount() * igstRate) / 100;
					currGst = (float) (Math.round(currGst * 100.0) / 100.0);
					float totGst = gstValue + currGst;
					totGst = (float) (Math.round(totGst * 100.0) / 100.0);
					taxMap.put(gstKey, totGst);

				} else {
					float gstValue = (purchaseItem.getAmount() * igstRate) / 100;
					gstValue = (float) (Math.round(gstValue * 100.0) / 100.0);
					taxMap.put(gstKey, gstValue);
				}

			}

			for (String name : taxMap.keySet()) {
				// search for value
				Float value = taxMap.get(name);
				System.out.println("Key = " + name + ", Value = " + value);
				int arrOfStr = name.indexOf("$");
				String gst = name.substring(arrOfStr + 1, name.length());
				CellStyle columnStyle = workbook.createCellStyle();
				XSSFDataFormat columnstyleformat = (XSSFDataFormat) workbook.createDataFormat();
				columnStyle.setDataFormat(columnstyleformat.getFormat("#,###.00"));
				Font colFont = workbook.createFont();
				colFont.setFontName("Calibri");
				colFont.setFontHeight((short) (7.5 * 35));
				columnStyle.setFont(colFont);
				columnStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				columnStyle.setBorderBottom(BORDER_THIN);
				columnStyle.setBorderLeft(BORDER_THIN);
				columnStyle.setBorderRight(BORDER_THIN);
				columnStyle.setBorderTop(BORDER_THIN);

				editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
						rowCount, // last row
						0, // first column
						3 // last column
				));
				Row hsnRow = editAccountSheet.createRow(rowCount);
				Cell hsnRowCell = hsnRow.createCell(0);
				hsnRowCell.setCellStyle(columnStyle);
				hsnRowCell.setCellValue("GST@" + gst + "%");

				Cell taxableValueRowCell = hsnRow.createCell(4);
				taxableValueRowCell.setCellStyle(columnStyle);
				float taxableValue = (value * 100) / Integer.parseInt(gst);
				taxableValue = (float) (Math.round(taxableValue * 100.0) / 100.0);
				totalTaxableAmount = totalTaxableAmount + taxableValue;
				totalTaxableAmount = (float) (Math.round(totalTaxableAmount * 100.0) / 100.0);
				taxableValueRowCell.setCellValue(taxableValue);

				Cell ctaxRateRowCell = hsnRow.createCell(5);
				ctaxRateRowCell.setCellStyle(columnStyle);
				Double crate = (double) (Float.parseFloat(gst) / 2);
				crate = Math.round(crate * 100.0) / 100.0;
				ctaxRateRowCell.setCellValue(crate + "%");

				Cell ctaxAmountRowCell = hsnRow.createCell(6);
				ctaxAmountRowCell.setCellStyle(columnStyle);
				float cgsValue = value / 2;
				cgsValue = (float) (Math.round(cgsValue * 100.0) / 100.0);
				ctaxAmountRowCell.setCellValue(cgsValue);

				Cell staxRateRowCell = hsnRow.createCell(7);
				staxRateRowCell.setCellStyle(columnStyle);
				Double srate = (double) (Float.parseFloat(gst) / 2);
				srate = Math.round(srate * 100.0) / 100.0;
				staxRateRowCell.setCellValue(srate + "%");

				Cell staxAmountRowCell = hsnRow.createCell(8);
				staxAmountRowCell.setCellStyle(columnStyle);
				float sgsValue = value / 2;
				sgsValue = (float) (Math.round(sgsValue * 100.0) / 100.0);
				staxAmountRowCell.setCellValue(sgsValue);

				editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
						rowCount, // last row
						9, // first column
						12 // last column
				));

				Cell totalTaxCellValue = hsnRow.createCell(9);
				totalTaxCellValue.setCellStyle(columnStyle);
				totalTax = totalTax + value;
				totalTax = (float) (Math.round(totalTax * 100.0) / 100.0);

				totalTaxCellValue.setCellValue(value);
				rowCount++;
			}

			// Last row of taxcell

			// Style
			CellStyle lastTaxstyle = workbook.createCellStyle();
			XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
			lastTaxstyle.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
			lastTaxstyle.setFont(taxableFont);
			lastTaxstyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			lastTaxstyle.setBorderLeft(BORDER_THIN);
			lastTaxstyle.setBorderRight(BORDER_THIN);
			lastTaxstyle.setBorderBottom(BORDER_THIN);
			lastTaxstyle.setBorderTop(BORDER_THIN);

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					0, // first column
					3 // last column
			));

			Row totalTaxable = editAccountSheet.createRow(rowCount);
			Cell totalHeader = totalTaxable.createCell(0);
			totalHeader.setCellValue("Total");
			Cell totTax = totalTaxable.createCell(4);
			totTax.setCellStyle(grandTotalStyle);
			totTax.setCellValue(totalTaxableAmount);

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					5, // first column
					8 // last column
			));

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					9, // first column
					12 // last column
			));

			Cell totalTaxes = totalTaxable.createCell(9);
			totalTaxes.setCellStyle(lastTaxstyle);
			totalTaxes.setCellValue(totalTax);

			rowCount++;
			rowCount++;
		}

		// For inter

		if (gstRegion.contains("Inter")) {
			Font taxFont = workbook.createFont();
			taxFont.setFontHeight((short) (7.5 * 35));
			taxFont.setFontName("Calibri");
			taxFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			
			Font taxableFont = workbook.createFont();
			taxableFont.setFontHeight((short) (7.5 * 35));
			taxableFont.setFontName("Calibri");

			CellStyle taxStyle = workbook.createCellStyle();
			taxStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			taxStyle.setWrapText(true);
			taxStyle.setFont(taxFont);

			// Style for rate and amount
			CellStyle rateStyle = workbook.createCellStyle();
			rateStyle.setFont(taxFont);
			rateStyle.setBorderRight(BORDER_THIN);
			rateStyle.setBorderBottom(BORDER_THIN);

			// For Tax break up table
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount + 1, // last row
					0, // first column
					3 // last column
			));
			Row hsnTax = editAccountSheet.createRow(rowCount);
			Cell hsnCell = hsnTax.createCell(0);
			hsnCell.setCellStyle(taxStyle);
			hsnCell.setCellValue("Taxable Value");
			
		
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					4, // first column
					7 // last column
			));
			// Row taxValue = editAccountSheet.createRow(rowCount);
			Cell taxCell = hsnTax.createCell(4);
			taxCell.setCellStyle(taxStyle);
			taxCell.setCellValue("IGST");

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount + 1, // first row
					rowCount + 1, // last row
					4, // first column
					5 // last column
			));
			Row centralRate = editAccountSheet.createRow(rowCount + 1);
			Cell centraltaxCell = centralRate.createCell(4);
			centraltaxCell.setCellStyle(taxStyle);
			centraltaxCell.setCellValue("Rate");

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount + 1, // first row
					rowCount + 1, // last row
					6, // first column
					7// last column
			));

			// Row centralRate = editAccountSheet.createRow(rowCount+1);
			Cell centralRateCell = centralRate.createCell(6);
			centralRateCell.setCellStyle(rateStyle);
			centralRateCell.setCellValue("Amount");

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount + 1, // last row
					8, // first column
					12 // last column
			));

			// Row totalTax = editAccountSheet.createRow(rowCount);
			Cell totalTaxCell = hsnTax.createCell(8);
			totalTaxCell.setCellStyle(taxStyle);
			totalTaxCell.setCellValue("Total" + "\n" + "Tax Amount");

			rowCount++;
			rowCount++;

			double totalTaxableAmount = 0;
			double totalTax = 0;
			double totalTaxesValues = 0;
			double igst18 = 0;
			double igst18taxable = 0;
			double igst28 = 0;
			double igst28taxable = 0;
			double igst5 = 0;
			double igst5taxable = 0;
			Map<String, Float> taxMap = new HashMap<String, Float>();
			for (PurchaseItem purchaseItem : newItems) {

				String key = purchaseItem.getModelNo();
				String modelValue = (String) modelList.get(key);
				int arrOfStr = modelValue.indexOf("$");
				String gst = modelValue.substring(arrOfStr + 1, modelValue.length());
				int igstRate = Integer.parseInt(gst);
				String gstKey = "GST" + "$" + igstRate;

				if (igstRate > 0) {

					if (taxMap.containsKey(gstKey)) {
						float gstValue = (float) taxMap.get(gstKey);
						float currGst = (purchaseItem.getAmount() * igstRate) / 100;
						currGst = (float) (Math.round(currGst * 100.0) / 100.0);
						float totGst = gstValue + currGst;
						totGst = (float) (Math.round(totGst * 100.0) / 100.0);
						taxMap.put(gstKey, totGst);

					} else {
						float gstValue = (purchaseItem.getAmount() * igstRate) / 100;
						gstValue = (float) (Math.round(gstValue * 100.0) / 100.0);
						taxMap.put(gstKey, gstValue);
					}

				}

			}

			for (String name : taxMap.keySet()) {
				// search for value
				Float value = taxMap.get(name);
				System.out.println("Key = " + name + ", Value = " + value);
				int arrOfStr = name.indexOf("$");
				String gst = name.substring(arrOfStr + 1, name.length());
				if (Integer.parseInt(gst) > 0) {
					CellStyle columnStyle = workbook.createCellStyle();
					XSSFDataFormat columnstyleformat = (XSSFDataFormat) workbook.createDataFormat();
					columnStyle.setDataFormat(columnstyleformat.getFormat("#,###.00"));
					Font colFont = workbook.createFont();
					colFont.setFontName("Calibri");
					colFont.setFontHeight((short) (7.5 * 35));
					columnStyle.setFont(colFont);
					columnStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					columnStyle.setBorderBottom(BORDER_THIN);
					columnStyle.setBorderLeft(BORDER_THIN);
					columnStyle.setBorderRight(BORDER_THIN);
					columnStyle.setBorderTop(BORDER_THIN);

					editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
							rowCount, // last row
							0, // first column
							3 // last column
					));
					Row hsnRow = editAccountSheet.createRow(rowCount);
					Cell hsnRowCell = hsnRow.createCell(0);
					hsnRowCell.setCellStyle(columnStyle);
					float taxableValue = (value * 100) / Integer.parseInt(gst);
					taxableValue = (float) (Math.round(taxableValue * 100.0) / 100.0);
					totalTaxableAmount = totalTaxableAmount + taxableValue;
					totalTaxableAmount = (float) (Math.round(totalTaxableAmount * 100.0) / 100.0);
					hsnRowCell.setCellValue(taxableValue);
					editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
							rowCount, // last row
							4, // first column
							5 // last column
					));

					Cell taxValueRowCell = hsnRow.createCell(4);
					taxValueRowCell.setCellStyle(columnStyle);
					float gstRateValue = (float) (Math.round(Integer.parseInt(gst) * 100.0) / 100.0);
					taxValueRowCell.setCellValue(gstRateValue + "%");

					editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
							rowCount, // last row
							6, // first column
							7 // last column
					));

					Cell ctaxRateRowCell = hsnRow.createCell(6);
					ctaxRateRowCell.setCellStyle(columnStyle);
					ctaxRateRowCell.setCellValue(value);

//					editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
//							rowCount, // last row
//							4, // first column
//							7 // last column
//					));

					
					editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
							rowCount, // last row
							8, // first column
							12 // last column
					));

					Cell totalTaxCellValue = hsnRow.createCell(8);
					totalTaxCellValue.setCellStyle(columnStyle);
					totalTax = totalTax + value;
					totalTax = (float) (Math.round(totalTax * 100.0) / 100.0);

					totalTaxCellValue.setCellValue(value);
					rowCount++;
				}
			}

			// Last row of taxcell

			// Style
			CellStyle lastTaxstyle = workbook.createCellStyle();
			XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
			lastTaxstyle.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
			lastTaxstyle.setFont(taxableFont);
			lastTaxstyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			lastTaxstyle.setBorderLeft(BORDER_THIN);
			lastTaxstyle.setBorderRight(BORDER_THIN);
			lastTaxstyle.setBorderBottom(BORDER_THIN);
			lastTaxstyle.setBorderTop(BORDER_THIN);

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					0, // first column
					1 // last column
			));

			Row totalTaxable = editAccountSheet.createRow(rowCount);
			Cell totalHeader = totalTaxable.createCell(0);
			totalHeader.setCellValue("Total");

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					2, // first column
					3 // last column
			));

			Cell totTax = totalTaxable.createCell(2);
			totTax.setCellStyle(grandTotalStyle);
			totTax.setCellValue(totalTaxableAmount);

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					4, // first column
					7 // last column
			));

			
			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					8, // first column
					12 // last column
			));

			Cell totalTaxes = totalTaxable.createCell(8);
			totalTaxes.setCellStyle(lastTaxstyle);
			totalTaxes.setCellValue(totalTax);

			rowCount++;
			rowCount++;
		}

		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		// Cell style for terms and condition
		CellStyle tncStyle = workbook.createCellStyle();
		Font tncFont = workbook.createFont();
		tncFont.setFontHeight((short) (7.5 * 35));
		tncStyle.setBorderLeft(BORDER_THIN);
		tncStyle.setBorderRight(BORDER_THIN);
		tncStyle.setBorderTop(BORDER_THIN);
		tncStyle.setBorderBottom(BORDER_THIN);
		tncStyle.setFont(tncFont);

		// For making termsBold
		CellStyle termsStyle = workbook.createCellStyle();
		termsStyle.setBorderLeft(BORDER_THIN);
		termsStyle.setBorderRight(BORDER_THIN);
		termsStyle.setBorderBottom(BORDER_THIN);
		termsStyle.setBorderTop(BORDER_THIN);
		Font termsFont = workbook.createFont();
		termsFont.setFontHeight((short) (7.5 * 35));
		termsFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		termsStyle.setFont(termsFont);

		// For adding border on right..
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
		// rowCount++;
		Row deliver = editAccountSheet.createRow(rowCount);
		Cell deliverCell = deliver.createCell(0);
		deliverCell.setCellStyle(tncStyle);
		deliverCell.setCellValue("Delivery : " + map.get("delivery"));
		// deliverCell.setCellValue(delivery);
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
		WarrantyCell.setCellValue("Warranty : " + map.get("warranty"));
		// WarrantyCell.setCellValue(warranty);
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
		PaymentCell.setCellValue("Mode of Payment : " + map.get("modeOfPayment").replace("$", "%").replace("|","/"));
		// PaymentCell.setCellValue(payment);
		Cell PaymentRightBorder = Payment.createCell(11);
		PaymentRightBorder.setCellStyle(borderRight);
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		// rowCount++;
		Row Taxes = editAccountSheet.createRow(rowCount);
		Cell TaxesCell = Taxes.createCell(0);
		TaxesCell.setCellStyle(tncStyle);
		TaxesCell.setCellValue(taxes);
		Cell TaxesRightBorder = Taxes.createCell(11);
		TaxesRightBorder.setCellStyle(borderRight);
		rowCount++;

		// rowCount++;
		Row transportationRow = editAccountSheet.createRow(rowCount);

		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));

		Cell transportCell = transportationRow.createCell(0);
		transportCell.setCellStyle(fourSideborder);
		transportCell.setCellValue("Freight :" + map.get("freight"));
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		// rowCount++;
		Row jurisdiction = editAccountSheet.createRow(rowCount);
		Cell jurisdictionCell = jurisdiction.createCell(0);
		jurisdictionCell.setCellStyle(tncStyle);
		jurisdictionCell.setCellValue("Jurisdiction : " + map.get("jurisdiction"));
		// jurisdictionCell.setCellValue(Jurisdiction);
		rowCount++;
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				7 // last column
		));
		// rowCount++;
		
		CellStyle cirtificatemergestyle = workbook.createCellStyle();
		cirtificatemergestyle.setBorderBottom(BORDER_THIN);
		cirtificatemergestyle.setBorderTop(BORDER_THIN);
		cirtificatemergestyle.setBorderRight(BORDER_THIN);
		cirtificatemergestyle.setBorderLeft(BORDER_THIN);
		cirtificatemergestyle.setVerticalAlignment((short) (VERTICAL_TOP));
		
		Font cirticateFont = workbook.createFont();
		cirticateFont.setFontHeight((short) (7.5 * 30));
		cirticateFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		cirtificatemergestyle.setFont(cirticateFont);
		cirtificatemergestyle.setWrapText(true);
		
		Row cirtificate = editAccountSheet.createRow(rowCount);
		Cell cirtificateCell = cirtificate.createCell(0);
		cirtificateCell.setCellStyle(cirtificatemergestyle);
		cirtificateCell.setCellValue("INVOICE WILL BE ACCEPTED WITH TEST CERTIFICATES ONLY");
		
		// jurisdictionCell.setCellValue(Jurisdiction);
		rowCount++;
		/*
		 * editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
		 * rowCount, // last row 0, // first column 7 // last column ));
		 * 
		 * //rowCount++; Row Quote = editAccountSheet.createRow(rowCount); Cell
		 * QuoteCell = Quote.createCell(0); QuoteCell.setCellStyle(tncStyle);
		 * QuoteCell.setCellValue(quote); rowCount++;
		 */
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowStart, // first row
				rowCount - 1, // last row
				8, // first column
				12 // last column
		));
		
		
		 signService.insertLogoInTemplate(workbook, editAccountSheet, request, rowStart,user);
		
		/*
		 * setBordersToMergedCells(workbook, editAccountSheet);
		 * editAccountSheet.addMergedRegion(new CellRangeAddress(rowStart, // first row
		 * rowStart, // last row 8, // first column 11 // last column ));
		 */
		// editAccountSheet.autoSizeColumn(6);
		// editAccountSheet.autoSizeColumn(7);

		// setBordersToMergedCells(workbook, editAccountSheet);
		Cell authName = term.createCell(8);
		CellStyle authStyle = workbook.createCellStyle();
		authStyle.setBorderLeft(BORDER_THIN);
		authStyle.setBorderRight(BORDER_THIN);
		authStyle.setBorderBottom(BORDER_THIN);
		authStyle.setBorderTop(BORDER_THIN);
		Font authFont = workbook.createFont();
		authFont.setFontHeight((short) (7.5 * 40));
		authStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		authStyle.setWrapText(true);
		authStyle.setFont(authFont);
		// authName.setCellValue(accountExcelDecrp);
		authName.setCellStyle(authStyle);
		authName.setCellValue("Neptune Controls Pvt Ltd" + "\n" + "\n " + "\n " + "\n " + "\n " + "  Authorized Signatory ");
		// authName.setCellValue( "Neptune Controls Pvt Ltd" +"\n"+"\n\nAuthorized
		// Signatory ");
		// authName.setCellValue("Neptune Controls Pvt Ltd");
		
     
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);

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

	public String getMoneyIntoWords(int grandTotal) {
		MoneyConverters converter = MoneyConverters.ENGLISH_BANKING_MONEY_VALUE;
		return converter.asWords(new BigDecimal(grandTotal));
	}

	public String capitalize(String str) {
		String output = str.substring(0, 1).toUpperCase() + str.substring(1);

		return output;
	}

	// For converting number to words..
	@SuppressWarnings("unused")
	public String convertToIndianCurrency(float num) {
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
		String digits[] = { "", "Hundred", "Thousand", "Lakh", "Crore" };
		while (i < digits_length) {
			int divider = (i == 2) ? 10 : 100;
			number = no % divider;
			no = no / divider;
			i += divider == 10 ? 1 : 2;
			if (number > 0) {
				int counter = str.size();
				String plural = (counter > 0 && number > 9) ? "s" : "";
				String tmp = (number < 21) ? words.get(Integer.valueOf((int) number)) + " " + digits[counter] + plural
						: words.get(Integer.valueOf((int) Math.floor(number / 10) * 10)) + " "
								+ words.get(Integer.valueOf((int) (number % 10))) + " " + digits[counter] + plural;
				str.add(tmp);
			} else {
				str.add("");
			}
		}

		Collections.reverse(str);
		String Rupees = String.join(" ", str).trim();

		String paise = (decimal) > 0
				? " And Paise " + words.get(Integer.valueOf((int) (decimal - decimal % 10))) + " "
						+ words.get(Integer.valueOf((int) (decimal % 10)))
				: "";
		return Rupees + " Only";
	}
}
