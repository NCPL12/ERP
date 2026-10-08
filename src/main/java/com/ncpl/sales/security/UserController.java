package com.ncpl.sales.security;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;

@Controller
public class UserController {

    @Value("${cashflow.standalone:false}")
    private boolean cashflowStandalone;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(Model model, String error, User user,
            HttpServletRequest request) throws JsonProcessingException {

        if (error != null) {
            model.addAttribute("error", "Invalid user name or password");
        }

        if (cashflowStandalone) {
            model.addAttribute("selectedMode", "TALLY");
            model.addAttribute("errorMsg", error == null ? null : "Invalid user name or password");
            return "cashflow/login";
        }
        return "login";
    }

    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
