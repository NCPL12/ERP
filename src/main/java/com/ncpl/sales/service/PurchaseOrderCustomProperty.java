package com.ncpl.sales.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PurchaseOrderCustomProperty {
	
	
	@Value("${company.address}")
	private String companyAddress;
	
	@Value("${shipping.address}")
	private String shippingAddress;
	
	@Value("${billing.address}")
	private String billingAddress;
	
	@Value("${purchase.modeofpayment}")
	private String modeOfPayment;
	
	@Value("${purchase.jurisdiction}")
	private String jursidiction;
	
	@Value("${purchase.freight}")
	private String frieght;
	
	@Value("${purchase.delivery}")
	private String delivery;
	
	@Value("${purchase.warranty}")
	private String warranty;
	

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public String getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(String billingAddress) {
		this.billingAddress = billingAddress;
	}

	public String getModeOfPayment() {
		return modeOfPayment;
	}

	public void setModeOfPayment(String modeOfPayment) {
		this.modeOfPayment = modeOfPayment;
	}

	public String getJursidiction() {
		return jursidiction;
	}

	public void setJursidiction(String jursidiction) {
		this.jursidiction = jursidiction;
	}

	public String getFrieght() {
		return frieght;
	}

	public void setFrieght(String frieght) {
		this.frieght = frieght;
	}

	public String getDelivery() {
		return delivery;
	}

	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}

	public String getWarranty() {
		return warranty;
	}

	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}

	public String getCompanyAddress() {
		return companyAddress;
	}

	public void setCompanyAddress(String companyAddress) {
		this.companyAddress = companyAddress;
	}
	
	
	
}
