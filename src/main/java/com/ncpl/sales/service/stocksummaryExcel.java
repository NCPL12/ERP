package com.ncpl.sales.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
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
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;

public class stocksummaryExcel extends AbstractXlsxView {

	short VERTICAL_TOP = 0x0;
	short VERTICAL_TOP1 = 0x1;
	short VERTICAL_JUSTIFY = 0x2;
	short BORDER_THIN = 0x1;

	String company = "Neptune controls pvt ltd No.8/2(Old No.114), 2nd Cross 7th Main Road Nandidurga Extension Bangalore-560046 Contact : 080-40904685,7624964492 "
			+ "E-Mail : accounts@ncpl.co";
	String companyName = "";

	String qtn = "";

	String contactPerson = " Ms Seema ";
	String contactNo = " 7624919715 ";
	String gstNo = "GSTIN : 29AADCN5426F1ZG";
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

	String invoiceTerm = "";
	String invoiceCondition = "";
	String s1 = "Complete solution for BMS, Lighting Control, CCTV & Security Systems, DDC Panels, Automation Panels, Lighting,panels, MCC & Starter Panels";

	FileNameGenerator fileNameGenerator = new FileNameGenerator();

	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");

	DcLogoService logoService = new DcLogoService();

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub

		Sheet editAccountSheet = workbook.createSheet("Stock summary");
		editAccountSheet.setDefaultColumnWidth(9);
		// editAccountSheet.autoSizeColumn(11);

		

		Map stockMap = (Map) model.get("stockSummary");
		Object month = stockMap.get("monthName");
		
		
		// Generating File Name
		String fileName = month.toString() + "-" + "Inward_outward_date.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		Map<String, Float> stockSummaryMap = (Map<String, Float>) stockMap.get("stockMap");
		Map<String, Map> stockListmap = (Map<String, Map>) stockMap.get("stockMap");
		Map<String, Map> invoicemap = (Map<String, Map>) stockListmap.get("grnlist");
		Map<String, Map> noGrnmap = (Map<String, Map>) stockListmap.get("nogrnlist");
		
		Map<String, Map> dcmap = (Map<String, Map>) stockMap.get("dcMap");
		Map<String, Map> grnmap = (Map<String, Map>) stockMap.get("grnMap");
		
		
		
		// setBordersToMergedCells(workbook, editAccountSheet);
		// create style for header cells

		CellStyle companyNamestyle = workbook.createCellStyle();
		companyNamestyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		Font cmpnyFont = workbook.createFont();
		cmpnyFont.setFontName("Calibri");
		cmpnyFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		cmpnyFont.setFontHeight((short) (7.5 * 35));
		companyNamestyle.setFont(cmpnyFont);

		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				1, // last row
				0, // first column
				2 // last column
		));

		Row firstRow = editAccountSheet.createRow(1);
		Cell compnyName = firstRow.createCell(0);
		compnyName.setCellStyle(companyNamestyle);
		compnyName.setCellValue("Neptune Controls Pvt Ltd");

		editAccountSheet.addMergedRegion(new CellRangeAddress(2, // first row
				7, // last row
				0, // first column
				3 // last column
		));
		Row secondRow = editAccountSheet.createRow(2);
		Cell address = secondRow.createCell(0);
		CellStyle addresstyle = workbook.createCellStyle();
		addresstyle.setWrapText(true);
		address.setCellStyle(addresstyle);
		address.setCellValue("# 09, Ground Floor, 2nd Cross" + "\n" + "7th Main, Nandidurga Extension" + "\n"
				+ "Banglaore- 560046" + "\n" + "CIN: U31200KA2011PTC056705" + "\n" + "Contact : 080-40904685,7624964492"
				+ "\n" + "www.ncpl.co/");

		editAccountSheet.addMergedRegion(new CellRangeAddress(9, // first row
				9, // last row
				0, // first column
				2 // last column
		));
		CellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		Font font = workbook.createFont();
		font.setFontName("Calibri");
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		// font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);

		Row header = editAccountSheet.createRow(9);
		Cell stock = header.createCell(0);
		stock.setCellStyle(style);
		stock.setCellValue("Stock Summary");

		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				10, // last row
				0, // first column
				3 // last column
		));

		Row columnHeader = editAccountSheet.createRow(10);
		Cell particular = columnHeader.createCell(0);
		particular.setCellStyle(style);
		particular.setCellValue("Particulars");

		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				10, // last row
				4, // first column
				6 // last column
		));

		Cell openingBal = columnHeader.createCell(4);
		openingBal.setCellStyle(style);
		openingBal.setCellValue("Opening Balance");

		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				10, // last row
				7, // first column
				9 // last column
		));

		Cell inwards = columnHeader.createCell(7);
		inwards.setCellStyle(style);
		inwards.setCellValue("Inwards");

		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				10, // last row
				10, // first column
				12// last column
		));

		Cell outwards = columnHeader.createCell(10);
		outwards.setCellStyle(style);
		outwards.setCellValue("Outwards");

		editAccountSheet.addMergedRegion(new CellRangeAddress(10, // first row
				10, // last row
				13, // first column
				15 // last column
		));

		Cell closingBal = columnHeader.createCell(13);
		closingBal.setCellStyle(style);
		closingBal.setCellValue("Closing Balance");

		CellStyle fourSideborder = workbook.createCellStyle();
		fourSideborder.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		fourSideborder.setBorderLeft(BORDER_THIN);
		fourSideborder.setBorderRight(BORDER_THIN);
		fourSideborder.setBorderBottom(BORDER_THIN);
		fourSideborder.setBorderTop(BORDER_THIN);

		Row columnSubGrp = editAccountSheet.createRow(11);
		Cell qty = columnSubGrp.createCell(4);
		qty.setCellStyle(fourSideborder);
		qty.setCellValue("Quantity");
		Cell rate = columnSubGrp.createCell(5);
		rate.setCellStyle(fourSideborder);
		rate.setCellValue("Rate");
		Cell value = columnSubGrp.createCell(6);
		value.setCellStyle(fourSideborder);
		value.setCellValue("Value");
		Cell qty1 = columnSubGrp.createCell(7);
		qty1.setCellStyle(fourSideborder);
		qty1.setCellValue("Quantity");
		Cell rate1 = columnSubGrp.createCell(8);
		rate1.setCellStyle(fourSideborder);
		rate1.setCellValue("Rate");
		Cell value1 = columnSubGrp.createCell(9);
		value1.setCellStyle(fourSideborder);
		value1.setCellValue("Value");
		Cell qty2 = columnSubGrp.createCell(10);
		qty2.setCellStyle(fourSideborder);
		qty2.setCellValue("Quantity");
		Cell rate2 = columnSubGrp.createCell(11);
		rate2.setCellStyle(fourSideborder);
		rate2.setCellValue("Rate");
		Cell value2 = columnSubGrp.createCell(12);
		value2.setCellStyle(fourSideborder);
		value2.setCellValue("Value");
		Cell qty3 = columnSubGrp.createCell(13);
		qty3.setCellStyle(fourSideborder);
		qty3.setCellValue("Quantity");
		Cell rate3 = columnSubGrp.createCell(14);
		rate3.setCellStyle(fourSideborder);
		rate3.setCellValue("Rate");
		Cell value3 = columnSubGrp.createCell(15);
		value3.setCellStyle(fourSideborder);
		value3.setCellValue("Value");

		int rowCount = 12;
		for (Map s : invoicemap.values()) {
			Row valuesRow = editAccountSheet.createRow(rowCount);

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					0, // first column
					3 // last column
			));

			// valuesRow.createCell(0).setCellValue((String) s.get("particulars"));
			String desc = (String) s.get("particulars");
			// desc = "TEKINIKA INFOTECH PVT LTD \n NEPTUNE CONTROLS PVT LIMITED";
			System.out.println("descc" + desc.length());
			if (desc.length() > 37) {
				// if(purchaseItem.getDescription().length()>80){
				valuesRow.setHeightInPoints((float) (2.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}

			// valuesRow.createCell(0).setCellValue("TEKINIKA INFOTECH PVT LTD \n NEPTUNE
			// CONTROLS PVT LIMITED");
			float openQ1 = (float) s.get("openQ1");
			float openR1 = (float) s.get("openR1");
			System.out.println((float) s.get("openV1"));
			float openV1 = (float) s.get("openV1");
			float grnQ1 = (float) s.get("grnQ1");
			float grnR1 = (float) s.get("grnR1");
			float grnV1 = (float) s.get("grnV1");
			float dcQ1 = (float) s.get("dcQ1");
			float dcR1 = (float) s.get("dcR1");
			float dcV1 = (float) s.get("dcV1");
			float clQ1 = (float) s.get("clQ1");
			float clV1 = (float) s.get("clV1");
			float clR1 = (float) s.get("clR1");

			CellStyle fourSideborderForValues = workbook.createCellStyle();
			fourSideborderForValues.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			fourSideborderForValues.setBorderLeft(BORDER_THIN);
			fourSideborderForValues.setBorderRight(BORDER_THIN);
			fourSideborderForValues.setBorderBottom(BORDER_THIN);
			fourSideborderForValues.setBorderTop(BORDER_THIN);

			CellStyle coulmnVales = workbook.createCellStyle();
			coulmnVales.setWrapText(true);
			coulmnVales.setBorderLeft(BORDER_THIN);
			coulmnVales.setBorderRight(BORDER_THIN);
			coulmnVales.setBorderBottom(BORDER_THIN);
			coulmnVales.setBorderTop(BORDER_THIN);
			Cell partcularsCell = valuesRow.createCell(0);
			partcularsCell.setCellStyle(coulmnVales);
			partcularsCell.setCellValue(desc);
			XSSFDataFormat columnstyleformat = (XSSFDataFormat) workbook.createDataFormat();
			coulmnVales.setDataFormat(columnstyleformat.getFormat("#,###.0"));
			if (openQ1 > 0 || openQ1 < 0) {
				Cell openQty = valuesRow.createCell(4);
				openQty.setCellStyle(coulmnVales);
				double openQtyValue = Math.round(openQ1 * 100.0) / 100.0;
				if(openQtyValue%1==0) {
					openQty.setCellStyle(fourSideborderForValues);
				}else {
					openQty.setCellStyle(coulmnVales);
					
				}
				openQty.setCellValue(openQtyValue);
				Cell openRate = valuesRow.createCell(5);
				openRate.setCellStyle(coulmnVales);
				openRate.setCellValue(Math.round(openR1 * 100.0) / 100.0);
				Cell openValue = valuesRow.createCell(6);
				openValue.setCellStyle(coulmnVales);
				openValue.setCellValue(Math.round(openV1 * 100.0) / 100.0);
			} else {
				Cell openQty = valuesRow.createCell(4);
				openQty.setCellStyle(fourSideborderForValues);
				openQty.setCellValue(0);
				Cell openRate = valuesRow.createCell(5);
				openRate.setCellStyle(fourSideborderForValues);
				openRate.setCellValue(0);
				Cell openValue = valuesRow.createCell(6);
				openValue.setCellStyle(fourSideborderForValues);
				openValue.setCellValue(0);
			}
			if (grnQ1 > 0 || grnQ1 < 0) {
				Cell grnQty = valuesRow.createCell(7);
				grnQty.setCellStyle(coulmnVales);
				double grnQtyValue = Math.round(grnQ1 * 100.0) / 100.0;
				if(grnQtyValue%1==0) {
					grnQty.setCellStyle(fourSideborderForValues);
				}else {
					grnQty.setCellStyle(coulmnVales);
					
				}
				grnQty.setCellValue(grnQtyValue);
				Cell grnRate = valuesRow.createCell(8);
				grnRate.setCellStyle(coulmnVales);
				grnRate.setCellValue(Math.round(grnR1 * 100.0) / 100.0);
				Cell grnValue = valuesRow.createCell(9);
				grnValue.setCellStyle(coulmnVales);
				grnValue.setCellValue(Math.round(grnV1 * 100.0) / 100.0);
			} else {
				Cell grnQty = valuesRow.createCell(7);
				grnQty.setCellStyle(fourSideborderForValues);
				grnQty.setCellValue(0);
				Cell grnRate = valuesRow.createCell(8);
				grnRate.setCellStyle(fourSideborderForValues);
				grnRate.setCellValue(0);
				Cell grnValue = valuesRow.createCell(9);
				grnValue.setCellStyle(fourSideborderForValues);
				grnValue.setCellValue(0);
			}

			if (dcQ1 > 0 || dcQ1 < 0) {
				Cell dcQty = valuesRow.createCell(10);
				dcQty.setCellStyle(coulmnVales);
				double dcQtyValue = Math.round(dcQ1 * 100.0) / 100.0;
				if(dcQtyValue%1==0) {
					dcQty.setCellStyle(fourSideborderForValues);
				}else {
					dcQty.setCellStyle(coulmnVales);
					
				}
				dcQty.setCellValue(dcQtyValue);
				Cell dcRate = valuesRow.createCell(11);
				dcRate.setCellStyle(coulmnVales);
				dcRate.setCellValue(Math.round(dcR1 * 100.0) / 100.0);
				Cell dcValue = valuesRow.createCell(12);
				dcValue.setCellStyle(coulmnVales);
				dcValue.setCellValue(Math.round(dcV1 * 100.0) / 100.0);
			} else {
				Cell dcQty = valuesRow.createCell(10);
				dcQty.setCellStyle(fourSideborderForValues);
				dcQty.setCellValue(0);
				Cell dcRate = valuesRow.createCell(11);
				dcRate.setCellStyle(fourSideborderForValues);
				dcRate.setCellValue(0);
				Cell dcValue = valuesRow.createCell(12);
				dcValue.setCellStyle(fourSideborderForValues);
				dcValue.setCellValue(0);
			}
			if (clQ1 > 0 || clQ1 < 0) {
				Cell closedQty = valuesRow.createCell(13);
				closedQty.setCellStyle(coulmnVales);
				
				double closedQtyValue = Math.round(clQ1 * 100.0) / 100.0;
				if(closedQtyValue%1==0) {
					closedQty.setCellStyle(fourSideborderForValues);
				}else {
					closedQty.setCellStyle(coulmnVales);
					
				}
				
				closedQty.setCellValue(closedQtyValue);
				Cell closedRate = valuesRow.createCell(14);
				closedRate.setCellStyle(coulmnVales);
				closedRate.setCellValue(Math.round(clR1 * 100.0) / 100.0);
				Cell closedValue = valuesRow.createCell(15);
				closedValue.setCellStyle(coulmnVales);
				closedValue.setCellValue(Math.round(clV1 * 100.0) / 100.0);
			} else {

				Cell closedQty = valuesRow.createCell(13);
				closedQty.setCellStyle(fourSideborderForValues);
				closedQty.setCellValue(0);
				Cell closedRate = valuesRow.createCell(14);
				closedRate.setCellStyle(fourSideborderForValues);
				closedRate.setCellValue(0);
				Cell closedValue = valuesRow.createCell(15);
				closedValue.setCellStyle(fourSideborderForValues);
				closedValue.setCellValue(0);
			}
			/*
			 * valuesRow.createCell(4).setCellValue(openQ1);
			 * valuesRow.createCell(5).setCellValue(openR1);
			 * valuesRow.createCell(6).setCellValue(openV1);
			 * valuesRow.createCell(7).setCellValue(grnQ1);
			 * valuesRow.createCell(8).setCellValue(grnR1);
			 * valuesRow.createCell(9).setCellValue(grnV1);
			 * valuesRow.createCell(10).setCellValue(dcQ1);
			 * valuesRow.createCell(11).setCellValue(dcR1);
			 * valuesRow.createCell(12).setCellValue(dcV1);
			 * valuesRow.createCell(13).setCellValue(clQ1);
			 * valuesRow.createCell(14).setCellValue(clR1);
			 * valuesRow.createCell(15).setCellValue(clV1);
			 */
			rowCount++;
		}
		for (Map s : noGrnmap.values()) {
			Row valuesRow = editAccountSheet.createRow(rowCount);

			editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
					rowCount, // last row
					0, // first column
					3 // last column
			));

			// valuesRow.createCell(0).setCellValue((String) s.get("particulars"));
			String desc = (String) s.get("particulars");
			// desc = "TEKINIKA INFOTECH PVT LTD \n NEPTUNE CONTROLS PVT LIMITED";
			System.out.println("descc" + desc.length());
			if (desc.length() > 37) {
				// if(purchaseItem.getDescription().length()>80){
				valuesRow.setHeightInPoints((float) (2.5 * editAccountSheet.getDefaultRowHeightInPoints()));
			}

			// valuesRow.createCell(0).setCellValue("TEKINIKA INFOTECH PVT LTD \n NEPTUNE
			// CONTROLS PVT LIMITED");
			float openQ1 = (float) s.get("openQ1");
			float openR1 = (float) s.get("openR1");
			System.out.println((float) s.get("openV1"));
			float openV1 = (float) s.get("openV1");
			float grnQ1 = (float) s.get("grnQ1");
			float grnR1 = (float) s.get("grnR1");
			float grnV1 = (float) s.get("grnV1");
			float dcQ1 = (float) s.get("dcQ1");
			float dcR1 = (float) s.get("dcR1");
			float dcV1 = (float) s.get("dcV1");
			float clQ1 = (float) s.get("clQ1");
			float clV1 = (float) s.get("clV1");
			float clR1 = (float) s.get("clR1");

			CellStyle fourSideborderForValues = workbook.createCellStyle();
			fourSideborderForValues.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			fourSideborderForValues.setBorderLeft(BORDER_THIN);
			fourSideborderForValues.setBorderRight(BORDER_THIN);
			fourSideborderForValues.setBorderBottom(BORDER_THIN);
			fourSideborderForValues.setBorderTop(BORDER_THIN);

			CellStyle coulmnVales = workbook.createCellStyle();
			coulmnVales.setWrapText(true);
			coulmnVales.setBorderLeft(BORDER_THIN);
			coulmnVales.setBorderRight(BORDER_THIN);
			coulmnVales.setBorderBottom(BORDER_THIN);
			coulmnVales.setBorderTop(BORDER_THIN);
			Cell partcularsCell = valuesRow.createCell(0);
			partcularsCell.setCellStyle(coulmnVales);
			partcularsCell.setCellValue(desc);
			XSSFDataFormat columnstyleformat = (XSSFDataFormat) workbook.createDataFormat();
			coulmnVales.setDataFormat(columnstyleformat.getFormat("#,###.0"));
			if (openQ1 > 0 || openQ1 < 0) {
				Cell openQty = valuesRow.createCell(4);
				openQty.setCellStyle(coulmnVales);
				double openQtyValue = Math.round(openQ1 * 100.0) / 100.0;
				if(openQtyValue%1==0) {
					openQty.setCellStyle(fourSideborderForValues);
				}else {
					openQty.setCellStyle(coulmnVales);
					
				}
				openQty.setCellValue(openQtyValue);
				Cell openRate = valuesRow.createCell(5);
				openRate.setCellStyle(coulmnVales);
				openRate.setCellValue(Math.round(openR1 * 100.0) / 100.0);
				Cell openValue = valuesRow.createCell(6);
				openValue.setCellStyle(coulmnVales);
				openValue.setCellValue(Math.round(openV1 * 100.0) / 100.0);
			} else {
				Cell openQty = valuesRow.createCell(4);
				openQty.setCellStyle(fourSideborderForValues);
				openQty.setCellValue(0);
				Cell openRate = valuesRow.createCell(5);
				openRate.setCellStyle(fourSideborderForValues);
				openRate.setCellValue(0);
				Cell openValue = valuesRow.createCell(6);
				openValue.setCellStyle(fourSideborderForValues);
				openValue.setCellValue(0);
			}
			if (grnQ1 > 0 || grnQ1 < 0) {
				Cell grnQty = valuesRow.createCell(7);
				grnQty.setCellStyle(coulmnVales);
				double grnQtyValue = Math.round(grnQ1 * 100.0) / 100.0;
				if(grnQtyValue%1==0) {
					grnQty.setCellStyle(fourSideborderForValues);
				}else {
					grnQty.setCellStyle(coulmnVales);
					
				}
				grnQty.setCellValue(grnQtyValue);
				Cell grnRate = valuesRow.createCell(8);
				grnRate.setCellStyle(coulmnVales);
				grnRate.setCellValue(Math.round(grnR1 * 100.0) / 100.0);
				Cell grnValue = valuesRow.createCell(9);
				grnValue.setCellStyle(coulmnVales);
				grnValue.setCellValue(Math.round(grnV1 * 100.0) / 100.0);
			} else {
				Cell grnQty = valuesRow.createCell(7);
				grnQty.setCellStyle(fourSideborderForValues);
				grnQty.setCellValue(0);
				Cell grnRate = valuesRow.createCell(8);
				grnRate.setCellStyle(fourSideborderForValues);
				grnRate.setCellValue(0);
				Cell grnValue = valuesRow.createCell(9);
				grnValue.setCellStyle(fourSideborderForValues);
				grnValue.setCellValue(0);
			}

			if (dcQ1 > 0 || dcQ1 < 0) {
				Cell dcQty = valuesRow.createCell(10);
				dcQty.setCellStyle(coulmnVales);
				double dcQtyValue = Math.round(dcQ1 * 100.0) / 100.0;
				if(dcQtyValue%1==0) {
					dcQty.setCellStyle(fourSideborderForValues);
				}else {
					dcQty.setCellStyle(coulmnVales);
					
				}
				dcQty.setCellValue(dcQtyValue);
				Cell dcRate = valuesRow.createCell(11);
				dcRate.setCellStyle(coulmnVales);
				dcRate.setCellValue(Math.round(dcR1 * 100.0) / 100.0);
				Cell dcValue = valuesRow.createCell(12);
				dcValue.setCellStyle(coulmnVales);
				dcValue.setCellValue(Math.round(dcV1 * 100.0) / 100.0);
			} else {
				Cell dcQty = valuesRow.createCell(10);
				dcQty.setCellStyle(fourSideborderForValues);
				dcQty.setCellValue(0);
				Cell dcRate = valuesRow.createCell(11);
				dcRate.setCellStyle(fourSideborderForValues);
				dcRate.setCellValue(0);
				Cell dcValue = valuesRow.createCell(12);
				dcValue.setCellStyle(fourSideborderForValues);
				dcValue.setCellValue(0);
			}
			if (clQ1 > 0 || clQ1 < 0) {
				Cell closedQty = valuesRow.createCell(13);
				closedQty.setCellStyle(coulmnVales);
				
				double closedQtyValue = Math.round(clQ1 * 100.0) / 100.0;
				if(closedQtyValue%1==0) {
					closedQty.setCellStyle(fourSideborderForValues);
				}else {
					closedQty.setCellStyle(coulmnVales);
					
				}
				
				closedQty.setCellValue(closedQtyValue);
				Cell closedRate = valuesRow.createCell(14);
				closedRate.setCellStyle(coulmnVales);
				closedRate.setCellValue(Math.round(clR1 * 100.0) / 100.0);
				Cell closedValue = valuesRow.createCell(15);
				closedValue.setCellStyle(coulmnVales);
				closedValue.setCellValue(Math.round(clV1 * 100.0) / 100.0);
			} else {

				Cell closedQty = valuesRow.createCell(13);
				closedQty.setCellStyle(fourSideborderForValues);
				closedQty.setCellValue(0);
				Cell closedRate = valuesRow.createCell(14);
				closedRate.setCellStyle(fourSideborderForValues);
				closedRate.setCellValue(0);
				Cell closedValue = valuesRow.createCell(15);
				closedValue.setCellStyle(fourSideborderForValues);
				closedValue.setCellValue(0);
			}
			/*
			 * valuesRow.createCell(4).setCellValue(openQ1);
			 * valuesRow.createCell(5).setCellValue(openR1);
			 * valuesRow.createCell(6).setCellValue(openV1);
			 * valuesRow.createCell(7).setCellValue(grnQ1);
			 * valuesRow.createCell(8).setCellValue(grnR1);
			 * valuesRow.createCell(9).setCellValue(grnV1);
			 * valuesRow.createCell(10).setCellValue(dcQ1);
			 * valuesRow.createCell(11).setCellValue(dcR1);
			 * valuesRow.createCell(12).setCellValue(dcV1);
			 * valuesRow.createCell(13).setCellValue(clQ1);
			 * valuesRow.createCell(14).setCellValue(clR1);
			 * valuesRow.createCell(15).setCellValue(clV1);
			 */
			rowCount++;
		}
		
		/*
		 * int rowCountDc = rowCount; for (Map s : dcmap.values()) { Row valuesRow =
		 * editAccountSheet.createRow(rowCountDc);
		 * valuesRow.createCell(0).setCellValue((String) s.get("particulars")); float
		 * openQ1 = (float) s.get("openQ1"); float openR1 = (float) s.get("openR1");
		 * System.out.println((float) s.get("openV1")); float openV1 = (float)
		 * s.get("openV1"); float grnQ1 = (float) s.get("grnQ1"); float grnR1 = (float)
		 * s.get("grnR1"); float grnV1 = (float) s.get("grnV1"); float dcQ1 = (float)
		 * s.get("dcQ1"); float dcR1 = (float) s.get("dcR1"); float dcV1 = (float)
		 * s.get("dcV1");
		 * 
		 * valuesRow.createCell(4).setCellValue(openQ1);
		 * valuesRow.createCell(5).setCellValue(openR1);
		 * valuesRow.createCell(6).setCellValue(openV1);
		 * valuesRow.createCell(7).setCellValue(grnQ1);
		 * valuesRow.createCell(8).setCellValue(grnR1);
		 * valuesRow.createCell(9).setCellValue(grnV1);
		 * valuesRow.createCell(10).setCellValue(dcQ1);
		 * valuesRow.createCell(11).setCellValue(dcR1);
		 * valuesRow.createCell(12).setCellValue(dcV1);
		 * valuesRow.createCell(13).setCellValue(openQ1+grnQ1-dcQ1);
		 * valuesRow.createCell(14).setCellValue(openR1+grnR1-dcR1);
		 * valuesRow.createCell(15).setCellValue(openV1+grnV1-dcV1); rowCountDc++; }
		 * 
		 * int rowCountGrn = rowCountDc; for (Map s : grnmap.values()) { Row valuesRow =
		 * editAccountSheet.createRow(rowCountGrn);
		 * valuesRow.createCell(0).setCellValue((String) s.get("particulars")); float
		 * openQ1 = (float) s.get("openQ1"); float openR1 = (float) s.get("openR1");
		 * System.out.println((float) s.get("openV1")); float openV1 = (float)
		 * s.get("openV1"); float grnQ1 = (float) s.get("grnQ1"); float grnR1 = (float)
		 * s.get("grnR1"); float grnV1 = (float) s.get("grnV1"); float dcQ1 = (float)
		 * s.get("dcQ1"); float dcR1 = (float) s.get("dcR1"); float dcV1 = (float)
		 * s.get("dcV1");
		 * 
		 * valuesRow.createCell(4).setCellValue(openQ1);
		 * valuesRow.createCell(5).setCellValue(openR1);
		 * valuesRow.createCell(6).setCellValue(openV1);
		 * valuesRow.createCell(7).setCellValue(grnQ1);
		 * valuesRow.createCell(8).setCellValue(grnR1);
		 * valuesRow.createCell(9).setCellValue(grnV1);
		 * valuesRow.createCell(10).setCellValue(dcQ1);
		 * valuesRow.createCell(11).setCellValue(dcR1);
		 * valuesRow.createCell(12).setCellValue(dcV1);
		 * valuesRow.createCell(13).setCellValue(openQ1+grnQ1-dcQ1);
		 * valuesRow.createCell(14).setCellValue(openR1+grnR1-dcR1);
		 * valuesRow.createCell(15).setCellValue(openV1+grnV1-dcV1); rowCountGrn++; }
		 * 
		 */
		setBordersToMergedCells(workbook, editAccountSheet);

	}

	private void setBordersToMergedCells(Workbook workBook, Sheet sheet) {
		int numMerged = sheet.getNumMergedRegions();
		for (int i = 0; i < numMerged; i++) {
			CellRangeAddress mergedRegions = sheet.getMergedRegion(i);

			// RegionUtil.setRightBorderColor(IndexedColors.WHITE.getIndex(), mergedRegions,
			// sheet, workBook);
			RegionUtil.setBorderTop(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			RegionUtil.setBorderRight(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);
			RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, mergedRegions, sheet, workBook);

		}
	}

}
