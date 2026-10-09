package com.ncpl.sales.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ncpl.sales.model.MonthlyStockMovement;
import com.ncpl.sales.repository.MonthlyStockMovementRepo;

@Service
public class MonthlyStockBaselineImportService {
    private final MonthlyStockMovementRepo repository;

    public MonthlyStockBaselineImportService(MonthlyStockMovementRepo repository) {
        this.repository = repository;
    }

    @Transactional
    public int importCurrentItemStock(InputStream input, LocalDate closingDate, String sourceName) throws Exception {
        if (repository.existsByReportDate(closingDate)) {
            throw new IllegalStateException("A monthly stock snapshot already exists for " + closingDate);
        }
        List<MonthlyStockMovement> records = new ArrayList<MonthlyStockMovement>();
        try (Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeaders(sheet.getRow(1));
            for (int rowIndex = 2; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;
                String itemId = text(row.getCell(0));
                if (itemId.isEmpty() || "total".equalsIgnoreCase(itemId)) continue;
                String modelNo = text(row.getCell(1));
                String description = text(row.getCell(2));
                BigDecimal quantity = number(row.getCell(7));
                BigDecimal rate = number(row.getCell(10));
                MonthlyStockMovement record = new MonthlyStockMovement();
                record.setItemMasterId(itemId);
                record.setReportDate(closingDate);
                record.setModelNo(modelNo);
                record.setDescription(description);
                record.setClosingQty(quantity);
                record.setClosingRate(rate);
                record.setClosingValue(quantity.multiply(rate).setScale(2, RoundingMode.HALF_UP));
                record.setFrozen(true);
                record.setSource(sourceName + (quantity.signum() != 0 && rate.signum() == 0 ? " [PRICE MISSING]" : ""));
                record.setCreatedAt(LocalDateTime.now());
                records.add(record);
            }
        }
        repository.saveAll(records);
        return records.size();
    }

    private static void validateHeaders(Row row) {
        if (row == null || !"Id".equalsIgnoreCase(text(row.getCell(0)))
                || !"Stock".equalsIgnoreCase(text(row.getCell(7)))
                || !"Supply Price".equalsIgnoreCase(text(row.getCell(10)))) {
            throw new IllegalArgumentException("Expected Current Item Stock columns: Id, Stock and Supply Price");
        }
    }

    private static String text(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == Cell.CELL_TYPE_STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
        return "";
    }

    private static BigDecimal number(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
        String value = text(cell).replace(",", "");
        return value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
    }
}
