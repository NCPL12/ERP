package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.Grn;
import com.ncpl.sales.model.GrnItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.model.SalesOrderDesign;
import com.ncpl.sales.repository.DeliveryChallanItemsRepo;
import com.ncpl.sales.repository.DeliveryChallanRepo;
import com.ncpl.sales.repository.GrnItemRepo;
import com.ncpl.sales.repository.GrnRepo;
import com.ncpl.sales.repository.ItemMasterRepo;
import com.ncpl.sales.repository.PurchaseItemRepo;
import com.ncpl.sales.repository.PurchaseRepo;
import com.ncpl.sales.repository.SalesOrderDesignItemsRepo;
import com.ncpl.sales.repository.SalesOrderDesignRepo;

/**
 * Optimized service for Material Tracker functionality
 * Reduces N+1 query problems by using batch queries and caching
 */
@Service
public class OptimizedMaterialTrackerService {

    @Autowired
    private DeliveryChallanItemsRepo dcItemRepo;
    
    @Autowired
    private PurchaseItemRepo purchaseItemRepo;
    
    @Autowired
    private SalesOrderDesignItemsRepo designItemsRepo;
    
    @Autowired
    private GrnItemRepo grnItemsRepo;
    
    @Autowired
    private DeliveryChallanRepo dcRepo;
    
    @Autowired
    private GrnRepo grnRepo;
    
    @Autowired
    private PurchaseRepo poRepo;
    
    @Autowired
    private ItemMasterRepo itemMasterRepo;
    
    @Autowired
    private SalesOrderDesignRepo designRepo;

    /**
     * Optimized method to get all material tracker data in batch queries
     * instead of individual queries for each sales item
     */
    public Map<String, String> getOptimizedMaterialTrackerData(SalesOrder salesOrder) {
        List<SalesItem> salesItems = salesOrder.getItems();
        List<String> salesItemIds = salesItems.stream()
                .map(SalesItem::getId)
                .collect(Collectors.toList());

        // Batch query for all DC items
        Map<String, List<DeliveryChallanItems>> dcItemsMap = getDcItemsBySalesItemIds(salesItemIds);
        
        // Batch query for all purchase items
        Map<String, List<PurchaseItem>> purchaseItemsMap = getPurchaseItemsBySalesItemIds(salesItemIds);
        
        // Batch query for all design items
        Map<String, List<DesignItems>> designItemsMap = getDesignItemsBySalesItemIds(salesItemIds);
        
        // Batch query for all GRN items
        Map<String, List<GrnItems>> grnItemsMap = getGrnItemsByPurchaseItems(purchaseItemsMap);

        Map<String, String> resultMap = new HashMap<>();
        
        for (SalesItem salesItem : salesItems) {
            String salesItemId = salesItem.getId();
            
            // Calculate quantities using pre-loaded data
            float deliveredQty = calculateDeliveredQty(dcItemsMap.get(salesItemId));
            float purchaseQty = calculatePurchaseQty(purchaseItemsMap.get(salesItemId));
            float designQty = calculateDesignQty(designItemsMap.get(salesItemId));
            float grnQty = calculateGrnQty(grnItemsMap, purchaseItemsMap.get(salesItemId));
            
            float noOrderQty = designQty - purchaseQty;
            
            if (deliveredQty > 0) {
                grnQty = grnQty - deliveredQty;
            }
            
            resultMap.put(salesItemId, deliveredQty + "$" + purchaseQty + "&" + noOrderQty + "%" + grnQty);
        }
        
        return resultMap;
    }

    /**
     * Batch query for DC items by sales item IDs
     */
    
    private Map<String, List<DeliveryChallanItems>> getDcItemsBySalesItemIds(List<String> salesItemIds) {
        List<DeliveryChallanItems> allDcItems = dcItemRepo.findBySalesItemIdIn(salesItemIds);
        return allDcItems.stream()
                .collect(Collectors.groupingBy(DeliveryChallanItems::getDescription));
    }

    /**
     * Batch query for purchase items by sales item IDs
     */
    private Map<String, List<PurchaseItem>> getPurchaseItemsBySalesItemIds(List<String> salesItemIds) {
        List<PurchaseItem> allPurchaseItems = purchaseItemRepo.findBySalesItemIdIn(salesItemIds);
        return allPurchaseItems.stream()
                .collect(Collectors.groupingBy(PurchaseItem::getDescription));
    }

    /**
     * Batch query for design items by sales item IDs
     */
    private Map<String, List<DesignItems>> getDesignItemsBySalesItemIds(List<String> salesItemIds) {
        List<DesignItems> allDesignItems = designItemsRepo.findBySalesItemIdIn(salesItemIds);
        return allDesignItems.stream()
                .collect(Collectors.groupingBy(item -> item.getSalesOrderDesign().getSalesItemId()));
    }

    /**
     * Batch query for GRN items by purchase items
     */
    private Map<String, List<GrnItems>> getGrnItemsByPurchaseItems(Map<String, List<PurchaseItem>> purchaseItemsMap) {
        List<String> purchaseItemIds = purchaseItemsMap.values().stream()
                .flatMap(List::stream)
                .map(pi -> String.valueOf(pi.getPurchase_item_id()))
                .collect(Collectors.toList());
        
        List<GrnItems> allGrnItems = grnItemsRepo.findByDescriptionIn(purchaseItemIds);
        return allGrnItems.stream()
                .collect(Collectors.groupingBy(GrnItems::getDescription));
    }

    /**
     * Get optimized data for Excel generation
     */
    public Map<String, Object> getOptimizedExcelData(SalesOrder salesOrder) {
        List<SalesItem> salesItems = salesOrder.getItems();
        List<String> salesItemIds = salesItems.stream()
                .map(SalesItem::getId)
                .collect(Collectors.toList());

        // Batch load all related data
        Map<String, List<DesignItems>> designItemsMap = getDesignItemsBySalesItemIds(salesItemIds);
        Map<String, List<PurchaseItem>> purchaseItemsMap = getPurchaseItemsBySalesItemIds(salesItemIds);
        Map<String, List<DeliveryChallanItems>> dcItemsMap = getDcItemsBySalesItemIds(salesItemIds);
        
        // Get all unique item IDs and design IDs for batch loading
        Set<String> itemIds = designItemsMap.values().stream()
                .flatMap(List::stream)
                .map(DesignItems::getItemId)
                .collect(Collectors.toSet());
        
        Set<Long> designIds = designItemsMap.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getSalesOrderDesign() != null)
                .map(item -> item.getSalesOrderDesign().getId())
                .collect(Collectors.toSet());
        
        // Batch load items and designs
        Map<String, ItemMaster> itemsMap = itemMasterRepo.findByIdIn(new ArrayList<>(itemIds))
                .stream()
                .collect(Collectors.toMap(ItemMaster::getId, item -> item));
        
        Iterable<SalesOrderDesign> designsIterable = designRepo.findAllById(new ArrayList<>(designIds));
        Map<Long, SalesOrderDesign> designsMap = StreamSupport.stream(designsIterable.spliterator(), false)
                .collect(Collectors.toMap(SalesOrderDesign::getId, design -> design));

        // Get all purchase order IDs for batch loading
        Set<String> poIds = purchaseItemsMap.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getPurchaseOrder() != null)
                .map(item -> item.getPurchaseOrder().getPoNumber())
                .collect(Collectors.toSet());
        
        Map<String, PurchaseOrder> poMap = poRepo.findByPoNumberIn(new ArrayList<>(poIds))
                .stream()
                .collect(Collectors.toMap(PurchaseOrder::getPoNumber, po -> po));

        // Get all GRN IDs for batch loading
        Set<String> grnIds = poIds; // Assuming GRN uses same IDs as PO
        Map<String, List<Grn>> grnMap = grnRepo.findByPoNumberIn(new ArrayList<>(grnIds))
                .stream()
                .collect(Collectors.groupingBy(Grn::getPoNumber));

        // Get all DC IDs for batch loading
        Set<Integer> dcIds = dcItemsMap.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getDeliveryChallan() != null)
                .map(item -> item.getDeliveryChallan().getDcId())
                .collect(Collectors.toSet());
        
        Map<Integer, DeliveryChallan> dcMap = dcRepo.findByDcIdInWithItems(new ArrayList<>(dcIds))
                .stream()
                .collect(Collectors.toMap(DeliveryChallan::getDcId, dc -> dc, (existing, duplicate) -> existing));

        Map<String, Object> result = new HashMap<>();
        result.put("designItemsMap", designItemsMap);
        result.put("purchaseItemsMap", purchaseItemsMap);
        result.put("dcItemsMap", dcItemsMap);
        result.put("itemsMap", itemsMap);
        result.put("designsMap", designsMap);
        result.put("poMap", poMap);
        result.put("grnMap", grnMap);
        result.put("dcMap", dcMap);
        
        return result;
    }

    private float calculateDeliveredQty(List<DeliveryChallanItems> dcItems) {
        if (dcItems == null || dcItems.isEmpty()) {
            return 0;
        }
        return (float) dcItems.stream()
                .mapToDouble(DeliveryChallanItems::getTodaysQty)
                .sum();
    }

    private float calculatePurchaseQty(List<PurchaseItem> purchaseItems) {
        if (purchaseItems == null || purchaseItems.isEmpty()) {
            return 0;
        }
        return (float) purchaseItems.stream()
                .mapToDouble(PurchaseItem::getQuantity)
                .sum();
    }

    private float calculateDesignQty(List<DesignItems> designItems) {
        if (designItems == null || designItems.isEmpty()) {
            return 0;
        }
        return (float) designItems.stream()
                .mapToDouble(DesignItems::getQuantity)
                .sum();
    }

    private float calculateGrnQty(Map<String, List<GrnItems>> grnItemsMap, List<PurchaseItem> purchaseItems) {
        if (purchaseItems == null || purchaseItems.isEmpty()) {
            return 0;
        }
        
        float totalGrnQty = 0;
        for (PurchaseItem purchaseItem : purchaseItems) {
            List<GrnItems> grnItems = grnItemsMap.get(String.valueOf(purchaseItem.getPurchase_item_id()));
            if (grnItems != null) {
                totalGrnQty += grnItems.stream()
                        .mapToDouble(GrnItems::getReceivedQuantity)
                        .sum();
            }
        }
        return totalGrnQty;
    }
}
