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
import com.ncpl.sales.repository.SalesOrderDesignRepo;
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

	@Autowired
	SalesOrderDesignRepo salesOrderDesignRepo;

	@Transactional(rollbackFor = Exception.class)
	public void saveTds(Tds tds, HttpServletRequest req) throws IOException {
		if (tds.getSoNumber() != null) {
			// Snapshot existing approved items before delete so they can be
			// restored if the form submission didn't include them.
			Map<String, TdsItems> existingApproved = new HashMap<>();
			List<Tds> existingTdsList = tdsRepo.getTdsListBySoNumber(tds.getSoNumber());
			if (!existingTdsList.isEmpty() && existingTdsList.get(0).getItems() != null) {
				for (TdsItems ei : existingTdsList.get(0).getItems()) {
					if (ei.isTdsApproved() && ei.getSiteQuantity() > 0) {
						existingApproved.put(tdsItemKey(ei), ei);
					}
				}
			}

			tdsRepo.deleteLotsBySoNumber(tds.getSoNumber());
			tdsRepo.deleteItemsBySoNumber(tds.getSoNumber());
			tdsRepo.deleteTdsBySoNumber(tds.getSoNumber());
			tdsRepo.flush();

			// Track which approved items are being submitted right now
			Set<String> submittedApprovedKeys = new HashSet<>();
			if (tds.getItems() != null) {
				for (TdsItems item : tds.getItems()) {
					if (item.isTdsApproved()) submittedApprovedKeys.add(tdsItemKey(item));
				}
			}

			// Re-add previously approved items that weren't in this submission
			for (Map.Entry<String, TdsItems> entry : existingApproved.entrySet()) {
				if (!submittedApprovedKeys.contains(entry.getKey())) {
					TdsItems prev = entry.getValue();
					TdsItems copy = new TdsItems();
					copy.setDescription(prev.getDescription());
					copy.setModelNumber(prev.getModelNumber());
					copy.setDesignQty(prev.getDesignQty());
					copy.setTdsApproved(true);
					copy.setSiteQuantity(prev.getSiteQuantity());
					copy.setTds(tds);
					List<Lot> lotCopies = new ArrayList<>();
					if (prev.getLots() != null) {
						for (Lot l : prev.getLots()) {
							Lot lc = new Lot();
							lc.setLotNumber(l.getLotNumber());
							lc.setQuantity(l.getQuantity());
							lc.setRemarks(l.getRemarks());
							lc.setTdsItems(copy);
							lotCopies.add(lc);
						}
					}
					copy.setLots(lotCopies);
					if (tds.getItems() == null) tds.setItems(new ArrayList<>());
					tds.getItems().add(copy);
				}
			}
		}
		if (tds.getItems() != null) {
			for (TdsItems tdsItem : tds.getItems()) {
				tdsItem.setTds(tds);
				tdsItem.setTdsItemId(0);
				float totalQty = 0;
				if (tdsItem.getLots() != null) {
					for (Lot lot : tdsItem.getLots()) {
						if (lot.getQuantity() == null) {
							lot.setQuantity(0f);
						}
						totalQty += lot.getQuantity();
						lot.setTdsItems(tdsItem);
						lot.setLotId(0);
					}
				}
				if (tdsItem.isTdsApproved()) {
					if (salesOrderDesignRepo.getDesginListBySoItemId(tdsItem.getDescription()).isEmpty()) {
						throw new RuntimeException("TDS cannot be approved for item \"" + tdsItem.getDescription()
								+ "\" because no design is added for it.");
					}
					// The form posts designQty only for rows built from design items;
					// rows without design items post 0, so fall back to the SO item qty.
					float maxQty = tdsItem.getDesignQty();
					String itemName = tdsItem.getDescription();
					Optional<SalesItem> salesItem = salesService.getSalesItemObjById(tdsItem.getDescription());
					if (salesItem.isPresent()) {
						if (maxQty <= 0) {
							maxQty = salesItem.get().getQuantity();
						}
						if (salesItem.get().getDescription() != null && !salesItem.get().getDescription().isEmpty()) {
							itemName = salesItem.get().getDescription();
						}
					}
					if (maxQty > 0 && totalQty > maxQty) {
						throw new RuntimeException("Lot quantity (" + (int) totalQty + ") exceeds allowed quantity (" + (int) maxQty + ") for item: " + itemName);
					}
					tdsItem.setSiteQuantity(totalQty);
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
		// if(tdsItemsList.size()>0) {
		//
		// 	String soNum = tdsObj.getSoNumber();
		// 	Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(soNum);
		// 	SalesOrder so = salesOrder.get();
		// 	Party party = null;
		// 	if (so.getShippingAddress() != null) {
		// 		party = partyRepo.findById(so.getShippingAddress());
		// 	}
		// 	if (party == null && so.getParty() != null) {
		// 		party = so.getParty();
		// 	}
		// 	new TdsApproved().buildExcelDocument(tdsObj, filePath,salesService,salesOrder,req,party,itemService);
		// 	Map<String, Object> emailContents = null;
		// 	String partyName = salesOrder.get().getParty() != null ? salesOrder.get().getParty().getPartyName() : "";
		// 	emailContents = tdsDetails(salesOrder.get().getClientPoNumber(), salesOrder.get().getClientPoDate(),
		// 				partyName);
		// 	emailService.sendEmailToServer(emailContents);
		// }

		// Generate Site Quantity Report for all TDS items and send email
		String soNum = tdsObj.getSoNumber();
		if (soNum != null) {
			Optional<SalesOrder> so = salesService.getSalesOrderById(soNum);
			if (so.isPresent()) {
				SalesOrder salesOrderObj = so.get();
				String siteFileName = new FileNameGenerator().generateFileNameAsDate() + "site_quantity_and_lot_number.xlsx";
				String siteFilePath = Constants.FILE_LOCATION + File.separator + siteFileName;
				tdsLotUpdateReportService.generateReport(salesOrderObj, tdsObj, siteFilePath);

				Map<String, Object> siteEmailContents = new HashMap<>();
				String partyName = salesOrderObj.getParty() != null ? salesOrderObj.getParty().getPartyName() : "";
				String dateFormatting = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
				if (salesOrderObj.getClientPoDate() != null) {
					dateFormatting = new SimpleDateFormat("dd-MM-yyyy").format(salesOrderObj.getClientPoDate());
				}
				float totalSiteQty = 0;
					java.util.TreeSet<String> lotNums = new java.util.TreeSet<>();
					for (TdsItems ti : tdsObj.getItems()) {
						if (ti.isTdsApproved()) {
							totalSiteQty += ti.getSiteQuantity();
							if (ti.getLots() != null) {
								for (Lot l : ti.getLots()) {
									if (l.getLotNumber() != null && !l.getLotNumber().isEmpty()) {
										lotNums.add(l.getLotNumber());
									}
								}
							}
						}
					}
					String subject = "Site Quantity and Lot Number for " + salesOrderObj.getClientPoNumber()
							+ " | Qty: " + (int) totalSiteQty
							+ " | Lot(s): " + (lotNums.isEmpty() ? "-" : String.join(", ", lotNums));
					siteEmailContents.put("subject", subject);
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
	
	public List<SalesOrder> getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboard(){
		
		ArrayList<SalesOrder> soList = salesrepo.getTdsApprovedAndPoNotDoneListDashboard();
		return soList;
		
	}

	private String tdsItemKey(TdsItems item) {
		return item.getDescription() + "|" + (item.getModelNumber() != null ? item.getModelNumber() : "");
	}

	public List<SalesOrder> getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboardPartial() {
		// Same canonical query as the full list and the dashboard tile count
		// (SalesRepo#getTdsApprovedAndPoNotDoneListDashboard / DashboardAggregateJdbcRepository),
		// just capped for the dashboard preview so all three can never disagree.
		List<SalesOrder> soList = getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboard();
		return soList.size() > 10 ? soList.subList(0, 10) : soList;
	}

}
