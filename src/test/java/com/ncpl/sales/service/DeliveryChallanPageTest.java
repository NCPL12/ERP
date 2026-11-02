package com.ncpl.sales.service;

import com.ncpl.sales.model.DeliveryChallan;
import com.ncpl.sales.model.DeliveryChallanItems;
import com.ncpl.sales.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DeliveryChallanPageTest {
    private final DeliveryChallanService service = new DeliveryChallanService();

    DeliveryChallanPageTest() {
        service.dcRepo = mock(DeliveryChallanRepo.class);
        service.salesRepo = mock(SalesRepo.class);
        service.partyRepo = mock(PartyRepo.class);
        service.partyAddressRepo = mock(PartyAddressRepo.class);
        service.dcItemRepo = mock(DeliveryChallanItemsRepo.class);
        service.salesService = mock(SalesService.class);
    }

    @Test void batchesRepeatedSalesOrdersAndNeverLoadsDcItemCollections() {
        Pageable request = PageRequest.of(0, 25);
        DeliveryChallan first = dc(11), second = dc(12);
        when(service.dcRepo.findByArchive(false, request))
                .thenReturn(new PageImpl<>(Arrays.asList(first, second), request, 2));
        when(service.salesRepo.findDcListHeaders(Collections.singletonList("SO1")))
                .thenReturn(Collections.singletonList(new Object[]{"SO1", "PO1", "Client", "ADDR1"}));
        when(service.partyRepo.findDcShippingAddresses(Collections.singletonList("ADDR1")))
                .thenReturn(Collections.emptyList());
        when(service.partyAddressRepo.findDcShippingAddresses(Collections.singletonList("ADDR1")))
                .thenReturn(Collections.singletonList(new Object[]{"ADDR1", "Shipping address"}));
        Date date = new Date(1000);
        when(service.dcItemRepo.findFirstCreatedDates(Arrays.asList(11, 12)))
                .thenReturn(Arrays.asList(new Object[]{11, date}, new Object[]{12, date}));

        Page<DeliveryChallan> result = service.getDeliveryChallanPage(request, "", false);

        assertThat(result.getTotalElements()).isEqualTo(2);
        for (DeliveryChallan row : result) {
            assertThat(row.get("clientName")).isEqualTo("Client");
            assertThat(row.get("clientPoNumber")).isEqualTo("PO1");
            assertThat(row.get("shippingAddress")).isEqualTo("Shipping address");
            assertThat(row.get("createdDate")).isEqualTo(date);
        }
        verify(service.salesRepo).findDcListHeaders(Collections.singletonList("SO1"));
        verify(service.partyRepo).findDcShippingAddresses(Collections.singletonList("ADDR1"));
        verify(service.partyAddressRepo).findDcShippingAddresses(Collections.singletonList("ADDR1"));
        verify(service.dcItemRepo).findFirstCreatedDates(Arrays.asList(11, 12));
        verifyNoMoreInteractions(service.salesRepo, service.partyRepo, service.partyAddressRepo, service.dcItemRepo);
        verifyNoInteractions(service.salesService);
    }

    @Test void emptyPagesDoNotRunEnrichmentQueries() {
        Pageable request = PageRequest.of(0, 25);
        when(service.dcRepo.findByArchive(true, request)).thenReturn(Page.empty(request));
        assertThat(service.getDeliveryChallanPage(request, "", true)).isEmpty();
        verifyNoInteractions(service.salesRepo, service.partyRepo, service.partyAddressRepo, service.dcItemRepo);
    }

    private DeliveryChallan dc(int id) {
        DeliveryChallan dc = new DeliveryChallan() {
            @Override public List<DeliveryChallanItems> getItems() {
                throw new AssertionError("Dashboard must not load DC item collections");
            }
        };
        dc.setDcId(id);
        dc.setSoNumber("SO1");
        return dc;
    }
}
