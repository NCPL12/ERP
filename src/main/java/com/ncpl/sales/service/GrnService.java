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
import java.util.stream.Collectors;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
import com.ncpl.sales.model.Supplier;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.GrnItemRepo;
import com.ncpl.sales.repository.GrnRepo;
import com.ncpl.sales.repository.ItemMasterRepo;
import com.ncpl.sales.repository.MonthlyReportStockRepo;
import com.ncpl.sales.repository.PurchaseItemRepo;
import com.ncpl.sales.repository.SalesItemRepo;
import com.ncpl.sales.repository.SalesOrderDesignItemsRepo;
import com.ncpl.sales.repository.StockRepo;
import com.ncpl.sales.repository.SupplierRepo;
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
	@Autowired
	ItemMasterRepo itemMasterRepo;
	@Autowired
	StockRepo stockRepo;
	@Autowired
	MonthlyReportStockRepo monthlyReportStockRepo;
	@Autowired
	SupplierRepo supplierRepo;
  
    
	public Grn saveGrn(Grn grn) {
		List<GrnItems> grnItemList = grn.getItems();

		for (GrnItems grnItems : grnItemList) {
			if (grnItems.getReceivedQuantity() <= 0) {
				throw new IllegalStateException("Received quantity must be greater than 0 for item "
						+ grnItems.getDescription() + " - GRN not saved.");
			}
		}

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

	// Lean replacement for the old getGrnList() (which loaded full Grn entities with
	// batched vendor/total enrichment) - the only caller (new GRN form) only ever
	// needed this for a duplicate-invoice-number check, so just return invoice numbers.
	public List<String> getAllInvoiceNumbers() {
		return grnRepo.findAllInvoiceNumbers();
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
					Date poDate = poObj.getCreated();
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
					Date poDate = poObj.getCreated();
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
					grn.set("poDate", poObj.getCreated());
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
		Date poDate = poObj.get().getCreated();
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
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map findgrnListByDate(Timestamp sqlFromDate, Timestamp sqlToDate) throws ParseException {
		long start = System.currentTimeMillis();

		// Batch 1: GRN qty by model_no (CLI's exact query)
		String grnSql = "SELECT pi.model_no, COALESCE(SUM(gi.received_quantity), 0) " +
				"FROM tbl_grn_items gi " +
				"JOIN tbl_purchase_items pi ON gi.po_item_id = pi.purchase_item_id " +
				"WHERE gi.updated >= ? AND gi.updated <= ? " +
				"GROUP BY pi.model_no";
		Query grnQuery = entityManager.createNativeQuery(grnSql);
		grnQuery.setParameter(1, sqlFromDate);
		grnQuery.setParameter(2, sqlToDate);
		List<Object[]> grnResults = grnQuery.getResultList();
		Map<String, Float> grnByModel = new HashMap<>();
		for (Object[] row : grnResults) {
			grnByModel.put((String) row[0], ((Number) row[1]).floatValue());
		}
		log.info("Stock summary: {} models with GRN found in {}ms", grnByModel.size(), System.currentTimeMillis() - start);

		// Batch 2: DC qty by so_model_no (CLI's exact query)
		String dcSql = "SELECT so_model_no, COALESCE(SUM(todays_qty), 0) " +
				"FROM tbl_dc_items " +
				"WHERE updated >= ? AND updated <= ? " +
				"GROUP BY so_model_no";
		Query dcQuery = entityManager.createNativeQuery(dcSql);
		dcQuery.setParameter(1, sqlFromDate);
		dcQuery.setParameter(2, sqlToDate);
		List<Object[]> dcResults = dcQuery.getResultList();
		Map<String, Float> dcByModel = new HashMap<>();
		for (Object[] row : dcResults) {
			dcByModel.put((String) row[0], ((Number) row[1]).floatValue());
		}
		log.info("Stock summary: {} models with DC found in {}ms", dcByModel.size(), System.currentTimeMillis() - start);

		// Collect all distinct model numbers with activity
		Set<String> allModels = new HashSet<>(grnByModel.keySet());
		allModels.addAll(dcByModel.keySet());

		// Caches for opening and supply price
		Map<String, Object> caches = new HashMap<>();
		caches.put("openingQuantCache", new HashMap<String, Float>());
		caches.put("supplyPriceCache", new HashMap<String, Float>());

		Map<String, Map> grnList = new HashMap<>();
		Map<String, Map> noGrnList = new HashMap<>();

		for (String modelNo : allModels) {
			try {
				ItemMaster item = itemMasterService.getItemByModelNo(modelNo.trim());
				if (item == null) continue;

				String itemId = item.getId();
				String itemKey = item.getItemName() + "/" + item.getModel();

				float grnQty = grnByModel.getOrDefault(modelNo, 0f);
				float dcQty = dcByModel.getOrDefault(modelNo, 0f);
				float supplyPrice = getSupplyPrice(itemId, caches);
				float openingQty = getOpeningQuant(itemId, sqlFromDate, sqlToDate, caches);

				float closingQty = openingQty + grnQty - dcQty;
				if (closingQty < 0) closingQty = 0;

				float openingValue = openingQty * supplyPrice;
				float closingValue = closingQty * supplyPrice;
				float dcValue = dcQty * supplyPrice;
				float grnValue = grnQty * supplyPrice;

				Map<String, Object> record = new HashMap<>();
				record.put("particulars", itemKey);
				record.put("openQ1", openingQty);
				record.put("openR1", supplyPrice);
				record.put("openV1", openingValue);
				record.put("grnQ1", grnQty);
				record.put("grnR1", supplyPrice);
				record.put("grnV1", grnValue);
				record.put("dcQ1", dcQty);
				record.put("dcR1", supplyPrice);
				record.put("dcV1", dcValue);
				record.put("clQ1", closingQty);
				record.put("clR1", supplyPrice);
				record.put("clV1", closingValue);

				if (grnQty > 0) {
					grnList.put(itemKey, record);
				} else {
					noGrnList.put(itemKey, record);
				}
			} catch (Exception e) {
				log.warn("Skipping model {}: {}", modelNo, e.getMessage());
			}
		}

		log.info("Stock summary: done in {}ms, {} items ({} with GRN, {} without)",
				System.currentTimeMillis() - start, grnList.size() + noGrnList.size(), grnList.size(), noGrnList.size());

		Map<String, Map> recordsMap = new HashMap();
		recordsMap.put("grnlist", grnList);
		recordsMap.put("nogrnlist", noGrnList);
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
		Map<String, Float> openingQuantCache = (Map<String, Float>) caches.get("openingQuantCache");
		@SuppressWarnings("unchecked")
		Map<String, List<DeliveryChallanItems>> dcByDescription = (Map<String, List<DeliveryChallanItems>>) caches.get("dcItemsBetweenDates");
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
				mn -> itemMasterRepo.findById(mn));
			if (!items.isPresent()) continue;

			String itemId = items.get().getId();
			String itemKey = items.get().getItemName() + "/" + items.get().getModel();

			if (excelSheetValue.containsKey(itemKey) || pMap.containsKey(itemKey)) continue;

			float supplyPrice = getSupplyPrice(itemId, caches);
			float grnTotal = 0.0f;
			float grnQuant = (Float) objects[1];
			if (objects[3] != null) grnTotal = (Float) objects[3];

			String soItemId = purchaseItem.get().getDescription();

			// DC in period [fromDate, toDate] from preloaded cache, deduplicated
			float dcPresentQty = 0;
			Set<String> relevantDescs = new HashSet<>();
			relevantDescs.add(soItemId);
			List<DesignItems> designItems = designItemsCache.computeIfAbsent(itemId,
				id -> designItemRepo.findDesignItemListByItemId(id));
			for (DesignItems designItemObj : designItems) {
				if (designItemObj.getSalesOrderDesign() != null && designItemObj.getSalesOrderDesign().getSalesItemId() != null) {
					relevantDescs.add(designItemObj.getSalesOrderDesign().getSalesItemId());
				}
			}
			List<DeliveryChallanItems> allDc = new ArrayList<>();
			for (String desc : relevantDescs) {
				List<DeliveryChallanItems> dcItemsForDesc = dcByDescription != null
					? dcByDescription.getOrDefault(desc, emptyDcList.get())
					: emptyDcList.get();
				allDc.addAll(dcItemsForDesc);
			}
			Set<DeliveryChallanItems> dcSet = new HashSet<>(allDc);
			for (DeliveryChallanItems dcItem : dcSet) {
				String dcDesc = dcItem.getDescription();
				SalesOrderDesign designObj = soDesignCache.computeIfAbsent(dcDesc,
					d -> soDesignService.findSalesOrderDesignObjBysalesItemId(d));
				if (designObj != null) {
					String diCacheKey = itemId + "_" + designObj.getId();
					DesignItems designItemsList = designItemObjCache.computeIfAbsent(diCacheKey,
						k -> designItemRepo.findDesignItemListByItemIdAndDesignId(itemId, designObj.getId()).stream().findFirst().orElse(null));
					if (designItemsList != null) {
						if (itemId.equalsIgnoreCase(designItemsList.getItemId()) && designItemsList.getDeliveredQty() > 0) {
							dcPresentQty = dcPresentQty + designItemsList.getDeliveredQty();
						} else {
							dcPresentQty = dcPresentQty + Math.max(dcItem.getTodaysQty(), dcItem.getDeliveredQuantity());
						}
					}
				}
			}

			// Opening = all GRN before fromDate - all DC before fromDate (comprehensive)
			float openingBalanceQuant = openingQuantCache.computeIfAbsent(
				itemId + "_" + sqlFromDate + "_" + sqlToDate,
				k -> getOpeningQuant(itemId, sqlFromDate, sqlToDate, caches));
			float openingBalValue = openingBalanceQuant * supplyPrice;
			float closedBalnceQuant = openingBalanceQuant - dcPresentQty;
			if (closedBalnceQuant < 0) closedBalnceQuant = 0;
			float closedBalnceValue = closedBalnceQuant * supplyPrice;

			Map<String, Object> ExcelcredMap = new HashMap<String, Object>();
			ExcelcredMap.put("dcR1", supplyPrice);
			ExcelcredMap.put("dcQ1", dcPresentQty);
			ExcelcredMap.put("dcV1", supplyPrice * dcPresentQty);
			ExcelcredMap.put("grnR1", supplyPrice);
			ExcelcredMap.put("grnQ1", 0.0f);
			ExcelcredMap.put("grnV1", 0.0f);
			ExcelcredMap.put("clR1", supplyPrice);
			ExcelcredMap.put("clQ1", closedBalnceQuant);
			ExcelcredMap.put("clV1", closedBalnceValue);
			ExcelcredMap.put("openQ1", openingBalanceQuant);
			ExcelcredMap.put("openR1", supplyPrice);
			ExcelcredMap.put("openV1", openingBalValue);
			ExcelcredMap.put("particulars", itemKey);
			pMap.put(itemKey, ExcelcredMap);
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
				mn -> itemMasterRepo.findById(mn));
			if (!item.isPresent()) continue;

			String itemId = item.get().getId();
			String itemKey = item.get().getItemName() + "/" + item.get().getModel();

			float supplyPrice = getSupplyPrice(itemId, caches);
			float grnTotal = 0.0f;
			float grnQuant = (Float) objects[1];
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
				openingBalValue = openingBalanceQuant * supplyPrice;
				closedBalnceQuant = openingBalanceQuant + grnQuant - dcQuantity;
				if (closedBalnceQuant < 0) closedBalnceQuant = 0;
				closedBalnceValue = supplyPrice * closedBalnceQuant;
			} else {
				openingBalanceQuant = 0;
				openingBalValue = 0;
				closedBalnceQuant = grnQuant - dcQuantity;
				if (closedBalnceQuant < 0) closedBalnceQuant = 0;
				closedBalnceValue = supplyPrice * closedBalnceQuant;
			}

			if (pMap.containsKey(itemKey)) {
				Map cMap = pMap.get(itemKey);

				float prevGrnQty = (float) cMap.get("grnQ1");
				float newTotalQty = prevGrnQty + grnQuant;
				float newGrnValue = supplyPrice * newTotalQty;

				float prevDcQuantity = (float) cMap.get("dcQ1");
				prevDcQuantity = (prevDcQuantity + dcQuantity);
				float prevDcValue = supplyPrice * prevDcQuantity;

				float preclosingQuantity = (float) cMap.get("clQ1");
				preclosingQuantity = (preclosingQuantity + closedBalnceQuant);
				if (preclosingQuantity < 0) preclosingQuantity = 0;
				float prevcloseValue = supplyPrice * preclosingQuantity;

				float prevopenQuantity = (float) cMap.get("openQ1");
				prevopenQuantity = (prevopenQuantity + openingBalanceQuant);
				float prevOpenValue = supplyPrice * prevopenQuantity;

				cMap.put("grnR1", supplyPrice);
				cMap.put("grnQ1", newTotalQty);
				cMap.put("grnV1", newGrnValue);
				cMap.put("dcR1", supplyPrice);
				cMap.put("dcQ1", prevDcQuantity);
				cMap.put("dcV1", prevDcValue);
				cMap.put("clR1", supplyPrice);
				cMap.put("clQ1", preclosingQuantity);
				cMap.put("clV1", prevcloseValue);
				cMap.put("openQ1", prevopenQuantity);
				cMap.put("openR1", supplyPrice);
				cMap.put("openV1", prevOpenValue);
			} else {
				Map<String, Object> ExcelcredMap = new HashMap<String, Object>();
				ExcelcredMap.put("dcR1", supplyPrice);
				ExcelcredMap.put("dcQ1", dcQuantity);
				ExcelcredMap.put("dcV1", supplyPrice * dcQuantity);
				ExcelcredMap.put("grnR1", supplyPrice);
				ExcelcredMap.put("grnQ1", grnQuant);
				ExcelcredMap.put("grnV1", grnTotal);
				ExcelcredMap.put("clR1", supplyPrice);
				ExcelcredMap.put("clQ1", closedBalnceQuant);
				ExcelcredMap.put("clV1", closedBalnceValue);
				ExcelcredMap.put("openQ1", openingBalanceQuant);
				ExcelcredMap.put("openR1", supplyPrice);
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

	private float computeDcQuantity(String itemId, List<PurchaseItem> poItemList, List<DesignItems> designItems,
			Map<String, List<DeliveryChallanItems>> dcMap, Map<String, SalesOrderDesign> soDesignCache,
			Map<String, DesignItems> designItemObjCache) {
		if (dcMap == null) return 0;
		List<DeliveryChallanItems> dcList = new ArrayList<>();
		for (PurchaseItem purchaseItem : poItemList) {
			dcList.addAll(dcMap.getOrDefault(purchaseItem.getDescription(), java.util.Collections.emptyList()));
			for (DesignItems designItemObj : designItems) {
				if (designItemObj.getSalesOrderDesign() == null) continue;
				dcList.addAll(dcMap.getOrDefault(designItemObj.getSalesOrderDesign().getSalesItemId(), java.util.Collections.emptyList()));
			}
		}
		float totalDc = 0;
		for (DeliveryChallanItems dcItem : new HashSet<>(dcList)) {
			String dcDesc = dcItem.getDescription();
			SalesOrderDesign designObj = soDesignCache.computeIfAbsent(dcDesc,
				d -> soDesignService.findSalesOrderDesignObjBysalesItemId(d));
			if (designObj != null) {
				String cacheKey = itemId + "_" + designObj.getId();
				DesignItems designItemsList = designItemObjCache.computeIfAbsent(cacheKey,
					k -> designItemRepo.findDesignItemListByItemIdAndDesignId(itemId, designObj.getId()).stream().findFirst().orElse(null));
				if (designItemsList != null) {
					if (itemId.equalsIgnoreCase(designItemsList.getItemId()) && designItemsList.getDeliveredQty() > 0) {
						totalDc += designItemsList.getDeliveredQty();
					} else {
						totalDc += Math.max(dcItem.getTodaysQty(), dcItem.getDeliveredQuantity());
					}
				}
			}
		}
		return totalDc;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private float getSupplyPrice(String itemId, Map<String, Object> caches) {
		Map<String, Float> supplyPriceCache = (Map<String, Float>) caches.get("supplyPriceCache");
		return supplyPriceCache.computeIfAbsent(itemId, id -> {
			try {
				ItemMaster item = itemMasterService.getItemById(itemId).orElse(null);
				if (item != null) {
					return (float) item.getSellPrice();
				}
			} catch (Exception e) {
				log.warn("Failed to get supply price for item {}: {}", id, e.getMessage());
			}
			return 0f;
		});
	}

	private float getOpeningQuant(String itemId, Timestamp sqlFromDate, Timestamp sqlToDate, Map<String, Object> caches) {
		// Try tbl_monthly_report_stock first
		try {
			LocalDate reportDate = sqlFromDate.toLocalDateTime().toLocalDate();
			java.util.List<java.math.BigDecimal> prevQty = monthlyReportStockRepo.findPreviousOutstandingQtyByItemAndBeforeDate(itemId, reportDate);
			if (prevQty != null && !prevQty.isEmpty() && prevQty.get(0) != null) {
				return prevQty.get(0).floatValue();
			}
		} catch (Exception e) {
			log.warn("Failed to read opening from monthly_report_stock for item {}: {}", itemId, e.getMessage());
		}
		// Fallback to pre-built stock-totals cache (not always present depending on caller)
		Map<String, Float> stockTotalsAsOfFromDateMap = (Map<String, Float>) caches.get("stockTotalsAsOfFromDateMap");
		if (stockTotalsAsOfFromDateMap == null) return 0f;
		float openingBalance = stockTotalsAsOfFromDateMap.getOrDefault(itemId, 0f);
		if (openingBalance < 0) openingBalance = 0;
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
				Date poDate = poObj.get().getCreated();
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
			Date poDate = poObj.get().getCreated();
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
		return grnItemsList.stream()
			.filter(item -> item.getReceivedQuantity() != 0)
			.collect(Collectors.toList());
	}

	private static final int MAX_AUTO_CLOSE_DEPTH = 24;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map<String, Object>> getMonthlyStockMovementReport(Timestamp fromDate, Timestamp toDate) {
		return getMonthlyStockMovementReport(fromDate, toDate, 0);
	}

	// ===================== OLD VERSION (commented out - kept for reference) =====================
	/*
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Map<String, Object>> getMonthlyStockMovementReport(Timestamp fromDate, Timestamp toDate, int depth) {
		// Opening qty must equal the immediately preceding month's closing qty.
		// If that month was never closed (report never run/viewed for it), close it
		// now first so the chain is unbroken, then read it back as this period's opening.
		java.time.LocalDate periodStart = fromDate.toLocalDateTime().toLocalDate();
		java.time.LocalDate prevMonthEnd = periodStart.minusDays(1);
		if (depth < MAX_AUTO_CLOSE_DEPTH
				&& monthlyReportStockRepo.findByReportDate(prevMonthEnd).isEmpty()) {
			java.time.LocalDate prevMonthStart = prevMonthEnd.withDayOfMonth(1);
			Timestamp prevFrom = Timestamp.valueOf(prevMonthStart.atStartOfDay());
			Timestamp prevTo = Timestamp.valueOf(prevMonthEnd.atTime(23, 59, 59));
			getMonthlyStockMovementReport(prevFrom, prevTo, depth + 1);
		}

		List<Object[]> liveRows = stockRepo.getStockTotalsGroupedByItemId();
		Map<String, Float> liveByItemId = new java.util.LinkedHashMap<>();
		for (Object[] row : liveRows) {
			float qty = ((Number) row[1]).floatValue();
			if (qty > 0) liveByItemId.put((String) row[0], qty);
		}

		// Inward = GRN received in the period (by item_master_id)
		String grnSql = "SELECT im.id, COALESCE(SUM(gi.received_quantity), 0) " +
				"FROM tbl_grn_items gi " +
				"JOIN tbl_purchase_items pi ON gi.po_item_id = pi.purchase_item_id " +
				"JOIN tbl_item_master im ON pi.model_no = im.id " +
				"WHERE gi.updated >= ? AND gi.updated <= ? GROUP BY im.id";
		javax.persistence.Query grnQ = entityManager.createNativeQuery(grnSql);
		grnQ.setParameter(1, fromDate);
		grnQ.setParameter(2, toDate);
		Map<String, Float> grnByItemId = new java.util.LinkedHashMap<>();
		for (Object[] row : (List<Object[]>) grnQ.getResultList()) {
			grnByItemId.put((String) row[0], ((Number) row[1]).floatValue());
		}

		// Outward = DC dispatched in the period (by model string → item_id)
		List<com.ncpl.sales.model.DeliveryChallanItems> dcRawList = dcService.getDcItemListByDate(fromDate, toDate);
		Map<String, Float> dcByModel = new java.util.LinkedHashMap<>();
		for (com.ncpl.sales.model.DeliveryChallanItems dcItem : dcRawList) {
			String model = (String) dcItem.get("modelNo");
			if (model == null || model.isEmpty()) continue;
			dcByModel.merge(model, dcItem.getTodaysQty(), Float::sum);
		}

		// Resolve DC model strings → item_master_id
		Map<String, String> modelToItemId = new java.util.HashMap<>();
		if (!dcByModel.isEmpty()) {
			List<ItemMaster> dcMasters = itemMasterRepo.findByModelIn(new java.util.ArrayList<>(dcByModel.keySet()));
			for (ItemMaster im : dcMasters) {
				if (im.getModel() != null) modelToItemId.put(im.getModel(), im.getId());
			}
		}
		Map<String, Float> dcByItemId = new java.util.LinkedHashMap<>();
		for (Map.Entry<String, Float> e : dcByModel.entrySet()) {
			String iid = modelToItemId.get(e.getKey());
			if (iid != null) dcByItemId.put(iid, e.getValue());
		}

		// Candidate item set = live + GRN + DC
		java.util.Set<String> allItemIds = new java.util.LinkedHashSet<>();
		allItemIds.addAll(liveByItemId.keySet());
		allItemIds.addAll(grnByItemId.keySet());
		allItemIds.addAll(dcByItemId.keySet());

		List<ItemMaster> items = itemMasterRepo.findAllById(allItemIds);

		// Build model → itemId map for the save section
		Map<String, String> modelToItemIdFull = new java.util.HashMap<>();
		for (ItemMaster im : items) {
			if (im.getModel() != null) modelToItemIdFull.put(im.getModel(), im.getId());
		}

		List<Map<String, Object>> result = new java.util.ArrayList<>();

		for (ItemMaster item : items) {
			String itemId = item.getId();
			String modelNo = item.getModel();

			float liveQty = liveByItemId.getOrDefault(itemId, 0f);
			float grnQty  = grnByItemId.getOrDefault(itemId, 0f);
			float dcQty   = dcByItemId.getOrDefault(itemId, 0f);

			// Opening qty = last period's closing qty (tbl_monthly_report_stock)
			float openQty;
			List<java.math.BigDecimal> prevQty = monthlyReportStockRepo
					.findPreviousOutstandingQtyByItemAndBeforeDate(itemId, periodStart);
			if (prevQty != null && !prevQty.isEmpty() && prevQty.get(0) != null) {
				openQty = prevQty.get(0).floatValue();
			} else {
				// No prior snapshot (e.g. first run for this item): fall back to
				// back-calculating from current live stock, same as before.
				openQty = Math.max(0, liveQty - grnQty + dcQty);
			}
			float closingQty = Math.max(0, openQty + grnQty - dcQty);

			// Price: avg of all PO unit prices for this item — identical to ISR methodology
			// (ISR uses findByModelNumberWithRecentPoItem(itemId) and averages unit prices).
			// Uses a price-only projection (not the full entity) so the parent PurchaseOrder
			// is never dragged into the session — see findUnitPricesByModelNumber for why.
			List<Float> poPrices = purchaseItemRepo.findUnitPricesByModelNumber(itemId);
			if (poPrices.isEmpty()) continue; // ISR only includes items with PO history

			float priceSum = 0;
			for (Float price : poPrices) priceSum += price;
			float avgPrice = priceSum / poPrices.size();

			if (openQty == 0 && grnQty == 0 && dcQty == 0) continue;

			Map<String, Object> record = new java.util.LinkedHashMap<>();
			record.put("modelNo", modelNo);
			record.put("description", item.getItemName());
			record.put("openQty", openQty);
			record.put("openRate", avgPrice);
			record.put("openValue", openQty * avgPrice);
			record.put("inwardQty", grnQty);
			record.put("inwardRate", avgPrice);
			record.put("inwardValue", grnQty * avgPrice);
			record.put("outwardQty", dcQty);
			record.put("outwardRate", avgPrice);
			record.put("outwardValue", dcQty * avgPrice);
			record.put("closingQty", closingQty);
			record.put("closingRate", avgPrice);
			record.put("closingValue", closingQty * avgPrice);
			result.add(record);
		}

		result.sort(java.util.Comparator.comparing(r -> (String) r.get("modelNo")));

		// Save closing balance to tbl_monthly_report_stock for next month's opening
		java.time.LocalDate reportDate = toDate.toLocalDateTime().toLocalDate();
		monthlyReportStockRepo.deleteByReportDate(reportDate);
		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		List<com.ncpl.sales.model.MonthlyReportStock> toSave = new java.util.ArrayList<>();
		for (Map<String, Object> rec : result) {
			String recModel = (String) rec.get("modelNo");
			String recItemId = modelToItemIdFull.get(recModel);
			if (recItemId == null) continue;
			float closingQtyRec = ((Number) rec.get("closingQty")).floatValue();
			float closingValRec = ((Number) rec.get("closingValue")).floatValue();
			com.ncpl.sales.model.MonthlyReportStock entry = new com.ncpl.sales.model.MonthlyReportStock();
			entry.setItemMasterId(recItemId);
			entry.setReportDate(reportDate);
			entry.setOutstandingQty(java.math.BigDecimal.valueOf(closingQtyRec));
			entry.setOutstandingValue(java.math.BigDecimal.valueOf(closingValRec));
			entry.setCreatedAt(now);
			toSave.add(entry);
		}
		monthlyReportStockRepo.saveAll(toSave);

		return result;
	}
	*/
	// ===================== END OLD VERSION =====================

	// ===================== NEW VERSION (same results; opening qty + price loaded in batch instead of 1 query per item; timing logs) =====================
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Map<String, Object>> getMonthlyStockMovementReport(Timestamp fromDate, Timestamp toDate, int depth) {
		long tStart = System.currentTimeMillis();
		// Opening qty must equal the immediately preceding month's closing qty.
		// If that month was never closed (report never run/viewed for it), close it
		// now first so the chain is unbroken, then read it back as this period's opening.
		java.time.LocalDate periodStart = fromDate.toLocalDateTime().toLocalDate();
		java.time.LocalDate prevMonthEnd = periodStart.minusDays(1);
		if (depth < MAX_AUTO_CLOSE_DEPTH
				&& monthlyReportStockRepo.findByReportDate(prevMonthEnd).isEmpty()) {
			java.time.LocalDate prevMonthStart = prevMonthEnd.withDayOfMonth(1);
			Timestamp prevFrom = Timestamp.valueOf(prevMonthStart.atStartOfDay());
			Timestamp prevTo = Timestamp.valueOf(prevMonthEnd.atTime(23, 59, 59));
			log.info("Monthly movement [depth={}]: previous month snapshot missing for {} -> auto-closing {} to {} first",
					depth, prevMonthEnd, prevMonthStart, prevMonthEnd);
			getMonthlyStockMovementReport(prevFrom, prevTo, depth + 1);
		}
		log.info("Monthly movement [depth={}]: auto-close check finished at {} ms", depth, System.currentTimeMillis() - tStart);

		long t1 = System.currentTimeMillis();
		List<Object[]> liveRows = stockRepo.getStockTotalsGroupedByItemId();
		Map<String, Float> liveByItemId = new java.util.LinkedHashMap<>();
		for (Object[] row : liveRows) {
			float qty = ((Number) row[1]).floatValue();
			if (qty > 0) liveByItemId.put((String) row[0], qty);
		}
		log.info("Monthly movement [depth={}]: live stock {} items in {} ms", depth, liveByItemId.size(), System.currentTimeMillis() - t1);

		// Inward = GRN received in the period (by item_master_id)
		long t2 = System.currentTimeMillis();
		String grnSql = "SELECT im.id, COALESCE(SUM(gi.received_quantity), 0) " +
				"FROM tbl_grn_items gi " +
				"JOIN tbl_purchase_items pi ON gi.po_item_id = pi.purchase_item_id " +
				"JOIN tbl_item_master im ON pi.model_no = im.id " +
				"WHERE gi.updated >= ? AND gi.updated <= ? GROUP BY im.id";
		javax.persistence.Query grnQ = entityManager.createNativeQuery(grnSql);
		grnQ.setParameter(1, fromDate);
		grnQ.setParameter(2, toDate);
		Map<String, Float> grnByItemId = new java.util.LinkedHashMap<>();
		for (Object[] row : (List<Object[]>) grnQ.getResultList()) {
			grnByItemId.put((String) row[0], ((Number) row[1]).floatValue());
		}
		log.info("Monthly movement [depth={}]: GRN query {} items in {} ms", depth, grnByItemId.size(), System.currentTimeMillis() - t2);

		// Outward = DC dispatched in the period (by model string → item_id)
		long t3 = System.currentTimeMillis();
		List<com.ncpl.sales.model.DeliveryChallanItems> dcRawList = dcService.getDcItemListByDate(fromDate, toDate);
		Map<String, Float> dcByModel = new java.util.LinkedHashMap<>();
		for (com.ncpl.sales.model.DeliveryChallanItems dcItem : dcRawList) {
			String model = (String) dcItem.get("modelNo");
			if (model == null || model.isEmpty()) continue;
			dcByModel.merge(model, dcItem.getTodaysQty(), Float::sum);
		}
		log.info("Monthly movement [depth={}]: DC items {} rows in {} ms", depth, dcRawList.size(), System.currentTimeMillis() - t3);

		// Resolve DC model strings → item_master_id
		Map<String, String> modelToItemId = new java.util.HashMap<>();
		if (!dcByModel.isEmpty()) {
			List<ItemMaster> dcMasters = itemMasterRepo.findByModelIn(new java.util.ArrayList<>(dcByModel.keySet()));
			for (ItemMaster im : dcMasters) {
				if (im.getModel() != null) modelToItemId.put(im.getModel(), im.getId());
			}
		}
		Map<String, Float> dcByItemId = new java.util.LinkedHashMap<>();
		for (Map.Entry<String, Float> e : dcByModel.entrySet()) {
			String iid = modelToItemId.get(e.getKey());
			if (iid != null) dcByItemId.put(iid, e.getValue());
		}

		// Candidate item set = live + GRN + DC
		java.util.Set<String> allItemIds = new java.util.LinkedHashSet<>();
		allItemIds.addAll(liveByItemId.keySet());
		allItemIds.addAll(grnByItemId.keySet());
		allItemIds.addAll(dcByItemId.keySet());

		long t4 = System.currentTimeMillis();
		List<ItemMaster> items = itemMasterRepo.findAllById(allItemIds);
		log.info("Monthly movement [depth={}]: item master load {} items in {} ms", depth, items.size(), System.currentTimeMillis() - t4);

		// Build model → itemId map for the save section
		Map<String, String> modelToItemIdFull = new java.util.HashMap<>();
		for (ItemMaster im : items) {
			if (im.getModel() != null) modelToItemIdFull.put(im.getModel(), im.getId());
		}

		List<Map<String, Object>> result = new java.util.ArrayList<>();

		long tLoop = System.currentTimeMillis();
		int openQueryCount = 0;
		int priceQueryCount = 0;
		int skippedNoActivity = 0;

		// ---- Batch 1: opening qty (latest snapshot before periodStart) for ALL items in a few queries.
		// Same rule as findPreviousOutstandingQtyByItemAndBeforeDate: newest reportDate < periodStart wins.
		// If the batch fails for any reason, openingBatch stays null and the old per-item query is used.
		long tOpen = System.currentTimeMillis();
		List<String> loopIds = new ArrayList<>();
		for (ItemMaster item : items) {
			loopIds.add(item.getId());
		}
		Map<String, java.math.BigDecimal> openingBatch = null;
		try {
			openingBatch = new HashMap<>();
			for (int i = 0; i < loopIds.size(); i += 1000) {
				List<String> chunk = loopIds.subList(i, Math.min(i + 1000, loopIds.size()));
				List<Object[]> openRows = entityManager.createQuery(
						"SELECT m.itemMasterId, m.outstandingQty FROM MonthlyReportStock m "
								+ "WHERE m.itemMasterId IN :ids AND m.reportDate < :d ORDER BY m.reportDate DESC",
						Object[].class)
						.setParameter("ids", chunk)
						.setParameter("d", periodStart)
						.getResultList();
				for (Object[] r : openRows) {
					String iid = (String) r[0];
					if (!openingBatch.containsKey(iid)) {
						openingBatch.put(iid, (java.math.BigDecimal) r[1]);
					}
				}
			}
		} catch (Exception e) {
			log.warn("Monthly movement [depth={}]: batch opening query failed, falling back to per-item queries: {}", depth, e.getMessage());
			openingBatch = null;
		}
		log.info("Monthly movement [depth={}]: opening batch ({}) took {} ms", depth,
				openingBatch != null ? openingBatch.size() + " snapshots" : "FAILED", System.currentTimeMillis() - tOpen);

		// ---- Pass 1: opening qty per item, and which items actually need a price
		Map<String, Float> openQtyByItem = new HashMap<>();
		List<String> needPriceIds = new ArrayList<>();
		for (ItemMaster item : items) {
			String itemId = item.getId();
			float liveQty = liveByItemId.getOrDefault(itemId, 0f);
			float grnQty  = grnByItemId.getOrDefault(itemId, 0f);
			float dcQty   = dcByItemId.getOrDefault(itemId, 0f);

			// Opening qty = last period's closing qty (tbl_monthly_report_stock)
			java.math.BigDecimal prevVal;
			if (openingBatch != null) {
				prevVal = openingBatch.get(itemId);
			} else {
				List<java.math.BigDecimal> prevQty = monthlyReportStockRepo
						.findPreviousOutstandingQtyByItemAndBeforeDate(itemId, periodStart);
				openQueryCount++;
				prevVal = (prevQty != null && !prevQty.isEmpty()) ? prevQty.get(0) : null;
			}
			float openQty;
			if (prevVal != null) {
				openQty = prevVal.floatValue();
			} else {
				// No prior snapshot (e.g. first run for this item): fall back to
				// back-calculating from current live stock, same as before.
				openQty = Math.max(0, liveQty - grnQty + dcQty);
			}
			openQtyByItem.put(itemId, openQty);

			// Items with no activity at all are skipped anyway, so don't fetch a price for them.
			if (openQty == 0 && grnQty == 0 && dcQty == 0) {
				skippedNoActivity++;
				continue;
			}
			needPriceIds.add(itemId);
		}

		// ---- Batch 2: PO unit prices (sum + count per model) for all items that need a price, in a few queries.
		// Same data as findUnitPricesByModelNumber (price-only projection, parent PO never loaded).
		// If the batch fails, priceSumBatch stays null and the old per-item query is used.
		long tPrice = System.currentTimeMillis();
		Map<String, Float> priceSumBatch = null;
		Map<String, Integer> priceCntBatch = null;
		try {
			priceSumBatch = new HashMap<>();
			priceCntBatch = new HashMap<>();
			for (int i = 0; i < needPriceIds.size(); i += 1000) {
				List<String> chunk = needPriceIds.subList(i, Math.min(i + 1000, needPriceIds.size()));
				List<Object[]> priceRows = entityManager.createQuery(
						"SELECT p.modelNo, p.unitPrice FROM PurchaseItem p WHERE p.modelNo IN :ids", Object[].class)
						.setParameter("ids", chunk)
						.getResultList();
				for (Object[] r : priceRows) {
					String mid = (String) r[0];
					float price = r[1] == null ? 0f : ((Number) r[1]).floatValue();
					priceSumBatch.merge(mid, price, Float::sum);
					priceCntBatch.merge(mid, 1, Integer::sum);
				}
			}
		} catch (Exception e) {
			log.warn("Monthly movement [depth={}]: batch price query failed, falling back to per-item queries: {}", depth, e.getMessage());
			priceSumBatch = null;
			priceCntBatch = null;
		}
		log.info("Monthly movement [depth={}]: price batch ({}) for {} items took {} ms", depth,
				priceSumBatch != null ? priceCntBatch.size() + " models with PO history" : "FAILED",
				needPriceIds.size(), System.currentTimeMillis() - tPrice);

		// ---- Pass 2: build the report rows (same order and same maths as before)
		for (ItemMaster item : items) {
			String itemId = item.getId();
			String modelNo = item.getModel();

			float grnQty  = grnByItemId.getOrDefault(itemId, 0f);
			float dcQty   = dcByItemId.getOrDefault(itemId, 0f);
			float openQty = openQtyByItem.get(itemId);
			if (openQty == 0 && grnQty == 0 && dcQty == 0) continue;
			float closingQty = Math.max(0, openQty + grnQty - dcQty);

			// Price: avg of all PO unit prices for this item — identical to ISR methodology
			// (ISR uses findByModelNumberWithRecentPoItem(itemId) and averages unit prices).
			float avgPrice;
			if (priceSumBatch != null) {
				Integer cnt = priceCntBatch.get(itemId);
				if (cnt == null || cnt == 0) continue; // ISR only includes items with PO history
				avgPrice = priceSumBatch.get(itemId) / cnt;
			} else {
				List<Float> poPrices = purchaseItemRepo.findUnitPricesByModelNumber(itemId);
				priceQueryCount++;
				if (poPrices.isEmpty()) continue; // ISR only includes items with PO history
				float priceSum = 0;
				for (Float price : poPrices) priceSum += price;
				avgPrice = priceSum / poPrices.size();
			}

			Map<String, Object> record = new java.util.LinkedHashMap<>();
			record.put("modelNo", modelNo);
			record.put("description", item.getItemName());
			record.put("openQty", openQty);
			record.put("openRate", avgPrice);
			record.put("openValue", openQty * avgPrice);
			record.put("inwardQty", grnQty);
			record.put("inwardRate", avgPrice);
			record.put("inwardValue", grnQty * avgPrice);
			record.put("outwardQty", dcQty);
			record.put("outwardRate", avgPrice);
			record.put("outwardValue", dcQty * avgPrice);
			record.put("closingQty", closingQty);
			record.put("closingRate", avgPrice);
			record.put("closingValue", closingQty * avgPrice);
			result.add(record);
		}
		log.info("Monthly movement [depth={}]: item loop total {} ms | per-item fallback queries: opening {}, price {} | skipped no-activity: {} | result rows: {}",
				depth, System.currentTimeMillis() - tLoop, openQueryCount, priceQueryCount, skippedNoActivity, result.size());

		result.sort(java.util.Comparator.comparing(r -> (String) r.get("modelNo")));

		// Save closing balance to tbl_monthly_report_stock for next month's opening
		long tSave = System.currentTimeMillis();
		java.time.LocalDate reportDate = toDate.toLocalDateTime().toLocalDate();
		monthlyReportStockRepo.deleteByReportDate(reportDate);
		java.time.LocalDateTime now = java.time.LocalDateTime.now();
		List<com.ncpl.sales.model.MonthlyReportStock> toSave = new java.util.ArrayList<>();
		for (Map<String, Object> rec : result) {
			String recModel = (String) rec.get("modelNo");
			String recItemId = modelToItemIdFull.get(recModel);
			if (recItemId == null) continue;
			float closingQtyRec = ((Number) rec.get("closingQty")).floatValue();
			float closingValRec = ((Number) rec.get("closingValue")).floatValue();
			com.ncpl.sales.model.MonthlyReportStock entry = new com.ncpl.sales.model.MonthlyReportStock();
			entry.setItemMasterId(recItemId);
			entry.setReportDate(reportDate);
			entry.setOutstandingQty(java.math.BigDecimal.valueOf(closingQtyRec));
			entry.setOutstandingValue(java.math.BigDecimal.valueOf(closingValRec));
			entry.setCreatedAt(now);
			toSave.add(entry);
		}
		monthlyReportStockRepo.saveAll(toSave);
		log.info("Monthly movement [depth={}]: save snapshot {} rows in {} ms", depth, toSave.size(), System.currentTimeMillis() - tSave);
		log.info("Monthly movement [depth={}]: TOTAL {} ms", depth, System.currentTimeMillis() - tStart);

		return result;
	}
	// ===================== END NEW VERSION =====================

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
		Set set = new HashSet();

		if (item != null) {
			List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemsByModelNumber(item.getId());
			for (PurchaseItem purchaseItem : poItemList) {
				PurchaseOrder po = purchaseItem.getPurchaseOrder();
				if (po == null) continue;
				List<Grn> grnList = findGrnByPoNumber(po.getPoNumber());
				for (Grn grnObject : grnList) {
					if (po.getParty() != null) {
						grnObject.set("vendor", po.getParty().getPartyName());
					}
					grnObject.set("poDate", po.getCreated());
					set.add(grnObject);
				}
			}
		}

		return new ArrayList<Grn>(set);
	}
	
	
	
	
}