package com.ncpl.sales.service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Make;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.ItemMasterRepo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ItemUploadCheckTest {
    private final ItemUploadService upload = new ItemUploadService();
    private final ItemMasterRepo repository = mock(ItemMasterRepo.class);
    private final ItemMasterService items = mock(ItemMasterService.class);
    private final MakeService makes = mock(MakeService.class);
    private ItemMaster saved;

    public ItemUploadCheckTest() {
        ReflectionTestUtils.setField(upload, "itemRepository", repository);
        ReflectionTestUtils.setField(upload, "itemService", items);
        ReflectionTestUtils.setField(upload, "makeService", makes);
        Make make = new Make(); make.setId(7);
        when(makes.getMakeByNaeme("Acme")).thenReturn(make);
        when(items.getUnitsByName("Nos")).thenReturn(new Units());
        when(repository.saveAll(anyList())).thenAnswer(call -> {
            List<ItemMaster> list = call.getArgument(0);
            if (!list.isEmpty()) saved = list.get(0);
            return list;
        });
    }

    private List<String> check(Workbook workbook, boolean validRow, boolean blankRow) throws Exception {
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(0).setCellValue("Model");
        if (validRow) {
            Row row = sheet.createRow(1);
            String[] values = {"MODEL-1", "Widget", "8536", "Nos", "", "Shelf B", "Acme", "Yes"};
            for (int i = 0; i < values.length; i++) row.createCell(i).setCellValue(values[i]);
            row.getCell(4).setCellValue(18);
        }
        if (blankRow) sheet.createRow(2).createCell(0).setCellValue(" ");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out); workbook.close();
        return upload.validateAndSave(new MockMultipartFile("file", workbook instanceof HSSFWorkbook ? "items.xls" : "items.xlsx", "application/octet-stream", out.toByteArray()));
    }

    @Test public void locationUsesLocationColumn() throws Exception {
        assertTrue(check(new XSSFWorkbook(), true, false).isEmpty());
        assertEquals("Shelf B", saved.getLocation());
    }
    @Test public void makeUsesMakeId() throws Exception {
        assertTrue(check(new XSSFWorkbook(), true, false).isEmpty());
        assertEquals("7", saved.getMake());
    }
    @Test public void advertisedXlsFormatIsAccepted() throws Exception {
        List<String> errors = check(new HSSFWorkbook(), true, false);
        assertTrue(errors.toString(), errors.isEmpty());
    }
    @Test public void headerOnlyFileIsRejected() throws Exception {
        assertFalse("An empty upload must not report success", check(new XSSFWorkbook(), false, false).isEmpty());
    }
    @Test public void blankTrailingRowsAreSkipped() throws Exception {
        List<String> errors = check(new XSSFWorkbook(), true, true);
        assertTrue(errors.toString(), errors.isEmpty());
    }
    @Test public void invalidRowsPreventSaving() throws Exception {
        when(items.getUnitsByName("Nos")).thenReturn(null);
        assertFalse(check(new XSSFWorkbook(), true, false).isEmpty());
        verify(repository, never()).saveAll(anyList());
    }
    @Test public void existingModelsPreventSaving() throws Exception {
        when(items.getItemByModelNo("MODEL-1")).thenReturn(new ItemMaster());
        assertFalse(check(new XSSFWorkbook(), true, false).isEmpty());
        verify(repository, never()).saveAll(anyList());
    }
    @Test public void unreadableFilesAreRejected() {
        assertFalse(upload.validateAndSave(new MockMultipartFile("file", "items.xlsx", "text/plain", new byte[]{1, 2, 3})).isEmpty());
        verify(repository, never()).saveAll(anyList());
    }
}
