package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.InvoiceItem;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.repository.InvoiceRepo;

@Service
public class InvoiceService {
	@Autowired
	InvoiceRepo invoiceRepo;
	@Autowired
	SalesService salesService;
	@Autowired
	InvCopyService invCopyService;
	
	/**
	 * save invoice
	 * @param invoice
	 * @return invoiceObj
	 */
	public Invoice saveInvoice(Invoice invoice) {
		Invoice invoiceObj=invoiceRepo.save(invoice);
		return invoiceObj;
	}
	
	/**
	 * get all the invoice
	 * @return invoiceList
	 */
	public List<Invoice> getInvoiceList() {
		List<Invoice> invoiceList=invoiceRepo.findAll();
		for (Invoice invoice : invoiceList) {
			String soNumber=invoice.getSoNumber();
			boolean isInvGenerated=invCopyService.isInvGenerated(invoice.getInvoiceId());
			Optional<SalesOrder> salesOrder=salesService.getSalesOrderById(soNumber);
			invoice.set("clientPoNumber",salesOrder.get().getClientPoNumber());
			invoice.set("clientName",salesOrder.get().getParty().getPartyName());
			invoice.set("invCopyCreated", isInvGenerated);
			invoice.set("invNo",invoice.getInvoiceId().replaceAll("-", "") );
			
		}
		return invoiceList;
	}
	
	/**
	 * get invoice by invoice id
	 * @param invoiceId
	 * @return invoiceObj
	 */
	public Optional<Invoice> getInvoiceById(String invoiceId) {
		Optional<Invoice> invoiceObj=invoiceRepo.findById(invoiceId);
		String soNumber=invoiceObj.get().getSoNumber();
		Optional<SalesOrder> salesOrder=salesService.getSalesOrderById(soNumber);
		invoiceObj.get().set("clientPoNumber",salesOrder.get().getClientPoNumber());
		return invoiceObj;
	}
	
	/**
	 * get all the invoice item by inovice id
	 * @param invoiceId
	 * @return itemList
	 */
	public List<InvoiceItem> getInvoiceItemList(String invoiceId) {
			List<Invoice> invoiceList = invoiceRepo.findInvoiceListById(invoiceId);
			ArrayList<InvoiceItem> itemList = new ArrayList<InvoiceItem>();
			for (Invoice invoice : invoiceList) {
				List<InvoiceItem> invoiceItemList=invoice.getItems();
				
				itemList.addAll(invoiceItemList);
			}
			return itemList;
		
	}
	
	public List<InvoiceItem> getInvoiceItemListById(String invoiceId) {
		List<Invoice> invoiceList = invoiceRepo.findInvoiceListById(invoiceId);
		ArrayList<InvoiceItem> itemList = new ArrayList<InvoiceItem>();
		for (Invoice invoice : invoiceList) {
			List<InvoiceItem> invoiceItemList=invoice.getItems();
			for (InvoiceItem invoiceItem : invoiceItemList) {
				if(invoiceItem.getQuantity()>0) {
					itemList.add(invoiceItem);
				}
			}
			
		}
		return itemList;
	
	}
	
	public List<Invoice> getInvoiceBySoId(String soId) {
		List<Invoice> invoiceList = invoiceRepo.findInvoiceBySoId(soId);
		for (Invoice invoice : invoiceList) {
			String soNumber=invoice.getSoNumber();
			Optional<SalesOrder> salesOrder=salesService.getSalesOrderById(soNumber);
			
			String clientPo = salesOrder.get().getClientPoNumber();
			clientPo = clientPo.replace("'", "&");
			clientPo = clientPo.replace("\"", "&");
			invoice.set("clientPoNumber",clientPo);
			String partyName = salesOrder.get().getParty().getPartyName();
			partyName = partyName.replace("\"", "&");
			partyName = partyName.replace("'", "&");
			invoice.set("clientName",partyName);
		}
		return invoiceList;
	
	}
	
	public boolean updateInvoice(String invoiceNo,String paymentMode,String paymentRemarks,String transactionNumber) {
		Optional<Invoice> invoice = invoiceRepo.findById(invoiceNo);
		invoice.get().setPaymentMode(paymentMode);
		invoice.get().setPaymentRemarks(paymentRemarks);
		invoice.get().setPaymentStatus("success");
		invoice.get().setTransactionNumber(transactionNumber);
		invoiceRepo.save(invoice.get());
		return true;
		
	}

	public List<Invoice> getInvoiceListPartial() {
		List<Invoice> invoiceList=invoiceRepo.findAll();
		ArrayList<Invoice> list=new ArrayList<Invoice>();
		if(invoiceList.size()>11) {
			for (int i=invoiceList.size()-1;i>invoiceList.size()-11;i--) {
				String soNumber=invoiceList.get(i).getSoNumber();
				boolean isInvGenerated=invCopyService.isInvGenerated(invoiceList.get(i).getInvoiceId());
				Optional<SalesOrder> salesOrder=salesService.getSalesOrderById(soNumber);
				invoiceList.get(i).set("clientPoNumber",salesOrder.get().getClientPoNumber());
				invoiceList.get(i).set("clientName",salesOrder.get().getParty().getPartyName());
				invoiceList.get(i).set("invCopyCreated", isInvGenerated);
				invoiceList.get(i).set("invNo",invoiceList.get(i).getInvoiceId().replaceAll("-", "") );
				list.add(invoiceList.get(i));
			}
		}else {
			list.addAll(invoiceList);
		}
		return list;
	}
	

}
