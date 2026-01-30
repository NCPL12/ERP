package com.ncpl.sales.service;

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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.Stock;

public class GrnReportByDateExcel extends AbstractXlsxView{
	FileNameGenerator fileNameGenerator = new FileNameGenerator();

	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");
	

	
	
	
	 ExcelLogoService logoService = new ExcelLogoService();
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void buildExcelDocument(Map  model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String fileName = fileNameGenerator.generateFileNameAsDate() + "grn_by_date.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		List<GrnItems> grnlist = (List<GrnItems>) model.get("grnByRegion");
		
		PurchaseItemService poItemService=(PurchaseItemService) request.getAttribute("purchaseItemService");
		ItemMasterService itemService=(ItemMasterService) request.getAttribute("itemMasterService");
		SalesService soService=(SalesService) request.getAttribute("salesService");
		GrnService grnService=(GrnService) request.getAttribute("grnService");
		
		Sheet itemsReportSheet = workbook.createSheet("GRN Report");
		itemsReportSheet.setDefaultColumnWidth(23);

		// create style for header cells
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Calibri");
		style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		font.setColor(HSSFColor.WHITE.index);
		style.setFont(font);

		Row secondRow = itemsReportSheet.createRow(1);
		itemsReportSheet.addMergedRegion(new CellRangeAddress(1, // first row
				2, // last row
				0, // first column
				6 // last column
		));

		Cell headingCell = secondRow.createCell(0);
		headingCell.setCellValue("GRN List By Date");
		CellStyle mergestyle = workbook.createCellStyle();
		mergestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		Font headingFont = workbook.createFont();
		headingFont.setFontName("Calibri");
		headingFont.setFontHeight((short) (15.5 * 20));
		headingFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		mergestyle.setFont(headingFont);
		headingCell.setCellStyle(mergestyle);

		// create header row
		Row header = itemsReportSheet.createRow(3);
	   
     	header.createCell(0).setCellValue("Model No");
		
		header.createCell(1).setCellValue("Description");
		header.createCell(2).setCellValue("Recieved Qty");
		header.createCell(3).setCellValue("Units");
		header.createCell(4).setCellValue("Unit Price");
		header.createCell(5).setCellValue("Amount");
		header.createCell(6).setCellValue("GRN No");


		populateStocksRecords(grnlist, itemsReportSheet, workbook,poItemService,itemService,soService,grnService);

	}
	@SuppressWarnings({ "unused", "rawtypes" })
	private void populateStocksRecords(List<GrnItems> grnlist, Sheet itemsReportSheet, Workbook workbook, PurchaseItemService poItemService, ItemMasterService itemService, SalesService soService, GrnService grnService) {
		// TODO Auto-generated method stub
		int rowCount =4;
		for (GrnItems grnItem : grnlist) {
			String description=grnItem.getDescription();
			System.out.println(description);
			Optional<PurchaseItem> poItem=poItemService.getPurchaseItemByPoItemId(Integer.parseInt(description));
			if (poItem.isPresent()) {
				Optional<ItemMaster> itemObj=itemService.getItemById(poItem.get().getModelNo());
		
			
			CellStyle declimalStyle = workbook.createCellStyle();
			XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
			declimalStyle.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
			
			
			
				// for(int i=0;i<glcList.size()-2;i++){
				Row row = itemsReportSheet.createRow(rowCount++);
				
				Cell oldQtyCell = row.createCell(2);
				oldQtyCell.setCellStyle(declimalStyle);
				oldQtyCell.setCellValue(grnItem.getReceivedQuantity());
				
				Cell newQtyCell = row.createCell(4);
				newQtyCell.setCellStyle(declimalStyle);
				newQtyCell.setCellValue(grnItem.getUnitPrice());
				
				Cell amountCell = row.createCell(5);
				amountCell.setCellStyle(declimalStyle);
				amountCell.setCellValue(grnItem.getAmount());
				
				row.createCell(0).setCellValue(itemObj.get().getModel());
				row.createCell(1).setCellValue(poItem.get().getPoDescription());
				//row.createCell(2).setCellValue(grnItem.getReceivedQuantity());
				row.createCell(3).setCellValue(itemObj.get().getItem_units().getName());
				//row.createCell(5).setCellValue(grnItem.getUnitPrice());
				//row.createCell(5).setCellValue(grnItem.getAmount());
				row.createCell(6).setCellValue(grnItem.getGrn().getGrnId());
			}
				
				
	}
}
}
