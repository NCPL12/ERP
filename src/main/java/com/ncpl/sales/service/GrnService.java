package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.GrnItemRepo;
import com.ncpl.sales.repository.GrnRepo;
import com.ncpl.sales.repository.PurchaseItemRepo;
import com.ncpl.sales.repository.SalesItemRepo;
import com.ncpl.sales.repository.SalesOrderDesignItemsRepo;
import com.ncpl.sales.util.DateConverterUtil;

@Service
public class GrnService {
	@Autowired
	GrnRepo grnRepo;
	@Autowired
	PurchaseOrderService purchaseOrderService;
	@Autowired
	GrnItemRepo grnItemRepo;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	SalesService salesService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	StockService stockService;
	@Autowired
	DeliveryChallanService dcService;
	@Autowired
	SalesOrderDesignService soDesignService;
	@Autowired
	SalesOrderDesignItemsRepo designItemRepo;
	@Autowired
	DateConverterUtil convertDate;
	@Autowired
	DeliveryChallanItemsRepo dcItemRepo;
	@Autowired
	PurchaseItemRepo poItemRepo;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	SalesItemRepo salesItemrepo;
	@Autowired
	PurchaseItemRepo purchaseItemRepo;
  
    
	public Grn saveGrn(Grn grn) {
		List<GrnItems> grnItemList = grn.getItems();
		
		String poNumber = grn.getPoNumber();
		Optional<PurchaseOrder> poObj = purchaseOrderService.findById(poNumber);
		String vendorId=poObj.get().getParty().getId();
		
		for (GrnItems grnItems : grnItemList) {

			// Get PO item id "in DB po item id as description"
			int poItemId = Integer.parseInt(grnItems.getDescription());
			Optional<PurchaseItem> purchaseItemObj = purchaseItemRepo.findById(poItemId);;
			String itemId = purchaseItemObj.get().getModelNo();
			// This holds sales item id
			String description = purchaseItemObj.get().getDescription();
			// Getting sales order to find out client
			boolean value = false;
			Optional<SalesItem> salesItemObj = salesService.getSalesItemObjById(description);
			SalesOrder soObj = salesItemObj.get().getSalesOrder();
			String clientId = soObj.getParty().getId();

			String className = "grn";

			// Grn quantity has to be updated in stock
			float grnQty = grnItems.getReceivedQuantity();
			// update stock on adding grn
			stockService.updateStockQuantityFromGrn(itemId, clientId, grnQty, className, soObj);
			itemMasterService.updateSupplier(itemId,vendorId,grnItems.getUnitPrice());
		}
		Grn grnObj = grnRepo.save(grn);
		return grnObj;
	}

	public List<Grn> getGrnList() {
		List<Grn> grnList = grnRepo.findAllGrn();
		for (Grn grn : grnList) {
			String poNumber = grn.getPoNumber();
										   
			Optional<PurchaseOrder> poObj = purchaseOrderService.findById(poNumber);
			String vendor = poObj.get().getParty().getPartyName();
									   
			Date poDate = poObj.get().getUpdated();
			grn.set("vendor", vendor);
			grn.set("poDate", poDate);
			float total = 0;
			List<GrnItems> grnItems=grn.getItems();
			for (GrnItems grnItem : grnItems) {
				total=total+grnItem.getAmount();
			}
			grn.set("total",total);

		}
		return grnList;

	}

	/**
	 * Optimized method to get paginated GRN list with batch loading to avoid N+1 queries
	 * @param pageNo - page number (0-based)
	 * @param pageSize - number of items per page
	 * @param searchValue - optional search term
	 * @return paginated list of GRNs with enriched data
	 */
	public Page<Grn> getPaginatedGrnList(int pageNo, int pageSize, String searchValue) {
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<Grn> pagedResult;
		
		if (searchValue != null && !searchValue.trim().isEmpty()) {
			// Add wildcards for LIKE query (removed CONCAT from HQL query)
			String searchKeyword = "%" + searchValue.trim() + "%";
			pagedResult = grnRepo.searchGrns(searchKeyword, paging);
		} else {
			pagedResult = grnRepo.findAllGrn(paging);
		}
		
		// Batch load PurchaseOrders to avoid N+1 queries
		List<Grn> grnList = pagedResult.getContent();
		if (!grnList.isEmpty()) {
			List<String> poNumbers = new ArrayList<>();
			for (Grn grn : grnList) {
				poNumbers.add(grn.getPoNumber());
			}
			
			// Batch fetch all PurchaseOrders in one query
			List<PurchaseOrder> purchaseOrders = purchaseOrderService.findByPoNumberIn(poNumbers);
			Map<String, PurchaseOrder> poMap = new HashMap<>();
			for (PurchaseOrder po : purchaseOrders) {
				poMap.put(po.getPoNumber(), po);
			}
			
			// Enrich GRN data
			for (Grn grn : grnList) {
				PurchaseOrder poObj = poMap.get(grn.getPoNumber());
				if (poObj != null) {
					String vendor = poObj.getParty().getPartyName();
					Date poDate = poObj.getUpdated();
					grn.set("vendor", vendor);
					grn.set("poDate", poDate);
				}
				
				// Calculate total from GRN items
				float total = 0;
				List<GrnItems> grnItems = grn.getItems();
				if (grnItems != null) {
					for (GrnItems grnItem : grnItems) {
						total += grnItem.getAmount();
					}
				}
				grn.set("total", total);
			}
		}
		
		return pagedResult;
	}
	
	/**
	 * Get total count of GRNs for pagination (only non-archived)
	 * @param searchValue - optional search term
	 * @return total count
	 */
	public long getGrnCount(String searchValue) {
		if (searchValue != null && !searchValue.trim().isEmpty()) {
			// Add wildcards for LIKE query (removed CONCAT from HQL query)
			String searchKeyword = "%" + searchValue.trim() + "%";
			return grnRepo.countSearchGrns(searchKeyword);
		} else {
			// Count only non-archived GRNs using optimized query
			return grnRepo.countAllNonArchivedGrns();
		}
	}

	public Optional<Grn> getGrnById(String grnId) {
		Optional<Grn> grn = grnRepo.findById(grnId);
		String poNumber = grn.get().getPoNumber();
		Optional<PurchaseOrder> poObj = purchaseOrderService.findById(poNumber);
		Date poDate = poObj.get().getUpdated();
		grn.get().set("poDate", poDate);
		return grn;
	}

	public List<GrnItems> getGrnListById(String grnId) {
		List<Grn> grnListById = grnRepo.getGrnListById(grnId);
		ArrayList<GrnItems> itemList = new ArrayList<GrnItems>();
		// get list of items for each sales order
		for (Grn grn : grnListById) {
			List<GrnItems> grnItemList = grn.getItems();
			itemList.addAll(grnItemList);
			for (GrnItems grnItems : itemList) {
				int poItemId = Integer.parseInt((grnItems.getDescription()));
				Optional<PurchaseItem> purchaseItem = purchaseItemService.getPurchaseItemById(poItemId);
				String salesItemId = purchaseItem.get().getDescription();
				boolean value = false;
				Optional<SalesItem> salesItem = salesService.getSalesItemById(salesItemId, value);
				grnItems.set("unitName", salesItem.get().getItem_units().getName());
			}
		}
		return itemList;
	}

	// get the list of grn items by po item id.
	public List<GrnItems> getGrnItemByPoItemId(String poItemId) {

		List<GrnItems> grnList = grnItemRepo.findByPoItemId(poItemId);
		return grnList;
	}
	
	public List<GrnItems> getGrnItemByPoItemIdWhereRcvdQtyNonZero(String poItemId) {

		List<GrnItems> grnList = grnItemRepo.findByPoItemIdWhereRcvdQtyNonZero(poItemId);
		return grnList;
	}

	public List<GrnItems> getGrnItemObjByPoItemId(String poItemId) {

		List<GrnItems> grnItemObj = grnItemRepo.findGrnObjByPoItemId(poItemId);
		return grnItemObj;
	}

	// Get Grn items list by date

	/*
	 * public Map findgrnListByDate(Timestamp sqlFromDate, Timestamp sqlToDate) {
	 * 
	 * List<GrnItems> grnList =
	 * grnItemRepo.findInwardQuantityBewteenDates(sqlFromDate, sqlToDate);
	 * 
	 * CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	 * CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
	 * Root<GrnItems> item = query.from(GrnItems.class); //Root<PurchaseItem> poitem
	 * = query.from(PurchaseItem.class);
	 * 
	 * //This will add all quantities for same items Expression<Float>
	 * totalReceivedQty =
	 * criteriaBuilder.sum(item.get("receivedQuantity")).as(Float.class);
	 * //Calculate sum of amount for same item Expression<Float> totalAmountEach =
	 * criteriaBuilder.sum(item.get("amount")).as(Float.class); //Calculate weighted
	 * amount for each item Expression<Number> weightedRate =
	 * criteriaBuilder.quot(totalAmountEach, totalReceivedQty); //Calculate total
	 * amount Expression<Number> totalAmount =
	 * criteriaBuilder.prod(totalReceivedQty,weightedRate);
	 * 
	 * List<Predicate> conditionsList = new ArrayList<Predicate>(); Predicate
	 * onStart =
	 * criteriaBuilder.greaterThanOrEqualTo(item.get("created"),sqlFromDate);
	 * Predicate onEnd = criteriaBuilder.lessThanOrEqualTo(item.get("updated"),
	 * sqlToDate); conditionsList.add(onStart); conditionsList.add(onEnd);
	 * 
	 * 
	 * 
	 * //query.where(criteriaBuilder.greaterThanOrEqualTo(item.get("created"),
	 * sqlFromDate));
	 * //query.where(criteriaBuilder.lessThanOrEqualTo(item.get("updated"),
	 * sqlToDate));
	 * 
	 * query.groupBy(item.get("description"));
	 * 
	 * query.multiselect( item.get("description"), //Purchase item id
	 * totalReceivedQty, weightedRate, totalAmount
	 * 
	 * );
	 * 
	 * Predicate fieldEquals = criteriaBuilder.equal(item.get("description"),
	 * poitem.get("purchase_item_id")); query.where(fieldEquals);
	 * query.select(poitem.get("description"));
	 * 
	 * TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
	 * List<Object[]> inwardList = typedQuery.getResultList(); for (Object[] objects
	 * : inwardList) { System.out.println("in object"+objects[0]); }
	 * 
	 * 
	 * 
	 * 
	 * public List<GrnItems> findInwardQuantityByItemsByDate(){ List<GrnItems>
	 * grnList = findInwardQuantityBewteenDates(); }
	 * 
	 * Map excelSheetValue = findOutwardQuantity(inwardList, sqlFromDate,
	 * sqlToDate);
	 * 
	 * return excelSheetValue; }
	 */

	// Get inward qty..
	/*
	 * public Map findgrnListByDate(Timestamp sqlFromDate, Timestamp sqlToDate) {
	 * 
	 * List<GrnItems> grnList =
	 * grnItemRepo.findInwardQuantityBewteenDates(sqlFromDate, sqlToDate);
	 * CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
	 * CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
	 * // CriteriaQuery<Object[]> query = criteriaBuilder.createTupleQuery();
	 * Root<GrnItems> item = query.from(GrnItems.class); // Root<PurchaseItem>
	 * poitem = query.from(PurchaseItem.class);
	 * 
	 * // This will add all quantities for same items Expression<Float>
	 * totalReceivedQty =
	 * criteriaBuilder.sum(item.get("receivedQuantity")).as(Float.class); //
	 * Calculate sum of amount for same item Expression<Float> totalAmountEach =
	 * criteriaBuilder.sum(item.get("amount")).as(Float.class); // Calculate
	 * weighted amount for each item Expression<Number> weightedRate =
	 * criteriaBuilder.quot(totalAmountEach, totalReceivedQty); // Calculate total
	 * amount Expression<Number> totalAmount =
	 * criteriaBuilder.prod(totalReceivedQty, weightedRate);
	 * 
	 * List<Predicate> conditionsList = new ArrayList<Predicate>(); Predicate
	 * onStart = criteriaBuilder.greaterThanOrEqualTo(item.get("created"),
	 * sqlFromDate); Predicate onEnd =
	 * criteriaBuilder.lessThanOrEqualTo(item.get("updated"), sqlToDate);
	 * conditionsList.add(onStart); conditionsList.add(onEnd);
	 * 
	 * query.multiselect(item.get("description"), // Purchase item id
	 * totalReceivedQty, weightedRate, totalAmount).where(conditionsList.toArray(new
	 * Predicate[] {}));
	 * 
	 * query.groupBy(item.get("description"));
	 * 
	 * TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
	 * List<Object[]> inwardList = typedQuery.getResultList();
	 * 
	 * Map excelSheetValue = findOutwardQuantity(inwardList, sqlFromDate,
	 * sqlToDate);
	 * 
	 * return excelSheetValue;
	 * 
	 * }
	 */
	
	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	public Map findgrnListByDate(Timestamp sqlFromDate, Timestamp sqlToDate) throws ParseException {

		List<GrnItems> grnList = grnItemRepo.findInwardQuantityBewteenDates(sqlToDate);

		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
		// CriteriaQuery<Object[]> query = criteriaBuilder.createTupleQuery();
		Root<GrnItems> item = query.from(GrnItems.class);
		// Root<PurchaseItem> poitem = query.from(PurchaseItem.class);

		// This will add all quantities for same items
		Expression<Float> totalReceivedQty = criteriaBuilder.sum(item.get("receivedQuantity")).as(Float.class);
		// Calculate sum of amount for same item
		Expression<Float> totalAmountEach = criteriaBuilder.sum(item.get("amount")).as(Float.class);
		// Calculate weighted amount for each item
		Expression<Number> weightedRate = criteriaBuilder.quot(totalAmountEach, totalReceivedQty);
		// Calculate total amount
		Expression<Number> totalAmount = criteriaBuilder.prod(totalReceivedQty, weightedRate);

		List<Predicate> conditionsList = new ArrayList<Predicate>();
		Predicate onStart = criteriaBuilder.greaterThanOrEqualTo(item.get("created"), sqlFromDate);
		Predicate onEnd = criteriaBuilder.lessThanOrEqualTo(item.get("updated"), sqlToDate);
		conditionsList.add(onStart);
		conditionsList.add(onEnd);

		query.multiselect(item.get("description"), // Purchase item id
				totalReceivedQty, weightedRate, totalAmount).where(conditionsList.toArray(new Predicate[] {}));

		query.groupBy(item.get("description"));

		TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
		List<Object[]> inwardList = typedQuery.getResultList();

		Map excelSheetValue = findOutwardQuantity(inwardList, sqlFromDate, sqlToDate);
		int size = excelSheetValue.keySet().size();

		Map newValues = findOPeningQuantityForGrnNotCreated(excelSheetValue, sqlFromDate, sqlToDate);
		Map<String, Map> recordsMap = new HashMap();
		recordsMap.put("grnlist", excelSheetValue);
		recordsMap.put("nogrnlist", newValues);
		return recordsMap;

	}
	
   /*
    * This function is for getting the list of items where no grn are created in the queried monthd
    * so here finding the grn list created lesser than the created from month and serching for dc 
    * and inserting into the existing map used for finding the data between present month..
    */
	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	private Map findOPeningQuantityForGrnNotCreated(Map excelSheetValue, Timestamp sqlFromDate, Timestamp sqlToDate)
			throws ParseException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
		// CriteriaQuery<Object[]> query = criteriaBuilder.createTupleQuery();
		Root<GrnItems> item = query.from(GrnItems.class);
		// Root<PurchaseItem> poitem = query.from(PurchaseItem.class);
		if (excelSheetValue.containsKey("Junction 3 Way- 19MM")) {
			System.out.println("stop");
		}
		Calendar c = Calendar.getInstance();
		Date fromDate = null;
		String c1 = sqlFromDate.toString();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		fromDate = sdf.parse(c1);
		c.setTime(fromDate);
		// c.add(Calendar.MONTH, -1);
		c.add(Calendar.MONTH, -1);
		Date previousMonthStartDate = c.getTime();
		Timestamp sqlFromDateReducing30Days = convertDate.convertJavaDateToSqlDate(previousMonthStartDate);
		// This will add all quantities for same items
		Expression<Float> totalReceivedQty = criteriaBuilder.sum(item.get("receivedQuantity")).as(Float.class);
		// Calculate sum of amount for same item
		Expression<Float> totalAmountEach = criteriaBuilder.sum(item.get("amount")).as(Float.class);
		// Calculate weighted amount for each item
		Expression<Number> weightedRate = criteriaBuilder.quot(totalAmountEach, totalReceivedQty);
		// Calculate total amount
		Expression<Number> totalAmount = criteriaBuilder.prod(totalReceivedQty, weightedRate);

		List<Predicate> conditionsList = new ArrayList<Predicate>();
		Predicate onStart = criteriaBuilder.lessThanOrEqualTo(item.get("created"), sqlFromDate);
		Predicate onEnd = criteriaBuilder.greaterThanOrEqualTo(item.get("updated"), sqlFromDateReducing30Days);
		conditionsList.add(onStart);
		// conditionsList.add(onEnd);

		query.multiselect(item.get("description"), // Purchase item id
				totalReceivedQty, weightedRate, totalAmount).where(conditionsList.toArray(new Predicate[] {}));

		query.groupBy(item.get("description"));
		TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
		List<Object[]> inwardList = typedQuery.getResultList();
//		System.out.println("received qty"+totalReceivedQty+"weightedRate"+weightedRate +"totalAmount"+totalAmount);
//		System.out.println("po Item"+inwardList.get(0));

		Map<String, Map> pMap = new HashMap<String, Map>();
		for (Object[] objects : inwardList) {
			Object poItem = objects[0];
			String poItemId = (String) poItem;
			System.out.println("po Item iddddd" + poItemId);
			Optional<PurchaseItem> purchaseItem = purchaseItemService.getPurchaseItemById(Integer.parseInt(poItemId));
			Optional<ItemMaster> items = itemMasterService.getItemById(purchaseItem.get().getModelNo());
			String itemId = items.get().getId();
			if (itemId.equalsIgnoreCase("ITEM-3354")) {
				System.out.println("inside item");
			}
			// if(!excelSheetValue.containsKey(items.get().getItemName() )) {
			if (items.get().getItemName().contains("Fingerprint, Face Time & Attendance Bio-metric Machine")) {
				System.out.println("inside if");
			}
			float grnRate = 0.0f;
			float grnTotal = 0.0f;
			Object quantity = objects[1];
			float grnQuant = (Float) quantity;
			Object rate = objects[2];
			if (rate == null) {

			} else {
				grnRate = (Float) rate;
			}
			Object amount = objects[3];
			if (amount == null) {

			} else {
				grnTotal = (Float) amount;
			}
			String soItemId = purchaseItem.get().getDescription();
			// List<DeliveryChallanItems> dcList =
			// dcService.findDcListByDate(sqlFromDateReducing30Days, sqlToDate, soItemId);
			// List<DeliveryChallanItems> dcList =
			// dcItemRepo.findByDateFrom(sqlToDate,soItemId);
			//List<DeliveryChallanItems> dcList = dcItemRepo.findDcListLessThanDate(sqlToDate);
			List<DeliveryChallanItems> dcList = dcItemRepo.findByDateFrom(sqlToDate,soItemId);
			
			List<DesignItems> designItems = designItemRepo.findDesignItemListByItemId(itemId);
			for (DesignItems designItemObj : designItems) {
				
				List<DeliveryChallanItems> dcItems = dcItemRepo.findByDateFrom(sqlToDate,designItemObj.getSalesOrderDesign().getSalesItemId());
	            for (DeliveryChallanItems dcObj : dcItems) {
	            	if(!dcObj.getDescription().equalsIgnoreCase(soItemId)) {
		            	dcList.add(dcObj);
		            	}
	            	
				}		
			}
			
			// List<DeliveryChallanItems> dcList =
			// dcItemRepo.findDcListBetweenDate(sqlFromDateReducing30Days, sqlToDate);
			// List<DeliveryChallanItems> dcList = dcItemRepo.(sqlFromDate,sqlToDate);
			if (dcList.size() > 0) {
				System.out.println("dcList dcList");
			}
			float dcItemsquantity = 0;
			float dcQuantity = 0;
			float dcPresentQty = 0;
			if (!excelSheetValue.containsKey(items.get().getItemName() + "/" + items.get().getModel())) {
			if (!pMap.containsKey(items.get().getItemName() + "/" + items.get().getModel())) {
				for (DeliveryChallanItems dcItem : dcList) {

					SalesOrderDesign designObj = soDesignService
							.findSalesOrderDesignObjBysalesItemId(dcItem.getDescription());
					if (designObj != null) {
						System.out.println("designObject " + designObj);
						System.out.println("design obj" + designObj.getId());
						DesignItems designItemsList = designItemRepo.findDesignItemObjByItemIdAndDesignId(itemId,
								designObj.getId());

//			float dcQuantity = 0.0f;
						System.out.println("item iddddd" + itemId);
						if (designItemsList != null) {
							if (itemId.equalsIgnoreCase(designItemsList.getItemId())
									&& designItemsList.getDeliveredQty() > 0) {
								// dcQuantity = designItemsList.getQuantity() * dcItemsquantity;
								dcQuantity = dcQuantity + designItemsList.getDeliveredQty();
								if (dcItem.getUpdated().getTime() >= sqlFromDate.getTime()
										&& dcItem.getUpdated().getTime() <= sqlToDate.getTime()) {
									dcPresentQty = dcPresentQty +designItemsList.getDeliveredQty();
								}
							} else {
								if (dcItem.getTodaysQty() > dcItem.getDeliveredQuantity()) {
									dcQuantity = dcQuantity + dcItem.getTodaysQty();
								} else {
									dcQuantity = dcQuantity + dcItem.getDeliveredQuantity();
								}

								if (dcItem.getUpdated().getTime() >= sqlFromDate.getTime()
										&& dcItem.getUpdated().getTime() <= sqlToDate.getTime()) {
									dcPresentQty = dcPresentQty + dcItem.getTodaysQty();
								}
							}
						}
					}
				}
			}
			}
			float closedBalnceQuant = 0;
			float closedBalnceValue = 0;
			float openingBalanceQuant = 0;
			float openingBalValue = 0;

//			AuditReader auditReader = AuditReaderFactory.get(entityManager);
//			AuditQuery q1 = auditReader.createQuery().forRevisionsOfEntity(Stock.class, true, true);
//		q1.add(AuditEntity.property("itemMaster").eq(items.get()))
//			//.add(AuditEntity.property("updated").ge(sqlFromDateReducing30Days))
//			.add(AuditEntity.property("updated").le(sqlFromDateReducing30Days))
//			.addOrder(AuditEntity.property("updated").desc());			
//////			add(AuditEntity.property("updated").le(sqlToDate)).
//////			add(AuditEntity.id().eq(itemId)).addOrder(AuditEntity.property("updated").desc());
//			List<Stock>  revisionNumbers1 = q1.getResultList();
//			if (pMap.containsKey(items.get().getItemName())) {
//		    if(revisionNumbers1.size()>0) {
//		    	grnQuant = revisionNumbers1.get(0).getQuantity();
//		    }
//			}

			/*
			 * if(grnQuant>0) { openingBalanceQuant =grnQuant; openingBalValue =
			 * openingBalanceQuant * grnRate; closedBalnceQuant = openingBalanceQuant -
			 * dcQuantity; closedBalnceValue = grnRate * closedBalnceQuant; }else {
			 * 
			 * }
			 */

			if (grnQuant > 0) {
				openingBalanceQuant = grnQuant;
				openingBalValue = openingBalanceQuant * grnRate;
				// closedBalnceQuant = openingBalanceQuant - dcQuantity;
				// closedBalnceValue = grnRate * closedBalnceQuant;
			} else {

			}

//		      if(revisionNumbers1.size()>0 ) {
//		    	  if(!pMap.containsKey(items.get().getItemName())) {
//		    		//  openingBalanceQuant = grnQuant + revisionNumbers1.get(0).getQuantity();
//		    		  //closedBalnceQuant = openingBalanceQuant  - dcQuantity;
//				}}
//			 

//			 if (lastUpdatedStock == null && dcQuantity > 0) {
//				    closedBalnceQuant = 0;
//					closedBalnceValue = grnRate * closedBalnceQuant;
//
//					openingBalanceQuant = closedBalnceQuant + dcQuantity - grnQuant;
//					openingBalValue = openingBalanceQuant * grnRate;
//			 }
//		//	List<Stock>  revisionNumbers = q.getResultList();
//			
//			if (lastUpdatedStock != null && dcQuantity > 0) {
//				closedBalnceQuant = lastUpdatedStock.getQuantity();
//				closedBalnceValue = grnRate * closedBalnceQuant;
//
//				openingBalanceQuant = closedBalnceQuant + dcQuantity - grnQuant;
//				openingBalValue = openingBalanceQuant * grnRate;
//			} else {
//				closedBalnceQuant = grnQuant;
//				closedBalnceValue = grnTotal;
//				
//				openingBalanceQuant = closedBalnceQuant + dcQuantity - grnQuant;
//				openingBalValue = openingBalanceQuant * grnRate;
//			}
			if (pMap.containsKey(items.get().getItemName() + "/" + items.get().getModel())) {
				Map cMap = pMap.get(items.get().getItemName() + "/" + items.get().getModel());

				float prevGrnQuantity = (float) cMap.get("grnQ1");
				prevGrnQuantity = (prevGrnQuantity + grnQuant);
				float prevgrnUnitPrice = (float) cMap.get("grnR1");
				prevgrnUnitPrice = (prevgrnUnitPrice + grnRate) / 2;
				float prevgrnValue = prevgrnUnitPrice * prevGrnQuantity;

				float prevDcQuantity = (float) cMap.get("dcQ1");
				// prevDcQuantity = (prevDcQuantity + dcQuantity) / 2;
				prevDcQuantity = (prevDcQuantity + dcQuantity);
				float prevDcValue = prevgrnUnitPrice * prevDcQuantity;

//				float preclosingQuantity = (float) cMap.get("clQ1");
//				preclosingQuantity = (preclosingQuantity + closedBalnceQuant);
//				float prevcloseValue = prevgrnUnitPrice * preclosingQuantity;

				float prevopenQuantity = (float) cMap.get("openQ1");
				prevopenQuantity = (prevopenQuantity + openingBalanceQuant);
				float prevOpenValue = prevgrnUnitPrice * prevopenQuantity;

				float preclosingQuantity = (float) cMap.get("clQ1");
				preclosingQuantity = prevopenQuantity - prevDcQuantity;
				float prevcloseValue = prevgrnUnitPrice * preclosingQuantity;
//				if(!dcList.isEmpty()) {
//                if(prevDcQuantity>0 ) {
//                	 prevopenQuantity = preclosingQuantity;
//               	 prevDcQuantity=dcPresentQty;
//               }
//			}
				// prevDcValue = prevDcValue + (prevdcUnitPrice*prevDcQuantity);
				cMap.put("grnR1", prevgrnUnitPrice);
				cMap.put("grnQ1", 0.0f);
				cMap.put("grnV1", 0.0f);
				cMap.put("dcR1", prevgrnUnitPrice);
				cMap.put("dcQ1", prevDcQuantity);
				cMap.put("dcV1", prevDcValue);
				cMap.put("clR1", prevgrnUnitPrice);
				cMap.put("clQ1", preclosingQuantity);
				cMap.put("clV1", prevcloseValue);
				cMap.put("openQ1", prevopenQuantity);
				cMap.put("openR1", prevgrnUnitPrice);
				cMap.put("openV1", prevOpenValue);
			} else {
				Map<String, Object> ExcelcredMap = new HashMap<String, Object>();
//				if(!dcList.isEmpty()) {  
//				if(dcQuantity>0 ) {
//					  openingBalanceQuant = closedBalnceQuant;
//					  dcQuantity=dcPresentQty;
//	                }}
				openingBalanceQuant = openingBalanceQuant - dcQuantity + dcPresentQty;

				closedBalnceQuant = openingBalanceQuant - dcPresentQty;
				ExcelcredMap.put("dcR1", grnRate);
				ExcelcredMap.put("dcQ1", dcPresentQty);
				ExcelcredMap.put("dcV1", grnRate * dcQuantity);
				ExcelcredMap.put("grnR1", grnRate);
				ExcelcredMap.put("grnQ1", 0.0f);
				ExcelcredMap.put("grnV1", 0.0f);
				ExcelcredMap.put("clR1", grnRate);
				ExcelcredMap.put("clQ1", closedBalnceQuant);
				ExcelcredMap.put("clV1", closedBalnceQuant * grnRate);
				ExcelcredMap.put("openQ1", openingBalanceQuant);
				ExcelcredMap.put("openR1", grnRate);
				ExcelcredMap.put("openV1", openingBalanceQuant * grnRate);
				ExcelcredMap.put("particulars", items.get().getItemName() + "/" + items.get().getModel());
				// ExcelcredMap.put(poItemId, poItemId);
				if (!excelSheetValue.containsKey(items.get().getItemName() + "/" + items.get().getModel())) {
					pMap.put(items.get().getItemName() + "/" + items.get().getModel(), ExcelcredMap);
				}
			}
			System.out.println("Inside object class");
		}
		// }
		return pMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public Map findOutwardQuantity(List<Object[]> inwardList, Timestamp sqlFromDate, Timestamp sqlToDate) {
		Map<String, Map> pMap = new HashMap<String, Map>();
		for (Object[] objects : inwardList) {
			Object poItem = objects[0];
			String poItemId = (String) poItem;
			System.out.println("po Item iddddd" + poItemId);
			Optional<PurchaseItem> purchaseItem = purchaseItemService.getPurchaseItemById(Integer.parseInt(poItemId));
			Optional<ItemMaster> item = itemMasterService.getItemById(purchaseItem.get().getModelNo());
			String itemId = item.get().getId();
			if (itemId.equalsIgnoreCase("ITEM-3354")) {
				System.out.println("inside item");
			}
			if (item.get().getItemName().contains("BNC")) {
				System.out.println("inside if");
			}
			float grnRate = 0.0f;
			float grnTotal = 0.0f;
			Object quantity = objects[1];
			float grnQuant = (Float) quantity;
			Object rate = objects[2];
			if (rate == null) {

			} else {
				grnRate = (Float) rate;
			}
			Object amount = objects[3];
			if (amount == null) {

			} else {
				grnTotal = (Float) amount;
			}
			String soItemId = purchaseItem.get().getDescription();
			// List<DeliveryChallanItems> dcList = dcService.findDcListByDate(sqlFromDate,
			// sqlToDate, soItemId);
			List<DeliveryChallanItems> dcList = dcItemRepo.findDcListBetweenDate(sqlFromDate, sqlToDate,soItemId);
			List<DesignItems> designItems = designItemRepo.findDesignItemListByItemId(itemId);
			for (DesignItems designItemObj : designItems) {
				
				List<DeliveryChallanItems> dcItems = dcItemRepo.findByDate(sqlFromDate, sqlToDate,designItemObj.getSalesOrderDesign().getSalesItemId());
	            for (DeliveryChallanItems dcObj : dcItems) {
	            	if(!dcObj.getDescription().equalsIgnoreCase(soItemId)) {
	            	dcList.add(dcObj);
	            	}
				}		
			}
			if (dcList.size() > 0) {
				System.out.println("dcList dcList");
			}
			float dcItemsquantity = 0;
			float dcQuantity = 0;
			if (!pMap.containsKey(item.get().getItemName() + "/" + item.get().getModel())) {
				for (DeliveryChallanItems dcItem : dcList) {
					// dcItemsquantity = dcItemsquantity + dcItem.getDeliveredQuantity();
					// dcItemsquantity = dcItemsquantity + dcItem.getTodaysQty();

					if (dcList.size() > 0) {
						// SalesOrderDesign designObj =
						// soDesignService.findSalesOrderDesignObjBysalesItemIdAndDate(dcItem.getDescription(),
						// sqlFromDate, sqlToDate);
						SalesOrderDesign designObj = soDesignService
								.findSalesOrderDesignObjBysalesItemId(dcItem.getDescription());
//////			float dcQuantity = 0.0f;
						if (designObj != null) {
							System.out.println("designObject " + designObj);
							System.out.println("design obj" + designObj);
							DesignItems designItemsList = designItemRepo.findDesignItemObjByItemIdAndDesignId(itemId,
									designObj.getId());
//////			float dcQuantity = 0.0f;
//				System.out.println("item iddddd" + itemId);
							if (designItemsList != null) {
								if (itemId.equalsIgnoreCase(designItemsList.getItemId())
										&& designItemsList.getDeliveredQty() > 0) {
//					dcQuantity = designItemsList.getQuantity() * dcItemsquantity;
									dcQuantity = dcQuantity + designItemsList.getDeliveredQty();
								} else {
									if (dcItem.getTodaysQty() > dcItem.getDeliveredQuantity()) {
										dcQuantity = dcQuantity + dcItem.getTodaysQty();
									} else {
										dcQuantity = dcQuantity + dcItem.getDeliveredQuantity();
									}

								}
							}
						}
					}
				}
			}
			float closedBalnceQuant = 0;
			float closedBalnceValue = 0;
			float openingBalanceQuant = 0;
			float openingBalValue = 0;

//			// Stock lastUpdatedStock = stockService.getLatestUpdatedStockByItemId(itemId,
//			// sqlFromDate, sqlToDate);
//			AuditReader auditReader = AuditReaderFactory.get(entityManager);
//			AuditQuery q = auditReader.createQuery().forRevisionsOfEntity(Stock.class, true, true);
//			q.add(AuditEntity.property("itemMaster").eq(item.get()))
//					.add(AuditEntity.property("updated").ge(sqlFromDate))
//					.add(AuditEntity.property("updated").le(sqlToDate))
//					.addOrder(AuditEntity.property("updated").desc());
////			q.add(AuditEntity.property("updated").ge(sqlFromDate)).
////			add(AuditEntity.property("updated").le(sqlToDate)).
////			add(AuditEntity.id().eq(itemId)).addOrder(AuditEntity.property("updated").desc());
//			List<Stock> revisionNumbers = q.getResultList();
//			if (revisionNumbers.size() > 0) {
//				Stock lastUpdatedStock = revisionNumbers.get(0);
//			}
//
//			openingBalanceQuant = getOpeningQuant(item.get().getId(),sqlFromDate);
//			
//			AuditQuery q1 = auditReader.createQuery().forRevisionsOfEntity(Stock.class, true, true);
//			q1.add(AuditEntity.property("itemMaster").eq(item.get()))
//					.add(AuditEntity.property("updated").le(sqlFromDate))
//					.addOrder(AuditEntity.property("updated").desc());
////			q.add(AuditEntity.property("updated").ge(sqlFromDate)).
////			add(AuditEntity.property("updated").le(sqlToDate)).
////			add(AuditEntity.id().eq(itemId)).addOrder(AuditEntity.property("updated").desc());
//			List<Stock> revisionNumbers1 = q1.getResultList();
//			Stock openingStock = null;
//			if (revisionNumbers1.size() > 0) {
//				openingStock = revisionNumbers1.get(0);
//			}
			openingBalanceQuant = getOpeningQuant(item.get().getId(),sqlFromDate,sqlToDate);
			if (!pMap.containsKey(item.get().getItemName() + "/" + item.get().getModel())) {

				// openingBalanceQuant = 0;
				openingBalValue = openingBalanceQuant * grnRate;
				closedBalnceQuant = openingBalanceQuant + grnQuant - dcQuantity;
				closedBalnceValue = grnRate * closedBalnceQuant;

			} else {
				openingBalanceQuant = 0;
				openingBalValue = openingBalanceQuant * grnRate;
				closedBalnceQuant = openingBalanceQuant + grnQuant - dcQuantity;
				closedBalnceValue = grnRate * closedBalnceQuant;
			}

//			 if (lastUpdatedStock == null && dcQuantity > 0) {
//				    closedBalnceQuant = 0;
//					closedBalnceValue = grnRate * closedBalnceQuant;
//
//					openingBalanceQuant = closedBalnceQuant + dcQuantity - grnQuant;
//					openingBalValue = openingBalanceQuant * grnRate;
//			 }
//		//	List<Stock>  revisionNumbers = q.getResultList();
//			
//			if (lastUpdatedStock != null && dcQuantity > 0) {
//				closedBalnceQuant = lastUpdatedStock.getQuantity();
//				closedBalnceValue = grnRate * closedBalnceQuant;
//
//				openingBalanceQuant = closedBalnceQuant + dcQuantity - grnQuant;
//				openingBalValue = openingBalanceQuant * grnRate;
//			} else {
//				closedBalnceQuant = grnQuant;
//				closedBalnceValue = grnTotal;
//				
//				openingBalanceQuant = closedBalnceQuant + dcQuantity - grnQuant;
//				openingBalValue = openingBalanceQuant * grnRate;
//			}
			if (pMap.containsKey(item.get().getItemName() + "/" + item.get().getModel())) {
				Map cMap = pMap.get(item.get().getItemName() + "/" + item.get().getModel());

				float prevGrnQuantity = (float) cMap.get("grnQ1");
				prevGrnQuantity = (prevGrnQuantity + grnQuant);
				float prevgrnUnitPrice = (float) cMap.get("grnR1");
				prevgrnUnitPrice = (prevgrnUnitPrice + grnRate) / 2;
				float prevgrnValue = prevgrnUnitPrice * prevGrnQuantity;

				float prevDcQuantity = (float) cMap.get("dcQ1");
				// prevDcQuantity = (prevDcQuantity + dcQuantity) / 2;
				prevDcQuantity = (prevDcQuantity + dcQuantity);
				float prevDcValue = prevgrnUnitPrice * prevDcQuantity;

				float preclosingQuantity = (float) cMap.get("clQ1");
				preclosingQuantity = (preclosingQuantity + closedBalnceQuant);
				float prevcloseValue = prevgrnUnitPrice * preclosingQuantity;

				float prevopenQuantity = (float) cMap.get("openQ1");
				prevopenQuantity = (prevopenQuantity + openingBalanceQuant);
				float prevOpenValue = prevgrnUnitPrice * prevopenQuantity;

				// prevDcValue = prevDcValue + (prevdcUnitPrice*prevDcQuantity);
				cMap.put("grnR1", prevgrnUnitPrice);
				cMap.put("grnQ1", prevGrnQuantity);
				cMap.put("grnV1", prevgrnValue);
				cMap.put("dcR1", prevgrnUnitPrice);
				cMap.put("dcQ1", prevDcQuantity);
				cMap.put("dcV1", prevDcValue);
				cMap.put("clR1", prevgrnUnitPrice);
				cMap.put("clQ1", preclosingQuantity);
				cMap.put("clV1", prevcloseValue);
				cMap.put("openQ1", prevopenQuantity);
				cMap.put("openR1", prevgrnUnitPrice);
				cMap.put("openV1", prevOpenValue);
			} else {
				Map<String, Object> ExcelcredMap = new HashMap<String, Object>();
				ExcelcredMap.put("dcR1", grnRate);
				ExcelcredMap.put("dcQ1", dcQuantity);
				ExcelcredMap.put("dcV1", grnRate * dcQuantity);
				ExcelcredMap.put("grnR1", grnRate);
				ExcelcredMap.put("grnQ1", grnQuant);
				ExcelcredMap.put("grnV1", grnTotal);
				ExcelcredMap.put("clR1", grnRate);
				ExcelcredMap.put("clQ1", closedBalnceQuant);
				ExcelcredMap.put("clV1", closedBalnceValue);
				ExcelcredMap.put("openQ1", openingBalanceQuant);
				ExcelcredMap.put("openR1", grnRate);
				ExcelcredMap.put("openV1", openingBalValue);
				ExcelcredMap.put("particulars", item.get().getItemName() + "/" + item.get().getModel());
				// ExcelcredMap.put(poItemId, poItemId);

				pMap.put(item.get().getItemName() + "/" + item.get().getModel(), ExcelcredMap);
			}
			System.out.println("Inside object class");
		}
		return pMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private float getOpeningQuant(String itemId, Timestamp sqlFromDate, Timestamp sqlToDate) {
		float openingBalance =0;
		if (itemId.equalsIgnoreCase("ITEM-3361") ) {
			System.out.println("inside item");
		}
		List<PurchaseItem> poItemList = poItemRepo.findByModelNumber(itemId);
		float grn = 0;
		float dcQuantity = 0;
		int i=0;
		List<GrnItems> grnList = new ArrayList();
		List<DeliveryChallanItems> dcList =  new ArrayList();
		for (PurchaseItem purchaseItem : poItemList) {
			grnList = grnItemRepo.findByPoItemIdAndUpdatedDate(Integer.toString(purchaseItem.getPurchase_item_id()),sqlFromDate);
		    for (GrnItems grnItem : grnList) {
		    	grn = grn + grnItem.getReceivedQuantity();
		    	i++;
			}
		}
		
		for (PurchaseItem purchaseItem : poItemList) {
		 List<DeliveryChallanItems>dcListByPoItemSo =  dcItemRepo.findByDateFrom(sqlFromDate,purchaseItem.getDescription());
		 dcList.addAll(dcListByPoItemSo);
		 
		
		List<DesignItems> designItems = designItemRepo.findDesignItemListByItemId(itemId);
		for (DesignItems designItemObj : designItems) {
			
			List<DeliveryChallanItems> dcItems = dcItemRepo.findByDateFrom(sqlFromDate,designItemObj.getSalesOrderDesign().getSalesItemId());
            for (DeliveryChallanItems dcObj : dcItems) {
            	
	            	dcList.add(dcObj);
            }
            	
			}		
		}
		
		
		
	
	    Set<DeliveryChallanItems> dcItemSet = new HashSet(dcList);
	    
		System.out.println("dc list size"+dcList.size());
		dcQuantity=0;
		List<DeliveryChallanItems> dcList1 = dcItemRepo.findDcListLessThanDate(sqlFromDate);
		System.out.println("dc list size"+dcList1.size());
		for (DeliveryChallanItems dcItem : dcItemSet) {
			
			if (dcList.size() > 0) {

				SalesOrderDesign designObj = soDesignService
						.findSalesOrderDesignObjBysalesItemId(dcItem.getDescription());

				if (designObj != null) {
					System.out.println("designObject " + designObj);
					System.out.println("design obj" + designObj);
					DesignItems designItemsList = designItemRepo.findDesignItemObjByItemIdAndDesignId(itemId,
							designObj.getId());

					if (designItemsList != null) {
						if (itemId.equalsIgnoreCase(designItemsList.getItemId())
								&& designItemsList.getDeliveredQty() > 0) {
							System.out.println("dc item idd"+dcItem.getDcItemId());
							dcQuantity = dcQuantity + designItemsList.getDeliveredQty();
						} else {
							if (dcItem.getTodaysQty() > dcItem.getDeliveredQuantity()) {
								System.out.println("dc item idd"+dcItem.getDcItemId());
								dcQuantity = dcQuantity + dcItem.getTodaysQty();
							} else {
								System.out.println("dc item idd"+dcItem.getDcItemId());
								dcQuantity = dcQuantity + dcItem.getDeliveredQuantity();
							}
						}
					} // Design items null check
				} // Design obj null check
			} // End of dcList size if condition
		} // DcItems list loop
	
		openingBalance = grn - dcQuantity;
		
		return openingBalance;
	}

	public List<Grn> findGrnByPoNumber(String poNumber) {
		List<Grn> grnList = grnRepo.findGrnListByPoNumber(poNumber);
		return grnList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<GrnItems> getGrnItemsListByPoNo(String poNo) {
		// TODO Auto-generated method stub
		List<GrnItems> grnItemsList =new ArrayList();
		List<Grn> grnList = grnRepo.findGrnListByPoNumber(poNo);
		for (Grn grn : grnList) {
			List<GrnItems> grnItems = grnItemRepo.findGrnItemsByGrnId(grn.getGrnId());
			for (GrnItems grnItemObj : grnItems) {
				grnItemsList.add(grnItemObj);
			}
		}
		return grnItemsList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Grn> getGrnListByItemId(String modelNo) {
		ItemMaster item = itemMasterService.getItemByModelNo(modelNo.trim());
		ArrayList<PurchaseOrder> poList=new ArrayList<PurchaseOrder>();
		ArrayList<Grn> grn;
		Set set = new HashSet();
		if(item==null) {
			grn=new ArrayList<Grn>(set);
		}else{
		List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemsByModelNumber(item.getId());
		
		for (PurchaseItem purchaseItem : poItemList) {
			PurchaseOrder po=purchaseItem.getPurchaseOrder();
			poList.add(po);
		}
		
		
		for (PurchaseOrder po : poList) {
			String poNumber = po.getPoNumber();
			List<Grn> grnList = findGrnByPoNumber(poNumber);
			for (Grn grnObject : grnList) {
				String poNum = grnObject.getPoNumber();
				Optional<PurchaseOrder> poObj = purchaseOrderService.findById(poNum);
				String vendor = poObj.get().getParty().getPartyName();
				Date poDate = poObj.get().getUpdated();
				grnObject.set("vendor", vendor);
				grnObject.set("poDate", poDate);
				set.add(grnObject);
			}
			
			
		}
		grn = new ArrayList<Grn>(set);
		
		}
		
		return grn;
	}

	public List<Grn> getGrnListArchived() {
		List<Grn> grnList = grnRepo.findAllGrnArchived();
		for (Grn grn : grnList) {
			String poNumber = grn.getPoNumber();
			Optional<PurchaseOrder> poObj = purchaseOrderService.findById(poNumber);
			String vendor = poObj.get().getParty().getPartyName();
			Date poDate = poObj.get().getUpdated();
			grn.set("vendor", vendor);
			grn.set("poDate", poDate);

		}
		return grnList;
	}

	public void archiveGrn(String grnNum) {
		Optional<Grn> grn = grnRepo.findById(grnNum);
		grn.get().setArchive(true);
		grnRepo.save(grn.get());
		
	}

	public void unArchiveGrn(String grnNum) {
		Optional<Grn> grn  = grnRepo.findById(grnNum);
		grn.get().setArchive(false);
		grnRepo.save(grn.get());
		
	}

	public List<GrnItems> findgrnListByDateandRegion(Timestamp sqlFromDate, Timestamp sqlToDate) {
		List<GrnItems> grnItemsList= grnItemRepo.findByDate(sqlFromDate,sqlToDate);
		return grnItemsList;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getPresentStockQtyForModel(String modelNo,String poItemId) {
		Map<String, Object> response = new HashMap<>();
		ItemMaster item=itemMasterService.getItemByModelNo(modelNo);
		Optional<PurchaseItem> purchaseItem = purchaseItemService.getPurchaseItemById(Integer.parseInt(poItemId));
		String salesItemId=purchaseItem.get().getDescription();
		Optional<SalesItem> salesItem=salesService.getSalesItemObjById(salesItemId);
		String partyId=salesItem.get().getSalesOrder().getParty().getId();
		List<Stock> stock=stockService.getStockListByItemIdAndClientId(item.getId(), partyId);
		response.put("presentQty", stock.get(0).getQuantity());
		return response;
		
	}
	
	
	
	
}
