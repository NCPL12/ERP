package com.ncpl.sales.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesItemRepo;
import com.ncpl.sales.repository.SalesRepo;

@Service
public class SOUploadService {
	 @Autowired
	    private SalesRepo salesOrderRepository;
	 @Autowired
	 	private PartyService partyService;
	 @Autowired
	 	private SalesItemRepo salesItemRepository;
	 @Autowired
	 	PartyRepo partyrepo;
	 @Autowired
	 	SalesService salesService;
	 @Autowired
	 	ItemMasterService itemService;

	  @SuppressWarnings("resource")
	public List<String> parseExcelFile(MultipartFile file) throws Exception {
	        List<String> errors = new ArrayList<>();
	        
	        Workbook workbook;
	        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    	 try (InputStream inputStream = file.getInputStream()){
	    			 if (file.getOriginalFilename().endsWith(".xls")) {
	    	                workbook = new HSSFWorkbook(inputStream); // For older Excel files (.xls)
	    	            } else if (file.getOriginalFilename().endsWith(".xlsx")) {
	    	                workbook = new XSSFWorkbook(inputStream); // For newer Excel files (.xlsx)
	    	            } else {
	    	                throw new IllegalArgumentException("Invalid file format. Only .xls and .xlsx files are supported.");
	    	            }
	    			 Sheet sheet = workbook.getSheetAt(0);
	    	            int rowNum = 2; // Data starts from row 3

	    	           
	    	                Row row = sheet.getRow(rowNum);
	    	               // if (row == null) continue;

	    	                

	    	                SalesOrder salesOrder = null;
	    	                List<SalesItem> salesItems = new ArrayList<>();

	    	                // ✅ Sales Order (Header) Validations
	    	                String clientPoNumber = getStringCellValue(row.getCell(0));
	    	                Optional<SalesOrder> salesOrderObj = salesOrderRepository.getSalesOrderByClientPoNo(clientPoNumber);
	    	                if (clientPoNumber.isEmpty()) {
	    	                	errors.add("Client PO Number is mandatory.");
	    	                }
	    	                else if (salesOrderObj.isPresent()) {
	    	                	//errors.add("Duplicate Client PO Number: " + clientPoNumber);
	    	                	salesOrder = salesOrderObj.get();
	    	                }else {
	    	                	salesOrder = new SalesOrder();
	    	                	salesOrder.setId("");
	    	                }
	    	                salesOrder.setClientPoNumber(clientPoNumber);
	    	                Date clientPoDate = getDateCellValue(row.getCell(1), errors, rowNum + 1, "Client PO Date");
	    	                salesOrder.setClientPoDate(clientPoDate);
	    	                
	    	                String partyName=getStringCellValue(row.getCell(2));
	     	                Party party=partyrepo.findByName(partyName);
	     	                
	     	               if (party == null) {
	     	            	  errors.add("Party is invalid or does not exist.");
	    	                } else {
	    	                	 salesOrder.setParty(party);
	    	                }
	     	               
	    	                salesOrder.setRegion(getStringCellValue(row.getCell(3)));
	    	                Integer gstRate = getIntCellValue(row.getCell(4), errors, rowNum + 1, "Tax Rate");

	    	                if (gstRate == null) {
	    	                    // Skip setting GST if it's null to avoid NullPointerException
	    	                	salesOrder.setGstRate(0);// Default to 0 or handle accordingly
	    	                } else if (gstRate < 0) {
	    	                    errors.add("Row " + (rowNum + 1) + ": Tax Rate cannot be negative.");
	    	                } else {
	    	                    salesOrder.setGstRate(gstRate);
	    	                }
	    	                
	    	               // Date projectClosureDate = getDateCellValue(row.getCell(5), errors, rowNum, dateFormat);
	    	                //if (projectClosureDate == null) salesOrder.setProjectClosureDate(null);
	    	              //  salesOrder.setProjectClosureDate(projectClosureDate);
	    	                salesOrder.setModeOfPayment(getStringCellValue(row.getCell(5)));
	    	                salesOrder.setJurisdiction(getStringCellValue(row.getCell(6)));
	    	                salesOrder.setFreight(getStringCellValue(row.getCell(7)));
	    	                salesOrder.setDelivery(getStringCellValue(row.getCell(8)));
	    	                salesOrder.setWarranty(getStringCellValue(row.getCell(9)));
	    	                salesOrder.setOtherTermsAndConditions(getStringCellValue(row.getCell(10)));
	    	                rowNum=6;
	    	                float total=0;
	    	                float gst=0;
	    	                float grandTotal=0;
	    	                
	    	                while (rowNum <= sheet.getLastRowNum()) {
	    	                	 Row itemRow = sheet.getRow(rowNum);
	    	                	 
	    	                	 List<String> rowErrors = new ArrayList<>();
		    	                // ✅ Sales Items (Details) Validations
		    	                SalesItem salesItem = new SalesItem();
	
		    	                salesItem.setSlNo(getStringCellValue(itemRow.getCell(0)));
		    	                String description=getStringCellValue(itemRow.getCell(1));
		    	                if (description.isEmpty()) rowErrors.add("Description is mandatory.");
		    	                salesItem.setDescription(description);
		    	                salesItem.setModelNo(getStringCellValue(itemRow.getCell(5)));
	
		    	                salesItem.setHsnCode(getStringCellValue(itemRow.getCell(6)));
		    	                salesItem.setServicehsnCode(getStringCellValue(itemRow.getCell(7)));
	
		    	                Float qty = getFloatCellValue(itemRow.getCell(8), rowErrors, rowNum + 1, "Quantity");
		    	                if (qty == null) {
		    	                	qty=(float) 0.0;
		    	                	salesItem.setQuantity(0);
		    	                }
		    	                else if( qty < 0) {
		    	                	rowErrors.add("Row " + (rowNum + 1) + ": Quantity cannot be negative.");
		    	                }else {
		    	                	salesItem.setQuantity(qty);
		    	                }
		    	                Units unitObj=itemService.getUnitsByName(getStringCellValue(itemRow.getCell(9)));
		    	                
		    	                if (unitObj == null) {
		    	                    rowErrors.add("Unit is invalid or does not exist.");
		    	                } else {
		    	                	salesItem.setItem_units(unitObj);
		    	                }
	
		    	                
		    	                Float unitPrice = getFloatCellValue(itemRow.getCell(10), rowErrors, rowNum + 1, "Unit Price");
		    	                if (unitPrice == null) {
		    	                	unitPrice=(float) 0.0;
		    	                	salesItem.setUnitPrice(0);
		    	                }
		    	                else if( unitPrice < 0) {
		    	                	rowErrors.add("Row " + (rowNum + 1) + ": Unit Price cannot be negative.");
		    	                }else {
		    	                	salesItem.setUnitPrice(unitPrice);
		    	                }
		    	                
		    	                Float servicePrice = getFloatCellValue(itemRow.getCell(11), rowErrors, rowNum + 1, "Service Price");
		    	                if (servicePrice == null) {
		    	                	servicePrice=(float) 0.0;
		    	                	salesItem.setServicePrice(0);
		    	                }
		    	                else if( servicePrice < 0) {
		    	                	rowErrors.add("Row " + (rowNum + 1) + ": Service Price cannot be negative.");
		    	                }else {
		    	                	salesItem.setServicePrice(servicePrice);
		    	                }
		    	                Float amount=(qty*unitPrice)+(qty*servicePrice);
		    	                salesItem.setAmount(amount);
		    	                total=total+amount;
		    	                
		    	                // ✅ If errors found, add to error list and skip saving
		    	                if (!rowErrors.isEmpty()) {
		    	                    errors.add("Row " + (rowNum + 1) + ": " + String.join(" | ", rowErrors));
		    	                    rowNum++;
		    	                    continue;
		    	                }
	
		    	                // ✅ Save to Database
		    	                salesItem.setSalesOrder(salesOrder);
		    	                salesItems.add(salesItem);
		    	                
		    	                rowNum++;
	    	                }
	    	                
	    	                
	    	                
	    	                if (salesOrder.getId() == null || salesOrder.getId().isEmpty()) {
	    	                	salesOrder.setTotal(total);
	    	                	if(party!=null) {
			    	                if(party.getId()!="C1143"){
			    	                	//float gstRateValue=gstRate/100;
			    	      		      gst=gstRate*total/100;
			    	      		    gst =(float) ( Math.round(gst * 100.0) / 100.0);
			    	      		    }else{
			    	      		    	gst=0;
			    	      		    }
		    	                }else {
		    	                	gst=0;
		    	                }
		    	                salesOrder.setGst(gst);
	    	                	grandTotal=total+gst;
		    	                salesOrder.setGrandTotal(Math.round(grandTotal * 100.0) / 100.0);
	    	                	salesOrder.setItems(salesItems);
	    	                }else {
	    	                	total=(float) (total+salesOrder.getTotal());
	    	                	salesOrder.setTotal(total);
	    	                	if(party!=null) {
			    	                if(party.getId()!="C1143"){
			    	                	//float gstRateValue=gstRate/100;
			    	      		      gst=gstRate*total/100;
			    	      		    gst =(float) ( Math.round(gst * 100.0) / 100.0);
			    	      		    }else{
			    	      		    	gst=0;
			    	      		    }
		    	                }else {
		    	                	gst=0;
		    	                }
		    	                salesOrder.setGst(gst);
	    	                	grandTotal=total+gst;
		    	                salesOrder.setGrandTotal(Math.round(grandTotal * 100.0) / 100.0);
	    	                	 for (SalesItem item : salesItems) {
	    	                         item.setSalesOrder(salesOrder);
	    	                         salesItemRepository.save(item);
	    	                     }
	    	                }
	    	                
	    	               
	    	                if (errors.isEmpty()) {
	    	                    salesOrderRepository.save(salesOrder);
	    	                }

	    	            } catch (IOException e) {
	    	                errors.add("Error reading Excel file: " + e.getMessage());
	    	            }
	         return errors;
	     }
	  
	  private String getStringCellValue(Cell cell) {
	        if (cell == null) return "";
	        
	        switch (cell.getCellType()) {
	            case Cell.CELL_TYPE_STRING:
	                return cell.getStringCellValue().trim();
	            case Cell.CELL_TYPE_NUMERIC:
	                if (DateUtil.isCellDateFormatted(cell)) {
	                    // Convert date cells to string format (optional)
	                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	                    return dateFormat.format(cell.getDateCellValue());
	                } else {
	                    return String.valueOf((long) cell.getNumericCellValue()); // Convert to String if numeric
	                }
	            case Cell.CELL_TYPE_BOOLEAN:
	                return String.valueOf(cell.getBooleanCellValue());
	            case Cell.CELL_TYPE_FORMULA:
	                return cell.getCellFormula(); // Return formula as a string
	            default:
	                return "";
	        }
	    }




	     private Integer getIntCellValue(Cell cell, List<String> errors, int rowNum, String columnName) {
	    	    if (cell == null) {
	    	        errors.add("Row " + rowNum + ": " + columnName + " is mandatory.");
	    	        return null;
	    	    }

	    	    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	    	        return (int) cell.getNumericCellValue(); // Convert to Integer
	    	    } else {
	    	        errors.add("Row " + rowNum + ": " + columnName + " must be a numeric value.");
	    	        return null;
	    	    }
	    	}

	     private Long getLongCellValue(Cell cell) {
	         return (cell == null) ? null : (long) cell.getNumericCellValue();
	     }

	     private Double getDoubleCellValue(Cell cell) {
	    	 if (cell == null) return null;

	    	    switch (cell.getCellType()) {
	    	        case Cell.CELL_TYPE_NUMERIC:
	    	            return  cell.getNumericCellValue(); // Direct numeric value

	    	        case Cell.CELL_TYPE_STRING:
	    	            try {
	    	                return Double.parseDouble(cell.getStringCellValue().trim()); // Convert text to number
	    	            } catch (NumberFormatException e) {
	    	                return null; // Invalid number format
	    	            }

	    	        default:
	    	            return null;
	    	    }
	    	}
	     private Float getFloatCellValue(Cell cell, List<String> errors, int rowNum, String columnName) {
	    	    if (cell == null) {
	    	        errors.add("Row " + rowNum + ": " + columnName + " is mandatory.");
	    	        return null;
	    	    }

	    	    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	    	        return (float) cell.getNumericCellValue(); // Convert to Float
	    	    } else {
	    	        errors.add("Row " + rowNum + ": " + columnName + " must be a numeric value.");
	    	        return null;
	    	    }
	    	}
	     private Date getDateCellValue(Cell cell, List<String> errors, int rowNum, String columnName) {
	    	    if (cell == null) {
	    	        errors.add("Row " + rowNum + ": " + columnName + " is mandatory.");
	    	        return null;
	    	    }

	    	    if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && DateUtil.isCellDateFormatted(cell)) {
	    	        return new Date(cell.getDateCellValue().getTime()); // Convert java.util.Date to java.sql.Date
	    	    } else {
	    	        errors.add("Row " + rowNum + ": " + columnName + " must be a valid date (MM/DD/YYYY).");
	    	        return null;
	    	    }
	    	}

	 }
