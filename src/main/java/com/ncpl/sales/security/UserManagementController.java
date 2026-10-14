package com.ncpl.sales.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Superseded by the authorized controller in com.ncpl.sales.controller.
public class UserManagementController {

    @GetMapping("/user-management")
    public String userManagement() {
        return "userManagement";
    }
}