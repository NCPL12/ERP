
package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ncpl.common.Constants;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.StockRepo;
import com.ncpl.sales.util.DateConverterUtil;

@Service
public class StockService {

	@Autowired
	private StockRepo stockRepo;
	@PersistenceContext
    private EntityManager em;
	
	@Autowired
	PartyService partyService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	GrnService grnService;
	@Autowired
	DateConverterUtil convertDate;
	@Autowired
	SalesService salesService;
	@Autowired
	PartyRepo partyRepo;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	PurchaseOrderService purchaseOrderService;
	/**
	 * code to save stock
	 * @param stock
	 * @param partyId
	 * @param itemId 
	 * @param existingPartyName 
	 * @return stockObject
	 */
	public Stock saveStock(Stock stock, String partyId, String itemId) {
		String stockId = stock.getStockId();
		float presentStockQuantity = 0.0f;
		Stock stockByClient = stockRepo.findStockByClientAndItemId(itemId, partyId);
		if (stockByClient != null) {
			presentStockQuantity = stockByClient.getQuantity();

			stockId = stockByClient.getStockId();
		}
		
		//Adding the assigned stock to an existing stock 
		presentStockQuantity = presentStockQuantity + stock.getQuantity();
		stock.setQuantity(presentStockQuantity);
		//to update the stock with received quantity from grn.
		/*
		 * List<PurchaseItem>
		 * poItemList=purchaseItemService.getPurchaseItemsByModelNumber(itemId); for
		 * (PurchaseItem purchaseItem : poItemList) { String poItemId =
		 * Integer.toString(purchaseItem.getPurchase_item_id()); List<GrnItems> grnList=
		 * grnService.getGrnItemByPoItemId(poItemId); for (GrnItems grnObj : grnList) {
		 * stock.setQuantity(grnObj.getReceivedQuantity()+stock.getQuantity()); }
		 * 
		 * }
		 */
		
		if(partyId!="") {
		Party party=partyService.getPartyById(partyId);
		stock.setParty(party);
		}
		
		Optional<ItemMaster> itemMasterObject = itemMasterService.getItemById(itemId);
		if(stockId.isEmpty()) {
			
			
			stock.setItemMaster(itemMasterObject.get());
			stock.setReason(Constants.ASSIGNMENT_REASON);
			stock.setActivity(Constants.ASSIGNMENT_REASON);
			Stock stockObj=stockRepo.save(stock);
			return stockObj;
		}else {
			Optional<Stock> persistedStock = getStockById(stockId);
			Date createdDate = persistedStock.get().getCreated();
			stock.setItemMaster(itemMasterObject.get());
			stock.setCreated(createdDate);
			Stock stockUpdated = stockRepo.getOne(stockId);
			stockUpdated.setQuantity(presentStockQuantity);
			stockUpdated.setReason(Constants.ASSIGNMENT_REASON);
			stockUpdated.setActivity(Constants.ASSIGNMENT_REASON);
			//Stock updatedStock=stockRepo.save(stock);
			return stockUpdated;
		}
		
		
		
	}
	
	/**
	 * get stock by stock Id
	 * @param id
	 * @return stockObj
	 */
	public Optional<Stock> getStockById(String id) {
		
		Optional<Stock> stockObj = stockRepo.findById(id);
		return stockObj;
	}
	
	/**
	 * get stock list
	 * @param itemId 
	 * @param quantity 
	 * @return stockList
	 */
	public List<Stock> getStockList(String itemId) {
		List<Stock> stockList = stockRepo.getAllStockByItemId(itemId);
		return stockList;
	}
	
	/**
	 * get stock list
	 
	 * @return stockList
	 */
	public List<Stock> getAllStockList() {
		List<Stock> allstockList = stockRepo.findAll();
		return allstockList;
	}
	
	/**
	 * duplicate validation for clientName
	 * @param itemId
	 * @param clientName
	 * @param stockId
	 * @return 
	 */
	public boolean checkClientNameExists(String itemId, String clientName, String stockId) {
		boolean response;
		List<Stock> stocklist;
		if(stockId.equals("null")) {
		 stocklist = stockRepo.findStockListByClientName(itemId, clientName);
			
		}else {
			stocklist = stockRepo.findStockListByClientNameForEdit(itemId,clientName,stockId);
		}
		if(stocklist.size()>=1) {
			response = true;
		}
		else {
			response = false;
		}
		return response;
	}

	
	
	public List<Stock> getStockByDateUpdated(String updatedDateStr,boolean byEmail) {
		List<Stock> updatedStockList = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");        
        Date todaysDate = null;
        Date fromDate = null;
		try {
			todaysDate = sdf.parse(updatedDateStr);
			Calendar c = Calendar.getInstance(); 
			c.setTime(todaysDate); 
			
			
			if(byEmail==true) {
				c.add(Calendar.DATE, -1);
			}else {
				c.add(Calendar.DATE, -31);
			}
			
			fromDate = c.getTime();
			
			Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);
			Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
			
			
			updatedStockList = stockRepo.findStockByDateUpdated(sqlFromDate, sqlToDate);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		return updatedStockList;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<?> getStockHistoryByDate(String todaysDateStr,boolean byEmail){
		AuditReader auditReader = AuditReaderFactory.get(em);
		
		//This list contains all the updated items for that particular date from main stock table
		List<Stock> updatedStockList = getStockByDateUpdated(todaysDateStr,  byEmail);
		
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");        
        Date todaysDate = null;
        Date fromDate = null;
		try {
			todaysDate = sdf.parse(todaysDateStr);
			Calendar c = Calendar.getInstance(); 
			c.setTime(todaysDate); 
			
			if(byEmail==true) {
				c.add(Calendar.DATE, -1);
			}else {
				c.add(Calendar.DATE, -31);
			}
			fromDate = c.getTime();
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);
		Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
		
		
		List<Map<Object, Object>> stockList = new ArrayList<>();
		for (Stock stock : updatedStockList) {
			
			Map<Object, Object> resultStockMap = new HashMap<>();
			
			AuditQuery q = auditReader.createQuery().forRevisionsOfEntity(Stock.class, true, true);
			q.add(AuditEntity.property("updated").ge(sqlFromDate)).
			add(AuditEntity.property("updated").le(sqlToDate)).
			add(AuditEntity.id().eq(stock.getStockId())).addOrder(AuditEntity.property("updated").desc());
			
			
			@SuppressWarnings("unchecked")
			List<Stock>  revisionNumbers = q.getResultList();
			
			
			//Single revision will have same values in main table and audit table, 
			//so old and new values will be same
			if(revisionNumbers.size()!=0) {
			if(revisionNumbers.size() == 1) {
				//Getting 0th record because only one revision available
				Stock recent_stock = revisionNumbers.get(0);
				Optional<ItemMaster> item = itemMasterService.getItemById(recent_stock.getItemMaster().getId());
				if(recent_stock.getParty() == null) {
					recent_stock.setClientName("NA");
				}else {
					Party oldParty = partyService.getPartyById(recent_stock.getParty().getId());
					String oldClient = oldParty.getPartyName();
					recent_stock.setClientName(oldClient);
				}
				recent_stock.setItemMaster(item.get());
				
				resultStockMap.put("newStock", recent_stock);
				
				//get all the stocks from audit table when stock size is one and set old stock as the previous stock
				//when there  is only one record in audit table for the updated date and stock has been imported on previous dates then old qty should be the previous stock qty which is not coming under the selected updated date
				List<Number> stocks = auditReader.getRevisions(Stock.class, stock.getStockId());
				if(stocks.size()>1) {
					Number rev =stocks.get(stocks.size()-2);
					Stock previousStock = auditReader.find(Stock.class,stock.getStockId(), rev);
					previousStock.setItemMaster(item.get());
					resultStockMap.put("oldStock",previousStock);
					
				}else {
					resultStockMap.put("oldStock", recent_stock);
				}
				resultStockMap.put("stockSize", revisionNumbers.size());				
				stockList.add(resultStockMap);
			}
			else {
				
					//updated stock
					Stock newStock = revisionNumbers.get(0);
					if(newStock.getParty() == null) {
						newStock.setClientName("NA");
					}else {
						Party party = partyService.getPartyById(newStock.getParty().getId());
						String newClient = party.getPartyName();
						newStock.setClientName(newClient);
						
					}
					Optional<ItemMaster> item_new = itemMasterService.getItemById(newStock.getItemMaster().getId());
					newStock.setItemMaster(item_new.get());
					resultStockMap.put("newStock", newStock);
					
					//old stock
					Stock oldStock = revisionNumbers.get(1);
					Optional<ItemMaster> item = itemMasterService.getItemById(oldStock.getItemMaster().getId());
					if(oldStock.getParty() == null) {
						oldStock.setClientName("NA");
					}else {
						Party oldParty = partyService.getPartyById(oldStock.getParty().getId());
						String oldClient = oldParty.getPartyName();
						oldStock.setClientName(oldClient);
						
					}
					oldStock.setItemMaster(item.get());
					resultStockMap.put("oldStock", oldStock);
					resultStockMap.put("stockSize", revisionNumbers.size());	
					
					stockList.add(resultStockMap);
					
				}
			}
			
			
		}
		
		return stockList;

		
	}
	




	public Map<String, Object> stockInfoEmail(String fileName) {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject","Daily Stock Report"); 
		emailContents.put("template","stock-info.html"); 
		emailContents.put("to", "anithapoojary846@gmail.com");
		emailContents.put("cc", "anitha@tek-nika.com");
		emailContents.put("month", Constants.currentDate());  
		return emailContents; 
	
		
	}
	
	//get stock history by stock id
	public List<Stock> getStockHistoryById(String stockId){
		AuditReader auditReader = AuditReaderFactory.get(em);
		ArrayList<Stock> auditedlist = new ArrayList<>(); 
		List<Number> stockList = auditReader.getRevisions(Stock.class, stockId);
		for (Number rev : stockList) {
			Stock auditedStock = auditReader.find(Stock.class,stockId, rev);
			if(!auditedStock.getReason().isEmpty()) {
				auditedlist.add(auditedStock);
			}
		}
		return auditedlist;
	}
	
	public List<Stock> getStockByDate(String date) throws ParseException {
		String d = date.replaceAll("/", "-");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");  
	    Date todaysDate = null;
	    todaysDate = sdf.parse(d);
	    Calendar c = Calendar.getInstance(); 
		c.setTime(todaysDate); 
		todaysDate = c.getTime();
		
		Timestamp selectedDate = convertDate.convertJavaDateToSqlDate(todaysDate);
		
		AuditReader auditReader = AuditReaderFactory.get(em);
		AuditQuery q = auditReader.createQuery().forRevisionsOfEntity(Stock.class, true, true);
		q.add(AuditEntity.property("updated").le(selectedDate)).addOrder(AuditEntity.property("updated").desc());
	
		@SuppressWarnings("unchecked")
		List<Stock>  revisionNumbers = q.getResultList();
		List<Stock> list = new ArrayList<>();
		for (Stock stock : revisionNumbers) {
			Optional<ItemMaster> item = itemMasterService.getItemById(stock.getItemMaster().getId());
			stock.setItemMaster(item.get());
			
			
			boolean exists = contains(list, stock);
			if(exists) {}
			else {
				list.add(stock);
			}
		}
		return list;
	}
	
	public boolean contains(List<Stock> stockList, Stock s1) {
	    for(Stock s : stockList) {
	        if(s.getItemMaster().getModel().equals(s1.getItemMaster().getModel())) {
	            return true;
	        }
	    }
		return false;
	    
	}
	
	//update stock on adding grn and dc
	public Stock updateStockQuantityFromGrn(String itemId, String clientId, float qty,String className,SalesOrder soObj) {
		
		//Optional<SalesItem> salesItemObj = salesService.getSalesItemById(soItemId);
		//SalesOrder soObj=soObj2.get().getSalesOrder();
		Party party=partyService.getPartyById(clientId);
		Stock stock = stockRepo.findStockByClientAndItemId(itemId,clientId);
		
		if(className=="grn") {
			if(stock == null) {
				Stock new_stock = new Stock();
				new_stock.setParty(party);
				new_stock.setQuantity(qty);
				String reason = Constants.PO_ACTIVITY_REASON +" "+party.getPartyName()+"-"+soObj.getClientPoNumber();
				new_stock.setReason(reason);
				new_stock.setItemMaster(itemMasterService.getItemById(itemId).get());
				new_stock.setActivity(Constants.PO_ACTIVITY_REASON);
				stockRepo.save(new_stock);
			}else {
				float new_quantity =  qty + stock.getQuantity();
				String reason = Constants.PO_ACTIVITY_REASON +" "+party.getPartyName()+"-"+soObj.getClientPoNumber();
				stock.setReason(reason);
				stock.setQuantity(new_quantity);
				stock.setActivity(Constants.PO_ACTIVITY_REASON);
				stockRepo.save(stock);
			}
		}else {
			if(stock == null) {
			}else {
				float new_quantity =  stock.getQuantity()-qty;
				if(new_quantity < 0)
					return stock;
				String reason = Constants.SO_ACTIVITY_REASON +" "+party.getPartyName()+"-"+soObj.getClientPoNumber();
				stock.setReason(reason);
				stock.setQuantity(new_quantity);
				stock.setActivity(Constants.SO_ACTIVITY_REASON);
				stockRepo.save(stock);
			}
		}
		return stock;
		
		
	}

	public List<Stock> findByFromAndToDate(Timestamp sqlFromDate, Timestamp sqlToDate) {
		List<Stock> stockList = stockRepo.findStockByDateUpdated(sqlFromDate, sqlToDate);
		return stockList;

	}

	public List<Stock> findStockByItemId(Timestamp sqlFromDate, Timestamp sqlToDate, String itemId) {
		List<Stock> stockObj = stockRepo.findStockByItemId(sqlFromDate,sqlToDate,itemId);
		return stockObj;

	}
	
	public List<Stock> findStockByActivity(String itemId,String activity) {
		List<Stock> stockObj =  stockRepo.findStockByActivity(itemId,activity);
		return stockObj;

	}

	public Stock updateStockAfterAssign(float existingquantity, String clientId, String itemId, float assignedQuantity, String storeName, String location) {
		// TODO Auto-generated method stub
		Stock stockObj = stockRepo.findStockByClientAndItemId(itemId, clientId);
		float currentQuantity = existingquantity - assignedQuantity;
		stockObj.setQuantity(currentQuantity);
		stockObj.setReason(Constants.ASSIGNMENT_REASON);
		stockObj.setActivity(Constants.ASSIGNMENT_REASON);
		//stockObj.setStoreName(storeName);
		//stockObj.setLocationInStore(location);
		stockRepo.save(stockObj);
		return null;
	}
	
	public Stock getLatestUpdatedStockByItemId(String itemId,Timestamp sqlFromDate,Timestamp sqlToDate) {
		
		Stock latestStock = null;
		List<Stock> stockList = stockRepo.findStockByUpdatedWithDescOrder(itemId,sqlFromDate,sqlToDate);
		if(!stockList.isEmpty()) {
			latestStock = stockList.get(0);
		System.out.println("latest Stock");
		}
		return latestStock;
		
	}

	public List<Stock> getStockListByItemId(String itemId) {
		List<Stock> stockList = stockRepo.getAllStockByItemId(itemId);
		return stockList;
	}

	public List<Stock> getStockListByItemIdAndClientId(String itemId, String clientId) {
		List<Stock> stockList = stockRepo.findStockListByClientName(itemId, clientId);
		return stockList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Object> getStockListByClientId(String id) {
		List<Object> outstandingReportList = new ArrayList();
		List<Stock> stockList = stockRepo.findStockListByClientId(id);
		for (Stock stock : stockList) {
			AuditReader auditReader = AuditReaderFactory.get(em);
			AuditQuery q = auditReader.createQuery().forRevisionsOfEntity(Stock.class, true, true);
			q.add(AuditEntity.id().eq(stock.getStockId()))
			.addOrder(AuditEntity.property("updated").desc());
			
			
			List<Stock>  revisionNumbers = q.getResultList();
			if(revisionNumbers.size()>0 && revisionNumbers.get(0).getQuantity()>0) {
				Optional<ItemMaster> item = itemMasterService.getItemById(revisionNumbers.get(0).getItemMaster().getId());
				
				System.out.println(item.get());
				//stock.setItemMaster(item.get());
//				System.out.println(revisionNumbers.get(0).getStockId());
//				System.out.println(revisionNumbers.get(0).getStockId()+"Item Obj"+revisionNumbers.get(0).getItemMaster());
				//List<SalesOrder> soList = salesService.getSalesListByPartyId(id);
				JSONObject object = new JSONObject();
				/*if(soList.size()==1) {
					object.put("clientPo", soList.get(0).getClientPoNumber());
					
				}else {
					for (SalesOrder salesOrder : soList) {
						List<SalesItem> soItems = salesOrder.getItems();
						for (SalesItem salesItem : soItems) {
							List<PurchaseItem> purchaseItemsList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItem.getId());
                            if(purchaseItemsList.size()>0) {
                            	if(purchaseItemsList.get(0).getModelNo().equalsIgnoreCase(revisionNumbers.get(0).getItemMaster().getId())) {
                            		object.put("clientPo", salesOrder.getClientPoNumber());
                            	}
                            }else {
                            	
                            }
						}
						
						
						}
					}*/
					List<PurchaseItem> poItemList=purchaseItemService.findByModelNumberWithRecentPoItem(item.get().getId());
					if(poItemList.size()>0) {
							object.put("price",poItemList.get(0).getUnitPrice());
					}else {
						object.put("price","");
					}
				
			//String units = itemObj.get().getItem_units().getName();
			object.put("model",item.get().getModel());
			object.put("itemName", item.get().getItemName());
			object.put("hsnCode",item.get().getHsnCode());
			object.put("units", item.get().getItem_units().getName());
			object.put("grnDate", revisionNumbers.get(0).getCreated());
			object.put("grnQty", revisionNumbers.get(0).getQuantity());
			outstandingReportList.add(object);
		}
		}
		
		return outstandingReportList;
	}
	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	public List<Stock> getStockListByItemAndDate(String itemId) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
	    Date todaysDate = new Date();;
	    Calendar c = Calendar.getInstance();
		c.setTime(todaysDate);
		todaysDate = c.getTime();
		
		Timestamp selectedDate = convertDate.convertJavaDateToSqlDate(todaysDate);
		
		List<Stock> latestStock = new ArrayList();
		List<Stock> stockList = stockRepo.getStockByItemIdWithDate(itemId, selectedDate);
		if(!stockList.isEmpty()) {
			latestStock.add(stockList.get(0));
		
		}
		return latestStock;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getStockForAllClient(){
		List<Party> partyList = partyService.getPartyListWhereStockExists();
		System.out.println(partyList.size());
		Map customMap = new HashMap();
		ArrayList list=new ArrayList<>();
		LinkedHashMap map = new LinkedHashMap();
		float totalQty=0;
		float total=0;
		for (Party party : partyList) {
			float qty=0;
			float totalprice=0;
			List<Stock> stockList = stockRepo.findStockListByPartyId(party.getId());
			for (Stock stock : stockList) {
				float price=0;
				float quantity=0;
				List<PurchaseItem> poItemList=purchaseItemService.findByModelNumberWithRecentPoItem(stock.getItemMaster().getId());
				/*for (PurchaseItem poItem : poItemList) {
					String salesItemId=poItem.getDescription();
					System.out.println(salesItemId);
					Optional<SalesItem> salesitem=salesService.getSalesItemObjById(salesItemId);
					System.out.println(salesitem.get().getSalesOrder().getParty().getId());
					if(salesitem.get().getSalesOrder().getParty().getId().equals(party.getId())) {
						price=price+poItem.getUnitPrice();
					}
				}*/
				quantity=stock.getQuantity();
				if(poItemList.size()>0) {
					price=poItemList.get(0).getUnitPrice();
				}
				price=quantity*price;
				System.out.println(price);
				
				totalprice=totalprice+price;
				qty=qty+stock.getQuantity();
			}
			total=totalprice+total;
			totalQty=totalQty+qty;
			map.put(party.getPartyName(), totalprice);
			
		}
		List<Entry<Party, Float>> nlist = new ArrayList<>(map.entrySet());
		nlist.sort(Entry.comparingByValue(Comparator.reverseOrder()));
		//nlist.forEach(System.out::println);
		for (int i = 0; i < 10; i++) {
			list.add(nlist.get(i));
		}
		
		//customMap.put("list", list);
		customMap.put("totalQty", total);
		list.add(customMap);
		System.out.println(customMap);
		return list;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getStockForCompanyStock(){
		List<Stock> stockList = stockRepo.findStockListByPartyId("C1255");
		LinkedHashMap map = new LinkedHashMap();
		ArrayList list=new ArrayList<>();
		for (Stock stock : stockList) {
			float price=0;
			float quantity=0;
			float unitPrice=0;
			List<PurchaseItem> poItemList=purchaseItemService.findByModelNumberWithRecentPoItem(stock.getItemMaster().getId());
			
			quantity=stock.getQuantity();
			if(poItemList.size()>0) {
				price=poItemList.get(0).getUnitPrice();
				unitPrice=poItemList.get(0).getUnitPrice();
			}
			price=quantity*price;
			System.out.println(price);
			map.put(stock.getItemMaster().getModel()+"&unitPrice="+unitPrice, price);
		}
		List<Entry<ItemMaster, Float>> nlist = new ArrayList<>(map.entrySet());
		nlist.sort(Entry.comparingByValue(Comparator.reverseOrder()));
		nlist.forEach(System.out::println);
		for (int i = 0; i < 10; i++) {
			list.add(nlist.get(i));
		}
		System.out.println(list);
		return list;
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getStockValueForAllItems(){
		List<ItemMaster> itemList = itemMasterService.getAllItemsWhereStockQtyNonZero();
		System.out.println("item size ="+itemList.size());
		Map itemMap = new HashMap();
		for (ItemMaster itemMaster : itemList) {
			LinkedHashMap map = new LinkedHashMap();
			float stockQty=0;
			float unitPrice=0;
			float stockValue=0;
			float stockSum=0;
			float avgStockValue=0;
			List<Stock> stockList=getStockList(itemMaster.getId());
			for (Stock stock : stockList) {
				stockQty=stockQty+stock.getQuantity();
			}
			List<PurchaseItem> poItemList=purchaseItemService.findByModelNumberWithRecentPoItem(itemMaster.getId());
			if (!poItemList.isEmpty()) {
				System.out.println("poItemList size"+poItemList.size());
				for (PurchaseItem purchaseItem : poItemList) {
					unitPrice=purchaseItem.getUnitPrice();
					stockValue=stockQty*unitPrice;
					map.put(purchaseItem.getPurchase_item_id()+"&Qty"+stockQty+"unitPrice$"+unitPrice, stockValue);
					stockSum=stockSum+stockValue;
				}
				System.out.println("stock sum="+stockSum);
				avgStockValue=(stockSum/poItemList.size());
				System.out.println("Avg stock value="+avgStockValue);
				System.out.println(map);
				itemMap.put(itemMaster.getModel()+"qty&"+stockQty, avgStockValue);
			}
			
			
		}
		List<Entry<ItemMaster, Float>> nlist = new ArrayList<>(itemMap.entrySet());
		nlist.sort(Entry.comparingByValue(Comparator.reverseOrder()));
		nlist.forEach(System.out::println);
		System.out.println(nlist);
		return nlist;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List getStockByCustomerForAllClient(){
		List<Party> partyList = partyService.getPartyListWhereStockExists();
		System.out.println(partyList.size());
		Map customMap = new HashMap();
		ArrayList list=new ArrayList<>();
		LinkedHashMap map = new LinkedHashMap();
		float totalQty=0;
		float total=0;
		for (Party party : partyList) {
			float qty=0;
			float totalprice=0;
			List<Stock> stockList = stockRepo.findStockListByPartyId(party.getId());
			for (Stock stock : stockList) {
				float price=0;
				float quantity=0;
				List<PurchaseItem> poItemList=purchaseItemService.findByModelNumberWithRecentPoItem(stock.getItemMaster().getId());
				/*for (PurchaseItem poItem : poItemList) {
					String salesItemId=poItem.getDescription();
					System.out.println(salesItemId);
					Optional<SalesItem> salesitem=salesService.getSalesItemObjById(salesItemId);
					System.out.println(salesitem.get().getSalesOrder().getParty().getId());
					if(salesitem.get().getSalesOrder().getParty().getId().equals(party.getId())) {
						price=price+poItem.getUnitPrice();
					}
				}*/
				quantity=stock.getQuantity();
				if(poItemList.size()>0) {
					price=poItemList.get(0).getUnitPrice();
				}
				price=quantity*price;
				System.out.println(price);
				
				totalprice=totalprice+price;
				qty=qty+stock.getQuantity();
			}
			total=totalprice+total;
			totalQty=totalQty+qty;
			map.put(party.getPartyName(), totalprice);
			
		}
		List<Entry<Party, Float>> nlist = new ArrayList<>(map.entrySet());
		nlist.sort(Entry.comparingByValue(Comparator.reverseOrder()));
		
		return nlist;
		
	}
}
