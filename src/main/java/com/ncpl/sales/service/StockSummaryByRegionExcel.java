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

public class StockSummaryByRegionExcel  extends AbstractXlsxView  {
	short VERTICAL_TOP = 0x0;
	short VERTICAL_TOP1 = 0x1;
	short VERTICAL_JUSTIFY = 0x2;
	short BORDER_THIN = 0x1;
	
	
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();

	// To read the message source from property file
	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
	MessageSource messageSource = (MessageSource) context.getBean("messageSource");

	DcLogoService logoService = new DcLogoService();

@SuppressWarnings({ "unchecked", "rawtypes" })
@Override
protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
		HttpServletResponse response) throws Exception {
	// TODO Auto-generated method stub
	Sheet editAccountSheet = workbook.createSheet("Stock summary By Region");
	editAccountSheet.setDefaultColumnWidth(9);
	Map<String, Object> stockRegionMapList = (Map<String, Object>) model.get("stockByRegion");
	//Map<String, Object> stockRegionMapList = (Map<String, Object>) stockMap.get("stockByRegion");
	
	CellStyle fourSideborderForValues = workbook.createCellStyle();
	fourSideborderForValues.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	fourSideborderForValues.setBorderLeft(BORDER_THIN);
	fourSideborderForValues.setBorderRight(BORDER_THIN);
	fourSideborderForValues.setBorderBottom(BORDER_THIN);
	fourSideborderForValues.setBorderTop(BORDER_THIN);
	
	CellStyle fourSideborderForValuesRightAligned = workbook.createCellStyle();
	XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
	fourSideborderForValuesRightAligned.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
	fourSideborderForValuesRightAligned.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
	fourSideborderForValuesRightAligned.setBorderLeft(BORDER_THIN);
	fourSideborderForValuesRightAligned.setBorderRight(BORDER_THIN);
	fourSideborderForValuesRightAligned.setBorderBottom(BORDER_THIN);
	fourSideborderForValuesRightAligned.setBorderTop(BORDER_THIN);
	
	CellStyle companyNamestyle = workbook.createCellStyle();
	companyNamestyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	Font cmpnyFont = workbook.createFont();
	cmpnyFont.setFontName("Calibri");
	cmpnyFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	cmpnyFont.setFontHeight((short) (7.5 * 35));
	companyNamestyle.setFont(cmpnyFont);

	editAccountSheet.addMergedRegion(new CellRangeAddress(1, // first row
			1, // last row
			0, // first column
			5 // last column
	));

	Row firstRow = editAccountSheet.createRow(1);
	Cell compnyName = firstRow.createCell(0);
	compnyName.setCellStyle(companyNamestyle);
	compnyName.setCellValue("Stock By Region");
	Row colHeader1 = editAccountSheet.createRow(4);
	editAccountSheet.addMergedRegion(new CellRangeAddress(4, // first row
			4, // last row
			0, // first column
			3 // last column
	));
	Cell headerCell1 = colHeader1.createCell(0);
	headerCell1.setCellStyle(fourSideborderForValues);
	headerCell1.setCellValue("Item Name");
	editAccountSheet.addMergedRegion(new CellRangeAddress(4, // first row
			4, // last row
			4, // first column
			5 // last column
	));
	Cell headerCell2 = colHeader1.createCell(4);
	headerCell2.setCellStyle(fourSideborderForValues);
	headerCell2.setCellValue("Remaining Qty");
	
	int rowCount = 5;
	for (String key : stockRegionMapList.keySet()) {
	    System.out.println(key + "=" + stockRegionMapList.get(key));
		Row valuesRow = editAccountSheet.createRow(rowCount);
        Map values = (Map) stockRegionMapList.get(key);
      
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				0, // first column
				3 // last column
		));
		
		
		Cell itemName =valuesRow.createCell(0);
		itemName.setCellStyle(fourSideborderForValues);
		itemName.setCellValue((String)values.get("itemId"));
		editAccountSheet.addMergedRegion(new CellRangeAddress(rowCount, // first row
				rowCount, // last row
				4, // first column
				5 // last column
		));
		
		Cell itemQty =valuesRow.createCell(4);
		itemQty.setCellStyle(fourSideborderForValuesRightAligned);
		itemQty.setCellValue((float)values.get("qty"));
		rowCount++;
		// valuesRow.createCell(0).setCellValue((String) s.get("particulars"));
		
	}
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