package com.ncpl.sales.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderAudit;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesItemRepo;
import com.ncpl.sales.repository.SalesOrderDesignItemsRepo;
import com.ncpl.sales.repository.SalesOrderDesignRepo;
import com.ncpl.sales.repository.SalesRepo;

@Service
public class DesignUploadService {
	@Autowired
	SalesOrderDesignService soDesignService;
	@Autowired
	SalesService salesService;
	@Autowired
	ItemMasterService itemService;
	@Autowired
	SalesRepo salesrepo;
	@Autowired
 	PartyRepo partyrepo;
	@Autowired
	SalesItemRepo salesItemRepo;
	@Autowired
	SalesOrderDesignRepo designRepo;
	@Autowired
	SalesOrderDesignItemsRepo designItemRepo;
	@Autowired
	SalesOrderAuditService auditService;
	
	@SuppressWarnings("unused")
	public List<String> processExcelFile(MultipartFile file, String clientPoNum) {
        List<String> errors = new ArrayList<>();
        Set<String> salesItemIdsWithDesigns = new HashSet<>();
        Map<String, Integer> designItemCounts = new HashMap<>();
        Map<String, String> salesOrderIdsMap = new HashMap<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            SalesOrderDesign soDesign = new SalesOrderDesign();
            List<DesignItems> designItems = new ArrayList<>();
	        for ( int rowNum=1; rowNum <= lastRowNum; rowNum++) {
		        Row row = sheet.getRow(rowNum);
		        if (row == null) continue;
		        
		        String slNo=getStringCellValue(row.getCell(0));
		        String description = getStringCellValue(row.getCell(1));
	            String modelName = getStringCellValue(row.getCell(2));
	            Float quantity = getFloatCellValue(row.getCell(3),errors,rowNum,"Quantity");
	            
	            if (description.isEmpty() ) {
	                errors.add("Row " + (rowNum + 1) + ": Description is mandatory.");
	            }
	            
	            if (quantity == null) {
	                errors.add("Row " + (rowNum + 1) + ": Quantity is mandatory");
	            }
	            
	           
	            
	           
	            
	            SalesItem salesItem=salesItemRepo.findByDescAndClientPoNumberAndSlNo(description, clientPoNum, slNo);
	            if(salesItem==null) {
	            	errors.add("Row " + (rowNum + 1) + ": Line item does not exists for the description.");
	            }else {
	            
		            SalesOrderDesign design = designRepo.getDesginObjBySalesItemId(salesItem.getId())
		                    .orElseGet(() -> {
		                        SalesOrderDesign d = new SalesOrderDesign();
		                        d.setSalesItemId(salesItem.getId());
		                        return designRepo.save(d);
		                    });
		            if (modelName.isEmpty()) {
		            	errors.add("Row " + (rowNum + 1) + ": Model is mandatory.");
		            }else {
			            ItemMaster item=itemService.getItemByModelNo(modelName);
			            if(item==null) {
			            	errors.add("Row " + (rowNum + 1) + ": Model does not exists.");
			            }
			            else {
			            Optional<DesignItems> designItemOpt = 
				                designItemRepo.findDesignItemByItemIdAndDesignId(item.getId(),design.getId());
			
				            if (designItemOpt.isPresent()) {
				            	DesignItems existing = designItemOpt.get();
				                existing.setQuantity(quantity);
				                designItemRepo.save(existing);
				                salesItemIdsWithDesigns.add(salesItem.getId());
				                designItemCounts.merge(salesItem.getId(), 1, Integer::sum);
				                salesOrderIdsMap.put(salesItem.getId(), salesItem.getSalesOrder().getId());
				            } else {
				            	DesignItems newItem = new DesignItems();
				                newItem.setSalesOrderDesign(design);
				                newItem.setItemId(item.getId());
				                newItem.setQuantity(quantity);
				                designItemRepo.save(newItem);
				                salesItemIdsWithDesigns.add(salesItem.getId());
				                designItemCounts.merge(salesItem.getId(), 1, Integer::sum);
				                salesOrderIdsMap.put(salesItem.getId(), salesItem.getSalesOrder().getId());
				            }
			            }
		            }
	            }
	        

	        }
	        
	        for (String salesItemId : salesItemIdsWithDesigns) {
	            try {
	                String salesOrderId = salesOrderIdsMap.get(salesItemId);
	                Integer itemCount = designItemCounts.get(salesItemId);
	                
	                SalesOrderAudit audit = new SalesOrderAudit();
	                audit.setSalesOrderId(salesOrderId);
	                audit.setAction("UPLOAD_DESIGN");
	                audit.setPerformedBy(getCurrentUsername());
	                audit.setActionPerformed(new java.sql.Timestamp(System.currentTimeMillis()));
	                audit.setDescription("Design uploaded via Excel for Client PO: " + clientPoNum + ", Sales Item: " + salesItemId + ", Items: " + itemCount);
	                
	                try {
	                    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	                    audit.setIpAddress(request.getRemoteAddr());
	                    audit.setSessionId(request.getSession().getId());
	                } catch (Exception e) {
	                    audit.setIpAddress("UNKNOWN");
	                    audit.setSessionId("UNKNOWN");
	                }
	                
	                auditService.saveAuditLog(audit);
	            } catch (Exception e) {
	                errors.add("Error creating audit log for sales item: " + salesItemId);
	            }
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
	    	
	    	private String getCurrentUsername() {
	    	    try {
	    	        return org.springframework.security.core.context.SecurityContextHolder.getContext()
	    	                .getAuthentication().getName();
	    	    } catch (Exception e) {
	    	        return "SYSTEM";
	    	    }
	    	}
}
