package com.ncpl.sales.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpSession;

import javax.persistence.criteria.CriteriaQuery;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ncpl.common.Constants;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.FileEntity;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.ItemsWithMinQty;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.model.Stages;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.model.Supplier;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.DeliveryChallanRepo;
import com.ncpl.sales.repository.FileRepo;
import com.ncpl.sales.repository.GrnItemRepo;
import com.ncpl.sales.repository.ItemsWithMinQtyRepo;
import com.ncpl.sales.repository.PurchaseItemRepo;
import com.ncpl.sales.repository.PurchaseRepo;
import com.ncpl.sales.repository.SalesItemRepo;
import com.ncpl.sales.repository.SalesOrderDesignItemsRepo;
import com.ncpl.sales.repository.SalesRepo;
import com.ncpl.sales.util.DateConverterUtil;

@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
@Service
public class SalesService {

	@Autowired
	private SalesRepo salesrepo;

	@Autowired
	private SalesItemRepo salesItemrepo;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	CityService cityservice;
	@Autowired
	PartyService partyService;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	DeliveryChallanService dcService;
	@Autowired
	ItemMasterService itemService;
	@Autowired
	GrnService grnService;
	@Autowired
	StockService stockService;
	@Autowired
	DateConverterUtil convertDate;
	@Autowired
	GrnItemRepo grnItemRepo;
	@Autowired
	SalesOrderDesignService soDesignService;
	@Autowired
	PurchaseRepo purchaseRepo;
	@Autowired
	EmailService emailService;
	@Autowired
	PurchaseItemRepo poItemRepo;
	@Autowired
	DeliveryChallanItemsRepo dcItemRepo;
	@Autowired
	SalesOrderDesignItemsRepo designItemRepo;
	@Autowired
	DeliveryChallanRepo dcRepo;
	@Autowired
	ItemsWithMinQtyRepo itemsWithMinQtyRepo;
	@Autowired
	FileRepo fileRepository;
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "sales_list_.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;

	public List<SalesOrder> getSalesOrderList() {
		boolean archive;
		List<SalesOrder> salesOrderList = salesrepo.findAllSalesOrder();
		return salesOrderList;

	}

	public List<SalesOrder> findAllSalesOrderList() {
		boolean archive;
		List<SalesOrder> salesOrderList = salesrepo.findAll();
		return salesOrderList;

	}

	public List<SalesOrder> getSalesListByPartyId(String partyId) {
		List<SalesOrder> salesOrderList = salesrepo.getSalesListByPartyId(partyId);
		return salesOrderList;
	}

	public List<SalesOrder> getSalesListByParty(String partyId) {
		List<SalesOrder> salesOrderList = salesrepo.getSalesListByParty(partyId);
		return salesOrderList;
	}

	// Saving SalesOrder to a repository
	public SalesOrder savesales(SalesOrder salesorder, String partyId) throws Exception {
		SalesOrder soObj;
		// Setting the total Items or quantity Size
		Party party = partyService.getPartyById(partyId);
		salesorder.setParty(party);
		salesorder.setClientPoNumber(salesorder.getClientPoNumber().trim());
		salesorder.setOtherTermsAndConditions(salesorder.getOtherTermsAndConditions().trim());
		ArrayList<SalesItem> itemList= new ArrayList<SalesItem>();
		if(salesorder.getItems()!=null) {
			salesorder.setTotalItems(salesorder.getItems().size());
			
			
			
			List<SalesItem> salesItemList = salesorder.getItems();
			for (SalesItem salesItem : salesItemList) {
				Long unitId = Long.parseLong(salesItem.getUnit());
				Optional<Units> unitObj = itemService.getUnitsById(unitId);
			    salesItem.setItem_units(unitObj.get());
				salesItem.setSalesOrder(salesorder);
			
				
			}
			
			
			itemList.addAll(salesItemList);
			
			salesorder.setItems(salesItemList);
		}else {
			salesorder.setItems(itemList);
		}

		if(salesorder.getId().isEmpty()) {
			soObj=salesrepo.save(salesorder);

			//Map<String, Object> emailContents = null;
			//SalesOrder salesOrderObj = salesrepo.getSalesOrderByClientPoNumber(salesorder.getClientPoNumber());
			//emailContents = salesorderDetails(salesorder.getClientPoNumber(), salesorder.getClientPoDate(),salesOrderObj.getGrandTotal(), party.getPartyName(),salesOrderObj.getGst());
			//emailService.sendSalesOrderEmailToServer(emailContents);
		} else {
			Optional<SalesOrder> updatedso = getSalesOrderById(salesorder.getId());
			Date createdDate = updatedso.get().getCreated();
			salesorder.setCreated(createdDate);
			// salesItemList.addAll(updatedso.get().getItems());
			soObj = salesrepo.save(salesorder);
			//SalesExcel.buildExcelDocument(soObj,filePath);
			//Map<String, Object> emailContents = null;
			//SalesOrder salesOrderObj = salesrepo.getSalesOrderByClientPoNumber(salesorder.getClientPoNumber());
			//emailContents = salesorderupdatedDetails(salesorder.getClientPoNumber(), salesorder.getClientPoDate(),salesOrderObj.getGrandTotal(), party.getPartyName(),salesOrderObj.getGst());
			//emailService.sendSOEmailToServer(emailContents);
		}

		Stages status = Stages.DESIGN;
		updateSoStatus(status, soObj.getId());

		return null;

	}

	private Map<String, Object> salesorderupdatedDetails(String clientPoNo, Date clientPoDate, double clientPoValue,
			String partyName, float gst) {
		String s = formatLakh(clientPoValue);
		DecimalFormat df = new DecimalFormat("#,###.00");
		Locale indiaLocale = new Locale("en", "IN");
		NumberFormat india = NumberFormat.getCurrencyInstance(indiaLocale);
		String dateFormatting = new SimpleDateFormat("dd-MM-yyyy").format(clientPoDate);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject", "SO Updated with " + clientPoNo);
		emailContents.put("template", "salesorder-created.html");
		emailContents.put("content", "Sales order Updated");
		emailContents.put("to1", "anitha@tek-nika.com");
		emailContents.put("to2", "anitha@tek-nika.com");
		emailContents.put("to3", "anitha@tek-nika.com");
		emailContents.put("to4", "anitha@tek-nika.com");
		emailContents.put("to5", "anitha@tek-nika.com");
//		emailContents.put("to1", "sunil@tek-nika.com");
//		emailContents.put("to2", "sunil@tek-nika.com");
//		emailContents.put("to3", "sunil@tek-nika.com");
		String gstAmount = formatLakh(gst);
		String total=formatLakh(clientPoValue-gst);
		emailContents.put("month", Constants.currentDate());
		emailContents.put("clientPo", clientPoNo);
		emailContents.put("clientPoDate", dateFormatting);
		emailContents.put("clientPoTotal", s);
		emailContents.put("gstAmount", gstAmount);
		emailContents.put("clientPoValueWithoutGst", total);
		// emailContents.put("clientPoValue", df.format(clientPoValue));
		emailContents.put("partyName", partyName);
		emailContents.put("attachment", filePath); 
		return emailContents;
	}

	private Map<String, Object> salesorderDetails(String clientPoNo, Date clientPoDate, double clientPoValue,
			String partyName, float gst) {
		String s = formatLakh(clientPoValue);
		DecimalFormat df = new DecimalFormat("#,###.00");
		Locale indiaLocale = new Locale("en", "IN");
		NumberFormat india = NumberFormat.getCurrencyInstance(indiaLocale);
		String dateFormatting = new SimpleDateFormat("dd-MM-yyyy").format(clientPoDate);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject", "SO Created with " + clientPoNo);
		emailContents.put("template", "salesorder-created.html");
		emailContents.put("content", "Sales order Created");
		emailContents.put("to1", "anitha@tek-nika.com");
		emailContents.put("to2", "anitha@tek-nika.com");
		emailContents.put("to3", "anitha@tek-nika.com");
		emailContents.put("to4", "anitha@tek-nika.com");
		emailContents.put("to5", "anitha@tek-nika.com");
//		emailContents.put("to1", "sunil@tek-nika.com");
//		emailContents.put("to2", "sunil@tek-nika.com");
//		emailContents.put("to3", "sunil@tek-nika.com");
		String gstAmount = formatLakh(gst);
		String total=formatLakh(clientPoValue-gst);
		emailContents.put("month", Constants.currentDate());
		emailContents.put("clientPo", clientPoNo);
		emailContents.put("clientPoDate", dateFormatting);
		emailContents.put("clientPoTotal", s);
		emailContents.put("gstAmount", gstAmount);
		emailContents.put("clientPoValueWithoutGst", total);
		// emailContents.put("clientPoValue", df.format(clientPoValue));
		emailContents.put("partyName", partyName);
		return emailContents;
	}

	private void updateSoStatus(Stages status, String soId) {
		Optional<SalesOrder> updatedso = getSalesOrderById(soId);
		updatedso.get().setStatus(status.name());
		salesrepo.save(updatedso.get());

	}

	/**
	 * get list of salesItems
	 * 
	 * @param id
	 * @param className
	 * @return salesItemList
	 */
	public List<SalesItem> getSalesListById(List<String> id, String className) {
		// get list of sales order for all sales id
		List<SalesOrder> salesOrderListbyId = salesrepo.findSalesOrderById(id);
		ArrayList<SalesItem> itemList = new ArrayList<SalesItem>();
		// get list of items for each sales order
		List<SalesItem> salesItemList = null;
		for (SalesOrder salesOrder : salesOrderListbyId) {

			if (className.equalsIgnoreCase("dc")) {
				salesItemList = getItemsWhereSupplyPriceIsGreaterThanZero(salesOrder.getId());
				for (SalesItem salesItem : salesItemList) {
					boolean value = false;
					Optional<SalesItem> item=getSalesItemById(salesItem.getId(),value);
					salesItem.set("unitName",item.get().getItem_units().getName());
					List<DesignItems> designItemList = soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
					if(!designItemList.isEmpty()) {
						itemList.add(salesItem);
					}
					List<DeliveryChallanItems> dcItemList = dcService.getDcItemListBySoItemId(salesItem.getId());
					float todaysQty = 0;
					// if for dc item list is empty for the selected salesItemId then set
					// delivered qty to 0.
					if (dcItemList.isEmpty()) {
						todaysQty = 0;
						salesItem.set("todaysQty", todaysQty);
						salesItem.set("deliveredQty", todaysQty);
					} else {
						for (DeliveryChallanItems dcItem : dcItemList) {
							todaysQty = todaysQty + dcItem.getTodaysQty();

						}
						 if (todaysQty == salesItem.getQuantity()) {
							 itemList.remove(salesItem);
						 }
						salesItem.set("todaysQty", salesItem.getQuantity() - todaysQty);
						salesItem.set("deliveredQty", todaysQty);
					}
					
					
				}
				//itemList.addAll(salesItemList);
			} else {
				salesItemList = salesOrder.getItems();
				
				for (SalesItem salesItem : salesItemList) {
					boolean value = false;
					Optional<SalesItem> item=getSalesItemById(salesItem.getId(),value);
					salesItem.set("unitName",item.get().getItem_units().getName());
					List<DesignItems> designItemList=soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
					ArrayList<DesignItems> vendoritemsList = new ArrayList<DesignItems>();
					for (DesignItems designItems : designItemList) {
						String designItemId=(String) designItems.get("itemMasterId");
						String itemId=designItemId;
						Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
						designItems.set("tax",itemObj.get().getGst());
						designItems.set("itemName",itemObj.get().getItemName());
						designItems.set("unit",itemObj.get().getItem_units().getName());
						designItems.set("hsnCode",itemObj.get().getHsnCode());
						designItems.set("model",itemObj.get().getModel());
						List<Supplier> supplierList=itemService.findItemsForSelectedVendor(designItemId,className);
						if(supplierList.size()>0) {
							vendoritemsList.add(designItems);
						}
					}
					salesItem.set("designItems", designItemList);
					salesItem.set("soNum",salesItem.getSalesOrder().getId());
					salesItem.set("clientPo",salesItem.getSalesOrder().getClientPoNumber());
					salesItem.set("client",salesItem.getSalesOrder().getParty().getPartyName());
					salesItem.set("vendoritemsList", vendoritemsList);
				}
				itemList.addAll(salesItemList);
			}
		}
		// this function is used in other places.so className is used to
		// differentiate.
		/*
		 * if (className.equalsIgnoreCase("dc")) {
		 * 
		 * for (SalesItem salesItem : salesItemList) { // get the list of dc items by
		 * salesItemId and check if the // total delivered qty is same as total sales
		 * item quantity List<DesignItems> designList=new ArrayList<>();
		 * SalesOrderDesign designObj =
		 * soDesignService.findSalesOrderDesignObjBysalesItemId(salesItem.getId());
		 * if(designObj!=null) { designList =designObj.getItems(); }
		 * List<DeliveryChallanItems> dcItemList =
		 * dcService.getDcItemListBySoItemId(salesItem.getId()); float todayQty = 0; for
		 * (DeliveryChallanItems dcItem : dcItemList) { todayQty = todayQty +
		 * dcItem.getTodaysQty();
		 * 
		 * }
		 * 
		 * // if the total quantity is equal to delivered quantity then // remove item
		 * from list if (todayQty == salesItem.getQuantity() && designList.size()==1) {
		 * itemList.remove(salesItem); } } }
		 */

		Collections.sort(itemList);
		return itemList;
	}
	
	public List<SalesItem> getSalesListBySalesOrderId(List<String> id, String className) {
		// get list of sales order for all sales id
		List<SalesOrder> salesOrderListbyId = salesrepo.findSalesOrderById(id);
		ArrayList<SalesItem> itemList = new ArrayList<SalesItem>();
		// get list of items for each sales order
		List<SalesItem> salesItemList = null;
		for (SalesOrder salesOrder : salesOrderListbyId) {

			if (className.equalsIgnoreCase("dc")) {
				salesItemList = getItemsWhereSupplyPriceIsGreaterThanZero(salesOrder.getId());
				for (SalesItem salesItem : salesItemList) {
					boolean value = false;
					Optional<SalesItem> item=getSalesItemById(salesItem.getId(),value);
					salesItem.set("unitName",item.get().getItem_units().getName());
					List<DesignItems> designItemList = soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
					if(!designItemList.isEmpty()) {
						itemList.add(salesItem);
					}
					List<DeliveryChallanItems> dcItemList = dcService.getDcItemListBySoItemId(salesItem.getId());
					float todaysQty = 0;
					// if for dc item list is empty for the selected salesItemId then set
					// delivered qty to 0.
					if (dcItemList.isEmpty()) {
						todaysQty = 0;
						salesItem.set("todaysQty", todaysQty);
						salesItem.set("deliveredQty", todaysQty);
					} else {
						for (DeliveryChallanItems dcItem : dcItemList) {
							todaysQty = todaysQty + dcItem.getTodaysQty();

						}
						 if (todaysQty == salesItem.getQuantity()) {
							 itemList.remove(salesItem);
						 }
						salesItem.set("todaysQty", salesItem.getQuantity() - todaysQty);
						salesItem.set("deliveredQty", todaysQty);
					}
					
					
				}
				//itemList.addAll(salesItemList);
			} else {
				salesItemList = salesItemrepo.getSalesItemListBySalesOrderId(salesOrder.getId());
				
				for (SalesItem salesItem : salesItemList) {
					boolean value = false;
					Optional<SalesItem> item=getSalesItemById(salesItem.getId(),value);
					salesItem.set("unitName",item.get().getItem_units().getName());
					List<DesignItems> designItemList=soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
					ArrayList<DesignItems> vendoritemsList = new ArrayList<DesignItems>();
					for (DesignItems designItems : designItemList) {
						String designItemId=(String) designItems.get("itemMasterId");
						String itemId=designItemId;
						Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
						designItems.set("tax",itemObj.get().getGst());
						designItems.set("itemName",itemObj.get().getItemName());
						designItems.set("unit",itemObj.get().getItem_units().getName());
						designItems.set("hsnCode",itemObj.get().getHsnCode());
						designItems.set("model",itemObj.get().getModel());
						List<Supplier> supplierList=itemService.findItemsForSelectedVendor(designItemId,className);
						if(supplierList.size()>0) {
							vendoritemsList.add(designItems);
						}
					}
					salesItem.set("designItems", designItemList);
					salesItem.set("soNum",salesItem.getSalesOrder().getId());
					salesItem.set("clientPo",salesItem.getSalesOrder().getClientPoNumber());
					salesItem.set("client",salesItem.getSalesOrder().getParty().getPartyName());
					salesItem.set("vendoritemsList", vendoritemsList);
				}
				itemList.addAll(salesItemList);
			}
		}
		// this function is used in other places.so className is used to
		// differentiate.
		/*
		 * if (className.equalsIgnoreCase("dc")) {
		 * 
		 * for (SalesItem salesItem : salesItemList) { // get the list of dc items by
		 * salesItemId and check if the // total delivered qty is same as total sales
		 * item quantity List<DesignItems> designList=new ArrayList<>();
		 * SalesOrderDesign designObj =
		 * soDesignService.findSalesOrderDesignObjBysalesItemId(salesItem.getId());
		 * if(designObj!=null) { designList =designObj.getItems(); }
		 * List<DeliveryChallanItems> dcItemList =
		 * dcService.getDcItemListBySoItemId(salesItem.getId()); float todayQty = 0; for
		 * (DeliveryChallanItems dcItem : dcItemList) { todayQty = todayQty +
		 * dcItem.getTodaysQty();
		 * 
		 * }
		 * 
		 * // if the total quantity is equal to delivered quantity then // remove item
		 * from list if (todayQty == salesItem.getQuantity() && designList.size()==1) {
		 * itemList.remove(salesItem); } } }
		 */

		Collections.sort(itemList);
		return itemList;
	}

	public Optional<SalesOrder> getSalesOrderById(String salesOrderId) {
		Optional<SalesOrder> salesOrder = salesrepo.findById(salesOrderId);
		return salesOrder;
	}
	
	public Optional<SalesItem> getSalesItemObjById(String salesItemId) {
		Optional<SalesItem> salesItem = salesItemrepo.findById(salesItemId);
		return salesItem;
	}

	public Optional<SalesItem> getSalesItemById(String salesItemid, boolean value) {
		System.out.println("this is"+ salesItemid);
		Optional<SalesItem> salesItem = salesItemrepo.findById(salesItemid);
		System.out.println(salesItem.get().getId());
		String clientId = salesItem.get().getSalesOrder().getParty().getId();
		// check for stock list while changing description in dc
		if (value) {
			Map<String, Object> map = dcService.checkForStockAvailable(salesItemid, clientId);

			if (map == null || map.isEmpty()) {
				return null;
			} else if (map.get("stockError") == "stockError") {

				salesItem.get().set("stockError", map.get("stockError"));
			} else if (map.get("mapDesign") == "mapDesign") {
				salesItem.get().set("mapDesign", map.get("mapDesign"));
			}
		}

		// get the list of delivery challan items by salesItem id
		List<DeliveryChallanItems> dcItemList = dcService.getDcItemListBySoItemId(salesItemid);
		float todaysQty = 0;
		// if for dc item list is empty for the selected salesItemId then set
		// delivered qty to 0.
		if (dcItemList.isEmpty()) {
			todaysQty = 0;
			salesItem.get().set("todaysQty", todaysQty);
			salesItem.get().set("deliveredQty", todaysQty);
		} else {
			for (DeliveryChallanItems dcItem : dcItemList) {
				todaysQty = todaysQty + dcItem.getTodaysQty();

			}
			salesItem.get().set("todaysQty", salesItem.get().getQuantity() - todaysQty);
			salesItem.get().set("deliveredQty", todaysQty);
		}

		return salesItem;
	}

	// For getting list of salesItems
	public List<SalesItem> getAllSalesItemList() {

		List<SalesItem> salesItemList = salesItemrepo.findAll();
		return salesItemList;
	}

	/**
	 * get Sales order list by party id
	 * 
	 * @param partyId
	 * @return salesOrderList
	 */
	public List<SalesOrder> getSalesOrderListByPartyId(String partyId) {
		List<SalesOrder> salesOrderList = salesrepo.getSalesOrdersByPartyId(partyId);
		return salesOrderList;
	}

	public List<SalesItem> getItemsWhereSupplyPriceIsGreaterThanZero(String salesOrderId) {
		// TODO Auto-generated method stub
		List<SalesItem> salesItemList = salesItemrepo.getSalesItemsBySalesOrderId(salesOrderId);
		return salesItemList;
	}

	public void deleteSalesItemById(String salesItemId) {
		salesItemrepo.deleteById(salesItemId);
	}

	public List<SalesOrder> getSalesOrderListWithStatusNotClosed() {
		List<SalesOrder> salesList = getSalesOrderList();
		for (SalesOrder salesOrder : salesList) {
			updateSoStatusToWorkInProgress(salesOrder.getId());
		}

		/*
		 * CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		 * CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);
		 * ArrayList<Object[]> salesOrderList = new ArrayList<Object[]>(); for
		 * (SalesOrder salesOrder : salesList) { String soNumber=salesOrder.getId();
		 * Root<SalesItem> salesItem = query.from(SalesItem.class); Expression<Float>
		 * totalQty = criteriaBuilder.sum(salesItem.get("quantity")).as(Float.class);
		 * List<DeliveryChallan> dc= dcService.getDcListBySoId(salesOrder.getId());
		 * if(!dc.isEmpty()) { Root<DeliveryChallanItems> dcItem =
		 * query.from(DeliveryChallanItems.class); Expression<Float> totalDeliverdQty =
		 * criteriaBuilder.sum(dcItem.get("deliveredQuantity")).as(Float.class);
		 * Predicate onStart = criteriaBuilder.notEqual(totalQty, totalDeliverdQty);
		 * query.multiselect(salesItem,totalQty).where(onStart);
		 * 
		 * TypedQuery<Object[]> typedQuery = em.createQuery(query); List<Object[]> list
		 * = typedQuery.getResultList(); salesOrderList.addAll(list); } }
		 */

		return salesList;
	}

	public void updateSoStatusToWorkInProgress(String soId) {
		Stages status = Stages.WORK_IN_PROGRESS;
		Optional<SalesOrder> salesOrder = getSalesOrderById(soId);
		List<SalesItem> items = salesOrder.get().getItems();
		ArrayList<SalesItem> soItems = new ArrayList<SalesItem>();
		for (SalesItem salesItem : items) {
			List<SalesOrderDesign> soDesignList = soDesignService.findSalesOrderDesignBysalesItemId(salesItem.getId());
			if (soDesignList.size() > 0) {
				soItems.add(salesItem);
			}
		}
		if (soItems.size() == items.size()) {
			salesOrder.get().setStatus(status.name());
			salesrepo.save(salesOrder.get());
		}

	}

	public List<SalesOrder> getAllSalesOrderList() {
		List<SalesOrder> salesList = getSalesOrderList();
		return salesList;
	}

	public List<PurchaseOrder> getAllPoBySalesOrderId(String salesOrderId) {
		Optional<SalesOrder> soObj = salesrepo.findById(salesOrderId);
		List<SalesItem> salesItemList = soObj.get().getItems();
		Set set = new HashSet();
		for (SalesItem salesItem : salesItemList) {
			List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItem.getId());
			for (PurchaseItem poItem : purchaseItemList) {
				Optional<PurchaseOrder> po = purchaseRepo.findById(poItem.getPurchaseOrder().getPoNumber());
				set.add(po.get());
			}

		}
		ArrayList<PurchaseOrder> poList = new ArrayList<PurchaseOrder>(set);
		for (PurchaseOrder purchaseOrder : poList) {
			Party party = purchaseOrder.getParty();

			String addr1 = party.getAddr1();
			addr1 = addr1.replace("'", "&");
			addr1 = addr1.replace("\"", "&");
			party.setAddr1(addr1);

			if (party.getAddr2() != null) {
				String addr2 = party.getAddr2();
				addr2 = addr2.replace("'", "&");
				addr2 = addr2.replace("\"", "&");
				party.setAddr2(addr2);
			}
			String partyName = party.getPartyName();
			partyName = partyName.replace("\"", "&");
			partyName = partyName.replace("'", "&");
			party.setPartyName(partyName);
		}

		return poList;
	}

	public List<Grn> getGrnListBySoId(String salesOrderId) {
		Optional<SalesOrder> soObj = salesrepo.findById(salesOrderId);
		List<SalesItem> salesItemList = soObj.get().getItems();
		Set set = new HashSet();
		ArrayList<Grn> grnList = new ArrayList<Grn>();
		for (SalesItem salesItem : salesItemList) {
			List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItem.getId());
			for (PurchaseItem poItem : purchaseItemList) {
				Optional<PurchaseOrder> po = purchaseRepo.findById(poItem.getPurchaseOrder().getPoNumber());
				set.add(po.get());
			}
		}
		ArrayList<PurchaseOrder> poList = new ArrayList<PurchaseOrder>(set);
		for (PurchaseOrder purchaseOrder : poList) {
			List<Grn> list = grnService.findGrnByPoNumber(purchaseOrder.getPoNumber());
			grnList.addAll(list);
		}
		return grnList;
	}

	public void saveUploadedFiles(List<MultipartFile> files) throws IOException {
		for (MultipartFile file : files) {

			if (file.isEmpty()) {
				continue;
			}

			byte[] bytes = file.getBytes();
			Path path = Paths.get(Constants.FILE_LOCATION + File.separator + file.getOriginalFilename());
			Files.write(path, bytes);

		}

	}

	public List<Object> getPendingDcList(String name) {
		// TODO Auto-generated method stub
		List<Object> pendingDcList = new ArrayList();
		List<SalesOrder> salesOrderList = getSalesListByPartyId(name);
		// for (SalesOrder salesOrder : salesOrderList) {
		for (int j = 0; j < salesOrderList.size(); j++) {

			List<DeliveryChallan> dcList = dcService.getDcListBySoId(salesOrderList.get(j).getId());
			List<SalesItem> soItemList = salesOrderList.get(j).getItems();
			if (dcList.isEmpty()) {
				// List<SalesItem> soItemList = salesOrderList.get(j).getItems();
				for (SalesItem salesItem : soItemList) {

					JSONObject object = new JSONObject();

					object.put("Description", salesItem.getDescription());
					object.put("hsnCode", salesItem.getHsnCode());
					object.put("units", salesItem.getItem_units().getName());
					object.put("unitprice", salesItem.getUnitPrice());
					object.put("amount", salesItem.getAmount());
					object.put("Date", salesOrderList.get(j).getCreated());
					object.put("Qty", salesItem.getQuantity());
				
					pendingDcList.add(object);
				}
			} else {
				Map<String,Object> m = new HashMap();
				for (SalesItem salesItem : soItemList) {
					float todayQty = 0;
					List<DeliveryChallanItems> dcitemList = dcService.getDcItemListBySoItemId(salesItem.getId());
					if (dcitemList.size() > 0) {
						for (DeliveryChallanItems dcItems : dcitemList) {
							 todayQty = todayQty +dcItems.getTodaysQty();
							float salesQty = dcItems.getTotalQuantity();
							float remQty = salesQty - todayQty;
							if (remQty > 0) {
								JSONObject object = new JSONObject();

								object.put("Description", salesItem.getDescription());
								object.put("hsnCode", salesItem.getHsnCode());
								object.put("units", salesItem.getItem_units().getName());
								object.put("unitprice", salesItem.getUnitPrice());
								object.put("amount", salesItem.getAmount());
								object.put("Date", salesOrderList.get(j).getCreated());
								object.put("Qty", remQty);
								if(m.containsKey(salesItem.getDescription())) {
									m.put(salesItem.getDescription(), object);
								}else {
								m.put(salesItem.getDescription(), object);
								}
							//	pendingDcList.add(object);
							}
						}
					} else {
						JSONObject object = new JSONObject();

						object.put("Description", salesItem.getDescription());
						object.put("hsnCode", salesItem.getHsnCode());
						object.put("units", salesItem.getItem_units().getName());
						object.put("unitprice", salesItem.getUnitPrice());
						object.put("amount", salesItem.getAmount());
						object.put("Date", salesOrderList.get(j).getCreated());
						object.put("Qty", salesItem.getQuantity());
						m.put(salesItem.getDescription(), object);
						//pendingDcList.add(object);

					}
					/*
					 * for (DeliveryChallan deliveryChallan : dcList) { List<DeliveryChallanItems>
					 * dcItems = deliveryChallan.getItems(); for (DeliveryChallanItems dcItem :
					 * dcItems) { float deliveredQty = dcItem.getDeliveredQuantity(); String
					 * salesItem = salesItemrepo.findById(id);
					 * 
					 * JSONObject object = new JSONObject();
					 * 
					 * object.put("Description", salesItem.getDescription()); object.put("hsnCode",
					 * salesItem.getHsnCode()); object.put("units",
					 * salesItem.getItem_units().getName()); object.put("unitprice",
					 * salesItem.getUnitPrice()); object.put("amount", salesItem.getAmount());
					 * object.put("Date", salesOrderList.get(j).getCreated()); object.put("Qty",
					 * salesItem.getQuantity()); } }
					 */
				}
				
				/*
				 * This is to add the objects to a list..because if there is a dc twice
				 * then it will create a problem so to overcome that using this..
				 */
				for (String  key : m.keySet()) {
					Object obj = m.get(key);
					pendingDcList.add(obj);
				}
			}
		}

		return pendingDcList;
	}

	/*public List<SalesOrder> getPendingSalesList() {
		long startTime = System.currentTimeMillis();
		List<SalesOrder> pendingSoList = new ArrayList();
		List<SalesOrder> soList = getAllSalesOrderList();
		for (SalesOrder salesOrder : soList) {
			List<DeliveryChallan> dcList = dcRepo.getAllDcBySoId(salesOrder.getId());
			if (dcList.size() == 0) {
				pendingSoList.add(salesOrder);
			} else {
				List<SalesItem> salesItemsList = salesOrder.getItems();
				for (SalesItem item : salesItemsList) {
					float dcQty = 0;
					List<DeliveryChallanItems> dcItemsList = dcService.getDcItemListBySoItemId(item.getId());
					for (DeliveryChallanItems dcItem : dcItemsList) {
						//if (dcItem.getTodaysQty() >= dcItem.getDeliveredQuantity()) {
							dcQty = dcQty + dcItem.getTodaysQty();
						//} else {
						//	dcQty = dcQty + dcItem.getDeliveredQuantity();
						//}
					}
				    
					if (dcQty != item.getQuantity()) {
						pendingSoList.add(salesOrder);
						break;
					}
				}
			}

		}
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	    System.out.println("time to loop each item of Sales list"+startTime+"&"+ stopTime);
		return pendingSoList;
	}*/

	public List<SalesOrder> getPendingSalesList() {
		long startTime = System.currentTimeMillis();
		List<SalesOrder> pendingSoList = new ArrayList();
		List<SalesOrder> soWhereDcNotDone = salesrepo.getSalesListWhereDCNotDone();
		pendingSoList.addAll(soWhereDcNotDone);
		List<SalesOrder> pendingList = salesrepo.getpendingSoList();
		pendingSoList.addAll(pendingList);
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	    System.out.println("time to loop each item of Sales list"+startTime+"&"+ stopTime);
		return pendingSoList;
	}
	
	public boolean checkForDcExists(String salesItemId) {
		boolean dcExists = false;
		List<DeliveryChallanItems> dcList = dcService.getDcItemListBySoItemIdWhereDcQtyNotZero(salesItemId);
		//List<SalesOrderDesign> designList = soDesignService.findSalesOrderDesignBysalesItemId(salesItemId);
		List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItemId);
		if (dcList.size() > 0 ||purchaseItemList.size()>0) {
			dcExists = true;
		} else {
			dcExists = false;
		}
		return dcExists;
	}

	private static String formatLakh(double d) {
		String s = String.format(Locale.UK, "%1.2f", Math.abs(d));
		s = s.replaceAll("(.+)(...\\...)", "$1,$2");
		while (s.matches("\\d{3,},.+")) {
			s = s.replaceAll("(\\d+)(\\d{2},.+)", "$1,$2");
		}
		return d < 0 ? ("-" + s) : s;
	}

	public Map getStockByRegionBetweenDates(Timestamp sqlFromDate, Timestamp sqlToDate, String region) {
		// TODO Auto-generated method stub
		List<SalesOrder> salesOrderList = salesrepo.findByRegion(region, sqlFromDate, sqlToDate);
		// Map stockMap = new HashMap();
		Map<String, Map> stockMap = new HashMap<String, Map>();
		String itemId = "";
		String itemName = "";
		float currQty = 0;
		float grnQty = 0;
		float dcQty = 0;
		for (SalesOrder salesOrder : salesOrderList) {
			List<SalesItem> soItemList = salesOrder.getItems();

			for (SalesItem salesItem : soItemList) {
				currQty = 0;
				grnQty = 0;
				dcQty = 0;
				String salesItemId = salesItem.getId();
				List<DeliveryChallanItems> dcItemList = dcItemRepo.findByBetweenDateAndSoItem(sqlFromDate, sqlToDate,
						salesItemId);

				List<PurchaseItem> poItemList = poItemRepo.findBySalesItemId(salesItemId);
				for (PurchaseItem poItem : poItemList) {
					System.out.println("Item idd" + poItem.getModelNo());
					String poModelNo = poItem.getModelNo();
					if (poItem.getModelNo().equalsIgnoreCase("ITEM-3143")) {
						System.out.println("item" + poModelNo);
					}
					if (poItem.getModelNo() != null && !poItem.getModelNo().equalsIgnoreCase("")
							&& poItem.getModelNo().isEmpty() == false) {
						Optional<ItemMaster> item = itemService.getItemById(poItem.getModelNo());
						itemId = item.get().getId();
						itemName = item.get().getItemName();
					}

					List<GrnItems> grnItems = grnItemRepo
							.findByPoItemId(Integer.toString(poItem.getPurchase_item_id()));
					for (GrnItems grnItem : grnItems) {
						grnQty = grnQty + grnItem.getReceivedQuantity();
					}
				} // Purchase Item list loop..

				for (DeliveryChallanItems dcItem : dcItemList) {
					SalesOrderDesign designObj = soDesignService
							.findSalesOrderDesignObjBysalesItemId(dcItem.getDescription());

					if (designObj != null) {
						System.out.println("designObject " + designObj);
						System.out.println("design obj" + designObj);
						DesignItems designItemsList = designItemRepo.findDesignItemObjByItemIdAndDesignId(itemId,
								designObj.getId());

						if (designItemsList != null) {
							if (itemId.equalsIgnoreCase(designItemsList.getItemId())
									&& designItemsList.getDeliveredQty() > 0) {
								dcQty = dcQty + designItemsList.getDeliveredQty();
							} else {
								if (dcItem.getTodaysQty() > dcItem.getDeliveredQuantity()) {
									dcQty = dcQty + dcItem.getTodaysQty();
								} else {
									dcQty = dcQty + dcItem.getDeliveredQuantity();
								}

							}

						}// design item not equal to null..
					}// Design object not equal to null
				} // For each of delivery items list..

			}
			currQty = grnQty - dcQty;
			Map<String, Object> itemMap = new HashMap();
			itemMap.put("itemId", itemName + "/" + itemId);
			itemMap.put("qty", currQty);
			if (currQty > 0 && (itemId != null || itemId != "")) {
				stockMap.put(itemName + "/" + itemId, itemMap);
			}
		}
		return stockMap;
	}

	public List<SalesItem> getSalesItemListWithoutDesign(String salesOrderId) {
		Optional<SalesOrder> salesObj = getSalesOrderById(salesOrderId);
		List<SalesItem> itemList=salesObj.get().getItems();
		ArrayList<SalesItem> salesItemList = new ArrayList<SalesItem>();
		for (SalesItem salesItem : itemList) {
			List<DesignItems> designItemList = soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
			if(designItemList.isEmpty()) {
				salesItemList.add(salesItem);
			}
		}
		
		return salesItemList;
	}

	public List<SalesOrder> getSalesOrderByItemId(String itemId) {
		List<DesignItems> designItemList = soDesignService.getDesignItemListByItemId(itemId);
		ArrayList<SalesOrderDesign> designList = new ArrayList<SalesOrderDesign>();
		for (DesignItems designItem : designItemList) {
			long designId = designItem.getSalesOrderDesign().getId();
			Optional<SalesOrderDesign> soDesign = soDesignService.findSalesOrderDesignById(designId);
			designList.add(soDesign.get());
		}
		Set set = new HashSet();
		for (SalesOrderDesign salesOrderDesign : designList) {
			String salesItemId = salesOrderDesign.getSalesItemId();
			boolean value = false;
			Optional<SalesItem> soItem =getSalesItemById(salesItemId, value);
			String soNumber = soItem.get().getSalesOrder().getId();
			Optional<SalesOrder> salesOrder =salesrepo.findById(soNumber);
			set.add(salesOrder.get());
		}
		ArrayList<SalesOrder> soList = new ArrayList<SalesOrder>(set);
		return soList;
	}
	
	public List<SalesItem> getSalesItemsBySalesOrderId(String salesOrderId) {
		// TODO Auto-generated method stub
		List<SalesItem> salesItemList = salesItemrepo.findSalesItemsBySalesOrderId(salesOrderId);
		return salesItemList;
	}
	
	
	public List<SalesItem> getAllSalesItemListWithoutDesign() {
		List<SalesOrder> salesOrderList = getSalesOrderList();
		Collections.sort(salesOrderList);
		ArrayList<SalesItem> salesItemList = new ArrayList<SalesItem>();
		for (SalesOrder salesOrder : salesOrderList) {
			List<SalesItem> itemList=getSalesItemsBySalesOrderId(salesOrder.getId());
			for (SalesItem salesItem : itemList) {
				List<DesignItems> designItemList = soDesignService.getAllDesignItemListBySOItemId(salesItem.getId());
				if(!salesItem.getItem_units().getName().equals("Heading")) {
					if(designItemList.isEmpty()) {
						salesItemList.add(salesItem);
					}
				}
			}
		}
		
		System.out.println(salesItemList.size());
		return salesItemList;
	}
	
	/*public List<SalesOrder> getAllSalesItemListWithoutDesignForDashboard() {
		long startTime = System.currentTimeMillis();
		List<SalesOrder> salesOrderList = getSalesOrderList();
		
		Set set = new HashSet();
		for (SalesOrder salesOrder : salesOrderList) {
			List<SalesItem> itemList=getSalesItemsBySalesOrderId(salesOrder.getId());
			for (SalesItem salesItem : itemList) {
				List<DesignItems> designItemList = soDesignService.getAllDesignItemListBySOItemId(salesItem.getId());
				if(designItemList.isEmpty()) {
					set.add(salesOrder);
				}
			}
		}
		ArrayList<SalesOrder> soList = new ArrayList<SalesOrder>(set);
		Collections.sort(soList);
		System.out.println(soList.size());
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    System.out.println(elapsedTime);
	    System.out.println("time to loop each item of so without design list"+startTime+"&"+ stopTime);
		return soList;
	}*/
	
	public List<SalesOrder> getAllSalesItemListWithoutDesignForDashboard(){
		List<SalesOrder> salesOrderList = salesrepo.getSalesOrderWithoutDesign();
		return salesOrderList;
	}
	
	public List<SalesOrder> getAllSalesOrderQithDesignAndPoNotDoneForDashboard(){
		List<SalesOrder> salesOrderList = salesrepo.getSalesOrderWithDesign();
		System.out.println("so with design and PO not done : "+salesOrderList.size());
		return salesOrderList;
	}
	
	public List<SalesItem> getAllSalesItemListWithouPOBySoId(String soId) {
		Optional<SalesOrder> soObj = getSalesOrderById(soId);
		ArrayList<SalesItem> itemList = new ArrayList<SalesItem>();
		List<SalesItem> salesItemList = soObj.get().getItems();
		
		for (SalesItem salesItem : salesItemList) {
			
			salesItem.set("soNum",salesItem.getSalesOrder().getId());
			salesItem.set("clientPo",salesItem.getSalesOrder().getClientPoNumber());
			salesItem.set("client",salesItem.getSalesOrder().getParty().getPartyName());
			boolean value = false;
			Optional<SalesItem> item=getSalesItemById(salesItem.getId(),value);
			salesItem.set("unitName",item.get().getItem_units().getName());
			List<DesignItems> designItemList=soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
			if(!designItemList.isEmpty()) {
				for (DesignItems designItems : designItemList) {
					String designItemId=(String) designItems.get("itemMasterId");
					String itemId=designItemId;
					Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
					designItems.set("tax",itemObj.get().getGst());
					designItems.set("itemName",itemObj.get().getItemName());
					designItems.set("unit",itemObj.get().getItem_units().getName());
					designItems.set("hsnCode",itemObj.get().getHsnCode());
					designItems.set("model",itemObj.get().getModel());
					List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(salesItem.getId(), designItems.getItemId());
					List<DeliveryChallanItems> dcItemList = dcService.getDcItemListBySoItemIdWhereDcQtyNotZero(salesItem.getId());
					if(dcItemList.isEmpty()) {
						
						if(purchaseItemList.isEmpty()) {
						
							itemList.add(salesItem);
						}
					}
					
					
				}
			}
			salesItem.set("designItems", designItemList);
		}
		//itemList.addAll(salesItemList);
		System.out.println("itemListSie"+itemList.size());
		return itemList;
		
	}
	
	public List<SalesItem> getAllSalesItemListWithouPO() {
		List<SalesOrder> salesOrderList = getSalesOrderList();
		Collections.sort(salesOrderList);
		ArrayList<SalesItem> salesItemList = new ArrayList<SalesItem>();
		for (SalesOrder salesOrder : salesOrderList) {
			List<SalesItem> itemList=getSalesItemsBySalesOrderId(salesOrder.getId());
			for (SalesItem salesItem : itemList) {
				List<DesignItems> designItemList = soDesignService.getAllDesignItemListBySOItemId(salesItem.getId());
				if(!designItemList.isEmpty()) {
					for (DesignItems designItem : designItemList) {
						List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(salesItem.getId(), designItem.getItemId());
						List<DeliveryChallanItems> dcItemList = dcService.getDcItemListBySoItemIdWhereDcQtyNotZero(salesItem.getId());
						if(dcItemList.isEmpty()) {
							
							if(purchaseItemList.isEmpty()) {
							
								salesItemList.add(salesItem);
							}
						}
					}
				
				}
			}
		}
		
		System.out.println(salesItemList.size());
		return salesItemList;
	}

	public List<SalesOrder> getPendingSalesListPartial() {
		long startTime = System.currentTimeMillis();
		List<SalesOrder> pendingSoList = new ArrayList();
		List<SalesOrder> soList = getAllSalesOrderList();
		Collections.sort(soList);
		
		
			
		int count=0;
		
		for (int i = soList.size()-1; i > 0; i--) {
			if(count<10) {
			System.out.println(soList.size()-1);
			List<DeliveryChallan> dcList = dcRepo.getAllDcBySoId(soList.get(i).getId());
			if (dcList.size() == 0) {
				pendingSoList.add(soList.get(i));
				count++;
			} else {
				List<SalesItem> salesItemsList = soList.get(i).getItems();
				for (SalesItem item : salesItemsList) {
					float dcQty = 0;
					List<DeliveryChallanItems> dcItemsList = dcService.getDcItemListBySoItemId(item.getId());
					for (DeliveryChallanItems dcItem : dcItemsList) {
						if (dcItem.getTodaysQty() >= dcItem.getDeliveredQuantity()) {
							dcQty = dcQty + dcItem.getTodaysQty();
						} else {
							dcQty = dcQty + dcItem.getDeliveredQuantity();
						}
					}
				    
					if (dcQty != item.getQuantity()) {
						pendingSoList.add(soList.get(i));
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
	    System.out.println("time to loop each item of Sales list"+startTime+"&"+ stopTime);
		return pendingSoList;
	}

	public List<SalesOrder> getAllSalesOrderListPartial() {
		List<SalesOrder> salesList = getSalesOrderList();
		Collections.sort(salesList);
		ArrayList<SalesOrder> list=new ArrayList<SalesOrder>();
		if(salesList.size()>11) {
			for (int i = salesList.size()-1; i > salesList.size()-11; i--) {
				list.add(salesList.get(i));
			}
		}else {
			list.addAll(salesList);
		}
		return list;
	}

	/*public List<SalesOrder> getAllSalesItemListWithoutDesignForDashboardPartial() {
		List<SalesOrder> salesOrderList = getSalesOrderList();
		Collections.sort(salesOrderList);
		Set set = new HashSet();
		for (int i = salesOrderList.size()-1; i > 0; i--) {
			if(set.size()<10) {
				List<SalesItem> itemList=getSalesItemsBySalesOrderId(salesOrderList.get(i).getId());
				for (SalesItem salesItem : itemList) {
					List<DesignItems> designItemList = soDesignService.getAllDesignItemListBySOItemId(salesItem.getId());
					if(designItemList.isEmpty()) {
						set.add(salesOrderList.get(i));
						System.out.println(set.size()+"set size in loop");
					}
				}
			}
		}
		System.out.println(set.size()+"set size");
		ArrayList<SalesOrder> soList = new ArrayList<SalesOrder>(set);
		
		return soList;
	}*/
	
	public List<SalesOrder> getAllSalesItemListWithoutDesignForDashboardPartial() {
		List<SalesOrder> salesOrderList = salesrepo.getSalesOrderWithoutDesign();
		Collections.sort(salesOrderList);
		ArrayList<SalesOrder> list=new ArrayList<SalesOrder>();
		if(salesOrderList.size()>11) {
			for (int i = salesOrderList.size()-1; i > salesOrderList.size()-11; i--) {
				list.add(salesOrderList.get(i));
			}
		}else {
			list.addAll(salesOrderList);
		}
		
		return list;
	}
	
	public List<SalesItem> getPendingItemsToBeDeliveredInSo(){
		List<SalesItem> pendingSoItemList = new ArrayList();
		List<SalesOrder> soList = getAllSalesOrderList();
		Collections.sort(soList);
		for (SalesOrder salesOrder : soList) {
				List<SalesItem> salesItemsList = getSalesItemsBySalesOrderId(salesOrder.getId());
				for (SalesItem item : salesItemsList) {
					if(item.getQuantity()>0 || item.getUnitPrice()>0) {
					//if(!item.getItem_units().getName().equals("Heading")) {
					
					List<DeliveryChallanItems> dcItemsList = dcService.getDcItemListBySoItemId(item.getId());
					if(dcItemsList.size()==0) {
						item.set("notDeliveredQty",item.getQuantity());
						pendingSoItemList.add(item);	
					}else {
						float dcQty = 0;
						for (DeliveryChallanItems dcItem : dcItemsList) {
							//if (dcItem.getTodaysQty() >= dcItem.getDeliveredQuantity()) {
								dcQty = dcQty + dcItem.getTodaysQty();
							//} else {
								//dcQty = dcQty + dcItem.getDeliveredQuantity();
							//}
						}
					    
						if (dcQty != item.getQuantity()) {
							float notDeliveredQty= item.getQuantity()-dcQty;
							System.out.println(notDeliveredQty);
							item.set("notDeliveredQty",notDeliveredQty);
							pendingSoItemList.add(item);
							break;
						}
					}
					}
				}

		}
		return pendingSoItemList;
		
	}
	
	public List<SalesOrder> activeSalesList() {
		List<SalesOrder> pendingSoList = new ArrayList();
		List<SalesOrder> soList = getAllSalesOrderList();
		Collections.sort(soList);
		for (SalesOrder salesOrder : soList) {

	
			List<DeliveryChallan> dcList = dcRepo.getAllDcBySoId(salesOrder.getId());
			if (dcList.size() == 0) {
				pendingSoList.add(salesOrder);
			} else {
				List<SalesItem> salesItemsList = getSalesItemsBySalesOrderId(salesOrder.getId());
				for (SalesItem item : salesItemsList) {
					float dcQty = 0;
					List<DeliveryChallanItems> dcItemsList = dcService.getDcItemListBySoItemId(item.getId());
					for (DeliveryChallanItems dcItem : dcItemsList) {
							dcQty = dcQty + dcItem.getTodaysQty();
						
					}
				    
					if (dcQty != item.getQuantity()) {
						pendingSoList.add(salesOrder);
						break;
					}
				}
			}

		}
		return pendingSoList;
	}
	
	
	public List<Party> getActiveSalesOrderWithoutPO() {
		List<Party> partyList =partyService.getPartyListByTypeCustomerWhereSoExist();
		Set set = new HashSet();
		
		Collections.sort(partyList);
		for (Party party : partyList) {
			List<SalesOrder> salesOrderList = getSalesListByPartyId(party.getId());
			ArrayList<SalesItem> salesItemList = new ArrayList<SalesItem>();
			for (SalesOrder salesOrder : salesOrderList) {
				List<SalesItem> itemList=getSalesItemsBySalesOrderId(salesOrder.getId());
				for (SalesItem salesItem : itemList) {
					if(!salesItem.getItem_units().getName().equals("Heading")) {
					List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItem.getId());
					
							if(poItemList.isEmpty()) {
								set.add(party);
							}
						}
				}
					
			}
		}
		ArrayList<Party> list = new ArrayList<Party>(set);
		Collections.sort(list);
		System.out.println(list.size());
		
		return list;
	}
	
	public List<SalesItem> getActiveSalesItemListByCustomer(String partyId){
		List<SalesOrder> salesOrderList = getSalesListByPartyId(partyId);
		Collections.sort(salesOrderList);
		ArrayList<SalesItem> salesItemList = new ArrayList<SalesItem>();
			for (SalesOrder so : salesOrderList) {
				
				List<SalesItem> salesList=getSalesItemsBySalesOrderId(so.getId());
				
				for (SalesItem soItem : salesList) {
					if(soItem.getItem_units().getName()!="Heading") {
						List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemsBySalesItemId(soItem.getId());
						
								if(poItemList.isEmpty()) {
									salesItemList.add(soItem);
								}
							}	
				}
			}
			return salesItemList;
	}

	public List<SalesOrder> getArchivedSalesOrderList() {
		List<SalesOrder> salesList = salesrepo.findArchivedSalesList();
		return salesList;
	}

	public void archiveSO(String soId) {
		Optional<SalesOrder> so = salesrepo.findById(soId);
		so.get().setArchive(true);
		salesrepo.save(so.get());
		
	}
	
	public void unArchiveSO(String soId) {
		Optional<SalesOrder> so = salesrepo.findById(soId);
		so.get().setArchive(false);
		salesrepo.save(so.get());
		
	}
	
	public List<SalesOrder> getSalesOrderWithoutDesignByPartyIdBan(String partyId){
		List<SalesOrder> salesOrderList = salesrepo.findSalesOrderWithoutDesignByPartyIdBan(partyId);
		return salesOrderList;
	}
	
	public List<SalesOrder> getSalesOrderWithoutDesignByPartyIdMan(String partyId){
		List<SalesOrder> salesOrderList = salesrepo.findSalesOrderWithoutDesignByPartyIdMan(partyId);
		return salesOrderList;
	}
	
	public List<Party> getAllSalesOrderWithoutDesignForEmailBan(){
		List<Party> partyList =partyService.getPartyListByTypeCustomerWhereSoExist();
		Set set = new HashSet();
		Collections.sort(partyList);
		for (Party party : partyList) {
			List<SalesOrder> salesOrderList = getSalesOrderWithoutDesignByPartyIdBan(party.getId());
			if(!salesOrderList.isEmpty()) {
				set.add(party);
			}
		}
		ArrayList<Party> list = new ArrayList<Party>(set);
		Collections.sort(list);
		return list;
	}
	
	public List<Party> getAllSalesOrderWithoutDesignForEmailMan(){
		List<Party> partyList =partyService.getPartyListByTypeCustomerWhereSoExist();
		Set set = new HashSet();
		Collections.sort(partyList);
		for (Party party : partyList) {
			List<SalesOrder> salesOrderList = getSalesOrderWithoutDesignByPartyIdMan(party.getId());
			if(!salesOrderList.isEmpty()) {
				set.add(party);
			}
		}
		ArrayList<Party> list = new ArrayList<Party>(set);
		Collections.sort(list);
		return list;
	}

	public List<SalesOrder> getAllSoCreatedToday(String todaysDateInStr) {
		List<SalesOrder> salesList = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");        
		sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        Date todaysDate = null;
        Date fromDate = null;
		try {
			todaysDate = sdf.parse(todaysDateInStr);
			Calendar c = Calendar.getInstance(); 
			c.setTime(todaysDate); 
			c.add(Calendar.DATE, -1);
			fromDate = c.getTime();
			
			Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);
			Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
			
			
			salesList = salesrepo.findSalesListByDate(sqlFromDate, sqlToDate);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		return salesList;
	}
	
	public Map<String, Map> getSoDetailsForChart(String year) throws ParseException{
		String from = "";
		String to = "";
		String yearConst=year;
		Map<String, Map> chartMap = new HashMap();
		Map map = new HashMap();
		for (int i = 0; i <= 11; i++) {
			int month=i+1;
			if(month==1 || month==2 || month==3) {
				int yearInt=Integer.parseInt(yearConst);
				year=String.valueOf(yearInt+1);
			}else {
				year=yearConst;
			}
			int yearInInt=Integer.parseInt(year);
			Month m = Month.of(month);
			if(((yearInInt % 4 == 0) && (yearInInt % 100!= 0)) || (yearInInt % 400 == 0)){
				if (m.length(true) > 30) {
					from = year + "-" + "0" + month + "-" + "01";
					to = year + "-" + "0" + month + "-" + "31";
				}else if(m.length(true) < 30) {
					from = year + "-" + "0" + month + "-" + "01";
					to = year + "-" + "0" + month + "-" + "29";
				} else {
					from = year + "-" + "0" + month + "-" + "01";
					to = year+ "-" + "0" + month + "-" + "30";
				}
			}else {
				if (m.length(true) > 30) {
					from = year + "-" + "0" + month + "-" + "01";
					to = year + "-" + "0" + month + "-" + "31";
				}else if(m.length(true) < 30) {
					from = year + "-" + "0" + month + "-" + "01";
					to = year + "-" + "0" + month + "-" + "28";
				} else {
					from = year + "-" + "0" + month + "-" + "01";
					to = year+ "-" + "0" + month + "-" + "30";
				}
			}
			
			
			Date dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse(from);
			Date dateTo = new SimpleDateFormat("yyyy-MM-dd").parse(to);
			
			Calendar c = Calendar.getInstance();
			Calendar c1 = Calendar.getInstance();
			c.setTime(dateTo);
			c1.setTime(dateFrom);
			// c.add(Calendar.DATE, 0);
			dateTo = c.getTime();
			// c.add(Calendar.DATE, 30); //Last 30 days data
			dateFrom = c1.getTime();
			c.add(Calendar.HOUR_OF_DAY, +23);
			c.add(Calendar.MINUTE, 59);
			dateTo = c.getTime();
			
			Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(dateFrom);
			Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(dateTo);
			System.out.println(sqlFromDate+"&"+sqlToDate);
			
			List<SalesOrder> salesList= salesrepo.findSalesListByDate(sqlFromDate, sqlToDate);
			System.out.println(salesList.size());
			double totalValue=0.0;
			for (SalesOrder salesOrder : salesList) {
				totalValue=totalValue+salesOrder.getTotal();
			}
			map.put(m, Math.round(totalValue * 100.0) / 100.0);
		}
		
		chartMap.put("value", map);
		System.out.println(chartMap);
		return chartMap;
		
	}
	
	public List<SalesOrder> getSoListForTest(){
		List<SalesOrder> salesList = salesrepo.findAll();
		ArrayList<SalesOrder> soList = new ArrayList<>();
		for (SalesOrder salesOrder : salesList) {
			List<SalesItem> soItems=salesOrder.getItems();
			if(soItems.size()>89) {
				soList.add(salesOrder);
			}
		}
		
		return soList;
		
	}

	public List<SalesOrder> findSalesListByClientId(String partyId) {
		List<SalesOrder> salesOrderList = salesrepo.getSalesListByClientId(partyId);
		return null;
	}
	
	public List<ItemMaster> getItemsWithMinQty(){
		List<ItemsWithMinQty> itemsList = itemsWithMinQtyRepo.findAll();
		ArrayList<ItemMaster> items=new ArrayList<ItemMaster>();
		
		for (ItemsWithMinQty itemsWithMinQty : itemsList) {
			float minQty=itemsWithMinQty.getQuantity();
			String itemId=itemsWithMinQty.getItemId();
			Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
			List<Stock> stockList = stockService.getStockList(itemObj.get().getId());
			float stockQty=0;
			for (Stock stock : stockList) {
				stockQty=stockQty+stock.getQuantity();
			}
			if(stockQty<=minQty) {
				items.add(itemObj.get());
			}
		}
		return items;
	}

	public FileEntity storeFile(MultipartFile file,String salesOrderId) throws IOException {
		Optional<FileEntity> fileEntitylist = fileRepository.findBySalesOrderId(salesOrderId);
		
		FileEntity fileEntity;
		 if (fileEntitylist.isPresent()) {
			fileEntity = fileEntitylist.get();
			fileEntity.setFileName(file.getOriginalFilename());
	        fileEntity.setFileType(file.getContentType());
	        fileEntity.setFileSize(file.getSize());
	        fileEntity.setData(file.getBytes());
	        fileEntity.setUploadTime(new Date());
	        fileEntity.setSalesOrderId(salesOrderId);
		 }else {
			fileEntity = new FileEntity();
	        fileEntity.setFileName(file.getOriginalFilename());
	        fileEntity.setFileType(file.getContentType());
	        fileEntity.setFileSize(file.getSize());
	        fileEntity.setData(file.getBytes());
	        fileEntity.setUploadTime(new Date());
	        fileEntity.setSalesOrderId(salesOrderId);
		 }
		
        
        return fileRepository.save(fileEntity);
	}

	public FileEntity getPdfFileBySalesId(String salesOrderId) {
		  Optional<FileEntity> entity= fileRepository.findBySalesOrderId(salesOrderId);
		  return entity.orElseThrow(() -> new RuntimeException("File not found with ID: " + salesOrderId));
	    }
	
	public List<SalesItem> getSalesListByVendorId(List<String> id, String className,String vendorId) {
		// get list of sales order for all sales id
		List<SalesOrder> salesOrderListbyId = salesrepo.findSalesOrderById(id);
		ArrayList<SalesItem> itemList = new ArrayList<SalesItem>();
		// get list of items for each sales order
		List<SalesItem> salesItemList = null;
		for (SalesOrder salesOrder : salesOrderListbyId) {

			if (className.equalsIgnoreCase("dc")) {
				salesItemList = getItemsWhereSupplyPriceIsGreaterThanZero(salesOrder.getId());
				for (SalesItem salesItem : salesItemList) {
					boolean value = false;
					Optional<SalesItem> item=getSalesItemById(salesItem.getId(),value);
					salesItem.set("unitName",item.get().getItem_units().getName());
					List<DesignItems> designItemList = soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
					if(!designItemList.isEmpty()) {
						itemList.add(salesItem);
					}
					List<DeliveryChallanItems> dcItemList = dcService.getDcItemListBySoItemId(salesItem.getId());
					float todaysQty = 0;
					// if for dc item list is empty for the selected salesItemId then set
					// delivered qty to 0.
					if (dcItemList.isEmpty()) {
						todaysQty = 0;
						salesItem.set("todaysQty", todaysQty);
						salesItem.set("deliveredQty", todaysQty);
					} else {
						for (DeliveryChallanItems dcItem : dcItemList) {
							todaysQty = todaysQty + dcItem.getTodaysQty();

						}
						 if (todaysQty == salesItem.getQuantity()) {
							 itemList.remove(salesItem);
						 }
						salesItem.set("todaysQty", salesItem.getQuantity() - todaysQty);
						salesItem.set("deliveredQty", todaysQty);
					}
					
					
				}
				//itemList.addAll(salesItemList);
			} else {
				salesItemList = getSalesItemListByVendorId(salesOrder,vendorId);
				
				for (SalesItem salesItem : salesItemList) {
					boolean value = false;
					Optional<SalesItem> item=getSalesItemById(salesItem.getId(),value);
					salesItem.set("unitName",item.get().getItem_units().getName());
					List<DesignItems> designItemList=soDesignService.getSalesOrderDesignItemListBySalesItemId(salesItem.getId());
					ArrayList<DesignItems> vendoritemsList = new ArrayList<DesignItems>();
					for (DesignItems designItems : designItemList) {
						String designItemId=(String) designItems.get("itemMasterId");
						String itemId=designItemId;
						Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
						designItems.set("tax",itemObj.get().getGst());
						designItems.set("itemName",itemObj.get().getItemName());
						designItems.set("unit",itemObj.get().getItem_units().getName());
						designItems.set("hsnCode",itemObj.get().getHsnCode());
						designItems.set("model",itemObj.get().getModel());
						List<Supplier> supplierList=itemService.findItemsForSelectedVendor(designItemId,className);
						if(supplierList.size()>0) {
							vendoritemsList.add(designItems);
						}
					}
					salesItem.set("designItems", designItemList);
					salesItem.set("soNum",salesItem.getSalesOrder().getId());
					salesItem.set("clientPo",salesItem.getSalesOrder().getClientPoNumber());
					salesItem.set("client",salesItem.getSalesOrder().getParty().getPartyName());
					salesItem.set("vendoritemsList", vendoritemsList);
				}
				itemList.addAll(salesItemList);
			}
		}
		
		Collections.sort(itemList);
		return itemList;
	}

	private List<SalesItem> getSalesItemListByVendorId(SalesOrder salesOrder, String vendorId) {
		List<SalesItem> salesItemList=salesOrder.getItems();
		List<SalesItem> itemList=new ArrayList<SalesItem>();
		for (SalesItem salesItem : salesItemList) {
			List<DesignItems> designItemList=soDesignService.getAllDesignItemListBySOItemId(salesItem.getId());
			List<Supplier> supplierList=new ArrayList<Supplier>();
			for (DesignItems designItem : designItemList) {
				 supplierList=itemService.findItemsForSelectedVendor(designItem.getItemId(), vendorId);
			}
			if(!supplierList.isEmpty()) {
				itemList.add(salesItem);
			}
		}
		return itemList;
	}
	
	public SalesItem getSalesItemByName(String description,String clientPoNum) {
		SalesOrder salesOrder=salesrepo.getSalesOrderByClientPoNumber(clientPoNum);
		SalesItem salesItemObj = null;
		if(salesOrder!=null) {
		List<SalesItem> salesItemList= getSalesItemsBySalesOrderId(salesOrder.getId());
		
		for (SalesItem salesItem : salesItemList) {
			if(salesItem.getDescription().equalsIgnoreCase(description)){
				salesItemObj=salesItemrepo.findSalesItemByDescription(description);
			}
		}
		}
		return salesItemObj;
	}
	
	public SalesItem getSalesItemByName(String description, String clientPoNum, String itemId) {
	    // 1. Fetch all SalesItems matching the description and clientPoNum
	    List<SalesItem> salesItems = salesItemrepo.findByDescriptionAndClientPoNumber(description, clientPoNum);

	    for (SalesItem salesItem : salesItems) {
	        String salesItemId = salesItem.getId();

	        // 2. Find designs linked to this SalesItem
	        List<DesignItems> designItemList = soDesignService.getAllDesignItemListBySOItemId(salesItemId);

	        
	            // 4. Check if any design item matches the itemId
	            for (DesignItems designItem : designItemList) {
	                if (designItem.getItemId().equals(itemId)) {
	                    // ✅ Found matching SalesItem
	                    return salesItem;
	                }
	            
	        }
	    }

	    // ❌ No match found
	    return null;
	}
	
	 public int archiveSalesList(List<String> ids) {
	       for (String soId : ids) {
	    	   archiveSO(soId);
	       }
		return ids.size();
	   }

	    public List<String> readIdsFromExcel(MultipartFile file) throws IOException {
	        List<String> ids = new ArrayList<>();
	        Workbook workbook = new XSSFWorkbook(file.getInputStream());
	        Sheet sheet = workbook.getSheetAt(0);

	        for (Row row : sheet) {
	            if (row.getRowNum() == 0) continue; // Skip header row
	            Cell cell = row.getCell(0); // Assuming 'id' is in the first column
	            if (cell != null) {
	                ids.add(cell.getStringCellValue());
	            }
	        }

	        workbook.close();
	        return ids;
	    }

		
		public List<SalesOrder> getAllSalesItemListWithDesignForDashboardPartial() {
			List<SalesOrder> salesOrderList = salesrepo.getSalesOrderWithDesign();
			Collections.sort(salesOrderList);
			ArrayList<SalesOrder> list=new ArrayList<SalesOrder>();
			if(salesOrderList.size()>11) {
				for (int i = salesOrderList.size()-1; i > salesOrderList.size()-11; i--) {
					list.add(salesOrderList.get(i));
				}
			}else {
				list.addAll(salesOrderList);
			}
			
			return list;
		}

}
