package com.ncpl.sales.controller;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.itextpdf.text.DocumentException;
import com.ncpl.sales.generator.FileNameGenerator;
import com.ncpl.sales.model.CompanyAssets;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.EmployeeMaster;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.InvoiceCopy;
import com.ncpl.sales.model.InvoiceItem;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Make;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.PurchaseCopy;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.model.Supplier;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.InvoiceCopyRepo;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserService;
import com.ncpl.sales.service.CompanyAssetService;
import com.ncpl.sales.service.DeliveryChallanExcel;
import com.ncpl.sales.service.DeliveryChallanExcelForAll;
import com.ncpl.sales.service.DeliveryChallanService;
import com.ncpl.sales.service.EmployeeService;
import com.ncpl.sales.service.GrnExcel;
import com.ncpl.sales.service.GrnService;
import com.ncpl.sales.service.InvCopyService;
import com.ncpl.sales.service.InvoicePdf;
import com.ncpl.sales.service.InvoiceService;
import com.ncpl.sales.service.ItemMasterService;
import com.ncpl.sales.service.MakeService;
import com.ncpl.sales.service.PartyAddressService;
import com.ncpl.sales.service.PartyService;
import com.ncpl.sales.service.PurchaseCopyService;
import com.ncpl.sales.service.PurchaseItemService;
import com.ncpl.sales.service.PurchaseOrderCustomProperty;
import com.ncpl.sales.service.PurchaseOrderService;
import com.ncpl.sales.service.PurchasePdf;
import com.ncpl.sales.service.PurchaseUploadExcel;
import com.ncpl.sales.service.SalesOrderDesignService;
import com.ncpl.sales.service.SalesService;
import com.ncpl.sales.service.StockService;
import com.ncpl.sales.service.invoiceExcel;
//import com.ncpl.sales.service.invoiceExcel;
import com.ncpl.sales.service.purchaseOrderExcel;
import com.ncpl.sales.util.NcplUtil;

import pl.allegro.finance.tradukisto.MoneyConverters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Controller
public class PurchaseController {

	@Autowired
	PurchaseOrderService purchaseService;
	@Autowired
	NcplUtil utilService;
	@Autowired
	SalesService salesService;
	@Autowired
	PartyService partyService;
	@Autowired
	PartyRepo partyRepo;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	StockService stockService;
	@Autowired
	PurchaseOrderCustomProperty customProperty;
	@Autowired
	PartyAddressService partyAddressService;
	@Autowired
	GrnService grnService;
	@Autowired
	PurchaseCopyService purchasecopyservice;
	@Autowired
	DeliveryChallanService deliveryChallanService;
	@Autowired
	InvoiceService invoiceService;
	@PersistenceContext
	private EntityManager em;
	@Autowired
	InvoiceCopyRepo invCopyRepo;
	@Autowired
	InvCopyService invCopyService;
	@Autowired
	UserService userService;
	@Autowired
	SalesOrderDesignService salesOrderDesignService;
	@Autowired
	MakeService makeService;
	@Autowired
	PurchaseUploadExcel purchaseUploadExcel;
	@Autowired
	CompanyAssetService companyAssetService;
	@Autowired
	EmployeeService employeeService;

	//@Autowired
	//@Qualifier("reportExecutor")
	//private Executor asyncExecutor;
	
	String pofileName =null;
//	@Autowired
//	InvoicePdf invPdf;

	@GetMapping("/purchase")
	public String purchaseDashBoard(Model model) throws JsonProcessingException {
		User userObj  = userService.getCurrentUser();
		String role = userObj.getRole();
		List<PurchaseOrder> poList = purchaseService.findAllPO();
		List<Party> partyList = partyService.getPartyListbyTypeSupplier(); 
		List<ItemMaster> itemList = itemMasterService.getItemList();
		ObjectMapper mapper = utilService.getObjectMapper();
		model.addAttribute("poList", mapper.writeValueAsString(poList));
		model.addAttribute("role", mapper.writeValueAsString(role));
		model.addAttribute("user", mapper.writeValueAsString(userObj.getUsername()));
		model.addAttribute("pageHeader", "Purchase Orders");
		model.addAttribute("partyList", mapper.writeValueAsString(partyList));
		model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		return "purchaseDashboard";
	}

	
	@GetMapping("/purchaseOrder")
	 public String purchaseOrder(Model model) throws JsonProcessingException {
		 ObjectMapper mapper= new ObjectMapper();
		 User userObj  = userService.getCurrentUser();
		 String role = userObj.getRole();
		 List<SalesOrder> salesList = salesService.findAllSalesOrderList();
		 List<ItemMaster> itemList=itemMasterService.getItemList();
		 List<Units> unitsList = itemMasterService.getUnitList();
		 model.addAttribute("salesOrderList",mapper.writeValueAsString(salesList));
		 List<Party> partyList=partyService.getPartyListbyTypeSupplier();
		 model.addAttribute("pageHeader", "Purchase Order");
		 model.addAttribute("role", mapper.writeValueAsString(role));
		 model.addAttribute("unitsList",mapper.writeValueAsString(unitsList));
		 model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		 model.addAttribute("partyList",mapper.writeValueAsString(partyList));
		 return "purchaseOrder";
	 }

	@PostMapping("/save/purchaseOrder")
	public  String savePurchaseOrder(PurchaseOrder purchaseOrder,HttpServletRequest req) {
		System.out.println(purchaseOrder);
	String salesOrderId = req.getParameter("salesOrder");
	String partyId = req.getParameter("partyByType");
	//String poNumber = req.getParameter("poNumber");
	if(purchaseOrder.getPoNumber()!=null){
		purchaseService.updatePo(purchaseOrder);
	}else{
	purchaseService.savePurchaseOrder(purchaseOrder,salesOrderId,partyId);
	}
	//System.out.println(purchaseOrder.getItems().size());
	return "redirect:/purchase";
	

  }   
		
	// Get all purchase Orders
	public List<PurchaseOrder> findAll() {
		List<PurchaseOrder> poList = purchaseService.findAllPO();
		
		return poList;
	}

	
	//display purchace items in edit page
	@GetMapping("/purchase/view")
	public String displayEditPurchaseOrder(@RequestParam("poNumber") String poNumber,
		@RequestParam("version") String version,@RequestParam("versionIndex") String versionIndex, Model model) throws JsonProcessingException {
		
		
		
		//Get purchase order by version
		Optional<PurchaseOrder> purchaseOrder = purchaseService.findByIdAndVersion(poNumber, version,versionIndex);
		//model.addAttribute("currentVersion", version);
	    //To display description as Name in Edit po Page sending sales list and
		// comparing with the id we are getting in edit page
		User userObj  = userService.getCurrentUser();
		String role = userObj.getRole();
		List<SalesItem> salesItemList = salesService.getAllSalesItemList();
		 List<ItemMaster> itemList=itemMasterService.getItemList();
		List<Party> partyList = partyService.getPartyListbyTypeSupplier();
		 List<Units> unitsList = itemMasterService.getUnitList();
		ObjectMapper mapper = utilService.getObjectMapper();
		model.addAttribute("purchaseOrderObj",mapper.writeValueAsString(purchaseOrder.get().getItems()));
		model.addAttribute("partyList", mapper.writeValueAsString(partyList));
		model.addAttribute("salesItemList", mapper.writeValueAsString(salesItemList));
		 model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		 model.addAttribute("unitsList",mapper.writeValueAsString(unitsList));
		
		//Get latest version
		Optional<PurchaseOrder> purchaseOrderLatest = purchaseService.findById(poNumber);
		model.addAttribute("role", mapper.writeValueAsString(role));
		model.addAttribute("purchaseOrder", purchaseOrderLatest.get());
		model.addAttribute("poLastVersion", purchaseOrderLatest.get().getVersion());
		model.addAttribute("version", version);
		model.addAttribute("partyId", purchaseOrder.get().getParty().getId());
		model.addAttribute("poNumber", poNumber);

		
		List<SalesOrder> salesList = salesService.getSalesOrderList();
		 model.addAttribute("salesOrderList",mapper.writeValueAsString(salesList));

		return "displayPurchaseOrder";
	}


	// Api For generating excel/Pdf
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/purchaseOrder/details/{json}")
	 
		public ModelAndView purchaseOrder(HttpServletRequest request,Model model,
			@PathVariable("json") String json) throws ParseException {
		
		
		Gson g = new Gson();
		Map<String, String> map =  g.fromJson(json, Map.class);
		
		//Field required for storing into purchase copy
		String shippingAddressKey = map.get("shippingAddressId");
		String billingAddressKey = map.get("billingAddressId");
		String billingAddress = null;
		String shippingAddress = null;
		String vendorAddress =null;
		
		
	
		//For Getting Billing Address and Shipping address
		String billingAddressesWithDelimiter = customProperty.getBillingAddress();
		String [] billingAddressesArray = billingAddressesWithDelimiter.split("\\^\\^");
		
		for(int i=0;i<billingAddressesArray.length;i++) {
			String[] singleAddressArr = billingAddressesArray[i].split("\\$\\$");
			String key = singleAddressArr[0].replace("\n", "").trim();
			if(key.equalsIgnoreCase(billingAddressKey)) {
				billingAddress = billingAddressesArray[i].replaceAll("\\$\\$", ",").replace(key+",", "").trim();
			}
		}
		billingAddress =billingAddress.replace(",Karnataka", "");
		billingAddress =billingAddress.replace(",India", "");
		if(billingAddress.contains("Mangalore")){
			billingAddress = billingAddress.replace("Sri Ganesh Kripa,", "Sri Ganesh Kripa,");
			billingAddress = billingAddress.replace("Venkatesh Sadana", "Venkatesh Sadana,");
			billingAddress = billingAddress.replace(",575015", "");
			billingAddress=  billingAddress.replace(",Near Ganapathi Temple", "Near Ganapathi Temple,");
			billingAddress=  billingAddress.replace(",Perlaguri", "");
			billingAddress=  billingAddress.replace(",Kavoor Post", "Perlaguri Kavoor Post");
			billingAddress = billingAddress.replace("Mangalore", "Mangalore 575015");
		}
		else{
			billingAddress =billingAddress.replace("560046", "");
			billingAddress = billingAddress.replace("Bengaluru", "Bangalore 560046");
		}
		billingAddress =billingAddress.replace(",", "\n");
		
		
		if(shippingAddressKey.contains("key")) {
			String shippingAddressesWithDelimiter = customProperty.getShippingAddress();
			String [] shippingAddressesArray = shippingAddressesWithDelimiter.split("\\^\\^");

			for(int i=0;i<shippingAddressesArray.length;i++) {
				String[] singleShippingAddressArr = shippingAddressesArray[i].split("\\$\\$");
				String key = singleShippingAddressArr[0].replace("\n", "").trim();
				if(key.equalsIgnoreCase(shippingAddressKey)) {
					shippingAddress = shippingAddressesArray[i].replaceAll("\\$\\$", ",").replace(key+",", "").trim();
				}
			}
			shippingAddress =shippingAddress.replace(",Karnataka", "");
			shippingAddress =shippingAddress.replace(",India", "");
			if(shippingAddress.contains("Mangalore")){
				shippingAddress = shippingAddress.replace("Sri Ganesh Kripa,", "Sri Ganesh Kripa,");
				shippingAddress = shippingAddress.replace("Venkatesh Sadana", "Venkatesh Sadana,");
				shippingAddress = shippingAddress.replace(",575015", "");
				shippingAddress=  shippingAddress.replace(",Near Ganapathi Temple", "Near Ganapathi Temple,");
				shippingAddress=  shippingAddress.replace(",Perlaguri", "");
				shippingAddress=  shippingAddress.replace(",Kavoor Post", "Perlaguri Kavoor Post");
				shippingAddress = shippingAddress.replace("Mangalore", "Mangalore 575015");
			}
			else{
				shippingAddress =shippingAddress.replace("560046", "");
				shippingAddress = shippingAddress.replace("Bengaluru", "Bangalore 560046");
			}
			shippingAddress =shippingAddress.replace(",", "\n");
		}else {
			String shippingAddressId = map.get("shippingAddressId");

			if(shippingAddressId.contains("$")) {
				shippingAddressId  = shippingAddressId.replace("$", "/");
			}
			Party altshippingParty = partyRepo.findById(shippingAddressId);
			request.setAttribute("altshippingParty", altshippingParty);

			if(altshippingParty!= null) {
				String addr2 = altshippingParty.getAddr2();
				String gst = altshippingParty.getGst();
				String contactNo = altshippingParty.getPhone1();
				String pincode=altshippingParty.getPin();
				if(pincode==null) {
					pincode="";
				}
				if(contactNo == null || contactNo ==""){
					contactNo="NA";
				}
				if(gst == null || gst ==""){
					gst="";
				}
				if(addr2 == null || addr2.equalsIgnoreCase("")) {
					//addr2 = "";
					shippingAddress = altshippingParty.getPartyName()+","+altshippingParty.getAddr1()+","+altshippingParty.getParty_city().getName()+","+altshippingParty.getParty_city().getCode()+" " +pincode+"<br>"+"Contact: "+contactNo
							+"<br>"+" Email: "+altshippingParty.getEmail1();
				}else{
					shippingAddress = altshippingParty.getPartyName()+","+altshippingParty.getAddr1()+","+addr2+","+altshippingParty.getParty_city().getName()+" " +pincode+"<br>"+"Contact: "+contactNo
							+"<br>"+" Email: "+altshippingParty.getEmail1();
				}
			}
		}

		
		
		
		String poNumber = map.get("poNumber");
		String vendorAddressId = map.get("vendorAddressId");

		if(vendorAddressId.contains("$")) {
			vendorAddressId  = vendorAddressId.replace("$", "/");
		}
		

		Party party = partyRepo.findById(vendorAddressId);
		request.setAttribute("party", party);
		
		Optional<PartyAddress> partyAddress = partyAddressService.getAddressByAddressId(vendorAddressId);
		request.setAttribute("partyAddress", partyAddress);
		
		//For saving into purchaseCopy Table
		PurchaseCopy purchaseCopyObj = new PurchaseCopy();
		
		//For Storing Vendor Address into purchasecopy table
		
		if(party!= null) {
			String addr2 = party.getAddr2();
			String gst = party.getGst();
			String contactNo = party.getPhone1();
			String pincode=party.getPin();
			if(pincode==null) {
				pincode="";
			}
			if(contactNo == null || contactNo ==""){
				contactNo="NA";
			}
			if(gst == null || gst ==""){
				gst="";
			}
			if(addr2 == null || addr2.equalsIgnoreCase("")) {
				//addr2 = "";
				vendorAddress = party.getPartyName()+","+party.getAddr1()+","+party.getParty_city().getName()+","+party.getParty_city().getCode()+" " +pincode+"<br>"+"Contact: "+contactNo
				+"<br>"+" GSTIN: "+gst+"<br>"+"Email: "+party.getEmail1();
			}else{
			vendorAddress = party.getPartyName()+","+party.getAddr1()+","+addr2+","+party.getParty_city().getName()+" " +pincode+"<br>"+"Contact: "+contactNo
			+"<br>"+" GSTIN: "+gst+"<br>"+"Email: "+party.getEmail1();
			}
		}
		User user=userService.getCurrentUser();
		
		purchaseCopyObj.setVendorAddress(vendorAddress);
		purchaseCopyObj.setBillingAddress(billingAddress);
		purchaseCopyObj.setDeliveryAddress(shippingAddress);
		purchaseCopyObj.setPoNumber(poNumber);
		purchaseCopyObj.setContactNumber("Contact Number: "+user.getNumber());
		purchaseCopyObj.setContactPerson("Contact Person: "+user.getName());
		
		purchaseCopyObj.setEmail("Email: "+user.getEmailId());
		
		purchaseCopyObj.setDeliveyTerm("Delivery : "+map.get("delivery"));
		purchaseCopyObj.setModeOfPayment("Mode of Payment : "+map.get("modeOfPayment").replace("$", "%").replace("|","/"));
		purchaseCopyObj.setRestrictions("Jurisdiction : "+map.get("jurisdiction"));
		purchaseCopyObj.setTaxesTerm("Taxes : Included in above prices");
		//purchaseCopyObj.setTaxType(taxType);
		purchaseCopyObj.setWarranty("Warranty : "+map.get("warranty"));
		
		
		//Variables requiired for storing into purchase copy table
		double totalAmount = 0;
		double gstAmount = 0;
		double grandTotal = 0;
		double sgstAmount =0;
		double cgstAmount =0;
		
		
		
		
		
		// Getting purchase order object and sending to a Map..
		Optional<PurchaseOrder> purchaseOrderObj = purchaseService.findById(poNumber);
		Date date = purchaseOrderObj.get().getCreated();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String poDate = formatter.format(date);
		purchaseCopyObj.setCreated(poDate);
		// Description was coming as sales Item id so setting description as
		// by getting sales item obj
		List<PurchaseItem> purchaseItems = purchaseOrderObj.get().getItems();
        Map<String,String> modelMap = new HashMap();
		for (PurchaseItem purchaseItem : purchaseItems) {
			//Optional<SalesItem> salesItemObj = salesService.getSalesItemById(purchaseItem.getDescription());
			Optional<ItemMaster> itemMasterObject = itemMasterService.getItemById(purchaseItem.getModelNo());
			float gst = 0;
			if(itemMasterObject.isPresent()){
			modelMap.put(purchaseItem.getModelNo(), itemMasterObject.get().getItem_units().getName()+"&"+itemMasterObject.get().getModel()+"$"
					+itemMasterObject.get().getGst());
			gst = itemMasterObject.get().getGst();
			}
			//purchaseItem.setDescription(salesItemObj.get().getDescription());
			//purchaseItem.setModelNo(purchaseItem.getModelNo());
			/*if(itemMasterObject.isPresent()){
			purchaseItem.setModelNo(itemMasterObject.get().getModel());
			}*/
			
			totalAmount = totalAmount + purchaseItem.getAmount();
			
			gst = gst/100;
			gstAmount = gstAmount +purchaseItem.getAmount() * gst;
			System.out.println(gstAmount);
		}
		sgstAmount = totalAmount*0.09;
		sgstAmount = Math.round(sgstAmount * 100.0) / 100.0;
		cgstAmount = totalAmount*0.09;
		cgstAmount = Math.round(cgstAmount * 100.0) / 100.0;
		//gstAmount = totalAmount*0.18;
		gstAmount = Math.round(gstAmount * 100.0) / 100.0;
		grandTotal = totalAmount + gstAmount;
		grandTotal = Math.round(grandTotal * 100.0) / 100.0;
		//String numberInWords = getMoneyIntoWords((int) grandTotal);
		String numberInWords = purchasecopyservice.convertToIndianCurrency((int) grandTotal);
		numberInWords = numberInWords.replace("£ 00/100", "");
		String capitalnumberInWords = capitalize(numberInWords);
		purchaseCopyObj.setAmountinwords(capitalnumberInWords);
		String quoteRefNo = map.get("quoteRefNo");
		String quoteDate = map.get("quoteDate").replace("-", "/");;
		purchaseCopyObj.setGst(gstAmount);
		purchaseCopyObj.setIgst(gstAmount);
		purchaseCopyObj.setSgst (sgstAmount);
		purchaseCopyObj.setTotal(totalAmount);
		purchaseCopyObj.setCgst (cgstAmount);
		purchaseCopyObj.setGrandTotal((int)grandTotal);
		purchaseCopyObj.setIgst(0);
		purchaseCopyObj.setQuoteRefNo(quoteRefNo);
		purchaseCopyObj.setQuoteDate(quoteDate);
		purchasecopyservice.save(purchaseCopyObj);
		User userObj  = userService.getCurrentUser();
		
		Map<String, Optional<PurchaseOrder>> poData = new HashMap<String, Optional<PurchaseOrder>>();
	
		poData.put("purchaseData", purchaseOrderObj);
		request.setAttribute("termsAndConditions", map);
		request.setAttribute("customProperty", customProperty);
		request.setAttribute("modelMaps", modelMap);
		request.setAttribute("user", userObj);
		
		
		return new ModelAndView(new purchaseOrderExcel(), "poData", poData);

	}

	 
	

	 
		
	public String capitalize(String str) {
		String output = str.substring(0, 1).toUpperCase() + str.substring(1);

		return output;
	}

	public String getMoneyIntoWords(int grandTotal) {
		MoneyConverters converter = MoneyConverters.ENGLISH_BANKING_MONEY_VALUE;
		return converter.asWords(new BigDecimal(grandTotal));
	}
	
	// NEW: DataTable endpoint with database pagination
		@GetMapping("/api/itemMaster/datatable")
		public ResponseEntity<?> getItemMasterDataTable(HttpServletRequest request) {
			long startTime = System.currentTimeMillis();
			System.out.println("=== DATATABLE API CALL START ===");
			
			try {
				// DataTables parameters
				int draw = Integer.parseInt(request.getParameter("draw"));
				int start = Integer.parseInt(request.getParameter("start"));
				int length = Integer.parseInt(request.getParameter("length"));
				String searchValue = request.getParameter("search[value]");
				String toolTrackerParam = request.getParameter("toolTracker");
				boolean toolTrackerOnly = "true".equalsIgnoreCase(toolTrackerParam);
				
				System.out.println("Request params - draw: " + draw + ", start: " + start + ", length: " + length + ", search: '" + searchValue + "', toolTrackerOnly: " + toolTrackerOnly);
				
				// Calculate page number for database pagination
				int pageNo = start / length;
				
				// Step 1: Get paginated items
				long step1Start = System.currentTimeMillis();
				List<ItemMaster> paginatedItems = itemMasterService.getPaginatedItemList(pageNo, length, searchValue, toolTrackerOnly);
				System.out.println("Step 1 - Get paginated items (" + paginatedItems.size() + "): " + (System.currentTimeMillis() - step1Start) + "ms");
				
				// Step 2: Get total counts
				long step2Start = System.currentTimeMillis();
				long recordsTotal = itemMasterService.getItemCount(null, toolTrackerOnly); // Total items without search
				long recordsFiltered = itemMasterService.getItemCount(searchValue, toolTrackerOnly); // Total items with search
				System.out.println("Step 2 - Get counts (total: " + recordsTotal + ", filtered: " + recordsFiltered + "): " + (System.currentTimeMillis() - step2Start) + "ms");
				
				// Step 3: Load stocks and suppliers for current page
				long step3Start = System.currentTimeMillis();
				List<Stock> pageStocksList = new ArrayList<>();
				List<Supplier> pageSupplierslist = new ArrayList<>();
				
				for (ItemMaster item : paginatedItems) {
					String itemId = item.getId();
					// Load stocks only for this item
					List<Stock> itemStocks = stockService.getStockList(itemId);
					pageStocksList.addAll(itemStocks);
					// Load suppliers only for this item
					List<Supplier> itemSuppliers = itemMasterService.getSupplierList(itemId);
					pageSupplierslist.addAll(itemSuppliers);
				}
				System.out.println("Step 3 - Load stocks (" + pageStocksList.size() + ") and suppliers (" + pageSupplierslist.size() + "): " + (System.currentTimeMillis() - step3Start) + "ms");
				
				// Step 4: Prepare response
				long step4Start = System.currentTimeMillis();
				Map<String, Object> response = new HashMap<>();
				response.put("draw", draw);
				response.put("recordsTotal", recordsTotal);
				response.put("recordsFiltered", recordsFiltered);
				response.put("data", paginatedItems);
				response.put("allStocksList", pageStocksList); // Only current page stocks
				response.put("allSupplierslist", pageSupplierslist); // Only current page suppliers
				System.out.println("Step 4 - Prepare response: " + (System.currentTimeMillis() - step4Start) + "ms");
				
				long totalTime = System.currentTimeMillis() - startTime;
				System.out.println("=== DATATABLE API CALL COMPLETE: " + totalTime + "ms ===");
				
				return new ResponseEntity<>(response, HttpStatus.OK);
			} catch (Exception e) {
				System.err.println("ERROR in getItemMasterDataTable: " + e.getMessage());
				e.printStackTrace();
				throw e;
			}
		}
		
		// NEW: AJAX validation endpoint
		@GetMapping("/api/itemMaster/validateModel")
		public ResponseEntity<?> validateModelNumber(@RequestParam("model") String model, 
				@RequestParam(value = "itemId", required = false) String itemId) {
			boolean exists = itemMasterService.checkModelExists(model, itemId);
			return new ResponseEntity<>(exists, HttpStatus.OK);
		}
		

		// @GetMapping("/itemMaster")
		// public String getItemMaster(Model model) throws JsonProcessingException {
		// 	itemMasterService.trimSpacesFromModelNo();
		// 	// Load only first page of items for initial display
		// 	List<ItemMaster> itemList=itemMasterService.findPaginatedItemsWithoutTools(0, 100);
			
		// 	// Load only essential data initially - other data will be loaded via AJAX
		// 	User userObj  = userService.getCurrentUser();
		// 	String role = userObj.getRole();
		// 	String user=userObj.getUsername();
		// 	ObjectMapper mapper = utilService.getObjectMapper();
		// 	model.addAttribute("role", mapper.writeValueAsString(role));
		// 	model.addAttribute("user", mapper.writeValueAsString(user));
		// 	model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		// 	model.addAttribute("pageHeader", "Item Master");
			
		// 	// Initialize empty lists - will be populated via AJAX calls
		// 	model.addAttribute("supplierPartyList", mapper.writeValueAsString(new ArrayList<>()));
		// 	model.addAttribute("customerPartyList", mapper.writeValueAsString(new ArrayList<>()));
		// 	model.addAttribute("allSupplierslist", mapper.writeValueAsString(new ArrayList<>()));
		// 	model.addAttribute("allStocksList", mapper.writeValueAsString(new ArrayList<>()));
		// 	model.addAttribute("makeList", mapper.writeValueAsString(new ArrayList<>()));
		// 	return "itemMaster";
		// }

	@GetMapping("/itemMaster")
	public String getItemMaster(Model model) throws JsonProcessingException {
		itemMasterService.trimSpacesFromModelNo();
		List<ItemMaster> itemList=itemMasterService.getItemListWithoutTools();
		List<Stock> allStocksList = stockService.getAllStockList();
		List<Supplier> allSupplierslist = itemMasterService.getAllSupplierList();
		List<Party> customerpartyList = partyService.getPartyListByTypeCustomer();
		List<Party> supplierPartyList = partyService.getPartyListbyTypeSupplier();
		List<Make> makeList=makeService.getMakeList();
		User userObj  = userService.getCurrentUser();
		String role = userObj.getRole();
		String user=userObj.getUsername();
		ObjectMapper mapper = utilService.getObjectMapper();
		model.addAttribute("role", mapper.writeValueAsString(role));
		model.addAttribute("user", mapper.writeValueAsString(user));
		model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		model.addAttribute("supplierPartyList", mapper.writeValueAsString(supplierPartyList));
		 model.addAttribute("customerPartyList", mapper.writeValueAsString(customerpartyList));
		model.addAttribute("pageHeader", "Item Master");
		model.addAttribute("allSupplierslist", mapper.writeValueAsString(allSupplierslist));
		model.addAttribute("allStocksList", mapper.writeValueAsString(allStocksList));
		model.addAttribute("makeList", mapper.writeValueAsString(makeList));
		return "itemMaster";
	}
	/*@GetMapping("/itemMaster")
	public String getItemMaster(Model model) throws JsonProcessingException {
	    long startTime = System.currentTimeMillis();
	    itemMasterService.trimSpacesFromModelNo(); // lightweight

	    Executor executor = asyncExecutor;

	    // Parallel Futures with execution time logs
	    CompletableFuture<List<ItemMaster>> itemListFuture = CompletableFuture
	            .supplyAsync(() -> itemMasterService.getItemListWithoutTools(), executor)
	            .thenApplyAsync(list -> {
	                System.out.println("⏱ itemList loaded: " + list.size());
	                return list;
	            }, executor);

	    CompletableFuture<List<Stock>> stockFuture = CompletableFuture
	            .supplyAsync(() -> stockService.getAllStockList(), executor)
	            .thenApplyAsync(list -> {
	                System.out.println("⏱ stockList loaded: " + list.size());
	                return list;
	            }, executor);

	    CompletableFuture<List<Supplier>> supplierFuture = CompletableFuture
	            .supplyAsync(() -> itemMasterService.getAllSupplierList(), executor)
	            .thenApplyAsync(list -> {
	                System.out.println("⏱ supplierList loaded: " + list.size());
	                return list;
	            }, executor);

	    CompletableFuture<List<Party>> customerFuture = CompletableFuture
	            .supplyAsync(() -> partyService.getPartyListByTypeCustomer(), executor)
	            .thenApplyAsync(list -> {
	                System.out.println("⏱ customerPartyList loaded: " + list.size());
	                return list;
	            }, executor);

	    CompletableFuture<List<Party>> supplierPartyFuture = CompletableFuture
	            .supplyAsync(() -> partyService.getPartyListbyTypeSupplier(), executor)
	            .thenApplyAsync(list -> {
	                System.out.println("⏱ supplierPartyList loaded: " + list.size());
	                
	                return list;
	            }, executor);

	    CompletableFuture<List<Make>> makeListFuture = CompletableFuture
	            .supplyAsync(() -> makeService.getMakeList(), executor)
	            .thenApplyAsync(list -> {
	                System.out.println("⏱ makeList loaded: " + list.size());
	                return list;
	            }, executor);

	    // Wait for all to finish
	    CompletableFuture.allOf(
	            itemListFuture, stockFuture, supplierFuture,
	            customerFuture, supplierPartyFuture, makeListFuture
	    ).join();

	    // Safe joins
	    List<ItemMaster> itemList = itemListFuture.join();
	    List<Stock> allStocksList = stockFuture.join();
	    List<Supplier> allSupplierslist = supplierFuture.join();
	    List<Party> customerpartyList = customerFuture.join();
	    List<Party> supplierPartyList = supplierPartyFuture.join();
	    List<Make> makeList = makeListFuture.join();

	    // Prepare frontend model
	    User userObj = userService.getCurrentUser();
	    ObjectMapper mapper = utilService.getObjectMapper();
	    model.addAttribute("role", mapper.writeValueAsString(userObj.getRole()));
	    model.addAttribute("user", mapper.writeValueAsString(userObj.getUsername()));
	    model.addAttribute("itemList", mapper.writeValueAsString(itemList));
	    model.addAttribute("supplierPartyList", mapper.writeValueAsString(supplierPartyList));
	    model.addAttribute("customerPartyList", mapper.writeValueAsString(customerpartyList));
	    model.addAttribute("pageHeader", "Item Master");
	    model.addAttribute("allSupplierslist", mapper.writeValueAsString(allSupplierslist));
	    model.addAttribute("allStocksList", mapper.writeValueAsString(allStocksList));
	    model.addAttribute("makeList", mapper.writeValueAsString(makeList));

	    System.out.println("✅ Total itemMaster load time: " + (System.currentTimeMillis() - startTime) + " ms");

	    return "itemMaster";
	}*/
	
	@GetMapping("/toolTackles")
	public String getToolsTracker(Model model) throws JsonProcessingException {
		itemMasterService.trimSpacesFromModelNo();
		List<ItemMaster> itemList=itemMasterService.getItemListWithoutTools();
		List<ItemMaster> toolTrackerList=itemMasterService.getItemListWithTools();
		List<Stock> allStocksList = stockService.getAllStockList();
		List<Supplier> allSupplierslist = itemMasterService.getAllSupplierList();
		List<Party> customerpartyList = partyService.getPartyListByTypeCustomer();
		List<Party> supplierPartyList = partyService.getPartyListbyTypeSupplier();
		List<Make> makeList=makeService.getMakeList();
		User userObj  = userService.getCurrentUser();
		String role = userObj.getRole();
		String user=userObj.getUsername();
		ObjectMapper mapper = utilService.getObjectMapper();
		model.addAttribute("role", mapper.writeValueAsString(role));
		model.addAttribute("user", mapper.writeValueAsString(user));
		model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		model.addAttribute("toolTrackerList", mapper.writeValueAsString(toolTrackerList));
		model.addAttribute("supplierPartyList", mapper.writeValueAsString(supplierPartyList));
		 model.addAttribute("customerPartyList", mapper.writeValueAsString(customerpartyList));
		model.addAttribute("pageHeader", "Tools and Tackles");
		model.addAttribute("allSupplierslist", mapper.writeValueAsString(allSupplierslist));
		model.addAttribute("allStocksList", mapper.writeValueAsString(allStocksList));
		model.addAttribute("makeList", mapper.writeValueAsString(makeList));
		return "toolTracker";
	}
	

	
	// Save the itemMaster to database 
	@SuppressWarnings("unused")
	@PostMapping("/add/itemMaster")
	public String saveItemMaster( Model model,ItemMaster itemMaster) throws JsonProcessingException {
		
		System.out.println("This is today" +itemMaster);
		ItemMaster item=	itemMasterService.saveItemMaster(itemMaster);
		
	List<ItemMaster> itemList=itemMasterService.getItemList();
	ObjectMapper mapper = utilService.getObjectMapper();
	User userObj  = userService.getCurrentUser();
	String role = userObj.getRole();
	String user=userObj.getUsername();
		model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		model.addAttribute("user", mapper.writeValueAsString(user));
		
		
		return "redirect:/itemMaster";
	}
	
	// GRN dashboard – lists GRNs using server-side DataTables
	@GetMapping("/grnLists")
	public String purchaseOrderList(Model model) throws JsonProcessingException {
		List<PurchaseOrder> poList = purchaseService.findAll();
		User userObj  = userService.getCurrentUser();
		List<ItemMaster> itemList = itemMasterService.getItemList();
		List<PurchaseItem> purchaseItemList = purchaseItemService.getAllPurchaseItems();
		String role = userObj.getRole();
		ObjectMapper mapper = utilService.getObjectMapper();
		model.addAttribute("poList", mapper.writeValueAsString(poList));
		model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		model.addAttribute("purchaseItemList", mapper.writeValueAsString(purchaseItemList));
		model.addAttribute("role", mapper.writeValueAsString(role));
		model.addAttribute("pageHeader","GRN");
		return "goodsReceiptNote";
	}
	
	@GetMapping("/new_grn")
	public String getNewGrn(Model model, HttpServletRequest req) throws JsonProcessingException {
		Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
		List<PurchaseOrder> poList = purchaseService.findAll();
		List<Grn> grnList = grnService.getGrnList();
		List<ItemMaster> itemList=itemMasterService.getItemList();
        List<PurchaseItem> purchaseItemList =purchaseItemService.getAllPurchaseItems();
        ObjectMapper mapper=utilService.getObjectMapper();
        model.addAttribute("grn", new Grn());
		 model.addAttribute("poList", mapper.writeValueAsString(poList));
		 model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		 model.addAttribute("grnList", mapper.writeValueAsString(grnList));
		 model.addAttribute("purchaseItemList", mapper.writeValueAsString(purchaseItemList));
		 model.addAttribute("pageHeader","New Grn");
		 if(flashMap!=null) {
				Grn grnObj =  (Grn) flashMap.get("grnObj");
				model.addAttribute("grnObj",mapper.writeValueAsString(grnObj) );
			}
			return "newGoodsReceiptNote";
	}
	
	@GetMapping("/api/getPurchaseItemList")
	public ResponseEntity<?> purchaseItemList(@RequestParam("id") String poNumber,
			@RequestParam("className") String className, Model model) {

		List<PurchaseItem> purchaseItems = purchaseItemService.getPurchaseItem(poNumber, className);
		System.out.println(purchaseItems);
		// model.addAttribute("purchaseItems", purchaseItems);
		return new ResponseEntity<>(purchaseItems, HttpStatus.OK);
		// return "goodsReceiptNote";
	}
	
	@GetMapping("/api/grn/datatable")
	public ResponseEntity<?> getGrnDataTable(HttpServletRequest request) {
		try {
			// DataTables parameters
			int draw = Integer.parseInt(request.getParameter("draw"));
			int start = Integer.parseInt(request.getParameter("start"));
			int length = Integer.parseInt(request.getParameter("length"));
			String searchValue = request.getParameter("search[value]");
			if (searchValue == null || searchValue.trim().isEmpty()) {
				for (int i = 0; i < 12; i++) {
					String columnSearchValue = request.getParameter("columns[" + i + "][search][value]");
					if (columnSearchValue != null && !columnSearchValue.trim().isEmpty()) {
						searchValue = columnSearchValue;
						break;
					}
				}
			}
			
			// Calculate page number for database pagination
			int pageNo = start / length;
			
			// Get paginated GRN list
			Page<Grn> grnPage = grnService.getPaginatedGrnList(pageNo, length, searchValue);
			List<Grn> grnList = grnPage.getContent();
			
			// Get total counts
			long recordsTotal = grnService.getGrnCount(null); // Total GRNs without search
			long recordsFiltered = grnService.getGrnCount(searchValue); // Total GRNs with search
			
			// Prepare response
			Map<String, Object> response = new HashMap<>();
			response.put("draw", draw);
			response.put("recordsTotal", recordsTotal);
			response.put("recordsFiltered", recordsFiltered);
			response.put("data", grnList);
			
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			System.err.println("ERROR in getGrnDataTable: " + e.getMessage());
			e.printStackTrace();
			return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	//get purchase items by purchseItemId		
	@GetMapping("/api/purchase-item_byId")
	 public ResponseEntity<?> getPurchaseItemsByPurchaseItemId(@RequestParam("id") int purchaseItemId){
		Optional<PurchaseItem> purchaseItem=purchaseItemService.getPurchaseItemById(purchaseItemId);
		 return new ResponseEntity<>(purchaseItem,HttpStatus.OK) ;
	 }
	
	@PostMapping(path="/api/purchase/delete")
	  public ResponseEntity<?> deleteItem(@RequestParam("id") int id) {
		boolean isDeleted=purchaseItemService.deletePurchaseItem(id);
		return new ResponseEntity<>(isDeleted,HttpStatus.OK);
	 }
	
	@GetMapping("/api/po/address/{id}")
	public ResponseEntity<?> getPurchaseOrderAddresses(@PathVariable("id") String poNumber){
		Map<Object, Object> addressMap = purchaseService.findVendorsByPurchaseOrder(poNumber);
		
		String billingAddress = customProperty.getBillingAddress();
		String shippingAddress = customProperty.getShippingAddress();
		addressMap.put("billingAddress", billingAddress);
		addressMap.put("shippingAddress", shippingAddress);
		addressMap.put("modeOfPayment", customProperty.getModeOfPayment());
		addressMap.put("jurisdiction", customProperty.getJursidiction());
		addressMap.put("frieght", customProperty.getFrieght());
		addressMap.put("delivery", customProperty.getDelivery());
		addressMap.put("warranty", customProperty.getWarranty());
		
		
		return new ResponseEntity<>(addressMap,HttpStatus.OK) ;
	}
	
	
	/*private static final String EXTERNAL_FILE_PATH = Constants.FILE_LOCATION + File.separator;
	//String filePath = Constants.FILE_LOCATION + File.separator + fileName;
	//@RequestMapping("/file/")
	@RequestMapping("/prevpurchaseOrder/file/{poid}")
	public String downloadPDFResource(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("poid") String fileName) throws IOException {
		fileName= fileName+"_purchaseOrder.xlsx";
		pofileName = fileName;
		File file = new File(EXTERNAL_FILE_PATH + fileName);
		String f = file.getAbsolutePath();
		boolean f1 = file.exists();
		if (file.exists()) {

			//get the mimetype
			String mimeType = URLConnection.guessContentTypeFromName(file.getName());
			if (mimeType == null) {
				//unknown mimetype so set the mimetype to application/octet-stream
				mimeType = "application/octet-stream";
			}

			response.setContentType(mimeType);

			*//**
			 * In a regular HTTP response, the Content-Disposition response header is a
			 * header indicating if the content is expected to be displayed inline in the
			 * browser, that is, as a Web page or as part of a Web page, or as an
			 * attachment, that is downloaded and saved locally.
			 * 
			 *//*

			*//**
			 * Here we have mentioned it to show inline
			 *//*
			response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

			 //Here we have mentioned it to show as attachment
			 //response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + file.getName() + "\""));

			response.setContentLength((int) file.length());

			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

			FileCopyUtils.copy(inputStream, response.getOutputStream());

		//}
		return null;
		}else{
			return "redirect:/purchase";
		}
	}
	*/
	
	@GetMapping("/api/po/purchaseCopy/{id}")
	public ResponseEntity<?> getPurchaseCopy(@PathVariable("id") String poNumber){
		PurchaseCopy purchasecopy = purchasecopyservice.getPurchaseCopyByPoNumber(poNumber);
		
		
		return new ResponseEntity<>(purchasecopy,HttpStatus.OK) ;
	}
	
	/**
	 * redirecting to grn view page
	 * @param grnId
	 * @param redirectAttr
	 * @param model
	 * @return
	 */
	 @GetMapping("/api/grn/view")
		public String displayGrnView(@RequestParam("grnId") String grnId,RedirectAttributes redirectAttr, Model model){
			
			Optional<Grn> grn =grnService.getGrnById(grnId);
			redirectAttr.addFlashAttribute("grnObj",grn.get());
			return "redirect:/new_grn";
		}
	 @GetMapping("/api/grn_items_by_grnId")
	 public  ResponseEntity<?> getGrnItemListByGrnId(@RequestParam("grnId")  String  grnId,Model model) {
		 List<GrnItems> grnItems=grnService.getGrnListById(grnId);
			return new ResponseEntity<>(grnItems,HttpStatus.OK) ;
	 }
	 
	 
	 /**
	  * Delivery Challan  page
	  * @param model
	  * @return
	  * @throws JsonProcessingException
	  */
	 @GetMapping("/deliveryChallan")
		public String deliveryChallanPage(Model model,HttpServletRequest req) throws JsonProcessingException {
		 Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
		 	List<Party> partyList = partyService.getPartyList();
		 	List<Party> clientList = partyService.getPartyListByTypeCustomer();
			List<ItemMaster> itemList=itemMasterService.getItemList();
			 List<SalesOrder> salesList = salesService.findAllSalesOrderList();
			ObjectMapper mapper = utilService.getObjectMapper();
			 model.addAttribute("salesOrderList",mapper.writeValueAsString(salesList));
			model.addAttribute("partyList", mapper.writeValueAsString(partyList));
			model.addAttribute("clientList", mapper.writeValueAsString(clientList));
			 model.addAttribute("itemList", mapper.writeValueAsString(itemList));
			model.addAttribute("pageHeader", "Delivery Challan");
			 if(flashMap!=null) {
				 DeliveryChallan dcObj =  (DeliveryChallan) flashMap.get("dcObj");
					model.addAttribute("dcObj",mapper.writeValueAsString(dcObj) );
					
			}
			
			return "deliveryChallan";
		}
	 /**
	  * delivery challan dashboard
	  * @param model
	  * @return
	  * @throws JsonProcessingException
	  */
		/*
		 * @GetMapping("/dcList") public String deliveryChallanDashboard(Model model)
		 * throws JsonProcessingException { List<Party> partyList =
		 * partyService.getPartyList(); User userObj = userService.getCurrentUser();
		 * String role = userObj.getRole(); List<DeliveryChallan> dcLists =
		 * deliveryChallanService.getDeliveryChallanLists(); ObjectMapper
		 * mapper=utilService.getObjectMapper(); model.addAttribute("dcLists",
		 * mapper.writeValueAsString(dcLists)); model.addAttribute("partyList",
		 * mapper.writeValueAsString(partyList)); model.addAttribute("role",
		 * mapper.writeValueAsString(role));
		 * model.addAttribute("pageHeader","DC Dashboard"); return "dcDashboard"; }
		 */
	 
	 @GetMapping("/dcList")
	 public String deliveryChallanDashboard(Model model) throws JsonProcessingException {
	     List<Party> partyList = partyService.getPartyList();
	     User userObj  = userService.getCurrentUser();
	     String role = userObj.getRole();

	     ObjectMapper mapper = utilService.getObjectMapper();
	     model.addAttribute("partyList", mapper.writeValueAsString(partyList));
	     model.addAttribute("role", mapper.writeValueAsString(role));
	     model.addAttribute("pageHeader","DC Dashboard");

	     // Don’t load dcLists here – fetch lazily via Ajax
	     return "dcDashboard";
	 }

	 // ✅ Lazy load API for delivery challans
     @GetMapping("/dcList/data")
     @ResponseBody
     public Page<DeliveryChallan> getDeliveryChallanList(
             @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "10") int size,
             @RequestParam(defaultValue = "") String keyword,
             @RequestParam(defaultValue = "dcId") String sortField,
             @RequestParam(defaultValue = "desc") String sortDir,
             // column filters
             @RequestParam(defaultValue = "") String dcId,
             @RequestParam(defaultValue = "") String soNumber,
             @RequestParam(defaultValue = "") String clientName,
             @RequestParam(defaultValue = "") String clientPo,
             @RequestParam(defaultValue = "") String shipping) {

         // Allow sorting only by known persistent fields
         String safeField;
         switch (sortField) {
             case "dcId":
             case "soNumber":
             case "archive":
                 safeField = sortField; break;
             default:
                 safeField = "dcId";
         }
         Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
         Pageable pageable = PageRequest.of(page, size, Sort.by(dir, safeField));

         // If any column filter provided, use advanced search
         if (!dcId.isEmpty() || !soNumber.isEmpty() || !clientName.isEmpty() || !clientPo.isEmpty() || !shipping.isEmpty()) {
             return deliveryChallanService.getDeliveryChallanPageAdvanced(pageable, dcId, soNumber, clientName, clientPo, shipping);
         }
         // Else use global keyword
         return deliveryChallanService.getDeliveryChallanPage(pageable, keyword);
     }
	
	 @GetMapping("/dcList/search")
	 @ResponseBody
	 public List<DeliveryChallan> searchDeliveryChallans(@RequestParam String keyword) {
	     return deliveryChallanService.searchDeliveryChallans(keyword);
	 }


	 
	 /**
	  * save delivery challan
	  * @param model
	  * @param deliveryChallan
	  * @return
	 * @throws Exception 
	  */
	 @PostMapping("/add/delivery_challan")
		public String saveDc( Model model,DeliveryChallan deliveryChallan,HttpServletRequest req) throws Exception {
			String[] designArray = req.getParameterValues("designArrData");
			String soNumber = req.getParameter("soNumber");
			deliveryChallanService.saveDc(deliveryChallan,designArray,soNumber);
			return "redirect:/dcList";
		}
	 
	 /**
	  * Invoice page
	  * @param model
	  * @return
	  * @throws JsonProcessingException
	  */
	 @GetMapping("/invoice")
		public String invoicePage(Model model,HttpServletRequest req) throws JsonProcessingException {
		 Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
		 List<SalesOrder> salesList = salesService.getSalesOrderList();
		 ObjectMapper mapper=utilService.getObjectMapper();
     	 model.addAttribute("salesList", mapper.writeValueAsString(salesList));
			model.addAttribute("pageHeader", "Invoice");
			 if(flashMap!=null) {
				 Invoice invoiceObj =  (Invoice) flashMap.get("invoiceObj");
					model.addAttribute("invoiceObj",mapper.writeValueAsString(invoiceObj) );
					
			}
			return "invoice";
		}
	 /**
	  * add invoice
	  * @param model
	  * @param invoice
	  * @return
	  */
	 @PostMapping("/add/invoice")
		public String saveInvoice( Model model,Invoice invoice) {
			
			invoiceService.saveInvoice(invoice);
			return "redirect:/invoiceList";
		}
	 
	 /**
	  * save GRN
	  * @param model
	  * @param grn
	  * @return
	  */
	 @PostMapping("/add/grn")
		public String saveGrn(Model model, Grn grn) {
			grnService.saveGrn(grn);
			return "redirect:/grnLists";
		}
	 
	 /**
	  * invoice dashboard page
	  * @param model
	  * @return
	  * @throws JsonProcessingException
	  */
	 @GetMapping("/invoiceList")
		public String invoiceDashboard(Model model) throws JsonProcessingException {
		 	 List<Invoice> invoiceList = invoiceService.getInvoiceList();
	         ObjectMapper mapper=utilService.getObjectMapper();
	     	 model.addAttribute("invoiceList", mapper.writeValueAsString(invoiceList));
			 model.addAttribute("pageHeader","Invoice Dashboard");
			 return "invoiceDashboard";
		}
	 
	 /**
	  * get dc list by SO id
	  * @param soId
	  * @param model
	  * @return dcList
	  */
	 @GetMapping("/api/dcList/by_so_id")
	 public  ResponseEntity<?> getDcListBySoId(@RequestParam("soId") String soId,Model model) {
		// List<DeliveryChallan> dcList=deliveryChallanService.getDcListBySoId(soId);
		 List<DeliveryChallan> dcList=deliveryChallanService.getDcListBySoIdWhereInvGenerated(soId);
			return new ResponseEntity<>(dcList,HttpStatus.OK) ;
	 }
	 


	 /**
	  * get dc item list by dc id
	  * @param dcId
	  * @param model
	  * @return dcItemList
	  */
	 @GetMapping("/api/dcItemList/by_dc_id")
		public ResponseEntity<?> getDcItemListByDcId(@RequestParam("id") int dcId, Model model) {
			List<DeliveryChallanItems> dcItemList= deliveryChallanService.getDcItemListForNonZeroQty(dcId);
		    return new ResponseEntity<>(dcItemList,HttpStatus.OK) ;
	 }
	 

	 /**
	  * dc view page
	  * @param dcId
	  * @param redirectAttr
	  * @param model
	  * @return
	  */
	 
	 @GetMapping("/api/dc/view")
		public String displayDcView(@RequestParam("dcId") int dcId,RedirectAttributes redirectAttr, Model model){
			
			Optional<DeliveryChallan> dc =deliveryChallanService.getDcById(dcId);
			if(dc.isPresent()) {
				String soNumber=dc.get().getSoNumber();
				Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(soNumber);
				if(salesOrder.isPresent()) {
					Party party = salesOrder.get().getParty();
					String partyName = party.getPartyName();
					partyName = partyName.replace("\"", "&");
					partyName = partyName.replace("'", "&");
					String clientPo = salesOrder.get().getClientPoNumber();
					clientPo = clientPo.replace("'", "&");
					clientPo = clientPo.replace("\"", "&");
					dc.get().set("clientPoNumber",clientPo);
					dc.get().set("clientName",partyName);
					redirectAttr.addFlashAttribute("dcObj",dc.get());
					return "redirect:/deliveryChallan";
				}
			}
			// If DC not found, redirect back to DC list
			return "redirect:/dcList";
		}
	 
	 @SuppressWarnings({ "rawtypes", "unused" })
	@GetMapping("/invoice/details/{invNumber}")
	 public ModelAndView invoiceDetails(HttpServletRequest request,Model model
			 ,@PathVariable("invNumber") String invNumber) throws DocumentException, IOException{
		
		 Map<String, Optional> invoiceData = new HashMap<String, Optional>();
		// String invoiceId = "INV-BLR-ELT-11-2020";
		 Optional<Invoice> invoice =invoiceService.getInvoiceById(invNumber);
		 String dcNo = invoice.get().getDcNumber();
		 DeliveryChallan deliveryChallanObj = null;
		 List<DeliveryChallanItems> dcItemsListForAll = null;
		 ArrayList<DeliveryChallanItems> dcItemsForAll = new ArrayList<DeliveryChallanItems>();
		 Map<String,DeliveryChallanItems> dcItemMap = new HashMap<String,DeliveryChallanItems>();
		 List<DeliveryChallanItems> newItems = new ArrayList();
		 if(dcNo.equalsIgnoreCase("All")){
			 dcNo ="";
			
		 }else if(dcNo.contains(",")) {
			 dcNo=invoice.get().getDcNumber();
			 String[] deliveryChallanNo=dcNo.split(",");
			  for (String dcNumber : deliveryChallanNo) {
				 dcItemsListForAll=deliveryChallanService.getAllDcItemList(Integer.parseInt(dcNumber));
				 for (DeliveryChallanItems deliveryitem : dcItemsListForAll) {
					 dcItemsForAll.add(deliveryitem);
				}
			}
			  for (DeliveryChallanItems deliveryitem : dcItemsForAll) {
				  String description = deliveryitem.getDescription();
				  System.out.println("boolean1"+dcItemMap.containsKey(description));
					 if(dcItemMap.containsKey(description)) {
						 DeliveryChallanItems prevValueWithUpdates =dcItemMap.get(description);
							float prevQty  =  prevValueWithUpdates.getTodaysQty();
							float newQty =  prevQty + deliveryitem.getTodaysQty();
							prevValueWithUpdates.setTodaysQty(newQty);
							dcItemMap.put(description, prevValueWithUpdates);
					 }else {
						 dcItemMap.put(description, deliveryitem);
						}
				}
				 dcItemMap.forEach((key, value) -> newItems.add(value));
				 
		 }
		 else if(dcNo.equalsIgnoreCase("")){
			 dcNo ="";
		 }else{
		 Optional<DeliveryChallan> dcObj =deliveryChallanService.getDcById(Integer.parseInt(dcNo));
		 invoiceData.put("dcObj", dcObj);
		 deliveryChallanObj = dcObj.get();
		 }
		 
		 Optional<SalesOrder> salesObj = salesService.getSalesOrderById(invoice.get().getSoNumber());
		 Party billAddress = partyRepo.findById(salesObj.get().getBillingAddress());
	     Party shipAddress = partyRepo.findById(salesObj.get().getShippingAddress());
	     Optional<PartyAddress> partyBillAddress = partyAddressService.getAddressByAddressId(salesObj.get().getBillingAddress());
	     request.setAttribute("partyBillAddress", partyBillAddress);
		 Optional<PartyAddress> partyShippAddress = partyAddressService.getAddressByAddressId(salesObj.get().getShippingAddress());
		 request.setAttribute("partyShippAddress", partyShippAddress);
		 //Map<String, Optional> invoiceData = new HashMap<String, Optional>();
		 invoiceData.put("invoiceObj", invoice);
		 invoiceData.put("salesObj", salesObj);
		 request.setAttribute("dcItemsList", newItems);
		 request.setAttribute("billAddress", billAddress);
		 request.setAttribute("shipAddress", shipAddress);
		 request.setAttribute("dcNo", dcNo);
		 request.setAttribute("dcDate", dcNo);
		 boolean isInvGenerated=invCopyService.isInvGenerated(invNumber);
		 if(isInvGenerated == false) {
		 InvoiceCopy invCopyObj = new InvoiceCopy();
		 invCopyObj.setInvId(invNumber);
		 invCopyRepo.save(invCopyObj);
		 }
		 return new ModelAndView(new invoiceExcel(), "invoiceData", invoiceData);
		
	 }
	 @SuppressWarnings({ "rawtypes", "resource" })
		@GetMapping("/invoice/pdf/details/{invNumber}")
		 public void invoicePdfDetails(HttpServletRequest request,HttpServletResponse response,Model model
				 ,@PathVariable("invNumber") String invNumber) throws DocumentException, IOException{
			
			 Map<String, Optional> invoiceData = new HashMap<String, Optional>();
			// String invoiceId = "INV-BLR-ELT-11-2020";
			 Optional<Invoice> invoice =invoiceService.getInvoiceById(invNumber);
			 String dcNo = invoice.get().getDcNumber();
			 DeliveryChallan deliveryChallanObj = null;
			 List<DeliveryChallanItems> dcItemsListForAll = null;
			 ArrayList<DeliveryChallanItems> dcItemsForAll = new ArrayList<DeliveryChallanItems>();
			 Map<String,DeliveryChallanItems> dcItemMap = new HashMap<String,DeliveryChallanItems>();
			 List<DeliveryChallanItems> newItems = new ArrayList();
			 if(dcNo.equalsIgnoreCase("All")){
				 dcNo ="";
				
			 }else if(dcNo.contains(",")){
				 dcNo=invoice.get().getDcNumber();
				 String[] deliveryChallanNo=dcNo.split(",");
				  for (String dcNumber : deliveryChallanNo) {
					 dcItemsListForAll=deliveryChallanService.getAllDcItemList(Integer.parseInt(dcNumber));
					 for (DeliveryChallanItems deliveryitem : dcItemsListForAll) {
						 dcItemsForAll.add(deliveryitem);
					}
				}
				  for (DeliveryChallanItems deliveryitem : dcItemsForAll) {
					  String description = deliveryitem.getDescription();
					  System.out.println("boolean1"+dcItemMap.containsKey(description));
						 if(dcItemMap.containsKey(description)) {
							 DeliveryChallanItems prevValueWithUpdates =dcItemMap.get(description);
								float prevQty  =  prevValueWithUpdates.getTodaysQty();
								float newQty =  prevQty + deliveryitem.getTodaysQty();
								prevValueWithUpdates.setTodaysQty(newQty);
								dcItemMap.put(description, prevValueWithUpdates);
						 }else {
							 dcItemMap.put(description, deliveryitem);
							}
					}
					 dcItemMap.forEach((key, value) -> newItems.add(value));
					 
				
				
			 }
			 else if(dcNo.equalsIgnoreCase("")){
				 dcNo ="";
			 }else{
			 Optional<DeliveryChallan> dcObj =deliveryChallanService.getDcById(Integer.parseInt(dcNo));
			 invoiceData.put("dcObj", dcObj);
			 deliveryChallanObj = dcObj.get();
			 }
			 
			 Optional<SalesOrder> salesObj = salesService.getSalesOrderById(invoice.get().getSoNumber());
			 Party billAddress = partyRepo.findById(salesObj.get().getBillingAddress());
		     Party shipAddress = partyRepo.findById(salesObj.get().getShippingAddress());
		     Optional<PartyAddress> partyBillAddress = partyAddressService.getAddressByAddressId(salesObj.get().getBillingAddress());
		     request.setAttribute("partyBillAddress", partyBillAddress);
			 Optional<PartyAddress> partyShippAddress = partyAddressService.getAddressByAddressId(salesObj.get().getShippingAddress());
			 request.setAttribute("partyShippAddress", partyShippAddress);
			 invoiceData.put("invoiceObj", invoice);
			 invoiceData.put("salesObj", salesObj);
			 request.setAttribute("billAddress", billAddress);
			 request.setAttribute("shipAddress", shipAddress);
			 request.setAttribute("dcNo", dcNo);
			 request.setAttribute("dcDate", dcNo);
			 InvoicePdf invPdf = new InvoicePdf();
			 String pdfPath=invPdf.invoiceFunction(invoice.get(),salesObj.get(),dcNo,deliveryChallanObj,request,dcItemsForAll,newItems);
			 boolean isInvGenerated=invCopyService.isInvGenerated(invNumber);
			 if(isInvGenerated == false) {
			 InvoiceCopy invCopyObj = new InvoiceCopy();
			 invCopyObj.setInvId(invNumber);
			 invCopyRepo.save(invCopyObj);
			 }
			 response.setContentType("application/pdf");

				response.setHeader("Content-Disposition", "attachment; filename=\"_INVOICE.pdf\"");
				try {

					File f = new File(pdfPath);
					if (f.exists()) {
						System.out.println("trueeeeeeee");
					}
					FileInputStream fis = new FileInputStream(f);
					DataOutputStream os = new DataOutputStream(response.getOutputStream());
					response.setHeader("Content-Length", String.valueOf(f.length()));
					byte[] buffer = new byte[1024];
					int len = 0;
					while ((len = fis.read(buffer)) >= 0) {
						os.write(buffer, 0, len);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			
		 }
	 
	 @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	@GetMapping("/dc/details/{dcId}/{option}")
     public ModelAndView dcDetails(HttpServletRequest request,Model model,
    		 @PathVariable("dcId") int dcId, @PathVariable("option") String option){
		Optional<DeliveryChallan> dcObj = deliveryChallanService.getDcById(dcId);
		List<DeliveryChallanItems> dcItems = deliveryChallanService.getDcItemList(dcId);
		
		Map<String,String> map = new HashMap();
		for (DeliveryChallanItems deliveryChallanItems : dcItems) {
			String salesItemId=(String) deliveryChallanItems.get("salesItemIdHidden");
			List<DesignItems> designItemsList =salesOrderDesignService.getDesignItemListBySOItemId(salesItemId);
			ArrayList list = new ArrayList();
			for (DesignItems designItem : designItemsList) {
				String itemId=designItem.getItemId();
				Optional<ItemMaster> itemMaster= itemMasterService.getItemById(itemId);
				String modelNumber=itemMaster.get().getModel();
				list.add(modelNumber);
			}
			map.put(Integer.toString(deliveryChallanItems.getDcItemId()), list.toString());
		}
         Map<String, Optional<DeliveryChallan>>  dcData = new HashMap<String, Optional<DeliveryChallan>> ();
         dcData.put("dcObj", dcObj);
         Optional<SalesOrder> salesObj = salesService.getSalesOrderById(dcObj.get().getSoNumber());
		 Party billAddress = partyRepo.findById(salesObj.get().getBillingAddress());
	     Party shipAddress = partyRepo.findById(salesObj.get().getShippingAddress());
	     
	     Optional<PartyAddress> partyBillAddress = partyAddressService.getAddressByAddressId(salesObj.get().getBillingAddress());
	     
		 Optional<PartyAddress> partyShippAddress = partyAddressService.getAddressByAddressId(salesObj.get().getShippingAddress());
		 User userObj  = userService.getCurrentUser();
		 request.setAttribute("partyBillAddress", partyBillAddress);
		 request.setAttribute("partyShippAddress", partyShippAddress);
	     request.setAttribute("billAddress", billAddress);
		 request.setAttribute("shipAddress", shipAddress);
		 request.setAttribute("option", option);
		 request.setAttribute("map", map);
		 request.setAttribute("user", userObj);
	     if(option.equals("All")) {
	    	 return new ModelAndView(new DeliveryChallanExcelForAll(), "dcData", dcData);
	     }else {
	    	 return new ModelAndView(new DeliveryChallanExcel(), "dcData", dcData);
	     }
     }

	 
	 /**
	  * Invoice view page
	  * @param invoiceId
	  * @param redirectAttr
	  * @param model
	  * @return
	  */
	 @GetMapping("/api/invoice/view")
		public String displayInvoiceView(@RequestParam("invoiceId") String invoiceId,RedirectAttributes redirectAttr, Model model){
			
			Optional<Invoice> invoice =invoiceService.getInvoiceById(invoiceId);
			String soNumber=invoice.get().getSoNumber();
			Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(soNumber);
			String clientPo = salesOrder.get().getClientPoNumber();
			clientPo = clientPo.replace("'", "&");
			clientPo = clientPo.replace("\"", "&");
			invoice.get().set("clientPoNumber",clientPo);
			redirectAttr.addFlashAttribute("invoiceObj",invoice.get());
			return "redirect:/invoice";
		}
	 
	 @GetMapping("/api/invoiceItemList/by_invoice_id")
		public ResponseEntity<?> getInvoiceItemListByInvoiceId(@RequestParam("invoiceId") String invoiceId, Model model) {
			List<InvoiceItem> invoiceItemList= invoiceService.getInvoiceItemListById(invoiceId);
		    return new ResponseEntity<>(invoiceItemList,HttpStatus.OK) ;
	 }

	 @GetMapping("/api/dc-item")
	 public ResponseEntity<?> byDcItemId(@RequestParam("id") int dcItemId){
		 Optional<DeliveryChallanItems> dcItem=deliveryChallanService.getDcItemByDcItemId(dcItemId);
		 return new ResponseEntity<>(dcItem,HttpStatus.OK) ;
	 }
	 
	 @GetMapping("/api/designList/salesItemId")
	 public ResponseEntity<?> SodesignListBySoItemId(@RequestParam("salesItemid") String salesItemid){
		 List<DesignItems> dsesignItemList =purchaseService.getDesignListOfItemBySalesItemId(salesItemid);
		 return new ResponseEntity<>(dsesignItemList,HttpStatus.OK) ;
	 }
	 
	 @GetMapping("/api/grn_items_by_poNo")
	 public  ResponseEntity<?> getGrnItemListByPoNo(@RequestParam("poNo")  String  poNo,Model model) {
		 List<GrnItems> grnItems=grnService.getGrnItemsListByPoNo(poNo);
		
			return new ResponseEntity<>(grnItems,HttpStatus.OK) ;
	 }
	 
	 @GetMapping("/api/dc_itemlist_for_all_dc/by_soId")
	 public  ResponseEntity<?> getDcItemListForAllDc(@RequestParam("soId")  String  soId,Model model) {
		 List<DeliveryChallanItems> dcItemList=deliveryChallanService.getDcItemListsForAllDc(soId);
		
			return new ResponseEntity<>(dcItemList,HttpStatus.OK) ;
	 }
	 
	 @GetMapping("/api/dc_itemlist_for_all_dc")
	 public  ResponseEntity<?> getDcItemListForAllDcBySoId(@RequestParam("soId")  String  soId,Model model) {
		 List<DeliveryChallanItems> dcItemList=deliveryChallanService.getDcItemListsForAllDcBySoId(soId);
		
			return new ResponseEntity<>(dcItemList,HttpStatus.OK) ;
	 }
	 
	 @GetMapping("/api/purchase/items/list")
	 public  ResponseEntity<?> getPoItemsModelNO() {
		 @SuppressWarnings("rawtypes")
		Map poItemMap= purchaseItemService.getModelNos();
		
			return new ResponseEntity<>(poItemMap,HttpStatus.OK) ;
	 }
	 
	 @GetMapping("/poItem/History/")
		public String poItemsHistory(HttpServletRequest request, Model model) throws JsonProcessingException {

			String itemId = request.getParameter("poItemHistoryReport");
			Optional<ItemMaster> itemMasterObj = itemMasterService.getItemById(itemId);
			// List<Object> pendingDcList = new ArrayList();
			List<Object> poItemHistoryList = purchaseItemService.getPoHistory(itemId);
			model.addAttribute("pageHeader", "Po Item History - " + itemMasterObj.get().getItemName() +"("+itemMasterObj.get().getModel()+")");
		   model.addAttribute("poItemHistoryList", poItemHistoryList);
			return "poItemHistory";

		}
	 
	 @GetMapping("/api/poItem/LastPrice")
		public ResponseEntity<?> poItemsLastPrice(HttpServletRequest request, Model model, @RequestParam("modelNo") String modelNo) throws JsonProcessingException {

		List<PurchaseItem> poItemList = purchaseItemService.findByModelNumberWithLatestAndCheapestPricePoItem(modelNo);
			return new ResponseEntity<>(poItemList,HttpStatus.OK) ;

		}
	 @GetMapping("/api/model/qty_details")
		public ResponseEntity<?> modelQuantityDetails(HttpServletRequest request, Model model, @RequestParam("modelNo") String modelNo,@RequestParam("salesItemId") String salesItemId) throws JsonProcessingException {

		Map<String, Object> poItemList = purchaseItemService.findByModelwiseQuantityDetails(modelNo,salesItemId);
			return new ResponseEntity<>(poItemList,HttpStatus.OK) ;

		}

		@PostMapping("/api/check_dc_invoice_exists")
		public ResponseEntity<?> checkForDcInvoice(@RequestParam("salesItemId") String salesItemId) {

			boolean itemExist = purchaseItemService.checkForDcInvoiceExists(salesItemId);

			return new ResponseEntity<>(itemExist, HttpStatus.OK);
		}
		
		@GetMapping("/api/partial_po_items")
		public ResponseEntity<?> getPartialItems(HttpServletRequest request, Model model, @RequestParam("poNumber") String poNumber) throws JsonProcessingException {

		List<PurchaseItem> poItemList = purchaseItemService.getPartialPoItems(poNumber);
			return new ResponseEntity<>(poItemList,HttpStatus.OK) ;

		}
		
		@PostMapping("/api/payment_status")
		public ResponseEntity<?> updateStatus(@RequestParam("invoiceNo") String invoiceNo,@RequestParam("paymentMode") String paymentMode,
				@RequestParam("paymentRemarks") String paymentRemarks,@RequestParam("transactionNumber") String transactionNumber) {

			boolean paymentStatus = invoiceService.updateInvoice(invoiceNo, paymentMode, paymentRemarks, transactionNumber);

			return new ResponseEntity<>(paymentStatus, HttpStatus.OK);
		}
		
		@GetMapping("/api/get_po_by_ponumber")
		public ResponseEntity<?> getPoByPoNumber(HttpServletRequest request, Model model, @RequestParam("poNumber") String poNumber) throws JsonProcessingException {

		Optional<PurchaseOrder> po = purchaseService.findById(poNumber);
			return new ResponseEntity<>(po,HttpStatus.OK) ;

		}
		//Purchase report by itemid
		@GetMapping("/purchase_list/by_item_id")
		public String purchaseOrderByItemId(HttpServletRequest request, Model model) throws JsonProcessingException {
			String modelNo = request.getParameter("itemId");
			Map<String, Object> po = purchaseService.getPurchaseOrderbyItemId(modelNo);
			ObjectMapper mapper = new ObjectMapper();
			model.addAttribute("pageHeader", "Purchase Order List By Item");
			model.addAttribute("poList", mapper.writeValueAsString(po));
			return "poByItem";
		}
		
		//grn list by itemid
		@GetMapping("/grn_list/by_item_id")
		public String grnByItemId(HttpServletRequest request, Model model) throws JsonProcessingException {
			String modelNo = request.getParameter("itemId");
			List<Grn> grnList = grnService.getGrnListByItemId(modelNo);
			ObjectMapper mapper = new ObjectMapper();
			model.addAttribute("pageHeader", "Grn List By Item");
			model.addAttribute("grnList", mapper.writeValueAsString(grnList));
			return "grnListByItem";
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		@GetMapping("/purchaseOrder_pdf/details/{json}")

		public void purchaseOrderPdf(HttpServletRequest request,HttpServletResponse response,Model model,
				@PathVariable("json") String json) throws ParseException, MalformedURLException, DocumentException, IOException {


			Gson g = new Gson();
			Map<String, String> map =  g.fromJson(json, Map.class);

			//Field required for storing into purchase copy
			String shippingAddressKey = map.get("shippingAddressId");
			String billingAddressKey = map.get("billingAddressId");
			String billingAddress = null;
			String shippingAddress = null;
			String vendorAddress =null;



			//For Getting Billing Address and Shipping address
			String billingAddressesWithDelimiter = customProperty.getBillingAddress();
			String [] billingAddressesArray = billingAddressesWithDelimiter.split("\\^\\^");

			for(int i=0;i<billingAddressesArray.length;i++) {
				String[] singleAddressArr = billingAddressesArray[i].split("\\$\\$");
				String key = singleAddressArr[0].replace("\n", "").trim();
				if(key.equalsIgnoreCase(billingAddressKey)) {
					billingAddress = billingAddressesArray[i].replaceAll("\\$\\$", ",").replace(key+",", "").trim();
				}
			}
			billingAddress =billingAddress.replace(",Karnataka", "");
			billingAddress =billingAddress.replace(",India", "");
			if(billingAddress.contains("Mangalore")){
				billingAddress = billingAddress.replace("Sri Ganesh Kripa,", "Sri Ganesh Kripa,");
				billingAddress = billingAddress.replace("Venkatesh Sadana", "Venkatesh Sadana,");
				billingAddress = billingAddress.replace(",575015", "");
				billingAddress=  billingAddress.replace(",Near Ganapathi Temple", "Near Ganapathi Temple,");
				billingAddress=  billingAddress.replace(",Perlaguri", "");
				billingAddress=  billingAddress.replace(",Kavoor Post", "Perlaguri Kavoor Post");
				billingAddress = billingAddress.replace("Mangalore", "Mangalore 575015");
			}
			else{
				billingAddress =billingAddress.replace("560046", "");
				billingAddress = billingAddress.replace("Bengaluru", "Bangalore 560046");
			}
			billingAddress =billingAddress.replace(",", "\n");

			if(shippingAddressKey.contains("key")) {
				String shippingAddressesWithDelimiter = customProperty.getShippingAddress();
				String [] shippingAddressesArray = shippingAddressesWithDelimiter.split("\\^\\^");
	
				for(int i=0;i<shippingAddressesArray.length;i++) {
					String[] singleShippingAddressArr = shippingAddressesArray[i].split("\\$\\$");
					String key = singleShippingAddressArr[0].replace("\n", "").trim();
					if(key.equalsIgnoreCase(shippingAddressKey)) {
						shippingAddress = shippingAddressesArray[i].replaceAll("\\$\\$", ",").replace(key+",", "").trim();
					}
				}
				shippingAddress =shippingAddress.replace(",Karnataka", "");
				shippingAddress =shippingAddress.replace(",India", "");
				if(shippingAddress.contains("Mangalore")){
					shippingAddress = shippingAddress.replace("Sri Ganesh Kripa,", "Sri Ganesh Kripa,");
					shippingAddress = shippingAddress.replace("Venkatesh Sadana", "Venkatesh Sadana,");
					shippingAddress = shippingAddress.replace(",575015", "");
					shippingAddress=  shippingAddress.replace(",Near Ganapathi Temple", "Near Ganapathi Temple,");
					shippingAddress=  shippingAddress.replace(",Perlaguri", "");
					shippingAddress=  shippingAddress.replace(",Kavoor Post", "Perlaguri Kavoor Post");
					shippingAddress = shippingAddress.replace("Mangalore", "Mangalore 575015");
				}
				else{
					shippingAddress =shippingAddress.replace("560046", "");
					shippingAddress = shippingAddress.replace("Bengaluru", "Bangalore 560046");
				}
				shippingAddress =shippingAddress.replace(",", "\n");
			}else {
				String shippingAddressId = map.get("shippingAddressId");

				if(shippingAddressId.contains("$")) {
					shippingAddressId  = shippingAddressId.replace("$", "/");
				}
				Party altshippingParty = partyRepo.findById(shippingAddressId);
				request.setAttribute("altshippingParty", altshippingParty);

				if(altshippingParty!= null) {
					String addr2 = altshippingParty.getAddr2();
					String gst = altshippingParty.getGst();
					String contactNo = altshippingParty.getPhone1();
					String pincode=altshippingParty.getPin();
					if(pincode==null) {
						pincode="";
					}
					if(contactNo == null || contactNo ==""){
						contactNo="NA";
					}
					if(gst == null || gst ==""){
						gst="";
					}
					if(addr2 == null || addr2.equalsIgnoreCase("")) {
						//addr2 = "";
						shippingAddress = altshippingParty.getPartyName()+","+altshippingParty.getAddr1()+","+altshippingParty.getParty_city().getName()+","+altshippingParty.getParty_city().getCode()+" " +pincode+"<br>"+"Contact: "+contactNo
								+"<br>"+" Email: "+altshippingParty.getEmail1();
					}else{
						shippingAddress = altshippingParty.getPartyName()+","+altshippingParty.getAddr1()+","+addr2+","+altshippingParty.getParty_city().getName()+" " +pincode+"<br>"+"Contact: "+contactNo
								+"<br>"+" Email: "+altshippingParty.getEmail1();
					}
				}
			}


			String poNumber = map.get("poNumber");
			String vendorAddressId = map.get("vendorAddressId");

			if(vendorAddressId.contains("$")) {
				vendorAddressId  = vendorAddressId.replace("$", "/");
			}

			User user=userService.getCurrentUser();
			Party party = partyRepo.findById(vendorAddressId);
			request.setAttribute("party", party);

			Optional<PartyAddress> partyAddress = partyAddressService.getAddressByAddressId(vendorAddressId);
			request.setAttribute("partyAddress", partyAddress);

			//For saving into purchaseCopy Table
			PurchaseCopy purchaseCopyObj = new PurchaseCopy();

			//For Storing Vendor Address into purchasecopy table

			if(party!= null) {
				String addr2 = party.getAddr2();
				String gst = party.getGst();
				String contactNo = party.getPhone1();
				String pincode=party.getPin();
				if(pincode==null) {
					pincode="";
				}
				if(contactNo == null || contactNo ==""){
					contactNo="NA";
				}
				if(gst == null || gst ==""){
					gst="";
				}
				if(addr2 == null || addr2.equalsIgnoreCase("")) {
					//addr2 = "";
					vendorAddress = party.getPartyName()+","+party.getAddr1()+","+party.getParty_city().getName()+","+party.getParty_city().getCode()+" " +pincode+"<br>"+"Contact: "+contactNo
							+"<br>"+" GSTIN: "+gst+"<br>"+"Email: "+party.getEmail1();
				}else{
					vendorAddress = party.getPartyName()+","+party.getAddr1()+","+addr2+","+party.getParty_city().getName()+" " +pincode+"<br>"+"Contact: "+contactNo
							+"<br>"+" GSTIN: "+gst+"<br>"+"Email: "+party.getEmail1();
				}
			}

			purchaseCopyObj.setVendorAddress(vendorAddress);
			purchaseCopyObj.setBillingAddress(billingAddress);
			purchaseCopyObj.setDeliveryAddress(shippingAddress);
			purchaseCopyObj.setPoNumber(poNumber);
			purchaseCopyObj.setContactNumber("Contact Number: "+user.getNumber());
			purchaseCopyObj.setContactPerson("Contact Person: "+user.getName());

			purchaseCopyObj.setEmail("Email: "+user.getEmailId());

			purchaseCopyObj.setDeliveyTerm("Delivery : "+map.get("delivery"));
			purchaseCopyObj.setModeOfPayment("Mode of Payment : "+map.get("modeOfPayment").replace("$", "%").replace("|","/"));
			purchaseCopyObj.setRestrictions("Jurisdiction : "+map.get("jurisdiction"));
			purchaseCopyObj.setTaxesTerm("Taxes : Included in above prices");
			//purchaseCopyObj.setTaxType(taxType);
			purchaseCopyObj.setWarranty("Warranty : "+map.get("warranty"));


			//Variables requiired for storing into purchase copy table
			double totalAmount = 0;
			double gstAmount = 0;
			double grandTotal = 0;
			double sgstAmount =0;
			double cgstAmount =0;

			// Getting purchase order object and sending to a Map..
			Optional<PurchaseOrder> purchaseOrderObj = purchaseService.findById(poNumber);
			Date date = purchaseOrderObj.get().getCreated();
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			String poDate = formatter.format(date);
			purchaseCopyObj.setCreated(poDate);
			// Description was coming as sales Item id so setting description as
			// by getting sales item obj
			List<PurchaseItem> purchaseItems = purchaseOrderObj.get().getItems();
			Map<String,String> modelMap = new HashMap();
			for (PurchaseItem purchaseItem : purchaseItems) {
				//Optional<SalesItem> salesItemObj = salesService.getSalesItemById(purchaseItem.getDescription());
				Optional<ItemMaster> itemMasterObject = itemMasterService.getItemById(purchaseItem.getModelNo());
				float gst = 0;
				if(itemMasterObject.isPresent()){
					modelMap.put(purchaseItem.getModelNo(), itemMasterObject.get().getItem_units().getName()+"&"+itemMasterObject.get().getModel()+"$"
							+itemMasterObject.get().getGst());
					gst = itemMasterObject.get().getGst();
				}
				//purchaseItem.setDescription(salesItemObj.get().getDescription());
				//purchaseItem.setModelNo(purchaseItem.getModelNo());
				/*if(itemMasterObject.isPresent()){
					purchaseItem.setModelNo(itemMasterObject.get().getModel());
					}*/

				totalAmount = totalAmount + purchaseItem.getAmount();

				gst = gst/100;
				gstAmount = gstAmount +purchaseItem.getAmount() * gst;
				System.out.println(gstAmount);
			}
			sgstAmount = totalAmount*0.09;
			sgstAmount = Math.round(sgstAmount * 100.0) / 100.0;
			cgstAmount = totalAmount*0.09;
			cgstAmount = Math.round(cgstAmount * 100.0) / 100.0;
			//gstAmount = totalAmount*0.18;
			gstAmount = Math.round(gstAmount * 100.0) / 100.0;
			grandTotal = totalAmount + gstAmount;
			grandTotal = Math.round(grandTotal * 100.0) / 100.0;
			//String numberInWords = getMoneyIntoWords((int) grandTotal);
			String numberInWords = purchasecopyservice.convertToIndianCurrency((int) grandTotal);
			numberInWords = numberInWords.replace("£ 00/100", "");
			String capitalnumberInWords = capitalize(numberInWords);
			purchaseCopyObj.setAmountinwords(capitalnumberInWords);
			
			String quoteRefNo = map.get("quoteRefNo");
			String quoteDate = map.get("quoteDate").replace("-", "/");;
			purchaseCopyObj.setGst(gstAmount);
			purchaseCopyObj.setIgst(gstAmount);
			purchaseCopyObj.setSgst (sgstAmount);
			purchaseCopyObj.setTotal(totalAmount);
			purchaseCopyObj.setCgst (cgstAmount);
			purchaseCopyObj.setGrandTotal((int)grandTotal);
			purchaseCopyObj.setIgst(0);
			purchaseCopyObj.setQuoteRefNo(quoteRefNo);
			purchaseCopyObj.setQuoteDate(quoteDate);
			purchasecopyservice.save(purchaseCopyObj);
			User userObj  = userService.getCurrentUser();

			Map<String, Optional<PurchaseOrder>> poData = new HashMap<String, Optional<PurchaseOrder>>();

			poData.put("purchaseData", purchaseOrderObj);
			request.setAttribute("termsAndConditions", map);
			request.setAttribute("customProperty", customProperty);
			request.setAttribute("modelMaps", modelMap);
			request.setAttribute("user",userObj);

			PurchasePdf purchasePdf = new PurchasePdf();
			 String pdfPath=purchasePdf.purchaseFunction(purchaseOrderObj.get(),request);
			
			 response.setContentType("application/pdf");

				response.setHeader("Content-Disposition", "attachment; filename=\"_PURCHASE.pdf\"");
				try {

					File f = new File(pdfPath);
					if (f.exists()) {
						System.out.println("trueeeeeeee");
					}
					FileInputStream fis = new FileInputStream(f);
					DataOutputStream os = new DataOutputStream(response.getOutputStream());
					response.setHeader("Content-Length", String.valueOf(f.length()));
					byte[] buffer = new byte[1024];
					int len = 0;
					while ((len = fis.read(buffer)) >= 0) {
						os.write(buffer, 0, len);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}

		@GetMapping("/api/purchase_item_by_item_id")
		 public  ResponseEntity<?> getPurchaseItemBySalesItemIdAndItemId(@RequestParam("salesItemId")  String  salesItemId,@RequestParam("itemId")  String  itemId,Model model) {
			 	List<PurchaseItem> purchaseItem=purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(salesItemId,itemId);
				return new ResponseEntity<>(purchaseItem,HttpStatus.OK) ;
		 }

		@GetMapping("/purchase_archived")
		public String archivedPurchaseDashBoard(Model model) throws JsonProcessingException {
			User userObj  = userService.getCurrentUser();
			String role = userObj.getRole();
			List<PurchaseOrder> poList = purchaseService.findAllArchivedPOList();
			List<Party> partyList = partyService.getPartyListbyTypeSupplier(); 
			List<ItemMaster> itemList = itemMasterService.getItemList();
			ObjectMapper mapper = utilService.getObjectMapper();
			model.addAttribute("poList", mapper.writeValueAsString(poList));
			model.addAttribute("role", mapper.writeValueAsString(role));
			model.addAttribute("pageHeader", "Archived Purchase Orders");
			model.addAttribute("partyList", mapper.writeValueAsString(partyList));
			model.addAttribute("itemList", mapper.writeValueAsString(itemList));
			return "archivedPurchaseDashboard";
		}
		
		 @GetMapping("/dc_archived")
			public String archivedDeliveryChallanDashboard(Model model) throws JsonProcessingException {
			 	User userObj  = userService.getCurrentUser();
				String role = userObj.getRole();
				List<Party> partyList = partyService.getPartyList();
			 	 List<DeliveryChallan> dcLists = deliveryChallanService.getDeliveryChallanListsArchived();
		         ObjectMapper mapper=utilService.getObjectMapper();
		     	 model.addAttribute("dcLists", mapper.writeValueAsString(dcLists));
		     	 model.addAttribute("partyList", mapper.writeValueAsString(partyList));
		     	model.addAttribute("role", mapper.writeValueAsString(role));
				 model.addAttribute("pageHeader","Archived DC");
				 return "archivedDcDashboard";
			}
		 
		 @GetMapping("/grn_archived")
			public String grnListArchivedDashboard(Model model) throws JsonProcessingException {
		          List<PurchaseOrder> poList = purchaseService.findAll();
		          List<Grn> grnList = grnService.getGrnListArchived();
		          User userObj  = userService.getCurrentUser();
		          String role = userObj.getRole();
		          ObjectMapper mapper=utilService.getObjectMapper();
		          model.addAttribute("poList", mapper.writeValueAsString(poList));
		          model.addAttribute("grnList", mapper.writeValueAsString(grnList));
		          model.addAttribute("role", mapper.writeValueAsString(role));
		          model.addAttribute("pageHeader","Archived GRN");
		          return "archivedGoodsReceiptNote";
			}	
		 
		 @PostMapping("/api/update_po_archive")
			public ResponseEntity<?> updatePoArchive(@RequestParam("poNum") String poNum) {
				purchaseService.archivePO(poNum);
				return new ResponseEntity<>(HttpStatus.OK);
			}
		 
		 @PostMapping("/api/update_po_unarchive")
			public ResponseEntity<?> updatePoUnArchive(@RequestParam("poNum") String poNum) {
			 purchaseService.unArchivePO(poNum);
				return new ResponseEntity<>(HttpStatus.OK);
			}
		 @PostMapping("/api/update_dc_archive")
			public ResponseEntity<?> updateDCArchive(@RequestParam("dcNum") int dcNum) {
				deliveryChallanService.archiveDC(dcNum);
				return new ResponseEntity<>(HttpStatus.OK);
			}
		 
		 @PostMapping("/api/update_dc_unarchive")
			public ResponseEntity<?> updateDCUnArchive(@RequestParam("dcNum") int dcNum) {
			 deliveryChallanService.unArchiveDC(dcNum);
				return new ResponseEntity<>(HttpStatus.OK);
			}
		 
		 @PostMapping("/api/update_grn_archive")
			public ResponseEntity<?> updateGrnArchive(@RequestParam("grnNum") String grnNum) {
				grnService.archiveGrn(grnNum);
				return new ResponseEntity<>(HttpStatus.OK);
			}
		 
		 @PostMapping("/api/update_grn_unarchive")
			public ResponseEntity<?> updateGrnUnArchive(@RequestParam("grnNum") String grnNum) {
			 grnService.unArchiveGrn(grnNum);
				return new ResponseEntity<>(HttpStatus.OK);
			}

		 @PostMapping("/api/purchase/upload")
		    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) throws Exception {
		    	 if (file.isEmpty()) {
		             return ResponseEntity.badRequest().body("Please upload a file.");
		         }

		         List<String> errors = purchaseUploadExcel.processExcelFile(file);

		         if (!errors.isEmpty()) {
		             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
		         }

		         return ResponseEntity.ok("File uploaded and processed successfully.");
		    }
		    
		@GetMapping("/grn/download/{grnId}")
		 public ModelAndView dgrnDetails(HttpServletRequest request,Model model,
		 		 @PathVariable("grnId") String grnId){
		 	Optional<Grn> grnObj = grnService.getGrnById(grnId);
		 	Map<String, Object>  grnData = new HashMap<String, Object> ();
		     grnData.put("grnObj", grnObj.get());
		     Optional<PurchaseOrder> poObj = purchaseService.findById(grnObj.get().getPoNumber());
		 	 User userObj  = userService.getCurrentUser();
		 	 request.setAttribute("user", userObj);
		 	request.setAttribute("poObj", poObj.get());
		 	request.setAttribute("purchaseService", purchaseService);
		 	request.setAttribute("purchaseItemService", purchaseItemService);
		 	request.setAttribute("itemMasterService", itemMasterService);
		     
		     return new ModelAndView(new GrnExcel(), "grnData", grnData);
		 }
		
		 @GetMapping("/api/stock/qty_details")
			public ResponseEntity<?> stockQuantityDetails(HttpServletRequest request, Model model, @RequestParam("model") String modelNo, @RequestParam("poItemId") String poItemId) throws JsonProcessingException {

			Map<String, Object> grnQtyList = grnService.getPresentStockQtyForModel(modelNo,poItemId);
				return new ResponseEntity<>(grnQtyList,HttpStatus.OK) ;

			}
		 @PostMapping("/add/companyAssets")
			public  String saveCompanyAssets(CompanyAssets companyAssets,HttpServletRequest req) {
				System.out.println(companyAssets);
			
			companyAssetService.saveCompanyAssets(companyAssets);
			return "redirect:/companyAssets";
			

		  }   
		 
		 @GetMapping("/api/employee_list")
		 public  ResponseEntity<?> getEmployeeMasterList() {
			List<EmployeeMaster> employeeList= employeeService.getEmployeeList();
			
				return new ResponseEntity<>(employeeList,HttpStatus.OK) ;
		 }
		 
		 
		 @GetMapping("/companyAssets")
			public String getCompanyAssets(Model model) throws JsonProcessingException {
				List<CompanyAssets> companyAssetList=companyAssetService.getAllCompanyAssetList();
				User userObj  = userService.getCurrentUser();
				String role = userObj.getRole();
				String user=userObj.getUsername();
				ObjectMapper mapper = utilService.getObjectMapper();
				model.addAttribute("role", mapper.writeValueAsString(role));
				model.addAttribute("user", mapper.writeValueAsString(user));
				model.addAttribute("companyAssetList", mapper.writeValueAsString(companyAssetList));
				model.addAttribute("pageHeader", "Company Assets");
				return "companyAssets";
			}
}
	



