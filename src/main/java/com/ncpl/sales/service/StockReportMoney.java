package com.ncpl.sales.service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import com.ncpl.sales.model.PurchaseItem;
/** Decimal stock-report valuation: six-decimal source precision, two-decimal money. */
public final class StockReportMoney {
 private StockReportMoney() {}
 public static BigDecimal source(float value) { return new BigDecimal(Float.toString(value)).setScale(6, RoundingMode.HALF_UP); }
 public static BigDecimal average(List<PurchaseItem> items) {
  BigDecimal sum=BigDecimal.ZERO;
  for(PurchaseItem item:items) sum=sum.add(source(item.getUnitPrice()));
  return items.isEmpty()?BigDecimal.ZERO:sum.divide(BigDecimal.valueOf(items.size()),2,RoundingMode.HALF_UP);
 }
 public static BigDecimal amount(float quantity, Number rate) {
  return source(quantity).multiply(new BigDecimal(rate.toString())).setScale(2,RoundingMode.HALF_UP);
 }
}
