package com.ncpl.sales.cashflow.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TallyLiveServiceAdvanceClassificationTest {

    private static final Map<String, String> GROUPS = com.ncpl.sales.cashflow.util.Java8Collections.map(
            "CUSTOMER SUBGROUP", "SUNDRY DEBTORS",
            "VENDOR SUBGROUP", "SUNDRY CREDITORS",
            "MANGALORE- CLIENTS", "SUNDRY DEBTORS");

    @Test
    void debtorCreditBalanceIsCustomerAdvance() {
        assertEquals(TallyLiveService.AdvanceType.CUSTOMER_ADVANCE_RECEIVED,
                TallyLiveService.classifyAdvance("Customer Subgroup", 1250d, GROUPS));
    }

    @Test
    void boonRelishTypeExcessPaymentIsCustomerAdvanceEvenWithoutTallyAdvanceFlag() {
        assertEquals(TallyLiveService.AdvanceType.CUSTOMER_ADVANCE_RECEIVED,
                TallyLiveService.classifyAdvance("Mangalore- Clients", 5292d, GROUPS));
    }

    @Test
    void creditorDebitBalanceIsSupplierAdvance() {
        assertEquals(TallyLiveService.AdvanceType.SUPPLIER_ADVANCE_PAID,
                TallyLiveService.classifyAdvance("Vendor Subgroup", -850d, GROUPS));
    }

    @Test
    void normalReceivableAndPayableBalancesAreNotAdvances() {
        assertNull(TallyLiveService.classifyAdvance("Sundry Debtors", -1250d, GROUPS));
        assertNull(TallyLiveService.classifyAdvance("Sundry Creditors", 850d, GROUPS));
    }

    @Test
    void zeroBalanceIsNotAnAdvance() {
        assertNull(TallyLiveService.classifyAdvance("Sundry Debtors", 0d, GROUPS));
        assertNull(TallyLiveService.classifyAdvance("Sundry Creditors", 0d, GROUPS));
    }
}
