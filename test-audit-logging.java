package com.ncpl.sales.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.service.SalesOrderAuditService;

public class AuditTest {
    public static void main(String[] args) {
        try {
            // Test JSON serialization of SalesOrder with addresses
            SalesOrder oldOrder = new SalesOrder();
            oldOrder.setId("TEST-001");
            oldOrder.setShippingAddress("Old Shipping Address");
            oldOrder.setBillingAddress("Old Billing Address");
            oldOrder.setClientPoNumber("PO-OLD-001");
            
            SalesOrder newOrder = new SalesOrder();
            newOrder.setId("TEST-001");
            newOrder.setShippingAddress("New Shipping Address");
            newOrder.setBillingAddress("New Billing Address");
            newOrder.setClientPoNumber("PO-OLD-001");
            
            ObjectMapper mapper = new ObjectMapper();
            
            String oldJson = mapper.writeValueAsString(oldOrder);
            String newJson = mapper.writeValueAsString(newOrder);
            
            System.out.println("=== AUDIT TEST RESULTS ===");
            System.out.println("Old Order JSON:");
            System.out.println(oldJson);
            System.out.println("\nNew Order JSON:");
            System.out.println(newJson);
            
            // Check if addresses are included in JSON
            boolean oldHasShipping = oldJson.contains("shippingAddress");
            boolean oldHasBilling = oldJson.contains("billingAddress");
            boolean newHasShipping = newJson.contains("shippingAddress");
            boolean newHasBilling = newJson.contains("billingAddress");
            
            System.out.println("\n=== ADDRESS DETECTION ===");
            System.out.println("Old JSON has shippingAddress: " + oldHasShipping);
            System.out.println("Old JSON has billingAddress: " + oldHasBilling);
            System.out.println("New JSON has shippingAddress: " + newHasShipping);
            System.out.println("New JSON has billingAddress: " + newHasBilling);
            
            if (oldHasShipping && newHasShipping && oldHasBilling && newHasBilling) {
                System.out.println("\n✅ SUCCESS: Addresses are properly serialized to JSON");
            } else {
                System.out.println("\n❌ ISSUE: Addresses are not being serialized properly");
            }
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
