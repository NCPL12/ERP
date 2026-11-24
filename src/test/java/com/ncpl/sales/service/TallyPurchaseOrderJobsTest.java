package com.ncpl.sales.service;

import com.ncpl.sales.controller.SalesController;
import com.ncpl.sales.model.PurchaseOrder;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TallyPurchaseOrderJobsTest {
    private final TallyPurchaseOrderService exporter=mock(TallyPurchaseOrderService.class);
    private final PurchaseOrderService orders=mock(PurchaseOrderService.class);
    private final PlatformTransactionManager manager=mock(PlatformTransactionManager.class);
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    private final TallyPurchaseOrderJobs jobs=new TallyPurchaseOrderJobs(exporter,orders,manager,executor);
    private final Timestamp from=Timestamp.valueOf("2026-10-01 00:00:00"),to=Timestamp.valueOf("2026-10-25 23:59:59");

    TallyPurchaseOrderJobsTest() {
        when(manager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        when(orders.getPurchaseOrderListByDate(any(),any())).thenReturn(Collections.singletonList(new PurchaseOrder()));
        when(exporter.getBusinessTimeZone()).thenReturn(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    private void completed() throws Exception {
        executor.shutdown();assertThat(executor.awaitTermination(5,TimeUnit.SECONDS)).isTrue();
    }

    @Test void httpReturnsAcceptedWhileTallyIsBlockedAndPollingNeverResends() throws Exception {
        CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
        doAnswer(call -> {
            java.util.function.Consumer<Map<String,Object>> observer=call.getArgument(1);
            observer.accept(new LinkedHashMap<String,Object>() {{put("stage","Reading Tally stock items");put("processed",0);put("total",1);}});
            entered.countDown();assertThat(release.await(5,TimeUnit.SECONDS)).isTrue();
            return new TallyPurchaseOrderService.ExportResult(1,1,0,0,Collections.singletonList(new TallyPurchaseOrderService.ExportLine("PO1","IMPORTED","Verified")));
        }).when(exporter).exportAllPending(anyList(),any(java.util.function.Consumer.class));
        SalesController controller=new SalesController();
        ReflectionTestUtils.setField(controller,"tallyPoJobs",jobs);
        ReflectionTestUtils.setField(controller,"tallyPurchaseOrderService",exporter);
        ReflectionTestUtils.setField(controller,"convertDate",new com.ncpl.sales.util.DateConverterUtil());
        MockMvc mvc=MockMvcBuilders.standaloneSetup(controller).build();
        try {
            mvc.perform(post("/po_list/tally/jobs").param("poListByFromDate","01-10-2026").param("poListByToDate","25-10-2026"))
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.running").value(true));
            assertThat(entered.await(2,TimeUnit.SECONDS)).isTrue();
            String id=jobs.current().get("jobId").toString();
            for(int i=0;i<3;i++) mvc.perform(get("/po_list/tally/jobs/"+id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.progress.stage").value("Reading Tally stock items"));
            mvc.perform(post("/po_list/tally/jobs").param("poListByFromDate","01-10-2026").param("poListByToDate","25-10-2026"))
                .andExpect(status().isConflict());
            mvc.perform(get("/po_list/tally/jobs/missing")).andExpect(status().isNotFound());
            release.countDown();completed();
            mvc.perform(get("/po_list/tally/jobs/current")).andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false)).andExpect(jsonPath("$.result.imported").value(1));
            verify(exporter,times(1)).exportAllPending(anyList(),any(java.util.function.Consumer.class));
            verify(manager).commit(any());
        } finally {release.countDown();jobs.shutdown();}
    }

    @Test void failedJobStopsAndReleasesSlotWithoutAutomaticRetry() throws Exception {
        doThrow(new IllegalStateException("Tally timed out")).when(exporter).exportAllPending(anyList(),any(java.util.function.Consumer.class));
        jobs.start(from,to);completed();
        assertThat(jobs.current()).containsEntry("running",false).containsEntry("error","Tally timed out").containsEntry("stage","Stopped");
        verify(exporter,times(1)).exportAllPending(anyList(),any(java.util.function.Consumer.class));
        verify(manager).rollback(any());
    }

    @Test void invalidDatesNeverQueueWorkAndReadOnlyPollingStartsNothing() throws Exception {
        SalesController controller=new SalesController();
        ReflectionTestUtils.setField(controller,"tallyPoJobs",jobs);
        ReflectionTestUtils.setField(controller,"tallyPurchaseOrderService",exporter);
        ReflectionTestUtils.setField(controller,"convertDate",new com.ncpl.sales.util.DateConverterUtil());
        MockMvc mvc=MockMvcBuilders.standaloneSetup(controller).build();
        try {
            mvc.perform(get("/po_list/tally/jobs/current")).andExpect(status().isOk()).andExpect(content().json("{}"));
            for(String[] dates:new String[][]{{"31-02-2026","01-03-2026"},{"25-10-2026","01-10-2026"}})
                mvc.perform(post("/po_list/tally/jobs").param("poListByFromDate",dates[0]).param("poListByToDate",dates[1])).andExpect(status().isBadRequest());
            verifyZeroInteractions(orders);
        } finally {jobs.shutdown();}
    }
}
