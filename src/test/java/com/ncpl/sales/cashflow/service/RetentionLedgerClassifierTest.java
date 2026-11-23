package com.ncpl.sales.cashflow.service;
import com.ncpl.sales.cashflow.model.Invoice;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
class RetentionLedgerClassifierTest {
    @Test void matchesExistingLedgerLabelsWithoutGuessingOrdinaryInvoices(){
        assertTrue(RetentionLedgerClassifier.isRetention("Retention-JSS"));
        assertTrue(RetentionLedgerClassifier.isRetention("Retention - Manipal Udupi"));
        assertTrue(RetentionLedgerClassifier.isRetention("retention_VJ Bio"));
        assertTrue(RetentionLedgerClassifier.isRetention("Retainage payable"));
        assertFalse(RetentionLedgerClassifier.isRetention("L&T Technology Services Limited"));
        assertFalse(RetentionLedgerClassifier.isRetention(null));
    }
    @Test void exportSeparatesRetentionWithoutAddingItToOutstanding() throws Exception {
        Invoice retention=new Invoice("Retention-JSS","R1",LocalDate.now(),LocalDate.now(),100,Invoice.InvoiceType.INFLOW);
        Invoice ordinary=new Invoice("Customer","B1",LocalDate.now(),LocalDate.now(),200,Invoice.InvoiceType.INFLOW);
        byte[] bytes=new ExcelExportService().exportOutstandingInvoices(Arrays.asList(retention,ordinary),"Test");
        try(XSSFWorkbook book=new XSSFWorkbook(new ByteArrayInputStream(bytes))){
            assertEquals(100,book.getSheetAt(0).getRow(3).getCell(4).getNumericCellValue());
            assertEquals(100,book.getSheetAt(0).getRow(3).getCell(9).getNumericCellValue());
            assertEquals(0,book.getSheetAt(0).getRow(4).getCell(9).getNumericCellValue());
        }
    }
}
