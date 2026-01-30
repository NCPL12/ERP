package com.ncpl.sales.service;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.common.Constants;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Tds;
import com.ncpl.sales.model.TdsItems;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesRepo;
import com.ncpl.sales.repository.TdsItemRepo;
import com.ncpl.sales.repository.TdsRepo;

@Service
public class TdsService {
	
	FileNameGenerator fileNameGenerator = new FileNameGenerator();
	String fileName = fileNameGenerator.generateFileNameAsDate() + "tds_approved.xlsx";
	String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	
	@Autowired
	TdsRepo tdsRepo;
	
	@Autowired
	SalesService salesService;
	@Autowired
	EmailService emailService;
	@Autowired
	PartyRepo partyRepo;
	@Autowired
	TdsItemRepo tdsItemRepo;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	SalesRepo salesrepo;
	
	@Autowired
	ItemMasterService itemService;

	public void saveTds(Tds tds, HttpServletRequest req) throws IOException {	
		Tds tdsObj=tdsRepo.save(tds);
		List<TdsItems> tdsItems = tdsObj.getItems();
		ArrayList<TdsItems> tdsItemsList = new ArrayList<TdsItems>();
		for (TdsItems tdsItem : tdsItems) {
			if(tdsItem.isTdsApproved()==true) {
				tdsItemsList.add(tdsItem);
			}
		}
		if(tdsItemsList.size()>0) {
			
			String soNum = tdsObj.getSoNumber();
			Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(soNum);
			String shippingPartyId=salesOrder.get().getShippingAddress();
			 Party party=partyRepo.findById(shippingPartyId);
			new TdsApproved().buildExcelDocument(tdsObj, filePath,salesService,salesOrder,req,party,itemService);
			Map<String, Object> emailContents = null;
			emailContents = tdsDetails(salesOrder.get().getClientPoNumber(), salesOrder.get().getClientPoDate(),
					salesOrder.get().getParty().getPartyName());
			emailService.sendEmailToServer(emailContents);
		}

	}

	private Map<String, Object> tdsDetails(String clientPoNumber, Date clientPoDate, String partyName) {
		//String s = formatLakh(clientPoValue);
		DecimalFormat df = new DecimalFormat("#,###.00");
		Locale indiaLocale = new Locale("en", "IN");
		NumberFormat india = NumberFormat.getCurrencyInstance(indiaLocale);
		String dateFormatting = new SimpleDateFormat("dd-MM-yyyy").format(clientPoDate);
		Map<String, Object> emailContents = new HashMap<String, Object>();
		emailContents.put("subject", "Tds Approved for " + clientPoNumber);
		emailContents.put("template", "tds-approved.html");
		emailContents.put("clientPo", clientPoNumber);
		emailContents.put("clientPoDate", dateFormatting);
		emailContents.put("partyName", partyName);
		emailContents.put("to1", "vighneshwar@ncpl.co");
		emailContents.put("to2", "ramsy@ncpl.co");
		emailContents.put("cc1", "purchase@ncpl.co");
		emailContents.put("cc2", "surendra@ncpl.co");
		emailContents.put("cc3", "prasadini@ncpl.co");
		emailContents.put("month", Constants.currentDate()); 
		emailContents.put("attachment", filePath); 
		return emailContents;
	}
	
	public List<TdsItems> getTdsItemsListWhereTdsApprovedAndPoNotDone(){
		List<TdsItems> tdsItemsList = tdsItemRepo.findAll();
		ArrayList<TdsItems> tdsItemList = new ArrayList<TdsItems>();
		for (TdsItems tdsItem : tdsItemsList) {
			if(tdsItem.isTdsApproved()==true && tdsItem.getSiteQuantity()>0) {
				String salesItemId=tdsItem.getDescription();
				Optional<SalesItem> salesItemObj=salesService.getSalesItemObjById(salesItemId);
				String itemId = tdsItem.getModelNumber();
				Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
				List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(salesItemId, itemId);
				if(poItemList.size()==0) {
					tdsItemList.add(tdsItem);
				}
				tdsItem.set("clientpoNum",salesItemObj.get().getSalesOrder().getClientPoNumber());
				tdsItem.set("salesOrderObj",salesItemObj.get().getSalesOrder());
				tdsItem.set("modelNum",itemObj.get().getModel());
				tdsItem.set("client",salesItemObj.get().getSalesOrder().getParty().getPartyName());
				tdsItem.set("createdDt",salesItemObj.get().getSalesOrder().getCreated());
				tdsItem.set("desc",salesItemObj.get().getDescription());
				tdsItem.set("slNo",salesItemObj.get().getSlNo());
			}
		}
		return tdsItemList;
		
	}
	
	/*@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<SalesOrder> getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboard(){
		List<TdsItems> tdsItemsList = tdsItemRepo.findAll();
		Set set = new HashSet();
		for (TdsItems tdsItem : tdsItemsList) {
			if(tdsItem.isTdsApproved()==true && tdsItem.getSiteQuantity()>0) {
				String salesItemId=tdsItem.getDescription();
				Optional<SalesItem> salesItemObj=salesService.getSalesItemObjById(salesItemId);
				String itemId = tdsItem.getModelNumber();
				List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(salesItemId, itemId);
				if(poItemList.size()==0) {
					set.add(salesItemObj.get().getSalesOrder());
				}
				
			}
		}
		ArrayList<SalesOrder> soList = new ArrayList<SalesOrder>(set);
		return soList;
		
	}*/
	
	public List<SalesOrder> getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboard(){
		
		ArrayList<SalesOrder> soList = salesrepo.getTdsApprovedAndPoNotDoneListDashboard();
		return soList;
		
	}

	public List<SalesOrder> getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboardPartial() {
		List<TdsItems> tdsItemsList = tdsItemRepo.findAll();
		Set set = new HashSet();
		for (TdsItems tdsItem : tdsItemsList) {
			if(set.size()<10) {
			if(tdsItem.isTdsApproved()==true && tdsItem.getSiteQuantity()>0) {
				String salesItemId=tdsItem.getDescription();
				Optional<SalesItem> salesItemObj=salesService.getSalesItemObjById(salesItemId);
				String itemId = tdsItem.getModelNumber();
				List<PurchaseItem> poItemList = purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(salesItemId, itemId);
				if(poItemList.size()==0) {
					set.add(salesItemObj.get().getSalesOrder());
				}
			}
				
			}
		}
		ArrayList<SalesOrder> soList = new ArrayList<SalesOrder>(set);
		return soList;
	}

}
