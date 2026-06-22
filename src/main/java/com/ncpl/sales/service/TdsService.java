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
import org.springframework.transaction.annotation.Transactional;

import com.ncpl.common.Constants;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Lot;
import com.ncpl.sales.model.Tds;
import com.ncpl.sales.model.TdsItems;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesRepo;
import com.ncpl.sales.repository.TdsItemRepo;
import com.ncpl.sales.repository.TdsRepo;
import com.ncpl.sales.service.TdsLotUpdateReportService;

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

	@Autowired
	TdsLotUpdateReportService tdsLotUpdateReportService;

	@Transactional(rollbackFor = Exception.class)
	public void saveTds(Tds tds, HttpServletRequest req) throws IOException {
		if (tds.getSoNumber() != null) {
			tdsRepo.deleteLotsBySoNumber(tds.getSoNumber());
			tdsRepo.deleteItemsBySoNumber(tds.getSoNumber());
			tdsRepo.deleteTdsBySoNumber(tds.getSoNumber());
			tdsRepo.flush();
		}
		if (tds.getItems() != null) {
			for (TdsItems tdsItem : tds.getItems()) {
				tdsItem.setTds(tds);
				tdsItem.setTdsItemId(0);
				if (tdsItem.getLots() != null) {
					float totalQty = 0;
					for (Lot lot : tdsItem.getLots()) {
						totalQty += lot.getQuantity();
						lot.setTdsItems(tdsItem);
						lot.setLotId(0);
					}
					if (tdsItem.isTdsApproved()) {
						float designQty = tdsItem.getDesignQty();
						if (totalQty > designQty) {
							throw new RuntimeException("Lot quantity (" + (int) totalQty + ") exceeds Design quantity (" + (int) designQty + ") for item: " + tdsItem.getDescription());
						}
					}
				}
			}
		}
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
			SalesOrder so = salesOrder.get();
			Party party = null;
			if (so.getShippingAddress() != null) {
				party = partyRepo.findById(so.getShippingAddress());
			}
			if (party == null && so.getParty() != null) {
				party = so.getParty();
			}
			new TdsApproved().buildExcelDocument(tdsObj, filePath,salesService,salesOrder,req,party,itemService);
			Map<String, Object> emailContents = null;
			String partyName = salesOrder.get().getParty() != null ? salesOrder.get().getParty().getPartyName() : "";
			emailContents = tdsDetails(salesOrder.get().getClientPoNumber(), salesOrder.get().getClientPoDate(),
					partyName);
			emailService.sendEmailToServer(emailContents);
		}

		// Generate Site Quantity Report for all TDS items and send email
		String soNum = tdsObj.getSoNumber();
		if (soNum != null) {
			Optional<SalesOrder> so = salesService.getSalesOrderById(soNum);
			if (so.isPresent()) {
				SalesOrder salesOrderObj = so.get();
				String siteFileName = new FileNameGenerator().generateFileNameAsDate() + "site_qty_report.xlsx";
				String siteFilePath = Constants.FILE_LOCATION + File.separator + siteFileName;
				tdsLotUpdateReportService.generateReport(salesOrderObj, tdsObj, siteFilePath);

				Map<String, Object> siteEmailContents = new HashMap<>();
				String partyName = salesOrderObj.getParty() != null ? salesOrderObj.getParty().getPartyName() : "";
				String dateFormatting = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				if (salesOrderObj.getClientPoDate() != null) {
					dateFormatting = new SimpleDateFormat("dd-MM-yyyy").format(salesOrderObj.getClientPoDate());
				}
				siteEmailContents.put("subject", "Site Quantity Report for " + salesOrderObj.getClientPoNumber());
				siteEmailContents.put("template", "site_qty_report.html");
				siteEmailContents.put("clientPo", salesOrderObj.getClientPoNumber());
				siteEmailContents.put("clientPoDate", dateFormatting);
				siteEmailContents.put("partyName", partyName);
				siteEmailContents.put("to1", "purchase@ncpl.co");
				siteEmailContents.put("to2", "vighneshwar@ncpl.co");
				siteEmailContents.put("cc1", "design@ncpl.co");
				siteEmailContents.put("cc2", "sagar.chandrashekar@ncpl.co");
				siteEmailContents.put("cc3", "hariharan@ncpl.co");
				siteEmailContents.put("month", Constants.currentDate());
				siteEmailContents.put("attachment", siteFilePath);
				emailService.sendEmailToServer(siteEmailContents);
			}
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
