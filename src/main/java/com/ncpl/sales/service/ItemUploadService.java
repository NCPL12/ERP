package com.ncpl.sales.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Make;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.ItemMasterRepo;
import com.ncpl.sales.repository.MakeRepo;
import com.ncpl.sales.repository.UnitsRepo;

@Service
public class ItemUploadService {
	 @Autowired
	    private ItemMasterRepo itemRepository;

	    @Autowired
	    private MakeRepo makeRepository; // For foreign key validation

	    @Autowired
	    private UnitsRepo unitRepository; // For foreign key validation
	    
	    @Autowired
	    private MakeService makeService;
	    
	    @Autowired
	    private ItemMasterService itemService;

	    public List<String> validateAndSave(MultipartFile file) {
	        List<String> errors = new ArrayList<>();
	        List<ItemMaster> itemList = new ArrayList<>();
	        Set<String> duplicateCheckSet = new HashSet<>();

	        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
	            Sheet sheet = workbook.getSheetAt(0);

	            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Skipping header row
	                Row row = sheet.getRow(i);
	                if (row == null) continue;
	                List<String> rowErrors = new ArrayList<>(); 
	                // ✅ Fetch and validate "Model Number" (Column 0, Mandatory, Unique)
	                String modelNumber = getCellValue(row.getCell(0));
	                if (modelNumber.isEmpty()) {
	                	rowErrors.add("Row " + (i + 1) + ": Model Number is mandatory.");
	                    //continue;
	                }

	               
	                // ✅ Duplicate check within the uploaded file
	                if (!duplicateCheckSet.add(modelNumber)) {
	                	rowErrors.add("Row " + (i + 1) + ": Duplicate Model Number found in the file.");
	                    //continue;
	                }
	                ItemMaster itemObj=itemService.getItemByModelNo(modelNumber);
	                // ✅ Duplicate check in database
	                if (itemObj!=null) {
	                	rowErrors.add("Row " + (i + 1) + ": Model Number '" + modelNumber + "' already exists in the database.");
	                    //continue;
	                }

	                // ✅ Fetch and validate "Product Name" (Column 1, Mandatory)
	                String itemName = getCellValue(row.getCell(1));
	                if (itemName.isEmpty()) {
	                	rowErrors.add("Row " + (i + 1) + ": Item Name is mandatory.");
	                   // continue;
	                }

	                // ✅ Fetch and validate "Price" (Column 2, Mandatory, Numeric)
	                String hsn = getCellValue(row.getCell(2));
	                if (hsn.isEmpty()) {
	                	rowErrors.add("Row " + (i + 1) + ": hsn is mandatory.");
	                    //continue;
	                }

	                // ✅ Fetch and validate "Quantity" (Column 3, Mandatory, Numeric)
	                Integer gst = getIntegerCellValue(row.getCell(4));
	                if (gst == null || gst < 0) {
	                	rowErrors.add("Row " + (i + 1) + ": gst is mandatory.");
	                    //continue;
	                }
	                
	                // ✅ Fetch and validate "Price" (Column 2, Mandatory, Numeric)
	                String location = getCellValue(row.getCell(2));
	               

	                // ✅ Fetch and validate "Make" (Column 4, Foreign Key)
	                String make = getCellValue(row.getCell(6));
	                if (make.isEmpty()) {
	                	rowErrors.add("Row " + (i + 1) + ": Make is mandatory.");
	                    //continue;
	                }
	                Make makeObj= makeService.getMakeByNaeme(make);
	                if (makeObj==null) {
	                	rowErrors.add("Row " + (i + 1) + ": Make '" + make + "' does not exist in the database.");
	                    //continue;
	                }
	               

	                // ✅ Fetch and validate "Unit" (Column 5, Foreign Key)
	                String unit = getCellValue(row.getCell(3));
	                if (unit.isEmpty()) {
	                	rowErrors.add("Row " + (i + 1) + ": Unit is mandatory.");
	                    //continue;
	                }
	                Units unitObj=itemService.getUnitsByName(unit);
	                if (unitObj==null) {
	                	rowErrors.add("Row " + (i + 1) + ": Unit '" + unit + "' does not exist in the database.");
	                    //continue;
	                }
	                if (!rowErrors.isEmpty()) {
	                    errors.add("Row " + (i + 1) + ": " + String.join(" | ", rowErrors));
	                    continue; // Skip processing this row further
	                }
	                // ✅ Fetch and validate "Price" (Column 2, Mandatory, Numeric)
	                String toolTracker = getCellValue(row.getCell(7));
	               


	                // ✅ If all validations pass, create the Item object
	                ItemMaster item = new ItemMaster();
	                item.setModel(modelNumber);
	                item.setItemName(itemName);
	                item.setHsnCode(hsn);
	                item.setGst(gst);
	                if(make.equals("")){
                    	item.setMake(null);
                    }else {
                    	
                    	item.setMake(makeObj.getId().toString());
                    }
	                item.setMake(make);
	                item.setLocation(location);
	                
	                item.setItem_units(unitObj);
                    if(toolTracker.equalsIgnoreCase("Yes")) {
                    	 item.setToolTracker(true);
                    }else {
                    	item.setToolTracker(false);
                    }
                    item.setId("");

	                itemList.add(item);
	            }

	            // ✅ Save valid data if no errors
	            if (errors.isEmpty()) {
	                itemRepository.saveAll(itemList);
	            }

	        } catch (IOException e) {
	            errors.add("Error reading the Excel file: " + e.getMessage());
	        } catch (Exception e) {
	            errors.add("Unexpected error occurred: " + e.getMessage());
	        }

	        return errors;
	    }

	    // ✅ Helper method to read String or Numeric cell values
	    private String getCellValue(Cell cell) {
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

	    // ✅ Helper method to read Numeric values (Price)
	    @SuppressWarnings("unused")
		private Double getNumericCellValue(Cell cell) {
	        if (cell == null) return null;
	        return (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) ? cell.getNumericCellValue() : null;
	    }

	    // ✅ Helper method to read Integer values (Quantity)
	    private Integer getIntegerCellValue(Cell cell) {
	        if (cell == null) return null;
	        return (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) ? (int) cell.getNumericCellValue() : null;
	    }
	    
	    private Long getLongCellValue(Cell cell) {
	        if (cell == null) return null;
	        
	        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
	            return (long) cell.getNumericCellValue();
	        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
	            try {
	                return Long.parseLong(cell.getStringCellValue().trim());
	            } catch (NumberFormatException e) {
	                return null; // Invalid number
	            }
	        }
	        
	        return null; // Invalid type
	    }
	}

