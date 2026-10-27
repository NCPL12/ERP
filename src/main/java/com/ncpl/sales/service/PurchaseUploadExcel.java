package com.ncpl.sales.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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

            /* ---------------- Vendor Validation ---------------- */
            Row vendorRow = sheet.getRow(VENDOR_ROW);
            String vendorName = vendorRow == null ? "" : getStringCellValue(vendorRow.getCell(0));
            if (vendorName.isEmpty()) {
                Row firstRow = sheet.getRow(0);
                vendorName = firstRow == null ? "" : getStringCellValue(firstRow.getCell(1));
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
            Map<String, Float> enteredInFile = new HashMap<>();
            int fullyOrdered = 0;
            int invalidPo = 0;
            int missingPo = 0;
            for (int rowNum = ITEM_START_ROW; rowNum <= sheet.getLastRowNum(); rowNum++) {

                Row row = sheet.getRow(rowNum);
                if (isBlankRow(row)) continue;

                List<String> rowErrors = new ArrayList<>();
                PurchaseItem poItem = new PurchaseItem();

                String clientPoNum = normalizePo(getStringCellValue(row.getCell(0)));
                if (clientPoNum.isEmpty()) {
                    clientPoNum = normalizePo(getMergedPoNumber(sheet, rowNum));
                }
                String description = getStringCellValue(row.getCell(1));
                String modelNo = getStringCellValue(row.getCell(5));
                String hsn = getStringCellValue(row.getCell(6));

                if (clientPoNum.isEmpty()) {
                    rowErrors.add("Client PO Number is mandatory.");
                    missingPo++;
                }
                if (description.isEmpty()) rowErrors.add("Description is mandatory.");
                if (modelNo.isEmpty()) rowErrors.add("Model Number is mandatory.");
                if (hsn.isEmpty()) rowErrors.add("HSN is mandatory.");

                SalesOrder salesOrder = clientPoNum.isEmpty() ? null : salesRepo.getSalesOrderByClientPoNumber(clientPoNum);
                if (salesOrder == null && !clientPoNum.isEmpty()) {
                    rowErrors.add("Invalid Client PO Number: " + clientPoNum);
                    invalidPo++;
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
                Set<String> orderedInPos = new LinkedHashSet<>();
                SalesItem salesItem = null;

                if (item != null && salesOrder != null) {

                    salesItem =
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
                            if (p.getPurchaseOrder() != null && p.getPurchaseOrder().getPoNumber() != null) {
                                orderedInPos.add(p.getPurchaseOrder().getPoNumber());
                            }
                        }
                    }
                }

                String quantityKey = salesItem == null || item == null ? "" : salesItem.getId() + "|" + item.getId();
                float alreadyInFile = enteredInFile.getOrDefault(quantityKey, 0f);
                float remainingQty = salesQty - orderedQty - alreadyInFile;

                Float qty = getFloatCellValue(row.getCell(7), rowErrors, "Quantity");
                if (qty == null) qty = 0f;

                if (qty < 0) {
                    rowErrors.add("Quantity cannot be negative.");
                } else if (salesItem != null) {
                    if (qty > salesQty) rowErrors.add("Entered quantity exceeds Design Quantity.");
                    if (qty > remainingQty + 0.0001f) {
                        if (remainingQty <= 0) fullyOrdered++;
                        rowErrors.add("Remaining Quantity available: " + Math.max(remainingQty, 0f)
                                + " (Design qty: " + salesQty + ", already ordered: " + orderedQty
                                + (orderedInPos.isEmpty() ? "" : " in PO " + String.join(", ", orderedInPos))
                                + ", in this file: " + alreadyInFile + ", you entered: " + qty + ")");
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
                enteredInFile.put(quantityKey, alreadyInFile + qty);
            }

            purchaseOrder.setItems(purchaseItems);

            if (purchaseItems.isEmpty() && errors.isEmpty()) {
                errors.add("No purchase items found in the Excel file.");
                return errors;
            }

            if (!errors.isEmpty()) {
                errors.add(0, "SUMMARY: " + errors.size() + " row(s) failed - "
                        + fullyOrdered + " fully ordered, " + invalidPo + " invalid Client PO, "
                        + missingPo + " missing Client PO.");
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

    private boolean isBlankRow(Row row) {
        if (row == null) return true;
        for (int column = 0; column < row.getLastCellNum(); column++) {
            if (!getStringCellValue(row.getCell(column)).isEmpty()) return false;
        }
        return true;
    }

    private String getMergedPoNumber(Sheet sheet, int rowNum) {
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(rowNum, 0)) {
                Row anchor = sheet.getRow(region.getFirstRow());
                return anchor == null ? "" : getStringCellValue(anchor.getCell(region.getFirstColumn()));
            }
        }
        return "";
    }

    private String normalizePo(String po) {
        return po.trim().replaceAll("\\s+", " ").replaceAll("\\s*/\\s*", "/");
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
