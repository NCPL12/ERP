package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.Returnable;
import com.ncpl.sales.model.ReturnableItems;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.DeliveryChallanRepo;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.ReturnableItemsRepo;
import com.ncpl.sales.repository.ReturnableRepo;
import com.ncpl.sales.repository.SalesItemRepo;

@Service
public class ReturnableService {
	
	@Autowired
	ReturnableRepo returnableRepo;
	@Autowired
	ReturnableItemsRepo returnableItemsRepo;
	@Autowired
	DeliveryChallanItemsRepo deliveryChallanItemsRepo;
	@Autowired
	SalesItemRepo salesItemRepo;
	@Autowired
	StockService stockService;
	@Autowired
	SalesOrderDesignService soDesignService;
	@Autowired
	DeliveryChallanRepo dcRepo;
	@Autowired
	PartyRepo partyRepo;
	@Autowired
	PartyAddressService partyAddressService;
	@Autowired
	ItemMasterService itemMasterService;
	
	@Transactional
	public Returnable saveReturnableDc(Returnable returnable,String partyId) {
		List<ReturnableItems> items = returnable.getItems();
		if (items == null || items.isEmpty()) return returnableRepo.save(returnable);
		// keep only rows with returnedQty > 0 — same rule as list query (returnedQty <> 0)
		List<ReturnableItems> filtered = items.stream()
				.filter(ri -> ri.getReturnedQty() > 0)
				.collect(Collectors.toList());
		if (filtered.isEmpty()) return returnableRepo.save(returnable);
		returnable.setItems(filtered);
		for (ReturnableItems returnableItems : filtered) {
			Optional<DeliveryChallanItems> dcItemOpt = deliveryChallanItemsRepo.findById(returnableItems.getDcItemId());
			if (!dcItemOpt.isPresent()) continue;
			DeliveryChallanItems dcItem = dcItemOpt.get();
			// derivable clientId is from the SO that owns this DC item — do not trust hidden partyId
			Optional<SalesItem> salesItemOpt = salesItemRepo.findById(dcItem.getDescription());
			if (!salesItemOpt.isPresent()) continue;
			SalesItem salesItem = salesItemOpt.get();
			SalesOrderDesign designObj = soDesignService.findSalesOrderDesignObjBysalesItemId(salesItem.getId());
			if (designObj == null || designObj.getItems() == null || designObj.getItems().isEmpty()) continue;
			String itemId = designObj.getItems().get(0).getItemId();
			SalesOrder soObj = salesItem.getSalesOrder();
			String clientId = soObj.getParty().getId();
			float qty = returnableItems.getReturnedQty();
			// guard: cannot return more than delivered today
			if (qty > dcItem.getTodaysQty()) qty = dcItem.getTodaysQty();
			if (qty <= 0) continue;
			stockService.updateStockQuantityFromGrn(itemId, clientId, qty, "grn", soObj);
		}
		return returnableRepo.save(returnable);
	}

	public List<ReturnableItems> getReturnableItemsList() {
		return enrich(returnableItemsRepo.findAllNonZeroReturned());
	}

	public List<ReturnableItems> getReturnableItemsByReturnableId(int returnableId) {
		return enrich(returnableItemsRepo.findByReturnableId(returnableId));
	}

	// @D0014 lazy-loaded, paginated Returnables list (see README.md)
	public Page<ReturnableItems> getReturnableItemsListPage(int page, int size, String orderNo) {
		Page<ReturnableItems> returnablePage = returnableItemsRepo.findAllNonZeroReturnedPaged(
				orderNo == null ? "" : orderNo.trim(),
				PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
		return new PageImpl<>(enrich(returnablePage.getContent()), returnablePage.getPageable(),
				returnablePage.getTotalElements());
	}

	private List<ReturnableItems> enrich(List<ReturnableItems> returnableItemsList) {
		if (returnableItemsList.isEmpty()) {
			return returnableItemsList;
		}

		List<Integer> dcItemIds = returnableItemsList.stream()
				.map(ReturnableItems::getDcItemId)
				.collect(Collectors.toList());

		List<DeliveryChallanItems> dcItems = deliveryChallanItemsRepo.findAllById(dcItemIds);
		Map<Integer, DeliveryChallanItems> dcItemMap = new HashMap<>();
		for (DeliveryChallanItems dcItem : dcItems) {
			dcItemMap.put(dcItem.getDcItemId(), dcItem);
		}

		List<String> salesItemIds = dcItems.stream()
				.map(DeliveryChallanItems::getDescription)
				.collect(Collectors.toList());

		List<SalesItem> salesItems = salesItemRepo.findByIdsWithJoins(salesItemIds);
		Map<String, SalesItem> salesItemMap = new HashMap<>();
		for (SalesItem si : salesItems) {
			salesItemMap.put(si.getId(), si);
		}

		// header info: Returnable -> DC -> SO -> Party / shipping
		List<Integer> dcIds = returnableItemsList.stream()
				.map(ri -> ri.getReturnable() != null ? ri.getReturnable().getDcId() : null)
				.filter(java.util.Objects::nonNull).distinct().collect(Collectors.toList());
		Map<Integer, DeliveryChallan> dcMap = new HashMap<>();
		if (!dcIds.isEmpty()) {
			for (DeliveryChallan dc : dcRepo.findByDcIdIn(dcIds)) dcMap.put(dc.getDcId(), dc);
		}

		ArrayList<ReturnableItems> returnableList = new ArrayList<>();
		for (ReturnableItems returnableItems : returnableItemsList) {
			DeliveryChallanItems dcItem = dcItemMap.get(returnableItems.getDcItemId());
			if (dcItem == null) continue;
			SalesItem salesItem = salesItemMap.get(dcItem.getDescription());
			if (salesItem == null) continue;
			Returnable ret = returnableItems.getReturnable();
			DeliveryChallan dc = dcMap.get(ret != null ? ret.getDcId() : null);
			SalesOrder so = salesItem.getSalesOrder();

			// item-level (for modal/view) — like DeliveryChallanService.getDcItemList sets soModelNo
			returnableItems.set("description", salesItem.getDescription());
			returnableItems.set("unit", salesItem.getItem_units().getName());
			returnableItems.set("clientId", so.getParty().getId());
			returnableItems.set("totalQty", salesItem.getQuantity());
			returnableItems.set("deliveredQty", dcItem.getTodaysQty());
			// soModelNo from DesignItems -> ItemMaster.model
			try {
				List<DesignItems> designItems = soDesignService.getDesignItemListBySOItemId(salesItem.getId());
				ArrayList<String> models = new ArrayList<>();
				for (DesignItems di : designItems) {
					String itemId = di.getItemId();
					java.util.Optional<com.ncpl.sales.model.ItemMaster> im = itemMasterService.getItemById(itemId);
					if (im.isPresent() && im.get().getModel() != null) models.add(im.get().getModel());
					else if (itemId != null) models.add(itemId);
				}
				returnableItems.set("soModelNo", String.join(", ", models));
			} catch (Exception e) {
				returnableItems.set("soModelNo", dcItem.getSoModelNo());
			}

			// header-level columns requested for /returnableList: No, returnableId, soNumber, clientName, clientPo, shippingAddress, dcNo, date
			returnableItems.set("returnableId", ret != null ? ret.getId() : null);
			returnableItems.set("dcNo", ret != null ? ret.getDcId() : null);
			String soNumber = dc != null ? dc.getSoNumber() : (so != null ? so.getId() : null);
			returnableItems.set("soNumber", soNumber);
			String clientName = so != null && so.getParty() != null ? so.getParty().getPartyName() : null;
			returnableItems.set("clientName", clientName);
			String clientPo = so != null ? so.getClientPoNumber() : null;
			returnableItems.set("clientPo", clientPo);
			String shippingAddress = null;
			if (so != null) {
				String shipId = so.getShippingAddress();
				if (shipId != null && !shipId.isEmpty()) {
					Party partyObj = partyRepo.findById(shipId);
					if (partyObj != null) shippingAddress = partyObj.getAddr1();
					else {
						try {
							Optional<PartyAddress> pa = partyAddressService.getAddressByAddressId(shipId);
							if (pa.isPresent()) shippingAddress = pa.get().getAddr1();
						} catch (Exception ignored) {}
					}
				}
				if ((shippingAddress == null || shippingAddress.isEmpty()) && so.getParty() != null) {
					shippingAddress = so.getParty().getAddr1();
				}
			}
			returnableItems.set("shippingAddress", shippingAddress);
			java.util.Date dateVal = null;
			if (ret != null && ret.getCreated() != null) dateVal = ret.getCreated();
			else if (dcItem.getCreated() != null) dateVal = dcItem.getCreated();
			else if (returnableItems.getCreated() != null) dateVal = returnableItems.getCreated();
			returnableItems.set("date", dateVal);
			// keep dcNo alias used by JS modal
			returnableItems.set("dcNo", ret != null ? ret.getDcId() : null);
			returnableList.add(returnableItems);
		}
		return returnableList;
	}
}
