package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.PurchaseItemRepo;
import com.ncpl.sales.repository.PurchaseRepo;

@Service
public class PurchaseItemService {
	@Autowired
	PurchaseItemRepo purchaseItemRepo;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	GrnService grnService;
	@Autowired
	SalesService salesService;
	@Autowired
	PurchaseRepo poRepo;
	@Autowired
	DeliveryChallanService dcService;
	@Autowired
	PurchaseOrderService poService;
	@Autowired
	SalesOrderDesignService designService;
	@Autowired
	DeliveryChallanItemsRepo dcItemRepo;
	// ADDED: used only by getPoHistory to load PO + vendor together in one query
	@javax.persistence.PersistenceContext
	private javax.persistence.EntityManager entityManager;
	
//	@PersistenceContext
//    private EntityManager em;

	@SuppressWarnings("unused")
	public List<PurchaseItem> getPurchaseItem(String poNumber, String className) {

		List<PurchaseItem> purchaseItems = purchaseItemRepo.findByPurchaseOrder_PoNumber(poNumber);
		ArrayList<PurchaseItem> list = new ArrayList<>();
		list.addAll(purchaseItems);
		for (PurchaseItem p : purchaseItems) {
			Optional<ItemMaster> itemMasterObject = itemMasterService.getItemById(p.getModelNo());
			String salesItemId1 = p.getDescription();
			boolean value1 = false;
			Optional<SalesItem> salesItemObj = salesService.getSalesItemById(salesItemId1, value1);
			p.set("unitName", salesItemObj.get().getItem_units().getName());
			if (itemMasterObject.isPresent()) {
				p.setModelNo(itemMasterObject.get().getModel());
			}

			// same API is used for po-preview so passing a string with className to
			// differentiate
			if (className.equalsIgnoreCase("grn")) {
				String salesItemId = p.getDescription();
				boolean value = false;
				Optional<SalesItem> salesItem = salesService.getSalesItemById(salesItemId, value);
				p.set("unitName", salesItem.get().getItem_units().getName());

				String purchaseItemId = Integer.toString(p.getPurchase_item_id());
				List<GrnItems> grnList = grnService.getGrnItemByPoItemId(purchaseItemId);
				float grnQty = 0;
				float receivedQty = 0;

				if (grnList.isEmpty()) {
					receivedQty = 0;
					p.set("receivedQty", receivedQty);
					// p.set("remainingQty", receivedQty);
				} else {
					for (GrnItems grnItems : grnList) {
						receivedQty = receivedQty + grnItems.getReceivedQuantity();

					}
					p.set("receivedQty", p.getQuantity() - receivedQty);
					// p.set("remainingQty", receivedQty);
				}
				/*
				 * for (GrnItems grnItem : grnList) {
				 * grnQty=grnQty+grnItem.getReceivedQuantity();
				 * 
				 * }
				 */

				if (receivedQty == p.getQuantity()) {
					list.remove(p);
				}

			}
		}

		return list;
	}

	public boolean deletePurchaseItem(int id) {
		boolean isDeleted = false;
		 Optional<PurchaseItem> purchaseItem = purchaseItemRepo.findById(id);
		// PurchaseOrder purchaseOrder=purchaseItem.get().getPurchaseOrder();
		List<DeliveryChallanItems> dcItemList = dcService.getDcItemListBySoItemIdWhereDcQtyNotZero(purchaseItem.get().getDescription());
		// Block deletion if ANY GRN item references this PO item, not just ones with
		// non-zero received qty - a GRN line existing at all means a delivery/inspection
		// was already recorded against this item, so it must not be silently orphaned.
		List<GrnItems> grnItemList = grnService.getGrnItemByPoItemId(Integer.toString(id));
		if (grnItemList.size() > 0 || dcItemList.size()>0) {
			isDeleted = false;
		} else {
			isDeleted = true;
			purchaseItemRepo.deletePurchaseItemById(id);
		}

		return isDeleted;

	}

	// get purchase items by purchaseItemId
	public Optional<PurchaseItem> getPurchaseItemById(int purchaseItemId) {
		System.out.println(purchaseItemId);
		Optional<PurchaseItem> purchaseItem = purchaseItemRepo.findById(purchaseItemId);
		String poItemItd = Integer.toString(purchaseItem.get().getPurchase_item_id());
		String salesItemId = purchaseItem.get().getDescription();
		System.out.println("salesitemiddddd" + salesItemId);
		boolean value = false;
		Optional<SalesItem> salesItem = salesService.getSalesItemById(salesItemId, value);
		purchaseItem.get().set("unitName", salesItem.get().getItem_units().getName());
		List<GrnItems> grnList = grnService.getGrnItemByPoItemId(poItemItd);
		float receivedQty = 0;

		if (grnList.isEmpty()) {
			receivedQty = 0;
			purchaseItem.get().set("receivedQty", receivedQty);
		} else {
			for (GrnItems grnItems : grnList) {
				receivedQty = receivedQty + grnItems.getReceivedQuantity();

			}
			purchaseItem.get().set("receivedQty", purchaseItem.get().getQuantity() - receivedQty);
		}
		return purchaseItem;
	}
	
	public Optional<PurchaseItem> getPurchaseItemByPoItemId(int purchaseItemId) {
		Optional<PurchaseItem> purchaseItem = purchaseItemRepo.findById(purchaseItemId);
		return purchaseItem;
	}

	// get the list of purchase item by itemId(model no)
	public List<PurchaseItem> getPurchaseItemsByModelNumber(String model) {

		List<PurchaseItem> poItemList = purchaseItemRepo.findByModelNumber(model);
		return poItemList;
	}

	public List<PurchaseItem> getAllPurchaseItems() {
		List<PurchaseItem> purchaseItemList = purchaseItemRepo.findAll();
		return purchaseItemList;
	}

	// Lightweight lookup for the GRN dashboard (avoids serializing full entities)
	public List<java.util.Map<String, Object>> getGrnLookupList() {
		return purchaseItemRepo.findGrnLookup();
	}

	public List<PurchaseItem> getPurchaseItemsBySalesItemId(String soItemId) {

		List<PurchaseItem> poItemList = purchaseItemRepo.findBySalesItemId(soItemId);
		/*
		 * Optional<PurchaseItem> purchaseItem =
		 * purchaseItemRepo.findById(poItemList.get(0).getPurchase_item_id());
		 * PurchaseOrder po = purchaseItem.get().getPurchaseOrder();
		 */
		// Optional<PurchaseOrder> po1 =
		// poRepo.getPurchaseOrderByPoId(purchaseItem.get().getPurchase_item_id());

		return poItemList;
	}

	//bulk variant: which of the given sales item ids have PO items
	public List<String> getSalesItemIdsWithPoItems(List<String> salesItemIds) {
		return purchaseItemRepo.findSalesItemIdsWithPoItems(salesItemIds);
	}

	public PurchaseItem getPurchaseItemBySalesItemIdAndItemId(String soItemId, String itemId) {
		PurchaseItem poItem = purchaseItemRepo.findPoItemBySoItemAndItemId(soItemId, itemId);
		return poItem;

	}

	// ===================== OLD VERSION (commented out - slow: loads EVERY PO item, then 1 DB query per PO item) =====================
	/*
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getModelNos() {
		// TODO Auto-generated method stub
		Map itemMap = new HashMap();
		List<PurchaseItem> poItems = purchaseItemRepo.findAll();

		for (PurchaseItem purchaseItem : poItems) {
			Optional<ItemMaster> itemMasterObject = itemMasterService.getItemById(purchaseItem.getModelNo());
			if (itemMasterObject.isPresent()) {
				itemMap.put(itemMasterObject.get().getId(), itemMasterObject.get().getModel());
			}
		}

		System.out.println("poItems" + poItems.size());
		return itemMap;
	}
	*/
	// ===================== END OLD VERSION =====================

	// ===================== NEW VERSION (same result; 1 query for the distinct model nos + item masters loaded in batches, instead of 1 query per PO item) =====================
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getModelNos() {
		long t0 = System.currentTimeMillis();
		Map itemMap = new HashMap();

		List<String> modelNos = entityManager.createQuery(
				"SELECT DISTINCT p.modelNo FROM PurchaseItem p WHERE p.modelNo IS NOT NULL AND p.modelNo <> ''",
				String.class).getResultList();

		int batchSize = 1000;
		for (int i = 0; i < modelNos.size(); i += batchSize) {
			List<String> batch = new ArrayList<String>(modelNos.subList(i, Math.min(i + batchSize, modelNos.size())));
			Map<String, ItemMaster> itemsById = itemMasterService.getItemsByIds(batch);
			for (ItemMaster item : itemsById.values()) {
				itemMap.put(item.getId(), item.getModel());
			}
		}

		System.out.println("getModelNos: " + modelNos.size() + " distinct model nos, " + itemMap.size()
				+ " items in " + (System.currentTimeMillis() - t0) + " ms");
		return itemMap;
	}
	// ===================== END NEW VERSION =====================

	// ===================== OLD VERSION (commented out - kept for reference) =====================
	/*
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Object> getPoHistory(String itemId) {
		List<PurchaseItem> poItems = purchaseItemRepo.findByModelNumber(itemId);
		List<Object> poHistoryList = new ArrayList();

		for (PurchaseItem purchaseItem : poItems) {
			JSONObject object = new JSONObject();
			object.put("Description", purchaseItem.getPoDescription());
			object.put("unitPrice", purchaseItem.getUnitPrice());
			object.put("created", purchaseItem.getPurchaseOrder().getCreated());
			object.put("vendor", purchaseItem.getPurchaseOrder().getParty().getPartyName());
			object.put("poNumber", purchaseItem.getPurchaseOrder().getPoNumber());
			poHistoryList.add(object);

		}

		return poHistoryList;
	}
	*/
	// ===================== END OLD VERSION =====================

	// ===================== NEW VERSION (same output; reads only the 5 needed columns in ONE query, no PO/history column loaded) =====================
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Object> getPoHistory(String itemId) {
		long tPh = System.currentTimeMillis();
		List<Object> poHistoryList = new ArrayList();
		boolean done = false;

		// ---- Fast path: ONE query that reads only the columns the page shows.
		// (The old code loaded every full PO - including its big history column - for every row.)
		try {
			List<Object[]> rows = entityManager.createQuery(
					"SELECT p.poDescription, p.unitPrice, po.created, pty.partyName, po.poNumber "
							+ "FROM PurchaseItem p JOIN p.purchaseOrder po JOIN po.party pty "
							+ "WHERE p.modelNo = :id ORDER BY p.purchase_item_id",
					Object[].class)
					.setParameter("id", itemId)
					.getResultList();
			// keep the JSON number type exactly as before (primitive float -> double, boxed Float -> Float)
			boolean unitPricePrimitive = PurchaseItem.class.getMethod("getUnitPrice").getReturnType().isPrimitive();
			for (Object[] r : rows) {
				JSONObject object = new JSONObject();
				object.put("Description", r[0]);
				if (unitPricePrimitive) {
					object.put("unitPrice", r[1] == null ? 0d : ((Number) r[1]).doubleValue());
				} else {
					object.put("unitPrice", r[1]);
				}
				object.put("created", r[2]);
				object.put("vendor", r[3]);
				object.put("poNumber", r[4]);
				poHistoryList.add(object);
			}
			done = true;
			System.out.println("PO item history: fast query " + rows.size() + " rows in " + (System.currentTimeMillis() - tPh) + " ms");
		} catch (Exception e) {
			System.err.println("getPoHistory: fast query failed, using fetch-join fallback: " + e.getMessage());
			poHistoryList.clear();
		}

		// ---- Fallback 1/2: full entities with PO + vendor fetched together; Fallback 2/2: the original repo call
		if (!done) {
			List<PurchaseItem> poItems;
			try {
				poItems = entityManager.createQuery(
						"SELECT p FROM PurchaseItem p JOIN FETCH p.purchaseOrder po JOIN FETCH po.party "
								+ "WHERE p.modelNo = :id ORDER BY p.purchase_item_id",
						PurchaseItem.class)
						.setParameter("id", itemId)
						.getResultList();
			} catch (Exception e) {
				System.err.println("getPoHistory: fetch-join query failed, using old query: " + e.getMessage());
				poItems = purchaseItemRepo.findByModelNumber(itemId);
			}
			for (PurchaseItem purchaseItem : poItems) {
				JSONObject object = new JSONObject();
				object.put("Description", purchaseItem.getPoDescription());
				object.put("unitPrice", purchaseItem.getUnitPrice());
				object.put("created", purchaseItem.getPurchaseOrder().getCreated());
				object.put("vendor", purchaseItem.getPurchaseOrder().getParty().getPartyName());
				object.put("poNumber", purchaseItem.getPurchaseOrder().getPoNumber());
				poHistoryList.add(object);
			}
		}
		System.out.println("PO item history: TOTAL " + (System.currentTimeMillis() - tPh) + " ms, " + poHistoryList.size() + " rows, fast path used = " + done);

		return poHistoryList;
	}
	// ===================== END NEW VERSION =====================

	public List<PurchaseItem> findByModelNumberWithLatestPoItem(String modelNo) {
		// TODO Auto-generated method stub
		List<PurchaseItem> poItemList = purchaseItemRepo.findByModelNumberWithLatestPoItem(modelNo);
		for (PurchaseItem purchaseItem : poItemList) {
			
			purchaseItem.set("vendor", purchaseItem.getPurchaseOrder().getParty().getPartyName());

		}
		return poItemList;
	
	}
	
	public List<PurchaseItem> findByModelNumberWithLatestAndCheapestPricePoItem(String modelNo) {
		// TODO Auto-generated method stub
		List<PurchaseItem> poItemList = purchaseItemRepo.findByModelNumberWithLatestPoItem(modelNo);
		List<PurchaseItem> purchaseItemList= new ArrayList<PurchaseItem>();
		purchaseItemList.add(poItemList.get(0));
		List<PurchaseItem> poItemCheapestList=purchaseItemRepo.findCheapestPurchaseItem(modelNo);
		purchaseItemList.add(poItemCheapestList.get(0));
		for (PurchaseItem purchaseItem : purchaseItemList) {
			
			purchaseItem.set("vendor", purchaseItem.getPurchaseOrder().getParty().getPartyName());

		}
		return purchaseItemList;
	
	}
	
	
	public List<PurchaseItem> findByModelNumberWithRecentPoItem(String modelNo) {
		// TODO Auto-generated method stub
		List<PurchaseItem> poItemList = purchaseItemRepo.findByModelNumberWithLatestPoItem(modelNo);
		return poItemList;
	
	}

	public boolean checkForDcInvoiceExists(String salesItemId) {
		boolean itemExists = false;
		List<DeliveryChallanItems> dcList = dcService.getDcItemListBySoItemId(salesItemId);
		//List<SalesOrderDesign> designList = soDesignService.findSalesOrderDesignBysalesItemId(salesItemId);
		//List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItemId);
		if (dcList.size() > 0) {
			itemExists = true;
		} else {
			itemExists = false;
		}
		return itemExists;
	}
	public List<PurchaseItem> getPartialPoItems(String poNumber) {
		Optional<PurchaseOrder> po = poService.findById(poNumber);
		List<PurchaseItem> poItemList = po.get().getItems();
		ArrayList<PurchaseItem> purchaseItemList = new ArrayList<PurchaseItem>();
		for (PurchaseItem purchaseItem : poItemList) {
			String salesItemId=purchaseItem.getDescription();
			List<DesignItems> designItems = designService.getDesignItemListBySOItemId(salesItemId);
			for (DesignItems designItem : designItems) {
				if(designItem.getItemId().equals(purchaseItem.getModelNo())) {
					if(designItem.getQuantity()!=purchaseItem.getQuantity()) {
						purchaseItemList.add(purchaseItem);
					}
				}
				
			}
		}
		return purchaseItemList;
	
	}
	
	public List<PurchaseItem> getPurchaseItemListBySalesItemIdAndItemId(String soItemId, String itemId) {
		List<PurchaseItem> poItemList = purchaseItemRepo.findPoItemListBySoItemAndItemId(soItemId, itemId);
		return poItemList;

	}
	
	@SuppressWarnings("unused")
	public List<PurchaseItem> getPurchaseItemList(String poNumber) {

		List<PurchaseItem> purchaseItems = purchaseItemRepo.findByPurchaseOrder_PoNumber(poNumber);
		ArrayList<PurchaseItem> list = new ArrayList<>();
		list.addAll(purchaseItems);
		return list;
	}

	public Map<String, Object> findByModelwiseQuantityDetails(String modelNo, String salesItemId) {
		List<PurchaseItem> poItemList=getPurchaseItemListBySalesItemIdAndItemId(salesItemId, modelNo);
		@SuppressWarnings("rawtypes")
		List<Object> poItemListArray = new ArrayList(); 
		//List<DesignItems> designItemsList=designService.getAllDesignItemListBySOItemId(salesItemId);
		 List<DeliveryChallanItems> dcItemList =dcItemRepo.getDcItemListBySalesItemId(salesItemId);
		float deliveredQty=0;
		Map<String, Object> response = new HashMap<>();
		
		for (DeliveryChallanItems dcItem : dcItemList) {
			deliveredQty=(int) (deliveredQty+dcItem.getTodaysQty());
		}
		/*for (DesignItems designItems : designItemsList) {
			if(designItems.getItemId()==modelNo) {
				deliveredQty=deliveredQty+designItems.getDeliveredQty();
			}
		}*/
		response.put("deliveredQty", deliveredQty);
		float orderedQty=0;
		for (PurchaseItem purchaseItem : poItemList) {
			
			orderedQty=orderedQty+purchaseItem.getQuantity();
			

		}
		response.put("orderedQty", orderedQty);
		System.out.println("qty detail"+orderedQty+"&"+deliveredQty);

		return response;
	}
}

