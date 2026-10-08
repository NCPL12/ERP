package com.ncpl.sales.service;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.concurrent.*;
class TallySyncCoordinatorIntegrationTest {
 @Test void poPushWaitsForCashflowAndCanProceedAfterRefresh() throws Exception {
  com.ncpl.sales.cashflow.service.TallyLiveService live=mock(com.ncpl.sales.cashflow.service.TallyLiveService.class);
  com.ncpl.sales.cashflow.repository.InvoiceRepository invoices=mock(com.ncpl.sales.cashflow.repository.InvoiceRepository.class);
  com.ncpl.sales.cashflow.repository.TallySyncStatusRepository statuses=mock(com.ncpl.sales.cashflow.repository.TallySyncStatusRepository.class);
  com.ncpl.sales.cashflow.service.TallySyncService cashflow=new com.ncpl.sales.cashflow.service.TallySyncService(live,invoices,statuses);
  java.lang.reflect.Field enabled=cashflow.getClass().getDeclaredField("syncEnabled");enabled.setAccessible(true);enabled.set(cashflow,true);
  CountDownLatch entered=new CountDownLatch(1);CountDownLatch finish=new CountDownLatch(1);
  when(live.fetchOutstandingBills()).thenAnswer(call->{entered.countDown();finish.await(5,TimeUnit.SECONDS);return java.util.Collections.emptyList();});
  ExecutorService executor=Executors.newSingleThreadExecutor();
  try {
   Future<?> refresh=executor.submit(()->cashflow.syncTallyInvoices());
   assertThat(entered.await(5,TimeUnit.SECONDS)).isTrue();
   TallyPurchaseOrderService po=new TallyPurchaseOrderService(null);
   try {assertThatThrownBy(()->po.exportAllPending(java.util.Collections.emptyList())).hasMessageContaining("Tally refresh is running");}
   finally {finish.countDown();}
   refresh.get(5,TimeUnit.SECONDS);
   assertThat(po.isExportRunning()).isFalse();
   assertThat(po.exportAllPending(java.util.Collections.emptyList()).getTotal()).isZero();
   verify(live).fetchOutstandingBills();
  } finally {finish.countDown();executor.shutdownNow();}
 }
}

