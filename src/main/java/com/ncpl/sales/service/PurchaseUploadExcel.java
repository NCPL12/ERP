package com.ncpl.sales.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ncpl.sales.model.DesignItems;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.repository.PartyRepo;
import com.ncpl.sales.repository.SalesRepo;

@Service
@Transactional
public class PurchaseUploadExcel {

    private static final int VENDOR_ROW = 1;
    private static final int ITEM_START_ROW = 5;

    @Autowired
    private PurchaseOrderService purchaseService;
    @Autowired
    private PartyRepo partyRepo;
    @Autowired
   private SalesService salesService;
    @Autowired
    private ItemMasterService itemService;
    @Autowired
    private SalesRepo salesRepo;
    @Autowired
    private PurchaseItemService purchaseItemService;

    public List<String> processExcelFile(MultipartFile file) {

        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                errors.add("Excel sheet is empty.");
                return errors;
            }

            PurchaseOrder purchaseOrder = new PurchaseOrder();
            List<PurchaseItem> purchaseItems = new ArrayList<>();

            // /* ---------------- Vendor Validation ---------------- */
            // Row vendorRow = sheet.getRow(VENDOR_ROW);
            // if (vendorRow == null) {
            //     errors.add("Supplier row is missing.");
            //     return errors;
            // }

            // String vendorName = getStringCellValue(vendorRow.getCell(0));
            // if (vendorName.isEmpty()) {
            //     errors.add("Supplier Name is mandatory.");
            //     return errors;
            // }
            /* ---------------- Vendor Validation ---------------- */
//Row vendorRow = sheet.getRow(VENDOR_ROW);
//if (vendorRow == null) {
//    errors.add("Supplier row is missing.");
//    return errors;
//}
//String vendorName = getStringCellValue(vendorRow.getCell(0));
String vendorName = "";
Row vendorRow = sheet.getRow(VENDOR_ROW);              // purana format: row 2, column A
if (vendorRow != null) {
    vendorName = getStringCellValue(vendorRow.getCell(0));
}
if (vendorName.isEmpty()) {
    Row firstRow = sheet.getRow(0);                    // naya format: A1 = "Vendor Name", B1 = naam
    if (firstRow != null) {
        vendorName = getStringCellValue(firstRow.getCell(1));
    }
}
if (vendorName.isEmpty()) {
    errors.add("Supplier Name is mandatory.");
    return errors;
}

            Party party = partyRepo.findByName(vendorName.trim());
            if (party == null) {
                errors.add("Supplier does not exist.");
                return errors;
            }
            purchaseOrder.setParty(party);

            /* ---------------- Item Rows ---------------- */

            // NEW: state used across rows
            String lastClientPo = "";                                            // for fill-down of blank Client PO
            java.util.Map<String, Float> enteredInFile = new java.util.HashMap<>(); // qty already accepted in this file
            int cntFullyOrdered = 0, cntInvalidPo = 0, cntMissingPo = 0;         // for summary

            for (int rowNum = ITEM_START_ROW; rowNum <= sheet.getLastRowNum(); rowNum++) {

                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                List<String> rowErrors = new ArrayList<>();
                PurchaseItem poItem = new PurchaseItem();

                String clientPoRaw = normPo(getStringCellValue(row.getCell(0)));   // CHANGED: normalised
                String description = getStringCellValue(row.getCell(1));
                String modelNo = getStringCellValue(row.getCell(5));
                String hsn = getStringCellValue(row.getCell(6));

                // NEW: skip fully blank rows
                if (clientPoRaw.isEmpty() && description.isEmpty() && modelNo.isEmpty() && hsn.isEmpty()) continue;

                // NEW: blank Client PO = same as row above (remove this if blank must stay an error)
                String clientPoNum = clientPoRaw.isEmpty() ? lastClientPo : clientPoRaw;
                if (!clientPoRaw.isEmpty()) lastClientPo = clientPoRaw;

                if (clientPoNum.isEmpty()) { rowErrors.add("Client PO Number is mandatory."); cntMissingPo++; }
                if (description.isEmpty()) rowErrors.add("Description is mandatory.");
                if (modelNo.isEmpty()) rowErrors.add("Model Number is mandatory.");
                if (hsn.isEmpty()) rowErrors.add("HSN is mandatory.");

                // CHANGED: try exact value first, then without "-Amendend" suffix
                SalesOrder salesOrder = null;
                if (!clientPoNum.isEmpty()) {
                    salesOrder = salesRepo.getSalesOrderByClientPoNumber(clientPoNum);
                    if (salesOrder == null) {
                        String basePo = stripAmend(clientPoNum);
                        if (!basePo.equals(clientPoNum)) {
                            salesOrder = salesRepo.getSalesOrderByClientPoNumber(basePo);
                            if (salesOrder != null) clientPoNum = basePo;
                        }
                    }
                    if (salesOrder == null) {
                        rowErrors.add("Invalid Client PO Number: " + clientPoNum);   // CHANGED: shows the value searched
                        cntInvalidPo++;
                    }
                }

                ItemMaster item = itemService.getItemByModelNo(modelNo);
                if (item == null) {
                    rowErrors.add("Invalid Model Number.");
                } else {
                    poItem.setModelNo(item.getId());
                    poItem.setPoDescription(item.getItemName());
                }

                float salesQty = 0;
                float orderedQty = 0;
                java.util.Set<String> orderedInPos = new java.util.LinkedHashSet<>();   // NEW: which POs already used the qty

                if (item != null && salesOrder != null) {

                    SalesItem salesItem =
                            salesService.getSalesItemByName(description, clientPoNum, item.getId());

                    if (salesItem == null) {
                        rowErrors.add("Description does not match Sales Item.");
                    } else {

                        poItem.setDescription(salesItem.getId());

                        List<DesignItems> designs =
                                purchaseService.getDesignListOfItemBySalesItemId(salesItem.getId());

                        for (DesignItems d : designs) {
                            if (d.getItemId().equals(item.getId())) {
                                salesQty += d.getQuantity();
                            }
                        }

                        List<PurchaseItem> existingItems =
                                purchaseItemService.getPurchaseItemListBySalesItemIdAndItemId(
                                        salesItem.getId(), item.getId());

                        for (PurchaseItem p : existingItems) {
                            orderedQty += p.getQuantity();
                            if (p.getPurchaseOrder() != null && p.getPurchaseOrder().getPoNumber() != null) {   // NEW
                                orderedInPos.add(p.getPurchaseOrder().getPoNumber());                            // NEW
                            }
                        }
                    }
                }

                float remainingQty = salesQty - orderedQty;

                Float qty = getFloatCellValue(row.getCell(7), rowErrors, "Quantity");
                if (qty == null) qty = 0f;

                // NEW: same Client PO + item + description repeated in the file (e.g. amended rows)
                String qtyKey = clientPoNum + "|" + (item != null ? item.getId() : "") + "|" + description;
                float alreadyInFile = enteredInFile.containsKey(qtyKey) ? enteredInFile.get(qtyKey) : 0f;
                remainingQty = remainingQty - alreadyInFile;

                if (qty < 0) {
                    rowErrors.add("Quantity cannot be negative.");
                } else if (salesOrder != null && item != null) {   // CHANGED: no quantity noise when PO / model is invalid
                    if (qty > salesQty) {
                        rowErrors.add("Entered quantity exceeds Design Quantity.");
                    } else if (qty > remainingQty + 0.0001f) {
                        if (remainingQty <= 0) cntFullyOrdered++;
                       //rowErrors.add("Remaining Quantity available: " + remainingQty);
rowErrors.add("Remaining Quantity available: " + Math.max(remainingQty, 0f)
        + " (Design qty: " + salesQty + ", already ordered: " + orderedQty
        + (orderedInPos.isEmpty() ? "" : " in PO " + String.join(", ", orderedInPos))   // NEW
        + ", in this file: " + alreadyInFile + ", you entered: " + qty + ")");
                    } else {
                        enteredInFile.put(qtyKey, alreadyInFile + qty);   // NEW: count only accepted rows
                    }
                }

                Float unitPrice = getFloatCellValue(row.getCell(8), rowErrors, "Unit Price");
                if (unitPrice == null) unitPrice = 0f;
                if (unitPrice < 0) rowErrors.add("Unit Price cannot be negative.");

                poItem.setQuantity(qty);
                poItem.setUnitPrice(unitPrice);
                poItem.setHsnCode(hsn);
                poItem.setAmount(qty * unitPrice);

                if (!rowErrors.isEmpty()) {
                    errors.add("Row " + (rowNum + 1) + ": " + String.join(" | ", rowErrors));
                    continue;
                }

                poItem.setPurchaseOrder(purchaseOrder);
                purchaseItems.add(poItem);
            }

            purchaseOrder.setItems(purchaseItems);

            // NEW: summary line at the top of the error list
            if (!errors.isEmpty()) {
                errors.add(0, "SUMMARY: " + errors.size() + " row(s) failed - "
                        + cntFullyOrdered + " fully ordered, "
                        + cntInvalidPo + " invalid Client PO, "
                        + cntMissingPo + " missing Client PO.");
            }

            if (errors.isEmpty()) {
                purchaseService.savePurchaseOrder(purchaseOrder, "", party.getId());
            }

        } catch (IOException e) {
            errors.add("Error reading Excel file: " + e.getMessage());
        }

        return errors;
    }

    /* ---------------- Helper Methods ---------------- */

    // NEW: trim, collapse repeated spaces, fix " / " -> "/" (no case change, so DB lookup is unaffected)
    private String normPo(String s) {
        if (s == null) return "";
        return s.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*/\\s*", "/");
    }

    // NEW: "UHP/sp/WO/14/2024-25-Amendend" -> "UHP/sp/WO/14/2024-25"
    private String stripAmend(String po) {
        return po.replaceAll("(?i)[-\\s]*AMEND\\w*$", "");
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {

            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue().trim();

            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd")
                            .format(cell.getDateCellValue());
                }
                return String.valueOf((long) cell.getNumericCellValue());

            case Cell.CELL_TYPE_BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();

            default:
                return "";
        }
    }


    private Float getFloatCellValue(Cell cell, List<String> errors, String field) {
        if (cell == null) {
            errors.add(field + " is mandatory.");
            return null;
        }

        switch (cell.getCellType()) {

            case Cell.CELL_TYPE_NUMERIC:
                return (float) cell.getNumericCellValue();

            case Cell.CELL_TYPE_STRING:
                try {
                    return Float.parseFloat(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    errors.add(field + " must be numeric.");
                    return null;
                }

            default:
                errors.add(field + " must be numeric.");
                return null;
        }
    }

    private Date getDateCellValue(Cell cell, List<String> errors, String field) {
        if (cell == null) {
            errors.add(field + " is mandatory.");
            return null;
        }

        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return new Date(cell.getDateCellValue().getTime());
        }

        errors.add(field + " must be a valid date.");
        return null;
    }

}
