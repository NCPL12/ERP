package com.ncpl.sales.service;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.common.Constants;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.CompanyAssets;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.model.Supplier;
import com.ncpl.sales.repository.CompanyAssetsRepo;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.PurchaseItemRepo;
import com.ncpl.sales.repository.PurchaseRepo;
import com.ncpl.sales.repository.SupplierRepo;
import com.ncpl.sales.util.JSONArrayPojoUtil;
import com.ncpl.sales.util.NcplUtil;

@Service
public class PurchaseOrderService {
	@Autowired
	PurchaseRepo purchaseRepo;
	@Autowired
	SalesService salesService;
	@Autowired
	PartyService partyService;
	@Autowired
	NcplUtil utilService;
	@Autowired
	JSONArrayPojoUtil jsonArrayPojoUtil;
	@Autowired
	PartyAddressService addressService;
	@Autowired
	SalesOrderDesignService designService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	GrnService grnService;
	@Autowired
	DeliveryChallanService dcService;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	PurchaseItemRepo purchaseItemRepo;
	@Autowired
	PartyRepo partyRepo;
	@Autowired
	SupplierRepo supplierRepo;
	 @Autowired
	    EntityManagerFactory emf;
			Session session;
	public PurchaseOrder savePurchaseOrder(PurchaseOrder purchaseOrder,String salesOrderId,String partyId ){
		
		//Optional<SalesOrder> salesOrder=salesService.getSalesOrderById(salesOrderId);
		//purchaseOrder.setSalesOrder(salesOrder.get());
		Party party=partyService.getPartyById(partyId);
		purchaseOrder.setParty(party);
		PurchaseOrder purchaseOrderObject=purchaseRepo.save(purchaseOrder);
		//PurchaseExcel.buildExcelDocument(purchaseOrderObject,response,filePath, itemMasterService);
		
		 
		//updateSupplierPrice(purchaseOrderObject);
		//update(purchaseOrderObject);
		return purchaseOrderObject;
	}
	
	/* private void updateSupplierPrice(PurchaseOrder purchaseOrderObject) {
		 List<PurchaseItem> purchaseItemList = purchaseOrderObject.getItems();
		 for (PurchaseItem purchaseItem : purchaseItemList) {
			 float newUnitPrice=purchaseItem.getUnitPrice();
			 String itemId=purchaseItem.getModelNo();
			 String vendorId = purchaseOrderObject.getParty().getId();
			 Supplier supplier = supplierRepo.findSupplierByItemIdAndClientId(itemId, vendorId);
			 if(supplier!=null) {
			 supplier.setCostPrice(newUnitPrice);
			 supplierRepo.save(supplier);
			 }
		}
	}*/

	public List<PurchaseOrder> getPurchaseOrderListByPartyId(String partyId){
			List<PurchaseOrder> purchaseOrderList=purchaseRepo.getPurchaseListByPartyId(partyId);
			return purchaseOrderList;
		}
	
	//Get all purchase Orders
	public List<PurchaseOrder> findAll(){
		List<PurchaseOrder> poList = purchaseRepo.findAll();
		return poList;
	}
	
	public List<PurchaseOrder> findAllPO(){
		List<PurchaseOrder> poList = purchaseRepo.findAllPO();
		Map<String, Object[]> totalsByPo = new HashMap<>();
		for (Object[] row : purchaseItemRepo.getActivePoTotals()) {
			if (row == null || row.length < 3 || row[0] == null) {
				continue;
			}
			totalsByPo.put(row[0].toString(), row);
		}
		for (PurchaseOrder po : poList) {
			Object[] totals = totalsByPo.get(po.getPoNumber());
			double total = 0;
			double gstTotal = 0;
			if (totals != null) {
				if (totals[1] instanceof Number) {
					total = ((Number) totals[1]).doubleValue();
				}
				if (totals[2] instanceof Number) {
					gstTotal = ((Number) totals[2]).doubleValue();
				}
			}
			double grandTotal = total + gstTotal;
			grandTotal = Math.round(grandTotal * 100.0) / 100.0;
			po.setGrandTotal(grandTotal);
		}
		return poList;
	}
	//Get purchase order by id
	public Optional<PurchaseOrder> findById(String purchaseOrderId) {
		Optional<PurchaseOrder> po = purchaseRepo.findById(purchaseOrderId);
		return po;
	}
	
	//Batch load purchase orders by PO numbers
	public List<PurchaseOrder> findByPoNumberIn(List<String> poNumbers) {
		return purchaseRepo.findByPoNumberIn(poNumbers);
	}
	
	public JSONArray preparePurchaseOrderHistory(String poNumber) {

		
		Optional<PurchaseOrder> purchaseOrder = findById(poNumber);
		PurchaseOrder previousVersion = purchaseOrder.get();

		//below code is to prepare history object
		JSONObject previousVersionObject = new JSONObject();
		ObjectMapper mapper = utilService.getObjectMapper();
	       
	    String jsonString = null;
		try {
			jsonString = mapper.writeValueAsString(previousVersion.getItems());
		} catch (JsonProcessingException e) {  
			
			e.printStackTrace();
		}
		
		previousVersionObject.put("version", previousVersion.getVersion());
		previousVersionObject.put("created", previousVersion.getCreated());
		previousVersionObject.put("updated", previousVersion.getUpdated());
		previousVersionObject.put("partyId", previousVersion.getParty().getId());
		previousVersionObject.put("partyName", previousVersion.getParty().getPartyName());
		previousVersionObject.put("items", jsonString);
		
	    JSONArray previousVersionHistoryArr = previousVersion.getHistory();
	    if(previousVersionHistoryArr == null) {
	    	 JSONArray  arr = new  JSONArray ();
	    	 arr.put(previousVersionObject);
	    	 return arr;
	    }else {
	    	 previousVersionHistoryArr.put(previousVersionObject);
	    	 previousVersion.setHistory(previousVersionHistoryArr);
	    	 
	    	 return previousVersionHistoryArr;
	    }
	
	}
	
	public Optional<PurchaseOrder> findByIdAndVersion(String poNumber, String version,String versionIndex) {
		Optional<PurchaseOrder> po = findById(poNumber);
			
		if(po.get().getVersion() == Integer.parseInt(version)) {
			return po;
		}else {
			JSONArray history = po.get().getHistory();
			JSONObject objectByVersion = (JSONObject) history.get(Integer.parseInt(versionIndex));
			PurchaseOrder poByVersion = new PurchaseOrder();
			poByVersion.setPoNumber(po.get().getPoNumber());
			poByVersion.setVersion(Integer.parseInt(version));
			
			String dateStr =  objectByVersion.getString("created");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = null;
			try {
				date = sdf.parse(dateStr);
			} catch (ParseException e) {
				
				e.printStackTrace();
			}
			
			poByVersion.setCreated(date);
			@SuppressWarnings("unchecked")
			List<PurchaseItem> items = (List<PurchaseItem>) jsonArrayPojoUtil.jsonArrayToPojo(objectByVersion.get("items").toString());
			poByVersion.setItems(items);
			poByVersion.setParty(po.get().getParty());
			
			Optional<PurchaseOrder> optionalPo = Optional.of(poByVersion); 
			
			return optionalPo;
		}
	}

	//On editing of purchase order updating items..
	public void updatePo(PurchaseOrder purchaseOrder) {
		String purchaseOrderNumber = purchaseOrder.getPoNumber();
		JSONArray history = preparePurchaseOrderHistory(purchaseOrderNumber);
		PurchaseOrder poToUpdate = purchaseRepo.getOne(purchaseOrderNumber);
		poToUpdate.setHistory(history);
		poToUpdate.setItems(purchaseOrder.getItems());
		List<PurchaseItem> purchaseItems = purchaseOrder.getItems();
		for (PurchaseItem purchaseItem : purchaseItems) {
			Date delDate =purchaseItem.getDelivaryDate();
			System.out.println(delDate);
			if(delDate.toString().equalsIgnoreCase("Wed Dec 31 19:00:00 EST 1969")) {
				purchaseItem.setDelivaryDate(null);
			}
		}
		PurchaseOrder poToUpdateObj=purchaseRepo.save(poToUpdate);
		//updateSupplierPrice(poToUpdateObj);
	}
	
	public Map<Object, Object> findVendorsByPurchaseOrder(String poNumber){
		Optional<PurchaseOrder> po = purchaseRepo.findById(poNumber);
		Party vendorParty = po.get().getParty();
		String partyId = vendorParty.getId();
		Map<Object, Object> addressMap = new HashMap<Object, Object>();
		addressMap.put("vendor", vendorParty);
		List<PartyAddress> addressList = addressService.getAddressById(partyId);
		addressMap.put("addresses", addressList);
		List<PurchaseItem> poItems=po.get().getItems();
		String alternateShippingAdr = "";
		List<Party> shippingAddressPartyList=new ArrayList<>();
		for (int i = 0; i < poItems.size(); i++) {
			String soItemId=poItems.get(i).getDescription();
			boolean value=false;
			Optional<SalesItem> salesItemObj = salesService.getSalesItemById(soItemId, value);
			SalesOrder soObj=salesItemObj.get().getSalesOrder();
			alternateShippingAdr=soObj.getShippingAddress();
			Party shippingAddreParty=partyRepo.findById(alternateShippingAdr);
			if(shippingAddreParty!=null) {
			shippingAddressPartyList.add(shippingAddreParty);
			}
		}
		@SuppressWarnings("unchecked")
		Set<Party> shippingAddressPartySet = new HashSet(shippingAddressPartyList);
		//Party shippingAddreParty=partyRepo.findById(alternateShippingAdr);
		
			addressMap.put("alternateShippingAdrress", shippingAddressPartySet);
		return addressMap;
	}
	
	public List<DesignItems> getDesignListOfItemBySalesItemId(String salesItemId) {
		//List<SalesOrderDesign> soDesignList=designService.findSalesOrderDesignBysalesItemId(salesItemId);
		SalesOrderDesign soDesignObj=designService.findSalesOrderDesignObjBysalesItemId(salesItemId);
		List<DesignItems> designItemList=new ArrayList<>();
		if(soDesignObj!=null) {
			//ArrayList<ItemMaster> list = new ArrayList<ItemMaster>();
			designItemList = soDesignObj.getItems();
		    for (DesignItems designItems : designItemList) {
		    	String itemId = designItems.getItemId();
		    	Optional<ItemMaster> itemObj=itemMasterService.getItemById(itemId);
		    	designItems.set("model", itemObj.get().getModel());
			} 
		}
		return designItemList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public List<Object> getPendingPoList(String name) {

		List<Object> pendingPoList = new ArrayList();
		List<PurchaseOrder> purchaseOrderList =getPurchaseOrderListByPartyId(name);
		for (int i = 0; i < purchaseOrderList.size(); i++) {
			List<Grn> grnObjList = grnService.findGrnByPoNumber(purchaseOrderList.get(i).getPoNumber());
			List<PurchaseItem> poItemList = purchaseOrderList.get(i).getItems();
			if(grnObjList.isEmpty()) {
			//	List<PurchaseItem> poItemList = purchaseOrderList.get(i).getItems();
				for (PurchaseItem purchaseItem : poItemList) {
					Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
					JSONObject object = new JSONObject();
					String units = itemObj.get().getItem_units().getName();
					object.put("model", itemObj.get().getModel());
					object.put("itemName", itemObj.get().getItemName());
					object.put("hsnCode", itemObj.get().getHsnCode());
					object.put("units", itemObj.get().getItem_units().getName());
					object.put("Date",purchaseOrderList.get(i).getCreated());
					object.put("Qty", purchaseItem.getQuantity());
					object.put("poNum", purchaseItem.getPurchaseOrder().getPoNumber());
					
					pendingPoList.add(object);
				}
			}else {
				for (PurchaseItem purchaseItem : poItemList) {
					int poItemId = purchaseItem.getPurchase_item_id();
					List<GrnItems> grnItemObj = grnService.getGrnItemObjByPoItemId(Integer.toString(poItemId));
					float receivedQuantity = 0;
					for (GrnItems grnObj : grnItemObj) {
						receivedQuantity =  receivedQuantity + grnObj.getReceivedQuantity();
					}
					if(grnItemObj.size()>0 ) {
		        		float remaminingQuantity = purchaseItem.getQuantity() - receivedQuantity;
		        		Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
						if(remaminingQuantity>0) {
		        		JSONObject object = new JSONObject();
						String units = itemObj.get().getItem_units().getName();
						object.put("model", itemObj.get().getModel());
						object.put("itemName", itemObj.get().getItemName());
						object.put("hsnCode", itemObj.get().getHsnCode());
						object.put("units", itemObj.get().getItem_units().getName());
						object.put("Date",purchaseOrderList.get(i).getCreated());
						object.put("Qty", remaminingQuantity);
						object.put("poNum", purchaseItem.getPurchaseOrder().getPoNumber());
						pendingPoList.add(object);
						}
					}else {
						Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
						JSONObject object = new JSONObject();
						String units = itemObj.get().getItem_units().getName();
						object.put("model", itemObj.get().getModel());
						object.put("itemName", itemObj.get().getItemName());
						object.put("hsnCode", itemObj.get().getHsnCode());
						object.put("units", itemObj.get().getItem_units().getName());
						object.put("Date",purchaseOrderList.get(i).getCreated());
						object.put("Qty", purchaseItem.getQuantity());
						object.put("poNum", purchaseItem.getPurchaseOrder().getPoNumber());
						
						pendingPoList.add(object);
						
					}
				}
				
				/*
				 * for (Grn grn : grnObjList) { List<GrnItems> grnItemsList = grn.getItems();
				 * for(int k =0;k<grnItemsList.size();k++) { int receivedQuantity =
				 * grnItemsList.get(k).getReceivedQuantity(); String poItemId =
				 * grnItemsList.get(k).getDescription(); Optional<PurchaseItem> poItemObj =
				 * purchaseItemService.getPurchaseItemById(Integer.parseInt(poItemId)); int
				 * poQuantity = poItemObj.get().getQuantity(); int remaminingQuantity =
				 * poQuantity - receivedQuantity; Optional<ItemMaster> itemObj =
				 * itemMasterService.getItemById(poItemObj.get().getModelNo()); JSONObject
				 * object = new JSONObject(); String units =
				 * itemObj.get().getItem_units().getName(); object.put("model",
				 * itemObj.get().getModel()); object.put("itemName",
				 * itemObj.get().getItemName()); object.put("hsnCode",
				 * itemObj.get().getHsnCode()); object.put("units",
				 * itemObj.get().getItem_units().getName());
				 * object.put("Date",purchaseOrderList.get(i).getCreated()); object.put("Qty",
				 * remaminingQuantity); pendingPoList.add(object); } }
				 */
					
					
				
			}
		}

		return pendingPoList;
	
	
	
}

	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public List<Object> getOutstandingReportList(String name) {
		// TODO Auto-generated method stub
		List<Object> oustandingStockList = new ArrayList();
		
		List<PurchaseOrder> purchaseList = getPurchaseOrderListByPartyId(name);
		// for (PurchaseOrder purchaseOrder : purchaseList) {
		for (int i = 0; i < purchaseList.size(); i++) {

			List<Grn> grnObjList = grnService.findGrnByPoNumber(purchaseList.get(i).getPoNumber());
			if (grnObjList.size() > 0) {
				List<PurchaseItem> soItem = purchaseList.get(i).getItems();
				for (PurchaseItem purchaseItem : soItem) {
					List<DeliveryChallanItems> dcList = dcService.getDcItemListBySoItemId(purchaseItem.getDescription());
					if (dcList.size() > 0) {

					} else {
						Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
						// ItemMaster itemObj =
						// itemMasterService.getItemByModelNo(purchaseItem.getModelNo());
						Object item = itemMasterService.getItemById(purchaseItem.getModelNo());
						List<GrnItems> grnItems = grnService
								.getGrnItemByPoItemId(Integer.toString(purchaseItem.getPurchase_item_id()));
						if (grnItems.size() > 0) {
						JSONObject object = new JSONObject();
						String units = itemObj.get().getItem_units().getName();
						object.put("model", itemObj.get().getModel());
						object.put("itemName", itemObj.get().getItemName());
						object.put("hsnCode", itemObj.get().getHsnCode());
						object.put("units", itemObj.get().getItem_units().getName());
						object.put("grnDate", grnObjList.get(i).getCreated());
						object.put("grnQty", grnItems.get(0).getReceivedQuantity());

						oustandingStockList.add(object);
						}
					}
				}
			}

			System.out.println("name" + name);

		}
		return oustandingStockList;
	}

	/*@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<PurchaseOrder> getPenidngPoList() {
		long startTime = System.currentTimeMillis();
		// TODO Auto-generated method stub
		List<PurchaseOrder> pendingPoList = new ArrayList();
		List<PurchaseOrder> puchaseOrderList = findAllPO();
		for (PurchaseOrder purchaseOrder : puchaseOrderList) {
			List<Grn> grnObj = grnService.findGrnByPoNumber(purchaseOrder.getPoNumber());
			if (grnObj.size() == 0) {
				pendingPoList.add(purchaseOrder);
			} else {
				List<PurchaseItem> poItems = purchaseOrder.getItems();
				for (PurchaseItem purchaseItemObj : poItems) {
					float grnReceivedQty = 0;
					List<GrnItems> grnItemsList = grnService
							.getGrnItemByPoItemId(Integer.toString(purchaseItemObj.getPurchase_item_id()));
					for (GrnItems grnItem : grnItemsList) {
						grnReceivedQty = grnReceivedQty + grnItem.getReceivedQuantity();
						}
                      if(grnReceivedQty!= purchaseItemObj.getQuantity()) {
                    	  pendingPoList.add(purchaseOrder);
                    	  break;
                      }
				}
			}

		}
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	    System.out.println("time to loop each item of purchase list"+startTime+"&"+ stopTime);
		return pendingPoList;

	}*/
	
	public List<PurchaseOrder> getPenidngPoList() {
		List<PurchaseOrder> pendingPoList = new ArrayList();
		List<PurchaseOrder> poWhereGrnNotDone = purchaseRepo.getPurchaseListWhereGrnNotDone();
		pendingPoList.addAll(poWhereGrnNotDone);
		List<PurchaseOrder> pendingList = purchaseRepo.getpendingPoList();
		pendingPoList.addAll(pendingList);
		return pendingPoList;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getPurchaseOrderbyItemId(String model) {
		ItemMaster item = itemMasterService.getItemByModelNo(model.trim());
		ArrayList<PurchaseOrder> poList;
		Set set = new HashSet();
		float accountedQty=0;
		float orderedQty=0;
		if(item==null) {
			poList=new ArrayList<PurchaseOrder>(set);
		}else{
		List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemsByModelNumber(item.getId());
		
		for (PurchaseItem purchaseItem : poItemList) {
			orderedQty=orderedQty+purchaseItem.getQuantity();
			PurchaseOrder po=purchaseItem.getPurchaseOrder();
			set.add(po);
			List<GrnItems> grnItemList = grnService.getGrnItemByPoItemId(Integer.toString(purchaseItem.getPurchase_item_id()));
			if(!grnItemList.isEmpty()) {
				for (GrnItems grnItem : grnItemList) {
					accountedQty=accountedQty+grnItem.getReceivedQuantity();
				}
			}
		}
		
		 poList = new ArrayList<PurchaseOrder>(set);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("poList", poList);
		map.put("accountedQty", accountedQty);
		map.put("orderedQty", orderedQty);
		return map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public List<Object> getPendingPoListByPoNumber(String poNumber) {

		List<Object> pendingPoList = new ArrayList();
		Optional<PurchaseOrder> purchaseOrder = findById(poNumber);
		//List<PurchaseOrder> purchaseOrderList =getPurchaseOrderListByPartyId(name);
			List<Grn> grnObjList = grnService.findGrnByPoNumber(poNumber);
			List<PurchaseItem> poItemList = purchaseOrder.get().getItems();
			if(grnObjList.isEmpty()) {
			//	List<PurchaseItem> poItemList = purchaseOrderList.get(i).getItems();
				for (PurchaseItem purchaseItem : poItemList) {
					Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
					JSONObject object = new JSONObject();
					String units = itemObj.get().getItem_units().getName();
					object.put("model", itemObj.get().getModel());
					object.put("itemName", itemObj.get().getItemName());
					object.put("hsnCode", itemObj.get().getHsnCode());
					object.put("units", itemObj.get().getItem_units().getName());
					object.put("Date",purchaseOrder.get().getCreated());
					object.put("Qty", purchaseItem.getQuantity());
					
					pendingPoList.add(object);
				}
			}else {
				for (PurchaseItem purchaseItem : poItemList) {
					int poItemId = purchaseItem.getPurchase_item_id();
					List<GrnItems> grnItemObj = grnService.getGrnItemObjByPoItemId(Integer.toString(poItemId));
					float receivedQuantity = 0;
					for (GrnItems grnObj : grnItemObj) {
						receivedQuantity =  receivedQuantity + grnObj.getReceivedQuantity();
					}
					if(grnItemObj.size()>0 ) {
		        		float remaminingQuantity = purchaseItem.getQuantity() - receivedQuantity;
		        		Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
						if(remaminingQuantity>0) {
		        		JSONObject object = new JSONObject();
						String units = itemObj.get().getItem_units().getName();
						object.put("model", itemObj.get().getModel());
						object.put("itemName", itemObj.get().getItemName());
						object.put("hsnCode", itemObj.get().getHsnCode());
						object.put("units", itemObj.get().getItem_units().getName());
						object.put("Date",purchaseOrder.get().getCreated());
						object.put("Qty", remaminingQuantity);
						pendingPoList.add(object);
						}
					}else {
						Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
						JSONObject object = new JSONObject();
						String units = itemObj.get().getItem_units().getName();
						object.put("model", itemObj.get().getModel());
						object.put("itemName", itemObj.get().getItemName());
						object.put("hsnCode", itemObj.get().getHsnCode());
						object.put("units", itemObj.get().getItem_units().getName());
						object.put("Date",purchaseOrder.get().getCreated());
						object.put("Qty", purchaseItem.getQuantity());
						
						pendingPoList.add(object);
						
					}
				}
				
					
				
			}
		

		return pendingPoList;
	
	
	
}

	public List<PurchaseOrder> getPurchaseOrderListByDate(Timestamp sqlFromDate, Timestamp sqlToDate) {
		List<PurchaseOrder> poList = purchaseRepo.findPurchaseOrderListByDate(sqlFromDate,sqlToDate);
		return poList;
	}
	
	public List<PurchaseItem> getPurchaseItemsWhereDlDateNotNullAndGrnNotDone(){
		Date todayDate = new Date();
		ArrayList<PurchaseOrder> poListWhereGrnNotDone = new ArrayList<PurchaseOrder>();
		ArrayList<PurchaseItem> purchaseItemWhereDlDateNotEmpty = new ArrayList<PurchaseItem>();
		List<PurchaseOrder> puchaseOrderList = findAllPO();
		for (PurchaseOrder purchaseOrder : puchaseOrderList) {
			List<Grn> grnList = grnService.findGrnByPoNumber(purchaseOrder.getPoNumber());
			if(grnList.size()==0) {
				poListWhereGrnNotDone.add(purchaseOrder);
			}
		}
		for (PurchaseOrder po : poListWhereGrnNotDone) {
			List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemList(po.getPoNumber());
			for (PurchaseItem poItem : poItemList) {
				Date dlDate = poItem.getDelivaryDate();
				
				if(dlDate!=null) {
					long difference_In_Time=  dlDate.getTime()-todayDate.getTime();
					long difference_In_Days = (difference_In_Time/ (1000 * 60 * 60 * 24))% 365;
					if(difference_In_Days<5 && difference_In_Days>=0) {
					purchaseItemWhereDlDateNotEmpty.add(poItem);
					System.out.println(poItem.getPurchase_item_id());
					}
				}
			}
		}
		
		return purchaseItemWhereDlDateNotEmpty;
		
	}
	
	public List<PurchaseItem> getPurchaseItemsWhereLrNumEMpty(){
		Date todayDate = new Date();
		ArrayList<PurchaseOrder> poListWhereGrnNotDone = new ArrayList<PurchaseOrder>();
		ArrayList<PurchaseItem> purchaseItemWhereDlDateNotEmpty = new ArrayList<PurchaseItem>();
		ArrayList<PurchaseItem> purchaseItemWhereLrNoEmpty = new ArrayList<PurchaseItem>();
		List<PurchaseOrder> puchaseOrderList = findAllPO();
		for (PurchaseOrder purchaseOrder : puchaseOrderList) {
			List<Grn> grnList = grnService.findGrnByPoNumber(purchaseOrder.getPoNumber());
			if(grnList.size()==0) {
				poListWhereGrnNotDone.add(purchaseOrder);
			}
		}
		for (PurchaseOrder po : poListWhereGrnNotDone) {
			List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemList(po.getPoNumber());
			for (PurchaseItem poItem : poItemList) {
				Date dlDate = poItem.getDelivaryDate();
				
				if(dlDate!=null) {
					long difference_In_Time=  dlDate.getTime()-todayDate.getTime();
					long difference_In_Days = (difference_In_Time/ (1000 * 60 * 60 * 24))% 365;
					if(difference_In_Days<3 && difference_In_Days>=0) {
					purchaseItemWhereDlDateNotEmpty.add(poItem);
					System.out.println(poItem.getPurchase_item_id());
					}
				}
			}
		}
		
		for (PurchaseItem purchaseItem : purchaseItemWhereDlDateNotEmpty) {
			if(purchaseItem.getLrNum().isEmpty() || purchaseItem.getLrNum().equals(null) || purchaseItem.getLrNum().equals("")) {
				purchaseItemWhereLrNoEmpty.add(purchaseItem);
			}
		}
		
		return purchaseItemWhereLrNoEmpty;
		
	}
	
	public List<PurchaseItem> getPurchaseItemsWhereDlDateForNextWeek(){
		Date todayDate = new Date();
		ArrayList<PurchaseOrder> poListWhereGrnNotDone = new ArrayList<PurchaseOrder>();
		ArrayList<PurchaseItem> purchaseItemWhereDlDateNotEmpty = new ArrayList<PurchaseItem>();
		List<PurchaseOrder> puchaseOrderList = findAllPO();
		for (PurchaseOrder purchaseOrder : puchaseOrderList) {
			List<Grn> grnList = grnService.findGrnByPoNumber(purchaseOrder.getPoNumber());
			if(grnList.size()==0) {
				poListWhereGrnNotDone.add(purchaseOrder);
			}
		}
		for (PurchaseOrder po : poListWhereGrnNotDone) {
			List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemList(po.getPoNumber());
			for (PurchaseItem poItem : poItemList) {
				Date dlDate = poItem.getDelivaryDate();
				
				if(dlDate!=null) {
					long difference_In_Time=  dlDate.getTime()-todayDate.getTime();
					long difference_In_Days = (difference_In_Time/ (1000 * 60 * 60 * 24))% 365;
					if(difference_In_Days<7 && difference_In_Days>=0) {
					purchaseItemWhereDlDateNotEmpty.add(poItem);
					System.out.println(poItem.getPurchase_item_id());
					}
				}
			}
		}
		
		return purchaseItemWhereDlDateNotEmpty;
		
	}
	
	public List<SalesItem> getSalesItemsWhereDelDateInNextWeek(){
		Date todayDate = new Date();
		ArrayList<SalesItem> itemList = new ArrayList<SalesItem>();
		List<SalesOrder> salesList = salesService.getAllSalesOrderList();
		for (SalesOrder salesOrder : salesList) {
			List<SalesItem> salesItemsList = salesService.getSalesItemsBySalesOrderId(salesOrder.getId());
			for (SalesItem salesItem : salesItemsList) {
				List<DesignItems> designItems = designService.getDesignItemListBySOItemId(salesItem.getId());
				for (DesignItems designItem : designItems) {
					List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(salesItem.getId(), designItem.getItemId());
					for (PurchaseItem poItem : purchaseItemList) {
						List<GrnItems> grnItems = grnService.getGrnItemObjByPoItemId(Integer.toString(poItem.getPurchase_item_id()));
						if(grnItems.size()==0){
							Date dlDate = poItem.getDelivaryDate();
							if(dlDate!=null) {
								long difference_In_Time=  dlDate.getTime()-todayDate.getTime();
								long difference_In_Days = (difference_In_Time/ (1000 * 60 * 60 * 24))% 365;
								if(difference_In_Days<7 && difference_In_Days>=0) {
									itemList.add(salesItem);
								System.out.println(salesItem.getId());
								}
							}
						}
					}
				}
			
			}
		}
		
		
		return itemList;
		
	}

	public List<PurchaseOrder> getPenidngPoListPartial() {
		long startTime = System.currentTimeMillis();
		List<PurchaseOrder> pendingPoList = new ArrayList();
		List<PurchaseOrder> puchaseOrderList = findAllPO();
		Collections.sort(puchaseOrderList);
		int count=0;
		for (int i = puchaseOrderList.size()-1; i >0; i--) {
			if(count<10) {
			List<Grn> grnObj = grnService.findGrnByPoNumber(puchaseOrderList.get(i).getPoNumber());
			if (grnObj.size() == 0) {
				pendingPoList.add(puchaseOrderList.get(i));
				count++;
			} else {
				List<PurchaseItem> poItems = puchaseOrderList.get(i).getItems();
				for (PurchaseItem purchaseItemObj : poItems) {
					float grnReceivedQty = 0;
					List<GrnItems> grnItemsList = grnService
							.getGrnItemByPoItemId(Integer.toString(purchaseItemObj.getPurchase_item_id()));
					for (GrnItems grnItem : grnItemsList) {
						grnReceivedQty = grnReceivedQty + grnItem.getReceivedQuantity();
						}
                      if(grnReceivedQty!= purchaseItemObj.getQuantity()) {
                    	  pendingPoList.add(puchaseOrderList.get(i));
                    	  count++;
                    	  break;
                      }
				}
			}
			}

		}
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	    System.out.println("time to loop each item of purchase list"+startTime+"&"+ stopTime);
		return pendingPoList;

	}

	public List<PurchaseOrder> findAllArchivedPOList() {
		List<PurchaseOrder> poList = purchaseRepo.findAllArchivedPOList();
		Map<String, Object[]> totalsByPo = new HashMap<>();
		for (Object[] row : purchaseItemRepo.getArchivedPoTotals()) {
			if (row == null || row.length < 3 || row[0] == null) {
				continue;
			}
			totalsByPo.put(row[0].toString(), row);
		}
		for (PurchaseOrder po : poList) {
			Object[] totals = totalsByPo.get(po.getPoNumber());
			double total = 0;
			double gstTotal = 0;
			if (totals != null) {
				if (totals[1] instanceof Number) {
					total = ((Number) totals[1]).doubleValue();
				}
				if (totals[2] instanceof Number) {
					gstTotal = ((Number) totals[2]).doubleValue();
				}
			}
			double grandTotal = total + gstTotal;
			grandTotal = Math.round(grandTotal * 100.0) / 100.0;
			po.setGrandTotal(grandTotal);
		}
		return poList;
	}
	
	public void archivePO(String poNum) {
		Optional<PurchaseOrder> po = purchaseRepo.findById(poNum);
		po.get().setArchive(true);
		purchaseRepo.save(po.get());
		
	}
	
	public void unArchivePO(String poNum) {
		Optional<PurchaseOrder> po = purchaseRepo.findById(poNum);
		po.get().setArchive(false);
		purchaseRepo.save(po.get());
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	public List<Object> getPendingPoListByPartyName(String name) {

		List<Object> pendingPoList = new ArrayList();
		List<PurchaseOrder> purchaseOrderList =getPurchaseOrderListByPartyId(name);
		for (int i = 0; i < purchaseOrderList.size(); i++) {
			List<PurchaseItem> poItemList = purchaseOrderList.get(i).getItems();
				for (PurchaseItem purchaseItem : poItemList) {
					Optional<ItemMaster> itemObj = itemMasterService.getItemById(purchaseItem.getModelNo());
					JSONObject object = new JSONObject();
					String units = itemObj.get().getItem_units().getName();
					object.put("model", itemObj.get().getModel());
					object.put("itemName", itemObj.get().getItemName());
					object.put("hsnCode", itemObj.get().getHsnCode());
					object.put("units", itemObj.get().getItem_units().getName());
					object.put("Date",purchaseOrderList.get(i).getCreated());
					object.put("Qty", purchaseItem.getQuantity());
					object.put("poNum", purchaseItem.getPurchaseOrder().getPoNumber());
					object.put("unitPrice", purchaseItem.getUnitPrice());
					
					pendingPoList.add(object);
				}
		}
		return pendingPoList;
	
	
	
}

	
}
