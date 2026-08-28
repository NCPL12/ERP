package com.ncpl.sales.service;

import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
public class SalesOrderUploadService {
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
	                
	                SalesOrder order = new SalesOrder();
	                List<SalesItem> items = new ArrayList<SalesItem>();

	                // Parse metadata (rows 1-6)
	                order.setClientPoNumber(sheet.getRow(1).getCell(0).getStringCellValue().split(":")[1].trim());
	                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	                
	                String clientPoDateStr = sheet.getRow(2).getCell(0).getStringCellValue().split(":")[1].trim();
	                if (clientPoDateStr != null && !clientPoDateStr.trim().isEmpty()) {
	                LocalDate localDate1 = LocalDate.parse(clientPoDateStr, inputFormatter);
	                Date clientPoDate = Date.valueOf(localDate1);
	                
	                order.setClientPoDate(clientPoDate);
	                }else {
	                	order.setClientPoDate(null);
	                }
	                String partyName=sheet.getRow(3).getCell(0).getStringCellValue().split(":")[1].trim();
 	                Party party=partyrepo.findByName(partyName);
	                order.setParty(party);
	                String shippingAddrName= sheet.getRow(4).getCell(0).getStringCellValue().split(":")[1].trim().split("-")[0].trim();
     	            Party shippingParty=partyrepo.findByName(shippingAddrName);
	               // order.setShippingAddress("");
	                String billingAddrName= sheet.getRow(5).getCell(0).getStringCellValue().split(":")[1].trim().split("-")[0].trim();
     	            Party billingParty=partyrepo.findByName(billingAddrName);

	                order.setBillingAddress("");
	                order.setRegion(sheet.getRow(6).getCell(0).getStringCellValue().split(":")[1].trim());
	                
	                order.setBillingAddress(billingParty.getId());
	                order.setShippingAddress(shippingParty.getId());
	               
	                order.setId("");
	                // Save SalesOrder to DB
	                

	                // Parse tabular data (row 9 onwards)
	                int rowNum = 0;
	                for (int i = 9; i <= sheet.getLastRowNum(); i++) {
	                    Row row = sheet.getRow(i);
	                    /*if(row.getCell(9).getStringCellValue()=="Total") {
	                    	break;
	                    }else {*/
	                    if (row == null || row.getCell(0) == null|| row.getCell(1).getStringCellValue().equals("")|| row.getCell(1).getStringCellValue().trim().isEmpty()) {
	                    	 rowNum=row.getRowNum();
	                    	break; // Skip empty rows
	                    }
	                    
	                    SalesItem item = new SalesItem();
	                    item.setSlNo(row.getCell(0).getStringCellValue());
	                    item.setDescription(row.getCell(1).getStringCellValue());
	                    item.setModelNo(row.getCell(5).getStringCellValue());
	                    item.setHsnCode(row.getCell(6).getStringCellValue());
	                    float quantity=(float) row.getCell(8).getNumericCellValue();
	                    
	                    item.setQuantity((float) (Math.round(quantity * 100.0) / 100.0));
	                    
	                    Units unitObj=itemService.getUnitsByName(row.getCell(9).getStringCellValue());
	                    item.setUnit(unitObj.getId().toString());
	                    float unitPrice=(float) row.getCell(10).getNumericCellValue();
	                    item.setUnitPrice((float) (Math.round(unitPrice * 100.0) / 100.0));
	                    float servicePrice=(float) row.getCell(11).getNumericCellValue();
	                    item.setServicePrice((float) (Math.round(servicePrice * 100.0) / 100.0));
	                    float amount=(float) row.getCell(12).getNumericCellValue();
	                    item.setAmount((float) (Math.round(amount * 100.0) / 100.0));
	                    item.setSalesOrder(order);
	                    item.setArchive(false);

	                    items.add(item);
	                   
	                    
	                }
	                float total=(float)sheet.getRow(rowNum).getCell(12).getNumericCellValue();
	                order.setTotal((float) (Math.round(total * 100.0) / 100.0));
	                rowNum=rowNum+1;
	                order.setGstRate((int)sheet.getRow(rowNum).getCell(12).getNumericCellValue());
	                rowNum=rowNum+1;
	                float gst=(float)sheet.getRow(rowNum).getCell(12).getNumericCellValue();
	                order.setGst((float) (Math.round(gst * 100.0) / 100.0));
	                rowNum=rowNum+1;
	                float grandTotal=(float)sheet.getRow(rowNum).getCell(12).getNumericCellValue();
	                order.setGrandTotal((float) (Math.round(grandTotal * 100.0) / 100.0));
	                rowNum=rowNum+1;
	                String projectclosureDateStr = sheet.getRow(rowNum).getCell(0).getStringCellValue().split(":")[1].trim();
	                
	                if (projectclosureDateStr != null && !projectclosureDateStr.trim().isEmpty()) {
	                // Parse date string
	                LocalDate localDate = LocalDate.parse(projectclosureDateStr, inputFormatter);

	                // Convert to java.sql.Date
	                Date projectClosureDate = Date.valueOf(localDate);

	                
	                order.setProjectClosureDate(projectClosureDate);
	                }else {
	                	order.setProjectClosureDate(null);
	                }
	                
	                order.setArchive(false);
	                rowNum=rowNum+2;
	                order.setModeOfPayment(sheet.getRow(rowNum).getCell(0).getStringCellValue().split(":")[1].trim());
	                rowNum=rowNum+1;
	                order.setJurisdiction(sheet.getRow(rowNum).getCell(0).getStringCellValue().split(":")[1].trim());
	                rowNum=rowNum+1;
	                order.setFreight(sheet.getRow(rowNum).getCell(0).getStringCellValue().split(":")[1].trim());
	                rowNum=rowNum+1;
	                order.setDelivery(sheet.getRow(rowNum).getCell(0).getStringCellValue().split(":")[1].trim());
	                rowNum=rowNum+1;
	                order.setWarranty(sheet.getRow(rowNum).getCell(0).getStringCellValue().split(":")[1].trim());
	                rowNum=rowNum+1;
	                order.setOtherTermsAndConditions(sheet.getRow(rowNum).getCell(0).getStringCellValue().split(":")[1].trim());
	                order.setItems(items);
	                salesService.savesales(order, party.getId());
	                // Save SalesItems to DB
	               // salesItemRepository.saveAll(items);
	                workbook.close();
	                return "File uploaded and data saved successfully!";
	            } catch (Exception e) {
	                throw new RuntimeException("Failed to process file: " + e.getMessage(), e);
	            }
	            
	    }
	        
}
	                
	                
	                
	                
	                
	         