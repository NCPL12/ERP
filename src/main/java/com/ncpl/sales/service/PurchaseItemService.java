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
		List<GrnItems> grnItemList = grnService.getGrnItemByPoItemIdWhereRcvdQtyNonZero(Integer.toString(id));
		if (grnItemList.size() > 0 || dcItemList.size()>0) {
			isDeleted = false;
		} else {
			isDeleted = true;
			purchaseItemRepo.deleteById(id);
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

	public PurchaseItem getPurchaseItemBySalesItemIdAndItemId(String soItemId, String itemId) {
		PurchaseItem poItem = purchaseItemRepo.findPoItemBySoItemAndItemId(soItemId, itemId);
		return poItem;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map getModelNos() {
		// TODO Auto-generated method stub
		Map itemMap = new HashMap();
		List<PurchaseItem> poItems = purchaseItemRepo.findAll();

		for (PurchaseItem purchaseItem : poItems) {
			Optional<ItemMaster> itemMasterObject = itemMasterService.getItemListById(purchaseItem.getModelNo());
			if (itemMasterObject.isPresent()) {
				itemMap.put(itemMasterObject.get().getId(), itemMasterObject.get().getModel());
			}
		}

		System.out.println("poItems" + poItems.size());
		return itemMap;
	}

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

