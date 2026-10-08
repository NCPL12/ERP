package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.InvoiceItem;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.WorkOrder;
import com.ncpl.sales.model.WorkOrderItems;
import com.ncpl.sales.repository.WorkOrderRepo;

@Service
public class WorkOrderService {
	
	@Autowired
	WorkOrderRepo workOrderRepo;
	
	@Autowired
	PartyService partyService;

	public WorkOrder saveWorkOrder(WorkOrder workOrder, String partyId) {
		Party party=partyService.getPartyById(partyId);
		workOrder.setParty(party);
		WorkOrder workOrderObj = workOrderRepo.save(workOrder);
		return workOrderObj;
	}

	public List<WorkOrder> getWorkOrderList() {
		List<WorkOrder> workOrderList = workOrderRepo.findAll();
		for (WorkOrder workOrder : workOrderList) {
			String clientPoNo=workOrder.getSalesOrder().getClientPoNumber();
			workOrder.set("clientPoNumber",clientPoNo);
		}
		return workOrderList;
	}

	public Optional<WorkOrder> getWorkOrderById(String workOrderId) {
		Optional<WorkOrder> workOrder = workOrderRepo.findById(workOrderId);
		return workOrder;
	}

	public List<WorkOrderItems> getWorkOrderItemList(String woId) {
		List<WorkOrder> workOrderList = workOrderRepo.findWorkOrderListById(woId);
		ArrayList<WorkOrderItems> itemList = new ArrayList<WorkOrderItems>();
		for (WorkOrder workOrder : workOrderList) {
			List<WorkOrderItems> workOrderItemList=workOrder.getItems();
			
			itemList.addAll(workOrderItemList);
		}
		return itemList;
	}

}
