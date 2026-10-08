package com.ncpl.sales.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.VerticalPositionMark;
import com.ncpl.common.Constants;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.InvoiceItem;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;

public class InvoicePdf extends PdfPageEventHelper {
	public static final String RUPEE = "\u20B9";
	@SuppressWarnings("unused")
	private static final Party shipAddress = null;

	@SuppressWarnings({ "unused", "rawtypes" })
	public String billByMonth(int bills, String startDate, Long flat_no, int months, Map<String, Map> meterReadingMap,
			float totalMeterUsage, float totalMeterUsagePrevious, String buildingName)
			throws DocumentException, IOException {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		Month month = Month.of(months);

		Date date = new Date();
		// writePdf(outputStream);
		String fileName = "billByMonth" + month + date.getTime() + ".pdf";
		String path = createPDF(fileName, bills, startDate, flat_no, month, meterReadingMap, totalMeterUsage,
				totalMeterUsagePrevious, buildingName);
		return path;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private String createPDF(String fileName, int bills, String startDate, Long flat_no, Month month,
			Map<String, Map> meterReadingMap, float totalMeterUsage, float totalMeterUsagePrevious, String buildngName)
			throws DocumentException, IOException {
		// TODO Auto-generated method stub

		Document doc = new Document(PageSize.A4, 50, 30, 25, 10);

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
		Paragraph paragraph = new Paragraph();
		paragraph.setAlignment(Element.ALIGN_CENTER);
		// paragraph.add(new Chunk("Neptune Controls Pvt Ltd"));
		paragraph.add("Neptune Controls Pvt Ltd");
		// paragraph.add(new Chunk("To :" + "NCPL \n"));
		// paragraph.add(new Chunk(" Email Id :" + "owner@ncpl.co \n"));

		PdfPTable table = new PdfPTable(3);
		table.setSpacingBefore(5);
		table.setWidths(new int[] { 2, 5, 7 });
		table.setWidthPercentage(100);
		table.addCell("Value a");
		table.addCell(paragraph);
		table.addCell(
				"Complete solution for BMS, Lighting Control, CCTV & Security Systems, DDC Panels, Automation Panels, Lighting,panels, "
						+ "MCC & Starter Panels");
		document.add(table);
		// document.add(new Paragraph("With 2 columns:"));
		table = new PdfPTable(2);
		table.getDefaultCell().setBorder(0);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		table.getDefaultCell().setColspan(1);
		Font personFont = new Font(FontFamily.HELVETICA, 10);
		personFont.setColor(BaseColor.WHITE);
		PdfPCell persondataCell = new PdfPCell();
		Phrase personPhrase = new Phrase();
		Chunk personChunk = new Chunk("Person Details");
		personChunk.setFont(personFont);
		personPhrase.add(personChunk);
		persondataCell.setPhrase(personPhrase);
		// persondataCell.setPhrase(new Phrase("Person Details"));
		persondataCell.setPaddingBottom(5.5f);
		persondataCell.setPaddingTop(0f);
		persondataCell.setBackgroundColor(new BaseColor(11, 128, 198));
		persondataCell.setBorder(0);
		table.addCell(persondataCell);
		Font accountFont = new Font(FontFamily.HELVETICA, 10);
		accountFont.setColor(BaseColor.WHITE);
		PdfPCell accountCell = new PdfPCell();
		Phrase accPhrase = new Phrase();
		Chunk accChunk = new Chunk("Account Details");
		accChunk.setFont(accountFont);
		accPhrase.add(accChunk);
		accountCell.setPhrase(accPhrase);
		accountCell.setPaddingBottom(5.5f);
		accountCell.setPaddingTop(0f);
		accountCell.setBackgroundColor(new BaseColor(11, 128, 198));
		// p.getDefaultCell().setBorder(Rectangle.BOTTOM);
		accountCell.setBorder(0);
		// table.getDefaultCell().setBorderColor(BaseColor.BLACK);
		table.addCell(accountCell);
		// table.addCell("Account Details");
		// table.addCell("To :"+" "+"NCPL ");
		// String toName = bills.getOwner_name();
		String toName = "abc";
		table.addCell(new Phrase("Name : " + toName, detailsFont));

		PdfPCell apartmentCell = new PdfPCell();
		apartmentCell.setBorder(0);
		Phrase apartment = new Phrase();
		apartment.add(new Chunk("Apartment Name :", detailsFont));
		apartment.add(new Chunk(new VerticalPositionMark()));
		apartment.add(new Chunk(buildngName, detailsFont));
		apartmentCell.setPaddingBottom(5.5f);
		apartmentCell.setPaddingTop(0f);
		apartmentCell.addElement(apartment);
		table.addCell(apartmentCell);

		table.getDefaultCell().setColspan(1);
		// String mobNO = bills.getPhone_no();
		String mobNO = "12343";
		table.addCell(new Phrase("Mobile No : " + mobNO, detailsFont));

		PdfPCell flatNoCell = new PdfPCell();
		flatNoCell.setBorder(0);
		Phrase flat = new Phrase();
		// String flatName = bills.getFlat_no();
		flat.add(new Chunk("Flat No :", detailsFont));
		flat.add(new Chunk(new VerticalPositionMark()));
		Long flatNo = 34l;
		// Long flatNo = bills.getHouse_number();
		flat.add(new Chunk(Long.toString(flatNo), detailsFont));
		flatNoCell.setPaddingBottom(5.5f);
		flatNoCell.setPaddingTop(0f);
		flatNoCell.addElement(flat);
		table.addCell(flatNoCell);
		// String email = bills.getEmail();
		String email = "xyz@abc.com";
		table.addCell(new Phrase("Email Id : " + email, detailsFont));

		PdfPCell accountIdCell = new PdfPCell();
		accountIdCell.setBorder(0);
		Phrase p = new Phrase();
		p.add(new Chunk("Account Id :", detailsFont));
		p.add(new Chunk(new VerticalPositionMark()));
		p.add(new Chunk("000242993", detailsFont));
		accountIdCell.setPaddingBottom(5.5f);
		accountIdCell.setPaddingTop(0f);
		accountIdCell.addElement(p);
		table.addCell(accountIdCell);

		table.addCell("");

		PdfPCell meterIdCell = new PdfPCell();
		meterIdCell.setBorder(0);
		meterIdCell.setPaddingBottom(5.5f);
		meterIdCell.setPaddingTop(0f);
		// String meterNo = bills.getMeter_no();
		// table.addCell(new Phrase("Meter No : M1234", detailsFont));
		// createPhrase("Meter No :", meterNo, detailsFont, meterIdCell, table);
		PdfPCell emailCell = new PdfPCell();
		emailCell.setBorder(0);
		Phrase emailCellId = new Phrase();
		emailCellId.add(new Chunk("Meter No :", detailsFont));
		emailCellId.add(new Chunk(new VerticalPositionMark()));
		emailCellId.add(new Chunk("M1234", detailsFont));
		// table.addCell(emailCellId);

		// table.getDefaultCell().setColspan(2);
		// table.addCell("Value b");
		// table.addCell("This is a long description for column c. It needs much more
		// space hence we made sure that the third column is wider.");
		document.add(table);
		// String IMG ="http://15.207.45.79:7080/SM_Meter/images/image_pdf.jpeg";
		// Image image1 = Image.getInstance(IMG);
		// image1.setAlignment(Element.ALIGN_LEFT);
		// image1.setAbsolutePosition(50, 450);
		// image1.scaleAbsolute(200, 200);
		// Add to document
		// document.add(image1);
		
		
		
		
		table = new PdfPTable(2);
		table.getDefaultCell().setBorder(0);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		table.getDefaultCell().setColspan(1);
		table.addCell("");
		Font billingFont = new Font(FontFamily.HELVETICA, 10);
		billingFont.setColor(BaseColor.WHITE);
		// table.addCell(new Phrase("Personal Details", detailsFont));
		PdfPCell billCell = new PdfPCell();

		Phrase billPhrase = new Phrase();
		Chunk billChunk = new Chunk("Billing Summary");
		billChunk.setFont(billingFont);
		billPhrase.add(billChunk);
		billCell.setPhrase(billPhrase);
		billCell.setPaddingBottom(5.5f);
		billCell.setPaddingTop(0f);

		billCell.setBackgroundColor(new BaseColor(11, 128, 198));

		// p.getDefaultCell().setBorder(Rectangle.BOTTOM);
		billCell.setBorder(0);
		// table.getDefaultCell().setBorderColor(BaseColor.BLACK);
		table.addCell(billCell);
		// table.addCell("Bill Summary");
		// table.addCell("To :"+" "+"NCPL ");
		table.addCell("");
		PdfPCell accountIdBillNoCell = new PdfPCell();
		accountIdBillNoCell.setBorder(0);
		// String billNo = bills.getAlternate_email();
		String billNo = "1234";
		createPhrase("Billing No :", billNo, detailsFont, accountIdBillNoCell, table);

		// PdfPCell accountIdBillCell = new PdfPCell();
		// accountIdBillCell.setBorder(0);
		// createPhrase("Account Id :", " 12345", detailsFont, accountIdBillCell,
		// table);
		// table.addCell(new Phrase("Account Id : 12345", detailsFont));
		// table.getDefaultCell().setColspan(1);
		// table.addCell("Value b");
		// table.addCell("This is a long description for column c. It needs much more
		// space hence we made sure that the third column is wider.");
		// table.getDefaultCell().setColspan(1);
		table.addCell("");
		PdfPCell accountIdBillMonthCell = new PdfPCell();
		accountIdBillMonthCell.setBorder(0);

		createPhrase("Billing Month	:", month.toString(), detailsFont, accountIdBillMonthCell, table);
		// table.addCell(new Phrase("Billing Month : September", detailsFont));
		table.addCell("");
		PdfPCell accountIdBillDateCell = new PdfPCell();
		accountIdBillDateCell.setBorder(0);

		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		String billDate = dateFormat.format(date);
		String[] newFormat = billDate.split("/");
		billDate = newFormat[1] + "/" + newFormat[0] + "/" + newFormat[2];
		// String billDate = bills.getDate();

		createPhrase("Billing Date	:", billDate, detailsFont, accountIdBillDateCell, table);

		// table.addCell(new Phrase("Billing Date : 28/09/2020", detailsFont));
		table.addCell("");
		// PdfPCell accountIdBillNoCell = new PdfPCell();
		// accountIdBillNoCell.setBorder(0);
		// createPhrase("Billing No :", "B123234", detailsFont, accountIdBillNoCell,
		// table);
		// table.addCell(new Phrase("Billing No : B123234", detailsFont));
		// table.addCell("");
		/*
		 * PdfPCell prevMnthUsageCell = new PdfPCell(); prevMnthUsageCell.setBorder(0);
		 * float prevReading = bills.getPrv_reading(); String prevMonthReading =
		 * Float.toString(prevReading);
		 * 
		 * createPhrase("Prev Month Reading :",prevMonthReading, detailsFont,
		 * prevMnthUsageCell, table); // table.addCell(new
		 * Phrase("Prev Month Usage : 123", detailsFont)); table.addCell(""); PdfPCell
		 * currMnthUsageCell = new PdfPCell(); currMnthUsageCell.setBorder(0); float
		 * currReading = bills.getReading(); String currMonthReading =
		 * Float.toString(currReading);
		 * createPhrase("Curr Month Reading :",currMonthReading, detailsFont,
		 * currMnthUsageCell, table); table.addCell("");
		 */
		PdfPCell presentUsageCell = new PdfPCell();
		presentUsageCell.setBorder(0);
		float billable = totalMeterUsage;
		String billingValue = Float.toString(billable);
		createPhrase("Billable Reading :", billingValue, detailsFont, presentUsageCell, table);
		// table.addCell(new Phrase("Current Month Usage : B123234", detailsFont));
		// table.getDefaultCell().setColspan(2);
		// table.addCell("Value b");
		// table.addCell("This is a long description for column c. It needs much more
		// space hence we made sure that the third column is wider.");
		document.add(table);
		// document.add(image1);
		// document.add(paragraph);
		// document.add( Chunk.NEWLINE );

		NumberFormat objin = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
		DecimalFormat formatter = new DecimalFormat("\u20B9");

		table = new PdfPTable(2);
		table.getDefaultCell().setBorder(0);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		table.getDefaultCell().setColspan(1);
		table.addCell("");
		// table.addCell(new Phrase("Personal Details", detailsFont));
		Font chargesFont = new Font(FontFamily.HELVETICA, 10);
		chargesFont.setColor(BaseColor.WHITE);
		PdfPCell chargesCell = new PdfPCell();
		Phrase chargePhrase = new Phrase();
		Chunk chargeChunk = new Chunk("Charges");
		chargeChunk.setFont(chargesFont);
		chargePhrase.add(chargeChunk);
		chargesCell.setPhrase(chargePhrase);
		// p.getDefaultCell().setBorder(Rectangle.BOTTOM);
		chargesCell.setPaddingBottom(5.5f);
		chargesCell.setPaddingTop(0f);
		chargesCell.setBackgroundColor(new BaseColor(11, 128, 198));
		chargesCell.setBorder(0);
		// table.getDefaultCell().setBorderColor(BaseColor.BLACK);
		table.addCell(chargesCell);
		// table.addCell("Charges");
		// table.addCell("To :"+" "+"NCPL ");
		table.addCell("");
		PdfPCell totlCostCell = new PdfPCell();
		totlCostCell.setBorder(0);
		PdfPCell unitCell = new PdfPCell();
		unitCell.setBorder(0);
		Phrase unitPhrase = new Phrase();
		Chunk unitChunk = new Chunk("Unit price Rs 8/unit");
		Font unitFont = new Font(FontFamily.HELVETICA, 10);
		unitFont.setColor(BaseColor.BLACK);
		unitChunk.setFont(unitFont);
		unitPhrase.add(unitChunk);
		unitCell.setPhrase(unitPhrase);
		table.addCell(unitCell);
		table.addCell("");
		float total = billable * 8;
		String totalValue = formatter.format(0);
		totalValue = totalValue.replace("0", "");
		String totalAmount = objin.format(total);
		totalAmount = totalAmount.replace("Rs.", "");
		totalAmount = totalValue + totalAmount;
		createPhrase("Total :", totalAmount, detailsFont, totlCostCell, table);

		// table.addCell(new Phrase("Total :1000", detailsFont));
		// table.getDefaultCell().setColspan(1);
		// table.addCell("Value b");
		// table.addCell("This is a long description for column c. It needs much more
		// space hence we made sure that the third column is wider.");
		// table.getDefaultCell().setColspan(1);
		table.addCell("");

		PdfPCell gstCell = new PdfPCell();
		gstCell.setBorder(0);
		float gst = (float) (total * (0.18));
		String gstAmount = objin.format(gst);
		gstAmount = gstAmount.replace("Rs.", "");
		gstAmount = totalValue + gstAmount;

		createPhrase("GST (18%):", gstAmount, detailsFont, gstCell, table);
		// table.addCell(new Phrase("GST : 180", detailsFont));
		table.addCell("");
		PdfPCell grandtotalCell = new PdfPCell();
		float p1 = grandtotalCell.getEffectivePaddingBottom();
		float p2 = grandtotalCell.getEffectivePaddingTop();

		grandtotalCell.setPaddingBottom(5.5f);
		grandtotalCell.setPaddingTop(0f);
		System.out.println("p1" + p1 + "p2:" + p2);
		grandtotalCell.setBackgroundColor(new BaseColor(11, 128, 198));
		grandtotalCell.setBorder(0);
		Font grandTotalFont = new Font(FontFamily.HELVETICA, 10);
		grandTotalFont.setColor(BaseColor.WHITE);
		Phrase gtPhrase = new Phrase();
		Chunk gc1 = new Chunk("Grand Total :");
		gc1.setFont(grandTotalFont);
		Chunk gc2 = new Chunk(new VerticalPositionMark());
		float grandTotal = gst + total;
		String grandTotalAmount = objin.format(grandTotal);
		grandTotalAmount = grandTotalAmount.replace("Rs.", "");
		grandTotalAmount = totalValue + grandTotalAmount;

		Chunk gc3 = new Chunk(grandTotalAmount);
		gc3.setFont(grandTotalFont);
		gtPhrase.add(gc1);
		gtPhrase.add(gc2);
		gtPhrase.add(gc3);
		grandtotalCell.setPhrase(gtPhrase);
		table.addCell(grandtotalCell);
		// createPhrase("Grand Total :", "1180", grandTotalFont, grandtotalCell, table);
		// table.addCell(new Phrase("Grand Total : 1180", detailsFont));
		// table.getDefaultCell().setColspan(2);
		// table.addCell("Value b");
		// table.addCell("This is a long description for column c. It needs much more
		// space hence we made sure that the third column is wider.");
		document.add(table);
		// document.add(image1);
		document.add(new Paragraph("\n\n"));
		document.add(new Paragraph("\n\n"));

		// Checking Code
		PdfPTable tableTest = new PdfPTable(4);

		tableTest.setWidthPercentage(90);
		tableTest.setSpacingBefore(0f);
		tableTest.setSpacingAfter(0f);

		// first row
		PdfPCell cell = new PdfPCell(new Phrase("Meter Readings"));
		cell.setColspan(10);
		cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		cell.setPadding(5.0f);
		cell.setBackgroundColor(new BaseColor(11, 128, 198));
		tableTest.addCell(cell);

		tableTest.addCell("Meter No");
		tableTest.addCell("Previous Reading(L)");
		tableTest.addCell("Present Reading(L)");
		tableTest.addCell("Usage(L)");
//		for (String mapkey : meterReadingMap.keySet()) {
//
//			Map readingDeatils = meterReadingMap.get(mapkey);
//			String meterId = (String) readingDeatils.get("meterId");
//			String meterprevReading = (String) readingDeatils.get("previousReading");
//			String meterpresReading = (String) readingDeatils.get("presentReading");
//			String meterUsage = (String) readingDeatils.get("usage");
//
//			tableTest.addCell(meterId);
//			tableTest.addCell(meterprevReading);
//			tableTest.addCell(meterpresReading);
//			tableTest.addCell(meterUsage);
//		}

		document.add(tableTest);

		document.add(new Paragraph("\n\n"));
		PdfPTable tncTable = new PdfPTable(1);
		tncTable.getDefaultCell().setBorder(0);
		tncTable.setWidthPercentage(100);
		tncTable.getDefaultCell().setColspan(1);

		PdfPCell tnc = new PdfPCell();
		tnc.setBorder(0);
		Phrase tncPhrase = new Phrase();
		Font termsFont = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
		Chunk chunkTerms = new Chunk("Commercial Terms and Conditions", termsFont);
		tncPhrase.add(chunkTerms);
		tnc.setPhrase(tncPhrase);
		tncTable.addCell(tnc);
		tncTable.addCell("");
		PdfPCell detailcell = new PdfPCell();
		detailcell.setBorder(0);
		Font tncdetailFont = new Font(FontFamily.HELVETICA, 10);
		Chunk chunkDetailTerms = new Chunk("1. Please Pay  the bill Before Due Date", tncdetailFont);
		detailcell.addElement(chunkDetailTerms);
		tncTable.addCell(detailcell);
		tncTable.addCell("");
		PdfPCell detailcell2 = new PdfPCell();
		detailcell2.setBorder(0);
		Font tncdetail2Font = new Font(FontFamily.HELVETICA, 10);
		Chunk chunkDetail2Terms = new Chunk("2. Consumer can pay monthly bills from 1st to 25th of every month.",
				tncdetail2Font);
		detailcell2.addElement(chunkDetail2Terms);
		tncTable.addCell(detailcell2);
		tncTable.addCell("");

		PdfPCell detailcell3 = new PdfPCell();
		detailcell3.setBorder(0);
		Font tncdetail3Font = new Font(FontFamily.HELVETICA, 10);
		Chunk chunkDetail3Terms = new Chunk(
				"3. Disconnection will be enforced after the 20th for belated consumers who stay unpaid.",
				tncdetail3Font);
		detailcell3.addElement(chunkDetail3Terms);
		tncTable.addCell(detailcell3);
		tncTable.addCell("");

		PdfPCell detailcell4 = new PdfPCell();
		detailcell4.setBorder(0);
		Font tncdetail4Font = new Font(FontFamily.HELVETICA, 10);
		Chunk chunkDetail4Terms = new Chunk(
				"4. For Disconnected consumers, option for Reconnection will be open only up to 6 months of non-payment.",
				tncdetail4Font);
		detailcell4.addElement(chunkDetail4Terms);
		tncTable.addCell(detailcell4);
		tncTable.addCell("");

		PdfPCell detailcell5 = new PdfPCell();
		detailcell5.setBorder(0);
		Font tncdetail5Font = new Font(FontFamily.HELVETICA, 10);
		Chunk chunkDetail5Terms = new Chunk(
				"5. After 6 months of non-payment permanent disconnection will be forced on the consumer and his name will be de-listed from the consumer list.",
				tncdetail5Font);
		detailcell5.addElement(chunkDetail5Terms);
		tncTable.addCell(detailcell5);
		tncTable.addCell("");

		document.add(tncTable);
		float rs = 1000;
		// DecimalFormat formatter = new DecimalFormat("\u20B9 000");

		System.out.println("hiiiiiiii" + formatter.format(rs));
		// BaseFont bf = BaseFont.createFont("c:/windows/fonts/arialuni.ttf",
		// BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		Font font = new Font(FontFamily.HELVETICA, 10);
		Chunk chunkRupee = new Chunk(" \u20A8 5410", font);
		System.out.println(chunkRupee);
		document.add(Chunk.NEWLINE);

		Double amount = 123459.0;
		// amount is an example ,can be used with any double value

		DecimalFormat IndianCurrencyFormat = new DecimalFormat("##,##,###.00");

		String formattedAmount = IndianCurrencyFormat.format(amount);
		System.out.println("formattd" + formattedAmount);

		Locale indian = new Locale("en", "IN");
		NumberFormat indianFormat = NumberFormat.getCurrencyInstance(indian);
		System.out.println("Indian: " + indianFormat.format(2289748));

		Paragraph paragraph1 = new Paragraph();
		paragraph1.add(chunkRupee);
		// document.add(paragraph1);
		// paragraph1.setAlignment(Element.ALIGN_RIGHT);
		Font bold = new Font(FontFamily.HELVETICA, 12, Font.BOLD);

		// Set paragraph's left side indent
		paragraph1.setIndentationLeft(370);

		// paragraph1.setFirstLineIndent(150);
		Chunk c = new Chunk("Unit price Rs 8/mtrs \n", bold);
		// Chunk c1 = new Chunk("Total Amount :"+"45,464.87 \n");
		Chunk c2 = new Chunk("Gst :" + "1464.87 \n");
		Chunk c3 = new Chunk("Grand Total :" + "49,464.87 \n");
		paragraph1.add(c);
		// paragraph1.add(c1);
		paragraph1.add(c2);
		paragraph1.add(c3);
		// document.add(paragraph1);

		/*
		 * //Checking Code PdfPTable tableTest = new PdfPTable(4);
		 * 
		 * tableTest.setWidthPercentage(75); tableTest.setSpacingBefore(0f);
		 * tableTest.setSpacingAfter(0f);
		 * 
		 * // first row PdfPCell cell = new PdfPCell(new Phrase("Meter Readings"));
		 * cell.setColspan(10); cell.setHorizontalAlignment(Element.ALIGN_CENTER);
		 * cell.setPadding(5.0f); cell.setBackgroundColor(new BaseColor(11, 128, 198));
		 * tableTest.addCell(cell);
		 * 
		 * tableTest.addCell("Meter No"); tableTest.addCell("Previous Reading");
		 * tableTest.addCell("Present Reading"); tableTest.addCell("Usage"); for (String
		 * mapkey : meterReadingMap.keySet()) {
		 * 
		 * Map readingDeatils = meterReadingMap.get(mapkey); String meterId = (String)
		 * readingDeatils.get("meterId"); String meterprevReading = (String)
		 * readingDeatils.get("previousReading"); String meterpresReading = (String)
		 * readingDeatils.get("presentReading"); String meterUsage = (String)
		 * readingDeatils.get("usage");
		 * 
		 * 
		 * tableTest.addCell(meterId); tableTest.addCell(meterprevReading);
		 * tableTest.addCell(meterpresReading); tableTest.addCell(meterUsage); }
		 */
		/*
		 * for (int i = 0; i < 5; i++) { tableTest.addCell("Meter" + i);
		 * tableTest.addCell("Previous" + i); tableTest.addCell("Present" + i);
		 * tableTest.addCell("Usage" + i);
		 * 
		 * }
		 */
		// document.add(tableTest);

		document.close();
		return path;

	}

	private void createPhrase(String string, String string2, Font detailsFont, PdfPCell Cell, PdfPTable table) {
		Phrase p = new Phrase();
		p.add(new Chunk(string, detailsFont));
		p.add(new Chunk(new VerticalPositionMark()));
		p.add(new Chunk(string2, detailsFont));
		Cell.addElement(p);
		table.addCell(Cell);
	}

	public void onStartPage(PdfWriter writer, Document document) {
		try {
			addHeader(writer);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
		// new Phrase("Teknika Infotech PVT Ltd"), 130, 800,0);
		// ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
		// new Phrase(), 550, 800, 0);
	}

	public void onEndPage(PdfWriter writer, Document document) {
		// addFooter(writer);
		ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, new Phrase("https://www.ncpl.co/"),
				110, 30, 0);
		ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
				new Phrase("page " + document.getPageNumber()), 550, 30, 0);
	}

	private void addHeader(PdfWriter writer) throws MalformedURLException, IOException {
		PdfPTable header = new PdfPTable(2);
		try {
			// set defaults
			header.setWidths(new int[] { 3, 28 });
			header.setTotalWidth(600);
			header.setLockedWidth(true);
			header.getDefaultCell().setFixedHeight(60);
			header.getDefaultCell().setBorder(Rectangle.BOTTOM);
			header.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

			// add image

			// String IMG = "/WebContent/WEB-INF/images/ncpl_logo.png";
			String IMG = "http://15.207.45.79:7080/SM_Meter/images/smart_meter_logo2.png";
			/*
			 * ======= String IMG ="http://localhost:7080/SM_Design/images/ncpl_logo.png";
			 * // String IMG ="http://15.207.45.79:7080/SM_Meter/images/ncpl_logo.png";
			 * >>>>>>> Stashed changes
			 */
			Image image = Image.getInstance(IMG);

			// Image logo =
			// Image.getInstance(BillPdf.class.getResource("/memorynotfound-logo.jpg"));
			header.addCell(image);

			// add text
			PdfPCell text = new PdfPCell();
			text.setPaddingBottom(15);
			text.setPaddingLeft(10);
			text.setBorder(Rectangle.BOTTOM);
			text.setBorderColor(BaseColor.LIGHT_GRAY);
			text.addElement(new Phrase("Water Bill Invoice"));
			// text.addElement(new Phrase(building));

			header.addCell(text);

			// write content
			header.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		}
	}

	@SuppressWarnings("unused")
	private void addFooter(PdfWriter writer) {
		PdfPTable footer = new PdfPTable(3);
		Font detailsFont = new Font(FontFamily.HELVETICA, 9);
		try {
			// set defaults
			footer.setWidths(new int[] { 18, 2, 1 });
			footer.setTotalWidth(527);
			footer.setLockedWidth(true);
			footer.getDefaultCell().setFixedHeight(40);
			footer.getDefaultCell().setBorder(Rectangle.TOP);
			footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

			// add copyright
			footer.addCell(new Phrase("\u00A9 Neptune Controls Pvt Ltd", detailsFont));
			int s = writer.getCurrentPageNumber();
			System.out.println(s);
			int s1 = writer.getPageNumber();
			System.out.println(s1);
			// add current page count
			footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
			footer.addCell(new Phrase(
					String.format("Page %d of " + writer.getPageNumber(), writer.getCurrentPageNumber()), detailsFont));

			// add placeholder for total page count
			PdfPCell totalPageCount = new PdfPCell();
			totalPageCount.setBorder(Rectangle.TOP);
			totalPageCount.setBorderColor(BaseColor.LIGHT_GRAY);
			footer.addCell(totalPageCount);

			// write page
			PdfContentByte canvas = writer.getDirectContent();
			canvas.beginMarkedContentSequence(PdfName.ART);
			footer.writeSelectedRows(0, -1, 34, 50, canvas);
			// canvas.endMarkedContentSequence();
		} catch (DocumentException de) {
			throw new ExceptionConverter(de);
		}
	}

	@SuppressWarnings("unused")
	public String invoiceFunction(Invoice invoice, SalesOrder salesOrder,
			String dcNo, DeliveryChallan deliveryChallanObj,HttpServletRequest request, ArrayList<DeliveryChallanItems> dcItemsListForAll, List<DeliveryChallanItems> newItems) throws DocumentException, MalformedURLException, IOException {
		// TODO Auto-generated method stub
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String company = "Neptune Controls Pvt Ltd."+"\n"+ "No.8/2(Old No.114), 2nd Cross 7th Main Road, Nandidurga Extension"+
				"\n"+"Banglaore- 560046"+"\n"+"GSTIN: 29AADCN5426F1ZG"+"\n"+"State Name : Karnataka, Code : 29"+"\n"+"CIN: U31200KA2011PTC056705"+
				"\n"+"Contact No.:7624964492"+"\n"+"E-Mail : accounts@ncpl.co";

		Date date = new Date();
		// writePdf(outputStream);
		String fileName = "billByMonth" + "1" + date.getTime() + ".pdf";
		String path = createInvoicePDF(fileName, invoice, salesOrder, dcNo, company, date,deliveryChallanObj,request,dcItemsListForAll,newItems);
		return path;
	}

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	private String createInvoicePDF(String fileName, Invoice invObject, SalesOrder salesOrder, String dcNo, String company, Date date, DeliveryChallan deliveryChallanObj,HttpServletRequest request, ArrayList<DeliveryChallanItems> dcItemsListForAll, List<DeliveryChallanItems> newItems) throws DocumentException, MalformedURLException, IOException {
		// TODO Auto-generated method stub
		
		Date d = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String invDate = formatter.format(d);
		Document doc = new Document(PageSize.A4, 50, 30, 25, 10);
		String billingAddress = "";
		String shippingAddress = "";
		String shippingAddr2 = "";
		String shippingPin = "";
		String shippingPan = "";
		String billingAddr2 = "";
		String billingPin = "";
		String billingPan = "";
		String billingGst = "";
		Party shipAddress = (Party) request.getAttribute("shipAddress");
		if (shipAddress != null) {
			if (shipAddress.getAddr2() == null || shipAddress.getAddr2().equalsIgnoreCase("")) {
				shippingAddr2 = "";
			} else {
				shippingAddr2 = shipAddress.getAddr2() + "\n";
			}

			if (shipAddress.getParty_city().getAreaCode() == null || shipAddress.getParty_city().getAreaCode() == "") {
				shippingPin = "" + "\n";
			} else {
				shippingPin = shipAddress.getParty_city().getAreaCode() + "\n";
			}

			// if(shipAddress.getGst() == null || shipAddress.getGst() == ""){
			// shippingGst = "GSTIN/UIN"+""+"\n";
			// }else{
			// shippingGst = "GSTIN/UIN"+shipAddress.getGst()+"\n";
			// }
			//
			if (shipAddress.getPan() == null || shipAddress.getPan() == "") {
				shippingPan = "PAN" + "";
			} else {
				shippingPan = "PAN" + shipAddress.getPan();
			}

			shippingAddress = "Ship to"+ "\n" +shipAddress.getPartyName() + "\n" + shipAddress.getAddr1() + "\n" + shippingAddr2
					+ shipAddress.getParty_city().getName() + "-" + shippingPin;
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
	        
	       // if(partyShippAddress.getGst() == null || partyShippAddress.getGst() == ""){
	        //	shippingGst = "GSTIN "+""+"\n";
	       // }else{
	        //	shippingGst = "GSTIN "+partyShippAddress.getGst()+"\n";
	        //}
	        
	        shippingAddress = "Ship to"+ "\n" + partyShippAddress.getPartyName()+"\n"+partyShippAddress.getAddr1()+"\n"+
	        		shippingAddr2 + partyShippAddress.getPartyaddr_city().getName()+"-"+
	        		shippingPin;
	        
        }
		Party billAddress = (Party) request.getAttribute("billAddress");
		String stateName;
		if (billAddress != null) {
			 stateName = billAddress.getParty_city().getState().getName();
			if (billAddress.getAddr2() == null || billAddress.getAddr2().equalsIgnoreCase("")) {
				billingAddr2 = "";
			} else {
				billingAddr2 = billAddress.getAddr2() + "\n";
			}

			if (billAddress.getParty_city().getAreaCode() == null || billAddress.getParty_city().getAreaCode() == "") {
				billingPin = "" + "\n";
			} else {
				billingPin = billAddress.getParty_city().getAreaCode() + "\n";
			}

			if (billAddress.getGst() == null || billAddress.getGst() == "") {
				billingGst = "GSTIN " + "" + "\n";
			} else {
				billingGst = "GSTIN " + billAddress.getGst() + "\n";
			}

			if (billAddress.getPan() == null || billAddress.getPan() == "") {
				billingPan = "PAN" + "";
			} else {
				billingPan = "PAN" + billAddress.getPan() + "\n";
			}

			billingAddress ="Bill to"+"\n"+ billAddress.getPartyName() + "\n" + billAddress.getAddr1() + "\n" + billingAddr2
					+ billAddress.getParty_city().getName() + "-" + billingPin + billingGst;
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
	        
	        billingAddress ="Bill to"+"\n"+partyBillAddress.getPartyName()+"\n"+partyBillAddress.getAddr1()+"\n"+
	        		billingAddr2 + partyBillAddress.getPartyaddr_city().getName()+"-"+
	        		billingPin+ billingGst;
	        

        }

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
		Paragraph paragraph = new Paragraph();
		paragraph.setAlignment(Element.ALIGN_CENTER);
		// paragraph.add(new Chunk("Neptune Controls Pvt Ltd"));
		paragraph.add("  Neptune Controls Pvt Ltd");
		// paragraph.add(new Chunk("To :" + "NCPL \n"));
		// paragraph.add(new Chunk(" Email Id :" + "owner@ncpl.co \n"));

		
			String imgLoc = "http://localhost:8080/ncpl-sales/resources/dist/img/ncpl_logo.png";
			  Image img = Image.getInstance(imgLoc);
      img.setAbsolutePosition(50f, 767f);
      document.add(img);
		
		
		PdfPTable table = new PdfPTable(2);
		table.setSpacingBefore(5);
		table.setWidths(new int[] {7, 7 });
		table.setWidthPercentage(100);
		table.addCell("");
		//table.addCell(paragraph);
		table.addCell(
				"Complete solution for BMS, Lighting Control, CCTV & Security Systems, DDC Panels, Automation Panels, Lighting,panels, "
						+ "MCC & Starter Panels");
		document.add(table);

		table = new PdfPTable(1);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		Font boldFont = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
		PdfPCell headerCell = new PdfPCell();
		Phrase headerPhrase = new Phrase();
		Chunk headerChunk = new Chunk("TAX INVOICE");
		headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		headerChunk.setFont(boldFont);
		headerPhrase.add(headerChunk);
		headerCell.setPhrase(headerPhrase);
		table.addCell(headerCell);
		document.add(table);

		table = new PdfPTable(2);
		table.setSpacingBefore(5);
		table.setWidths(new int[] { 4, 4 });
		table.setWidthPercentage(100);
		PdfPCell cell1 = new PdfPCell(new Paragraph(company));
		PdfPCell cell2 = new PdfPCell(new Paragraph("Cell 2"));

		Paragraph paragraphCompany = new Paragraph();
		// paragraphCompany.setAlignment(Element.ALIGN_CENTER);
		// paragraph.add(new Chunk("Neptune Controls Pvt Ltd"));
		// paragraphCompany.add(company);
		table.addCell(cell1);

		PdfPTable nestedTable = new PdfPTable(1);
		nestedTable.getDefaultCell().setBorder(0);
		nestedTable.setWidthPercentage(103);
		PdfPTable invoiceTbl = new PdfPTable(1);
		PdfPCell invoiceCell = new PdfPCell();
		invoiceTbl.setWidthPercentage(100);
		//invoiceCell.setFixedHeight(10f);
		invoiceCell.setBorder(0);
		Phrase invoiceNumner = new Phrase();
		invoiceNumner.add(new Chunk("Invoice No: ", detailsFont));
		// invoiceNumner.add(new Chunk(new VerticalPositionMark()));
		invoiceNumner.add(new Chunk(invObject.getInvoiceId(), detailsFont));
		invoiceCell.setPaddingBottom(0f);
		invoiceCell.setPaddingTop(0f);
		invoiceCell.addElement(invoiceNumner);
		invoiceTbl.addCell(invoiceCell);

		PdfPCell invoiceDateCell = new PdfPCell();
		invoiceDateCell.setBorder(0);
		Phrase invoiceNumnerDate = new Phrase();
		invoiceNumnerDate.add(new Chunk("Invoice Date: ", detailsFont));
		// invoiceNumnerDate.add(new Chunk(new VerticalPositionMark()));
		invoiceNumnerDate.add(new Chunk(invDate, detailsFont));
		invoiceDateCell.setPaddingBottom(0f);
		invoiceDateCell.setPaddingTop(0f);
		invoiceDateCell.addElement(invoiceNumnerDate);
		invoiceTbl.addCell(invoiceDateCell);
		nestedTable.addCell(invoiceTbl);
		
		PdfPTable dcTabl = new PdfPTable(1);
		dcTabl.setWidthPercentage(100);
		PdfPCell deliverDateCell = new PdfPCell();
		deliverDateCell.setBorder(1);
		Phrase deliverDate = new Phrase();
		deliverDate.add(new Chunk("Deliver Date: ", detailsFont));
		// deliverDate.add(new Chunk(new VerticalPositionMark()));
		if(dcNo!="") {
		deliverDate.add(new Chunk(invDate, detailsFont));
		}else {
		deliverDate.add(new Chunk("", detailsFont));
		}
		deliverDateCell.setPaddingBottom(0f);
		deliverDateCell.setPaddingTop(0f);
		deliverDateCell.addElement(deliverDate);
		dcTabl.addCell(deliverDateCell);
		
		PdfPCell deliverNoCell = new PdfPCell();
		deliverNoCell.setBorder(0);
		Phrase deliverNo = new Phrase();
		deliverNo.add(new Chunk("Deliver No:", detailsFont));
		// deliverNo.add(new Chunk(new VerticalPositionMark()));
		if(dcNo!="") {
		deliverNo.add(new Chunk(dcNo, detailsFont));
		}else {
		deliverNo.add(new Chunk("", detailsFont));
		}
		deliverNoCell.setPaddingBottom(0f);
		deliverNoCell.setPaddingTop(0f);
		deliverNoCell.addElement(deliverNo);
		dcTabl.addCell(deliverNoCell);
		nestedTable.addCell(dcTabl);
		
		
		PdfPTable clientPoTabl = new PdfPTable(1);
		clientPoTabl.setWidthPercentage(100);
		PdfPCell clientPoNoCell = new PdfPCell();
		clientPoNoCell.setBorder(1);
		Phrase clientNo = new Phrase();
		clientNo.add(new Chunk("Client Po No: ", detailsFont));
		// deliverNo.add(new Chunk(new VerticalPositionMark()));
		clientNo.add(new Chunk(salesOrder.getClientPoNumber(), detailsFont));
		clientPoNoCell.setPaddingBottom(0f);
		clientPoNoCell.setPaddingTop(0f);
		clientPoNoCell.addElement(clientNo);
		clientPoTabl.addCell(clientPoNoCell);
		nestedTable.addCell(clientPoTabl);

		PdfPTable clientPoDateTabl = new PdfPTable(1);
		clientPoDateTabl.setWidthPercentage(100);
		PdfPCell clientPoDateCell = new PdfPCell();
		clientPoDateCell.setBorder(1);
		Phrase clientPoDateNo = new Phrase();
		clientPoDateNo.add(new Chunk("Client po Date: ", detailsFont));
		// deliverNo.add(new Chunk(new VerticalPositionMark()));
		clientPoDateNo.add(new Chunk( formatter.format(salesOrder.getClientPoDate()).toString(), detailsFont));
		clientPoDateCell.setPaddingBottom(0f);
		clientPoDateCell.setPaddingTop(0f);
		clientPoDateCell.addElement(clientPoDateNo);
		clientPoDateTabl.addCell(clientPoDateCell);
		nestedTable.addCell(clientPoDateTabl);

		//nestedTable.addCell(invoiceNumner);
		//nestedTable.addCell(invoiceDateCell);
		//nestedTable.addCell(deliverDateCell);
		//nestedTable.addCell(deliverNoCell);
		//nestedTable.addCell(clientPoNoCell);
		//nestedTable.addCell(clientPoDateCell);
		cell2.addElement(nestedTable);
		table.addCell(cell2);

		document.add(table);

		table = new PdfPTable(2);
		table.setSpacingBefore(5);
		table.setWidths(new int[] { 4, 4 });
		table.setWidthPercentage(100);
		PdfPCell billAddress1 = new PdfPCell(new Paragraph(billingAddress));
		PdfPCell shippingAddress1 = new PdfPCell(new Paragraph(shippingAddress));
		table.addCell(billAddress1);
		table.addCell(shippingAddress1);
		document.add(table);

		table = new PdfPTable(7);
		table.setSpacingBefore(5);
		table.setWidths(new int[] {1,3,2,1,2,2,2});
		table.setWidthPercentage(100);
		table.addCell("Sl No"); 
		table.addCell("Particulars"); 
		table.addCell("HSN/SAC Code"); 
		table.addCell("Qty"); 
		table.addCell("Supply Rate/Unit"); 
		table.addCell("Installation Rate/Unit"); 
		table.addCell("Taxable Amount"); 
		document.add(table);
		
		double invTotal = invObject.getTotal();
		double grandTotalInv = invObject.getGrandTotal();
		double gst = invTotal*(invObject.getGstRate()/100);
		List<InvoiceItem> invoiceItems = invObject.getItems();
		SalesOrder salesObj = salesOrder;
        List<SalesItem> salesItems = salesObj.getItems();
        List<SalesItem> salesItemsReq = new ArrayList();
		
        DeliveryChallan dcObj = deliveryChallanObj;
        
        List<DeliveryChallanItems>  dcItemsList =  new ArrayList();
        //This is to differentiate the items of with dc and without
		  if(dcNo.equalsIgnoreCase("all") || dcNo.equalsIgnoreCase("")) {
			  for (SalesItem salesItem : salesItems) { 
				  salesItemsReq.add(salesItem);
				  } 
			  }else if (dcNo.contains(",")) {
				  for (SalesItem salesItem : salesItems) { 
					  dcItemsList=newItems;
					  for (DeliveryChallanItems deliveryChallanItems : dcItemsList) {
						  if(deliveryChallanItems.getDescription().equalsIgnoreCase(salesItem.getId())){
							  salesItemsReq.add(salesItem);
						  }
				  }
					
				}
				
			}  
		  else {
			  for (SalesItem salesItem : salesItems) { 
				  dcItemsList = dcObj.getItems();
				  for (DeliveryChallanItems deliveryChallanItems : dcItemsList) {
					  if(deliveryChallanItems.getDescription().equalsIgnoreCase(salesItem.getId())){
						  salesItemsReq.add(salesItem);
					  }
				}
			  
			  }
		  }
		
		float totalAmount = 0;
		float gstAmount = 0;
		float grandTotal = 0;
		float sgstAmount =0;
		float cgstAmount =0;
		float roundoff =0;
		int slNumber =1;

		for (SalesItem salesItem : salesItemsReq) {
			
			float quantity = 0;
			// This is to set dc quantity if invoice selected as supply and dc no..
			if (dcItemsList.size() > 0) {
				for (DeliveryChallanItems dcItem : dcItemsList) {
					if (dcItem.getDescription().equalsIgnoreCase(salesItem.getId())) {
						quantity = dcItem.getTodaysQty();
					}
				}
			} else {
				quantity = salesItem.getQuantity();
			}
			if(quantity!=0) {
			table = new PdfPTable(7);
			table.setSpacingBefore(5);
			table.setWidths(new int[] { 1, 3, 2, 1, 2, 2, 2 });
			table.setWidthPercentage(100);
			table.addCell(salesItem.getSlNo());
			table.addCell(salesItem.getDescription());
			table.addCell(salesItem.getHsnCode());
			table.addCell(Float.toString(quantity));
			if(invObject.getType().equalsIgnoreCase("Supply")){
				table.addCell(Float.toString(salesItem.getUnitPrice()));
				table.addCell(Float.toString((float) 0.0));
				double taxableValue =salesItem.getUnitPrice()*quantity;
				taxableValue = Math.round(taxableValue * 100.0) / 100.0;
				table.addCell(Double.toString(taxableValue));
			}else {
				table.addCell(Float.toString((float) 0.0));
				table.addCell(Float.toString(salesItem.getServicePrice()));
				double taxableValue =salesItem.getServicePrice()*quantity;
				taxableValue = Math.round(taxableValue * 100.0) / 100.0;
				table.addCell(Double.toString(taxableValue));
			}
			
			
			document.add(table);
		}
		}
		
		table= new PdfPTable(1);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		PdfPCell emptyCell = new PdfPCell();
		emptyCell.setBorder(0);
		
		PdfPTable Totaltable = new PdfPTable(2);
		Totaltable.setWidths(new int[] { 7,2 });
		//Totaltable.setSpacingBefore(5);
		Totaltable.addCell(emptyCell);
		PdfPCell totalCell = new PdfPCell();
		totalCell.setBorder(0);
		Phrase totalPhrase= new Phrase();
		totalPhrase.add(new Chunk("				Total:", detailsFont));
		totalPhrase.add(new Chunk(new VerticalPositionMark()));
		totalPhrase.add(new Chunk(Double.toString(invObject.getTotal()), detailsFont));
		totalCell.setPaddingBottom(0f);
		totalCell.setPaddingTop(0f);
		totalCell.addElement(totalPhrase);
		
		Totaltable.addCell(totalCell);
		table.addCell(Totaltable);
		//document.add(table);
//		table = new PdfPTable(2);
//		table.setSpacingBefore(5);
//		table.addCell(emptyCell);
//		PdfPCell grandtotalCell = new PdfPCell();
//		grandtotalCell.setBorder(0);
//		Phrase grandtotalPhrase= new Phrase();
//		grandtotalPhrase.add(new Chunk("Grand Total:", detailsFont));
//		// deliverNo.add(new Chunk(new VerticalPositionMark()));
//		grandtotalPhrase.add(new Chunk(Double.toString(invObject.getGrandTotal()), detailsFont));
//		grandtotalCell.setPaddingBottom(0f);
//		grandtotalCell.setPaddingTop(0f);
//		grandtotalCell.addElement(grandtotalPhrase);
//		
//		table.addCell(grandtotalCell);
//		document.add(table);
		
	//	String stateName = billAddress.getParty_city().getState().getName();
		if(stateName.equalsIgnoreCase("karnataka")) {
			PdfPTable gstTable = new PdfPTable(2);
			gstTable.setWidths(new int[] { 7,2 });
			//gstTable.setSpacingBefore(5);
			gstTable.addCell(emptyCell);
			PdfPCell gstCell = new PdfPCell();
			gstCell.setBorder(0);
			Phrase gsttotalPhrase= new Phrase();
			gsttotalPhrase.add(new Chunk("			Gst@"+invObject.getGstRate()+"%", detailsFont));
			gsttotalPhrase.add(new Chunk(new VerticalPositionMark()));
			gsttotalPhrase.add(new Chunk(Double.toString( Math.round(gst)), detailsFont));
			gstCell.setPaddingBottom(0f);
			gstCell.setPaddingTop(0f);
			gstCell.addElement(gsttotalPhrase);
			gstTable.addCell(gstCell);
			table.addCell(gstTable);
			//document.add(table);
			}else {
				PdfPTable igstTable = new PdfPTable(2);
				igstTable.setWidths(new int[] { 7,2 });
				//igstTable.setSpacingBefore(5);
				igstTable.addCell(emptyCell);
				PdfPCell igstCell = new PdfPCell();
				igstCell.setBorder(0);
				Phrase igstPhrase= new Phrase();
				igstPhrase.add(new Chunk("			IGst@"+invObject.getGstRate()+"%", detailsFont));
			    igstPhrase.add(new Chunk(new VerticalPositionMark()));
				igstPhrase.add(new Chunk(Double.toString( Math.round(gst)), detailsFont));
				igstCell.setPaddingBottom(0f);
				igstCell.setPaddingTop(0f);
				igstCell.addElement(igstPhrase);
				igstTable.addCell(igstCell);
				table.addCell(igstTable);
				//document.add(table);
			}
		
		if(stateName.equalsIgnoreCase("karnataka")) {
			PdfPTable cgstTable = new PdfPTable(2);
			cgstTable.setWidths(new int[] { 7,2 });
			//cgstTable.setSpacingBefore(5);
			cgstTable.addCell(emptyCell);
			PdfPCell gstCell = new PdfPCell();
			gstCell.setBorder(0);
			Phrase gsttotalPhrase= new Phrase();
			gsttotalPhrase.add(new Chunk("			CGst@"+(invObject.getGstRate()/2)+"%", detailsFont));
			gsttotalPhrase.add(new Chunk(new VerticalPositionMark()));
			gsttotalPhrase.add(new Chunk(Double.toString(Math.round(gst)/2), detailsFont));
			gstCell.setPaddingBottom(0f);
			gstCell.setPaddingTop(0f);
			gstCell.addElement(gsttotalPhrase);
			cgstTable.addCell(gstCell);
			table.addCell(cgstTable);
			//document.add(table);
			
			PdfPTable sgstTable = new PdfPTable(2);
			sgstTable.setWidths(new int[] { 7,2 });
			//sgstTable.setSpacingBefore(5);
			sgstTable.addCell(emptyCell);
			PdfPCell cgstCell = new PdfPCell();
			cgstCell.setBorder(0);
			Phrase cgsttotalPhrase= new Phrase();
			cgsttotalPhrase.add(new Chunk("			SGst@"+(invObject.getGstRate()/2)+"%", detailsFont));
			cgsttotalPhrase.add(new Chunk(new VerticalPositionMark()));
			cgsttotalPhrase.add(new Chunk(Double.toString(Math.round(gst)/2), detailsFont));
			cgstCell.setPaddingBottom(0f);
			cgstCell.setPaddingTop(0f);
			cgstCell.addElement(cgsttotalPhrase);
			sgstTable.addCell(cgstCell);
			table.addCell(sgstTable);
			//document.add(table);
		}
		
		PdfPTable grandTotalTable = new PdfPTable(2);
		grandTotalTable.setWidths(new int[] { 7,2 });
		//grandTotalTable.setSpacingBefore(5);
		
//		PdfPCell emptyCell = new PdfPCell();
//		emptyCell.setBorder(0);
		grandTotalTable.addCell(emptyCell);
		PdfPCell grandTotalCell = new PdfPCell();
		grandTotalCell.setBorder(0);
		Phrase grandtotalPhraseCell= new Phrase();
		grandtotalPhraseCell.add(new Chunk("			Grand Total", detailsFont));
		grandtotalPhraseCell.add(new Chunk(new VerticalPositionMark()));
		grandtotalPhraseCell.add(new Chunk(Double.toString(invObject.getGrandTotal()), detailsFont));
		grandTotalCell.setPaddingBottom(0f);
		grandTotalCell.setPaddingTop(0f);
		grandTotalCell.addElement(grandtotalPhraseCell);
		grandTotalTable.addCell(grandTotalCell);
		table.addCell(grandTotalTable);
		document.add(table);
		
		
		table= new PdfPTable(1);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		String numberInWords =  convertToIndianCurrency((int) Math.round(grandTotalInv * 100) / 100);
		
		PdfPTable totalInWordTable = new PdfPTable(1);
		//totalInWordTable.setSpacingBefore(5);
		PdfPCell grandTotalCellInWords = new PdfPCell();
		grandTotalCellInWords.setBorder(0);
		Phrase grandtotalINWordsPhraseCell= new Phrase();
		grandtotalINWordsPhraseCell.add(new Chunk("Grand Total in Words : ", detailsFont));
		// deliverNo.add(new Chunk(new VerticalPositionMark()));
		grandtotalINWordsPhraseCell.add(new Chunk(numberInWords, detailsFont));
		grandTotalCellInWords.setPaddingBottom(0f);
		grandTotalCellInWords.setPaddingTop(0f);
		grandTotalCellInWords.addElement(grandtotalINWordsPhraseCell);
		totalInWordTable.addCell(grandTotalCellInWords);
		table.addCell(totalInWordTable);
		document.add(table);
		
		
		
		table = new PdfPTable(2);
		table.setSpacingBefore(5);
		table.setWidths(new int[] { 6,3});
		table.setWidthPercentage(100);
		PdfPCell tncConditions = new PdfPCell();
		tncConditions.setBorder(1);
		tncConditions.setBorderWidthRight(1);
		tncConditions.setBorderWidthLeft(1);
		tncConditions.setBorderWidthBottom(1);
		Phrase tncConditionsPhraseCell= new Phrase();
		tncConditionsPhraseCell.add(new Chunk(" E &O.E> \n 1.Subject to 'Bangalore' Jurisdiction Only \n"
				+ " Interest of 2% per month for payments made after 30 days of bill date.\n ", detailsFont));
		// deliverNo.add(new Chunk(new VerticalPositionMark()));
		
		tncConditions.setPaddingBottom(0f);
		tncConditions.setPaddingTop(0f);
		tncConditions.addElement(tncConditionsPhraseCell);
		table.addCell(tncConditions);
		
		PdfPCell companyName = new PdfPCell();
		companyName.setBorder(0);
		companyName.setBorderWidthRight(1);
		companyName.setBorderWidthTop(1);
		companyName.setBorderWidthBottom(1);
		Phrase companyNamePhraseCell= new Phrase();
		companyNamePhraseCell.add(new Chunk("   Neptune Controls Pvt Ltd  \n"+new Chunk()
				+new Chunk()+"\n       Authorized Sign \n ", detailsFont));
		// deliverNo.add(new Chunk(new VerticalPositionMark()));
	
		companyName.setPaddingBottom(0f);
		companyName.setPaddingTop(0f);
		companyName.addElement(companyNamePhraseCell);
		table.addCell(companyName);
		document.add(table);
		
		table = new PdfPTable(1);
		table.setSpacingBefore(5);
		table.setWidthPercentage(100);
		PdfPCell bankDetailCell = new PdfPCell();
		grandTotalCellInWords.setBorder(0);
		Phrase bankDetailCellPhraseCell= new Phrase();
		bankDetailCellPhraseCell.add(new Chunk("Neptune Controls Pvt Ltd. State Bank of India- A/c No : 37658677572. IFSC Code : SBIN0011349. Branch : SBI Incube Bangalore \n ", detailsFont));
		// deliverNo.add(new Chunk(new VerticalPositionMark()));
		//bankDetailCellPhraseCell.add(new Chunk(numberInWords, detailsFont));
		bankDetailCell.setPaddingBottom(0f);
		bankDetailCell.setPaddingTop(0f);
		bankDetailCell.addElement(bankDetailCellPhraseCell);
		table.addCell(bankDetailCell);
		//table.addCell("Neptune Controls Pvt Ltd.State Bank of India- A/c No : 36195878441. IFSC Code : SBIN0011349.Branch : SBI Incube Bangalore");
		document.add(table);
		document.close();
		
		return path;

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

}
