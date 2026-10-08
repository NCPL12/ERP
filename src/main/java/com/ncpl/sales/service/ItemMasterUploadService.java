package com.ncpl.sales.service;

import java.io.InputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
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
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesItemRepo;
import com.ncpl.sales.repository.SalesRepo;

@Service
public class ItemMasterUploadService {
	@Autowired
    private SalesRepo salesOrderRepository;
 @Autowired
 	private MakeService makeService;
 @Autowired
 	private SalesItemRepo salesItemRepository;
 @Autowired
 	PartyRepo partyrepo;
 @Autowired
 	SalesService salesService;
 @Autowired
 	ItemMasterService itemService;
 
    public String uploadExcelFile(MultipartFile file) {
    	 Workbook workbook;
    	 try (InputStream inputStream = file.getInputStream()){
    			 if (file.getOriginalFilename().endsWith(".xls")) {
    	                workbook = new HSSFWorkbook(inputStream); // For older Excel files (.xls)
    	            } else if (file.getOriginalFilename().endsWith(".xlsx")) {
    	                workbook = new XSSFWorkbook(inputStream); // For newer Excel files (.xlsx)
    	            } else {
    	                throw new IllegalArgumentException("Invalid file format. Only .xls and .xlsx files are supported.");
    	            }

                Sheet sheet = workbook.getSheetAt(0);
                
                
                
                // Parse tabular data (row 9 onwards)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null || row.getCell(0) == null) {
                    	break; // Skip empty rows
                    }
                    
                    ItemMaster item = new ItemMaster();
                    
                    if(row.getCell(0).getCellType()==Cell.CELL_TYPE_NUMERIC) {
                    	item.setModel(String.valueOf(row.getCell(0).getNumericCellValue()));
                    }else {
                    	item.setModel(row.getCell(0).getStringCellValue());
                    }
                    
                    if(row.getCell(1).getCellType()==Cell.CELL_TYPE_NUMERIC) {
                    	item.setItemName(String.valueOf(row.getCell(1).getNumericCellValue()));
                    }else {
                    	item.setItemName(row.getCell(1).getStringCellValue());
                    }
                    
                    if(row.getCell(2).getCellType()==Cell.CELL_TYPE_NUMERIC) {
                    	item.setHsnCode(String.valueOf((long)row.getCell(2).getNumericCellValue()));
                    }else {
                    	item.setHsnCode(row.getCell(2).getStringCellValue());
                    }
                    
                    Units unitObj=itemService.getUnitsByName(row.getCell(3).getStringCellValue());
                    item.setItem_units(unitObj);
                    item.setGst((int) row.getCell(4).getNumericCellValue());
                    item.setLocation(row.getCell(5).getStringCellValue());
                    String makeCell=row.getCell(6).getStringCellValue();
                    
                    if(makeCell.equals("")){
                    	item.setMake(null);
                    }else {
                    	Make make= makeService.getMakeByNaeme(makeCell);
                    	item.setMake(make.getId().toString());
                    }
                    
                    
                    String toolTacklesFlag=row.getCell(7).getStringCellValue();
                    if(toolTacklesFlag.equalsIgnoreCase("Yes")) {
                    	 item.setToolTracker(true);
                    }else {
                    	item.setToolTracker(false);
                    }
                    item.setId("");

                    itemService.saveItemMaster(item);
                   
                    
                }
               
                workbook.close();
                return "File uploaded and data saved successfully!";
            } catch (Exception e) {
                throw new RuntimeException("Failed to process file: " + e.getMessage(), e);
            }
            
    }

}
