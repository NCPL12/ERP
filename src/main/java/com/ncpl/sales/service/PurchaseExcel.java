package com.ncpl.sales.service;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;

public class PurchaseExcel extends AbstractXlsxView{
	
	
	 short VERTICAL_TOP = 0x0;
	 short VERTICAL_JUSTIFY = 0x2;
	 short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
		@SuppressWarnings("unchecked")
		@Override
		protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
				HttpServletResponse response) throws Exception {
			// TODO Auto-generated method stub
			
			String fileName =  "Purchase.xlsx";
			// set excel file name
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			Sheet editAccountSheet = workbook.createSheet("Purchase");
			editAccountSheet.setDefaultColumnWidth(9);
			
			List<PurchaseOrder> poList =  (List<PurchaseOrder>) request.getAttribute("poList");
			Map modelList = (Map) request.getAttribute("modelMaps");
		//	setBordersToMergedCells(workbook, editAccountSheet);
			// create style for header cells
			CellStyle style = workbook.createCellStyle();
			Font font = workbook.createFont();
			font.setFontName("Calibri");

			font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			font.setColor(HSSFColor.WHITE.index);
			style.setFont(font);
			
			Font fontColumn = workbook.createFont();
			fontColumn.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			fontColumn.setFontHeight((short) (7.5 * 30));
			
			
			Row header = editAccountSheet.createRow(0);
			CellStyle itemHeaderStyle = workbook.createCellStyle();
			itemHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			itemHeaderStyle.setFont(fontColumn);
			itemHeaderStyle.setBorderBottom(BORDER_THIN);
			itemHeaderStyle.setBorderTop(BORDER_THIN);
			itemHeaderStyle.setBorderRight(BORDER_THIN);
			itemHeaderStyle.setBorderLeft(BORDER_THIN);
			itemHeaderStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			itemHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			  
	     	header.createCell(0).setCellValue("Inv Date");
	     	
	    	header.createCell(1).setCellValue("Inv No");
	    	header.createCell(2).setCellValue("SR Inv Ref");
	    	header.createCell(3).setCellValue("SR date");
	    	header.createCell(4).setCellValue("Voucher Type");
			header.createCell(5).setCellValue("Alias");
			header.createCell(6).setCellValue("Party Name");
			header.createCell(7).setCellValue("Address 1");
			header.createCell(8).setCellValue("Address 2");
			header.createCell(9).setCellValue("Address 3");
			header.createCell(10).setCellValue("Address 4");
			header.createCell(11).setCellValue("Party State");
			header.createCell(12).setCellValue("GST Type");
			header.createCell(13).setCellValue("Party GST No");
			header.createCell(14).setCellValue("Part Number");
			header.createCell(15).setCellValue("ITEM NAME");
			header.createCell(16).setCellValue("Godown");
			header.createCell(17).setCellValue("Batch No");
			header.createCell(18).setCellValue("Item Group");
			header.createCell(19).setCellValue("UOM");
			header.createCell(20).setCellValue("HSN Code");
			header.createCell(21).setCellValue("Qty");
			header.createCell(22).setCellValue("Free Qty");
			header.createCell(23).setCellValue("Rate");
			header.createCell(24).setCellValue("MRP");
			header.createCell(25).setCellValue("Amount");
			header.createCell(26).setCellValue("Disc %");
			header.createCell(27).setCellValue("Net Amount");
			header.createCell(28).setCellValue("Freight");
			header.createCell(29).setCellValue("Loading");
			header.createCell(30).setCellValue("Unloading");
			header.createCell(31).setCellValue("Discount Amount");
			header.createCell(32).setCellValue("Taxable Amount");
			header.createCell(33).setCellValue("GST %");
			header.createCell(34).setCellValue("GST Amount");
			header.createCell(35).setCellValue("NARRATION");
			for (int i = 0; i < header.getLastCellNum(); i++) {
				header.getCell(i).setCellStyle(itemHeaderStyle); 
				editAccountSheet.autoSizeColumn(i);
			}

			populateRecords(poList, editAccountSheet, workbook,modelList, request);

			
	        System.out.println("po List by Date report Has been Created successfully!");
	        
		
			
		}
		@SuppressWarnings({ "unused", "rawtypes" })
		private static void populateRecords(List<PurchaseOrder> poList, Sheet editAccountSheet,
				Workbook workbook,Map modelList, HttpServletRequest request) {
			int rowCount=1;
			ItemMasterService itemService=(ItemMasterService) request.getAttribute("itemMasterService");
			SalesService salesService=(SalesService) request.getAttribute("salesService");
			Collections.sort(poList);
			for (PurchaseOrder purchaseOrder : poList) {
				
			List<PurchaseItem> poItemsList = purchaseOrder.getItems();
			for (PurchaseItem purchaseItem : poItemsList) {
				
				Optional<ItemMaster> itemObj = itemService.getItemById(purchaseItem.getModelNo());
				String itemName =  itemObj.get().getItemName();
				int gst = itemObj.get().getGst();
				String unitName = itemObj.get().getItem_units().getName();
				String hsn = itemObj.get().getHsnCode();
				String partNum =  itemObj.get().getModel();
				
				String invNum = purchaseOrder.getPoNumber();
				Date invDate = purchaseOrder.getCreated();
				String dateString = invDate.toString();
				String[] arr = dateString.split(" ");

				// Formatting date to a required format
				String formattedDate = arr[0];
				formattedDate = formattedDate.substring(formattedDate.length() - 2, formattedDate.length()) + "-"
						+ formattedDate.substring(formattedDate.length() - 5, formattedDate.length() - 3) + "-"
						+ formattedDate.substring(0, 4);

						
				String voucherType = "Purchase";
				Party party = purchaseOrder.getParty();
				String partyName = party.getPartyName();
				String partyState = party.getParty_city().getState().getName();
				String partyGstNum = party.getGst();
				String itemId = purchaseItem.getModelNo();
				String salesItemId=purchaseItem.getDescription();
				Optional<SalesItem> salesItemObj=salesService.getSalesItemObjById(salesItemId);
				String clientName=salesItemObj.get().getSalesOrder().getParty().getPartyName();
				
				float qty = purchaseItem.getQuantity();
				double rate = purchaseItem.getUnitPrice();
				double amount = purchaseItem.getAmount();
				double taxableAount = purchaseItem.getAmount();
				double gstAmount = (taxableAount * gst)/100 ;
				gstAmount = (float) (Math.round(gstAmount * 100.0) / 100.0);
				//double netAmount =amount+ gstAmount;
				double netAmount = purchaseItem.getAmount();
				
				CellStyle declimalStyle = workbook.createCellStyle();
				XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
				declimalStyle.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
				declimalStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				
				Row row = editAccountSheet.createRow(rowCount++);
				
				Cell qtyCell = row.createCell(21);
				qtyCell.setCellStyle(declimalStyle);
				qtyCell.setCellValue(qty);
				
				Cell rateCell = row.createCell(23);
				rateCell.setCellStyle(declimalStyle);
				rateCell.setCellValue(rate);
				
				Cell amountCell = row.createCell(25);
				amountCell.setCellStyle(declimalStyle);
				amountCell.setCellValue(amount);
				
				Cell netAmountCell = row.createCell(27);
				netAmountCell.setCellStyle(declimalStyle);
				netAmountCell.setCellValue(netAmount);
				
				Cell taxableAmountCell = row.createCell(32);
				taxableAmountCell.setCellStyle(declimalStyle);
				taxableAmountCell.setCellValue(taxableAount);
				
				Cell gstAmountCell = row.createCell(34);
				gstAmountCell.setCellStyle(declimalStyle);
				gstAmountCell.setCellValue(gstAmount);
				
				
				row.createCell(0).setCellValue(formattedDate);
				row.createCell(1).setCellValue(invNum);
				row.createCell(4).setCellValue(voucherType);
				row.createCell(6).setCellValue(partyName);
				row.createCell(11).setCellValue(partyState);
				row.createCell(13).setCellValue(partyGstNum);
				row.createCell(14).setCellValue(partNum);
				row.createCell(15).setCellValue(itemName);
				row.createCell(16).setCellValue(clientName);
				row.createCell(19).setCellValue(unitName);
				row.createCell(20).setCellValue(hsn);
				row.createCell(33).setCellValue(gst);
				for (int i = 0; i < row.getLastCellNum(); i++) {
					editAccountSheet.autoSizeColumn(i);
				}
			}
				
			}
		}
		
}
