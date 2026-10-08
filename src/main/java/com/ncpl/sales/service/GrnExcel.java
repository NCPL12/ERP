package com.ncpl.sales.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;

public class GrnExcel extends AbstractXlsxView{
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

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			Map grnMap =  (Map) model.get("grnData");
			Grn grnObj =  (Grn) grnMap.get("grnObj");
			PurchaseOrder poObj=(PurchaseOrder) request.getAttribute("poObj");
			List<GrnItems> grnItems = grnObj.getItems();
			PurchaseOrderService purchaseService=(PurchaseOrderService) request.getAttribute("purchaseService");
			PurchaseItemService purchaseItemService=(PurchaseItemService) request.getAttribute("purchaseItemService");
			ItemMasterService itemService=(ItemMasterService) request.getAttribute("itemMasterService");
			Date poDate=poObj.getUpdated();
			Date date = grnObj.getCreated();
			// Converting date to String
			String dateString = date.toString();
			String[] arr = dateString.split(" ");
			// Formatting date to a required format
			String formattedDate = arr[0];
			formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "/"
					+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "/"
					+ formattedDate.substring(0, 4);
			
			String dateString1 = poDate.toString();
			String[] arr1 = dateString1.split(" ");
			// Formatting date to a required format
			String formattedDate1 = arr1[0];
			formattedDate1 = formattedDate1.substring(formattedDate1.length() - 2, formattedDate1.length()) + "/"
					+ formattedDate1.substring(formattedDate1.length() - 5, formattedDate1.length() - 3) + "/"
					+ formattedDate1.substring(0, 4);
			
			Date todayDate = new Date();
			DateFormat formatter4 = new SimpleDateFormat("dd/MM/yyyy");
			String date4=formatter4.format(todayDate);
			//Generating File Name 
			String fileName = grnObj.getGrnId() + "_grn.xlsx";
			// set excel file name
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			Sheet editAccountSheet = workbook.createSheet("GRN");
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
			descriptionFont.setFontHeightInPoints((short)8);
			descriptionFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			descriptionmergestyle.setFont(descriptionFont);
			descriptionmergestyle.setWrapText(true);
			descriptionCell.setCellStyle(descriptionmergestyle);*/

			editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
					1, // last row
					6, // first column
					11 // last column
			));
			// setBordersToMergedCells(workbook, editAccountSheet);

			// Row poNumber = editAccountSheet.createRow(1);
			Cell poNum = secondRow.createCell(6);
			poNum.setCellValue("GRN No.: " + grnObj.getGrnId());

			CellStyle poNumStyle = workbook.createCellStyle();
			poNumStyle.setBorderLeft(BORDER_THIN);
			poNumStyle.setBorderRight(BORDER_THIN);
			poNumStyle.setBorderTop(BORDER_THIN);
			poNumStyle.setBorderBottom(BORDER_THIN);
			Font poNumFont = workbook.createFont();
			poNumFont.setFontName("Calibri");
			poNumFont.setFontHeightInPoints((short)8);
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
			podte.setCellValue("Invoice Date : " + formattedDate);

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
			poDateFont.setFontHeightInPoints((short)8);
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
			quotation.setCellValue("Invoice No.: " +grnObj.getInvoiceNo());

			CellStyle quotStyle = workbook.createCellStyle();
			quotStyle.setBorderLeft(BORDER_THIN);
			quotStyle.setBorderRight(BORDER_THIN);
			quotStyle.setBorderTop(BORDER_THIN);
			quotStyle.setBorderBottom(BORDER_THIN);
			Font quotFont = workbook.createFont();
			quotFont.setFontName("Calibri");
			quotFont.setFontHeightInPoints((short)8);
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
			qtndate.setCellValue("PO No. : " +grnObj.getPoNumber());

			CellStyle QuotationDatestyle = workbook.createCellStyle();
			QuotationDatestyle.setWrapText(true);
			QuotationDatestyle.setBorderLeft(BORDER_THIN);
			QuotationDatestyle.setBorderRight(BORDER_THIN);
			QuotationDatestyle.setBorderTop(BORDER_THIN);
			QuotationDatestyle.setBorderBottom(BORDER_THIN);
			Font QuotationDateFont = workbook.createFont();
			QuotationDateFont.setFontName("Calibri");
			QuotationDateFont.setFontHeightInPoints((short)8);
			QuotationDatestyle.setFont(QuotationDateFont);
			QuotationDatestyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			QuotationDatestyle.setVerticalAlignment((short) (VERTICAL_TOP));
			qtndate.setCellStyle(QuotationDatestyle);
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(5, // first row
					5, // last row
					6, // first column
					11 // last column
			));
			Row todaysDate = editAccountSheet.createRow(5);
			Cell datetoday = todaysDate.createCell(6);
			datetoday.setCellValue("Vendor Name: " +poObj.getParty().getPartyName());

			CellStyle datetodaystyle = workbook.createCellStyle();
			datetodaystyle.setWrapText(true);
			datetodaystyle.setBorderLeft(BORDER_THIN);
			datetodaystyle.setBorderRight(BORDER_THIN);
			datetodaystyle.setBorderTop(BORDER_THIN);
			datetodaystyle.setBorderBottom(BORDER_THIN);
			Font datetodaystyleFont = workbook.createFont();
			datetodaystyleFont.setFontName("Calibri");
			datetodaystyleFont.setFontHeightInPoints((short)8);
			datetodaystyle.setFont(datetodaystyleFont);
			datetodaystyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			datetodaystyle.setVerticalAlignment((short) (VERTICAL_TOP));
			datetoday.setCellStyle(datetodaystyle);
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(6, // first row
					6, // last row
					6, // first column
					11 // last column
			));

			Row poDate1 = editAccountSheet.createRow(6);
			Cell podatetoday = poDate1.createCell(6);
			podatetoday.setCellValue("PO Date: " +formattedDate1);

			CellStyle podatetodaystyle = workbook.createCellStyle();
			podatetodaystyle.setWrapText(true);
			podatetodaystyle.setBorderLeft(BORDER_THIN);
			podatetodaystyle.setBorderRight(BORDER_THIN);
			podatetodaystyle.setBorderTop(BORDER_THIN);
			podatetodaystyle.setBorderBottom(BORDER_THIN);
			Font podatetodaystyleFont = workbook.createFont();
			podatetodaystyleFont.setFontName("Calibri");
			podatetodaystyleFont.setFontHeightInPoints((short)8);
			podatetodaystyle.setFont(podatetodaystyleFont);
			podatetodaystyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			podatetodaystyle.setVerticalAlignment((short) (VERTICAL_TOP));
			podatetoday.setCellStyle(podatetodaystyle);
			
			
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
			
			Cell modelNum = itemListHeader.createCell(7);
			CellStyle modelNumStyle = workbook.createCellStyle();
			modelNumStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			modelNumStyle.setFont(fontColumn);
			modelNumStyle.setWrapText(true);
			modelNumStyle.setBorderBottom(BORDER_THIN);
			modelNumStyle.setBorderTop(BORDER_THIN);
			modelNumStyle.setBorderRight(BORDER_THIN);
			modelNumStyle.setBorderLeft(BORDER_THIN);
			modelNum.setCellStyle(modelNumStyle);
			modelNum.setCellValue("Model No.");
			
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
					10, // last row
					8, // first column
					8 // last column
			));
			
			Cell gstRate = itemListHeader.createCell(8);
			CellStyle gstRateStyle = workbook.createCellStyle();
			gstRateStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			gstRateStyle.setFont(fontColumn);
			gstRateStyle.setWrapText(true);
			gstRateStyle.setBorderBottom(BORDER_THIN);
			gstRateStyle.setBorderTop(BORDER_THIN);
			gstRateStyle.setBorderRight(BORDER_THIN);
			gstRateStyle.setBorderLeft(BORDER_THIN);
			gstRate.setCellStyle(gstRateStyle);
			gstRate.setCellValue("Total Qty");
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
					10, // last row
					9, // first column
					9 // last column
			));
			
			Cell hsn = itemListHeader.createCell(9);
			CellStyle hsnStyle = workbook.createCellStyle();
			hsnStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			hsnStyle.setFont(fontColumn);
			hsnStyle.setWrapText(true);
			hsnStyle.setBorderBottom(BORDER_THIN);
			hsnStyle.setBorderTop(BORDER_THIN);
			hsnStyle.setBorderRight(BORDER_THIN);
			hsnStyle.setBorderLeft(BORDER_THIN);
			hsn.setCellStyle(hsnStyle);
			hsn.setCellValue("Recieved Qty");

			editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
					10, // last row
					10, // first column
					10 // last column
			));
			
			Cell modelNo = itemListHeader.createCell(10);
			
			CellStyle modelNoStyle = workbook.createCellStyle();
			modelNoStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			modelNoStyle.setFont(fontColumn);
			modelNoStyle.setWrapText(true);
			modelNoStyle.setBorderBottom(BORDER_THIN);
			modelNoStyle.setBorderTop(BORDER_THIN);
			modelNoStyle.setBorderRight(BORDER_THIN);
			modelNoStyle.setBorderLeft(BORDER_THIN);
			modelNo.setCellStyle(modelNoStyle);
			modelNo.setCellValue("Unit Price");
			
			editAccountSheet.addMergedRegion(new CellRangeAddress(7, // first row
					10, // last row
					11, // first column
					11 // last column
			));
			
			Cell qty = itemListHeader.createCell(11);
			CellStyle qtyStyle = workbook.createCellStyle();
			qtyStyle.setWrapText(true);
			qtyStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			qtyStyle.setFont(fontColumn);
			qtyStyle.setBorderBottom(BORDER_THIN);
			qtyStyle.setBorderTop(BORDER_THIN);
			qtyStyle.setBorderRight(BORDER_THIN);
			qtyStyle.setBorderLeft(BORDER_THIN);
			qty.setCellStyle(qtyStyle);
			qty.setCellValue("Amount");
			
			
			
			
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
			threeSideborderRight.setVerticalAlignment((short) (VERTICAL_TOP));
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
			int slNo=1;
			float total=0;
			for (GrnItems grnItem : grnItems) {
				
				Optional<PurchaseItem> poItemObj=purchaseItemService.getPurchaseItemById(Integer.parseInt(grnItem.getDescription()));
				System.out.println(poItemObj.get().getModelNo());
				Optional<ItemMaster> itemObj=itemService.getItemById(poItemObj.get().getModelNo());
				
				
				editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
						rowCount, // last row
						1, // first column
						6// last column
				));
				
				
		
			Row itemList = editAccountSheet.createRow(rowCount);
			Cell slno = itemList.createCell(0);
			slno.setCellStyle(threeSideborder);
			Cell description = itemList.createCell(1);
			CellStyle descriptionStyle = workbook.createCellStyle();
			descriptionStyle.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
			descriptionStyle.setWrapText(true);
			descriptionStyle.setFont(itemListFont);
			descriptionStyle.setBorderLeft(BORDER_THIN);
			descriptionStyle.setBorderRight(BORDER_THIN);
			descriptionStyle.setBorderTop(BORDER_THIN);
			descriptionStyle.setBorderBottom(BORDER_THIN);
			description.setCellStyle(descriptionStyle);
			Cell modelCell = itemList.createCell(7);
			modelCell.setCellStyle(threeSideborder);
			Cell unitCell = itemList.createCell(8);
			unitCell.setCellStyle(qtyborder);
			Cell qtyBoqCell = itemList.createCell(9);
			qtyBoqCell.setCellStyle(qtyborder);
			Cell qtySiteCell = itemList.createCell(10);
			qtySiteCell.setCellStyle(qtyborder);
			Cell orderedQtyCell = itemList.createCell(11);
			orderedQtyCell.setCellStyle(qtyborder);
			
			
	
				
			slno.setCellValue(slNo);
			description.setCellValue(poItemObj.get().getPoDescription());
			modelCell.setCellValue(itemObj.get().getModel());
			unitCell.setCellValue(poItemObj.get().getQuantity());
			qtyBoqCell.setCellValue(grnItem.getReceivedQuantity());
			qtySiteCell.setCellValue(grnItem.getUnitPrice());
			
			
			orderedQtyCell.setCellValue(grnItem.getAmount());
			total=total+grnItem.getAmount();
			slNo++;
			rowCount++;
			}
			int rowLastItemCount = rowCount;
			Row row1 = editAccountSheet.createRow(rowCount);
			
			Cell TotalCell = row1.createCell(10);
			CellStyle TotalCellStyle = workbook.createCellStyle();
			TotalCellStyle.setWrapText(true);
			TotalCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			TotalCellStyle.setBorderBottom(BORDER_THIN);
			TotalCellStyle.setBorderTop(BORDER_THIN);
			TotalCellStyle.setBorderRight(BORDER_THIN);
			TotalCellStyle.setBorderLeft(BORDER_THIN);
			TotalCellStyle.setFont(fontColumn);
			TotalCell.setCellStyle(TotalCellStyle);
			TotalCell.setCellValue("Total");
			
			Cell totalCell1 = row1.createCell(11);
			totalCell1.setCellStyle(qtyborder);
			totalCell1.setCellValue(total);
			setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
			
		}
		
		private void setBordersToMergedCells(Workbook workBook, Sheet sheet, int rowLastItemCount) {
			int numMerged = sheet.getNumMergedRegions();
			for (int i = 0; i < numMerged; i++) {
				CellRangeAddress mergedRegions = sheet.getMergedRegion(i);

//				if (mergedRegions.getFirstRow() == 18
//						|| (mergedRegions.getFirstRow() < rowLastItemCount && mergedRegions.getFirstRow() > 18)) {
	//
//				} else {
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