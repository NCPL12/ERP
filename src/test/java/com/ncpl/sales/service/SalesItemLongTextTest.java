package com.ncpl.sales.service;

import com.ncpl.sales.model.SalesItem;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Collections;
import javax.persistence.Column;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class SalesItemLongTextTest {
    @Test void migrationPreservesExistingTextAndStoresLongUnicodeValues() throws Exception {
        for (String field : new String[]{"description", "modelNo"}) {
            assertThat(SalesItem.class.getDeclaredField(field).getAnnotation(Column.class).columnDefinition())
                    .isEqualToIgnoringCase("LONGTEXT");
        }
        try (Connection db = DriverManager.getConnection("jdbc:h2:mem:sales_long_text;MODE=MySQL");
             Statement sql = db.createStatement()) {
            sql.execute("CREATE TABLE tbl_sales_item(description VARCHAR(255), model_no VARCHAR(255))");
            sql.execute("INSERT INTO tbl_sales_item VALUES ('Existing description', 'Existing model')");
            String migration = new String(Files.readAllBytes(Paths.get(
                    "src/main/resources/db-sales-item-long-text.sql")), StandardCharsets.UTF_8);
            for (String statement : migration.split(";")) {
                if (!statement.trim().isEmpty()) sql.execute(statement);
            }
            try (ResultSet row = sql.executeQuery("SELECT * FROM tbl_sales_item")) {
                assertThat(row.next()).isTrue();
                assertThat(row.getString("description")).isEqualTo("Existing description");
                assertThat(row.getString("model_no")).isEqualTo("Existing model");
            }
            String text = String.join("", Collections.nCopies(20000, "Long text \u2713 "));
            try (PreparedStatement insert = db.prepareStatement("INSERT INTO tbl_sales_item VALUES (?, ?)")) {
                insert.setString(1, text);
                insert.setString(2, text);
                insert.executeUpdate();
            }
            try (ResultSet row = sql.executeQuery("SELECT * FROM tbl_sales_item WHERE description <> 'Existing description'")) {
                assertThat(row.next()).isTrue();
                assertThat(row.getString("description")).isEqualTo(text);
                assertThat(row.getString("model_no")).isEqualTo(text);
            }
        }
    }
}
