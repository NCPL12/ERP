package com.ncpl.sales.service;
import org.junit.jupiter.api.Test;
import com.ncpl.sales.model.PurchaseItem;
import java.math.BigDecimal;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
class StockReportMoneyTest {
 @Test void preservesHalfCentTransactionRounding(){assertThat(StockReportMoney.amount(3f,new BigDecimal("0.335"))).isEqualByComparingTo("1.01");}
 @Test void avoidsFloatingPointAverageDrift(){PurchaseItem a=new PurchaseItem();a.setUnitPrice(426.20f);PurchaseItem b=new PurchaseItem();b.setUnitPrice(426.21f);assertThat(StockReportMoney.average(Arrays.asList(a,b))).isEqualByComparingTo("426.21");}
 @Test void preservesFractionalAndReturnQuantities(){assertThat(StockReportMoney.amount(0.125f,new BigDecimal("100"))).isEqualByComparingTo("12.50");assertThat(StockReportMoney.amount(-2f,new BigDecimal("10.55"))).isEqualByComparingTo("-21.10");}
}
