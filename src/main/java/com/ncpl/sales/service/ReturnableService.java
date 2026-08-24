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

import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Returnable;
import com.ncpl.sales.model.ReturnableItems;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
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
	
	public Returnable saveReturnableDc(Returnable returnable,String partyId) {
		
		List<ReturnableItems> returnableItemsList=returnable.getItems();
		for (ReturnableItems returnableItems : returnableItemsList) {
			Optional<DeliveryChallanItems> dcItem = deliveryChallanItemsRepo.findById(returnableItems.getDcItemId());
			Optional<SalesItem> salesItem=salesItemRepo.findById(dcItem.get().getDescription());
			SalesOrderDesign designObj = soDesignService.findSalesOrderDesignObjBysalesItemId(salesItem.get().getId());
			List<DesignItems> designItemList=designObj.getItems();
			String itemId =(String) designItemList.get(0).getItemId();
			SalesOrder soObj = salesItem.get().getSalesOrder();
			String clientId = soObj.getParty().getId();
			String className = "grn";

			float qty = returnableItems.getReturnedQty();
			stockService.updateStockQuantityFromGrn(itemId, clientId, qty, className, soObj);
		}
		Returnable returnableObj = returnableRepo.save(returnable);
		
		return returnableObj;
		
	}

	public List<ReturnableItems> getReturnableItemsList() {
		return enrich(returnableItemsRepo.findAllNonZeroReturned());
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

		ArrayList<ReturnableItems> returnableList = new ArrayList<>();
		for (ReturnableItems returnableItems : returnableItemsList) {
			DeliveryChallanItems dcItem = dcItemMap.get(returnableItems.getDcItemId());
			if (dcItem == null) continue;
			SalesItem salesItem = salesItemMap.get(dcItem.getDescription());
			if (salesItem == null) continue;
			returnableItems.set("description", salesItem.getDescription());
			returnableItems.set("unit", salesItem.getItem_units().getName());
			returnableItems.set("clientId", salesItem.getSalesOrder().getParty().getId());
			returnableItems.set("totalQty", salesItem.getQuantity());
			returnableItems.set("deliveredQty", dcItem.getTodaysQty());
			returnableItems.set("dcNo", returnableItems.getReturnable().getDcId());
			returnableList.add(returnableItems);
		}
		return returnableList;
	}
}
