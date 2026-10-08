package com.ncpl.sales.cashflow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@org.springframework.web.bind.annotation.RequestMapping("/cashflow-analyzer")
public class DashboardController {
    @GetMapping({"", "/"}) public String entry() { return "redirect:/cashflow-analyzer/overview"; }
    @GetMapping({"/overview", "/receivables", "/payables", "/advances", "/cost-centres", "/aging", "/reconciliation"})
    public String financeWorkspace(Model model, HttpSession session, HttpServletRequest request, Authentication authentication) {
        // The application currently operates in Tally-only mode.
        String selectedMode = (String) session.getAttribute("selectedMode");
        if (!"TALLY".equals(selectedMode)) {
            selectedMode = "TALLY";
            session.setAttribute("selectedMode", selectedMode);
        }
        
        // Pass the selected mode to the view
        model.addAttribute("selectedMode", selectedMode);
        String path = request.getRequestURI();
        String initialSection = path == null || "/overview".equals(path)
                ? "overview" : path.substring(path.lastIndexOf('/') + 1);
        model.addAttribute("initialSection", initialSection);
        String userRole = authentication == null ? "" : authentication.getAuthorities().stream()
                .findFirst().map(authority -> authority.getAuthority()).orElse("");
        model.addAttribute("userRole", "ROLE_ADMIN");
        return "cashflow/overview";
    }
    
    @GetMapping("/finance-cash-plan")
    public String financeCashPlan() {
        return "cashflow/finance-cash-plan";
    }

    @GetMapping("/analytics")
    public String analytics() {
        return "cashflow/analytics";
    }

    @GetMapping("/cashflow-forecast")
    public String cashflowForecast() {
        return "cashflow/cashflow-forecast";
    }
    
    @GetMapping("/due-date-tracker")
    public String dueDateTracker() {
        return "cashflow/due-date-tracker";
    }
    
    @GetMapping("/customer-vendor")
    public String customerVendor() {
        return "cashflow/customer-vendor";
    }
    
    @GetMapping("/aging-analysis")
    public String agingAnalysis() {
        return "redirect:/cashflow-analyzer/aging";
    }
    
    @GetMapping("/top-performers")
    public String topPerformers() {
        return "cashflow/top-performers";
    }
    
    @GetMapping("/debtors-creditors")
    public String debtorsCreditors() {
        return "cashflow/debtors-creditors";
    }
    
    @GetMapping("/recurring-transactions")
    public String recurringTransactions() {
        return "cashflow/recurring-transactions";
    }
}
