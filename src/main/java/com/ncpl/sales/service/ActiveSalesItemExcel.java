package com.ncpl.sales.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.model.SalesItem;

public class ActiveSalesItemExcel extends AbstractXlsxView{
	static short VERTICAL_TOP = 0x0;
	static short VERTICAL_JUSTIFY = 0x2;
	 static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	@SuppressWarnings("unchecked")
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String fileName =  "active_items.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		Sheet editAccountSheet = workbook.createSheet("Items");
		editAccountSheet.setDefaultColumnWidth(9);
		
		List<SalesItem> salesItemList =  (List<SalesItem>) request.getAttribute("salesItemList");
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

		CellStyle threeSideborderBold = workbook.createCellStyle();
		threeSideborderBold.setWrapText(true);
		threeSideborderBold.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderBold.setBorderLeft(BORDER_THIN);
		threeSideborderBold.setBorderRight(BORDER_THIN);
		threeSideborderBold.setBorderBottom(BORDER_THIN);
		threeSideborderBold.setBorderTop(BORDER_THIN);
		threeSideborderBold.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font fontColumns = workbook.createFont();
		fontColumns.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumns.setFontHeightInPoints((short)10);
		threeSideborderBold.setFont(fontColumns);
		
    	header.createCell(0).setCellValue("SO Number");
    	header.createCell(1).setCellValue("SO Date");
    	header.createCell(2).setCellValue("Client PO No");
    	header.createCell(3).setCellValue("Item Description");
		header.createCell(4).setCellValue("Qty");
		
		for (int i = 0; i < header.getLastCellNum(); i++) {
			header.getCell(i).setCellStyle(threeSideborderBold); 
			editAccountSheet.autoSizeColumn(i);
		}

		populateRecords(salesItemList, editAccountSheet, workbook, request);

		
        System.out.println("po List by Date report Has been Created successfully!");
       
	}
	
	@SuppressWarnings({ "unused" })
	private static void populateRecords(List<SalesItem> salesItemList, Sheet editAccountSheet,
			Workbook workbook, HttpServletRequest request) {
		int rowCount=1;
		Collections.sort(salesItemList);
		for (SalesItem salesItem : salesItemList) {
			Date created = salesItem.getSalesOrder().getCreated();
			String dateString = created.toString();
			String[] arr = dateString.split(" ");

			// Formatting date to a required format
			String formattedDate = arr[0];
			formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
					+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
					+ formattedDate.substring(0, 4);

					
			String voucherType = "Purchase";
			
			
			CellStyle declimalStyle = workbook.createCellStyle();
			XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
			declimalStyle.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
			declimalStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			
			
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
			
			Row row = editAccountSheet.createRow(rowCount++);
			row.createCell(0).setCellValue(salesItem.getSalesOrder().getId());
			row.createCell(1).setCellValue(formattedDate);
			row.createCell(2).setCellValue(salesItem.getSalesOrder().getClientPoNumber());
			row.createCell(3).setCellValue(salesItem.getDescription());
			row.createCell(4).setCellValue(salesItem.getQuantity());
			for (int i = 0; i < row.getLastCellNum(); i++) {
				row.getCell(i).setCellStyle(threeSideborder);
				editAccountSheet.autoSizeColumn(i);
			}
		}
			
		
	
	}

}
