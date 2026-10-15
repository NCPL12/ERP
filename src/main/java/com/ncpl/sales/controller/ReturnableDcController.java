package com.ncpl.sales.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ncpl.sales.service.ReturnableDcService;

@Controller
public class ReturnableDcController {

	@Autowired
	private ReturnableDcService returnableDcService;

	@GetMapping({"/returnableDcList", "/returnableDCList"})
	public String returnableDcList(Model model) {
		model.addAttribute("returnableDcList", returnableDcService.getAll());
		return "returnableDCList"; // must match the definition name in tiles.xml
	}

	// Bean used by returnableDCList.jsp (public static so JSP/EL can read it)
	public static class ReturnableDc {

		private Long id;
		private String returnableDcNo;
		private String dcNo;
		private String clientName;
		private String shippingAddress;
		private String date;

		public Long getId() { return id; }
		public void setId(Long id) { this.id = id; }

		public String getReturnableDcNo() { return returnableDcNo; }
		public void setReturnableDcNo(String returnableDcNo) { this.returnableDcNo = returnableDcNo; }

		public String getDcNo() { return dcNo; }
		public void setDcNo(String dcNo) { this.dcNo = dcNo; }

		public String getClientName() { return clientName; }
		public void setClientName(String clientName) { this.clientName = clientName; }

		public String getShippingAddress() { return shippingAddress; }
		public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

		public String getDate() { return date; }
		public void setDate(String date) { this.date = date; }
	}
}