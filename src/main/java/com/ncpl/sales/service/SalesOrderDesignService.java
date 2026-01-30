package com.ncpl.sales.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.model.Stock;
import com.ncpl.sales.repository.SalesOrderDesignItemsRepo;
import com.ncpl.sales.repository.SalesOrderDesignRepo;

@Service
public class SalesOrderDesignService {

	@Autowired
	SalesOrderDesignRepo designSo;
	@Autowired
	ItemMasterService itemService;
	@PersistenceContext
	private EntityManager em;
	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;
	@Autowired
	SalesOrderDesignItemsRepo designItemRepo;
	@Autowired
	PurchaseItemService poItemService;
	@Autowired
	GrnService grnService;
	@Autowired
	DeliveryChallanService dcService;
	@Autowired
	SalesService salesService;
	@Autowired
	StockService stockService;
	
	public SalesOrderDesign save(SalesOrderDesign design) {
		String soItemId = design.getSalesItemId();
		List<DesignItems> items = design.getItems();
		SalesOrderDesign soDesignObj = designSo.getDesginObjBySoItemId(soItemId);
		
		if(soDesignObj!=null) {
			List<DesignItems> existingItems = soDesignObj.getItems();
			for (DesignItems designItems : items) {
				existingItems.add(designItems);
				designItems.setSalesOrderDesign(soDesignObj);
			}
			return designSo.save(soDesignObj);
		}else {
			for (DesignItems designItems : items) {
				designItems.setSalesOrderDesign(design);
			}
			
			return designSo.save(design);
		}
		

	}

	public List<DesignItems> getSalesOrderDesignItemListBySalesItemId(String salesItemId) {
		List<SalesOrderDesign> designList = designSo.getDesginListBySoItemId(salesItemId);
		ArrayList<DesignItems> list = new ArrayList<DesignItems>();
		for (SalesOrderDesign salesOrderDesign : designList) {
			List<DesignItems> designItemList = salesOrderDesign.getItems();
			for (DesignItems designItems : designItemList) {
				String itemId = designItems.getItemId();
				Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
				System.out.println(itemId);
				designItems.setItemId(itemObj.get().getModel());
				
				designItems.setQuantity(designItems.getQuantity());
				designItems.set("designId", salesOrderDesign.getId());
				designItems.set("itemMasterId", itemId);
				designItems.set("unit", itemObj.get().getItem_units().getName());
				designItems.set("salesItemId", salesOrderDesign.getSalesItemId());
				list.add(designItems);
			}
		}
		return list;
	}
	
	public List<DesignItems> getDesignItemListBySalesItemId(String salesItemId) {
        List<SalesOrderDesign> designList = designSo.getDesginListBySoItemId(salesItemId);
        ArrayList<DesignItems> list = new ArrayList<DesignItems>();
        // Ensure the referenced SalesItem exists; if not, return empty list gracefully
        Optional<SalesItem> salesItemOpt = salesService.getSalesItemObjById(salesItemId);
        if (!salesItemOpt.isPresent()) {
            return list;
        }
        String partyId = salesItemOpt.get().getSalesOrder().getParty().getId();
        for (SalesOrderDesign salesOrderDesign : designList) {
            List<DesignItems> designItemList = salesOrderDesign.getItems();
            for (DesignItems designItems : designItemList) {
                String itemId = designItems.getItemId();
                Optional<ItemMaster> itemObj = itemService.getItemById(itemId);
                List<Stock> stock = stockService.getStockListByItemIdAndClientId(itemId, partyId);
                // Set a readable identifier for the UI: prefer model if item present, else keep original itemId
                if (itemObj.isPresent()) {
                    designItems.setItemId(itemObj.get().getModel());
                } else {
                    designItems.setItemId(itemId);
                }

                designItems.setQuantity(designItems.getQuantity());
                designItems.set("designId", salesOrderDesign.getId());
                designItems.set("itemMasterId", itemId);
                designItems.set("salesItemId", salesOrderDesign.getSalesItemId());
                // Unit information if available
                if (itemObj.isPresent() && itemObj.get().getItem_units() != null) {
                    designItems.set("unit", itemObj.get().getItem_units().getName());
                } else {
                    designItems.set("unit", "");
                }
                // Present stock quantity
                if (stock.isEmpty()) {
                    designItems.set("presentQty", 0);
                } else {
                    designItems.set("presentQty", stock.get(0).getQuantity());
                }
                list.add(designItems);
            }
        }
        return list;
    }
  /*
   * Here checking whether dc or grn created for if created not allowing to delete desing item..
   */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean deleteDesignByDesignItemId(long id,long designId) {
        boolean isDeleted = false;
        Optional<SalesOrderDesign> soDesignObj = designSo.findById(designId);
        if (!soDesignObj.isPresent()) {
            return false;
        }
        String soItemId = soDesignObj.get().getSalesItemId();
        Optional<DesignItems> designItemObj = designItemRepo.findById(id);
        if (!designItemObj.isPresent()) {
            return false;
        }
        String designItemId = designItemObj.get().getItemId();
        PurchaseItem poItem =poItemService.getPurchaseItemBySalesItemIdAndItemId(soItemId, designItemId);
        List<GrnItems> grnItemsList = new ArrayList();
        if(poItem!=null) {
         grnItemsList = grnService.getGrnItemByPoItemId(Integer.toString(poItem.getPurchase_item_id()));
        
        }
        List<DeliveryChallanItems> dcItemsList = dcService.getDcItemListBySoItemIdWhereDcQtyNotZero(soItemId);
        if(grnItemsList.size()>0 || dcItemsList.size()>0 || poItem!=null) {
            isDeleted = false;
        }
        else {
        designItemRepo.deleteById(id);
        isDeleted = true;
        List<DesignItems> itemlist =  designItemRepo.findDesignItemListByDesignId(designId);
        if(itemlist.isEmpty()) {
            designSo.deleteById(designId);
        }
        }
        return isDeleted;
    }

	public boolean checkDuplicateItemIdExists(List<String> itemIdList, String salesItemId) {
		SalesOrderDesign soDesignObj = designSo.getDesginObjBySoItemId(salesItemId);
		//List<DesignItems> designItems=soDesignObj.getItems();
		boolean response = false;
		if(soDesignObj!=null) {
		
		for (int i = 0; i < itemIdList.size(); i++) {
			String itemId=itemIdList.get(i);
		
			List<DesignItems> designItems = designItemRepo.findDesignItemListByItemIdAndDesignId(itemId, soDesignObj.getId());
			if(designItems.size()>=1) {
				response = true;
				break;
			}else {
				response = false;
			}
		}

		}
		return response;
	}

	public List<SalesOrderDesign> findSalesOrderDesignBysalesItemId(String soItemId) {
		List<SalesOrderDesign> designList = designSo.getDesginListBySoItemId(soItemId);
		return designList;
	}

	public SalesOrderDesign findSalesOrderDesignObjBysalesItemId(String soItemId) {
		SalesOrderDesign designObj = designSo.getDesginObjBySoItemId(soItemId);
		return designObj;
	}

	public SalesOrderDesign findSalesOrderDesignObjBysalesItemIdAndDate(String soItemId, Timestamp sqlFromDate,
			Timestamp sqlToDate) {
		// TODO Auto-generated method stub
		SalesOrderDesign designObj = designSo.getDesginObjBySoItemIdByDate(sqlFromDate,sqlToDate,soItemId);
		return designObj;
	}
	public List<DesignItems> getDesignItemListBySOItemId(String salesItemId) {
		List<SalesOrderDesign> designList = designSo.getDesginListBySoItemId(salesItemId);
		ArrayList<DesignItems> list = new ArrayList<DesignItems>();
		for (SalesOrderDesign salesOrderDesign : designList) {
			List<DesignItems> designItemList = salesOrderDesign.getItems();
			list.addAll(designItemList);
		}
		return list;
	}

	public List<DesignItems> getDesignItemListByItemId(String itemId) {
		List<DesignItems> designItemList = designItemRepo.findDesignItemListByItemId(itemId);
		return designItemList;
	}

	public Optional<SalesOrderDesign> findSalesOrderDesignById(long designId) {
		Optional<SalesOrderDesign> soDesign = designSo.findById(designId);
		return soDesign;
	}

	public List<DesignItems> getDesignItemListByDesignId(long designId){
		List<DesignItems> designItemList = designItemRepo.findDesignItemListByDesignId(designId);
		return designItemList;
	}
	
	public List<DesignItems> getAllDesignItemListBySOItemId(String salesItemId) {
		List<SalesOrderDesign> designList = designSo.getDesginListBySoItemId(salesItemId);
		ArrayList<DesignItems> list = new ArrayList<DesignItems>();
		for (SalesOrderDesign salesOrderDesign : designList) {
			List<DesignItems> designItemList = getDesignItemListByDesignId(salesOrderDesign.getId());
			list.addAll(designItemList);
		}
		return list;
	}
	
}
