package com.ncpl.sales.service;

import com.ncpl.sales.model.ItemMaster;
import com.ncpl.sales.model.Make;
import com.ncpl.sales.model.Units;
import com.ncpl.sales.repository.ItemMasterRepo;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemUploadServiceTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void uploadReadsLocationFromColumnFAndKeepsMakeId() throws Exception {
        ItemUploadService upload = new ItemUploadService();
        ItemMasterRepo repository = mock(ItemMasterRepo.class);
        ItemMasterService items = mock(ItemMasterService.class);
        MakeService makes = mock(MakeService.class);
        Make make = new Make();
        make.setId(42);
        make.setName("Acme");
        when(makes.getMakeByNaeme("Acme")).thenReturn(make);
        when(items.getUnitsByName("Nos")).thenReturn(new Units());
        ReflectionTestUtils.setField(upload, "itemRepository", repository);
        ReflectionTestUtils.setField(upload, "itemService", items);
        ReflectionTestUtils.setField(upload, "makeService", makes);

        MockMultipartFile file;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            Row header = sheet.createRow(0);
            String[] headings = {"Model", "Item Name", "HSN", "Unit", "GST", "Location", "Make", "Tool Tracker"};
            for (int column = 0; column < headings.length; column++) {
                header.createCell(column).setCellValue(headings[column]);
            }
            for (int number = 1; number <= 2; number++) {
                Row row = sheet.createRow(number);
                row.createCell(0).setCellValue("MODEL-" + number);
                row.createCell(1).setCellValue("Widget");
                row.createCell(2).setCellValue("8537");
                row.createCell(3).setCellValue("Nos");
                row.createCell(4).setCellValue(18);
                if (number == 1) row.createCell(5).setCellValue("Rack A");
                row.createCell(6).setCellValue("Acme");
                row.createCell(7).setCellValue("Yes");
            }
            workbook.write(bytes);
            file = new MockMultipartFile("file", "items.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes.toByteArray());
        }

        assertThat(upload.validateAndSave(file)).isEmpty();
        ArgumentCaptor<List<ItemMaster>> saved = ArgumentCaptor.forClass((Class) List.class);
        verify(repository).saveAll(saved.capture());
        assertThat(saved.getValue()).extracting(ItemMaster::getModel, ItemMaster::getLocation,
                ItemMaster::getMake, ItemMaster::getHsnCode).containsExactly(
                        tuple("MODEL-1", "Rack A", "42", "8537"),
                        tuple("MODEL-2", "", "42", "8537"));
    }
}
