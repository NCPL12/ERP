package com.ncpl.sales.service;

import java.util.List;
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
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Stock;

public class StockReportByDateExcel extends AbstractXlsxView{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		
		Sheet editAccountSheet = workbook.createSheet("Stock summary By Date");
		editAccountSheet.setDefaultColumnWidth(9);
		//editAccountSheet.autoSizeColumn(11);
		
		// Generating File Name
		String fileName =  "Stock_summary_date.xlsx";
		// set excel file name
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		
		CellStyle companyNamestyle = workbook.createCellStyle();
		companyNamestyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		Font cmpnyFont = workbook.createFont();
		cmpnyFont.setFontName("Calibri");
		cmpnyFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		cmpnyFont.setFontHeight((short) (7.5 * 35));
		companyNamestyle.setFont(cmpnyFont);
		
		
		
		editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
				1, // last row
				0, // first column
				5// last column
		));

		Row firstRow = editAccountSheet.createRow(1);
		Cell compnyName = firstRow.createCell(0);
		compnyName.setCellStyle(companyNamestyle);
		compnyName.setCellValue("Stock Summary By Date");
		
		List<Stock> stockList = (List) model.get("stockData");
		//up=unitPrice sp =sell Price
		Row headerColumns = editAccountSheet.createRow(2);
		Cell modelHeader = headerColumns.createCell(0);
		modelHeader.setCellValue("Model");
		
		Cell  descHeader = headerColumns.createCell(1);
		descHeader.setCellValue("Description");
		
		Cell qtyHeader = headerColumns.createCell(2);
		qtyHeader.setCellValue("Quantity");
		
		Cell costHeader1 = headerColumns.createCell(3);
		costHeader1.setCellValue("Cost Price");
		
		Cell ttlcostHeader = headerColumns.createCell(4);
		ttlcostHeader.setCellValue("Total Cost");
		
		int rowCount =3;
		for (Stock stock : stockList) {
			if(stock.getQuantity() > 0) {
				CellStyle declimalStyle = workbook.createCellStyle();
				XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
				declimalStyle.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
				
				
				Row columnDetails = editAccountSheet.createRow(rowCount);
				
				Cell qtyCell = columnDetails.createCell(2);
				qtyCell.setCellStyle(declimalStyle);
				qtyCell.setCellValue(stock.getQuantity());
				
				ItemMaster itemMaster = stock.getItemMaster();
				columnDetails.createCell(0).setCellValue(itemMaster.getModel());
				columnDetails.createCell(1).setCellValue(itemMaster.getItemName());
				//columnDetails.createCell(2).setCellValue(stock.getQuantity());
				columnDetails.createCell(3).setCellValue(itemMaster.getPrefferedCost());
				
				columnDetails.createCell(4).setCellValue(Math.round(stock.getQuantity() * itemMaster.getPrefferedCost()));
				
				rowCount++;
			}
			
		}
		
	}

}
