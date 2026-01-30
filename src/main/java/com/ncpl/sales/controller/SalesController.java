/*
 * @Author
 * @Copyright
 * @date
 */
package com.ncpl.sales.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.ncpl.sales.model.Category;
import com.ncpl.sales.model.City;
import com.ncpl.sales.model.Country;
import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Designation;
import com.ncpl.sales.model.FileEntity;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.NonBillable;
import com.ncpl.sales.model.NonBillableItems;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PartyAddress;
import com.ncpl.sales.model.PartyBank;
import com.ncpl.sales.model.PartyCategory;
import com.ncpl.sales.model.PartyContact;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.Returnable;
import com.ncpl.sales.model.ReturnableItems;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.model.State;
import com.ncpl.sales.model.Tds;
import com.ncpl.sales.model.TdsItems;
import com.ncpl.sales.model.Type;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.model.WorkOrder;
import com.ncpl.sales.model.WorkOrderItems;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.GrnItemRepo;
import com.ncpl.sales.repository.PartyContactRepo;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.security.User;
import com.ncpl.sales.security.UserService;
import com.ncpl.sales.service.ActiveSalesItemExcel;
import com.ncpl.sales.service.CategoryService;
import com.ncpl.sales.service.CityService;
import com.ncpl.sales.service.CountryService;
import com.ncpl.sales.service.DeliveryChallanService;
import com.ncpl.sales.service.DesignUploadService;
import com.ncpl.sales.service.DesignationService;
import com.ncpl.sales.service.GrnReportByDateExcel;
import com.ncpl.sales.service.GrnService;
import com.ncpl.sales.service.InvoiceService;
import com.ncpl.sales.service.ItemMasterService;
import com.ncpl.sales.service.MaterialTrackerExcel;
import com.ncpl.sales.service.NonBillableService;
import com.ncpl.sales.service.OptimizedMaterialTrackerService;
import com.ncpl.sales.service.OptimizedMaterialTrackerExcel;
import com.ncpl.sales.service.PartyAddressService;
import com.ncpl.sales.service.PartyBankService;
import com.ncpl.sales.service.PartyCategoryService;
import com.ncpl.sales.service.PartyContactService;
import com.ncpl.sales.service.PartyService;
import com.ncpl.sales.service.PurchaseExcel;
import com.ncpl.sales.service.PurchaseItemService;
import com.ncpl.sales.service.PurchaseOrderCustomProperty;
import com.ncpl.sales.service.PurchaseOrderService;
import com.ncpl.sales.service.ReturnableService;
import com.ncpl.sales.service.SOUploadService;
import com.ncpl.sales.service.SalesOrderDesignService;
import com.ncpl.sales.service.SalesOrderDownloadExcel;
import com.ncpl.sales.service.SalesOrderExcel;
import com.ncpl.sales.service.SalesOrderUploadService;
import com.ncpl.sales.service.SalesService;
import com.ncpl.sales.service.StateService;
import com.ncpl.sales.service.StockService;
import com.ncpl.sales.service.StockSummaryByRegionExcel;
import com.ncpl.sales.service.TdsService;
import com.ncpl.sales.service.TypeService;
import com.ncpl.sales.service.WorkOrderService;
import com.ncpl.sales.service.stocksummaryExcel;
import com.ncpl.sales.util.DateConverterUtil;
import com.ncpl.sales.util.NcplUtil;

@Controller
public class SalesController {

	private static final Logger log = LoggerFactory.getLogger(SalesController.class);

	@Autowired
	NcplUtil utilService;

	@Autowired
	SalesService salesService;

	@Autowired
	PartyService partyService;
	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	CityService cityService;
	@Autowired
	StateService stateService;
	@Autowired
	CategoryService categoryService;

	@Autowired
	PartyContactService partyContactService;
	@Autowired
	PartyCategoryService partyCategoryService;
	@Autowired
	PartyAddressService partyAddressService;
	@Autowired
	PartyBankService partyBankService;

	@Autowired
	TypeService typeService;
	@Autowired
	CountryService countryService;
	@Autowired
	DesignationService designationService;

	@Autowired
	GrnService grnService;

	@Autowired
	GrnItemRepo grnRepo;
	@Autowired
	DateConverterUtil convertDate;
	@Autowired
	DeliveryChallanItemsRepo dcItemRepo;

	@Autowired
	PartyContactRepo partyContactRepo;
	@Autowired
	PurchaseOrderCustomProperty customProperty;
	@Autowired
	PurchaseOrderService purchaseOrderService;
	@Autowired
	PurchaseItemService purchaseItemService;
	@Autowired
	UserService userService;
	@Autowired
	SalesOrderDesignService designService;
	@Autowired
	PartyRepo partyRepo;
	@Autowired
	InvoiceService invoiceService;
	@Autowired
	DeliveryChallanService dcService;
	@Autowired
	StockService stockService;
	
	@Autowired
	ReturnableService returnableService;
	@Autowired
	WorkOrderService workOrderService;
	
	@Autowired
	DeliveryChallanService deliveryChallanService;
	
	@Autowired
	EntityManager em;
	
	@Autowired
	TdsService tdsService;
	
	@Autowired
	NonBillableService nonBilableService;
	
	@Autowired
	OptimizedMaterialTrackerService optimizedMaterialTrackerService;
	
	@Autowired
	PartyAddressService addressService;
	@Autowired
	SalesOrderUploadService excelUploadService;
	@Autowired
	SOUploadService excelProcessingService;
	@Autowired
	DesignUploadService designUploadService;

	@GetMapping("/welcome")

	public String welcome(Model model) throws JsonProcessingException {
		List<ItemMaster> itemList = itemMasterService.getItemList();
		List<Units> unitsList = itemMasterService.getUnitList();
		List<SalesOrder> salesList = salesService.getSalesOrderList();
		List<Party> customerpartyList = partyService.getPartyListByTypeCustomer();
		User userObj  = userService.getCurrentUser();
		String userName = userObj.getUsername();
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("userName", mapper.writeValueAsString(userName));
		model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		model.addAttribute("salesOrder", new SalesOrder());
		model.addAttribute("unitsList", mapper.writeValueAsString(unitsList));
		model.addAttribute("salesOrderList", mapper.writeValueAsString(salesList));
		model.addAttribute("customerPartyList", mapper.writeValueAsString(customerpartyList));
		model.addAttribute("role", mapper.writeValueAsString(userObj.getRole()));
		model.addAttribute("pageHeader", "Sales Order");
		//stockService.getStockValueForAllItems();
		// model.addAttribute("user", userService.getCurrentUser());
		//userService.save();
		//itemMasterService.saveUnits();
		return "welcome";

	}

// Below are the apis for getting report of outstanding,dc,po
	@GetMapping("/stock/outstandingReport/")
	public String outStandingReport(HttpServletRequest request, Model model) throws JsonProcessingException {
		String name = request.getParameter("client");
		Party partyObj = partyService.getPartyById(name);
		List<Object> oustandingStockList = stockService.getStockListByClientId(partyObj.getId());
		model.addAttribute("pageHeader", "Pending Outstanding Report - " + partyObj.getPartyName());
		model.addAttribute("outstandingStockList", oustandingStockList);
		return "outstandingreport";
	}

	@GetMapping("/stock/pendingporeport/")
	public String pendingPoReport(HttpServletRequest request, Model model) throws JsonProcessingException {

		String name = request.getParameter("clientInPendingReport");
		Party partyObj = partyService.getPartyById(name);
		List<Object> pendingPoList = purchaseOrderService.getPendingPoList(name);
		model.addAttribute("pageHeader", "Pending PO Report - " + partyObj.getPartyName());
		model.addAttribute("pendingList", pendingPoList);
		return "pendingporeport";

	}

	@GetMapping("/stock/dcReport/")
	public String pendingDcReport(HttpServletRequest request, Model model) throws JsonProcessingException {

		String name = request.getParameter("clientDcPEnding");
		Party partyObj = partyService.getPartyById(name);
		// List<Object> pendingDcList = new ArrayList();
		List<Object> pendingDcList = salesService.getPendingDcList(name);
		model.addAttribute("pageHeader", "Pending DC Report - " + partyObj.getPartyName());
		model.addAttribute("pendingDcList", pendingDcList);
		return "pendingdcreport";

	}

	@PostMapping("/add/salesOrder")
	public String saveSalesOrder(@ModelAttribute @Valid SalesOrder salesOrder, Errors errors, HttpServletRequest req)
			throws Exception {
		String partyId = req.getParameter("party");

		if (errors.hasErrors()) {
			System.out.println("Error....." + errors.getFieldError());
		}
		salesService.savesales(salesOrder, partyId);

		return "redirect:/salesList";

	}

	@GetMapping("/salesList")
	public String salesOrderList(Model model) throws JsonProcessingException, ParseException {
		//salesService.getSoDetailsForChart("2022");
		List<SalesOrder> salesList = salesService.getSalesOrderList();
		User userObj  = userService.getCurrentUser();
		String role = userObj.getRole();
		String user = userObj.getUsername();
		ObjectMapper mapper = utilService.getObjectMapper();
		model.addAttribute("salesOrderList", mapper.writeValueAsString(salesList));
		model.addAttribute("role", mapper.writeValueAsString(role));
		model.addAttribute("user", mapper.writeValueAsString(user));
		model.addAttribute("pageHeader", "Sales List");
		return "salesDashboard";
	}

	@GetMapping("/grnList")
	public String grnOrderList(Model model) throws JsonProcessingException {
		List<SalesOrder> salesList = salesService.getSalesOrderList();
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("salesOrderList", mapper.writeValueAsString(salesList));
		model.addAttribute("pageHeader", "GRN List");
		return "goodsReceiptNote";
	}

	@GetMapping("/new_salesOrder")
	public String newSalesOrder(Model model, HttpServletRequest req) throws JsonProcessingException {
		Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
		model.addAttribute("salesOrder", new SalesOrder());
		List<Units> unitsList = itemMasterService.getUnitList();
		List<SalesOrder> salesList = salesService.getSalesOrderList();
		List<Party> customerpartyList = partyService.getPartyListByTypeCustomer();
		List<ItemMaster> itemList = itemMasterService.getItemList();
		User userObj  = userService.getCurrentUser();
		String userName = userObj.getUsername();
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("userName", mapper.writeValueAsString(userName));
		model.addAttribute("itemList", mapper.writeValueAsString(itemList));
		model.addAttribute("unitsList", mapper.writeValueAsString(unitsList));
		model.addAttribute("customerPartyList", mapper.writeValueAsString(customerpartyList));
		model.addAttribute("salesOrderList", mapper.writeValueAsString(salesList));
		model.addAttribute("role", mapper.writeValueAsString(userObj.getRole()));
		model.addAttribute("pageHeader", "Sales Order");
		if (flashMap != null) {
			SalesOrder salesOrder = (SalesOrder) flashMap.get("salesOrderObj");

			model.addAttribute("salesOrderObj", mapper.writeValueAsString(salesOrder));

		}
		return "welcome";
	}

	@GetMapping("/party")
	public String partyMaster(Model model, HttpServletRequest req) throws JsonProcessingException {

		Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);

		model.addAttribute("pageHeader", "Party");
		model.addAttribute("party", new Party());

		List<Party> partyList = partyService.getPartyList();
		List<City> cityList = cityService.getCityList();
		model.addAttribute("cityList", cityList);
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("categoryList", mapper.writeValueAsString(cityList));
		List<Category> categoryList = categoryService.getCategoryList();
		model.addAttribute("categoryList", mapper.writeValueAsString(categoryList));
		model.addAttribute("partyList", mapper.writeValueAsString(partyList));

		if (flashMap != null) {
			Party partyObj = (Party) flashMap.get("partyObj");
			// ObjectMapper mapper = new ObjectMapper();
			model.addAttribute("partyObj", mapper.writeValueAsString(partyObj));
			model.addAttribute("partyId", partyObj.getId());
			int addressCount = partyAddressService.getTotalNoofAddress(partyObj.getId());
			model.addAttribute("addressCount", addressCount);
			int bankDetailsCount = partyBankService.getTotalNoofBankDetails(partyObj.getId());
			model.addAttribute("bankDetailsCount", bankDetailsCount);
			int partyCategoryListCount = partyCategoryService.getPartyCategoryCountbyPartyId(partyObj.getId());
			model.addAttribute("partyCategoryListCount", partyCategoryListCount);
		}
		return "party";

	}

	@PostMapping("/add/party")
	public String saveParty(@ModelAttribute("party") Party party, Model model, HttpServletRequest req) throws JsonProcessingException {
	    
	    // ==================== VALIDATE PIN ====================
	    if (party.getPin() == null || party.getPin().trim().isEmpty()) {
	        model.addAttribute("party", party);
	        model.addAttribute("pinError", "Please enter the pin");
	        List<City> cityList = cityService.getCityList();
	        model.addAttribute("cityList", cityList);
	        ObjectMapper mapper = new ObjectMapper();
	        List<Category> categoryList = categoryService.getCategoryList();
	        model.addAttribute("categoryList", mapper.writeValueAsString(categoryList));
	        model.addAttribute("partyList", mapper.writeValueAsString(partyService.getPartyList()));
	        return "party";
	    }
	    // ==================== END PIN VALIDATION ====================
		String interState = req.getParameter("interStateHidden");
		
		if (interState.equalsIgnoreCase("true")) {
			party.setInterState(interState);
		} else {
			party.setInterState("false");
		}
		System.out.println("this is sending party....." + party.toString());
		if (party.getId() == "" || party.getId() == null) {
			partyService.saveParty(party);
		} else {
			List<PartyContact> list = party.getContacts();
			party.setContacts(list);
			List<PartyCategory> partyCategoryList = partyCategoryService.getPartyCategorybyPartyId(party.getId());
			party.setgetcategories(partyCategoryList);
			List<SalesOrder> salesList = salesService.getSalesListByParty(party.getId());
			party.setSalesOrder(salesList);
			List<PurchaseOrder> purchaseOrderList = purchaseOrderService.getPurchaseOrderListByPartyId(party.getId());
			party.setPurchaseOrder(purchaseOrderList);
			partyService.saveParty(party);
		}
		System.out.println(party);

		return "redirect:/partyList";
	}

	@PostMapping("/add/party#")
	public String savePartyContacts(@ModelAttribute("partyContact") PartyContact partyContact) {

		// System.out.println(partyContact.toString());

		partyContactService.savePartyContacts(partyContact);
		return "redirect:/partyList";
	}

	@GetMapping("/partyList")
	public String partyList(Model model) throws JsonProcessingException {
		List<Party> partyList = partyService.getPartyList();

		for (Party party : partyList) {
			int partyCategoryListCount = partyCategoryService.getPartyCategoryCountbyPartyId(party.getId());
			party.setCategory(partyCategoryListCount);
		}

		for (Party p : partyList) {
			System.out.println("This is party" + p);
		}
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("partyList", mapper.writeValueAsString(partyList));
		model.addAttribute("pageHeader", "Party List");
		return "partyList";

	}

	@GetMapping("/api/partyListDropdown")
	public ResponseEntity<List<Party>> partyListDropdown() {
		List<Party> partyList = partyService.getPartyList();
		return new ResponseEntity<List<Party>>(partyList, HttpStatus.OK);
	}

	@GetMapping("/api/purchaseOrderDropDown")
	public ResponseEntity<List<SalesOrder>> purchaseOrderDropDown() {
		List<SalesOrder> salesList = salesService.getSalesOrderList();

		System.out.println("This is sales list" + salesList);
		return new ResponseEntity<List<SalesOrder>>(salesList, HttpStatus.OK);

	}

	/**
	 * 
	 * @return list of all the cities
	 */

	@GetMapping("/api/city-list/")
	public ResponseEntity<?> getCitiesList() {
		List<City> cityList = cityService.getCityList();
		return new ResponseEntity<>(cityList, HttpStatus.OK);
	}

	@GetMapping("api/state/byId")
	public ResponseEntity<?> getStateById(@RequestParam("id") Long id) {
		Optional<State> state = stateService.findSateById(id);
		return new ResponseEntity<>(state, HttpStatus.OK);
	}

	// API For populating state country on selection of city
	@GetMapping("/api/city/{id}")
	public ResponseEntity<?> getStateByCity(@PathVariable("id") int id) {
		Optional<City> city = cityService.findCityById(id);

		return new ResponseEntity<>(city, HttpStatus.OK);

	}

	// category list
	@GetMapping("/api/list/category")
	public ResponseEntity<?> bySalesOrderId() {
		List<Category> categoryList = categoryService.getCategoryList();
		return new ResponseEntity<>(categoryList, HttpStatus.OK);
	}

	// save category
	@PostMapping("/api/add/category")
	public ResponseEntity<?> saveCategory(Category category) {
		Boolean savedCategory = categoryService.saveCategory(category);
		return new ResponseEntity<>(savedCategory, HttpStatus.OK);
	}

	// delete category
	@PostMapping("/api/category/delete")
	public ResponseEntity<?> deleteCategory(@RequestParam("id") int categoryId) {
		categoryService.deleteCategory(categoryId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// validate category
	@GetMapping("/api/category/validate")
	public ResponseEntity<?> checkDuplicateCategory(@RequestParam("name") String name,
			@RequestParam("id") Integer categoryId) {

		boolean categoryExist = categoryService.checkCategoryNameExists(name, categoryId);

		return new ResponseEntity<>(categoryExist, HttpStatus.OK);
	}

	// API for displaying sales item on selecting sales Order
	@GetMapping("/api/sales-order")
	public ResponseEntity<?> bySalesOrderId(@RequestParam("id") List<String> id,
			@RequestParam("className") String className, Model model) {
		try {
			log.info("API called with id: {} and className: {}", id, className);
			List<SalesItem> salesItem = salesService.getSalesListById(id, className);
			log.info("Returning {} sales items", salesItem.size());
			return new ResponseEntity<>(salesItem, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Error in bySalesOrderId API", e);
			Map<String, String> errorResponse = new HashMap<>();
			errorResponse.put("errorCode", "500");
			errorResponse.put("errorMessage", "Error retrieving sales order data: " + e.getMessage());
			return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	// Test endpoint to verify API is working
	@GetMapping("/api/test")
	public ResponseEntity<?> testApi() {
		Map<String, String> response = new HashMap<>();
		response.put("status", "success");
		response.put("message", "API is working correctly");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@GetMapping("/api/sales_item_list_by_soId")
	public ResponseEntity<?> bySalesOrderBySOId(@RequestParam("id") List<String> id,
			@RequestParam("className") String className,@RequestParam("vendorId") String vendorId, Model model) {
		List<SalesItem> salesItem = salesService.getSalesListByVendorId(id, className,vendorId);
		return new ResponseEntity<>(salesItem, HttpStatus.OK);
	}
	
	@GetMapping("/api/salesitem_list_by_id")
	public ResponseEntity<?> getSalesItemListById(@RequestParam("id") List<String> id,
			@RequestParam("className") String className, Model model) {
		List<SalesItem> salesItem = salesService.getSalesListBySalesOrderId(id, className);
		return new ResponseEntity<>(salesItem, HttpStatus.OK);
	}

	// API for getting salesItem by sales Item id
	@GetMapping("/api/sales-Item")
	public ResponseEntity<?> bySalesItemId(@RequestParam("id") String salesItemId) {
		boolean value = true;
		Optional<SalesItem> salesItem = salesService.getSalesItemById(salesItemId, value);
		return new ResponseEntity<>(salesItem, HttpStatus.OK);
	}
	/*
	 * Satish Edit This is used to fetch the data of category from party class
	 */
	/*
	 * @GetMapping("/") public ModelAndView getParty(){
	 * 
	 * ModelAndView mv= new ModelAndView(); List<Party>
	 * partyList=partyService.getPartyList();
	 * 
	 * List<String> categoryList= new ArrayList<String>();
	 * 
	 * for(Party p: partyList) { categoryList.add(p.getCategory()); }
	 * 
	 * mv.addObject("categoryList", categoryList); mv.setViewName("party"); return
	 * mv;
	 * 
	 * }
	 */

	/* This is used to send the Category Id to Service class */

	@GetMapping("/api/category/{id}")
	public ResponseEntity<?> getCategoryById(@PathVariable("id") int id) {
		Optional<Category> category = categoryService.findCategoryById(id);

		return new ResponseEntity<>(category, HttpStatus.OK);

	}
	/* This is used to call the service class to find the categoryList */

	/*
	 * @GetMapping("/api/categoryList") public ResponseEntity<?> getCategory() {
	 * List<Category> categoryList = categoryService.getCategoryList();
	 * 
	 * return new ResponseEntity<>(categoryList,HttpStatus.OK);
	 * 
	 * }
	 */

	/**
	 * get party object by id and redirect to party
	 * 
	 * @param partyId
	 * @param redirectAttr
	 * @param model
	 * @return
	 */
	@GetMapping("/api/party/view")
	public String displayEditParty(@RequestParam("partyId") String partyId, RedirectAttributes redirectAttr,
			Model model) {

		Party party = partyService.getPartyById(partyId);

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

		String name = party.getPartyName();
		name = name.replace("'", "&");
		name = name.replace("\"", "&");
		party.setPartyName(name);

		redirectAttr.addFlashAttribute("partyObj", party);
		return "redirect:/party";
	}

	/**
	 * get contact list by party id
	 * 
	 * @param partyId
	 * @return contactList
	 */
	@GetMapping("/api/party-contact")
	public ResponseEntity<?> contactByPartyId(@RequestParam("partyId") String partyId) {
		List<PartyContact> contactList = partyContactService.getContactById(partyId);
		return new ResponseEntity<>(contactList, HttpStatus.OK);
	}

	// For saving partyCategory
	@GetMapping("/api/save/Party_category")
	public void savePartyCategory(@RequestParam("id") String partyId,
			@RequestParam("categoryId") List<String> categoryIds) {

		// To avoid saving data twice deleting the partycategories associated..
		List<PartyCategory> partyCategoryList = partyCategoryService.getPartyCategorybyPartyId(partyId);
		if (partyCategoryList.size() > 0) {
			for (PartyCategory partyCategory : partyCategoryList) {

				partyCategoryService.deleteById(partyCategory.getId());
			}
		}

		// As there are list of categories so saving each with party

		for (String string : categoryIds) {
			int id = Integer.parseInt(string);
			PartyCategory partyCategory = new PartyCategory();
			Party party = partyService.getPartyById(partyId);
			partyCategory.setParty(party);
			Optional<Category> category = categoryService.findCategoryById(id);
			partyCategory.setCategory_id(category.get());
			partyCategoryService.savepartyCategory(partyCategory);
		}
	}

	@GetMapping("/api/party_category/partyId")
	public ResponseEntity<List<PartyCategory>> savePartyCategory(@RequestParam("id") String partyId) {
		List<PartyCategory> partyCategoryList = partyCategoryService.getPartyCategorybyPartyId(partyId);
		return new ResponseEntity<List<PartyCategory>>(partyCategoryList, HttpStatus.OK);
	}

	@RequestMapping(value = "/partyAltAddress", method = RequestMethod.GET)
	public String gerPartyAddressPage(@RequestParam("partyId") String partyId, Model model)
			throws JsonProcessingException {

		Party party = partyService.getPartyById(partyId);
		model.addAttribute("pageHeader", "Party Address");
		List<City> cityList = cityService.getCityList();
		model.addAttribute("cityList", cityList);
		List<PartyAddress> addressList = partyAddressService.getAddressById(partyId);
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("AddressList", mapper.writeValueAsString(addressList));

		model.addAttribute("Party Address", new PartyAddress());
		model.addAttribute("partyName", party.getPartyName());
		model.addAttribute("partyId", party.getId());
		int bankCountinAddress = partyBankService.getTotalNoofBankDetails(party.getId());
		model.addAttribute("bankCountinAddress", bankCountinAddress);
		return "partyAddress";
	}

	@PostMapping("/add/partyAddress")
	public String savePartyAddress(@ModelAttribute("partyaddress") PartyAddress partyAddress,
			HttpServletRequest request) {
		String partyAddressBtnVal = request.getParameter("savePartyAddress");
		String id = request.getParameter("partyId");
		Party party = partyService.getPartyById(id);
		partyAddress.setParty(party);
		// Party Address id not null then update
		if (partyAddress.getId() != "") {
			partyAddressService.updatePartyAddress(partyAddress);
		} else {
			partyAddressService.savePartyAddress(partyAddress);
		}
		// partyAddressService.savePartyAddress(partyAddress);
		// if the value is of save button then redirecting to same page else if
		// it is save&exit button then redirecting to partyList page
		if (partyAddressBtnVal.equalsIgnoreCase("partyAddressSave")) {
			return "redirect:/partyAltAddress?partyId=" + id;
		} else {
			return "redirect:/partyList";
		}

	}

	/**
	 * get address by Address Id
	 * 
	 * @param id
	 * @return partyAddress
	 */
	@GetMapping("/api/party_address/by_id")
	public ResponseEntity<Optional<PartyAddress>> getPartyAddressByAddressId(@RequestParam("id") String id) {
		Optional<PartyAddress> partyAddress = partyAddressService.getAddressByAddressId(id);
		return new ResponseEntity<Optional<PartyAddress>>(partyAddress, HttpStatus.OK);
	}

	@PostMapping("/api/partyAltAddress/delete")
	public ResponseEntity<?> deletePartyAddress(@RequestParam("id") String id) {

		partyAddressService.deletePartyAddress(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/partyBank", method = RequestMethod.GET)
	public String getPartyBankPage(@RequestParam("partyId") String partyId, Model model)
			throws JsonProcessingException {

		Party party = partyService.getPartyById(partyId);
		List<PartyBank> partyBankList = partyBankService.getPartyBankBypartyId(partyId);

		model.addAttribute("pageHeader", "Party Bank");
		model.addAttribute("Party Bank", new PartyBank());
		model.addAttribute("partyName", party.getPartyName());
		model.addAttribute("partyId", party.getId());
		int addressCountinbank = partyAddressService.getTotalNoofAddress(party.getId());
		model.addAttribute("addressCountinbank", addressCountinbank);
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("partyBankList", mapper.writeValueAsString(partyBankList));
		return "partyBank";
	}

	@PostMapping("/add/partyBank")
	public String savePartyBank(@ModelAttribute("Party Bank") PartyBank partyBank, HttpServletRequest request) {
		String partyBankBtnVal = request.getParameter("savePartyBank");

		String id = request.getParameter("partyId");
		Party party = partyService.getPartyById(id);
		partyBank.setParty(party);
		partyBankService.savePartyBank(partyBank);
		// if the value is of save button then redirecting to same page else if
		// it is save&exit button then redirecting to partyList page
		if (partyBankBtnVal.equalsIgnoreCase("partyBankSave")) {
			return "redirect:/partyBank?partyId=" + id;
		} else {
			return "redirect:/partyList";
		}
	}

	@PostMapping("/api/partyBank/delete")
	public ResponseEntity<?> deletePartyBank(@RequestParam("id") Integer id) {
		partyBankService.deletePartyBankDetails(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/api/type/list")
	public ResponseEntity<List<Type>> getTypeList() {
		List<Type> typeList = typeService.getTypeList();
		return new ResponseEntity<List<Type>>(typeList, HttpStatus.OK);
	}

	@PostMapping("/api/type/add")
	public ResponseEntity<?> saveType(Type type) {
		Boolean savedType = typeService.saveType(type);
		return new ResponseEntity<>(savedType, HttpStatus.OK);
	}

	@PostMapping("/api/type/delete")
	public ResponseEntity<?> deleteType(@RequestParam("id") Integer id) {
		typeService.deleteType(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	// validate category
	@GetMapping("/api/type/validate/{name}/{id}")
	public ResponseEntity<?> checkDuplicateType(@PathVariable("name") String name, @PathVariable("id") Integer typeId) {

		boolean typeExist = typeService.checkTypeNameExists(name, typeId);

		return new ResponseEntity<>(typeExist, HttpStatus.OK);
	}

	@GetMapping("/api/state/list")
	public ResponseEntity<List<State>> getStateList() {
		List<State> stateList = stateService.stateList();
		return new ResponseEntity<List<State>>(stateList, HttpStatus.OK);
	}

	@GetMapping("/api/country/list")
	public ResponseEntity<List<Country>> getCountryList() {
		List<Country> countryList = countryService.countryList();
		return new ResponseEntity<List<Country>>(countryList, HttpStatus.OK);
	}

	@PostMapping("/api/country/add")
	public ResponseEntity<?> saveCountry(Country country) {
		Boolean savedCountry = countryService.saveCountry(country);
		return new ResponseEntity<>(savedCountry, HttpStatus.OK);
	}

	@PostMapping("/api/state/add")
	public ResponseEntity<?> saveState(State state) {
		Boolean savedState = stateService.saveState(state);
		return new ResponseEntity<>(savedState, HttpStatus.OK);
	}

	@PostMapping("/api/city/add")
	public ResponseEntity<?> saveCity(City city) {
		Boolean savedCity = cityService.saveCity(city);
		return new ResponseEntity<>(savedCity, HttpStatus.OK);

	}

	@PostMapping("/api/city/delete")
	public ResponseEntity<?> deleteCity(@RequestParam("id") int id) {
		cityService.deleteType(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/api/city/validate")
	public ResponseEntity<?> checkDuplicateCity(@RequestParam("name") String name, @RequestParam("id") Integer id) {

		boolean cityExists = cityService.checkCityNameExists(name, id);

		return new ResponseEntity<>(cityExists, HttpStatus.OK);
	}

	/**
	 * add designation
	 * 
	 * @param designation
	 * @return designationObj
	 */
	@PostMapping("/api/add/designation")
	public ResponseEntity<?> saveDesignation(Designation designation) {
		Designation designationObj = designationService.saveDesignation(designation);
		return new ResponseEntity<>(designationObj, HttpStatus.OK);
	}

	/**
	 * get list of designation
	 * 
	 * @return designationList
	 */
	@GetMapping(path = "/api/designation/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> designationList() {
		List<Designation> designationList = designationService.getAllDesignation();
		return new ResponseEntity<List<Designation>>(designationList, HttpStatus.OK);
	}

	/**
	 * delete designation by id
	 * 
	 * @param id
	 * @return
	 */
	@PostMapping("/api/designation/delete")
	public ResponseEntity<?> deleteDesignation(@RequestParam("id") Integer id) {
		designationService.deleteDesignation(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * check for duplicate designation by name
	 * 
	 * @param name
	 * @return designationExists
	 */
	@GetMapping("/api/designation/validate")
	public ResponseEntity<?> checkDuplicateDesignation(@RequestParam("name") String name,
			@RequestParam("id") Integer id) {

		boolean designationExists = designationService.checkDesignationNameExists(name, id);

		return new ResponseEntity<>(designationExists, HttpStatus.OK);
	}

	/**
	 * to display sales order view page
	 * 
	 * @param salesOrderId
	 * @param redirectAttr
	 * @param model
	 * @return
	 */
	@GetMapping("/api/sales_order/view")
	public String displaySalesOrderView(@RequestParam("salesOrderId") String salesOrderId,
			RedirectAttributes redirectAttr, Model model) {

		Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(salesOrderId);
		// This code is to overcome the pblm of single quote issue with client po and
		// addresses
		Party party = salesOrder.get().getParty();

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

		String clientPo = salesOrder.get().getClientPoNumber();
		String partyName = party.getPartyName();
		clientPo = clientPo.replace("'", "&");
		partyName = partyName.replace("\"", "&");
		partyName = partyName.replace("'", "&");
		clientPo = clientPo.replace("\"", "&");
		
		String otherTC=salesOrder.get().getOtherTermsAndConditions();
		otherTC=otherTC.replaceAll("'", "&");
		otherTC=otherTC.replaceAll("\"", "&");
		/*
		 * otherTC=otherTC.replaceAll("\\(", "&"); otherTC=otherTC.replaceAll("\\)",
		 * "&"); otherTC=otherTC.replaceAll("\\\\", "&");
		 */
		otherTC = otherTC.replaceAll("(\r\n|\n)", "");
		salesOrder.get().setClientPoNumber(clientPo);
		salesOrder.get().setOtherTermsAndConditions(otherTC);
		party.setPartyName(partyName);

		redirectAttr.addFlashAttribute("salesOrderObj", salesOrder.get());
		// salesService.updateSoStatusToWorkInProgress(salesOrder.get().getId());
		return "redirect:/new_salesOrder";
	}

	/**
	 * get all the partyAddress
	 * 
	 * @return partyAddressList
	 */
	@GetMapping(path = "/api/partyAddress/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getPartyAddressList() {
		List<PartyAddress> partyAddressList = partyAddressService.getAllPartyAddresses();
		return new ResponseEntity<List<PartyAddress>>(partyAddressList, HttpStatus.OK);
	}

	/**
	 * get party Address list by party Id
	 * 
	 * @param partyId
	 * @return partyAddressList
	 */
	@GetMapping(path = "/api/partyAddress/list_by_partyId", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getPartyAddressListByPartyId(@RequestParam("partyId") String partyId) {
		List<PartyAddress> partyAddressList = partyAddressService.getAddressById(partyId);
		return new ResponseEntity<List<PartyAddress>>(partyAddressList, HttpStatus.OK);
	}

	/**
	 * get terms and condition
	 * 
	 * @return addressMap
	 */
	@GetMapping("/api/so_address/terms_and_condition")
	public ResponseEntity<?> getSoAddressAndTermsAndCondition(@RequestParam("partyId") String partyId) {
		Map<Object, Object> addressMap = partyService.findPartById(partyId);

		addressMap.put("modeOfPayment", customProperty.getModeOfPayment());
		addressMap.put("jurisdiction", customProperty.getJursidiction());
		addressMap.put("frieght", customProperty.getFrieght());
		addressMap.put("delivery", customProperty.getDelivery());
		addressMap.put("warranty", customProperty.getWarranty());

		return new ResponseEntity<>(addressMap, HttpStatus.OK);
	}

	/**
	 * get sales order list by party Id
	 * 
	 * @param partyId
	 * @param model
	 * @return salesOrderList
	 */
	@GetMapping("/api/sales_order_list/by_party_id")
	public ResponseEntity<?> getSalesOrderlistByPartyId(@RequestParam("partyId") String partyId, Model model) {
		List<SalesOrder> salesOrderList = salesService.getSalesOrderListByPartyId(partyId);
		return new ResponseEntity<>(salesOrderList, HttpStatus.OK);
	}

	/**
	 * get sales order obj by id
	 * 
	 * @param salesOrderId
	 * @return salesOrder
	 */
	@GetMapping("/api/sales_order/by_id")
	public ResponseEntity<?> getSalesOrderById(@RequestParam("salesOrderId") String salesOrderId) {

		Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(salesOrderId);
		return new ResponseEntity<>(salesOrder, HttpStatus.OK);
	}

	// For getting the excel report between two dates

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/stock/summary_details/")
	public ModelAndView stocksummary(HttpServletRequest request, Model model) throws ParseException {
		String fromDateString = request.getParameter("reportFromDate");
		fromDateString = fromDateString.replaceAll("/", "-");
		String todateString = request.getParameter("reportToDate");
		todateString = todateString.replaceAll("/", "-");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Date todaysDate = null;
		Date fromDate = null;
		// Parsing date
		try {
			todaysDate = sdf.parse(todateString);
			fromDate = sdf.parse(fromDateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar c = Calendar.getInstance();
		Calendar c1 = Calendar.getInstance();
		c.setTime(todaysDate);
		c1.setTime(fromDate);
		// c.add(Calendar.DATE, 0);
		todaysDate = c.getTime();
		// c.add(Calendar.DATE, 30); //Last 30 days data
		fromDate = c1.getTime();
		c.add(Calendar.HOUR_OF_DAY, +23);
		c.add(Calendar.MINUTE, 59);
		todaysDate = c.getTime();
		Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
		Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);

		// Map<String, Map> pMap =
		// salesService.getstockSummarydetails(sqlFromDate,sqlToDate);

		Map<String, Map> pMap = grnService.findgrnListByDate(sqlFromDate, sqlToDate);

		// Map<String, Map> dcMap =
		// salesService.getDcdetails(sqlFromDate,sqlToDate,pMap);
		// Map<String, Map> grnMap =
		// salesService.getGrnSummarydetails(sqlFromDate,sqlToDate,pMap);
		int month = c.get(Calendar.MONTH);
		Month monthName = Month.of(month + 1);
		Map stockSummary = new HashMap();

		stockSummary.put("stockMap", pMap);
		stockSummary.put("monthName", monthName);
		// stockSummary.put("dcMap", dcMap);
		// stockSummary.put("grnMap", grnMap);
		return new ModelAndView(new stocksummaryExcel(), "stockSummary", stockSummary);
	}

	@PostMapping(value = "/api/add/design")
	public ResponseEntity<?> saveDesign(@Valid @RequestBody SalesOrderDesign designJson) {
		return new ResponseEntity<>(designService.save(designJson), HttpStatus.OK);
	}

	@GetMapping("/api/design_item_list/by_sales_item_id")
	public ResponseEntity<?> getSalesOrderDesignBySalesItemId(@RequestParam("salesItemId") String salesItemId,
			Model model) {
		List<DesignItems> designItemList = designService.getSalesOrderDesignItemListBySalesItemId(salesItemId);
		return new ResponseEntity<>(designItemList, HttpStatus.OK);
	}
	
	@GetMapping("/api/designItem_list/sales_item_id")
	public ResponseEntity<?> getDesignBySalesItemId(@RequestParam("salesItemId") String salesItemId,
			Model model) {
		List<DesignItems> designItemList = designService.getDesignItemListBySalesItemId(salesItemId);
		return new ResponseEntity<>(designItemList, HttpStatus.OK);
	}

	@PostMapping("/api/design/delete")
	public ResponseEntity<?> deleteDesign(@RequestParam("id") long id, @RequestParam("designId") long designId) {
		boolean isDeleted = designService.deleteDesignByDesignItemId(id, designId);
		return new ResponseEntity<>(isDeleted, HttpStatus.OK);
	}

	@GetMapping("/api/design/validate")
	public ResponseEntity<?> checkDuplicateDesign(@RequestParam("itemIdList") List<String> itemIdList,
			@RequestParam("salesItemId") String salesItemId) {

		boolean designExists = designService.checkDuplicateItemIdExists(itemIdList, salesItemId);

		return new ResponseEntity<>(designExists, HttpStatus.OK);
	}

	@PostMapping("/api/salesItem/delete")
	public ResponseEntity<?> deleteSalesItem(@RequestParam("salesItemId") String salesItemId) {
		salesService.deleteSalesItemById(salesItemId);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/api/design_list/by_sales_item_id")
	public ResponseEntity<?> getDesignListBySalesItemId(@RequestParam("salesItemId") String salesItemId, Model model) {
		List<SalesOrderDesign> designList = designService.findSalesOrderDesignBysalesItemId(salesItemId);
		return new ResponseEntity<>(designList, HttpStatus.OK);
	}

	@GetMapping("/dashboard")
	public String dashboard(Model model) throws JsonProcessingException {

		model.addAttribute("pageHeader", "Dashboard");
		return "dashboard";
	}

	@GetMapping("/api/sales_list/with_status_not_closed")
	public ResponseEntity<?> salesListWithStatusNotClosed(Model model) {
		List<SalesOrder> salesList = salesService.getSalesOrderListWithStatusNotClosed();
		return new ResponseEntity<>(salesList, HttpStatus.OK);
	}

	@GetMapping("/api/sales_list/pending")
	public ResponseEntity<?> salesListPending(Model model) {
		List<SalesOrder> salesList = salesService.getPendingSalesList();
		return new ResponseEntity<>(salesList, HttpStatus.OK);
	}

	@GetMapping("/api/purchase_List")
	public ResponseEntity<?> purchaseList(Model model) {
		List<PurchaseOrder> puchaseList = purchaseOrderService.findAllPO();
		return new ResponseEntity<>(puchaseList, HttpStatus.OK);
	}

	@GetMapping("/api/purchase_List/pending")
	public ResponseEntity<?> pendingPurchaseList(Model model) {
		List<PurchaseOrder> puchaseList = purchaseOrderService.getPenidngPoList();
		return new ResponseEntity<>(puchaseList, HttpStatus.OK);
	}

	@GetMapping("/api/invoice_List")
	public ResponseEntity<?> invoiceList(Model model) {
		List<Invoice> invoiceList = invoiceService.getInvoiceList();
		return new ResponseEntity<>(invoiceList, HttpStatus.OK);
	}

	@GetMapping("/api/get_all_sales_list")
	public ResponseEntity<?> allSalesList(Model model) {
		List<SalesOrder> salesList = salesService.getAllSalesOrderList();
		return new ResponseEntity<>(salesList, HttpStatus.OK);
	}

	@GetMapping("/api/project_preview")
	public String projectPreview(@RequestParam("salesOrderId") String salesOrderId, RedirectAttributes redirectAttr,
			Model model) {
		List<PurchaseOrder> purchaseList = salesService.getAllPoBySalesOrderId(salesOrderId);
		List<DeliveryChallan> dcList = dcService.getDcListBySoId(salesOrderId);
		List<Invoice> invoiceList = invoiceService.getInvoiceBySoId(salesOrderId);
		List<Grn> grnList = salesService.getGrnListBySoId(salesOrderId);
		redirectAttr.addFlashAttribute("dcList", dcList);
		redirectAttr.addFlashAttribute("invoiceList", invoiceList);
		redirectAttr.addFlashAttribute("purchaseList", purchaseList);
		redirectAttr.addFlashAttribute("grnList", grnList);

		return "redirect:/project_preview_page";
	}

	@GetMapping("/project_preview_page")
	public String getAllListInProjectPreviewPage(Model model, HttpServletRequest req) throws JsonProcessingException {
		Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
		model.addAttribute("pageHeader", "Project Preview");
		if (flashMap != null) {
			@SuppressWarnings("unchecked")
			List<DeliveryChallan> dcList = (List<DeliveryChallan>) flashMap.get("dcList");
			@SuppressWarnings("unchecked")
			List<Invoice> invoiceList = (List<Invoice>) flashMap.get("invoiceList");
			@SuppressWarnings("unchecked")
			List<PurchaseOrder> purchaseList = (List<PurchaseOrder>) flashMap.get("purchaseList");
			@SuppressWarnings("unchecked")
			List<Grn> grnList = (List<Grn>) flashMap.get("grnList");
			ObjectMapper mapper = new ObjectMapper();
			model.addAttribute("dcList", mapper.writeValueAsString(dcList));
			model.addAttribute("invoiceList", mapper.writeValueAsString(invoiceList));
			model.addAttribute("purchaseList", mapper.writeValueAsString(purchaseList));
			model.addAttribute("grnList", mapper.writeValueAsString(grnList));
			
		}
		return "projectPreview";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostMapping("/api/upload")
	@ResponseBody
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadfile) throws IOException {

		if (uploadfile.isEmpty()) {
			return new ResponseEntity("please select a file!", HttpStatus.OK);
		}

		salesService.saveUploadedFiles(Arrays.asList(uploadfile));

		return new ResponseEntity("Successfully uploaded - " + uploadfile.getOriginalFilename(), new HttpHeaders(),
				HttpStatus.OK);

	}

	@PostMapping("/api/check_dc_exists")
	public ResponseEntity<?> checkForDcExists(@RequestParam("salesItemId") String salesItemId) {

		boolean dcExists = salesService.checkForDcExists(salesItemId);

		return new ResponseEntity<>(dcExists, HttpStatus.OK);
	}

	@SuppressWarnings({ "rawtypes" })
	@GetMapping("/stock_report_by_region/Download")
	public ModelAndView stocksummarybyRegion(HttpServletRequest request, Model model) throws ParseException {
		String region = request.getParameter("region");
		String fromDateString = request.getParameter("reportByRegionFromDate");
		fromDateString = fromDateString.replaceAll("/", "-");
		String todateString = request.getParameter("reportByRegionToDate");
		todateString = todateString.replaceAll("/", "-");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Date todaysDate = null;
		Date fromDate = null;
		// Parsing date
		try {
			todaysDate = sdf.parse(todateString);
			fromDate = sdf.parse(fromDateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Calendar c = Calendar.getInstance();
		Calendar c1 = Calendar.getInstance();
		c.setTime(todaysDate);
		c1.setTime(fromDate);
		// c.add(Calendar.DATE, 0);
		todaysDate = c.getTime();
		// c.add(Calendar.DATE, 30); //Last 30 days data
		fromDate = c1.getTime();
		c.add(Calendar.HOUR_OF_DAY, +23);
		c.add(Calendar.MINUTE, 59);
		todaysDate = c.getTime();
		Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
		Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);
		// List<Object> results = em.createQuery("SELECT p.description,
		// n.purchase_item_id,n.poDescription FROM GrnItems p INNER JOIN PurchaseItem n
		// ON p.description = n.purchase_item_id where
		// p.description='20'").getResultList();
		Map stockByRegionMap = salesService.getStockByRegionBetweenDates(sqlFromDate, sqlToDate, region);
		return new ModelAndView(new StockSummaryByRegionExcel(), "stockByRegion", stockByRegionMap);
	}
	
	@GetMapping("/api/sales-items-without-design")
	public ResponseEntity<?> salesItemWithoutDesign(@RequestParam("salesOrderId") String salesOrderId) {
		List<SalesItem> salesItem = salesService.getSalesItemListWithoutDesign(salesOrderId);
		return new ResponseEntity<>(salesItem, HttpStatus.OK);
	}
	
	
	//Sales report by itemid
	@GetMapping("/sales_list/by_item_id")
	public String salesOrderByItemId(HttpServletRequest request, Model model) throws JsonProcessingException {
		String item = request.getParameter("item");
		List<SalesOrder> so = (List<SalesOrder>)salesService.getSalesOrderByItemId(item);
		ObjectMapper mapper = new ObjectMapper();
		model.addAttribute("pageHeader", "Sales List By Item");
		model.addAttribute("soList", mapper.writeValueAsString(so));
		return "salesListReport";
	}
	
	@GetMapping("/stock/pendingporeport/byPoNumber")
	public String pendingPoReportByPoNumber(HttpServletRequest request, Model model) throws JsonProcessingException {

		String poNumber = request.getParameter("poNumber");
		List<Object> pendingPoList = purchaseOrderService.getPendingPoListByPoNumber(poNumber);
		model.addAttribute("pageHeader", "Pending PO Report " + poNumber);
		model.addAttribute("pendingPoList", pendingPoList);
		return "pendingPOReportbyPoNum";

	}
	 @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
		@GetMapping("/sales/details/{salesOrderNo}")
		 public ModelAndView salesDetails(HttpServletRequest request,Model model
				 ,@PathVariable("salesOrderNo") String salesOrderNo) throws DocumentException, IOException{
			
			 Map<String, Object> salesData = new HashMap<String, Object>();
			 Optional<SalesOrder> salesOrder =salesService.getSalesOrderById(salesOrderNo);
			 String shippingPartyId=salesOrder.get().getShippingAddress();
			 Party party=partyRepo.findById(shippingPartyId);
			
			 if(party==null) {
					Optional<PartyAddress> partyaddr=addressService.getAddressByAddressId(shippingPartyId);
					//shippingPartyId=partyaddr.get().getParty().getId();
					//party= partyRepo.findById(shippingPartyId);
					request.setAttribute("shippingParty", partyaddr.get().getPartyName());
					request.setAttribute("shippingPartyAddr", partyaddr.get().getAddr1());
				 }else {
					 request.setAttribute("shippingParty", party.getPartyName());
					 request.setAttribute("shippingPartyAddr", party.getAddr1());
				 }
			 List<SalesItem> salesItemList = salesOrder.get().getItems();
			 Map<String,String> map = new HashMap();
			 for (SalesItem salesItem : salesItemList) {
				 float deliveredQty=0;
				 float purchaseQty=0;
				 float noOrderQty=0;
				 float grnQty=0;
				 float designQty=0;
				 ArrayList grnItemsList = new ArrayList();
				 List<DeliveryChallanItems> dcItemList =dcItemRepo.getDcItemListBySalesItemId(salesItem.getId());
				 List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItem.getId());
				 List<DesignItems> designItemsList = designService.getAllDesignItemListBySOItemId(salesItem.getId());
				 for (DesignItems designItem : designItemsList) {
					 designQty=designQty+designItem.getQuantity();
				}
				 	if(dcItemList.size()>0) {
						for (DeliveryChallanItems dcItem : dcItemList) {
							deliveredQty=(int) (deliveredQty+dcItem.getTodaysQty());
						}
					}else {
						deliveredQty=0;
					}
					if(purchaseItemList.size()>0) {
						for (PurchaseItem purchaseItem : purchaseItemList) {
							purchaseQty=purchaseQty+purchaseItem.getQuantity();
							
							List<GrnItems> grnItems = grnService.getGrnItemObjByPoItemId(Integer.toString(purchaseItem.getPurchase_item_id()));
							if(grnItems.size()>0) {
								for (GrnItems grnItem : grnItems) {
									grnQty=grnQty+grnItem.getReceivedQuantity();
								}
							}else {
								grnQty=0;
							}
						}
					}else {
						purchaseQty=0;
					}
					noOrderQty=designQty-purchaseQty;
					/*if(grnQty>0) {
						purchaseQty=purchaseQty-grnQty;
					}*/
					
					if(deliveredQty>0) {
						grnQty=grnQty-deliveredQty;
					}
					
				 map.put(salesItem.getId(), deliveredQty+"$"+purchaseQty+"&"+noOrderQty+"%"+grnQty);
			}
			 request.setAttribute("map", map);
			 request.setAttribute("designService",designService);
			 request.setAttribute("itemMasterService",itemMasterService);
			 salesData.put("salesObj", salesOrder.get());
			 return new ModelAndView(new SalesOrderExcel(), "salesData", salesData);
			
		 }
	 
	 @GetMapping("/returnable/{dcId}")
		public String returnableItems(Model model,@PathVariable("dcId") int dcId) throws JsonProcessingException {
			ObjectMapper mapper = utilService.getObjectMapper();
			List<DeliveryChallanItems> dcItemList= dcService.getDcItemList(dcId);
			model.addAttribute("pageHeader", "Returnable Items");
			model.addAttribute("dcItemList",mapper.writeValueAsString(dcItemList));
			return "returnable";
		}
	 @PostMapping("/add/returned_items")
		public String saveReturnableDc( Model model,Returnable returnable,HttpServletRequest req) {
			String partyId = req.getParameter("partyId");
			returnableService.saveReturnableDc(returnable,partyId);
			return "redirect:/returnableList";
		}
	 
	 @GetMapping("/returnableList")
		public String retunableItemsList(Model model) throws JsonProcessingException {
			List<ReturnableItems> retunableItemsList = returnableService.getReturnableItemsList();
			ObjectMapper mapper = utilService.getObjectMapper();
			model.addAttribute("retunableItemsList", mapper.writeValueAsString(retunableItemsList));
			model.addAttribute("pageHeader", "Returnable Items List");
			return "returnableList";
		}
	 
	 @GetMapping("/work_order")
		public String workOrder(Model model, HttpServletRequest req) throws JsonProcessingException {
		 Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
		 List<SalesOrder> salesList = salesService.getSalesOrderList();
		 List<Party> contractorPartyList = partyService.getPartyListByTypeContractor();
		 ObjectMapper mapper = utilService.getObjectMapper();
		 model.addAttribute("salesOrderList",mapper.writeValueAsString(salesList));
		 model.addAttribute("contractorPartyList", mapper.writeValueAsString(contractorPartyList));
		 model.addAttribute("pageHeader", "Work Order");
		 if (flashMap != null) {
				WorkOrder workOrder = (WorkOrder) flashMap.get("workOrderObj");

				model.addAttribute("workOrderObj", mapper.writeValueAsString(workOrder));

			}
		 userService.save();
		 
		 return "workOrder";
		}
	 
	 @GetMapping("/api/sales-Item_by_id")
		public ResponseEntity<?> SalesItemById(@RequestParam("id") String salesItemId) {
			Optional<SalesItem> salesItem = salesService.getSalesItemObjById(salesItemId);
			return new ResponseEntity<>(salesItem, HttpStatus.OK);
		}
	 
	 @PostMapping("/add/work_order")
		public String saveWorkOrder( Model model,WorkOrder workOrder,HttpServletRequest req) {
			String partyId = req.getParameter("partyByType");
			workOrderService.saveWorkOrder(workOrder,partyId);
			return "redirect:/workOrderList";
		}
	 @GetMapping("/workOrderList")
		public String workOrderList(Model model) throws JsonProcessingException {
			List<WorkOrder> workOrderList = workOrderService.getWorkOrderList();
			ObjectMapper mapper = utilService.getObjectMapper();
			model.addAttribute("workOrderList", mapper.writeValueAsString(workOrderList));
			model.addAttribute("pageHeader", "Work Order List");
			return "workOrderList";
		}
	 
	 @GetMapping("/api/work_order/view")
		public String displayWorkOrderView(@RequestParam("workOrderId") String workOrderId,
				RedirectAttributes redirectAttr, Model model) {

			Optional<WorkOrder> workOrder = workOrderService.getWorkOrderById(workOrderId);
			// This code is to overcome the pblm of single quote issue with client po and
			// addresses
			Party party = workOrder.get().getParty();

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

			String clientPo = workOrder.get().getSalesOrder().getClientPoNumber();
			String partyName = party.getPartyName();
			clientPo = clientPo.replace("'", "&");
			partyName = partyName.replace("\"", "&");
			partyName = partyName.replace("'", "&");
			clientPo = clientPo.replace("\"", "&");

			workOrder.get().getSalesOrder().setClientPoNumber(clientPo);
			party.setPartyName(partyName);
			
			
			Party salesOrderParty = workOrder.get().getSalesOrder().getParty();
			String soPartyAddr1 = party.getAddr1();
			soPartyAddr1 = soPartyAddr1.replace("'", "&");
			soPartyAddr1 = soPartyAddr1.replace("\"", "&");
			salesOrderParty.setAddr1(soPartyAddr1);

			if (party.getAddr2() != null) {
				String soPartyAddr2 = party.getAddr2();
				soPartyAddr2 = soPartyAddr2.replace("'", "&");
				soPartyAddr2 = soPartyAddr2.replace("\"", "&");
				salesOrderParty.setAddr2(soPartyAddr2);
			}
			String soPartyName = salesOrderParty.getPartyName();
			soPartyName = soPartyName.replace("\"", "&");
			soPartyName = soPartyName.replace("'", "&");
			salesOrderParty.setPartyName(soPartyName);
			
			redirectAttr.addFlashAttribute("workOrderObj", workOrder.get());
			return "redirect:/work_order";
		}
	 
	 @GetMapping("/api/workOrderItemList/by_wo_id")
		public ResponseEntity<?> getInvoiceItemListByInvoiceId(@RequestParam("woId") String woId, Model model) {
			List<WorkOrderItems> workOrderItemsList= workOrderService.getWorkOrderItemList(woId);
		    return new ResponseEntity<>(workOrderItemsList,HttpStatus.OK) ;
	 }
	 
	 @GetMapping("/dc_list/by_item")
		public String dcListByItem(HttpServletRequest request, Model model) throws JsonProcessingException {
			String item = request.getParameter("designItemId");
			List<DeliveryChallan> dcList = (List<DeliveryChallan>) dcService.getDcListBysalesItemId(item);
			ObjectMapper mapper = new ObjectMapper();
			model.addAttribute("pageHeader", "DC List By Item");
			model.addAttribute("dcList", mapper.writeValueAsString(dcList));
			return "dcListByItemReport";
		}
	 @GetMapping("/api/dc_list")
		public ResponseEntity<?> dcList(HttpServletRequest request, Model model) throws JsonProcessingException {
		 List<DeliveryChallan> dcLists = deliveryChallanService.getDeliveryChallanLists();
		 return new ResponseEntity<>(dcLists,HttpStatus.OK) ;
		}
	 
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/po_list/by_date")
		public ModelAndView poListByDate(HttpServletRequest request, Model model) throws ParseException {
			String fromDateString = request.getParameter("poListByFromDate");
			fromDateString = fromDateString.replaceAll("/", "-");
			String todateString = request.getParameter("poListByToDate");
			todateString = todateString.replaceAll("/", "-");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			Date todaysDate = null;
			Date fromDate = null;
			// Parsing date
			try {
				todaysDate = sdf.parse(todateString);
				fromDate = sdf.parse(fromDateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar c = Calendar.getInstance();
			Calendar c1 = Calendar.getInstance();
			c.setTime(todaysDate);
			c1.setTime(fromDate);
			// c.add(Calendar.DATE, 0);
			todaysDate = c.getTime();
			// c.add(Calendar.DATE, 30); //Last 30 days data
			fromDate = c1.getTime();
			c.add(Calendar.HOUR_OF_DAY, +23);
			c.add(Calendar.MINUTE, 59);
			todaysDate = c.getTime();
			Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
			Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);
			Map<String,String> modelMap = new HashMap();
			List<PurchaseOrder> poList = purchaseOrderService.getPurchaseOrderListByDate(sqlFromDate, sqlToDate);
			
			int month = c.get(Calendar.MONTH);
			Month monthName = Month.of(month + 1);
			Map poMap = new HashMap();

			request.setAttribute("poList", poList);
			request.setAttribute("modelMaps", modelMap);
			request.setAttribute("itemMasterService", itemMasterService);
			request.setAttribute("salesService", salesService);
			poMap.put("monthName", monthName);
			
			return new ModelAndView(new PurchaseExcel(), "poMap", poMap);
		}
	 
	 @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
		@GetMapping("/material/tracker/{salesOrderNo}")
		 public ModelAndView materialTracker(HttpServletRequest request,Model model
				 ,@PathVariable("salesOrderNo") String salesOrderNo) throws DocumentException, IOException{
			
			 Map<String, Object> salesData = new HashMap<String, Object>();
			 Optional<SalesOrder> salesOrder =salesService.getSalesOrderById(salesOrderNo);
			 String shippingPartyId=salesOrder.get().getShippingAddress();
			 Party party=partyRepo.findById(shippingPartyId);
			 
			 if(party==null) {
					Optional<PartyAddress> partyaddr=addressService.getAddressByAddressId(shippingPartyId);
					//shippingPartyId=partyaddr.get().getParty().getId();
					//party= partyRepo.findById(shippingPartyId);
					request.setAttribute("shippingParty", partyaddr.get().getPartyName());
					request.setAttribute("shippingPartyAddr", partyaddr.get().getAddr1());
				 }else {
					 request.setAttribute("shippingParty", party.getPartyName());
					 request.setAttribute("shippingPartyAddr", party.getAddr1());
				 }
			 List<SalesItem> salesItemList = salesOrder.get().getItems();
			 Map<String,String> map = new HashMap();
			 for (SalesItem salesItem : salesItemList) {
				 float deliveredQty=0;
				 float purchaseQty=0;
				 float noOrderQty=0;
				 float grnQty=0;
				 float designQty=0;
				 ArrayList grnItemsList = new ArrayList();
				 List<DeliveryChallanItems> dcItemList =dcItemRepo.getDcItemListBySalesItemId(salesItem.getId());
				 List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItem.getId());
				 List<DesignItems> designItemsList = designService.getAllDesignItemListBySOItemId(salesItem.getId());
				 for (DesignItems designItem : designItemsList) {
					 designQty=designQty+designItem.getQuantity();
				}
				 	if(dcItemList.size()>0) {
						for (DeliveryChallanItems dcItem : dcItemList) {
							deliveredQty=(int) (deliveredQty+dcItem.getTodaysQty());
						}
					}else {
						deliveredQty=0;
					}
					if(purchaseItemList.size()>0) {
						for (PurchaseItem purchaseItem : purchaseItemList) {
							purchaseQty=purchaseQty+purchaseItem.getQuantity();
							List<GrnItems> grnItems = grnService.getGrnItemObjByPoItemId(Integer.toString(purchaseItem.getPurchase_item_id()));
							if(grnItems.size()>0) {
								for (GrnItems grnItem : grnItems) {
									grnQty=grnQty+grnItem.getReceivedQuantity();
								}
							}else {
								grnQty=0;
							}
						}
					}else {
						purchaseQty=0;
					}
					noOrderQty=designQty-purchaseQty;
					/*if(grnQty>0) {
						purchaseQty=purchaseQty-grnQty;
					}*/
					
					if(deliveredQty>0) {
						grnQty=grnQty-deliveredQty;
					}
					
				 map.put(salesItem.getId(), deliveredQty+"$"+purchaseQty+"&"+noOrderQty+"%"+grnQty);
			}
			 request.setAttribute("map", map);
			 request.setAttribute("dcService", deliveryChallanService);
			 request.setAttribute("grnSrvce",grnService);
			 request.setAttribute("poItemService",purchaseItemService);
			 request.setAttribute("designService",designService);
			 request.setAttribute("poService",purchaseOrderService);
			 request.setAttribute("itemMasterService",itemMasterService);
			 
			 salesData.put("salesObj", salesOrder.get());
			 return new ModelAndView(new MaterialTrackerExcel(), "salesData", salesData);
			
		 }
		 
		 /**
		  * Optimized Material Tracker endpoint with batch queries and caching
		  * Significantly faster for large GrandTotal values
		  */
		 @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
		 @GetMapping("/material/tracker/optimized/{salesOrderNo}")
		 public ModelAndView materialTrackerOptimized(HttpServletRequest request, Model model
				 , @PathVariable("salesOrderNo") String salesOrderNo) throws DocumentException, IOException {
			 
			 Map<String, Object> salesData = new HashMap<String, Object>();
			 Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(salesOrderNo);
			 String shippingPartyId = salesOrder.get().getShippingAddress();
			 Party party = partyRepo.findById(shippingPartyId);
			 
			 if (party == null) {
				 Optional<PartyAddress> partyaddr = addressService.getAddressByAddressId(shippingPartyId);
				 request.setAttribute("shippingParty", partyaddr.get().getPartyName());
				 request.setAttribute("shippingPartyAddr", partyaddr.get().getAddr1());
			 } else {
				 request.setAttribute("shippingParty", party.getPartyName());
				 request.setAttribute("shippingPartyAddr", party.getAddr1());
			 }
			 
			 // Use optimized service for batch queries
			 Map<String, String> map = optimizedMaterialTrackerService.getOptimizedMaterialTrackerData(salesOrder.get());
			 
			 request.setAttribute("map", map);
			 request.setAttribute("dcService", deliveryChallanService);
			 request.setAttribute("grnSrvce", grnService);
			 request.setAttribute("poItemService", purchaseItemService);
			 request.setAttribute("designService", designService);
			 request.setAttribute("poService", purchaseOrderService);
			 request.setAttribute("itemMasterService", itemMasterService);
			 
			 salesData.put("salesObj", salesOrder.get());
			 return new ModelAndView(new OptimizedMaterialTrackerExcel(), "salesData", salesData);
		 }
		 
		 /**
		  * Paginated Material Tracker for very large datasets
		  * Processes data in chunks to prevent memory issues
		  */
		 @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
		 @GetMapping("/material/tracker/paginated/{salesOrderNo}")
		 public ModelAndView materialTrackerPaginated(HttpServletRequest request, Model model
				 , @PathVariable("salesOrderNo") String salesOrderNo,
				 @RequestParam(value = "page", defaultValue = "0") int page,
				 @RequestParam(value = "size", defaultValue = "50") int size) throws DocumentException, IOException {
			 
			 Map<String, Object> salesData = new HashMap<String, Object>();
			 Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(salesOrderNo);
			 String shippingPartyId = salesOrder.get().getShippingAddress();
			 Party party = partyRepo.findById(shippingPartyId);
			 
			 if (party == null) {
				 Optional<PartyAddress> partyaddr = addressService.getAddressByAddressId(shippingPartyId);
				 request.setAttribute("shippingParty", partyaddr.get().getPartyName());
				 request.setAttribute("shippingPartyAddr", partyaddr.get().getAddr1());
			 } else {
				 request.setAttribute("shippingParty", party.getPartyName());
				 request.setAttribute("shippingPartyAddr", party.getAddr1());
			 }
			 
			 // Get paginated sales items
			 List<SalesItem> allItems = salesOrder.get().getItems();
			 int startIndex = page * size;
			 int endIndex = Math.min(startIndex + size, allItems.size());
			 List<SalesItem> paginatedItems = allItems.subList(startIndex, endIndex);
			 
			 // Create a temporary sales order with paginated items
			 SalesOrder paginatedSalesOrder = new SalesOrder();
			 paginatedSalesOrder.setId(salesOrder.get().getId());
			 paginatedSalesOrder.setClientPoNumber(salesOrder.get().getClientPoNumber());
			 paginatedSalesOrder.setClientPoDate(salesOrder.get().getClientPoDate());
			 paginatedSalesOrder.setShippingAddress(salesOrder.get().getShippingAddress());
			 paginatedSalesOrder.setBillingAddress(salesOrder.get().getBillingAddress());
			 paginatedSalesOrder.setItems(paginatedItems);
			 
			 // Use optimized service for batch queries
			 Map<String, String> map = optimizedMaterialTrackerService.getOptimizedMaterialTrackerData(paginatedSalesOrder);
			 
			 request.setAttribute("map", map);
			 request.setAttribute("dcService", deliveryChallanService);
			 request.setAttribute("grnSrvce", grnService);
			 request.setAttribute("poItemService", purchaseItemService);
			 request.setAttribute("designService", designService);
			 request.setAttribute("poService", purchaseOrderService);
			 request.setAttribute("itemMasterService", itemMasterService);
			 request.setAttribute("currentPage", page);
			 request.setAttribute("totalPages", (int) Math.ceil((double) allItems.size() / size));
			 request.setAttribute("totalItems", allItems.size());
			 
			 salesData.put("salesObj", paginatedSalesOrder);
			 return new ModelAndView(new OptimizedMaterialTrackerExcel(), "salesData", salesData);
		 }
	 
	 
	 @GetMapping("/tds")
		public String tds(Model model, HttpServletRequest req) throws JsonProcessingException {
			Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(req);
			model.addAttribute("salesOrder", new SalesOrder());
			List<Units> unitsList = itemMasterService.getUnitList();
			List<SalesOrder> salesList = salesService.getSalesOrderList();
			List<Party> customerpartyList = partyService.getPartyListByTypeCustomer();
			List<ItemMaster> itemList = itemMasterService.getItemList();
			User userObj  = userService.getCurrentUser();
			String userName = userObj.getUsername();
			ObjectMapper mapper = new ObjectMapper();
			model.addAttribute("userName", mapper.writeValueAsString(userName));
			model.addAttribute("itemList", mapper.writeValueAsString(itemList));
			model.addAttribute("unitsList", mapper.writeValueAsString(unitsList));
			model.addAttribute("customerPartyList", mapper.writeValueAsString(customerpartyList));
			model.addAttribute("salesOrderList", mapper.writeValueAsString(salesList));
			model.addAttribute("pageHeader", "Sales Order");
			if (flashMap != null) {
				SalesOrder salesOrder = (SalesOrder) flashMap.get("salesOrderObj");

				model.addAttribute("salesOrderObj", mapper.writeValueAsString(salesOrder));

			}
			return "tds";
		}
	 
	 @GetMapping("/api/salesOrder_tds/view")
		public String displayTdsView(@RequestParam("salesOrderId") String salesOrderId,
				RedirectAttributes redirectAttr, Model model) {

		 Optional<SalesOrder> salesOrder = salesService.getSalesOrderById(salesOrderId);
			// This code is to overcome the pblm of single quote issue with client po and
			// addresses
			Party party = salesOrder.get().getParty();

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

			String clientPo = salesOrder.get().getClientPoNumber();
			String partyName = party.getPartyName();
			clientPo = clientPo.replace("'", "&");
			partyName = partyName.replace("\"", "&");
			partyName = partyName.replace("'", "&");
			clientPo = clientPo.replace("\"", "&");

			salesOrder.get().setClientPoNumber(clientPo);
			party.setPartyName(partyName);

			redirectAttr.addFlashAttribute("salesOrderObj", salesOrder.get());
			return "redirect:/tds";
		}
	 
	 @PostMapping("/add/tds")
		public String saveTdsSalesOrder(@ModelAttribute("tds") Tds tds, HttpServletRequest req)
				throws IOException {
		 	String tdsApproved = req.getParameter("tdsApproved");
			tdsService.saveTds(tds,req);

			return "redirect:/salesList";

		}
	 
	 @GetMapping("/api/tds_approved_list")
		public ResponseEntity<?> tdsApprovedList(Model model) {
			List<SalesOrder> soList = tdsService.getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboard();
			return new ResponseEntity<>(soList, HttpStatus.OK);
		}
	 
	 @GetMapping("/api/salesItems_without_design_list")
		public ResponseEntity<?> salesItemsWithoutDesign(Model model) {
			List<SalesOrder> soList = salesService.getAllSalesItemListWithoutDesignForDashboard();
			return new ResponseEntity<>(soList, HttpStatus.OK);
		}
	 
	 @GetMapping("/api/salesOrder_with_design_withou_po_list")
		public ResponseEntity<?> salesOrderWithDesign(Model model) {
			List<SalesOrder> soList = salesService.getAllSalesOrderQithDesignAndPoNotDoneForDashboard();
			return new ResponseEntity<>(soList, HttpStatus.OK);
		}
	 
	 @GetMapping("/nonBillableList")
		public String nonBillableList(Model model) throws JsonProcessingException {
		 	List<NonBillable> nonBillableList = nonBilableService.getNonBillableList();
		 	ObjectMapper mapper = new ObjectMapper();
			model.addAttribute("pageHeader", "Non Billable");
			model.addAttribute("nonBillableList", mapper.writeValueAsString(nonBillableList));
			return "nonbillable";
		}
	 
	 @GetMapping("/api/non_billable_items_by_id")
		public ResponseEntity<?> nonBillableItemsById(@RequestParam("id") int id) {
			List<NonBillableItems> nonBillableItemsList = nonBilableService.getNonBillableItemsByNonBillableId(id);
			return new ResponseEntity<>(nonBillableItemsList, HttpStatus.OK);
		}
	 @GetMapping("/api/sales_list/pending_partial")
		public ResponseEntity<?> salesListPendingPartial(Model model) {
			List<SalesOrder> salesList = salesService.getPendingSalesListPartial();
			return new ResponseEntity<>(salesList, HttpStatus.OK);
		}
	 @GetMapping("/api/purchase_List/pending_partial")
		public ResponseEntity<?> pendingPurchaseListPartial(Model model) {
			List<PurchaseOrder> puchaseList = purchaseOrderService.getPenidngPoListPartial();
			return new ResponseEntity<>(puchaseList, HttpStatus.OK);
		}
	 @GetMapping("/api/invoice_List_partial")
		public ResponseEntity<?> invoiceListPartial(Model model) {
			List<Invoice> invoiceList = invoiceService.getInvoiceListPartial();
			return new ResponseEntity<>(invoiceList, HttpStatus.OK);
		}
	 @GetMapping("/api/get_all_sales_list_partial")
		public ResponseEntity<?> allSalesListPartial(Model model) {
			List<SalesOrder> salesList = salesService.getAllSalesOrderListPartial();
			return new ResponseEntity<>(salesList, HttpStatus.OK);
		}
	 @GetMapping("/api/tds_approved_list_partial")
		public ResponseEntity<?> tdsApprovedListPartial(Model model) {
			List<SalesOrder> soList = tdsService.getTdsItemsListWhereTdsApprovedAndPoNotDoneForDashboardPartial();
			return new ResponseEntity<>(soList, HttpStatus.OK);
		}
	 @GetMapping("/api/salesItems_without_design_list_partial")
		public ResponseEntity<?> salesItemsWithoutDesignPartial(Model model) {
			List<SalesOrder> soList = salesService.getAllSalesItemListWithoutDesignForDashboardPartial();
			return new ResponseEntity<>(soList, HttpStatus.OK);
		}
	 @GetMapping("/api/salesItems_with_design_list_partial")
		public ResponseEntity<?> salesItemsWithDesignPartial(Model model) {
			List<SalesOrder> soList = salesService.getAllSalesItemListWithDesignForDashboardPartial();
			System.out.println("soList size"+soList.size());
			return new ResponseEntity<>(soList, HttpStatus.OK);
		}
	
	 @SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/active_sales/by_customer")
	 	public ModelAndView activeSOByCustomer(HttpServletRequest request, Model model) throws ParseException {
			 String partyId = request.getParameter("clientName");
			 List<SalesItem> salesItemList = salesService.getActiveSalesItemListByCustomer(partyId);
			 request.setAttribute("salesItemList", salesItemList);
			 Map map = new HashMap();
			 map.put("partyId", partyId);
			 return new ModelAndView(new ActiveSalesItemExcel(), "map", map);
	 	}
	 @GetMapping("/salesList_archived")
		public String archivedSalesOrderList(Model model) throws JsonProcessingException {
			List<SalesOrder> salesList = salesService.getArchivedSalesOrderList();
			User userObj  = userService.getCurrentUser();
			String role = userObj.getRole();
			ObjectMapper mapper = utilService.getObjectMapper();
			model.addAttribute("salesOrderList", mapper.writeValueAsString(salesList));
			model.addAttribute("role", mapper.writeValueAsString(role));
			model.addAttribute("user", mapper.writeValueAsString(userObj.getUsername()));
			model.addAttribute("pageHeader", "Archived Sales List");
			return "archivedSalesDashboard";
		}
	 @PostMapping("/api/update_so_archive")
		public ResponseEntity<?> updateSoArchive(@RequestParam("soId") String soId) {
			salesService.archiveSO(soId);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	 
	 @PostMapping("/api/update_so_unarchive")
		public ResponseEntity<?> updateSoUnArchive(@RequestParam("soId") String soId) {
			salesService.unArchiveSO(soId);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	 
	 @GetMapping("/soChart")
		public String soChart(Model model) throws JsonProcessingException {
			model.addAttribute("pageHeader", "Sales Order Monthly Chart");
			return "soChart";
		}
	 
	 @SuppressWarnings("rawtypes")
	@GetMapping("/api/so_monthly_chart")
		public ResponseEntity<?> getReadingForTotalConsumption(HttpServletRequest req,
	      @RequestParam("year") String year) throws ParseException {
	       
		 Map<String, Map> chartMap = (Map<String, Map>) salesService.getSoDetailsForChart(year);
		 return new ResponseEntity<>(chartMap, HttpStatus.OK);

		}
	 
	 @GetMapping("/api/partial_dc_items")
		public ResponseEntity<?> getDcItemListPartial(@RequestParam("id") int dcId, Model model) {
			List<DeliveryChallanItems> dcItemList= deliveryChallanService.getItemsNotDelivered(dcId);
		    return new ResponseEntity<>(dcItemList,HttpStatus.OK) ;
	 }
	 
	 @SuppressWarnings({ "rawtypes" })
		@GetMapping("/grn_itemwise/by_date")
		public ModelAndView grnReportbyRegionandDate(HttpServletRequest request, Model model) throws ParseException {
		//	String region = request.getParameter("grnregion");
			String fromDateString = request.getParameter("grnreportByRegionFromDate");
			fromDateString = fromDateString.replaceAll("/", "-");
			String todateString = request.getParameter("grnreportByRegionToDate");
			todateString = todateString.replaceAll("/", "-");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			Date todaysDate = null;
			Date fromDate = null;
			// Parsing date
			try {
				todaysDate = sdf.parse(todateString);
				fromDate = sdf.parse(fromDateString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Calendar c = Calendar.getInstance();
			Calendar c1 = Calendar.getInstance();
			c.setTime(todaysDate);
			c1.setTime(fromDate);
			// c.add(Calendar.DATE, 0);
			todaysDate = c.getTime();
			// c.add(Calendar.DATE, 30); //Last 30 days data
			fromDate = c1.getTime();
			c.add(Calendar.HOUR_OF_DAY, +23);
			c.add(Calendar.MINUTE, 59);
			todaysDate = c.getTime();
			Timestamp sqlToDate = convertDate.convertJavaDateToSqlDate(todaysDate);
			Timestamp sqlFromDate = convertDate.convertJavaDateToSqlDate(fromDate);
			List<GrnItems> grnRegionMap = grnService.findgrnListByDateandRegion(sqlFromDate, sqlToDate);
			request.setAttribute("purchaseItemService",purchaseItemService);
			request.setAttribute("itemMasterService",itemMasterService);
			request.setAttribute("salesService",salesService);
			request.setAttribute("grnService",grnService);
			 
			return new ModelAndView(new GrnReportByDateExcel(), "grnByRegion", grnRegionMap);
		}
	 

	    @PostMapping("api/clientPo/upload/{salesOrderId}")
	    public ResponseEntity<String> uploadClientPoFile(@RequestParam("file") MultipartFile file, @PathVariable("salesOrderId") String salesOrderId) throws IOException {
	        
	            FileEntity storedFile = salesService.storeFile(file,salesOrderId);
	            return ResponseEntity.ok("File uploaded successfully: " + storedFile.getFileName());
	        
	    }
	    
	    @GetMapping("api/clientPo/download/{salesOrderId}")
	    public ResponseEntity<byte[]> downloadPdfFileById(@PathVariable("salesOrderId") String salesOrderId) {
	        try {
	        	FileEntity pdfEntity = salesService.getPdfFileBySalesId(salesOrderId);

	            return ResponseEntity.ok()
	                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pdfEntity.getFileName() + "\"")
	                    .contentType(MediaType.APPLICATION_PDF)
	                    .body(pdfEntity.getData());
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	        }
	    }
	    
	    @GetMapping("/api/get_solist_byclient")
		public ResponseEntity<?> getSalesListByClient(@RequestParam("partyId") String partyId,
				Model model) {
			List<SalesOrder> salesList = salesService.getSalesListByParty(partyId);
			return new ResponseEntity<>(salesList, HttpStatus.OK);
		}

	    @SuppressWarnings({ "unused", "unchecked", "rawtypes" })
		@GetMapping("/sales/downlaod/{salesOrderNo}")
		 public ModelAndView salesOrderDownload(HttpServletRequest request,Model model
				 ,@PathVariable("salesOrderNo") String salesOrderNo) throws DocumentException, IOException{
			
			 Map<String, Object> salesData = new HashMap<String, Object>();
			 Optional<SalesOrder> salesOrder =salesService.getSalesOrderById(salesOrderNo);
			 String shippingPartyId=salesOrder.get().getShippingAddress();
			 String billingPartyId=salesOrder.get().getBillingAddress();
			 System.out.println(billingPartyId);
			 Party party=partyRepo.findById(shippingPartyId);
			
			 if(party==null) {
					Optional<PartyAddress> partyaddr=addressService.getAddressByAddressId(shippingPartyId);
					//shippingPartyId=partyaddr.get().getParty().getId();
					//party= partyRepo.findById(shippingPartyId);
					request.setAttribute("shippingParty", partyaddr.get().getPartyName());
					request.setAttribute("shippingPartyAddr", partyaddr.get().getAddr1());
				 }else {
					 request.setAttribute("shippingParty", party.getPartyName());
					 request.setAttribute("shippingPartyAddr", party.getAddr1());
				 }
			 Party billingParty=partyRepo.findById(billingPartyId);
				
			 if(billingParty==null) {
					List<PartyAddress> partyaddr=addressService.getAddressById(billingPartyId);
					
					//shippingPartyId=partyaddr.get().getParty().getId();
					//party= partyRepo.findById(shippingPartyId);
					request.setAttribute("billingParty", partyaddr.get(0).getPartyName());
					request.setAttribute("billingPartyAddr", partyaddr.get(0).getAddr1());
				 }else {
					 request.setAttribute("billingParty", billingParty.getPartyName());
					 request.setAttribute("billingPartyAddr", billingParty.getAddr1());
				 } 
			 List<SalesItem> salesItemList = salesOrder.get().getItems();
			 Map<String,String> map = new HashMap();
			 for (SalesItem salesItem : salesItemList) {
				 float deliveredQty=0;
				 float purchaseQty=0;
				 float noOrderQty=0;
				 float grnQty=0;
				 float designQty=0;
				 ArrayList grnItemsList = new ArrayList();
				 List<DeliveryChallanItems> dcItemList =dcItemRepo.getDcItemListBySalesItemId(salesItem.getId());
				 List<PurchaseItem> purchaseItemList = purchaseItemService.getPurchaseItemsBySalesItemId(salesItem.getId());
				 List<DesignItems> designItemsList = designService.getAllDesignItemListBySOItemId(salesItem.getId());
				 for (DesignItems designItem : designItemsList) {
					 designQty=designQty+designItem.getQuantity();
				}
				 	if(dcItemList.size()>0) {
						for (DeliveryChallanItems dcItem : dcItemList) {
							deliveredQty=(int) (deliveredQty+dcItem.getTodaysQty());
						}
					}else {
						deliveredQty=0;
					}
					if(purchaseItemList.size()>0) {
						for (PurchaseItem purchaseItem : purchaseItemList) {
							purchaseQty=purchaseQty+purchaseItem.getQuantity();
							
							List<GrnItems> grnItems = grnService.getGrnItemObjByPoItemId(Integer.toString(purchaseItem.getPurchase_item_id()));
							if(grnItems.size()>0) {
								for (GrnItems grnItem : grnItems) {
									grnQty=grnQty+grnItem.getReceivedQuantity();
								}
							}else {
								grnQty=0;
							}
						}
					}else {
						purchaseQty=0;
					}
					noOrderQty=designQty-purchaseQty;
					/*if(grnQty>0) {
						purchaseQty=purchaseQty-grnQty;
					}*/
					
					if(deliveredQty>0) {
						grnQty=grnQty-deliveredQty;
					}
					
				 map.put(salesItem.getId(), deliveredQty+"$"+purchaseQty+"&"+noOrderQty+"%"+grnQty);
			}
			 request.setAttribute("map", map);
			 request.setAttribute("designService",designService);
			 request.setAttribute("itemMasterService",itemMasterService);
			 salesData.put("salesObj", salesOrder.get());
			 return new ModelAndView(new SalesOrderDownloadExcel(), "salesData", salesData);
			
		 }
	    @PostMapping("/api/sales/upload")
	    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) throws Exception {
	    	 if (file.isEmpty()) {
	             return ResponseEntity.badRequest().body("Please upload a file.");
	         }

	         List<String> errors = excelProcessingService.parseExcelFile(file);

	         if (!errors.isEmpty()) {
	             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
	         }

	         return ResponseEntity.ok("File uploaded and processed successfully.");
	    }
	    
	    @GetMapping("/api/get_user_list")
		public ResponseEntity<?> getUserList(Model model) {
			List<User> userList = userService.getAllUsers();
			return new ResponseEntity<>(userList, HttpStatus.OK);
		}
	    
	    /*@PostMapping("/api/sales/upload")
	    public ResponseEntity<String> updateToolTackle(@RequestParam("file") MultipartFile file){
	        
	            // Save the uploaded file locally
	    	 try {
	             List<String> ids = itemMasterService.readIdsFromExcel(file);
	             int updatedCount = itemMasterService.updateToolTackleStatus(ids);
	             return ResponseEntity.ok(updatedCount + " records updated successfully.");
	         } catch (Exception e) {
	             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
	         }
	    }*/
	    
	    @PostMapping("/api/sales/archive")
	    public ResponseEntity<String> archiveSalesList(@RequestParam("file") MultipartFile file){
	        
	            // Save the uploaded file locally
	    	 try {
	             List<String> ids = salesService.readIdsFromExcel(file);
	             int updatedCount = salesService.archiveSalesList(ids);
	             return ResponseEntity.ok(updatedCount + " records updated successfully.");
	         } catch (Exception e) {
	             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
	         }
	    }
	    
	    @GetMapping("/api/get_salesItem_list_by_id")
		public ResponseEntity<?> getSalesItemListById(@RequestParam("soId") String soId,
				Model model) {
			List<SalesItem> itemList = salesService.getAllSalesItemListWithouPOBySoId(soId);
			return new ResponseEntity<>(itemList, HttpStatus.OK);
		}
	    
	    @PostMapping("/api/design/upload")
	    public ResponseEntity<?> designUpload(@RequestParam("file") MultipartFile file, @RequestParam("clientPONum") String clientPONum) throws Exception {
	    	 if (file.isEmpty()) {
	             return ResponseEntity.badRequest().body("Please upload a file.");
	         }

	         List<String> errors = designUploadService.processExcelFile(file,clientPONum);

	         if (!errors.isEmpty()) {
	             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
	         }

	         return ResponseEntity.ok("File uploaded and processed successfully.");
	    }
	    
}
