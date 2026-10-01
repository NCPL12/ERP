package com.ncpl.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserManagementController {

    @GetMapping("/user-management")
    public String userManagement() {
        return "userManagement";
    }
}
