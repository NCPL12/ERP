package com.ncpl.sales.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserManagementController {

    @GetMapping("/user-management")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER ADMIN','USER_MANAGEMENT_VIEW','USER_MANAGEMENT_EDIT')")
    public String userManagement() {
        return "userManagement";
    }
}
