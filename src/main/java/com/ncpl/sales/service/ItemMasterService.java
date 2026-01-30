package com.ncpl.sales.service;

					
							   
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
									   

										
									   
										 
											
												  
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
													   

															  
import com.ncpl.common.Constants;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.ItemsWithMinQty;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.model.Supplier;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.ItemMasterRepo;
import com.ncpl.sales.repository.ItemsWithMinQtyRepo;
import com.ncpl.sales.repository.StockRepo;
import com.ncpl.sales.repository.SupplierRepo;
import com.ncpl.sales.repository.UnitsRepo;
import com.ncpl.sales.util.DateConverterUtil;
import com.ncpl.sales.util.NcplUtil;
@Service
public class ItemMasterService {
	@Autowired
	ItemMasterRepo itemMasterRepo;
	@Autowired
	StockRepo stockRepo;
	@Autowired
	PartyService partyService;
	@Autowired
	UnitsRepo unitsRepo;
	@Autowired
	SupplierRepo supplierRepo;
	@Autowired
	NcplUtil util;
	@Autowired
	EmailService emailService;
	@Autowired
	StockService stockService;
	@PersistenceContext
    private EntityManager em;
	@Autowired
	DateConverterUtil convertDate;
	@Autowired
	ItemsWithMinQtyRepo itemsWithMinQtyRepo;
	@Autowired
	PurchaseItemService purchaseItemService;
										 

	public List<ItemMaster> getItemList() {
		List<ItemMaster> itemList = itemMasterRepo.findAll();
		//trimSpacesFromModelNo();
		Collections.sort(itemList);
		return itemList;
	}
	
	/**
	 * Optimized method to get paginated items with database-level pagination
	 * @param pageNo - page number (0-based)
	 * @param pageSize - number of items per page
	 * @param searchValue - optional search term
	 * @param toolTrackerOnly - if true, only return items where tool_tracker=1
	 * @return paginated list of items
	 */
	public List<ItemMaster> getPaginatedItemList(int pageNo, int pageSize, String searchValue, boolean toolTrackerOnly) {
		long startTime = System.currentTimeMillis();
		System.out.println("  -> getPaginatedItemList called - pageNo: " + pageNo + ", pageSize: " + pageSize + ", search: '" + searchValue + "', toolTrackerOnly: " + toolTrackerOnly);
		
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<ItemMaster> pagedResult;
		
		if (toolTrackerOnly) {
			// Get only toolTracker items
			if (searchValue != null && !searchValue.trim().isEmpty()) {
				long dbStart = System.currentTimeMillis();
				pagedResult = itemMasterRepo.findToolTrackerItemsWithSearch(searchValue, paging);
				System.out.println("  -> Database toolTracker search query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			} else {
				long dbStart = System.currentTimeMillis();
				pagedResult = itemMasterRepo.findToolTrackerItems(paging);
				System.out.println("  -> Database toolTracker query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			}
		} else {
			// Get all items (existing logic)
			if (searchValue != null && !searchValue.trim().isEmpty()) {
				// Use database-level search for better performance
				long dbStart = System.currentTimeMillis();
				pagedResult = itemMasterRepo.findByModelContainingIgnoreCaseOrItemNameContainingIgnoreCaseOrHsnCodeContainingIgnoreCase(
					searchValue, searchValue, searchValue, paging);
				System.out.println("  -> Database search query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			} else {
				long dbStart = System.currentTimeMillis();
				pagedResult = itemMasterRepo.findAll(paging);
				System.out.println("  -> Database findAll query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			}
		}
		
		System.out.println("  -> getPaginatedItemList completed: " + (System.currentTimeMillis() - startTime) + "ms, returned " + pagedResult.toList().size() + " items");
		return pagedResult.toList();
	}
	
	/**
	 * Optimized method to get paginated items with database-level pagination (backward compatibility)
	 * @param pageNo - page number (0-based)
	 * @param pageSize - number of items per page
	 * @param searchValue - optional search term
	 * @return paginated list of items
	 */
	public List<ItemMaster> getPaginatedItemList(int pageNo, int pageSize, String searchValue) {
		return getPaginatedItemList(pageNo, pageSize, searchValue, false);
	}
	
	/**
	 * Get total count of items for pagination
	 * @param searchValue - optional search term
	 * @param toolTrackerOnly - if true, only count items where tool_tracker=1
	 * @return total count
	 */
	public long getItemCount(String searchValue, boolean toolTrackerOnly) {
		long startTime = System.currentTimeMillis();
		System.out.println("  -> getItemCount called - search: '" + searchValue + "', toolTrackerOnly: " + toolTrackerOnly);
		
		long count;
		if (toolTrackerOnly) {
			// Count only toolTracker items
			if (searchValue != null && !searchValue.trim().isEmpty()) {
				long dbStart = System.currentTimeMillis();
				count = itemMasterRepo.countToolTrackerItemsWithSearch(searchValue);
				System.out.println("  -> Database toolTracker count search query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			} else {
				long dbStart = System.currentTimeMillis();
				count = itemMasterRepo.countToolTrackerItems();
				System.out.println("  -> Database toolTracker count query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			}
		} else {
			// Count all items (existing logic)
			if (searchValue != null && !searchValue.trim().isEmpty()) {
				long dbStart = System.currentTimeMillis();
				count = itemMasterRepo.countByModelContainingIgnoreCaseOrItemNameContainingIgnoreCaseOrHsnCodeContainingIgnoreCase(
					searchValue, searchValue, searchValue);
				System.out.println("  -> Database count search query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			} else {
				long dbStart = System.currentTimeMillis();
				count = itemMasterRepo.count();
				System.out.println("  -> Database count query took: " + (System.currentTimeMillis() - dbStart) + "ms");
			}
		}
		
		System.out.println("  -> getItemCount completed: " + (System.currentTimeMillis() - startTime) + "ms, returned " + count);
		return count;
	}
	
	/**
	 * Get total count of items for pagination (backward compatibility)
	 * @param searchValue - optional search term
	 * @return total count
	 */
	public long getItemCount(String searchValue) {
		return getItemCount(searchValue, false);
	}
	
	/**
	 * Get items without tools and company assets
	 * @return list of items without tools
	 */
	public List<ItemMaster> getItemListWithoutTools() {
		List<ItemMaster> itemList = itemMasterRepo.findItemListWithoutTools();
							
		Collections.sort(itemList);
		return itemList;
	}
	
	/**
	 * Get items with tools
	 * @return list of items with tools
	 */
	public List<ItemMaster> getItemListWithTools() {
		List<ItemMaster> itemList = itemMasterRepo.findItemListWithTools();
							
		Collections.sort(itemList);
		return itemList;
	}
	
	/**
	 * Get items with minimum quantity by item ID
	 * @param itemId - the item ID
	 * @return Optional containing the minimum quantity item
	 */
	public Optional<ItemsWithMinQty> getItemsWithMinQtyById(String itemId) {
		Optional<ItemsWithMinQty> minQtyItem = itemsWithMinQtyRepo.findbyItemId(itemId);
		return minQtyItem;
	}
	
	/**
	 * Get units by name
	 * @param unitName - the unit name
	 * @return Units object
	 */
	public Units getUnitsByName(String unitName) {
		Units unitObj = unitsRepo.findByName(unitName);
		return unitObj;
	}
	
	
	
	public void trimSpacesFromModelNo() {
		// PERFORMANCE OPTIMIZATION: Only run this once or on demand
		// This method was loading ALL 6881 items on every page load!
		System.out.println("  -> trimSpacesFromModelNo: SKIPPED for performance (was loading all items)");
		System.out.println("  -> If needed, run this manually via admin endpoint");
	}
	
	/**
	 * Manual trim method - only run when needed
	 * Call this via admin endpoint if trimming is required
	 */
	public void trimSpacesFromModelNoManual() {
		long startTime = System.currentTimeMillis();
		System.out.println("  -> trimSpacesFromModelNoManual called");
		
		long dbStart = System.currentTimeMillis();
		List<ItemMaster> itemList = itemMasterRepo.findAll();
		System.out.println("  -> Loaded " + itemList.size() + " items for trimming: " + (System.currentTimeMillis() - dbStart) + "ms");
		
		if (itemList.size() > 0) {
			long saveStart = System.currentTimeMillis();
			for (ItemMaster itemMaster : itemList) {
				itemMaster.setModel(itemMaster.getModel().trim());
				itemMasterRepo.save(itemMaster);
			}
			System.out.println("  -> Trimmed and saved " + itemList.size() + " items: " + (System.currentTimeMillis() - saveStart) + "ms");
		}
		
		System.out.println("  -> trimSpacesFromModelNoManual completed: " + (System.currentTimeMillis() - startTime) + "ms");
	}



	public ItemMaster saveItemMaster(ItemMaster itemMaster) {
		String ItemId = itemMaster.getId();
		Long unitId = itemMaster.getUnits();
		itemMaster.setModel(itemMaster.getModel().trim());
		if(unitId == null){
			itemMaster.setUnits((long) 0);
		}else{
		Optional<Units> unitObj= getUnitsById(unitId);
			itemMaster.setItem_units(unitObj.get());
		}
		if(ItemId.isEmpty()) {
			ItemMaster item= itemMasterRepo.save(itemMaster);
			Map<String, Object> emailContents = null;
		
			emailContents = itemDetails(item.getItemName(),item.getModel());
			//emailService.sendItemAddedEmailToServer(emailContents);
			return item;
		}else {
			Optional<ItemMaster> persistedItem = getItemById(itemMaster.getId());
			Date createdDate = persistedItem.get().getCreated();
			itemMaster.setCreated(createdDate);
			ItemMaster updatedItem= itemMasterRepo.save(itemMaster);
			return updatedItem;
		}	
	}



	private Map<String, Object> itemDetails(String itemName, String model) {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject", "Item Created with " + itemName);
		emailContents.put("template", "item-created.html");
	    emailContents.put("to1", "store@ncpl.co");
		emailContents.put("to2", "purchase@ncpl.co");
		emailContents.put("to3", "design@ncpl.co");
		emailContents.put("cc1", "ramsy@ncpl.co");
		emailContents.put("cc2", "prasadini@ncpl.co");
		emailContents.put("cc3", "surendra@ncpl.co");
		emailContents.put("cc4", "prashanth@ncpl.co");
//		 emailContents.put("to1", "sunil@tek-nika.com");
	//		emailContents.put("to2", "sunil@tek-nika.com");
		//	emailContents.put("cc1", "sunil@tek-nika.com");
		//	emailContents.put("cc2", "sunil189441@gmail.com");
		//	emailContents.put("cc3", "sunil189441@gmail.com");
		//	emailContents.put("cc4", "sunil189441@gmail.com");
		emailContents.put("month", Constants.currentDate());
		emailContents.put("ItemName", itemName);
		emailContents.put("ModelNo", model);
		return emailContents;
	}



	public Optional<ItemMaster> getItemById(String id) {
		Optional<ItemMaster> itemMasterObject = itemMasterRepo.findById(id);
									
		List<Supplier> supplier = getSupplierList(itemMasterObject.get().getId());
		
		//No supplier mapped
		if(supplier.size() == 0) {
			itemMasterObject.get().setPrefferedCost(0);
		}
		
		else {
			List<Supplier> prefferSupplier = getSupplierListWithpreferredYes(itemMasterObject.get().getId());
			if(prefferSupplier.size() == 0) {
				itemMasterObject.get().setPrefferedCost(supplier.get(0).getCostPrice());
			}else {
				itemMasterObject.get().setPrefferedCost(prefferSupplier.get(0).getCostPrice());
			}
			
		}
		
		
		
		return itemMasterObject;
	}
	

	/**
	 * delete item code
	 * @param id
	 */
	public void deleteItem(String id) {
		itemMasterRepo.deleteById(id);
	}



	
	
	/**
	 * get units by Id
	 * @param id
	 * @return unitObj
	 */
	public Optional<Units> getUnitsById(Long id) {
		
		Optional<Units> unitObj = unitsRepo.findById(id);
		return unitObj;
	}

	/**
	 * get list of units
	 * @return unitList
	 */
	public List<Units> getUnitList() {
		List<Units> unitList = unitsRepo.findAll();
		//Collections.sort(unitList);
		Collections.sort(unitList, new Comparator<Units>()
		  {
		    public int compare(Units o1, Units o2)
		     {
		         if (o1.getName().equals(o2.getName())) // update to make it stable
		           return 0;
		         if (o1.getName().equals("Heading"))
		           return -1;
		         if (o2.getName().equals("Heading"))
		           return 1;
		         return o1.getName().compareTo(o2.getName());
		     }
		});
		return unitList;
	}


	/**
	 * save supplier
	 * @param supplier
	 * @param partyId
	 * @param preferrred 
	 * @return supplierObj
	 */
	public Supplier saveSuppler(Supplier supplier,String partyId,String itemId, String preferred) {
		Optional<ItemMaster> itemMasterObject = getItemById(itemId);
		if(preferred.equals("Yes")) {
			
			double costPrice=supplier.getCostPrice();
			//set sell price as 10% of cost price.
			double sellPrice=(costPrice*10)/100;
			//set min sell price as 30% of cost price.
			double minSellPrice=(costPrice*30)/100;
			itemMasterObject.get().setSellPrice(sellPrice);
			itemMasterObject.get().setMinSellPrice(minSellPrice);
			
			List<Supplier> list = getSupplierList(itemId);
			for (Supplier supplr : list) {
				supplr.setPreferred("No");
				
			}
		}
		String supplierId = supplier.getSupplierId();
		if(partyId!="") {
		Party party=partyService.getPartyById(partyId);
		supplier.setParty(party);
		}
		
		if(supplierId.isEmpty()) {
			
			
			supplier.setItemMaster(itemMasterObject.get());
			Supplier supplierObj=supplierRepo.save(supplier);
			return supplierObj;
		}else {
			Optional<Supplier> persistedStock = getSupplierById(supplierId);
			Date createdDate = persistedStock.get().getCreated();
			supplier.setCreated(createdDate);
			supplier.setItemMaster(itemMasterObject.get());
			Supplier updatedSupplier=supplierRepo.save(supplier);
			return updatedSupplier;
		
		}
	}
	
	/**
	 * get supplier by Id
	 * @param id
	 * @return supplierObj
	 */
	public Optional<Supplier> getSupplierById(String id) {
		
		Optional<Supplier> supplierObj = supplierRepo.findById(id);
		return supplierObj;
	}
	
	/**
	 * get list of supplier
	 * @return supplierList
	 */
	public List<Supplier> getSupplierList(String itemId) {
		List<Supplier> supplierList = supplierRepo.getAllSupplierByItemId(itemId);
		return supplierList;
	}

	public List<Supplier> getSupplierListWithPoHistory(String itemId) {
		List<Supplier> supplierList = supplierRepo.getAllSupplierByItemId(itemId);
		List<PurchaseItem> poItemList= purchaseItemService.findByModelNumberWithLatestPoItem(itemId);
		Optional<ItemMaster> itemObj= getItemById(itemId);
		
		for (PurchaseItem purchaseItem : poItemList) {
			Supplier supplier=new Supplier();
			supplier.setParty(purchaseItem.getPurchaseOrder().getParty());
			supplier.setSupplierName(purchaseItem.getPurchaseOrder().getParty().getPartyName());
			supplier.setCostPrice(purchaseItem.getUnitPrice());
			supplier.setItemMaster(itemObj.get());
			supplier.setPreferred("No");
			supplier.setCreated(purchaseItem.getPurchaseOrder().getCreated());
			supplier.setUpdated(purchaseItem.getPurchaseOrder().getUpdated());
			supplierList.add(supplier);
			System.out.println(supplierList.size());
		}
		return supplierList;
	}
																	
																			
																							   
													
  
												
									
																 
																					   
													  
										 
							   
																	 
																	 
							  
										   
   
					  
  
 
	/**
	 * get list of all supplier
	 * @return supplierList
	 */
	public List<Supplier> getAllSupplierList() {
		List<Supplier> allSupplierList = supplierRepo.findAll();
		return allSupplierList;
	}
	
	/**
	 * get list of supplier with preferred yes
	 * @param itemId
	 * @return supplierList
	 */
	public List<Supplier> getSupplierListWithpreferredYes(String itemId) {
		List<Supplier> supplierList = supplierRepo.getSupplierWithPreferredYes(itemId);
		return supplierList;
	}

	/**
	 * get list of supplier with preferred yes
	 * @param itemId
	 * @param supplierName
	 * @param supplierId 
	 * @return response
	 */
	public boolean checkSupplierExists(String itemId, String supplierName, String supplierId) {
		boolean response;
		List<Supplier> supplierList;
		if(supplierId.equals("null")) {
			supplierList=supplierRepo.findSupplierListBySupplierName(itemId, supplierName);
		}else {
			supplierList=supplierRepo.findSupplierListBySupplierNameForEdit(itemId,supplierName,supplierId);
		}
		if(supplierList.size()>=1) {
			response = true;
		}
		else {
			response = false;
		}
		return response;
	}

	


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map findItemDetails() {
		Map<String, List<Object>> itemDetails = new HashMap<String, List<Object>>();
		List<Object> itemIdList = new ArrayList();
		List<Object> itemDescList= new ArrayList();
		List<Object> quantityList = new ArrayList();
		List<Object> costList = new ArrayList();
		
		List<ItemMaster> itemList= getItemList();
		
		for (ItemMaster itemMaster : itemList) {
			String itemId = itemMaster.getId();
			if(itemId == null || itemId == ""){
				itemIdList.add("");
			}else{
				itemIdList.add(itemId);
			}
			String itemDesc = itemMaster.getItemName();
			if(itemDesc == null || itemDesc == ""){
				itemDescList.add("");
			}else{
				itemDescList.add(itemDesc);
			}
			float quantity = 0;
			List<Stock> stockList = stockService.getStockList(itemId);
			for (Stock stock : stockList) {
				float stockCount = stock.getQuantity();
				quantity = quantity +stockCount;
			}
			quantityList.add(quantity);
			List<Supplier> supplierList = getSupplierList(itemId);
			String preferred = "";
			for (Supplier supplier : supplierList) {
				preferred = supplier.getPreferred();
				if(preferred.equalsIgnoreCase("yes")){
					costList.add(supplier.getCostPrice());
					break;
				}
			}
			if(preferred == "" || preferred.equalsIgnoreCase("no")){
				costList.add((double) 0);
			}
			 
		}
		itemDetails.put("itemId", itemIdList);
		itemDetails.put("itemDesc", itemDescList);
		itemDetails.put("itemqQuantity", quantityList);
		itemDetails.put("costList", costList);
		
		
		
		return itemDetails;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	public List<Stock> getStocketRevisionByDate(String todaysDateStr) throws IOException{
		
		AuditReader auditReader = AuditReaderFactory.get(em);
		
		//Getting available revisions for particular entity object
		/*List<Number> revisionNumbers = auditReader.getRevisions(Stock.class, "STK-171");
		for (Number number : revisionNumbers) {
			Stock stock = auditReader.find(Stock.class, "STK-171", number);
			System.out.println(">>>>>>>>>>>>>>>> : " +stock.getStoreName());
		}*/
		
	//	stockService.getStockHistoryByDate(todaysDateStr);
	
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");        
	        Date todaysDate = null;
	        Date tomorrowsDate = null;
			try {
				todaysDate = sdf.parse(todaysDateStr);
				Calendar c = Calendar.getInstance(); 
				c.setTime(todaysDate); 
				c.add(Calendar.DATE, 1);
				tomorrowsDate = c.getTime();
			
			} catch (ParseException e) {
				
				e.printStackTrace();
			}

		List<Stock> stockHistory = new ArrayList<>();	
		AuditQuery q = auditReader.createQuery().forRevisionsOfEntity(Stock.class, true, true);
		q.add(AuditEntity.property("updated").ge(todaysDate)).add(AuditEntity.property("updated").le(tomorrowsDate));
		List<Stock> revisionNumbers = q.getResultList();
	
		for(int i=0;i<revisionNumbers.size(); i++) {
			
			
			Stock s = revisionNumbers.get(i);
			stockHistory.add(revisionNumbers.get(i));
		}
		
		
		return stockHistory;	
	}



	public ItemMaster getItemByModelNo(String model) {
		// TODO Auto-generated method stub
							
		ItemMaster itemMasterObject = itemMasterRepo.getItemByModelNo(model);
		return itemMasterObject;
	}



	public Optional<ItemMaster> getItemListById(String id) {
		// TODO Auto-generated method stub
		Optional<ItemMaster> itemMasterObject = itemMasterRepo.findById(id);
		return itemMasterObject;
	}



	public List<Supplier> findItemsForSelectedVendor(String itemId, String supplierName) {
		List<Supplier> supplierList=supplierRepo.findSupplierListBySupplierName(itemId,supplierName);
		return supplierList;
	}
	
	public Supplier getSupplierByItemIdAndClientId(String itemId, String supplierName) {
		Supplier supplierObj=supplierRepo.findSupplierByItemIdAndClientId(itemId,supplierName);
		return supplierObj;
	}



	public void updateSupplier(String itemId, String vendorId, double unitPrice) {
		Party party=partyService.getPartyById(vendorId);
		System.out.println(itemId);
		Supplier supplier= supplierRepo.findSupplierByItemIdAndClientId(itemId,vendorId);
		Optional<ItemMaster> item = getItemById(itemId);
		
		if(supplier==null) {
			Supplier new_supplier = new Supplier();
			new_supplier.setParty(party);
			new_supplier.setCostPrice(unitPrice);
			
			new_supplier.setPreferred("Yes");
			double costPrice=new_supplier.getCostPrice();
			//set sell price as 10% of cost price.
			double sellPrice=(costPrice*10)/100;
			//set min sell price as 30% of cost price.
			double minSellPrice=(costPrice*30)/100;
			item.get().setSellPrice(sellPrice);
			item.get().setMinSellPrice(minSellPrice);
			new_supplier.setItemMaster(item.get());
			List<Supplier> list = getSupplierList(itemId);
			for (Supplier supplr : list) {
				supplr.setPreferred("No");
				
			}
			supplierRepo.save(new_supplier);
		}else {
			double new_price=unitPrice;
			supplier.setCostPrice(new_price);
			if(supplier.getPreferred().equals("Yes")) {
				
				double costPrice=supplier.getCostPrice();
				//set sell price as 10% of cost price.
				double sellPrice=(costPrice*10)/100;
				//set min sell price as 30% of cost price.
				double minSellPrice=(costPrice*30)/100;
				item.get().setSellPrice(sellPrice);
				item.get().setMinSellPrice(minSellPrice);
				
				List<Supplier> list = getSupplierList(itemId);
				for (Supplier supplr : list) {
					supplr.setPreferred("No");
					
				}
				supplier.setPreferred("Yes");
				supplier.setItemMaster(item.get());
			}
			supplierRepo.save(supplier);
		}
		
	}
	
    public List<ItemMaster> findPaginatedItems(int pageNo, int pageSize) {

        Pageable paging = PageRequest.of(pageNo, pageSize);
        Page<ItemMaster> pagedResult = itemMasterRepo.findAll(paging);

        return pagedResult.toList();
    }
    public List<ItemMaster> getItemsByDate(String updatedDateStr) {
		List<ItemMaster> itemList = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");        
		sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        Date todaysDate = null;
        Date fromDate = null;
		try {
			todaysDate = sdf.parse(updatedDateStr);
			Calendar c = Calendar.getInstance(); 
			c.setTime(todaysDate); 
			c.add(Calendar.DATE, -1);
			fromDate = c.getTime();
			
			Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);
			Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
			
			
			itemList = itemMasterRepo.findItemListByDateUpdated(sqlFromDate, sqlToDate);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		return itemList;
	}



	public List<ItemMaster> getAllItemsWhereStockQtyNonZero() {
		List<ItemMaster> itemList = itemMasterRepo.findAllItemsWhereStockQtyNonZero();
		return itemList;
	}
	
	/**
	 * Check if model number already exists (for validation)
	 * @param model - model number to check
	 * @param itemId - current item ID (for edit mode)
	 * @return true if model exists, false otherwise
	 */
	public boolean checkModelExists(String model, String itemId) {
		if (model == null || model.trim().isEmpty()) {
			return false;
									   
						  
							
						  
						
	
		}
		
		ItemMaster existingItem = itemMasterRepo.getItemByModelNo(model.trim());
		
		// If no existing item found, model is unique
		if (existingItem == null) {
													
									  
									  
		 
					
	 

																				  
											  
																	 
											  

								
																   
																				
								
													
			  
		  

						  
					
	  
												
												
			return false;
		}
		
		// If editing existing item and it's the same item, model is valid
		if (itemId != null && !itemId.isEmpty() && existingItem.getId().equals(itemId)) {
							  
			return false;
		}
		
		// Model exists and it's not the current item being edited
		return true;
	}
}
