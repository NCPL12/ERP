package com.ncpl.sales.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.ncpl.common.Constants;
import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.security.User;

public class PurchasePdf  extends PdfPageEventHelper {
	public static final String RUPEE = "\u20B9";
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	Date d = new Date();
	SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	String invDate = formatter.format(d);
	
	String companyName = "";
	String billingGst = "";
	String billingPan = "";
	String qtn = "";

	String contactPerson = " Ms Sumathy ";
	String contactNo = " 7624919715 ";
	String contactPerson1 = " Mr Shawn ";
	String contactNo1 = " 9901725778 ";
	String gstNo = "GSTIN : 29AADCN5426F1ZG";
	String email = "purchase@ncpl.co";
	String excelHeading1 = "";
	String itemList = "";

	String terms = "";
	String delivery = "";
	String warranty = "";
	String payment = "";
	String taxes = "";
	String Jurisdiction = "";
	String quote = "";
	String billingAddress = null;
	String shippingAddress = null;
	String vendorAddress = null;
	String companyAddress = null;
	@SuppressWarnings("unused")
	private static final Party shipAddress = null;
	private static final DecimalFormat dfZero = new DecimalFormat("#,##0.00");
	
	@SuppressWarnings("unused")
	public String purchaseFunction(PurchaseOrder purchase,HttpServletRequest request) throws DocumentException, MalformedURLException, IOException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String company = "Neptune Controls Pvt Ltd."+"\n"+ "No.8/2(Old No.114),2nd Cross 7th Main Road, Nandidurga Extension"+
				"\n"+"Banglaore- 560046"+"\n"+"GSTIN: 29AADCN5426F1ZG"+"\n"+"State Name : Karnataka, Code : 29"+"\n"+"CIN: U31200KA2011PTC056705"+
				"\n"+"Contact No.:7624964492"+"\n"+"E-Mail : accounts@ncpl.co";

		Date date = new Date();
		// writePdf(outputStream);
		String fileName = "purchase" + "1" + date.getTime() + ".pdf";
		String path = createPurchasePDF(fileName, purchase, company,request);
		return path;
	}

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	private String createPurchasePDF(String fileName, PurchaseOrder poObject, String company,HttpServletRequest request) throws DocumentException, MalformedURLException, IOException {
		
		Document doc = new Document(PageSize.A4, 50, 30, 25, 10);

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
		
		User user = (User) request.getAttribute("user");

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
						+ party.getParty_city().getName() +" " +pincode+ "\n" + "Contact: " + contactNo + "\nGSTIN: " + gst;
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
			billingAddress = billingAddress.replace("No 8/2(Old No 114),2nd Cross 7th Main Road,Nandi",
					"No 8/2(Old No 114).2nd Cross 7th Main Road.Nandi,");
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
						+ altshippingParty.getParty_city().getName() +" " +pincode+ "\n" + "Contact: " + contactNo + "\nEmail: " + altshippingParty.getEmail1();
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
		
		Date date = poObject.getCreated();
		// Converting date to String
		String dateString = date.toString();
		String[] arr = dateString.split(" ");

		// Formatting date to a required format
		String formattedDate = arr[0];
		formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "/"
				+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "/"
				+ formattedDate.substring(0, 4);

		// SalesOrder salesOrderObject = poObject.get().getSalesOrder();
		List<PurchaseItem> items = poObject.getItems();
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
		String ponum = poObject.getPoNumber();
		
		
		PdfWriter docWriter = null;

		DecimalFormat df = new DecimalFormat("0.00");
		ByteArrayOutputStream baos = null;
		String path = null;

		String FILE_LOCATION = System.getProperty("user.home") + File.separator + "PDF_FILES";

		path = FILE_LOCATION + File.separator + fileName;
		System.out.println(path + "path>>>>>>>>>");
		Document document = new Document(PageSize.A4, 50, 30, 25, 30);

		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(path));
		// BillByMonthPdf event = new BillByMonthPdf();
		// writer.setPageEvent(event);
		document.open();

		Font detailsFont = new Font(FontFamily.HELVETICA, 10);
		Font detailsFontBold = new Font(FontFamily.HELVETICA, 10,Font.BOLD);
		Font gtBoldFont = new Font(FontFamily.HELVETICA, 11,Font.BOLD);
		Paragraph paragraph = new Paragraph();
		paragraph.setAlignment(Element.ALIGN_CENTER);
		Font companyFont = new Font(FontFamily.HELVETICA, 14,Font.BOLD);
		
		// paragraph.add(new Chunk("Neptune Controls Pvt Ltd"));
		paragraph.add(new Chunk("  Neptune Controls Pvt Ltd",companyFont));
		// paragraph.add(new Chunk("To :" + "NCPL \n"));
		// paragraph.add(new Chunk(" Email Id :" + "owner@ncpl.co \n"));

		
			String imgLoc = "http://localhost:8080/ncpl-sales/resources/dist/img/ncpl_logo_pdf.png";
			  Image img = Image.getInstance(imgLoc);
      img.setAbsolutePosition(53f, 760f);
      document.add(img);
		
		
		PdfPTable table = new PdfPTable(2);
		table.setSpacingBefore(5);
		table.setWidths(new int[] { 7, 7 });
		table.setWidthPercentage(100);
		table.addCell("");
		//table.addCell(paragraph);
		
		PdfPTable purchaseTbl = new PdfPTable(1);
		purchaseTbl.setWidthPercentage(100);
		
		Phrase poNumner = new Phrase();
		poNumner.add(new Chunk(new VerticalPositionMark()));
		poNumner.add(new Chunk("PO No: ", detailsFont));
		poNumner.add(new Chunk(poObject.getPoNumber(), detailsFont));
		purchaseTbl.addCell(poNumner);
		
		
		Phrase poDate = new Phrase();
		poDate.add(new Chunk(new VerticalPositionMark()));
		poDate.add(new Chunk("PO Date: ", detailsFont));
		poDate.add(new Chunk(formattedDate, detailsFont));
		purchaseTbl.addCell(poDate);
		
		Phrase contactPersn = new Phrase();
		contactPersn.add(new Chunk(new VerticalPositionMark()));
		contactPersn.add(new Chunk("Quote Ref No.: ", detailsFont));
		contactPersn.add(new Chunk(quoteRefNo, detailsFont));
		purchaseTbl.addCell(contactPersn);
		
		Phrase contactNum = new Phrase();
		contactNum.add(new Chunk(new VerticalPositionMark()));
		contactNum.add(new Chunk("Quote Date: ", detailsFont));
		contactNum.add(new Chunk(quoteDate, detailsFont));
		purchaseTbl.addCell(contactNum);
		
		/*Phrase emailAdr = new Phrase();
		emailAdr.add(new Chunk(new VerticalPositionMark()));
		emailAdr.add(new Chunk("Email: ", detailsFont));
		emailAdr.add(new Chunk("purchase@ncpl.co", detailsFont));
		purchaseTbl.addCell(emailAdr);*/
		
		table.addCell(purchaseTbl);
		document.add(table);
		
		table = new PdfPTable(3);
		table.setSpacingBefore(5);
		table.setWidths(new int[] { 4, 4, 4 });
		table.setWidthPercentage(100);
		PdfPCell shippingAddr = null;
		PdfPCell vendorAddr = new PdfPCell(new Paragraph(new Chunk("Supplier:" + "\n" + vendorAddress,detailsFont)));
		if(shippingAddressKey.contains("key")) {
		    shippingAddr = new PdfPCell(new Paragraph(new Chunk("Deliver To:" + "\n" + shippingAddress + "\nContact: " + user.getName() + "@" + user.getNumber() +"\nEmail: " + user.getEmailId() ,detailsFont)));
		}else {
			shippingAddr= new PdfPCell(new Paragraph(new Chunk("Deliver To:" + "\n" + shippingAddress,detailsFont)));
		}
		PdfPCell billingAddr = new PdfPCell(new Paragraph(new Chunk("Billing & Invoice:" + "\n" + billingAddress + "\nPAN NO: " + billingPan + "\nGSTIN : " + billingGst,detailsFont)));
		table.addCell(vendorAddr);
		table.addCell(shippingAddr);
		table.addCell(billingAddr);
		document.add(table);
		
		table = new PdfPTable(1);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		table.addCell(new Paragraph(new Chunk(itemList,detailsFont)));  
		document.add(table);
		
		table = new PdfPTable(9);
		table.setSpacingBefore(5);
		table.setWidths(new int[] {1,4,4,2,1,2,1,2,3});
		table.setWidthPercentage(100);
		table.addCell(new Phrase(new Chunk("Sl No",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("Description of Goods",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("Model No",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("HSN Code",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("GST(%)",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("Qty",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("Unit",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("Unit Price",detailsFontBold))); 
		table.addCell(new Phrase(new Chunk("Amount",detailsFontBold))); 
		document.add(table);
		
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
		double igst = 0;
		double tGst = 0;
		double cGst = 0;
		double sGst = 0;
		for (PurchaseItem purchaseItem : newItems) {
			if(purchaseItem.getQuantity()!=0) {
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
	
				
				if (Integer.parseInt(gst) > 0) {
					double gstValue = Float.parseFloat(gst) / 2;
					gstValue = gstValue / 100;
					cGst = (cGst + (gstValue * purchaseItem.getAmount()));
					sGst = (sGst + (gstValue * purchaseItem.getAmount()));
					igst = (igst + ((Float.parseFloat(gst) / 100) * purchaseItem.getAmount()));
					
				}
				
				table = new PdfPTable(9);
				table.setSpacingBefore(5);
				table.setWidths(new int[] {1,4,4,2,1,2,1,2,3});
				table.setWidthPercentage(100);
				//table.addCell(new Paragraph(new Chunk(Integer.toString(slNumber),detailsFont)));
				//table.addCell(new Paragraph(new Chunk(purchaseItem.getPoDescription(),detailsFont)));
				

				PdfPCell slNumCell = new PdfPCell();
				Phrase slNumPhrase= new Phrase();
				//slNumPhrase.add(new Chunk(new VerticalPositionMark()));
				slNumPhrase.add(new Chunk(Integer.toString(slNumber),detailsFont));
				
				slNumCell.addElement(slNumPhrase);
				table.addCell(slNumCell);
				

				PdfPCell poDescCell = new PdfPCell();
				Phrase poDescPhrase= new Phrase();
				//poDescPhrase.add(new Chunk(new VerticalPositionMark()));
				poDescPhrase.add(new Chunk(purchaseItem.getPoDescription(),detailsFont));
				
				poDescCell.addElement(poDescPhrase);
				table.addCell(poDescCell);
				
				PdfPCell modelCell = new PdfPCell();
				Phrase modelPhrase= new Phrase();
				//modelPhrase.add(new Chunk(new VerticalPositionMark()));
				if (modelValue == null || modelValue == "") {
					//table.addCell(new Paragraph(new Chunk(purchaseItem.getModelNo(),detailsFont)));
					modelPhrase.add(new Chunk(purchaseItem.getModelNo(),detailsFont));
	
				} else {
					//table.addCell(new Paragraph(new Chunk(modelValue,detailsFont)));
					modelPhrase.add(new Chunk(modelValue,detailsFont));
				}
				modelCell.addElement(modelPhrase);
				table.addCell(modelCell);
				//table.addCell(new Paragraph(new Chunk(purchaseItem.getHsnCode(),detailsFont)));
				//table.addCell(new Paragraph(new Chunk(gst,detailsFont)));
				
				PdfPCell hsnCell = new PdfPCell();
				Phrase hsnPhrase= new Phrase();
				//hsnPhrase.add(new Chunk(new VerticalPositionMark()));
				hsnPhrase.add(new Chunk(purchaseItem.getHsnCode(),detailsFont));
				
				hsnCell.addElement(hsnPhrase);
				table.addCell(hsnCell);
				
				PdfPCell gstCell = new PdfPCell();
				Phrase gstPhrase= new Phrase();
				//gstPhrase.add(new Chunk(new VerticalPositionMark()));
				gstPhrase.add(new Chunk(gst,detailsFont));
				
				gstCell.addElement(gstPhrase);
				table.addCell(gstCell);
				
				PdfPCell qtyCell = new PdfPCell();
				Phrase qtyPhrase= new Phrase();
				qtyPhrase.add(new Chunk(new VerticalPositionMark()));
				qtyPhrase.add(new Chunk(formatLakh(purchaseItem.getQuantity()),detailsFont));
				
				qtyCell.addElement(qtyPhrase);
				table.addCell(qtyCell);
				
				
				PdfPCell unitNameCell = new PdfPCell();
				Phrase unitNamePhrase= new Phrase();
				//unitNamePhrase.add(new Chunk(new VerticalPositionMark()));
				unitNamePhrase.add(new Chunk(unitName,detailsFont));
				
				unitNameCell.addElement(unitNamePhrase);
				table.addCell(unitNameCell);
				//table.addCell(new Paragraph(new Chunk(unitName,detailsFont)));
				
				PdfPCell upCell = new PdfPCell();
				Phrase upPhrase= new Phrase();
				upPhrase.add(new Chunk(new VerticalPositionMark()));
				upPhrase.add(new Chunk(formatLakh(purchaseItem.getUnitPrice()),detailsFont));
				
				upCell.addElement(upPhrase);
				table.addCell(upCell);
				
				PdfPCell amCell = new PdfPCell();
				Phrase amPhrase= new Phrase();
				amPhrase.add(new Chunk(new VerticalPositionMark()));
				amPhrase.add(new Chunk(formatLakh(purchaseItem.getAmount()),detailsFont));
				
				amCell.addElement(amPhrase);
				table.addCell(amCell);
				
				float amount = purchaseItem.getAmount();
				totalAmount = totalAmount + amount;
				slNumber++;
				document.add(table);
			}
		}
		table= new PdfPTable(1);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		PdfPCell emptyCell = new PdfPCell();
		emptyCell.setBorder(0);
		
		PdfPTable Totaltable = new PdfPTable(2);
		Totaltable.setWidths(new int[] { 7,3 });
		//Totaltable.setSpacingBefore(5);
		Totaltable.addCell(emptyCell);
		PdfPCell totalCell = new PdfPCell();
		totalCell.setBorder(0);
		Phrase totalPhrase= new Phrase();
		double totalAm  = Math.round(totalAmount * 100.0) / 100.0;
		totalPhrase.add(new Chunk("				Total:", detailsFont));
		totalPhrase.add(new Chunk(new VerticalPositionMark()));
		totalPhrase.add(new Chunk(formatLakh(totalAm),detailsFont));
		
		totalCell.addElement(totalPhrase);
		Totaltable.addCell(totalCell);
		table.addCell(Totaltable);
		
		
		PdfPTable gstTable = new PdfPTable(2);
		gstTable.setWidths(new int[] { 7,3 });
		gstTable.addCell(emptyCell);
		PdfPCell gstCell = new PdfPCell();
		gstCell.setBorder(0);
		Phrase gstPhrase= new Phrase();
		if (gstRegion.contains("State")) {
			gstPhrase.add(new Chunk("				GST:", detailsFont));
			tGst = cGst + sGst;
			tGst  = Math.round(tGst * 100.0) / 100.0;
			gstPhrase.add(new Chunk(new VerticalPositionMark()));
			gstPhrase.add(new Chunk(formatLakh(tGst),detailsFont));

		}
		if (gstRegion.contains("Inter")) {
			double igstAm  = Math.round(igst * 100.0) / 100.0;
			gstPhrase.add(new Chunk("				IGST:", detailsFont));
			gstPhrase.add(new Chunk(new VerticalPositionMark()));
			gstPhrase.add(new Chunk(formatLakh(igstAm),detailsFont));
		}
		
		gstCell.addElement(gstPhrase);
		gstTable.addCell(gstCell);
		table.addCell(gstTable);
		
		PdfPTable roundOffTbl = new PdfPTable(2);
		roundOffTbl.setWidths(new int[] { 7,3 });
		roundOffTbl.addCell(emptyCell);
		PdfPCell roundOffCell = new PdfPCell();
		roundOffCell.setBorder(0);
		Phrase roundOffPhrase= new Phrase();
		roundOffPhrase.add(new Chunk("				Round Off:", detailsFont));
		roundOffPhrase.add(new Chunk(new VerticalPositionMark()));
		if (gstRegion.contains("Inter")) {
			gstAmount = igst;
		} else {
			gstAmount = cGst + sGst;
			// gstAmount = sgst9+cgst9+sgst14+cgst14+sgst025+cgst025;
		}
		grandTotal = gstAmount + totalAmount;
		roundoff = grandTotal - Math.round(grandTotal * 100) / 100;
		Double roundOffamount = Math.round(roundoff * 100.0) / 100.0;
		roundOffPhrase.add(new Chunk(formatLakh(roundOffamount),detailsFont));
		
		roundOffCell.addElement(roundOffPhrase);
		roundOffTbl.addCell(roundOffCell);
		table.addCell(roundOffTbl);
		
		PdfPTable grandTotalTbl = new PdfPTable(2);
		grandTotalTbl.setWidths(new int[] { 7,3 });
		grandTotalTbl.addCell(emptyCell);
		PdfPCell  grandTotalCell = new PdfPCell();
		grandTotalCell.setBorder(0);
		Phrase  grandTotalPhrase= new Phrase();
		grandTotalPhrase.add(new Chunk("				Grand Total:", gtBoldFont));
		grandTotalPhrase.add(new Chunk(new VerticalPositionMark()));
		if (gstRegion.contains("Inter")) {
			gstAmount = igst;
		} else {
			gstAmount = cGst + sGst;
		}
		grandTotal = gstAmount + totalAmount;
		grandTotal = Math.round(grandTotal * 100) / 100;
		if (roundoff >= 0.5) {
			grandTotal = grandTotal + 1;
		}
		grandTotalPhrase.add(new Chunk(formatLakh(grandTotal),gtBoldFont));
		
		grandTotalCell.addElement(grandTotalPhrase);
		grandTotalTbl.addCell(grandTotalCell);
		table.addCell(grandTotalTbl);
		document.add(table);

		table= new PdfPTable(1);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		String numberInWords =  convertToIndianCurrency((int) Math.round(grandTotal * 100) / 100);
		PdfPTable totalInWordTable = new PdfPTable(1);
		PdfPCell grandTotalCellInWords = new PdfPCell();
		grandTotalCellInWords.setBorder(0);
		Phrase grandtotalINWordsPhraseCell= new Phrase();
		grandtotalINWordsPhraseCell.add(new Chunk("Rupees (In Words) : ", detailsFont));
		grandtotalINWordsPhraseCell.add(new Chunk(numberInWords, detailsFont));
		
		grandTotalCellInWords.addElement(grandtotalINWordsPhraseCell);
		totalInWordTable.addCell(grandTotalCellInWords);
		table.addCell(totalInWordTable);
		document.add(table);
		
		
		
		
		if (gstRegion.contains("State")) {
			
			table = new PdfPTable(5);
			table.setSpacingBefore(5);
			table.setWidths(new int[] {5,4,4,4,3});
			table.setWidthPercentage(100);
			
			PdfPCell taxRateCell = new PdfPCell();
			Phrase taxRatePhrase = new Phrase();
			Chunk taxRateChunk = new Chunk("Tax Rate",detailsFontBold);
			taxRateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			taxRatePhrase.add(taxRateChunk);
			taxRateCell.setPhrase(taxRatePhrase);
			table.addCell(taxRateCell); 
			
			
			PdfPCell taxableVCell = new PdfPCell();
			Phrase taxableVPhrase = new Phrase();
			Chunk taxableVChunk = new Chunk("Taxable Value",detailsFontBold);
			taxableVCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			taxableVPhrase.add(taxableVChunk);
			taxableVCell.setPhrase(taxableVPhrase);
			table.addCell(taxableVCell); 
			
			PdfPCell centralTaxCell = new PdfPCell();
			Phrase centralTaxPhrase = new Phrase();
			Chunk centralTaxChunk = new Chunk("Central Tax",detailsFontBold);
			centralTaxCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			centralTaxPhrase.add(centralTaxChunk);
			centralTaxCell.setPhrase(centralTaxPhrase);
			table.addCell(centralTaxCell); 
			
			PdfPCell staxVCell = new PdfPCell();
			Phrase staxVPhrase = new Phrase();
			Chunk staxVChunk = new Chunk("State Tax",detailsFontBold);
			staxVCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			staxVPhrase.add(staxVChunk);
			staxVCell.setPhrase(staxVPhrase);
			table.addCell(staxVCell); 
			
			
			PdfPCell taxAmCell = new PdfPCell();
			Phrase taxAmPhrase = new Phrase();
			Chunk taxAmChunk = new Chunk("Total" + "\n" + "Tax Amount",detailsFontBold);
			taxAmCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			taxAmPhrase.add(taxAmChunk);
			taxAmCell.setPhrase(taxAmPhrase);
			table.addCell(taxAmCell); 
			
			document.add(table);
			
			
			table = new PdfPTable(7);
			table.setWidths(new int[] {5,4,2,2,2,2,3});
			table.setWidthPercentage(100);
			table.addCell(""); 
			table.addCell(""); 
			table.addCell(new Paragraph(new Chunk("Rate",detailsFontBold))); 
			table.addCell(new Paragraph(new Chunk("Amount",detailsFontBold))); 
			table.addCell(new Paragraph(new Chunk("Rate",detailsFontBold))); 
			table.addCell(new Paragraph(new Chunk("Amount",detailsFontBold))); 
			table.addCell(""); 
			document.add(table);
			
			
			table = new PdfPTable(7);
			table.setWidths(new int[] {5,4,2,2,2,2,3});
			table.setWidthPercentage(100);
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
				
				PdfPCell taxGstCell = new PdfPCell();
				Phrase taxGstPhrase= new Phrase();
				taxGstPhrase.add(new Chunk(new VerticalPositionMark()));
				taxGstPhrase.add(new Chunk("GST@" + gst + "%",detailsFont));
				
				taxGstCell.addElement(taxGstPhrase);
				table.addCell(taxGstCell);
				
				double taxableValue = (value * 100) / Integer.parseInt(gst);
				taxableValue = Math.round(taxableValue * 100.0) / 100.0;
				totalTaxableAmount = totalTaxableAmount + taxableValue;
				totalTaxableAmount = Math.round(totalTaxableAmount * 100.0) / 100.0;
				
				PdfPCell taxableCell = new PdfPCell();
				Phrase taxablePhrase= new Phrase();
				taxablePhrase.add(new Chunk(new VerticalPositionMark()));
				taxablePhrase.add(new Chunk(formatLakh(taxableValue),detailsFont));
				
				taxableCell.addElement(taxablePhrase);
				table.addCell(taxableCell);
				
				
				Double crate = (double) (Float.parseFloat(gst) / 2);
				crate = Math.round(crate * 100.0) / 100.0;
				

				PdfPCell crateCell = new PdfPCell();
				Phrase cratePhrase= new Phrase();
				cratePhrase.add(new Chunk(new VerticalPositionMark()));
				cratePhrase.add(new Chunk(crate + "%",detailsFont));
				
				crateCell.addElement(cratePhrase);
				table.addCell(crateCell);
				
				double cgsValue = value / 2;
				cgsValue = Math.round(cgsValue * 100.0) / 100.0;
				
				PdfPCell cgsValueCell = new PdfPCell();
				Phrase cgsValuePhrase= new Phrase();
				cgsValuePhrase.add(new Chunk(new VerticalPositionMark()));
				cgsValuePhrase.add(new Chunk(formatLakh(cgsValue),detailsFont));
				
				cgsValueCell.addElement(cgsValuePhrase);
				table.addCell(cgsValueCell);
				
				Double srate = (double) (Float.parseFloat(gst) / 2);
				srate = Math.round(srate * 100.0) / 100.0;
				
				PdfPCell srateCell = new PdfPCell();
				Phrase sratePhrase= new Phrase();
				sratePhrase.add(new Chunk(new VerticalPositionMark()));
				sratePhrase.add(new Chunk(srate + "%",detailsFont));
				
				srateCell.addElement(sratePhrase);
				table.addCell(srateCell);
				
				double sgsValue = value / 2;
				sgsValue = Math.round(sgsValue * 100.0) / 100.0;
				
				PdfPCell sgsValueCell = new PdfPCell();
				Phrase sgsValuePhrase= new Phrase();
				sgsValuePhrase.add(new Chunk(new VerticalPositionMark()));
				sgsValuePhrase.add(new Chunk(formatLakh(sgsValue),detailsFont));
				
				sgsValueCell.addElement(sgsValuePhrase);
				table.addCell(sgsValueCell);
				

				totalTax = totalTax + value;
				totalTax =Math.round(totalTax * 100.0) / 100.0;
				
				PdfPCell valueCell = new PdfPCell();
				Phrase valuePhrase= new Phrase();
				valuePhrase.add(new Chunk(new VerticalPositionMark()));
				valuePhrase.add(new Chunk(formatLakh(value),detailsFont));
				
				valueCell.addElement(valuePhrase);
				table.addCell(valueCell);

				
			}
			document.add(table);
			// Last row of taxcell

			// Style
			

			table = new PdfPTable(4);
			table.setWidths(new int[] {5,4,8,3});
			table.setWidthPercentage(100);
			table.addCell(new Paragraph(new Chunk("Total",detailsFont)));
			
			PdfPCell totalTaxableAmountCell = new PdfPCell();
			Phrase totalTaxableAmountPhrase= new Phrase();
			totalTaxableAmountPhrase.add(new Chunk(new VerticalPositionMark()));
			totalTaxableAmountPhrase.add(new Chunk(formatLakh(totalTaxableAmount),detailsFont));
			
			totalTaxableAmountCell.addElement(totalTaxableAmountPhrase);
			table.addCell(totalTaxableAmountCell);
			
			table.addCell("");
			
			PdfPCell totalTaxCell = new PdfPCell();
			Phrase totalTaxPhrase= new Phrase();
			totalTaxPhrase.add(new Chunk(new VerticalPositionMark()));
			totalTaxPhrase.add(new Chunk(formatLakh(totalTax),detailsFont));
			
			totalTaxCell.addElement(totalTaxPhrase);
			table.addCell(totalTaxCell);
			
			document.add(table);
		}
		// For inter

		if (gstRegion.contains("Inter")) {
			table = new PdfPTable(3);
			table.setSpacingBefore(5);
			table.setWidths(new int[] {5,8,7});
			table.setWidthPercentage(100);
			
			PdfPCell taxableVCell = new PdfPCell();
			Phrase taxableVPhrase = new Phrase();
			Chunk taxableVChunk = new Chunk("Taxable Value",detailsFontBold);
			taxableVCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			taxableVPhrase.add(taxableVChunk);
			taxableVCell.setPhrase(taxableVPhrase);
			table.addCell(taxableVCell); 
			
			PdfPCell igstVCell = new PdfPCell();
			Phrase igstVPhrase = new Phrase();
			Chunk igstVChunk = new Chunk("IGST",detailsFontBold);
			igstVCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			igstVPhrase.add(igstVChunk);
			igstVCell.setPhrase(igstVPhrase);
			table.addCell(igstVCell); 
			
			PdfPCell taxAmCell = new PdfPCell();
			Phrase taxAmPhrase = new Phrase();
			Chunk taxAmChunk = new Chunk("Total" + "\n" + "Tax Amount",detailsFontBold);
			taxAmCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			taxAmPhrase.add(taxAmChunk);
			taxAmCell.setPhrase(taxAmPhrase);
			table.addCell(taxAmCell); 
			
			document.add(table);
			
			
			table = new PdfPTable(4);
			table.setWidths(new int[] {5,4,4,7});
			table.setWidthPercentage(100);
			table.addCell(""); 
			table.addCell(new Paragraph(new Chunk("Rate",detailsFontBold))); 
			table.addCell(new Paragraph(new Chunk("Amount",detailsFontBold))); 
			table.addCell(""); 
			document.add(table);
			
			table = new PdfPTable(4);
			table.setWidths(new int[] {5,4,4,7});
			table.setWidthPercentage(100);
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
					double taxableValue = (value * 100) / Integer.parseInt(gst);
					taxableValue = Math.round(taxableValue * 100.0) / 100.0;
					totalTaxableAmount = totalTaxableAmount + taxableValue;
					totalTaxableAmount =Math.round(totalTaxableAmount * 100.0) / 100.0;
					
					PdfPCell taxableValueCell = new PdfPCell();
					Phrase taxableValuePhrase= new Phrase();
					taxableValuePhrase.add(new Chunk(new VerticalPositionMark()));
					taxableValuePhrase.add(new Chunk(formatLakh(taxableValue),detailsFont));
					
					taxableValueCell.addElement(taxableValuePhrase);
					table.addCell(taxableValueCell);
					
					float gstRateValue = (float) (Math.round(Integer.parseInt(gst) * 100.0) / 100.0);
					
					PdfPCell gstRateValueCell = new PdfPCell();
					Phrase gstRateValuePhrase= new Phrase();
					gstRateValuePhrase.add(new Chunk(new VerticalPositionMark()));
					gstRateValuePhrase.add(new Chunk(gstRateValue + "%",detailsFont));
					
					gstRateValueCell.addElement(gstRateValuePhrase);
					table.addCell(gstRateValueCell);
					
					PdfPCell gvalueCell = new PdfPCell();
					Phrase gvaluePhrase= new Phrase();
					gvaluePhrase.add(new Chunk(new VerticalPositionMark()));
					gvaluePhrase.add(new Chunk(formatLakh(value),detailsFont));
				
					gvalueCell.addElement(gvaluePhrase);
					table.addCell(gvalueCell);
					
					totalTax = totalTax + value;
					totalTax = Math.round(totalTax * 100.0) / 100.0;
					
					PdfPCell valueCell = new PdfPCell();
					Phrase valuePhrase= new Phrase();
					valuePhrase.add(new Chunk(new VerticalPositionMark()));
					valuePhrase.add(new Chunk(formatLakh(value),detailsFont));
					
					valueCell.addElement(valuePhrase);
					table.addCell(valueCell);
					
				}
			}
			document.add(table);
			// Last row

			// Style

			table = new PdfPTable(4);
			table.setWidths(new int[] {2,3,8,7});
			table.setWidthPercentage(100);
			table.addCell(new Paragraph(new Chunk("Total",detailsFont)));
			
			PdfPCell totalTaxableAmountCell = new PdfPCell();
			Phrase totalTaxableAmountPhrase= new Phrase();
			totalTaxableAmountPhrase.add(new Chunk(new VerticalPositionMark()));
			totalTaxableAmountPhrase.add(new Chunk(formatLakh(totalTaxableAmount),detailsFont));
			
			totalTaxableAmountCell.addElement(totalTaxableAmountPhrase);
			table.addCell(totalTaxableAmountCell);
			
			table.addCell("");
			
			PdfPCell totalTaxCell = new PdfPCell();
			Phrase totalTaxPhrase= new Phrase();
			totalTaxPhrase.add(new Chunk(new VerticalPositionMark()));
			totalTaxPhrase.add(new Chunk(formatLakh(totalTax),detailsFont));
			
			totalTaxCell.addElement(totalTaxPhrase);
			table.addCell(totalTaxCell);
			
			document.add(table);
		}
	
		table = new PdfPTable(2);
		table.setSpacingBefore(5);
		table.setWidths(new int[] {13, 7});
		table.setWidthPercentage(100);
		
		PdfPTable TermsnCondnTbl = new PdfPTable(1);
		TermsnCondnTbl.setWidthPercentage(100);
		
		Phrase TermsnCondn = new Phrase();
		TermsnCondn.add(new Chunk(terms, detailsFontBold));
		TermsnCondnTbl.addCell(TermsnCondn);
		
		Phrase delvery = new Phrase();
		delvery.add(new Chunk("Delivery :", detailsFont));
		delvery.add(new Chunk(map.get("delivery"), detailsFont));
		TermsnCondnTbl.addCell(delvery);
		
		Phrase warnty = new Phrase();
		warnty.add(new Chunk("Warranty : ", detailsFont));
		warnty.add(new Chunk(map.get("warranty"), detailsFont));
		TermsnCondnTbl.addCell(warnty);
		
		Phrase modeOfPay = new Phrase();
		modeOfPay.add(new Chunk("Mode of Payment : ", detailsFont));
		modeOfPay.add(new Chunk(map.get("modeOfPayment").replace("$","%").replace("|","/"), detailsFont));
		TermsnCondnTbl.addCell(modeOfPay);
		
		Phrase taxs = new Phrase();
		taxs.add(new Chunk(taxes, detailsFont));
		TermsnCondnTbl.addCell(taxs);
		
		Phrase friegt = new Phrase();
		friegt.add(new Chunk("Freight :", detailsFont));
		friegt.add(new Chunk(map.get("freight"), detailsFont));
		TermsnCondnTbl.addCell(friegt);
		
		Phrase jurisdiction = new Phrase();
		jurisdiction.add(new Chunk("Jurisdiction : ", detailsFont));
		jurisdiction.add(new Chunk(map.get("jurisdiction"), detailsFont));
		TermsnCondnTbl.addCell(jurisdiction);
		
		Phrase connditn = new Phrase();
		connditn.add(new Chunk("INVOICE WILL BE ACCEPTED WITH TEST CERTIFICATES ONLY", detailsFontBold));
		TermsnCondnTbl.addCell(connditn);
		table.addCell(TermsnCondnTbl);
		
		PdfPTable signTable = new PdfPTable(1);
		PdfPCell companyCell = new PdfPCell();
		signTable.setWidthPercentage(100);
		companyCell.setBorder(0);
		Phrase companyName = new Phrase();
		companyName.add(new Chunk("Neptune Controls Pvt Ltd"+"\n"+"\n", detailsFont));
		companyCell.setPaddingBottom(0f);
		companyCell.setPaddingTop(0f);
		companyCell.addElement(companyName);
		signTable.addCell(companyCell);

		PdfPCell signCell = new PdfPCell();
		signCell.setBorder(0);
		//Phrase sign = new Phrase();
		String imgLoc1;
		if(user.getUsername().equalsIgnoreCase("vighneshwar")) {
			 imgLoc1 = "http://localhost:8080/ncpl-sales/resources/dist/img/vigneshwar_sign.jpg";
		}else {
			 //imgLoc1 = "http://localhost:8888/ncpl-sales/resources/dist/img/abhilashSign2.jpg";
			imgLoc1 = "http://localhost:8080/ncpl-sales/resources/dist/img/sumathySign2.jpg";

		}
		  Image img2 = Image.getInstance(imgLoc1);
		 // img2.scaleAbsoluteHeight(1f);
		//  img2.scaleAbsoluteWidth(1f);
		 
		signCell.setPaddingBottom(0f);
		signCell.setPaddingTop(0f);
		 signCell.setImage(img2);
		signTable.addCell(signCell);
		
		PdfPCell authCell = new PdfPCell();
		authCell.setBorder(0);
		Phrase auth = new Phrase();
		auth.add(new Chunk("Authorized Signatory", detailsFont));
		authCell.setPaddingBottom(0f);
		authCell.setPaddingTop(0f);
		authCell.addElement(auth);
		signTable.addCell(authCell);
		table.addCell(signTable);
		//table.addCell(new Paragraph(new Chunk("Neptune Controls Pvt Ltd" + "\n " + "\n " + "\n " + "\n " + "\n "+"\n " + "\n " + "\n " + "  Authorized Signatory ",detailsFont)));
		document.add(table);
		
		
		
		document.close();
		
		return path;

	}
	public String capitalize(String str) {
		String output = str.substring(0, 1).toUpperCase() + str.substring(1);

		return output;
	}
	
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
	private String formatLakh(double d) {
	    String s = String.format(Locale.UK, "%1.2f", Math.abs(d));
	    s = s.replaceAll("(.+)(...\\...)", "$1,$2");
	    while (s.matches("\\d{3,},.+")) {
	        s = s.replaceAll("(\\d+)(\\d{2},.+)", "$1,$2");
	    }
	    return d < 0 ? ("-" + s) : s;
	}

	
}
