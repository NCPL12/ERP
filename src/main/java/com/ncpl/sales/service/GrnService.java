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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import org.springframework.data.domain.Sort;
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
	private static final Logger log = LoggerFactory.getLogger(GrnService.class);
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
	 * Get paginated list of GRNs with sorting support
	 * @param pageNo - page number (0-based)
	 * @param pageSize - number of items per page
	 * @param searchValue - optional search term
	 * @param sortField - field to sort by
	 * @param sortDirection - sort direction (asc/desc)
	 * @return paginated list of GRNs with enriched data
	 */
	public Page<Grn> getPaginatedGrnList(int pageNo, int pageSize, String searchValue, String sortField, String sortDirection) {
		// Create sort object
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Sort sort = Sort.by(direction, sortField);
		Pageable paging = PageRequest.of(pageNo, pageSize, sort);
		Page<Grn> pagedResult;
		
		if (searchValue != null && !searchValue.trim().isEmpty()) {
			// Add wildcards for LIKE query
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

	/**
	 * Column-specific search: only non-empty params are applied (each filters its own column).
	 * Pass null or empty for columns that should not filter.
	 * searchPoDate: raw string (e.g. "13-05-25"); repo adds LIKE wildcards.
	 */
	public Page<Grn> getPaginatedGrnListByColumns(int pageNo, int pageSize,
			String searchGrnId, String searchPoNumber, String searchPoDate, String searchInvoiceNo, String searchVendor,
			String sortField, String sortDirection) {
		Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Sort sort = Sort.by(direction, sortField);
		Pageable paging = PageRequest.of(pageNo, pageSize, sort);
		String grnIdKw = (searchGrnId != null && !searchGrnId.trim().isEmpty()) ? "%" + searchGrnId.trim() + "%" : null;
		String poNumberKw = (searchPoNumber != null && !searchPoNumber.trim().isEmpty()) ? "%" + searchPoNumber.trim() + "%" : null;
		String poDateKw = (searchPoDate != null && !searchPoDate.trim().isEmpty()) ? searchPoDate.trim() : null; // repo uses CONCAT('%', :searchPoDate, '%')
		String invoiceNoKw = (searchInvoiceNo != null && !searchInvoiceNo.trim().isEmpty()) ? "%" + searchInvoiceNo.trim() + "%" : null;
		String vendorKw = (searchVendor != null && !searchVendor.trim().isEmpty()) ? "%" + searchVendor.trim() + "%" : null;
		boolean hasColumnFilter = (grnIdKw != null || poNumberKw != null || poDateKw != null || invoiceNoKw != null || vendorKw != null);
		Page<Grn> pagedResult = hasColumnFilter
				? grnRepo.searchGrnsByColumns(grnIdKw, poNumberKw, poDateKw, invoiceNoKw, vendorKw, paging)
				: grnRepo.findAllGrn(paging);
		List<Grn> grnList = pagedResult.getContent();
		if (!grnList.isEmpty()) {
			List<String> poNumbers = new ArrayList<>();
			for (Grn grn : grnList) {
				poNumbers.add(grn.getPoNumber());
			}
			List<PurchaseOrder> purchaseOrders = purchaseOrderService.findByPoNumberIn(poNumbers);
			Map<String, PurchaseOrder> poMap = new HashMap<>();
			for (PurchaseOrder po : purchaseOrders) {
				poMap.put(po.getPoNumber(), po);
			}
			for (Grn grn : grnList) {
				PurchaseOrder poObj = poMap.get(grn.getPoNumber());
				if (poObj != null) {
					grn.set("vendor", poObj.getParty().getPartyName());
					grn.set("poDate", poObj.getUpdated());
				}
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

	public long getGrnCountByColumns(String searchGrnId, String searchPoNumber, String searchPoDate, String searchInvoiceNo, String searchVendor) {
		String grnIdKw = (searchGrnId != null && !searchGrnId.trim().isEmpty()) ? "%" + searchGrnId.trim() + "%" : null;
		String poNumberKw = (searchPoNumber != null && !searchPoNumber.trim().isEmpty()) ? "%" + searchPoNumber.trim() + "%" : null;
		String poDateKw = (searchPoDate != null && !searchPoDate.trim().isEmpty()) ? searchPoDate.trim() : null;
		String invoiceNoKw = (searchInvoiceNo != null && !searchInvoiceNo.trim().isEmpty()) ? "%" + searchInvoiceNo.trim() + "%" : null;
		String vendorKw = (searchVendor != null && !searchVendor.trim().isEmpty()) ? "%" + searchVendor.trim() + "%" : null;
		boolean hasColumnFilter = (grnIdKw != null || poNumberKw != null || poDateKw != null || invoiceNoKw != null || vendorKw != null);
		return hasColumnFilter
				? grnRepo.countSearchGrnsByColumns(grnIdKw, poNumberKw, poDateKw, invoiceNoKw, vendorKw)
				: grnRepo.countAllNonArchivedGrns();
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
		long start = System.currentTimeMillis();
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
		log.info("Stock summary: {} inward items found in {}ms", inwardList.size(), System.currentTimeMillis() - start);

		Map<String, Object> caches = new HashMap<>();
		caches.put("purchaseItemCache", new HashMap<Integer, Optional<PurchaseItem>>());
		caches.put("itemMasterCache", new HashMap<String, Optional<ItemMaster>>());
		caches.put("designItemsCache", new HashMap<String, List<DesignItems>>());
		caches.put("soDesignCache", new HashMap<String, SalesOrderDesign>());
		caches.put("openingQuantCache", new HashMap<String, Float>());
		caches.put("poItemsByModelCache", new HashMap<String, List<PurchaseItem>>());
		caches.put("designItemObjCache", new HashMap<String, DesignItems>());

		List<DeliveryChallanItems> allDcInRange = dcItemRepo.findByUpdatedBetween(sqlFromDate, sqlToDate);
		Map<String, List<DeliveryChallanItems>> dcByDescription = new HashMap<>();
		for (DeliveryChallanItems dc : allDcInRange) {
			dcByDescription.computeIfAbsent(dc.getDescription(), k -> new ArrayList<>()).add(dc);
		}
		caches.put("dcItemsBetweenDates", dcByDescription);
		log.info("Stock summary: {} DC items in date range", allDcInRange.size());

		// Preload all DC items up to toDate/fromDate to avoid N+1 in opening quantity and outward
		Map<String, List<DeliveryChallanItems>> dcUpToToDate = new HashMap<>();
		for (DeliveryChallanItems dc : dcItemRepo.findDcListLessThanDate(sqlToDate)) {
			dcUpToToDate.computeIfAbsent(dc.getDescription(), k -> new ArrayList<>()).add(dc);
		}
		caches.put("dcItemsUpToToDate", dcUpToToDate);
		Map<String, List<DeliveryChallanItems>> dcUpToFromDate = new HashMap<>();
		for (DeliveryChallanItems dc : dcItemRepo.findDcListLessThanDate(sqlFromDate)) {
			dcUpToFromDate.computeIfAbsent(dc.getDescription(), k -> new ArrayList<>()).add(dc);
		}
		caches.put("dcItemsUpToFromDate", dcUpToFromDate);
		log.info("Stock summary: DC preload done in {}ms", System.currentTimeMillis() - start);

		Map excelSheetValue = findOutwardQuantity(inwardList, sqlFromDate, sqlToDate, caches);
		log.info("Stock summary: outward done in {}ms, {} items", System.currentTimeMillis() - start, excelSheetValue.keySet().size());

		Map newValues = findOPeningQuantityForGrnNotCreated(excelSheetValue, sqlFromDate, sqlToDate, caches);
		log.info("Stock summary: opening qty done in {}ms", System.currentTimeMillis() - start);

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
	private Map findOPeningQuantityForGrnNotCreated(Map excelSheetValue, Timestamp sqlFromDate, Timestamp sqlToDate, Map<String, Object> caches)
			throws ParseException {
		Map<Integer, Optional<PurchaseItem>> purchaseItemCache = (Map<Integer, Optional<PurchaseItem>>) caches.get("purchaseItemCache");
		Map<String, Optional<ItemMaster>> itemMasterCache = (Map<String, Optional<ItemMaster>>) caches.get("itemMasterCache");
		Map<String, List<DesignItems>> designItemsCache = (Map<String, List<DesignItems>>) caches.get("designItemsCache");
		Map<String, SalesOrderDesign> soDesignCache = (Map<String, SalesOrderDesign>) caches.get("soDesignCache");
		Map<String, DesignItems> designItemObjCache = (Map<String, DesignItems>) caches.get("designItemObjCache");
		@SuppressWarnings("unchecked")
		Map<String, List<DeliveryChallanItems>> dcUpToToDate = (Map<String, List<DeliveryChallanItems>>) caches.get("dcItemsUpToToDate");
		java.util.function.Supplier<List<DeliveryChallanItems>> emptyDcList = () -> java.util.Collections.emptyList();
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
		// CriteriaQuery<Object[]> query = criteriaBuilder.createTupleQuery();
		Root<GrnItems> item = query.from(GrnItems.class);
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
		conditionsList.add(onEnd);

		query.multiselect(item.get("description"), totalReceivedQty, weightedRate, totalAmount)
				.where(conditionsList.toArray(new Predicate[] {}));
		query.groupBy(item.get("description"));
		TypedQuery<Object[]> typedQuery = entityManager.createQuery(query);
		typedQuery.setMaxResults(5000);
		List<Object[]> inwardList = typedQuery.getResultList();
//		System.out.println("received qty"+totalReceivedQty+"weightedRate"+weightedRate +"totalAmount"+totalAmount);
//		System.out.println("po Item"+inwardList.get(0));

		Map<String, Map> pMap = new HashMap<String, Map>();
		for (Object[] objects : inwardList) {
		  try {
			String poItemId = (String) objects[0];
			int poItemIdInt = Integer.parseInt(poItemId);

			Optional<PurchaseItem> purchaseItem = purchaseItemCache.computeIfAbsent(poItemIdInt,
				id -> purchaseItemService.getPurchaseItemByPoItemId(id));
			if (!purchaseItem.isPresent()) continue;

			String modelNo = purchaseItem.get().getModelNo();
			Optional<ItemMaster> items = itemMasterCache.computeIfAbsent(modelNo,
				mn -> itemMasterService.getItemById(mn));
			if (!items.isPresent()) continue;

			String itemId = items.get().getId();
			String itemKey = items.get().getItemName() + "/" + items.get().getModel();

			if (excelSheetValue.containsKey(itemKey) || pMap.containsKey(itemKey)) continue;

			float grnRate = 0.0f;
			float grnTotal = 0.0f;
			float grnQuant = (Float) objects[1];
			if (objects[2] != null) grnRate = (Float) objects[2];
			if (objects[3] != null) grnTotal = (Float) objects[3];

			String soItemId = purchaseItem.get().getDescription();
			List<DeliveryChallanItems> dcList = new ArrayList<>(dcUpToToDate != null ? dcUpToToDate.getOrDefault(soItemId, emptyDcList.get()) : emptyDcList.get());

			List<DesignItems> designItems = designItemsCache.computeIfAbsent(itemId,
				id -> designItemRepo.findDesignItemListByItemId(id));
			for (DesignItems designItemObj : designItems) {
				if (designItemObj.getSalesOrderDesign() == null) continue;
				List<DeliveryChallanItems> dcItems = dcUpToToDate != null ? dcUpToToDate.getOrDefault(designItemObj.getSalesOrderDesign().getSalesItemId(), emptyDcList.get()) : emptyDcList.get();
				for (DeliveryChallanItems dcObj : dcItems) {
					if (!dcObj.getDescription().equalsIgnoreCase(soItemId)) {
						dcList.add(dcObj);
					}
				}
			}

			float dcQuantity = 0;
			float dcPresentQty = 0;
			{
				for (DeliveryChallanItems dcItem : dcList) {
					String dcDesc = dcItem.getDescription();
					SalesOrderDesign designObj = soDesignCache.computeIfAbsent(dcDesc,
						d -> soDesignService.findSalesOrderDesignObjBysalesItemId(d));
					if (designObj != null) {
						String diCacheKey = itemId + "_" + designObj.getId();
						DesignItems designItemsList = designItemObjCache.computeIfAbsent(diCacheKey,
							k -> designItemRepo.findDesignItemListByItemIdAndDesignId(itemId, designObj.getId()).stream().findFirst().orElse(null));
						if (designItemsList != null) {
							if (itemId.equalsIgnoreCase(designItemsList.getItemId()) && designItemsList.getDeliveredQty() > 0) {
								dcQuantity = dcQuantity + designItemsList.getDeliveredQty();
								if (dcItem.getUpdated().getTime() >= sqlFromDate.getTime()
										&& dcItem.getUpdated().getTime() <= sqlToDate.getTime()) {
									dcPresentQty = dcPresentQty + designItemsList.getDeliveredQty();
								}
							} else {
								float qty = Math.max(dcItem.getTodaysQty(), dcItem.getDeliveredQuantity());
								dcQuantity = dcQuantity + qty;
								if (dcItem.getUpdated().getTime() >= sqlFromDate.getTime()
										&& dcItem.getUpdated().getTime() <= sqlToDate.getTime()) {
									dcPresentQty = dcPresentQty + dcItem.getTodaysQty();
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

			if (grnQuant > 0) {
				openingBalanceQuant = grnQuant;
				openingBalValue = openingBalanceQuant * grnRate;
			}

			if (pMap.containsKey(itemKey)) {
				Map cMap = pMap.get(itemKey);

				float prevGrnQuantity = (float) cMap.get("grnQ1");
				prevGrnQuantity = (prevGrnQuantity + grnQuant);
				float prevgrnUnitPrice = (float) cMap.get("grnR1");
				prevgrnUnitPrice = (prevgrnUnitPrice + grnRate) / 2;
				float prevgrnValue = prevgrnUnitPrice * prevGrnQuantity;

				float prevDcQuantity = (float) cMap.get("dcQ1") + dcQuantity;
				float prevDcValue = prevgrnUnitPrice * prevDcQuantity;

				float prevopenQuantity = (float) cMap.get("openQ1") + openingBalanceQuant;
				float prevOpenValue = prevgrnUnitPrice * prevopenQuantity;

				float preclosingQuantity = prevopenQuantity - prevDcQuantity;
				float prevcloseValue = prevgrnUnitPrice * preclosingQuantity;
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
				ExcelcredMap.put("particulars", itemKey);
				if (!excelSheetValue.containsKey(itemKey)) {
					pMap.put(itemKey, ExcelcredMap);
				}
			}
		  } catch (Exception e) {
			log.warn("Skipping item in opening quantity (no GRN): {}", e.getMessage());
		  }
		}
		return pMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public Map findOutwardQuantity(List<Object[]> inwardList, Timestamp sqlFromDate, Timestamp sqlToDate, Map<String, Object> caches) {
		Map<Integer, Optional<PurchaseItem>> purchaseItemCache = (Map<Integer, Optional<PurchaseItem>>) caches.get("purchaseItemCache");
		Map<String, Optional<ItemMaster>> itemMasterCache = (Map<String, Optional<ItemMaster>>) caches.get("itemMasterCache");
		Map<String, List<DesignItems>> designItemsCache = (Map<String, List<DesignItems>>) caches.get("designItemsCache");
		Map<String, SalesOrderDesign> soDesignCache = (Map<String, SalesOrderDesign>) caches.get("soDesignCache");
		Map<String, Float> openingQuantCache = (Map<String, Float>) caches.get("openingQuantCache");
		Map<String, DesignItems> designItemObjCache = (Map<String, DesignItems>) caches.get("designItemObjCache");
		Map<String, List<DeliveryChallanItems>> dcByDescription = (Map<String, List<DeliveryChallanItems>>) caches.get("dcItemsBetweenDates");

		Map<String, Map> pMap = new HashMap<String, Map>();
		for (Object[] objects : inwardList) {
		  try {
			String poItemId = (String) objects[0];
			int poItemIdInt = Integer.parseInt(poItemId);

			Optional<PurchaseItem> purchaseItem = purchaseItemCache.computeIfAbsent(poItemIdInt,
				id -> purchaseItemService.getPurchaseItemByPoItemId(id));
			if (!purchaseItem.isPresent()) continue;

			String modelNo = purchaseItem.get().getModelNo();
			Optional<ItemMaster> item = itemMasterCache.computeIfAbsent(modelNo,
				mn -> itemMasterService.getItemById(mn));
			if (!item.isPresent()) continue;

			String itemId = item.get().getId();
			String itemKey = item.get().getItemName() + "/" + item.get().getModel();

			float grnRate = 0.0f;
			float grnTotal = 0.0f;
			float grnQuant = (Float) objects[1];
			if (objects[2] != null) grnRate = (Float) objects[2];
			if (objects[3] != null) grnTotal = (Float) objects[3];

			String soItemId = purchaseItem.get().getDescription();
			List<DeliveryChallanItems> dcList = new ArrayList<>(dcByDescription != null ? dcByDescription.getOrDefault(soItemId, java.util.Collections.emptyList()) : java.util.Collections.emptyList());

			List<DesignItems> designItems = designItemsCache.computeIfAbsent(itemId,
				id -> designItemRepo.findDesignItemListByItemId(id));
			for (DesignItems designItemObj : designItems) {
				if (designItemObj.getSalesOrderDesign() == null) continue;
				String salesItemId = designItemObj.getSalesOrderDesign().getSalesItemId();
				List<DeliveryChallanItems> dcItems = dcByDescription != null ? dcByDescription.getOrDefault(salesItemId, java.util.Collections.emptyList()) : java.util.Collections.emptyList();
				for (DeliveryChallanItems dcObj : dcItems) {
					if (!dcObj.getDescription().equalsIgnoreCase(soItemId)) {
						dcList.add(dcObj);
					}
				}
			}

			float dcQuantity = 0;
			if (!pMap.containsKey(itemKey)) {
				for (DeliveryChallanItems dcItem : dcList) {
					String dcDesc = dcItem.getDescription();
					SalesOrderDesign designObj = soDesignCache.computeIfAbsent(dcDesc,
						d -> soDesignService.findSalesOrderDesignObjBysalesItemId(d));
					if (designObj != null) {
						String diCacheKey = itemId + "_" + designObj.getId();
						DesignItems designItemsList = designItemObjCache.computeIfAbsent(diCacheKey,
							k -> designItemRepo.findDesignItemListByItemIdAndDesignId(itemId, designObj.getId()).stream().findFirst().orElse(null));
						if (designItemsList != null) {
							if (itemId.equalsIgnoreCase(designItemsList.getItemId()) && designItemsList.getDeliveredQty() > 0) {
								dcQuantity = dcQuantity + designItemsList.getDeliveredQty();
							} else {
								dcQuantity = dcQuantity + Math.max(dcItem.getTodaysQty(), dcItem.getDeliveredQuantity());
							}
						}
					}
				}
			}

			float closedBalnceQuant = 0;
			float closedBalnceValue = 0;
			float openingBalanceQuant = 0;
			float openingBalValue = 0;

			openingBalanceQuant = openingQuantCache.computeIfAbsent(
				itemId + "_" + sqlFromDate + "_" + sqlToDate,
				k -> getOpeningQuant(itemId, sqlFromDate, sqlToDate, caches));
			if (!pMap.containsKey(itemKey)) {
				openingBalValue = openingBalanceQuant * grnRate;
				closedBalnceQuant = openingBalanceQuant + grnQuant - dcQuantity;
				closedBalnceValue = grnRate * closedBalnceQuant;
			} else {
				openingBalanceQuant = 0;
				openingBalValue = 0;
				closedBalnceQuant = grnQuant - dcQuantity;
				closedBalnceValue = grnRate * closedBalnceQuant;
			}

			if (pMap.containsKey(itemKey)) {
				Map cMap = pMap.get(itemKey);

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
				ExcelcredMap.put("particulars", itemKey);
				pMap.put(itemKey, ExcelcredMap);
			}
		  } catch (Exception e) {
			log.warn("Skipping item in outward calculation: {}", e.getMessage());
		  }
		}
		return pMap;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	private float getOpeningQuant(String itemId, Timestamp sqlFromDate, Timestamp sqlToDate, Map<String, Object> caches) {
		Map<String, List<DesignItems>> designItemsCache = (Map<String, List<DesignItems>>) caches.get("designItemsCache");
		Map<String, SalesOrderDesign> soDesignCache = (Map<String, SalesOrderDesign>) caches.get("soDesignCache");
		Map<String, List<PurchaseItem>> poItemsByModelCache = (Map<String, List<PurchaseItem>>) caches.get("poItemsByModelCache");
		Map<String, DesignItems> designItemObjCache = (Map<String, DesignItems>) caches.get("designItemObjCache");
		@SuppressWarnings("unchecked")
		Map<String, List<DeliveryChallanItems>> dcUpToFromDate = (Map<String, List<DeliveryChallanItems>>) caches.get("dcItemsUpToFromDate");

		float openingBalance = 0;
		try {
		List<PurchaseItem> poItemList = poItemsByModelCache.computeIfAbsent(itemId,
			id -> poItemRepo.findByModelNumber(id));
		float grn = 0;
		float dcQuantity = 0;
		List<DeliveryChallanItems> dcList = new ArrayList<>();
		for (PurchaseItem purchaseItem : poItemList) {
			List<GrnItems> grnList = grnItemRepo.findByPoItemIdAndUpdatedDate(Integer.toString(purchaseItem.getPurchase_item_id()), sqlFromDate);
			for (GrnItems grnItem : grnList) {
				grn = grn + grnItem.getReceivedQuantity();
			}
		}

		List<DesignItems> designItems = designItemsCache.computeIfAbsent(itemId,
			id -> designItemRepo.findDesignItemListByItemId(id));

		for (PurchaseItem purchaseItem : poItemList) {
			List<DeliveryChallanItems> dcListByPoItemSo = dcUpToFromDate != null ? dcUpToFromDate.getOrDefault(purchaseItem.getDescription(), java.util.Collections.emptyList()) : java.util.Collections.emptyList();
			dcList.addAll(dcListByPoItemSo);

			for (DesignItems designItemObj : designItems) {
				if (designItemObj.getSalesOrderDesign() == null) continue;
				List<DeliveryChallanItems> dcItems = dcUpToFromDate != null ? dcUpToFromDate.getOrDefault(designItemObj.getSalesOrderDesign().getSalesItemId(), java.util.Collections.emptyList()) : java.util.Collections.emptyList();
				dcList.addAll(dcItems);
			}
		}

		Set<DeliveryChallanItems> dcItemSet = new HashSet(dcList);
		dcQuantity = 0;
		for (DeliveryChallanItems dcItem : dcItemSet) {
			String dcDesc = dcItem.getDescription();
			SalesOrderDesign designObj = soDesignCache.computeIfAbsent(dcDesc,
				d -> soDesignService.findSalesOrderDesignObjBysalesItemId(d));
			if (designObj != null) {
				String cacheKey = itemId + "_" + designObj.getId();
				DesignItems designItemsList = designItemObjCache.computeIfAbsent(cacheKey,
					k -> designItemRepo.findDesignItemListByItemIdAndDesignId(itemId, designObj.getId()).stream().findFirst().orElse(null));
				if (designItemsList != null) {
					if (itemId.equalsIgnoreCase(designItemsList.getItemId()) && designItemsList.getDeliveredQty() > 0) {
						dcQuantity = dcQuantity + designItemsList.getDeliveredQty();
					} else {
						dcQuantity = dcQuantity + Math.max(dcItem.getTodaysQty(), dcItem.getDeliveredQuantity());
					}
				}
			}
		}

		openingBalance = grn - dcQuantity;
		} catch (Exception e) {
			log.warn("Error calculating opening quantity for item {}: {}", itemId, e.getMessage());
		}
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
	
	// Get GRN and PO details by model number
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Grn> getGrnAndPoDetailsByModel(String modelNo) {
		ItemMaster item = itemMasterService.getItemByModelNo(modelNo.trim());
		ArrayList<PurchaseOrder> poList = new ArrayList<PurchaseOrder>();
		ArrayList<Grn> grn;
		Set set = new HashSet();
		
		if (item == null) {
			grn = new ArrayList<Grn>(set);
		} else {
			List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemsByModelNumber(item.getId());
			
			for (PurchaseItem purchaseItem : poItemList) {
				PurchaseOrder po = purchaseItem.getPurchaseOrder();
				poList.add(po);
			}
			
			for (PurchaseOrder po : poList) {
				String poNumber = po.getPoNumber();
				List<Grn> grnList = findGrnByPoNumber(poNumber);
				for (Grn grnObject : grnList) {
					String poNum = grnObject.getPoNumber();
					Optional<PurchaseOrder> poObj = purchaseOrderService.findById(poNum);
					if (poObj.isPresent()) {
						String vendor = poObj.get().getParty().getPartyName();
						Date poDate = poObj.get().getUpdated();
						grnObject.set("vendor", vendor);
						grnObject.set("poDate", poDate);
					}
					set.add(grnObject);
				}
			}
			grn = new ArrayList<Grn>(set);
		}
		
		return grn;
	}
	
	
	
	
}
