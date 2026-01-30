package com.ncpl.sales.service;

import java.io.File;
						   
import java.sql.Timestamp;
							   
							  
								  
import java.util.ArrayList;
					  
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
						
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ncpl.common.Constants;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
										  
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.DeliveryChallanRepo;
import com.ncpl.sales.repository.InvoiceRepo;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesOrderDesignItemsRepo;
import com.ncpl.sales.repository.SalesRepo;
import com.ncpl.sales.repository.StockRepo;
import com.ncpl.sales.security.UserService;

@Service
public class DeliveryChallanService {
	@Autowired
	DeliveryChallanRepo dcRepo;
	@Autowired
	SalesService salesService;
	@Autowired
	PartyRepo partyRepo;
	@Autowired
	PartyAddressService partyAddressService;
	@Autowired
	DeliveryChallanItemsRepo dcItemRepo;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	StockService stockService;
	@Autowired
	SalesOrderDesignService designService;
	@Autowired
	StockRepo stockRepo;
	@Autowired
	ItemMasterService itemService;
	@Autowired
	SalesRepo salesRepo;
	@Autowired
	SalesOrderDesignItemsRepo designItemRepo;
	@Autowired
	InvoiceRepo invRepo;
	@Autowired
	EmailService emailService;
	@Autowired
	UserService userService;
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "dc_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	//code to dave delivery challan
	@SuppressWarnings("unchecked")
	public DeliveryChallan saveDc(DeliveryChallan deliveryChallan, String[] designArray, String soNumber) throws Exception {
		
		String className="dc";
		Optional<SalesOrder> salesOrderObj = salesRepo.findById(soNumber);
		String clientId=salesOrderObj.get().getParty().getId();
		@SuppressWarnings("unused")
		Stock updatedStock = null;
		boolean errorFlag = false;
		List<DeliveryChallanItems> dcItemList = deliveryChallan.getItems();
		
		//List<PurchaseItem> poItemList = null;
		
				for (int k = 0; k < designArray.length; k++) {
					if (!designArray[k].trim().startsWith("[")) {
						errorFlag=true;
						 throw new JSONException("Expected a JSON array but got: " + designArray[k]);
					}
				if(errorFlag==false){
					JSONArray array = new JSONArray(designArray[k]); 
					for (int i = 0; i <array.length(); i++) {
						JSONObject object = array.getJSONObject(i);  
						String model=(String) object.get("model");
						String itemId=(String) object.get("itemId");
						long designId=Long.parseLong((String) object.get("designId"));
						float deliveredQty =  Float.parseFloat((String) object.get("deliveredQty"));
						
						
						float todayQty;
						if(object.get("todayQty")=="") {
							todayQty=0;
						}else {
							todayQty =  Float.parseFloat((String) object.get("todayQty"));
						}
						
						DesignItems designItemsList = designItemRepo.findDesignItemObjByItemIdAndDesignId(itemId,designId);
						designItemsList.setDeliveredQty(deliveredQty+todayQty);
						Stock stock = stockRepo.findStockByClientAndItemId(itemId,clientId);
						if(stock!=null) {
							if(stock.getQuantity() == 0)
								continue;
						}
						updatedStock=stockService.updateStockQuantityFromGrn(itemId, clientId, todayQty, className, salesOrderObj.get());
						System.out.println(model);
						System.out.println(itemId);

						/*if(updatedStock.getQuantity() == 0)	
							deliveryChallan.getItems().remove(k);*/
					}
					}
				}

				for (DeliveryChallanItems deliveryChallanItems : dcItemList) {
					//get soItem id
					String soItemId=deliveryChallanItems.getDescription();
					SalesOrderDesign designObj = designService.findSalesOrderDesignObjBysalesItemId(soItemId);
					List<DesignItems> designList =designObj.getItems();
					if (designList.isEmpty()) {
						return null;
					}
					if(designList.size()==1) {
						String itemId=designList.get(0).getItemId();
						float todayQty=deliveryChallanItems.getTodaysQty();
						Stock stock = stockRepo.findStockByClientAndItemId(itemId,clientId);
						if(stock!=null) {
							if(stock.getQuantity() == 0)
								continue;
						}
						if(errorFlag==false) {
						updatedStock=stockService.updateStockQuantityFromGrn(itemId, clientId, todayQty, className, salesOrderObj.get());
						}
						//if(updatedStock.getQuantity() == 0)	
							//deliveryChallan.getItems().remove(deliveryChallanItems);
					}
				}
				DeliveryChallan dcObj = null;
			if(errorFlag==false) {
			dcObj = dcRepo.save(deliveryChallan);
			List<DeliveryChallanItems> dcItemsList=dcObj.getItems();
			Map<String,String> map = new HashMap<String,String>();
			for (DeliveryChallanItems deliveryChallanItems : dcItemsList) {
				String salesItemId=(String) deliveryChallanItems.getDescription();
				List<DesignItems> designItemsList =designService.getDesignItemListBySOItemId(salesItemId);
				ArrayList<String> list = new ArrayList<String>();
				for (DesignItems designItem : designItemsList) {
					String itemId=designItem.getItemId();
					Optional<ItemMaster> itemMaster= itemService.getItemById(itemId);
					String modelNumber=itemMaster.get().getModel();
					list.add(modelNumber);
				}
				map.put(Integer.toString(deliveryChallanItems.getDcItemId()), list.toString());
			}
			
			DcExcel.buildExcelDocument(dcObj,salesService,filePath,map);
			Map<String, Object> emailContents=dcDetails(salesOrderObj.get().getClientPoNumber(), salesOrderObj.get().getParty().getPartyName(),dcObj.getDcId());
			
			emailService.sendEmailToServerForDCCreated(emailContents);
			}
			return dcObj;
		
		
	}
	private Map<String, Object> dcDetails(String clientPoNo,
			String partyName,int dcId) {
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject", "DC created with dc No.: " + dcId);
		emailContents.put("template", "dc-created.html");
		emailContents.put("content", "DeliveryChallan Created");
		emailContents.put("to1", "billing@ncpl.co");
		//emailContents.put("cc2", "anitha@tek-nika.com");
		emailContents.put("cc1", "surendra@ncpl.co");
	
		emailContents.put("month", Constants.currentDate());
		emailContents.put("clientPo", clientPoNo);
		emailContents.put("partyName", partyName);
		emailContents.put("dcId", dcId);
		emailContents.put("createdby", userService.getCurrentUser().getName());
		emailContents.put("attachment", filePath); 
		return emailContents;
	}

	public List<DeliveryChallan> getAllDcList(){
		List<DeliveryChallan> dcList = dcRepo.findAllDc();
		return dcList;
	}
	
	//code to get list of delivery challan
	public List<DeliveryChallan> getDeliveryChallanLists() {

		List<DeliveryChallan> dcLists = getAllDcList();
		for (DeliveryChallan dc : dcLists) {
			String soNumber=dc.getSoNumber();
										   
			Optional<SalesOrder> soObj=salesService.getSalesOrderById(soNumber);
			String shippingAddrId=soObj.get().getShippingAddress();
													 
			if(shippingAddrId!=null) {
				if(!shippingAddrId.isEmpty()) {
				//if the shipping address id is party id then get the party object by id else get party address obj by id
				Party partyObj =partyRepo.findById(shippingAddrId);
				if(partyObj!=null) {
				dc.set("shippingAddress",partyObj.getAddr1());
				}else {
					System.out.println(shippingAddrId);
					Optional<PartyAddress> partyAddressobj =partyAddressService.getAddressByAddressId(shippingAddrId);
					dc.set("shippingAddress",partyAddressobj.get().getAddr1());
				}
				}
			}
			dc.set("clientPoNumber",soObj.get().getClientPoNumber());
			dc.set("clientName",soObj.get().getParty().getPartyName());
			dc.set("createdDate",dc.getItems().get(0).getCreated());
			
		}
		
		return dcLists;
	}
	//get all the dc list
	public List<DeliveryChallan> getDcListBySoId(String soId) {
		List<DeliveryChallan> dcList=dcRepo.getAllDcBySoId(soId);
		@SuppressWarnings("unused")
		List<DeliveryChallan> dcListWhereInvNotGenerated=dcRepo.getAllDcBySoId(soId);
		for (DeliveryChallan dc : dcList) {
			String soNumber=dc.getSoNumber();
			Optional<SalesOrder> soObj=salesService.getSalesOrderById(soNumber);
			//dc.set("clientPoNumber",soObj.get().getClientPoNumber());
			
			String clientPo = soObj.get().getClientPoNumber();
			clientPo = clientPo.replace("'", "&");
			clientPo = clientPo.replace("\"", "&");
			dc.set("clientPoNumber",clientPo);
			String partyName = soObj.get().getParty().getPartyName();
			partyName = partyName.replace("\"", "&");
			partyName = partyName.replace("'", "&");
			dc.set("clientName",partyName);
		}
		return dcList;
	}
	//get list of delivery challan items by dc id
	public List<DeliveryChallanItems> getDcItemList(int dcId) {
		List<DeliveryChallan> dcList = dcRepo.findDcListById(dcId);
		ArrayList<DeliveryChallanItems> itemList = new ArrayList<DeliveryChallanItems>();
		for (DeliveryChallan dc : dcList) {
			List<DeliveryChallanItems> dcItemList=dc.getItems();
			for (DeliveryChallanItems deliveryChallanItems : dcItemList) {
				String soItemId=deliveryChallanItems.getDescription();
				//Optional<SalesItem> salesItem =salesService.getSalesItemById(soItemId);
				boolean value =false;
				Optional<SalesItem> salesItem =salesService.getSalesItemById(soItemId,value);
				deliveryChallanItems.setDescription(salesItem.get().getDescription());
				deliveryChallanItems.set("hsnCode",salesItem.get().getHsnCode());
				deliveryChallanItems.set("supplyPrice",salesItem.get().getUnitPrice());
				deliveryChallanItems.set("servicePrice",salesItem.get().getServicePrice());
				deliveryChallanItems.set("sac",salesItem.get().getServicehsnCode());
				deliveryChallanItems.set("amount",salesItem.get().getAmount());
				//deliveryChallanItems.set("unit",salesItem.get().getUnit());
				deliveryChallanItems.set("unit",salesItem.get().getItem_units().getName());
				deliveryChallanItems.set("clientId",salesItem.get().getSalesOrder().getParty().getId());
				deliveryChallanItems.set("dcNum",dc.getDcId());
				deliveryChallanItems.set("salesItemIdHidden",salesItem.get().getId());
				deliveryChallanItems.set("particulars",salesItem.get().getDescription());
				deliveryChallanItems.set("serialNo",salesItem.get().getSlNo());
			}
			itemList.addAll(dcItemList);
		}
		return itemList;
	}
	
	public List<DeliveryChallanItems> getDcItemListForNonZeroQty(int dcId) {
		List<DeliveryChallan> dcList = dcRepo.findDcListById(dcId);
		ArrayList<DeliveryChallanItems> itemList = new ArrayList<DeliveryChallanItems>();
		for (DeliveryChallan dc : dcList) {
			List<DeliveryChallanItems> dcItemList=dc.getItems();
			for (DeliveryChallanItems deliveryChallanItems : dcItemList) {
				if(deliveryChallanItems.getTodaysQty()>0) {
				String soItemId=deliveryChallanItems.getDescription();
				//Optional<SalesItem> salesItem =salesService.getSalesItemById(soItemId);
				boolean value =false;
				Optional<SalesItem> salesItem =salesService.getSalesItemById(soItemId,value);
				deliveryChallanItems.setDescription(salesItem.get().getDescription());
				deliveryChallanItems.set("hsnCode",salesItem.get().getHsnCode());
				deliveryChallanItems.set("supplyPrice",salesItem.get().getUnitPrice());
				deliveryChallanItems.set("servicePrice",salesItem.get().getServicePrice());
				deliveryChallanItems.set("sac",salesItem.get().getServicehsnCode());
				deliveryChallanItems.set("amount",salesItem.get().getAmount());
				//deliveryChallanItems.set("unit",salesItem.get().getUnit());
				deliveryChallanItems.set("unit",salesItem.get().getItem_units().getName());
				deliveryChallanItems.set("clientId",salesItem.get().getSalesOrder().getParty().getId());
				deliveryChallanItems.set("dcNum",dc.getDcId());
				deliveryChallanItems.set("salesItemIdHidden",salesItem.get().getId());
				deliveryChallanItems.set("particulars",salesItem.get().getDescription());
				deliveryChallanItems.set("serialNo",salesItem.get().getSlNo());
				itemList.add(deliveryChallanItems);
				}
			}
			
		}
		return itemList;
	}
	
	public List<DeliveryChallanItems> getDcItemListBySoItemId(String soItemId){
		List<DeliveryChallanItems> dcItemList = dcItemRepo.getDcItemListBySalesItemId(soItemId);
		return dcItemList;
	}
	
	public List<DeliveryChallanItems> getDcItemListBySoItemIdWhereDcQtyNotZero(String soItemId){
		List<DeliveryChallanItems> dcItemList = dcItemRepo.getDcItemListBySOItemIdWhereDcQtyNonZero(soItemId);
		return dcItemList;
	}
	
	//get dc object by dc id
	public Optional<DeliveryChallan> getDcById(int dcId) {
		 Optional<DeliveryChallan> dcObj=dcRepo.findById(dcId);
		 String soNumber=dcObj.get().getSoNumber();
		 Optional<SalesOrder> soObj=salesService.getSalesOrderById(soNumber);
		 
		 dcObj.get().set("clientPoNumber",soObj.get().getClientPoNumber());
		 dcObj.get().set("clientName",soObj.get().getParty().getPartyName()); 
		return dcObj;
	}
	
	public Optional<DeliveryChallanItems> getDcItemByDcItemId(int dcItemId){
		Optional<DeliveryChallanItems> dcItem = dcItemRepo.findById(dcItemId);
		return dcItem;
	}
	
	//Get dcItemsList by Date
	public List<DeliveryChallanItems> findDcListByDate(Timestamp sqlFromDate, Timestamp sqlToDate, String grnSoItemId) {
		List<DeliveryChallanItems> dcItems = dcItemRepo.findByDate(sqlFromDate, sqlToDate,grnSoItemId);
		//List<DeliveryChallanItems> dcItems = dcItemRepo.findByDateFrom(sqlFromDate,grnSoItemId);
		return dcItems;
	}
	/*
	 * public Map<String, Object> checkForStockAvailable(String salesItemId, String
	 * clientId) { List<SalesOrderDesign>
	 * soDesignList=designService.findSalesOrderDesignBysalesItemId(salesItemId);
	 * Map<String, Object> mapObj = new HashMap<>(); ArrayList<Stock> list = new
	 * ArrayList<Stock>(); for (SalesOrderDesign design : soDesignList) {
	 * List<DesignItems> designItemList=design.getItems(); for (DesignItems
	 * designItem : designItemList) { String itemId=designItem.getItemId();
	 * ItemMaster itemObj = (ItemMaster) itemService.getItemByModelNo(itemId); float
	 * designQty=designItem.getQuantity(); float remainingQtyNeeded=0; List<Stock>
	 * stockList=stockRepo.findStockListByClientName(itemObj.getId(), clientId);
	 * float stockQuantity=0; for (Stock stock : stockList) {
	 * stockQuantity=stockQuantity+stock.getQuantity();
	 * 
	 * }
	 * 
	 * if(designQty<=stockQuantity) { list.addAll(stockList); }
	 * 
	 * float stockqty=0; remainingQtyNeeded=designQty-stockQuantity;
	 * if(stockList.isEmpty() || designQty>stockQuantity) { List<Stock>
	 * stocks=stockService.getStockList(itemObj.getId()); if(stocks.isEmpty()) {
	 * return null; }else { for (Stock stock : stocks) {
	 * if(stock.getParty().getId()!=clientId) {
	 * stockqty=stockqty+stock.getQuantity();
	 * 
	 * } } if(remainingQtyNeeded>stockqty) { return null ; } list.addAll(stocks);
	 * mapObj.put("stockError", "stockError"); } }
	 * 
	 * 
	 * }
	 * 
	 * } mapObj.put("stockList", list); return mapObj; }
	 */
	
	//Checking for stock Available with the client..
	@SuppressWarnings("unused")
	public Map<String, Object> checkForStockAvailable(String salesItemId, String clientId) {
		// List<SalesOrderDesign>
		// soDesignList=designService.findSalesOrderDesignBysalesItemId(salesItemId);
		SalesOrderDesign soDesign = designService.findSalesOrderDesignObjBysalesItemId(salesItemId);
		Map<String, Object> mapObj = new HashMap<>();
		if(soDesign==null) {
			mapObj.put("mapDesign", "mapDesign");
			return mapObj;
		}
		
		ArrayList<Stock> list = new ArrayList<Stock>();

		List<DesignItems> designItemList = soDesign.getItems();
		if(designItemList.isEmpty()) {
			mapObj.put("mapDesign", "mapDesign");
			return mapObj;
		}
		for (DesignItems designItem : designItemList) {
			String itemId = designItem.getItemId();
			// ItemMaster itemObj = (ItemMaster) itemService.getItemByModelNo(itemId);
			float designQty = designItem.getQuantity();
			float deliveredQty=designItem.getDeliveredQty();
			float quantityNeeded=designQty-deliveredQty;
			float remainingQtyNeeded = 0;
			List<Stock> stockList = stockRepo.findStockListByClientName(itemId, clientId);
			float stockQuantity = 0;
			for (Stock stock : stockList) {
				stockQuantity = stockQuantity + stock.getQuantity();

			}

			if (stockQuantity>0) {
				list.addAll(stockList);
			}

			float stockqty = 0;
			remainingQtyNeeded = quantityNeeded - stockQuantity;
			if (stockList.isEmpty() || stockQuantity<=0) {
				List<Stock> stocks = stockService.getStockList(itemId);
				if (stocks.isEmpty()) {
					return null;
				} else {
					for (Stock stock : stocks) {
						if (stock.getParty().getId() != clientId) {
							stockqty = stockqty + stock.getQuantity();

						}
					}
					if (stockqty>0) {
					list.addAll(stocks);
					}
					mapObj.put("stockError", "stockError");
				}
			}

		}

		mapObj.put("stockList", list);
		return mapObj;
	}
	
	//Checking invoice generated or not if generated dont add that dc items..
	public List<DeliveryChallanItems> getDcItemListsForAllDc(String soId) {
		List<DeliveryChallan> dcList = getDcListBySoId(soId);
		ArrayList<DeliveryChallanItems> dcItemList = new ArrayList<DeliveryChallanItems>();
		List<Invoice> invListBySoId = invRepo.findInvoiceBySoIdWhereDcNoIsAll(soId, "All");
		if (invListBySoId.isEmpty()) {
			for (DeliveryChallan deliveryChallan : dcList) {
				List<DeliveryChallanItems> dcItems = getDcItemListForNonZeroQty(deliveryChallan.getDcId());
				List<Invoice> invList = invRepo.getItemByDcNo(Integer.toString(deliveryChallan.getDcId()));
				if (invList.size() > 0) {

				} else {
					dcItemList.addAll(dcItems);
				}
			}

		}
		return dcItemList;
	}
	
	//Checking the condition whether invoice generated if generated dont add the list of dc..
	public List<DeliveryChallan> getDcListBySoIdWhereInvGenerated(String soId) {
		List<DeliveryChallan> dcList = dcRepo.getAllDcBySoId(soId);
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<DeliveryChallan> dcListWhereInvNotGenerated = new ArrayList();
		List<Invoice> invListBySoId = invRepo.findInvoiceBySoIdWhereDcNoIsAll(soId, "All");
		if (invListBySoId.isEmpty()) {
			for (DeliveryChallan dc : dcList) {
				List<Invoice> invList = invRepo.getItemByDcNo(Integer.toString(dc.getDcId()));
				if (invList.size() > 0) {

				} else {
					String soNumber = dc.getSoNumber();
					Optional<SalesOrder> soObj = salesService.getSalesOrderById(soNumber);
					dc.set("clientPoNumber", soObj.get().getClientPoNumber());
					dc.set("clientName", soObj.get().getParty().getPartyName());
					dcListWhereInvNotGenerated.add(dc);
				}

			}
		}
		return dcListWhereInvNotGenerated;
	}
	public List<DeliveryChallanItems> getDcItemListsForAllDcBySoId(String soId) {
		List<DeliveryChallan> dcList = getDcListBySoId(soId);
		ArrayList<DeliveryChallanItems> dcItemList = new ArrayList<DeliveryChallanItems>();
			for (DeliveryChallan deliveryChallan : dcList) {
				List<DeliveryChallanItems> dcItems = getDcItemListForNonZeroQty(deliveryChallan.getDcId());
					dcItemList.addAll(dcItems);
			}

		return dcItemList;
	}
	public List<DeliveryChallanItems> getAllDcItemList(int dcId) {
		List<DeliveryChallan> dcList = dcRepo.findDcListById(dcId);
		ArrayList<DeliveryChallanItems> itemList = new ArrayList<DeliveryChallanItems>();
		for (DeliveryChallan dc : dcList) {
			List<DeliveryChallanItems> dcItemList=dc.getItems();
			
			itemList.addAll(dcItemList);
		}
		return itemList;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<DeliveryChallan> getDcListBysalesItemId(String itemId){
		List<DesignItems> designItemList = designService.getDesignItemListByItemId(itemId);
		Set set = new HashSet();
		if(designItemList.size()>0) {
		ArrayList<SalesOrderDesign> designList = new ArrayList<SalesOrderDesign>();
		ArrayList<DeliveryChallanItems> deliveryChallanItemList = new ArrayList<DeliveryChallanItems>();
		System.out.println(designItemList.size());
		for (DesignItems designItem : designItemList) {
			long designId = designItem.getSalesOrderDesign().getId();
			System.out.println("design ID="+designId);
			Optional<SalesOrderDesign> soDesign = designService.findSalesOrderDesignById(designId);
			String salesItemId = soDesign.get().getSalesItemId();
			boolean value = false;
			Optional<SalesItem> soItem =salesService.getSalesItemById(salesItemId, value);
			List<DeliveryChallanItems> dcItemList = dcItemRepo.getDcItemListBySOItemIdWhereDcQtyNonZero(soItem.get().getId());
			for (DeliveryChallanItems dcItem : dcItemList) {
				if(dcItem.getTodaysQty()>0) {
				deliveryChallanItemList.add(dcItem);
				System.out.println(dcItem.getDcItemId());
				}
			}
			
		}
		
			/*
			 * for (SalesOrderDesign salesOrderDesign : designList) { String salesItemId =
			 * salesOrderDesign.getSalesItemId(); boolean value = false; Optional<SalesItem>
			 * soItem =salesService.getSalesItemById(salesItemId, value);
			 * List<DeliveryChallanItems> dcItemList =
			 * dcItemRepo.getDcItemListBySOItemIdWhereDcQtyNonZero(soItem.get().getId());
			 * for (DeliveryChallanItems dcItem : dcItemList) { if(dcItem.getTodaysQty()>0
			 * || dcItem.getDeliveredQuantity()>0) { deliveryChallanItemList.add(dcItem);
			 * System.out.println(dcItem.getDcItemId()); } }
			 * 
			 * }
			 */
		for (DeliveryChallanItems deliveryChallanItems : deliveryChallanItemList) {
			System.out.println(deliveryChallanItems.getDescription()+"dcId : "+deliveryChallanItems.getDeliveryChallan().getDcId());
			int dcId=deliveryChallanItems.getDeliveryChallan().getDcId();
			Optional<DeliveryChallan> deliveryChallan = dcRepo.findById(dcId);
			String salesOrderId = deliveryChallan.get().getSoNumber();
			Optional<SalesOrder> soObj=salesService.getSalesOrderById(salesOrderId);
			String shippingAddrId=soObj.get().getShippingAddress();
			if(shippingAddrId!=null) {
				if(!shippingAddrId.isEmpty()) {
				//if the shipping address id is party id then get the party object by id else get party address obj by id
				Party partyObj =partyRepo.findById(shippingAddrId);
				if(partyObj!=null) {
					deliveryChallan.get().set("shippingAddress",partyObj.getAddr1());
				}else {
					Optional<PartyAddress> partyAddressobj =partyAddressService.getAddressByAddressId(shippingAddrId);
					deliveryChallan.get().set("shippingAddress",partyAddressobj.get().getAddr1());
				}
				}
			}
			deliveryChallan.get().set("clientPoNumber",soObj.get().getClientPoNumber());
			deliveryChallan.get().set("clientName",soObj.get().getParty().getPartyName());
			set.add(deliveryChallan.get());
		}
		
		}
		ArrayList<DeliveryChallan> dc = new ArrayList<DeliveryChallan>(set);
		
		return dc;
	}

	public List<DeliveryChallan> getDeliveryChallanListsArchived() {
		List<DeliveryChallan> dcList = dcRepo.findDcListArchived();
		for (DeliveryChallan dc : dcList) {
			String soNumber=dc.getSoNumber();
			Optional<SalesOrder> soObj=salesService.getSalesOrderById(soNumber);
			String shippingAddrId=soObj.get().getShippingAddress();
			if(shippingAddrId!=null) {
				if(!shippingAddrId.isEmpty()) {
				//if the shipping address id is party id then get the party object by id else get party address obj by id
				Party partyObj =partyRepo.findById(shippingAddrId);
				if(partyObj!=null) {
				dc.set("shippingAddress",partyObj.getAddr1());
				}else {
					Optional<PartyAddress> partyAddressobj =partyAddressService.getAddressByAddressId(shippingAddrId);
					dc.set("shippingAddress",partyAddressobj.get().getAddr1());
				}
				}
			}
			dc.set("clientPoNumber",soObj.get().getClientPoNumber());
			dc.set("clientName",soObj.get().getParty().getPartyName());
			dc.set("createdDate",dc.getItems().get(0).getCreated());
			
		}
		return dcList;
	}

	public void archiveDC(int dcNum) {
		Optional<DeliveryChallan> dc = dcRepo.findById(dcNum);
		dc.get().setArchive(true);
		dcRepo.save(dc.get());
		
	}
	
	public void unArchiveDC(int dcNum) {
		Optional<DeliveryChallan> dc = dcRepo.findById(dcNum);
		dc.get().setArchive(false);
		dcRepo.save(dc.get());
		
	}
	
	public List<DeliveryChallanItems> getItemsNotDelivered(int dcId){
		Optional<DeliveryChallan> dcObj=dcRepo.findById(dcId);
		ArrayList<DeliveryChallanItems> dcItemLists=new ArrayList<DeliveryChallanItems>();
		List<DeliveryChallanItems> dcItemList=dcObj.get().getItems();
		for (DeliveryChallanItems deliveryChallanItems : dcItemList) {
			float totalQty=deliveryChallanItems.getTotalQuantity();
			float todaysQty=deliveryChallanItems.getTodaysQty();
			float deliveredQty=deliveryChallanItems.getDeliveredQuantity();
			float totalDeliveredQty=deliveredQty+todaysQty;
			
			String soItemId=deliveryChallanItems.getDescription();
			//Optional<SalesItem> salesItem =salesService.getSalesItemById(soItemId);
			boolean value =false;
			Optional<SalesItem> salesItem =salesService.getSalesItemById(soItemId,value);
			deliveryChallanItems.setDescription(salesItem.get().getDescription());
			deliveryChallanItems.set("hsnCode",salesItem.get().getHsnCode());
			deliveryChallanItems.set("supplyPrice",salesItem.get().getUnitPrice());
			deliveryChallanItems.set("servicePrice",salesItem.get().getServicePrice());
			deliveryChallanItems.set("sac",salesItem.get().getServicehsnCode());
			deliveryChallanItems.set("amount",salesItem.get().getAmount());
			//deliveryChallanItems.set("unit",salesItem.get().getUnit());
			deliveryChallanItems.set("unit",salesItem.get().getItem_units().getName());
			deliveryChallanItems.set("clientId",salesItem.get().getSalesOrder().getParty().getId());
			deliveryChallanItems.set("dcNum",dcObj.get().getDcId());
			deliveryChallanItems.set("salesItemIdHidden",salesItem.get().getId());
			deliveryChallanItems.set("particulars",salesItem.get().getDescription());
			deliveryChallanItems.set("serialNo",salesItem.get().getSlNo());
			if(totalDeliveredQty<totalQty) {
				dcItemLists.add(deliveryChallanItems);
			}
		}
		return dcItemLists;
		
	}
	
	public List<DeliveryChallanItems> getPartialDcItems(){
		List<DeliveryChallan> dcList=getAllDcList();
		ArrayList<DeliveryChallanItems> dcItemLists=new ArrayList<DeliveryChallanItems>();
		for (DeliveryChallan dc : dcList) {
			List<DeliveryChallanItems> dcItemList=dc.getItems();
			for (DeliveryChallanItems deliveryChallanItems : dcItemList) {
				System.out.println("not found"+deliveryChallanItems.getDescription());
				Optional<SalesItem> salesItem=salesService.getSalesItemObjById(deliveryChallanItems.getDescription());
				if(salesItem!=null) {
				System.out.println(salesItem.get().getId());
					List<DesignItems> designItemsList= designService.getAllDesignItemListBySOItemId(salesItem.get().getId());
					for (DesignItems designItem : designItemsList) {
						if(designItem.getDeliveredQty()!=0) {
							if(designItem.getDeliveredQty()<designItem.getQuantity()) {
								dcItemLists.add(deliveryChallanItems);
							
								break;
							}
						}
					}
				}
			}
		}
		System.out.println("size:" + dcItemLists.size());
		return dcItemLists;
		
	}
	
	// Pagination methods for lazy loading
	public Page<DeliveryChallan> getDeliveryChallanPage(Pageable pageable, String keyword) {
		Page<DeliveryChallan> dcPage;
		
		if (keyword != null && !keyword.trim().isEmpty()) {
			dcPage = dcRepo.searchByKeyword(keyword, pageable);
		} else {
			dcPage = dcRepo.findAllActive(pageable);
		}
		
		// Enrich the data with additional fields
		for (DeliveryChallan dc : dcPage.getContent()) {
			enrichDeliveryChallanData(dc);
		}
		
		return dcPage;
	}
	
	public Page<DeliveryChallan> getDeliveryChallanPageAdvanced(Pageable pageable, String dcId, String soNumber, 
			String clientName, String clientPo, String shipping) {
		
		Integer dcIdInt = null;
		if (dcId != null && !dcId.trim().isEmpty()) {
			try {
				dcIdInt = Integer.parseInt(dcId);
			} catch (NumberFormatException e) {
				// Invalid dcId, will be ignored
			}
		}
		
		Page<DeliveryChallan> dcPage = dcRepo.searchAdvanced(
			dcIdInt, 
			soNumber, 
			clientName, 
			clientPo, 
			shipping, 
			pageable
		);
		
		// Enrich the data with additional fields
		for (DeliveryChallan dc : dcPage.getContent()) {
			enrichDeliveryChallanData(dc);
		}
		
		return dcPage;
	}
	
	private void enrichDeliveryChallanData(DeliveryChallan dc) {
		try {
			String soNumber = dc.getSoNumber();
			Optional<SalesOrder> soObj = salesService.getSalesOrderById(soNumber);
			if (soObj.isPresent()) {
				SalesOrder so = soObj.get();
				
				// Set client PO number
				dc.set("clientPoNumber", so.getClientPoNumber());
				
				// Set client name
				dc.set("clientName", so.getParty().getPartyName());
				
				// Set shipping address
				String shippingAddrId = so.getShippingAddress();
				if (shippingAddrId != null && !shippingAddrId.isEmpty()) {
					Party partyObj = partyRepo.findById(shippingAddrId);
					if (partyObj != null) {
						dc.set("shippingAddress", partyObj.getAddr1());
					} else {
						Optional<PartyAddress> partyAddressObj = partyAddressService.getAddressByAddressId(shippingAddrId);
						if (partyAddressObj.isPresent()) {
							dc.set("shippingAddress", partyAddressObj.get().getAddr1());
						}
					}
				}
				
				// Set created date from first item
				if (!dc.getItems().isEmpty()) {
					dc.set("createdDate", dc.getItems().get(0).getCreated());
				}
			}
		} catch (Exception e) {
			System.err.println("Error enriching DC data for DC ID: " + dc.getDcId() + " - " + e.getMessage());
		}
	}
	
	// Search method for the search endpoint
	public List<DeliveryChallan> searchDeliveryChallans(String keyword) {
		List<DeliveryChallan> dcList;
		
		if (keyword != null && !keyword.trim().isEmpty()) {
			dcList = dcRepo.searchByKeyword(keyword);
		} else {
			dcList = dcRepo.findAllDc();
		}
		
		// Enrich the data with additional fields
		for (DeliveryChallan dc : dcList) {
			enrichDeliveryChallanData(dc);
		}
		
		return dcList;
	}
	
	
}
