package com.ncpl.sales.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.ncpl.sales.service.SalesOrderAuditService;

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
	@Autowired
	 	SalesOrderAuditService auditService;

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

	    	                //  Sales Order (Header) Validations
	    	                String clientPoNumber = getStringCellValue(row.getCell(0));
	    	                //Optional<SalesOrder> salesOrderObj = salesOrderRepository.getSalesOrderByClientPoNo(clientPoNumber);
	    	                if (clientPoNumber.isEmpty()) {
	    	                	errors.add("Client PO Number is mandatory.");
	    	                	workbook.close();
	    	                	return errors;
	    	                }
	    	                else if (salesOrderRepository.existsByClientPoNumberIgnoreCase(clientPoNumber)) {
	    	                	errors.add("Duplicate Client PO Number: " + clientPoNumber + " is already uploaded. Duplicate upload is not allowed.");
	    	                	workbook.close();
	    	                	return errors;
	    	                	//salesOrder = salesOrderObj.get();
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
	    	                	 // SO upload sheet has no address columns: default billing and shipping to the party's main address (party id)
	    	                	 salesOrder.setBillingAddress(party.getId());
	    	                	 salesOrder.setShippingAddress(party.getId());
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
	    	                // Old layout: Mode of Payment in F. New layout: shifted one column right (G). Detected, not assumed.
	    	                int pc = findPaymentColumn(sheet, row);
	    	                if (pc < 0) {
	    	                	errors.add("Unrecognised layout for Payment/Jurisdiction/Freight/Delivery/Warranty/Other Terms (columns F-L). Please use the standard template.");
	    	                	workbook.close();
	    	                	return errors;
	    	                }
	    	                salesOrder.setModeOfPayment(getStringCellValue(row.getCell(pc)));
	    	                salesOrder.setJurisdiction(getStringCellValue(row.getCell(pc + 1)));
	    	                salesOrder.setFreight(getStringCellValue(row.getCell(pc + 2)));
	    	                salesOrder.setDelivery(getStringCellValue(row.getCell(pc + 3)));
	    	                salesOrder.setWarranty(getStringCellValue(row.getCell(pc + 4)));
	    	                salesOrder.setOtherTermsAndConditions(getStringCellValue(row.getCell(pc + 5)));
	    	                rowNum=6;
	    	                float total=0;
	    	                float gst=0;
	    	                float grandTotal=0;
	    	                
	    	                Map<String, Units> unitCache = new HashMap<>();
	    	                while (rowNum <= sheet.getLastRowNum()) {
	    	                	 Row itemRow = sheet.getRow(rowNum);
	    	                	 if (itemRow == null) { rowNum++; continue; }
	    	                	 
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
	
		    	                // Heading rows (unit = "Heading") may have blank Quantity / Unit Price / Service Price: no error, treated as 0
		    	                boolean isHeading = "Heading".equalsIgnoreCase(getStringCellValue(itemRow.getCell(9)));
		    	                //Float qty = getFloatCellValue(itemRow.getCell(8), rowErrors, rowNum + 1, "Quantity");
		    	                Float qty = getFloatCellValue(itemRow.getCell(8), isHeading ? new ArrayList<String>() : rowErrors, rowNum + 1, "Quantity");
		    	                if (qty == null) {
		    	                	qty=(float) 0.0;
		    	                	salesItem.setQuantity(0);
		    	                }
		    	                else if( qty < 0) {
		    	                	rowErrors.add("Row " + (rowNum + 1) + ": Quantity cannot be negative.");
		    	                }else {
		    	                	salesItem.setQuantity(qty);
		    	                }
		    	                String unitName = getStringCellValue(itemRow.getCell(9));
		    	                Units unitObj;
		    	                if (unitCache.containsKey(unitName)) {
		    	                    unitObj = unitCache.get(unitName);
		    	                } else {
		    	                    //unitObj = itemService.getUnitsByName(unitName);
		    	                    unitObj = findUnitByName(unitName);
		    	                    unitCache.put(unitName, unitObj);
		    	                }
		    	                
		    	                if (unitObj == null) {
		    	                    rowErrors.add("Unit is invalid or does not exist.");
		    	                } else {
		    	                	salesItem.setItem_units(unitObj);
		    	                	salesItem.setUnit(String.valueOf(unitObj.getId()));
		    	                }
	
		    	                
		    	                //Float unitPrice = getFloatCellValue(itemRow.getCell(10), rowErrors, rowNum + 1, "Unit Price");
		    	                Float unitPrice = getFloatCellValue(itemRow.getCell(10), isHeading ? new ArrayList<String>() : rowErrors, rowNum + 1, "Unit Price");
		    	                if (unitPrice == null) {
		    	                	unitPrice=(float) 0.0;
		    	                	salesItem.setUnitPrice(0);
		    	                }
		    	                else if( unitPrice < 0) {
		    	                	rowErrors.add("Row " + (rowNum + 1) + ": Unit Price cannot be negative.");
		    	                }else {
		    	                	salesItem.setUnitPrice(unitPrice);
		    	                }
		    	                
		    	                //Float servicePrice = getFloatCellValue(itemRow.getCell(11), rowErrors, rowNum + 1, "Service Price");
		    	                Float servicePrice = getFloatCellValue(itemRow.getCell(11), isHeading ? new ArrayList<String>() : rowErrors, rowNum + 1, "Service Price");
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
	    	                     }
	    	                	 salesItemRepository.saveAll(salesItems);
	    	                }
	    	                
	    	               
	    	                if (errors.isEmpty()) {
	    	                	// Use SalesService for proper audit logging instead of direct repository save
	    	                	if (salesOrder.getId() == null || salesOrder.getId().isEmpty()) {
	    	                		// New sales order - use savesales method which includes audit logging
	    	                		salesService.savesales(salesOrder, party.getId());
	    	                	} else {
	    	                		// Existing sales order - use update method which includes audit logging
	    	                		salesOrderRepository.save(salesOrder);
	    	                		// Log audit for the update
	    	                		try {
	    	                			auditService.logSalesOrderUpdate(
	    	                				salesOrder, // Both old and new are the same for upload
	    	                				salesOrder, 
	    	                				"system", // Performed by system during upload
	    	                				null
	    	                			);
	    	                		} catch (Exception e) {
	    	                			System.err.println("Error logging audit for sales order upload: " + e.getMessage());
	    	                			e.printStackTrace();
	    	                		}
	    	                	}
	    	                }

	    	            } catch (IOException e) {
	    	                errors.add("Error reading Excel file: " + e.getMessage());
	    	            }
	         return errors;
	     }
	  
	  // Expected header labels, in order, for the six term columns (Payment ... Other Terms)
	  private static final String[] TERM_HEADERS = {"payment", "jurisdiction", "freight", "delivery", "warranty", "other"};

	  // Finds the "Mode of Payment" column so both header layouts work (F = old layout, G = shifted layout)
	  // Returns 5 (F) or 6 (G), or -1 if the layout cannot be determined (caller must reject the file)
	  private int findPaymentColumn(Sheet sheet, Row dataRow) {
	        // 1) header text: look in the first two rows for all six labels in sequence starting at F or G
	        for (int r = 0; r <= 1; r++) {
	            Row header = sheet.getRow(r);
	            if (header == null) continue;
	            for (int start : new int[]{5, 6}) {
	                if (headersMatchAt(header, start)) {
	                    return start;
	                }
	            }
	        }
	        // 2) no usable header: use the far edge of the block, not F/G emptiness.
	        //    Old layout ends at K (L empty); shifted layout ends at L (F unused).
	        boolean fFilled = !getStringCellValue(dataRow.getCell(5)).isEmpty();
	        boolean lFilled = !getStringCellValue(dataRow.getCell(11)).isEmpty();
	        if (lFilled && !fFilled) {
	            return 6; // shifted layout
	        }
	        if (!lFilled) {
	            return 5; // original layout (F may legitimately be blank)
	        }
	        // 3) F and L both filled: ambiguous, reject
	        return -1;
	    }

	  // True if the six term headers appear consecutively starting at column 'start'
	  private boolean headersMatchAt(Row header, int start) {
	        for (int i = 0; i < TERM_HEADERS.length; i++) {
	            String h = getStringCellValue(header.getCell(start + i)).toLowerCase();
	            if (!h.contains(TERM_HEADERS[i])) {
	                return false;
	            }
	        }
	        return true;
	    }

	  // Unit lookup that tolerates Excel typos: hidden non-breaking spaces, extra spaces, and "Nos" vs "Nos."
	  private Units findUnitByName(String rawName) {
	        if (rawName == null) return null;
	        String name = rawName.replace('\u00A0', ' ').trim().replaceAll("\\s+", " ");
	        Units unit = itemService.getUnitsByName(name);
	        if (unit == null && name.endsWith(".")) {
	            unit = itemService.getUnitsByName(name.substring(0, name.length() - 1).trim()); // "Nos." -> "Nos"
	        }
	        if (unit == null && !name.endsWith(".")) {
	            unit = itemService.getUnitsByName(name + "."); // "Nos" -> "Nos."
	        }
	        return unit;
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
	                    double numVal = cell.getNumericCellValue();
	                    if (numVal == Math.rint(numVal)) {
	                        return String.valueOf((long) numVal); // whole number: 6 stays "6"
	                    }
	                    return java.math.BigDecimal.valueOf(numVal).stripTrailingZeros().toPlainString(); // 6.1 stays "6.1"
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