package com.ncpl.sales.cashflow.config;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Supplies view mode without leaking it into the aging redirect query string. */
@ControllerAdvice(basePackages = "com.ncpl.sales.cashflow.controller")
public class CashflowStandaloneViewAdvice {
    @Value("${cashflow.standalone:false}")
    private boolean standalone;

    @ModelAttribute
    public void cashflowStandalone(Model model, HttpServletRequest request) {
        if (!request.getRequestURI().endsWith("/aging-analysis")) {
            model.addAttribute("cashflowStandalone", standalone);
        }
    }
}