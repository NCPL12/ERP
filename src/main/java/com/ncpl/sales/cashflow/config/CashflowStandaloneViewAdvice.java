package com.ncpl.sales.cashflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.ncpl.sales.cashflow.controller")
public class CashflowStandaloneViewAdvice {

    @Value("${cashflow.standalone:false}")
    private boolean standalone;

    @ModelAttribute("cashflowStandalone")
    public boolean cashflowStandalone() {
        return standalone;
    }
}

