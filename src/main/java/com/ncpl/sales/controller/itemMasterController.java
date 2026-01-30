package com.ncpl.sales.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Make;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.model.Supplier;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.service.ItemMasterService;
import com.ncpl.sales.service.ItemMasterUploadService;
import com.ncpl.sales.service.ItemUploadService;
import com.ncpl.sales.service.MakeService;
import com.ncpl.sales.service.PartyService;
import com.ncpl.sales.service.StockReportByDateExcel;
import com.ncpl.sales.service.StockService;
import com.ncpl.sales.service.itemListExcel;
import com.ncpl.sales.service.stockModifiedExcel;
import com.ncpl.sales.util.DateConverterUtil;
import com.ncpl.sales.util.NcplUtil;
@Controller
public class itemMasterController {

	@Autowired
	ItemMasterService itemMasterService;
	@Autowired
	NcplUtil utilService;
	@Autowired
	PartyService partyService;
	@Autowired
	StockService stockService;
	@Autowired
	DateConverterUtil convertDate;
	@Autowired
	MakeService makeService;
	@Autowired
	ItemUploadService excelUploadService;

	@GetMapping("/api/items/byId")
	public ResponseEntity<?> bySalesOrderId(@RequestParam("id") String id, Model model) {
		if (id == null || id.trim().isEmpty()) {
			Map<String, String> error = new HashMap<>();
			error.put("errorCode", "400");
			error.put("errorMessage", "Missing required parameter: id");
			return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
		}
		Optional<ItemMaster> item = itemMasterService.getItemById(id);
		if (!item.isPresent()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(item.get(), HttpStatus.OK);
	}

	/**
	 * code to delete item
	 * @param id
	 * @return
	 */
	@PostMapping(path="/api/item/delete")
	  public ResponseEntity<?> deleteItem(@RequestParam("id") String id) {
			itemMasterService.deleteItem(id);
			return new ResponseEntity<>(HttpStatus.OK);
	 }
	
	/**
	 * save stock
	 * @param stock
	 * @return stock object
	 */
	@PostMapping(path="/api/add/stock")
	 public ResponseEntity<?> saveStock(Stock stock,HttpServletRequest req,
			 @RequestParam("existingquantity") float existingquantity, @RequestParam(required=false,name="existingclientName") String clientName) {

		String partyId =stock.getClientName();
		String itemId=req.getParameter("itemId");
		float assignedQuantity = stock.getQuantity();
		Stock stockObj=stockService.saveStock(stock,partyId,itemId);
		String storeName = req.getParameter("storeName");
		String location = req.getParameter("locationInStore");
	    //Updating stock after assigning to new comapany
		stockService.updateStockAfterAssign(existingquantity,clientName,itemId,assignedQuantity,storeName,location);
		
	     return new ResponseEntity<>(stockObj, HttpStatus.OK);
	  }
	/**
	 * get list of stock
	 * @return stockList
	 */
	@GetMapping(path="/api/stock/list", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> stockList(@RequestParam("itemId") String itemId) {
			List<Stock> stockList= stockService.getStockList(itemId);
			return new ResponseEntity<List<Stock>>(stockList, HttpStatus.OK);
	 }
	/**
	 * get list of units
	 * @return
	 */
	@GetMapping(path="/api/units/list", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> unitsList() {
			List<Units> unitsList= itemMasterService.getUnitList();
			return new ResponseEntity<List<Units>>(unitsList, HttpStatus.OK);
	 }
	/**
	 * save supplier
	 * @param supplier
	 * @return supplierObj
	 */
	@PostMapping(path="/api/add/supplier")
	 public ResponseEntity<?> saveSupplier(Supplier supplier,HttpServletRequest req) {
		String partyId =supplier.getSupplierName();
		String itemId=req.getParameter("itemId");
		String preferred = req.getParameter("preferred");
		Supplier supplierObj=itemMasterService.saveSuppler(supplier,partyId,itemId,preferred);
	    
	     return new ResponseEntity<>(supplierObj, HttpStatus.OK);
	  }
	/**
	 * get list of supplier
	 * @return supplierList
	 */
	@GetMapping(path="/api/supplier/list", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> supplierList(@RequestParam("itemId") String itemId) {
			List<Supplier> supplierList= itemMasterService.getSupplierList(itemId);
			return new ResponseEntity<List<Supplier>>(supplierList, HttpStatus.OK);
	 }
	
	@GetMapping(path="/api/supplier/list/with_purchase_history", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> supplierListWithPOHistory(@RequestParam("itemId") String itemId) {
			List<Supplier> supplierList= itemMasterService.getSupplierListWithPoHistory(itemId);
			return new ResponseEntity<List<Supplier>>(supplierList, HttpStatus.OK);
	 }
	/**
	 * get list of supplier with preferred yes
	 * @param itemId
	 * @return supplierList
	 */
	@GetMapping(path="/api/supplier_preferred/list", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> supplierPreferred(@RequestParam("itemId") String itemId) {
			List<Supplier> supplierList= itemMasterService.getSupplierListWithpreferredYes(itemId);
			return new ResponseEntity<List<Supplier>>(supplierList, HttpStatus.OK);
	 }


	 @GetMapping(path="/api/supplier/validate", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> checkDuplicateDeviceOwner(@RequestParam("itemId") String itemId, @RequestParam("supplierName") String supplierName,@RequestParam("supplierId") String supplierId)	 {
		
			boolean supplierExists= itemMasterService.checkSupplierExists(itemId, supplierName,supplierId);
			
			return new ResponseEntity<>(supplierExists, HttpStatus.OK);
	 }
	/**
	 * duplicate validation for client Name in stock
	 * @param itemId
	 * @param clientName
	 * @param stockId
	 * @return
	 */
	 @GetMapping(path="/api/client/validate", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> checkDuplicateClient(@RequestParam("itemId") String itemId, @RequestParam("clientName") String clientName,@RequestParam("stockId") String stockId)	 {
			boolean stockExists= stockService.checkClientNameExists(itemId,clientName,stockId);
			
			return new ResponseEntity<>(stockExists, HttpStatus.OK);
	 }
	// Api For generating excel/Pdf
		 @GetMapping("/itemist/Download")
			public ModelAndView itemList(HttpServletRequest request,Model model)
			throws ParseException {

		
		Map<?,?> itemDetails = itemMasterService.findItemDetails();

		return new ModelAndView(new itemListExcel(), "itemsData", itemDetails);

	}
		 
		// Api For generating excel/Pdf
		 @GetMapping("/stockModified/Download")
			public ModelAndView stockModifiedList()
			throws ParseException {
			
			 	Date todayDate = new Date();  
			    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");  
			    String date= formatter.format(todayDate); 
			    boolean byEmail=false;
			 List<?>  stockList = stockService.getStockHistoryByDate(date,byEmail);

		return new ModelAndView(new stockModifiedExcel(), "stockData", stockList);

	}
		/**
		 * get stock history by stockId
		 * @param stockId
		 * @return stockAuditList
		 * @throws ParseException
		 */
		@GetMapping("/api/stock/history")
		public ResponseEntity<?>  stockHistory(@RequestParam("stockId") String stockId)throws ParseException {
		List<Stock>  stockAuditList = stockService.getStockHistoryById(stockId);
		for (Stock stock : stockAuditList) {
			String id =stock.getParty().getId();
			Party partyObj = partyService.getPartyById(id);
			stock.setParty(partyObj);
			String itemId = stock.getItemMaster().getId();
			Optional<ItemMaster> itemObj = itemMasterService.getItemById(itemId);
			stock.setItemMaster(itemObj.get());
			
			
		}
		
		return new ResponseEntity<>(stockAuditList, HttpStatus.OK);

	}
		 /**
		  * Temporary api for checking stock audit working?
		  * @return
		  */
		 /*@GetMapping(path="/api/stock-rev-by-date", produces = MediaType.APPLICATION_JSON_VALUE)
		  public ResponseEntity<?> getStockRevisionByDate() {
			 String date = "2020-06-04";
				List<Stock> revisions= itemMasterService.getStocketRevisionByDate(date);
				System.out.println("##################" +revisions.size());
				return new ResponseEntity<>("tet", HttpStatus.OK);
		 }
		 
		// Api For generating excel/Pdf
				 @GetMapping("/stockList/Download")
					public ModelAndView stockList()throws ParseException, IOException {
					 String date = "2020-06-04";
                 Map stock = new HashMap();
				List<Stock> stockList = itemMasterService.getStocketRevisionByDate(date);
				stock.put("stockList", stockList);
				return new ModelAndView(new StockListExcel(), "stockData", stock);

			}*/
		
		 @GetMapping("/sales_report")
			public String deliveryChallanPage(Model model,HttpServletRequest req) throws JsonProcessingException {
			 List<Party> customerpartyList = partyService.getPartyListByTypeCustomer();
			 List<Party> partyList = partyService.getPartyListbyTypeSupplier();
			 ObjectMapper mapper = new ObjectMapper();
				model.addAttribute("clientList", mapper.writeValueAsString(customerpartyList));
				model.addAttribute("vendorList", mapper.writeValueAsString(partyList));
				model.addAttribute("pageHeader", "Sales Reports");
				
				return "salesReport";
			}
		// Api For generating excel/Pdf for stock history by selected date
				 @GetMapping("/stock_history/Download")
					public ModelAndView stockHistoryDownload(HttpServletRequest req)
					throws ParseException {
					 String reportDate=req.getParameter("reportDate");
					 String[] format=reportDate.split("-");
					 reportDate=format[2]+"-"+format[1]+"-"+format[0];
					 boolean byEmail=false;
					 List<?>  stockList = stockService.getStockHistoryByDate(reportDate,byEmail);

				return new ModelAndView(new stockModifiedExcel(), "stockData", stockList);

			}
				// Api For generating excel/Pdf for stock history by selected date
				/* @GetMapping("/stock_report_by_region/Download")
					public ModelAndView stockReportByRegion(HttpServletRequest req)
					throws ParseException {
					 String region=req.getParameter("region");
					 String reportFromDate=req.getParameter("reportByRegionFromDate");
					 String reportToDate=req.getParameter("reportByRegionFromDate");
					return null;

				

			}*/
	// Api For generating excel/Pdf for stock report by selected date
	@GetMapping("/stock_report_by_date/Download")
	public ModelAndView stockReportByDate(HttpServletRequest req) throws ParseException {
		String dateString = req.getParameter("date");

		List<Stock> stockList = stockService.getStockByDate(dateString);
		return new ModelAndView(new StockReportByDateExcel(), "stockData", stockList);

	}
	
	@GetMapping("/api/stock-list/by_itemId_clientId")
	public ResponseEntity<?> stockListByItemIdAndClientId(HttpServletRequest req,@RequestParam("itemId") String itemId,@RequestParam("clientId") String clientId) {

		List<Stock> stockList = stockService.getStockListByItemIdAndClientId(itemId,clientId);
		return new ResponseEntity<List<Stock>>(stockList, HttpStatus.OK);

	}
	@GetMapping("/api/stock-list/by_itemId")
	public ResponseEntity<?> stockListByItemId(HttpServletRequest req,@RequestParam("itemId") String itemId){

		List<Stock> stockList = stockService.getStockListByItemId(itemId);
		return new ResponseEntity<List<Stock>>(stockList, HttpStatus.OK);

	}
	@GetMapping(path="/api/stock/list/ItemId", produces = MediaType.APPLICATION_JSON_VALUE)
	  public ResponseEntity<?> stockListByItemAndDate(@RequestParam("itemId") String itemId) {
			List<Stock> stockObj= stockService.getStockListByItemId(itemId);
			return new ResponseEntity<List<Stock>>(stockObj, HttpStatus.OK);
	 }
	@GetMapping("/api/get/item_list")
	 public  ResponseEntity<?> getItems(Model model) {
		 List<ItemMaster> itemList=itemMasterService.getItemList();
			return new ResponseEntity<>(itemList,HttpStatus.OK) ;
	 }
	
	 @GetMapping("/api/get_all_itemList/{pageNo}/{pageSize}")
	    public  ResponseEntity<?> getPaginatedItems(@PathVariable int pageNo, 
	            @PathVariable int pageSize) {

		 List<ItemMaster> itemList=itemMasterService.findPaginatedItems(pageNo, pageSize);
	        return new ResponseEntity<>(itemList,HttpStatus.OK) ;
	    }	
	 
	 @GetMapping("/api/make/list")
		public ResponseEntity<List<Make>> getMakeList() {
			List<Make> makeList = makeService.getMakeList();
			return new ResponseEntity<List<Make>>(makeList, HttpStatus.OK);
		}
	 
	 @PostMapping("/api/make/add")
		public ResponseEntity<?> saveMake(Make make) {
			Boolean savedMake = makeService.saveMake(make);
			return new ResponseEntity<>(savedMake, HttpStatus.OK);
		}

		@PostMapping("/api/make/delete")
		public ResponseEntity<?> deleteMake(@RequestParam("id") Integer id) {
			makeService.deleteMake(id);
			return new ResponseEntity<>(HttpStatus.OK);
		}

		// validate category
		@GetMapping("/api/make/validate/{name}/{id}")
		public ResponseEntity<?> checkDuplicateMake(@PathVariable("name") String name, @PathVariable("id") Integer makeId) {

			boolean makeExist = makeService.checkMakeNameExists(name, makeId);

			return new ResponseEntity<>(makeExist, HttpStatus.OK);
		}
		
		 @PostMapping("/api/items/upload")
		 public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
		        Map<String, Object> response = new HashMap<>();

		        if (file.isEmpty()) {
		            response.put("error", "File is empty");
		            return ResponseEntity.badRequest().body(response);
		        }

		        try {
		            List<String> errors = excelUploadService.validateAndSave(file);
		            if (!errors.isEmpty()) {
		                response.put("errors", errors);
		                return ResponseEntity.badRequest().body(response);
		            }
		            response.put("message", "File uploaded successfully");
		            return ResponseEntity.ok(response);
		        } catch (Exception e) {
		            response.put("error", "Error processing file: " + e.getMessage());
		            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		        }
		    }
		    

}
