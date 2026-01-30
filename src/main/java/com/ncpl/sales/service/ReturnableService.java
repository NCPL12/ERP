package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
		List<ReturnableItems> returnableItemsList = returnableItemsRepo.findAll();
		ArrayList<ReturnableItems> returnableList = new ArrayList<ReturnableItems>();
		for (ReturnableItems returnableItems : returnableItemsList) {
			if(returnableItems.getReturnedQty()!=0) {
			Optional<DeliveryChallanItems> dcItem = deliveryChallanItemsRepo.findById(returnableItems.getDcItemId());
			if(dcItem.isPresent()) {
			Optional<SalesItem> salesItem=salesItemRepo.findById(dcItem.get().getDescription());
			if (salesItem.isPresent()) {
			returnableItems.set("description",salesItem.get().getDescription());
			returnableItems.set("unit",salesItem.get().getItem_units().getName());
			returnableItems.set("clientId",salesItem.get().getSalesOrder().getParty().getId());
			returnableItems.set("totalQty",salesItem.get().getQuantity());
			returnableItems.set("deliveredQty",dcItem.get().getTodaysQty());
			returnableItems.set("dcNo",returnableItems.getReturnable().getDcId());
			returnableList.add(returnableItems);
			}
			}
			}
		}
		return returnableList;
	}
}
