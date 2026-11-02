package com.ncpl.sales.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// The login route is handled by com.ncpl.sales.security.UserController.
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
