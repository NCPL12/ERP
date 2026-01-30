package com.ncpl.sales.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.SystemUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ncpl.common.Constants;
import com.ncpl.sales.config.LangConfig;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.SalesOrder;
@Service
@EnableScheduling
public class StockByCustomerEmailSchedular {
	@Autowired
	EmailService emailService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	PurchaseOrderService purchaseService;
	@Autowired
	SalesService salesService;
	@Autowired
	TdsService tdsService;
	@Autowired
	StockService stockService;
	
	
	
	
	
	static List list = null;
	
	//@Scheduled(cron = "0 15 11 ? * MON", zone="IST")
	//@Scheduled(cron = "0 */2 * ? * *", zone="IST")
	public void delivaryDateScheduler() throws IOException {
		System.out.println("Running......" +SystemUtils.getUserHome());
		
		Date todayDate = new Date();  
	    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
	    formatter.setTimeZone(TimeZone.getTimeZone("IST"));
	    String date= formatter.format(todayDate);  
	    
	    SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");
	    formatter2.setTimeZone(TimeZone.getTimeZone("IST"));
		Date filedate = new Date();
		String formattedDate3=formatter2.format(filedate);
	    
	    String fileName = "stock_custormerwise_as_on-"+formattedDate3 + ".xlsx";
		String filePath = Constants.FILE_LOCATION + File.separator + fileName;
		
	    System.out.println(date);  	
	    list = stockService.getStockForAllClient();
		if(list.size()!=0) {
	    	try {
	    		StockListByCustomerExcel.buildExcelDocument(list, filePath,itemMasterService,salesService);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			
	    	Map<String, Object> emailContents = null;
	    	emailContents=purchaseInfoEmail(filePath);
	    	emailService.sendEmailToServerForStockByCustomerEmailSchedular(emailContents);
		}
	}
	
	public Map<String, Object> purchaseInfoEmail(String filePath) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		formatter.setTimeZone(TimeZone.getTimeZone("IST"));
		Date created = new Date();
		String formattedDate1=formatter.format(created);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Stock By Customer "+formattedDate1); 
		emailContents.put("template","stock_by_customer.html"); 
		emailContents.put("to1", "surendra@ncpl.co");
		emailContents.put("to2", "store@ncpl.co");
		emailContents.put("cc1", "purchase@ncpl.co");
		emailContents.put("cc2", "prasadini@ncpl.co");
		emailContents.put("cc3", "ramsy@ncpl.co");
		emailContents.put("cc4", "vighneshwar@ncpl.co");
		emailContents.put("cc5", "aparna@ncpl.co");
		emailContents.put("cc6", "mani@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents; 
	
		
	}
}

class StockListByCustomerExcel{
	
		static short VERTICAL_TOP = 0x0;
		static short VERTICAL_JUSTIFY = 0x2;
		static short BORDER_THIN = 0x1;
	// To read the message source from property file
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LangConfig.class);
		MessageSource messageSource = (MessageSource) context.getBean("messageSource");
		
	public static void buildExcelDocument(List list, String filePath, ItemMasterService itemMasterService, SalesService salesService) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet editAccountSheet = workbook.createSheet("Stock");
        editAccountSheet.setDefaultColumnWidth(11);
		
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
		
		
		CellStyle itemHeaderStyle = workbook.createCellStyle();
		itemHeaderStyle.setWrapText(true);
		itemHeaderStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		itemHeaderStyle.setFont(fontColumn);
		itemHeaderStyle.setBorderBottom(BORDER_THIN);
		itemHeaderStyle.setBorderTop(BORDER_THIN);
		itemHeaderStyle.setBorderRight(BORDER_THIN);
		itemHeaderStyle.setBorderLeft(BORDER_THIN);
		
		
		Cell itemHeader = header.createCell(0);
		itemHeader.setCellStyle(itemHeaderStyle);
		itemHeader.setCellValue("Client Name");
		
		Cell poNumCell = header.createCell(1);
		poNumCell.setCellStyle(itemHeaderStyle);
		poNumCell.setCellValue("Value");
		
		
		int rowLastItemCount=0;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
		populateSalesRecords(list, editAccountSheet, workbook,itemMasterService,salesService);
		 /* Create JFreeChart object that will hold the Pie Chart Data */
        DefaultPieDataset my_pie_chart_data = new DefaultPieDataset();
        /* We have to get the input data into DefaultPieDataset object */
        /* So, we iterate over the rows and cells */
        /* Create an Iterator object */
        Iterator<Row> rowIterator = editAccountSheet.iterator(); 
        /* Loop through worksheet data and populate Pie Chart Dataset */
        String chart_label="a";
        double chart_data=0;            
        while(rowIterator.hasNext()) {
                //Read Rows from Excel document
                Row row = rowIterator.next(); 
                System.out.println(row.getRowNum());
                if(row.getRowNum()!=0 && row.getRowNum()!=11) {
                //Read cells in Rows and get chart data
                Iterator<Cell> cellIterator = row.cellIterator();
                        while(cellIterator.hasNext()) {
                                Cell cell = cellIterator.next(); 
                                switch(cell.getCellType()) { 
                                case Cell.CELL_TYPE_NUMERIC:
                                        chart_data=cell.getNumericCellValue();
                                        break;
                                case Cell.CELL_TYPE_STRING:
                                        chart_label=cell.getStringCellValue();
                                        break;
                                }
                        }
               
        /* Add data to the data set */          
        my_pie_chart_data.setValue(chart_label+ "[" +formatLakh(chart_data)+"]",chart_data);
         }
        
        }               
        /* Create a logical chart object with the chart data collected */
        JFreeChart myPieChart=ChartFactory.createPieChart("Stock By Customer",my_pie_chart_data,true,true,false);
        /* Specify the height and width of the Pie Chart */
        int width=740; /* Width of the chart */
        int height=580; /* Height of the chart */
        float quality=1; /* Quality factor */
        /* We don't want to create an intermediate file. So, we create a byte array output stream 
        and byte array input stream
        And we pass the chart data directly to input stream through this */             
        /* Write chart as JPG to Output Stream */
        ByteArrayOutputStream chart_out = new ByteArrayOutputStream();          
        ChartUtilities.writeChartAsJPEG(chart_out,quality,myPieChart,width,height);
        /* We now read from the output stream and frame the input chart data */
        /* We don't need InputStream, as it is required only to convert the output chart to byte array */
        /* We can directly use toByteArray() method to get the data in bytes */
        /* Add picture to workbook */
        int my_picture_id = workbook.addPicture(chart_out.toByteArray(), Workbook.PICTURE_TYPE_JPEG);                
        /* Close the output stream */
        chart_out.close();
        /* Create the drawing container */
        Drawing drawing = editAccountSheet.createDrawingPatriarch();
        /* Create an anchor point */
        ClientAnchor my_anchor = new XSSFClientAnchor();
        /* Define top left corner, and we can resize picture suitable from there */
        my_anchor.setCol1(4);
        my_anchor.setRow1(1);
        /* Invoke createPicture and pass the anchor point and ID */
        XSSFPicture  my_picture = (XSSFPicture) drawing.createPicture(my_anchor, my_picture_id);
        /* Call resize method, which resizes the image */
        my_picture.resize();
		
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        System.out.println("Daily Delivery Date Report Sheets Has been Created successfully!");
	}
	
	@SuppressWarnings("rawtypes")
	private static void populateSalesRecords(List list, Sheet editAccountSheet, Workbook workbook,
			ItemMasterService itemMasterService, SalesService salesService) {
		
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
		
		CellStyle threeSideborderRightAllign = workbook.createCellStyle();
		XSSFDataFormat lastTaxstyleformat = (XSSFDataFormat) workbook.createDataFormat();
		threeSideborderRightAllign.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
		threeSideborderRightAllign.setWrapText(true);
		threeSideborderRightAllign.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderRightAllign.setBorderLeft(BORDER_THIN);
		threeSideborderRightAllign.setBorderRight(BORDER_THIN);
		threeSideborderRightAllign.setBorderBottom(BORDER_THIN);
		threeSideborderRightAllign.setBorderTop(BORDER_THIN);
		threeSideborderRightAllign.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		threeSideborderRightAllign.setFont(itemListFont);
		
		CellStyle threeSideborderBold = workbook.createCellStyle();
		threeSideborderBold.setDataFormat(lastTaxstyleformat.getFormat("#,###.00"));
		threeSideborderBold.setWrapText(true);
		threeSideborderBold.setVerticalAlignment((short) (VERTICAL_JUSTIFY));
		threeSideborderBold.setBorderLeft(BORDER_THIN);
		threeSideborderBold.setBorderRight(BORDER_THIN);
		threeSideborderBold.setBorderBottom(BORDER_THIN);
		threeSideborderBold.setBorderTop(BORDER_THIN);
		threeSideborderBold.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		Font fontColumns = workbook.createFont();
		fontColumns.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		fontColumns.setFontHeightInPoints((short)8);
		threeSideborderBold.setFont(fontColumns);

		int rowCount =1;
		for (int i=0;i<list.size()-1;i++) {
			
			
			String clientName = list.get(i).toString().split("=")[0];
			String qty=list.get(i).toString().split("=")[1];
			
			float quantity=Float.parseFloat(qty);
			
			
			Row row = editAccountSheet.createRow(rowCount);
			
			Cell slNoCell = row.createCell(0);
			slNoCell.setCellStyle(threeSideborder);
			slNoCell.setCellValue(clientName);
			
			Cell soNoCell = row.createCell(1);
			soNoCell.setCellStyle(threeSideborderRightAllign);
			soNoCell.setCellValue(quantity);
			
			
			
			
			rowCount++;
			
			
		}
		
		String total= list.get(10).toString().split("=")[1].split("}")[0];
		float totalQty=Float.parseFloat(total);
		Row row1 = editAccountSheet.createRow(rowCount);
		
		Cell TotalCell = row1.createCell(0);
		TotalCell.setCellStyle(threeSideborderBold);
		TotalCell.setCellValue("Total Stock");
		
		Cell totalCell1 = row1.createCell(1);
		totalCell1.setCellStyle(threeSideborderBold);
		totalCell1.setCellValue(totalQty);
		
		
		int rowLastItemCount=rowCount;
		setBordersToMergedCells(workbook, editAccountSheet, rowLastItemCount);
	}
	
	private static void setBordersToMergedCells(Workbook workBook, Sheet sheet, int rowLastItemCount) {
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
	
	private static String formatLakh(double d) {
	    String s = String.format(Locale.UK, "%1.2f", Math.abs(d));
	    s = s.replaceAll("(.+)(...\\...)", "$1,$2");
	    while (s.matches("\\d{3,},.+")) {
	        s = s.replaceAll("(\\d+)(\\d{2},.+)", "$1,$2");
	    }
	    return d < 0 ? ("-" + s) : s;
	}
}
