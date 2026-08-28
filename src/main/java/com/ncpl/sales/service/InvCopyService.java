package com.ncpl.sales.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.InvoiceCopy;
import com.ncpl.sales.repository.InvoiceCopyRepo;

@Service
public class InvCopyService {
	@Autowired
	InvoiceCopyRepo invCopyRepo;

	public boolean isInvGenerated(String invId) {
		// TODO Auto-generated method stub
		boolean result = false;
		InvoiceCopy invCopy = invCopyRepo.getInvCopyByInvNo(invId);
		if(invCopy!=null) {
			result =true;
		}
		return result;
	}

	
	
	
}
