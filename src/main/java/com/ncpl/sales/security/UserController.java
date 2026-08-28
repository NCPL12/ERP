package com.ncpl.sales.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.core.JsonProcessingException;


@Controller
public class UserController {
	
	@GetMapping("/login")
	public String login(Model model, String error, User user, HttpServletRequest request) throws JsonProcessingException {
		
		if(error !=null) {
			model.addAttribute("error", "Invalid user name or password");
		}
		return "login";

  }
	  
}
